package com.goncalogoncalves.resilia

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.theme.EmergencyTheme
import com.goncalogoncalves.resilia.core.designsystem.theme.ResiliaTheme
import com.goncalogoncalves.resilia.feature.morale.EmergencyActivitiesSheet
import com.goncalogoncalves.resilia.feature.places.EmergencyPlacesSheet
import com.goncalogoncalves.resilia.feature.prepare.plan.EmergencyPlanSheet
import com.goncalogoncalves.resilia.core.domain.Disclaimer
import com.goncalogoncalves.resilia.core.domain.repository.AppState
import com.goncalogoncalves.resilia.core.domain.repository.AppStateRepository
import com.goncalogoncalves.resilia.feature.alerts.AlertIntents
import com.goncalogoncalves.resilia.feature.alerts.AlertsRoute
import com.goncalogoncalves.resilia.feature.emergency.EmergencyRoot
import com.goncalogoncalves.resilia.feature.guides.ScenarioRoute
import com.goncalogoncalves.resilia.feature.settings.DisclaimerAcceptScreen
import com.goncalogoncalves.resilia.navigation.MainScaffold
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(private val appState: AppStateRepository) : ViewModel() {
    /** null until the stored state is read (avoids flashing the wrong mode on start). */
    val state: StateFlow<AppState?> = appState.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Only after the person confirms the suggestion from an alert (decision 4). */
    fun activateEmergency(scenarioId: String?) {
        viewModelScope.launch { appState.setEmergencyMode(active = true, scenarioId = scenarioId) }
    }
}

/** Which top-level UI is shown. */
enum class RootMode { LOADING, DISCLAIMER, EMERGENCY, NORMAL }

fun rootModeFor(state: AppState?): RootMode = when {
    state == null -> RootMode.LOADING
    Disclaimer.mustShow(state.disclaimerAcceptedVersion) -> RootMode.DISCLAIMER
    state.emergencyModeActive -> RootMode.EMERGENCY
    else -> RootMode.NORMAL
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /** An alert notification the person tapped, waiting to be handled by [Root]. */
    private val alertIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) alertIntent.value = intent.takeIf { it.action == AlertIntents.ACTION }
        enableEdgeToEdge()
        setContent { Root() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == AlertIntents.ACTION) alertIntent.value = intent
    }

    @Composable
    private fun Root(viewModel: RootViewModel = hiltViewModel()) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        val mode = rootModeFor(state)
        val dark = isSystemInDarkTheme()

        // Alert notifications: open a screen, or *suggest* Emergency mode (never activate it directly).
        var pendingRoute by remember { mutableStateOf<Any?>(null) }
        var suggestSummary by rememberSaveable { mutableStateOf<String?>(null) }
        var suggestScenario by rememberSaveable { mutableStateOf<String?>(null) }
        val incoming = alertIntent.value
        LaunchedEffect(incoming) {
            val i = incoming ?: return@LaunchedEffect
            val scenario = i.getStringExtra(AlertIntents.EXTRA_SCENARIO)
            when (i.getStringExtra(AlertIntents.EXTRA_TARGET)) {
                AlertIntents.TARGET_SCENARIO -> pendingRoute = scenario?.let { ScenarioRoute(it) } ?: AlertsRoute
                AlertIntents.TARGET_EMERGENCY -> {
                    suggestSummary = i.getStringExtra(AlertIntents.EXTRA_SUMMARY).orEmpty()
                    suggestScenario = scenario
                }
                else -> pendingRoute = AlertsRoute
            }
            alertIntent.value = null
        }

        // Emergency mode always uses light icons on black bars.
        LaunchedEffect(mode, dark) {
            val style = if (mode == RootMode.EMERGENCY || dark) {
                SystemBarStyle.dark(Color.BLACK)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            }
            enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        }

        when (mode) {
            RootMode.LOADING -> Unit // window background is already the right colour
            RootMode.DISCLAIMER -> ResiliaTheme { DisclaimerAcceptScreen() }
            RootMode.EMERGENCY -> {
                // The family plan on screen, over emergency mode (no navigation there).
                var showPlan by rememberSaveable { mutableStateOf(false) }
                var showActivities by rememberSaveable { mutableStateOf(false) }
                var showPlaces by rememberSaveable { mutableStateOf(false) }
                BackHandler(enabled = showPlan || showActivities || showPlaces) {
                    showPlan = false
                    showActivities = false
                    showPlaces = false
                }
                when {
                    showPlan -> EmergencyTheme { EmergencyPlanSheet(onClose = { showPlan = false }) }
                    showActivities -> EmergencyTheme { EmergencyActivitiesSheet(onClose = { showActivities = false }) }
                    showPlaces -> EmergencyTheme { EmergencyPlacesSheet(onClose = { showPlaces = false }) }
                    else -> EmergencyRoot(
                        onOpenPlan = { showPlan = true },
                        onOpenActivities = { showActivities = true },
                        onOpenPlaces = { showPlaces = true },
                    )
                }
            }
            RootMode.NORMAL -> ResiliaTheme {
                MainScaffold(pendingRoute = pendingRoute, onPendingRouteHandled = { pendingRoute = null })
                val summary = suggestSummary
                if (summary != null) {
                    val dismiss = {
                        suggestSummary = null
                        suggestScenario = null
                    }
                    AlertDialog(
                        onDismissRequest = dismiss,
                        title = { Text(stringResource(R.string.alert_emergency_title)) },
                        text = { Text(stringResource(R.string.alert_emergency_text, summary)) },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.activateEmergency(suggestScenario)
                                dismiss()
                            }) { Text(stringResource(R.string.alert_emergency_confirm)) }
                        },
                        dismissButton = {
                            TextButton(onClick = dismiss) { Text(stringResource(R.string.alert_emergency_cancel)) }
                        },
                    )
                }
            }
        }
        // Already in Emergency mode: the suggestion is moot.
        LaunchedEffect(mode) {
            if (mode == RootMode.EMERGENCY) {
                pendingRoute = null
                suggestSummary = null
                suggestScenario = null
            }
        }
    }
}
