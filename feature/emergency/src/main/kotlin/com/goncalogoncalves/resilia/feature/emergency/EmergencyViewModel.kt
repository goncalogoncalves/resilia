package com.goncalogoncalves.resilia.feature.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.domain.device.Torch
import com.goncalogoncalves.resilia.core.domain.model.Contact
import com.goncalogoncalves.resilia.core.domain.model.Scenario
import com.goncalogoncalves.resilia.core.domain.model.Source
import com.goncalogoncalves.resilia.core.domain.repository.AppStateRepository
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmergencyUiState(
    val scenarios: List<Scenario> = emptyList(),
    val active: Scenario? = null,
    val contacts: List<Contact> = emptyList(),
    val sources: Map<String, Source> = emptyMap(),
)

/** Picks the scenario to show: the stored one, else the first bundled scenario. */
internal fun resolveActiveScenario(scenarios: List<Scenario>, activeId: String?): Scenario? =
    scenarios.firstOrNull { it.id == activeId } ?: scenarios.firstOrNull()

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    content: ContentRepository,
    private val appState: AppStateRepository,
    private val torch: Torch,
) : ViewModel() {

    val uiState: StateFlow<EmergencyUiState> = combine(
        flow { emit(content.load()) },
        appState.state,
    ) { bundle, state ->
        EmergencyUiState(
            scenarios = bundle.scenarios,
            active = resolveActiveScenario(bundle.scenarios, state.activeScenarioId),
            contacts = bundle.contacts,
            sources = bundle.sources.associateBy { it.id },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EmergencyUiState())

    val torchAvailable: Boolean get() = torch.isAvailable
    val torchOn: StateFlow<Boolean> = torch.isOn

    fun toggleTorch() {
        torch.set(!torch.isOn.value)
    }

    fun selectScenario(id: String) = viewModelScope.launch { appState.setActiveScenario(id) }

    fun exit() = viewModelScope.launch {
        torch.set(false)
        appState.setEmergencyMode(active = false)
    }
}
