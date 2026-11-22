package com.goncalogoncalves.resilia.feature.morale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.HubCard
import com.goncalogoncalves.resilia.core.designsystem.component.text
import com.goncalogoncalves.resilia.core.domain.model.Activity
import com.goncalogoncalves.resilia.core.domain.model.ActivityFilter
import com.goncalogoncalves.resilia.core.domain.model.ActivityType
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.LightNeed
import com.goncalogoncalves.resilia.core.domain.repository.ActivityFilterRepository
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoraleViewModel @Inject constructor(
    content: ContentRepository,
    private val filters: ActivityFilterRepository,
) : ViewModel() {
    val bundle = flow<ContentBundle?> { emit(content.load()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val filter = filters.filter.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivityFilter())

    fun setFilter(f: ActivityFilter) = viewModelScope.launch { filters.setFilter(f) }
}

/** Calma tab: emotional support on top, then screen-free activities with filters. */
@Composable
internal fun MoraleScreen(
    onOpenSupport: () -> Unit,
    onOpenActivity: (String) -> Unit,
    viewModel: MoraleViewModel = hiltViewModel(),
) {
    val bundle by viewModel.bundle.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    MoraleContent(
        activities = bundle?.activities.orEmpty(),
        filter = filter,
        onFilter = viewModel::setFilter,
        onOpenSupport = onOpenSupport,
        onOpenActivity = onOpenActivity,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MoraleContent(
    activities: List<Activity>,
    filter: ActivityFilter,
    onFilter: (ActivityFilter) -> Unit,
    onOpenSupport: () -> Unit,
    onOpenActivity: (String) -> Unit,
) {
    val shown = activities.filter(filter::matches)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HubCard(
                title = stringResource(R.string.morale_support),
                icon = Icons.Outlined.VolunteerActivism,
                subtitle = stringResource(R.string.morale_support_subtitle),
                onClick = onOpenSupport,
            )
        }
        item {
            Text(
                stringResource(R.string.morale_activities_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
        }
        item { Text(stringResource(R.string.morale_intro), style = MaterialTheme.typography.bodyLarge) }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filter.type == null,
                    onClick = { onFilter(filter.copy(type = null)) },
                    label = { Text(stringResource(R.string.morale_type_all)) },
                )
                ActivityType.entries.forEach { t ->
                    FilterChip(
                        selected = filter.type == t,
                        onClick = { onFilter(filter.copy(type = if (filter.type == t) null else t)) },
                        label = { Text(stringResource(t.label())) },
                    )
                }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Toggle(R.string.morale_filter_dark, filter.dark) { onFilter(filter.copy(dark = it)) }
                Toggle(R.string.morale_filter_quiet, filter.quiet) { onFilter(filter.copy(quiet = it)) }
                Toggle(R.string.morale_filter_no_materials, filter.noMaterials) { onFilter(filter.copy(noMaterials = it)) }
                Toggle(R.string.morale_filter_alone, filter.alone) { onFilter(filter.copy(alone = it)) }
                Toggle(R.string.morale_filter_short, filter.short) { onFilter(filter.copy(short = it)) }
                AgeChip(filter.age) { onFilter(filter.copy(age = it)) }
            }
        }
        if (shown.isEmpty()) {
            item { Text(stringResource(R.string.morale_no_results), style = MaterialTheme.typography.bodyLarge) }
            item {
                TextButton(onClick = { onFilter(ActivityFilter()) }) {
                    Text(stringResource(R.string.morale_clear_filters), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        items(shown, key = { it.id }) { a ->
            HubCard(
                title = a.title.text(),
                icon = a.type.icon(),
                subtitle = a.summary.text() + "\n" + a.metaLine(),
                onClick = { onOpenActivity(a.id) },
            )
        }
        item { DisclaimerFooter() }
    }
}

@Composable
private fun Toggle(label: Int, selected: Boolean, onChange: (Boolean) -> Unit) {
    FilterChip(selected = selected, onClick = { onChange(!selected) }, label = { Text(stringResource(label)) })
}

@Composable
private fun AgeChip(age: Int?, onChange: (Int?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = age != null,
            onClick = { open = true },
            label = {
                Text(
                    if (age == null) stringResource(R.string.morale_filter_age_any)
                    else stringResource(R.string.morale_filter_age, age),
                )
            },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.morale_filter_age_any)) },
                onClick = { onChange(null); open = false },
            )
            ActivityFilter.AGE_OPTIONS.forEach { a ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.morale_filter_age, a)) },
                    onClick = { onChange(a); open = false },
                )
            }
        }
    }
}

internal fun ActivityType.label() = when (this) {
    ActivityType.GAME -> R.string.morale_games
    ActivityType.STORIES -> R.string.morale_stories
    ActivityType.CALM -> R.string.morale_calm
    ActivityType.CHALLENGE -> R.string.morale_challenges
}

internal fun ActivityType.icon(): ImageVector = when (this) {
    ActivityType.GAME -> Icons.Outlined.Casino
    ActivityType.STORIES -> Icons.Outlined.AutoStories
    ActivityType.CALM -> Icons.Outlined.SelfImprovement
    ActivityType.CHALLENGE -> Icons.Outlined.EmojiEvents
}

/** "10 min · 2+ · in the dark" */
@Composable
internal fun Activity.metaLine(): String = listOfNotNull(
    stringResource(R.string.morale_meta_minutes, durationMin),
    stringResource(R.string.morale_meta_age, ageMin),
    when (light) {
        LightNeed.NONE -> stringResource(R.string.morale_light_none_short)
        LightNeed.TORCH -> stringResource(R.string.morale_light_torch_short)
        LightNeed.LIGHT -> null
    },
).joinToString(" · ")
