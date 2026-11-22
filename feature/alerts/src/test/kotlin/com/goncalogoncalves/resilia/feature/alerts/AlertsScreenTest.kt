package com.goncalogoncalves.resilia.feature.alerts

import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasScrollAction
import com.goncalogoncalves.resilia.core.designsystem.theme.ResiliaTheme
import com.goncalogoncalves.resilia.core.domain.model.AlertLevel
import com.goncalogoncalves.resilia.core.domain.model.AlertLevelInfo
import com.goncalogoncalves.resilia.core.domain.model.AlertRegion
import com.goncalogoncalves.resilia.core.domain.model.AlertSettings
import com.goncalogoncalves.resilia.core.domain.model.AlertSnapshot
import com.goncalogoncalves.resilia.core.domain.model.AlertZone
import com.goncalogoncalves.resilia.core.domain.model.AlertsContent
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.ContentManifest
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Warning
import com.goncalogoncalves.resilia.core.domain.model.WarningArea
import com.goncalogoncalves.resilia.core.domain.model.WarningTypeInfo
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en")
class AlertsScreenTest {

    @get:Rule val compose = createComposeRule()


    private fun t(s: String) = LocalizedText.of(s, s)
    private val now = Instant.parse("2026-11-21T19:00:00Z")

    private val bundle = ContentBundle(
        manifest = ContentManifest("0", 1, emptyList(), "s", "c", "k", "p", "b"),
        sources = emptyList(),
        scenarios = emptyList(),
        alerts = AlertsContent(
            sourceIds = emptyList(),
            areas = listOf(WarningArea("PTO", t("Porto"), AlertRegion.CONTINENT), WarningArea("MCN", t("Madeira – North Coast"), AlertRegion.MADEIRA)),
            municipalities = emptyList(),
            warningTypes = listOf(WarningTypeInfo("Vento", t("Wind"))),
            levels = listOf(AlertLevelInfo(AlertLevel.ORANGE, t("Orange"), t("Moderate to high risk."))),
            fireRiskLevels = emptyList(),
        ),
    )

    private fun state(zone: AlertZone?, snapshot: AlertSnapshot) =
        AlertsUiState(bundle, AlertSettings(zone = zone), snapshot, now, refreshing = false)

    @Test
    fun `active warning is shown with its level written out`() {
        val warning = Warning("PTO", "Vento", AlertLevel.ORANGE, now.minusSeconds(3600), now.plusSeconds(3600), "Strong gusts.")
        compose.setContent {
            ResiliaTheme { Surface { AlertsContent(state(AlertZone("PTO"), AlertSnapshot(listOf(warning), fetchedAt = now)), {}, {}, {}) } }
        }
        compose.onNodeWithText("Orange warning").assertIsDisplayed()
        compose.onNodeWithText("Wind").assertIsDisplayed()
        compose.onNodeWithText("IPMA text, in Portuguese.").assertIsDisplayed()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("No warning in the app", substring = true))
        compose.onNodeWithText("No warning in the app", substring = true).assertIsDisplayed()
    }

    @Test
    fun `without a zone it asks for one`() {
        var opened = false
        compose.setContent {
            ResiliaTheme { Surface { AlertsContent(state(null, AlertSnapshot()), {}, {}, onOpenSettings = { opened = true }) } }
        }
        compose.onNodeWithText("Choose your area").performClick()
        assertTrue(opened)
    }

    @Test
    fun `failed download says so and islands have no fire risk`() {
        compose.setContent {
            ResiliaTheme { Surface { AlertsContent(state(AlertZone("MCN"), AlertSnapshot(lastAttemptFailed = true)), {}, {}, {}) } }
        }
        compose.onNodeWithText("Could not get the warnings").assertIsDisplayed()
        compose.onNodeWithText("No reading yet", substring = true).assertIsDisplayed()
        compose.onNodeWithText("mainland only", substring = true).assertIsDisplayed()
    }
}
