package com.goncalogoncalves.resilia.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.goncalogoncalves.resilia.core.designsystem.theme.WarningColors
import com.goncalogoncalves.resilia.core.domain.model.AlertLevel

/** The IPMA colour for a level; null for green and unknown levels (shown without colour). */
fun AlertLevel.warningColor(): Color? = when (this) {
    AlertLevel.YELLOW -> WarningColors.Yellow
    AlertLevel.ORANGE -> WarningColors.Orange
    AlertLevel.RED -> WarningColors.Red
    AlertLevel.GREEN, AlertLevel.UNKNOWN -> null
}

/** Fire risk 1–5 on the same reserved colours: 3 yellow, 4 orange, 5 red. */
fun fireRiskColor(level: Int): Color? = when (level) {
    3 -> WarningColors.Yellow
    4 -> WarningColors.Orange
    5 -> WarningColors.Red
    else -> null
}

/**
 * A coloured label with the level written out, so colour is never the only signal
 * (accessibility: colour blindness, black-and-white screens).
 */
@Composable
fun WarningLabel(text: String, color: Color?, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(6.dp)
    val base = if (color != null) {
        modifier.background(color, shape)
    } else {
        modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
    }
    Box(base.padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = when (color) {
                null -> MaterialTheme.colorScheme.onSurface
                // Black on #D62828 is about 4.2:1; white reaches about 5:1 (WCAG AA for small text).
                WarningColors.Red -> Color.White
                else -> WarningColors.OnWarning
            },
        )
    }
}
