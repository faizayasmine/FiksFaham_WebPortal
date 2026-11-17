package com.example.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AssessmentAttemptEntity
import com.example.data.model.AssessmentEntity
import com.example.data.model.CapstoneSubmissionEntity
import com.example.data.model.MateriEntity
import com.example.data.model.ModuleEntity
import com.example.data.model.ModuleProgressEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserPointsBreakdown
import com.example.ui.AppLanguage
import com.example.ui.components.FiksButton
import com.example.ui.components.PointBadge
import com.example.ui.components.bounceClickEffect
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksAccentLight
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

// Data model for profile avatar presets
data class AvatarOption(
    val id: String,
    val labelId: String,
    val labelEn: String,
    val gradientColors: List<Color>,
    val icon: ImageVector,
    val badgeEmoji: String
)

val AVATAR_OPTIONS = listOf(
    AvatarOption("avatar_1", "Desainer Fisha", "Fisha Designer", listOf(Color(0xFFE25A20), Color(0xFFF8B259)), Icons.Default.Person, "🎨"),
    AvatarOption("avatar_2", "UX Researcher", "UX Researcher", listOf(Color(0xFF238636), Color(0xFF56B368)), Icons.Default.Psychology, "🔍"),
    AvatarOption("avatar_3", "Visual Creative", "Visual Creative", listOf(Color(0xFF8E24AA), Color(0xFFBA68C8)), Icons.Default.Palette, "✨"),
    AvatarOption("avatar_4", "Product Lead", "Product Lead", listOf(Color(0xFF0288D1), Color(0xFF4FC3F7)), Icons.Default.Star, "🚀"),
    AvatarOption("avatar_5", "Akademisi UNESA", "UNESA Scholar", listOf(Color(0xFFD81B60), Color(0xFFFF80AB)), Icons.Default.School, "🎓"),
    AvatarOption("avatar_6", "Design Engineer", "Design Engineer", listOf(Color(0xFFE65100), Color(0xFFFFB74D)), Icons.Default.Face, "💻")
)

fun getAvatarOption(avatarId: String?): AvatarOption {
    return AVATAR_OPTIONS.firstOrNull { it.id == avatarId } ?: AVATAR_OPTIONS[0]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: UserEntity?,
    progressPercent: Int = 0,
    capstoneSubmission: CapstoneSubmissionEntity? = null,
    modules: List<ModuleEntity> = emptyList(),
    progressList: List<ModuleProgressEntity> = emptyList(),
    materis: List<MateriEntity> = emptyList(),
    allAttempts: List<AssessmentAttemptEntity> = emptyList(),
    assessments: List<AssessmentEntity> = emptyList(),
    userPoints: UserPointsBreakdown = UserPointsBreakdown(),
    userMonthlyCoins: Int = 0,
    userRank: Int = 1,
    isDarkMode: Boolean = false,
    appLanguage: AppLanguage = AppLanguage.INDONESIAN,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onSelectLanguage: (AppLanguage) -> Unit = {},
    onUpdateProfile: (name: String, instansi: String, email: String, avatarUrl: String?, photoUri: Uri?) -> Unit = { _, _, _, _, _ -> },
    onUpdatePassword: (newPassword: String) -> Unit = {},
    onVerifyEmailChange: (suspend (newEmail: String, password: String?) -> Result<Unit>)? = null,
    onNavigateToCapstone: () -> Unit = {},
    onNavigateToCourse: (moduleId: Int?) -> Unit = {},
    onNavigateToLeaderboard: () -> Unit = {},
    onNavigateToSavedModules: () -> Unit = {},
    onNavigateToCertificates: () -> Unit = {},
    onNavigateToAccessibility: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToGallery: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPosttestDetailDialog by remember { mutableStateOf(false) }
    var successNotice by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val isIndonesian = appLanguage == AppLanguage.INDONESIAN

    // Rekap Skor Evaluasi (Hanya Post-Test: skor terakhir di kartu + rincian per materi saat diklik)
    val posttestRecap = remember(allAttempts, assessments, materis) {
        ProfileRecapUtils.calculatePosttestRecap(allAttempts, assessments, materis)
    }

    // =========================================================================
    // DIALOGS
    // =========================================================================

    // 1. Edit Profile Modal (Username, Email read-only, Avatar Preset, Kamera & Galeri)
    if (showEditDialog && user != null) {
        var editName by remember { mutableStateOf(user.nama) }
        var selectedAvatarId by remember { mutableStateOf(if (user.avatarUrl.isNotBlank()) user.avatarUrl else "avatar_1") }
        var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
        var isCameraPhoto by remember { mutableStateOf(false) }
        var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        // Android Gallery Photo Picker
        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                selectedPhotoUri = uri
                isCameraPhoto = false
            }
        }

        // Camera Photo Capture Launcher
        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && pendingCameraUri != null) {
                selectedPhotoUri = pendingCameraUri
                isCameraPhoto = true
            }
        }

        BasicAlertDialog(onDismissRequest = { showEditDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = FiksScoreCard,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_profile_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isIndonesian) "Edit Profil" else "Edit Profile",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = FiksText
                        )
                        IconButton(
                            onClick = { showEditDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isIndonesian) "Tutup" else "Close",
                                tint = FiksTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION A: Pilih Foto Profil (Avatar Gallery & Device Picker)
                    Text(
                        text = if (isIndonesian) "Pilih Foto Profil / Avatar" else "Choose Profile Photo / Avatar",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Preview
                    val activeOption = getAvatarOption(selectedAvatarId)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FiksScoreCard,
                            border = BorderStroke(2.5.dp, FiksPrimary),
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(64.dp)
                        ) {
                            if (selectedPhotoUri != null) {
                                AsyncImage(
                                    model = selectedPhotoUri,
                                    contentDescription = "Preview Foto",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(activeOption.gradientColors))
                                ) {
                                    Icon(
                                        imageVector = activeOption.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedPhotoUri != null) {
                                    if (isCameraPhoto) {
                                        stringResource(R.string.profile_photo_camera_selected)
                                    } else {
                                        stringResource(R.string.profile_photo_gallery_selected)
                                    }
                                } else {
                                    if (isIndonesian) activeOption.labelId else activeOption.labelEn
                                },
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = FiksPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isIndonesian) "Akan tersinkron ke profil akun" else "Will sync to your account profile",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Camera & Gallery buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val photoDir = File(context.cacheDir, "profile_photos").apply { mkdirs() }
                                val photoFile = File(photoDir, "avatar_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                pendingCameraUri = uri
                                cameraLauncher.launch(uri)
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            border = BorderStroke(1.dp, FiksPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_camera_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.profile_camera_btn),
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            border = BorderStroke(1.dp, FiksPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_gallery_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.profile_gallery_btn),
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Avatars Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AVATAR_OPTIONS.forEach { opt ->
                            val isSelected = selectedPhotoUri == null && opt.id == selectedAvatarId
                            Surface(
                                shape = CircleShape,
                                color = FiksScoreCard,
                                border = BorderStroke(
                                    if (isSelected) 2.5.dp else 1.dp,
                                    if (isSelected) FiksPrimary else FiksCardBorder
                                ),
                                shadowElevation = if (isSelected) 4.dp else 1.dp,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        selectedPhotoUri = null
                                        selectedAvatarId = opt.id
                                    }
                                    .testTag("avatar_option_${opt.id}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(opt.gradientColors))
                                ) {
                                    Icon(
                                        imageVector = opt.icon,
                                        contentDescription = opt.labelId,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(Color.Black.copy(alpha = 0.2f))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // SECTION B: Input Username / Nama
                    Text(
                        text = stringResource(R.string.profile_username_label),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FiksPrimary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder,
                            focusedContainerColor = FiksBg,
                            unfocusedContainerColor = FiksBg
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION C: Email (Read-Only)
                    Text(
                        text = stringResource(R.string.auth_field_email),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = user.email,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = FiksTextMuted)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_email_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = FiksTextMuted,
                            disabledBorderColor = FiksCardBorder,
                            disabledContainerColor = FiksBg.copy(alpha = 0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.profile_email_readonly_hint),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 10.sp,
                        color = FiksTextMuted
                    )

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMsg!!,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksScoreRed
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    FiksButton(
                        text = if (isIndonesian) "Simpan Perubahan" else "Save Changes",
                        onClick = {
                            if (editName.isBlank()) {
                                errorMsg = if (isIndonesian) "Username wajib diisi!" else "Username is required!"
                            } else {
                                val targetAvatar = if (selectedPhotoUri != null) null else selectedAvatarId
                                onUpdateProfile(editName.trim(), user.asalInstansi, user.email, targetAvatar, selectedPhotoUri)
                                showEditDialog = false
                                successNotice = if (isIndonesian) "Profil dan foto berhasil diperbarui!" else "Profile and photo updated successfully!"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_profile_button")
                    )
                }
            }
        }
    }

    // 2. Change Password Modal
    if (showPasswordDialog) {
        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var oldPassVisible by remember { mutableStateOf(false) }
        var newPassVisible by remember { mutableStateOf(false) }
        var confirmPassVisible by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        BasicAlertDialog(onDismissRequest = { showPasswordDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = FiksScoreCard,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isIndonesian) "Ubah Kata Sandi" else "Change Password",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = FiksText
                        )
                        IconButton(
                            onClick = { showPasswordDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isIndonesian) "Tutup" else "Close",
                                tint = FiksTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isIndonesian) "Kata Sandi Sekarang" else "Current Password",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = {
                            oldPass = it
                            errorMsg = null
                        },
                        singleLine = true,
                        visualTransformation = if (oldPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { oldPassVisible = !oldPassVisible }) {
                                Icon(
                                    imageVector = if (oldPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = FiksTextMuted
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("old_password_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder,
                            focusedContainerColor = FiksBg,
                            unfocusedContainerColor = FiksBg
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isIndonesian) "Kata Sandi Baru" else "New Password",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = {
                            newPass = it
                            errorMsg = null
                        },
                        singleLine = true,
                        visualTransformation = if (newPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPassVisible = !newPassVisible }) {
                                Icon(
                                    imageVector = if (newPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = FiksTextMuted
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_password_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder,
                            focusedContainerColor = FiksBg,
                            unfocusedContainerColor = FiksBg
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isIndonesian) "Konfirmasi Kata Sandi Baru" else "Confirm New Password",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = {
                            confirmPass = it
                            errorMsg = null
                        },
                        singleLine = true,
                        visualTransformation = if (confirmPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPassVisible = !confirmPassVisible }) {
                                Icon(
                                    imageVector = if (confirmPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = FiksTextMuted
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_password_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder,
                            focusedContainerColor = FiksBg,
                            unfocusedContainerColor = FiksBg
                        )
                    )

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FiksScoreRed.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMsg!!,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = FiksScoreRed,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    FiksButton(
                        text = if (isIndonesian) "Simpan Kata Sandi" else "Save Password",
                        onClick = {
                            when {
                                newPass.length < 6 -> {
                                    errorMsg = if (isIndonesian) "Kata sandi baru minimal 6 karakter!" else "New password must be at least 6 characters!"
                                }
                                newPass != confirmPass -> {
                                    errorMsg = if (isIndonesian) "Kata sandi baru dan konfirmasi tidak cocok!" else "New passwords do not match!"
                                }
                                else -> {
                                    onUpdatePassword(newPass)
                                    showPasswordDialog = false
                                    successNotice = if (isIndonesian) "Kata sandi akun berhasil diperbarui!" else "Account password updated successfully!"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_password_button")
                    )
                }
            }
        }
    }

    // 3. Language Selector Dialog
    if (showLanguageDialog) {
        BasicAlertDialog(onDismissRequest = { showLanguageDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = FiksScoreCard,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("language_dialog")
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isIndonesian) "Pilih Bahasa Aplikasi" else "Select App Language",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = FiksText
                        )
                        IconButton(
                            onClick = { showLanguageDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = FiksTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LanguageOptionRow(
                        flag = "🇮🇩",
                        title = "Bahasa Indonesia",
                        subtitle = "Bahasa utama aplikasi FiksFaham",
                        isSelected = appLanguage == AppLanguage.INDONESIAN,
                        onClick = {
                            onSelectLanguage(AppLanguage.INDONESIAN)
                            showLanguageDialog = false
                            successNotice = "Bahasa diubah ke Bahasa Indonesia"
                        },
                        testTag = "lang_option_id"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LanguageOptionRow(
                        flag = "🇬🇧",
                        title = "English",
                        subtitle = "Default international language",
                        isSelected = appLanguage == AppLanguage.ENGLISH,
                        onClick = {
                            onSelectLanguage(AppLanguage.ENGLISH)
                            showLanguageDialog = false
                            successNotice = "Language changed to English"
                        },
                        testTag = "lang_option_en"
                    )
                }
            }
        }
    }

    // 4. Logout Confirmation Dialog (Friendly)
    if (showLogoutDialog) {
        BasicAlertDialog(onDismissRequest = { showLogoutDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = FiksScoreCard,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout_confirmation_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = FiksPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = FiksPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.profile_logout_dialog_title),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FiksText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.profile_logout_dialog_body),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = FiksTextMuted,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showLogoutDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, FiksPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.profile_logout_cancel),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = FiksPrimary
                            )
                        }
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                onLogout()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FiksScoreRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.profile_logout_confirm),
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

    // 5. Post-Test Detail Modal (muncul saat kartu skor Post-Test terakhir diklik)
    if (showPosttestDetailDialog) {
        BasicAlertDialog(onDismissRequest = { showPosttestDetailDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = FiksScoreCard,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("posttest_detail_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.profile_recap_posttest_modal_title),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = FiksText
                        )
                        IconButton(
                            onClick = { showPosttestDetailDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = FiksTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (posttestRecap.entries.isEmpty()) {
                        Text(
                            text = stringResource(R.string.profile_recap_posttest_empty),
                            fontFamily = PoppinsFontFamily,
                            fontSize = 13.sp,
                            color = FiksTextMuted
                        )
                    } else {
                        posttestRecap.entries.forEachIndexed { idx, entry ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FiksBg,
                                border = BorderStroke(1.dp, FiksCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.materiTitle,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = FiksText,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (entry.latestScore != null && entry.bestScore != null) {
                                            Text(
                                                text = "${stringResource(R.string.profile_recap_posttest_latest, entry.latestScore)} • ${stringResource(R.string.profile_recap_posttest_best, entry.bestScore)}",
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                color = FiksTextMuted
                                            )
                                        } else {
                                            Text(
                                                text = stringResource(R.string.profile_recap_posttest_empty),
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                color = FiksTextMuted
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (entry.isPassed) FiksScoreGreen.copy(alpha = 0.14f) else FiksScoreCard,
                                        border = BorderStroke(
                                            1.dp,
                                            if (entry.isPassed) FiksScoreGreen.copy(alpha = 0.4f) else FiksCardBorder
                                        )
                                    ) {
                                        Text(
                                            text = if (entry.latestScore == null) "-"
                                            else if (entry.isPassed) stringResource(R.string.profile_recap_posttest_passed)
                                            else stringResource(R.string.profile_recap_posttest_not_passed),
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp,
                                            color = if (entry.isPassed) FiksScoreGreen else FiksTextMuted,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            if (idx < posttestRecap.entries.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // MAIN SCREEN CONTENT
    // =========================================================================
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .testTag("profile_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // -------------------------------------------------------------
            // 1. CURVED GRADIENT HEADER BANNER (Diperbesar & tanpa chip "Profil Akun")
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(232.dp)
                    .clip(RoundedCornerShape(bottomStart = 42.dp, bottomEnd = 42.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                FiksPrimaryDark,
                                FiksPrimary,
                                FiksAccent
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Background decorative circles
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = size.width * 0.35f,
                        center = Offset(size.width * 0.9f, size.height * 0.2f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = size.width * 0.22f,
                        center = Offset(size.width * 0.1f, size.height * 0.8f)
                    )
                }

                // Top action bar: tombol Edit Profil di pojok kanan atas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .bounceClickEffect()
                            .clickable { showEditDialog = true }
                            .testTag("profile_top_edit_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = if (isIndonesian) "Ubah Profil" else "Edit Profile",
                                tint = FiksPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 2. HEADER INFO: NAMA, ASAL INSTANSI, FOTO, PERINGKAT LEADERBOARD
            // (Sub-point a)
            // -------------------------------------------------------------
            val userAvatar = getAvatarOption(user?.avatarUrl)
            val hasCustomPhoto = !user?.avatarUrl.isNullOrBlank() && (
                user!!.avatarUrl.startsWith("http") ||
                user.avatarUrl.startsWith("content:") ||
                user.avatarUrl.startsWith("file:")
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-52).dp)
                    .padding(horizontal = 24.dp)
            ) {
                // Foto / Avatar Circle
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.size(104.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = FiksScoreCard,
                        border = BorderStroke(4.dp, Color.White),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(104.dp)
                            .testTag("profile_avatar_circle")
                    ) {
                        if (hasCustomPhoto) {
                            AsyncImage(
                                model = user?.avatarUrl,
                                contentDescription = "Foto Profil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(userAvatar.gradientColors))
                            ) {
                                Icon(
                                    imageVector = userAvatar.icon,
                                    contentDescription = "Foto Profil",
                                    tint = Color.White,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }
                    }

                    // Edit camera badge
                    Surface(
                        shape = CircleShape,
                        color = FiksAccent,
                        border = BorderStroke(2.dp, Color.White),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .bounceClickEffect()
                            .clickable { showEditDialog = true }
                            .testTag("profile_avatar_camera_badge")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = if (isIndonesian) "Ganti Foto" else "Change Photo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Nama Lengkap
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = user?.nama ?: "Pengguna FiksFaham",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = FiksText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("profile_user_greeting")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FiksPrimary.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, FiksPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("profile_user_level_badge")
                    ) {
                        Text(
                            text = "Lv. ${user?.level ?: 0}",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = FiksPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Email langsung di bawah username
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user?.email ?: "-",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = FiksTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("profile_user_email")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Header Badges: Leaderboard Rank, Role, and Total Progress
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Peringkat Leaderboard (Required by Requirement a)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = FiksAccent.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, FiksAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("profile_header_rank_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Leaderboard",
                                tint = FiksPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (isIndonesian) "Peringkat" else "Rank"} #$userRank",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFE65100)
                            )
                        }
                    }

                    // Role Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = FiksScoreGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, FiksScoreGreen.copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            val roleLower = user?.role?.trim()?.lowercase()
                            val isSuperAdmin = roleLower == "superadmin"
                            val isMentorOrFac = roleLower == "mentor" || roleLower == "fasilitator"
                            val isStaff = isSuperAdmin || isMentorOrFac
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = if (isStaff) Color(0xFF6A1B9A) else FiksScoreGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when {
                                    isSuperAdmin -> "Super Admin"
                                    roleLower == "fasilitator" -> if (isIndonesian) "Fasilitator UI/UX" else "UI/UX Facilitator"
                                    isMentorOrFac -> if (isIndonesian) "Mentor UI/UX" else "UI/UX Mentor"
                                    else -> if (isIndonesian) "Mahasiswa UI/UX" else "UI/UX Student"
                                },
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = if (isStaff) Color(0xFF6A1B9A) else FiksScoreGreen
                            )
                        }
                    }

                    // Progress Belajar Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = FiksPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, FiksPrimary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "${if (isIndonesian) "Progres" else "Progress"}: $progressPercent%",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = FiksPrimaryDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 3. BODY CONTENT SECTIONS
            // -------------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .padding(horizontal = 20.dp)
            ) {
                // Success banner
                AnimatedVisibility(visible = successNotice != null) {
                    successNotice?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FiksScoreGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, FiksScoreGreen.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = FiksScoreGreen
                                )
                                Text(
                                    text = msg,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = FiksScoreGreen
                                )
                            }
                        }
                    }
                }

                // =========================================================
                // REKAP SKOR EVALUASI (Post-Test Terakhir -> Klik Detail Lengkap)
                // =========================================================
                CategoryTitle(
                    title = stringResource(R.string.profile_recap_posttest_title),
                    subtitle = stringResource(R.string.profile_recap_posttest_subtitle)
                )

                val latestPosttest = posttestRecap.latestEntry
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.dp, FiksCardBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClickEffect()
                        .clickable { showPosttestDetailDialog = true }
                        .testTag("assessment_recap_section")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("recap_posttest_score"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FiksScoreGreen.copy(alpha = 0.14f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = FiksScoreGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.profile_recap_posttest_latest_label),
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksTextMuted
                            )
                            Text(
                                text = latestPosttest?.latestScore?.let { "$it/100" }
                                    ?: stringResource(R.string.profile_recap_posttest_empty),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = if (latestPosttest?.latestScore != null) FiksScoreGreen else FiksTextMuted
                            )
                            Text(
                                text = latestPosttest?.materiTitle
                                    ?: stringResource(R.string.profile_recap_posttest_tap_hint),
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = FiksTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =========================================================
                // SEKSI LEADERBOARD
                // =========================================================
                CategoryTitle(
                    title = if (isIndonesian) "Papan Peringkat Mahasiswa" else "Student Leaderboard",
                    subtitle = stringResource(R.string.profile_leaderboard_sub)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.dp, FiksCardBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClickEffect()
                        .clickable(onClick = onNavigateToLeaderboard)
                        .testTag("menu_leaderboard_profile")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FiksAccentLight,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Leaderboard",
                                    tint = FiksPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${if (isIndonesian) "Peringkat" else "Rank"} #$userRank",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = FiksText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                PointBadge(
                                    points = userMonthlyCoins,
                                    fontSize = 10.sp,
                                    coinSize = 13.dp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.profile_leaderboard_sub),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = FiksTextMuted
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Buka Leaderboard",
                            tint = FiksTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =========================================================
                // KELOMPOK 1: PENCAPAIAN BELAJAR
                // =========================================================
                CategoryTitle(
                    title = stringResource(R.string.profile_section_achievements)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.dp, FiksCardBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("achievements_menu_card")
                ) {
                    Column {
                        ProfileMenuItemRow(
                            icon = Icons.Default.Bookmark,
                            label = stringResource(R.string.profile_menu_saved_modules),
                            subtitle = stringResource(R.string.profile_menu_saved_modules_sub),
                            onClick = onNavigateToSavedModules,
                            testTag = "menu_saved_modules"
                        )

                        HorizontalDivider(
                            color = FiksCardBorder.copy(alpha = 0.7f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        ProfileMenuItemRow(
                            icon = Icons.Default.Verified,
                            label = stringResource(R.string.profile_menu_certificates),
                            subtitle = stringResource(R.string.profile_menu_certificates_sub),
                            onClick = onNavigateToCertificates,
                            testTag = "placeholder_row_certificate"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =========================================================
                // KELOMPOK 2: PENGATURAN LANJUTAN
                // =========================================================
                CategoryTitle(
                    title = stringResource(R.string.profile_section_advanced)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FiksScoreCard,
                    border = BorderStroke(1.dp, FiksCardBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("appearance_card")
                ) {
                    Column {
                        // Bahasa Aplikasi
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLanguageDialog = true }
                                .padding(16.dp)
                                .testTag("language_preference_row"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = FiksPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Language",
                                        tint = FiksPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.profile_menu_language),
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = FiksText
                                )
                                Text(
                                    text = if (isIndonesian) "Indonesia (ID)" else "English (EN)",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = FiksTextMuted
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FiksBg,
                                border = BorderStroke(1.dp, FiksCardBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isIndonesian) "🇮🇩 ID" else "🇬🇧 EN",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = FiksPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = FiksTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = FiksCardBorder.copy(alpha = 0.7f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Mode Gelap dan Terang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isDarkMode) Color(0xFF38302A) else FiksAccent.copy(alpha = 0.18f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = "Theme Icon",
                                        tint = if (isDarkMode) Color(0xFFF8B259) else FiksPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.profile_menu_theme),
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = FiksText
                                )
                                Text(
                                    text = if (isDarkMode)
                                        (if (isIndonesian) "Mode Gelap aktif" else "Dark Mode active")
                                    else
                                        (if (isIndonesian) "Mode Terang aktif" else "Light Mode active"),
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = FiksTextMuted
                                )
                            }

                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = onToggleDarkMode,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FiksPrimary,
                                    uncheckedThumbColor = FiksPrimary,
                                    uncheckedTrackColor = FiksCardBorder
                                ),
                                modifier = Modifier.testTag("dark_mode_toggle")
                            )
                        }

                        HorizontalDivider(
                            color = FiksCardBorder.copy(alpha = 0.7f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Keamanan Akun
                        ProfileMenuItemRow(
                            icon = Icons.Default.Security,
                            label = stringResource(R.string.profile_menu_security),
                            subtitle = stringResource(R.string.profile_menu_security_sub),
                            onClick = onNavigateToSecurity,
                            testTag = "placeholder_row_security"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // =========================================================
                // TOMBOL KELUAR AKUN (Desain mencolok & berbeda)
                // =========================================================
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = FiksScoreRed.copy(alpha = 0.08f),
                    border = BorderStroke(1.2.dp, FiksScoreRed.copy(alpha = 0.4f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .bounceClickEffect()
                        .clickable { showLogoutDialog = true }
                        .testTag("logout_button")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FiksScoreRed,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = stringResource(R.string.profile_logout_btn),
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.profile_logout_btn),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = FiksScoreRed
                            )
                            Text(
                                text = stringResource(R.string.profile_logout_sub),
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = FiksTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FiksScoreRed.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = stringResource(R.string.profile_logout_confirm),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = FiksScoreRed,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // App Logo & Version Stamp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "Logo FiksFaham",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FiksFaham • v${com.example.BuildConfig.VERSION_NAME}",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = FiksTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTitle(
    title: String,
    subtitle: String? = null
) {
    Column(modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)) {
        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = FiksText
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = FiksTextMuted,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ProfileMenuItemRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = FiksPrimary.copy(alpha = 0.12f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = FiksPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = FiksText
            )
            Text(
                text = subtitle,
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = FiksTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = FiksTextMuted.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LanguageOptionRow(
    flag: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
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
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = flag, fontSize = 24.sp)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = FiksText
                )
                Text(
                    text = subtitle,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.sp,
                    color = FiksTextMuted
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Terpilih",
                    tint = FiksPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
