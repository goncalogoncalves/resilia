package com.goncalogoncalves.resilia.feature.guides

import com.goncalogoncalves.resilia.core.domain.model.ContentLanguage
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Phases
import com.goncalogoncalves.resilia.core.domain.model.Scenario
import com.goncalogoncalves.resilia.core.domain.model.Severity
import com.goncalogoncalves.resilia.core.domain.model.Step
import com.goncalogoncalves.resilia.core.domain.model.StepKind
import com.goncalogoncalves.resilia.core.domain.model.Urgency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenarioSearchTest {
    private val s = Scenario(
        id = "power-outage",
        title = LocalizedText.of("Falha de eletricidade", "Power outage"),
        summary = LocalizedText.of("", ""),
        icon = "power_off",
        typicalSeverity = Severity.HIGH,
        typicalDuration = LocalizedText.of("", ""),
        keywords = mapOf("pt-PT" to listOf("apagão"), "en" to listOf("blackout")),
        phases = Phases(
            during = listOf(
                Step(
                    id = "power-outage.during.01",
                    text = LocalizedText.of("Nunca uses um gerador dentro de casa.", "Never use a generator indoors."),
                    kind = StepKind.SAFETY,
                    urgency = Urgency.NOW,
                ),
            ),
        ),
    )

    @Test fun `blank query matches`() = assertTrue(s.matches("  ", ContentLanguage.PT_PT))
    @Test fun `accent insensitive keyword`() = assertTrue(s.matches("APAGAO", ContentLanguage.PT_PT))
    @Test fun `title substring`() = assertTrue(s.matches("eletri", ContentLanguage.PT_PT))
    @Test fun `uses only current language`() = assertFalse(s.matches("blackout", ContentLanguage.PT_PT))
    @Test fun `english keyword`() = assertTrue(s.matches("blackout", ContentLanguage.EN))
    @Test fun `step text is searchable`() = assertTrue(s.matches("gerador", ContentLanguage.PT_PT))
    @Test fun `all words must match`() = assertFalse(s.matches("gerador inundação", ContentLanguage.PT_PT))
    @Test fun `words in any order`() = assertTrue(s.matches("casa gerador", ContentLanguage.PT_PT))

    @Test fun `favourites come first`() {
        val other = s.copy(id = "flood")
        assertEquals(listOf("flood", "power-outage"), listOf(s, other).sortedByFavorite(setOf("flood")).map { it.id })
    }
}
