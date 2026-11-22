package com.goncalogoncalves.resilia.core.designsystem.component

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.goncalogoncalves.resilia.core.designsystem.R
import com.goncalogoncalves.resilia.core.domain.model.Contact
import com.goncalogoncalves.resilia.core.domain.model.Region
import com.goncalogoncalves.resilia.core.domain.model.Source
import com.goncalogoncalves.resilia.core.domain.model.SourceEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Opens the dialer pre-filled (ACTION_DIAL: no CALL_PHONE permission; the user confirms the call). */
fun Context.openDialer(number: String) {
    try {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
    } catch (_: ActivityNotFoundException) {
        // No dialer (some tablets): the number stays visible on screen.
    }
}

/** Opens an https page in the user's browser. Only used for source links. */
fun Context.openWebPage(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: ActivityNotFoundException) {
        // No browser installed: nothing else to do.
    }
}

/** "2026-11-20" -> "20/11/2026" (pt) or "20 Nov 2026" (en-GB). Falls back to the raw string. */
fun formatIsoDate(iso: String, locale: Locale): String = try {
    LocalDate.parse(iso).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
} catch (_: Exception) {
    iso
}

@Composable
private fun currentLocale(): Locale {
    val locales = LocalConfiguration.current.locales
    return if (locales.isEmpty) Locale.forLanguageTag("pt-PT") else locales[0]
}

/** Who published a source, e.g. "DGS", "Governo de Portugal", "E-REDES". */
@Composable
fun Source.publisherLabel(): String = publisher ?: when (entity) {
    SourceEntity.GOV -> stringResource(R.string.ds_entity_gov)
    SourceEntity.EU -> stringResource(R.string.ds_entity_eu)
    SourceEntity.CVP -> stringResource(R.string.ds_entity_cvp)
    SourceEntity.SNS24 -> "SNS 24"
    SourceEntity.UTILITY, SourceEntity.OTHER -> stringResource(R.string.ds_entity_other)
    else -> entity.name
}

/** One source: publisher, title, dates and a link to the original page. */
@Composable
fun SourceItem(source: Source, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val locale = currentLocale()
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(source.publisherLabel(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(source.title.text(), style = MaterialTheme.typography.bodyMedium)
        val dates = buildList {
            source.publishedAt?.let { add(stringResource(R.string.ds_source_published, formatIsoDate(it, locale))) }
            add(stringResource(R.string.ds_source_accessed, formatIsoDate(source.accessedAt, locale)))
        }.joinToString(" · ")
        Text(dates, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = { context.openWebPage(source.url) }, modifier = Modifier.heightIn(min = 48.dp)) {
            Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
            Text(
                stringResource(R.string.ds_source_open),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/** Dialog listing the sources of one step. */
@Composable
fun SourcesDialog(sources: List<Source>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ds_sources_title)) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                sources.forEach { SourceItem(it) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ds_close)) } },
    )
}

@Composable
fun Region.label(): String = stringResource(
    when (this) {
        Region.ALL -> R.string.ds_region_all
        Region.MAINLAND -> R.string.ds_region_mainland
        Region.AZORES -> R.string.ds_region_azores
        Region.MADEIRA -> R.string.ds_region_madeira
    },
)

/** A phone contact with a large call button (opens the dialer, never calls directly). */
@Composable
fun ContactCard(
    contact: Contact,
    sources: List<Source>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                contact.name.text(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(contact.purpose.text(), style = MaterialTheme.typography.bodyMedium)
            val meta = listOfNotNull(
                contact.region.label(),
                contact.hours?.text(),
                contact.cost?.text(),
            ).joinToString(" · ")
            Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BigActionButton(
                text = stringResource(R.string.ds_call_number, contact.number),
                icon = Icons.Outlined.Call,
                onClick = { context.openDialer(contact.dial) },
            )
            SourcesLink(sources)
        }
    }
}

/**
 * "Source: DGS, ..." link that opens the sources dialog. Plain clickable text rather than a
 * TextButton: with several publishers it wraps, and a pill-shaped button clips the first letters.
 */
@Composable
fun SourcesLink(
    sources: List<Source>,
    modifier: Modifier = Modifier,
    label: String = stringResource(
        R.string.ds_sources_inline,
        sources.map { it.publisherLabel() }.distinct().joinToString(", "),
    ),
) {
    if (sources.isEmpty()) return
    var show by remember { mutableStateOf(false) }
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button) { show = true }
            .padding(vertical = 12.dp),
    )
    if (show) SourcesDialog(sources) { show = false }
}
