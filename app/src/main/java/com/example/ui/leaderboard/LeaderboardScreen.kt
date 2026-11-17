package com.example.ui.leaderboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.FirestoreLeaderboardDoc
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LeaderboardPeriod
import com.example.data.model.PeriodPointsSummary
import com.example.data.model.UserEntity
import com.example.data.model.UserPointsBreakdown
import com.example.ui.components.FiksButton
import com.example.ui.components.PointBadge
import com.example.ui.components.PointCoinIcon
import com.example.ui.components.bounceClickEffect
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksAccentLight
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksPrimaryLight
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import com.example.util.LeaderboardUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    user: UserEntity?,
    userPoints: UserPointsBreakdown,
    leaderboardEntries: List<LeaderboardEntry>,
    userRank: Int,
    leaderboardDocs: List<com.example.data.model.FirestoreLeaderboardDoc> = emptyList(),
    currentUserSummary: com.example.data.model.PeriodPointsSummary? = null,
    isOffline: Boolean = false,
    onBack: (() -> Unit)? = null,
    dailyCoinEntries: List<LeaderboardEntry> = emptyList(),
    monthlyCoinEntries: List<LeaderboardEntry> = emptyList(),
    userDailyCoins: Int = 0,
    userMonthlyCoins: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(LeaderboardPeriod.DAILY) }
    var selectedUserForDetail by remember { mutableStateOf<LeaderboardEntry?>(null) }
    var showFull50 by remember { mutableStateOf(false) }

    // Gunakan entri koin Harian / Bulanan dari Firestore + coin_grants lokal (maks 50 untuk papan utama)
    val fullPeriodList = remember(
        selectedPeriod,
        dailyCoinEntries,
        monthlyCoinEntries,
        user,
        userDailyCoins,
        userMonthlyCoins
    ) {
        when (selectedPeriod) {
            LeaderboardPeriod.DAILY -> {
                if (dailyCoinEntries.isNotEmpty()) dailyCoinEntries
                else LeaderboardUtils.buildCoinLeaderboardEntries(emptyList(), user, userDailyCoins)
            }
            LeaderboardPeriod.MONTHLY -> {
                if (monthlyCoinEntries.isNotEmpty()) monthlyCoinEntries
                else LeaderboardUtils.buildCoinLeaderboardEntries(emptyList(), user, userMonthlyCoins)
            }
            else -> {
                if (dailyCoinEntries.isNotEmpty()) dailyCoinEntries
                else LeaderboardUtils.buildCoinLeaderboardEntries(emptyList(), user, userDailyCoins)
            }
        }
    }

    val currentPeriodList = remember(fullPeriodList) { fullPeriodList.take(50) }

    val top3 = remember(currentPeriodList) {
        if (currentPeriodList.size >= 3) currentPeriodList.take(3) else emptyList()
    }

    // List peringkat berikutnya mulai dari peringkat 4 ke bawah (maks 50)
    val listFromRank4 = remember(currentPeriodList) {
        if (currentPeriodList.size >= 3) currentPeriodList.drop(3) else emptyList()
    }

    // Tampilkan 10 besar secara default (3 di podium + 7 di list), atau hingga 50 jika showFull50 == true
    val displayedListFromRank4 = remember(listFromRank4, showFull50) {
        if (showFull50) listFromRank4 else listFromRank4.take(7)
    }

    val currentUserEntry = remember(fullPeriodList) {
        fullPeriodList.firstOrNull { it.isCurrentUser }
    }

    val currentPeriodUserRank = currentUserEntry?.rank ?: 1
    val currentPeriodUserPoints = currentUserEntry?.points ?: when (selectedPeriod) {
        LeaderboardPeriod.MONTHLY -> userMonthlyCoins
        else -> userDailyCoins
    }

    // Modal Dialog Detail Jangkauan/Pantauan Peringkat Mahasiswa
    if (selectedUserForDetail != null) {
        val entry = selectedUserForDetail!!
        Dialog(onDismissRequest = { selectedUserForDetail = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_standing_detail_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pantauan Peringkat",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = FiksText
                        )
                        IconButton(
                            onClick = { selectedUserForDetail = null },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = FiksTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LeaderboardAvatarCircle(
                        name = entry.name,
                        avatarUrl = entry.avatarUrl,
                        size = 64.dp,
                        isHighlighted = entry.isCurrentUser,
                        borderColor = if (entry.isCurrentUser) FiksPrimary else FiksAccent,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = entry.name + if (entry.isCurrentUser) " (Anda)" else "",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = FiksText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Standing Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(FiksPrimaryLight.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Peringkat",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = FiksTextMuted
                            )
                            Text(
                                text = "#${entry.rank}",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = FiksPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(FiksCardBorder)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Koin ${if (selectedPeriod == LeaderboardPeriod.MONTHLY) stringResource(R.string.leaderboard_tab_monthly) else stringResource(R.string.leaderboard_tab_daily)}",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = FiksTextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                PointCoinIcon(size = 18.dp)
                                Text(
                                    text = "${entry.points}",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = FiksPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    FiksButton(
                        text = "Tutup",
                        onClick = { selectedUserForDetail = null },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .statusBarsPadding()
            .testTag("leaderboard_screen")
    ) {
        // 1. Top Bar: Hanya judul Leaderboard tanpa deskripsi atau tombol di atas posisi anda
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("leaderboard_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = FiksPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(FiksAccentLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = FiksPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = "Leaderboard",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = FiksPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(thickness = 1.dp, color = FiksCardBorder)

        // 2. Tab Periode: Harian & Bulanan saja (Revisi Bagian 6)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val displayedPeriods = listOf(
                LeaderboardPeriod.DAILY to stringResource(R.string.leaderboard_tab_daily),
                LeaderboardPeriod.MONTHLY to stringResource(R.string.leaderboard_tab_monthly)
            )
            displayedPeriods.forEach { (period, tabTitle) ->
                val isSelected = period == selectedPeriod
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) FiksPrimary else FiksScoreCard,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) FiksPrimary else FiksCardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .bounceClickEffect()
                        .clickable { selectedPeriod = period }
                        .testTag("period_tab_${period.name.lowercase()}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = tabTitle,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else FiksText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Text(
            text = if (selectedPeriod == LeaderboardPeriod.MONTHLY) {
                stringResource(R.string.leaderboard_monthly_period, LeaderboardUtils.getWibMonthlyDocId())
            } else {
                stringResource(R.string.leaderboard_daily_reset_wib, LeaderboardUtils.getCurrentWibDateKey())
            },
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = FiksTextMuted,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
        )

        // 3. Klasemen Utama (Posisi Anda, Podium 1, 2, 3 + List Peringkat 4..50)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("leaderboard_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Posisi Anda saat ini
            item(key = "user_standing_quick_bar") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = FiksPrimaryLight.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, FiksPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClickEffect()
                        .clickable {
                            if (currentUserEntry != null) {
                                selectedUserForDetail = currentUserEntry
                            }
                        }
                        .testTag("user_quick_standing_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(FiksPrimary)
                            ) {
                                Text(
                                    text = "#$currentPeriodUserRank",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = stringResource(R.string.leaderboard_your_position, currentPeriodUserRank),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = FiksPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PointCoinIcon(size = 16.dp)
                            Text(
                                text = "$currentPeriodUserPoints",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = FiksPrimary
                            )
                        }
                    }
                }
            }

            // Banner Mode Offline
            if (isOffline) {
                item(key = "offline_status_banner") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FiksAccent.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, FiksAccent.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Mode Offline",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = FiksPrimary
                                )
                                Text(
                                    text = "Koneksi cloud belum tersambung. Menampilkan perolehan skor lokal Anda.",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = FiksTextMuted
                                )
                            }
                        }
                    }
                }
            }

            // State Kosong: Belum ada skor sama sekali di periode yang dipilih
            val isPeriodEmpty = currentPeriodList.isEmpty() || currentPeriodList.all { it.points == 0 }
            if (isPeriodEmpty) {
                item(key = "empty_state_${selectedPeriod.name}") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FiksScoreCard,
                        border = BorderStroke(1.dp, FiksCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = FiksPrimary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.leaderboard_empty_title),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = FiksText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.leaderboard_empty_body),
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = FiksTextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                // 1, 2, 3: Podium Juara (Hanya jika peserta >= 3)
                if (top3.size >= 3) {
                    item(key = "podium_section_${selectedPeriod.name}") {
                        Spacer(modifier = Modifier.height(2.dp))
                        LeaderboardPodium(
                            top3 = top3,
                            onSelectUser = { entry -> selectedUserForDetail = entry }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // List siswa dari peringkat 4 ke bawah
                    items(
                        items = displayedListFromRank4,
                        key = { "${selectedPeriod.name}_${it.userId}" }
                    ) { entry ->
                        LeaderboardItemCard(
                            entry = entry,
                            onClick = { selectedUserForDetail = entry }
                        )
                    }

                    // Button Toggle 50 Besar (Hanya jika sisa list > 7)
                    if (listFromRank4.size > 7) {
                        item(key = "toggle_50_besar_button_${selectedPeriod.name}") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (showFull50) FiksScoreCard else FiksPrimary,
                                border = BorderStroke(
                                    1.dp,
                                    if (showFull50) FiksCardBorder else FiksPrimary
                                ),
                                shadowElevation = if (showFull50) 1.dp else 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClickEffect()
                                    .clickable { showFull50 = !showFull50 }
                                    .testTag("toggle_50_besar_button")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 13.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = if (showFull50) FiksPrimary else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (showFull50) "Tampilkan 10 Besar Saja" else "Lihat Semua Peringkat",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (showFull50) FiksPrimary else Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Jika peserta < 3 dan memiliki poin, langsung tampilkan list peserta tanpa podium kosong
                    items(
                        items = currentPeriodList,
                        key = { "${selectedPeriod.name}_${it.userId}" }
                    ) { entry ->
                        LeaderboardItemCard(
                            entry = entry,
                            onClick = { selectedUserForDetail = entry }
                        )
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // Baris pengguna tetap terlihat dan disorot jika berada di luar 50 besar
        if (currentUserEntry != null && currentUserEntry.rank > 50) {
            Surface(
                color = FiksScoreCard,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, FiksPrimary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pinned_user_outside_top50")
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text(
                        text = stringResource(R.string.leaderboard_outside_top50),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = FiksPrimary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LeaderboardItemCard(
                        entry = currentUserEntry,
                        onClick = { selectedUserForDetail = currentUserEntry }
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardPodium(
    top3: List<LeaderboardEntry>,
    onSelectUser: (LeaderboardEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    if (top3.size < 3) return

    val rank1 = top3[0]
    val rank2 = top3[1]
    val rank3 = top3[2]

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, FiksCardBorder),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = FiksPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Peringkat Teratas",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = FiksText
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Rank 2 (Silver)
                PodiumColumn(
                    entry = rank2,
                    rank = 2,
                    badgeColor = Color(0xFFC0C0C0),
                    podiumHeight = 84.dp,
                    onClick = { onSelectUser(rank2) },
                    modifier = Modifier.weight(1f)
                )

                // Rank 1 (Gold - Center & Taller)
                PodiumColumn(
                    entry = rank1,
                    rank = 1,
                    badgeColor = Color(0xFFFFD700),
                    podiumHeight = 110.dp,
                    isFirst = true,
                    onClick = { onSelectUser(rank1) },
                    modifier = Modifier.weight(1.1f)
                )

                // Rank 3 (Bronze)
                PodiumColumn(
                    entry = rank3,
                    rank = 3,
                    badgeColor = Color(0xFFCD7F32),
                    podiumHeight = 72.dp,
                    onClick = { onSelectUser(rank3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    entry: LeaderboardEntry,
    rank: Int,
    badgeColor: Color,
    podiumHeight: androidx.compose.ui.unit.Dp,
    isFirst: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .bounceClickEffect()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Crown icon for rank 1
        if (isFirst) {
            Text(
                text = "👑",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        // Avatar
        Box(contentAlignment = Alignment.BottomEnd) {
            LeaderboardAvatarCircle(
                name = entry.name,
                avatarUrl = entry.avatarUrl,
                size = if (isFirst) 54.dp else 46.dp,
                isHighlighted = entry.isCurrentUser || isFirst,
                borderColor = badgeColor,
                fontSize = if (isFirst) 16.sp else 13.sp
            )

            // Rank Number Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(19.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            ) {
                Text(
                    text = rank.toString(),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Name
        Text(
            text = entry.name,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = FiksText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Points with Point Logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            PointCoinIcon(size = 13.dp)
            Text(
                text = "${entry.points}",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = FiksPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Podium Pillar Base
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    if (isFirst) {
                        Brush.verticalGradient(
                            listOf(FiksAccent.copy(alpha = 0.5f), FiksAccentLight)
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(FiksCardBorder, FiksBg)
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (isFirst) 20.sp else 16.sp,
                color = if (isFirst) FiksPrimary else FiksTextMuted
            )
        }
    }
}

@Composable
private fun LeaderboardItemCard(
    entry: LeaderboardEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = entry.isCurrentUser

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isUser) FiksPrimaryLight.copy(alpha = 0.45f) else FiksScoreCard,
        border = BorderStroke(
            width = if (isUser) 1.5.dp else 1.dp,
            color = if (isUser) FiksPrimary else FiksCardBorder
        ),
        shadowElevation = if (isUser) 2.dp else 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .bounceClickEffect()
            .clickable(onClick = onClick)
            .testTag("leaderboard_item_${entry.rank}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(30.dp)
            ) {
                Text(
                    text = "#${entry.rank}",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = FiksTextMuted
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Avatar Foto / Inisial
            LeaderboardAvatarCircle(
                name = entry.name,
                avatarUrl = entry.avatarUrl,
                size = 40.dp,
                isHighlighted = isUser,
                borderColor = if (isUser) FiksPrimary else FiksCardBorder,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Hanya Nama Mahasiswa & Koin (tanpa bintang/badge/nilai)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.name,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = if (isUser) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = FiksText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = FiksPrimary
                        ) {
                            Text(
                                text = "Anda",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Point Logo Badge
            PointBadge(
                points = entry.points,
                isHighlighted = isUser,
                fontSize = 12.sp,
                coinSize = 15.dp
            )
        }
    }
}

@Composable
private fun LeaderboardAvatarCircle(
    name: String,
    avatarUrl: String,
    size: androidx.compose.ui.unit.Dp,
    isHighlighted: Boolean,
    borderColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    val hasPhoto = avatarUrl.startsWith("http", ignoreCase = true) ||
        avatarUrl.startsWith("content://") ||
        avatarUrl.startsWith("file://")
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isHighlighted) FiksPrimary else FiksCardBorder)
            .border(width = 2.dp, color = borderColor, shape = CircleShape)
    ) {
        if (hasPhoto) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val initials = name.split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
                .ifBlank { "F" }
            Text(
                text = initials,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = if (isHighlighted) Color.White else FiksText
            )
        }
    }
}
