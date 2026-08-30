package com.fabriziogo.epona.feature.auth.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.feature.auth.components.AuthHeader
import com.fabriziogo.epona.feature.auth.components.OrDivider
import com.fabriziogo.epona.feature.auth.components.PasswordTextField
import com.fabriziogo.epona.feature.auth.components.SocialSignInButton
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun RegisterScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onLaunchGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                RegisterNavEvent.NavigateToHome -> onNavigateToHome()
                RegisterNavEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    RegisterContent(
        state = state,
        onEvent = viewModel::onEvent,
        onLaunchGoogleSignIn = onLaunchGoogleSignIn,
        modifier = modifier
    )
}

@Composable
private fun RegisterContent(
    state: RegisterUiState,
    onEvent: (RegisterEvent) -> Unit,
    onLaunchGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(RegisterEvent.ErrorDismissed)
        }
    }

    state.pendingConfirmationEmail?.let { email ->
        AlertDialog(
            onDismissRequest = { onEvent(RegisterEvent.ConfirmationAcknowledged) },
            title = { Text(stringResource(R.string.auth_confirm_email_title)) },
            text = {
                Text(
                    text = stringResource(R.string.auth_confirm_email_message, email),
                    style = EponaTypography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(RegisterEvent.ConfirmationAcknowledged) }
                ) {
                    Text(stringResource(R.string.auth_go_to_sign_in))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            AuthHeader(
                title = stringResource(R.string.auth_create_account),
                subtitle = stringResource(R.string.auth_sign_up_subtitle)
            )

            Spacer(Modifier.height(32.dp))

            // Google Sign In
            SocialSignInButton(
                text = stringResource(R.string.auth_google),
                isLoading = state.isGoogleLoading,
                onClick = onLaunchGoogleSignIn
            )

            Spacer(Modifier.height(20.dp))

            OrDivider()

            Spacer(Modifier.height(20.dp))

            // Display Name
            EponaTextField(
                value = state.displayName,
                onValueChange = { onEvent(RegisterEvent.NameChanged(it)) },
                label = stringResource(R.string.auth_full_name),
                placeholder = stringResource(R.string.auth_full_name_placeholder),
                errorText = state.nameError,
                imeAction = ImeAction.Next,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Email
            EponaTextField(
                value = state.email,
                onValueChange = { onEvent(RegisterEvent.EmailChanged(it)) },
                label = stringResource(R.string.auth_email),
                placeholder = stringResource(R.string.auth_email_placeholder),
                errorText = state.emailError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Password
            PasswordTextField(
                value = state.password,
                onValueChange = { onEvent(RegisterEvent.PasswordChanged(it)) },
                label = stringResource(R.string.auth_password),
                errorText = state.passwordError,
                imeAction = ImeAction.Next
            )

            Spacer(Modifier.height(12.dp))

            // Confirm Password
            PasswordTextField(
                value = state.confirmPassword,
                onValueChange = { onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                label = stringResource(R.string.auth_confirm_password),
                errorText = state.confirmPasswordError,
                imeAction = ImeAction.Done,
                onImeAction = { onEvent(RegisterEvent.SignUpClicked) }
            )

            Spacer(Modifier.height(24.dp))

            // Sign up button
            EponaFilledButton(
                text = stringResource(R.string.auth_sign_up),
                onClick = { onEvent(RegisterEvent.SignUpClicked) },
                loading = state.isLoading,
                fullWidth = true,
                enabled = state.displayName.isNotBlank()
                    && state.email.isNotBlank()
                    && state.password.isNotBlank()
                    && state.confirmPassword.isNotBlank()
            )

            Spacer(Modifier.height(12.dp))

            // Terms text
            Text(
                text = stringResource(R.string.auth_terms),
                style = EponaTypography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Login link
            TextButton(
                onClick = { onEvent(RegisterEvent.NavigateToLogin) }
            ) {
                Text(
                    text = stringResource(R.string.auth_has_account),
                    style = EponaTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    // We use RegisterContent for the preview to avoid hiltViewModel() instantiation issues
    RegisterContent(
        state = RegisterUiState(),
        onEvent = {},
        onLaunchGoogleSignIn = {}
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenConfirmationPreview() {
    RegisterContent(
        state = RegisterUiState(
            displayName = "Ada Lovelace",
            email = "ada@example.com",
            pendingConfirmationEmail = "ada@example.com"
        ),
        onEvent = {},
        onLaunchGoogleSignIn = {}
    )
}