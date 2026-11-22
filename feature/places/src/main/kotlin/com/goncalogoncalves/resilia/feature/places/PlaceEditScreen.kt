package com.goncalogoncalves.resilia.feature.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goncalogoncalves.resilia.core.designsystem.component.SectionTitle
import com.goncalogoncalves.resilia.core.designsystem.component.labelRes
import com.goncalogoncalves.resilia.core.domain.model.CoordinateParser
import com.goncalogoncalves.resilia.core.domain.model.Place
import com.goncalogoncalves.resilia.core.domain.model.PlaceType

@Composable
internal fun PlaceEditScreen(id: Long, onDone: () -> Unit, viewModel: PlacesViewModel = hiltViewModel()) {
    val places by viewModel.places.collectAsStateWithLifecycle()
    val all = places ?: return
    val existing = all.firstOrNull { it.id == id }
    if (id != 0L && existing == null) return

    var loaded by rememberSaveable { mutableStateOf(false) }
    var type by rememberSaveable { mutableStateOf(PlaceType.HOME) }
    var name by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var coordinates by rememberSaveable { mutableStateOf("") }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(existing) {
        if (!loaded) {
            existing?.let {
                type = it.type
                name = it.name
                address = it.address
                notes = it.notes
                coordinates = it.position?.format().orEmpty()
            }
            // A new place defaults to "home" only if there is no home yet.
            if (existing == null && all.any { it.type == PlaceType.HOME }) type = PlaceType.MEETING
            loaded = true
        }
    }

    val parsed = CoordinateParser.parse(coordinates)
    val coordinatesInvalid = coordinates.isNotBlank() && parsed == null
    val canSave = name.isNotBlank() && !coordinatesInvalid
    val clipboard = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionTitle(stringResource(R.string.place_type)) }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaceType.entries.forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t },
                        label = { Text(stringResource(t.labelRes())) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.place_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(stringResource(R.string.place_address)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = coordinates,
                onValueChange = { coordinates = it },
                label = { Text(stringResource(R.string.place_coordinates)) },
                isError = coordinatesInvalid,
                supportingText = {
                    Text(
                        when {
                            coordinatesInvalid -> stringResource(R.string.place_coordinates_invalid)
                            parsed != null -> stringResource(R.string.place_coordinates_read, parsed.format())
                            else -> stringResource(R.string.place_coordinates_help)
                        },
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            TextButton(
                onClick = { clipboard.getText()?.text?.let { coordinates = it } },
                modifier = Modifier.heightIn(min = 48.dp),
            ) { Text(stringResource(R.string.place_paste)) }
        }
        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.place_notes)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Text(
                stringResource(R.string.place_privacy),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Button(
                onClick = {
                    val place = (existing ?: Place(type = type, name = name)).copy(
                        type = type,
                        name = name,
                        address = address,
                        notes = notes,
                        position = parsed,
                    )
                    viewModel.save(place, onDone)
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) { Text(stringResource(R.string.place_save)) }
        }
        if (existing != null) {
            item {
                TextButton(onClick = { confirmDelete = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.place_delete), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.place_delete_title, existing.name)) },
            text = { Text(stringResource(R.string.place_delete_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete(existing.id, onDone)
                }) { Text(stringResource(R.string.place_delete)) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.place_cancel)) } },
        )
    }
}
