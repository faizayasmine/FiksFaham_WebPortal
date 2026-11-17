package com.example.ui.news

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.R
import com.example.data.model.ChallengeParticipant
import com.example.data.model.DesignChallenge
import com.example.util.CommunityRulesUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MateriEntity
import com.example.data.repository.DesignTaskSubmission
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily

@Composable
fun GaleriCommunitySubTab(
    items: List<DesignTaskSubmission>,
    currentUserUid: String,
    isAdmin: Boolean,
    onRefresh: () -> Unit,
    onToggleLike: (DesignTaskSubmission) -> Unit,
    onReport: (DesignTaskSubmission, String, () -> Unit, (String) -> Unit) -> Unit,
    onAdminDelete: (DesignTaskSubmission, () -> Unit, (String) -> Unit) -> Unit,
    onOpenFullGallery: () -> Unit,
    challenges: List<DesignChallenge> = emptyList(),
    challengeParticipants: List<ChallengeParticipant> = emptyList(),
    userOwnPublicWorks: List<DesignTaskSubmission> = emptyList(),
    onJoinChallenge: (DesignChallenge, DesignTaskSubmission, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onSaveChallenge: (DesignChallenge, () -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    onDeleteChallenge: (String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    canManageChallenges: Boolean = isAdmin,
    modifier: Modifier = Modifier
) {
    var reportTarget by remember { mutableStateOf<DesignTaskSubmission?>(null) }
    var reportReason by remember { mutableStateOf("") }
    var feedbackNotice by remember { mutableStateOf<String?>(null) }
    var feedbackIsError by remember { mutableStateOf(false) }
    var showJoinChallengeDialog by remember { mutableStateOf<DesignChallenge?>(null) }
    var editingChallenge by remember { mutableStateOf<DesignChallenge?>(null) }
    var showChallengeFormDialog by remember { mutableStateOf(false) }

    val ownLikeForbiddenText = stringResource(R.string.gallery_like_own_forbidden)
    val activeChallenge = challenges.firstOrNull { it.status.equals("active", ignoreCase = true) }
        ?: challenges.firstOrNull()

    LaunchedEffect(Unit) {
        onRefresh()
    }

    // Dialog pilih karya publik untuk ikut Tantangan Desain
    if (showJoinChallengeDialog != null) {
        val targetChallenge = showJoinChallengeDialog!!
        AlertDialog(
            onDismissRequest = { showJoinChallengeDialog = null },
            containerColor = FiksScoreCard,
            title = {
                Text(
                    text = stringResource(R.string.challenge_pick_work_title),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = FiksText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (userOwnPublicWorks.isEmpty()) {
                        Text(
                            text = stringResource(R.string.challenge_no_public_work),
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksTextMuted
                        )
                    } else {
                        userOwnPublicWorks.forEach { work ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FiksBg,
                                border = BorderStroke(1.dp, FiksCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onJoinChallenge(
                                            targetChallenge,
                                            work,
                                            {
                                                feedbackIsError = false
                                                feedbackNotice = "Berhasil mengikuti tantangan! (+15 koin)"
                                            },
                                            { err ->
                                                feedbackIsError = true
                                                feedbackNotice = err
                                            }
                                        )
                                        showJoinChallengeDialog = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    AsyncImage(
                                        model = work.url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Materi ${work.materiId} • Sub Materi ${work.subMateriId}",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = FiksText
                                        )
                                        Text(
                                            text = "${work.likeCount} Suka",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            color = FiksPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (userOwnPublicWorks.isEmpty()) {
                    Button(
                        onClick = {
                            showJoinChallengeDialog = null
                            onOpenFullGallery()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary)
                    ) {
                        Text("Karyaku", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinChallengeDialog = null }) {
                    Text(stringResource(R.string.common_close), fontFamily = PoppinsFontFamily, color = FiksTextMuted)
                }
            }
        )
    }

    // Dialog CRUD Tantangan Desain untuk Mentor
    if (showChallengeFormDialog) {
        val base = editingChallenge
        var cTitle by remember(base) { mutableStateOf(base?.title ?: "") }
        var cDesc by remember(base) { mutableStateOf(base?.description ?: "") }
        var cTheme by remember(base) { mutableStateOf(base?.theme ?: "") }
        var cStatus by remember(base) { mutableStateOf(base?.status ?: "active") }

        AlertDialog(
            onDismissRequest = { showChallengeFormDialog = false },
            containerColor = FiksScoreCard,
            title = {
                Text(
                    text = if (base == null) stringResource(R.string.challenge_create_btn) else stringResource(R.string.challenge_manage_btn),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = FiksText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cTitle,
                        onValueChange = { cTitle = it },
                        label = { Text("Judul Tantangan", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cTheme,
                        onValueChange = { cTheme = it },
                        label = { Text("Tema", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cDesc,
                        onValueChange = { cDesc = it },
                        label = { Text("Deskripsi Singkat", fontSize = 12.sp) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("active" to "Aktif", "ended" to "Selesai").forEach { (st, lbl) ->
                            val selected = cStatus == st
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) FiksPrimary else FiksBg,
                                border = BorderStroke(1.dp, if (selected) FiksPrimary else FiksCardBorder),
                                modifier = Modifier.clickable { cStatus = st }
                            ) {
                                Text(
                                    text = lbl,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (selected) Color.White else FiksText,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        val sevenDays = 7L * 24 * 60 * 60 * 1000
                        val toSave = DesignChallenge(
                            id = base?.id ?: "",
                            title = cTitle,
                            description = cDesc,
                            theme = cTheme,
                            startDateMillis = base?.startDateMillis?.takeIf { it > 0L } ?: now,
                            endDateMillis = base?.endDateMillis?.takeIf { it > 0L } ?: (now + sevenDays),
                            status = cStatus,
                            createdByUid = base?.createdByUid ?: currentUserUid,
                            createdAt = base?.createdAt ?: now
                        )
                        onSaveChallenge(
                            toSave,
                            {
                                feedbackIsError = false
                                feedbackNotice = "Tantangan desain tersimpan."
                            },
                            { err ->
                                feedbackIsError = true
                                feedbackNotice = err
                            }
                        )
                        showChallengeFormDialog = false
                    },
                    enabled = cTitle.isNotBlank() && cTheme.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary)
                ) {
                    Text("Simpan", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (base != null) {
                        TextButton(onClick = {
                            onDeleteChallenge(
                                base.id,
                                {
                                    feedbackIsError = false
                                    feedbackNotice = "Tantangan dihapus."
                                },
                                { err ->
                                    feedbackIsError = true
                                    feedbackNotice = err
                                }
                            )
                            showChallengeFormDialog = false
                        }) {
                            Text("Hapus", fontFamily = PoppinsFontFamily, color = FiksScoreRed)
                        }
                    }
                    TextButton(onClick = { showChallengeFormDialog = false }) {
                        Text(stringResource(R.string.common_close), fontFamily = PoppinsFontFamily, color = FiksTextMuted)
                    }
                }
            }
        )
    }

    if (reportTarget != null) {
        val target = reportTarget!!
        AlertDialog(
            onDismissRequest = { reportTarget = null },
            containerColor = FiksScoreCard,
            title = {
                Text("Laporkan Karya", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = FiksText)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sertakan alasan laporan untuk karya milik ${target.userName.ifBlank { "Peserta" }}.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksTextMuted
                    )
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("Contoh: Konten tidak relevan / duplikat", fontSize = 12.sp) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = reportReason.trim().ifBlank { "Konten tidak sesuai" }
                        onReport(
                            target,
                            reason,
                            { feedbackNotice = "Laporan berhasil dikirim untuk ditinjau." },
                            { err -> feedbackNotice = err }
                        )
                        reportReason = ""
                        reportTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FiksScoreRed)
                ) {
                    Text("Kirim Laporan", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reportTarget = null }) {
                    Text("Batal", fontFamily = PoppinsFontFamily, color = FiksTextMuted)
                }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize().testTag("galeri_community_subtab")
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Etalase Karya Peserta",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FiksText
                        )
                        Text(
                            text = "Apresiasi karya desain sesama peserta atau kelola portofoliomu.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = FiksTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedButton(onClick = onOpenFullGallery, shape = RoundedCornerShape(10.dp)) {
                        Text("Karyaku", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FiksPrimary)
                    }
                }
            }
        }

        if (!feedbackNotice.isNullOrBlank()) {
            item {
                Text(
                    text = feedbackNotice!!,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = if (feedbackIsError) FiksScoreRed else FiksScoreGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Kartu Tantangan Desain + Juara 1-3 Otomatis berdasarkan Poin Like
        if (activeChallenge != null || canManageChallenges) {
            item {
                DesignChallengeCard(
                    challenge = activeChallenge,
                    participants = challengeParticipants.filter { activeChallenge == null || it.challengeId == activeChallenge.id },
                    galleryItems = items,
                    currentUserUid = currentUserUid,
                    userOwnPublicWorks = userOwnPublicWorks,
                    isAdmin = canManageChallenges,
                    onJoinClick = { ch -> showJoinChallengeDialog = ch },
                    onManageClick = { ch ->
                        editingChallenge = ch
                        showChallengeFormDialog = true
                    },
                    onCreateClick = {
                        editingChallenge = null
                        showChallengeFormDialog = true
                    }
                )
            }
        }

        if (items.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.dp, FiksCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada karya publik", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = FiksText)
                        Text(
                            text = "Bagikan tugas desainmu ke galeri publik agar tampil di sini!",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksTextMuted
                        )
                    }
                }
            }
        } else {
            items(items, key = { "${it.uid}_${it.subMateriId}" }) { item ->
                val isLiked = currentUserUid.isNotBlank() && currentUserUid in item.likedBy
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.dp, FiksCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (item.url.isNotBlank()) {
                            AsyncImage(
                                model = item.url,
                                contentDescription = item.userName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.userName.ifBlank { "Desainer FiksFaham" },
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = FiksText
                                )
                                Text(
                                    text = "Materi ${item.materiId} • Tugas Sub Materi ${item.subMateriId}",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = FiksTextMuted
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val canLike = CommunityRulesUtils.canLikeGallerySubmission(item.uid, currentUserUid)
                                IconButton(
                                    onClick = {
                                        if (!canLike && item.uid == currentUserUid && currentUserUid.isNotBlank()) {
                                            feedbackIsError = true
                                            feedbackNotice = ownLikeForbiddenText
                                        } else {
                                            feedbackIsError = false
                                            onToggleLike(item)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Suka",
                                        tint = when {
                                            !canLike && item.uid == currentUserUid && currentUserUid.isNotBlank() -> FiksTextMuted.copy(alpha = 0.45f)
                                            isLiked -> FiksScoreRed
                                            else -> FiksTextMuted
                                        }
                                    )
                                }
                                Text(
                                    text = "${item.likeCount}",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = FiksText
                                )
                                IconButton(onClick = { reportTarget = item }) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = "Laporkan",
                                        tint = FiksTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (isAdmin) {
                                    IconButton(onClick = {
                                        onAdminDelete(
                                            item,
                                            { feedbackNotice = "Karya dihapus oleh moderator." },
                                            { err -> feedbackNotice = err }
                                        )
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Moderator",
                                            tint = FiksScoreRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesignChallengeCard(
    challenge: DesignChallenge?,
    participants: List<ChallengeParticipant>,
    galleryItems: List<DesignTaskSubmission>,
    currentUserUid: String,
    userOwnPublicWorks: List<DesignTaskSubmission>,
    isAdmin: Boolean,
    onJoinClick: (DesignChallenge) -> Unit,
    onManageClick: (DesignChallenge) -> Unit,
    onCreateClick: () -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM", Locale("id", "ID")) }
    val winners = remember(participants, galleryItems) {
        CommunityRulesUtils.rankChallengeWinners(participants, galleryItems)
    }
    val hasJoined = currentUserUid.isNotBlank() && participants.any { it.participantUid == currentUserUid }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.5.dp, FiksPrimary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth().testTag("design_challenge_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(20.dp))
                    Text(
                        text = stringResource(R.string.challenge_card_header),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FiksPrimary
                    )
                }
                if (challenge != null) {
                    val isActive = challenge.status.equals("active", ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isActive) FiksScoreGreen.copy(alpha = 0.14f) else FiksTextMuted.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = if (isActive) stringResource(R.string.challenge_status_active) else stringResource(R.string.challenge_status_ended),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (isActive) FiksScoreGreen else FiksTextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (challenge == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Belum ada Tantangan Desain aktif saat ini.",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = FiksTextMuted
                )
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onCreateClick,
                        colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.challenge_create_btn), fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = challenge.title,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = FiksText
                )
                if (challenge.theme.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.challenge_theme_label, challenge.theme),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = FiksPrimary
                    )
                }
                if (challenge.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = challenge.description,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksTextMuted,
                        lineHeight = 17.sp
                    )
                }
                if (challenge.startDateMillis > 0L && challenge.endDateMillis > 0L) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.challenge_period_label,
                            dateFmt.format(Date(challenge.startDateMillis)),
                            dateFmt.format(Date(challenge.endDateMillis))
                        ),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksTextMuted
                    )
                }

                // Juara 1-3 otomatis dari perolehan like/poin peserta
                if (winners.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.challenge_winners_title),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    winners.forEachIndexed { idx, w ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${idx + 1} ${w.participantName}",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = FiksText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${w.likeCount} Suka",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = FiksPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasJoined) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FiksScoreGreen.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = stringResource(R.string.challenge_joined_badge),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = FiksScoreGreen,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    } else if (challenge.status.equals("active", ignoreCase = true)) {
                        Button(
                            onClick = { onJoinClick(challenge) },
                            colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.challenge_join_btn),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (isAdmin) {
                        OutlinedButton(
                            onClick = { onManageClick(challenge) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = FiksPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.challenge_manage_btn),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = FiksPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

