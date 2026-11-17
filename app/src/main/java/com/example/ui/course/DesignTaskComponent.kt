package com.example.ui.course

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.DesignTaskEntity
import com.example.data.model.DesignTaskSubmissionEntity
import com.example.ui.components.FiksButton
import com.example.ui.components.FiksCompactButton
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily

/**
 * Komponen Tugas Desain (Tahap 9):
 * Hanya ditampilkan jika Sub Materi memiliki definisi tugas desain (task != null).
 * Menyediakan tombol "Unggah Desain", validasi format/ukuran, kompresi,
 * pratinjau gambar yang sudah dikumpulkan, dan tombol unggah ulang (menimpa).
 * Tugas ini bersifat opsional dan tidak mengubah syarat kelulusan Sub Materi.
 */
@Composable
fun DesignTaskSection(
    task: DesignTaskEntity?,
    submission: DesignTaskSubmissionEntity?,
    onUploadImage: (Uri, onProgress: (Float) -> Unit, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    isFacilitatorOrAdmin: Boolean = false,
    onSaveDesignTaskDefinition: ((judulTugas: String, instruksi: String, batasUkuranMb: Int) -> Unit)? = null
) {
    var showTaskEditorDialog by remember { mutableStateOf(false) }

    if (showTaskEditorDialog && onSaveDesignTaskDefinition != null) {
        var titleInput by remember(task) { mutableStateOf(task?.judulTugas.orEmpty()) }
        var instructionInput by remember(task) { mutableStateOf(task?.instruksi.orEmpty()) }
        var maxMbInput by remember(task) { mutableStateOf((task?.batasUkuranMb ?: 5).toString()) }

        AlertDialog(
            onDismissRequest = { showTaskEditorDialog = false },
            containerColor = FiksScoreCard,
            title = {
                Text(
                    text = if (task == null) "Tambah Tugas Desain" else "Perbarui Tugas Desain",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = FiksText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Judul Tugas", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = instructionInput,
                        onValueChange = { instructionInput = it },
                        label = { Text("Instruksi Pengerjaan", fontSize = 12.sp) },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = maxMbInput,
                        onValueChange = { maxMbInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Batas Ukuran (MB, 1-20)", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val maxMb = (maxMbInput.toIntOrNull() ?: 5).coerceIn(1, 20)
                        onSaveDesignTaskDefinition(titleInput.trim(), instructionInput.trim(), maxMb)
                        showTaskEditorDialog = false
                    },
                    enabled = titleInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = FiksPrimary)
                ) {
                    Text("Simpan", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTaskEditorDialog = false }) {
                    Text("Batal", fontFamily = PoppinsFontFamily, color = FiksTextMuted)
                }
            }
        )
    }

    // Jika tugas belum diunggah oleh fasilitator / super admin, tampilkan slot tempatnya
    if (task == null) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = FiksScoreCard,
            border = BorderStroke(1.dp, FiksCardBorder),
            shadowElevation = 1.dp,
            modifier = modifier
                .fillMaxWidth()
                .testTag("design_task_empty_slot")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = FiksAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tugas Desain",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = FiksText
                        )
                        Text(
                            text = if (isFacilitatorOrAdmin) {
                                "Slot tugas siap diisi oleh Fasilitator / Super Admin"
                            } else {
                                "Belum ada tugas desain dari fasilitator untuk sub-materi ini"
                            },
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksTextMuted
                        )
                    }
                }
                if (isFacilitatorOrAdmin && onSaveDesignTaskDefinition != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FiksCompactButton(
                        text = "Tambah Tugas Desain",
                        onClick = { showTaskEditorDialog = true },
                        leadingIcon = Icons.Default.Edit,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "add_design_task_definition_button"
                    )
                }
            }
        }
        return
    }

    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNotice by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                isUploading = true
                errorMessage = null
                uploadProgress = 0f

                onUploadImage(
                    uri,
                    { progress -> uploadProgress = progress },
                    {
                        isUploading = false
                        successNotice = "Desain berhasil dikumpulkan!"
                    },
                    { error ->
                        isUploading = false
                        errorMessage = error
                    }
                )
            }
        }
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, FiksCardBorder),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("design_task_section")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = FiksAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "Tugas Desain",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FiksText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isFacilitatorOrAdmin && onSaveDesignTaskDefinition != null) {
                        IconButton(
                            onClick = { showTaskEditorDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Tugas Desain",
                                tint = FiksPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    // Badge Opsional
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FiksBg,
                        border = BorderStroke(0.5.dp, FiksCardBorder)
                    ) {
                        Text(
                            text = "Opsional",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = FiksTextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Judul & Instruksi Tugas
            Text(
                text = task.judulTugas,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = FiksText
            )

            if (task.instruksi.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.instruksi,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = FiksTextMuted,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Keterangan Format & Batas Ukuran
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = FiksTextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Format: JPG, PNG · Maks: ${task.batasUkuranMb} MB (otomatis dikompres)",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.sp,
                    color = FiksTextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Banner Error jika ada
            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FiksScoreRed.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, FiksScoreRed.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("design_task_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = FiksScoreRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = errorMessage.orEmpty(),
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksScoreRed
                        )
                    }
                }
            }

            // Indikator Unggah Berjalan
            if (isUploading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("design_task_uploading_indicator")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mengompres & mengunggah...",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(uploadProgress * 100).toInt()}%",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { uploadProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = FiksPrimary,
                        trackColor = FiksScoreCard,
                        strokeCap = StrokeCap.Round
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // TAMPILAN PRATINJAU JIKA SUDAH DIUNGGAH
            if (submission != null && submission.url.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.dp, FiksCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Status Badge: "Sudah dikumpulkan"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.testTag("design_task_status_submitted")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = FiksScoreGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Sudah dikumpulkan",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = FiksScoreGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Pratinjau Gambar yang Diunggah
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FiksBg)
                                .testTag("design_task_image_preview"),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = submission.url,
                                contentDescription = "Pratinjau Tugas Desain",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tombol Unggah Ulang (menimpa)
                        FiksCompactButton(
                            text = "Unggah Ulang",
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            isSecondary = true,
                            leadingIcon = Icons.Default.Refresh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("design_task_reupload_button")
                        )
                    }
                }
            } else if (!isUploading) {
                // TAMPILAN TOMBOL UNGGAH DESAIN PERTAMA KALI
                FiksButton(
                    text = "Unggah Desain",
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    leadingIcon = Icons.Default.AddPhotoAlternate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("design_task_upload_button")
                )
            }
        }
    }
}
