package com.goncalogoncalves.resilia.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.component.BigActionButton
import com.goncalogoncalves.resilia.core.domain.Disclaimer
import com.goncalogoncalves.resilia.core.domain.repository.AppStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DisclaimerViewModel @Inject constructor(
    private val appState: AppStateRepository,
) : ViewModel() {
    fun accept() = viewModelScope.launch { appState.acceptDisclaimer(Disclaimer.VERSION) }
}

/** First-run gate (decision D4): the app is only usable after reading and accepting this. */
@Composable
fun DisclaimerAcceptScreen(viewModel: DisclaimerViewModel = hiltViewModel()) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.disclaimer_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        LegalText()
        BigActionButton(
            text = stringResource(R.string.disclaimer_accept),
            onClick = viewModel::accept,
        )
    }
    }
}

@Composable
internal fun LegalText() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            R.string.legal_p1_not_112,
            R.string.legal_p2_general_info,
            R.string.legal_p3_no_liability,
            R.string.legal_p4_unreviewed,
            R.string.legal_p5_alerts,
            R.string.legal_p6_privacy,
        ).forEach {
            Text(stringResource(it), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
