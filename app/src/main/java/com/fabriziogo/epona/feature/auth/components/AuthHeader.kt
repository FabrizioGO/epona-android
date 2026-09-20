package com.fabriziogo.epona.feature.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.theme.EponaTypography

/**
 * Title block for the teal [AuthHeroCard] header: a circular paw badge over a
 * title/subtitle pair. Always renders in white — it only ever sits on the
 * app's teal primaryContainer hero, never on a surface background.
 */
@Composable
fun AuthHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = title,
            style = EponaTypography.headlineLarge,
            color = Color.White
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = subtitle,
            style = EponaTypography.bodyLarge,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}
