package com.goncalogoncalves.resilia.feature.morale

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data object MoraleRoute

@Serializable
data class ActivityRoute(val activityId: String)

@Serializable
data object SupportRoute

@Serializable
data class SupportGuideRoute(val guideId: String)

/** [onOpen] navigates to one of the routes above. */
fun NavGraphBuilder.moraleScreen(onOpen: (Any) -> Unit) {
    composable<MoraleRoute> {
        MoraleScreen(
            onOpenSupport = { onOpen(SupportRoute) },
            onOpenActivity = { onOpen(ActivityRoute(it)) },
        )
    }
    composable<ActivityRoute> { ActivityScreen(it.toRoute<ActivityRoute>().activityId) }
    composable<SupportRoute> { SupportScreen(onOpenGuide = { id -> onOpen(SupportGuideRoute(id)) }) }
    composable<SupportGuideRoute> { SupportGuideScreen(it.toRoute<SupportGuideRoute>().guideId) }
}
