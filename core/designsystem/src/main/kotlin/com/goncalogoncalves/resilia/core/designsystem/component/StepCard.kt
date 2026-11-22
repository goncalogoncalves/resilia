package com.goncalogoncalves.resilia.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.goncalogoncalves.resilia.core.designsystem.R
import com.goncalogoncalves.resilia.core.domain.model.Source
import com.goncalogoncalves.resilia.core.domain.model.Step

/**
 * One numbered step with its detail, note, review badge and sources. Shared by the
 * scenario guides and the tool guides (e.g. battery), so every step looks and behaves
 * the same: no source → "No official source"; only non-official sources → "Not an
 * official source", next to the link.
 */
@Composable
fun StepCard(
    number: Int,
    step: Step,
    sources: List<Source>,
    modifier: Modifier = Modifier,
    /** Names of the kit items this step mentions (`relatedKitItemIds`), already resolved. */
    kitItems: List<String> = emptyList(),
    onOpenKit: (() -> Unit)? = null,
) {
    val stepLabel = stringResource(R.string.ds_step_number, number)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "$number.",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .widthIn(min = 32.dp)
                    .semantics { contentDescription = stepLabel },
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(step.text.text(), style = MaterialTheme.typography.bodyLarge)
                step.detail?.let {
                    Text(it.text(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                step.note?.let {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(it.text(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                val label = when {
                    sources.isEmpty() -> R.string.ds_no_source
                    sources.none { it.entity.isOfficial } -> R.string.ds_unofficial_source
                    else -> null
                }
                label?.let {
                    Text(
                        stringResource(it),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (kitItems.isNotEmpty()) {
                    val kitText = stringResource(R.string.ds_in_kit, kitItems.joinToString(", "))
                    if (onOpenKit != null) {
                        TextButton(onClick = onOpenKit, modifier = Modifier.heightIn(min = 48.dp)) {
                            Icon(Icons.Outlined.Backpack, contentDescription = null)
                            Text(kitText, modifier = Modifier.padding(start = 8.dp))
                        }
                    } else {
                        Text(kitText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                SourcesLink(sources)
            }
        }
    }
}
