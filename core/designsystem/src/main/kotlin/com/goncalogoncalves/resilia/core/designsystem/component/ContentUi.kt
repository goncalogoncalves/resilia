package com.goncalogoncalves.resilia.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PowerOff
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import com.goncalogoncalves.resilia.core.domain.model.ContentLanguage
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText

/** Content language derived from the app's current locale. */
@Composable
@ReadOnlyComposable
fun currentContentLanguage(): ContentLanguage {
    val locales = LocalConfiguration.current.locales
    val language = if (locales.isEmpty) "pt" else locales[0].language
    return ContentLanguage.fromSystemLanguage(language)
}

@Composable
@ReadOnlyComposable
fun LocalizedText.text(): String = resolve(currentContentLanguage())

/** Maps the `icon` field of the content JSON to a bundled vector icon. */
fun scenarioIcon(name: String): ImageVector = when (name) {
    "power_off" -> Icons.Outlined.PowerOff
    "water_drop" -> Icons.Outlined.WaterDrop
    "flood" -> Icons.Outlined.Water
    "local_fire_department" -> Icons.Outlined.LocalFireDepartment
    "earthquake" -> Icons.Outlined.Vibration
    "air" -> Icons.Outlined.Air
    "thermostat" -> Icons.Outlined.Thermostat
    "ac_unit" -> Icons.Outlined.AcUnit
    "directions_run" -> Icons.Outlined.DirectionsRun
    else -> Icons.Outlined.WarningAmber
}
