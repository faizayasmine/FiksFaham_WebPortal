package com.example.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.FiksButton
import com.example.ui.components.bounceClickEffect
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onChangePassword: suspend (oldPass: String, newPass: String) -> Result<Unit>,
    onLogout: () -> Unit,
    onDeleteAccount: suspend (password: String) -> Result<Unit>,
    onBack: () -> Unit,
    isGoogleOnlyAccount: Boolean = false,
    onDeleteAccountWithGoogle: (suspend (idToken: String) -> Result<Unit>)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // =========================================================================
    // DIALOG 1: UBAH KATA SANDI
    // =========================================================================
    if (showChangePasswordDialog) {
        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var oldPassVisible by remember { mutableStateOf(false) }
        var newPassVisible by remember { mutableStateOf(false) }
        var confirmPassVisible by remember { mutableStateOf(false) }
        var dialogError by remember { mutableStateOf<String?>(null) }
        var isSubmitting by remember { mutableStateOf(false) }

        BasicAlertDialog(onDismissRequest = { if (!isSubmitting) showChangePasswordDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("change_password_modal")
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
                            text = stringResource(R.string.security_change_password),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = FiksText
                        )
                        IconButton(
                            onClick = { showChangePasswordDialog = false },
                            enabled = !isSubmitting,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.common_close),
                                tint = FiksTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Input Sandi Lama
                    Text(
                        text = stringResource(R.string.security_old_password),
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
                            dialogError = null
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
                            .testTag("security_old_password_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder,
                            focusedContainerColor = FiksBg,
                            unfocusedContainerColor = FiksBg
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Sandi Baru
                    Text(
                        text = stringResource(R.string.security_new_password),
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
                            dialogError = null
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
                            .testTag("security_new_password_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder,
                            focusedContainerColor = FiksBg,
                            unfocusedContainerColor = FiksBg
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Konfirmasi Sandi Baru
                    Text(
                        text = stringResource(R.string.security_confirm_new_password),
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
                            dialogError = null
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
                            .testTag("security_confirm_password_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksPrimary,
                            unfocusedBorderColor = FiksCardBorder,
                            focusedContainerColor = FiksBg,
                            unfocusedContainerColor = FiksBg
                        )
                    )

                    if (dialogError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = dialogError!!,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksScoreRed
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    FiksButton(
                        text = if (isSubmitting) stringResource(R.string.common_loading) else stringResource(R.string.security_btn_save_password),
                        enabled = !isSubmitting,
                        onClick = {
                            when {
                                oldPass.isBlank() -> {
                                    dialogError = "Harap masukkan kata sandi lama."
                                }
                                newPass.length < 6 -> {
                                    dialogError = "Kata sandi baru minimal 6 karakter!"
                                }
                                newPass != confirmPass -> {
                                    dialogError = "Kata sandi baru dan konfirmasi tidak cocok!"
                                }
                                else -> {
                                    isSubmitting = true
                                    coroutineScope.launch {
                                        val result = onChangePassword(oldPass, newPass)
                                        isSubmitting = false
                                        if (result.isSuccess) {
                                            showChangePasswordDialog = false
                                            successMessage = "Kata sandi akun berhasil diperbarui!"
                                        } else {
                                            dialogError = result.exceptionOrNull()?.message ?: "Gagal memperbarui kata sandi."
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("security_submit_password_button")
                    )
                }
            }
        }
    }

    // =========================================================================
    // DIALOG 2: KELUAR AKUN
    // =========================================================================
    if (showLogoutDialog) {
        BasicAlertDialog(onDismissRequest = { showLogoutDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("security_logout_dialog")
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = stringResource(R.string.security_dialog_logout_title),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.security_dialog_logout_desc),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = FiksTextMuted,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showLogoutDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.common_cancel),
                                fontFamily = PoppinsFontFamily,
                                color = FiksText
                            )
                        }
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                onLogout()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FiksScoreRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("security_confirm_logout_button")
                        ) {
                            Text(
                                text = stringResource(R.string.security_sign_out),
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG 3: HAPUS AKUN (Double Confirmation)
    // =========================================================================
    if (showDeleteAccountDialog) {
        var confirmKeyword by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        var isPasswordVisible by remember { mutableStateOf(false) }
        var isDeleting by remember { mutableStateOf(false) }
        var deleteError by remember { mutableStateOf<String?>(null) }

        BasicAlertDialog(onDismissRequest = { if (!isDeleting) showDeleteAccountDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksScoreRed.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_account_modal")
            ) {
                Column(
                    modifier = Modifier
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = FiksScoreRed,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = stringResource(R.string.security_dialog_delete_title),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = FiksScoreRed
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.security_dialog_delete_warning),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksTextMuted,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Double confirmation step 1: Ketik kata "HAPUS"
                    Text(
                        text = stringResource(R.string.security_delete_confirm_prompt),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = FiksText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = confirmKeyword,
                        onValueChange = {
                            confirmKeyword = it
                            deleteError = null
                        },
                        placeholder = { Text("HAPUS", color = FiksTextMuted.copy(alpha = 0.6f)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_keyword_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FiksScoreRed,
                            unfocusedBorderColor = FiksCardBorder,
                            focusedContainerColor = FiksBg,
                            unfocusedContainerColor = FiksBg
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Double confirmation step 2: Password atau Google Re-auth
                    if (isGoogleOnlyAccount && onDeleteAccountWithGoogle != null) {
                        Text(
                            text = "Akun Google terdeteksi. Saat menekan Hapus Akun, Anda akan diminta verifikasi ulang melalui Google.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksTextMuted,
                            lineHeight = 17.sp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.security_delete_enter_password),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = FiksText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                deleteError = null
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = FiksTextMuted
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delete_password_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FiksScoreRed,
                                unfocusedBorderColor = FiksCardBorder,
                                focusedContainerColor = FiksBg,
                                unfocusedContainerColor = FiksBg
                            )
                        )
                    }

                    if (deleteError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = deleteError!!,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = FiksScoreRed
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteAccountDialog = false },
                            enabled = !isDeleting,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.common_cancel),
                                fontFamily = PoppinsFontFamily,
                                color = FiksText
                            )
                        }

                        Button(
                            onClick = {
                                if (!confirmKeyword.equals("HAPUS", ignoreCase = true)) {
                                    deleteError = "Ketik kata 'HAPUS' dengan tepat untuk konfirmasi."
                                } else if (isGoogleOnlyAccount && onDeleteAccountWithGoogle != null) {
                                    isDeleting = true
                                    coroutineScope.launch {
                                        val tokenRes = com.example.ui.auth.requestGoogleIdToken(context)
                                        val result = tokenRes.fold(
                                            onSuccess = { idToken -> onDeleteAccountWithGoogle(idToken) },
                                            onFailure = { err -> Result.failure(err) }
                                        )
                                        isDeleting = false
                                        if (result.isSuccess) {
                                            showDeleteAccountDialog = false
                                            onLogout()
                                        } else {
                                            deleteError = result.exceptionOrNull()?.message ?: "Verifikasi Google untuk hapus akun gagal."
                                        }
                                    }
                                } else if (passwordInput.isBlank()) {
                                    deleteError = "Kata sandi wajib diisi untuk verifikasi keamanan."
                                } else {
                                    isDeleting = true
                                    coroutineScope.launch {
                                        val result = onDeleteAccount(passwordInput)
                                        isDeleting = false
                                        if (result.isSuccess) {
                                            showDeleteAccountDialog = false
                                            onLogout()
                                        } else {
                                            deleteError = result.exceptionOrNull()?.message ?: "Gagal menghapus akun."
                                        }
                                    }
                                }
                            },
                            enabled = !isDeleting,
                            colors = ButtonDefaults.buttonColors(containerColor = FiksScoreRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_delete_account_button")
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.security_delete_account),
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // MAIN CONTENT
    // =========================================================================
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .statusBarsPadding()
            .testTag("security_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("security_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = FiksText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.security_title),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FiksText
                    )
                    Text(
                        text = stringResource(R.string.security_subtitle),
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = FiksTextMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            // Success Notice Banner
            AnimatedVisibility(visible = successMessage != null) {
                successMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FiksScoreGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, FiksScoreGreen.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = FiksScoreGreen)
                            Text(
                                text = msg,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = FiksScoreGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action 1: Ubah Kata Sandi
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClickEffect()
                    .clickable { showChangePasswordDialog = true }
                    .testTag("action_change_password")
            ) {
                SecurityActionRow(
                    icon = Icons.Default.LockReset,
                    iconTint = FiksPrimary,
                    title = stringResource(R.string.security_change_password),
                    subtitle = stringResource(R.string.security_change_password_desc),
                    badgeText = "Update"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action 2: Keluar Akun
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FiksScoreCard,
                border = BorderStroke(1.dp, FiksCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClickEffect()
                    .clickable { showLogoutDialog = true }
                    .testTag("action_sign_out")
            ) {
                SecurityActionRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    iconTint = Color(0xFFF57C00),
                    title = stringResource(R.string.security_sign_out),
                    subtitle = stringResource(R.string.security_sign_out_desc),
                    badgeText = "Sign Out"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action 3: Hapus Akun Secara Permanen
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FiksScoreRed.copy(alpha = 0.06f),
                border = BorderStroke(1.dp, FiksScoreRed.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClickEffect()
                    .clickable { showDeleteAccountDialog = true }
                    .testTag("action_delete_account")
            ) {
                SecurityActionRow(
                    icon = Icons.Default.DeleteForever,
                    iconTint = FiksScoreRed,
                    title = stringResource(R.string.security_delete_account),
                    subtitle = stringResource(R.string.security_delete_account_desc),
                    badgeText = "Permanent Danger",
                    isDestructive = true
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SecurityActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String,
    isDestructive: Boolean = false
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
            color = iconTint.copy(alpha = 0.14f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isDestructive) FiksScoreRed else FiksText
            )
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
