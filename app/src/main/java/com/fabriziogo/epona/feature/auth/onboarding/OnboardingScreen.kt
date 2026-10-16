package com.fabriziogo.epona.feature.auth.onboarding

import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaOutlinedButton
import com.fabriziogo.epona.core.ui.components.EponaTextButton
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = listOf(
        OnboardingPage(
            emoji = "📢",
            title = stringResource(R.string.onboarding_title_1),
            description = stringResource(R.string.onboarding_desc_1)
        ),
        OnboardingPage(
            emoji = "👁️",
            title = stringResource(R.string.onboarding_title_2),
            description = stringResource(R.string.onboarding_desc_2)
        ),
        OnboardingPage(
            emoji = "🏠",
            title = stringResource(R.string.onboarding_title_3),
            description = stringResource(R.string.onboarding_desc_3)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage

    // Back steps to the previous page instead of leaving onboarding.
    BackHandler(enabled = currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (currentPage < pages.lastIndex) {
                EponaTextButton(
                    text = stringResource(R.string.onboarding_skip),
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pages.lastIndex) }
                    }
                )
            }
        }

        // Swipeable pages; the pager gives the transition and the swipe gesture.
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Big emoji illustration
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pages[page].emoji,
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.clearAndSetSemantics {}
                    )
                }

                Spacer(Modifier.height(40.dp))

                Text(
                    text = pages[page].title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = pages[page].description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Page indicators
        val pageDescription = stringResource(
            R.string.onboarding_page_progress, currentPage + 1, pages.size
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 32.dp)
                .semantics(mergeDescendants = true) { stateDescription = pageDescription }
        ) {
            pages.forEachIndexed { i, _ ->
                Box(
                    modifier = Modifier
                        .size(if (i == currentPage) 24.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == currentPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        // Action buttons
        if (currentPage < pages.lastIndex) {
            EponaFilledButton(
                text = stringResource(R.string.onboarding_next),
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                },
                fullWidth = true
            )
        } else {
            EponaFilledButton(
                text = stringResource(R.string.onboarding_get_started),
                onClick = onNavigateToRegister,
                fullWidth = true
            )

            Spacer(Modifier.height(12.dp))

            EponaOutlinedButton(
                text = stringResource(R.string.onboarding_has_account),
                onClick = onNavigateToLogin,
                fullWidth = true
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@EponaPreviews
@Composable
fun OnboardingScreenPreview() {
    EponaTheme(dynamicColor = false) {
        OnboardingScreen(
            onNavigateToLogin = {},
            onNavigateToRegister = {}
        )
    }
}