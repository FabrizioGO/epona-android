package com.fabriziogo.epona.feature.detail.components

import androidx.compose.runtime.Composable
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog

@Composable
fun ResolveDialog(
    petName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    EponaConfirmDialog(
        title = "Mark as reunited?",
        message = "This will mark $petName as found and close the alert. " +
            "Your community will be notified of the happy reunion!",
        confirmText = "Yes, we're reunited! 🎉",
        dismissText = "Not yet",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}