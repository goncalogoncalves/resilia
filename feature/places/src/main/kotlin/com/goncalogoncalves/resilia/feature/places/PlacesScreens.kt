package com.goncalogoncalves.resilia.feature.places

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.HubCard
import com.goncalogoncalves.resilia.core.designsystem.component.SectionTitle
import com.goncalogoncalves.resilia.core.designsystem.component.copyToClipboard
import com.goncalogoncalves.resilia.core.designsystem.component.describe
import com.goncalogoncalves.resilia.core.designsystem.component.icon
import com.goncalogoncalves.resilia.core.designsystem.component.labelRes
import com.goncalogoncalves.resilia.core.designsystem.component.openInMaps
import com.goncalogoncalves.resilia.core.domain.model.Geo
import com.goncalogoncalves.resilia.core.domain.model.Place
import kotlinx.serialization.Serializable

@Serializable
data object PlacesRoute

/** [id] 0 = new place. */
@Serializable
data class PlaceEditRoute(val id: Long = 0)

@Serializable
data object HospitalsRoute

fun NavGraphBuilder.placesScreens(onOpen: (Any) -> Unit, onBack: () -> Unit) {
    composable<PlacesRoute> { PlacesScreen(onOpen) }
    composable<PlaceEditRoute> { entry -> PlaceEditScreen(entry.toRoute<PlaceEditRoute>().id, onDone = onBack) }
    composable<HospitalsRoute> { HospitalsScreen() }
}

@Composable
internal fun PlacesScreen(onOpen: (Any) -> Unit, viewModel: PlacesViewModel = hiltViewModel()) {
    val places by viewModel.places.collectAsStateWithLifecycle()
    PlacesContent(places.orEmpty(), onOpen)
}

@Composable
internal fun PlacesContent(places: List<Place>, onOpen: (Any) -> Unit) {
    val home = places.home()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(stringResource(R.string.places_intro), style = MaterialTheme.typography.bodyLarge) }
        item {
            Button(onClick = { onOpen(PlaceEditRoute()) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text(stringResource(R.string.places_add), modifier = Modifier.padding(start = 8.dp))
            }
        }
        if (places.isEmpty()) {
            item { Text(stringResource(R.string.places_empty), style = MaterialTheme.typography.bodyMedium) }
        }
        items(places, key = { it.id }) { p -> PlaceCard(p, home, onEdit = { onOpen(PlaceEditRoute(p.id)) }) }
        if (places.isNotEmpty() && home == null) {
            item {
                Text(
                    stringResource(R.string.places_home_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { SectionTitle(stringResource(R.string.places_official_title), Modifier.padding(top = 8.dp)) }
        item {
            HubCard(
                title = stringResource(R.string.hospitals_title),
                subtitle = stringResource(R.string.hospitals_subtitle),
                icon = Icons.Outlined.LocalHospital,
                onClick = { onOpen(HospitalsRoute) },
                trailing = { Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null) },
            )
        }
        item {
            Text(
                stringResource(R.string.places_no_map_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { DisclaimerFooter() }
    }
}

/** One place: used in the list and (bigger) in Emergency mode. */
@Composable
internal fun PlaceCard(place: Place, home: Place?, onEdit: (() -> Unit)?) {
    val context = LocalContext.current
    val distance = if (place.id != home?.id) Geo.fromHome(home?.position, place.position) else null
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(place.type.icon(), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(place.type.labelRes()),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(place.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                }
            }
            if (place.address.isNotBlank()) Text(place.address, style = MaterialTheme.typography.bodyLarge)
            distance?.let { Text(context.describe(it), style = MaterialTheme.typography.bodyMedium) }
            if (place.notes.isNotBlank()) {
                Text(place.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (place.position != null || place.address.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            if (!context.openInMaps(place.name, place.position, place.address)) {
                                Toast.makeText(context, R.string.places_no_maps_app, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Icon(Icons.Outlined.Map, contentDescription = null)
                        Text(stringResource(R.string.places_open_maps), modifier = Modifier.padding(start = 8.dp))
                    }
                }
                place.position?.let { pos ->
                    OutlinedButton(
                        onClick = {
                            context.copyToClipboard(place.name, pos.format())
                            Toast.makeText(context, R.string.places_copied, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) { Text(stringResource(R.string.places_copy_coordinates)) }
                }
                if (onEdit != null) {
                    OutlinedButton(onClick = onEdit, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.places_edit))
                    }
                }
            }
        }
    }
}
