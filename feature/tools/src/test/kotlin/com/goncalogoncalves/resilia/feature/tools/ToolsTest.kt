package com.goncalogoncalves.resilia.feature.tools

import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.goncalogoncalves.resilia.core.designsystem.component.StepCard
import com.goncalogoncalves.resilia.core.designsystem.theme.ResiliaTheme
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Source
import com.goncalogoncalves.resilia.core.domain.model.SourceEntity
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
class ToolsTest {

    @get:Rule val compose = createComposeRule()


    @Test
    fun `sos pattern is three short three long three short`() {
        val flashes = SosPattern.sequence.filter { it.first }.map { it.second }
        val u = SosPattern.UNIT_MS
        assertEquals(listOf(u, u, u, 3 * u, 3 * u, 3 * u, u, u, u), flashes)
    }

    @Test
    fun `sos never flashes 3 or more times per second`() {
        // WCAG 2.2 SC 2.3.1 limit used as the design rule (marco-5-fontes.md §2).
        assertTrue(SosPattern.maxFlashesPerSecond() < 3)
    }

    private val step = Step(
        id = "battery.07", text = LocalizedText.of("pt", "Lower the screen brightness."),
        kind = StepKind.PRACTICAL, urgency = Urgency.NOW, sourceIds = listOf("g"),
    )

    private fun show(source: Source) = compose.setContent {
        ResiliaTheme { Surface { StepCard(7, step, listOf(source)) } }
    }

    @Test
    fun `step citing only a non-official source says so`() {
        show(Source("g", SourceEntity.OTHER, LocalizedText.of("t", "t"), "https://example.org", "2026-11-21", publisher = "Google (Ajuda do Android)"))
        compose.onNodeWithText("Not an official source").assertIsDisplayed()
        compose.onNodeWithText("Source: Google (Ajuda do Android)").assertIsDisplayed()
    }

    @Test
    fun `step citing an official source has no warning`() {
        show(Source("a", SourceEntity.ANEPC, LocalizedText.of("t", "t"), "https://example.org", "2026-11-21"))
        assertEquals(0, compose.onAllNodes(androidx.compose.ui.test.hasText("Not an official source")).fetchSemanticsNodes().size)
    }
}
