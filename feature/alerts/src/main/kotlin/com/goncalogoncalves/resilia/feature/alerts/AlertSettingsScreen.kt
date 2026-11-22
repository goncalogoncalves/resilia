package com.goncalogoncalves.resilia.feature.alerts

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goncalogoncalves.resilia.core.designsystem.component.SectionTitle
import com.goncalogoncalves.resilia.core.designsystem.component.text
import com.goncalogoncalves.resilia.core.domain.model.AlertRegion
import com.goncalogoncalves.resilia.core.domain.model.AlertSettings
import com.goncalogoncalves.resilia.core.domain.model.AlertZone

@Composable
internal fun AlertSettingsScreen(viewModel: AlertsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state ?: return
    val context = LocalContext.current
    val labels = s.bundle.alerts ?: return
    val settings = s.settings
    val zone = settings.zone

    // Notifications are asked for only when the person chooses a zone (never at start-up).
    var permissionTick by remember { mutableIntStateOf(0) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionTick++ }
    fun askNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !AlertNotifier.canNotify(context)) {
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val canNotify = remember(permissionTick, settings) { AlertNotifier.canNotify(context) }

    fun choose(areaId: String) {
        if (zone == null && settings.intervalHours > 0) askNotifications()
        viewModel.setZone(AlertZone(areaId, dico = zone?.dico?.takeIf { labels.municipality(it)?.areaId == areaId }))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item { Text(stringResource(R.string.alerts_privacy), style = MaterialTheme.typography.bodyMedium) }

        // --- Interval (decision 2)
        item { SectionTitle(stringResource(R.string.alerts_interval_title), Modifier.padding(top = 12.dp)) }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlertSettings.INTERVAL_OPTIONS.forEach { hours ->
                    FilterChip(
                        selected = settings.intervalHours == hours,
                        onClick = {
                            if (hours > 0 && zone != null) askNotifications()
                            viewModel.setInterval(hours)
                        },
                        label = {
                            Text(
                                if (hours == 0) stringResource(R.string.alerts_interval_off)
                                else stringResource(R.string.alerts_interval_hours, hours),
                            )
                        },
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
            }
        }
        item {
            Text(
                stringResource(if (settings.intervalHours == 0) R.string.alerts_interval_off_note else R.string.alerts_interval_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .toggleable(settings.notifyYellow, role = Role.Switch) { viewModel.setNotifyYellow(it) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.alerts_notify_yellow), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = settings.notifyYellow, onCheckedChange = null)
            }
        }
        if (zone != null && settings.intervalHours > 0 && !canNotify) {
            item {
                Text(stringResource(R.string.alerts_notifications_off), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            item {
                OutlinedButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                            )
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.alerts_allow_notifications)) }
            }
        }

        // --- Municipality (fire risk, mainland only)
        val area = zone?.let { labels.area(it.areaId) }
        if (zone != null && area?.region == AlertRegion.CONTINENT) {
            item { SectionTitle(stringResource(R.string.alerts_municipality_title), Modifier.padding(top = 12.dp)) }
            item {
                val options = labels.municipalities.filter { it.areaId == zone.areaId }
                    .sortedWith(compareBy(java.text.Collator.getInstance(java.util.Locale.forLanguageTag("pt-PT"))) { it.name })
                var open by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Text(
                            zone.dico?.let { labels.municipality(it)?.name } ?: stringResource(R.string.alerts_municipality_none),
                            modifier = Modifier.weight(1f),
                        )
                        Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.alerts_municipality_none)) },
                            onClick = {
                                open = false
                                viewModel.setZone(zone.copy(dico = null))
                            },
                        )
                        options.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name) },
                                onClick = {
                                    open = false
                                    viewModel.setZone(zone.copy(dico = m.dico))
                                },
                            )
                        }
                    }
                }
            }
            item {
                Text(stringResource(R.string.alerts_municipality_note), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // --- Zone (decision 1: chosen from a list, no location permission)
        item { SectionTitle(stringResource(R.string.alerts_zone_title), Modifier.padding(top = 12.dp)) }
        AlertRegion.entries.forEach { region ->
            item {
                Text(
                    stringResource(
                        when (region) {
                            AlertRegion.CONTINENT -> R.string.alerts_region_continent
                            AlertRegion.MADEIRA -> R.string.alerts_region_madeira
                            AlertRegion.AZORES -> R.string.alerts_region_azores
                        },
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(labels.areas.filter { it.region == region }, key = { it.id }) { a ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .selectable(selected = zone?.areaId == a.id, role = Role.RadioButton) { choose(a.id) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = zone?.areaId == a.id, onClick = null)
                    Text(a.name.text(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
                }
            }
        }
        if (zone != null) {
            item {
                TextButton(onClick = { viewModel.setZone(null) }, modifier = Modifier.heightIn(min = 48.dp).padding(top = 8.dp)) {
                    Text(stringResource(R.string.alerts_zone_remove))
                }
            }
        }
    }
}
