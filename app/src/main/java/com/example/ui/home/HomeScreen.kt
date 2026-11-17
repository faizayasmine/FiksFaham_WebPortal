package com.example.ui.home

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity
import com.example.data.model.NotificationEntity
import com.example.ui.CapstoneState
import com.example.ui.HomeUiState
import com.example.ui.components.CapstoneProjectCard
import com.example.ui.components.FiksButton
import com.example.ui.components.NotificationDialog
import com.example.ui.components.ProgressBarCard
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksDecor
import com.example.ui.theme.FiksLock
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksPrimaryDark
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modules: List<ModuleEntity>,
    progressList: List<ModuleProgressEntity>,
    allAttempts: List<AssessmentAttemptEntity> = emptyList(),
    notifications: List<NotificationEntity>,
    onNavigateToCourse: () -> Unit,
    onOpenLevel: (Int) -> Unit,
    onOpenPretest: () -> Unit,
    onOpenPosttest: () -> Unit,
    onOpenCapstone: () -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onNotificationClick: ((NotificationEntity) -> Unit)? = null,
    onOpenLeaderboard: () -> Unit = {},
    onOpenGallery: () -> Unit = {},
    materis: List<com.example.data.model.MateriEntity> = emptyList(),
    assessments: List<com.example.data.model.AssessmentEntity> = emptyList(),
    onOpenDrawer: () -> Unit = {},
    onOpenMateri: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showNotificationDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showNotificationDialog) {
        NotificationDialog(
            notifications = notifications,
            onDismiss = { showNotificationDialog = false },
            onMarkAllRead = onMarkAllNotificationsRead,
            onNotificationClick = { notif ->
                showNotificationDialog = false
                onNotificationClick?.invoke(notif)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp, bottom = 24.dp)
            .testTag("home_screen")
    ) {
        // 1. Header: Burger Menu, Greeting & Notification Bell
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 4.dp)
                    .testTag("home_burger_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Buka Menu",
                    tint = FiksPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Halo, ${uiState.user?.nama?.split(" ")?.firstOrNull() ?: "Fisha"}!",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = FiksPrimary,
                        modifier = Modifier.testTag("greeting_text")
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FiksPrimary.copy(alpha = 0.14f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FiksPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("user_level_badge")
                    ) {
                        Text(
                            text = "Lv. ${uiState.user?.level ?: 1}",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = FiksPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Pantau progres belajar kamu",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = FiksText
                )
            }

            // Notification Bell with #F8B259 badge (Shortcut Galeri Desain dihapus dari Beranda)
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(
                    onClick = { showNotificationDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("notification_bell_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifikasi",
                        tint = FiksPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                if (uiState.unreadNotificationsCount > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .offset(x = (-2).dp, y = 2.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(FiksAccent)
                            .testTag("notification_badge")
                    ) {
                        Text(
                            text = uiState.unreadNotificationsCount.toString(),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = FiksText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Progress Card dengan Background Image Penuh & Overlay
        ProgressBarCard(
            progressPercent = uiState.progressPercent,
            subtitle = uiState.currentLevelText
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Leaderboard & Koin Card
        com.example.ui.components.LeaderboardHomeCard(
            userPoints = uiState.userPoints,
            userRank = uiState.userRank,
            onClick = onOpenLeaderboard
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Roadmap Progress Belajar Berkelok Vertikal (Materi 1-5 + Capstone)
        com.example.ui.components.LearningRoadmapProgress(
            modules = modules,
            progressList = progressList,
            onOpenLevel = onOpenLevel,
            materis = materis,
            assessments = assessments,
            allAttempts = allAttempts,
            onOpenMateri = onOpenMateri,
            capstoneState = uiState.capstoneState,
            capstoneSubmission = uiState.capstoneSubmission,
            onOpenCapstone = onOpenCapstone
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Capstone Project Card
        CapstoneProjectCard(
            state = uiState.capstoneState,
            onClick = onOpenCapstone
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
