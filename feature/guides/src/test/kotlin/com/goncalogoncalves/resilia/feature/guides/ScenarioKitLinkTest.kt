package com.goncalogoncalves.resilia.feature.guides

import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.goncalogoncalves.resilia.core.designsystem.theme.ResiliaTheme
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Phases
import com.goncalogoncalves.resilia.core.domain.model.Scenario
import com.goncalogoncalves.resilia.core.domain.model.Severity
import com.goncalogoncalves.resilia.core.domain.model.Step
import com.goncalogoncalves.resilia.core.domain.model.StepKind
import com.goncalogoncalves.resilia.core.domain.model.Urgency
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-w411dp-h2400dp")
class ScenarioKitLinkTest {

    @get:Rule val compose = createComposeRule()


    @Test
    fun `a step with kit items links to the kit`() {
        val t = { s: String -> LocalizedText.of(s, s) }
        val scenario = Scenario(
            id = "power-outage", title = t("Power outage"), summary = t("s"), icon = "power_off",
            typicalSeverity = Severity.HIGH, typicalDuration = t("d"),
            phases = Phases(during = listOf(Step("power-outage.during.01", t("Use a torch"), kind = StepKind.PRACTICAL, urgency = Urgency.NOW, relatedKitItemIds = listOf("torch", "batteries")))),
        )
        var kitOpened = 0
        compose.setContent {
            ResiliaTheme {
                Surface {
                    ScenarioContent(
                        state = ScenarioUiState(scenario, emptyMap(), false, kitItems = mapOf("torch" to t("Torch"), "batteries" to t("Batteries"))),
                        onToggleFavorite = {},
                        onActivateEmergency = {},
                        onOpenKit = { kitOpened++ },
                    )
                }
            }
        }
        compose.onNodeWithText("In the kit: Torch, Batteries").performClick()
        assertEquals(1, kitOpened)
    }
}
