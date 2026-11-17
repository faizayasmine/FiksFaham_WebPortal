package com.example

import com.example.util.AuthValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test untuk TAHAP 2: Redesain Halaman Login dan Daftar yang Ramah Pengguna.
 *
 * Menguji:
 * - Validasi email (kosong, format tanpa @ atau domain, email valid).
 * - Validasi kata sandi (kosong, kurang dari 6 karakter, valid).
 * - Validasi nama lengkap (kosong, kurang dari 2 karakter, valid).
 * - Validasi asal instansi (kosong, valid).
 * - Validasi konfirmasi kata sandi (kosong, tidak cocok, cocok).
 * - Evaluasi indikator kekuatan sandi (EMPTY, WEAK, MEDIUM, STRONG).
 */
class AuthPhase2Test {

    @Test
    fun emailValidation_rejectsEmptyAndInvalidFormats() {
        // Kosong
        val emptyErr = AuthValidationUtils.validateEmail("")
        assertNotNull("Email kosong harus ditolak", emptyErr)
        assertEquals("Email tidak boleh kosong", emptyErr)

        // Hanya spasi
        val whitespaceErr = AuthValidationUtils.validateEmail("   ")
        assertNotNull("Email spasi harus ditolak", whitespaceErr)

        // Tanpa @
        val noAtErr = AuthValidationUtils.validateEmail("userkampus.ac.id")
        assertNotNull("Email tanpa @ harus ditolak", noAtErr)
        assertTrue(noAtErr!!.contains("Format email tidak valid"))

        // Tanpa domain TLD
        val noTldErr = AuthValidationUtils.validateEmail("user@kampus")
        assertNotNull("Email tanpa TLD harus ditolak", noTldErr)

        // Email valid
        val validErr1 = AuthValidationUtils.validateEmail("mahasiswa@unesa.ac.id")
        assertNull("Email valid harus diterima", validErr1)

        val validErr2 = AuthValidationUtils.validateEmail("fiksfaham.user@gmail.com")
        assertNull("Email valid harus diterima", validErr2)
    }

    @Test
    fun passwordValidation_enforcesMinimumLength() {
        // Kosong
        val emptyErr = AuthValidationUtils.validatePassword("")
        assertNotNull("Password kosong harus ditolak", emptyErr)
        assertEquals("Kata sandi tidak boleh kosong", emptyErr)

        // Kurang dari 6 karakter
        val shortErr = AuthValidationUtils.validatePassword("12345")
        assertNotNull("Password kurang dari 6 karakter harus ditolak", shortErr)
        assertEquals("Kata sandi minimal 6 karakter", shortErr)

        // 6 karakter
        val exact6Err = AuthValidationUtils.validatePassword("123456")
        assertNull("Password 6 karakter harus valid", exact6Err)

        // Lebih dari 6 karakter
        val longErr = AuthValidationUtils.validatePassword("superRahasia2026!")
        assertNull("Password panjang harus valid", longErr)
    }

    @Test
    fun nameValidation_requiresNonEmptyAndMinimum2Chars() {
        val emptyErr = AuthValidationUtils.validateName("")
        assertNotNull("Nama kosong harus ditolak", emptyErr)
        assertEquals("Nama lengkap tidak boleh kosong", emptyErr)

        val singleCharErr = AuthValidationUtils.validateName("A")
        assertNotNull("Nama 1 huruf harus ditolak", singleCharErr)
        assertEquals("Nama lengkap minimal 2 karakter", singleCharErr)

        val validName = AuthValidationUtils.validateName("Faiza Yasmine")
        assertNull("Nama valid harus diterima", validName)
    }

    @Test
    fun instansiValidation_requiresNonEmpty() {
        val emptyErr = AuthValidationUtils.validateInstansi("  ")
        assertNotNull("Instansi kosong harus ditolak", emptyErr)
        assertEquals("Asal instansi tidak boleh kosong", emptyErr)

        val validErr = AuthValidationUtils.validateInstansi("Universitas Negeri Surabaya")
        assertNull("Instansi valid harus diterima", validErr)
    }

    @Test
    fun confirmPasswordValidation_detectsMismatch() {
        val emptyConfirm = AuthValidationUtils.validateConfirmPassword("pass123", "")
        assertNotNull("Konfirmasi kosong harus ditolak", emptyConfirm)
        assertEquals("Konfirmasi kata sandi tidak boleh kosong", emptyConfirm)

        val mismatch = AuthValidationUtils.validateConfirmPassword("pass123", "pass456")
        assertNotNull("Konfirmasi beda harus ditolak", mismatch)
        assertEquals("Konfirmasi kata sandi tidak sama dengan kata sandi", mismatch)

        val match = AuthValidationUtils.validateConfirmPassword("pass123", "pass123")
        assertNull("Konfirmasi cocok harus valid", match)
    }

    @Test
    fun passwordStrengthEvaluator_scoresAccurately() {
        // Kosong -> EMPTY
        assertEquals(
            AuthValidationUtils.PasswordStrength.EMPTY,
            AuthValidationUtils.evaluatePasswordStrength("")
        )

        // Kurang dari 6 karakter -> WEAK
        assertEquals(
            AuthValidationUtils.PasswordStrength.WEAK,
            AuthValidationUtils.evaluatePasswordStrength("abc")
        )
        assertEquals(
            AuthValidationUtils.PasswordStrength.WEAK,
            AuthValidationUtils.evaluatePasswordStrength("12345")
        )

        // 6 karakter biasa -> MEDIUM
        assertEquals(
            AuthValidationUtils.PasswordStrength.MEDIUM,
            AuthValidationUtils.evaluatePasswordStrength("abcdef")
        )

        // Kuat: >= 8 karakter, huruf besar & kecil, angka, dan simbol
        val strongPass = "FiksFaham@2026!"
        assertEquals(
            AuthValidationUtils.PasswordStrength.STRONG,
            AuthValidationUtils.evaluatePasswordStrength(strongPass)
        )
        assertEquals(3, AuthValidationUtils.PasswordStrength.STRONG.score)
    }
}
