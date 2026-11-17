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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FiksBg
import com.example.ui.theme.FiksFahamTheme
import com.example.ui.theme.FiksPrimary
import com.example.ui.theme.FiksText
import com.example.ui.theme.FiksTextMuted
import com.example.ui.theme.PoppinsFontFamily
import com.example.util.AuthValidationUtils
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSubmit: suspend (email: String, password: String) -> Result<Unit>,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onGoogleSignInSubmit: (suspend (idToken: String) -> Result<Unit>)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val shakeController = rememberShakeController()

    fun attemptLogin() {
        val trimmedEmail = email.trim()
        val trimmedPassword = password

        // Real-time validation checks
        val errEmail = AuthValidationUtils.validateEmail(trimmedEmail)
        val errPassword = AuthValidationUtils.validatePassword(trimmedPassword)

        emailError = errEmail
        passwordError = errPassword
        generalError = null

        if (errEmail != null || errPassword != null) {
            coroutineScope.launch {
                shakeController.shake()
            }
            return
        }

        isLoading = true
        focusManager.clearFocus()

        coroutineScope.launch {
            val result = onLoginSubmit(trimmedEmail, trimmedPassword)
            isLoading = false
            if (result.isFailure) {
                val msg = result.exceptionOrNull()?.message
                    ?: "Gagal masuk. Periksa kembali email dan kata sandimu."
                generalError = msg
                shakeController.shake()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FiksBg)
            .testTag("login_screen")
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
            Spacer(modifier = Modifier.height(36.dp))

            // 1. Logo FiksFaham in soft squircle container
            AuthLogoSquircle()

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Bold title & subtle subtitle
            Text(
                text = stringResource(id = R.string.auth_login_title),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = FiksText,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("login_header")
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(id = R.string.auth_login_subtitle),
                fontFamily = PoppinsFontFamily,
                fontSize = 13.sp,
                color = FiksTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("login_subtitle")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Card Container for input fields (centered, max width for tablets/wide screens)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3. Email Field
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
                    testTag = "login_email_input"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Password Field
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
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { attemptLogin() }
                    ),
                    testTag = "login_password_input"
                )

                // General Error Banner (if auth backend returns error)
                if (generalError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = generalError!!,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = com.example.ui.theme.FiksScoreRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .testTag("login_general_error")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Forgot Password Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = stringResource(id = R.string.auth_forgot_password),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = FiksPrimary,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onNavigateToForgotPassword
                            )
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("login_forgot_password_link")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 6. Full Pill Primary Button "Masuk"
                AuthPillButton(
                    text = if (isLoading) stringResource(id = R.string.auth_loading) else stringResource(id = R.string.auth_btn_login),
                    isLoading = isLoading,
                    enabled = !isLoading,
                    onClick = { attemptLogin() },
                    testTag = "login_submit_button"
                )

                if (onGoogleSignInSubmit != null) {
                    Spacer(modifier = Modifier.height(18.dp))
                    AuthDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthGoogleButton(
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
                                        generalError = res.exceptionOrNull()?.localizedMessage ?: "Gagal masuk dengan Google."
                                    }
                                }.onFailure { err ->
                                    isLoading = false
                                    generalError = err.localizedMessage ?: "Login Google dibatalkan."
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 7. Footer Link: Belum punya akun? Daftar
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.auth_no_account) + " ",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = FiksTextMuted
                    )
                    Text(
                        text = stringResource(id = R.string.auth_link_signup),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = FiksPrimary,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onNavigateToSignUp
                            )
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("login_to_signup_link")
                    )
                }
            }
        }
    }
}

@Preview(name = "Login Light Mode", showBackground = true)
@Composable
private fun LoginScreenLightPreview() {
    FiksFahamTheme(darkTheme = false) {
        LoginScreen(
            onLoginSubmit = { _, _ -> Result.success(Unit) },
            onNavigateToSignUp = {},
            onNavigateToForgotPassword = {}
        )
    }
}

@Preview(name = "Login Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LoginScreenDarkPreview() {
    FiksFahamTheme(darkTheme = true) {
        LoginScreen(
            onLoginSubmit = { _, _ -> Result.success(Unit) },
            onNavigateToSignUp = {},
            onNavigateToForgotPassword = {}
        )
    }
}
