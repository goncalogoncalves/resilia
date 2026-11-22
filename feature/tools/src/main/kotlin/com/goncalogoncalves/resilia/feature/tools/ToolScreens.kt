package com.goncalogoncalves.resilia.feature.tools

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.BatteryManager
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.component.BigActionButton
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.SourcesLink
import com.goncalogoncalves.resilia.core.designsystem.component.StepCard
import com.goncalogoncalves.resilia.core.designsystem.component.text
import com.goncalogoncalves.resilia.core.domain.device.Torch
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ToolContentViewModel @Inject constructor(content: ContentRepository, val torch: Torch) : ViewModel() {
    val bundle = flow<ContentBundle?> { emit(content.load()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    override fun onCleared() {
        torch.set(false) // never leave the torch on after leaving a tool
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Screen at full brightness and kept on while this composable is shown. Restored on exit. */
@Composable
private fun FullBrightness() {
    val context = LocalContext.current
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        val previous = window?.attributes?.screenBrightness
        window?.let { w -> w.attributes = w.attributes.apply { screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL } }
        view.keepScreenOn = true
        onDispose {
            window?.let { w ->
                w.attributes = w.attributes.apply {
                    screenBrightness = previous ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                }
            }
            view.keepScreenOn = false
        }
    }
}

// --- Screen as a light: never flashes (decision §3.3)
private val Warm = Color(0xFFFFC46B)

@Composable
internal fun ScreenLightScreen() {
    FullBrightness()
    var warm by rememberSaveable { mutableStateOf(false) }
    val background = if (warm) Warm else Color.White
    Box(
        Modifier
            .fillMaxSize()
            .background(background)
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.tools_screen_light_switch)) { warm = !warm },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            stringResource(R.string.tools_screen_light_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp),
        )
    }
}

// --- SOS light (decision §3.1, proposal A)
@Composable
internal fun SosScreen(viewModel: ToolContentViewModel = hiltViewModel()) {
    val bundle by viewModel.bundle.collectAsStateWithLifecycle()
    val torch = viewModel.torch
    var running by rememberSaveable { mutableStateOf(false) }
    var screenOn by remember { mutableStateOf(false) }

    if (running) {
        if (!torch.isAvailable) FullBrightness()
        LaunchedEffect(Unit) {
            SosPattern.play { on -> if (torch.isAvailable) torch.set(on) else screenOn = on }
        }
    }
    DisposableEffect(Unit) { onDispose { torch.set(false) } }

    val flashArea = running && !torch.isAvailable
    Column(
        Modifier
            .fillMaxSize()
            .background(if (flashArea && screenOn) Color.White else Color.Transparent),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                BigActionButton(
                    text = stringResource(if (running) R.string.tools_sos_stop else R.string.tools_sos_start),
                    icon = if (running) Icons.Outlined.Stop else Icons.Outlined.Sos,
                    onClick = { running = !running },
                )
            }
            if (!flashArea) {
                item { Text(stringResource(R.string.tools_sos_what), style = MaterialTheme.typography.bodyLarge) }
                item {
                    Text(
                        stringResource(if (torch.isAvailable) R.string.tools_sos_uses_torch else R.string.tools_sos_uses_screen),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                item { NoOfficialSource(R.string.tools_sos_no_source) }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.tools_sos_flash_warning), style = MaterialTheme.typography.bodyMedium)
                        bundle?.let { b -> SourcesLink(listOfNotNull(b.source("w3c-wcag22-flashes"))) }
                    }
                }
                item { DisclaimerFooter() }
            }
        }
    }
}

@Composable
private fun NoOfficialSource(text: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(text), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        Text(stringResource(R.string.tools_call_112_first), style = MaterialTheme.typography.bodyLarge)
    }
}

// --- Whistle (decision §3.2, proposal A)
@Composable
internal fun WhistleScreen(viewModel: ToolContentViewModel = hiltViewModel()) {
    val bundle by viewModel.bundle.collectAsStateWithLifecycle()
    var running by rememberSaveable { mutableStateOf(false) }
    if (running) {
        LaunchedEffect(Unit) { withContext(Dispatchers.Default) { Whistle.play() } }
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            BigActionButton(
                text = stringResource(if (running) R.string.tools_whistle_stop else R.string.tools_whistle_start),
                icon = if (running) Icons.Outlined.Stop else Icons.Outlined.Campaign,
                onClick = { running = !running },
            )
        }
        item { Text(stringResource(R.string.tools_whistle_ear), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.tools_whistle_real), style = MaterialTheme.typography.bodyLarge)
                bundle?.let { b -> SourcesLink(listOfNotNull(b.source("anepc-sismos-kit"))) }
            }
        }
        item { NoOfficialSource(R.string.tools_whistle_no_source) }
        item { DisclaimerFooter() }
    }
}

// --- Battery (decision §3.4) + guide (§4)
@Composable
internal fun BatteryScreen(viewModel: ToolContentViewModel = hiltViewModel()) {
    val bundle by viewModel.bundle.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val level = remember {
        context.getSystemService(BatteryManager::class.java)?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            ?.takeIf { it in 0..100 }
    }
    val guide = bundle?.battery
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        level?.let {
            item {
                Text(
                    stringResource(R.string.tools_battery_level, it),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() },
                )
            }
        }
        item {
            BigActionButton(
                text = stringResource(R.string.tools_battery_open_saver),
                icon = Icons.Outlined.BatterySaver,
                onClick = { context.openBatterySaver() },
            )
        }
        if (guide != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Text(guide.title.text(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                    Text(guide.summary.text(), style = MaterialTheme.typography.bodyLarge)
                }
            }
            itemsIndexed(guide.steps, key = { _, s -> s.id }) { i, step ->
                StepCard(i + 1, step, step.sourceIds.mapNotNull { bundle?.source(it) })
            }
        }
        item { DisclaimerFooter() }
        item { Box(Modifier.fillMaxWidth().height(8.dp)) }
    }
}

/** Opens Android's battery saver settings. The app never changes settings itself. */
private fun Context.openBatterySaver() {
    for (action in listOf(Settings.ACTION_BATTERY_SAVER_SETTINGS, Settings.ACTION_SETTINGS)) {
        try {
            startActivity(Intent(action))
            return
        } catch (_: ActivityNotFoundException) {
            // try the next one
        }
    }
}
