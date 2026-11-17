package com.example.ui.course

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleSectionEntity
import com.example.data.model.QuestionEntity
import com.example.ui.components.FiksButton
import com.example.ui.components.FiksCompactButton
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksScoreYellow
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import com.example.util.ScoreColorUtils
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.WarningAmber
import kotlinx.coroutines.delay

/**
 * Layar Kuis Evaluasi Sub Materi:
 * Langkah evaluasi setelah mempelajari sub materi.
 *
 * Fitur:
 * 1. Lobby Kuis: Info soal, passing grade 75%, riwayat percobaan sebelumnya.
 * 2. Pengerjaan Kuis Interaktif: nomor soal, pilihan ganda 4 opsi, dialog konfirmasi keluar.
 * 3. Hasil & Review: skor berwarna (>=80 hijau, 75-79 kuning, <75 merah), pembahasan lengkap rujukan sub-topik.
 * 4. Tanpa cooldown pemblokiran: dapat langsung diulang atau lanjut ke sub materi berikutnya.
 */
@Composable
fun LevelQuizScreen(
    module: ModuleEntity,
    sections: List<ModuleSectionEntity>,
    questions: List<QuestionEntity>,
    latestAttempt: AssessmentAttemptEntity?,
    cooldownRemainingSeconds: Long = 0L,
    onSubmitQuiz: (score: Int, wrongQuestionIds: List<Int>) -> Unit,
    onJumpToSubtopic: (sectionId: Int) -> Unit,
    onBackToSubtopics: () -> Unit,
    onDemoBypassCooldown: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isQuizStarted by remember { mutableStateOf(false) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var isSubmitted by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showRemedialPopup by remember { mutableStateOf(false) }
    // Kuis sub materi tidak menerapkan waktu blokir
    val isBlocked = false

    // BackHandler: Jika kuis sedang dikerjakan, tampilkan dialog konfirmasi
    BackHandler(enabled = true) {
        if (isQuizStarted && !isSubmitted) {
            showExitDialog = true
        } else {
            onBackToSubtopics()
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "Batalkan Kuis?",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = FiksText
                )
            },
            text = {
                Text(
                    text = "Jawaban yang telah kamu pilih saat ini tidak akan disimpan jika kamu keluar sekarang.",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    color = FiksTextMuted,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        isQuizStarted = false
                        selectedAnswers.clear()
                        currentQuestionIndex = 0
                        onBackToSubtopics()
                    }
                ) {
                    Text("Keluar", color = FiksScoreRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Lanjut Kuis", color = FiksPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .testTag("level_quiz_screen")
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
                    onClick = {
                        if (isQuizStarted && !isSubmitted) {
                            showExitDialog = true
                        } else {
                            onBackToSubtopics()
                        }
                    },
                    modifier = Modifier.testTag("quiz_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Daftar Sub-topik",
                        tint = FiksPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Kuis Sub Materi ${module.subMateriOrder}",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = FiksPrimary
                    )
                    Text(
                        text = if (isQuizStarted && !isSubmitted) "Soal ${currentQuestionIndex + 1} dari ${questions.size}" else "Evaluasi Pemahaman",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FiksText
                    )
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            if (!isQuizStarted && !isSubmitted) {
                // ==============================================================
                // 1. LOBBY KUIS
                // ==============================================================
                Text(
                    text = "Kuis Asesmen Sub Materi ${module.subMateriOrder}",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = FiksPrimary
                )
                Text(
                    text = module.judul,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = FiksTextMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Card Info Kuis
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.dp, FiksCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Informasi Kuis",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = FiksText
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = FiksPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${questions.size}",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = FiksPrimary
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Jumlah Pertanyaan: ${questions.size} Soal",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = FiksText
                                )
                                Text(
                                    text = "Passing grade: 75% · Pilihan ganda 4 opsi",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = FiksTextMuted
                                )
                            }
                        }
                    }
                }

                if (questions.isEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = FiksScoreCard,
                        border = BorderStroke(1.dp, FiksCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_quiz_card")
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
                                text = "Soal kuis untuk sub materi ini sedang dipersiapkan.",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = FiksTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Riwayat attempt sebelumnya jika ada
                if (latestAttempt != null) {
                    val attemptColor = ScoreColorUtils.getScoreColor(latestAttempt.score)
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = attemptColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, attemptColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Percobaan Sebelumnya:",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = FiksTextMuted
                                )
                                Text(
                                    text = "Skor: ${latestAttempt.score} - ${if (latestAttempt.isPassed) "Lulus" else "Belum Memenuhi Passing Grade"}",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = attemptColor
                                )
                            }
                            Text(
                                text = "Lihat Review",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = FiksPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { isSubmitted = true }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val buttonText = when {
                    questions.isEmpty() -> "SOAL BELUM TERSEDIA"
                    latestAttempt != null -> "MULAI KUIS ULANG"
                    else -> "MULAI KUIS"
                }

                FiksButton(
                    text = buttonText,
                    onClick = {
                        isQuizStarted = true
                        selectedAnswers.clear()
                        currentQuestionIndex = 0
                        isSubmitted = false
                    },
                    enabled = questions.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "start_quiz_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                FiksCompactButton(
                    text = "Kembali ke Sub-topik",
                    onClick = onBackToSubtopics,
                    isSecondary = true,
                    leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "back_to_subtopics_from_lobby"
                )

            } else if (isQuizStarted && !isSubmitted && questions.isNotEmpty()) {
                // ==============================================================
                // 2. PENGERJAAN KUIS
                // ==============================================================
                val currentQ = questions[currentQuestionIndex]
                val answeredCount = selectedAnswers.size
                val quizProgress = (answeredCount.toFloat() / questions.size).coerceIn(0f, 1f)

                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pertanyaan ${currentQuestionIndex + 1} dari ${questions.size}",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = FiksPrimary
                    )
                    Text(
                        text = "$answeredCount/${questions.size} Terjawab",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { quizProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = FiksPrimary,
                    trackColor = Color(0xFFE5DDD5),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Kartu Soal
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.dp, FiksCardBorder),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = currentQ.teksSoal,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = FiksText,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // 4 Pilihan Jawaban
                        val options = listOf(
                            0 to currentQ.opsiA,
                            1 to currentQ.opsiB,
                            2 to currentQ.opsiC,
                            3 to currentQ.opsiD
                        )

                        val labels = listOf("A", "B", "C", "D")

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            options.forEachIndexed { optIndex, (idx, optionText) ->
                                val isSelected = selectedAnswers[currentQuestionIndex] == idx

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) FiksPrimary.copy(alpha = 0.08f) else FiksBg,
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) FiksPrimary else FiksCardBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedAnswers[currentQuestionIndex] = idx
                                        }
                                        .testTag("quiz_option_${optIndex}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) FiksPrimary else FiksScoreCard,
                                            border = BorderStroke(1.dp, if (isSelected) FiksPrimary else FiksCardBorder),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = labels[optIndex],
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (isSelected) Color.White else FiksText
                                                )
                                            }
                                        }
                                        Text(
                                            text = optionText,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 13.sp,
                                            color = if (isSelected) FiksPrimary else FiksText,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tombol Navigasi Soal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentQuestionIndex > 0) {
                        FiksCompactButton(
                            text = "Sebelumnya",
                            onClick = { currentQuestionIndex-- },
                            isSecondary = true,
                            leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
                            modifier = Modifier.weight(1f),
                            testTag = "quiz_prev_button"
                        )
                    }

                    if (currentQuestionIndex < questions.size - 1) {
                        FiksButton(
                            text = "Selanjutnya",
                            onClick = { currentQuestionIndex++ },
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            modifier = Modifier.weight(1f),
                            testTag = "quiz_next_button"
                        )
                    } else {
                        // Soal Terakhir: Tombol Kumpul Kuis
                        val allAnswered = questions.indices.all { it in selectedAnswers }
                        FiksButton(
                            text = "KUMPULKAN QUIZ",
                            onClick = {
                                // Hitung skor
                                var correctCount = 0
                                val wrongIds = mutableListOf<Int>()
                                questions.forEachIndexed { i, q ->
                                    val ans = selectedAnswers[i]
                                    if (ans == q.jawabanBenar) {
                                        correctCount++
                                    } else {
                                        wrongIds.add(q.id)
                                    }
                                }
                                val finalScore = ((correctCount.toFloat() / questions.size) * 100).toInt()
                                isSubmitted = true
                                isQuizStarted = false
                                if (finalScore < 75) {
                                    showRemedialPopup = true
                                }
                                onSubmitQuiz(finalScore, wrongIds)
                            },
                            enabled = allAnswered,
                            modifier = Modifier.weight(1f),
                            testTag = "submit_quiz_button"
                        )
                    }
                }

            } else {
                // ==============================================================
                // 3. HASIL KUIS & REVIEW PEMBAHASAN
                // ==============================================================
                val score = latestAttempt?.score ?: 0
                val isPassed = latestAttempt?.isPassed ?: (score >= 75)
                val wrongIds = latestAttempt?.incorrectQuestionIds
                    ?.split(",")
                    ?.mapNotNull { it.trim().toIntOrNull() }
                    ?: emptyList()

                // Pop-up Keterangan Kelulusan jika belum mencapai passing grade
                if (showRemedialPopup && !isPassed) {
                    Dialog(onDismissRequest = { showRemedialPopup = false }) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = FiksScoreCard,
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, FiksScoreRed.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().testTag("remedial_popup_dialog")
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
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
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(FiksScoreRed.copy(alpha = 0.15f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.WarningAmber,
                                                contentDescription = null,
                                                tint = FiksScoreRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            text = "Hasil Evaluasi Kuis",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = FiksText
                                        )
                                    }
                                    IconButton(
                                        onClick = { showRemedialPopup = false },
                                        modifier = Modifier.size(32.dp).testTag("close_remedial_popup_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Tutup Pop-up",
                                            tint = FiksTextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Nilaimu belum mencapai batas kelulusan. Pelajari kembali materi yang belum tepat.",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 13.sp,
                                    color = FiksText,
                                    lineHeight = 19.sp
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                FiksButton(
                                    text = "Tinjau Pembahasan",
                                    onClick = { showRemedialPopup = false },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Kartu Skor Utama
                val scoreColor = ScoreColorUtils.getScoreColor(score)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = scoreColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.5.dp, scoreColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isPassed) "SELAMAT! KAMU LULUS!" else "BELUM MENCAPAI 75 — BOLEH LANJUT ATAU ULANG",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = scoreColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "$score",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 52.sp,
                            color = scoreColor
                        )

                        Text(
                            text = "Passing Grade: 75",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksTextMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isPassed)
                                "Kamu telah menguasai materi Sub Materi ${module.subMateriOrder} dengan sangat baik!"
                            else
                                "Nilaimu $score dari batas minimal 75. Kamu tetap bisa lanjut atau ulang kuis kapan saja.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksText,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Review Pembahasan Soal
                if (questions.isNotEmpty()) {
                    Text(
                        text = "Review Pembahasan Soal:",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        questions.forEachIndexed { qIndex, q ->
                            val userAnsIdx = selectedAnswers[qIndex]
                            val isQuestionCorrect = (userAnsIdx == q.jawabanBenar)
                            val userAnsText = when (userAnsIdx) {
                                0 -> q.opsiA
                                1 -> q.opsiB
                                2 -> q.opsiC
                                3 -> q.opsiD
                                else -> "Tidak dijawab"
                            }
                            val correctAnsText = when (q.jawabanBenar) {
                                0 -> q.opsiA
                                1 -> q.opsiB
                                2 -> q.opsiC
                                3 -> q.opsiD
                                else -> ""
                            }

                            // Temukan sub-topik yang bersesuaian dengan soal ini
                            val relevantSection = sections.find { it.id == q.id }
                                ?: sections.getOrNull(qIndex.coerceIn(0, sections.lastIndex))
                                ?: sections.firstOrNull()

                            val cardBorder = if (isQuestionCorrect) FiksScoreGreen.copy(alpha = 0.5f) else FiksScoreRed.copy(alpha = 0.5f)

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FiksScoreCard,
                                border = BorderStroke(1.2.dp, cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Header Soal dengan Status Benar/Salah
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Soal No. ${qIndex + 1}",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = FiksTextMuted
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isQuestionCorrect) FiksScoreGreen.copy(alpha = 0.15f) else FiksScoreRed.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isQuestionCorrect) Icons.Default.Check else Icons.Default.Close,
                                                    contentDescription = if (isQuestionCorrect) "Benar" else "Salah",
                                                    tint = if (isQuestionCorrect) FiksScoreGreen else FiksScoreRed,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = if (isQuestionCorrect) "Benar" else "Salah",
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (isQuestionCorrect) FiksScoreGreen else FiksScoreRed
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = q.teksSoal,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = FiksText,
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Kartu Jawaban User
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isQuestionCorrect) Color(0xFFEAF5ED) else Color(0xFFFDE8E8),
                                        border = BorderStroke(1.dp, if (isQuestionCorrect) FiksScoreGreen.copy(alpha = 0.5f) else FiksScoreRed.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isQuestionCorrect) FiksScoreGreen else FiksScoreRed)
                                            ) {
                                                Icon(
                                                    imageVector = if (isQuestionCorrect) Icons.Default.Check else Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isQuestionCorrect) "Jawaban Kamu (Benar)" else "Jawaban Kamu (Salah)",
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = if (isQuestionCorrect) FiksScoreGreen else FiksScoreRed
                                                )
                                                Text(
                                                    text = userAnsText,
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = FiksText
                                                )
                                            }
                                        }
                                    }

                                    // Jika salah, tampilkan jawaban benar dengan ceklis hijau
                                    if (!isQuestionCorrect) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFEAF5ED),
                                            border = BorderStroke(1.dp, FiksScoreGreen.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(FiksScoreGreen)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Kunci Jawaban (Benar)",
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        color = FiksScoreGreen
                                                    )
                                                    Text(
                                                        text = correctAnsText,
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp,
                                                        color = FiksText
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Pembahasan: ${q.pembahasan}",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        color = FiksTextMuted,
                                        lineHeight = 16.sp
                                    )

                                    if (relevantSection != null && !isQuestionCorrect) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = FiksPrimary.copy(alpha = 0.1f),
                                            border = BorderStroke(1.dp, FiksPrimary.copy(alpha = 0.4f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onJumpToSubtopic(relevantSection.id) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MenuBook,
                                                    contentDescription = null,
                                                    tint = FiksPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Pelajari Sub-topik: ${relevantSection.judulSubtopik}",
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = FiksPrimary,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = FiksPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Aksi: Selalu boleh lanjut; jika belum lulus tersedia opsi Ulangi Kuis tanpa cooldown
                FiksButton(
                    text = "Lanjut",
                    onClick = onBackToSubtopics,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "continue_from_quiz_button"
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (!isPassed) {
                    FiksCompactButton(
                        text = "Ulangi Kuis",
                        onClick = {
                            isQuizStarted = true
                            selectedAnswers.clear()
                            currentQuestionIndex = 0
                            isSubmitted = false
                        },
                        isSecondary = false,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "retry_quiz_button"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                FiksCompactButton(
                    text = "Kembali ke Daftar Sub-topik",
                    onClick = onBackToSubtopics,
                    isSecondary = true,
                    leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "back_to_subtopics_from_result"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
