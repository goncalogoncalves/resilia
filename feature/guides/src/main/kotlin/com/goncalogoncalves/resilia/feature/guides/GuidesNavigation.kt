package com.goncalogoncalves.resilia.feature.guides

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data object GuidesRoute

@Serializable
data class ScenarioRoute(val scenarioId: String)

fun NavGraphBuilder.guidesScreens(
    onOpenScenario: (String) -> Unit,
    onOpenKit: () -> Unit = {},
) {
    composable<GuidesRoute> { GuidesScreen(onOpenScenario = onOpenScenario) }
    composable<ScenarioRoute> { entry ->
        ScenarioScreen(scenarioId = entry.toRoute<ScenarioRoute>().scenarioId, onOpenScenario = onOpenScenario, onOpenKit = onOpenKit)
    }
}
