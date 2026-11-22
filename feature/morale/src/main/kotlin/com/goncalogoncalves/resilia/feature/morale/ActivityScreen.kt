package com.goncalogoncalves.resilia.feature.morale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.DraftBadge
import com.goncalogoncalves.resilia.core.designsystem.component.SourcesLink
import com.goncalogoncalves.resilia.core.designsystem.component.currentContentLanguage
import com.goncalogoncalves.resilia.core.designsystem.component.text
import com.goncalogoncalves.resilia.core.domain.model.Activity
import com.goncalogoncalves.resilia.core.domain.model.ActivityOrigin
import com.goncalogoncalves.resilia.core.domain.model.ActivitySetting
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.LightNeed
import com.goncalogoncalves.resilia.core.domain.model.Noise
import com.goncalogoncalves.resilia.core.domain.model.ReviewStatus

@Composable
internal fun ActivityScreen(activityId: String, viewModel: MoraleViewModel = hiltViewModel()) {
    val bundle by viewModel.bundle.collectAsStateWithLifecycle()
    val b = bundle ?: return
    val activity = b.activity(activityId) ?: return
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        activityDetail(activity, b, showTitle = true)
        item { DisclaimerFooter() }
    }
}

/** Shared by the activity screen and the Emergency-mode sheet. */
internal fun androidx.compose.foundation.lazy.LazyListScope.activityDetail(
    a: Activity,
    b: ContentBundle,
    showTitle: Boolean,
) {
    if (showTitle) {
        item {
            Text(
                a.title.text(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() },
            )
        }
    }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(a.summary.text(), style = MaterialTheme.typography.bodyLarge)
            if (a.review.status == ReviewStatus.DRAFT) DraftBadge()
        }
    }
    item { Facts(a, b) }
    item {
        Text(
            stringResource(R.string.morale_how_to),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 4.dp).semantics { heading() },
        )
    }
    itemsIndexed(a.instructions) { i, step ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("${i + 1}.", style = MaterialTheme.typography.titleMedium, modifier = Modifier.widthIn(min = 28.dp))
            Text(step.text(), style = MaterialTheme.typography.bodyLarge)
        }
    }
    a.variants?.let { v ->
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    stringResource(R.string.morale_variants),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Text(v.text(), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
            Text(
                stringResource(
                    when (a.origin) {
                        ActivityOrigin.ORIGINAL -> R.string.morale_origin_original
                        ActivityOrigin.TRADITIONAL -> R.string.morale_origin_traditional
                        ActivityOrigin.PUBLIC_DOMAIN -> R.string.morale_origin_public_domain
                        ActivityOrigin.GUIDANCE -> R.string.morale_origin_guidance
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            a.originNote?.let { Text(it.text(), style = MaterialTheme.typography.bodyMedium) }
            val sources = a.sourceIds.mapNotNull(b::source)
            if (sources.isNotEmpty()) SourcesLink(sources)
        }
    }
}

@Composable
private fun Facts(a: Activity, b: ContentBundle) {
    val age = if (a.ageMax == null) {
        stringResource(R.string.morale_age_from, a.ageMin)
    } else {
        stringResource(R.string.morale_age_range, a.ageMin, a.ageMax ?: a.ageMin)
    }
    val people = when {
        a.peopleMax == null -> stringResource(R.string.morale_people_min, a.peopleMin)
        a.peopleMax == a.peopleMin -> stringResource(R.string.morale_people_exact, a.peopleMin)
        else -> stringResource(R.string.morale_people_range, a.peopleMin, a.peopleMax ?: a.peopleMin)
    }
    val language = currentContentLanguage()
    val materials = if (a.materials.isEmpty()) {
        stringResource(R.string.morale_materials_none)
    } else {
        stringResource(
            R.string.morale_materials,
            a.materials.mapNotNull(b::material).joinToString(", ") { it.name.resolve(language) },
        )
    }
    val lines = listOf(
        age,
        people,
        stringResource(R.string.morale_duration, a.durationMin),
        materials,
        stringResource(
            when (a.light) {
                LightNeed.NONE -> R.string.morale_light_none
                LightNeed.TORCH -> R.string.morale_light_torch
                LightNeed.LIGHT -> R.string.morale_light_light
            },
        ),
        stringResource(
            when (a.noise) {
                Noise.QUIET -> R.string.morale_noise_quiet
                Noise.MODERATE -> R.string.morale_noise_moderate
                Noise.LOUD -> R.string.morale_noise_loud
            },
        ),
        stringResource(
            when (a.setting) {
                ActivitySetting.INDOOR -> R.string.morale_setting_indoor
                ActivitySetting.OUTDOOR -> R.string.morale_setting_outdoor
                ActivitySetting.BOTH -> R.string.morale_setting_both
            },
        ),
    )
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}
