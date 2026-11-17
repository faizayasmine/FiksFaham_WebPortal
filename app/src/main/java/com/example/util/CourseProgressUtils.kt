package com.example.util

import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.MateriEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity

/**
 * Logika terpusat untuk aturan akses Materi, Kuis, Posttest, dan Capstone Akhir Tunggal.
 * Sesuai PRD:
 * - Materi dan video selalu terbuka (tidak ada kunci berjenjang antar materi).
 * - Kuis Sub Materi & Posttest terkunci hingga sub materi selesai (semua halaman materi & video dibuka + tekan Selesai).
 * - Satu Capstone Akhir (materiId = 0) aktif setelah seluruh 5 Materi & Posttest lulus (>= 75).
 */
object CourseProgressUtils {

    const val FINAL_CAPSTONE_MATERI_ID = 0

    @Suppress("UNUSED_PARAMETER")
    fun isMateriUnlocked(
        materiId: Int,
        materis: List<MateriEntity>,
        assessments: List<AssessmentEntity>,
        attempts: List<AssessmentAttemptEntity>
    ): Boolean = true

    fun isQuizUnlocked(progress: ModuleProgressEntity?): Boolean {
        return progress?.isFinished == true || progress?.isCompleted == true
    }

    fun isPosttestUnlocked(
        materiId: Int,
        modules: List<ModuleEntity>,
        progressList: List<ModuleProgressEntity>
    ): Boolean {
        val subModules = modules.filter { it.materiId == materiId }
        if (subModules.isEmpty()) return false
        return subModules.all { sub ->
            val progress = progressList.firstOrNull { it.moduleId == sub.id }
            progress?.isFinished == true || progress?.isCompleted == true
        }
    }

    fun getPosttestAssessmentId(materiId: Int): Int {
        return if (materiId == 1) 20 else materiId * 100 + 20
    }

    fun isPosttestPassed(
        materiId: Int,
        assessments: List<AssessmentEntity>,
        attempts: List<AssessmentAttemptEntity>
    ): Boolean {
        val posttest = assessments.firstOrNull { it.materiId == materiId && it.tipe == "posttest" }
        val posttestId = posttest?.id ?: getPosttestAssessmentId(materiId)
        return attempts.any { it.assessmentId == posttestId && (it.isPassed || it.score >= ScoreColorUtils.PASS_THRESHOLD) }
    }

    /**
     * Capstone Akhir Tunggal terbuka bila seluruh Materi (5 materi) telah selesai dan lulus Posttest.
     */
    fun isFinalCapstoneUnlocked(
        materis: List<MateriEntity>,
        modules: List<ModuleEntity>,
        progressList: List<ModuleProgressEntity>,
        assessments: List<AssessmentEntity>,
        attempts: List<AssessmentAttemptEntity>
    ): Boolean {
        if (materis.isEmpty()) return false
        return materis.all { materi ->
            isMateriCompleted(materi.id, modules, progressList, assessments, attempts)
        }
    }

    /**
     * Kompatibilitas dengan pengecekan berbasis materiId:
     * Untuk materiId == 0 memeriksa kelulusan seluruh materi; untuk materi spesifik memeriksa posttest materi tsb.
     */
    fun isCapstoneUnlocked(
        materiId: Int,
        assessments: List<AssessmentEntity>,
        attempts: List<AssessmentAttemptEntity>,
        materis: List<MateriEntity> = emptyList(),
        modules: List<ModuleEntity> = emptyList(),
        progressList: List<ModuleProgressEntity> = emptyList()
    ): Boolean {
        if (materiId == FINAL_CAPSTONE_MATERI_ID) {
            if (materis.isNotEmpty() && modules.isNotEmpty()) {
                return isFinalCapstoneUnlocked(materis, modules, progressList, assessments, attempts)
            }
            val targetIds = listOf(1, 2, 3, 4, 5)
            return targetIds.all { id -> isPosttestPassed(id, assessments, attempts) }
        }
        return isPosttestPassed(materiId, assessments, attempts)
    }

    /**
     * Syarat status "Selesai" pada Kartu Materi:
     * Menampilkan status "Selesai" bila semua Sub Materi selesai dan Posttest lulus.
     */
    fun isMateriCompleted(
        materiId: Int,
        modules: List<ModuleEntity>,
        progressList: List<ModuleProgressEntity>,
        assessments: List<AssessmentEntity>,
        attempts: List<AssessmentAttemptEntity>
    ): Boolean {
        val subModules = modules.filter { it.materiId == materiId }
        if (subModules.isEmpty()) return false
        val allSubFinished = subModules.all { sub ->
            progressList.any { it.moduleId == sub.id && (it.isFinished || it.isCompleted) }
        }
        return allSubFinished && isPosttestPassed(materiId, assessments, attempts)
    }
}
