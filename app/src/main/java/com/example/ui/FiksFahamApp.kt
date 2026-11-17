package com.example.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.profile.getAvatarOption
import com.example.data.model.ModuleSectionEntity
import com.example.data.model.ModuleVideoEntity
import com.example.data.model.QuestionEntity
import com.example.ui.auth.ForgotPasswordScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.OnboardingScreen
import com.example.ui.auth.SignUpScreen
import com.example.ui.auth.SplashScreen
import com.example.ui.capstone.CapstoneScreen
import com.example.ui.components.FiksBottomNavBar
import com.example.ui.components.MainTab
import com.example.ui.course.CourseScreen
import com.example.ui.course.LevelQuizScreen
import com.example.ui.course.LevelSubtopicsScreen
import com.example.ui.course.PosttestDialog
import com.example.ui.course.PretestDialog
import com.example.ui.course.SubtopicDetailScreen
import com.example.ui.home.HomeScreen
import com.example.ui.leaderboard.LeaderboardScreen
import com.example.ui.news.CreatorProfileScreen
import com.example.ui.news.DiscussionScreen
import com.example.ui.news.NewsScreen
import com.example.ui.profile.AccessibilityScreen
import com.example.ui.profile.CertificateScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.profile.SavedModulesScreen
import com.example.ui.profile.SecurityScreen
import com.example.ui.gallery.GalleryScreen
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed interface AppDestination {
    data class TabView(val tab: MainTab) : AppDestination
    data class LevelSubtopics(val moduleId: Int) : AppDestination
    data class SubtopicDetail(val moduleId: Int, val sectionId: Int) : AppDestination
    data class LevelQuiz(val moduleId: Int) : AppDestination
    data object CapstoneView : AppDestination
    data class CreatorProfile(val creatorId: Int) : AppDestination
    data object LeaderboardView : AppDestination
    data object AccessibilityView : AppDestination
    data object SecurityView : AppDestination
    data object CertificateView : AppDestination
    data class GalleryView(val initialTab: Int = 0) : AppDestination
    data object SavedModulesView : AppDestination
    data class DiscussionView(val materiId: Int) : AppDestination
}

private fun AppDestination.encode(): String = when (this) {
    is AppDestination.TabView -> "TAB:${tab.name}"
    is AppDestination.LevelSubtopics -> "LEVEL:$moduleId"
    is AppDestination.SubtopicDetail -> "DETAIL:$moduleId:$sectionId"
    is AppDestination.LevelQuiz -> "QUIZ:$moduleId"
    AppDestination.CapstoneView -> "CAPSTONE"
    is AppDestination.CreatorProfile -> "CREATOR:$creatorId"
    AppDestination.LeaderboardView -> "LEADERBOARD"
    AppDestination.AccessibilityView -> "ACCESSIBILITY"
    AppDestination.SecurityView -> "SECURITY"
    AppDestination.CertificateView -> "CERTIFICATE"
    is AppDestination.GalleryView -> "GALLERY:$initialTab"
    AppDestination.SavedModulesView -> "SAVED"
    is AppDestination.DiscussionView -> "DISCUSSION:$materiId"
}

private fun decodeAppDestination(raw: String): AppDestination? {
    val parts = raw.split(":")
    return when (parts.firstOrNull()) {
        "TAB" -> parts.getOrNull(1)?.let { name ->
            runCatching { MainTab.valueOf(name) }.getOrNull()?.let { AppDestination.TabView(it) }
        }
        "LEVEL" -> parts.getOrNull(1)?.toIntOrNull()?.let { AppDestination.LevelSubtopics(it) }
        "DETAIL" -> {
            val m = parts.getOrNull(1)?.toIntOrNull()
            val s = parts.getOrNull(2)?.toIntOrNull()
            if (m != null && s != null) AppDestination.SubtopicDetail(m, s) else null
        }
        "QUIZ" -> parts.getOrNull(1)?.toIntOrNull()?.let { AppDestination.LevelQuiz(it) }
        "CAPSTONE" -> AppDestination.CapstoneView
        "CREATOR" -> parts.getOrNull(1)?.toIntOrNull()?.let { AppDestination.CreatorProfile(it) }
        "LEADERBOARD" -> AppDestination.LeaderboardView
        "ACCESSIBILITY" -> AppDestination.AccessibilityView
        "SECURITY" -> AppDestination.SecurityView
        "CERTIFICATE" -> AppDestination.CertificateView
        "GALLERY" -> AppDestination.GalleryView(parts.getOrNull(1)?.toIntOrNull() ?: 0)
        "SAVED" -> AppDestination.SavedModulesView
        "DISCUSSION" -> parts.getOrNull(1)?.toIntOrNull()?.let { AppDestination.DiscussionView(it) }
        else -> null
    }
}

private val AppBackStackSaver: Saver<SnapshotStateList<AppDestination>, List<String>> = listSaver(
    save = { stack -> stack.map { it.encode() } },
    restore = { saved ->
        saved.mapNotNull(::decodeAppDestination)
            .ifEmpty { listOf(AppDestination.TabView(MainTab.HOME)) }
            .toMutableStateList()
    }
)

private class LocaleContextWrapper(
    base: Context,
    private val localizedResources: Resources
) : ContextWrapper(base) {
    override fun getResources(): Resources = localizedResources
}

@Composable
private fun ProvideAppLocale(
    appLanguage: AppLanguage,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val baseConfig = LocalConfiguration.current
    val targetLocale = remember(appLanguage) {
        if (appLanguage == AppLanguage.ENGLISH) Locale("en") else Locale("in", "ID")
    }
    SideEffect {
        if (Locale.getDefault() != targetLocale) {
            Locale.setDefault(targetLocale)
        }
    }
    val localizedConfig = remember(baseConfig, targetLocale) {
        Configuration(baseConfig).apply { setLocale(targetLocale) }
    }
    val localizedContext = remember(baseContext, localizedConfig) {
        val cfgCtx = baseContext.createConfigurationContext(localizedConfig)
        LocaleContextWrapper(baseContext, cfgCtx.resources)
    }
    CompositionLocalProvider(
        LocalConfiguration provides localizedConfig,
        LocalContext provides localizedContext,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiksFahamApp(
    viewModel: FiksFahamViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    ProvideAppLocale(appLanguage = appLanguage) {
        FiksFahamAppContent(
            viewModel = viewModel,
            appLanguage = appLanguage,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiksFahamAppContent(
    viewModel: FiksFahamViewModel,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val materis by viewModel.materis.collectAsStateWithLifecycle()
    val selectedMateriId by viewModel.selectedMateriId.collectAsStateWithLifecycle()
    val modules by viewModel.modules.collectAsStateWithLifecycle()
    val progressList by viewModel.progressList.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val allAttempts by viewModel.allAttempts.collectAsStateWithLifecycle()
    val assessments by viewModel.assessments.collectAsStateWithLifecycle()

    val creators by viewModel.filteredCreators.collectAsStateWithLifecycle()
    val curatedVideos by viewModel.filteredCuratedVideos.collectAsStateWithLifecycle()
    val bootcamps by viewModel.filteredBootcamps.collectAsStateWithLifecycle()

    val contentSearch by viewModel.newsContentSearch.collectAsStateWithLifecycle()
    val bootcampSearch by viewModel.newsBootcampSearch.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val authScreenState by viewModel.authScreenState.collectAsStateWithLifecycle()
    val viewedSectionIds by viewModel.viewedSectionIds.collectAsStateWithLifecycle()
    val allCapstoneSubmissions by viewModel.allCapstoneSubmissions.collectAsStateWithLifecycle()
    val designTasks by viewModel.designTasks.collectAsStateWithLifecycle()
    val designTaskSubmissions by viewModel.designTaskSubmissions.collectAsStateWithLifecycle()
    val inspirasiList by viewModel.inspirasiList.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val bookmarkedModuleIds by viewModel.bookmarkedModuleIds.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 1. Auth Flow Transitions
    if (authScreenState != AuthScreenState.AUTHENTICATED) {
        AnimatedContent(
            targetState = authScreenState,
            transitionSpec = {
                when {
                    // Login -> Register / Forgot Password: Slide in from right with fade
                    initialState == AuthScreenState.LOGIN && (targetState == AuthScreenState.REGISTER || targetState == AuthScreenState.FORGOT_PASSWORD) -> {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(320)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> -(fullWidth * 0.35f).toInt() },
                                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(220))
                            )
                    }
                    // Register / Forgot Password -> Login: Slide in from left with fade
                    (initialState == AuthScreenState.REGISTER || initialState == AuthScreenState.FORGOT_PASSWORD) && targetState == AuthScreenState.LOGIN -> {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> -(fullWidth * 0.35f).toInt() },
                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(320)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(220))
                            )
                    }
                    // Default smooth fade
                    else -> {
                        fadeIn(animationSpec = tween(280))
                            .togetherWith(fadeOut(animationSpec = tween(200)))
                    }
                }
            },
            label = "auth_screen_flow"
        ) { state ->
            when (state) {
                AuthScreenState.SPLASH -> {
                    SplashScreen(
                        onTimeout = { viewModel.completeSplash() }
                    )
                }
                AuthScreenState.ONBOARDING -> {
                    OnboardingScreen(
                        onFinishOnboarding = { viewModel.completeOnboarding() }
                    )
                }
                AuthScreenState.LOGIN -> {
                    LoginScreen(
                        onLoginSubmit = { email, password ->
                            viewModel.loginWithAuth(email, password)
                        },
                        onNavigateToSignUp = { viewModel.navigateToRegister() },
                        onNavigateToForgotPassword = { viewModel.navigateToForgotPassword() },
                        onGoogleSignInSubmit = { idToken ->
                            viewModel.loginWithGoogle(idToken)
                        }
                    )
                }
                AuthScreenState.REGISTER -> {
                    BackHandler { viewModel.navigateToLogin() }
                    SignUpScreen(
                        onSignUpSubmit = { name, email, password, instansi ->
                            viewModel.registerWithAuth(name, email, password, instansi)
                        },
                        onNavigateToLogin = { viewModel.navigateToLogin() },
                        onGoogleSignInSubmit = { idToken ->
                            viewModel.loginWithGoogle(idToken)
                        }
                    )
                }
                AuthScreenState.FORGOT_PASSWORD -> {
                    BackHandler { viewModel.navigateToLogin() }
                    ForgotPasswordScreen(
                        onResetPasswordSubmit = { email ->
                            viewModel.sendPasswordReset(email)
                        },
                        onNavigateBackToLogin = { viewModel.navigateToLogin() }
                    )
                }
                AuthScreenState.AUTHENTICATED -> {
                    // Handled by main layout below
                }
            }
        }
        return
    }

    val navStack = rememberSaveable(saver = AppBackStackSaver) {
        mutableStateListOf<AppDestination>(AppDestination.TabView(MainTab.HOME))
    }
    val currentDestination = navStack.lastOrNull() ?: AppDestination.TabView(MainTab.HOME)
    val isFacilitatorOrAdmin = isAdmin || com.example.CapstoneStatusUtils.isStaffRole(homeUiState.user?.role)

    fun navigateTo(dest: AppDestination) {
        if (dest == currentDestination) return
        if (dest is AppDestination.TabView) {
            navStack.clear()
            navStack.add(AppDestination.TabView(MainTab.HOME))
            if (dest.tab != MainTab.HOME) {
                navStack.add(dest)
            }
        } else {
            navStack.add(dest)
        }
    }

    fun replaceTop(dest: AppDestination) {
        if (navStack.isEmpty()) {
            navStack.add(dest)
        } else {
            navStack[navStack.lastIndex] = dest
        }
    }

    fun popBack() {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.lastIndex)
        } else {
            navStack[0] = AppDestination.TabView(MainTab.HOME)
        }
    }

    fun isPretestDoneForMateri(materiId: Int): Boolean {
        val pretestId = assessments.firstOrNull { it.materiId == materiId && it.tipe == "pretest" }?.id
            ?: if (materiId == 1) 10 else materiId * 100 + 10
        return allAttempts.any { it.assessmentId == pretestId && it.isPassed }
    }

    // Dialog states
    var activePretestMateriId by rememberSaveable { mutableIntStateOf(1) }
    var activePosttestMateriId by rememberSaveable { mutableIntStateOf(1) }
    var showPretestDialog by remember { mutableStateOf(false) }
    var showPosttestDialog by remember { mutableStateOf(false) }
    var pretestQuestions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var posttestQuestions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var posttestCooldownSeconds by remember { mutableLongStateOf(0L) }

    val coroutineScope = rememberCoroutineScope()

    // Pretest questions loader
    fun loadAndOpenPretest(materiId: Int = selectedMateriId) {
        coroutineScope.launch {
            activePretestMateriId = materiId
            pretestQuestions = viewModel.getQuestionsForMateriAssessment(materiId, "pretest")
            showPretestDialog = true
        }
    }

    // Posttest questions loader
    fun loadAndOpenPosttest(materiId: Int = selectedMateriId) {
        coroutineScope.launch {
            activePosttestMateriId = materiId
            val posttestId = assessments.firstOrNull { it.materiId == materiId && it.tipe == "posttest" }?.id
                ?: com.example.util.CourseProgressUtils.getPosttestAssessmentId(materiId)
            posttestCooldownSeconds = viewModel.getAssessmentCooldownRemainingSeconds(posttestId, "posttest")
            posttestQuestions = viewModel.getQuestionsForMateriAssessment(materiId, "posttest")
            showPosttestDialog = true
        }
    }

    // Handle Hardware Back Button
    BackHandler(enabled = navStack.size > 1 || currentDestination != AppDestination.TabView(MainTab.HOME)) {
        popBack()
    }

    // Dialogs
    if (showPretestDialog) {
        PretestDialog(
            questions = pretestQuestions,
            modules = modules,
            onCompletePretest = { score ->
                viewModel.submitPretest(activePretestMateriId, score)
            },
            onDismiss = { showPretestDialog = false },
            onNavigateToCourse = {
                showPretestDialog = false
                navigateTo(AppDestination.TabView(MainTab.COURSE))
            }
        )
    }

    if (showPosttestDialog) {
        PosttestDialog(
            questions = posttestQuestions,
            modules = modules,
            cooldownRemainingSeconds = posttestCooldownSeconds,
            onCompletePosttest = { score ->
                viewModel.submitPosttest(activePosttestMateriId, score)
            },
            onCompletePosttestWithWrongIds = { score, wrongIds ->
                viewModel.submitPosttest(activePosttestMateriId, score, wrongIds)
            },
            onJumpToModule = { moduleId ->
                showPosttestDialog = false
                navigateTo(AppDestination.LevelSubtopics(moduleId))
            },
            onDismiss = { showPosttestDialog = false }
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var drawerSettingsExpanded by remember { mutableStateOf(false) }
    var showDiscussionPickerDialog by remember { mutableStateOf(false) }

    if (showDiscussionPickerDialog) {
        BasicAlertDialog(onDismissRequest = { showDiscussionPickerDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.drawer_discussion_picker_title),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = FiksText
                        )
                        IconButton(
                            onClick = { showDiscussionPickerDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.common_close), tint = FiksTextMuted)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.drawer_discussion_picker_sub),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksTextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val sortedMateris = materis.sortedBy { it.urutan }
                    sortedMateris.forEachIndexed { idx, materi ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FiksBg,
                            border = BorderStroke(1.dp, FiksCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDiscussionPickerDialog = false
                                    viewModel.selectMateri(materi.id)
                                    viewModel.loadQaPosts(materi.id)
                                    navigateTo(AppDestination.DiscussionView(materi.id))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = FiksPrimary.copy(alpha = 0.14f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${materi.urutan}",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = FiksPrimary
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Materi ${materi.urutan}: ${materi.judul}",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = FiksText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = FiksTextMuted
                                )
                            }
                        }
                        if (idx < sortedMateris.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentDestination is AppDestination.TabView,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = FiksScoreCard,
                modifier = Modifier.fillMaxWidth(0.80f)
            ) {
                val drawerColors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = FiksScoreCard,
                    unselectedTextColor = FiksText,
                    unselectedIconColor = FiksPrimary
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header: Foto Profil + Username + Email
                    val drawerUser = homeUiState.user
                    val drawerAvatar = getAvatarOption(drawerUser?.avatarUrl)
                    val hasDrawerPhoto = !drawerUser?.avatarUrl.isNullOrBlank() && (
                        drawerUser!!.avatarUrl.startsWith("http") ||
                        drawerUser.avatarUrl.startsWith("content:") ||
                        drawerUser.avatarUrl.startsWith("file:")
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FiksBg,
                            border = BorderStroke(2.dp, FiksPrimary),
                            modifier = Modifier.size(54.dp)
                        ) {
                            if (hasDrawerPhoto) {
                                AsyncImage(
                                    model = drawerUser?.avatarUrl,
                                    contentDescription = drawerUser?.nama,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(drawerAvatar.gradientColors))
                                ) {
                                    Icon(
                                        imageVector = drawerAvatar.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = drawerUser?.nama?.ifBlank { "Peserta FiksFaham" } ?: "Peserta FiksFaham",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = FiksText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = drawerUser?.email.orEmpty(),
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (com.example.CapstoneStatusUtils.isStaffRole(drawerUser?.role) || isAdmin) {
                                Spacer(modifier = Modifier.height(3.dp))
                                val roleLabel = when {
                                    drawerUser?.role.equals("superadmin", ignoreCase = true) || isAdmin -> "Super Admin"
                                    drawerUser?.role.equals("fasilitator", ignoreCase = true) -> "Fasilitator UI/UX"
                                    else -> "Mentor UI/UX"
                                }
                                Text(
                                    text = roleLabel,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = FiksPrimary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = FiksCardBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.drawer_menu_profile), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium) },
                        selected = false,
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        colors = drawerColors,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navigateTo(AppDestination.TabView(MainTab.PROFIL))
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.drawer_menu_gallery), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium) },
                        selected = false,
                        icon = { Icon(Icons.Default.Palette, contentDescription = null) },
                        colors = drawerColors,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navigateTo(AppDestination.GalleryView())
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.drawer_menu_discussion), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium) },
                        selected = false,
                        icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                        colors = drawerColors,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showDiscussionPickerDialog = true
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.drawer_menu_leaderboard), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium) },
                        selected = false,
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                        colors = drawerColors,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navigateTo(AppDestination.TabView(MainTab.LEADERBOARD))
                        }
                    )
                    NavigationDrawerItem(
                        label = {
                            Text(
                                stringResource(R.string.drawer_menu_saved_modules, bookmarkedModuleIds.size),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        selected = false,
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                        colors = drawerColors,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navigateTo(AppDestination.SavedModulesView)
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.drawer_menu_certificates), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium) },
                        selected = false,
                        icon = { Icon(Icons.Default.Verified, contentDescription = null) },
                        colors = drawerColors,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navigateTo(AppDestination.CertificateView)
                        }
                    )

                    // Pengaturan Lanjutan (Bahasa, Mode Gelap/Terang, Keamanan Akun)
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.drawer_menu_advanced_settings), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium) },
                        selected = drawerSettingsExpanded,
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        badge = {
                            Icon(
                                imageVector = if (drawerSettingsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = FiksTextMuted
                            )
                        },
                        colors = drawerColors,
                        onClick = { drawerSettingsExpanded = !drawerSettingsExpanded }
                    )

                    AnimatedVisibility(visible = drawerSettingsExpanded) {
                        Column(modifier = Modifier.padding(start = 18.dp, end = 12.dp, bottom = 6.dp)) {
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        "${stringResource(R.string.profile_menu_language)}: ${if (appLanguage == AppLanguage.INDONESIAN) "ID" else "EN"}",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 13.sp
                                    )
                                },
                                selected = false,
                                icon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                colors = drawerColors,
                                onClick = {
                                    val nextLang = if (appLanguage == AppLanguage.INDONESIAN) AppLanguage.ENGLISH else AppLanguage.INDONESIAN
                                    viewModel.setAppLanguage(nextLang)
                                }
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = FiksPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.profile_menu_theme),
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 13.sp,
                                        color = FiksText
                                    )
                                }
                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = { viewModel.setDarkMode(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = FiksPrimary
                                    )
                                )
                            }
                            NavigationDrawerItem(
                                label = { Text(stringResource(R.string.profile_menu_security), fontFamily = PoppinsFontFamily, fontSize = 13.sp) },
                                selected = false,
                                icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                colors = drawerColors,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navigateTo(AppDestination.SecurityView)
                                }
                            )
                        }
                    }

                    // Menu Staf (khusus Fasilitator/Mentor atau Super Admin)
                    if (isFacilitatorOrAdmin) {
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.drawer_menu_mentor), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold) },
                            selected = false,
                            icon = { Icon(Icons.Default.RateReview, contentDescription = null) },
                            colors = drawerColors,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                navigateTo(AppDestination.CapstoneView)
                            }
                        )
                    }
                }
                HorizontalDivider(color = FiksCardBorder)
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.profile_logout_confirm), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = FiksScoreRed) },
                    selected = false,
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = FiksScoreRed) },
                    colors = drawerColors,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.logout()
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold(
            bottomBar = {
            if (currentDestination is AppDestination.TabView) {
                val activeTab = (currentDestination as AppDestination.TabView).tab
                FiksBottomNavBar(
                    selectedTab = activeTab,
                    appLanguage = appLanguage,
                    onTabSelected = { newTab ->
                        // FR-PRE-01: Saat pertama membuka Course, jika pretest materi aktif belum selesai, otomatis tawarkan pretest!
                        if (newTab == MainTab.COURSE && !isPretestDoneForMateri(selectedMateriId)) {
                            loadAndOpenPretest(selectedMateriId)
                        }
                        navigateTo(AppDestination.TabView(newTab))
                    }
                )
            }
        },
        containerColor = FiksBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(FiksBg)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    fun destRank(dest: AppDestination): Int = when (dest) {
                        is AppDestination.TabView -> dest.tab.ordinal
                        is AppDestination.LevelSubtopics -> 10
                        is AppDestination.SubtopicDetail -> 11
                        is AppDestination.LevelQuiz -> 12
                        is AppDestination.CapstoneView -> 13
                        is AppDestination.CreatorProfile -> 14
                        is AppDestination.LeaderboardView -> 15
                        is AppDestination.AccessibilityView -> 16
                        is AppDestination.SecurityView -> 17
                        is AppDestination.CertificateView -> 18
                        is AppDestination.GalleryView -> 19
                        is AppDestination.SavedModulesView -> 20
                        is AppDestination.DiscussionView -> 21
                    }
                    val initialRank = destRank(initialState)
                    val targetRank = destRank(targetState)
                    if (targetRank >= initialRank) {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth / 2 },
                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(240))) togetherWith (
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth / 2 },
                                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(180))
                        )
                    } else {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth / 2 },
                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(240))) togetherWith (
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth / 2 },
                                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(180))
                        )
                    }
                },
                label = "navigation_transition"
            ) { target ->
                when (target) {
                    is AppDestination.TabView -> {
                        when (target.tab) {
                            MainTab.HOME -> {
                                HomeScreen(
                                    uiState = homeUiState,
                                    modules = modules,
                                    progressList = progressList,
                                    allAttempts = allAttempts,
                                    notifications = notifications,
                                    onNavigateToCourse = {
                                        if (!isPretestDoneForMateri(selectedMateriId)) {
                                            loadAndOpenPretest(selectedMateriId)
                                        }
                                        navigateTo(AppDestination.TabView(MainTab.COURSE))
                                    },
                                    onOpenLevel = { moduleId ->
                                        navigateTo(AppDestination.LevelSubtopics(moduleId))
                                    },
                                    onOpenPretest = { loadAndOpenPretest(selectedMateriId) },
                                    onOpenPosttest = { loadAndOpenPosttest(selectedMateriId) },
                                    onOpenCapstone = {
                                        navigateTo(AppDestination.CapstoneView)
                                    },
                                    onMarkAllNotificationsRead = {
                                        viewModel.markAllNotificationsRead()
                                    },
                                    onNotificationClick = { notif ->
                                        viewModel.markNotificationRead(notif.id)
                                        when {
                                            notif.assessmentId == 999 || notif.judul.contains("Capstone", ignoreCase = true) -> {
                                                navigateTo(AppDestination.CapstoneView)
                                            }
                                            notif.assessmentId % 100 == 10 || notif.judul.contains("Pretest", ignoreCase = true) -> {
                                                val mId = if (notif.assessmentId <= 10) 1 else notif.assessmentId / 100
                                                loadAndOpenPretest(mId.coerceAtLeast(1))
                                            }
                                            notif.assessmentId % 100 == 20 || notif.judul.contains("Posttest", ignoreCase = true) -> {
                                                val mId = if (notif.assessmentId <= 20) 1 else notif.assessmentId / 100
                                                loadAndOpenPosttest(mId.coerceAtLeast(1))
                                            }
                                            modules.any { it.id == notif.assessmentId } -> {
                                                navigateTo(AppDestination.LevelSubtopics(notif.assessmentId))
                                            }
                                            else -> {
                                                navigateTo(AppDestination.TabView(MainTab.COURSE))
                                            }
                                        }
                                    },
                                    onOpenLeaderboard = {
                                        navigateTo(AppDestination.TabView(MainTab.LEADERBOARD))
                                    },
                                    onOpenGallery = {
                                        navigateTo(AppDestination.GalleryView())
                                    },
                                    materis = materis,
                                    assessments = assessments,
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onOpenMateri = { materiId ->
                                        viewModel.selectMateri(materiId)
                                        navigateTo(AppDestination.TabView(MainTab.COURSE))
                                    }
                                )
                            }
                            MainTab.COURSE -> {
                                CourseScreen(
                                    materis = materis,
                                    selectedMateriId = selectedMateriId,
                                    onSelectMateri = { viewModel.selectMateri(it) },
                                    modules = modules,
                                    progressList = progressList,
                                    assessments = assessments,
                                    allAttempts = allAttempts,
                                    isPretestDone = isPretestDoneForMateri(selectedMateriId),
                                    isPosttestDone = com.example.util.CourseProgressUtils.isPosttestPassed(
                                        selectedMateriId,
                                        assessments,
                                        allAttempts
                                    ),
                                    onOpenPretest = { materiId -> loadAndOpenPretest(materiId) },
                                    onOpenPosttest = { materiId -> loadAndOpenPosttest(materiId) },
                                    onOpenCapstone = { _ -> navigateTo(AppDestination.CapstoneView) },
                                    onOpenGallery = { navigateTo(AppDestination.GalleryView()) },
                                    onSelectModule = { moduleId ->
                                        navigateTo(AppDestination.LevelSubtopics(moduleId))
                                    }
                                )
                            }
                            MainTab.LEADERBOARD -> {
                                LeaderboardScreen(
                                    user = homeUiState.user,
                                    userPoints = homeUiState.userPoints,
                                    leaderboardEntries = homeUiState.leaderboardEntries,
                                    userRank = homeUiState.userRank,
                                    leaderboardDocs = homeUiState.leaderboardDocs,
                                    currentUserSummary = homeUiState.currentUserSummary,
                                    isOffline = homeUiState.isLeaderboardOffline,
                                    onBack = null,
                                    dailyCoinEntries = homeUiState.dailyCoinEntries,
                                    monthlyCoinEntries = homeUiState.monthlyCoinEntries,
                                    userDailyCoins = homeUiState.userDailyCoins,
                                    userMonthlyCoins = homeUiState.userMonthlyCoins
                                )
                            }
                            MainTab.NEWS -> {
                                val publicGalleryItems by viewModel.publicGalleryItems.collectAsStateWithLifecycle()
                                val challenges by viewModel.challenges.collectAsStateWithLifecycle()
                                val challengeParticipants by viewModel.challengeParticipants.collectAsStateWithLifecycle()
                                val currentUid = homeUiState.user?.firebaseUid.orEmpty()
                                val userOwnPublicWorks = remember(publicGalleryItems, designTaskSubmissions, currentUid) {
                                    val fromCloud = publicGalleryItems.filter { it.uid == currentUid && it.visibility == "public" }
                                    val fromLocal = designTaskSubmissions
                                        .filter { it.visibility == "public" && it.url.isNotBlank() }
                                        .map { local ->
                                            com.example.data.repository.DesignTaskSubmission(
                                                uid = currentUid.ifBlank { local.uid },
                                                materiId = local.materiId,
                                                subMateriId = local.subMateriId,
                                                url = local.url,
                                                visibility = local.visibility,
                                                userName = local.userName,
                                                createdAt = local.createdAt
                                            )
                                        }
                                    (fromCloud + fromLocal).distinctBy { "${it.uid}_${it.subMateriId}" }
                                }
                                NewsScreen(
                                    creators = creators,
                                    curatedVideos = curatedVideos,
                                    bootcamps = bootcamps,
                                    contentSearchQuery = contentSearch,
                                    bootcampSearchQuery = bootcampSearch,
                                    onContentSearchChange = { viewModel.updateNewsContentSearch(it) },
                                    onBootcampSearchChange = { viewModel.updateNewsBootcampSearch(it) },
                                    onOpenCreatorProfile = { creatorId ->
                                        navigateTo(AppDestination.CreatorProfile(creatorId))
                                    },
                                    inspirasiList = inspirasiList,
                                    isAdmin = isAdmin,
                                    canManageChallenges = isFacilitatorOrAdmin,
                                    onAddInspirasi = { item, onSuccess, onError ->
                                        viewModel.addInspirasi(item, onSuccess, onError)
                                    },
                                    onUpdateInspirasi = { item, onSuccess, onError ->
                                        viewModel.updateInspirasi(item, onSuccess, onError)
                                    },
                                    onDeleteInspirasi = { id, onSuccess, onError ->
                                        viewModel.deleteInspirasi(id, onSuccess, onError)
                                    },
                                    publicGalleryItems = publicGalleryItems,
                                    currentUserUid = currentUid,
                                    onRefreshPublicGallery = { viewModel.loadPublicGallery(loadMore = false) },
                                    onToggleGalleryLike = { item -> viewModel.toggleGalleryLike(item) },
                                    onReportGallerySubmission = { item, reason, onSuccess, onError ->
                                        viewModel.reportGallerySubmission(item, reason, onSuccess, onError)
                                    },
                                    onAdminDeleteGallerySubmission = { item, onSuccess, onError ->
                                        viewModel.adminDeleteGallerySubmission(item, onSuccess, onError)
                                    },
                                    onOpenFullGallery = {
                                        navigateTo(AppDestination.GalleryView())
                                    },
                                    challenges = challenges,
                                    challengeParticipants = challengeParticipants,
                                    userOwnPublicWorks = userOwnPublicWorks,
                                    onRefreshChallenges = { viewModel.loadChallenges() },
                                    onJoinChallenge = { ch, work, onSuccess, onError ->
                                        viewModel.joinChallenge(ch, work, onSuccess, onError)
                                    },
                                    onSaveChallenge = { ch, onSuccess, onError ->
                                        viewModel.saveChallenge(ch, onSuccess, onError)
                                    },
                                    onDeleteChallenge = { chId, onSuccess, onError ->
                                        viewModel.deleteChallenge(chId, onSuccess, onError)
                                    },
                                    materis = materis
                                )
                            }
                            MainTab.PROFIL -> {
                                ProfileScreen(
                                    user = homeUiState.user,
                                    progressPercent = homeUiState.progressPercent,
                                    capstoneSubmission = homeUiState.capstoneSubmission,
                                    modules = modules,
                                    progressList = progressList,
                                    materis = materis,
                                    allAttempts = allAttempts,
                                    assessments = assessments,
                                    isDarkMode = isDarkMode,
                                    appLanguage = appLanguage,
                                    onToggleDarkMode = { viewModel.setDarkMode(it) },
                                    onSelectLanguage = { viewModel.setAppLanguage(it) },
                                    onUpdateProfile = { name, instansi, email, avatarUrl, photoUri ->
                                        viewModel.updateProfile(name, instansi, email, avatarUrl, photoUri)
                                    },
                                    onUpdatePassword = { newPassword ->
                                        viewModel.updatePassword(newPassword)
                                    },
                                    onVerifyEmailChange = { newEmail, pass ->
                                        viewModel.requestEmailVerificationChange(newEmail, pass)
                                    },
                                    onLogout = {
                                        viewModel.logout()
                                    },
                                    onNavigateToCapstone = {
                                        navigateTo(AppDestination.CapstoneView)
                                    },
                                    onNavigateToCourse = { moduleId ->
                                        if (moduleId != null) {
                                            navigateTo(AppDestination.LevelSubtopics(moduleId))
                                        } else {
                                            navigateTo(AppDestination.TabView(MainTab.COURSE))
                                        }
                                    },
                                    userPoints = homeUiState.userPoints,
                                    userMonthlyCoins = homeUiState.userMonthlyCoins,
                                    userRank = homeUiState.userRank,
                                    onNavigateToLeaderboard = {
                                        navigateTo(AppDestination.TabView(MainTab.LEADERBOARD))
                                    },
                                    onNavigateToSavedModules = {
                                        navigateTo(AppDestination.SavedModulesView)
                                    },
                                    onNavigateToCertificates = {
                                        navigateTo(AppDestination.CertificateView)
                                    },
                                    onNavigateToAccessibility = {
                                        navigateTo(AppDestination.AccessibilityView)
                                    },
                                    onNavigateToSecurity = {
                                        navigateTo(AppDestination.SecurityView)
                                    },
                                    onNavigateToGallery = {
                                        navigateTo(AppDestination.GalleryView())
                                    }
                                )
                            }
                        }
                    }

                    is AppDestination.LevelSubtopics -> {
                        val selectedModule = modules.firstOrNull { it.id == target.moduleId }
                        if (selectedModule != null) {
                            var levelSections by remember { mutableStateOf<List<ModuleSectionEntity>>(emptyList()) }
                            var levelVideo by remember { mutableStateOf<ModuleVideoEntity?>(null) }

                            LaunchedEffect(target.moduleId) {
                                coroutineScope {
                                    launch {
                                        levelSections = viewModel.getSections(target.moduleId).firstOrNull() ?: emptyList()
                                    }
                                    launch {
                                        levelVideo = viewModel.getVideo(target.moduleId).firstOrNull()
                                    }
                                }
                            }

                            val progress = progressList.firstOrNull { it.moduleId == target.moduleId }
                            val moduleAttempt = allAttempts
                                .filter { it.assessmentId == target.moduleId }
                                .maxByOrNull { it.attemptNo }

                            val subMateriDesignTask = designTasks.firstOrNull { it.subMateriId == target.moduleId }
                            val subMateriDesignSubmission = designTaskSubmissions.firstOrNull { it.subMateriId == target.moduleId }

                            LevelSubtopicsScreen(
                                module = selectedModule,
                                sections = levelSections,
                                video = levelVideo,
                                progress = progress,
                                viewedSectionIds = viewedSectionIds,
                                latestAttempt = moduleAttempt,
                                cooldownSeconds = 0L,
                                onSelectSubtopic = { sectionId ->
                                    navigateTo(AppDestination.SubtopicDetail(target.moduleId, sectionId))
                                },
                                onOpenQuiz = {
                                    navigateTo(AppDestination.LevelQuiz(target.moduleId))
                                },
                                onBackToCourse = {
                                    popBack()
                                },
                                designTask = subMateriDesignTask,
                                designTaskSubmission = subMateriDesignSubmission,
                                onUploadDesignTask = { uri, onProg, onSucc, onErr ->
                                    viewModel.uploadDesignTask(
                                        context = context,
                                        imageUri = uri,
                                        materiId = selectedModule.materiId,
                                        subMateriId = selectedModule.id,
                                        maxMb = subMateriDesignTask?.batasUkuranMb ?: 5,
                                        onProgress = onProg,
                                        onSuccess = onSucc,
                                        onError = onErr
                                    )
                                },
                                isBookmarked = target.moduleId in bookmarkedModuleIds,
                                onToggleBookmark = { viewModel.toggleBookmark(target.moduleId) },
                                onOpenDiscussion = {
                                    viewModel.loadQaPosts(selectedModule.materiId)
                                    navigateTo(AppDestination.DiscussionView(selectedModule.materiId))
                                },
                                isFacilitatorOrAdmin = isFacilitatorOrAdmin,
                                onSaveDesignTaskDefinition = { judul, instruksi, batasMb ->
                                    viewModel.saveDesignTaskDefinition(
                                        subMateriId = selectedModule.id,
                                        materiId = selectedModule.materiId,
                                        judulTugas = judul,
                                        instruksi = instruksi,
                                        batasUkuranMb = batasMb
                                    )
                                }
                            )
                        }
                    }

                    is AppDestination.SubtopicDetail -> {
                        val selectedModule = modules.firstOrNull { it.id == target.moduleId }
                        if (selectedModule != null) {
                            var levelSections by remember { mutableStateOf<List<ModuleSectionEntity>>(emptyList()) }
                            var levelVideo by remember { mutableStateOf<ModuleVideoEntity?>(null) }

                            LaunchedEffect(target.moduleId) {
                                coroutineScope {
                                    launch {
                                        levelSections = viewModel.getSections(target.moduleId).firstOrNull() ?: emptyList()
                                    }
                                    launch {
                                        levelVideo = viewModel.getVideo(target.moduleId).firstOrNull()
                                    }
                                }
                            }

                            val currentSection = levelSections.firstOrNull { it.id == target.sectionId }
                                ?: levelSections.firstOrNull()

                            val subMateriDesignTask = designTasks.firstOrNull { it.subMateriId == target.moduleId }
                            val subMateriDesignSubmission = designTaskSubmissions.firstOrNull { it.subMateriId == target.moduleId }
                            val moduleProgress = progressList.firstOrNull { it.moduleId == target.moduleId }

                            if (currentSection != null) {
                                SubtopicDetailScreen(
                                    module = selectedModule,
                                    section = currentSection,
                                    sections = levelSections,
                                    video = levelVideo,
                                    onMarkSectionViewed = { sectionId ->
                                        viewModel.markSectionViewed(
                                            moduleId = target.moduleId,
                                            sectionId = sectionId,
                                            allModuleSectionIds = levelSections.map { it.id }
                                        )
                                    },
                                    onMarkVideoWatched = {
                                        viewModel.markVideoWatched(target.moduleId)
                                    },
                                    onNavigateToSection = { newSectionId ->
                                        replaceTop(AppDestination.SubtopicDetail(target.moduleId, newSectionId))
                                    },
                                    onOpenQuiz = {
                                        navigateTo(AppDestination.LevelQuiz(target.moduleId))
                                    },
                                    onBackToSubtopics = {
                                        popBack()
                                    },
                                    designTask = subMateriDesignTask,
                                    designTaskSubmission = subMateriDesignSubmission,
                                    onUploadDesignTask = { uri, onProg, onSucc, onErr ->
                                        viewModel.uploadDesignTask(
                                            context = context,
                                            imageUri = uri,
                                            materiId = selectedModule.materiId,
                                            subMateriId = selectedModule.id,
                                            maxMb = subMateriDesignTask?.batasUkuranMb ?: 5,
                                            onProgress = onProg,
                                            onSuccess = onSucc,
                                            onError = onErr
                                        )
                                    },
                                    isBookmarked = target.moduleId in bookmarkedModuleIds,
                                    onToggleBookmark = { viewModel.toggleBookmark(target.moduleId) },
                                    isSubMateriFinished = moduleProgress?.isFinished == true || moduleProgress?.isCompleted == true,
                                    onFinishSubMateri = { viewModel.markSubMateriFinished(target.moduleId) },
                                    viewedSectionIds = viewedSectionIds,
                                    videoOpened = moduleProgress?.isVideoWatched == true,
                                    onOpenDiscussion = {
                                        viewModel.loadQaPosts(selectedModule.materiId)
                                        navigateTo(AppDestination.DiscussionView(selectedModule.materiId))
                                    },
                                    isFacilitatorOrAdmin = isFacilitatorOrAdmin,
                                    onSaveDesignTaskDefinition = { judul, instruksi, batasMb ->
                                        viewModel.saveDesignTaskDefinition(
                                            subMateriId = selectedModule.id,
                                            materiId = selectedModule.materiId,
                                            judulTugas = judul,
                                            instruksi = instruksi,
                                            batasUkuranMb = batasMb
                                        )
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(FiksBg)
                                        .padding(24.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.material3.Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = androidx.compose.material3.CardDefaults.cardColors(
                                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                    ) {
                                        androidx.compose.foundation.layout.Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                                        ) {
                                            androidx.compose.material3.Text(
                                                text = "Konten segera hadir",
                                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                                            )
                                            androidx.compose.material3.Text(
                                                text = "Materi bacaan untuk bagian ini sedang dalam tahap penyusunan.",
                                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            com.example.ui.components.FiksButton(
                                                text = "Kembali ke Sub Materi",
                                                onClick = { popBack() },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is AppDestination.LevelQuiz -> {
                        val selectedModule = modules.firstOrNull { it.id == target.moduleId }
                        if (selectedModule != null) {
                            var levelSections by remember { mutableStateOf<List<ModuleSectionEntity>>(emptyList()) }
                            var levelQuestions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }

                            LaunchedEffect(target.moduleId) {
                                coroutineScope {
                                    launch {
                                        levelSections = viewModel.getSections(target.moduleId).firstOrNull() ?: emptyList()
                                    }
                                    launch {
                                        levelQuestions = viewModel.getQuestionsForAssessment(target.moduleId)
                                    }
                                }
                            }

                            val moduleAttempt = allAttempts
                                .filter { it.assessmentId == target.moduleId }
                                .maxByOrNull { it.attemptNo }

                            LevelQuizScreen(
                                module = selectedModule,
                                sections = levelSections,
                                questions = levelQuestions,
                                latestAttempt = moduleAttempt,
                                cooldownRemainingSeconds = 0L,
                                onSubmitQuiz = { score, wrongIds ->
                                    viewModel.submitQuiz(target.moduleId, score, wrongIds) {}
                                },
                                onJumpToSubtopic = { sectionId ->
                                    navigateTo(AppDestination.SubtopicDetail(target.moduleId, sectionId))
                                },
                                onBackToSubtopics = {
                                    popBack()
                                }
                            )
                        }
                    }

                    is AppDestination.CreatorProfile -> {
                        val creator = creators.find { it.id == target.creatorId }
                            ?: com.example.data.db.InitialData.getCreators().find { it.id == target.creatorId }
                            ?: com.example.data.db.InitialData.getCreators().first()
                        val creatorVideos = curatedVideos.filter { it.creatorId == creator.id }
                        CreatorProfileScreen(
                            creator = creator,
                            videos = creatorVideos,
                            onBack = { popBack() }
                        )
                    }

                    is AppDestination.CapstoneView -> {
                        val mentorSubmissions by viewModel.mentorSubmissions.collectAsStateWithLifecycle()
                        val mentorFilter by viewModel.mentorFilter.collectAsStateWithLifecycle()
                        val isLoadingSubmissions by viewModel.isLoadingSubmissions.collectAsStateWithLifecycle()
                        val studentUid = homeUiState.user?.firebaseUid.orEmpty()

                        val effectiveCapstoneRole = when {
                            com.example.CapstoneStatusUtils.isStaffRole(homeUiState.user?.role) -> homeUiState.user?.role ?: "mahasiswa"
                            isAdmin -> "superadmin"
                            else -> "mahasiswa"
                        }

                        CapstoneScreen(
                            capstoneState = homeUiState.capstoneState,
                            submission = homeUiState.capstoneSubmission,
                            currentUserRole = effectiveCapstoneRole,
                            currentStudentUid = studentUid,
                            mentorSubmissions = mentorSubmissions,
                            mentorFilter = mentorFilter,
                            isLoadingSubmissions = isLoadingSubmissions,
                            onFilterChange = { filter ->
                                viewModel.setMentorFilter(filter)
                            },
                            onRefreshSubmissions = {
                                viewModel.loadMentorSubmissions()
                            },
                            onUploadFile = { uri, name, onProgress, onSuccess, onError ->
                                viewModel.uploadCapstoneFile(uri, name, onProgress, onSuccess, onError)
                            },
                            onSubmit = { title, desc, figmaUrl, fileUrl ->
                                viewModel.submitCapstone(title, desc, figmaUrl, fileUrl)
                            },
                            onReviewSubmission = { uid, materiId, score, feedback ->
                                viewModel.reviewCapstone(uid, materiId, score, feedback)
                            },
                            onReviewSubmissionWithDetails = { uid, materiId, score, feedback, status, rubricSummary ->
                                viewModel.reviewCapstone(uid, materiId, score, feedback, status, rubricSummary)
                            },
                            onObserveStudentCapstone = { uid ->
                                viewModel.observeStudentCapstone(uid)
                            },
                            onBack = { popBack() }
                        )
                    }

                    is AppDestination.LeaderboardView -> {
                        LeaderboardScreen(
                            user = homeUiState.user,
                            userPoints = homeUiState.userPoints,
                            leaderboardEntries = homeUiState.leaderboardEntries,
                            userRank = homeUiState.userRank,
                            leaderboardDocs = homeUiState.leaderboardDocs,
                            currentUserSummary = homeUiState.currentUserSummary,
                            isOffline = homeUiState.isLeaderboardOffline,
                            onBack = { popBack() },
                            dailyCoinEntries = homeUiState.dailyCoinEntries,
                            monthlyCoinEntries = homeUiState.monthlyCoinEntries,
                            userDailyCoins = homeUiState.userDailyCoins,
                            userMonthlyCoins = homeUiState.userMonthlyCoins
                        )
                    }

                    is AppDestination.AccessibilityView -> {
                        AccessibilityScreen(
                            currentThemeMode = themeMode,
                            currentLanguage = appLanguage,
                            onSelectThemeMode = { newTheme ->
                                viewModel.setThemeMode(newTheme)
                            },
                            onSelectLanguage = { newLang ->
                                viewModel.setAppLanguage(newLang)
                            },
                            onBack = { popBack() }
                        )
                    }

                    is AppDestination.SecurityView -> {
                        SecurityScreen(
                            onChangePassword = { oldPassword, newPassword ->
                                viewModel.changePasswordWithReauth(oldPassword, newPassword)
                            },
                            onLogout = {
                                viewModel.logout()
                            },
                            onDeleteAccount = { password ->
                                viewModel.deleteUserAccount(password)
                            },
                            isGoogleOnlyAccount = viewModel.isGoogleOnlyAccount(),
                            onDeleteAccountWithGoogle = { idToken ->
                                viewModel.deleteUserAccountWithGoogle(idToken)
                            },
                            onBack = { popBack() }
                        )
                    }

                    is AppDestination.CertificateView -> {
                        CertificateScreen(
                            user = homeUiState.user,
                            materis = materis,
                            allAttempts = allAttempts,
                            capstoneSubmissions = allCapstoneSubmissions,
                            allAssessments = assessments,
                            onRegisterCertificateNumber = { eligibility, cb ->
                                viewModel.ensureAndRegisterCertificateNumber(eligibility, cb)
                            },
                            onVerifyCertificateNumber = { certNum, cb ->
                                viewModel.verifyCertificateNumber(certNum, cb)
                            },
                            onOpenEditProfile = {
                                navigateTo(AppDestination.TabView(MainTab.PROFIL))
                            },
                            onBack = { popBack() }
                        )
                    }

                    is AppDestination.GalleryView -> {
                        val publicGalleryItems by viewModel.publicGalleryItems.collectAsStateWithLifecycle()
                        val isLoadingPublicGallery by viewModel.isLoadingPublicGallery.collectAsStateWithLifecycle()
                        val hasMorePublicGallery by viewModel.hasMorePublicGallery.collectAsStateWithLifecycle()

                        GalleryScreen(
                            mySubmissions = designTaskSubmissions,
                            publicSubmissions = publicGalleryItems,
                            modules = modules,
                            isLoadingPublic = isLoadingPublicGallery,
                            hasMorePublic = hasMorePublicGallery,
                            onLoadMorePublic = { viewModel.loadPublicGallery(loadMore = true) },
                            onRefreshPublic = { viewModel.loadPublicGallery(loadMore = false) },
                            onUpdateVisibility = { subMateriId, newVisibility, onSuccess, onError ->
                                viewModel.updateDesignVisibility(subMateriId, newVisibility, onSuccess, onError)
                            },
                            onDeleteSubmission = { subMateriId, onSuccess, onError ->
                                viewModel.deleteDesignTask(subMateriId, onSuccess, onError)
                            },
                            onNavigateToCourse = {
                                navigateTo(AppDestination.TabView(MainTab.COURSE))
                            },
                            onBack = { popBack() },
                            initialTab = target.initialTab
                        )
                    }

                    is AppDestination.SavedModulesView -> {
                        SavedModulesScreen(
                            modules = modules,
                            materis = materis,
                            bookmarkedModuleIds = bookmarkedModuleIds,
                            onOpenModule = { modId ->
                                navigateTo(AppDestination.LevelSubtopics(modId))
                            },
                            onToggleBookmark = { modId ->
                                viewModel.toggleBookmark(modId)
                            },
                            onBack = { popBack() }
                        )
                    }

                    is AppDestination.DiscussionView -> {
                        val qaPosts by viewModel.qaPosts.collectAsStateWithLifecycle()
                        val qaRepliesByPost by viewModel.qaRepliesByPost.collectAsStateWithLifecycle()
                        val currentUid = homeUiState.user?.firebaseUid?.ifBlank { "local_${homeUiState.user?.id}" }.orEmpty()
                        val mentorFlag = isFacilitatorOrAdmin
                        DiscussionScreen(
                            materiId = target.materiId,
                            materis = materis,
                            posts = qaPosts,
                            repliesByPost = qaRepliesByPost,
                            currentUserUid = currentUid,
                            isMentor = mentorFlag,
                            onLoadPosts = { mId -> viewModel.loadQaPosts(mId) },
                            onAddPost = { mId, title, body, onSuccess, onError ->
                                viewModel.addQaPost(mId, title, body, onSuccess, onError)
                            },
                            onLoadReplies = { pId -> viewModel.loadQaReplies(pId) },
                            onAddReply = { pId, mId, body, onSuccess, onError ->
                                viewModel.addQaReply(pId, mId, body, onSuccess, onError)
                            },
                            onToggleReplyHelpful = { reply, authorUid, onError ->
                                viewModel.toggleQaReplyHelpful(reply, authorUid, onError)
                            },
                            onReportContent = { targetType, targetId, ownerUid, reason, onSuccess, onError ->
                                viewModel.reportQaContent(targetType, targetId, ownerUid, reason, onSuccess, onError)
                            },
                            onDeletePost = { pId, onSuccess, onError ->
                                viewModel.deleteQaPost(pId, onSuccess, onError)
                            },
                            onDeleteReply = { reply, onSuccess, onError ->
                                viewModel.deleteQaReply(reply, onSuccess, onError)
                            },
                            onBack = { popBack() }
                        )
                    }
                }
            }
        }
        }
    }
}
