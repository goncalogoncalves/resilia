package com.goncalogoncalves.resilia.feature.alerts

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object AlertsRoute

@Serializable
data object AlertSettingsRoute

fun NavGraphBuilder.alertsScreens(onOpenScenario: (String) -> Unit, onOpenSettings: () -> Unit) {
    composable<AlertsRoute> { AlertsScreen(onOpenScenario = onOpenScenario, onOpenSettings = onOpenSettings) }
    composable<AlertSettingsRoute> { AlertSettingsScreen() }
}
