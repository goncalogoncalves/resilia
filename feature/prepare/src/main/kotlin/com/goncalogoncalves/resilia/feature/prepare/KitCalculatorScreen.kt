package com.goncalogoncalves.resilia.feature.prepare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.SourcesLink
import com.goncalogoncalves.resilia.core.designsystem.component.publisherLabel
import com.goncalogoncalves.resilia.core.designsystem.component.text
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.Household
import com.goncalogoncalves.resilia.core.domain.model.InventoryItem
import com.goncalogoncalves.resilia.core.domain.model.KitCalculator
import com.goncalogoncalves.resilia.core.domain.model.KitLine
import com.goncalogoncalves.resilia.core.domain.model.KitList
import com.goncalogoncalves.resilia.core.domain.model.KitQuantityKind
import com.goncalogoncalves.resilia.core.domain.model.Source
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import com.goncalogoncalves.resilia.core.domain.repository.InventoryRepository
import com.goncalogoncalves.resilia.core.domain.repository.PreparednessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class KitUiState(
    val bundle: ContentBundle,
    val household: Household,
    /** Catalogue ids already in the inventory. */
    val inInventory: Set<String>,
)

@HiltViewModel
class KitCalculatorViewModel @Inject constructor(
    content: ContentRepository,
    private val preparedness: PreparednessRepository,
    private val inventory: InventoryRepository,
) : ViewModel() {

    val uiState = combine(
        flow { emit(content.load()) },
        preparedness.state,
        inventory.items,
    ) { bundle, prep, items ->
        KitUiState(bundle, prep.household, items.mapNotNull { it.kitItemId }.toSet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun update(household: Household) = viewModelScope.launch { preparedness.setHousehold(household) }

    /** The user fills in what they actually have (quantity, expiry) in the inventory. */
    fun addToInventory(line: KitLine, name: String) {
        viewModelScope.launch {
            inventory.save(
                InventoryItem(
                    name = name,
                    list = line.item.list,
                    kitItemId = line.item.id,
                    addedOn = LocalDate.now(),
                ),
            )
        }
    }
}

@Composable
internal fun KitCalculatorScreen(
    onOpenInventory: () -> Unit,
    viewModel: KitCalculatorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    state?.let { s ->
        KitCalculatorContent(
            state = s,
            onChange = viewModel::update,
            onAdd = viewModel::addToInventory,
            onOpenInventory = onOpenInventory,
        )
    }
}

@Composable
internal fun KitCalculatorContent(
    state: KitUiState,
    onChange: (Household) -> Unit,
    onAdd: (KitLine, String) -> Unit,
    onOpenInventory: () -> Unit,
) {
    val kit = state.bundle.kit ?: return
    val h = state.household.copy(days = state.household.days.coerceAtLeast(kit.minimumDays))
    var tab by rememberSaveable { mutableStateOf(KitList.HOME) }
    val lines = KitCalculator.calculate(kit, h, tab)
    val sources = state.bundle.sources.associateBy { it.id }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(stringResource(R.string.prepare_kit_intro), style = MaterialTheme.typography.bodyLarge) }

        // --- People
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.prepare_kit_people),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                FilledTonalIconButton(
                    onClick = { onChange(h.copy(people = h.people - 1)) },
                    enabled = h.people > 1,
                ) { Icon(Icons.Outlined.Remove, stringResource(R.string.prepare_kit_people_less)) }
                Text(
                    h.people.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                FilledTonalIconButton(
                    onClick = { onChange(h.copy(people = h.people + 1)) },
                    enabled = h.people < Household.MAX_PEOPLE,
                ) { Icon(Icons.Outlined.Add, stringResource(R.string.prepare_kit_people_more)) }
            }
        }

        // --- Days
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.prepare_kit_days), style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    kit.dayOptions.forEach { d ->
                        FilterChip(
                            selected = h.days == d,
                            onClick = { onChange(h.copy(days = d)) },
                            label = { Text(pluralStringResource(R.plurals.prepare_days, d, d)) },
                            modifier = Modifier.heightIn(min = 48.dp),
                        )
                    }
                }
                SourcesLink(
                    kit.minimumDaysSourceIds.mapNotNull { sources[it] },
                    label = stringResource(
                        R.string.prepare_kit_minimum,
                        pluralStringResource(R.plurals.prepare_days, kit.minimumDays, kit.minimumDays),
                        kit.minimumDaysSourceIds.mapNotNull { sources[it] }.map { it.publisherLabel() }.distinct().joinToString(", "),
                    ),
                )
            }
        }

        // --- Household needs
        item { NeedSwitch(stringResource(R.string.prepare_kit_baby), h.hasBaby) { onChange(h.copy(hasBaby = it)) } }
        item { NeedSwitch(stringResource(R.string.prepare_kit_medical), h.hasMedicalNeeds) { onChange(h.copy(hasMedicalNeeds = it)) } }
        item { NeedSwitch(stringResource(R.string.prepare_kit_pets), h.hasPets) { onChange(h.copy(hasPets = it)) } }

        item {
            PrimaryTabRow(selectedTabIndex = tab.ordinal) {
                KitList.entries.forEach { l ->
                    Tab(
                        selected = tab == l,
                        onClick = { tab = l },
                        text = { Text(stringResource(l.label()), style = MaterialTheme.typography.titleSmall) },
                    )
                }
            }
        }
        item {
            Text(
                stringResource(if (tab == KitList.HOME) R.string.prepare_list_home_intro else R.string.prepare_list_bag_intro),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        items(lines, key = { it.item.id }) { line ->
            KitLineCard(
                line = line,
                sources = line.item.sourceIds.mapNotNull { sources[it] },
                inInventory = line.item.id in state.inInventory,
                onAdd = onAdd,
            )
        }
        item {
            TextButton(onClick = onOpenInventory, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.prepare_open_inventory), style = MaterialTheme.typography.labelLarge)
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
            }
        }
        item { DisclaimerFooter() }
    }
}

@Composable
private fun NeedSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}

internal fun KitList.label() = when (this) {
    KitList.HOME -> R.string.prepare_list_home
    KitList.BAG -> R.string.prepare_list_bag
}

/** "24" for whole numbers, otherwise one decimal. */
internal fun formatAmount(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else "%.1f".format(value)

@Composable
internal fun KitLine.amountText(): String? {
    val q = item.quantity ?: return null
    return when (q.kind) {
        KitQuantityKind.PER_PERSON_PER_DAY -> stringResource(
            R.string.prepare_amount_litres_per_person,
            formatAmount(total ?: 0.0),
            formatAmount(q.amount ?: 0.0),
        )
        KitQuantityKind.FOR_DURATION -> stringResource(
            R.string.prepare_amount_for_duration,
            pluralStringResource(R.plurals.prepare_people, people, people),
            pluralStringResource(R.plurals.prepare_days, days, days),
        )
    }
}

@Composable
private fun KitLineCard(
    line: KitLine,
    sources: List<Source>,
    inInventory: Boolean,
    onAdd: (KitLine, String) -> Unit,
) {
    val name = line.item.name.text()
    val amount = line.amountText()
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            amount?.let {
                Text(it, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            line.item.detail?.let { Text(it.text(), style = MaterialTheme.typography.bodyMedium) }
            line.item.note?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(it.text(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            SourcesLink(sources)
            if (inInventory) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.prepare_in_inventory), style = MaterialTheme.typography.labelLarge)
                }
            } else {
                TextButton(
                    onClick = { onAdd(line, name) },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.prepare_add_to_inventory), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
