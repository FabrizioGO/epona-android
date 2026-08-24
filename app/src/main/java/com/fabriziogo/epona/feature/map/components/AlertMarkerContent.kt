package com.fabriziogo.epona.feature.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.ui.theme.EponaColors

@Composable
fun AlertMarkerContent(
    alertWithDetails: AlertWithDetails,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val alert = alertWithDetails.alert
    val pet = alertWithDetails.pet

    val containerColor = if (alert.type == AlertType.LOST) {
        EponaColors.LostContainer
    } else {
        EponaColors.FoundContainer
    }

    val borderColor = if (alert.type == AlertType.LOST) {
        EponaColors.Lost
    } else {
        EponaColors.Found
    }

    val emoji = when (pet.species.value) {
        "dog" -> "🐕"
        "cat" -> "🐈"
        "bird" -> "🐦"
        "rabbit" -> "🐇"
        else -> "🐾"
    }

    val shape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .shadow(if (isSelected) 8.dp else 4.dp, shape)
            .clip(shape)
            .background(containerColor)
            .border(
                width = if (isSelected) 3.dp else 1.5.dp,
                color = borderColor,
                shape = shape
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = if (isSelected) 20.sp else 16.sp
        )
    }
}