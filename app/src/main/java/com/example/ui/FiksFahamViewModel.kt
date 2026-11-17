package com.example.ui

import android.content.Context
import android.net.Uri
import com.example.CapstoneFilter
import com.example.CapstoneRepository
import com.example.CapstoneSubmission
import com.example.data.model.ChallengeParticipant
import com.example.data.model.DesignChallenge
import com.example.data.model.DesignTaskEntity
import com.example.data.model.DesignTaskSubmissionEntity
import com.example.data.model.InspirasiItem
import com.example.data.model.QaPost
import com.example.data.model.QaReply
import com.example.data.repository.CommunityRepository
import com.example.data.repository.DesignTaskRepository
import com.example.data.repository.DesignTaskSubmission
import com.example.data.repository.InspirasiRepository
import com.example.util.CommunityRulesUtils
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.FiksFahamDatabase
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.BootcampEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.CreatorEntity
import com.example.data.model.CuratedVideoEntity
import com.example.data.model.MateriEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.UserEntity
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserPointsBreakdown
import com.example.util.CertificateConfig
import com.example.util.CourseProgressUtils
import com.example.util.LeaderboardUtils
import com.example.data.repository.FiksFahamRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.data.preferences.AppPreferencesRepository
import com.example.data.preferences.ThemeMode

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    INDONESIAN("id", "Bahasa Indonesia", "🇮🇩"),
    ENGLISH("en", "English", "🇬🇧");

    companion object {
        val DEFAULT = INDONESIAN

        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
    }
}

data class HomeUiState(
    val user: UserEntity? = null,
    val progressPercent: Int = 0,
    val currentLevelText: String = "Level 1 dari 4",
    val pretestScore: Int? = null,
    val posttestScore: Int? = null,
    val isPretestDone: Boolean = false,
    val isPosttestDone: Boolean = false,
    val isAllModulesCompleted: Boolean = false,
    val capstoneState: CapstoneState = CapstoneState.LOCKED,
    val capstoneSubmission: CapstoneSubmissionEntity? = null,
    val unreadNotificationsCount: Int = 0,
    val userPoints: UserPointsBreakdown = UserPointsBreakdown(),
    val leaderboardEntries: List<LeaderboardEntry> = emptyList(),
    val userRank: Int = 1,
    val leaderboardDocs: List<com.example.data.model.FirestoreLeaderboardDoc> = emptyList(),
    val currentUserSummary: com.example.data.model.PeriodPointsSummary? = null,
    val isLeaderboardOffline: Boolean = false,
    val dailyCoinEntries: List<LeaderboardEntry> = emptyList(),
    val monthlyCoinEntries: List<LeaderboardEntry> = emptyList(),
    val userDailyCoins: Int = 0,
    val userMonthlyCoins: Int = 0
)

enum class CapstoneState {
    LOCKED,
    ACTIVE,
    SUBMITTED
}

enum class AuthScreenState {
    SPLASH,
    ONBOARDING,
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    AUTHENTICATED
}

class FiksFahamViewModel @JvmOverloads constructor(
    application: Application,
    customRepository: FiksFahamRepository? = null,
    customCloudCapstone: CapstoneRepository? = null,
    customPreferencesRepository: AppPreferencesRepository? = null,
    customDesignTaskRepository: DesignTaskRepository? = null,
    customInspirasiRepository: InspirasiRepository? = null,
    customCommunityRepository: CommunityRepository? = null
) : AndroidViewModel(application) {

    private val repository: FiksFahamRepository = customRepository ?: run {
        val db = FiksFahamDatabase.getDatabase(application)
        FiksFahamRepository(db.dao())
    }

    private val appPreferences: AppPreferencesRepository =
        customPreferencesRepository ?: AppPreferencesRepository(application)

    val cloudCapstone: CapstoneRepository = customCloudCapstone ?: CapstoneRepository()

    val designTaskRepository: DesignTaskRepository = customDesignTaskRepository ?: run {
        val db = FiksFahamDatabase.getDatabase(application)
        DesignTaskRepository(db.dao())
    }

    val inspirasiRepository: InspirasiRepository = customInspirasiRepository ?: InspirasiRepository()

    val communityRepository: CommunityRepository = customCommunityRepository ?: CommunityRepository()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    val inspirasiList: StateFlow<List<InspirasiItem>> = inspirasiRepository.observeInspirasiList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mentor submissions list from Firestore
    private val _mentorSubmissions = MutableStateFlow<List<CapstoneSubmission>>(emptyList())
    val mentorSubmissions: StateFlow<List<CapstoneSubmission>> = _mentorSubmissions.asStateFlow()

    private val _mentorFilter = MutableStateFlow<String?>(null) // null = ALL, "submitted", "reviewed"
    val mentorFilter: StateFlow<String?> = _mentorFilter.asStateFlow()

    private val _isLoadingSubmissions = MutableStateFlow(false)
    val isLoadingSubmissions: StateFlow<Boolean> = _isLoadingSubmissions.asStateFlow()

    private var capstoneObserveJob: Job? = null
    private var observedCapstoneUid: String? = null
    private var lastCheckedAdminUid: String? = null
    private var lastLoadedMentorUid: String? = null

    init {
        viewModelScope.launch {
            try {
                repository.seedDatabaseIfEmpty()
            } catch (e: Exception) {
                Log.e("FiksFahamViewModel", "Error seeding database: ${e.message}", e)
            }
            runCatching { designTaskRepository.syncDesignTasksFromCloud() }
        }

        viewModelScope.launch {
            repository.user.collect { u ->
                if (u != null) {
                    val uid = u.firebaseUid
                    if (uid.isNotBlank()) {
                        if (com.example.CapstoneStatusUtils.isStaffRole(u.role)) {
                            if (lastLoadedMentorUid != uid) {
                                lastLoadedMentorUid = uid
                                loadMentorSubmissions()
                            }
                            if (u.role.equals("superadmin", ignoreCase = true)) {
                                _isAdmin.value = true
                            }
                        } else if (observedCapstoneUid != uid) {
                            observedCapstoneUid = uid
                            observeStudentCapstone(uid)
                        }
                        if (lastCheckedAdminUid != uid) {
                            lastCheckedAdminUid = uid
                            checkAdminRole(uid)
                        }
                    }
                } else {
                    capstoneObserveJob?.cancel()
                    capstoneObserveJob = null
                    observedCapstoneUid = null
                    lastCheckedAdminUid = null
                    lastLoadedMentorUid = null
                    _isAdmin.value = false
                    _mentorSubmissions.value = emptyList()
                }
            }
        }
    }

    val user: StateFlow<UserEntity?> = repository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val materis: StateFlow<List<MateriEntity>> = repository.materis
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val modules: StateFlow<List<ModuleEntity>> = repository.modules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assessments: StateFlow<List<AssessmentEntity>> = repository.assessments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMateriId = MutableStateFlow<Int?>(null)
    val selectedMateriId: StateFlow<Int?> = _selectedMateriId.asStateFlow()

    fun selectMateri(materiId: Int?) {
        _selectedMateriId.value = materiId
    }

    val progressList: StateFlow<List<ModuleProgressEntity>> = repository.progressList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttempts: StateFlow<List<AssessmentAttemptEntity>> = repository.allAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val capstoneSubmission: StateFlow<CapstoneSubmissionEntity?> = repository.capstoneSubmission
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCapstoneSubmissions: StateFlow<List<CapstoneSubmissionEntity>> = repository.allCapstoneSubmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedModuleIds: StateFlow<Set<Int>> = repository.bookmarkedModuleIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleBookmark(moduleId: Int) {
        viewModelScope.launch {
            repository.toggleBookmark(moduleId)
        }
    }

    val designTasks: StateFlow<List<DesignTaskEntity>> = designTaskRepository.getAllDesignTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val designTaskSubmissions: StateFlow<List<DesignTaskSubmissionEntity>> = repository.user
        .flatMapLatest { u ->
            val userId = u?.id ?: 1
            designTaskRepository.getAllLocalSubmissions(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Galeri Publik (Tahap 10)
    private val _publicGalleryItems = MutableStateFlow<List<DesignTaskSubmission>>(emptyList())
    val publicGalleryItems: StateFlow<List<DesignTaskSubmission>> = _publicGalleryItems.asStateFlow()

    private val _isLoadingPublicGallery = MutableStateFlow(false)
    val isLoadingPublicGallery: StateFlow<Boolean> = _isLoadingPublicGallery.asStateFlow()

    private val _hasMorePublicGallery = MutableStateFlow(true)
    val hasMorePublicGallery: StateFlow<Boolean> = _hasMorePublicGallery.asStateFlow()

    fun loadPublicGallery(loadMore: Boolean = false) {
        viewModelScope.launch {
            if (_isLoadingPublicGallery.value) return@launch
            if (loadMore && !_hasMorePublicGallery.value) return@launch

            _isLoadingPublicGallery.value = true
            val lastCreatedAt = if (loadMore) _publicGalleryItems.value.lastOrNull()?.createdAt else null
            val result = designTaskRepository.getPublicGallery(limit = 10, startAfterCreatedAt = lastCreatedAt)
            _isLoadingPublicGallery.value = false

            if (result.isSuccess) {
                val newItems = result.getOrNull().orEmpty()
                if (loadMore) {
                    _publicGalleryItems.value = _publicGalleryItems.value + newItems
                } else {
                    _publicGalleryItems.value = newItems
                }
                _hasMorePublicGallery.value = newItems.size >= 10
            }
        }
    }

    fun updateDesignVisibility(
        subMateriId: Int,
        newVisibility: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentUser = user.value
            val userId = currentUser?.id ?: 1
            val uid = currentUser?.firebaseUid.takeIf { !it.isNullOrBlank() }
                ?: try {
                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                } catch (e: Exception) {
                    "user_$userId"
                }

            val result = designTaskRepository.updateVisibility(
                userId = userId,
                uid = uid,
                subMateriId = subMateriId,
                visibility = newVisibility
            )
            if (result.isSuccess) {
                // Refresh galeri publik
                loadPublicGallery(loadMore = false)
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Gagal memperbarui visibilitas")
            }
        }
    }

    fun deleteDesignTask(
        subMateriId: Int,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentUser = user.value
            val userId = currentUser?.id ?: 1
            val uid = currentUser?.firebaseUid.takeIf { !it.isNullOrBlank() }
                ?: try {
                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                } catch (e: Exception) {
                    "user_$userId"
                }

            val result = designTaskRepository.deleteSubmission(
                userId = userId,
                uid = uid,
                subMateriId = subMateriId
            )
            if (result.isSuccess) {
                // Refresh galeri publik
                loadPublicGallery(loadMore = false)
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Gagal menghapus karya desain")
            }
        }
    }

    fun uploadDesignTask(
        context: Context,
        imageUri: Uri,
        materiId: Int,
        subMateriId: Int,
        maxMb: Int = 5,
        onProgress: (Float) -> Unit = {},
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentUser = user.value
            val userId = currentUser?.id ?: 1
            val uid = currentUser?.firebaseUid.takeIf { !it.isNullOrBlank() }
                ?: try {
                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                } catch (e: Exception) {
                    "user_$userId"
                }

            val result = designTaskRepository.uploadAndSubmit(
                context = context,
                imageUri = imageUri,
                userId = userId,
                uid = uid,
                materiId = materiId,
                subMateriId = subMateriId,
                userName = currentUser?.nama.orEmpty(),
                maxMb = maxMb,
                onProgress = onProgress
            )

            if (result.isSuccess) {
                repository.grantDesignTaskCoin(subMateriId)
                syncLeaderboardPoints()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Gagal mengunggah desain")
            }
        }
    }

    // =========================================================================
    // INSPIRASI & KURASI ADMIN (Tahap 11)
    // =========================================================================

    fun checkAdminRole(uid: String) {
        viewModelScope.launch {
            if (uid.isBlank()) {
                _isAdmin.value = false
                return@launch
            }
            val adminFlag = inspirasiRepository.checkIsAdmin(uid) ||
                user.value?.role.equals("superadmin", ignoreCase = true)
            _isAdmin.value = adminFlag
            if (adminFlag && lastLoadedMentorUid != uid) {
                lastLoadedMentorUid = uid
                loadMentorSubmissions()
            }
        }
    }

    fun addInspirasi(
        item: InspirasiItem,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = inspirasiRepository.addInspirasi(item)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Gagal menambahkan inspirasi")
            }
        }
    }

    fun addInspirasi(
        judul: String,
        gambarUrl: String,
        deskripsi: String,
        link: String = "",
        tanggal: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val item = InspirasiItem(
            judul = judul.trim(),
            gambarUrl = gambarUrl.trim(),
            deskripsi = deskripsi.trim(),
            link = link.trim(),
            tanggal = tanggal.trim().ifBlank {
                java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID")).format(java.util.Date())
            },
            createdAt = System.currentTimeMillis()
        )
        addInspirasi(item, onSuccess, onError)
    }

    fun updateInspirasi(
        item: InspirasiItem,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = inspirasiRepository.updateInspirasi(item)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Gagal memperbarui inspirasi")
            }
        }
    }

    fun updateInspirasi(
        id: String,
        judul: String,
        gambarUrl: String,
        deskripsi: String,
        link: String = "",
        tanggal: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val item = InspirasiItem(
            id = id,
            judul = judul.trim(),
            gambarUrl = gambarUrl.trim(),
            deskripsi = deskripsi.trim(),
            link = link.trim(),
            tanggal = tanggal.trim().ifBlank {
                java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID")).format(java.util.Date())
            }
        )
        updateInspirasi(item, onSuccess, onError)
    }

    fun deleteInspirasi(
        id: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = inspirasiRepository.deleteInspirasi(id)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Gagal menghapus inspirasi")
            }
        }
    }

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = repository.unreadNotificationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Search query states for News
    private val _newsContentSearch = MutableStateFlow("")
    val newsContentSearch: StateFlow<String> = _newsContentSearch.asStateFlow()

    private val _newsBootcampSearch = MutableStateFlow("")
    val newsBootcampSearch: StateFlow<String> = _newsBootcampSearch.asStateFlow()

    // Track subtopik yang telah dibuka
    private val _viewedSectionIds = MutableStateFlow<Set<Int>>(emptySet())
    val viewedSectionIds: StateFlow<Set<Int>> = _viewedSectionIds.asStateFlow()

    fun markSectionViewed(moduleId: Int, sectionId: Int, allModuleSectionIds: List<Int>) {
        val updated = _viewedSectionIds.value + sectionId
        _viewedSectionIds.value = updated
        // Jika seluruh sub-topik modul ini sudah dibuka, tandai materiViewed di database
        if (allModuleSectionIds.isNotEmpty() && allModuleSectionIds.all { it in updated }) {
            markMateriViewed(moduleId)
        }
    }

    // ThemeMode (SYSTEM, LIGHT, DARK) backed by DataStore (Tahap 8A)
    val themeMode: StateFlow<ThemeMode> = appPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val isDarkMode: StateFlow<Boolean> = themeMode
        .map { it == ThemeMode.DARK }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            appPreferences.setThemeMode(mode)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    // App Language state (Bahasa Indonesia or English) backed by DataStore (Tahap 8A)
    val appLanguage: StateFlow<AppLanguage> = appPreferences.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.INDONESIAN)

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch {
            appPreferences.setAppLanguage(language)
        }
    }

    // Authentication and Onboarding Flow State
    private val _authScreenState = MutableStateFlow(AuthScreenState.SPLASH)
    val authScreenState: StateFlow<AuthScreenState> = _authScreenState.asStateFlow()

    fun completeSplash() {
        _authScreenState.value = if (repository.hasActiveAuthSession()) {
            AuthScreenState.AUTHENTICATED
        } else {
            AuthScreenState.ONBOARDING
        }
    }

    fun completeOnboarding() {
        _authScreenState.value = AuthScreenState.LOGIN
    }

    fun navigateToLogin() {
        _authScreenState.value = AuthScreenState.LOGIN
    }

    fun navigateToRegister() {
        _authScreenState.value = AuthScreenState.REGISTER
    }

    fun navigateToForgotPassword() {
        _authScreenState.value = AuthScreenState.FORGOT_PASSWORD
    }

    suspend fun loginWithAuth(email: String, password: String): Result<Unit> {
        val res = repository.loginWithAuth(email, password)
        return if (res.isSuccess) {
            _authScreenState.value = AuthScreenState.AUTHENTICATED
            Result.success(Unit)
        } else {
            Result.failure(res.exceptionOrNull() ?: Exception("Gagal masuk."))
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        val res = repository.loginWithGoogle(idToken)
        return if (res.isSuccess) {
            _authScreenState.value = AuthScreenState.AUTHENTICATED
            Result.success(Unit)
        } else {
            Result.failure(res.exceptionOrNull() ?: Exception("Gagal masuk dengan Google."))
        }
    }

    suspend fun requestEmailVerificationChange(newEmail: String, password: String? = null): Result<Unit> {
        return repository.requestEmailVerificationChange(newEmail, password)
    }

    suspend fun registerWithAuth(
        name: String,
        email: String,
        password: String,
        instansi: String
    ): Result<Unit> {
        val res = repository.registerWithAuth(name, email, password, instansi)
        return if (res.isSuccess) {
            _authScreenState.value = AuthScreenState.AUTHENTICATED
            Result.success(Unit)
        } else {
            Result.failure(res.exceptionOrNull() ?: Exception("Gagal mendaftar."))
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return repository.sendPasswordReset(email)
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            repository.updatePassword(newPassword)
        }
    }

    suspend fun changePasswordWithReauth(oldPass: String, newPass: String): Result<Unit> {
        return repository.changePasswordWithReauth(oldPass, newPass)
    }

    fun isGoogleOnlyAccount(): Boolean = repository.isGoogleOnlySession()

    suspend fun deleteUserAccount(password: String): Result<Unit> {
        val result = repository.deleteUserAccount(password)
        if (result.isSuccess) {
            _isAdmin.value = false
            _mentorSubmissions.value = emptyList()
            lastCheckedAdminUid = null
            lastLoadedMentorUid = null
            _authScreenState.value = AuthScreenState.LOGIN
        }
        return result
    }

    suspend fun deleteUserAccountWithGoogle(idToken: String): Result<Unit> {
        val result = repository.deleteUserAccount(password = "", googleIdToken = idToken)
        if (result.isSuccess) {
            _isAdmin.value = false
            _mentorSubmissions.value = emptyList()
            lastCheckedAdminUid = null
            lastLoadedMentorUid = null
            _authScreenState.value = AuthScreenState.LOGIN
        }
        return result
    }

    fun logout() {
        repository.logout()
        capstoneObserveJob?.cancel()
        capstoneObserveJob = null
        observedCapstoneUid = null
        lastCheckedAdminUid = null
        lastLoadedMentorUid = null
        _isAdmin.value = false
        _mentorSubmissions.value = emptyList()
        _authScreenState.value = AuthScreenState.LOGIN
    }

    val filteredCreators: StateFlow<List<CreatorEntity>> = _newsContentSearch
        .flatMapLatest { query -> repository.searchCreators(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCuratedVideos: StateFlow<List<CuratedVideoEntity>> = _newsContentSearch
        .flatMapLatest { query -> repository.searchCuratedVideos(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredBootcamps: StateFlow<List<BootcampEntity>> = _newsBootcampSearch
        .flatMapLatest { query -> repository.searchBootcamps(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val coinLeaderboardFlow = combine(
        repository.allCoinGrants,
        repository.getDailyCoinLeaderboardFlow(),
        repository.getMonthlyCoinLeaderboardFlow()
    ) { grants, dailyDocs, monthlyDocs ->
        Triple(grants, dailyDocs, monthlyDocs)
    }

    val homeUiState: StateFlow<HomeUiState> = combine(
        combine(user, modules, materis) { u, m, mats -> Triple(u, m, mats) },
        assessments,
        combine(repository.getLeaderboardDocsFlow(), coinLeaderboardFlow) { legacyDocs, coinsTriple ->
            legacyDocs to coinsTriple
        },
        combine(progressList, allAttempts, capstoneSubmission, unreadNotificationCount) { p, a, c, uCount ->
            p to Triple(a, c, uCount)
        }
    ) { (currentUser, moduleList, materiList), assessmentList, (firestoreDocs, coinsTriple), (progress, extras) ->
        val (coinGrants, dailyCoinDocs, monthlyCoinDocs) = coinsTriple
        val (attempts, capstone, unreadCount) = extras
        val pretestAssessment = assessmentList.firstOrNull { it.materiId == 1 && it.tipe == "pretest" }
        val posttestAssessment = assessmentList.firstOrNull { it.materiId == 1 && it.tipe == "posttest" }
        val pretestAttempt = attempts.firstOrNull { it.assessmentId == (pretestAssessment?.id ?: 10) }
        val posttestAttempt = attempts.firstOrNull { it.assessmentId == (posttestAssessment?.id ?: 20) }

        val pretestScore = pretestAttempt?.score
        val posttestScore = posttestAttempt?.score

        val isPretestDone = pretestAttempt != null
        val isPosttestDone = posttestAttempt != null

        val totalModules = if (moduleList.isNotEmpty()) moduleList.size else 25
        val completedModuleCount = progress.count { it.isFinished || it.isCompleted }
        val isAllCompleted = completedModuleCount >= totalModules

        // Calculate progress percentage dynamically:
        // Pretest: 10%, Sub-materi: 80% split evenly, Posttest: 10% = Total 100%
        var percent = 0
        if (isPretestDone) percent += 10
        if (totalModules > 0) {
            val moduleRatio = (completedModuleCount.toFloat() / totalModules.toFloat()) * 80f
            percent += moduleRatio.toInt()
        }
        if (isPosttestDone) percent += 10
        percent = percent.coerceIn(0, 100)

        val activeOrder = progress.firstOrNull { !(it.isFinished || it.isCompleted) }?.let { unlockedProg ->
            moduleList.firstOrNull { it.id == unlockedProg.moduleId }?.subMateriOrder
        } ?: (completedModuleCount + 1).coerceAtMost(totalModules)

        // Capstone Akhir Tunggal terbuka setelah seluruh 5 Materi & Posttest selesai
        val isFinalUnlocked = CourseProgressUtils.isFinalCapstoneUnlocked(
            materis = materiList,
            modules = moduleList,
            progressList = progress,
            assessments = assessmentList,
            attempts = attempts
        )

        val capstoneState = when {
            capstone != null -> CapstoneState.SUBMITTED
            isFinalUnlocked -> CapstoneState.ACTIVE
            else -> CapstoneState.LOCKED
        }

        // Hitung poin leaderboard dinamis terhadap jumlah Sub Materi
        val userPoints = LeaderboardUtils.calculateUserPoints(
            allAttempts = attempts,
            capstone = capstone,
            assessments = assessmentList,
            totalModulesCount = totalModules
        )
        // Level XP = jumlah Sub Materi yang sudah lulus kuis oleh pengguna (turunan module_progress.isCompleted)
        val computedUserLevel = completedModuleCount
        val userWithComputedLevel = currentUser?.copy(level = computedUserLevel) ?: currentUser

        val pointEvents = LeaderboardUtils.extractPointEvents(attempts, capstone, assessmentList)
        val periodSummary = LeaderboardUtils.calculatePeriodPointsSummary(pointEvents)

        val leaderboardEntries = LeaderboardUtils.buildLeaderboardFromDocs(
            docs = firestoreDocs,
            currentUser = userWithComputedLevel,
            currentUserSummary = periodSummary,
            currentUserBreakdown = userPoints,
            period = com.example.data.model.LeaderboardPeriod.WEEKLY
        )
        val userRank = leaderboardEntries.firstOrNull { it.isCurrentUser }?.rank ?: 1

        val nowCoinMillis = System.currentTimeMillis()
        val userDailyCoins = LeaderboardUtils.computePeriodCoinsFromGrants(
            grants = coinGrants,
            period = com.example.data.model.LeaderboardPeriod.DAILY,
            nowMillis = nowCoinMillis
        )
        val userMonthlyCoins = LeaderboardUtils.computePeriodCoinsFromGrants(
            grants = coinGrants,
            period = com.example.data.model.LeaderboardPeriod.MONTHLY,
            nowMillis = nowCoinMillis
        )
        val dailyCoinEntries = LeaderboardUtils.buildCoinLeaderboardEntries(
            docs = dailyCoinDocs,
            currentUser = userWithComputedLevel,
            localPeriodCoins = userDailyCoins
        )
        val monthlyCoinEntries = LeaderboardUtils.buildCoinLeaderboardEntries(
            docs = monthlyCoinDocs,
            currentUser = userWithComputedLevel,
            localPeriodCoins = userMonthlyCoins
        )
        val monthlyCoinRank = monthlyCoinEntries.firstOrNull { it.isCurrentUser }?.rank ?: userRank

        HomeUiState(
            user = userWithComputedLevel,
            progressPercent = percent,
            currentLevelText = "Sub Materi $activeOrder dari $totalModules",
            pretestScore = pretestScore,
            posttestScore = posttestScore,
            isPretestDone = isPretestDone,
            isPosttestDone = isPosttestDone,
            isAllModulesCompleted = isAllCompleted,
            capstoneState = capstoneState,
            capstoneSubmission = capstone,
            unreadNotificationsCount = unreadCount,
            userPoints = userPoints,
            leaderboardEntries = leaderboardEntries,
            userRank = monthlyCoinRank,
            leaderboardDocs = firestoreDocs,
            currentUserSummary = periodSummary,
            isLeaderboardOffline = false,
            dailyCoinEntries = dailyCoinEntries,
            monthlyCoinEntries = monthlyCoinEntries,
            userDailyCoins = userDailyCoins,
            userMonthlyCoins = userMonthlyCoins
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun syncLeaderboardPoints() {
        viewModelScope.launch {
            val u = user.firstOrNull() ?: return@launch
            if (u.firebaseUid.isNotBlank()) {
                val attempts = repository.allAttempts.firstOrNull() ?: emptyList()
                val capstone = repository.capstoneSubmission.firstOrNull()
                val assessments = repository.assessments.firstOrNull() ?: emptyList()
                val points = LeaderboardUtils.calculateUserPoints(attempts, capstone, assessments)
                repository.syncUserPointsToFirestore(u, points, attempts, capstone, assessments)
            }
        }
    }

    fun updateNewsContentSearch(query: String) {
        _newsContentSearch.value = query
    }

    fun updateNewsBootcampSearch(query: String) {
        _newsBootcampSearch.value = query
    }

    fun submitPretest(materiId: Int = 1, score: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.submitPretest(materiId, score)
            syncLeaderboardPoints()
            onComplete()
        }
    }

    fun markMateriViewed(moduleId: Int) {
        viewModelScope.launch {
            repository.markMateriViewed(moduleId)
        }
    }

    fun markVideoWatched(moduleId: Int) {
        viewModelScope.launch {
            repository.markVideoWatched(moduleId)
        }
    }

    fun markSubMateriFinished(moduleId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.markSubMateriFinished(moduleId)
            syncLeaderboardPoints()
            onComplete()
        }
    }

    fun submitQuiz(
        moduleId: Int,
        score: Int,
        incorrectQuestionIds: List<Int>,
        onResult: (AssessmentAttemptEntity) -> Unit
    ) {
        viewModelScope.launch {
            val attempt = repository.submitQuiz(moduleId, score, incorrectQuestionIds)
            syncLeaderboardPoints()
            onResult(attempt)
        }
    }

    fun submitPosttest(
        materiId: Int = 1,
        score: Int,
        incorrectQuestionIds: List<Int> = emptyList(),
        onResult: (AssessmentAttemptEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            val attempt = repository.submitPosttest(materiId, score, incorrectQuestionIds)
            syncLeaderboardPoints()
            onResult(attempt)
        }
    }

    fun submitPosttest(materiId: Int = 1, score: Int, onComplete: () -> Unit = {}) {
        submitPosttest(materiId, score, emptyList()) {
            onComplete()
        }
    }

    suspend fun getAssessmentCooldownRemainingSeconds(assessmentId: Int, assessmentType: String): Long {
        val userId = user.value?.id ?: 1
        return repository.getRemainingCooldownSeconds(userId, assessmentId, assessmentType)
    }

    suspend fun getAssessmentForMateri(materiId: Int, tipe: String): AssessmentEntity? {
        return repository.getAssessmentForMateri(materiId, tipe)
    }

    fun setMentorFilter(status: String?) {
        _mentorFilter.value = status
        loadMentorSubmissions(status)
    }

    fun loadMentorSubmissions(status: String? = _mentorFilter.value) {
        viewModelScope.launch {
            _isLoadingSubmissions.value = true
            try {
                val filter = when (status) {
                    "submitted" -> CapstoneFilter.SUBMITTED
                    "approved", "reviewed" -> CapstoneFilter.REVIEWED
                    "under_review" -> CapstoneFilter(status = "under_review")
                    "revision_needed" -> CapstoneFilter(status = "revision_needed")
                    else -> CapstoneFilter.ALL
                }
                val result = cloudCapstone.listSubmissions(filter)
                _mentorSubmissions.value = result.getOrDefault(emptyList())
            } catch (e: Exception) {
                Log.e("FiksFahamViewModel", "Error loading mentor submissions: ${e.message}", e)
            } finally {
                _isLoadingSubmissions.value = false
            }
        }
    }

    fun uploadCapstoneFile(
        fileUri: Uri,
        fileName: String,
        onProgress: (Float) -> Unit,
        onSuccess: (downloadUrl: String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val current = user.value
        val uid = current?.firebaseUid.orEmpty().ifBlank { current?.id?.toString() ?: "1" }
        val materiId = CourseProgressUtils.FINAL_CAPSTONE_MATERI_ID
        cloudCapstone.uploadFile(
            fileUri = fileUri,
            uid = uid,
            materiId = materiId,
            fileName = fileName,
            onProgress = onProgress,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun submitCapstone(
        title: String,
        description: String,
        linkFigma: String,
        fileUrl: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val materiId = CourseProgressUtils.FINAL_CAPSTONE_MATERI_ID
            val existing = allCapstoneSubmissions.value.firstOrNull { it.materiId == materiId }
            val nextRevisionCount = if (existing?.status == "revision_needed") {
                (existing.revisionCount + 1).coerceAtMost(com.example.CapstoneRubricHelper.MAX_REVISIONS)
            } else {
                existing?.revisionCount ?: 0
            }
            repository.submitCapstone(materiId, title, description, linkFigma, fileUrl)
            syncLeaderboardPoints()
            onComplete()

            // Kirim salinan ke Firestore memakai ID {uid}_{materiId}
            val currentUser = user.value
            val uid = currentUser?.firebaseUid.orEmpty().ifBlank { currentUser?.id?.toString() ?: "1" }
            try {
                cloudCapstone.submit(
                    data = CapstoneSubmission(
                        uid = uid,
                        materiId = materiId,
                        nama = currentUser?.nama ?: "-",
                        email = currentUser?.email ?: "-",
                        judul = title,
                        deskripsi = description,
                        linkUrl = linkFigma,
                        fileUrl = fileUrl,
                        status = "submitted",
                        nilai = null,
                        feedback = "",
                        reviewedBy = null,
                        submittedAt = System.currentTimeMillis(),
                        reviewedAt = null,
                        revisionCount = nextRevisionCount,
                        rubricSummary = ""
                    ),
                    onSuccess = {},
                    onError = {},
                )
            } catch (e: Exception) {
                // Ignore Firestore initialization / connection errors gracefully
            }
        }
    }

    fun reviewCapstone(
        uid: String,
        materiId: Int,
        nilai: Int,
        feedback: String,
        status: String = if (nilai >= 75) "approved" else "revision_needed",
        rubricSummary: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val current = user.value
            val mentorUid = current?.firebaseUid.orEmpty().ifBlank { current?.id?.toString() ?: "" }
            val result = cloudCapstone.review(
                uid = uid,
                materiId = materiId,
                nilai = nilai,
                feedback = feedback,
                mentorUid = mentorUid,
                status = status,
                rubricSummary = rubricSummary
            )
            if (result.isSuccess) {
                loadMentorSubmissions()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Gagal menyimpan review.")
            }
        }
    }

    fun observeStudentCapstone(uid: String, materiId: Int = CourseProgressUtils.FINAL_CAPSTONE_MATERI_ID) {
        if (uid.isBlank()) return
        capstoneObserveJob?.cancel()
        capstoneObserveJob = viewModelScope.launch {
            try {
                cloudCapstone.observeMine(uid, materiId).collect { cloudSub ->
                    if (cloudSub != null) {
                        repository.saveReviewedCapstoneFromCloud(cloudSub)
                        if (cloudSub.status == "approved") {
                            syncLeaderboardPoints()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("FiksFahamViewModel", "Could not observe student capstone from cloud: ${e.message}")
            }
        }
    }

    fun saveDesignTaskDefinition(
        materiId: Int,
        subMateriId: Int,
        judulTugas: String,
        instruksi: String,
        batasUkuranMb: Int = 5,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val cleanTitle = judulTugas.trim()
        if (cleanTitle.isBlank()) {
            onError("Judul tugas desain tidak boleh kosong.")
            return
        }
        viewModelScope.launch {
            val task = DesignTaskEntity(
                id = subMateriId,
                materiId = materiId,
                subMateriId = subMateriId,
                judulTugas = cleanTitle,
                instruksi = instruksi.trim(),
                batasUkuranMb = batasUkuranMb.coerceIn(1, 20),
                formatDiizinkan = "image/jpeg,image/png"
            )
            val result = designTaskRepository.saveDesignTaskDefinition(task)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Gagal menyimpan tugas desain.")
            }
        }
    }

    // =========================================================================
    // SERTIFIKAT & VERIFIKASI NOMOR SERTIFIKAT (Fase 3)
    // =========================================================================

    fun ensureAndRegisterCertificateNumber(
        eligibility: CertificateConfig.GraduationCertificateEligibility,
        onResult: (String?) -> Unit = {}
    ) {
        val current = user.value ?: return
        if (eligibility.status !is CertificateConfig.EligibilityStatus.Eligible) {
            onResult(null)
            return
        }
        val uid = current.firebaseUid.ifBlank { "local_${current.id}" }
        val certNo = CertificateConfig.generateCertificateNumber(uid)
        viewModelScope.launch {
            try {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val docRef = firestore.collection("certificates").document(certNo)
                val snap = docRef.get().await()
                if (!snap.exists() && current.firebaseUid.isNotBlank()) {
                    val capScore = eligibility.capstoneScore ?: 0
                    val predikat = CertificateConfig.calculatePredikat(capScore)
                    docRef.set(
                        mapOf(
                            "certificateNumber" to certNo,
                            "uid" to current.firebaseUid,
                            "recipientName" to current.nama,
                            "nama" to current.nama,
                            "institution" to current.asalInstansi,
                            "averagePosttestScore" to eligibility.averagePosttestScore,
                            "capstoneScore" to capScore,
                            "predikat" to predikat,
                            "status" to "active",
                            "issuedAt" to System.currentTimeMillis()
                        )
                    ).await()
                }
            } catch (t: Throwable) {
                Log.w("FiksFahamViewModel", "Certificate registration offline fallback: ${t.message}")
            }
            onResult(certNo)
        }
    }

    fun verifyCertificateNumber(
        certNumber: String,
        onResult: (Result<Map<String, Any?>>) -> Unit
    ) {
        val cleanNo = certNumber.trim().uppercase()
        if (cleanNo.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Nomor sertifikat tidak boleh kosong.")))
            return
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val snap = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("certificates")
                    .document(cleanNo)
                    .get()
                    .await()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (snap.exists() && snap.data != null) {
                        onResult(Result.success(snap.data!!))
                    } else {
                        onResult(Result.failure(Exception("Sertifikat dengan nomor $cleanNo tidak ditemukan.")))
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(Result.failure(Exception("Gagal memverifikasi sertifikat: ${e.message}")))
                }
            }
        }
    }

    // =========================================================================
    // SOSIAL GALERI (LIKE, REPORT, MODERASI ADMIN) & DISKUSI PER MATERI (Fase 6)
    // =========================================================================

    fun toggleGalleryLike(
        item: DesignTaskSubmission,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val current = user.value
            val uid = current?.firebaseUid?.ifBlank { "user_${current.id}" } ?: return@launch
            if (!CommunityRulesUtils.canLikeGallerySubmission(item.uid, uid)) {
                onError("Kamu tidak bisa menyukai karya milik sendiri.")
                return@launch
            }
            val result = designTaskRepository.toggleLike(item.uid, item.subMateriId, uid)
            val updated = result.getOrNull()
            if (updated != null) {
                _publicGalleryItems.value = _publicGalleryItems.value.map {
                    if (it.uid == updated.uid && it.subMateriId == updated.subMateriId) updated else it
                }
            } else if (result.isFailure) {
                onError(result.exceptionOrNull()?.message ?: "Gagal memperbarui suka.")
            }
        }
    }

    // =========================================================================
    // TANTANGAN DESAIN (Tab Galeri) & RUANG DISKUSI Q&A PER MATERI (Bagian 5)
    // =========================================================================

    private val _challenges = MutableStateFlow<List<DesignChallenge>>(emptyList())
    val challenges: StateFlow<List<DesignChallenge>> = _challenges.asStateFlow()

    private val _challengeParticipants = MutableStateFlow<List<ChallengeParticipant>>(emptyList())
    val challengeParticipants: StateFlow<List<ChallengeParticipant>> = _challengeParticipants.asStateFlow()

    private val _qaPosts = MutableStateFlow<List<QaPost>>(emptyList())
    val qaPosts: StateFlow<List<QaPost>> = _qaPosts.asStateFlow()

    private val _qaRepliesByPost = MutableStateFlow<Map<String, List<QaReply>>>(emptyMap())
    val qaRepliesByPost: StateFlow<Map<String, List<QaReply>>> = _qaRepliesByPost.asStateFlow()

    fun loadChallenges() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val list = communityRepository.loadChallenges().getOrDefault(emptyList())
            _challenges.value = list
            val activeOrLatest = list.firstOrNull { it.status.equals("active", ignoreCase = true) } ?: list.firstOrNull()
            if (activeOrLatest != null) {
                val participants = communityRepository.loadChallengeParticipants(activeOrLatest.id)
                    .getOrDefault(emptyList())
                _challengeParticipants.value = participants
            } else {
                _challengeParticipants.value = emptyList()
            }
        }
    }

    fun joinChallenge(
        challenge: DesignChallenge,
        submission: DesignTaskSubmission,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val current = user.value
            val uid = current?.firebaseUid?.ifBlank { "user_${current.id}" } ?: ""
            val name = current?.nama ?: "Peserta FiksFaham"
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                communityRepository.joinChallenge(challenge, uid, name, submission)
            }
            if (result.isSuccess) {
                val joined = result.getOrThrow()
                _challengeParticipants.value = (_challengeParticipants.value.filterNot {
                    it.participantUid == joined.participantUid && it.challengeId == joined.challengeId
                }) + joined
                repository.grantChallengeJoinCoin(challenge.id)
                syncLeaderboardPoints()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Gagal mengikuti tantangan.")
            }
        }
    }

    fun saveChallenge(
        challenge: DesignChallenge,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val current = user.value
            val uid = current?.firebaseUid?.ifBlank { "user_${current.id}" } ?: ""
            val enriched = if (challenge.createdByUid.isBlank()) challenge.copy(createdByUid = uid) else challenge
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                communityRepository.saveChallenge(enriched)
            }
            if (result.isSuccess) {
                loadChallenges()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Gagal menyimpan tantangan.")
            }
        }
    }

    fun deleteChallenge(
        challengeId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                communityRepository.deleteChallenge(challengeId)
            }
            if (result.isSuccess) {
                loadChallenges()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Gagal menghapus tantangan.")
            }
        }
    }

    fun loadQaPosts(materiId: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val posts = communityRepository.loadQaPosts(materiId).getOrDefault(emptyList())
            _qaPosts.value = posts
        }
    }

    fun addQaPost(
        materiId: Int,
        title: String,
        content: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val u = user.value
            val uid = u?.firebaseUid?.ifBlank { "local_${u.id}" } ?: "local"
            val res = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                communityRepository.createQaPost(
                    materiId = materiId,
                    uid = uid,
                    authorName = u?.nama ?: "Pembelajar",
                    authorRole = u?.role ?: "mahasiswa",
                    title = title,
                    content = content
                )
            }
            if (res.isSuccess) {
                _qaPosts.value = listOf(res.getOrThrow()) + _qaPosts.value
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.message ?: "Gagal mengirim pertanyaan.")
            }
        }
    }

    fun loadQaReplies(postId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val list = communityRepository.loadQaReplies(postId).getOrDefault(emptyList())
            _qaRepliesByPost.value = _qaRepliesByPost.value + (postId to list)
        }
    }

    fun addQaReply(
        postId: String,
        materiId: Int,
        content: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val u = user.value
            val uid = u?.firebaseUid?.ifBlank { "local_${u.id}" } ?: "local"
            val res = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                communityRepository.addQaReply(
                    postId = postId,
                    materiId = materiId,
                    uid = uid,
                    authorName = u?.nama ?: "Pembelajar",
                    authorRole = u?.role ?: "mahasiswa",
                    content = content
                )
            }
            if (res.isSuccess) {
                val created = res.getOrThrow()
                val existing = _qaRepliesByPost.value[postId].orEmpty()
                _qaRepliesByPost.value = _qaRepliesByPost.value + (postId to (existing + created))
                _qaPosts.value = _qaPosts.value.map {
                    if (it.id == postId) it.copy(replyCount = it.replyCount + 1) else it
                }
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.message ?: "Gagal mengirim balasan.")
            }
        }
    }

    fun toggleQaReplyHelpful(
        reply: QaReply,
        postAuthorUid: String,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val u = user.value
            val uid = u?.firebaseUid?.ifBlank { "local_${u.id}" } ?: ""
            val mentorFlag = _isAdmin.value || com.example.CapstoneStatusUtils.isStaffRole(u?.role)
            val res = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                communityRepository.markReplyHelpful(reply, postAuthorUid, uid, mentorFlag)
            }
            if (res.isSuccess) {
                val updated = res.getOrThrow()
                val updatedList = _qaRepliesByPost.value[reply.postId].orEmpty()
                    .map { if (it.id == updated.id) updated else it }
                    .sortedWith(compareByDescending<QaReply> { it.membantu }.thenBy { it.createdAt })
                _qaRepliesByPost.value = _qaRepliesByPost.value + (reply.postId to updatedList)
                _qaPosts.value = _qaPosts.value.map {
                    if (it.id == reply.postId) it.copy(hasHelpfulReply = updatedList.any { r -> r.membantu }) else it
                }
            } else {
                onError(res.exceptionOrNull()?.message ?: "Tidak memiliki izin menandai jawaban.")
            }
        }
    }

    fun reportQaContent(
        targetType: String,
        targetId: String,
        targetOwnerUid: String,
        reason: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val u = user.value
            val reporterUid = u?.firebaseUid?.ifBlank { "local_${u.id}" } ?: ""
            val res = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                communityRepository.reportQaContent(targetType, targetId, targetOwnerUid, reporterUid, reason)
            }
            if (res.isSuccess) onSuccess()
            else onError(res.exceptionOrNull()?.message ?: "Gagal mengirim laporan.")
        }
    }

    fun deleteQaPost(postId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                communityRepository.deleteQaPost(postId)
            }
            if (res.isSuccess) {
                _qaPosts.value = _qaPosts.value.filterNot { it.id == postId }
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.message ?: "Gagal menghapus pertanyaan.")
            }
        }
    }

    fun deleteQaReply(reply: QaReply, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val res = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                communityRepository.deleteQaReply(reply.id, reply.postId)
            }
            if (res.isSuccess) {
                val updated = _qaRepliesByPost.value[reply.postId].orEmpty().filterNot { it.id == reply.id }
                _qaRepliesByPost.value = _qaRepliesByPost.value + (reply.postId to updated)
                _qaPosts.value = _qaPosts.value.map {
                    if (it.id == reply.postId) it.copy(replyCount = (it.replyCount - 1).coerceAtLeast(0)) else it
                }
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.message ?: "Gagal menghapus balasan.")
            }
        }
    }

    fun reportGallerySubmission(
        item: DesignTaskSubmission,
        reason: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val current = user.value
            val reporterUid = current?.firebaseUid?.ifBlank { "user_${current.id}" }.orEmpty()
            val result = designTaskRepository.reportSubmission(item.uid, item.subMateriId, reporterUid, reason)
            if (result.isSuccess) onSuccess()
            else onError(result.exceptionOrNull()?.message ?: "Gagal mengirim laporan.")
        }
    }

    fun adminDeleteGallerySubmission(
        item: DesignTaskSubmission,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = designTaskRepository.adminDeleteSubmission(item.uid, item.subMateriId)
            if (result.isSuccess) {
                _publicGalleryItems.value = _publicGalleryItems.value.filterNot {
                    it.uid == item.uid && it.subMateriId == item.subMateriId
                }
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Gagal menghapus karya.")
            }
        }
    }

    fun markNotificationRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun updateProfile(
        name: String,
        instansi: String,
        email: String,
        avatarUrl: String? = null,
        photoUri: Uri? = null,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            var finalAvatar = avatarUrl
            if (photoUri != null) {
                val current = user.value
                val uid = current?.firebaseUid.orEmpty().ifBlank { current?.id?.toString() ?: "1" }
                val uploadResult = repository.uploadProfilePhoto(photoUri, uid)
                if (uploadResult.isSuccess) {
                    finalAvatar = uploadResult.getOrNull()
                } else if (finalAvatar.isNullOrBlank()) {
                    finalAvatar = photoUri.toString()
                }
            }
            repository.updateUser(name, instansi, email, finalAvatar)
            syncLeaderboardPoints()
            onComplete()
        }
    }

    fun updatePassword(newPassword: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updatePassword(newPassword)
            onComplete()
        }
    }

    suspend fun getQuestionsForAssessment(assessmentId: Int): List<QuestionEntity> {
        return repository.getQuestionsForAssessment(assessmentId)
    }

    suspend fun getQuestionsForMateriAssessment(materiId: Int, tipe: String): List<QuestionEntity> {
        val assessment = repository.getAssessmentForMateri(materiId, tipe)
        return if (assessment != null) {
            repository.getQuestionsForAssessment(assessment.id)
        } else {
            val fallbackId = if (tipe == "pretest") 10 else 20
            repository.getQuestionsForAssessment(fallbackId)
        }
    }

    suspend fun getQuestionById(questionId: Int): QuestionEntity? {
        return repository.getQuestionById(questionId)
    }

    fun getSections(moduleId: Int) = repository.getSections(moduleId)
    fun getVideo(moduleId: Int) = repository.getVideo(moduleId)

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FiksFahamViewModel(application) as T
                }
            }
    }
}
