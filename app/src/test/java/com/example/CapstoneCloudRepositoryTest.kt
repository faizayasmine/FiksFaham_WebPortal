package com.example

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Fake implementation of CapstoneFirestoreService for Unit Testing without relying on live Firebase.
 */
class FakeCapstoneFirestoreService(
    private val verifiedMentors: Set<String> = setOf("mentor_unesa_01", "mentor_fiks_leader")
) : CapstoneFirestoreService {

    val storageMap = mutableMapOf<String, CapstoneSubmission>()
    private val flows = mutableMapOf<String, MutableStateFlow<CapstoneSubmission?>>()

    override suspend fun submit(submission: CapstoneSubmission): Result<Unit> {
        val docId = "${submission.uid}_${submission.materiId}"
        storageMap[docId] = submission
        getOrCreateFlow(docId).value = submission
        return Result.success(Unit)
    }

    override fun observeMine(uid: String, materiId: Int): Flow<CapstoneSubmission?> {
        val docId = "${uid}_${materiId}"
        return getOrCreateFlow(docId)
    }

    override suspend fun getSubmission(uid: String, materiId: Int): Result<CapstoneSubmission?> {
        val docId = "${uid}_${materiId}"
        return Result.success(storageMap[docId])
    }

    override suspend fun listSubmissions(filter: CapstoneFilter): Result<List<CapstoneSubmission>> {
        var list = storageMap.values.toList()
        if (filter.materiId != null) {
            list = list.filter { it.materiId == filter.materiId }
        }
        if (!filter.status.isNullOrBlank()) {
            list = list.filter { it.status == filter.status }
        }
        return Result.success(list)
    }

    override suspend fun review(
        uid: String,
        materiId: Int,
        nilai: Int,
        feedback: String,
        mentorUid: String
    ): Result<Unit> {
        val docId = "${uid}_${materiId}"
        val existing = storageMap[docId]
            ?: return Result.failure(NoSuchElementException("Submission Capstone dengan ID $docId tidak ditemukan."))

        val updated = existing.copy(
            status = "reviewed",
            nilai = nilai,
            feedback = feedback,
            reviewedBy = mentorUid,
            reviewedAt = System.currentTimeMillis()
        )
        storageMap[docId] = updated
        getOrCreateFlow(docId).value = updated
        return Result.success(Unit)
    }

    override suspend fun isMentor(uid: String): Boolean {
        return verifiedMentors.contains(uid)
    }

    private fun getOrCreateFlow(docId: String): MutableStateFlow<CapstoneSubmission?> {
        return flows.getOrPut(docId) {
            MutableStateFlow(storageMap[docId])
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CapstoneCloudRepositoryTest {

    private lateinit var fakeService: FakeCapstoneFirestoreService
    private lateinit var repository: CapstoneRepository

    private val studentUid = "mhs_unesa_123"
    private val mentorUid = "mentor_unesa_01"
    private val fakeStudentUid = "mhs_hacker_999"
    private val materiId = 1

    @Before
    fun setup() {
        fakeService = FakeCapstoneFirestoreService()
        // Provide fake service to CapstoneRepository
        repository = CapstoneRepository(
            firestoreService = fakeService
        )
    }

    @Test
    fun testLifecycle_SubmittedToReviewedSuccessfully() = runTest {
        // 1. Mahasiswa mengirim tugas Capstone (status = "submitted", nilai = null)
        val initialSubmission = CapstoneSubmission(
            uid = studentUid,
            materiId = materiId,
            nama = "Budi Mahasiswa",
            email = "budi@unesa.ac.id",
            judul = "Desain Aplikasi UI/UX E-Commerce Lokal",
            deskripsi = "Penerapan prinsip desain visual dan user flow belanja UMKM.",
            linkUrl = "https://figma.com/@budi/capstone",
            fileUrl = "https://firebasestorage.googleapis.com/v0/b/app/o/capstone_files%2Fmhs_unesa_123%2F1%2Fdokumentasi.pdf",
            status = "submitted",
            nilai = null,
            feedback = "",
            reviewedBy = null
        )

        val submitResult = repository.submit(initialSubmission)
        assertTrue("Pengiriman submission Capstone harus berhasil", submitResult.isSuccess)

        // Verifikasi ID Dokumen Firestore menggunakan format {uid}_{materiId}
        val expectedDocId = "${studentUid}_$materiId"
        assertTrue("Dokumen tersimpan dengan format {uid}_{materiId}", fakeService.storageMap.containsKey(expectedDocId))

        // Observe alur submission
        val observedBeforeReview = repository.observeMine(studentUid, materiId).first()
        assertNotNull(observedBeforeReview)
        assertEquals("submitted", observedBeforeReview?.status)
        assertNull("Nilai belum ada sebelum dinilai", observedBeforeReview?.nilai)
        assertNull("reviewedBy belum ada sebelum dinilai", observedBeforeReview?.reviewedBy)

        // 2. Mentor memberikan penilaian
        val reviewResult = repository.review(
            uid = studentUid,
            materiId = materiId,
            nilai = 92,
            feedback = "Desain wireframe dan user journey sangat rapi serta memenuhi standar WCAG.",
            mentorUid = mentorUid
        )
        assertTrue("Penilaian oleh mentor terverifikasi harus berhasil", reviewResult.isSuccess)

        // 3. Verifikasi status berubah menjadi 'reviewed', nilai 92, dan reviewedBy terisi
        val observedAfterReview = repository.observeMine(studentUid, materiId).first()
        assertNotNull(observedAfterReview)
        assertEquals("reviewed", observedAfterReview?.status)
        assertEquals(92, observedAfterReview?.nilai)
        assertEquals(
            "Desain wireframe dan user journey sangat rapi serta memenuhi standar WCAG.",
            observedAfterReview?.feedback
        )
        assertEquals(mentorUid, observedAfterReview?.reviewedBy)
        assertNotNull("Waktu penilaian harus tercatat", observedAfterReview?.reviewedAt)
    }

    @Test
    fun testStudentCannotReviewCapstone() = runTest {
        // Mahasiswa submit tugas
        repository.submit(
            CapstoneSubmission(
                uid = studentUid,
                materiId = materiId,
                nama = "Budi Mahasiswa",
                email = "budi@unesa.ac.id",
                judul = "Tugas UI",
                deskripsi = "Deskripsi",
                linkUrl = "https://figma.com",
                fileUrl = "https://storage.url",
                status = "submitted"
            )
        )

        // Mahasiswa lain (bukan mentor) mencoba memanggil review
        val unauthorizedReviewResult = repository.review(
            uid = studentUid,
            materiId = materiId,
            nilai = 100,
            feedback = "Mencoba menilai diri sendiri atau teman tanpa hak mentor.",
            mentorUid = fakeStudentUid // Tidak ada di daftar mentor
        )

        assertFalse("Mahasiswa atau user non-mentor TIDAK boleh bisa menilai Capstone", unauthorizedReviewResult.isSuccess)
        val exception = unauthorizedReviewResult.exceptionOrNull()
        assertNotNull("Harus mengembalikan exception penolakan", exception)
        assertTrue(
            "Pesan penolakan harus menjelaskan izin mentor",
            exception?.message?.contains("mentor", ignoreCase = true) == true
        )

        // Status submission tetap 'submitted' dan nilai tetap null
        val currentSubmission = repository.observeMine(studentUid, materiId).first()
        assertEquals("submitted", currentSubmission?.status)
        assertNull(currentSubmission?.nilai)
    }

    @Test
    fun testReviewRejectsScoreOutsideZeroToHundred() = runTest {
        repository.submit(
            CapstoneSubmission(
                uid = studentUid,
                materiId = materiId,
                nama = "Siti",
                email = "siti@unesa.ac.id",
                judul = "Capstone Siti",
                status = "submitted"
            )
        )

        // 1. Nilai di atas 100 harus ditolak
        val overResult = repository.review(
            uid = studentUid,
            materiId = materiId,
            nilai = 105,
            feedback = "Nilai melebihi 100",
            mentorUid = mentorUid
        )
        assertFalse("Nilai > 100 harus ditolak", overResult.isSuccess)
        assertTrue(overResult.exceptionOrNull() is IllegalArgumentException)

        // 2. Nilai di bawah 0 harus ditolak
        val underResult = repository.review(
            uid = studentUid,
            materiId = materiId,
            nilai = -10,
            feedback = "Nilai negatif",
            mentorUid = mentorUid
        )
        assertFalse("Nilai < 0 harus ditolak", underResult.isSuccess)
        assertTrue(underResult.exceptionOrNull() is IllegalArgumentException)

        // 3. Batas valid 0 dan 100 harus diterima
        val validBoundaryMin = repository.review(
            uid = studentUid,
            materiId = materiId,
            nilai = 0,
            feedback = "Perlu revisi total",
            mentorUid = mentorUid
        )
        assertTrue("Nilai 0 harus diterima", validBoundaryMin.isSuccess)

        val validBoundaryMax = repository.review(
            uid = studentUid,
            materiId = materiId,
            nilai = 100,
            feedback = "Sempurna",
            mentorUid = mentorUid
        )
        assertTrue("Nilai 100 harus diterima", validBoundaryMax.isSuccess)
    }

    @Test
    fun testListSubmissionsFilters() = runTest {
        // Submit 2 tugas
        repository.submit(
            CapstoneSubmission(
                uid = "mhs_1",
                materiId = 1,
                nama = "User 1",
                status = "submitted"
            )
        )
        repository.submit(
            CapstoneSubmission(
                uid = "mhs_2",
                materiId = 1,
                nama = "User 2",
                status = "submitted"
            )
        )

        // Nilai user 1
        repository.review(
            uid = "mhs_1",
            materiId = 1,
            nilai = 88,
            feedback = "Bagus",
            mentorUid = mentorUid
        )

        // Filter: ALL
        val allList = repository.listSubmissions(CapstoneFilter.ALL).getOrNull()
        assertEquals(2, allList?.size)

        // Filter: SUBMITTED
        val submittedList = repository.listSubmissions(CapstoneFilter.SUBMITTED).getOrNull()
        assertEquals(1, submittedList?.size)
        assertEquals("mhs_2", submittedList?.first()?.uid)

        // Filter: REVIEWED
        val reviewedList = repository.listSubmissions(CapstoneFilter.REVIEWED).getOrNull()
        assertEquals(1, reviewedList?.size)
        assertEquals("mhs_1", reviewedList?.first()?.uid)
    }

    @Test
    fun testCamelCaseMapSerialization() {
        val original = CapstoneSubmission(
            uid = "user_abc",
            materiId = 2,
            nama = "Nama Pengguna",
            email = "user@test.com",
            judul = "Judul Proyek",
            deskripsi = "Deskripsi Lengkap",
            linkUrl = "https://figma.com/file",
            fileUrl = "https://storage/file.pdf",
            status = "reviewed",
            nilai = 85,
            feedback = "Keren",
            reviewedBy = "mentor_xyz",
            submittedAt = 1700000000000L,
            reviewedAt = 1700005000000L
        )

        val map = FirestoreCapstoneService.submissionToMap(original)

        // Pastikan field menggunakan camelCase persis seperti yang diminta
        assertEquals("user_abc", map["uid"])
        assertEquals(2, map["materiId"])
        assertEquals("Nama Pengguna", map["nama"])
        assertEquals("user@test.com", map["email"])
        assertEquals("Judul Proyek", map["judul"])
        assertEquals("Deskripsi Lengkap", map["deskripsi"])
        assertEquals("https://figma.com/file", map["linkUrl"])
        assertEquals("https://storage/file.pdf", map["fileUrl"])
        assertEquals("reviewed", map["status"])
        assertEquals(85, map["nilai"])
        assertEquals("Keren", map["feedback"])
        assertEquals("mentor_xyz", map["reviewedBy"])
        assertEquals(1700000000000L, map["submittedAt"])
        assertEquals(1700005000000L, map["reviewedAt"])

        // Deserialisasi kembali ke CapstoneSubmission
        val restored = FirestoreCapstoneService.mapToSubmission(map)
        assertEquals(original, restored)
    }
}
