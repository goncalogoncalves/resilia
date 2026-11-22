package com.goncalogoncalves.resilia.feature.emergency

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goncalogoncalves.resilia.core.designsystem.component.BigActionButton
import com.goncalogoncalves.resilia.core.designsystem.component.ContactCard
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.text
import com.goncalogoncalves.resilia.core.designsystem.theme.EmergencyTheme
import com.goncalogoncalves.resilia.core.domain.EmergencyNumbers
import com.goncalogoncalves.resilia.core.domain.model.Scenario
import com.goncalogoncalves.resilia.core.domain.model.Step

/**
 * Emergency mode root. Shown INSTEAD of the normal navigation while active.
 * Rules: black background, big type, no animations, only essentials.
 */
@Composable
fun EmergencyRoot(
    onOpenPlan: () -> Unit = {},
    onOpenActivities: () -> Unit = {},
    onOpenPlaces: () -> Unit = {},
    viewModel: EmergencyViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val torchOn by viewModel.torchOn.collectAsStateWithLifecycle()
    val context = LocalContext.current

    EmergencyTheme {
        EmergencyContent(
            state = state,
            torchAvailable = viewModel.torchAvailable,
            torchOn = torchOn,
            onToggleTorch = viewModel::toggleTorch,
            onCall112 = {
                // ACTION_DIAL opens the dialer pre-filled: no CALL_PHONE permission, the user confirms.
                val intent = Intent(Intent.ACTION_DIAL, "tel:${EmergencyNumbers.EUROPEAN_EMERGENCY}".toUri())
                try {
                    context.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    // No dialer (e.g. some tablets): nothing else we can do; the number is on screen.
                }
            },
            onSelectScenario = viewModel::selectScenario,
            onExit = viewModel::exit,
            onOpenPlan = onOpenPlan,
            onOpenActivities = onOpenActivities,
            onOpenPlaces = onOpenPlaces,
        )
    }
}

@Composable
fun EmergencyContent(
    state: EmergencyUiState,
    torchAvailable: Boolean,
    torchOn: Boolean,
    onToggleTorch: () -> Unit,
    onCall112: () -> Unit,
    onSelectScenario: (String) -> Unit,
    onExit: () -> Unit,
    onOpenPlan: () -> Unit = {},
    onOpenActivities: () -> Unit = {},
    onOpenPlaces: () -> Unit = {},
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    var showAllSteps by rememberSaveable { mutableStateOf(false) }
    var showContacts by rememberSaveable { mutableStateOf(false) }
    val active = state.active
    val steps: List<Step> = when {
        active == null -> emptyList()
        showAllSteps -> active.phases.during
        else -> active.emergencySteps().ifEmpty { active.phases.during }
    }

    // Surface sets the content colour (white); without it text defaults to black on black.
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
    // "Call 112" is pinned below the list so it is always on screen, even with very large text
    // (Marco 9a audit: at 200% font it had scrolled out of view).
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
    LazyColumn(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .testTag("emergency_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.emergency_mode_label),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    active?.title?.text().orEmpty(),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() },
                )
                if (state.scenarios.size > 1) {
                    TextButton(onClick = { showPicker = true }) {
                        Text(stringResource(R.string.emergency_change_scenario), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        item {
            Text(
                stringResource(R.string.emergency_now),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
        }
        itemsIndexed(steps, key = { _, s -> s.id }) { index, step ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${index + 1}.", style = MaterialTheme.typography.titleLarge, modifier = Modifier.widthIn(min = 36.dp))
                Text(step.text.text(), style = MaterialTheme.typography.bodyLarge)
            }
        }
        if (active != null && active.phases.during.size > active.emergencySteps().size) {
            item {
                TextButton(onClick = { showAllSteps = !showAllSteps }) {
                    Text(
                        stringResource(if (showAllSteps) R.string.emergency_show_essential else R.string.emergency_show_all),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        item {
            BigActionButton(
                text = stringResource(
                    when {
                        !torchAvailable -> R.string.emergency_torch_unavailable
                        torchOn -> R.string.emergency_torch_off
                        else -> R.string.emergency_torch_on
                    },
                ),
                icon = if (torchOn) Icons.Outlined.FlashlightOff else Icons.Outlined.FlashlightOn,
                onClick = onToggleTorch,
                enabled = torchAvailable,
            )
        }
        if (state.contacts.isNotEmpty()) {
            item {
                BigActionButton(
                    text = stringResource(if (showContacts) R.string.emergency_contacts_hide else R.string.emergency_contacts),
                    icon = Icons.Outlined.Contacts,
                    onClick = { showContacts = !showContacts },
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (showContacts) {
            items(state.contacts, key = { "contact-" + it.id }) { c ->
                ContactCard(contact = c, sources = c.sourceIds.mapNotNull { state.sources[it] })
            }
        }
        item {
            BigActionButton(
                text = stringResource(R.string.emergency_plan),
                icon = Icons.Outlined.FamilyRestroom,
                onClick = onOpenPlan,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            BigActionButton(
                text = stringResource(R.string.emergency_places),
                icon = Icons.Outlined.Place,
                onClick = onOpenPlaces,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            BigActionButton(
                text = stringResource(R.string.emergency_activities),
                icon = Icons.Outlined.SelfImprovement,
                onClick = onOpenActivities,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        }
        item { DisclaimerFooter() }
        item {
            TextButton(onClick = { confirmExit = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.emergency_exit), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
    BigActionButton(
        text = stringResource(R.string.emergency_call_112),
        icon = Icons.Outlined.Call,
        onClick = onCall112,
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    }

    }

    if (showPicker) {
        ScenarioPicker(
            scenarios = state.scenarios,
            onPick = {
                onSelectScenario(it)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text(stringResource(R.string.emergency_exit_confirm_title)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmExit = false
                    onExit()
                }) { Text(stringResource(R.string.emergency_exit_confirm_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmExit = false }) { Text(stringResource(R.string.emergency_exit_confirm_no)) }
            },
        )
    }
}

@Composable
private fun ScenarioPicker(scenarios: List<Scenario>, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.emergency_change_scenario)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                scenarios.forEach { s ->
                    item(key = s.id) {
                        BigActionButton(
                            text = s.title.text(),
                            onClick = { onPick(s.id) },
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.emergency_exit_confirm_no)) }
        },
    )
}
