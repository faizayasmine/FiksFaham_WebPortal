package com.example.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FiksButton
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    onResetPasswordSubmit: (suspend (email: String) -> Result<Unit>)? = null,
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Smooth card entrance slide-up
    val slideUpAnim = remember { Animatable(30f) }
    LaunchedEffect(Unit) {
        slideUpAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .testTag("forgot_password_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header area with Overlapping Card Anchor
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                AuthHeaderBackground(
                    height = 230.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AuthHeaderContent(
                        title = "Atur Ulang Kata Sandi",
                        subtitle = "Kami akan membantumu memulihkan akun",
                        backButtonText = "Kembali ke Masuk",
                        onBackClick = onNavigateBackToLogin,
                        modifier = Modifier.padding(bottom = 26.dp)
                    )

                    // Overlapping Form Card
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = FiksScoreCard,
                        border = BorderStroke(1.dp, FiksCardBorder),
                        shadowElevation = 5.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 480.dp)
                            .padding(horizontal = 20.dp)
                            .graphicsLayer {
                                translationY = slideUpAnim.value
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 28.dp)
                        ) {
                            AnimatedContent(
                                targetState = isSubmitted,
                                label = "forgot_password_state_transition"
                            ) { submitted ->
                                if (!submitted) {
                                    Column {
                                        Text(
                                            text = "Lupa Kata Sandi?",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            color = FiksText,
                                            modifier = Modifier.testTag("forgot_password_title")
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "Masukkan alamat email yang terdaftar. Kami akan mengirimkan tautan untuk mengatur ulang kata sandi kamu.",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 13.sp,
                                            color = FiksTextMuted,
                                            lineHeight = 20.sp
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        AuthTextField(
                                            value = email,
                                            onValueChange = {
                                                email = it
                                                errorMessage = null
                                            },
                                            label = "Email",
                                            placeholder = "nama@kampus.ac.id",
                                            leadingIcon = Icons.Default.Email,
                                            testTag = "forgot_email_input"
                                        )

                                        if (errorMessage != null) {
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = FiksScoreRed.copy(alpha = 0.12f),
                                                border = BorderStroke(1.dp, FiksScoreRed.copy(alpha = 0.35f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null,
                                                        tint = FiksScoreRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = errorMessage!!,
                                                        fontFamily = PoppinsFontFamily,
                                                        fontSize = 12.sp,
                                                        color = FiksScoreRed
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(28.dp))

                                        FiksButton(
                                            text = if (isLoading) "Mengirim..." else "Kirim Tautan Atur Ulang Kata Sandi",
                                            enabled = !isLoading,
                                            onClick = {
                                                val trimmedEmail = email.trim()
                                                when {
                                                    trimmedEmail.isBlank() -> {
                                                        errorMessage = "Email wajib diisi!"
                                                    }
                                                    !trimmedEmail.contains("@") || !trimmedEmail.contains(".") -> {
                                                        errorMessage = "Format email tidak valid!"
                                                    }
                                                    else -> {
                                                        if (onResetPasswordSubmit != null) {
                                                            isLoading = true
                                                            errorMessage = null
                                                            coroutineScope.launch {
                                                                val res = onResetPasswordSubmit(trimmedEmail)
                                                                isLoading = false
                                                                if (res.isSuccess) {
                                                                    isSubmitted = true
                                                                } else {
                                                                    errorMessage = res.exceptionOrNull()?.message
                                                                        ?: "Gagal mengirim email reset kata sandi."
                                                                }
                                                            }
                                                        } else {
                                                            isSubmitted = true
                                                        }
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("submit_reset_button")
                                        )
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp)
                                            .testTag("forgot_password_confirmation")
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(CircleShape)
                                                .background(FiksScoreGreen.copy(alpha = 0.15f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Berhasil",
                                                tint = FiksScoreGreen,
                                                modifier = Modifier.size(34.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Text(
                                            text = "Tautan atur ulang kata sandi terkirim!",
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = FiksText,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 26.sp,
                                            modifier = Modifier.testTag("reset_sent_message")
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "Periksa kotak masuk atau folder spam di $email untuk melanjutkan proses atur ulang kata sandi.",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 13.sp,
                                            color = FiksTextMuted,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 20.sp
                                        )

                                        Spacer(modifier = Modifier.height(30.dp))

                                        FiksButton(
                                            text = "Kembali ke Masuk",
                                            onClick = onNavigateBackToLogin,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("back_to_login_button")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
