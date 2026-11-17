package com.example.ui.course

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.DesignTaskEntity
import com.example.data.model.DesignTaskSubmissionEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity
import com.example.data.model.ModuleSectionEntity
import com.example.data.model.ModuleVideoEntity
import android.net.Uri
import com.example.ui.components.FiksButton
import com.example.ui.components.bounceClickEffect
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksPrimaryDark
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily

/**
 * Layar Daftar Sub-Topik Level (Screen 2 dalam alur navigasi Course).
 * Memiliki Single Unified Header di atas, diikuti Smooth Animated Tab Selector (Materi & Quiz).
 */
@Composable
fun LevelSubtopicsScreen(
    module: ModuleEntity,
    sections: List<ModuleSectionEntity>,
    video: ModuleVideoEntity?,
    progress: ModuleProgressEntity?,
    viewedSectionIds: Set<Int>,
    latestAttempt: AssessmentAttemptEntity?,
    cooldownSeconds: Long,
    onSelectSubtopic: (Int) -> Unit,
    onOpenQuiz: () -> Unit,
    onBackToCourse: () -> Unit,
    designTask: DesignTaskEntity? = null,
    designTaskSubmission: DesignTaskSubmissionEntity? = null,
    onUploadDesignTask: (Uri, onProgress: (Float) -> Unit, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _, _ -> },
    isBookmarked: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    onOpenDiscussion: () -> Unit = {},
    isFacilitatorOrAdmin: Boolean = false,
    onSaveDesignTaskDefinition: ((judulTugas: String, instruksi: String, batasUkuranMb: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Materi, 1: Quiz
    val isMateriCompleted = progress?.materiViewed == true || progress?.isFinished == true
    val isSubMateriFinished = progress?.isFinished == true || progress?.isCompleted == true
    val viewedCount = sections.count { it.id in viewedSectionIds || isMateriCompleted }
    val progressFraction = if (sections.isNotEmpty()) {
        (viewedCount.toFloat() / sections.size).coerceIn(0f, 1f)
    } else 0f

    val isQuizPassed = progress?.quizPassed == true || (latestAttempt?.isPassed == true)
    val isQuizUnlocked = isSubMateriFinished || isQuizPassed || latestAttempt != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .testTag("level_subtopics_screen")
    ) {
        // Top App Bar
        Surface(
            color = FiksScoreCard,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToCourse,
                    modifier = Modifier.testTag("subtopics_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Daftar Sub Materi",
                        tint = FiksPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sub Materi ${module.subMateriOrder}",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = FiksPrimary
                    )
                    Text(
                        text = module.judul,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FiksText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onOpenDiscussion,
                    modifier = Modifier.testTag("subtopics_discussion_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = "Ruang Diskusi Materi",
                        tint = FiksPrimary
                    )
                }
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("subtopics_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Hapus dari tersimpan" else "Simpan sub materi",
                        tint = if (isBookmarked) FiksPrimary else FiksTextMuted
                    )
                }
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ==============================================================
            // 1. UNIFIED LEVEL HEADER CARD (Diletakkan di atas Tab Materi & Quiz)
            // ==============================================================
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("level_header_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FiksPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "SUB MATERI ${module.subMateriOrder}",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = FiksPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (isQuizPassed) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FiksScoreGreen.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = FiksScoreGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Sub Materi Selesai",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = FiksScoreGreen
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = module.judul,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = FiksText,
                        lineHeight = 23.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = module.deskripsiSingkat,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksTextMuted,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progres Sub-topik Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progres Membaca",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = FiksText
                        )
                        Text(
                            text = "$viewedCount / ${sections.size} Selesai",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (viewedCount == sections.size && sections.isNotEmpty()) FiksScoreGreen else FiksPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (viewedCount == sections.size && sections.isNotEmpty()) FiksScoreGreen else FiksPrimary,
                        trackColor = Color(0xFFE5DDD5),
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==============================================================
            // 2. SMOOTH MODERN TAB SELECTOR (Materi vs Quiz)
            // ==============================================================
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF1EAE1),
                border = BorderStroke(1.dp, Color(0xFFE6DCD1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("level_subtopics_tab_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabMateriInteraction = remember { MutableInteractionSource() }
                    val tabQuizInteraction = remember { MutableInteractionSource() }

                    val tabMateriBg by animateColorAsState(
                        targetValue = if (selectedTab == 0) FiksPrimary else Color.Transparent,
                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                        label = "tab_materi_bg"
                    )
                    val tabMateriTextColor by animateColorAsState(
                        targetValue = if (selectedTab == 0) Color.White else FiksTextMuted,
                        animationSpec = tween(durationMillis = 200),
                        label = "tab_materi_text"
                    )
                    val tabMateriElevation by animateDpAsState(
                        targetValue = if (selectedTab == 0) 3.dp else 0.dp,
                        animationSpec = tween(durationMillis = 200),
                        label = "tab_materi_elev"
                    )

                    val tabQuizBg by animateColorAsState(
                        targetValue = if (selectedTab == 1) FiksPrimary else Color.Transparent,
                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                        label = "tab_quiz_bg"
                    )
                    val tabQuizTextColor by animateColorAsState(
                        targetValue = if (selectedTab == 1) Color.White else FiksTextMuted,
                        animationSpec = tween(durationMillis = 200),
                        label = "tab_quiz_text"
                    )
                    val tabQuizElevation by animateDpAsState(
                        targetValue = if (selectedTab == 1) 3.dp else 0.dp,
                        animationSpec = tween(durationMillis = 200),
                        label = "tab_quiz_elev"
                    )

                    // Tab 1: Materi
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = tabMateriBg,
                        shadowElevation = tabMateriElevation,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .bounceClickEffect(tabMateriInteraction)
                            .clickable(
                                interactionSource = tabMateriInteraction,
                                indication = null
                            ) { selectedTab = 0 }
                            .testTag("tab_subtopics_materi")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 11.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = tabMateriTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Materi ($viewedCount/${sections.size})",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = tabMateriTextColor
                            )
                        }
                    }

                    // Tab 2: Quiz
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = tabQuizBg,
                        shadowElevation = tabQuizElevation,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .bounceClickEffect(tabQuizInteraction)
                            .clickable(
                                interactionSource = tabQuizInteraction,
                                indication = null
                            ) { selectedTab = 1 }
                            .testTag("tab_subtopics_quiz")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 11.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = tabQuizTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isQuizPassed) "Kuis" else "Kuis Level",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = tabQuizTextColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==============================================================
            // 3. ANIMATED TAB CONTENT (Materi Sub-topik vs Quiz Evaluasi)
            // ==============================================================
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, delayMillis = 50)) togetherWith
                        fadeOut(animationSpec = tween(150))
                },
                label = "tab_content_transition"
            ) { targetTab ->
                if (targetTab == 0) {
                    // ==============================================================
                    // TAB 1 CONTENT: DAFTAR SUB-TOPIK MATERI
                    // ==============================================================
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Daftar Sub-topik (${sections.size} Materi):",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = FiksText
                            )
                            Text(
                                text = "Ketuk untuk membaca",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sub-topik Card List
                        if (sections.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FiksScoreCard,
                                border = BorderStroke(1.2.dp, FiksCardBorder),
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = FiksPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Konten segera hadir",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = FiksText
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Sub-topik materi ini sedang disusun dan akan segera hadir.",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 12.sp,
                                        color = FiksTextMuted,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                sections.sortedBy { it.urutan }.forEach { section ->
                                    val isViewed = section.id in viewedSectionIds || isMateriCompleted
                                val cardInteraction = remember { MutableInteractionSource() }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = FiksScoreCard,
                                    border = BorderStroke(
                                        1.2.dp,
                                        if (isViewed) FiksScoreGreen.copy(alpha = 0.5f) else FiksCardBorder
                                    ),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .bounceClickEffect(cardInteraction)
                                        .clickable(
                                            interactionSource = cardInteraction,
                                            indication = null
                                        ) { onSelectSubtopic(section.id) }
                                        .testTag("subtopic_card_${section.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Subtopic Number Badge
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isViewed) FiksScoreGreen else FiksPrimary,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (isViewed) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Sudah Dibuka",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = "${section.urutan}",
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        // Subtopic Info
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "Sub-topik ${section.urutan}",
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 11.sp,
                                                    color = FiksTextMuted
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (isViewed) FiksScoreGreen.copy(alpha = 0.12f) else FiksPrimary.copy(alpha = 0.1f)
                                                ) {
                                                    Text(
                                                        text = if (isViewed) "Sudah Dibuka" else "Belum Dibuka",
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 9.sp,
                                                        color = if (isViewed) FiksScoreGreen else FiksPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(3.dp))

                                            Text(
                                                text = section.judulSubtopik,
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = FiksText,
                                                lineHeight = 19.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = section.keteranganGambar,
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                color = FiksTextMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Buka Sub-topik",
                                            tint = FiksPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // TUGAS DESAIN (Tahap 9): Menampilkan tugas atau slot fasilitator/super admin
                        Spacer(modifier = Modifier.height(18.dp))
                        DesignTaskSection(
                            task = designTask,
                            submission = designTaskSubmission,
                            onUploadImage = onUploadDesignTask,
                            isFacilitatorOrAdmin = isFacilitatorOrAdmin,
                            onSaveDesignTaskDefinition = onSaveDesignTaskDefinition
                        )
                    }
                }
            } else {
                    // ==============================================================
                    // TAB 2 CONTENT: QUIZ EVALUASI LEVEL (Tampil Langsung)
                    // ==============================================================
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FiksScoreCard,
                        border = BorderStroke(1.5.dp, if (isQuizPassed) FiksScoreGreen else FiksPrimary),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("level_quiz_card")
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isQuizPassed) FiksScoreGreen else FiksPrimary,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Quiz,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Evaluasi Pemahaman",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = if (isQuizPassed) FiksScoreGreen else FiksPrimary
                                    )
                                    Text(
                                        text = "Kuis Sub Materi ${module.subMateriOrder}",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = FiksText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Uji pemahaman materi Sub Materi ${module.subMateriOrder}. Batas nilai kelulusan adalah 75% untuk dapat menyelesaikan sub materi ini.",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = FiksTextMuted,
                                lineHeight = 18.sp
                            )

                            // Status Attempt Sebelumnya
                            if (latestAttempt != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (latestAttempt.isPassed) FiksScoreGreen.copy(alpha = 0.12f) else FiksScoreRed.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, if (latestAttempt.isPassed) FiksScoreGreen else FiksScoreRed),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = if (latestAttempt.isPassed) "Status: Lulus" else "Status: Perlu Remedial",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (latestAttempt.isPassed) FiksScoreGreen else FiksScoreRed
                                            )
                                            Text(
                                                text = "Nilai Terakhir: ${latestAttempt.score} (Batas Kelulusan: 75)",
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                color = FiksText
                                            )
                                        }
                                    }
                                }
                            }

                            if (!isQuizUnlocked) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FiksPrimary.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, FiksPrimary.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = FiksPrimary, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Selesaikan sub materi (baca materi & klik Selesai) sebelum mengerjakan kuis.",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = FiksText
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val quizButtonText = when {
                                !isQuizUnlocked -> "KUIS TERKUNCI"
                                isQuizPassed -> "TINJAU / ULANGI KUIS"
                                latestAttempt != null -> "ULANGI KUIS"
                                else -> "MULAI KUIS LEVEL"
                            }

                            FiksButton(
                                text = quizButtonText,
                                onClick = onOpenQuiz,
                                enabled = isQuizUnlocked,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "open_level_quiz_button"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
