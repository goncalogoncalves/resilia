package com.goncalogoncalves.resilia.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * Palette: calm petrol-blue (teal) + warm neutrals.
 * Yellow / orange / red are RESERVED for official warning levels (see [WarningColors])
 * and must not be used as decoration.
 */
internal object Palette {
    val Teal10 = Color(0xFF002022)
    val Teal20 = Color(0xFF003739)
    val Teal30 = Color(0xFF0F4F52)
    val Teal40 = Color(0xFF0F5E63)
    val Teal80 = Color(0xFF7FD3D6)
    val Teal90 = Color(0xFFCDE8E8)

    val Sage30 = Color(0xFF334B4B)
    val Sage40 = Color(0xFF4A6363)
    val Sage80 = Color(0xFFB1CCCB)
    val Sage90 = Color(0xFFCCE8E7)

    val Warm99 = Color(0xFFFBFAF6)
    val Warm95 = Color(0xFFF1EFE9)
    val Warm92 = Color(0xFFE8E6E0)
    val Warm10 = Color(0xFF1B1C1A)
    val Warm6 = Color(0xFF121413)
    val Warm12 = Color(0xFF1E201F)
    val Warm17 = Color(0xFF292B29)
    val Warm90 = Color(0xFFE3E2DE)
    val Neutral30 = Color(0xFF444746)
    val Neutral50 = Color(0xFF747775)
    val Neutral80 = Color(0xFFC4C7C5)

    val Red40 = Color(0xFFB3261E)
    val Red80 = Color(0xFFF2B8B5)
}

internal val LightColors = lightColorScheme(
    primary = Palette.Teal40,
    onPrimary = Color.White,
    primaryContainer = Palette.Teal90,
    onPrimaryContainer = Palette.Teal10,
    secondary = Palette.Sage40,
    onSecondary = Color.White,
    secondaryContainer = Palette.Sage90,
    onSecondaryContainer = Palette.Teal10,
    background = Palette.Warm99,
    onBackground = Palette.Warm10,
    surface = Palette.Warm99,
    onSurface = Palette.Warm10,
    surfaceVariant = Palette.Warm92,
    onSurfaceVariant = Palette.Neutral30,
    surfaceContainer = Palette.Warm95,
    surfaceContainerHigh = Palette.Warm92,
    outline = Palette.Neutral50,
    error = Palette.Red40,
    onError = Color.White,
)

internal val DarkColors = darkColorScheme(
    primary = Palette.Teal80,
    onPrimary = Palette.Teal20,
    primaryContainer = Palette.Teal30,
    onPrimaryContainer = Palette.Teal90,
    secondary = Palette.Sage80,
    onSecondary = Palette.Sage30,
    secondaryContainer = Palette.Sage30,
    onSecondaryContainer = Palette.Sage90,
    background = Palette.Warm6,
    onBackground = Palette.Warm90,
    surface = Palette.Warm6,
    onSurface = Palette.Warm90,
    surfaceVariant = Palette.Neutral30,
    onSurfaceVariant = Palette.Neutral80,
    surfaceContainer = Palette.Warm12,
    surfaceContainerHigh = Palette.Warm17,
    outline = Palette.Neutral80,
    error = Palette.Red80,
    onError = Color(0xFF601410),
)

/**
 * Emergency mode: pure black (saves power on OLED screens), white text,
 * maximum contrast. Only the 112 action uses a colour.
 */
internal val EmergencyColors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1A1A1A),
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color.White,
    surfaceContainer = Color(0xFF1A1A1A),
    surfaceContainerHigh = Color(0xFF242424),
    outline = Color.White,
    error = Color(0xFFFF6B6B),
    onError = Color.Black,
)

/** Official warning levels (IPMA scale). Use ONLY for warnings. */
object WarningColors {
    val Yellow = Color(0xFFFFD60A)
    val Orange = Color(0xFFFF8C00)
    val Red = Color(0xFFD62828)
    val OnWarning = Color.Black
}
