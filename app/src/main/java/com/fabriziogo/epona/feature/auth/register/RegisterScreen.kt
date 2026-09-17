package com.fabriziogo.epona.feature.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.fabriziogo.epona.feature.auth.components.AuthHeroCard
import com.fabriziogo.epona.feature.auth.components.OrDivider
import com.fabriziogo.epona.feature.auth.components.PasswordTextField
import com.fabriziogo.epona.feature.auth.components.SocialSignInButton
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.fabriziogo.epona.core.ui.theme.StatusBarIcons

@Composable
fun RegisterScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onLaunchGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // White glyphs read against the teal hero regardless of the app theme.
    StatusBarIcons(darkIcons = false)

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
        modifier = modifier,
        // The hero draws its own status-bar padding so it can sit under the transparent bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
        ) {
            AuthHeroCard(
                title = stringResource(R.string.auth_create_account),
                subtitle = stringResource(R.string.auth_sign_up_subtitle),
                onBackClick = { onEvent(RegisterEvent.NavigateToLogin) }
            ) {
                // Google Sign In
                SocialSignInButton(
                    text = stringResource(R.string.auth_google),
                    isLoading = state.isGoogleLoading,
                    onClick = onLaunchGoogleSignIn
                )

                Spacer(Modifier.height(20.dp))

                OrDivider()

                Spacer(Modifier.height(20.dp))

                // First / Last name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EponaTextField(
                        value = state.firstName,
                        onValueChange = { onEvent(RegisterEvent.FirstNameChanged(it)) },
                        label = stringResource(R.string.auth_first_name),
                        errorText = state.firstNameError,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.weight(1f)
                    )
                    EponaTextField(
                        value = state.lastName,
                        onValueChange = { onEvent(RegisterEvent.LastNameChanged(it)) },
                        label = stringResource(R.string.auth_last_name),
                        errorText = state.lastNameError,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.weight(1f)
                    )
                }

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

                Spacer(Modifier.height(20.dp))

                // Sign up button
                EponaFilledButton(
                    text = stringResource(R.string.auth_sign_up),
                    onClick = { onEvent(RegisterEvent.SignUpClicked) },
                    loading = state.isLoading,
                    fullWidth = true,
                    enabled = state.firstName.isNotBlank()
                        && state.lastName.isNotBlank()
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Login link
                TextButton(
                    onClick = { onEvent(RegisterEvent.NavigateToLogin) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.auth_has_account),
                        style = EponaTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    // We use RegisterContent for the preview to avoid hiltViewModel() instantiation issues
    EponaTheme(dynamicColor = false) {
        RegisterContent(
            state = RegisterUiState(),
            onEvent = {},
            onLaunchGoogleSignIn = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenConfirmationPreview() {
    EponaTheme(dynamicColor = false) {
        RegisterContent(
            state = RegisterUiState(
                firstName = "Ada",
                lastName = "Lovelace",
                email = "ada@example.com",
                pendingConfirmationEmail = "ada@example.com"
            ),
            onEvent = {},
            onLaunchGoogleSignIn = {}
        )
    }
}
