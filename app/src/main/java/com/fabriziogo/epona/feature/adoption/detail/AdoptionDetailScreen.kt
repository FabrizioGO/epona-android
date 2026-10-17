package com.fabriziogo.epona.feature.adoption.detail

import com.fabriziogo.epona.core.ui.text.ErrorSnackbarEffect
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AdoptionPost
import com.fabriziogo.epona.core.domain.model.AdoptionPostWithOwner
import com.fabriziogo.epona.core.domain.model.AdoptionStatus
import com.fabriziogo.epona.core.domain.model.AgeStage
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.ui.components.AdoptedStatusBadge
import com.fabriziogo.epona.core.ui.components.AdoptionBadge
import com.fabriziogo.epona.core.ui.components.AdoptionPriceBadge
import com.fabriziogo.epona.core.ui.components.EmptyState
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaOutlinedButton
import com.fabriziogo.epona.core.ui.components.EponaTonalButton
import com.fabriziogo.epona.core.ui.components.FullScreenProgress
import com.fabriziogo.epona.core.ui.components.emoji
import com.fabriziogo.epona.core.ui.components.formatTimeAgo
import com.fabriziogo.epona.core.ui.layout.constrainedWidth
import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.StatusBarIcons
import com.fabriziogo.epona.core.ui.theme.extendedColors

private val HeroContentHeight = 260.dp

@Composable
fun AdoptionDetailScreen(
    onNavigateBack: () -> Unit,
    onShare: (String, String) -> Unit,
    onDialPhone: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdoptionDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                AdoptionDetailNavEvent.NavigateBack -> onNavigateBack()
                is AdoptionDetailNavEvent.Share -> onShare(buildShareText(context, event.post), event.url)
                is AdoptionDetailNavEvent.DialPhone -> onDialPhone(event.phone)
            }
        }
    }

    ErrorSnackbarEffect(state.error, snackbarHostState) { viewModel.onEvent(AdoptionDetailEvent.ErrorDismissed) }

    if (state.showMarkAdoptedDialog) {
        EponaConfirmDialog(
            title = stringResource(R.string.adoption_mark_adopted_title),
            message = stringResource(R.string.adoption_mark_adopted_message),
            confirmText = stringResource(R.string.adoption_mark_adopted_confirm),
            onConfirm = { viewModel.onEvent(AdoptionDetailEvent.MarkAdoptedConfirmed) },
            onDismiss = { viewModel.onEvent(AdoptionDetailEvent.MarkAdoptedDismissed) }
        )
    }

    if (state.showDeleteDialog) {
        EponaConfirmDialog(
            title = stringResource(R.string.adoption_delete_title),
            message = stringResource(R.string.adoption_delete_message),
            confirmText = stringResource(R.string.action_delete),
            onConfirm = { viewModel.onEvent(AdoptionDetailEvent.DeleteConfirmed) },
            onDismiss = { viewModel.onEvent(AdoptionDetailEvent.DeleteDismissed) }
        )
    }

    AdoptionDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun AdoptionDetailContent(
    state: AdoptionDetailUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (AdoptionDetailEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        val detail = state.detail
        when {
            state.isLoading -> FullScreenProgress(
                modifier = Modifier.padding(padding).statusBarsPadding()
            )

            detail == null -> EmptyState(
                icon = Icons.Outlined.Warning,
                title = stringResource(R.string.detail_not_found_title),
                description = stringResource(R.string.detail_not_found_desc),
                modifier = Modifier.padding(padding).statusBarsPadding(),
                action = {
                    EponaFilledButton(
                        text = stringResource(R.string.action_go_back),
                        onClick = { onEvent(AdoptionDetailEvent.BackClicked) }
                    )
                }
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item(key = "hero") {
                    AdoptionHero(
                        post = detail.post,
                        onBackClick = { onEvent(AdoptionDetailEvent.BackClicked) },
                        onShareClick = { onEvent(AdoptionDetailEvent.ShareClicked) }
                    )
                }
                item(key = "body") {
                    AdoptionBody(
                        detail = detail,
                        isOwner = state.isOwner,
                        isUpdating = state.isUpdating,
                        onEvent = onEvent,
                        modifier = Modifier
                            .constrainedWidth()
                            .padding(horizontal = 20.dp)
                            .navigationBarsPadding()
                    )
                }
            }
        }
    }
}

@Composable
private fun AdoptionHero(
    post: AdoptionPost,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val buttonColors = IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
    )

    // White glyphs read against the scrim below regardless of the photo or the theme.
    StatusBarIcons(darkIcons = false)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HeroContentHeight + statusBarHeight)
            .background(MaterialTheme.extendedColors.adoptionContainer),
        contentAlignment = Alignment.Center
    ) {
        if (post.photoUrls.isEmpty()) {
            Text(
                text = post.species.emoji(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = MaterialTheme.typography.displayLarge.fontSize * 2
                )
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { post.photoUrls.size })
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = post.photoUrls[page],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (post.photoUrls.size > 1) {
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(post.photoUrls.size) { index ->
                        val active = pagerState.currentPage == index
                        Box(
                            Modifier
                                .size(if (active) 8.dp else 6.dp)
                                .background(
                                    if (active) Color.White else Color.White.copy(alpha = 0.6f),
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(statusBarHeight + 72.dp)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent)))
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 12.dp, top = 12.dp),
            colors = buttonColors
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
        }

        IconButton(
            onClick = onShareClick,
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(end = 12.dp, top = 12.dp),
            colors = buttonColors
        ) {
            Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.action_share))
        }
    }
}

@Composable
private fun AdoptionBody(
    detail: AdoptionPostWithOwner,
    isOwner: Boolean,
    isUpdating: Boolean,
    onEvent: (AdoptionDetailEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val post = detail.post
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AdoptionBadge()
            if (post.status == AdoptionStatus.ADOPTED) {
                AdoptedStatusBadge()
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = post.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(Modifier.height(8.dp))
        val postedBy = if (detail.ownerName.isNotBlank()) {
            stringResource(R.string.adoption_detail_posted_by, detail.ownerName, formatTimeAgo(post.createdAt))
        } else {
            formatTimeAgo(post.createdAt)
        }
        Text(
            text = postedBy,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))
        AdoptionPriceBadge(price = post.price)

        Spacer(Modifier.height(24.dp))
        FeaturesCard(post)

        if (!post.description.isNullOrBlank()) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.adoption_detail_about),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(32.dp))
        if (isOwner) {
            if (post.status != AdoptionStatus.ADOPTED) {
                EponaTonalButton(
                    text = stringResource(R.string.adoption_action_mark_adopted),
                    onClick = { onEvent(AdoptionDetailEvent.MarkAdoptedClicked) },
                    icon = Icons.Outlined.CheckCircle,
                    enabled = !isUpdating,
                    fullWidth = true
                )
                Spacer(Modifier.height(12.dp))
            }
            EponaOutlinedButton(
                text = stringResource(R.string.action_delete),
                onClick = { onEvent(AdoptionDetailEvent.DeleteClicked) },
                icon = Icons.Outlined.Delete,
                enabled = !isUpdating,
                fullWidth = true
            )
        } else if (post.status != AdoptionStatus.ADOPTED) {
            EponaFilledButton(
                text = stringResource(R.string.adoption_action_call),
                onClick = { onEvent(AdoptionDetailEvent.CallClicked) },
                icon = Icons.Outlined.Phone,
                fullWidth = true
            )
        }
    }
}

/** One row per detail the poster filled in; the card disappears when none were. */
@Composable
private fun FeaturesCard(post: AdoptionPost) {
    val rows = buildList {
        add(stringResource(R.string.pet_species_label) to stringResource(post.species.label))
        post.breed?.let { add(stringResource(R.string.pet_breed_label) to it) }
        post.petName?.let { add(stringResource(R.string.adoption_feature_name) to it) }
        post.ageStage?.let { add(stringResource(R.string.adoption_field_age) to stringResource(it.label(post.species))) }
        post.size?.let { add(stringResource(R.string.pet_size_label) to stringResource(it.label)) }
        post.gender?.let { add(stringResource(R.string.pet_gender_label) to stringResource(it.label)) }
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun buildShareText(context: Context, post: AdoptionPost): String =
    context.getString(R.string.share_text_adoption, post.title)

@EponaPreviews
@Composable
private fun AdoptionDetailPreview() {
    EponaTheme {
        AdoptionDetailContent(
            state = AdoptionDetailUiState(
                isLoading = false,
                detail = AdoptionPostWithOwner(
                    post = AdoptionPost(
                        id = "1",
                        title = "Golden Retriever puppies in adoption",
                        species = Species.DOG,
                        breed = "Golden Retriever",
                        ageStage = AgeStage.BABY,
                        description = "Six healthy puppies, vaccinated and dewormed.",
                        price = 50.0,
                        contactPhone = "0000-000-0000",
                        createdAt = System.currentTimeMillis() - 3_600_000
                    ),
                    ownerName = "Alex"
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {}
        )
    }
}
