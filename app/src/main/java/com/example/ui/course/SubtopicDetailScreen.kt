package com.example.ui.course

import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DesignTaskEntity
import com.example.data.model.DesignTaskSubmissionEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleSectionEntity
import com.example.data.model.ModuleVideoEntity
import com.example.ui.components.FiksButton
import com.example.ui.components.FiksCompactButton
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily

/**
 * Layar Belajar Sub Materi sesuai revisi alur linear:
 * Materi 1 -> Materi 2 -> Interaksi Visual (bila ada) -> Video -> Pop-up konfirmasi ("Lanjut ke Kuis" / "Nanti").
 */
@Composable
fun SubtopicDetailScreen(
    module: ModuleEntity,
    section: ModuleSectionEntity,
    sections: List<ModuleSectionEntity>,
    video: ModuleVideoEntity?,
    onMarkSectionViewed: (Int) -> Unit,
    onMarkVideoWatched: () -> Unit,
    onNavigateToSection: (Int) -> Unit,
    onOpenQuiz: () -> Unit,
    onBackToSubtopics: () -> Unit,
    designTask: DesignTaskEntity? = null,
    designTaskSubmission: DesignTaskSubmissionEntity? = null,
    onUploadDesignTask: (Uri, onProgress: (Float) -> Unit, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _, _ -> },
    isBookmarked: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    isSubMateriFinished: Boolean = false,
    onFinishSubMateri: () -> Unit = {},
    viewedSectionIds: Set<Int> = emptySet(),
    videoOpened: Boolean = false,
    onOpenDiscussion: () -> Unit = {},
    isFacilitatorOrAdmin: Boolean = false,
    onSaveDesignTaskDefinition: ((judulTugas: String, instruksi: String, batasUkuranMb: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showVideoStep by remember(module.id) { mutableStateOf(false) }
    var showQuizConfirmDialog by remember(module.id) { mutableStateOf(false) }
    var quizPopupShownOnce by remember(module.id) { mutableStateOf(false) }

    val sortedSections = sections.sortedBy { it.urutan }
    val currentIndex = sortedSections.indexOfFirst { it.id == section.id }.coerceAtLeast(0)
    val splitAfterIndex = ((sortedSections.size / 2) - 1).coerceAtLeast(0)

    LaunchedEffect(section.id, showVideoStep) {
        if (!showVideoStep) {
            onMarkSectionViewed(section.id)
        } else {
            onMarkVideoWatched()
        }
    }

    val allSectionsVisited = sortedSections.isNotEmpty() && sortedSections.all { it.id in viewedSectionIds || it.id == section.id }
    val videoVisitedNow = videoOpened || showVideoStep
    val canFinishNow = allSectionsVisited && videoVisitedNow
    val hasNext = currentIndex < sortedSections.size - 1
    val hasPrevious = currentIndex > 0 || showVideoStep

    LaunchedEffect(showVideoStep, allSectionsVisited) {
        if (showVideoStep && allSectionsVisited && !quizPopupShownOnce && !isSubMateriFinished) {
            quizPopupShownOnce = true
            onFinishSubMateri()
            showQuizConfirmDialog = true
        }
    }

    if (showQuizConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showQuizConfirmDialog = false },
            containerColor = FiksScoreCard,
            title = {
                Text(
                    text = stringResource(R.string.subtopic_quiz_popup_title),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FiksText
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.subtopic_quiz_popup_body),
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    color = FiksTextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQuizConfirmDialog = false
                        if (!isSubMateriFinished) onFinishSubMateri()
                        onOpenQuiz()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("popup_proceed_to_quiz_button")
                ) {
                    Text(
                        text = stringResource(R.string.subtopic_quiz_popup_now),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showQuizConfirmDialog = false
                        if (!isSubMateriFinished && canFinishNow) onFinishSubMateri()
                    },
                    modifier = Modifier.testTag("popup_later_quiz_button")
                ) {
                    Text(
                        text = stringResource(R.string.subtopic_quiz_popup_later),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = FiksTextMuted
                    )
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .testTag("subtopic_detail_screen")
    ) {
        // Top App Bar (responsif Dark Mode menggunakan FiksScoreCard)
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
                    onClick = onBackToSubtopics,
                    modifier = Modifier.testTag("subtopic_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Daftar Sub Materi",
                        tint = FiksPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val phaseLabel = when {
                        showVideoStep -> "Video Materi"
                        currentIndex <= splitAfterIndex -> "Materi 1 · Bagian ${currentIndex + 1}/${sortedSections.size}"
                        else -> "Materi 2 · Bagian ${currentIndex + 1}/${sortedSections.size}"
                    }
                    Text(
                        text = "${module.judul} · $phaseLabel",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = FiksPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (showVideoStep) (video?.judul ?: "Video Pembelajaran") else section.judulSubtopik,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FiksText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onOpenDiscussion,
                    modifier = Modifier.testTag("subtopic_discussion_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = stringResource(R.string.subtopic_open_discussion),
                        tint = FiksPrimary
                    )
                }
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Hapus dari Modul Tersimpan" else "Simpan Modul",
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
            // Alur Step Strip baru: Materi 1 -> Materi 2 -> Video
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FlowStepChip(
                    label = stringResource(R.string.subtopic_step_materi1),
                    active = !showVideoStep && currentIndex <= splitAfterIndex,
                    done = currentIndex > splitAfterIndex || showVideoStep,
                    onClick = {
                        showVideoStep = false
                        if (sortedSections.isNotEmpty()) onNavigateToSection(sortedSections.first().id)
                    },
                    modifier = Modifier.weight(1f)
                )
                FlowStepChip(
                    label = stringResource(R.string.subtopic_step_materi2),
                    active = !showVideoStep && currentIndex > splitAfterIndex,
                    done = showVideoStep || isSubMateriFinished || (sortedSections.size <= 1 && videoVisitedNow),
                    onClick = {
                        showVideoStep = false
                        val idx = (splitAfterIndex + 1).coerceAtMost(sortedSections.lastIndex.coerceAtLeast(0))
                        sortedSections.getOrNull(idx)?.let { onNavigateToSection(it.id) }
                    },
                    modifier = Modifier.weight(1f)
                )
                FlowStepChip(
                    label = stringResource(R.string.subtopic_step_video),
                    active = showVideoStep,
                    done = videoVisitedNow && isSubMateriFinished && !showVideoStep,
                    onClick = {
                        showVideoStep = true
                        onMarkVideoWatched()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showVideoStep) {
                VideoAccessSection(
                    video = video,
                    onVideoPlayed = onMarkVideoWatched
                )

                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSubMateriFinished || canFinishNow) FiksScoreGreen.copy(alpha = 0.1f) else FiksScoreCard,
                    border = BorderStroke(1.dp, if (isSubMateriFinished || canFinishNow) FiksScoreGreen else FiksCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isSubMateriFinished || canFinishNow) FiksScoreGreen else FiksPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isSubMateriFinished || canFinishNow) "Sub Materi Selesai — Kuis Terbuka!" else "Buka Semua Halaman Materi",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSubMateriFinished || canFinishNow) FiksScoreGreen else FiksText
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (!canFinishNow && !isSubMateriFinished) {
                                "Pastikan semua halaman Materi 1 dan Materi 2 sudah dibuka sebelum melanjutkan ke Kuis."
                            } else {
                                "Setelah membuka halaman Video, kamu bisa langsung lanjut mengerjakan Kuis Sub Materi."
                            },
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = FiksTextMuted
                        )
                    }
                }
            } else {
                // Konten Materi Teks + Visual
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.dp, FiksCardBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = FiksPrimary,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${section.urutan}",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = section.judulSubtopik,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = FiksText,
                                lineHeight = 22.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = section.isiTeks,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 13.sp,
                            color = FiksText,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("section_visual_container")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = FiksAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Tampilannya tuh gini loh!",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = FiksPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionVisualIllustration(
                                gambarTipe = section.gambarTipe,
                                keteranganGambar = section.keteranganGambar
                            )
                        }
                    }
                }

                // Pada halaman akhir Materi 2: tampilkan Interaksi Visual (bila relevan) + Tugas Desain sebelum ke Video
                if (!hasNext) {
                    if (module.interaksiTipe.isNotBlank()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        SubMateriInteractiveSection(
                            interaksiTipe = module.interaksiTipe,
                            moduleTitle = module.judul
                        )
                    }

                    // Tugas Desain opsional (slot selalu tersedia untuk fasilitator/super admin)
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

            Spacer(modifier = Modifier.height(22.dp))

            // Navigasi Bawah: Materi 1 -> Materi 2 -> Video -> Pop-up Konfirmasi Kuis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasPrevious) {
                    FiksCompactButton(
                        text = "Sebelumnya",
                        onClick = {
                            if (showVideoStep) {
                                showVideoStep = false
                                val lastIdx = sortedSections.lastIndex.coerceAtLeast(0)
                                sortedSections.getOrNull(lastIdx)?.let { onNavigateToSection(it.id) }
                            } else if (currentIndex > 0) {
                                onNavigateToSection(sortedSections[currentIndex - 1].id)
                            }
                        },
                        isSecondary = true,
                        leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
                        modifier = Modifier.weight(1f),
                        testTag = "prev_subtopic_button"
                    )
                }

                if (showVideoStep) {
                    FiksButton(
                        text = "Lanjut ke Kuis",
                        enabled = isSubMateriFinished || canFinishNow,
                        onClick = {
                            if (!isSubMateriFinished && canFinishNow) {
                                onFinishSubMateri()
                            }
                            showQuizConfirmDialog = true
                        },
                        trailingIcon = Icons.Default.Quiz,
                        modifier = Modifier.weight(1f),
                        testTag = "finish_to_quiz_button"
                    )
                } else if (hasNext) {
                    FiksButton(
                        text = if (currentIndex == splitAfterIndex) "Lanjut ke Materi 2" else "Lanjut Halaman",
                        onClick = { onNavigateToSection(sortedSections[currentIndex + 1].id) },
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        modifier = Modifier.weight(1f),
                        testTag = "next_subtopic_button"
                    )
                } else {
                    FiksButton(
                        text = "Lanjut ke Video",
                        onClick = {
                            showVideoStep = true
                            onMarkVideoWatched()
                        },
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        modifier = Modifier.weight(1f),
                        testTag = "next_subtopic_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun FlowStepChip(
    label: String,
    active: Boolean,
    done: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = when {
            active -> FiksPrimary
            done -> FiksScoreGreen.copy(alpha = 0.14f)
            else -> FiksScoreCard
        },
        border = BorderStroke(
            width = 1.dp,
            color = when {
                active -> FiksPrimary
                done -> FiksScoreGreen
                else -> FiksCardBorder
            }
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
        ) {
            Text(
                text = label,
                fontFamily = PoppinsFontFamily,
                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 11.sp,
                color = when {
                    active -> Color.White
                    done -> FiksScoreGreen
                    else -> FiksTextMuted
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

