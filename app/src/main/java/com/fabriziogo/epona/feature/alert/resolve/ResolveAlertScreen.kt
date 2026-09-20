package com.fabriziogo.epona.feature.alert.resolve

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaOutlinedButton
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun ResolveAlertScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResolveAlertViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingIndicator()
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", style = EponaTypography.displayLarge)
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.resolve_success_title),
            style = EponaTypography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(
                R.string.resolve_success_message,
                state.petName.ifBlank { stringResource(R.string.resolve_default_pet_name) }
            ),
            style = EponaTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(36.dp))
        EponaFilledButton(
            stringResource(R.string.resolve_back_home),
            onClick = onNavigateToHome,
            fullWidth = true
        )
        Spacer(Modifier.height(12.dp))
        EponaOutlinedButton(
            stringResource(R.string.resolve_view_alert),
            onClick = onNavigateBack,
            fullWidth = true
        )
    }
}
