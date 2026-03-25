package com.graceon.feature.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.graceon.core.ui.component.GraceOnAmbientBackground
import com.graceon.core.ui.theme.GlassBorder
import com.graceon.core.ui.theme.GlassSurface
import com.graceon.core.ui.theme.GlassSurfaceStrong
import com.graceon.core.ui.theme.Primary
import com.graceon.core.ui.theme.Secondary
import kotlinx.coroutines.launch

private const val LOGIN_HERO_IMAGE =
    "https://images.unsplash.com/photo-1470115636492-6d2b56f9146d?q=80&w=1200&auto=format&fit=crop"

private enum class AuthMode {
    SignIn,
    SignUp
}

private enum class AuthLoadingAction {
    None,
    EmailSubmit,
    GoogleSignIn,
    ResendConfirmation,
    ResetPassword
}

@Composable
fun LoginScreen(
    noticeMessage: String? = null,
    onNoticeMessageShown: () -> Unit = {},
    onSignIn: suspend (String, String) -> Unit,
    onSignUp: suspend (String, String) -> Boolean,
    onGoogleLogin: suspend () -> Unit,
    onResendConfirmationEmail: suspend (String) -> Unit,
    onSendPasswordResetEmail: suspend (String) -> Unit,
    isGoogleLoginEnabled: Boolean
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var mode by rememberSaveable { mutableStateOf(AuthMode.SignIn) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var loadingAction by remember { mutableStateOf(AuthLoadingAction.None) }

    LaunchedEffect(noticeMessage) {
        noticeMessage?.let {
            snackbarHostState.showSnackbar(it)
            onNoticeMessageShown()
        }
    }

    fun validateInputs(): String? {
        if (email.isBlank() || "@" !in email) {
            return "올바른 이메일 주소를 입력해주세요."
        }
        if (password.length < 6) {
            return "비밀번호는 6자 이상이어야 합니다."
        }
        return null
    }

    fun validateEmailOnly(): String? {
        if (email.isBlank() || "@" !in email) {
            return "이메일 주소를 먼저 입력해주세요."
        }
        return null
    }

    fun submit() {
        val error = validateInputs()
        if (error != null) {
            coroutineScope.launch { snackbarHostState.showSnackbar(error) }
            return
        }

        coroutineScope.launch {
            loadingAction = AuthLoadingAction.EmailSubmit
            runCatching {
                when (mode) {
                    AuthMode.SignIn -> {
                        onSignIn(email.trim(), password)
                    }

                    AuthMode.SignUp -> {
                        val signedIn = onSignUp(email.trim(), password)
                        if (!signedIn) {
                            snackbarHostState.showSnackbar("회원가입이 완료되었습니다. 인증 메일을 확인한 뒤 로그인해주세요.")
                        }
                    }
                }
            }.onFailure { throwable ->
                snackbarHostState.showSnackbar(
                    throwable.message ?: "로그인 처리에 실패했습니다. 다시 시도해주세요."
                )
            }
            loadingAction = AuthLoadingAction.None
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GraceOnAmbientBackground()

        NetworkHeroImage(
            url = LOGIN_HERO_IMAGE,
            contentDescription = "새벽 숲길",
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.material3.MaterialTheme.colorScheme.background.copy(alpha = 0.18f),
                            androidx.compose.material3.MaterialTheme.colorScheme.background.copy(alpha = 0.74f),
                            androidx.compose.material3.MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(124.dp))
                AuthCard(
                    mode = mode,
                    email = email,
                    password = password,
                    loadingAction = loadingAction,
                    onChangeMode = { mode = it },
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onSubmit = ::submit,
                    onGoogleLogin = {
                        coroutineScope.launch {
                            loadingAction = AuthLoadingAction.GoogleSignIn
                            runCatching { onGoogleLogin() }
                                .onFailure { throwable ->
                                    snackbarHostState.showSnackbar(
                                        throwable.message ?: "Google 로그인에 실패했습니다. 다시 시도해주세요."
                                    )
                                }
                            loadingAction = AuthLoadingAction.None
                        }
                    },
                    isGoogleLoginEnabled = isGoogleLoginEnabled,
                    onResendConfirmationEmail = {
                        val error = validateEmailOnly()
                        if (error != null) {
                            coroutineScope.launch { snackbarHostState.showSnackbar(error) }
                            return@AuthCard
                        }
                        coroutineScope.launch {
                            loadingAction = AuthLoadingAction.ResendConfirmation
                            runCatching {
                                onResendConfirmationEmail(email.trim())
                                snackbarHostState.showSnackbar("인증 메일을 다시 보냈습니다. 메일함을 확인해주세요.")
                            }.onFailure { throwable ->
                                snackbarHostState.showSnackbar(
                                    throwable.message ?: "인증 메일 재발송에 실패했습니다."
                                )
                            }
                            loadingAction = AuthLoadingAction.None
                        }
                    },
                    onSendPasswordResetEmail = {
                        val error = validateEmailOnly()
                        if (error != null) {
                            coroutineScope.launch { snackbarHostState.showSnackbar(error) }
                            return@AuthCard
                        }
                        coroutineScope.launch {
                            loadingAction = AuthLoadingAction.ResetPassword
                            runCatching {
                                onSendPasswordResetEmail(email.trim())
                                snackbarHostState.showSnackbar("비밀번호 재설정 메일을 보냈습니다. 메일함을 확인해주세요.")
                            }.onFailure { throwable ->
                                snackbarHostState.showSnackbar(
                                    throwable.message ?: "비밀번호 재설정 메일 전송에 실패했습니다."
                                )
                            }
                            loadingAction = AuthLoadingAction.None
                        }
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun AuthCard(
    mode: AuthMode,
    email: String,
    password: String,
    loadingAction: AuthLoadingAction,
    onChangeMode: (AuthMode) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoogleLogin: () -> Unit,
    isGoogleLoginEnabled: Boolean,
    onResendConfirmationEmail: () -> Unit,
    onSendPasswordResetEmail: () -> Unit
) {
    val isAnyLoading = loadingAction != AuthLoadingAction.None
    val isEmailSubmitting = loadingAction == AuthLoadingAction.EmailSubmit
    val isGoogleSigningIn = loadingAction == AuthLoadingAction.GoogleSignIn
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassSurfaceStrong,
        shape = RoundedCornerShape(32.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.20f),
                shape = RoundedCornerShape(999.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text(
                    text = "GraceOn",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = if (mode == AuthMode.SignIn) {
                    "메일로 로그인하고\n말씀을 이어보세요"
                } else {
                    "메일로 가입하고\n무료 횟수를 시작하세요"
                },
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                lineHeight = 40.sp
            )

            Text(
                text = "계정으로 무료 횟수와 저장한 말씀을 안전하게 이어서 관리합니다.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 23.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AuthModeChip(
                    text = "로그인",
                    selected = mode == AuthMode.SignIn,
                    onClick = { onChangeMode(AuthMode.SignIn) },
                    modifier = Modifier.weight(1f)
                )
                AuthModeChip(
                    text = "회원가입",
                    selected = mode == AuthMode.SignUp,
                    onClick = { onChangeMode(AuthMode.SignUp) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("이메일") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isAnyLoading,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isEmailSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (mode == AuthMode.SignIn) "이메일로 로그인" else "이메일로 회원가입",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isGoogleLoginEnabled) {
                OutlinedButton(
                    onClick = onGoogleLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !isAnyLoading,
                    shape = RoundedCornerShape(999.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    if (isGoogleSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        Text(
                            text = "Google로 계속",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (isGoogleSigningIn) {
                    Text(
                        text = "로그인 화면을 여는 중입니다. 브라우저가 열리면 Google 로그인을 완료해주세요.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = Secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (mode == AuthMode.SignUp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onResendConfirmationEmail,
                        enabled = !isAnyLoading
                    ) {
                        Text("인증 메일 재발송")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onSendPasswordResetEmail,
                        enabled = !isAnyLoading
                    ) {
                        Text("비밀번호 재설정")
                    }
                }
            }

            Text(
                text = if (mode == AuthMode.SignUp) {
                    "Supabase에서 이메일 인증이 켜져 있으면, 인증 메일 확인 후 다시 로그인해야 합니다."
                } else {
                    "로그인 후 하루 무료 횟수와 저장한 말씀이 계정 기준으로 관리됩니다."
                },
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = Secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AuthModeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (selected) Primary.copy(alpha = 0.16f) else GlassSurface,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) Primary.copy(alpha = 0.48f) else GlassBorder
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) Primary else androidx.compose.material3.MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun HighlightCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassSurface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Primary.copy(alpha = 0.14f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
