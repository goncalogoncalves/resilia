package com.goncalogoncalves.resilia.feature.morale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goncalogoncalves.resilia.core.designsystem.component.BigActionButton
import com.goncalogoncalves.resilia.core.designsystem.component.text

/**
 * "Something to do" over Emergency mode: only activities that work with the lights out and
 * without noise. One tap opens the steps in place; no navigation away from Emergency mode.
 */
@Composable
fun EmergencyActivitiesSheet(onClose: () -> Unit, viewModel: MoraleViewModel = hiltViewModel()) {
    val bundle by viewModel.bundle.collectAsStateWithLifecycle()
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    val b = bundle
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.morale_emergency_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() },
                )
            }
            item { Text(stringResource(R.string.morale_emergency_intro), style = MaterialTheme.typography.bodyLarge) }
            if (b != null) {
                val open = openId?.let(b::activity)
                if (open != null) {
                    item {
                        Text(
                            open.title.text(),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                    activityDetail(open, b, showTitle = false)
                    item {
                        TextButton(onClick = { openId = null }) {
                            Text(stringResource(R.string.morale_emergency_other), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                } else {
                    items(b.activities.filter { it.goodForDarkAndQuiet }, key = { it.id }) { a ->
                        BigActionButton(
                            text = a.title.text(),
                            icon = a.type.icon(),
                            onClick = { openId = a.id },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item {
                BigActionButton(
                    text = stringResource(R.string.morale_emergency_close),
                    icon = Icons.Outlined.Close,
                    onClick = onClose,
                )
            }
        }
    }
}
