package com.goncalogoncalves.resilia.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.goncalogoncalves.resilia.core.designsystem.component.BigActionButton
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.HubCard
import com.goncalogoncalves.resilia.core.designsystem.component.SectionTitle
import com.goncalogoncalves.resilia.core.designsystem.component.scenarioIcon
import com.goncalogoncalves.resilia.core.designsystem.component.text
import com.goncalogoncalves.resilia.core.designsystem.component.WarningLabel
import com.goncalogoncalves.resilia.core.designsystem.component.fireRiskColor
import com.goncalogoncalves.resilia.core.designsystem.component.warningColor
import com.goncalogoncalves.resilia.core.domain.model.Alerts
import com.goncalogoncalves.resilia.core.domain.model.FireRisk
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Warning
import com.goncalogoncalves.resilia.core.domain.repository.AlertsRepository
import com.goncalogoncalves.resilia.core.domain.model.Scenario
import com.goncalogoncalves.resilia.core.domain.repository.AppStateRepository
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

@Serializable
data object HomeRoute

fun NavGraphBuilder.homeScreen(
    onOpenScenario: (String) -> Unit,
    onOpenTools: () -> Unit,
    onOpenPaper: () -> Unit,
    onOpenAlerts: () -> Unit,
) {
    composable<HomeRoute> {
        HomeScreen(onOpenScenario, onOpenTools, onOpenPaper, onOpenAlerts)
    }
}

/** The alert strip at the top of Home (milestone 7). [area] is null when no zone was chosen. */
data class HomeAlert(
    val area: LocalizedText?,
    val top: Warning? = null,
    val levelName: LocalizedText? = null,
    val typeName: LocalizedText? = null,
    val others: Int = 0,
    /** Highest fire risk (4 or 5) for today or later, if any. */
    val fire: FireRisk? = null,
    val fireName: LocalizedText? = null,
    val hasData: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    content: ContentRepository,
    private val appState: AppStateRepository,
    alerts: AlertsRepository,
) : ViewModel() {

    private val bundle = flow { emit(content.load()) }

    val scenarios = bundle.map { it.scenarios }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val alert: StateFlow<HomeAlert?> = combine(bundle, alerts.settings, alerts.snapshot) { b, settings, snap ->
        val labels = b.alerts
        val zone = settings.zone
        val area = zone?.let { labels?.area(it.areaId) } ?: return@combine HomeAlert(area = null)
        val active = Alerts.active(snap.warnings, area.id, Instant.now())
        val top = active.firstOrNull()
        val fire = Alerts.fireRiskFor(snap.fireRisk, zone.dico)
            .filter { it.level >= Alerts.FIRE_NOTIFY_FROM && !it.day.isBefore(LocalDate.now()) }
            .maxByOrNull { it.level }
        HomeAlert(
            area = area.name,
            top = top,
            levelName = top?.let { labels?.level(it.level)?.name },
            typeName = top?.let { labels?.type(it.type)?.name },
            others = (active.size - 1).coerceAtLeast(0),
            fire = fire,
            fireName = fire?.let { labels?.fireRisk(it.level)?.name },
            hasData = snap.fetchedAt != null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun activateEmergency() = viewModelScope.launch {
        appState.setEmergencyMode(active = true)
    }
}

@Composable
internal fun HomeScreen(
    onOpenScenario: (String) -> Unit,
    onOpenTools: () -> Unit,
    onOpenPaper: () -> Unit,
    onOpenAlerts: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val scenarios by viewModel.scenarios.collectAsStateWithLifecycle()
    val alert by viewModel.alert.collectAsStateWithLifecycle()
    HomeContent(
        scenarios = scenarios,
        alert = alert,
        onOpenAlerts = onOpenAlerts,
        onActivateEmergency = viewModel::activateEmergency,
        onOpenScenario = onOpenScenario,
        onOpenTools = onOpenTools,
        onOpenPaper = onOpenPaper,
    )
}

@Composable
internal fun HomeContent(
    scenarios: List<Scenario>,
    alert: HomeAlert?,
    onOpenAlerts: () -> Unit,
    onActivateEmergency: () -> Unit,
    onOpenScenario: (String) -> Unit,
    onOpenTools: () -> Unit,
    onOpenPaper: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        alert?.let { item { AlertStrip(it, onOpenAlerts) } }
        item {
            BigActionButton(
                text = stringResource(R.string.home_activate_emergency),
                icon = Icons.Outlined.Sos,
                onClick = onActivateEmergency,
                containerColor = MaterialTheme.colorScheme.primary,
            )
        }
        item { SectionTitle(stringResource(R.string.home_scenarios_title)) }
        items(scenarios, key = { it.id }) { scenario ->
            HubCard(
                title = scenario.title.text(),
                subtitle = scenario.typicalDuration.text(),
                icon = scenarioIcon(scenario.icon),
                onClick = { onOpenScenario(scenario.id) },
                trailing = { Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null) },
            )
        }
        item { SectionTitle(stringResource(R.string.home_shortcuts_title)) }
        item {
            HubCard(
                title = stringResource(R.string.home_shortcut_torch),
                icon = Icons.Outlined.FlashlightOn,
                onClick = onOpenTools,
            )
        }
        item {
            HubCard(
                title = stringResource(R.string.home_shortcut_paper),
                icon = Icons.Outlined.Print,
                onClick = onOpenPaper,
            )
        }
        item { DisclaimerFooter() }
        item {
            Text(
                stringResource(R.string.home_offline_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AlertStrip(alert: HomeAlert, onOpen: () -> Unit) {
    val area = alert.area
    val top = alert.top
    when {
        area == null -> HubCard(
            title = stringResource(R.string.home_alerts_title),
            subtitle = stringResource(R.string.home_alerts_choose),
            icon = Icons.Outlined.NotificationsNone,
            onClick = onOpen,
        )
        top != null || alert.fire != null -> Card(
            onClick = onOpen,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (top != null) {
                    WarningLabel(
                        stringResource(R.string.home_alerts_level, alert.levelName?.text() ?: stringResource(R.string.home_alerts_ipma)),
                        top.level.warningColor(),
                    )
                    Text(
                        stringResource(R.string.home_alerts_type_area, alert.typeName?.text() ?: top.type, area.text()),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (alert.others > 0) {
                        Text(
                            pluralStringResource(R.plurals.home_alerts_others, alert.others, alert.others),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                alert.fire?.let { fire ->
                    WarningLabel(
                        stringResource(R.string.home_alerts_fire, alert.fireName?.text() ?: fire.level.toString()),
                        fireRiskColor(fire.level),
                    )
                }
                Text(
                    stringResource(R.string.home_alerts_see),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        else -> HubCard(
            title = stringResource(R.string.home_alerts_title),
            subtitle = if (alert.hasData) stringResource(R.string.home_alerts_none, area.text())
            else stringResource(R.string.home_alerts_no_data),
            icon = Icons.Outlined.NotificationsNone,
            onClick = onOpen,
        )
    }
}

