package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthService
import com.example.data.auth.AuthUser
import com.example.data.db.FiksFahamDatabase
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.MateriEntity
import com.example.data.repository.FiksFahamRepository
import com.example.util.CertificateConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PortalContractPhaseATest {

    private class FakePortalAuthService : AuthService {
        var fakeUser: AuthUser? = null
        var fakeClaimRole: String? = null
        var googleReauthCalled = false

        override val currentUser: AuthUser?
            get() = fakeUser

        override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
            val u = AuthUser(
                uid = "uid_email_${email.hashCode()}",
                email = email,
                displayName = "Pengguna Email",
                providerIds = listOf("password")
            )
            fakeUser = u
            return Result.success(u)
        }

        override suspend fun signUpWithEmail(
            name: String,
            email: String,
            password: String,
            instansi: String,
            role: String
        ): Result<AuthUser> {
            val u = AuthUser(
                uid = "uid_reg_${email.hashCode()}",
                email = email,
                displayName = name,
                providerIds = listOf("password")
            )
            fakeUser = u
            return Result.success(u)
        }

        override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
            val u = AuthUser(
                uid = "uid_google_1",
                email = "staf@fiksfaham.id",
                displayName = "Staf Google",
                photoUrl = "https://example.com/photo.jpg",
                providerIds = listOf("google.com")
            )
            fakeUser = u
            return Result.success(u)
        }

        override suspend fun sendPasswordResetEmail(email: String) = Result.success(Unit)
        override suspend fun updatePassword(newPassword: String) = Result.success(Unit)
        override suspend fun reauthenticate(password: String) = Result.success(Unit)
        override suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> {
            googleReauthCalled = idToken.isNotBlank()
            return if (googleReauthCalled) Result.success(Unit) else Result.failure(IllegalArgumentException("Empty token"))
        }
        override suspend fun changePasswordWithReauth(oldPassword: String, newPassword: String) = Result.success(Unit)
        override suspend fun getCustomRoleClaim(): String? = fakeClaimRole
        override suspend fun deleteAuthUser(): Result<Unit> {
            fakeUser = null
            return Result.success(Unit)
        }
        override fun signOut() {
            fakeUser = null
        }
    }

    private class FakeCapstoneFirestore : CapstoneFirestoreService {
        var reviewerAllowed = true
        var lastUpdatedStatus: String? = null
        var lastUpdatedNilai: Int? = null

        override suspend fun submit(submission: CapstoneSubmission): Result<Unit> = Result.success(Unit)
        override fun observeMine(uid: String, materiId: Int): Flow<CapstoneSubmission?> = flowOf(null)
        override suspend fun getSubmission(uid: String, materiId: Int): Result<CapstoneSubmission?> = Result.success(null)
        override suspend fun listSubmissions(filter: CapstoneFilter): Result<List<CapstoneSubmission>> = Result.success(emptyList())
        override suspend fun review(uid: String, materiId: Int, nilai: Int, feedback: String, mentorUid: String) = Result.success(Unit)
        override suspend fun reviewWithStatus(
            uid: String,
            materiId: Int,
            status: String,
            nilai: Int?,
            feedback: String,
            rubricSummary: String,
            mentorUid: String
        ): Result<Unit> {
            lastUpdatedStatus = status
            lastUpdatedNilai = nilai
            return Result.success(Unit)
        }
        override suspend fun isMentor(uid: String): Boolean = reviewerAllowed
    }

    private lateinit var app: Application
    private lateinit var db: FiksFahamDatabase

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(app, FiksFahamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun capstoneStatusUtils_normalizesAndroidAndWebPortalStatuses() {
        assertEquals("submitted", CapstoneStatusUtils.normalize("submitted"))
        assertEquals("submitted", CapstoneStatusUtils.normalize("pending"))
        assertEquals("under_review", CapstoneStatusUtils.normalize("under_review"))
        assertEquals("under_review", CapstoneStatusUtils.normalize("in_review"))
        assertEquals("revision_needed", CapstoneStatusUtils.normalize("revision_needed"))
        assertEquals("revision_needed", CapstoneStatusUtils.normalize("revision"))
        assertEquals("approved", CapstoneStatusUtils.normalize("approved"))
        assertEquals("approved", CapstoneStatusUtils.normalize("reviewed"))
        assertEquals("rejected", CapstoneStatusUtils.normalize("rejected"))
    }

    @Test
    fun capstoneStatusUtils_identifiesStaffRolesOnly() {
        assertTrue(CapstoneStatusUtils.isStaffRole("mentor"))
        assertTrue(CapstoneStatusUtils.isStaffRole("fasilitator"))
        assertTrue(CapstoneStatusUtils.isStaffRole("superadmin"))
        assertTrue(CapstoneStatusUtils.isStaffRole("admin"))
        assertFalse(CapstoneStatusUtils.isStaffRole("mahasiswa"))
        assertFalse(CapstoneStatusUtils.isStaffRole(null))
    }

    @Test
    fun certificateConfig_calculatesPredikatAndHandlesWebPortalStatuses() {
        assertEquals("Unggul", CertificateConfig.calculatePredikat(96))
        assertEquals("Mahir", CertificateConfig.calculatePredikat(88))
        assertEquals("Cakap", CertificateConfig.calculatePredikat(75))
        assertEquals("", CertificateConfig.calculatePredikat(70))

        val materis = (1..5).map { MateriEntity(id = it, judul = "M$it", deskripsi = "", urutan = it) }
        val passedAttempts = (1..5).map { id ->
            val aId = if (id == 1) 20 else id * 100 + 20
            AssessmentAttemptEntity(id = id, userId = 1, assessmentId = aId, attemptNo = 1, score = 85, isPassed = true)
        }

        val revRes = CertificateConfig.evaluateGraduationEligibility(
            materis,
            passedAttempts,
            listOf(CapstoneSubmissionEntity(userId = 1, materiId = 0, status = "revision", nilai = 65))
        )
        assertEquals(
            CertificateConfig.ReasonKey.CAPSTONE_REVISION_NEEDED,
            (revRes.status as CertificateConfig.EligibilityStatus.Locked).reasonKey
        )

        val rejRes = CertificateConfig.evaluateGraduationEligibility(
            materis,
            passedAttempts,
            listOf(CapstoneSubmissionEntity(userId = 1, materiId = 0, status = "rejected", nilai = 40))
        )
        assertEquals(
            CertificateConfig.ReasonKey.CAPSTONE_REJECTED,
            (rejRes.status as CertificateConfig.EligibilityStatus.Locked).reasonKey
        )

        val appRes = CertificateConfig.evaluateGraduationEligibility(
            materis,
            passedAttempts,
            listOf(CapstoneSubmissionEntity(userId = 1, materiId = 0, status = "approved", nilai = 90))
        )
        assertTrue(appRes.status is CertificateConfig.EligibilityStatus.Eligible)
    }

    @Test
    fun capstoneRepository_acceptsWebStatusesForReview() = runTest {
        val fakeService = FakeCapstoneFirestore()
        val repo = CapstoneRepository(firestoreService = fakeService)

        val res = repo.review(
            uid = "u1",
            materiId = 0,
            nilai = 45,
            feedback = "Pelanggaran orisinalitas",
            mentorUid = "staff_1",
            status = "rejected"
        )
        assertTrue(res.isSuccess)
        assertEquals("rejected", fakeService.lastUpdatedStatus)
        assertEquals(45, fakeService.lastUpdatedNilai)
    }

    @Test
    fun fiksFahamRepository_resolvesCustomClaimRoleAndGoogleSession() = runTest {
        val fakeAuth = FakePortalAuthService().apply {
            fakeClaimRole = "superadmin"
        }
        val repo = FiksFahamRepository(db.dao(), fakeAuth)

        val loginRes = repo.loginWithGoogle("mock_google_id_token")
        assertTrue(loginRes.isSuccess)
        val user = loginRes.getOrThrow()
        assertEquals("superadmin", user.role)
        assertEquals("https://example.com/photo.jpg", user.avatarUrl)
        assertTrue(repo.isGoogleOnlySession())
        assertTrue(repo.hasActiveAuthSession())

        val deleteRes = repo.deleteUserAccount(password = "", googleIdToken = "reauth_token_123")
        assertTrue(deleteRes.isSuccess)
        assertTrue(fakeAuth.googleReauthCalled)
        assertFalse(repo.hasActiveAuthSession())
    }
}
