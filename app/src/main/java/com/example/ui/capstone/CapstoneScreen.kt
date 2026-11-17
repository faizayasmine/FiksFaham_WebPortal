package com.example.ui.capstone

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.CapstoneRubricHelper
import com.example.CapstoneSubmission
import com.example.RubricLevel
import com.example.data.model.CapstoneSubmissionEntity
import com.example.ui.CapstoneState
import com.example.ui.components.FiksButton
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksLock
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksPrimaryLight
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksScoreYellow
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import com.example.util.ScoreColorUtils

@Composable
fun CapstoneScreen(
    capstoneState: CapstoneState,
    submission: CapstoneSubmissionEntity?,
    currentUserRole: String = "mahasiswa",
    currentStudentUid: String = "",
    mentorSubmissions: List<CapstoneSubmission> = emptyList(),
    mentorFilter: String? = null,
    isLoadingSubmissions: Boolean = false,
    onFilterChange: (String?) -> Unit = {},
    onRefreshSubmissions: () -> Unit = {},
    onUploadFile: (
        uri: Uri,
        fileName: String,
        onProgress: (Float) -> Unit,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) -> Unit = { _, _, _, _, _ -> },
    onSubmit: (title: String, desc: String, linkFigma: String, fileUrl: String) -> Unit = { _, _, _, _ -> },
    onReviewSubmission: ((uid: String, materiId: Int, score: Int, feedback: String) -> Unit)? = null,
    onReviewSubmissionWithDetails: ((uid: String, materiId: Int, score: Int, feedback: String, status: String, rubricSummary: String) -> Unit)? = null,
    onObserveStudentCapstone: ((uid: String) -> Unit)? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (com.example.CapstoneStatusUtils.isStaffRole(currentUserRole)) {
        MentorCapstoneView(
            submissions = mentorSubmissions,
            currentFilter = mentorFilter,
            isLoading = isLoadingSubmissions,
            onFilterChange = onFilterChange,
            onRefresh = onRefreshSubmissions,
            onReview = { uid, materiId, score, feedback, status, rubricSummary ->
                if (onReviewSubmissionWithDetails != null) {
                    onReviewSubmissionWithDetails.invoke(uid, materiId, score, feedback, status, rubricSummary)
                } else {
                    onReviewSubmission?.invoke(uid, materiId, score, feedback)
                }
            },
            onBack = onBack,
            modifier = modifier
        )
    } else {
        StudentCapstoneView(
            capstoneState = capstoneState,
            submission = submission,
            currentStudentUid = currentStudentUid,
            onUploadFile = onUploadFile,
            onSubmit = onSubmit,
            onObserveStudentCapstone = onObserveStudentCapstone,
            onBack = onBack,
            modifier = modifier
        )
    }
}

@Composable
private fun MentorCapstoneView(
    submissions: List<CapstoneSubmission>,
    currentFilter: String?,
    isLoading: Boolean,
    onFilterChange: (String?) -> Unit,
    onRefresh: () -> Unit,
    onReview: (uid: String, materiId: Int, score: Int, feedback: String, status: String, rubricSummary: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubmissionForReview by remember { mutableStateOf<CapstoneSubmission?>(null) }
    var reviewScoreText by remember { mutableStateOf("") }
    var reviewFeedbackText by remember { mutableStateOf("") }
    var reviewStatus by remember { mutableStateOf("approved") }
    val selectedRubricLevels = remember { mutableStateMapOf<String, RubricLevel>() }
    var reviewError by remember { mutableStateOf<String?>(null) }
    var showReviewDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .testTag("capstone_screen")
    ) {
        // App Bar
        Surface(
            color = FiksBg,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = FiksPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Evaluasi Capstone (Mentor)",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FiksPrimary
                    )
                }

                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Muat Ulang",
                        tint = FiksPrimary
                    )
                }
            }
        }

        // Filter Bar (Semua / Menunggu Penilaian / Sudah Dinilai)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterTabChip(
                label = "Semua",
                isSelected = currentFilter == null,
                onClick = { onFilterChange(null) },
                modifier = Modifier.testTag("filter_chip_all")
            )
            FilterTabChip(
                label = "Menunggu Penilaian",
                isSelected = currentFilter == "submitted",
                onClick = { onFilterChange("submitted") },
                modifier = Modifier.testTag("filter_chip_submitted")
            )
            FilterTabChip(
                label = "Sudah Dinilai",
                isSelected = currentFilter == "reviewed",
                onClick = { onFilterChange("reviewed") },
                modifier = Modifier.testTag("filter_chip_reviewed")
            )
        }

        // Content
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FiksPrimary)
            }
        } else if (submissions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = FiksTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum Ada Pengumpulan",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentFilter == "submitted") {
                            "Tidak ada submission yang sedang menunggu penilaian."
                        } else if (currentFilter == "reviewed") {
                            "Belum ada submission yang telah dinilai."
                        } else {
                            "Belum ada mahasiswa yang mengumpulkan Capstone Project."
                        },
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = FiksTextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(submissions, key = { "${it.uid}_${it.materiId}" }) { item ->
                    val normalizedStatus = com.example.CapstoneStatusUtils.normalize(item.status)
                    val isApproved = normalizedStatus == "approved" && item.nilai != null
                    val isRevisionNeeded = normalizedStatus == "revision_needed"
                    val isRejected = normalizedStatus == "rejected"
                    val isReviewed = (isApproved || isRevisionNeeded || isRejected) && item.nilai != null
                    val badgeColor = when {
                        isApproved -> FiksScoreGreen
                        isRevisionNeeded || isRejected -> FiksScoreRed
                        else -> FiksPrimary
                    }
                    val badgeLabel = when {
                        isApproved -> "Disetujui (${item.nilai}/100)"
                        isRejected -> "Ditolak Final (${item.nilai ?: 0}/100)"
                        isRevisionNeeded -> "Perlu Revisi (${item.nilai ?: 0}/100)"
                        normalizedStatus == "under_review" -> "Sedang Ditinjau"
                        else -> "Menunggu Penilaian"
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FiksScoreCard,
                        border = BorderStroke(1.2.dp, FiksCardBorder),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header: Nama & Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.nama.ifBlank { "Mahasiswa" },
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = FiksText
                                    )
                                    if (item.email.isNotBlank()) {
                                        Text(
                                            text = item.email,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 12.sp,
                                            color = FiksTextMuted
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = badgeColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = badgeLabel,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Judul Proyek
                            Text(
                                text = "Judul Proyek:",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = FiksPrimary
                            )
                            Text(
                                text = item.judul.ifBlank { "(Tanpa Judul)" },
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = FiksText
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Deskripsi
                            Text(
                                text = "Deskripsi:",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = FiksPrimary
                            )
                            Text(
                                text = item.deskripsi.ifBlank { "(Tanpa deskripsi)" },
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = FiksText,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Tautan / Lampiran
                            if (item.linkUrl.isNotBlank() || item.fileUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (item.linkUrl.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = FiksAccent.copy(alpha = 0.15f),
                                            modifier = Modifier.clickable {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.linkUrl))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Link,
                                                    contentDescription = null,
                                                    tint = FiksPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Tautan Figma",
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.sp,
                                                    color = FiksPrimary
                                                )
                                            }
                                        }
                                    }

                                    if (item.fileUrl.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = FiksPrimary.copy(alpha = 0.1f),
                                            modifier = Modifier.clickable {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.fileUrl))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AttachFile,
                                                    contentDescription = null,
                                                    tint = FiksPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Berkas Tugas",
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

                            // Jika sudah dinilai, tampilkan feedback ringkas
                            if (isReviewed) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FiksBg,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Evaluasi Mentor (Nilai: ${item.nilai}/100):",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = FiksScoreGreen
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.feedback.ifBlank { "-" },
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            color = FiksText,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Button
                            Button(
                                onClick = {
                                    selectedSubmissionForReview = item
                                    reviewScoreText = item.nilai?.toString() ?: ""
                                    reviewFeedbackText = item.feedback
                                    reviewStatus = when (com.example.CapstoneStatusUtils.normalize(item.status)) {
                                        "approved" -> "approved"
                                        "revision_needed" -> "revision_needed"
                                        "under_review" -> "under_review"
                                        "rejected" -> "rejected"
                                        else -> "approved"
                                    }
                                    selectedRubricLevels.clear()
                                    reviewError = null
                                    showReviewDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FiksScoreGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("mentor_give_review_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RateReview,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isReviewed) "UBAH REVIEW MENTOR" else "BERI PENILAIAN CAPSTONE",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Mentor Review Dialog
    if (showReviewDialog && selectedSubmissionForReview != null) {
        val target = selectedSubmissionForReview!!
        Dialog(onDismissRequest = { showReviewDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mentor_review_dialog")
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
                            text = "Review: ${target.nama.ifBlank { "Mahasiswa" }}",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = FiksText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showReviewDialog = false },
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

                    Text(
                        text = "Nilai Capstone (0 - 100) *",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = reviewScoreText,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                reviewScoreText = input
                            }
                        },
                        placeholder = { Text("Contoh: 90", fontFamily = PoppinsFontFamily, fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("capstone_review_score_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder,
                            focusedContainerColor = FiksScoreCard,
                            unfocusedContainerColor = FiksScoreCard
                        )
                    )

                    Text(
                        text = "Rubrik Penilaian (5 Kriteria x 4 Level)",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CapstoneRubricHelper.defaultCriteria.forEach { criterion ->
                        val pickedLevel = selectedRubricLevels[criterion.key]
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                text = "${criterion.title} (bobot 20%)",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = FiksTextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                RubricLevel.entries.forEach { lvl ->
                                    val isSel = pickedLevel == lvl
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSel) FiksPrimary else FiksBg,
                                        border = BorderStroke(1.dp, if (isSel) FiksPrimary else FiksCardBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                selectedRubricLevels[criterion.key] = lvl
                                                val criteriaWithLevels = CapstoneRubricHelper.defaultCriteria.map { c ->
                                                    c.copy(level = selectedRubricLevels[c.key] ?: c.level)
                                                }
                                                val calc = CapstoneRubricHelper.calculateScore(criteriaWithLevels)
                                                reviewScoreText = calc.toString()
                                                reviewStatus = if (calc >= 75) "approved" else "revision_needed"
                                            }
                                    ) {
                                        Text(
                                            text = lvl.label.split(" ").first(),
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp,
                                            color = if (isSel) Color.White else FiksText,
                                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Status Evaluasi",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "approved" to "Disetujui",
                            "revision_needed" to "Revisi",
                            "under_review" to "Ditinjau"
                        ).forEach { (stKey, stLabel) ->
                            val isSel = reviewStatus == stKey
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) FiksPrimary else FiksBg,
                                border = BorderStroke(1.dp, if (isSel) FiksPrimary else FiksCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { reviewStatus = stKey }
                            ) {
                                Text(
                                    text = stLabel,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.White else FiksText,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Catatan & Feedback Evaluasi *",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = reviewFeedbackText,
                        onValueChange = { reviewFeedbackText = it },
                        placeholder = { Text("Tuliskan evaluasi desain, user flow, dan rubrik portofolio...", fontFamily = PoppinsFontFamily, fontSize = 13.sp) },
                        minLines = 4,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("capstone_review_feedback_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder,
                            focusedContainerColor = FiksScoreCard,
                            unfocusedContainerColor = FiksScoreCard
                        )
                    )

                    if (reviewError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = reviewError!!,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksScoreRed,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showReviewDialog = false },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, FiksCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Batal",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = FiksTextMuted
                            )
                        }

                        Button(
                            onClick = {
                                val score = reviewScoreText.toIntOrNull()
                                if (score == null || score !in 0..100) {
                                    reviewError = "Nilai harus berupa angka antara 0 hingga 100."
                                    return@Button
                                }
                                if (reviewFeedbackText.trim().isBlank()) {
                                    reviewError = "Catatan evaluasi tidak boleh kosong."
                                    return@Button
                                }
                                reviewError = null
                                showReviewDialog = false
                                val summary = if (selectedRubricLevels.isNotEmpty()) {
                                    val criteriaWithLevels = CapstoneRubricHelper.defaultCriteria.map { c ->
                                        c.copy(level = selectedRubricLevels[c.key] ?: c.level)
                                    }
                                    CapstoneRubricHelper.formatSummary(criteriaWithLevels)
                                } else ""
                                val effectiveStatus = if (reviewStatus == "approved" && score < 75) {
                                    "revision_needed"
                                } else reviewStatus
                                onReview(target.uid, target.materiId, score, reviewFeedbackText.trim(), effectiveStatus, summary)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FiksScoreGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("submit_review_button")
                        ) {
                            Text(
                                text = "Simpan Review",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) FiksPrimary else FiksPrimary.copy(alpha = 0.08f),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else FiksPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun StudentCapstoneView(
    capstoneState: CapstoneState,
    submission: CapstoneSubmissionEntity?,
    currentStudentUid: String,
    onUploadFile: (
        uri: Uri,
        fileName: String,
        onProgress: (Float) -> Unit,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) -> Unit,
    onSubmit: (title: String, desc: String, linkFigma: String, fileUrl: String) -> Unit,
    onObserveStudentCapstone: ((uid: String) -> Unit)? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Mendengarkan dokumen milik mahasiswa dari Firestore
    LaunchedEffect(currentStudentUid) {
        if (currentStudentUid.isNotBlank()) {
            onObserveStudentCapstone?.invoke(currentStudentUid)
        }
    }

    var judul by remember(submission?.judulProject) { mutableStateOf(submission?.judulProject ?: "") }
    var deskripsi by remember(submission?.deskripsi) { mutableStateOf(submission?.deskripsi ?: "") }
    var linkFigma by remember(submission?.linkFigma) { mutableStateOf(submission?.linkFigma ?: "") }
    var selectedTrack by remember { mutableStateOf("UI/UX Design") }
    var isEditingRevision by remember { mutableStateOf(false) }

    // Upload state
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSize by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadedFileUrl by remember { mutableStateOf(submission?.fileUrl ?: "") }
    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            val meta = getFileNameAndSize(context, uri)
            selectedFileName = meta.first
            selectedFileSize = formatFileSize(meta.second)
            uploadStatusMessage = null
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .testTag("capstone_screen")
    ) {
        // App Bar
        Surface(
            color = FiksBg,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = FiksPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Capstone Project UI/UX",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FiksPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            if (capstoneState == CapstoneState.LOCKED) {
                // Locked View
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(FiksLock.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Terkunci",
                            tint = FiksLock,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Capstone Masih Terkunci",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FiksLock
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Untuk membuka pengumpulan Capstone Project, kamu wajib menyelesaikan seluruh Level 1-4 dan lulus Posttest materi.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = FiksTextMuted,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FiksButton(
                        text = "KEMBALI KE BERANDA",
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (capstoneState == CapstoneState.SUBMITTED && submission != null && !isEditingRevision) {
                // Submitted / Under Review / Revision Needed / Approved / Rejected View
                val normStatus = com.example.CapstoneStatusUtils.normalize(submission.status)
                val isRejected = normStatus == "rejected"
                val isApproved = (normStatus == "approved" && (submission.nilai == null || submission.nilai >= 75))
                val isRevisionNeeded = normStatus == "revision_needed" ||
                    (normStatus == "approved" && submission.nilai != null && submission.nilai < 75)
                val isUnderReview = normStatus == "under_review"
                val isReviewed = (isApproved || isRevisionNeeded || isRejected) && submission.nilai != null
                val statusColor = when {
                    isApproved -> FiksScoreGreen
                    isRevisionNeeded || isRejected -> FiksScoreRed
                    else -> FiksPrimary
                }
                val headerTitle = when {
                    isApproved -> "Capstone Disetujui Mentor!"
                    isRejected -> "Capstone Ditolak Final"
                    isRevisionNeeded -> "Capstone Perlu Revisi"
                    isUnderReview -> "Capstone Sedang Ditinjau Mentor"
                    else -> "Capstone Berhasil Dikumpulkan!"
                }
                val headerSubtitle = when {
                    isApproved -> "Status: Disetujui (Approved)"
                    isRejected -> "Status: Ditolak Final (Rejected)"
                    isRevisionNeeded -> "Status: Perlu Revisi • Kesempatan Revisi ${submission.revisionCount}/${CapstoneRubricHelper.MAX_REVISIONS}"
                    isUnderReview -> "Status: Sedang Ditinjau Mentor"
                    else -> "Status: Menunggu Evaluasi Mentor"
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.2.dp, FiksCardBorder),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                contentDescription = headerSubtitle,
                                tint = statusColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = headerTitle,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = statusColor
                                )
                                Text(
                                    text = headerSubtitle,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = if (isReviewed) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (isReviewed) statusColor else FiksTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Judul Proyek:",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = FiksPrimary
                        )
                        Text(
                            text = submission.judulProject,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = FiksText
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Deskripsi Solusi UI/UX:",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = FiksPrimary
                        )
                        Text(
                            text = submission.deskripsi,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 13.sp,
                            color = FiksText,
                            lineHeight = 18.sp
                        )

                        if (submission.linkFigma.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tautan Figma / Prototype:",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = FiksPrimary
                            )
                            Text(
                                text = submission.linkFigma,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 13.sp,
                                color = FiksAccent,
                                modifier = Modifier.clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(submission.linkFigma))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                            )
                        }

                        if (submission.fileUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "File Dokumen / Portofolio:",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = FiksPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FiksPrimary.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(submission.fileUrl))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = FiksPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Buka File Lampiran",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = FiksPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Launch,
                                        contentDescription = null,
                                        tint = FiksPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        if (isReviewed && submission.nilai != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FiksPrimaryLight.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, FiksPrimary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("capstone_score_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val scoreColor = ScoreColorUtils.getScoreColor(submission.nilai)
                                    Column {
                                        Text(
                                            text = "Nilai Capstone Project:",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = FiksPrimary
                                        )
                                        Text(
                                            text = "${submission.nilai} / 100",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            color = scoreColor
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = scoreColor
                                    ) {
                                        val badgeResultText = when {
                                            isRejected -> "DITOLAK"
                                            isApproved -> {
                                                val predikat = com.example.util.CertificateConfig.calculatePredikat(submission.nilai)
                                                if (predikat.isNotBlank()) "LULUS • ${predikat.uppercase()}" else "LULUS"
                                            }
                                            else -> "PERLU REVISI"
                                        }
                                        Text(
                                            text = badgeResultText,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (!submission.rubricSummary.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FiksBg,
                                border = BorderStroke(1.dp, FiksCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Ringkasan Rubrik Mentor:",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = FiksPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = submission.rubricSummary,
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        color = FiksText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Catatan Mentor Feedback Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FiksBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("capstone_feedback_box")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RateReview,
                                        contentDescription = "Feedback",
                                        tint = FiksPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isReviewed) "Evaluasi & Catatan Mentor:" else "Catatan Dosen / Mentor:",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = FiksPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = submission.feedback ?: "Proyek telah diterima dan sedang dalam antrean peninjauan portofolio.",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.sp,
                                    color = FiksText,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isRejected) {
                    Text(
                        text = "Submission Capstone berstatus ditolak final. Hubungi Fasilitator atau Super Admin melalui forum jika membutuhkan klarifikasi.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksScoreRed,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                } else if (isRevisionNeeded && submission.revisionCount < CapstoneRubricHelper.MAX_REVISIONS) {
                    FiksButton(
                        text = "KIRIM REVISI CAPSTONE (${submission.revisionCount + 1}/${CapstoneRubricHelper.MAX_REVISIONS})",
                        onClick = { isEditingRevision = true },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "resubmit_capstone_button"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                } else if (isRevisionNeeded && submission.revisionCount >= CapstoneRubricHelper.MAX_REVISIONS) {
                    Text(
                        text = "Batas revisi maksimal (2 kali) telah digunakan.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksScoreRed,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                FiksButton(
                    text = "KEMBALI KE BERANDA",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Active Form View
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FiksAccent.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = FiksPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Capstone Project Akhir Dunia Desain",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = FiksPrimary
                            )
                            Text(
                                text = "Pilih fokus bidang desain, lalu unggah file dokumen atau link prototype kamu!",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksText,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Fokus Bidang Desain (5 Track Materi)",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = FiksText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("UI/UX Design", "Desain Grafis", "Motion & Animasi", "Desain Ruang", "Desain Produk").forEach { track ->
                        val isSel = selectedTrack == track
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSel) FiksPrimary else FiksScoreCard,
                            border = BorderStroke(1.dp, if (isSel) FiksPrimary else FiksCardBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTrack = track }
                        ) {
                            Text(
                                text = track,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else FiksText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input Judul
                Text(
                    text = "Judul Proyek Desain *",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = FiksText
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    placeholder = { Text("Contoh: Redesain Aplikasi Pembayaran Mahasiswa", fontFamily = PoppinsFontFamily, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("capstone_title_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FiksPrimary,
                        unfocusedBorderColor = FiksCardBorder,
                        focusedContainerColor = FiksScoreCard,
                        unfocusedContainerColor = FiksScoreCard
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input Deskripsi
                Text(
                    text = "Deskripsi Masalah & Solusi UI/UX *",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = FiksText
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    placeholder = { Text("Jelaskan user problem, alur user flow yang dibuat, dan alasan pemilihan layout...", fontFamily = PoppinsFontFamily, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("capstone_desc_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FiksPrimary,
                        unfocusedBorderColor = FiksCardBorder,
                        focusedContainerColor = FiksScoreCard,
                        unfocusedContainerColor = FiksScoreCard
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input Figma Link
                Text(
                    text = "Tautan Figma / Prototype (Opsional jika upload file)",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = FiksText
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = linkFigma,
                    onValueChange = { linkFigma = it },
                    placeholder = { Text("https://www.figma.com/file/...", fontFamily = PoppinsFontFamily, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = FiksPrimary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("capstone_link_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FiksPrimary,
                        unfocusedBorderColor = FiksCardBorder,
                        focusedContainerColor = FiksScoreCard,
                        unfocusedContainerColor = FiksScoreCard
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // File Upload Section
                Text(
                    text = "Unggah Berkas Capstone (PDF / Gambar) *",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = FiksText
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.2.dp, if (selectedFileUri != null) FiksPrimary else FiksCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (selectedFileUri == null && uploadedFileUrl.isBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { filePickerLauncher.launch("*/*") }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(FiksPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = "Unggah Berkas",
                                        tint = FiksPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Pilih Berkas Dokumen / Gambar",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = FiksPrimary
                                    )
                                    Text(
                                        text = "Mendukung PDF, PNG, JPG (Maks. 25MB)",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        color = FiksTextMuted
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val icon = if (selectedFileName.endsWith(".pdf", ignoreCase = true)) {
                                        Icons.Default.PictureAsPdf
                                    } else {
                                        Icons.Default.Description
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = FiksPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (selectedFileName.isNotBlank()) selectedFileName else "Berkas Capstone Terpilih",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = FiksText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (selectedFileSize.isNotBlank()) {
                                            Text(
                                                text = selectedFileSize,
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                color = FiksTextMuted
                                            )
                                        }
                                    }
                                }

                                if (!isUploading) {
                                    IconButton(
                                        onClick = {
                                            selectedFileUri = null
                                            selectedFileName = ""
                                            selectedFileSize = ""
                                            uploadedFileUrl = ""
                                            uploadProgress = 0f
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus Berkas",
                                            tint = FiksScoreRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            if (isUploading) {
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { uploadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = FiksPrimary,
                                    trackColor = FiksPrimary.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Mengunggah berkas ke penyimpanan...",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        color = FiksPrimary
                                    )
                                    Text(
                                        text = "${(uploadProgress * 100).toInt()}%",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = FiksPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "* Minimal sertakan salah satu: Tautan Figma ATAU Berkas Dokumen/Gambar.",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.sp,
                    color = FiksTextMuted
                )

                uploadStatusMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksScoreRed
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                val hasLinkOrFile = linkFigma.isNotBlank() || selectedFileUri != null || uploadedFileUrl.isNotBlank()
                val isValid = judul.isNotBlank() && deskripsi.isNotBlank() && hasLinkOrFile && !isUploading

                FiksButton(
                    text = if (isUploading) "MENGUNGGAH..." else "KUMPULKAN CAPSTONE",
                    onClick = {
                        val fileUri = selectedFileUri
                        val finalJudul = if (judul.trim().startsWith("[")) judul.trim() else "[$selectedTrack] ${judul.trim()}"
                        uploadStatusMessage = null
                        if (fileUri != null && uploadedFileUrl.isBlank()) {
                            isUploading = true
                            uploadProgress = 0.05f
                            onUploadFile(
                                fileUri,
                                selectedFileName,
                                { progress -> uploadProgress = progress },
                                { downloadUrl ->
                                    isUploading = false
                                    if (downloadUrl.startsWith("https://", ignoreCase = true) || downloadUrl.startsWith("http://", ignoreCase = true)) {
                                        uploadedFileUrl = downloadUrl
                                        isEditingRevision = false
                                        onSubmit(finalJudul, deskripsi.trim(), linkFigma.trim(), downloadUrl)
                                    } else {
                                        uploadStatusMessage = "URL unggahan tidak valid untuk akses web. Silakan unggah ulang berkas."
                                    }
                                },
                                { error ->
                                    isUploading = false
                                    uploadStatusMessage = "Gagal mengunggah berkas ke Firebase Storage (${error.localizedMessage ?: "Periksa koneksi internet"}). Silakan coba unggah ulang."
                                }
                            )
                        } else {
                            isEditingRevision = false
                            val safeUploadedUrl = uploadedFileUrl.takeIf {
                                it.startsWith("https://", ignoreCase = true) || it.startsWith("http://", ignoreCase = true)
                            }.orEmpty()
                            onSubmit(finalJudul, deskripsi.trim(), linkFigma.trim(), safeUploadedUrl)
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "submit_capstone_button"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun getFileNameAndSize(context: Context, uri: Uri): Pair<String, Long> {
    var name = "capstone_${System.currentTimeMillis()}"
    var size = 0L
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) name = cursor.getString(nameIndex)
                if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
            }
        }
    } catch (_: Exception) {}
    return Pair(name, size)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return ""
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(java.util.Locale.US, "%.1f MB", mb)
    } else {
        String.format(java.util.Locale.US, "%.0f KB", kb)
    }
}
