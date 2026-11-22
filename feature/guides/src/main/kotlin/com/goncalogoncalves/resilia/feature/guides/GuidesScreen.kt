package com.goncalogoncalves.resilia.feature.guides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.component.DraftBadge
import com.goncalogoncalves.resilia.core.designsystem.component.HubCard
import com.goncalogoncalves.resilia.core.designsystem.component.currentContentLanguage
import com.goncalogoncalves.resilia.core.designsystem.component.scenarioIcon
import com.goncalogoncalves.resilia.core.designsystem.component.text
import com.goncalogoncalves.resilia.core.domain.model.ContentLanguage
import com.goncalogoncalves.resilia.core.domain.model.ReviewStatus
import com.goncalogoncalves.resilia.core.domain.model.Scenario
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import com.goncalogoncalves.resilia.core.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.text.Normalizer
import javax.inject.Inject

data class GuidesUiState(
    val scenarios: List<Scenario> = emptyList(),
    val favorites: Set<String> = emptySet(),
)

@HiltViewModel
class GuidesViewModel @Inject constructor(
    content: ContentRepository,
    favorites: FavoritesRepository,
) : ViewModel() {
    val uiState = combine(flow { emit(content.load().scenarios) }, favorites.favorites) { scenarios, favs ->
        GuidesUiState(scenarios, favs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GuidesUiState())
}

/**
 * Accent- and case-insensitive search ("apagao" finds "apagão") over title, keywords,
 * summary and the text of every step. Every word of the query must appear somewhere.
 */
internal fun Scenario.matches(query: String, language: ContentLanguage): Boolean {
    if (query.isBlank()) return true
    val haystack = buildList {
        add(title.resolve(language))
        addAll(keywords[language.tag].orEmpty())
        add(summary.resolve(language))
        phases.all.forEach { step ->
            add(step.text.resolve(language))
            step.detail?.let { add(it.resolve(language)) }
        }
    }.joinToString(" ").normalizedForSearch()
    return query.normalizedForSearch().split(Regex("\\s+")).all { haystack.contains(it) }
}

/** Favourites first, then the bundled order. */
internal fun List<Scenario>.sortedByFavorite(favorites: Set<String>): List<Scenario> =
    sortedBy { if (it.id in favorites) 0 else 1 }

private fun String.normalizedForSearch(): String =
    Normalizer.normalize(lowercase().trim(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")

@Composable
internal fun GuidesScreen(
    onOpenScenario: (String) -> Unit,
    viewModel: GuidesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scenarios = state.scenarios
    var query by rememberSaveable { mutableStateOf("") }
    val language = currentContentLanguage()
    val visible = scenarios.filter { it.matches(query, language) }.sortedByFavorite(state.favorites)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.guides_search)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        items(visible, key = { it.id }) { scenario ->
            HubCard(
                title = scenario.title.text(),
                subtitle = scenario.summary.text(),
                icon = scenarioIcon(scenario.icon),
                onClick = { onOpenScenario(scenario.id) },
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (scenario.id in state.favorites) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = stringResource(R.string.guides_favorite_on),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        if (scenario.effectiveReviewStatus == ReviewStatus.DRAFT) {
                            DraftBadge()
                        } else {
                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
                        }
                    }
                },
            )
        }
        if (visible.isEmpty() && scenarios.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.guides_no_results),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
