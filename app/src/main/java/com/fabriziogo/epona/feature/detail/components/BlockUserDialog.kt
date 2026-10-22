package com.fabriziogo.epona.feature.detail.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.fabriziogo.epona.R

/**
 * Confirms blocking [userName]. Blocking hides that user's alerts, sightings and
 * adoption posts from the blocker (and the blocker's from them), and stops
 * notifications between the two; it can be undone from Profile > Blocked users.
 */
@Composable
fun BlockUserDialog(
    userName: String,
    isBlocking: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isBlocking) onDismiss() },
        title = { Text(stringResource(R.string.block_dialog_title, userName)) },
        text = { Text(stringResource(R.string.block_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isBlocking) {
                Text(stringResource(R.string.block_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBlocking) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
