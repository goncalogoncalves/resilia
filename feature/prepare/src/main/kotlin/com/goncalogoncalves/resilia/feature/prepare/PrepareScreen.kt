package com.goncalogoncalves.resilia.feature.prepare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.goncalogoncalves.resilia.core.designsystem.component.HubCard
import com.goncalogoncalves.resilia.feature.prepare.paper.PaperScreen
import com.goncalogoncalves.resilia.feature.prepare.plan.FamilyPlanScreen
import kotlinx.serialization.Serializable

@Serializable
data object PrepareRoute

@Serializable
data object KitCalculatorRoute

@Serializable
data object InventoryRoute

@Serializable
data object FamilyPlanRoute

@Serializable
data object PaperRoute

fun NavGraphBuilder.prepareScreens(
    onOpenCalculator: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenPlan: () -> Unit,
    onOpenPaper: () -> Unit,
    onOpenPlaces: () -> Unit,
) {
    composable<PrepareRoute> { PrepareScreen(onOpenCalculator, onOpenInventory, onOpenPlan, onOpenPaper, onOpenPlaces) }
    composable<KitCalculatorRoute> { KitCalculatorScreen(onOpenInventory) }
    composable<InventoryRoute> { InventoryScreen() }
    composable<FamilyPlanRoute> { FamilyPlanScreen(onOpenPaper) }
    composable<PaperRoute> { PaperScreen() }
}

/** Hub for preparation tools. */
@Composable
internal fun PrepareScreen(
    onOpenCalculator: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenPlan: () -> Unit,
    onOpenPaper: () -> Unit,
    onOpenPlaces: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(stringResource(R.string.prepare_intro), style = MaterialTheme.typography.bodyLarge) }
        item {
            HubCard(
                stringResource(R.string.prepare_kit_calculator),
                Icons.Outlined.Calculate,
                subtitle = stringResource(R.string.prepare_kit_calculator_subtitle),
                onClick = onOpenCalculator,
            )
        }
        item {
            HubCard(
                stringResource(R.string.prepare_inventory),
                Icons.Outlined.Inventory2,
                subtitle = stringResource(R.string.prepare_inventory_subtitle),
                onClick = onOpenInventory,
            )
        }
        item {
            HubCard(
                stringResource(R.string.prepare_family_plan),
                Icons.Outlined.FamilyRestroom,
                subtitle = stringResource(R.string.prepare_family_plan_subtitle),
                onClick = onOpenPlan,
            )
        }
        item {
            HubCard(
                stringResource(R.string.prepare_paper),
                Icons.Outlined.Print,
                subtitle = stringResource(R.string.prepare_paper_subtitle),
                onClick = onOpenPaper,
            )
        }
        item {
            HubCard(
                stringResource(R.string.prepare_places),
                Icons.Outlined.Place,
                subtitle = stringResource(R.string.prepare_places_subtitle),
                onClick = onOpenPlaces,
            )
        }
    }
}
