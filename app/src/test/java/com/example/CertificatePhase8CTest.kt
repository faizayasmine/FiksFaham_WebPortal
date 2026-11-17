package com.example

import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.MateriEntity
import com.example.util.CertificateConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test untuk logika kelayakan sertifikat (Tahap 8C):
 * - Konfigurasi terpusat di CertificateConfig: Posttest lulus (>= 75) DAN Capstone berstatus reviewed dengan nilai >= 75.
 * - Pengecekan alasan terkunci (POSTTEST_NOT_PASSED, CAPSTONE_NOT_SUBMITTED, CAPSTONE_UNDER_REVIEW, CAPSTONE_SCORE_LOW).
 * - Pengecekan nama pengguna (wajib diisi di profil sebelum mencetak).
 */
class CertificatePhase8CTest {

    private val materi1 = MateriEntity(
        id = 1,
        urutan = 1,
        judul = "Fondasi UI/UX",
        deskripsi = "Pengenalan UI/UX",
        courseId = 1
    )

    @Test
    fun certificateConfig_passThresholds_areSeventyFive() {
        assertEquals(75, CertificateConfig.POSTTEST_PASS_THRESHOLD)
        assertEquals(75, CertificateConfig.CAPSTONE_PASS_THRESHOLD)
    }

    @Test
    fun evaluateEligibility_noPosttest_returnsPosttestNotPassed() {
        val status = CertificateConfig.evaluateEligibility(
            materiId = 1,
            allAttempts = emptyList(),
            capstoneSubmissions = emptyList()
        )
        assertTrue(status is CertificateConfig.EligibilityStatus.Locked)
        assertEquals(
            CertificateConfig.ReasonKey.POSTTEST_NOT_PASSED,
            (status as CertificateConfig.EligibilityStatus.Locked).reasonKey
        )
    }

    @Test
    fun evaluateEligibility_posttestFailed_returnsPosttestNotPassed() {
        val failedAttempt = AssessmentAttemptEntity(
            id = 1,
            assessmentId = 20, // Posttest Materi 1
            attemptNo = 1,
            score = 60,
            isPassed = false,
            submittedAt = System.currentTimeMillis()
        )
        val status = CertificateConfig.evaluateEligibility(
            materiId = 1,
            allAttempts = listOf(failedAttempt),
            capstoneSubmissions = emptyList()
        )
        assertTrue(status is CertificateConfig.EligibilityStatus.Locked)
        assertEquals(
            CertificateConfig.ReasonKey.POSTTEST_NOT_PASSED,
            (status as CertificateConfig.EligibilityStatus.Locked).reasonKey
        )
    }

    @Test
    fun evaluateEligibility_posttestPassed_noCapstone_returnsCapstoneNotSubmitted() {
        val passedAttempt = AssessmentAttemptEntity(
            id = 1,
            assessmentId = 20,
            attemptNo = 1,
            score = 80,
            isPassed = true,
            submittedAt = System.currentTimeMillis()
        )
        val status = CertificateConfig.evaluateEligibility(
            materiId = 1,
            allAttempts = listOf(passedAttempt),
            capstoneSubmissions = emptyList()
        )
        assertTrue(status is CertificateConfig.EligibilityStatus.Locked)
        assertEquals(
            CertificateConfig.ReasonKey.CAPSTONE_NOT_SUBMITTED,
            (status as CertificateConfig.EligibilityStatus.Locked).reasonKey
        )
    }

    @Test
    fun evaluateEligibility_posttestPassed_capstonePending_returnsCapstoneUnderReview() {
        val passedAttempt = AssessmentAttemptEntity(
            id = 1,
            assessmentId = 20,
            attemptNo = 1,
            score = 90,
            isPassed = true,
            submittedAt = System.currentTimeMillis()
        )
        val pendingCapstone = CapstoneSubmissionEntity(
            userId = 1,
            materiId = 1,
            judulProject = "Project Desain UI",
            deskripsi = "Deskripsi proyek",
            linkFigma = "https://figma.com/file/123",
            fileUrl = "",
            status = "submitted",
            nilai = null,
            feedback = null,
            submittedAt = System.currentTimeMillis()
        )
        val status = CertificateConfig.evaluateEligibility(
            materiId = 1,
            allAttempts = listOf(passedAttempt),
            capstoneSubmissions = listOf(pendingCapstone)
        )
        assertTrue(status is CertificateConfig.EligibilityStatus.Locked)
        assertEquals(
            CertificateConfig.ReasonKey.CAPSTONE_UNDER_REVIEW,
            (status as CertificateConfig.EligibilityStatus.Locked).reasonKey
        )
    }

    @Test
    fun evaluateEligibility_posttestPassed_capstoneReviewedScoreLow_returnsCapstoneScoreLow() {
        val passedAttempt = AssessmentAttemptEntity(
            id = 1,
            assessmentId = 20,
            attemptNo = 1,
            score = 85,
            isPassed = true,
            submittedAt = System.currentTimeMillis()
        )
        val reviewedLowScoreCapstone = CapstoneSubmissionEntity(
            userId = 1,
            materiId = 1,
            judulProject = "Project Desain UI",
            deskripsi = "Deskripsi proyek",
            linkFigma = "https://figma.com/file/123",
            fileUrl = "",
            status = "reviewed",
            nilai = 70, // Kurang dari 75
            feedback = "Perbaiki kontras warna",
            submittedAt = System.currentTimeMillis()
        )
        val status = CertificateConfig.evaluateEligibility(
            materiId = 1,
            allAttempts = listOf(passedAttempt),
            capstoneSubmissions = listOf(reviewedLowScoreCapstone)
        )
        assertTrue(status is CertificateConfig.EligibilityStatus.Locked)
        val lockedStatus = status as CertificateConfig.EligibilityStatus.Locked
        assertEquals(CertificateConfig.ReasonKey.CAPSTONE_SCORE_LOW, lockedStatus.reasonKey)
        assertEquals(70, lockedStatus.currentCapstoneScore)
    }

    @Test
    fun evaluateEligibility_bothPassedAndReviewed_returnsEligible() {
        val passedAttempt = AssessmentAttemptEntity(
            id = 1,
            assessmentId = 20,
            attemptNo = 1,
            score = 90,
            isPassed = true,
            submittedAt = System.currentTimeMillis()
        )
        val reviewedPassedCapstone = CapstoneSubmissionEntity(
            userId = 1,
            materiId = 1,
            judulProject = "Design System FiksFaham",
            deskripsi = "Lengkap dengan dokumentasi",
            linkFigma = "https://figma.com/file/123",
            fileUrl = "",
            status = "reviewed",
            nilai = 88, // >= 75
            feedback = "Sangat rapi dan konsisten!",
            submittedAt = System.currentTimeMillis()
        )
        val status = CertificateConfig.evaluateEligibility(
            materiId = 1,
            allAttempts = listOf(passedAttempt),
            capstoneSubmissions = listOf(reviewedPassedCapstone)
        )
        assertTrue(status is CertificateConfig.EligibilityStatus.Eligible)
        val eligibleStatus = status as CertificateConfig.EligibilityStatus.Eligible
        assertEquals(90, eligibleStatus.posttestScore)
        assertEquals(88, eligibleStatus.capstoneScore)
    }

    @Test
    fun userNameValidation_forCertificate_checksBlankOrEmpty() {
        fun isUserNameValid(name: String?): Boolean {
            return !name.isNullOrBlank()
        }

        assertTrue(isUserNameValid("Budi Santoso"))
        assertTrue(isUserNameValid("Jane Doe"))
        assertFalse(isUserNameValid(""))
        assertFalse(isUserNameValid("   "))
        assertFalse(isUserNameValid(null))
    }

    @Test
    fun evaluateGraduationEligibility_requiresFivePosttestsAndApprovedCapstone() {
        val materis = (1..5).map { idx ->
            MateriEntity(id = idx, judul = "Materi $idx", deskripsi = "", urutan = idx, courseId = 1)
        }
        val passedPosttests = listOf(20, 220, 320, 420, 520).mapIndexed { i, id ->
            AssessmentAttemptEntity(
                id = i + 1,
                assessmentId = id,
                attemptNo = 1,
                score = 80 + i,
                isPassed = true,
                submittedAt = 1000L + i
            )
        }

        // Tanpa capstone -> terkunci CAPSTONE_NOT_SUBMITTED
        val lockedNoCap = CertificateConfig.evaluateGraduationEligibility(
            materis = materis,
            allAttempts = passedPosttests,
            capstoneSubmissions = emptyList()
        )
        assertTrue(lockedNoCap.status is CertificateConfig.EligibilityStatus.Locked)
        assertEquals(
            CertificateConfig.ReasonKey.CAPSTONE_NOT_SUBMITTED,
            (lockedNoCap.status as CertificateConfig.EligibilityStatus.Locked).reasonKey
        )

        // Dengan Final Capstone (materiId = 0) berstatus approved & nilai 92 -> Eligible
        val approvedCapstone = CapstoneSubmissionEntity(
            userId = 1,
            materiId = 0,
            judulProject = "Portofolio Dunia Desain",
            deskripsi = "Lengkap",
            linkFigma = "https://figma.com/file/final",
            status = "approved",
            nilai = 92,
            feedback = "Memenuhi seluruh rubrik",
            submittedAt = 5000L
        )
        val eligible = CertificateConfig.evaluateGraduationEligibility(
            materis = materis,
            allAttempts = passedPosttests,
            capstoneSubmissions = listOf(approvedCapstone)
        )
        assertTrue(eligible.status is CertificateConfig.EligibilityStatus.Eligible)
        assertEquals(5, eligible.passedPosttestCount)
        assertEquals(92, eligible.capstoneScore)
    }

    @Test
    fun generateCertificateNumber_isDeterministicAndFormatted() {
        val cert1 = CertificateConfig.generateCertificateNumber("uid_abc_123", 1767225600000L)
        val cert2 = CertificateConfig.generateCertificateNumber("uid_abc_123", 1767225600000L)
        assertEquals(cert1, cert2)
        assertTrue(cert1.startsWith("FF-DSGN-"))
    }
}
