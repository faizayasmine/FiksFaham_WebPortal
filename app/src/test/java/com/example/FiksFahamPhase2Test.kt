package com.example

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.auth.AuthService
import com.example.data.auth.AuthUser
import com.example.data.db.FiksFahamDatabase
import com.example.data.repository.FiksFahamRepository
import com.example.ui.FiksFahamViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Test fake implementation for AuthService to test repository and viewmodel logic
 * without depending on live network or uninitialized Firebase instances.
 */
class FakeAuthService : AuthService {
    private val users = mutableMapOf<String, Pair<AuthUser, String>>() // email -> (AuthUser, password)
    private var activeUser: AuthUser? = null

    override val currentUser: AuthUser?
        get() = activeUser

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        val entry = users[email.trim().lowercase()]
            ?: return Result.failure(Exception("Akun dengan email ini belum terdaftar."))
        if (entry.second != password) {
            return Result.failure(Exception("Kata sandi salah atau format email tidak valid."))
        }
        activeUser = entry.first
        return Result.success(entry.first)
    }

    override suspend fun signUpWithEmail(
        name: String,
        email: String,
        password: String,
        instansi: String,
        role: String
    ): Result<AuthUser> {
        val cleanEmail = email.trim().lowercase()
        if (users.containsKey(cleanEmail)) {
            return Result.failure(Exception("Email ini sudah digunakan oleh akun lain. Silakan langsung masuk."))
        }
        val uid = "fake_uid_${System.currentTimeMillis()}_${users.size}"
        val user = AuthUser(uid = uid, email = cleanEmail, displayName = name.trim())
        users[cleanEmail] = user to password
        activeUser = user
        return Result.success(user)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        if (!users.containsKey(cleanEmail)) {
            return Result.failure(Exception("Email ini belum terdaftar di FiksFaham."))
        }
        return Result.success(Unit)
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        val current = activeUser ?: return Result.failure(Exception("Tidak ada sesi login aktif."))
        val pair = users[current.email] ?: return Result.failure(Exception("Pengguna tidak ditemukan."))
        users[current.email] = pair.first to newPassword
        return Result.success(Unit)
    }

    override suspend fun reauthenticate(password: String): Result<Unit> {
        val current = activeUser ?: return Result.failure(Exception("Tidak ada sesi login aktif."))
        val pair = users[current.email] ?: return Result.failure(Exception("Pengguna tidak ditemukan."))
        if (pair.second != password) {
            return Result.failure(Exception("Kata sandi salah."))
        }
        return Result.success(Unit)
    }

    override suspend fun changePasswordWithReauth(oldPassword: String, newPassword: String): Result<Unit> {
        val reauth = reauthenticate(oldPassword)
        if (reauth.isFailure) return reauth
        return updatePassword(newPassword)
    }

    override suspend fun deleteAuthUser(): Result<Unit> {
        val current = activeUser ?: return Result.failure(Exception("Tidak ada sesi login aktif."))
        users.remove(current.email)
        activeUser = null
        return Result.success(Unit)
    }

    override fun signOut() {
        activeUser = null
    }
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class FiksFahamPhase2Test {

    private lateinit var database: FiksFahamDatabase
    private lateinit var fakeAuthService: FakeAuthService
    private lateinit var repository: FiksFahamRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FiksFahamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        fakeAuthService = FakeAuthService()
        repository = FiksFahamRepository(database.dao(), fakeAuthService)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testRoomMigration6To7PreservesUserDataAndRemovesPassword() {
        // 1. Create v6 SQLite database schema manually
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("migration-test-db")
                .callback(object : SupportSQLiteOpenHelper.Callback(6) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE users (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                nama TEXT NOT NULL,
                                email TEXT NOT NULL,
                                asalInstansi TEXT NOT NULL,
                                avatarUrl TEXT NOT NULL,
                                role TEXT NOT NULL DEFAULT 'mahasiswa',
                                password TEXT NOT NULL DEFAULT 'fiks12345',
                                level INTEGER NOT NULL DEFAULT 0
                            )
                            """.trimIndent()
                        )
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )
        val db = helper.writableDatabase

        db.execSQL(
            """
            INSERT INTO users (id, nama, email, asalInstansi, avatarUrl, role, password, level)
            VALUES (101, 'Mahasiswa Unesa', 'mhs.unesa@test.ac.id', 'UNESA Surabaya', 'avatar_2', 'mahasiswa', 'oldPassword123', 3)
            """.trimIndent()
        )

        // 2. Run Migration 6 -> 7
        FiksFahamDatabase.MIGRATION_6_7.migrate(db)

        // 3. Query migrated table
        val cursor = db.query("SELECT * FROM users WHERE id = 101")
        assertTrue("Migrated record must exist", cursor.moveToFirst())

        val namaIndex = cursor.getColumnIndex("nama")
        val emailIndex = cursor.getColumnIndex("email")
        val roleIndex = cursor.getColumnIndex("role")
        val levelIndex = cursor.getColumnIndex("level")
        val uidIndex = cursor.getColumnIndex("firebaseUid")
        val passwordIndex = cursor.getColumnIndex("password")

        assertEquals("Mahasiswa Unesa", cursor.getString(namaIndex))
        assertEquals("mhs.unesa@test.ac.id", cursor.getString(emailIndex))
        assertEquals("mahasiswa", cursor.getString(roleIndex))
        assertEquals(3, cursor.getInt(levelIndex))
        assertEquals("", cursor.getString(uidIndex))
        assertEquals("Password column must be removed from users table in Room v7", -1, passwordIndex)

        cursor.close()
        db.close()
    }

    @Test
    fun testRoomMigration7To8ClearsSubMateri5Video() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("migration-test-7-8-db")
                .callback(object : SupportSQLiteOpenHelper.Callback(7) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE module_videos (
                                id INTEGER PRIMARY KEY NOT NULL,
                                moduleId INTEGER NOT NULL,
                                judul TEXT NOT NULL,
                                youtubeUrl TEXT NOT NULL,
                                videoId TEXT NOT NULL,
                                channelName TEXT NOT NULL,
                                durasiMenit INTEGER NOT NULL
                            )
                            """.trimIndent()
                        )
                        db.execSQL(
                            """
                            INSERT INTO module_videos VALUES
                            (5, 5, 'Prinsip Desain Visual', 'https://www.youtube.com/watch?v=F0PU_t6YQEI', 'F0PU_t6YQEI', 'FiksFaham Academy', 12)
                            """.trimIndent()
                        )
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )
        val db = helper.writableDatabase

        FiksFahamDatabase.MIGRATION_7_8.migrate(db)

        val cursor = db.query("SELECT youtubeUrl, videoId, channelName FROM module_videos WHERE id = 5")
        assertTrue("Record must exist", cursor.moveToFirst())
        assertEquals("", cursor.getString(0))
        assertEquals("", cursor.getString(1))
        assertEquals("", cursor.getString(2))

        cursor.close()
        db.close()
    }

    @Test
    fun testFirebaseAuthAndAccountIsolation() = runBlocking {
        repository.seedDatabaseIfEmpty()

        // 1. Register Mahasiswa 1 with Auth
        val mhsResult = repository.registerWithAuth(
            name = "Rina Mahasiswa",
            email = "rina@unesa.ac.id",
            password = "password123",
            instansi = "UNESA"
        )
        assertTrue("Registration must succeed", mhsResult.isSuccess)

        val mhsUser = repository.user.first()
        assertNotNull(mhsUser)
        assertEquals("Rina Mahasiswa", mhsUser?.nama)
        assertEquals("rina@unesa.ac.id", mhsUser?.email)
        assertEquals("mahasiswa", mhsUser?.role)
        assertTrue("Firebase UID must be linked", mhsUser?.firebaseUid?.startsWith("fake_uid_") == true)

        val mhsUserId = mhsUser!!.id

        // 2. Register Mahasiswa 2 (Semua akun baru otomatis mahasiswa)
        val secondResult = repository.registerWithAuth(
            name = "Siti Mahasiswa",
            email = "siti@unesa.ac.id",
            password = "password123",
            instansi = "UNESA Studio"
        )
        assertTrue("Second registration must succeed", secondResult.isSuccess)

        val secondUser = repository.user.first()
        assertNotNull(secondUser)
        assertEquals("Siti Mahasiswa", secondUser?.nama)
        assertEquals("mahasiswa", secondUser?.role)
        assertTrue("New account gets its own distinct userId", secondUser!!.id != mhsUserId)

        // 3. Switch back to Mahasiswa 1 via Login
        val loginResult = repository.loginWithAuth("rina@unesa.ac.id", "password123")
        assertTrue("Login must succeed with correct password", loginResult.isSuccess)

        val switchedUser = repository.user.first()
        assertEquals(mhsUserId, switchedUser?.id)
        assertEquals("Rina Mahasiswa", switchedUser?.nama)

        // 4. Verify login fails with wrong password
        val badLogin = repository.loginWithAuth("rina@unesa.ac.id", "wrongPassword")
        assertTrue("Login with wrong password must fail", badLogin.isFailure)
    }

    @Test
    fun testPasswordResetFlow() = runBlocking {
        repository.seedDatabaseIfEmpty()
        repository.registerWithAuth("Budi", "budi@test.com", "secretPass123", "UNESA")

        val resetSuccess = repository.sendPasswordReset("budi@test.com")
        assertTrue("Reset email request for registered user must succeed", resetSuccess.isSuccess)

        val resetUnknown = repository.sendPasswordReset("unknown@test.com")
        assertTrue("Reset email request for unknown user must return failure", resetUnknown.isFailure)
    }
}
