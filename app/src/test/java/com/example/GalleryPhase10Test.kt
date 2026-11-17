package com.example

import com.example.data.repository.DesignTaskSubmission
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit Test untuk TAHAP 10: Galeri Desain.
 *
 * Menguji:
 * - Visibilitas default adalah "private".
 * - Logika filter "Galeri Saya" (seluruh desain milik pengguna, privat & publik).
 * - Logika filter "Galeri Publik" (hanya desain berstatus publik, menampilkan nama pengguna sesuai profil).
 * - Logika toggle visibilitas (privat <-> publik).
 * - Pemilik dapat menghapus desain miliknya sendiri (Storage + Firestore).
 * - Alur hapus akun (Tahap 8B) membersihkan seluruh desain milik pengguna (privat & publik) tanpa mengganggu pengguna lain.
 * - Logika pagination galeri publik (limit dan cursor startAfter).
 */
class GalleryPhase10Test {

    private lateinit var fakeService: FakeDesignTaskRemoteService

    @Before
    fun setUp() {
        fakeService = FakeDesignTaskRemoteService()
    }

    @Test
    fun defaultVisibility_isPrivate() {
        val submission = DesignTaskSubmission(
            uid = "user_abc",
            materiId = 1,
            subMateriId = 1,
            url = "https://example.com/design1.jpg"
        )
        // Default visibility must be "private"
        assertEquals("private", submission.visibility)
    }

    @Test
    fun filter_myGallery_includesAllSubmissionsForCurrentUser() = runBlocking {
        // User 1 has 2 submissions: 1 private, 1 public
        val user1Sub1 = DesignTaskSubmission(
            uid = "user_1",
            materiId = 1,
            subMateriId = 1,
            url = "https://example.com/u1_s1.jpg",
            visibility = "private",
            userName = "Budi Santoso",
            createdAt = 1000L
        )
        val user1Sub2 = DesignTaskSubmission(
            uid = "user_1",
            materiId = 1,
            subMateriId = 2,
            url = "https://example.com/u1_s2.jpg",
            visibility = "public",
            userName = "Budi Santoso",
            createdAt = 2000L
        )
        // User 2 has 1 submission
        val user2Sub1 = DesignTaskSubmission(
            uid = "user_2",
            materiId = 1,
            subMateriId = 1,
            url = "https://example.com/u2_s1.jpg",
            visibility = "public",
            userName = "Siti Rahma",
            createdAt = 3000L
        )

        fakeService.saveSubmissionMetadata(user1Sub1)
        fakeService.saveSubmissionMetadata(user1Sub2)
        fakeService.saveSubmissionMetadata(user2Sub1)

        // Filter Galeri Saya untuk User 1:
        val allUser1Submissions = fakeService.submissions.values.filter { it.uid == "user_1" }
        assertEquals(2, allUser1Submissions.size)
        assertTrue(allUser1Submissions.any { it.visibility == "private" })
        assertTrue(allUser1Submissions.any { it.visibility == "public" })
        assertTrue(allUser1Submissions.none { it.uid == "user_2" })
    }

    @Test
    fun filter_publicGallery_strictlyIncludesOnlyPublicDesignsWithProfileName() = runBlocking {
        val user1Private = DesignTaskSubmission(
            uid = "user_1",
            materiId = 1,
            subMateriId = 1,
            url = "https://example.com/u1_private.jpg",
            visibility = "private",
            userName = "Budi Santoso",
            createdAt = 1000L
        )
        val user1Public = DesignTaskSubmission(
            uid = "user_1",
            materiId = 1,
            subMateriId = 2,
            url = "https://example.com/u1_public.jpg",
            visibility = "public",
            userName = "Budi Santoso",
            createdAt = 2000L
        )
        val user2Private = DesignTaskSubmission(
            uid = "user_2",
            materiId = 1,
            subMateriId = 1,
            url = "https://example.com/u2_private.jpg",
            visibility = "private",
            userName = "Ahmad Dani",
            createdAt = 3000L
        )
        val user2Public = DesignTaskSubmission(
            uid = "user_2",
            materiId = 1,
            subMateriId = 2,
            url = "https://example.com/u2_public.jpg",
            visibility = "public",
            userName = "Ahmad Dani",
            createdAt = 4000L
        )

        fakeService.saveSubmissionMetadata(user1Private)
        fakeService.saveSubmissionMetadata(user1Public)
        fakeService.saveSubmissionMetadata(user2Private)
        fakeService.saveSubmissionMetadata(user2Public)

        // Query Galeri Publik
        val publicList = fakeService.getPublicSubmissions(limit = 10).getOrThrow()

        // Harus hanya berisi 2 desain berstatus "public"
        assertEquals(2, publicList.size)
        assertTrue(publicList.all { it.visibility == "public" })
        assertFalse(publicList.any { it.visibility == "private" })

        // Menampilkan nama pengguna sesuai profil
        val userNames = publicList.map { it.userName }.toSet()
        assertTrue(userNames.contains("Budi Santoso"))
        assertTrue(userNames.contains("Ahmad Dani"))
    }

    @Test
    fun toggleVisibility_switchesBetweenPrivateAndPublic() = runBlocking {
        val initialSubmission = DesignTaskSubmission(
            uid = "user_1",
            materiId = 1,
            subMateriId = 1,
            url = "https://example.com/design.jpg",
            visibility = "private",
            userName = "Dewi Lestari",
            createdAt = 1000L
        )
        fakeService.saveSubmissionMetadata(initialSubmission)

        // 1. Awalnya private: tidak muncul di galeri publik
        var publicList = fakeService.getPublicSubmissions(limit = 10).getOrThrow()
        assertTrue(publicList.isEmpty())

        // 2. Pemilik mengubah ke public via toggle
        fakeService.updateVisibility("user_1", 1, "public")
        val updated = fakeService.getSubmission("user_1", 1).getOrNull()
        assertNotNull(updated)
        assertEquals("public", updated?.visibility)

        // Sekarang muncul di galeri publik
        publicList = fakeService.getPublicSubmissions(limit = 10).getOrThrow()
        assertEquals(1, publicList.size)
        assertEquals("Dewi Lestari", publicList.first().userName)

        // 3. Pemilik mengembalikan ke private
        fakeService.updateVisibility("user_1", 1, "private")
        val reverted = fakeService.getSubmission("user_1", 1).getOrNull()
        assertEquals("private", reverted?.visibility)

        // Hilang lagi dari galeri publik
        publicList = fakeService.getPublicSubmissions(limit = 10).getOrThrow()
        assertTrue(publicList.isEmpty())
    }

    @Test
    fun deleteSubmission_removesDesignFromStorageAndFirestore() = runBlocking {
        val fileBytes = ByteArray(512) { 7 }
        fakeService.uploadImageBytes(fileBytes, "user_1", 3, "image/jpeg") {}

        val submission = DesignTaskSubmission(
            uid = "user_1",
            materiId = 1,
            subMateriId = 3,
            url = "https://example.com/sub3.jpg",
            visibility = "public",
            userName = "Citra",
            createdAt = 1000L
        )
        fakeService.saveSubmissionMetadata(submission)

        // Verifikasi sebelum dihapus
        assertNotNull(fakeService.getSubmission("user_1", 3).getOrNull())
        assertTrue(fakeService.uploadedFiles.containsKey("user_1_3"))

        // Hapus oleh pemilik
        fakeService.deleteImageFile("user_1", 3)
        fakeService.deleteSubmission("user_1", 3)

        // Verifikasi sesudah dihapus
        assertNull(fakeService.getSubmission("user_1", 3).getOrNull())
        assertFalse(fakeService.uploadedFiles.containsKey("user_1_3"))
        val publicList = fakeService.getPublicSubmissions(limit = 10).getOrThrow()
        assertTrue(publicList.none { it.uid == "user_1" && it.subMateriId == 3 })
    }

    @Test
    fun deleteAccount_removesUserDesigns_whilePreservingOtherUsersDesigns() = runBlocking {
        // User A memiliki 2 desain (1 privat, 1 publik)
        fakeService.saveSubmissionMetadata(
            DesignTaskSubmission(uid = "user_A", subMateriId = 1, visibility = "private", userName = "User A")
        )
        fakeService.saveSubmissionMetadata(
            DesignTaskSubmission(uid = "user_A", subMateriId = 2, visibility = "public", userName = "User A")
        )

        // User B memiliki 1 desain publik
        fakeService.saveSubmissionMetadata(
            DesignTaskSubmission(uid = "user_B", subMateriId = 1, visibility = "public", userName = "User B")
        )

        // User A menghapus akun
        fakeService.deleteAllUserSubmissions("user_A")

        // Verifikasi: Desain User A harus terhapus seluruhnya
        val userASubmissions = fakeService.submissions.values.filter { it.uid == "user_A" }
        assertTrue(userASubmissions.isEmpty())

        // Desain publik User B tetap aman dan utuh
        val publicList = fakeService.getPublicSubmissions(limit = 10).getOrThrow()
        assertEquals(1, publicList.size)
        assertEquals("User B", publicList.first().userName)
    }

    @Test
    fun publicGallery_paginationSplitsResultsProperly() = runBlocking {
        // Masukkan 5 desain publik dengan timestamp berbeda
        for (i in 1..5) {
            fakeService.saveSubmissionMetadata(
                DesignTaskSubmission(
                    uid = "user_$i",
                    subMateriId = 1,
                    visibility = "public",
                    userName = "User $i",
                    createdAt = i * 1000L
                )
            )
        }

        // Halaman 1: Limit 2
        val page1 = fakeService.getPublicSubmissions(limit = 2).getOrThrow()
        assertEquals(2, page1.size)
        assertEquals(5000L, page1[0].createdAt) // Urutan terbaru duluan
        assertEquals(4000L, page1[1].createdAt)

        // Halaman 2: Limit 2 setelah createdAt 4000L
        val page2 = fakeService.getPublicSubmissions(limit = 2, startAfterCreatedAt = page1.last().createdAt).getOrThrow()
        assertEquals(2, page2.size)
        assertEquals(3000L, page2[0].createdAt)
        assertEquals(2000L, page2[1].createdAt)

        // Halaman 3: Sisa 1
        val page3 = fakeService.getPublicSubmissions(limit = 2, startAfterCreatedAt = page2.last().createdAt).getOrThrow()
        assertEquals(1, page3.size)
        assertEquals(1000L, page3[0].createdAt)
    }
}
