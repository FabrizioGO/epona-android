package com.fabriziogo.epona.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.colorResource
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
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.fabriziogo.epona.feature.auth.components.AuthHeader
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
            Spacer(Modifier.height(60.dp))

            AuthHeader(
                title = "Welcome back",
                subtitle = "Sign in to find and help lost pets in your community"
            )

            Spacer(Modifier.height(36.dp))

            // Google Sign In
            SocialSignInButton(
                text = "Continue with Google",
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
                label = "Email",
                placeholder = "you@example.com",
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
                label = "Password",
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
                    "Forgot password?",
                    style = EponaTypography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Sign in button
            EponaFilledButton(
                text = "Sign In",
                onClick = { onEvent(LoginEvent.SignInClicked) },
                loading = state.isLoading,
                fullWidth = true,
                enabled = state.email.isNotBlank() && state.password.isNotBlank()
            )

            Spacer(Modifier.height(24.dp))

            // Register link
            TextButton(
                onClick = { onEvent(LoginEvent.NavigateToRegister) }
            ) {
                Text(
                    text = "Don't have an account? Sign up",
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
fun LoginScreenPreview(){
    LoginContent(
        state = LoginUiState(),
        onEvent = {},
        onLaunchGoogleSignIn = {}
    )
}