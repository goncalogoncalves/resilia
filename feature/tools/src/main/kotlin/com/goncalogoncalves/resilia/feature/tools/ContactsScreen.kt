package com.goncalogoncalves.resilia.feature.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.component.ContactCard
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(content: ContentRepository) : ViewModel() {
    val bundle = flow<ContentBundle?> { emit(content.load()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/** National useful contacts. Every number comes from /content/contacts with a cited source. */
@Composable
internal fun ContactsScreen(viewModel: ContactsViewModel = hiltViewModel()) {
    val bundle by viewModel.bundle.collectAsStateWithLifecycle()
    val b = bundle ?: return
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(stringResource(R.string.tools_contacts_intro), style = MaterialTheme.typography.bodyLarge) }
        items(b.contacts, key = { it.id }) { c ->
            ContactCard(contact = c, sources = c.sourceIds.mapNotNull(b::source))
        }
        item { Text(stringResource(R.string.tools_contacts_madeira_note), style = MaterialTheme.typography.bodyMedium) }
        item { DisclaimerFooter() }
    }
}
