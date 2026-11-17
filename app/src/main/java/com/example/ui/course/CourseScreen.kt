package com.example.ui.course

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import com.example.R
import com.example.util.CourseProgressUtils
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.MateriEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity
import com.example.ui.components.bounceClickEffect
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksIconPostest
import com.example.ui.theme.FiksLock
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksPrimaryDark
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily

@Composable
fun CourseScreen(
    materis: List<MateriEntity> = emptyList(),
    selectedMateriId: Int? = null,
    onSelectMateri: (Int?) -> Unit = {},
    modules: List<ModuleEntity>,
    progressList: List<ModuleProgressEntity>,
    assessments: List<AssessmentEntity> = emptyList(),
    allAttempts: List<AssessmentAttemptEntity> = emptyList(),
    isPretestDone: Boolean = false,
    isPosttestDone: Boolean = false,
    onOpenPretest: (materiId: Int) -> Unit = {},
    onOpenPosttest: (materiId: Int) -> Unit = {},
    onOpenCapstone: (materiId: Int) -> Unit = {},
    onOpenGallery: () -> Unit = {},
    onSelectModule: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Internal fallback selection state if not externally managed
    var localSelectedMateriId by remember(selectedMateriId) { mutableStateOf(selectedMateriId) }
    val effectiveSelectedMateriId = localSelectedMateriId

    fun isMateriPosttestPassed(materiId: Int): Boolean {
        return CourseProgressUtils.isPosttestPassed(materiId, assessments, allAttempts) || (materiId == 1 && isPosttestDone)
    }

    fun isMateriPretestDone(materiId: Int): Boolean {
        val pretest = assessments.firstOrNull { it.materiId == materiId && it.tipe == "pretest" }
        val pretestId = pretest?.id ?: (if (materiId == 1) 10 else materiId * 100 + 10)
        return allAttempts.any { it.assessmentId == pretestId } || (materiId == 1 && isPretestDone)
    }

    val effectiveMateris = if (materis.isNotEmpty()) {
        materis.sortedBy { it.urutan }
    } else {
        listOf(
            MateriEntity(
                id = 1,
                judul = "Design Thinking Dasar untuk UI/UX",
                deskripsi = "Pelajari prinsip dasar desain, eksplorasi kebutuhan pengguna, dan perancangan antarmuka visual yang terstandar.",
                urutan = 1,
                courseId = 1
            )
        )
    }

    if (effectiveSelectedMateriId == null) {
        // ==========================================
        // LAYAR PERTAMA: DAFTAR MATERI (Course Level 1)
        // KEPUTUSAN 1: Materi berikutnya TERKUNCI BERURUTAN
        // ==========================================
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(FiksBg)
                .padding(horizontal = 24.dp)
                .testTag("course_materi_list_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Header Kursus (tanpa shortcut Galeri Desain)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.course_screen_title),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = FiksPrimary,
                        modifier = Modifier.testTag("course_screen_title")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.course_screen_subtitle),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = FiksTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Pre-Test Banner Card (Akses Awal Kursus)
            item {
                val interactionSource = remember { MutableInteractionSource() }
                val pretestCardBg = if (isPretestDone) FiksScoreGreen.copy(alpha = 0.12f) else FiksPrimary.copy(alpha = 0.10f)
                val pretestBorderColor = if (isPretestDone) FiksScoreGreen.copy(alpha = 0.5f) else FiksPrimary.copy(alpha = 0.4f)

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = pretestCardBg,
                    border = BorderStroke(1.2.dp, pretestBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .bounceClickEffect(interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onOpenPretest(1) }
                        .testTag("course_pretest_banner_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val m1PretestDone = isMateriPretestDone(1)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (m1PretestDone) FiksScoreGreen.copy(alpha = 0.18f) else FiksPrimary.copy(alpha = 0.18f))
                            ) {
                                Icon(
                                    imageVector = if (m1PretestDone) Icons.Default.Check else Icons.Default.AssignmentTurnedIn,
                                    contentDescription = "Pretest",
                                    tint = if (m1PretestDone) FiksScoreGreen else FiksPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Pre-Test Kemampuan Awal",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = FiksText
                                )
                                Text(
                                    text = if (m1PretestDone) "Status: Selesai dikerjakan" else "Ukur pemahaman awalmu sebelum materi",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = if (m1PretestDone) FiksScoreGreen else FiksTextMuted
                                )
                            }
                        }

                        val m1PretestDone = isMateriPretestDone(1)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (m1PretestDone) FiksScoreGreen else FiksPrimary
                        ) {
                            Text(
                                text = if (m1PretestDone) "SELESAI" else "KERJAKAN",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Daftar Materi Cards (KEPUTUSAN 1: Terkunci Berurutan)
            items(effectiveMateris) { materi ->
                val interactionSource = remember { MutableInteractionSource() }
                val subModules = modules.filter { it.materiId == materi.id }
                val completedSubCount = subModules.count { sub ->
                    progressList.any { it.moduleId == sub.id && (it.isFinished || it.isCompleted) }
                }
                val totalSubCount = if (subModules.isNotEmpty()) subModules.size else 5
                val isAllSubCompleted = completedSubCount >= totalSubCount && totalSubCount > 0
                val isPosttestPassed = isMateriPosttestPassed(materi.id)
                val isMateriCompleted = CourseProgressUtils.isMateriCompleted(
                    materiId = materi.id,
                    modules = modules,
                    progressList = progressList,
                    assessments = assessments,
                    attempts = allAttempts
                ) || (isAllSubCompleted && isPosttestPassed)

                // Semua Materi bebas diakses kapan saja untuk dibaca
                val prevMateri = if (materi.urutan > 1) effectiveMateris.firstOrNull { it.urutan == materi.urutan - 1 } else null
                val isMateriUnlocked = CourseProgressUtils.isMateriUnlocked(
                    materiId = materi.id,
                    materis = effectiveMateris,
                    assessments = assessments,
                    attempts = allAttempts
                )

                val cardBg = if (isMateriUnlocked) FiksScoreCard else FiksBg
                val cardBorderColor = when {
                    !isMateriUnlocked -> FiksCardBorder
                    isMateriCompleted -> FiksScoreGreen.copy(alpha = 0.5f)
                    else -> FiksPrimary.copy(alpha = 0.4f)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    shadowElevation = if (isMateriUnlocked) 2.dp else 0.dp,
                    border = BorderStroke(1.2.dp, cardBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (isMateriUnlocked) {
                                Modifier
                                    .bounceClickEffect(interactionSource)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        localSelectedMateriId = materi.id
                                        onSelectMateri(materi.id)
                                    }
                            } else Modifier
                        )
                        .testTag("materi_card_${materi.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Circular Badge Materi
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                !isMateriUnlocked -> FiksLock
                                                isMateriCompleted -> FiksScoreGreen
                                                else -> FiksPrimary
                                            }
                                        )
                                ) {
                                    when {
                                        !isMateriUnlocked -> {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Materi Terkunci",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        isMateriCompleted -> {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Materi Selesai",
                                                tint = Color.White,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                imageVector = Icons.Default.Layers,
                                                contentDescription = "Materi ${materi.urutan}",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when {
                                            !isMateriUnlocked -> FiksCardBorder.copy(alpha = 0.55f)
                                            isMateriCompleted -> FiksScoreGreen.copy(alpha = 0.14f)
                                            else -> FiksPrimary.copy(alpha = 0.14f)
                                        }
                                    ) {
                                        Text(
                                            text = when {
                                                !isMateriUnlocked -> "TERKUNCI"
                                                isMateriCompleted -> "SELESAI"
                                                else -> "SEDANG BERJALAN"
                                            },
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = when {
                                                !isMateriUnlocked -> FiksTextMuted
                                                isMateriCompleted -> FiksScoreGreen
                                                else -> FiksPrimary
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = materi.judul,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isMateriUnlocked) FiksText else FiksTextMuted
                                    )
                                }
                            }

                            if (isMateriUnlocked) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isMateriCompleted) FiksScoreGreen.copy(alpha = 0.18f) else FiksPrimary.copy(alpha = 0.18f))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Buka Sub Materi",
                                        tint = if (isMateriCompleted) FiksScoreGreen else FiksPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Terkunci",
                                    tint = FiksLock,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = materi.deskripsi,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksTextMuted,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!isMateriUnlocked) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FiksCardBorder.copy(alpha = 0.55f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Lulus Posttest Materi ${prevMateri?.urutan ?: (materi.urutan - 1)} untuk membuka",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = FiksTextMuted,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        } else {
                            // Progress bar Sub Materi
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Progress Sub Materi",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = FiksText
                                    )
                                    Text(
                                        text = "$completedSubCount / $totalSubCount Lulus",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isAllSubCompleted) FiksScoreGreen else FiksPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress = { (completedSubCount.toFloat() / totalSubCount.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (isAllSubCompleted) FiksScoreGreen else FiksPrimary,
                                    trackColor = FiksCardBorder
                                )
                            }
                        }
                    }
                }
            }

            item {
                val isFinalCapstoneUnlocked = CourseProgressUtils.isFinalCapstoneUnlocked(
                    materis = effectiveMateris,
                    modules = modules,
                    progressList = progressList,
                    assessments = assessments,
                    attempts = allAttempts
                )
                val capstoneInteraction = remember { MutableInteractionSource() }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isFinalCapstoneUnlocked) FiksScoreCard else FiksBg,
                    shadowElevation = if (isFinalCapstoneUnlocked) 2.dp else 0.dp,
                    border = BorderStroke(
                        1.4.dp,
                        if (isFinalCapstoneUnlocked) FiksPrimary else FiksCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .bounceClickEffect(capstoneInteraction)
                        .then(
                            if (isFinalCapstoneUnlocked) {
                                Modifier.clickable(
                                    interactionSource = capstoneInteraction,
                                    indication = null
                                ) { onOpenCapstone(CourseProgressUtils.FINAL_CAPSTONE_MATERI_ID) }
                            } else Modifier
                        )
                        .testTag("final_capstone_materi_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFinalCapstoneUnlocked) FiksPrimary.copy(alpha = 0.16f)
                                    else FiksCardBorder
                                )
                        ) {
                            Icon(
                                imageVector = if (isFinalCapstoneUnlocked) Icons.Default.AssignmentTurnedIn else Icons.Default.Lock,
                                contentDescription = "Capstone Akhir",
                                tint = if (isFinalCapstoneUnlocked) FiksPrimary else FiksLock,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Capstone Project Akhir",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isFinalCapstoneUnlocked) FiksText else FiksTextMuted
                            )
                            Text(
                                text = if (isFinalCapstoneUnlocked)
                                    "Semua 5 Posttest Materi lulus! Kumpulkan karya akhir untuk sertifikat."
                                else
                                    "Lulus ke-5 Posttest Materi terlebih dahulu untuk membuka.",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksTextMuted
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    } else {
        // ==========================================
        // LAYAR KEDUA: DAFTAR SUB MATERI (Course Level 2)
        // Menampilkan sub materi di dalam Materi yang dipilih
        // ==========================================
        BackHandler {
            localSelectedMateriId = null
            onSelectMateri(null)
        }

        val currentMateri = effectiveMateris.firstOrNull { it.id == effectiveSelectedMateriId }
        val materiModules = modules
            .filter { it.materiId == effectiveSelectedMateriId }
            .sortedBy { it.subMateriOrder }

        val allMateriModulesCompleted = materiModules.isNotEmpty() && materiModules.all { sub ->
            progressList.any { it.moduleId == sub.id && (it.isFinished || it.isCompleted) }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(FiksBg)
                .padding(horizontal = 24.dp)
                .testTag("course_submateri_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Header Navigasi Kembali ke Layar Daftar Materi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            localSelectedMateriId = null
                            onSelectMateri(null)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("back_to_materi_list_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Daftar Materi",
                            tint = FiksPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Materi ${currentMateri?.urutan ?: 1}",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = FiksPrimaryDark
                        )
                        Text(
                            text = currentMateri?.judul ?: "Daftar Sub Materi",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = FiksText
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = FiksPrimary.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, FiksPrimary.copy(alpha = 0.35f))
                    ) {
                        val completedCount = materiModules.count { sub ->
                            progressList.any { it.moduleId == sub.id && (it.isFinished || it.isCompleted) }
                        }
                        Text(
                            text = "$completedCount/${materiModules.size} Selesai",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = FiksPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            if (materiModules.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FiksScoreCard,
                        border = BorderStroke(1.dp, FiksCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("empty_submateri_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(36.dp)
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
                                text = "Sub materi untuk materi ini sedang disiapkan.",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = FiksTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Daftar Sub Materi Cards
            items(materiModules) { module ->
                val progress = progressList.firstOrNull { it.moduleId == module.id }
                // Semua Sub Materi bebas dibuka untuk dibaca kapan saja
                val isUnlocked = progress?.isUnlocked ?: true
                val isCompleted = progress?.isFinished == true || progress?.isCompleted == true
                val interactionSource = remember { MutableInteractionSource() }

                val cardBg = if (isUnlocked) FiksScoreCard else FiksBg

                val cardBorderColor = when {
                    isCompleted -> FiksScoreGreen.copy(alpha = 0.4f)
                    isUnlocked -> FiksPrimary.copy(alpha = 0.4f)
                    else -> FiksCardBorder
                }

                val badgeBg = when {
                    isCompleted -> FiksScoreGreen
                    isUnlocked -> FiksPrimary
                    else -> FiksLock
                }

                val tagBg = when {
                    isCompleted -> FiksScoreGreen.copy(alpha = 0.14f)
                    isUnlocked -> FiksPrimary.copy(alpha = 0.14f)
                    else -> FiksCardBorder.copy(alpha = 0.55f)
                }

                val tagTextColor = when {
                    isCompleted -> FiksScoreGreen
                    isUnlocked -> FiksPrimary
                    else -> FiksTextMuted
                }

                val pillBg = when {
                    isCompleted -> FiksScoreGreen.copy(alpha = 0.14f)
                    isUnlocked -> FiksPrimary.copy(alpha = 0.14f)
                    else -> FiksCardBorder.copy(alpha = 0.55f)
                }

                val pillTextColor = when {
                    isCompleted -> FiksScoreGreen
                    isUnlocked -> FiksPrimary
                    else -> FiksTextMuted
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = cardBg,
                    shadowElevation = if (isUnlocked) 2.dp else 0.dp,
                    border = BorderStroke(1.2.dp, cardBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .bounceClickEffect(interactionSource)
                        .then(
                            if (isUnlocked) {
                                Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { onSelectModule(module.id) }
                            } else Modifier
                        )
                        .testTag("course_level_card_${module.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Circular Badge Sub Materi
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(badgeBg)
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selesai",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else if (isUnlocked) {
                                        Text(
                                            text = "${module.subMateriOrder}",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = Color.White
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Terkunci",
                                            tint = Color.White.copy(alpha = 0.9f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = tagBg
                                        ) {
                                            Text(
                                                text = "SUB MATERI ${module.subMateriOrder}",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = tagTextColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (isCompleted) {
                                            Text(
                                                text = "Selesai 100%",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = FiksScoreGreen
                                            )
                                        } else if (isUnlocked) {
                                            Text(
                                                text = "Tersedia",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = FiksPrimaryDark
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = module.judul,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isUnlocked) FiksText else FiksTextMuted
                                    )
                                }
                            }

                            if (isUnlocked) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isCompleted) FiksScoreGreen.copy(alpha = 0.18f) else FiksPrimary.copy(alpha = 0.18f))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Buka Sub Materi",
                                        tint = if (isCompleted) FiksScoreGreen else FiksPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Info Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = pillBg
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = pillTextColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "${module.subtopikCount} Sub-topik",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp,
                                        color = pillTextColor
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = pillBg
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentTurnedIn,
                                        contentDescription = null,
                                        tint = pillTextColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Kuis",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp,
                                        color = pillTextColor
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = pillBg
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = pillTextColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "${module.estimasiMenit} Menit",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp,
                                        color = pillTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Posttest Banner untuk Materi ini
            item {
                val isPosttestAccessible = allMateriModulesCompleted
                val currentMateriPosttestDone = isMateriPosttestPassed(effectiveSelectedMateriId)
                val interactionSource = remember { MutableInteractionSource() }

                val posttestCardBg = when {
                    currentMateriPosttestDone -> FiksScoreGreen.copy(alpha = 0.12f)
                    isPosttestAccessible -> FiksScoreCard
                    else -> FiksBg
                }

                val posttestBorderColor = when {
                    currentMateriPosttestDone -> FiksScoreGreen.copy(alpha = 0.55f)
                    isPosttestAccessible -> FiksPrimary.copy(alpha = 0.5f)
                    else -> FiksCardBorder
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = posttestCardBg,
                    shadowElevation = if (isPosttestAccessible) 1.dp else 0.dp,
                    border = BorderStroke(1.2.dp, posttestBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .bounceClickEffect(interactionSource)
                        .then(
                            if (isPosttestAccessible) {
                                Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { onOpenPosttest(effectiveSelectedMateriId) }
                            } else Modifier
                        )
                        .testTag("posttest_banner_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentMateriPosttestDone) FiksScoreGreen.copy(alpha = 0.18f)
                                        else if (isPosttestAccessible) FiksPrimary.copy(alpha = 0.18f)
                                        else FiksCardBorder.copy(alpha = 0.55f)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isPosttestAccessible) Icons.Default.Quiz else Icons.Default.Lock,
                                    contentDescription = "Posttest",
                                    tint = if (currentMateriPosttestDone) FiksScoreGreen
                                    else if (isPosttestAccessible) FiksIconPostest
                                    else FiksLock,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Post-Test: ${currentMateri?.judul ?: "Materi"}",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isPosttestAccessible) FiksText else FiksTextMuted
                                )
                                Text(
                                    text = when {
                                        currentMateriPosttestDone -> "Status: Selesai"
                                        isPosttestAccessible -> "Siap dikerjakan untuk evaluasi materi"
                                        else -> "Selesaikan semua Sub Materi terlebih dahulu"
                                    },
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = if (currentMateriPosttestDone) FiksScoreGreen
                                    else if (isPosttestAccessible) FiksTextMuted
                                    else FiksTextMuted
                                )
                            }
                        }

                        if (isPosttestAccessible) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (currentMateriPosttestDone) FiksScoreGreen else FiksPrimary
                            ) {
                                Text(
                                    text = if (currentMateriPosttestDone) "LIHAT" else "KERJAKAN",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Capstone Project Akhir Dunia Desain (Terbuka setelah 5 Posttest Materi lulus)
            item {
                val isCapstoneAccessible = CourseProgressUtils.isFinalCapstoneUnlocked(
                    materis = effectiveMateris,
                    modules = modules,
                    progressList = progressList,
                    assessments = assessments,
                    attempts = allAttempts
                )
                val interactionSource = remember { MutableInteractionSource() }

                val capstoneCardBg = if (isCapstoneAccessible) FiksScoreCard else FiksBg
                val capstoneBorderColor = if (isCapstoneAccessible) FiksPrimary.copy(alpha = 0.5f) else FiksCardBorder

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = capstoneCardBg,
                    shadowElevation = if (isCapstoneAccessible) 1.dp else 0.dp,
                    border = BorderStroke(1.2.dp, capstoneBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .bounceClickEffect(interactionSource)
                        .then(
                            if (isCapstoneAccessible) {
                                Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { onOpenCapstone(CourseProgressUtils.FINAL_CAPSTONE_MATERI_ID) }
                            } else Modifier
                        )
                        .testTag("capstone_banner_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCapstoneAccessible) FiksPrimary.copy(alpha = 0.16f)
                                        else FiksCardBorder
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isCapstoneAccessible) Icons.Default.AssignmentTurnedIn else Icons.Default.Lock,
                                    contentDescription = "Capstone Project",
                                    tint = if (isCapstoneAccessible) FiksPrimary else FiksLock,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Capstone Project Akhir",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isCapstoneAccessible) FiksText else FiksTextMuted
                                )
                                Text(
                                    text = if (isCapstoneAccessible) "Kumpulkan proyek akhir untuk penilaian Mentor"
                                           else "Lulus semua 5 Posttest Materi terlebih dahulu untuk membuka",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = FiksTextMuted
                                )
                            }
                        }

                        if (isCapstoneAccessible) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FiksPrimary
                            ) {
                                Text(
                                    text = "BUKA",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
