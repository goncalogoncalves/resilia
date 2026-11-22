package com.goncalogoncalves.resilia.feature.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.domain.device.AlertScheduler
import com.goncalogoncalves.resilia.core.domain.model.AlertSettings
import com.goncalogoncalves.resilia.core.domain.model.AlertSnapshot
import com.goncalogoncalves.resilia.core.domain.model.AlertZone
import com.goncalogoncalves.resilia.core.domain.model.Alerts
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.repository.AlertsRepository
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class AlertsUiState(
    val bundle: ContentBundle,
    val settings: AlertSettings,
    val snapshot: AlertSnapshot,
    val now: Instant,
    val refreshing: Boolean,
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    content: ContentRepository,
    private val alerts: AlertsRepository,
    private val scheduler: AlertScheduler,
) : ViewModel() {

    private val refreshing = MutableStateFlow(false)

    /** Ticks once a minute so "downloaded X ago" and "out of date" stay right while the screen is open. */
    private val clock = flow {
        while (true) {
            emit(Instant.now())
            delay(60_000)
        }
    }

    private val bundle = flow { emit(content.load()) }

    val state: StateFlow<AlertsUiState?> =
        combine(bundle, alerts.settings, alerts.snapshot, clock, refreshing) { b, s, snap, now, r ->
            AlertsUiState(b, s, snap, now, r)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Opening the screen refreshes when the data is more than an hour old (decision 2).
        viewModelScope.launch {
            val settings = alerts.settings.first()
            if (settings.zone != null && Alerts.shouldRefreshOnOpen(alerts.snapshot.first(), Instant.now())) refresh()
        }
    }

    fun refresh() {
        if (refreshing.value) return
        viewModelScope.launch {
            refreshing.value = true
            try {
                alerts.refresh()
            } finally {
                refreshing.value = false
            }
        }
    }

    fun setZone(zone: AlertZone?) {
        viewModelScope.launch {
            val before = alerts.settings.first().zone
            alerts.setZone(zone)
            scheduler.schedule(alerts.settings.first())
            if (zone != null && zone != before) refresh()
        }
    }

    fun setInterval(hours: Int) = update { alerts.setInterval(hours) }

    fun setNotifyYellow(enabled: Boolean) = update { alerts.setNotifyYellow(enabled) }

    private fun update(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
            scheduler.schedule(alerts.settings.first())
        }
    }
}
