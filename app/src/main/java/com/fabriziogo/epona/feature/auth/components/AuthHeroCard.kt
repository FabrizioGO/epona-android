package com.fabriziogo.epona.feature.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R

/**
 * Shared auth-screen frame: a teal hero (title/subtitle) that transitions into a
 * rounded-top sheet holding the form, mirroring the hero-into-sheet pattern used
 * on [com.fabriziogo.epona.feature.home.HomeScreen].
 */
@Composable
fun AuthHeroCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = if (onBackClick != null) 4.dp else 32.dp, bottom = 28.dp)
        ) {
            onBackClick?.let { back ->
                IconButton(onClick = back) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = Color.White
                    )
                }
            }

            AuthHeader(title = title, subtitle = subtitle)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .padding(horizontal = 24.dp, vertical = 28.dp),
            content = content
        )
    }
}
