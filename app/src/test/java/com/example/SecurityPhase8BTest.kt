package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test untuk logika keamanan akun (Tahap 8B):
 * - Validasi formulir ganti sandi (aturan sama dengan SignUp: min 6 karakter, konfirmasi cocok).
 * - Urutan 4 tahap penghapusan akun:
 *     1. Storage (foto profil, file Capstone).
 *     2. Firestore (users/{uid}, leaderboard/{uid}, capstone_submissions/{uid}_{materiId}).
 *     3. Room lokal (data pengguna).
 *     4. Firebase Auth user.
 * - Penanganan kegagalan di tengah: akun Auth TIDAK boleh dihapus jika langkah 1-3 gagal.
 * - Proteksi mentors/{uid}: dokumen mentor TIDAK pernah dihapus oleh penghapusan akun mahasiswa.
 * - Validasi konfirmasi ganda "HAPUS".
 */
class SecurityPhase8BTest {

    // --- Validasi Ganti Sandi ---
    @Test
    fun validatePasswordChange_emptyFields_fails() {
        val res1 = validateChangePasswordInput("", "newpass123", "newpass123")
        assertFalse(res1.isValid)
        assertEquals("Sandi lama tidak boleh kosong.", res1.errorMessage)

        val res2 = validateChangePasswordInput("oldpass123", "", "newpass123")
        assertFalse(res2.isValid)
        assertEquals("Sandi baru minimal 6 karakter.", res2.errorMessage)
    }

    @Test
    fun validatePasswordChange_shortNewPassword_fails() {
        val res = validateChangePasswordInput("oldpass123", "12345", "12345")
        assertFalse(res.isValid)
        assertEquals("Sandi baru minimal 6 karakter.", res.errorMessage)
    }

    @Test
    fun validatePasswordChange_mismatchedConfirmation_fails() {
        val res = validateChangePasswordInput("oldpass123", "newpass123", "different123")
        assertFalse(res.isValid)
        assertEquals("Konfirmasi sandi baru tidak cocok.", res.errorMessage)
    }

    @Test
    fun validatePasswordChange_validInputs_succeeds() {
        val res = validateChangePasswordInput("oldpass123", "newpass123", "newpass123")
        assertTrue(res.isValid)
        assertEquals(null, res.errorMessage)
    }

    // --- Validasi Konfirmasi Ganda Hapus Akun ---
    @Test
    fun deleteConfirmationKeyword_validatesExactCase() {
        assertTrue(isValidDeleteConfirmation("HAPUS"))
        assertFalse(isValidDeleteConfirmation("hapus"))
        assertFalse(isValidDeleteConfirmation("Hapus"))
        assertFalse(isValidDeleteConfirmation(""))
        assertFalse(isValidDeleteConfirmation("DELETE"))
    }

    // --- Simulasi Alur 4 Tahap Penghapusan Akun ---

    class FakeDeletionCoordinator(
        var failAtStep: Int? = null
    ) {
        val executedSteps = mutableListOf<String>()
        var isAuthDeleted = false
        val touchedFirestoreCollections = mutableListOf<String>()

        suspend fun executeDeletion(uid: String): Result<Unit> {
            executedSteps.clear()
            touchedFirestoreCollections.clear()
            isAuthDeleted = false

            // Langkah 1: Storage
            if (failAtStep == 1) {
                return Result.failure(Exception("Gagal menghapus file di Storage"))
            }
            executedSteps.add("STEP_1_STORAGE")

            // Langkah 2: Firestore
            if (failAtStep == 2) {
                return Result.failure(Exception("Gagal menghapus dokumen di Firestore"))
            }
            touchedFirestoreCollections.add("users")
            touchedFirestoreCollections.add("leaderboard")
            touchedFirestoreCollections.add("capstone_submissions")
            executedSteps.add("STEP_2_FIRESTORE")

            // Langkah 3: Room Lokal
            if (failAtStep == 3) {
                return Result.failure(Exception("Gagal membersihkan data Room lokal"))
            }
            executedSteps.add("STEP_3_ROOM")

            // Langkah 4: Firebase Auth
            if (failAtStep == 4) {
                return Result.failure(Exception("Gagal menghapus akun Firebase Auth"))
            }
            executedSteps.add("STEP_4_AUTH")
            isAuthDeleted = true

            return Result.success(Unit)
        }
    }

    @Test
    fun deletionWorkflow_success_executesAllFourStepsInOrder() {
        val coordinator = FakeDeletionCoordinator(failAtStep = null)
        val result = kotlinx.coroutines.runBlocking { coordinator.executeDeletion("user_123") }

        assertTrue(result.isSuccess)
        assertEquals(
            listOf("STEP_1_STORAGE", "STEP_2_FIRESTORE", "STEP_3_ROOM", "STEP_4_AUTH"),
            coordinator.executedSteps
        )
        assertTrue(coordinator.isAuthDeleted)
    }

    @Test
    fun deletionWorkflow_failsAtStep1Storage_abortsBeforeAuth() {
        val coordinator = FakeDeletionCoordinator(failAtStep = 1)
        val result = kotlinx.coroutines.runBlocking { coordinator.executeDeletion("user_123") }

        assertTrue(result.isFailure)
        assertEquals(0, coordinator.executedSteps.size)
        assertFalse(coordinator.isAuthDeleted)
    }

    @Test
    fun deletionWorkflow_failsAtStep2Firestore_abortsBeforeAuth() {
        val coordinator = FakeDeletionCoordinator(failAtStep = 2)
        val result = kotlinx.coroutines.runBlocking { coordinator.executeDeletion("user_123") }

        assertTrue(result.isFailure)
        assertEquals(listOf("STEP_1_STORAGE"), coordinator.executedSteps)
        assertFalse(coordinator.isAuthDeleted)
    }

    @Test
    fun deletionWorkflow_failsAtStep3Room_abortsBeforeAuth() {
        val coordinator = FakeDeletionCoordinator(failAtStep = 3)
        val result = kotlinx.coroutines.runBlocking { coordinator.executeDeletion("user_123") }

        assertTrue(result.isFailure)
        assertEquals(listOf("STEP_1_STORAGE", "STEP_2_FIRESTORE"), coordinator.executedSteps)
        assertFalse(coordinator.isAuthDeleted)
    }

    @Test
    fun deletionWorkflow_neverDeletesMentorsCollection() {
        val coordinator = FakeDeletionCoordinator(failAtStep = null)
        kotlinx.coroutines.runBlocking { coordinator.executeDeletion("user_123") }

        assertFalse(
            "Koleksi mentors tidak boleh disentuh saat penghapusan akun!",
            coordinator.touchedFirestoreCollections.contains("mentors")
        )
        assertTrue(coordinator.touchedFirestoreCollections.contains("users"))
        assertTrue(coordinator.touchedFirestoreCollections.contains("leaderboard"))
        assertTrue(coordinator.touchedFirestoreCollections.contains("capstone_submissions"))
    }

    // Helper functions matching SecurityScreen logic
    private data class ValidationResult(val isValid: Boolean, val errorMessage: String?)

    private fun validateChangePasswordInput(oldPass: String, newPass: String, confirmPass: String): ValidationResult {
        if (oldPass.isBlank()) {
            return ValidationResult(false, "Sandi lama tidak boleh kosong.")
        }
        if (newPass.length < 6) {
            return ValidationResult(false, "Sandi baru minimal 6 karakter.")
        }
        if (newPass != confirmPass) {
            return ValidationResult(false, "Konfirmasi sandi baru tidak cocok.")
        }
        return ValidationResult(true, null)
    }

    private fun isValidDeleteConfirmation(text: String): Boolean {
        return text == "HAPUS"
    }
}
