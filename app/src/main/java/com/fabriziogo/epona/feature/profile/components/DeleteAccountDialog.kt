package com.fabriziogo.epona.feature.profile.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog

@Composable
fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    EponaConfirmDialog(
        title = stringResource(R.string.profile_delete_account_title),
        message = stringResource(R.string.profile_delete_account_message),
        confirmText = stringResource(R.string.action_delete),
        dismissText = stringResource(R.string.action_cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
