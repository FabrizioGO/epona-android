package com.fabriziogo.epona.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
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
import com.fabriziogo.epona.feature.auth.components.OrDivider
import com.fabriziogo.epona.feature.auth.components.PasswordTextField
import com.fabriziogo.epona.feature.auth.components.SocialSignInButton

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLaunchGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // White glyphs read against the teal hero regardless of the app theme.
    StatusBarIcons(darkIcons = false)

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                LoginNavEvent.NavigateToHome -> onNavigateToHome()
                LoginNavEvent.NavigateToRegister -> onNavigateToRegister()
                LoginNavEvent.LaunchGoogleSignIn -> onLaunchGoogleSignIn()
            }
        }
    }

    LoginContent(
        state = state,
        onEvent = viewModel::onEvent,
        onLaunchGoogleSignIn = onLaunchGoogleSignIn,
        modifier = modifier
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    onLaunchGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(LoginEvent.ErrorDismissed)
        }
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
                title = stringResource(R.string.auth_welcome_back),
                subtitle = stringResource(R.string.auth_sign_in_subtitle)
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

                // Email
                EponaTextField(
                    value = state.email,
                    onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
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
                    onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                    label = stringResource(R.string.auth_password),
                    errorText = state.passwordError,
                    imeAction = ImeAction.Done,
                    onImeAction = { onEvent(LoginEvent.SignInClicked) }
                )

                Spacer(Modifier.height(8.dp))

                // Forgot password
                TextButton(
                    onClick = { /* TODO: navigate to reset */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        stringResource(R.string.auth_forgot_password),
                        style = EponaTypography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Sign in button
                EponaFilledButton(
                    text = stringResource(R.string.auth_sign_in),
                    onClick = { onEvent(LoginEvent.SignInClicked) },
                    loading = state.isLoading,
                    fullWidth = true,
                    enabled = state.email.isNotBlank() && state.password.isNotBlank()
                )

                Spacer(Modifier.height(20.dp))

                // Register link
                TextButton(
                    onClick = { onEvent(LoginEvent.NavigateToRegister) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.auth_no_account),
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
fun LoginScreenPreview() {
    EponaTheme(dynamicColor = false) {
        LoginContent(
            state = LoginUiState(),
            onEvent = {},
            onLaunchGoogleSignIn = {}
        )
    }
}
