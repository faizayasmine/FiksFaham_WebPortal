package com.example.data.auth

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

/**
 * Data class representing an authenticated user from authentication service.
 */
data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String = "",
    val providerIds: List<String> = emptyList()
)

/**
 * Interface contract for authentication operations.
 * Allows clean separation between UI/domain and Firebase, and enables mock injection in tests.
 */
interface AuthService {
    val currentUser: AuthUser?
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signUpWithEmail(
        name: String,
        email: String,
        password: String,
        instansi: String = "",
        role: String = "mahasiswa"
    ): Result<AuthUser>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    suspend fun reauthenticate(password: String): Result<Unit>
    suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Autentikasi ulang Google belum didukung pada service ini."))
    suspend fun changePasswordWithReauth(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
        Result.failure(UnsupportedOperationException("Google Sign-In belum didukung pada service ini."))
    suspend fun verifyBeforeUpdateEmail(newEmail: String, password: String? = null): Result<Unit> =
        Result.failure(UnsupportedOperationException("Verifikasi pembaruan email belum didukung pada service ini."))
    suspend fun getCustomRoleClaim(): String? = null
    suspend fun deleteAuthUser(): Result<Unit>
    fun signOut()
}

/**
 * Production implementation of AuthService backed by Firebase Authentication SDK.
 */
class FirebaseAuthService(
    private val authProvider: () -> FirebaseAuth? = {
        try {
            FirebaseAuth.getInstance()
        } catch (t: Throwable) {
            Log.w("FirebaseAuthService", "FirebaseAuth instance not ready: ${t.message}")
            null
        }
    }
) : AuthService {

    private val auth: FirebaseAuth?
        get() = authProvider()

    private fun com.google.firebase.auth.FirebaseUser.toAuthUser(fallbackEmail: String = "", fallbackName: String = ""): AuthUser {
        return AuthUser(
            uid = uid,
            email = email?.ifBlank { fallbackEmail } ?: fallbackEmail,
            displayName = displayName?.ifBlank { fallbackName } ?: fallbackName,
            photoUrl = photoUrl?.toString().orEmpty(),
            providerIds = providerData.mapNotNull { it.providerId }.filter { it.isNotBlank() }.distinct()
        )
    }

    override val currentUser: AuthUser?
        get() = auth?.currentUser?.toAuthUser()

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Layanan autentikasi belum siap. Periksa koneksi internetmu.")
        )
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw IllegalStateException("Pengguna tidak ditemukan.")
            Result.success(user.toAuthUser(fallbackEmail = email.trim()))
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("Akun dengan email ini belum terdaftar."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Kata sandi salah atau format email tidak valid."))
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("Koneksi jaringan gagal. Periksa koneksi internetmu."))
        } catch (e: Throwable) {
            val msg = e.message.orEmpty()
            val friendlyMsg = when {
                msg.contains("network", ignoreCase = true) || msg.contains("timeout", ignoreCase = true) ->
                    "Koneksi jaringan gagal. Periksa koneksi internetmu."
                msg.contains("password", ignoreCase = true) ->
                    "Kata sandi salah."
                msg.contains("email", ignoreCase = true) ->
                    "Format email salah atau tidak valid."
                msg.contains("user", ignoreCase = true) ->
                    "Akun dengan email ini belum terdaftar."
                else -> e.message ?: "Gagal masuk ke akun. Periksa koneksi internetmu."
            }
            Result.failure(Exception(friendlyMsg))
        }
    }

    override suspend fun signUpWithEmail(
        name: String,
        email: String,
        password: String,
        instansi: String,
        role: String
    ): Result<AuthUser> {
        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Layanan autentikasi belum siap. Periksa koneksi internetmu.")
        )
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw IllegalStateException("Gagal membuat akun pengguna.")

            // Update displayName in Firebase Auth Profile
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            } catch (t: Throwable) {
                Log.w("FirebaseAuthService", "Failed to set display name: ${t.message}")
            }

            Result.success(user.toAuthUser(fallbackEmail = email.trim(), fallbackName = name.trim()))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Email ini sudah digunakan oleh akun lain. Silakan langsung masuk."))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Kata sandi terlalu lemah. Gunakan minimal 6 karakter."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Format email tidak valid."))
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("Koneksi jaringan gagal. Periksa koneksi internetmu."))
        } catch (e: Throwable) {
            val msg = e.message.orEmpty()
            val friendlyMsg = when {
                msg.contains("network", ignoreCase = true) || msg.contains("timeout", ignoreCase = true) ->
                    "Koneksi jaringan gagal. Periksa koneksi internetmu."
                msg.contains("collision", ignoreCase = true) || msg.contains("already in use", ignoreCase = true) ->
                    "Email ini sudah digunakan oleh akun lain. Silakan langsung masuk."
                msg.contains("weak", ignoreCase = true) ->
                    "Kata sandi terlalu lemah. Gunakan minimal 6 karakter."
                msg.contains("email", ignoreCase = true) ->
                    "Format email tidak valid."
                else -> e.message ?: "Pendaftaran gagal. Periksa koneksi internetmu."
            }
            Result.failure(Exception(friendlyMsg))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Layanan autentikasi belum siap. Periksa koneksi internetmu.")
        )
        return try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("Email ini belum terdaftar di FiksFaham."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Format email tidak valid."))
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("Koneksi jaringan gagal. Periksa koneksi internetmu."))
        } catch (e: Throwable) {
            val msg = e.message.orEmpty()
            val friendlyMsg = when {
                msg.contains("network", ignoreCase = true) || msg.contains("timeout", ignoreCase = true) ->
                    "Koneksi jaringan gagal. Periksa koneksi internetmu."
                msg.contains("user", ignoreCase = true) ->
                    "Email ini belum terdaftar di FiksFaham."
                msg.contains("email", ignoreCase = true) ->
                    "Format email tidak valid."
                else -> e.message ?: "Gagal mengirim email reset kata sandi."
            }
            Result.failure(Exception(friendlyMsg))
        }
    }

    override suspend fun reauthenticate(password: String): Result<Unit> {
        val user = auth?.currentUser ?: return Result.failure(
            IllegalStateException("Sesi telah kedaluwarsa. Silakan keluar dan masuk kembali.")
        )
        val email = user.email ?: return Result.failure(
            IllegalStateException("Email akun tidak ditemukan.")
        )
        return try {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Kata sandi lama salah. Harap periksa kembali sandi Anda."))
        } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
            Result.failure(Exception("Sesi telah kedaluwarsa. Silakan keluar dan masuk kembali."))
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("Koneksi jaringan gagal. Periksa koneksi internetmu."))
        } catch (e: Throwable) {
            val msg = e.message.orEmpty()
            val friendly = when {
                msg.contains("password", ignoreCase = true) -> "Kata sandi lama salah. Harap periksa kembali sandi Anda."
                msg.contains("recent", ignoreCase = true) -> "Sesi telah kedaluwarsa. Silakan keluar dan masuk kembali."
                else -> e.message ?: "Autentikasi ulang gagal."
            }
            Result.failure(Exception(friendly))
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        val user = auth?.currentUser ?: return Result.failure(
            IllegalStateException("Sesi telah kedaluwarsa. Silakan keluar dan masuk kembali.")
        )
        return try {
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Kata sandi baru terlalu lemah. Minimal 6 karakter."))
        } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
            Result.failure(Exception("Sesi telah kedaluwarsa. Silakan keluar dan masuk kembali."))
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Gagal memperbarui kata sandi."))
        }
    }

    override suspend fun changePasswordWithReauth(oldPassword: String, newPassword: String): Result<Unit> {
        val user = auth?.currentUser ?: return Result.failure(
            IllegalStateException("Sesi telah kedaluwarsa. Silakan keluar dan masuk kembali.")
        )
        val email = user.email ?: return Result.failure(
            IllegalStateException("Email akun tidak ditemukan.")
        )
        if (newPassword.length < 6) {
            return Result.failure(Exception("Kata sandi baru terlalu lemah. Minimal 6 karakter."))
        }

        // 1. Reautentikasi dengan sandi lama terlebih dahulu
        val reauthRes = try {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, oldPassword)
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return Result.failure(Exception("Kata sandi lama salah. Harap periksa kembali sandi Anda."))
        } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
            return Result.failure(Exception("Sesi telah kedaluwarsa. Silakan keluar dan masuk kembali."))
        } catch (e: Throwable) {
            val msg = e.message.orEmpty()
            val friendly = when {
                msg.contains("password", ignoreCase = true) -> "Kata sandi lama salah. Harap periksa kembali sandi Anda."
                msg.contains("recent", ignoreCase = true) -> "Sesi telah kedaluwarsa. Silakan keluar dan masuk kembali."
                else -> e.message ?: "Gagal memverifikasi kata sandi lama."
            }
            return Result.failure(Exception(friendly))
        }

        // 2. Pembaruan kata sandi
        return try {
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Kata sandi baru terlalu lemah. Minimal 6 karakter."))
        } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
            Result.failure(Exception("Sesi telah kedaluwarsa. Silakan keluar dan masuk kembali."))
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Gagal memperbarui kata sandi."))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Layanan autentikasi belum siap. Periksa koneksi internetmu.")
        )
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw IllegalStateException("Gagal masuk dengan akun Google.")
            Result.success(user.toAuthUser())
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("Koneksi jaringan gagal. Periksa koneksi internetmu."))
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Gagal masuk menggunakan Google."))
        }
    }

    override suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> {
        val user = auth?.currentUser ?: return Result.failure(
            IllegalStateException("Sesi telah kedaluwarsa. Silakan keluar dan masuk kembali.")
        )
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("Koneksi jaringan gagal. Periksa koneksi internetmu."))
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Autentikasi ulang Google gagal."))
        }
    }

    override suspend fun getCustomRoleClaim(): String? {
        return try {
            val user = auth?.currentUser ?: return null
            val token = user.getIdToken(false).await()
            token.claims["role"] as? String
        } catch (_: Throwable) {
            null
        }
    }

    override suspend fun verifyBeforeUpdateEmail(newEmail: String, password: String?): Result<Unit> {
        val user = auth?.currentUser ?: return Result.failure(
            IllegalStateException("Tidak ada pengguna aktif. Silakan masuk kembali.")
        )
        val trimmedEmail = newEmail.trim()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Format email baru tidak valid."))
        }
        if (!password.isNullOrBlank() && !user.email.isNullOrBlank()) {
            try {
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, password)
                user.reauthenticate(credential).await()
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                return Result.failure(Exception("Kata sandi konfirmasi salah."))
            } catch (e: Throwable) {
                return Result.failure(Exception(e.message ?: "Gagal melakukan verifikasi keamanan."))
            }
        }
        return try {
            user.verifyBeforeUpdateEmail(trimmedEmail).await()
            Result.success(Unit)
        } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
            Result.failure(Exception("Demi keamanan, masukkan kata sandi Anda atau masuk ulang sebelum mengganti email."))
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Gagal mengirim tautan verifikasi ke email baru."))
        }
    }

    override suspend fun deleteAuthUser(): Result<Unit> {
        val user = auth?.currentUser ?: return Result.failure(
            IllegalStateException("Tidak ada pengguna aktif untuk dihapus.")
        )
        return try {
            user.delete().await()
            Result.success(Unit)
        } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
            Result.failure(Exception("Sesi telah kedaluwarsa. Silakan keluar dan masuk kembali untuk menghapus akun."))
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Gagal menghapus akun autentikasi pengguna."))
        }
    }

    override fun signOut() {
        try {
            auth?.signOut()
        } catch (t: Throwable) {
            Log.w("FirebaseAuthService", "Error signing out: ${t.message}")
        }
    }
}
