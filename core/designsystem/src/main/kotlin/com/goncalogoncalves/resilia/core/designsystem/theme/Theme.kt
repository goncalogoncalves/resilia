package com.goncalogoncalves.resilia.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** Whether the UI is currently in Emergency mode (lets shared components adapt). */
val LocalEmergencyMode = staticCompositionLocalOf { false }

private val CalmShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

private val EmergencyShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
)

/** Normal mode: calm, friendly; follows the system light/dark setting. No dynamic colour (consistent identity). */
@Composable
fun ResiliaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalEmergencyMode provides false) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = NormalTypography,
            shapes = CalmShapes,
            content = content,
        )
    }
}

/** Emergency mode: always black, high contrast, larger type. */
@Composable
fun EmergencyTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalEmergencyMode provides true) {
        MaterialTheme(
            colorScheme = EmergencyColors,
            typography = EmergencyTypography,
            shapes = EmergencyShapes,
            content = content,
        )
    }
}
