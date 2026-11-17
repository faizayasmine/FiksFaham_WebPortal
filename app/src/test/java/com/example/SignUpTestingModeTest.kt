package com.example

import com.example.util.AuthValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test Suite for Registration Form Validation.
 * Validates local form checks before submitting to Firebase Authentication.
 */
class SignUpTestingModeTest {

    @Test
    fun testEmptyFormValidation() {
        val result = AuthValidationUtils.validateSignUpForm(
            nama = "",
            email = "",
            instansi = "",
            password = "",
            confirmPassword = "",
            termsAgreed = false
        )
        assertTrue("Empty form should be invalid", result is AuthValidationUtils.ValidationResult.Invalid)
        val error = (result as AuthValidationUtils.ValidationResult.Invalid).errorMessage
        assertEquals("Semua kolom pendaftaran wajib diisi!", error)
    }

    @Test
    fun testInvalidEmailFormatValidation() {
        // Test missing @ symbol
        val noAtResult = AuthValidationUtils.validateSignUpForm(
            nama = "Budi Santoso",
            email = "budisantosogmail.com",
            instansi = "UNESA",
            password = "password123",
            confirmPassword = "password123",
            termsAgreed = true
        )
        assertTrue(noAtResult is AuthValidationUtils.ValidationResult.Invalid)
        assertEquals("Format email tidak valid (contoh: user@kampus.ac.id)!", (noAtResult as AuthValidationUtils.ValidationResult.Invalid).errorMessage)

        // Test missing domain extension / TLD
        val noTldResult = AuthValidationUtils.validateSignUpForm(
            nama = "Budi Santoso",
            email = "budi@gmail",
            instansi = "UNESA",
            password = "password123",
            confirmPassword = "password123",
            termsAgreed = true
        )
        assertTrue(noTldResult is AuthValidationUtils.ValidationResult.Invalid)
        assertEquals("Format email tidak valid (contoh: user@kampus.ac.id)!", (noTldResult as AuthValidationUtils.ValidationResult.Invalid).errorMessage)

        // Test whitespace email
        assertFalse("Whitespace email must not be valid", AuthValidationUtils.isValidEmail("   "))
        assertTrue("Valid email must be accepted", AuthValidationUtils.isValidEmail("mahasiswa@unesa.ac.id"))
        assertTrue("Valid email with dots must be accepted", AuthValidationUtils.isValidEmail("john.doe@subdomain.example.com"))
    }

    @Test
    fun testPasswordLengthValidation() {
        // Less than 6 characters
        val shortPassResult = AuthValidationUtils.validateSignUpForm(
            nama = "Budi Santoso",
            email = "budi@unesa.ac.id",
            instansi = "UNESA",
            password = "12345",
            confirmPassword = "12345",
            termsAgreed = true
        )
        assertTrue(shortPassResult is AuthValidationUtils.ValidationResult.Invalid)
        assertEquals("Kata sandi minimal 6 karakter!", (shortPassResult as AuthValidationUtils.ValidationResult.Invalid).errorMessage)
    }

    @Test
    fun testPasswordMismatchValidation() {
        val mismatchResult = AuthValidationUtils.validateSignUpForm(
            nama = "Budi Santoso",
            email = "budi@unesa.ac.id",
            instansi = "UNESA",
            password = "password123",
            confirmPassword = "password456",
            termsAgreed = true
        )
        assertTrue(mismatchResult is AuthValidationUtils.ValidationResult.Invalid)
        assertEquals("Konfirmasi kata sandi tidak sama dengan kata sandi!", (mismatchResult as AuthValidationUtils.ValidationResult.Invalid).errorMessage)
    }

    @Test
    fun testTermsUncheckedValidation() {
        val uncheckResult = AuthValidationUtils.validateSignUpForm(
            nama = "Budi Santoso",
            email = "budi@unesa.ac.id",
            instansi = "UNESA",
            password = "password123",
            confirmPassword = "password123",
            termsAgreed = false
        )
        assertTrue(uncheckResult is AuthValidationUtils.ValidationResult.Invalid)
        assertEquals("Harap setujui Syarat & Kebijakan Privasi!", (uncheckResult as AuthValidationUtils.ValidationResult.Invalid).errorMessage)
    }

    @Test
    fun testInstitutionEmptyValidation() {
        val emptyInstansiResult = AuthValidationUtils.validateSignUpForm(
            nama = "Budi Santoso",
            email = "budi@unesa.ac.id",
            instansi = "   ",
            password = "password123",
            confirmPassword = "password123",
            termsAgreed = true
        )
        assertTrue(emptyInstansiResult is AuthValidationUtils.ValidationResult.Invalid)
        assertEquals("Semua kolom pendaftaran wajib diisi!", (emptyInstansiResult as AuthValidationUtils.ValidationResult.Invalid).errorMessage)
    }

    @Test
    fun testAllDataValidValidation() {
        val validResult = AuthValidationUtils.validateSignUpForm(
            nama = "Citra Mahasiswa",
            email = "citra@unesa.ac.id",
            instansi = "Universitas Negeri Surabaya",
            password = "amanPassword2026",
            confirmPassword = "amanPassword2026",
            termsAgreed = true
        )
        assertTrue("Form with all valid fields must pass validation", validResult is AuthValidationUtils.ValidationResult.Valid)
    }
}
