package com.goncalogoncalves.resilia.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.goncalogoncalves.resilia.R
import com.goncalogoncalves.resilia.feature.alerts.AlertSettingsRoute
import com.goncalogoncalves.resilia.feature.alerts.AlertsRoute
import com.goncalogoncalves.resilia.feature.alerts.alertsScreens
import com.goncalogoncalves.resilia.feature.places.HospitalsRoute
import com.goncalogoncalves.resilia.feature.places.PlaceEditRoute
import com.goncalogoncalves.resilia.feature.places.PlacesRoute
import com.goncalogoncalves.resilia.feature.places.placesScreens
import com.goncalogoncalves.resilia.feature.guides.GuidesRoute
import com.goncalogoncalves.resilia.feature.guides.ScenarioRoute
import com.goncalogoncalves.resilia.feature.guides.guidesScreens
import com.goncalogoncalves.resilia.feature.home.HomeRoute
import com.goncalogoncalves.resilia.feature.home.homeScreen
import com.goncalogoncalves.resilia.feature.morale.ActivityRoute
import com.goncalogoncalves.resilia.feature.morale.MoraleRoute
import com.goncalogoncalves.resilia.feature.morale.SupportGuideRoute
import com.goncalogoncalves.resilia.feature.morale.SupportRoute
import com.goncalogoncalves.resilia.feature.morale.moraleScreen
import com.goncalogoncalves.resilia.feature.prepare.FamilyPlanRoute
import com.goncalogoncalves.resilia.feature.prepare.InventoryRoute
import com.goncalogoncalves.resilia.feature.prepare.PaperRoute
import com.goncalogoncalves.resilia.feature.prepare.KitCalculatorRoute
import com.goncalogoncalves.resilia.feature.prepare.PrepareRoute
import com.goncalogoncalves.resilia.feature.prepare.prepareScreens
import com.goncalogoncalves.resilia.feature.settings.AboutRoute
import com.goncalogoncalves.resilia.feature.settings.BackupRoute
import com.goncalogoncalves.resilia.feature.settings.PrivacyRoute
import com.goncalogoncalves.resilia.feature.settings.LegalRoute
import com.goncalogoncalves.resilia.feature.settings.SettingsRoute
import com.goncalogoncalves.resilia.feature.settings.settingsScreens
import com.goncalogoncalves.resilia.feature.tools.BatteryRoute
import com.goncalogoncalves.resilia.feature.tools.ContactsRoute
import com.goncalogoncalves.resilia.feature.tools.ScreenLightRoute
import com.goncalogoncalves.resilia.feature.tools.SosRoute
import com.goncalogoncalves.resilia.feature.tools.WhistleRoute
import com.goncalogoncalves.resilia.feature.tools.ToolsRoute
import com.goncalogoncalves.resilia.feature.tools.toolsScreen
import kotlin.reflect.KClass

/** The 5 bottom-bar destinations (decision §6). */
private enum class TopLevel(
    val route: Any,
    val routeClass: KClass<*>,
    @StringRes val label: Int,
    val icon: ImageVector,
) {
    HOME(HomeRoute, HomeRoute::class, R.string.nav_home, Icons.Outlined.Home),
    GUIDES(GuidesRoute, GuidesRoute::class, R.string.nav_guides, Icons.AutoMirrored.Outlined.MenuBook),
    PREPARE(PrepareRoute, PrepareRoute::class, R.string.nav_prepare, Icons.Outlined.Backpack),
    MORALE(MoraleRoute, MoraleRoute::class, R.string.nav_morale, Icons.Outlined.SelfImprovement),
    TOOLS(ToolsRoute, ToolsRoute::class, R.string.nav_tools, Icons.Outlined.FlashlightOn),
}

@StringRes
private fun titleFor(destination: NavDestination?): Int {
    TopLevel.entries.firstOrNull { destination?.hasRoute(it.routeClass) == true }?.let {
        return if (it == TopLevel.HOME) R.string.app_name else it.label
    }
    return when {
        destination?.hasRoute(ScenarioRoute::class) == true -> R.string.title_scenario
        destination?.hasRoute(SettingsRoute::class) == true -> R.string.title_settings
        destination?.hasRoute(LegalRoute::class) == true -> R.string.title_legal
        destination?.hasRoute(AboutRoute::class) == true -> R.string.title_about
        destination?.hasRoute(BackupRoute::class) == true -> R.string.title_backup
        destination?.hasRoute(PrivacyRoute::class) == true -> R.string.title_privacy
        destination?.hasRoute(ContactsRoute::class) == true -> R.string.title_contacts
        destination?.hasRoute(ScreenLightRoute::class) == true -> R.string.title_screen_light
        destination?.hasRoute(SosRoute::class) == true -> R.string.title_sos
        destination?.hasRoute(WhistleRoute::class) == true -> R.string.title_whistle
        destination?.hasRoute(BatteryRoute::class) == true -> R.string.title_battery
        destination?.hasRoute(ActivityRoute::class) == true -> R.string.title_activity
        destination?.hasRoute(SupportRoute::class) == true -> R.string.title_support
        destination?.hasRoute(SupportGuideRoute::class) == true -> R.string.title_support
        destination?.hasRoute(AlertsRoute::class) == true -> R.string.title_alerts
        destination?.hasRoute(PlacesRoute::class) == true -> R.string.title_places
        destination?.hasRoute(PlaceEditRoute::class) == true -> R.string.title_place_edit
        destination?.hasRoute(HospitalsRoute::class) == true -> R.string.title_hospitals
        destination?.hasRoute(AlertSettingsRoute::class) == true -> R.string.title_alert_settings
        destination?.hasRoute(KitCalculatorRoute::class) == true -> R.string.title_kit_calculator
        destination?.hasRoute(InventoryRoute::class) == true -> R.string.title_inventory
        destination?.hasRoute(FamilyPlanRoute::class) == true -> R.string.title_family_plan
        destination?.hasRoute(PaperRoute::class) == true -> R.string.title_paper
        else -> R.string.app_name
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController = rememberNavController(),
    pendingRoute: Any? = null,
    onPendingRouteHandled: () -> Unit = {},
) {
    // Opened from an alert notification (MainActivity): show it once the graph exists.
    LaunchedEffect(pendingRoute) {
        if (pendingRoute != null) {
            navController.navigate(pendingRoute) { launchSingleTop = true }
            onPendingRouteHandled()
        }
    }
    val entry by navController.currentBackStackEntryAsState()
    val destination = entry?.destination
    val isTopLevel = TopLevel.entries.any { destination?.hasRoute(it.routeClass) == true }
    // Recomputed whenever the back stack changes (entry is observed above).
    val currentTab = if (entry != null) navController.owningTab() else TopLevel.HOME

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(titleFor(destination)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    if (!isTopLevel && destination != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.action_back))
                        }
                    }
                },
                actions = {
                    if (isTopLevel) {
                        IconButton(onClick = { navController.navigate(SettingsRoute) { launchSingleTop = true } }) {
                            Icon(Icons.Outlined.Settings, stringResource(R.string.action_settings))
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                TopLevel.entries.forEach { item ->
                    NavigationBarItem(
                        selected = item == currentTab,
                        onClick = {
                            // Tapping the tab you are already in takes you back to its first screen.
                            if (item == currentTab) navController.popBackStack(item.route, inclusive = false)
                            else navController.navigateTopLevel(item.route)
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = {
                            // Shrinks only when the label does not fit (e.g. system font at 200%).
                            // TalkBack still reads the full label.
                            BasicText(
                                stringResource(item.label),
                                maxLines = 1,
                                style = MaterialTheme.typography.labelSmall.copy(color = LocalContentColor.current),
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = with(LocalDensity.current) { 10.dp.toSp() }, // not font-scaled: must always fit
                                    maxFontSize = MaterialTheme.typography.labelSmall.fontSize,
                                ),
                            )
                        },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = HomeRoute) {
                homeScreen(
                    onOpenScenario = { navController.navigate(ScenarioRoute(it)) },
                    onOpenTools = { navController.navigateTopLevel(ToolsRoute) },
                    onOpenPaper = { navController.navigate(PaperRoute) { launchSingleTop = true } },
                    onOpenAlerts = { navController.navigate(AlertsRoute) { launchSingleTop = true } },
                )
                guidesScreens(
                    onOpenScenario = { navController.navigate(ScenarioRoute(it)) },
                    onOpenKit = { navController.navigate(KitCalculatorRoute) { launchSingleTop = true } },
                )
                prepareScreens(
                    onOpenCalculator = { navController.navigate(KitCalculatorRoute) },
                    onOpenInventory = { navController.navigate(InventoryRoute) { launchSingleTop = true } },
                    onOpenPlan = { navController.navigate(FamilyPlanRoute) { launchSingleTop = true } },
                    onOpenPaper = { navController.navigate(PaperRoute) { launchSingleTop = true } },
                    onOpenPlaces = { navController.navigate(PlacesRoute) { launchSingleTop = true } },
                )
                placesScreens(onOpen = { route -> navController.navigate(route) { launchSingleTop = true } }, onBack = { navController.popBackStack() })
                moraleScreen(onOpen = { route -> navController.navigate(route) { launchSingleTop = true } })
                toolsScreen(onOpen = { route -> navController.navigate(route) { launchSingleTop = true } })
                settingsScreens(
                    onOpenLegal = { navController.navigate(LegalRoute) },
                    onOpenAbout = { navController.navigate(AboutRoute) },
                    onOpenAlerts = { navController.navigate(AlertSettingsRoute) { launchSingleTop = true } },
                    onOpen = { route -> navController.navigate(route) { launchSingleTop = true } },
                )
                alertsScreens(
                    onOpenScenario = { navController.navigate(ScenarioRoute(it)) },
                    onOpenSettings = { navController.navigate(AlertSettingsRoute) { launchSingleTop = true } },
                )
            }
        }
    }
}

/**
 * The tab the current screen belongs to. [navigateTopLevel] keeps at most Home plus one
 * other tab in the back stack, so a non-Home tab in the stack owns everything above it.
 */
private fun NavHostController.owningTab(): TopLevel =
    TopLevel.entries.lastOrNull { it != TopLevel.HOME && hasEntry(it.route) } ?: TopLevel.HOME

private fun NavHostController.hasEntry(route: Any) = runCatching { getBackStackEntry(route) }.isSuccess

private fun NavHostController.navigateTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
