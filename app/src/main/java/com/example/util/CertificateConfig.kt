package com.example.util

import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.MateriEntity
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Konfigurasi Kelayakan Penerbitan Sertifikat Kelulusan Tunggal FiksFaham (sesuai PRD):
 * 1. Seluruh Posttest pada 5 Materi harus lulus (skor >= 75).
 * 2. Satu Capstone Akhir (materiId = 0) harus disetujui mentor (status "approved" / "reviewed") dengan nilai >= 75.
 * 3. Memiliki nomor sertifikat resmi yang dapat diverifikasi pada koleksi Firestore certificates/{certNumber}.
 */
object CertificateConfig {
    const val POSTTEST_PASS_THRESHOLD = 75
    const val CAPSTONE_PASS_THRESHOLD = 75
    const val FINAL_CAPSTONE_MATERI_ID = 0

    sealed class EligibilityStatus {
        data class Eligible(
            val posttestScore: Int,
            val capstoneScore: Int
        ) : EligibilityStatus()

        data class Locked(
            val reasonKey: ReasonKey,
            val currentCapstoneScore: Int? = null
        ) : EligibilityStatus()
    }

    enum class ReasonKey {
        POSTTEST_NOT_PASSED,
        ALL_POSTTESTS_NOT_PASSED,
        CAPSTONE_NOT_SUBMITTED,
        CAPSTONE_UNDER_REVIEW,
        CAPSTONE_REVISION_NEEDED,
        CAPSTONE_REJECTED,
        CAPSTONE_SCORE_LOW
    }

    fun calculatePredikat(capstoneScore: Int): String = when {
        capstoneScore >= 95 -> "Unggul"
        capstoneScore >= 85 -> "Mahir"
        capstoneScore >= 75 -> "Cakap"
        else -> ""
    }

    data class CertificateItem(
        val materiId: Int,
        val materiTitle: String,
        val status: EligibilityStatus
    )

    data class GraduationCertificateEligibility(
        val status: EligibilityStatus,
        val passedPosttestCount: Int,
        val totalMateriCount: Int,
        val averagePosttestScore: Int,
        val capstoneScore: Int?
    )

    private fun findBestPosttestAttempt(
        materiId: Int,
        allAttempts: List<AssessmentAttemptEntity>
    ): AssessmentAttemptEntity? {
        return allAttempts
            .filter { attempt ->
                val matchesMateri1 = materiId == 1 && (attempt.assessmentId == 20 || attempt.assessmentId == 10020)
                val matchesOther = attempt.assessmentId == (materiId * 100 + 20) || attempt.assessmentId == (materiId * 10 + 2)
                matchesMateri1 || matchesOther
            }
            .maxWithOrNull(compareBy<AssessmentAttemptEntity> { it.isPassed }.thenBy { it.score }.thenBy { it.submittedAt })
    }

    /**
     * Evaluasi kelulusan sertifikat tunggal akhir program (5 Posttest + 1 Capstone Akhir).
     */
    fun evaluateGraduationEligibility(
        materis: List<MateriEntity>,
        allAttempts: List<AssessmentAttemptEntity>,
        capstoneSubmissions: List<CapstoneSubmissionEntity>
    ): GraduationCertificateEligibility {
        val targetMateriIds = if (materis.isNotEmpty()) materis.map { it.id } else listOf(1, 2, 3, 4, 5)
        val posttestAttempts = targetMateriIds.map { id -> id to findBestPosttestAttempt(id, allAttempts) }
        val passedPosttests = posttestAttempts.count { (_, att) ->
            att != null && (att.isPassed || att.score >= POSTTEST_PASS_THRESHOLD)
        }
        val avgPosttest = if (posttestAttempts.isNotEmpty()) {
            posttestAttempts.map { it.second?.score ?: 0 }.average().toInt()
        } else 0

        if (passedPosttests < targetMateriIds.size) {
            return GraduationCertificateEligibility(
                status = EligibilityStatus.Locked(ReasonKey.ALL_POSTTESTS_NOT_PASSED),
                passedPosttestCount = passedPosttests,
                totalMateriCount = targetMateriIds.size,
                averagePosttestScore = avgPosttest,
                capstoneScore = null
            )
        }

        val finalCapstone = capstoneSubmissions.firstOrNull { it.materiId == FINAL_CAPSTONE_MATERI_ID }
            ?: capstoneSubmissions.firstOrNull()

        val lockedReason = checkCapstoneEligibilityLocked(finalCapstone)
        if (lockedReason != null) {
            return GraduationCertificateEligibility(
                status = lockedReason,
                passedPosttestCount = passedPosttests,
                totalMateriCount = targetMateriIds.size,
                averagePosttestScore = avgPosttest,
                capstoneScore = finalCapstone?.nilai
            )
        }

        return GraduationCertificateEligibility(
            status = EligibilityStatus.Eligible(
                posttestScore = avgPosttest,
                capstoneScore = finalCapstone!!.nilai!!
            ),
            passedPosttestCount = passedPosttests,
            totalMateriCount = targetMateriIds.size,
            averagePosttestScore = avgPosttest,
            capstoneScore = finalCapstone.nilai
        )
    }

    private fun checkCapstoneEligibilityLocked(capstone: CapstoneSubmissionEntity?): EligibilityStatus.Locked? {
        if (capstone == null || capstone.status == "not_started") {
            return EligibilityStatus.Locked(ReasonKey.CAPSTONE_NOT_SUBMITTED)
        }
        val normalizedStatus = com.example.CapstoneStatusUtils.normalize(capstone.status)
        return when {
            normalizedStatus == "rejected" -> EligibilityStatus.Locked(
                ReasonKey.CAPSTONE_REJECTED,
                currentCapstoneScore = capstone.nilai
            )
            normalizedStatus == "revision_needed" -> EligibilityStatus.Locked(
                ReasonKey.CAPSTONE_REVISION_NEEDED,
                currentCapstoneScore = capstone.nilai
            )
            normalizedStatus != "approved" || capstone.nilai == null -> EligibilityStatus.Locked(
                ReasonKey.CAPSTONE_UNDER_REVIEW,
                currentCapstoneScore = capstone.nilai
            )
            capstone.nilai < CAPSTONE_PASS_THRESHOLD -> EligibilityStatus.Locked(
                ReasonKey.CAPSTONE_SCORE_LOW,
                currentCapstoneScore = capstone.nilai
            )
            else -> null
        }
    }

    /**
     * Menghasilkan nomor sertifikat deterministik & dapat diverifikasi.
     * Contoh: FF-DSGN-2026-8F2A19C4
     */
    fun generateCertificateNumber(uid: String, issuedAtMillis: Long = System.currentTimeMillis()): String {
        val year = SimpleDateFormat("yyyy", Locale.US).apply {
            timeZone = LeaderboardUtils.WIB_TIMEZONE
        }.format(Date(issuedAtMillis))
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("${uid.ifBlank { "local" }}:fiksfaham:graduation".toByteArray())
        val shortHash = digest.take(4).joinToString("") { "%02X".format(it) }
        return "FF-DSGN-$year-$shortHash"
    }

    /**
     * Memeriksa kelayakan sertifikat untuk Materi tertentu (kompatibilitas modul/per-materi).
     */
    fun evaluateEligibility(
        materiId: Int,
        allAttempts: List<AssessmentAttemptEntity>,
        capstoneSubmissions: List<CapstoneSubmissionEntity>
    ): EligibilityStatus {
        val posttestAttempt = findBestPosttestAttempt(materiId, allAttempts)

        if (posttestAttempt == null || !posttestAttempt.isPassed || posttestAttempt.score < POSTTEST_PASS_THRESHOLD) {
            return EligibilityStatus.Locked(ReasonKey.POSTTEST_NOT_PASSED)
        }

        val capstone = capstoneSubmissions.firstOrNull { it.materiId == materiId }
            ?: capstoneSubmissions.firstOrNull { it.materiId == FINAL_CAPSTONE_MATERI_ID }
        val lockedReason = checkCapstoneEligibilityLocked(capstone)
        if (lockedReason != null) {
            return lockedReason
        }

        return EligibilityStatus.Eligible(
            posttestScore = posttestAttempt.score,
            capstoneScore = capstone!!.nilai!!
        )
    }

    fun buildCertificateList(
        materis: List<MateriEntity>,
        allAttempts: List<AssessmentAttemptEntity>,
        capstoneSubmissions: List<CapstoneSubmissionEntity>
    ): List<CertificateItem> {
        val effectiveMateris = if (materis.isNotEmpty()) materis else listOf(
            MateriEntity(1, "Pengantar dan Proses Desain", "Fondasi desain dan etika berkarya", 1),
            MateriEntity(2, "Elemen dan Prinsip Desain", "Garis, ruang, keseimbangan, kontras, dan ritme", 2),
            MateriEntity(3, "Warna, Tipografi, dan Komposisi", "Roda warna, hierarki huruf, dan grid", 3),
            MateriEntity(4, "Riset, Ide, dan Visualisasi", "Persona, moodboard, sketsa, dan format file", 4),
            MateriEntity(5, "Penerapan Desain", "Logo, media promosi, UI/UX, kemasan, dan portofolio", 5)
        )

        return effectiveMateris.sortedBy { it.urutan }.map { materi ->
            val status = evaluateEligibility(materi.id, allAttempts, capstoneSubmissions)
            CertificateItem(
                materiId = materi.id,
                materiTitle = materi.judul,
                status = status
            )
        }
    }
}
