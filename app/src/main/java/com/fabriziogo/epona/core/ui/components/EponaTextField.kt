package com.fabriziogo.epona.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.fabriziogo.epona.R

/**
 * Outlined text field used by every form.
 *
 * [imeAction] defaults to Next for single-line fields and to the keyboard's default (Enter,
 * new line) for multiline ones. [optional] appends the localized "(optional)" suffix to the
 * label. [characterCount] is shown at the end of the supporting row and stays visible while
 * an error is showing.
 */
@Composable
fun EponaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    supportingText: String? = null,
    errorText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction? = null,
    onImeAction: () -> Unit = {},
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 5,
    minLines: Int = 1,
    optional: Boolean = false,
    characterCount: Pair<Int, Int>? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    focusRequester: FocusRequester? = null
) {
    val focusManager = LocalFocusManager.current
    val resolvedLabel =
        if (optional) stringResource(R.string.form_label_optional, label) else label
    val resolvedIme = imeAction ?: if (singleLine) ImeAction.Next else ImeAction.Default

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(resolvedLabel) },
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder) }
            } else null,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            supportingText = if (errorText != null || supportingText != null || characterCount != null) {
                {
                    Row(Modifier.fillMaxWidth()) {
                        when {
                            errorText != null -> Text(
                                errorText,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            supportingText != null ->
                                Text(supportingText, modifier = Modifier.weight(1f))
                            else -> Spacer(Modifier.weight(1f))
                        }
                        characterCount?.let { (count, max) -> Text("$count/$max") }
                    }
                }
            } else null,
            isError = errorText != null,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = resolvedIme
            ),
            // onNext must keep moving focus: a custom handler replaces the default behaviour,
            // so run the caller's action and then advance.
            keyboardActions = KeyboardActions(
                onDone = { onImeAction() },
                onNext = {
                    onImeAction()
                    focusManager.moveFocus(FocusDirection.Down)
                },
                onSearch = { onImeAction() }
            ),
            visualTransformation = visualTransformation,
            enabled = enabled,
            readOnly = readOnly,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
        )
    }
}
