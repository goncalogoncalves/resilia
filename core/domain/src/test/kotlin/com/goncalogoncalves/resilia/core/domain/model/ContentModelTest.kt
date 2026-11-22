package com.goncalogoncalves.resilia.core.domain.model

import com.goncalogoncalves.resilia.core.domain.Disclaimer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentModelTest {

    private fun step(id: String, status: ReviewStatus = ReviewStatus.DRAFT) = Step(
        id = id,
        text = LocalizedText.of("pt $id", "en $id"),
        kind = StepKind.PRACTICAL,
        urgency = Urgency.NOW,
        review = Review(status),
    )

    private fun scenario(
        status: ReviewStatus,
        steps: List<Step>,
        emergencyNow: List<String> = emptyList(),
    ) = Scenario(
        id = "s",
        title = LocalizedText.of("t", "t"),
        summary = LocalizedText.of("s", "s"),
        icon = "x",
        typicalSeverity = Severity.HIGH,
        typicalDuration = LocalizedText.of("d", "d"),
        phases = Phases(during = steps),
        emergencyNow = emergencyNow,
        review = Review(status),
    )

    @Test
    fun `resolve returns requested language`() {
        val t = LocalizedText.of("Olá", "Hello")
        assertEquals("Olá", t.resolve(ContentLanguage.PT_PT))
        assertEquals("Hello", t.resolve(ContentLanguage.EN))
    }

    @Test
    fun `resolve falls back to pt-PT when language missing`() {
        val t = LocalizedText(mapOf("pt-PT" to "Olá", "en" to " "))
        assertEquals("Olá", t.resolve(ContentLanguage.EN))
        assertEquals(listOf(ContentLanguage.EN), t.missingLanguages())
    }

    @Test
    fun `system language maps to content language`() {
        assertEquals(ContentLanguage.PT_PT, ContentLanguage.fromSystemLanguage("pt"))
        assertEquals(ContentLanguage.EN, ContentLanguage.fromSystemLanguage("en"))
        assertEquals(ContentLanguage.EN, ContentLanguage.fromSystemLanguage("fr"))
    }

    @Test
    fun `scenario is draft if any step is draft`() {
        val s = scenario(
            ReviewStatus.REVIEWED,
            listOf(step("a", ReviewStatus.REVIEWED), step("b")),
        )
        assertEquals(ReviewStatus.DRAFT, s.effectiveReviewStatus)
    }

    @Test
    fun `scenario is reviewed only if it and all steps are reviewed`() {
        val s = scenario(ReviewStatus.REVIEWED, listOf(step("a", ReviewStatus.REVIEWED)))
        assertEquals(ReviewStatus.REVIEWED, s.effectiveReviewStatus)
    }

    @Test
    fun `emergency steps keep declared order and skip unknown ids`() {
        val s = scenario(
            ReviewStatus.DRAFT,
            listOf(step("a"), step("b"), step("c")),
            emergencyNow = listOf("c", "missing", "a"),
        )
        assertEquals(listOf("c", "a"), s.emergencySteps().map { it.id })
    }

    @Test
    fun `disclaimer must be shown until current version accepted`() {
        assertTrue(Disclaimer.mustShow(0))
        assertFalse(Disclaimer.mustShow(Disclaimer.VERSION))
    }
}
