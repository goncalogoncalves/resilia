package com.goncalogoncalves.resilia.feature.places

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goncalogoncalves.resilia.core.designsystem.component.ContactCard
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.SectionTitle
import com.goncalogoncalves.resilia.core.designsystem.component.SourcesLink
import com.goncalogoncalves.resilia.core.designsystem.component.copyToClipboard
import com.goncalogoncalves.resilia.core.designsystem.component.formatIsoDate
import com.goncalogoncalves.resilia.core.designsystem.component.openInMaps
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.Hospital
import com.goncalogoncalves.resilia.core.domain.model.UrgencyLevel
import java.text.Collator
import java.text.Normalizer
import java.util.Locale

@Composable
internal fun HospitalsScreen(viewModel: PlacesViewModel = hiltViewModel()) {
    val bundle by viewModel.bundle.collectAsStateWithLifecycle()
    HospitalsContent(bundle ?: return)
}

private fun String.folded(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)

/** Matches name, locality, address, postal code or group, ignoring accents and case. */
internal fun Hospital.matches(query: String): Boolean {
    val q = query.trim().folded()
    if (q.isEmpty()) return true
    return listOf(name, locality, address, postalCode, group).any { q in it.folded() }
}

@Composable
internal fun HospitalsContent(bundle: ContentBundle) {
    val data = bundle.hospitals ?: return
    var query by rememberSaveable { mutableStateOf("") }
    val collator = Collator.getInstance(Locale.forLanguageTag("pt-PT"))
    // No distances: the dataset's coordinates were not reliable (decision taken with you in Marco 8a).
    val byRegion = data.hospitals.filter { it.matches(query) }
        .groupBy { it.region }
        .toSortedMap(collator)
        .mapValues { (_, list) -> list.sortedWith(compareBy(collator) { it.locality + " " + it.name }) }
    val sources = data.sourceIds.mapNotNull(bundle::source)
    val locale = LocalConfiguration.current.locales[0]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Decision 3: 112 and SNS 24 always on top, with their own sources.
        listOf("emergency-112", "sns24").mapNotNull(bundle::contact).forEach { c ->
            item(key = "contact-" + c.id) { ContactCard(contact = c, sources = c.sourceIds.mapNotNull(bundle::source)) }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.hospitals_data_line, formatIsoDate(data.dataModified, locale)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.hospitals_search)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (byRegion.isEmpty()) {
            item { Text(stringResource(R.string.hospitals_none), style = MaterialTheme.typography.bodyLarge) }
        }
        byRegion.forEach { (region, list) ->
            item(key = "region-$region") { SectionTitle(region, Modifier.padding(top = 8.dp)) }
            items(list, key = { it.id }) { HospitalCard(it) }
        }
        item {
            Text(
                stringResource(R.string.hospitals_no_coordinates),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { SourcesLink(sources) }
        item { DisclaimerFooter() }
    }
}

@Composable
private fun HospitalCard(h: Hospital) {
    val context = LocalContext.current
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(h.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            Text(stringResource(h.urgency.labelRes()), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text(h.fullAddress, style = MaterialTheme.typography.bodyLarge)
            if (h.group.isNotBlank() && h.group != h.name) {
                Text(h.group, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                OutlinedButton(
                    onClick = {
                        if (!context.openInMaps(h.name, null, "${h.name}, ${h.fullAddress}")) {
                            Toast.makeText(context, R.string.places_no_maps_app, Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Icon(Icons.Outlined.Map, contentDescription = null)
                    Text(stringResource(R.string.places_open_maps), modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(
                    onClick = {
                        context.copyToClipboard(h.name, h.fullAddress)
                        Toast.makeText(context, R.string.places_copied, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.hospitals_copy_address)) }
            }
        }
    }
}

internal fun UrgencyLevel.labelRes(): Int = when (this) {
    UrgencyLevel.POLYVALENT_TRAUMA -> R.string.urgency_polyvalent_trauma
    UrgencyLevel.POLYVALENT -> R.string.urgency_polyvalent
    UrgencyLevel.MEDICAL_SURGICAL -> R.string.urgency_medical_surgical
    UrgencyLevel.BASIC -> R.string.urgency_basic
}
