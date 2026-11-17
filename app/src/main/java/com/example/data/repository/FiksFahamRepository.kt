package com.example.data.repository

import com.example.data.db.FiksFahamDao
import com.example.data.db.InitialData
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentCooldownEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.BookmarkedModuleEntity
import com.example.data.model.BootcampEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.CoinGrantEntity
import com.example.data.model.CoinLeaderboardEntryDoc
import com.example.util.AssessmentCooldownConfig
import com.example.util.CourseProgressUtils
import kotlinx.coroutines.flow.map
import com.example.data.model.CreatorEntity
import com.example.data.model.CuratedVideoEntity
import com.example.data.model.MateriEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity
import com.example.data.model.ModuleSectionEntity
import com.example.data.model.ModuleVideoEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.UserEntity
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import com.example.util.LeaderboardUtils
import com.example.data.auth.AuthService
import com.example.data.auth.FirebaseAuthService
import com.example.CapstoneSubmission

class FiksFahamRepository(
    private val dao: FiksFahamDao,
    private val authService: AuthService = FirebaseAuthService()
) {

    private val _currentUserId = MutableStateFlow(1)
    val currentUserId: StateFlow<Int> = _currentUserId.asStateFlow()

    fun hasActiveAuthSession(): Boolean = authService.currentUser != null

    fun isGoogleOnlySession(): Boolean {
        val providers = authService.currentUser?.providerIds.orEmpty()
        return providers.isNotEmpty() && "password" !in providers
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val user: Flow<UserEntity?> = _currentUserId.flatMapLatest { userId ->
        dao.getUserById(userId)
    }

    val materis: Flow<List<MateriEntity>> = dao.getAllMateri()
    val modules: Flow<List<ModuleEntity>> = dao.getAllModules()
    val assessments: Flow<List<AssessmentEntity>> = dao.getAllAssessments()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allCoinGrants: Flow<List<CoinGrantEntity>> = _currentUserId.flatMapLatest { userId ->
        dao.getAllCoinGrantsForUser(userId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val bookmarkedModuleIds: Flow<Set<Int>> = _currentUserId.flatMapLatest { userId ->
        dao.getBookmarkedModules(userId).map { list -> list.map { it.moduleId }.toSet() }
    }

    fun getModulesByMateri(materiId: Int): Flow<List<ModuleEntity>> = dao.getModulesByMateri(materiId)

    @OptIn(ExperimentalCoroutinesApi::class)
    val progressList: Flow<List<ModuleProgressEntity>> = _currentUserId.flatMapLatest { userId ->
        dao.getAllProgressForUser(userId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val allAttempts: Flow<List<AssessmentAttemptEntity>> = _currentUserId.flatMapLatest { userId ->
        dao.getAllAttemptsForUser(userId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val capstoneSubmission: Flow<CapstoneSubmissionEntity?> = _currentUserId.flatMapLatest { userId ->
        dao.getCapstoneSubmissionForUser(userId)
    }

    val allCapstoneSubmissions: Flow<List<CapstoneSubmissionEntity>> = dao.getAllCapstoneSubmissions()

    val notifications: Flow<List<NotificationEntity>> = dao.getNotifications()
    val unreadNotificationCount: Flow<Int> = dao.getUnreadNotificationCount()

    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val firstUser = dao.getFirstUser()
        if (firstUser == null) {
            val defaultUser = InitialData.getDefaultUser()
            val insertedId = dao.insertUser(defaultUser).toInt()
            val activeId = if (insertedId > 0) insertedId else 1

            dao.insertMateri(InitialData.getMateri())
            dao.insertModules(InitialData.getModules())
            dao.insertSections(InitialData.getModuleSections())
            dao.insertVideos(InitialData.getModuleVideos())
            dao.insertAssessments(InitialData.getAssessments())
            dao.insertQuestions(InitialData.getQuestions())
            dao.insertProgress(InitialData.getDefaultProgress(activeId))
            dao.insertNotifications(InitialData.getNotifications())
            dao.insertCreators(InitialData.getCreators())
            dao.insertCuratedVideos(InitialData.getCuratedVideos())
            dao.insertBootcamps(InitialData.getBootcamps())

            _currentUserId.value = activeId
        } else {
            // Sinkronkan kurikulum 5 Materi Dunia Desain & metadata video pembelajaran saat upgrade
            dao.insertMateri(InitialData.getMateri())
            dao.insertModules(InitialData.getModules())
            dao.insertSections(InitialData.getModuleSections())
            dao.insertVideos(InitialData.getModuleVideos())
            dao.insertAssessments(InitialData.getAssessments())
            dao.insertQuestions(InitialData.getQuestions())

            // Check if there is an active Firebase authenticated user
            val currentAuth = authService.currentUser
            if (currentAuth != null && currentAuth.uid.isNotBlank()) {
                val authDbUser = dao.getUserByFirebaseUid(currentAuth.uid)
                    ?: (if (currentAuth.email.isNotBlank()) dao.getUserByEmail(currentAuth.email) else null)
                if (authDbUser != null) {
                    ensureProgressForAllModules(authDbUser.id)
                    syncPosttestLocksFromFirestore(authDbUser.id)
                    _currentUserId.value = authDbUser.id
                    return@withContext
                }
            }
            ensureProgressForAllModules(firstUser.id)
            _currentUserId.value = firstUser.id
        }
    }

    private suspend fun ensureProgressForAllModules(userId: Int) {
        val existing = dao.getAllProgressForUserSync(userId)
        val existingIds = existing.map { it.moduleId }.toSet()
        val missing = InitialData.getDefaultProgress(userId).filter { it.moduleId !in existingIds }
        if (missing.isNotEmpty()) {
            dao.insertProgress(missing)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val authResult = authService.signInWithGoogle(idToken)
        if (authResult.isFailure) {
            return@withContext Result.failure(authResult.exceptionOrNull() ?: Exception("Gagal masuk dengan Google."))
        }
        val authUser = authResult.getOrThrow()
        val cleanEmail = authUser.email.trim()
        val (resolvedRole, isDisabled) = resolveStaffRoleAndStatusFromCloud(authUser.uid)
        if (isDisabled) {
            authService.signOut()
            return@withContext Result.failure(IllegalStateException("Akun Anda sedang dinonaktifkan oleh pengelola portal. Hubungi Super Admin."))
        }

        val existingByUid = if (authUser.uid.isNotBlank()) dao.getUserByFirebaseUid(authUser.uid) else null
        val existing = existingByUid ?: (if (cleanEmail.isNotBlank()) dao.getUserByEmail(cleanEmail) else null)

        val activeUser = if (existing != null) {
            val resolvedAvatar = existing.avatarUrl.takeIf { it.isNotBlank() && it != "avatar_1" }
                ?: authUser.photoUrl.ifBlank { existing.avatarUrl }
            val updated = existing.copy(
                nama = existing.nama.ifBlank { authUser.displayName.ifBlank { "Pengguna FiksFaham" } },
                email = cleanEmail.ifBlank { existing.email },
                avatarUrl = resolvedAvatar,
                role = resolvedRole,
                firebaseUid = authUser.uid.ifBlank { existing.firebaseUid }
            )
            dao.updateUser(updated)
            upsertFirestoreUserProfile(
                uid = authUser.uid,
                nama = updated.nama,
                email = updated.email,
                instansi = updated.asalInstansi,
                avatarUrl = updated.avatarUrl,
                providerIds = authUser.providerIds,
                isNewUser = false
            )
            ensureProgressForAllModules(updated.id)
            syncPosttestLocksFromFirestore(updated.id)
            val completedCount = dao.getAllProgressForUserSync(updated.id).count { it.isFinished || it.isCompleted }
            dao.updateUserLevel(updated.id, completedCount)
            _currentUserId.value = updated.id
            updated.copy(level = completedCount)
        } else {
            val resolvedName = authUser.displayName.ifBlank { cleanEmail.substringBefore("@").ifBlank { "Pengguna FiksFaham" } }
            val resolvedAvatar = authUser.photoUrl.ifBlank { "avatar_1" }
            val newUser = UserEntity(
                id = 0,
                nama = resolvedName,
                email = cleanEmail,
                asalInstansi = "Universitas / Instansi",
                avatarUrl = resolvedAvatar,
                role = resolvedRole,
                firebaseUid = authUser.uid,
                level = 0
            )
            val newId = dao.insertUser(newUser).toInt()
            dao.insertProgress(InitialData.getDefaultProgress(newId))
            upsertFirestoreUserProfile(
                uid = authUser.uid,
                nama = newUser.nama,
                email = newUser.email,
                instansi = newUser.asalInstansi,
                avatarUrl = authUser.photoUrl,
                providerIds = authUser.providerIds,
                isNewUser = true
            )
            _currentUserId.value = newId
            newUser.copy(id = newId)
        }
        Result.success(activeUser)
    }

    /**
     * Resolusi peran terpadu lintas Android & Web Portal:
     * Custom Claims -> staff/{uid} (aktif == true) -> legacy admins/{uid} & mentors/{uid}.
     * Sekaligus memeriksa flag disabled pada users/{uid}.
     */
    private suspend fun resolveStaffRoleAndStatusFromCloud(uid: String): Pair<String, Boolean> {
        if (uid.isBlank()) return "mahasiswa" to false
        var isDisabled = false
        var firestore: FirebaseFirestore? = null
        try {
            firestore = FirebaseFirestore.getInstance()
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                isDisabled = userDoc.getBoolean("disabled") == true
            }
        } catch (t: Throwable) {
            Log.w("FiksFahamRepository", "Skipped users/{uid} status check: ${t.message}")
        }
        if (isDisabled) return "mahasiswa" to true

        val claimRole = runCatching { authService.getCustomRoleClaim()?.trim()?.lowercase() }.getOrNull()
        when (claimRole) {
            "superadmin" -> return "superadmin" to false
            "fasilitator", "mentor" -> return claimRole to false
        }

        if (firestore != null) {
            try {
                val staffDoc = firestore.collection("staff").document(uid).get().await()
                if (staffDoc.exists()) {
                    val isAktif = staffDoc.getBoolean("aktif") ?: true
                    val staffRole = staffDoc.getString("role")?.trim()?.lowercase()
                    if (isAktif) {
                        when (staffRole) {
                            "superadmin" -> return "superadmin" to false
                            "fasilitator", "mentor" -> return staffRole to false
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.w("FiksFahamRepository", "Failed to check staff/{uid}: ${t.message}")
            }

            try {
                val adminDoc = firestore.collection("admins").document(uid).get().await()
                if (adminDoc.exists()) return "superadmin" to false
            } catch (t: Throwable) {
                Log.w("FiksFahamRepository", "Failed to check admins/{uid}: ${t.message}")
            }

            try {
                val mentorDoc = firestore.collection("mentors").document(uid).get().await()
                if (mentorDoc.exists()) return "mentor" to false
            } catch (t: Throwable) {
                Log.w("FiksFahamRepository", "Failed to check mentors/{uid}: ${t.message}")
            }
        }
        return "mahasiswa" to false
    }

    /**
     * Sinkronisasi profil aman ke users/{uid} saat Daftar/Login agar tersedia di Portal Super Admin.
     * Sengaja TIDAK menulis field `role` atau `disabled` dari sisi klien.
     */
    private suspend fun upsertFirestoreUserProfile(
        uid: String,
        nama: String,
        email: String,
        instansi: String,
        avatarUrl: String?,
        providerIds: List<String>,
        isNewUser: Boolean
    ) {
        if (uid.isBlank()) return
        try {
            val firestore = FirebaseFirestore.getInstance()
            val payload = mutableMapOf<String, Any>(
                "uid" to uid,
                "nama" to nama,
                "email" to email,
                "asalInstansi" to instansi,
                "lastLoginAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            if (providerIds.isNotEmpty()) {
                payload["authProviders"] = providerIds
            }
            if (!avatarUrl.isNullOrBlank()) {
                payload["avatarUrl"] = avatarUrl
            }
            if (isNewUser) {
                payload["createdAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
            }
            firestore.collection("users").document(uid)
                .set(payload, com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (t: Throwable) {
            Log.w("FiksFahamRepository", "Optional Firestore user profile upsert skipped: ${t.message}")
        }
    }

    suspend fun loginWithAuth(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val authResult = authService.signInWithEmail(cleanEmail, password)
        if (authResult.isFailure) {
            return@withContext Result.failure(authResult.exceptionOrNull() ?: Exception("Gagal masuk."))
        }
        val authUser = authResult.getOrThrow()

        // Resolusi peran staf (Custom Claim / staff/{uid} / admins / mentors) & cek status akun
        val (resolvedRole, isDisabled) = resolveStaffRoleAndStatusFromCloud(authUser.uid)
        if (isDisabled) {
            authService.signOut()
            return@withContext Result.failure(IllegalStateException("Akun Anda sedang dinonaktifkan oleh pengelola portal. Hubungi Super Admin."))
        }

        // Link with local Room database preserving user progress
        val existingByUid = if (authUser.uid.isNotBlank()) dao.getUserByFirebaseUid(authUser.uid) else null
        val existing = existingByUid ?: dao.getUserByEmail(cleanEmail)

        val activeUser = if (existing != null) {
            val updated = existing.copy(
                role = resolvedRole,
                firebaseUid = authUser.uid.ifBlank { existing.firebaseUid }
            )
            dao.updateUser(updated)
            upsertFirestoreUserProfile(
                uid = authUser.uid,
                nama = updated.nama,
                email = updated.email,
                instansi = updated.asalInstansi,
                avatarUrl = updated.avatarUrl,
                providerIds = authUser.providerIds,
                isNewUser = false
            )
            val userProgress = dao.getAllProgressForUser(updated.id).firstOrNull()
            if (userProgress.isNullOrEmpty()) {
                dao.insertProgress(InitialData.getDefaultProgress(updated.id))
            }
            // Sinkronisasi Level = jumlah Sub Materi yang selesai
            val completedCount = dao.getAllProgressForUserSync(updated.id).count { it.isCompleted }
            dao.updateUserLevel(updated.id, completedCount)
            syncPosttestLocksFromFirestore(updated.id)

            _currentUserId.value = updated.id
            updated.copy(level = completedCount)
        } else {
            val name = authUser.displayName.ifBlank {
                cleanEmail.substringBefore("@")
                    .replace(".", " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { part ->
                        part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }.ifBlank { "Pengguna FiksFaham" }
            }
            val newUser = UserEntity(
                id = 0,
                nama = name,
                email = cleanEmail,
                asalInstansi = "Universitas / Instansi",
                role = resolvedRole,
                firebaseUid = authUser.uid,
                level = 0
            )
            val newId = dao.insertUser(newUser).toInt()
            dao.insertProgress(InitialData.getDefaultProgress(newId))
            upsertFirestoreUserProfile(
                uid = authUser.uid,
                nama = newUser.nama,
                email = newUser.email,
                instansi = newUser.asalInstansi,
                avatarUrl = authUser.photoUrl,
                providerIds = authUser.providerIds,
                isNewUser = true
            )
            _currentUserId.value = newId
            newUser.copy(id = newId)
        }
        Result.success(activeUser)
    }

    suspend fun registerWithAuth(
        name: String,
        email: String,
        password: String,
        instansi: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val cleanName = name.trim()
        val cleanInstansi = instansi.trim()
        val defaultRole = "mahasiswa" // Semua akun baru otomatis mahasiswa

        val authResult = authService.signUpWithEmail(cleanName, cleanEmail, password, cleanInstansi, defaultRole)
        if (authResult.isFailure) {
            return@withContext Result.failure(authResult.exceptionOrNull() ?: Exception("Gagal mendaftar."))
        }
        val authUser = authResult.getOrThrow()

        val existingByUid = if (authUser.uid.isNotBlank()) dao.getUserByFirebaseUid(authUser.uid) else null
        val existing = existingByUid ?: dao.getUserByEmail(cleanEmail)

        val activeUser = if (existing != null) {
            val updated = existing.copy(
                nama = cleanName.ifBlank { existing.nama },
                asalInstansi = cleanInstansi.ifBlank { existing.asalInstansi },
                role = defaultRole,
                firebaseUid = authUser.uid
            )
            dao.updateUser(updated)
            upsertFirestoreUserProfile(
                uid = authUser.uid,
                nama = updated.nama,
                email = updated.email,
                instansi = updated.asalInstansi,
                avatarUrl = updated.avatarUrl,
                providerIds = authUser.providerIds,
                isNewUser = true
            )
            val userProgress = dao.getAllProgressForUser(existing.id).firstOrNull()
            if (userProgress.isNullOrEmpty()) {
                dao.insertProgress(InitialData.getDefaultProgress(existing.id))
            }
            _currentUserId.value = existing.id
            updated
        } else {
            val newUser = UserEntity(
                id = 0,
                nama = cleanName,
                email = cleanEmail,
                asalInstansi = cleanInstansi,
                role = defaultRole,
                firebaseUid = authUser.uid,
                level = 0
            )
            val newId = dao.insertUser(newUser).toInt()
            dao.insertProgress(InitialData.getDefaultProgress(newId))
            upsertFirestoreUserProfile(
                uid = authUser.uid,
                nama = newUser.nama,
                email = newUser.email,
                instansi = newUser.asalInstansi,
                avatarUrl = null,
                providerIds = authUser.providerIds,
                isNewUser = true
            )
            _currentUserId.value = newId
            newUser.copy(id = newId)
        }
        Result.success(activeUser)
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        authService.sendPasswordResetEmail(email.trim())
    }

    fun getSections(moduleId: Int): Flow<List<ModuleSectionEntity>> =
        dao.getSectionsByModule(moduleId)

    fun getVideo(moduleId: Int): Flow<ModuleVideoEntity?> =
        dao.getVideoByModule(moduleId)

    suspend fun getQuestionsForAssessment(assessmentId: Int): List<QuestionEntity> =
        withContext(Dispatchers.IO) {
            dao.getQuestionsForAssessment(assessmentId)
        }

    suspend fun getQuestionById(questionId: Int): QuestionEntity? =
        withContext(Dispatchers.IO) {
            dao.getQuestionById(questionId)
        }

    suspend fun getAssessmentForMateri(materiId: Int, tipe: String): AssessmentEntity? = withContext(Dispatchers.IO) {
        dao.getAssessmentByMateriAndTipe(materiId, tipe)
    }

    suspend fun submitPretest(materiId: Int = 1, score: Int): AssessmentAttemptEntity = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value
        val pretestAssessment = dao.getAssessmentByMateriAndTipe(materiId, "pretest")
        val assessmentId = pretestAssessment?.id ?: 10
        val attempt = AssessmentAttemptEntity(
            userId = userId,
            assessmentId = assessmentId,
            attemptNo = 1,
            score = score,
            isPassed = true,
            incorrectQuestionIds = "",
            blockedUntil = 0L,
            submittedAt = System.currentTimeMillis()
        )
        dao.insertAttempt(attempt)

        // Unlock Sub Materi pertama untuk Materi ini setelah pretest selesai!
        val firstModule = dao.getFirstModuleInMateri(materiId)
        val firstModuleId = firstModule?.id ?: 1
        val firstProg = dao.getProgressForModule(userId, firstModuleId)
        if (firstProg != null) {
            dao.saveProgress(firstProg.copy(isUnlocked = true))
        } else {
            dao.saveProgress(ModuleProgressEntity(userId = userId, moduleId = firstModuleId, isUnlocked = true))
        }

        attempt
    }

    suspend fun submitPretest(score: Int): AssessmentAttemptEntity = submitPretest(1, score)

    suspend fun markMateriViewed(moduleId: Int) = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value
        val current = dao.getProgressForModule(userId, moduleId)
            ?: ModuleProgressEntity(userId = userId, moduleId = moduleId, isUnlocked = true)
        dao.saveProgress(current.copy(isUnlocked = true, materiViewed = true))
    }

    suspend fun markVideoWatched(moduleId: Int) = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value
        val current = dao.getProgressForModule(userId, moduleId)
            ?: ModuleProgressEntity(userId = userId, moduleId = moduleId, isUnlocked = true)
        dao.saveProgress(current.copy(isUnlocked = true, videoWatched = true))
    }

    /**
     * Menandai Sub Materi selesai (setelah membuka semua halaman materi & halaman video lalu menekan tombol Selesai).
     * Membuka akses Kuis Sub Materi terkait.
     */
    suspend fun markSubMateriFinished(moduleId: Int) = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value
        val current = dao.getProgressForModule(userId, moduleId)
            ?: ModuleProgressEntity(userId = userId, moduleId = moduleId, isUnlocked = true)
        val wasFinished = current.isFinished || current.isCompleted
        dao.saveProgress(
            current.copy(
                isUnlocked = true,
                materiViewed = true,
                videoWatched = true,
                isFinished = true,
                isCompleted = true,
                completedAt = if (wasFinished) current.completedAt else System.currentTimeMillis()
            )
        )
        val userAllProgress = dao.getAllProgressForUserSync(userId)
        val completedCount = userAllProgress.count { it.isFinished || it.isCompleted }
        dao.updateUserLevel(userId, completedCount)

        dao.getModuleById(moduleId)?.let { mod ->
            maybeGrantMateriCompletionCoin(userId, mod.materiId, System.currentTimeMillis())
        }
    }

    private suspend fun recordActivityCoinCapDelta(
        userId: Int,
        capKey: String,
        sourceType: String,
        targetMaxCoins: Int,
        txSuffix: String,
        now: Long
    ): Int {
        val existingCap = dao.getCoinGrant(userId, "cap_$capKey")?.coinsAwarded ?: 0
        val delta = LeaderboardUtils.computeCoinDelta(targetMaxCoins, existingCap)
        if (delta <= 0) return 0
        dao.upsertCoinGrant(
            CoinGrantEntity(
                userId = userId,
                grantKey = "cap_$capKey",
                coinsAwarded = targetMaxCoins,
                sourceType = "cap_tracker",
                grantedAt = now
            )
        )
        dao.upsertCoinGrant(
            CoinGrantEntity(
                userId = userId,
                grantKey = "tx_${capKey}_$txSuffix",
                coinsAwarded = delta,
                sourceType = sourceType,
                grantedAt = now
            )
        )
        return delta
    }

    private suspend fun recordOneTimeCoinGrant(
        userId: Int,
        grantKey: String,
        sourceType: String,
        coins: Int,
        now: Long = System.currentTimeMillis()
    ): Int {
        if (coins <= 0) return 0
        if (dao.getCoinGrant(userId, grantKey) != null) return 0
        dao.upsertCoinGrant(
            CoinGrantEntity(
                userId = userId,
                grantKey = grantKey,
                coinsAwarded = coins,
                sourceType = sourceType,
                grantedAt = now
            )
        )
        return coins
    }

    private suspend fun maybeGrantMateriCompletionCoin(
        userId: Int,
        materiId: Int,
        now: Long
    ): Int {
        if (dao.getCoinGrant(userId, "materi_complete_$materiId") != null) return 0
        val modulesInMateri = dao.getModulesByMateri(materiId).firstOrNull().orEmpty()
        if (modulesInMateri.isEmpty()) return 0
        val progressByModule = dao.getAllProgressForUserSync(userId).associateBy { it.moduleId }
        val allModulesDone = modulesInMateri.all { m ->
            val p = progressByModule[m.id]
            p != null && (p.isFinished || p.isCompleted || p.quizPassed)
        }
        if (!allModulesDone) return 0
        val posttest = dao.getAssessmentByMateriAndTipe(materiId, "posttest") ?: return 0
        val posttestAttempts = dao.getAttemptsForAssessmentAndUser(posttest.id, userId).firstOrNull().orEmpty()
        if (posttestAttempts.none { it.isPassed }) return 0
        return recordOneTimeCoinGrant(
            userId = userId,
            grantKey = "materi_complete_$materiId",
            sourceType = "materi_complete",
            coins = LeaderboardUtils.COIN_MATERI_COMPLETE_BONUS,
            now = now
        )
    }

    suspend fun grantDesignTaskCoin(subMateriId: Int): Int = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value
        recordOneTimeCoinGrant(
            userId = userId,
            grantKey = "design_task_$subMateriId",
            sourceType = "design_task",
            coins = LeaderboardUtils.COIN_DESIGN_TASK_SUBMIT
        )
    }

    suspend fun grantChallengeJoinCoin(challengeId: String): Int = withContext(Dispatchers.IO) {
        if (challengeId.isBlank()) return@withContext 0
        val userId = _currentUserId.value
        recordOneTimeCoinGrant(
            userId = userId,
            grantKey = "challenge_join_$challengeId",
            sourceType = "challenge_join",
            coins = LeaderboardUtils.COIN_CHALLENGE_JOIN
        )
    }

    suspend fun toggleBookmark(moduleId: Int) = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value
        val existing = dao.getBookmarkedModules(userId).firstOrNull()?.any { it.moduleId == moduleId } == true
        if (existing) {
            dao.deleteBookmark(userId, moduleId)
        } else {
            dao.insertBookmark(BookmarkedModuleEntity(userId = userId, moduleId = moduleId))
        }
    }

    suspend fun submitQuiz(
        moduleId: Int,
        score: Int,
        incorrectQuestionIds: List<Int>
    ): AssessmentAttemptEntity = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value
        val quiz = dao.getQuizByModule(moduleId)
        val assessmentId = quiz?.id ?: moduleId
        val passingGrade = quiz?.passingGrade ?: 75

        val previousAttempts = dao.getAttemptsForAssessmentAndUser(assessmentId, userId).firstOrNull() ?: emptyList()
        val attemptNo = previousAttempts.size + 1
        val isPassed = score >= passingGrade

        // Kuis Sub Materi tidak pernah memblokir atau menerapkan cooldown (PRD Section 7)
        val now = System.currentTimeMillis()
        dao.deleteCooldown(userId, assessmentId)

        val currentProgress = dao.getProgressForModule(userId, moduleId)
            ?: ModuleProgressEntity(userId = userId, moduleId = moduleId, isUnlocked = true)

        val wasAlreadyCompleted = currentProgress.isCompleted
        dao.saveProgress(
            currentProgress.copy(
                isUnlocked = true,
                isFinished = true,
                quizPassed = currentProgress.quizPassed || isPassed,
                isCompleted = true,
                completedAt = if (wasAlreadyCompleted) currentProgress.completedAt else now
            )
        )

        val userAllProgress = dao.getAllProgressForUserSync(userId)
        val completedCount = userAllProgress.count { it.isFinished || it.isCompleted }
        dao.updateUserLevel(userId, completedCount)

        // Koin Kuis (Bagian 6): floor(score/10) selisih + 5 lulus pertama + 20 bonus selesai materi
        val targetScoreCoins = LeaderboardUtils.calculateQuizScoreCoins(score)
        val scoreDelta = recordActivityCoinCapDelta(
            userId = userId,
            capKey = "quiz_$assessmentId",
            sourceType = "quiz",
            targetMaxCoins = targetScoreCoins,
            txSuffix = attemptNo.toString(),
            now = now
        )
        val passBonus = if (isPassed) {
            recordOneTimeCoinGrant(
                userId = userId,
                grantKey = "quiz_pass_$assessmentId",
                sourceType = "quiz_pass",
                coins = LeaderboardUtils.COIN_QUIZ_PASS_BONUS,
                now = now
            )
        } else 0
        val materiId = dao.getModuleById(moduleId)?.materiId
        val materiBonus = if (materiId != null) maybeGrantMateriCompletionCoin(userId, materiId, now) else 0
        val attemptCoins = scoreDelta + passBonus + materiBonus

        val attempt = AssessmentAttemptEntity(
            userId = userId,
            assessmentId = assessmentId,
            attemptNo = attemptNo,
            score = score,
            isPassed = isPassed,
            incorrectQuestionIds = incorrectQuestionIds.joinToString(","),
            blockedUntil = 0L,
            submittedAt = now,
            koinDiberikan = attemptCoins
        )
        dao.insertAttempt(attempt)

        attempt
    }

    suspend fun submitPosttest(
        materiId: Int = 1,
        score: Int,
        incorrectQuestionIds: List<Int> = emptyList()
    ): AssessmentAttemptEntity = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value
        val posttest = dao.getAssessmentByMateriAndTipe(materiId, "posttest")
        val assessmentId = posttest?.id ?: (if (materiId == 1) 20 else materiId * 100 + 20)
        val passingGrade = posttest?.passingGrade ?: 75
        val isPassed = score >= passingGrade

        val previousAttempts = dao.getAttemptsForAssessmentAndUser(assessmentId, userId).firstOrNull() ?: emptyList()
        val attemptNo = previousAttempts.size + 1

        val now = System.currentTimeMillis()
        val existingCooldown = dao.getCooldown(userId, assessmentId)
        val blockedUntil = if (!isPassed) {
            val prevFailures = if (existingCooldown != null && !AssessmentCooldownConfig.isBlocked(existingCooldown, now)) {
                existingCooldown.consecutiveFailures
            } else {
                0
            }
            val newFailures = prevFailures + 1
            if (newFailures >= AssessmentCooldownConfig.POSTTEST_MAX_CONSECUTIVE_FAILS) {
                val until = now + AssessmentCooldownConfig.POSTTEST_COOLDOWN_MS
                dao.saveCooldown(
                    AssessmentCooldownEntity(
                        userId = userId,
                        assessmentId = assessmentId,
                        lastFailedAt = now,
                        consecutiveFailures = 0, // reset hitungan untuk siklus berikutnya setelah jeda
                        blockedUntil = until
                    )
                )
                syncPosttestLockToFirestore(userId, assessmentId, until)
                until
            } else {
                dao.saveCooldown(
                    AssessmentCooldownEntity(
                        userId = userId,
                        assessmentId = assessmentId,
                        lastFailedAt = now,
                        consecutiveFailures = newFailures,
                        blockedUntil = 0L
                    )
                )
                0L
            }
        } else {
            dao.deleteCooldown(userId, assessmentId)
            syncPosttestLockToFirestore(userId, assessmentId, 0L)
            0L
        }

        // Koin Posttest (Bagian 6): floor(score/5) selisih + 10 lulus pertama + 20 selesai materi
        val targetScoreCoins = LeaderboardUtils.calculatePosttestScoreCoins(score)
        val scoreDelta = recordActivityCoinCapDelta(
            userId = userId,
            capKey = "posttest_$materiId",
            sourceType = "posttest",
            targetMaxCoins = targetScoreCoins,
            txSuffix = attemptNo.toString(),
            now = now
        )
        val passBonus = if (isPassed) {
            recordOneTimeCoinGrant(
                userId = userId,
                grantKey = "posttest_pass_$materiId",
                sourceType = "posttest_pass",
                coins = LeaderboardUtils.COIN_POSTTEST_PASS_BONUS,
                now = now
            )
        } else 0
        val attemptCoinsPreInsert = scoreDelta + passBonus

        val attempt = AssessmentAttemptEntity(
            userId = userId,
            assessmentId = assessmentId,
            attemptNo = attemptNo,
            score = score,
            isPassed = isPassed,
            incorrectQuestionIds = incorrectQuestionIds.joinToString(","),
            blockedUntil = blockedUntil,
            submittedAt = now,
            koinDiberikan = attemptCoinsPreInsert
        )
        dao.insertAttempt(attempt)

        if (isPassed) {
            maybeGrantMateriCompletionCoin(userId, materiId, now)
        }

        attempt
    }

    suspend fun submitPosttest(score: Int): AssessmentAttemptEntity = submitPosttest(1, score, emptyList())

    private suspend fun syncPosttestLockToFirestore(userId: Int, assessmentId: Int, blockedUntil: Long) {
        val user = dao.getUserByIdSync(userId) ?: return
        val uid = user.firebaseUid.ifBlank { authService.currentUser?.uid.orEmpty() }
        if (uid.isBlank()) return
        try {
            val docRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("assessment_locks")
                .document(assessmentId.toString())
            if (blockedUntil > 0L) {
                docRef.set(
                    mapOf(
                        "assessmentId" to assessmentId,
                        "blockedUntil" to blockedUntil,
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.delete().await()
            }
        } catch (t: Throwable) {
            Log.w("FiksFahamRepository", "Optional Firestore lock sync skipped: ${t.message}")
        }
    }

    private suspend fun syncPosttestLocksFromFirestore(userId: Int) {
        val user = dao.getUserByIdSync(userId) ?: return
        val uid = user.firebaseUid.ifBlank { authService.currentUser?.uid.orEmpty() }
        if (uid.isBlank()) return
        try {
            val snap = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("assessment_locks")
                .get()
                .await()
            val now = System.currentTimeMillis()
            for (doc in snap.documents) {
                val assessmentId = (doc.data?.get("assessmentId") as? Number)?.toInt()
                    ?: doc.id.toIntOrNull()
                    ?: continue
                val blockedUntil = (doc.data?.get("blockedUntil") as? Number)?.toLong() ?: 0L
                if (blockedUntil > now) {
                    dao.saveCooldown(
                        AssessmentCooldownEntity(
                            userId = userId,
                            assessmentId = assessmentId,
                            lastFailedAt = now,
                            consecutiveFailures = 0,
                            blockedUntil = blockedUntil
                        )
                    )
                }
            }
        } catch (t: Throwable) {
            Log.w("FiksFahamRepository", "Optional Firestore lock restore skipped: ${t.message}")
        }
    }

    suspend fun getAssessmentCooldown(userId: Int, assessmentId: Int): AssessmentCooldownEntity? = withContext(Dispatchers.IO) {
        dao.getCooldown(userId, assessmentId)
    }

    suspend fun getRemainingCooldownSeconds(
        userId: Int,
        assessmentId: Int,
        assessmentType: String,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        if (!assessmentType.equals("posttest", ignoreCase = true)) return@withContext 0L
        val cooldown = dao.getCooldown(userId, assessmentId) ?: return@withContext 0L
        if (cooldown.blockedUntil > 0L) {
            AssessmentCooldownConfig.getRemainingBlockedSeconds(cooldown, currentTimeMillis)
        } else {
            0L
        }
    }

    suspend fun isCooldownActive(
        userId: Int,
        assessmentId: Int,
        assessmentType: String,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean = withContext(Dispatchers.IO) {
        getRemainingCooldownSeconds(userId, assessmentId, assessmentType, currentTimeMillis) > 0L
    }

    fun observeCooldown(userId: Int, assessmentId: Int): Flow<AssessmentCooldownEntity?> {
        return dao.getCooldownFlow(userId, assessmentId)
    }

    suspend fun submitCapstone(
        materiId: Int,
        title: String,
        description: String,
        linkFigma: String,
        fileUrl: String = ""
    ) = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value
        val existing = dao.getCapstoneSubmissionSync(userId, materiId)
        val nextRevisionCount = if (existing?.status == "revision_needed") {
            (existing.revisionCount + 1).coerceAtMost(com.example.CapstoneRubricHelper.MAX_REVISIONS)
        } else {
            existing?.revisionCount ?: 0
        }
        val submission = CapstoneSubmissionEntity(
            userId = userId,
            materiId = materiId,
            judulProject = title,
            deskripsi = description,
            linkFigma = linkFigma,
            fileUrl = fileUrl,
            status = "submitted",
            nilai = null,
            feedback = "Proyek telah diterima. Menunggu evaluasi mentor dan rubrik penilaian.",
            submittedAt = System.currentTimeMillis(),
            revisionCount = nextRevisionCount,
            rubricSummary = ""
        )
        dao.saveCapstoneSubmission(submission)
    }

    suspend fun submitCapstone(
        title: String,
        description: String,
        linkFigma: String,
        fileUrl: String = ""
    ) = submitCapstone(CourseProgressUtils.FINAL_CAPSTONE_MATERI_ID, title, description, linkFigma, fileUrl)

    fun getCapstoneSubmissionForMateri(materiId: Int): Flow<CapstoneSubmissionEntity?> = _currentUserId.flatMapLatest { userId ->
        dao.getCapstoneSubmissionForUser(userId, materiId)
    }

    fun getAllCapstoneSubmissionsForCurrentUser(): Flow<List<CapstoneSubmissionEntity>> = _currentUserId.flatMapLatest { userId ->
        dao.getAllCapstoneSubmissionsForUser(userId)
    }

    /**
     * Menyimpan salinan review Capstone dari Firestore ke Room lokal perangkat MAHASISWA
     * sebagai offline cache dan membuat notifikasi lokal bila status berubah.
     */
    suspend fun saveReviewedCapstoneFromCloud(submission: CapstoneSubmission) = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value
        val materiId = submission.materiId
        val existingLocal = dao.getCapstoneSubmissionSync(userId, materiId)
        val normalizedStatus = com.example.CapstoneStatusUtils.normalize(submission.status)

        val statusChanged = existingLocal == null ||
            existingLocal.status != normalizedStatus ||
            existingLocal.nilai != submission.nilai ||
            existingLocal.feedback != submission.feedback

        val entity = CapstoneSubmissionEntity(
            userId = userId,
            materiId = materiId,
            judulProject = submission.judul.ifBlank { existingLocal?.judulProject ?: "Capstone Project Desain" },
            deskripsi = submission.deskripsi.ifBlank { existingLocal?.deskripsi ?: "" },
            linkFigma = submission.linkUrl.ifBlank { existingLocal?.linkFigma ?: "" },
            fileUrl = submission.fileUrl.ifBlank { existingLocal?.fileUrl ?: "" },
            status = normalizedStatus,
            nilai = submission.nilai,
            feedback = submission.feedback,
            submittedAt = submission.submittedAt,
            revisionCount = submission.revisionCount,
            rubricSummary = submission.rubricSummary
        )
        dao.saveCapstoneSubmission(entity)

        if (entity.status == "approved") {
            recordOneTimeCoinGrant(
                userId = userId,
                grantKey = "capstone_approved",
                sourceType = "capstone_approved",
                coins = LeaderboardUtils.COIN_CAPSTONE_APPROVED,
                now = submission.reviewedAt ?: System.currentTimeMillis()
            )
        }

        if (statusChanged && normalizedStatus in setOf("approved", "revision_needed", "under_review", "rejected")) {
            val statusLabel = when (normalizedStatus) {
                "approved" -> "Disetujui"
                "revision_needed" -> "Perlu Revisi"
                "rejected" -> "Ditolak"
                "under_review" -> "Sedang Direview"
                else -> "Diperbarui"
            }
            val scoreText = submission.nilai?.let { " (Nilai: $it/100)" } ?: ""
            dao.insertNotification(
                NotificationEntity(
                    assessmentId = 999,
                    judul = "Status Capstone: $statusLabel$scoreText",
                    pesan = submission.feedback.ifBlank { "Mentor memperbarui status evaluasi Capstone kamu." },
                    deadlineInfo = statusLabel,
                    isRead = false,
                    createdAt = submission.reviewedAt ?: System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun markNotificationRead(id: Int) = withContext(Dispatchers.IO) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsRead() = withContext(Dispatchers.IO) {
        dao.markAllNotificationsAsRead()
    }

    suspend fun updateUser(nama: String, instansi: String, email: String, avatarUrl: String? = null) = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value
        val current = dao.getUserByIdSync(userId) ?: dao.getFirstUser() ?: InitialData.getDefaultUser()
        val finalAvatar = avatarUrl ?: current.avatarUrl
        val updatedUser = current.copy(
            nama = nama,
            asalInstansi = instansi,
            email = email,
            avatarUrl = finalAvatar
        )
        dao.updateUser(updatedUser)

        // Sync to Firestore under users/{userId}
        val effectiveUid = if (updatedUser.firebaseUid.isNotBlank()) {
            updatedUser.firebaseUid
        } else {
            try {
                FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            } catch (e: Exception) {
                ""
            }
        }

        if (effectiveUid.isNotBlank()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val profileMap = mapOf(
                    "nama" to nama,
                    "asalInstansi" to instansi,
                    "email" to email,
                    "avatarUrl" to (finalAvatar ?: ""),
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(effectiveUid)
                    .set(profileMap, com.google.firebase.firestore.SetOptions.merge())

                // Update leaderboard & daily/monthly coin snapshots jika dokumen sudah ada
                firestore.collection("leaderboard").document(effectiveUid)
                    .update(mapOf("name" to nama, "institution" to instansi))
                    .addOnFailureListener { }

                val nowMillis = System.currentTimeMillis()
                val dailyDocId = LeaderboardUtils.getWibDailyDocId(nowMillis)
                val monthlyDocId = LeaderboardUtils.getWibMonthlyDocId(nowMillis)
                val coinProfilePatch = mapOf(
                    "username" to nama,
                    "avatarUrl" to (finalAvatar ?: "")
                )
                firestore.collection("leaderboard_daily")
                    .document(dailyDocId)
                    .collection("entries")
                    .document(effectiveUid)
                    .update(coinProfilePatch)
                    .addOnFailureListener { }
                firestore.collection("leaderboard_monthly")
                    .document(monthlyDocId)
                    .collection("entries")
                    .document(effectiveUid)
                    .update(coinProfilePatch)
                    .addOnFailureListener { }
            } catch (e: Exception) {
                Log.w("FiksFahamRepository", "Firestore profile sync warning: ${e.message}")
            }
        }
    }

    suspend fun uploadProfilePhoto(uri: Uri, uid: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val storage = FirebaseStorage.getInstance()
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val ref = storage.reference.child("profile_photos/$uid/$fileName")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.w("FiksFahamRepository", "Failed to upload profile photo to Storage: ${e.message}")
            Result.failure(e)
        }
    }

    private val activeFirestoreListeners = java.util.Collections.synchronizedList(mutableListOf<com.google.firebase.firestore.ListenerRegistration>())

    fun registerFirestoreListener(registration: com.google.firebase.firestore.ListenerRegistration) {
        activeFirestoreListeners.add(registration)
    }

    fun stopAllFirestoreListeners() {
        synchronized(activeFirestoreListeners) {
            activeFirestoreListeners.forEach {
                try {
                    it.remove()
                } catch (t: Throwable) {
                    Log.d("FiksFahamRepository", "Listener remove notice: ${t.message}")
                }
            }
            activeFirestoreListeners.clear()
        }
    }

    suspend fun reauthenticate(password: String): Result<Unit> = withContext(Dispatchers.IO) {
        authService.reauthenticate(password)
    }

    suspend fun changePasswordWithReauth(oldPassword: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        authService.changePasswordWithReauth(oldPassword, newPassword)
    }

    /**
     * TAHAP 8B: Hapus Akun Pengguna
     * Urutan penghapusan:
     * 1. Hapus file pengguna di Storage (foto profil, file Capstone).
     * 2. Hapus dokumen Firestore milik pengguna (profil, leaderboard, capstone_submissions).
     * 3. Hapus data Room lokal milik pengguna.
     * 4. Terakhir, hapus akun Firebase Auth.
     * Jika langkah gagal di tengah, batalkan dan jangan hapus akun Auth.
     * DILARANG menghapus dokumen mentors/{uid}.
     */
    suspend fun deleteUserAccount(
        password: String,
        googleIdToken: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = dao.getUserByIdSync(_currentUserId.value) ?: dao.getFirstUser()
        val uid = user?.firebaseUid?.ifBlank { authService.currentUser?.uid.orEmpty() } ?: authService.currentUser?.uid.orEmpty()
        val localUserId = user?.id ?: _currentUserId.value

        if (uid.isBlank()) {
            return@withContext Result.failure(IllegalStateException("ID akun tidak ditemukan."))
        }

        // 0. Autentikasi ulang terlebih dahulu (mendukung password maupun Google Sign-In)
        val reauthResult = if (!googleIdToken.isNullOrBlank()) {
            authService.reauthenticateWithGoogle(googleIdToken)
        } else {
            authService.reauthenticate(password)
        }
        if (reauthResult.isFailure) {
            return@withContext reauthResult
        }

        // 1. Hapus berkas pengguna di Firebase Storage (foto profil & berkas Capstone)
        try {
            val storage = FirebaseStorage.getInstance()
            try {
                val photoFolder = storage.reference.child("profile_photos/$uid")
                val photoList = photoFolder.listAll().await()
                for (item in photoList.items) {
                    item.delete().await()
                }
            } catch (t: Throwable) {
                Log.d("FiksFahamRepository", "Notice: No profile photos to delete: ${t.message}")
            }

            try {
                val capstoneFolder = storage.reference.child("capstone_files/$uid")
                val capstoneList = capstoneFolder.listAll().await()
                for (prefix in capstoneList.prefixes) {
                    val subItems = prefix.listAll().await()
                    for (item in subItems.items) {
                        item.delete().await()
                    }
                }
                for (item in capstoneList.items) {
                    item.delete().await()
                }
            } catch (t: Throwable) {
                Log.d("FiksFahamRepository", "Notice: No capstone files to delete: ${t.message}")
            }

            // Hapus berkas tugas desain milik pengguna di Storage (Tahap 10.e)
            try {
                val designFolder = storage.reference.child("design_task_files/$uid")
                val designList = designFolder.listAll().await()
                for (item in designList.items) {
                    item.delete().await()
                }
            } catch (t: Throwable) {
                Log.d("FiksFahamRepository", "Notice: No design task files to delete: ${t.message}")
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Gagal menghapus berkas di Cloud Storage: ${e.message}"))
        }

        // 2. Hapus dokumen Firestore milik pengguna: users/{uid}, leaderboard/{uid}, capstone_submissions, design_task_submissions
        // DILARANG menghapus dokumen mentors/{uid}!
        try {
            val firestore = FirebaseFirestore.getInstance()

            // users/{uid}
            try {
                firestore.collection("users").document(uid).delete().await()
            } catch (e: Exception) {
                Log.w("FiksFahamRepository", "Delete users/$uid error: ${e.message}")
                throw e
            }

            // leaderboard/{uid}
            try {
                firestore.collection("leaderboard").document(uid).delete().await()
            } catch (e: Exception) {
                Log.w("FiksFahamRepository", "Delete leaderboard/$uid error: ${e.message}")
                throw e
            }

            // capstone_submissions miliknya (0 untuk final capstone + legacy 1..5)
            try {
                for (materiId in 0..5) {
                    val docId = "${uid}_$materiId"
                    firestore.collection("capstone_submissions").document(docId).delete().await()
                }
            } catch (e: Exception) {
                Log.w("FiksFahamRepository", "Delete capstone submissions error: ${e.message}")
                throw e
            }

            // certificates miliknya
            try {
                val certs = firestore.collection("certificates").whereEqualTo("uid", uid).get().await()
                for (doc in certs.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {
                Log.w("FiksFahamRepository", "Delete certificates error: ${e.message}")
            }

            // design_task_submissions miliknya (Tahap 10.e)
            try {
                val designSubmissions = firestore.collection("design_task_submissions")
                    .whereEqualTo("uid", uid)
                    .get()
                    .await()
                for (doc in designSubmissions.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {
                Log.w("FiksFahamRepository", "Delete design task submissions error: ${e.message}")
                throw e
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Gagal menghapus dokumen database Firestore: ${e.message}"))
        }

        // 3. Hapus data Room lokal milik pengguna
        try {
            dao.deleteAllUserData(localUserId)
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Gagal menghapus data lokal perangkat: ${e.message}"))
        }

        // 4. Terakhir, hapus akun Firebase Auth
        val authDeleteResult = authService.deleteAuthUser()
        if (authDeleteResult.isFailure) {
            return@withContext Result.failure(
                authDeleteResult.exceptionOrNull() ?: Exception("Gagal menghapus akun autentikasi Firebase.")
            )
        }

        stopAllFirestoreListeners()
        Result.success(Unit)
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        authService.updatePassword(newPassword)
    }

    suspend fun requestEmailVerificationChange(newEmail: String, password: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        authService.verifyBeforeUpdateEmail(newEmail, password)
    }

    fun logout() {
        stopAllFirestoreListeners()
        authService.signOut()
    }

    fun searchCreators(query: String): Flow<List<CreatorEntity>> = dao.searchCreators(query)
    fun searchCuratedVideos(query: String): Flow<List<CuratedVideoEntity>> = dao.searchCuratedVideos(query)
    fun searchBootcamps(query: String): Flow<List<BootcampEntity>> = dao.searchBootcamps(query)

    fun getLeaderboardDocsFlow(): Flow<List<com.example.data.model.FirestoreLeaderboardDoc>> = kotlinx.coroutines.flow.callbackFlow {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val registration = firestore.collection("leaderboard")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FiksFahamRepository", "Leaderboard snapshot listener warning: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val docs = snapshot.documents.mapNotNull { d ->
                            try {
                                d.toObject(com.example.data.model.FirestoreLeaderboardDoc::class.java)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        trySend(docs)
                    }
                }
            registerFirestoreListener(registration)
            awaitClose {
                registration.remove()
                activeFirestoreListeners.remove(registration)
            }
        } catch (t: Throwable) {
            Log.w("FiksFahamRepository", "Firestore not available: ${t.message}")
            trySend(emptyList())
            close()
        }
    }

    suspend fun syncUserPointsToFirestore(
        user: UserEntity,
        pointsBreakdown: com.example.data.model.UserPointsBreakdown,
        allAttempts: List<AssessmentAttemptEntity>,
        capstone: CapstoneSubmissionEntity?,
        assessments: List<AssessmentEntity> = emptyList()
    ) = withContext(Dispatchers.IO) {
        val uid = user.firebaseUid.ifBlank { authService.currentUser?.uid.orEmpty() }
        if (uid.isBlank()) return@withContext

        try {
            val events = LeaderboardUtils.extractPointEvents(allAttempts, capstone, assessments)
            val summary = LeaderboardUtils.calculatePeriodPointsSummary(events)

            val doc = com.example.data.model.FirestoreLeaderboardDoc(
                uid = uid,
                name = user.nama.ifBlank { "Pengguna FiksFaham" },
                institution = user.asalInstansi,
                totalPoints = summary.totalPoints,
                dailyPoints = summary.dailyPoints,
                weeklyPoints = summary.weeklyPoints,
                monthlyPoints = summary.monthlyPoints,
                stars = pointsBreakdown.starCount,
                starTier = pointsBreakdown.starTierTitle,
                badgeLabel = if (summary.totalPoints >= 600) "Top Learner" else null,
                lastDailyTimestamp = summary.lastDailyTimestamp,
                lastWeeklyTimestamp = summary.lastWeeklyTimestamp,
                lastMonthlyTimestamp = summary.lastMonthlyTimestamp,
                updatedAt = System.currentTimeMillis()
            )

            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("leaderboard").document(uid).set(doc)

            // Snapshot koin Harian & Bulanan zona WIB (Bagian 6)
            val grants = dao.getAllCoinGrantsForUserSync(user.id)
            val nowMillis = System.currentTimeMillis()
            val dailyCoins = LeaderboardUtils.computePeriodCoinsFromGrants(
                grants = grants,
                period = com.example.data.model.LeaderboardPeriod.DAILY,
                nowMillis = nowMillis
            )
            val monthlyCoins = LeaderboardUtils.computePeriodCoinsFromGrants(
                grants = grants,
                period = com.example.data.model.LeaderboardPeriod.MONTHLY,
                nowMillis = nowMillis
            )
            val dailyDocId = LeaderboardUtils.getWibDailyDocId(nowMillis)
            val monthlyDocId = LeaderboardUtils.getWibMonthlyDocId(nowMillis)

            firestore.collection("leaderboard_daily")
                .document(dailyDocId)
                .collection("entries")
                .document(uid)
                .set(
                    mapOf(
                        "uid" to uid,
                        "username" to doc.name,
                        "avatarUrl" to user.avatarUrl,
                        "koin" to dailyCoins,
                        "updatedAt" to nowMillis
                    )
                )

            firestore.collection("leaderboard_monthly")
                .document(monthlyDocId)
                .collection("entries")
                .document(uid)
                .set(
                    mapOf(
                        "uid" to uid,
                        "username" to doc.name,
                        "avatarUrl" to user.avatarUrl,
                        "koin" to monthlyCoins,
                        "updatedAt" to nowMillis
                    )
                )
        } catch (t: Throwable) {
            Log.w("FiksFahamRepository", "Failed to sync points to Firestore: ${t.message}")
        }
    }

    fun getDailyCoinLeaderboardFlow(
        dailyDocId: String = LeaderboardUtils.getWibDailyDocId()
    ): Flow<List<CoinLeaderboardEntryDoc>> = observeCoinLeaderboardCollection(
        collectionPath = "leaderboard_daily",
        periodDocId = dailyDocId
    )

    fun getMonthlyCoinLeaderboardFlow(
        monthlyDocId: String = LeaderboardUtils.getWibMonthlyDocId()
    ): Flow<List<CoinLeaderboardEntryDoc>> = observeCoinLeaderboardCollection(
        collectionPath = "leaderboard_monthly",
        periodDocId = monthlyDocId
    )

    private fun observeCoinLeaderboardCollection(
        collectionPath: String,
        periodDocId: String
    ): Flow<List<CoinLeaderboardEntryDoc>> = kotlinx.coroutines.flow.callbackFlow {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val registration = firestore.collection(collectionPath)
                .document(periodDocId)
                .collection("entries")
                .orderBy("koin", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FiksFahamRepository", "Coin leaderboard listener warning: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val docs = snapshot?.documents?.mapNotNull { d ->
                        val data = d.data ?: return@mapNotNull null
                        CoinLeaderboardEntryDoc(
                            uid = data["uid"] as? String ?: d.id,
                            username = data["username"] as? String ?: (data["name"] as? String ?: ""),
                            avatarUrl = data["avatarUrl"] as? String ?: "",
                            koin = (data["koin"] as? Number)?.toInt() ?: (data["pointsToday"] as? Number)?.toInt() ?: 0,
                            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                    }.orEmpty()
                    trySend(docs)
                }
            registerFirestoreListener(registration)
            awaitClose {
                registration.remove()
                activeFirestoreListeners.remove(registration)
            }
        } catch (t: Throwable) {
            Log.w("FiksFahamRepository", "Firestore coin leaderboard unavailable: ${t.message}")
            trySend(emptyList())
            close()
        }
    }
}
