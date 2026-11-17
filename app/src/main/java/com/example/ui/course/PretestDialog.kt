package com.example.ui.course

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ModuleEntity
import com.example.data.model.QuestionEntity
import com.example.ui.components.FiksButton
import com.example.ui.components.FiksCompactButton
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksIconPretest
import com.example.ui.theme.FiksLock
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.FiksTrack
import com.example.ui.theme.PoppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PretestDialog(
    questions: List<QuestionEntity>,
    onCompletePretest: (score: Int) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToCourse: () -> Unit = onDismiss,
    modules: List<ModuleEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var isSubmitted by remember { mutableStateOf(false) }
    var calculatedScore by remember { mutableIntStateOf(0) }

    val currentQuestion = questions.getOrNull(currentIndex)
    val totalQuestions = questions.size

    BasicAlertDialog(
        onDismissRequest = {
            if (isSubmitted) onNavigateToCourse() else onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("pretest_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FiksBg,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            if (questions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Soal Belum Tersedia",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Soal Pretest untuk materi ini sedang dipersiapkan.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = FiksTextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    FiksButton(
                        text = "KEMBALI",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (!isSubmitted) {
                // Testing phase
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FiksIconPretest.copy(alpha = 0.25f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = FiksIconPretest,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Pretest Tebak Desain",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = FiksPrimary
                                )
                                Text(
                                    text = "Soal ${currentIndex + 1} dari $totalQuestions",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.sp,
                                    color = FiksTextMuted
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = FiksTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(FiksTrack)
                    ) {
                        val progress = if (totalQuestions > 0) (currentIndex + 1) / totalQuestions.toFloat() else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(FiksPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Question Card
                    currentQuestion?.let { q ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FiksScoreCard,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = FiksAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Tebak Gambar UI/UX",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = FiksPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = q.teksSoal,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = FiksText,
                                    lineHeight = 22.sp
                                )

                                // Visual Representation Box (Canvas/Illustration)
                                Spacer(modifier = Modifier.height(12.dp))
                                VisualPreviewBox(visualPrompt = q.visualPrompt)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Options
                        val options = listOf(q.opsiA, q.opsiB, q.opsiC, q.opsiD)
                        val selectedOpt = selectedAnswers[currentIndex]

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            options.forEachIndexed { optIndex, text ->
                                val isSelected = selectedOpt == optIndex
                                val borderCol = if (isSelected) FiksPrimary else Color.Transparent
                                val bgCol = if (isSelected) FiksPrimary.copy(alpha = 0.12f) else FiksScoreCard

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = bgCol,
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, borderCol),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedAnswers[currentIndex] = optIndex
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) FiksPrimary else FiksBg)
                                        ) {
                                            Text(
                                                text = ('A' + optIndex).toString(),
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isSelected) Color.White else FiksText
                                            )
                                        }

                                        Text(
                                            text = text,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 13.sp,
                                            color = FiksText,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f, fill = false))
                    Spacer(modifier = Modifier.height(24.dp))

                    // Bottom Navigation Buttons (Proporsional & Compact)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentIndex > 0) {
                            FiksCompactButton(
                                text = "KEMBALI",
                                onClick = { currentIndex-- },
                                isSecondary = true,
                                leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
                                testTag = "pretest_back_button"
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        val isAnswered = selectedAnswers.containsKey(currentIndex)
                        val isLast = currentIndex == totalQuestions - 1

                        FiksCompactButton(
                            text = if (isLast) "SELESAI" else "LANJUT",
                            onClick = {
                                if (isLast) {
                                    // Calculate score
                                    var correctCount = 0
                                    questions.forEachIndexed { index, question ->
                                        if (selectedAnswers[index] == question.jawabanBenar) {
                                            correctCount++
                                        }
                                    }
                                    val score = if (totalQuestions > 0) {
                                        ((correctCount.toFloat() / totalQuestions) * 100).toInt()
                                    } else 80
                                    calculatedScore = score
                                    isSubmitted = true
                                    onCompletePretest(score)
                                } else {
                                    currentIndex++
                                }
                            },
                            enabled = isAnswered,
                            trailingIcon = if (isLast) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            testTag = "pretest_next_button"
                        )
                    }
                }
            } else {
                // Pretest Finished Screen - Nilai (Persentase), Review Pemahaman Materi, & Jawaban yang Salah
                val moduleNames = if (modules.isNotEmpty()) {
                    val questionModuleIds = questions.map { it.moduleId }.toSet()
                    val matching = modules.filter { it.id in questionModuleIds }.sortedBy { it.subMateriOrder }
                    val target = if (matching.isNotEmpty()) matching else modules.sortedBy { it.subMateriOrder }
                    target.map { it.id to it.judul }
                } else {
                    questions.map { it.moduleId }.distinct().sorted().map { id ->
                        id to when (id) {
                            1 -> "Pengenalan UI/UX"
                            2 -> "User & User Needs"
                            3 -> "User Flow"
                            4 -> "Wireframe"
                            5 -> "Prinsip Desain Visual"
                            else -> "Sub Materi $id"
                        }
                    }
                }

                // Hitung persentase pemahaman materi per modul berdasarkan jawaban pretest
                val modulePercentageList = moduleNames.map { (modId, name) ->
                    val modQuestions = questions.filter { it.moduleId == modId }
                    val correctCount = questions.filterIndexed { idx, q ->
                        q.moduleId == modId && selectedAnswers[idx] == q.jawabanBenar
                    }.size
                    val percent = if (modQuestions.isNotEmpty()) {
                        ((correctCount.toFloat() / modQuestions.size) * 100).toInt()
                    } else 0
                    Triple(name, percent, correctCount to modQuestions.size)
                }

                // Daftar soal yang dijawab salah
                val wrongQuestions = questions.mapIndexed { idx, q ->
                    val userAns = selectedAnswers[idx]
                    Triple(idx + 1, q, userAns)
                }.filter { (_, q, userAns) -> userAns != q.jawabanBenar }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(FiksIconPretest.copy(alpha = 0.22f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = FiksIconPretest,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Pretest Selesai!",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = FiksPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Nilai hasil pretest kamu:",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksTextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Nilai pada umumnya (skor angka standar tanpa tanda persen)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = FiksScoreCard,
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, FiksCardBorder),
                        shadowElevation = 3.dp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "$calculatedScore",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 42.sp,
                            color = FiksPrimary,
                            modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Tinjauan Pemahaman Materi (Tanpa angka 1/4, langsung persentase di tiap materi)
                    Text(
                        text = "Tinjauan Pemahaman Materi",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FiksText,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        modulePercentageList.forEach { (name, percent, countPair) ->
                            val (correctCount, totalCount) = countPair
                            val isHigh = percent >= 75
                            val isMedium = percent in 1..74

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FiksScoreCard,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    when {
                                        isHigh -> FiksScoreGreen.copy(alpha = 0.5f)
                                        isMedium -> FiksPrimary.copy(alpha = 0.35f)
                                        else -> FiksScoreRed.copy(alpha = 0.35f)
                                    }
                                ),
                                shadowElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isHigh -> FiksScoreGreen.copy(alpha = 0.15f)
                                                    isMedium -> FiksPrimary.copy(alpha = 0.12f)
                                                    else -> FiksScoreRed.copy(alpha = 0.12f)
                                                }
                                            )
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                isHigh -> Icons.Default.CheckCircle
                                                isMedium -> Icons.Default.MenuBook
                                                else -> Icons.Default.Cancel
                                            },
                                            contentDescription = null,
                                            tint = when {
                                                isHigh -> FiksScoreGreen
                                                isMedium -> FiksPrimary
                                                else -> FiksScoreRed
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = FiksText
                                        )
                                        Text(
                                            text = "$correctCount dari $totalCount soal benar",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = FiksTextMuted
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when {
                                            isHigh -> FiksScoreGreen
                                            isMedium -> FiksPrimary
                                            else -> FiksScoreRed.copy(alpha = 0.12f)
                                        }
                                    ) {
                                        Text(
                                            text = "$percent%",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = when {
                                                isHigh -> Color.White
                                                isMedium -> Color.White
                                                else -> FiksScoreRed
                                            },
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // 2. Tinjauan Jawaban yang Salah pada Pretest
                    Text(
                        text = "Tinjauan Jawaban Salah",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FiksText,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (wrongQuestions.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            wrongQuestions.forEach { (questionNum, question, userAns) ->
                                val topicName = moduleNames.firstOrNull { it.first == question.moduleId }?.second ?: "Sub Materi"

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = FiksScoreCard,
                                    border = androidx.compose.foundation.BorderStroke(1.2.dp, FiksScoreRed.copy(alpha = 0.35f)),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = FiksScoreRed.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "Soal #$questionNum",
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = FiksScoreRed,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Text(
                                                text = topicName,
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = FiksTextMuted
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = question.teksSoal,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = FiksText,
                                            lineHeight = 18.sp
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Jawaban Salah Pengguna
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = FiksScoreRed.copy(alpha = 0.08f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, FiksScoreRed.copy(alpha = 0.25f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = FiksScoreRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Jawaban Kamu: ${getOptionLabel(question, userAns)}",
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 11.sp,
                                                    color = FiksScoreRed
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Kunci Jawaban Benar
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = FiksScoreGreen.copy(alpha = 0.08f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, FiksScoreGreen.copy(alpha = 0.25f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = FiksScoreGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Kunci Jawaban: ${getOptionLabel(question, question.jawabanBenar)}",
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.sp,
                                                    color = FiksScoreGreen
                                                )
                                            }
                                        }

                                        if (question.pembahasan.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = FiksBg,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalAlignment = Alignment.Top,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lightbulb,
                                                        contentDescription = null,
                                                        tint = FiksAccent,
                                                        modifier = Modifier.size(15.dp).padding(top = 1.dp)
                                                    )
                                                    Text(
                                                        text = "Pembahasan: ${question.pembahasan}",
                                                        fontFamily = PoppinsFontFamily,
                                                        fontSize = 11.sp,
                                                        color = FiksTextMuted,
                                                        lineHeight = 15.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Jika semua benar
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FiksScoreCard,
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, FiksScoreGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = FiksScoreGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Luar biasa! Semua soal pretest berhasil kamu jawab dengan tepat.",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.sp,
                                    color = FiksText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Langsung ke halaman Kursus tanpa button Mulai Level 1
                    FiksButton(
                        text = "KE HALAMAN KURSUS",
                        onClick = onNavigateToCourse,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

private fun getOptionLabel(question: QuestionEntity, optionIndex: Int?): String {
    return when (optionIndex) {
        0 -> "A. ${question.opsiA}"
        1 -> "B. ${question.opsiB}"
        2 -> "C. ${question.opsiC}"
        3 -> "D. ${question.opsiD}"
        else -> "Belum dijawab"
    }
}

/**
 * Visual illustration card for visual guessing questions
 */
@Composable
fun VisualPreviewBox(visualPrompt: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = FiksBg,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        when (visualPrompt) {
            "button_contrast" -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Good button
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FiksPrimary,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "BELI SEKARANG",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }

                    // Poor contrast button
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE2DAD0),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "BELI SEKARANG",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color(0xFFF7F3EE),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
            "diamond_symbol" -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = FiksAccent,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "Login Sukses? (Ya / Tidak)",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = FiksText,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Studi Kasus Desain Visual UI/UX",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = FiksPrimary
                    )
                    Text(
                        text = "Amati pilihan jawaban dan tentukan prinsip terbaik",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksTextMuted
                    )
                }
            }
        }
    }
}
