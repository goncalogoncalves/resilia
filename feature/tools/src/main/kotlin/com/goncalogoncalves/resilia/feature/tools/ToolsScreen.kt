package com.goncalogoncalves.resilia.feature.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.goncalogoncalves.resilia.core.designsystem.component.HubCard
import com.goncalogoncalves.resilia.core.domain.device.Torch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data object ToolsRoute

@Serializable
data object ContactsRoute

@Serializable
data object ScreenLightRoute

@Serializable
data object SosRoute

@Serializable
data object WhistleRoute

@Serializable
data object BatteryRoute

/** [onOpen] navigates to one of the routes above. */
fun NavGraphBuilder.toolsScreen(onOpen: (Any) -> Unit) {
    composable<ToolsRoute> { ToolsScreen(onOpen = onOpen) }
    composable<ContactsRoute> { ContactsScreen() }
    composable<ScreenLightRoute> { ScreenLightScreen() }
    composable<SosRoute> { SosScreen() }
    composable<WhistleRoute> { WhistleScreen() }
    composable<BatteryRoute> { BatteryScreen() }
}

@HiltViewModel
class ToolsViewModel @Inject constructor(private val torch: Torch) : ViewModel() {
    val torchAvailable get() = torch.isAvailable
    val torchOn = torch.isOn
    fun toggleTorch() {
        torch.set(!torch.isOn.value)
    }
}

/** Tools hub: torch (toggles in place), contacts, screen light, SOS light, whistle, battery. */
@Composable
internal fun ToolsScreen(onOpen: (Any) -> Unit, viewModel: ToolsViewModel = hiltViewModel()) {
    val torchOn by viewModel.torchOn.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            if (viewModel.torchAvailable) {
                HubCard(
                    title = stringResource(R.string.tools_torch),
                    subtitle = stringResource(if (torchOn) R.string.tools_torch_is_on else R.string.tools_torch_is_off),
                    icon = if (torchOn) Icons.Outlined.FlashlightOn else Icons.Outlined.FlashlightOff,
                    onClick = viewModel::toggleTorch,
                    // The whole card toggles; the switch only mirrors state (not separately focusable).
                    trailing = { Switch(checked = torchOn, onCheckedChange = null) },
                )
            } else {
                HubCard(
                    title = stringResource(R.string.tools_torch),
                    subtitle = stringResource(R.string.tools_torch_unavailable),
                    icon = Icons.Outlined.FlashlightOff,
                    onClick = {},
                )
            }
        }
        item {
            HubCard(
                title = stringResource(R.string.tools_contacts),
                subtitle = stringResource(R.string.tools_contacts_subtitle),
                icon = Icons.Outlined.Call,
                onClick = { onOpen(ContactsRoute) },
            )
        }
        item {
            HubCard(
                stringResource(R.string.tools_screen_light), Icons.Outlined.LightMode,
                subtitle = stringResource(R.string.tools_screen_light_subtitle), onClick = { onOpen(ScreenLightRoute) },
            )
        }
        item {
            HubCard(
                stringResource(R.string.tools_sos), Icons.Outlined.Sos,
                subtitle = stringResource(R.string.tools_sos_subtitle), onClick = { onOpen(SosRoute) },
            )
        }
        item {
            HubCard(
                stringResource(R.string.tools_whistle), Icons.Outlined.Campaign,
                subtitle = stringResource(R.string.tools_whistle_subtitle), onClick = { onOpen(WhistleRoute) },
            )
        }
        item {
            HubCard(
                stringResource(R.string.tools_battery), Icons.Outlined.BatterySaver,
                subtitle = stringResource(R.string.tools_battery_subtitle), onClick = { onOpen(BatteryRoute) },
            )
        }
    }
}
