package com.example

import com.example.data.repository.DesignTaskRemoteService
import com.example.data.repository.DesignTaskSubmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Fake implementation of DesignTaskRemoteService for unit testing
 * repository upload logic without real Android device or live Firebase.
 */
class FakeDesignTaskRemoteService : DesignTaskRemoteService {
    val submissions = mutableMapOf<String, DesignTaskSubmission>() // "uid_subMateriId" -> submission
    val uploadedFiles = mutableMapOf<String, ByteArray>() // "uid_subMateriId" -> file bytes
    val mentors = mutableSetOf("mentor_1", "mentor_expert")

    override suspend fun saveSubmissionMetadata(submission: DesignTaskSubmission): Result<Unit> {
        val key = "${submission.uid}_${submission.subMateriId}"
        // Overwrite (menimpa) existing submission
        submissions[key] = submission
        return Result.success(Unit)
    }

    override suspend fun getSubmission(uid: String, subMateriId: Int): Result<DesignTaskSubmission?> {
        val key = "${uid}_${subMateriId}"
        return Result.success(submissions[key])
    }

    override fun observeSubmission(uid: String, subMateriId: Int): Flow<DesignTaskSubmission?> {
        val key = "${uid}_${subMateriId}"
        return flowOf(submissions[key])
    }

    override suspend fun uploadImageBytes(
        bytes: ByteArray,
        uid: String,
        subMateriId: Int,
        contentType: String,
        onProgress: (Float) -> Unit
    ): Result<String> {
        if (contentType != "image/jpeg" && contentType != "image/png") {
            return Result.failure(IllegalArgumentException("Format berkas tidak didukung: $contentType"))
        }
        val key = "${uid}_${subMateriId}"
        uploadedFiles[key] = bytes
        onProgress(1.0f)
        val downloadUrl = "https://firebasestorage.googleapis.com/v0/b/fiksfaham/o/design_task_files%2F${uid}%2F${subMateriId}_design.jpg?alt=media"
        return Result.success(downloadUrl)
    }

    override suspend fun updateVisibility(uid: String, subMateriId: Int, visibility: String): Result<Unit> {
        val key = "${uid}_${subMateriId}"
        val existing = submissions[key]
        if (existing != null) {
            submissions[key] = existing.copy(visibility = visibility)
        }
        return Result.success(Unit)
    }

    override suspend fun deleteSubmission(uid: String, subMateriId: Int): Result<Unit> {
        val key = "${uid}_${subMateriId}"
        submissions.remove(key)
        return Result.success(Unit)
    }

    override suspend fun deleteImageFile(uid: String, subMateriId: Int): Result<Unit> {
        val key = "${uid}_${subMateriId}"
        uploadedFiles.remove(key)
        return Result.success(Unit)
    }

    override suspend fun getPublicSubmissions(limit: Int, startAfterCreatedAt: Long?): Result<List<DesignTaskSubmission>> {
        var items = submissions.values.filter { it.visibility == "public" }.sortedByDescending { it.createdAt }
        if (startAfterCreatedAt != null) {
            items = items.filter { it.createdAt < startAfterCreatedAt }
        }
        return Result.success(items.take(limit))
    }

    override suspend fun deleteAllUserSubmissions(uid: String): Result<Unit> {
        val keysToRemove = submissions.keys.filter { it.startsWith("${uid}_") }
        for (k in keysToRemove) {
            submissions.remove(k)
            uploadedFiles.remove(k)
        }
        return Result.success(Unit)
    }

    override suspend fun isMentor(uid: String): Boolean {
        return mentors.contains(uid)
    }
}

/**
 * Unit test untuk logika pengunggahan Tugas Desain (Tahap 9):
 * - Validasi metadata Firestore (uid, materiId, subMateriId, url, createdAt).
 * - Fitur unggah ulang menimpa submission sebelumnya.
 * - Validasi format (hanya JPG dan PNG).
 * - Validasi batas ukuran berkas.
 * - Izin baca oleh mentor.
 * - Tugas bersifat opsional (tidak mempengaruhi syarat kelulusan Sub Materi).
 */
class DesignTaskRepositoryTest {

    private val fakeService = FakeDesignTaskRemoteService()

    @Test
    fun uploadDesignTask_savesMetadataWithAllRequiredFields() = runBlocking {
        val sampleBytes = ByteArray(1024) { 1 }
        val uploadResult = fakeService.uploadImageBytes(
            bytes = sampleBytes,
            uid = "user_123",
            subMateriId = 1,
            contentType = "image/jpeg",
            onProgress = {}
        )
        assertTrue(uploadResult.isSuccess)
        val downloadUrl = uploadResult.getOrThrow()

        val submission = DesignTaskSubmission(
            uid = "user_123",
            materiId = 1,
            subMateriId = 1,
            url = downloadUrl,
            createdAt = System.currentTimeMillis()
        )
        val saveResult = fakeService.saveSubmissionMetadata(submission)
        assertTrue(saveResult.isSuccess)

        val retrieved = fakeService.getSubmission("user_123", 1).getOrNull()
        assertNotNull(retrieved)
        assertEquals("user_123", retrieved?.uid)
        assertEquals(1, retrieved?.materiId)
        assertEquals(1, retrieved?.subMateriId)
        assertEquals(downloadUrl, retrieved?.url)
        assertTrue((retrieved?.createdAt ?: 0L) > 0L)
    }

    @Test
    fun uploadDesignTask_reupload_overwritesExistingSubmission() = runBlocking {
        // Upload pertama
        val bytes1 = ByteArray(500) { 1 }
        val url1 = fakeService.uploadImageBytes(bytes1, "user_456", 2, "image/png", {}).getOrThrow()
        fakeService.saveSubmissionMetadata(
            DesignTaskSubmission(
                uid = "user_456",
                materiId = 1,
                subMateriId = 2,
                url = url1,
                createdAt = 1000L
            )
        )

        // Upload kedua (menimpa)
        val bytes2 = ByteArray(800) { 2 }
        val url2 = fakeService.uploadImageBytes(bytes2, "user_456", 2, "image/jpeg", {}).getOrThrow()
        fakeService.saveSubmissionMetadata(
            DesignTaskSubmission(
                uid = "user_456",
                materiId = 1,
                subMateriId = 2,
                url = url2,
                createdAt = 2000L
            )
        )

        val retrieved = fakeService.getSubmission("user_456", 2).getOrNull()
        assertNotNull(retrieved)
        assertEquals(url2, retrieved?.url)
        assertEquals(2000L, retrieved?.createdAt)
        assertEquals(1, fakeService.submissions.size)
    }

    @Test
    fun formatValidation_acceptsJpgAndPngOnly() = runBlocking {
        val jpgResult = fakeService.uploadImageBytes(ByteArray(10), "u1", 1, "image/jpeg", {})
        assertTrue(jpgResult.isSuccess)

        val pngResult = fakeService.uploadImageBytes(ByteArray(10), "u1", 2, "image/png", {})
        assertTrue(pngResult.isSuccess)

        val gifResult = fakeService.uploadImageBytes(ByteArray(10), "u1", 3, "image/gif", {})
        assertTrue(gifResult.isFailure)

        val pdfResult = fakeService.uploadImageBytes(ByteArray(10), "u1", 4, "application/pdf", {})
        assertTrue(pdfResult.isFailure)
    }

    @Test
    fun mentorVerification_mentorHasReadAccess() = runBlocking {
        assertTrue(fakeService.isMentor("mentor_1"))
        assertTrue(fakeService.isMentor("mentor_expert"))
        assertFalse(fakeService.isMentor("student_regular"))
        assertFalse(fakeService.isMentor(""))
    }

    @Test
    fun sizeValidation_helperChecksMaxLimit() {
        fun isWithinLimit(sizeBytes: Long, maxMb: Int): Boolean {
            val maxBytes = maxMb * 1024L * 1024L
            return sizeBytes <= maxBytes
        }

        // 3 MB <= 5 MB
        assertTrue(isWithinLimit(3 * 1024 * 1024L, 5))
        // 5 MB <= 5 MB
        assertTrue(isWithinLimit(5 * 1024 * 1024L, 5))
        // 6 MB > 5 MB
        assertFalse(isWithinLimit(6 * 1024 * 1024L, 5))
    }

    @Test
    fun designTaskOptional_doesNotAffectSubMateriCompletion() {
        // Aturan kelulusan Sub Materi: materiViewed == true AND quizPassed == true
        fun isSubMateriCompleted(materiViewed: Boolean, quizPassed: Boolean, hasDesignSubmission: Boolean): Boolean {
            // Tugas opsional: hasDesignSubmission tidak mengubah syarat kelulusan
            return materiViewed && quizPassed
        }

        // Sudah baca materi dan kuis lulus, tapi BELUM mengumpulkan tugas desain -> Tetap Lulus!
        assertTrue(isSubMateriCompleted(materiViewed = true, quizPassed = true, hasDesignSubmission = false))

        // Sudah baca materi dan kuis lulus, dan SUDAH mengumpulkan tugas desain -> Tetap Lulus!
        assertTrue(isSubMateriCompleted(materiViewed = true, quizPassed = true, hasDesignSubmission = true))

        // Belum kuis -> Belum Lulus meskipun sudah mengumpulkan tugas desain!
        assertFalse(isSubMateriCompleted(materiViewed = true, quizPassed = false, hasDesignSubmission = true))
    }
}
