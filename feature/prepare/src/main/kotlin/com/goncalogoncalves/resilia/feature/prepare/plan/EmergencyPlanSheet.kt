package com.goncalogoncalves.resilia.feature.prepare.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.domain.model.FamilyPlan
import com.goncalogoncalves.resilia.core.domain.model.PlanContact
import com.goncalogoncalves.resilia.core.domain.repository.FamilyPlanRepository
import com.goncalogoncalves.resilia.feature.prepare.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EmergencyPlanViewModel @Inject constructor(repo: FamilyPlanRepository) : ViewModel() {
    val state = combine(repo.plan, repo.contacts) { p, c -> p to c }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/**
 * Emergency mode: the essentials of the family plan on screen, in big type.
 * No medical data here (it is never shown without the user opening the plan).
 * Shown by the app over the emergency screen; uses the caller's (black) theme.
 */
@Composable
fun EmergencyPlanSheet(onClose: () -> Unit, viewModel: EmergencyPlanViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val (plan, contacts) = state ?: return
    EmergencyPlanContent(plan, contacts, onClose)
}

@Composable
internal fun EmergencyPlanContent(plan: FamilyPlan, contacts: List<PlanContact>, onClose: () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                TextButton(onClick = onClose, modifier = Modifier.heightIn(min = 56.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    Text(stringResource(R.string.plan_emergency_back), style = MaterialTheme.typography.titleMedium)
                }
            }
            item {
                Text(stringResource(R.string.plan_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
            }
            if (plan.isEmpty && contacts.isEmpty()) {
                item { Text(stringResource(R.string.plan_emergency_empty), style = MaterialTheme.typography.bodyLarge) }
            }
            item {
                Block(
                    R.string.plan_meeting,
                    listOf(
                        stringResource(R.string.plan_meeting_near) to plan.meetingNear,
                        stringResource(R.string.plan_meeting_far) to plan.meetingFar,
                    ),
                )
            }
            item {
                Block(
                    R.string.plan_contact_person,
                    listOf(
                        stringResource(R.string.plan_field_name) to plan.contactName,
                        stringResource(R.string.plan_field_phone) to plan.contactPhone,
                        stringResource(R.string.plan_field_address) to plan.contactAddress,
                    ),
                )
            }
            item {
                Block(
                    R.string.plan_shutoff,
                    listOf(
                        stringResource(R.string.plan_shutoff_power) to plan.shutoffPower,
                        stringResource(R.string.plan_shutoff_water) to plan.shutoffWater,
                        stringResource(R.string.plan_shutoff_gas) to plan.shutoffGas,
                    ),
                )
            }
            if (contacts.isNotEmpty()) {
                item { Block(R.string.plan_contacts, contacts.map { it.name to it.phone }) }
            }
            item { DisclaimerFooter() }
        }
    }
}

@Composable
private fun Block(title: Int, lines: List<Pair<String, String>>) {
    val filled = lines.filter { it.second.isNotBlank() }
    if (filled.isEmpty()) return
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        filled.forEach { (label, value) ->
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
