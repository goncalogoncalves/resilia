package com.goncalogoncalves.resilia.feature.guides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.component.BigActionButton
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.currentContentLanguage
import com.goncalogoncalves.resilia.core.designsystem.component.HubCard
import com.goncalogoncalves.resilia.core.designsystem.component.SourceItem
import com.goncalogoncalves.resilia.core.designsystem.component.StepCard
import com.goncalogoncalves.resilia.core.designsystem.component.publisherLabel
import com.goncalogoncalves.resilia.core.designsystem.component.scenarioIcon
import com.goncalogoncalves.resilia.core.designsystem.component.text
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.Phase
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Scenario
import com.goncalogoncalves.resilia.core.domain.model.Source
import com.goncalogoncalves.resilia.core.domain.model.Step
import com.goncalogoncalves.resilia.core.domain.model.of
import com.goncalogoncalves.resilia.core.domain.repository.AppStateRepository
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import com.goncalogoncalves.resilia.core.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScenarioUiState(
    val scenario: Scenario,
    /** All sources, by id, so steps can resolve theirs. */
    val sources: Map<String, Source>,
    val isFavorite: Boolean,
    /** "See also" guides, in the order of `relatedScenarioIds`. */
    val related: List<Scenario> = emptyList(),
    /** Kit item names by id, for "In the kit: …" under a step. */
    val kitItems: Map<String, LocalizedText> = emptyMap(),
)

@HiltViewModel
class ScenarioViewModel @Inject constructor(
    private val content: ContentRepository,
    private val appState: AppStateRepository,
    private val favorites: FavoritesRepository,
) : ViewModel() {
    private val bundle = MutableStateFlow<Pair<String, ContentBundle>?>(null)

    val uiState = combine(bundle, favorites.favorites) { loaded, favs ->
        val (id, b) = loaded ?: return@combine null
        b.scenario(id)?.let { sc ->
            ScenarioUiState(
                scenario = sc,
                sources = b.sources.associateBy { s -> s.id },
                isFavorite = id in favs,
                related = sc.relatedScenarioIds.mapNotNull(b::scenario),
                kitItems = b.kit?.items.orEmpty().associate { it.id to it.name },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun load(id: String) {
        if (bundle.value?.first == id) return
        viewModelScope.launch { bundle.value = id to content.load() }
    }

    fun setFavorite(id: String, favorite: Boolean) = viewModelScope.launch {
        favorites.setFavorite(id, favorite)
    }

    fun activateEmergency(id: String) = viewModelScope.launch {
        appState.setEmergencyMode(active = true, scenarioId = id)
    }
}

@Composable
internal fun ScenarioScreen(
    scenarioId: String,
    onOpenScenario: (String) -> Unit,
    onOpenKit: () -> Unit = {},
    viewModel: ScenarioViewModel = hiltViewModel(),
) {
    viewModel.load(scenarioId)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    state?.let { s ->
        ScenarioContent(
            state = s,
            onToggleFavorite = { viewModel.setFavorite(s.scenario.id, it) },
            onActivateEmergency = { viewModel.activateEmergency(s.scenario.id) },
            onOpenScenario = onOpenScenario,
            onOpenKit = onOpenKit,
        )
    }
}

@Composable
internal fun ScenarioContent(
    state: ScenarioUiState,
    onToggleFavorite: (Boolean) -> Unit,
    onActivateEmergency: () -> Unit,
    onOpenScenario: (String) -> Unit = {},
    onOpenKit: () -> Unit = {},
) {
    val scenario = state.scenario
    val language = currentContentLanguage()
    // Default to "During": the most likely reason to open a guide.
    var tab by rememberSaveable { mutableIntStateOf(Phase.DURING.ordinal) }
    val phase = Phase.entries[tab]
    val steps = scenario.phases.of(phase)
    val guideSources = scenario.sourceIds.mapNotNull { state.sources[it] }

    Column(Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = tab) {
            Phase.entries.forEach { p ->
                Tab(
                    selected = tab == p.ordinal,
                    onClick = { tab = p.ordinal },
                    text = { Text(stringResource(p.label()), style = MaterialTheme.typography.titleSmall) },
                )
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            scenario.title.text(),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.weight(1f).semantics { heading() },
                        )
                        IconToggleButton(checked = state.isFavorite, onCheckedChange = onToggleFavorite) {
                            Icon(
                                if (state.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = stringResource(
                                    if (state.isFavorite) R.string.guides_favorite_on else R.string.guides_favorite_off,
                                ),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Text(scenario.summary.text(), style = MaterialTheme.typography.bodyLarge)
                }
            }
            itemsIndexed(steps, key = { _, s -> s.id }) { index, step ->
                StepCard(
                    number = index + 1,
                    step = step,
                    sources = step.sourceIds.mapNotNull { state.sources[it] },
                    kitItems = step.relatedKitItemIds.mapNotNull { state.kitItems[it]?.resolve(language) },
                    onOpenKit = onOpenKit,
                )
            }
            if (steps.isEmpty()) {
                item { Text(stringResource(R.string.guides_phase_empty), style = MaterialTheme.typography.bodyLarge) }
            }
            item {
                BigActionButton(
                    text = stringResource(R.string.guides_activate_emergency),
                    icon = Icons.Outlined.Sos,
                    onClick = onActivateEmergency,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            item { DisclaimerFooter() }
            if (state.related.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.guides_related_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp).semantics { heading() },
                    )
                }
                items(state.related, key = { "rel-" + it.id }) { rel ->
                    HubCard(
                        title = rel.title.text(),
                        icon = scenarioIcon(rel.icon),
                        onClick = { onOpenScenario(rel.id) },
                    )
                }
            }
            if (guideSources.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.guides_sources_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp).semantics { heading() },
                    )
                }
                items(guideSources, key = { "src-" + it.id }) { SourceItem(it) }
            }
        }
    }
}

private fun Phase.label() = when (this) {
    Phase.BEFORE -> R.string.guides_phase_before
    Phase.DURING -> R.string.guides_phase_during
    Phase.AFTER -> R.string.guides_phase_after
}
