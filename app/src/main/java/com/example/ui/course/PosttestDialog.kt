package com.example.ui.course

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.util.ScoreColorUtils
import kotlinx.coroutines.delay
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksIconPostest
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksScoreYellow
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.FiksTrack
import com.example.ui.theme.PoppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosttestDialog(
    questions: List<QuestionEntity>,
    onCompletePosttest: (score: Int) -> Unit,
    onDismiss: () -> Unit,
    modules: List<ModuleEntity> = emptyList(),
    cooldownRemainingSeconds: Long = 0L,
    onCompletePosttestWithWrongIds: ((score: Int, wrongQuestionIds: List<Int>) -> Unit)? = null,
    onJumpToModule: (moduleId: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var isSubmitted by remember { mutableStateOf(false) }
    var calculatedScore by remember { mutableIntStateOf(0) }
    var remainingCooldown by remember(cooldownRemainingSeconds) { mutableLongStateOf(cooldownRemainingSeconds) }

    LaunchedEffect(remainingCooldown) {
        if (remainingCooldown > 0L) {
            delay(1000L)
            remainingCooldown = (remainingCooldown - 1L).coerceAtLeast(0L)
        }
    }

    val currentQuestion = questions.getOrNull(currentIndex)
    val totalQuestions = questions.size

    BasicAlertDialog(
        onDismissRequest = {
            if (isSubmitted || remainingCooldown > 0L) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("posttest_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FiksBg,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            if (remainingCooldown > 0L && !isSubmitted) {
                val m = remainingCooldown / 60
                val s = remainingCooldown % 60
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(FiksScoreRed.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = FiksScoreRed,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Posttest Terkunci Sementara",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FiksScoreRed,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format("Coba lagi dalam %02d:%02d", m, s),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Kamu sudah gagal 2 kali berturut-turut pada Posttest ini. Silakan pelajari kembali sub materi terkait selama masa jeda 10 menit.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksTextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    FiksButton(
                        text = "KEMBALI BELAJAR",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (questions.isEmpty()) {
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
                        text = "Soal Posttest untuk materi ini sedang dipersiapkan.",
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
                                    .background(FiksIconPostest.copy(alpha = 0.25f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = FiksIconPostest,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Posttest Materi 1-4",
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
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${currentIndex + 1}. ${q.teksSoal}",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = FiksText,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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
                                        .clickable { selectedAnswers[currentIndex] = optIndex }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) FiksPrimary else FiksBg)
                                        ) {
                                            Text(
                                                text = ('A' + optIndex).toString(),
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
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
                                testTag = "posttest_back_button"
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        val isAnswered = selectedAnswers.containsKey(currentIndex)
                        val isLast = currentIndex == totalQuestions - 1

                        FiksCompactButton(
                            text = if (isLast) "KUMPULKAN" else "LANJUT",
                            onClick = {
                                if (isLast) {
                                    var correctCount = 0
                                    val wrongIds = mutableListOf<Int>()
                                    questions.forEachIndexed { index, question ->
                                        if (selectedAnswers[index] == question.jawabanBenar) {
                                            correctCount++
                                        } else {
                                            wrongIds.add(question.id)
                                        }
                                    }
                                    val score = if (totalQuestions > 0) {
                                        ((correctCount.toFloat() / totalQuestions) * 100).toInt()
                                    } else 85
                                    calculatedScore = score
                                    isSubmitted = true
                                    if (onCompletePosttestWithWrongIds != null) {
                                        onCompletePosttestWithWrongIds.invoke(score, wrongIds)
                                    } else {
                                        onCompletePosttest(score)
                                    }
                                } else {
                                    currentIndex++
                                }
                            },
                            enabled = isAnswered,
                            trailingIcon = if (isLast) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            testTag = "posttest_next_button"
                        )
                    }
                }
            } else {
                // Posttest Result - Nilai, Penguasaan Materi, & Lompatan ke Materi Salah
                val scoreColor = ScoreColorUtils.getScoreColor(calculatedScore)

                val moduleNames = if (modules.isNotEmpty()) {
                    val qModuleIds = questions.map { it.moduleId }.toSet()
                    val filtered = modules.filter { it.id in qModuleIds }.sortedBy { it.subMateriOrder }
                    val target = if (filtered.isNotEmpty()) filtered else modules.sortedBy { it.subMateriOrder }
                    target.map { it.id to it.judul }
                } else {
                    questions.map { it.moduleId }.distinct().sorted().map { id ->
                        id to "Sub Materi $id"
                    }
                }

                val wrongQuestions = questions.mapIndexed { idx, q ->
                    Triple(idx + 1, q, selectedAnswers[idx])
                }.filter { (_, q, userAns) -> userAns != q.jawabanBenar }

                val moduleMasteryList = moduleNames.map { (modId, name) ->
                    val modQuestions = questions.filter { it.moduleId == modId }
                    val correctCount = questions.filterIndexed { idx, q ->
                        q.moduleId == modId && selectedAnswers[idx] == q.jawabanBenar
                    }.size
                    val isMastered = modQuestions.isNotEmpty() && correctCount >= (modQuestions.size / 2.0).coerceAtLeast(1.0)
                    Triple(modId, name, isMastered)
                }

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
                            .background(FiksIconPostest.copy(alpha = 0.22f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = FiksIconPostest,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Posttest Selesai!",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = FiksPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Skor akhir kompetensi UI/UX kamu:",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksTextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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
                            color = scoreColor,
                            modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Review Penguasaan Materi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tinjauan Penguasaan Materi",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FiksText
                        )
                        val masteredCount = moduleMasteryList.count { it.third }
                        Text(
                            text = "$masteredCount/${moduleMasteryList.size} Kompeten",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (masteredCount >= 3) FiksScoreGreen else FiksPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moduleMasteryList.forEach { (modId, name, isMastered) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FiksScoreCard,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    if (isMastered) FiksScoreGreen.copy(alpha = 0.5f) else FiksCardBorder
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
                                                if (isMastered) FiksScoreGreen.copy(alpha = 0.15f)
                                                else FiksPrimary.copy(alpha = 0.12f)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = if (isMastered) Icons.Default.CheckCircle else Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = if (isMastered) FiksScoreGreen else FiksPrimary,
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
                                            text = if (isMastered) "Kompetensi teruji dengan baik" else "Perlu penguatan materi",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            color = if (isMastered) FiksScoreGreen else FiksTextMuted
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isMastered) FiksScoreGreen else FiksPrimary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = if (isMastered) "KOMPETEN" else "TINJAU",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = if (isMastered) Color.White else FiksPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (wrongQuestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Review Jawaban & Materi Terkait",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FiksText,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            wrongQuestions.forEach { (num, question, _) ->
                                val modName = moduleNames.firstOrNull { it.first == question.moduleId }?.second ?: "Sub Materi terkait"
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = FiksScoreCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, FiksScoreRed.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Soal #$num • $modName",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = FiksScoreRed
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = question.teksSoal,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = FiksText
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = question.pembahasan,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            color = FiksTextMuted
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = FiksPrimary.copy(alpha = 0.1f),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    onDismiss()
                                                    onJumpToModule(question.moduleId)
                                                }
                                        ) {
                                            Text(
                                                text = "Pelajari Ulang: $modName",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = FiksPrimary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    FiksButton(
                        text = "KEMBALI KE BERANDA",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
