package com.goncalogoncalves.resilia.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.goncalogoncalves.resilia.core.designsystem.component.SectionTitle

/** In-app privacy policy (Google Play requires it inside the app too). */
@Composable
internal fun PrivacyScreen() {
    val paragraphs = listOf(
        R.string.privacy_p1_summary,
        R.string.privacy_p2_on_device,
        R.string.privacy_p3_network,
        R.string.privacy_p4_other_apps,
        R.string.privacy_p5_export,
        R.string.privacy_p6_permissions,
        R.string.privacy_p7_delete,
        R.string.privacy_p8_contact,
    )
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle(stringResource(R.string.privacy_title)) }
        items(paragraphs) { Text(stringResource(it), style = MaterialTheme.typography.bodyLarge) }
    }
}
