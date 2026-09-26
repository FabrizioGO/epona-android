package com.fabriziogo.epona.feature.auth.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.fabriziogo.epona.core.ui.theme.EponaTypography

/**
 * Renders text containing one or more `<a href="...">` links, opened through the
 * platform [androidx.compose.ui.platform.LocalUriHandler] when tapped — Compose's
 * [Text] composable handles [LinkAnnotation.Url] clicks itself, so no click
 * handling is wired up here.
 *
 * [text] must already have its `%1$s`/`%2$s` URL placeholders substituted (via
 * `stringResource(id, url1, url2)`) before it reaches this composable, since the
 * href values themselves come from those placeholders.
 */
@Composable
fun LegalLinksText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = EponaTypography.bodySmall,
    color: Color = MaterialTheme.colorScheme.outline,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    textAlign: TextAlign = TextAlign.Center
) {
    val annotated = remember(text, linkColor) {
        AnnotatedString.fromHtml(
            htmlString = text,
            linkStyles = TextLinkStyles(
                style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
            )
        )
    }

    Text(
        text = annotated,
        style = style,
        color = color,
        textAlign = textAlign,
        modifier = modifier
    )
}
