package com.fabriziogo.epona.feature.detail.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog

@Composable
fun ResolveDialog(
    petName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    EponaConfirmDialog(
        title = stringResource(R.string.resolve_title),
        message = stringResource(R.string.resolve_message, petName),
        confirmText = stringResource(R.string.resolve_confirm),
        dismissText = stringResource(R.string.resolve_dismiss),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}