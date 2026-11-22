package com.goncalogoncalves.resilia.feature.prepare.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.component.BigActionButton
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.SourcesLink
import com.goncalogoncalves.resilia.core.designsystem.component.text
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.FamilyPlan
import com.goncalogoncalves.resilia.core.domain.model.MedicalInfo
import com.goncalogoncalves.resilia.core.domain.model.Member
import com.goncalogoncalves.resilia.core.domain.model.PlanContact
import com.goncalogoncalves.resilia.core.domain.model.PlanSectionId
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import com.goncalogoncalves.resilia.core.domain.repository.FamilyPlanRepository
import com.goncalogoncalves.resilia.core.domain.repository.PreparednessRepository
import com.goncalogoncalves.resilia.feature.prepare.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class FamilyPlanUiState(
    val bundle: ContentBundle,
    val plan: FamilyPlan,
    val members: List<Member>,
    val contacts: List<PlanContact>,
    val medicalLock: Boolean,
    /** Unlocked for this visit to the screen only; never stored. */
    val unlocked: Boolean,
) {
    val medicalVisible: Boolean get() = !medicalLock || unlocked
}

@HiltViewModel
class FamilyPlanViewModel @Inject constructor(
    content: ContentRepository,
    private val repo: FamilyPlanRepository,
    private val preparedness: PreparednessRepository,
) : ViewModel() {

    /** Edited in memory and saved on every change (one small row). */
    private val draft = MutableStateFlow<FamilyPlan?>(null)
    private val unlocked = MutableStateFlow(false)
    private val saveLock = Mutex()

    init {
        viewModelScope.launch { draft.value = repo.plan.first() }
    }

    val uiState = combine(
        flow { emit(content.load()) },
        draft,
        repo.members,
        repo.contacts,
        combine(preparedness.state, unlocked) { p, u -> p.medicalLock to u },
    ) { bundle, plan, members, contacts, (lock, isUnlocked) ->
        plan?.let { FamilyPlanUiState(bundle, it, members, contacts, lock, isUnlocked) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun updatePlan(plan: FamilyPlan) {
        draft.value = plan
        viewModelScope.launch { saveLock.withLock { repo.savePlan(plan) } }
    }

    fun saveMember(member: Member) = viewModelScope.launch { repo.saveMember(member) }
    fun deleteMember(id: Long) = viewModelScope.launch { repo.deleteMember(id) }
    fun saveContact(contact: PlanContact) = viewModelScope.launch { repo.saveContact(contact) }
    fun deleteContact(id: Long) = viewModelScope.launch { repo.deleteContact(id) }
    fun unlock() { unlocked.value = true }
    fun setMedicalLock(enabled: Boolean) = viewModelScope.launch {
        preparedness.setMedicalLock(enabled)
        unlocked.value = enabled // just authenticated: stay unlocked for this visit
    }
}

@Composable
internal fun FamilyPlanScreen(onOpenPaper: () -> Unit, viewModel: FamilyPlanViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lock = rememberDeviceLock()
    val unlockTitle = stringResource(R.string.plan_unlock_title)
    state?.let { s ->
        FamilyPlanContent(
            state = s,
            onPlanChange = viewModel::updatePlan,
            onSaveMember = viewModel::saveMember,
            onDeleteMember = viewModel::deleteMember,
            onSaveContact = viewModel::saveContact,
            onDeleteContact = viewModel::deleteContact,
            onUnlock = { lock.authenticate(unlockTitle, viewModel::unlock) },
            lockAvailable = lock.isAvailable,
            onSetLock = { on -> lock.authenticate(unlockTitle) { viewModel.setMedicalLock(on) } },
            onOpenPaper = onOpenPaper,
        )
    }
}

@Composable
internal fun FamilyPlanContent(
    state: FamilyPlanUiState,
    onPlanChange: (FamilyPlan) -> Unit,
    onSaveMember: (Member) -> Unit,
    onDeleteMember: (Long) -> Unit,
    onSaveContact: (PlanContact) -> Unit,
    onDeleteContact: (Long) -> Unit,
    onUnlock: () -> Unit,
    lockAvailable: Boolean,
    onSetLock: (Boolean) -> Unit,
    onOpenPaper: () -> Unit,
) {
    val p = state.plan
    var editingMember by remember { mutableStateOf<Member?>(null) }
    var editingContact by remember { mutableStateOf<PlanContact?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(stringResource(R.string.plan_intro), style = MaterialTheme.typography.bodyLarge) }
        item {
            BigActionButton(text = stringResource(R.string.plan_open_paper), icon = Icons.Outlined.Print, onClick = onOpenPaper)
        }

        section(state, PlanSectionId.MEMBERS, R.string.plan_members) {
            state.members.forEach { m ->
                MemberRow(m, state.medicalVisible) { editingMember = m }
            }
            OutlinedButton(onClick = { editingMember = Member(name = "") }, modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.plan_add_member))
            }
        }
        section(state, PlanSectionId.MEETING, R.string.plan_meeting) {
            PlanField(R.string.plan_meeting_near, p.meetingNear) { onPlanChange(p.copy(meetingNear = it)) }
            PlanField(R.string.plan_meeting_far, p.meetingFar) { onPlanChange(p.copy(meetingFar = it)) }
        }
        section(state, PlanSectionId.CONTACT_PERSON, R.string.plan_contact_person) {
            PlanField(R.string.plan_field_name, p.contactName) { onPlanChange(p.copy(contactName = it)) }
            PlanField(R.string.plan_field_phone, p.contactPhone, phone = true) { onPlanChange(p.copy(contactPhone = it)) }
            PlanField(R.string.plan_field_address, p.contactAddress) { onPlanChange(p.copy(contactAddress = it)) }
        }
        section(state, PlanSectionId.ROLES, R.string.plan_roles) {
            PlanField(R.string.plan_roles_helps, p.helpsOthers) { onPlanChange(p.copy(helpsOthers = it)) }
            PlanField(R.string.plan_roles_kit, p.bringsKit) { onPlanChange(p.copy(bringsKit = it)) }
            PlanField(R.string.plan_roles_calls, p.callsEmergency) { onPlanChange(p.copy(callsEmergency = it)) }
        }
        section(state, PlanSectionId.EXITS, R.string.plan_exits) {
            PlanField(R.string.plan_exits_main, p.exitMain) { onPlanChange(p.copy(exitMain = it)) }
            PlanField(R.string.plan_exits_alt, p.exitAlternative) { onPlanChange(p.copy(exitAlternative = it)) }
        }
        section(state, PlanSectionId.SHUTOFF, R.string.plan_shutoff) {
            PlanField(R.string.plan_shutoff_power, p.shutoffPower) { onPlanChange(p.copy(shutoffPower = it)) }
            PlanField(R.string.plan_shutoff_water, p.shutoffWater) { onPlanChange(p.copy(shutoffWater = it)) }
            PlanField(R.string.plan_shutoff_gas, p.shutoffGas) { onPlanChange(p.copy(shutoffGas = it)) }
        }
        section(state, PlanSectionId.CONTACTS, R.string.plan_contacts) {
            state.bundle.contacts.forEach { c ->
                Text("${c.name.text()} · ${c.number}", style = MaterialTheme.typography.bodyMedium)
            }
            if (state.contacts.isNotEmpty()) HorizontalDivider()
            state.contacts.forEach { c ->
                Text(
                    "${c.name} · ${c.phone}" + if (c.onWalletCard) " · " + stringResource(R.string.plan_on_wallet) else "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable(onClickLabel = stringResource(R.string.plan_edit)) { editingContact = c }
                        .padding(vertical = 12.dp),
                )
            }
            OutlinedButton(onClick = { editingContact = PlanContact(name = "", phone = "") }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.plan_add_contact))
            }
        }
        section(state, PlanSectionId.SHELTER, R.string.plan_shelter) {
            PlanField(R.string.plan_shelter_field, p.shelter) { onPlanChange(p.copy(shelter = it)) }
        }
        section(state, PlanSectionId.REVIEW, R.string.plan_review) {}

        item { LockCard(state.medicalLock, lockAvailable, onSetLock) }
        item { DisclaimerFooter() }
    }

    editingMember?.let { m ->
        MemberDialog(
            initial = m,
            medicalVisible = state.medicalVisible,
            onUnlock = onUnlock,
            onDismiss = { editingMember = null },
            onSave = { onSaveMember(it); editingMember = null },
            onDelete = if (m.id != 0L) ({ onDeleteMember(m.id); editingMember = null }) else null,
        )
    }
    editingContact?.let { c ->
        ContactDialog(
            initial = c,
            walletFull = state.contacts.count { it.onWalletCard && it.id != c.id } >= PlanContact.WALLET_MAX,
            onDismiss = { editingContact = null },
            onSave = { onSaveContact(it); editingContact = null },
            onDelete = if (c.id != 0L) ({ onDeleteContact(c.id); editingContact = null }) else null,
        )
    }
}

/** A plan section: title, hint with sources and "Not yet reviewed", then the fields. */
private fun LazyListScope.section(
    state: FamilyPlanUiState,
    id: String,
    title: Int,
    content: @Composable () -> Unit,
) {
    item(key = id) {
        val guide = state.bundle.plan?.section(id)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                guide?.hint?.let { Text(it.text(), style = MaterialTheme.typography.bodyMedium) }
                guide?.let { g -> SourcesLink(g.sourceIds.mapNotNull(state.bundle::source)) }
                content()
            }
        }
    }
}

@Composable
private fun PlanField(label: Int, value: String, phone: Boolean = false, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        keyboardOptions = if (phone) KeyboardOptions(keyboardType = KeyboardType.Phone)
        else KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MemberRow(member: Member, medicalVisible: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClickLabel = stringResource(R.string.plan_edit), onClick = onClick)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(member.name, style = MaterialTheme.typography.titleMedium)
        val meta = listOf(member.relation, member.phone).filter { it.isNotBlank() }.joinToString(" · ")
        if (meta.isNotEmpty()) Text(meta, style = MaterialTheme.typography.bodyMedium)
        if (!member.medical.isEmpty) {
            Text(
                stringResource(if (medicalVisible) R.string.plan_medical_filled else R.string.plan_medical_locked),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MemberDialog(
    initial: Member,
    medicalVisible: Boolean,
    onUnlock: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (Member) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var relation by rememberSaveable { mutableStateOf(initial.relation) }
    var phone by rememberSaveable { mutableStateOf(initial.phone) }
    var notes by rememberSaveable { mutableStateOf(initial.notes) }
    var blood by rememberSaveable { mutableStateOf(initial.medical.bloodType) }
    var allergies by rememberSaveable { mutableStateOf(initial.medical.allergies) }
    var medication by rememberSaveable { mutableStateOf(initial.medical.medication) }
    var conditions by rememberSaveable { mutableStateOf(initial.medical.conditions) }
    var doctor by rememberSaveable { mutableStateOf(initial.medical.doctor) }
    var onPaper by rememberSaveable { mutableStateOf(initial.medicalOnPaper) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial.id == 0L) R.string.plan_add_member else R.string.plan_edit_member)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogField(R.string.plan_field_name, name) { name = it }
                DialogField(R.string.plan_field_relation, relation) { relation = it }
                DialogField(R.string.plan_field_phone, phone, phone = true) { phone = it }
                DialogField(R.string.plan_field_notes, notes) { notes = it }
                Text(stringResource(R.string.plan_medical), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                if (medicalVisible) {
                    Text(stringResource(R.string.plan_medical_hint), style = MaterialTheme.typography.bodySmall)
                    DialogField(R.string.plan_medical_blood, blood) { blood = it }
                    DialogField(R.string.plan_medical_allergies, allergies) { allergies = it }
                    DialogField(R.string.plan_medical_medication, medication) { medication = it }
                    DialogField(R.string.plan_medical_conditions, conditions) { conditions = it }
                    DialogField(R.string.plan_medical_doctor, doctor) { doctor = it }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .toggleable(value = onPaper, role = Role.Checkbox, onValueChange = { onPaper = it }),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = onPaper, onCheckedChange = null)
                        Text(stringResource(R.string.plan_medical_on_paper), modifier = Modifier.padding(start = 8.dp))
                    }
                    if (onPaper) {
                        Text(
                            stringResource(R.string.plan_medical_on_paper_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                } else {
                    OutlinedButton(onClick = onUnlock, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.plan_medical_show))
                    }
                }
                if (onDelete != null) {
                    TextButton(onClick = { confirmDelete = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.plan_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = {
                onSave(
                    initial.copy(
                        name = name, relation = relation, phone = phone, notes = notes,
                        // While locked the sheet was not shown: keep the stored one untouched.
                        medical = if (medicalVisible) MedicalInfo(blood, allergies, medication, conditions, doctor) else initial.medical,
                        medicalOnPaper = if (medicalVisible) onPaper else initial.medicalOnPaper,
                    ),
                )
            }) { Text(stringResource(R.string.plan_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.plan_cancel)) } },
    )
    if (confirmDelete && onDelete != null) {
        ConfirmDelete(name, onConfirm = { confirmDelete = false; onDelete() }, onDismiss = { confirmDelete = false })
    }
}

@Composable
private fun ContactDialog(
    initial: PlanContact,
    walletFull: Boolean,
    onDismiss: () -> Unit,
    onSave: (PlanContact) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var phone by rememberSaveable { mutableStateOf(initial.phone) }
    var wallet by rememberSaveable { mutableStateOf(initial.onWalletCard) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial.id == 0L) R.string.plan_add_contact else R.string.plan_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogField(R.string.plan_field_name, name) { name = it }
                DialogField(R.string.plan_field_phone, phone, phone = true) { phone = it }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .toggleable(value = wallet, enabled = wallet || !walletFull, role = Role.Checkbox, onValueChange = { wallet = it }),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = wallet, onCheckedChange = null, enabled = wallet || !walletFull)
                    Text(stringResource(R.string.plan_on_wallet_choice), modifier = Modifier.padding(start = 8.dp))
                }
                if (walletFull && !wallet) {
                    Text(stringResource(R.string.plan_wallet_full), style = MaterialTheme.typography.bodySmall)
                }
                if (onDelete != null) {
                    TextButton(onClick = { confirmDelete = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.plan_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank() && phone.isNotBlank(), onClick = {
                onSave(initial.copy(name = name, phone = phone, onWalletCard = wallet))
            }) { Text(stringResource(R.string.plan_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.plan_cancel)) } },
    )
    if (confirmDelete && onDelete != null) {
        ConfirmDelete(name, onConfirm = { confirmDelete = false; onDelete() }, onDismiss = { confirmDelete = false })
    }
}

@Composable
private fun ConfirmDelete(name: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.plan_delete_title)) },
        text = { Text(stringResource(R.string.plan_delete_text, name)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.plan_delete), color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.plan_cancel)) } },
    )
}

@Composable
private fun DialogField(label: Int, value: String, phone: Boolean = false, onChange: (String) -> Unit) =
    PlanField(label, value, phone, onChange)

@Composable
private fun LockCard(enabled: Boolean, available: Boolean, onSet: (Boolean) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .toggleable(value = enabled, enabled = available || enabled, role = Role.Switch, onValueChange = onSet),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null)
                Text(stringResource(R.string.plan_lock), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(checked = enabled, onCheckedChange = null, enabled = available || enabled)
            }
            Text(stringResource(R.string.plan_lock_hint), style = MaterialTheme.typography.bodyMedium)
            if (!available && !enabled) {
                Text(stringResource(R.string.plan_lock_unavailable), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
