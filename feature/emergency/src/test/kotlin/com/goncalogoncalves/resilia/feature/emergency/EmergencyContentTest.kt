package com.goncalogoncalves.resilia.feature.emergency

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.goncalogoncalves.resilia.core.designsystem.theme.EmergencyTheme
import com.goncalogoncalves.resilia.core.domain.model.Contact
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Phases
import com.goncalogoncalves.resilia.core.domain.model.Scenario
import com.goncalogoncalves.resilia.core.domain.model.Severity
import com.goncalogoncalves.resilia.core.domain.model.Step
import com.goncalogoncalves.resilia.core.domain.model.StepKind
import com.goncalogoncalves.resilia.core.domain.model.Urgency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en")
class EmergencyContentTest {

    @get:Rule val compose = createComposeRule()


    private fun step(id: String, text: String) =
        Step(id, LocalizedText.of("pt $text", text), kind = StepKind.PRACTICAL, urgency = Urgency.NOW)

    private val scenario = Scenario(
        id = "power-outage",
        title = LocalizedText.of("Falha de eletricidade", "Power outage"),
        summary = LocalizedText.of("s", "s"),
        icon = "power_off",
        typicalSeverity = Severity.HIGH,
        typicalDuration = LocalizedText.of("d", "d"),
        phases = Phases(
            during = listOf(
                step("power-outage.during.01", "Essential step"),
                step("power-outage.during.02", "Extra step"),
            ),
        ),
        emergencyNow = listOf("power-outage.during.01"),
    )

    private fun setContent(
        onCall112: () -> Unit = {},
        onExit: () -> Unit = {},
        torchAvailable: Boolean = true,
    ) = compose.setContent {
        EmergencyTheme {
            EmergencyContent(
                state = EmergencyUiState(
                    scenarios = listOf(scenario),
                    active = scenario,
                    contacts = listOf(
                        Contact(
                            id = "sns24",
                            name = LocalizedText.of("Linha SNS 24", "SNS 24 health line"),
                            number = "808 24 24 24",
                            purpose = LocalizedText.of("p", "Health advice"),
                        ),
                    ),
                ),
                torchAvailable = torchAvailable,
                torchOn = false,
                onToggleTorch = {},
                onCall112 = onCall112,
                onSelectScenario = {},
                onExit = onExit,
            )
        }
    }

    /** The screen is a LazyColumn: off-screen items only exist after scrolling the list. */
    private fun scrollTo(text: String, substring: Boolean = false) =
        compose.onNodeWithTag("emergency_root").performScrollToNode(hasText(text, substring = substring)).let {
            compose.onNodeWithText(text, substring = substring)
        }

    @Test
    fun showsScenarioAndOnlyEssentialStepsFirst() {
        setContent()
        compose.onNodeWithText("Power outage").assertIsDisplayed()
        compose.onNodeWithText("Essential step").assertIsDisplayed()
        compose.onNodeWithText("Extra step").assertDoesNotExist()

        scrollTo("Show all steps").performClick()
        compose.onNodeWithText("Extra step").assertExists()
    }

    @Test
    fun call112ButtonInvokesDialer() {
        var calls = 0
        setContent(onCall112 = { calls++ })
        // Pinned below the list: visible without scrolling, whatever the text size.
        compose.onNodeWithText("Call 112").assertIsDisplayed().performClick()
        assertEquals(1, calls)
    }

    @Test
    fun exitRequiresConfirmation() {
        var exited = false
        setContent(onExit = { exited = true })
        scrollTo("Leave emergency mode").performClick()
        assertTrue(!exited)
        compose.onNodeWithText("Leave").performClick()
        assertTrue(exited)
    }

    @Test
    fun torchDisabledWhenUnavailable() {
        setContent(torchAvailable = false)
        scrollTo("No torch on this phone").assertIsNotEnabled()
    }

    @Test
    fun disclaimerAlwaysVisible() {
        setContent()
        scrollTo("In an emergency, call 112", substring = true).assertIsDisplayed()
    }

    @Test
    fun resolveActiveFallsBackToFirstScenario() {
        assertEquals("power-outage", resolveActiveScenario(listOf(scenario), "unknown")?.id)
        assertEquals(null, resolveActiveScenario(emptyList(), null))
    }

    @Test
    fun contactsAreHiddenUntilRequested() {
        setContent()
        compose.onNodeWithText("Call 808 24 24 24").assertDoesNotExist()
        scrollTo("Useful contacts").performClick()
        scrollTo("Call 808 24 24 24").assertIsDisplayed()
        scrollTo("Hide contacts").performClick()
        compose.onNodeWithText("Call 808 24 24 24").assertDoesNotExist()
    }
}
