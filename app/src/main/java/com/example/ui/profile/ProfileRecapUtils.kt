package com.example.ui.profile

import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.MateriEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity

/**
 * Ringkasan progres sub-materi per Materi utama.
 */
data class MateriProgressSummary(
    val materiId: Int,
    val judulMateri: String,
    val urutan: Int,
    val completedSubMateri: Int,
    val totalSubMateri: Int,
    val isCompleted: Boolean
)

/**
 * Rekap evaluasi: skor pretest, rata-rata kuis, posttest, dan nilai capstone.
 */
data class AssessmentRecapSummary(
    val pretestScore: Int?,
    val posttestScore: Int?,
    val quizAverage: Int?,
    val quizzesCount: Int,
    val capstoneScore: Int?,
    val capstoneStatus: String // "belum_dikerjakan", "menunggu_review", "dinilai"
)

/**
 * Rincian nilai Post-Test per materi/evaluasi untuk Rekap Skor Profil.
 */
data class PosttestDetailEntry(
    val assessmentId: Int,
    val materiId: Int,
    val materiTitle: String,
    val latestScore: Int?,
    val bestScore: Int?,
    val attemptsCount: Int,
    val isPassed: Boolean,
    val lastSubmittedAt: Long?
)

data class PosttestRecapSummary(
    val latestEntry: PosttestDetailEntry?,
    val entries: List<PosttestDetailEntry>
)

object ProfileRecapUtils {

    /**
     * Menghitung progres penyelesaian Sub Materi untuk setiap Materi utama.
     */
    fun calculateMateriProgressList(
        materis: List<MateriEntity>,
        modules: List<ModuleEntity>,
        progressList: List<ModuleProgressEntity>
    ): List<MateriProgressSummary> {
        val completedModuleIds = progressList.filter { it.isCompleted }.map { it.moduleId }.toSet()

        return materis.sortedBy { it.urutan }.map { materi ->
            val modulesForMateri = modules.filter { it.materiId == materi.id }
            val total = modulesForMateri.size
            val completed = modulesForMateri.count { it.id in completedModuleIds }
            val isDone = total > 0 && completed == total

            MateriProgressSummary(
                materiId = materi.id,
                judulMateri = materi.judul,
                urutan = materi.urutan,
                completedSubMateri = completed,
                totalSubMateri = total,
                isCompleted = isDone
            )
        }
    }

    /**
     * Menghitung rekap skor Pretest, Kuis (rata-rata), Posttest, dan Capstone.
     */
    fun calculateAssessmentRecap(
        attempts: List<AssessmentAttemptEntity>,
        capstone: CapstoneSubmissionEntity? = null,
        assessments: List<AssessmentEntity> = emptyList()
    ): AssessmentRecapSummary {
        val pretestIds = assessments.filter { it.tipe == "pretest" }.map { it.id }.ifEmpty { listOf(10) }
        val posttestIds = assessments.filter { it.tipe == "posttest" }.map { it.id }.ifEmpty { listOf(20) }

        // Pretest: ambil skor attempt
        val pretest = attempts.firstOrNull { it.assessmentId in pretestIds }?.score

        // Posttest: ambil skor attempt
        val posttest = attempts.firstOrNull { it.assessmentId in posttestIds }?.score

        // Kuis
        val quizAttempts = if (assessments.isNotEmpty()) {
            val quizIds = assessments.filter { it.tipe == "quiz" }.map { it.id }.toSet()
            attempts.filter { it.assessmentId in quizIds }
        } else {
            attempts.filter { it.assessmentId !in pretestIds && it.assessmentId !in posttestIds }
        }

        val quizAverage = if (quizAttempts.isNotEmpty()) {
            quizAttempts.map { it.score }.average().toInt()
        } else {
            null
        }

        // Capstone status & score
        val capstoneScore = capstone?.nilai
        val capstoneStatus = when {
            capstone == null -> "belum_dikerjakan"
            capstone.nilai != null || capstone.status == "reviewed" -> "dinilai"
            else -> "menunggu_review"
        }

        return AssessmentRecapSummary(
            pretestScore = pretest,
            posttestScore = posttest,
            quizAverage = quizAverage,
            quizzesCount = quizAttempts.size,
            capstoneScore = capstoneScore,
            capstoneStatus = capstoneStatus
        )
    }

    /**
     * Rekap skor Post-Test khusus Profil:
     * - latestEntry: skor terakhir perolehan Post-Test (ditampilkan di kartu utama)
     * - entries: daftar lengkap nilai Post-Test per materi (ditampilkan saat kartu diklik)
     */
    fun calculatePosttestRecap(
        attempts: List<AssessmentAttemptEntity>,
        assessments: List<AssessmentEntity> = emptyList(),
        materis: List<MateriEntity> = emptyList()
    ): PosttestRecapSummary {
        val posttestAssessments = assessments.filter { it.tipe == "posttest" }
        val posttestIds = if (posttestAssessments.isNotEmpty()) {
            posttestAssessments.map { it.id }.toSet()
        } else {
            setOf(20, 21, 22, 23, 24, 25)
        }
        val posttestAttempts = attempts.filter { it.assessmentId in posttestIds }
        val materiById = materis.associateBy { it.id }

        val entries = if (posttestAssessments.isNotEmpty()) {
            posttestAssessments
                .sortedBy { materiById[it.materiId]?.urutan ?: it.materiId }
                .map { assessment ->
                    val matchingAttempts = posttestAttempts
                        .filter { it.assessmentId == assessment.id }
                        .sortedWith(compareByDescending<AssessmentAttemptEntity> { it.submittedAt }.thenByDescending { it.id })
                    val latestAttempt = matchingAttempts.firstOrNull()
                    val bestAttempt = matchingAttempts.maxByOrNull { it.score }
                    val materi = materiById[assessment.materiId]
                    val title = materi?.let { "Materi ${it.urutan}: ${it.judul}" } ?: assessment.judul
                    PosttestDetailEntry(
                        assessmentId = assessment.id,
                        materiId = assessment.materiId,
                        materiTitle = title,
                        latestScore = latestAttempt?.score,
                        bestScore = bestAttempt?.score,
                        attemptsCount = matchingAttempts.size,
                        isPassed = matchingAttempts.any { it.isPassed },
                        lastSubmittedAt = latestAttempt?.submittedAt
                    )
                }
        } else {
            posttestAttempts
                .groupBy { it.assessmentId }
                .entries
                .sortedBy { it.key }
                .map { (assessmentId, matchingAttempts) ->
                    val sorted = matchingAttempts.sortedWith(
                        compareByDescending<AssessmentAttemptEntity> { it.submittedAt }.thenByDescending { it.id }
                    )
                    val latestAttempt = sorted.first()
                    val bestAttempt = sorted.maxBy { it.score }
                    PosttestDetailEntry(
                        assessmentId = assessmentId,
                        materiId = assessmentId,
                        materiTitle = "Post-Test #$assessmentId",
                        latestScore = latestAttempt.score,
                        bestScore = bestAttempt.score,
                        attemptsCount = sorted.size,
                        isPassed = sorted.any { it.isPassed },
                        lastSubmittedAt = latestAttempt.submittedAt
                    )
                }
        }

        val latestEntry = entries
            .filter { it.latestScore != null }
            .maxByOrNull { it.lastSubmittedAt ?: 0L }

        return PosttestRecapSummary(
            latestEntry = latestEntry,
            entries = entries
        )
    }
}
