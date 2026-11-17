package com.example.util

/**
 * Utility functions for authentication form validations and password strength scoring.
 * Used for production checks, real-time feedback, and unit tests.
 */
object AuthValidationUtils {
    // RFC-compliant email regex suitable for client-side format checking without depending on android.util.Patterns
    private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.isNotEmpty() && EMAIL_REGEX.matches(trimmed)
    }

    enum class PasswordStrength(val score: Int) {
        EMPTY(0),
        WEAK(1),
        MEDIUM(2),
        STRONG(3)
    }

    /**
     * Evaluates password strength:
     * - EMPTY: Empty string
     * - WEAK: Length < 6
     * - MEDIUM: Length >= 6, with basic variety
     * - STRONG: Length >= 8, mixed uppercase/lowercase, and contains numbers or special characters
     */
    fun evaluatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.EMPTY
        if (password.length < 6) return PasswordStrength.WEAK

        var bonus = 0
        if (password.length >= 8) bonus++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) bonus++
        if (password.any { it.isDigit() }) bonus++
        if (password.any { !it.isLetterOrDigit() }) bonus++

        return when {
            password.length >= 8 && bonus >= 3 -> PasswordStrength.STRONG
            bonus >= 1 || password.length >= 6 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> "Email tidak boleh kosong"
            !isValidEmail(trimmed) -> "Format email tidak valid (contoh: nama@email.com)"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Kata sandi tidak boleh kosong"
            password.length < 6 -> "Kata sandi minimal 6 karakter"
            else -> null
        }
    }

    fun validateName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Nama lengkap tidak boleh kosong"
            trimmed.length < 2 -> "Nama lengkap minimal 2 karakter"
            else -> null
        }
    }

    fun validateInstansi(instansi: String): String? {
        val trimmed = instansi.trim()
        return when {
            trimmed.isEmpty() -> "Asal instansi tidak boleh kosong"
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isEmpty() -> "Konfirmasi kata sandi tidak boleh kosong"
            password != confirmPassword -> "Konfirmasi kata sandi tidak sama dengan kata sandi"
            else -> null
        }
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errorMessage: String) : ValidationResult()
    }

    fun validateSignUpForm(
        nama: String,
        email: String,
        instansi: String,
        password: String,
        confirmPassword: String,
        termsAgreed: Boolean
    ): ValidationResult {
        val trimmedName = nama.trim()
        val trimmedEmail = email.trim()
        val trimmedInstansi = instansi.trim()

        return when {
            trimmedName.isBlank() || trimmedEmail.isBlank() || trimmedInstansi.isBlank() || password.isBlank() -> {
                ValidationResult.Invalid("Semua kolom pendaftaran wajib diisi!")
            }
            !isValidEmail(trimmedEmail) -> {
                ValidationResult.Invalid("Format email tidak valid (contoh: user@kampus.ac.id)!")
            }
            password.length < 6 -> {
                ValidationResult.Invalid("Kata sandi minimal 6 karakter!")
            }
            password != confirmPassword -> {
                ValidationResult.Invalid("Konfirmasi kata sandi tidak sama dengan kata sandi!")
            }
            !termsAgreed -> {
                ValidationResult.Invalid("Harap setujui Syarat & Kebijakan Privasi!")
            }
            else -> {
                ValidationResult.Valid
            }
        }
    }
}
