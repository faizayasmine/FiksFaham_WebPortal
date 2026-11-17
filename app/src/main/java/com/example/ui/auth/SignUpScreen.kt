package com.example.ui.auth

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksCardBorder
import com.example.ui.theme.FiksFahamTheme
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksScoreCard
import com.example.ui.theme.FiksScoreRed
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import com.example.util.AuthValidationUtils
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    onSignUpSubmit: suspend (nama: String, email: String, password: String, instansi: String) -> Result<Unit>,
    onNavigateToLogin: () -> Unit,
    onGoogleSignInSubmit: (suspend (idToken: String) -> Result<Unit>)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var instansi by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var instansiError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val shakeController = rememberShakeController()

    val passwordStrength = remember(password) {
        AuthValidationUtils.evaluatePasswordStrength(password)
    }

    fun attemptSignUp() {
        val trimmedName = nama.trim()
        val trimmedEmail = email.trim()
        val trimmedInstansi = instansi.trim()
        val trimmedPassword = password
        val trimmedConfirm = confirmPassword

        // Real-time validations
        val errName = AuthValidationUtils.validateName(trimmedName)
        val errEmail = AuthValidationUtils.validateEmail(trimmedEmail)
        val errInstansi = AuthValidationUtils.validateInstansi(trimmedInstansi)
        val errPass = AuthValidationUtils.validatePassword(trimmedPassword)
        val errConfirm = AuthValidationUtils.validateConfirmPassword(trimmedPassword, trimmedConfirm)

        nameError = errName
        emailError = errEmail
        instansiError = errInstansi
        passwordError = errPass
        confirmPasswordError = errConfirm
        generalError = null

        val hasError = errName != null || errEmail != null || errInstansi != null || errPass != null || errConfirm != null
        if (hasError) {
            coroutineScope.launch {
                shakeController.shake()
            }
            return
        }

        isLoading = true
        focusManager.clearFocus()

        coroutineScope.launch {
            val res = onSignUpSubmit(trimmedName, trimmedEmail, trimmedPassword, trimmedInstansi)
            isLoading = false
            if (res.isFailure) {
                val msg = res.exceptionOrNull()?.message
                    ?: "Pendaftaran gagal. Periksa kembali koneksi internet dan data akunmu."
                generalError = msg
                shakeController.shake()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .testTag("signup_screen")
    ) {
        // Soft translucent neomorphic background circles/blobs in theme colors
        AuthSoftBackground()

        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = FiksScoreCard,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onNavigateToLogin)
                        .testTag("signup_back_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.auth_back_to_login),
                            tint = FiksPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Logo FiksFaham in soft squircle container
            AuthLogoSquircle()

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Bold title "Buat Akun" & subtitle
            Text(
                text = stringResource(id = R.string.auth_signup_title),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = FiksText,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("signup_header")
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(id = R.string.auth_signup_subtitle),
                fontFamily = PoppinsFontFamily,
                fontSize = 13.sp,
                color = FiksTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("signup_subtitle")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Form container (max width for tablets/wide screens)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3. Nama Lengkap
                AuthNeomorphicTextField(
                    value = nama,
                    onValueChange = {
                        nama = it
                        if (nameError != null) nameError = null
                        if (generalError != null) generalError = null
                    },
                    placeholder = stringResource(id = R.string.auth_field_name_placeholder),
                    leadingIcon = Icons.Default.Person,
                    errorMessage = nameError,
                    shakeController = shakeController,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    testTag = "signup_name_input"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Email
                AuthNeomorphicTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                        if (generalError != null) generalError = null
                    },
                    placeholder = stringResource(id = R.string.auth_field_email_placeholder),
                    leadingIcon = Icons.Default.Email,
                    errorMessage = emailError,
                    shakeController = shakeController,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    testTag = "signup_email_input"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Asal Instansi
                AuthNeomorphicTextField(
                    value = instansi,
                    onValueChange = {
                        instansi = it
                        if (instansiError != null) instansiError = null
                        if (generalError != null) generalError = null
                    },
                    placeholder = stringResource(id = R.string.auth_field_instansi_placeholder),
                    leadingIcon = Icons.Default.School,
                    errorMessage = instansiError,
                    shakeController = shakeController,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    testTag = "signup_instansi_input"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 6. Kata Sandi
                AuthNeomorphicTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) passwordError = null
                        if (generalError != null) generalError = null
                    },
                    placeholder = stringResource(id = R.string.auth_field_password_placeholder),
                    leadingIcon = Icons.Default.Lock,
                    errorMessage = passwordError,
                    shakeController = shakeController,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = stringResource(
                                    id = if (passwordVisible) R.string.auth_password_hide else R.string.auth_password_show
                                ),
                                tint = FiksTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    testTag = "signup_password_input"
                )

                // Password strength indicator
                if (password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    PasswordStrengthBar(strength = passwordStrength)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 7. Konfirmasi Kata Sandi
                AuthNeomorphicTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (confirmPasswordError != null) confirmPasswordError = null
                        if (generalError != null) generalError = null
                    },
                    placeholder = stringResource(id = R.string.auth_field_confirm_password_placeholder),
                    leadingIcon = Icons.Default.Lock,
                    errorMessage = confirmPasswordError,
                    shakeController = shakeController,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = stringResource(
                                    id = if (confirmPasswordVisible) R.string.auth_password_hide else R.string.auth_password_show
                                ),
                                tint = FiksTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { attemptSignUp() }
                    ),
                    testTag = "signup_confirm_password_input"
                )

                // General Error Banner (if auth backend returns error)
                if (generalError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = generalError!!,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = FiksScoreRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .testTag("signup_general_error")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 8. Full Pill Primary Button "Daftar"
                AuthPillButton(
                    text = if (isLoading) stringResource(id = R.string.auth_loading) else stringResource(id = R.string.auth_btn_signup),
                    isLoading = isLoading,
                    enabled = !isLoading,
                    onClick = { attemptSignUp() },
                    testTag = "signup_submit_button"
                )

                if (onGoogleSignInSubmit != null) {
                    Spacer(modifier = Modifier.height(18.dp))
                    AuthDivider(text = "Atau daftar dengan")
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthGoogleButton(
                        text = "Daftar dengan Google",
                        testTag = "signup_google_button",
                        onClick = {
                            if (isLoading) return@AuthGoogleButton
                            isLoading = true
                            generalError = null
                            coroutineScope.launch {
                                val tokenRes = requestGoogleIdToken(context)
                                tokenRes.onSuccess { idToken ->
                                    val res = onGoogleSignInSubmit(idToken)
                                    isLoading = false
                                    if (res.isFailure) {
                                        generalError = res.exceptionOrNull()?.localizedMessage ?: "Gagal daftar dengan Google."
                                    }
                                }.onFailure { err ->
                                    isLoading = false
                                    generalError = err.localizedMessage ?: "Login Google dibatalkan."
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 9. Footer: Sudah punya akun? Masuk
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.auth_has_account) + " ",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = FiksTextMuted
                    )
                    Text(
                        text = stringResource(id = R.string.auth_link_login),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = FiksPrimary,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onNavigateToLogin
                            )
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("signup_to_login_link")
                    )
                }
            }
        }
    }
}

@Preview(name = "SignUp Light Mode", showBackground = true)
@Composable
private fun SignUpScreenLightPreview() {
    FiksFahamTheme(darkTheme = false) {
        SignUpScreen(
            onSignUpSubmit = { _, _, _, _ -> Result.success(Unit) },
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "SignUp Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SignUpScreenDarkPreview() {
    FiksFahamTheme(darkTheme = true) {
        SignUpScreen(
            onSignUpSubmit = { _, _, _, _ -> Result.success(Unit) },
            onNavigateToLogin = {}
        )
    }
}
