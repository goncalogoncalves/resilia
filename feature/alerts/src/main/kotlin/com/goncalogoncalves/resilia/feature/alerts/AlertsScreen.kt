package com.goncalogoncalves.resilia.feature.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.SectionTitle
import com.goncalogoncalves.resilia.core.designsystem.component.SourcesLink
import com.goncalogoncalves.resilia.core.designsystem.component.WarningLabel
import com.goncalogoncalves.resilia.core.designsystem.component.currentContentLanguage
import com.goncalogoncalves.resilia.core.designsystem.component.fireRiskColor
import com.goncalogoncalves.resilia.core.designsystem.component.openWebPage
import com.goncalogoncalves.resilia.core.designsystem.component.text
import com.goncalogoncalves.resilia.core.designsystem.component.warningColor
import com.goncalogoncalves.resilia.core.domain.model.AlertRegion
import com.goncalogoncalves.resilia.core.domain.model.Alerts
import com.goncalogoncalves.resilia.core.domain.model.ContentLanguage
import com.goncalogoncalves.resilia.core.domain.model.FireRisk
import com.goncalogoncalves.resilia.core.domain.model.Warning
import java.time.ZoneId

@Composable
internal fun AlertsScreen(
    onOpenScenario: (String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: AlertsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state ?: return
    AlertsContent(s, onRefresh = viewModel::refresh, onOpenScenario = onOpenScenario, onOpenSettings = onOpenSettings)
}

@Composable
internal fun AlertsContent(
    state: AlertsUiState,
    onRefresh: () -> Unit,
    onOpenScenario: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val labels = state.bundle.alerts
    val zone = state.settings.zone
    val area = zone?.let { labels?.area(it.areaId) }
    val snapshot = state.snapshot
    val warnings = zone?.let { Alerts.active(snapshot.warnings, it.areaId, state.now) }.orEmpty()
    val fire = Alerts.fireRiskFor(snapshot.fireRisk, zone?.dico)
    val sources = labels?.sourceIds?.mapNotNull(state.bundle::source).orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (zone == null || area == null) {
            item { Text(stringResource(R.string.alerts_intro), style = MaterialTheme.typography.bodyLarge) }
            item { Text(stringResource(R.string.alerts_privacy), style = MaterialTheme.typography.bodyMedium) }
            item {
                Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Text(stringResource(R.string.alerts_choose_zone))
                }
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        area.name.text(),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.semantics { heading() },
                    )
                    zone.dico?.let { labels?.municipality(it) }?.let {
                        Text(stringResource(R.string.alerts_municipality_line, it.name), style = MaterialTheme.typography.bodyLarge)
                    }
                    StatusLine(state)
                }
            }
            if (snapshot.lastAttemptFailed || snapshot.fetchedAt == null) {
                item { ErrorCard(state) }
            }
            if (snapshot.fetchedAt != null) {
                if (warnings.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.alerts_none, area.name.text()),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    items(warnings, key = { it.key }) { w ->
                        WarningCard(w, state, onOpenScenario)
                    }
                }
            }
            item { SectionTitle(stringResource(R.string.alerts_fire_title)) }
            when {
                area.region != AlertRegion.CONTINENT -> item {
                    Text(stringResource(R.string.alerts_fire_islands), style = MaterialTheme.typography.bodyMedium)
                }
                zone.dico == null -> item {
                    Text(stringResource(R.string.alerts_fire_choose_municipality), style = MaterialTheme.typography.bodyMedium)
                }
                fire.isEmpty() -> item {
                    Text(stringResource(R.string.alerts_fire_unavailable), style = MaterialTheme.typography.bodyMedium)
                }
                else -> items(fire, key = { it.key }) { r -> FireRiskRow(r, state, onOpenScenario) }
            }
            item {
                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !state.refreshing,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                    Text(
                        stringResource(if (state.refreshing) R.string.alerts_refreshing else R.string.alerts_refresh),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
        // Decision 8: always visible.
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                Text(
                    stringResource(R.string.alerts_absence_note),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        state.bundle.source("ipma-avisos")?.let { src ->
            item { LinkButton(stringResource(R.string.alerts_link_ipma)) { context.openWebPage(src.url) } }
        }
        state.bundle.source("anepc-avisos-populacao")?.let { src ->
            item { LinkButton(stringResource(R.string.alerts_link_anepc)) { context.openWebPage(src.url) } }
        }
        if (zone != null) {
            item {
                TextButton(onClick = onOpenSettings, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.Tune, contentDescription = null)
                    Text(stringResource(R.string.alerts_settings_title), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        item { SourcesLink(sources) }
        item { DisclaimerFooter() }
    }
}

@Composable
private fun StatusLine(state: AlertsUiState) {
    val context = LocalContext.current
    val fetched = state.snapshot.fetchedAt ?: return
    val stale = Alerts.isStale(state.snapshot, state.now)
    val text = if (stale) {
        stringResource(R.string.alerts_stale, AlertFormat.ago(context, fetched, state.now))
    } else {
        stringResource(R.string.alerts_fetched, AlertFormat.time(fetched), AlertFormat.ago(context, fetched, state.now))
    }
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (stale) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        stringResource(R.string.alerts_source_ipma),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ErrorCard(state: AlertsUiState) {
    val fetched = state.snapshot.fetchedAt
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(if (state.refreshing) R.string.alerts_refreshing else R.string.alerts_error),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                if (fetched == null) stringResource(R.string.alerts_error_never)
                else stringResource(R.string.alerts_error_last_good, AlertFormat.time(fetched)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun WarningCard(w: Warning, state: AlertsUiState, onOpenScenario: (String) -> Unit) {
    val context = LocalContext.current
    val labels = state.bundle.alerts
    val level = labels?.level(w.level)
    val scenario = Alerts.scenarioForWarning(state.bundle.scenarios, w.type)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WarningLabel(
                stringResource(R.string.alerts_level_label, level?.name?.text() ?: stringResource(R.string.alerts_level_unknown)),
                w.level.warningColor(),
            )
            Text(
                labels?.type(w.type)?.name?.text() ?: w.type,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                AlertFormat.period(context, w.start, w.end, ZoneId.systemDefault()),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (w.text.isNotBlank()) {
                Text(w.text, style = MaterialTheme.typography.bodyLarge)
                if (currentContentLanguage() != ContentLanguage.PT_PT) {
                    Text(
                        stringResource(R.string.alerts_text_in_portuguese),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            level?.meaning?.let {
                Text(it.text(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (scenario != null) {
                Button(onClick = { onOpenScenario(scenario.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.alerts_see_what_to_do, scenario.title.text()))
                }
            }
        }
    }
}

@Composable
private fun FireRiskRow(r: FireRisk, state: AlertsUiState, onOpenScenario: (String) -> Unit) {
    val context = LocalContext.current
    val name = state.bundle.alerts?.fireRisk(r.level)?.name?.text() ?: r.level.toString()
    val scenario = Alerts.scenarioForFireRisk(state.bundle.scenarios, r.level)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(AlertFormat.day(context, r.day), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            WarningLabel(name, fireRiskColor(r.level))
        }
        if (scenario != null) {
            TextButton(onClick = { onOpenScenario(scenario.id) }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.alerts_see_what_to_do, scenario.title.text()))
            }
        }
    }
}

@Composable
private fun LinkButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(text, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
    }
}
