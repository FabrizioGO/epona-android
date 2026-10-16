package com.fabriziogo.epona.feature.profile.help

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.fabriziogo.epona.core.ui.layout.constrainedWidth
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaCard
import com.fabriziogo.epona.core.ui.components.EponaOutlinedButton
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar

private data class FaqItem(val question: String, val answer: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val faqItems = listOf(
        FaqItem(
            stringResource(R.string.faq_q1_question),
            stringResource(R.string.faq_q1_answer)
        ),
        FaqItem(
            stringResource(R.string.faq_q2_question),
            stringResource(R.string.faq_q2_answer)
        ),
        FaqItem(
            stringResource(R.string.faq_q3_question),
            stringResource(R.string.faq_q3_answer)
        ),
        FaqItem(
            stringResource(R.string.faq_q4_question),
            stringResource(R.string.faq_q4_answer)
        ),
        FaqItem(
            stringResource(R.string.faq_q5_question),
            stringResource(R.string.faq_q5_answer)
        )
    )

    Scaffold(
        topBar = {
            EponaTopAppBar(
                title = stringResource(R.string.profile_help),
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .constrainedWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.help_faq_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(12.dp))

            faqItems.forEach { faq ->
                FaqCard(faq)
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(12.dp))

            EponaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.help_contact_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.help_contact_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    val emailSubject = stringResource(R.string.help_email_subject)
                    EponaOutlinedButton(
                        text = stringResource(R.string.help_email_support),
                        icon = Icons.Outlined.Email,
                        fullWidth = true,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@epona.app")
                                putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FaqCard(faq: FaqItem, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val expandedText = stringResource(R.string.state_expanded)
    val collapsedText = stringResource(R.string.state_collapsed)

    EponaCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { stateDescription = if (expanded) expandedText else collapsedText },
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
