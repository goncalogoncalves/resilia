package com.goncalogoncalves.resilia.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.goncalogoncalves.resilia.core.designsystem.R

/** Atkinson Hyperlegible (SIL OFL 1.1), bundled so it works offline and in PDFs. */
val AtkinsonHyperlegible = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Bold),
)

private fun style(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = AtkinsonHyperlegible,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
)

/** Slightly larger than Material defaults: the app must be readable under stress. */
internal val NormalTypography = Typography(
    displaySmall = style(34, 42, FontWeight.Bold),
    headlineLarge = style(30, 38, FontWeight.Bold),
    headlineMedium = style(26, 34, FontWeight.Bold),
    headlineSmall = style(23, 31, FontWeight.Bold),
    titleLarge = style(22, 28, FontWeight.Bold),
    titleMedium = style(18, 24, FontWeight.Bold),
    titleSmall = style(16, 22, FontWeight.Bold),
    bodyLarge = style(18, 27),
    bodyMedium = style(16, 24),
    bodySmall = style(14, 20),
    labelLarge = style(16, 22, FontWeight.Bold),
    labelMedium = style(14, 20, FontWeight.Bold),
    labelSmall = style(13, 18, FontWeight.Bold),
)

/** Emergency mode: everything bigger. Still scales with the system font size (sp). */
internal val EmergencyTypography = Typography(
    displaySmall = style(40, 48, FontWeight.Bold),
    headlineLarge = style(34, 42, FontWeight.Bold),
    headlineMedium = style(30, 38, FontWeight.Bold),
    headlineSmall = style(26, 34, FontWeight.Bold),
    titleLarge = style(26, 32, FontWeight.Bold),
    titleMedium = style(22, 28, FontWeight.Bold),
    titleSmall = style(20, 26, FontWeight.Bold),
    bodyLarge = style(22, 32),
    bodyMedium = style(20, 28),
    bodySmall = style(17, 24),
    labelLarge = style(22, 28, FontWeight.Bold),
    labelMedium = style(18, 24, FontWeight.Bold),
    labelSmall = style(16, 22, FontWeight.Bold),
)
