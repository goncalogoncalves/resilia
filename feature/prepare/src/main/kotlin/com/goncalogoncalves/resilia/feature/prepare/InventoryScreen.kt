package com.goncalogoncalves.resilia.feature.prepare

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.component.BigActionButton
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.SourcesLink
import com.goncalogoncalves.resilia.core.domain.device.ReminderScheduler
import com.goncalogoncalves.resilia.core.domain.model.Expiry
import com.goncalogoncalves.resilia.core.domain.model.ExpiryStatus
import com.goncalogoncalves.resilia.core.domain.model.InventoryItem
import com.goncalogoncalves.resilia.core.domain.model.KitList
import com.goncalogoncalves.resilia.core.domain.model.KitReview
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

data class InventoryUiState(
    val items: List<InventoryItem>,
    val today: LocalDate,
    val remindersEnabled: Boolean,
    /** Null while the inventory is empty. */
    val nextReview: LocalDate?,
    val reviewSources: List<Source>,
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    content: ContentRepository,
    private val inventory: InventoryRepository,
    private val preparedness: PreparednessRepository,
    private val scheduler: ReminderScheduler,
) : ViewModel() {

    val uiState = combine(flow { emit(content.load()) }, inventory.items, preparedness.state) { bundle, items, prep ->
        val kit = bundle.kit
        InventoryUiState(
            items = items,
            today = LocalDate.now(),
            remindersEnabled = prep.remindersEnabled,
            nextReview = kit?.let {
                KitReview.nextReview(prep.lastReviewedOn, items.minOfOrNull { i -> i.addedOn }, it.reviewIntervalMonths)
            }?.takeIf { items.isNotEmpty() },
            reviewSources = kit?.reviewSourceIds.orEmpty().mapNotNull(bundle::source),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(item: InventoryItem) = viewModelScope.launch {
        // A new expiry date re-arms its reminders.
        val old = uiState.value?.items?.firstOrNull { it.id == item.id }
        val rearmed = if (old != null && old.expiresOn != item.expiresOn) {
            item.copy(notifiedSoonFor = null, notifiedExpiredFor = null)
        } else item
        inventory.save(rearmed)
    }

    fun delete(id: Long) = viewModelScope.launch { inventory.delete(id) }

    fun markReviewed() = viewModelScope.launch { preparedness.markReviewed(LocalDate.now()) }

    fun setReminders(enabled: Boolean) = viewModelScope.launch {
        preparedness.setRemindersEnabled(enabled)
        scheduler.setEnabled(enabled)
    }
}

@Composable
internal fun InventoryScreen(viewModel: InventoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    state?.let { s ->
        InventoryContent(
            state = s,
            onSave = viewModel::save,
            onDelete = viewModel::delete,
            onMarkReviewed = viewModel::markReviewed,
            onSetReminders = viewModel::setReminders,
        )
    }
}

@Composable
internal fun InventoryContent(
    state: InventoryUiState,
    onSave: (InventoryItem) -> Unit,
    onDelete: (Long) -> Unit,
    onMarkReviewed: () -> Unit,
    onSetReminders: (Boolean) -> Unit,
) {
    var editing by remember { mutableStateOf<InventoryItem?>(null) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(stringResource(R.string.prepare_inventory_intro), style = MaterialTheme.typography.bodyLarge) }
        item {
            BigActionButton(
                text = stringResource(R.string.prepare_inventory_add),
                icon = Icons.Outlined.Add,
                onClick = { editing = InventoryItem(name = "", addedOn = state.today) },
            )
        }
        if (state.nextReview != null) {
            item { ReviewCard(state.nextReview, state.today, state.reviewSources, onMarkReviewed) }
        }
        item { RemindersCard(state.remindersEnabled, onSetReminders) }
        if (state.items.isEmpty()) {
            item { Text(stringResource(R.string.prepare_inventory_empty), style = MaterialTheme.typography.bodyLarge) }
        }
        items(state.items, key = { it.id }) { item ->
            InventoryRow(item, state.today) { editing = item }
        }
        item { DisclaimerFooter() }
    }
    editing?.let { item ->
        EditItemDialog(
            initial = item,
            onDismiss = { editing = null },
            onSave = { onSave(it); editing = null },
            onDelete = if (item.id != 0L) ({ onDelete(item.id); editing = null }) else null,
        )
    }
}

private fun LocalDate.display(): String =
    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

@Composable
private fun ExpiryStatus.label(): String = when (this) {
    ExpiryStatus.NoDate -> stringResource(R.string.prepare_expiry_none)
    ExpiryStatus.Today -> stringResource(R.string.prepare_expiry_today)
    is ExpiryStatus.Expired -> pluralStringResource(R.plurals.prepare_expiry_expired, daysAgo.toInt(), daysAgo.toInt())
    is ExpiryStatus.Soon -> pluralStringResource(R.plurals.prepare_expiry_soon, days.toInt(), days.toInt())
    is ExpiryStatus.Ok -> stringResource(R.string.prepare_expiry_ok)
}

@Composable
private fun ExpiryStatus.color(): Color = when (this) {
    ExpiryStatus.Today, is ExpiryStatus.Expired -> MaterialTheme.colorScheme.error
    is ExpiryStatus.Soon -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun InventoryRow(item: InventoryItem, today: LocalDate, onClick: () -> Unit) {
    val status = Expiry.status(item.expiresOn, today)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.prepare_inventory_edit), onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            val meta = listOfNotNull(stringResource(item.list.label()), item.quantity.ifBlank { null })
            Text(meta.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
            val dateText = item.expiresOn?.let { stringResource(R.string.prepare_expiry_date, it.display()) + " · " }.orEmpty()
            Text(dateText + status.label(), style = MaterialTheme.typography.labelLarge, color = status.color())
        }
    }
}

@Composable
private fun ReviewCard(next: LocalDate, today: LocalDate, sources: List<Source>, onMarkReviewed: () -> Unit) {
    val due = KitReview.isDue(next, today)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (due) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.EventRepeat, contentDescription = null)
                Text(
                    stringResource(R.string.prepare_review_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() },
                )
            }
            Text(
                if (due) stringResource(R.string.prepare_review_due) else stringResource(R.string.prepare_review_next, next.display()),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(stringResource(R.string.prepare_review_hint), style = MaterialTheme.typography.bodyMedium)
            SourcesLink(sources)
            FilledTonalButton(onClick = onMarkReviewed, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.prepare_review_done))
            }
        }
    }
}

private fun Context.notificationsAllowed(): Boolean {
    val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    return granted && NotificationManagerCompat.from(this).areNotificationsEnabled()
}

/**
 * Decision §5.3: the notification permission is asked only here, after an explanation,
 * when the user turns reminders on. Refusing keeps everything else working.
 */
@Composable
private fun RemindersCard(enabled: Boolean, onSet: (Boolean) -> Unit) {
    val context = LocalContext.current
    var explain by rememberSaveable { mutableStateOf(false) }
    var denied by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        denied = !granted
        if (granted) onSet(true)
    }
    val blocked = enabled && !context.notificationsAllowed()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .toggleable(
                        value = enabled,
                        role = Role.Switch,
                        onValueChange = { on -> if (on) explain = true else onSet(false) },
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Outlined.NotificationsActive, contentDescription = null)
                Text(stringResource(R.string.prepare_reminders), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(checked = enabled, onCheckedChange = null)
            }
            Text(stringResource(R.string.prepare_reminders_hint), style = MaterialTheme.typography.bodyMedium)
            if (denied || blocked) {
                Text(
                    stringResource(R.string.prepare_reminders_denied),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    if (explain) {
        AlertDialog(
            onDismissRequest = { explain = false },
            title = { Text(stringResource(R.string.prepare_reminders_dialog_title)) },
            text = { Text(stringResource(R.string.prepare_reminders_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    explain = false
                    if (context.notificationsAllowed() || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        denied = false
                        onSet(true)
                    } else {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) { Text(stringResource(R.string.prepare_continue)) }
            },
            dismissButton = { TextButton(onClick = { explain = false }) { Text(stringResource(R.string.prepare_cancel)) } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemDialog(
    initial: InventoryItem,
    onDismiss: () -> Unit,
    onSave: (InventoryItem) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var quantity by rememberSaveable { mutableStateOf(initial.quantity) }
    var list by rememberSaveable { mutableStateOf(initial.list) }
    var expires by rememberSaveable { mutableStateOf(initial.expiresOn?.toString()) }
    var picking by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (initial.id == 0L) R.string.prepare_inventory_add else R.string.prepare_inventory_edit))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.prepare_field_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(stringResource(R.string.prepare_field_quantity)) },
                    placeholder = { Text(stringResource(R.string.prepare_field_quantity_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KitList.entries.forEach { l ->
                        FilterChip(
                            selected = list == l,
                            onClick = { list = l },
                            label = { Text(stringResource(l.label())) },
                            modifier = Modifier.heightIn(min = 48.dp),
                        )
                    }
                }
                val date = expires?.let(LocalDate::parse)
                Text(
                    date?.let { stringResource(R.string.prepare_expiry_date, it.display()) } ?: stringResource(R.string.prepare_expiry_none),
                    style = MaterialTheme.typography.bodyLarge,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { picking = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.prepare_field_pick_date))
                    }
                    if (date != null) {
                        TextButton(onClick = { expires = null }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text(stringResource(R.string.prepare_field_no_date))
                        }
                    }
                }
                if (onDelete != null) {
                    TextButton(onClick = { confirmDelete = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.prepare_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(initial.copy(name = name, quantity = quantity, list = list, expiresOn = expires?.let(LocalDate::parse)))
                },
            ) { Text(stringResource(R.string.prepare_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.prepare_cancel)) } },
    )

    if (picking) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (expires?.let(LocalDate::parse) ?: LocalDate.now())
                .atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        expires = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    picking = false
                }) { Text(stringResource(R.string.prepare_ok)) }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text(stringResource(R.string.prepare_cancel)) } },
        ) { DatePicker(state = pickerState) }
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.prepare_delete_title)) },
            text = { Text(stringResource(R.string.prepare_delete_text, name)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text(stringResource(R.string.prepare_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.prepare_cancel)) } },
        )
    }
}
