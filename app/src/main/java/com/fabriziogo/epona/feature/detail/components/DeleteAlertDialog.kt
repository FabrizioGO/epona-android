package com.fabriziogo.epona.feature.detail.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog

@Composable
fun DeleteAlertDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    EponaConfirmDialog(
        title = stringResource(R.string.detail_delete_alert_title),
        message = stringResource(R.string.detail_delete_alert_message),
        confirmText = stringResource(R.string.action_delete),
        dismissText = stringResource(R.string.action_cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
