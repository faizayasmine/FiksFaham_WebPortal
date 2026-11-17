package com.example.ui.auth

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.bounceClickEffect
import com.example.ui.theme.FiksAccent
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksPrimaryDark
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreGreen
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.LocalFiksColors
import com.example.ui.theme.PoppinsFontFamily
import com.example.util.AuthValidationUtils
import android.content.Context
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Modern Header Background for Auth Screens
 */
@Composable
fun AuthHeaderBackground(
    height: Dp = 230.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        FiksPrimaryDark,
                        FiksPrimary,
                        FiksAccent.copy(alpha = 0.90f)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Top-right soft glowing orb
            drawCircle(
                color = Color.White.copy(alpha = 0.09f),
                radius = canvasWidth * 0.38f,
                center = Offset(canvasWidth * 0.92f, canvasHeight * 0.15f)
            )

            // Inner circle ring
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = canvasWidth * 0.52f,
                center = Offset(canvasWidth * 0.95f, canvasHeight * 0.10f)
            )

            // Bottom-left soft glow
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = canvasWidth * 0.28f,
                center = Offset(canvasWidth * 0.08f, canvasHeight * 0.95f)
            )
        }
    }
}

/**
 * Modern Header Content (Title, Subtitle, optional Back Button)
 */
@Composable
fun AuthHeaderContent(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    backButtonText: String? = null,
    onBackClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 28.dp)
    ) {
        // Optional Back button with label
        if (onBackClick != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBackClick
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("auth_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = backButtonText ?: "Kembali",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                if (backButtonText != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = backButtonText,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        } else {
            // Brand pill tag when no back button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FIKSFAHAM",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Main Greeting Title (e.g. "Hello!")
        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = Color.White,
            lineHeight = 38.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Subtitle
        Text(
            text = subtitle,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.90f),
            lineHeight = 20.sp
        )
    }
}


/**
 * Modern Consistent Auth TextField with labels, icons, and clear typography
 */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    testTag: String = ""
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = FiksText
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    color = FiksTextMuted.copy(alpha = 0.7f)
                )
            },
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = FiksPrimary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = trailingIcon,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FiksPrimary,
                unfocusedBorderColor = FiksCardBorder,
                focusedContainerColor = FiksScoreCard,
                unfocusedContainerColor = FiksScoreCard,
                focusedTextColor = FiksText,
                unfocusedTextColor = FiksText
            )
        )
    }
}

/**
 * Separator Line with text "Or login with"
 */
@Composable
fun AuthDivider(
    text: String = "Atau masuk dengan",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = FiksCardBorder,
            thickness = 1.dp
        )
        Text(
            text = text,
            fontFamily = PoppinsFontFamily,
            fontSize = 12.sp,
            color = FiksTextMuted,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = FiksCardBorder,
            thickness = 1.dp
        )
    }
}

/**
 * Social Auth Button (e.g. Google Login)
 */
@Composable
fun AuthGoogleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Lanjutkan dengan Google",
    testTag: String = "login_google_button"
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.dp, FiksCardBorder),
        shadowElevation = 0.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .bounceClickEffect()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 13.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google Logo",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = FiksText
            )
        }
    }
}

/**
 * Slide-up animation controller for Form Card
 */
@Composable
fun rememberSlideUpOffset(): Float {
    val animOffset = remember { Animatable(32f) }
    LaunchedEffect(Unit) {
        animOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        )
    }
    return animOffset.value
}

/**
 * Soft translucent circular gradient background shapes (2-3 corner blobs)
 * respecting FiksFaham theme colors without disturbing readability.
 */
@Composable
fun AuthSoftBackground(
    modifier: Modifier = Modifier
) {
    val primaryColor = FiksPrimary
    val accentColor = FiksAccent
    val isDark = LocalFiksColors.current.isDark

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Top-left soft translucent circle / ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = if (isDark) 0.16f else 0.12f),
                    primaryColor.copy(alpha = if (isDark) 0.05f else 0.04f),
                    Color.Transparent
                ),
                center = Offset(w * 0.10f, h * 0.08f),
                radius = w * 0.45f
            ),
            center = Offset(w * 0.10f, h * 0.08f),
            radius = w * 0.45f
        )
        drawCircle(
            color = primaryColor.copy(alpha = if (isDark) 0.10f else 0.08f),
            center = Offset(w * 0.02f, h * 0.05f),
            radius = w * 0.32f,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 24f)
        )

        // 2. Top-right soft accent sphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = if (isDark) 0.18f else 0.14f),
                    accentColor.copy(alpha = if (isDark) 0.04f else 0.03f),
                    Color.Transparent
                ),
                center = Offset(w * 0.92f, h * 0.16f),
                radius = w * 0.38f
            ),
            center = Offset(w * 0.92f, h * 0.16f),
            radius = w * 0.38f
        )

        // 3. Bottom-left soft glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = if (isDark) 0.12f else 0.09f),
                    Color.Transparent
                ),
                center = Offset(w * 0.05f, h * 0.92f),
                radius = w * 0.40f
            ),
            center = Offset(w * 0.05f, h * 0.92f),
            radius = w * 0.40f
        )

        // 4. Bottom-right soft curved ring
        drawCircle(
            color = accentColor.copy(alpha = if (isDark) 0.12f else 0.09f),
            center = Offset(w * 0.95f, h * 0.90f),
            radius = w * 0.42f,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 20f)
        )
    }
}

/**
 * Soft pillowy neomorphic-styled card container holding the authentic FiksFaham logo.
 */
@Composable
fun AuthLogoSquircle(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = FiksScoreCard,
        border = BorderStroke(1.5.dp, FiksCardBorder),
        shadowElevation = 6.dp,
        modifier = modifier
            .size(92.dp)
            .testTag("auth_logo_container")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(18.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_fiks_logo_vector),
                contentDescription = stringResource(id = R.string.auth_logo_content_desc),
                modifier = Modifier.size(54.dp)
            )
        }
    }
}

/**
 * Shake animation controller for error feedback
 */
class ShakeController {
    val offset = Animatable(0f)

    suspend fun shake() {
        offset.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 350
                0f at 0
                -12f at 40
                12f at 80
                -9f at 130
                9f at 180
                -5f at 230
                5f at 280
                0f at 350
            }
        )
    }
}

@Composable
fun rememberShakeController(): ShakeController {
    return remember { ShakeController() }
}

/**
 * Highly rounded pill-like neomorphic text field with real-time error support and shake animation.
 */
@Composable
fun AuthNeomorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    errorMessage: String? = null,
    shakeController: ShakeController? = null,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String = ""
) {
    val shakeOffset = shakeController?.offset?.value ?: 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = shakeOffset }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 14.sp,
                    color = FiksTextMuted.copy(alpha = 0.65f)
                )
            },
            singleLine = true,
            visualTransformation = visualTransformation,
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (errorMessage != null) FiksScoreRed else FiksPrimary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = errorMessage != null,
            shape = RoundedCornerShape(26.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FiksPrimary,
                unfocusedBorderColor = FiksCardBorder,
                errorBorderColor = FiksScoreRed,
                focusedContainerColor = FiksScoreCard,
                unfocusedContainerColor = FiksScoreCard,
                errorContainerColor = FiksScoreCard,
                focusedTextColor = FiksText,
                unfocusedTextColor = FiksText,
                errorTextColor = FiksText,
                cursorColor = FiksPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag(testTag)
        )

        // Error message row with small warning icon
        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .testTag("${testTag}_error")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = FiksScoreRed,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = errorMessage,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.5.sp,
                    color = FiksScoreRed,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

/**
 * Full pill primary button with theme gradient, subtle shadow, loading spinner, and trailing arrow icon.
 */
@Composable
fun AuthPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    testTag: String = "auth_pill_button"
) {
    val isEnabled = enabled && !isLoading
    val gradientColors = if (isEnabled) {
        listOf(FiksPrimary, FiksPrimaryDark)
    } else {
        listOf(FiksPrimary.copy(alpha = 0.5f), FiksPrimaryDark.copy(alpha = 0.5f))
    }

    val bounceMod = if (isEnabled) Modifier.bounceClickEffect() else Modifier

    Surface(
        shape = RoundedCornerShape(28.dp),
        shadowElevation = if (isEnabled) 5.dp else 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .then(bounceMod)
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                enabled = isEnabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(horizontal = 24.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = text,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3-bar Password Strength Indicator for SignUp screen.
 */
@Composable
fun PasswordStrengthBar(
    strength: AuthValidationUtils.PasswordStrength,
    modifier: Modifier = Modifier
) {
    if (strength == AuthValidationUtils.PasswordStrength.EMPTY) return

    val label = when (strength) {
        AuthValidationUtils.PasswordStrength.WEAK -> stringResource(R.string.auth_password_strength_weak)
        AuthValidationUtils.PasswordStrength.MEDIUM -> stringResource(R.string.auth_password_strength_medium)
        AuthValidationUtils.PasswordStrength.STRONG -> stringResource(R.string.auth_password_strength_strong)
        AuthValidationUtils.PasswordStrength.EMPTY -> ""
    }

    val activeColor = when (strength) {
        AuthValidationUtils.PasswordStrength.WEAK -> FiksScoreRed
        AuthValidationUtils.PasswordStrength.MEDIUM -> FiksAccent
        AuthValidationUtils.PasswordStrength.STRONG -> FiksScoreGreen
        AuthValidationUtils.PasswordStrength.EMPTY -> Color.Transparent
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("password_strength_indicator")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.auth_password_strength_label),
                fontFamily = PoppinsFontFamily,
                fontSize = 11.5.sp,
                color = FiksTextMuted
            )
            Text(
                text = label,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = activeColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val score = strength.score
            for (step in 1..3) {
                val isFilled = score >= step
                val barColor = if (isFilled) activeColor else FiksCardBorder.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(barColor)
                )
            }
        }
    }
}

/**
 * Logo vektor geometris FiksFaham (kotak rounded oranye dengan pen tool / kurva desain).
 */
@Composable
fun FiksBrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val primary = FiksPrimary
    val accent = FiksAccent
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            brush = Brush.linearGradient(listOf(primary, accent)),
            cornerRadius = CornerRadius(w * 0.28f, w * 0.28f),
            size = Size(w, h)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.24f),
            radius = w * 0.28f,
            center = Offset(w * 0.34f, h * 0.34f)
        )
        drawLine(
            color = Color.White,
            start = Offset(w * 0.32f, h * 0.68f),
            end = Offset(w * 0.68f, h * 0.32f),
            strokeWidth = w * 0.11f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.11f,
            center = Offset(w * 0.68f, h * 0.32f),
            style = Stroke(width = w * 0.06f)
        )
    }
}

/**
 * Maskot vektor ramah FiksFaham ("Fika", asisten eksplorasi dunia desain).
 */
@Composable
fun FiksMascot(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val primary = FiksPrimary
    val accent = FiksAccent
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Kepala karakter bulat hangat
        drawCircle(
            brush = Brush.verticalGradient(listOf(accent, primary)),
            radius = w * 0.42f,
            center = Offset(w * 0.5f, h * 0.54f)
        )
        // Antena kreativitas
        drawLine(
            color = primary,
            start = Offset(w * 0.5f, h * 0.14f),
            end = Offset(w * 0.5f, h * 0.06f),
            strokeWidth = w * 0.06f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = accent,
            radius = w * 0.07f,
            center = Offset(w * 0.5f, h * 0.05f)
        )
        // Visor mata putih
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.24f, h * 0.38f),
            size = Size(w * 0.52f, h * 0.26f),
            cornerRadius = CornerRadius(w * 0.13f, w * 0.13f)
        )
        // Mata ekspresif
        drawCircle(color = Color(0xFF1E1713), radius = w * 0.05f, center = Offset(w * 0.39f, h * 0.51f))
        drawCircle(color = Color(0xFF1E1713), radius = w * 0.05f, center = Offset(w * 0.61f, h * 0.51f))
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext?.findActivity()
    else -> null
}

/**
 * Mengambil Google ID Token via Credential Manager Android.
 */
suspend fun requestGoogleIdToken(context: Context): Result<String> {
    return try {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        val serverClientId = if (resId != 0) context.getString(resId) else ""
        if (serverClientId.isBlank()) {
            return Result.failure(
                IllegalStateException("Google Sign-In belum dikonfigurasi pada Firebase Console (default_web_client_id belum tersedia).")
            )
        }
        val activityContext = context.findActivity() ?: context
        val credentialManager = CredentialManager.create(activityContext)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val response = credentialManager.getCredential(context = activityContext, request = request)
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
            Result.success(googleCred.idToken)
        } else {
            Result.failure(IllegalStateException("Kredensial Google tidak valid."))
        }
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.localizedMessage ?: "Login Google dibatalkan atau gagal."))
    }
}
