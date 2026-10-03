package com.fabriziogo.epona.feature.auth.forgotpassword

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.fabriziogo.epona.core.ui.theme.StatusBarIcons
import com.fabriziogo.epona.feature.auth.components.AuthHeroCard
import com.fabriziogo.epona.feature.auth.components.PasswordTextField

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StatusBarIcons(darkIcons = false)

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                ForgotPasswordNavEvent.NavigateBack -> onNavigateBack()
                ForgotPasswordNavEvent.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    BackHandler { viewModel.onEvent(ForgotPasswordEvent.BackClicked) }

    ForgotPasswordContent(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun ForgotPasswordContent(
    state: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(ForgotPasswordEvent.ErrorDismissed)
        }
    }

    val (title, subtitle) = when (state.step) {
        ForgotPasswordStep.EnterEmail ->
            stringResource(R.string.forgot_title) to stringResource(R.string.forgot_email_subtitle)
        ForgotPasswordStep.EnterCode ->
            stringResource(R.string.forgot_code_title) to
                stringResource(R.string.forgot_code_subtitle, state.email.trim())
        ForgotPasswordStep.NewPassword ->
            stringResource(R.string.forgot_new_password_title) to
                stringResource(R.string.forgot_new_password_subtitle)
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            AuthHeroCard(
                title = title,
                subtitle = subtitle,
                onBackClick = { onEvent(ForgotPasswordEvent.BackClicked) }
            ) {
                when (state.step) {
                    ForgotPasswordStep.EnterEmail -> EmailStep(state, onEvent)
                    ForgotPasswordStep.EnterCode -> CodeStep(state, onEvent)
                    ForgotPasswordStep.NewPassword -> NewPasswordStep(state, onEvent)
                }
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun EmailStep(
    state: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit
) {
    EponaTextField(
        value = state.email,
        onValueChange = { onEvent(ForgotPasswordEvent.EmailChanged(it)) },
        label = stringResource(R.string.auth_email),
        placeholder = stringResource(R.string.auth_email_placeholder),
        errorText = state.emailError,
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Done,
        onImeAction = { onEvent(ForgotPasswordEvent.SendCodeClicked) },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(20.dp))

    EponaFilledButton(
        text = stringResource(R.string.forgot_send_code),
        onClick = { onEvent(ForgotPasswordEvent.SendCodeClicked) },
        loading = state.isLoading,
        fullWidth = true,
        enabled = state.email.isNotBlank()
    )
}

@Composable
private fun ColumnScope.CodeStep(
    state: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit
) {
    EponaTextField(
        value = state.code,
        onValueChange = { onEvent(ForgotPasswordEvent.CodeChanged(it)) },
        label = stringResource(R.string.forgot_code_label),
        placeholder = "000000",
        errorText = state.codeError,
        keyboardType = KeyboardType.NumberPassword,
        imeAction = ImeAction.Done,
        onImeAction = { onEvent(ForgotPasswordEvent.VerifyCodeClicked) },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(20.dp))

    EponaFilledButton(
        text = stringResource(R.string.forgot_verify),
        onClick = { onEvent(ForgotPasswordEvent.VerifyCodeClicked) },
        loading = state.isLoading,
        fullWidth = true,
        enabled = state.code.length in RESET_CODE_MIN_LENGTH..RESET_CODE_MAX_LENGTH
    )

    Spacer(Modifier.height(8.dp))

    val cooling = state.resendCooldownSeconds > 0
    TextButton(
        onClick = { onEvent(ForgotPasswordEvent.ResendCodeClicked) },
        enabled = !cooling && !state.isLoading,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    ) {
        Text(
            text = if (cooling) {
                stringResource(R.string.forgot_resend_in, state.resendCooldownSeconds)
            } else {
                stringResource(R.string.forgot_resend_code)
            },
            style = EponaTypography.labelLarge
        )
    }
}

@Composable
private fun NewPasswordStep(
    state: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit
) {
    PasswordTextField(
        value = state.newPassword,
        onValueChange = { onEvent(ForgotPasswordEvent.NewPasswordChanged(it)) },
        label = stringResource(R.string.forgot_new_password_label),
        errorText = state.newPasswordError,
        imeAction = ImeAction.Next
    )

    Spacer(Modifier.height(12.dp))

    PasswordTextField(
        value = state.confirmPassword,
        onValueChange = { onEvent(ForgotPasswordEvent.ConfirmPasswordChanged(it)) },
        label = stringResource(R.string.auth_confirm_password),
        errorText = state.confirmPasswordError,
        imeAction = ImeAction.Done,
        onImeAction = { onEvent(ForgotPasswordEvent.SavePasswordClicked) }
    )

    Spacer(Modifier.height(20.dp))

    EponaFilledButton(
        text = stringResource(R.string.forgot_save_password),
        onClick = { onEvent(ForgotPasswordEvent.SavePasswordClicked) },
        loading = state.isLoading,
        fullWidth = true,
        enabled = state.newPassword.isNotBlank() && state.confirmPassword.isNotBlank()
    )
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordEmailPreview() {
    EponaTheme(dynamicColor = false) {
        ForgotPasswordContent(state = ForgotPasswordUiState(), onEvent = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordCodePreview() {
    EponaTheme(dynamicColor = false) {
        ForgotPasswordContent(
            state = ForgotPasswordUiState(
                step = ForgotPasswordStep.EnterCode,
                email = "you@example.com",
                code = "123",
                resendCooldownSeconds = 42
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordNewPasswordPreview() {
    EponaTheme(dynamicColor = false) {
        ForgotPasswordContent(
            state = ForgotPasswordUiState(step = ForgotPasswordStep.NewPassword),
            onEvent = {}
        )
    }
}
