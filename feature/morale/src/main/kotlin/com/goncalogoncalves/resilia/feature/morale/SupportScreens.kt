package com.goncalogoncalves.resilia.feature.morale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Elderly
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goncalogoncalves.resilia.core.designsystem.component.ContactCard
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.HubCard
import com.goncalogoncalves.resilia.core.designsystem.component.StepCard
import com.goncalogoncalves.resilia.core.designsystem.component.text
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle

/** Emotional support hub: one card per short guide, then the help lines. */
@Composable
internal fun SupportScreen(onOpenGuide: (String) -> Unit, viewModel: MoraleViewModel = hiltViewModel()) {
    val bundle by viewModel.bundle.collectAsStateWithLifecycle()
    val b = bundle ?: return
    val support = b.support ?: return
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(support.note.text(), style = MaterialTheme.typography.bodyLarge) }
        items(support.guides, key = { it.id }) { g ->
            HubCard(
                title = g.title.text(),
                icon = supportIcon(g.id),
                subtitle = g.summary.text(),
                onClick = { onOpenGuide(g.id) },
            )
        }
        helpLines(b)
        item { DisclaimerFooter() }
    }
}

@Composable
internal fun SupportGuideScreen(guideId: String, viewModel: MoraleViewModel = hiltViewModel()) {
    val bundle by viewModel.bundle.collectAsStateWithLifecycle()
    val b = bundle ?: return
    val support = b.support ?: return
    val guide = support.guides.firstOrNull { it.id == guideId } ?: return
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    guide.title.text(),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Text(guide.summary.text(), style = MaterialTheme.typography.bodyLarge)
                Text(
                    support.note.text(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        itemsIndexed(guide.steps, key = { _, s -> s.id }) { i, step ->
            StepCard(i + 1, step, step.sourceIds.mapNotNull(b::source))
        }
        helpLines(b)
        item { DisclaimerFooter() }
    }
}

private fun LazyListScope.helpLines(b: ContentBundle) {
    val contacts = b.support?.contactIds.orEmpty().mapNotNull(b::contact)
    if (contacts.isEmpty()) return
    item {
        Text(
            stringResource(R.string.morale_help_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp).semantics { heading() },
        )
    }
    items(contacts, key = { "c-" + it.id }) { c ->
        ContactCard(contact = c, sources = c.sourceIds.mapNotNull(b::source))
    }
}

private fun supportIcon(id: String) = when (id) {
    "support-children" -> Icons.Outlined.ChildCare
    "support-older" -> Icons.Outlined.Elderly
    "support-flashbacks" -> Icons.Outlined.Psychology
    "support-warning" -> Icons.Outlined.WarningAmber
    else -> Icons.Outlined.Favorite
}
