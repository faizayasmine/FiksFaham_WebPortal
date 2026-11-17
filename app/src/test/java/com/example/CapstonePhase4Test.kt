package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.FiksFahamDatabase
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.repository.FiksFahamRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit Test Suite for FASE 4: Capstone Mentor Review via Cloud Firestore & Student Local Cache.
 * Validates mentor role authorization, review submission via repository,
 * student offline cache in Room, validation, and student-side local notification generation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CapstonePhase4Test {

    private lateinit var app: Application
    private lateinit var database: FiksFahamDatabase
    private lateinit var repository: FiksFahamRepository
    private lateinit var fakeFirestore: FakeCapstoneFirestoreService
    private lateinit var cloudRepository: CapstoneRepository

    private val studentUid = "student_unesa_01"
    private val mentorUid = "mentor_unesa_01"
    private val nonMentorUid = "student_hacker_99"
    private val materiId = 1

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(app, FiksFahamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FiksFahamRepository(database.dao())
        fakeFirestore = FakeCapstoneFirestoreService(verifiedMentors = setOf(mentorUid))
        cloudRepository = CapstoneRepository(fakeFirestore)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testMentorCanReviewCapstoneSuccessfully() = runTest {
        repository.seedDatabaseIfEmpty()
        val user = repository.user.first()!!
        val userId = user.id

        // 1. Mahasiswa mengumpulkan tugas Capstone
        repository.submitCapstone(
            title = "Aplikasi Edukasi UI/UX FiksFaham",
            description = "Studi kasus perancangan antarmuka dan alur navigasi mahasiswa.",
            linkFigma = "https://figma.com/@fiksfaham/project",
            fileUrl = "https://firebasestorage.googleapis.com/v0/b/app/o/capstone_files%2Fcapstone.pdf"
        )
        cloudRepository.submit(
            CapstoneSubmission(
                uid = studentUid,
                materiId = materiId,
                nama = user.nama,
                email = user.email,
                judul = "Aplikasi Edukasi UI/UX FiksFaham",
                deskripsi = "Studi kasus perancangan antarmuka dan alur navigasi mahasiswa.",
                linkUrl = "https://figma.com/@fiksfaham/project",
                fileUrl = "https://firebasestorage.googleapis.com/v0/b/app/o/capstone_files%2Fcapstone.pdf",
                status = "submitted"
            )
        )

        val initialSubmission = repository.capstoneSubmission.first()
        assertNotNull("Submission lokal mahasiswa harus ada", initialSubmission)
        assertEquals("submitted", initialSubmission?.status)
        assertEquals(null, initialSubmission?.nilai)

        // 2. Mentor memberikan penilaian melalui repository Cloud
        val reviewResult = cloudRepository.review(
            uid = studentUid,
            materiId = materiId,
            nilai = 90,
            feedback = "Desain wireframe dan perancangan user flow sangat matang. Portofolio siap dipublikasikan.",
            mentorUid = mentorUid
        )
        assertTrue("Penilaian oleh mentor terverifikasi harus berhasil", reviewResult.isSuccess)

        // 3. Dokumen Firestore berubah ke "reviewed", mahasiswa menerima dan menyimpan salinan ke Room
        val reviewedCloud = cloudRepository.observeMine(studentUid, materiId).first()!!
        assertEquals("reviewed", reviewedCloud.status)
        assertEquals(90, reviewedCloud.nilai)

        repository.saveReviewedCapstoneFromCloud(reviewedCloud)

        // 4. Verifikasi submission di Room mahasiswa diperbarui menjadi 'reviewed'
        val reviewedSubmission = repository.capstoneSubmission.first()
        assertNotNull("Reviewed submission lokal harus ada", reviewedSubmission)
        assertEquals("reviewed", reviewedSubmission?.status)
        assertEquals(90, reviewedSubmission?.nilai)
        assertEquals(
            "Desain wireframe dan perancangan user flow sangat matang. Portofolio siap dipublikasikan.",
            reviewedSubmission?.feedback
        )
    }

    @Test
    fun testStudentRoleCannotReviewCapstone() = runTest {
        repository.seedDatabaseIfEmpty()
        val user = repository.user.first()!!

        // Student submits capstone
        cloudRepository.submit(
            CapstoneSubmission(
                uid = studentUid,
                materiId = materiId,
                nama = user.nama,
                email = user.email,
                judul = "Proyek Mandiri",
                deskripsi = "Deskripsi proyek",
                linkUrl = "https://figma.com/test",
                status = "submitted"
            )
        )

        // Non-mentor / student attempts to review
        val result = cloudRepository.review(
            uid = studentUid,
            materiId = materiId,
            nilai = 100,
            feedback = "Saya beri nilai diri sendiri",
            mentorUid = nonMentorUid
        )

        assertFalse("Review oleh user non-mentor harus gagal", result.isSuccess)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception penolakan harus dikembalikan", exception)
        assertTrue(
            "Pesan penolakan harus mencakup verifikasi mentor",
            exception?.message?.contains("mentor", ignoreCase = true) == true
        )

        // Status di Firestore tetap 'submitted'
        val cloudSub = cloudRepository.observeMine(studentUid, materiId).first()
        assertEquals("submitted", cloudSub?.status)
        assertEquals(null, cloudSub?.nilai)
    }

    @Test
    fun testReviewValidation_ScoreClampingAndEmptyFeedback() = runTest {
        cloudRepository.submit(
            CapstoneSubmission(
                uid = studentUid,
                materiId = materiId,
                judul = "Judul",
                deskripsi = "Deskripsi",
                status = "submitted"
            )
        )

        // 1. Empty feedback must fail with IllegalArgumentException
        val blankResult = cloudRepository.review(studentUid, materiId, 85, "   ", mentorUid)
        assertFalse("Blank feedback must fail", blankResult.isSuccess)
        assertTrue(blankResult.exceptionOrNull() is IllegalArgumentException)

        // 2. Score > 100 must fail with IllegalArgumentException
        val overResult = cloudRepository.review(studentUid, materiId, 150, "Bagus sekali!", mentorUid)
        assertFalse("Score > 100 must fail", overResult.isSuccess)
        assertTrue(overResult.exceptionOrNull() is IllegalArgumentException)

        // 3. Score < 0 must fail with IllegalArgumentException
        val underResult = cloudRepository.review(studentUid, materiId, -20, "Perlu perbaikan total.", mentorUid)
        assertFalse("Score < 0 must fail", underResult.isSuccess)
        assertTrue(underResult.exceptionOrNull() is IllegalArgumentException)

        // 4. Boundary valid scores 0 and 100 must succeed
        val validBoundaryMin = cloudRepository.review(studentUid, materiId, 0, "Perlu revisi total", mentorUid)
        assertTrue("Nilai 0 harus diterima", validBoundaryMin.isSuccess)

        val validBoundaryMax = cloudRepository.review(studentUid, materiId, 100, "Sempurna", mentorUid)
        assertTrue("Nilai 100 harus diterima", validBoundaryMax.isSuccess)
    }

    @Test
    fun testNotificationGeneratedOnReview() = runTest {
        repository.seedDatabaseIfEmpty()
        val user = repository.user.first()!!

        repository.submitCapstone("Judul Capstone", "Deskripsi", "https://figma.com", "")
        cloudRepository.submit(
            CapstoneSubmission(
                uid = studentUid,
                materiId = materiId,
                nama = user.nama,
                email = user.email,
                judul = "Judul Capstone",
                deskripsi = "Deskripsi",
                status = "submitted"
            )
        )

        // Mentor reviews capstone in cloud
        val reviewResult = cloudRepository.review(
            uid = studentUid,
            materiId = materiId,
            nilai = 88,
            feedback = "Kombinasi warna dan hierarki tipografi sangat baik.",
            mentorUid = mentorUid
        )
        assertTrue(reviewResult.isSuccess)

        // Notifikasi TIDAK dibuat di sisi mentor
        val mentorNotifications = repository.notifications.first()
        val mentorNotif = mentorNotifications.firstOrNull { it.assessmentId == 999 }
        assertEquals("Mentor device must NOT have capstone review notification", null, mentorNotif)

        // Saat mahasiswa menerima status "reviewed", notifikasi dibuat di perangkat MAHASISWA
        val reviewedCloud = cloudRepository.observeMine(studentUid, materiId).first()!!
        repository.saveReviewedCapstoneFromCloud(reviewedCloud)

        val studentNotifications = repository.notifications.first()
        val reviewNotification = studentNotifications.firstOrNull { it.assessmentId == 999 }

        assertNotNull("Notifikasi review capstone harus dibuat di perangkat mahasiswa", reviewNotification)
        assertTrue(
            "Judul notifikasi harus menyebutkan Capstone",
            reviewNotification!!.judul.contains("Capstone", ignoreCase = true)
        )
        assertTrue(
            "Pesan notifikasi harus memuat nilai yang diberikan",
            reviewNotification.pesan.contains("88")
        )
        assertFalse("Notifikasi baru harus unread", reviewNotification.isRead)
    }

    @Test
    fun testUpdateExistingReview() = runTest {
        repository.seedDatabaseIfEmpty()
        val user = repository.user.first()!!

        repository.submitCapstone("Revisi Capstone", "Deskripsi", "https://figma.com", "")
        cloudRepository.submit(
            CapstoneSubmission(
                uid = studentUid,
                materiId = materiId,
                nama = user.nama,
                email = user.email,
                judul = "Revisi Capstone",
                deskripsi = "Deskripsi",
                status = "submitted"
            )
        )

        // First review
        cloudRepository.review(studentUid, materiId, 70, "Perlu perbaiki kontras warna.", mentorUid)
        var reviewedCloud = cloudRepository.observeMine(studentUid, materiId).first()!!
        repository.saveReviewedCapstoneFromCloud(reviewedCloud)

        var submission = repository.capstoneSubmission.first()
        assertEquals(70, submission?.nilai)
        assertEquals("Perlu perbaiki kontras warna.", submission?.feedback)

        // Mentor updates review after revision
        cloudRepository.review(studentUid, materiId, 95, "Revisi sempurna! Kontras warna sangat memenuhi standar WCAG.", mentorUid)
        reviewedCloud = cloudRepository.observeMine(studentUid, materiId).first()!!
        repository.saveReviewedCapstoneFromCloud(reviewedCloud)

        submission = repository.capstoneSubmission.first()
        assertEquals(95, submission?.nilai)
        assertEquals("Revisi sempurna! Kontras warna sangat memenuhi standar WCAG.", submission?.feedback)
        assertEquals("reviewed", submission?.status)
    }
}
