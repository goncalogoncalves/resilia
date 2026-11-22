package com.goncalogoncalves.resilia.feature.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.goncalogoncalves.resilia.core.designsystem.component.BigActionButton

/** "My places" inside Emergency mode (decision 4): read-only, big buttons, no navigation. */
@Composable
fun EmergencyPlacesSheet(onClose: () -> Unit, viewModel: PlacesViewModel = hiltViewModel()) {
    val places by viewModel.places.collectAsStateWithLifecycle()
    val list = places.orEmpty()
    val home = list.home()
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                BigActionButton(
                    text = stringResource(R.string.places_back),
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    onClick = onClose,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
            item {
                Text(
                    stringResource(R.string.places_emergency_title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() },
                )
            }
            if (places != null && list.isEmpty()) {
                item { Text(stringResource(R.string.places_emergency_empty), style = MaterialTheme.typography.bodyLarge) }
            }
            items(list, key = { it.id }) { PlaceCard(it, home, onEdit = null) }
        }
    }
}
