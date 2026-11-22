package com.goncalogoncalves.resilia.core.content

import com.goncalogoncalves.resilia.core.domain.model.Activity
import com.goncalogoncalves.resilia.core.domain.model.ActivityFilter
import com.goncalogoncalves.resilia.core.domain.model.ActivityOrigin
import com.goncalogoncalves.resilia.core.domain.model.ActivitySetting
import com.goncalogoncalves.resilia.core.domain.model.ActivityType
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.ContentLanguage
import com.goncalogoncalves.resilia.core.domain.model.ContentManifest
import com.goncalogoncalves.resilia.core.domain.model.LightNeed
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Material
import com.goncalogoncalves.resilia.core.domain.model.Noise
import com.goncalogoncalves.resilia.core.domain.model.Source
import com.goncalogoncalves.resilia.core.domain.model.SourceEntity
import com.goncalogoncalves.resilia.core.domain.model.Step
import com.goncalogoncalves.resilia.core.domain.model.StepKind
import com.goncalogoncalves.resilia.core.domain.model.SupportContent
import com.goncalogoncalves.resilia.core.domain.model.ToolGuide
import com.goncalogoncalves.resilia.core.domain.model.Urgency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MoraleRulesTest {

    private val t = LocalizedText.of("pt", "en")
    private val official = Source("dgs", SourceEntity.DGS, t, "https://example.org", "2026-11-21")
    private val other = Source("x", SourceEntity.OTHER, t, "https://example.org", "2026-11-21")

    private fun activity(
        origin: ActivityOrigin = ActivityOrigin.ORIGINAL,
        sourceIds: List<String> = emptyList(),
        originNote: LocalizedText? = null,
        materials: List<String> = emptyList(),
        noise: Noise = Noise.QUIET,
        light: LightNeed = LightNeed.NONE,
        ageMin: Int = 4,
        peopleMin: Int = 1,
        durationMin: Int = 5,
    ) = Activity(
        id = "a", type = ActivityType.GAME, title = t, summary = t, ageMin = ageMin, peopleMin = peopleMin,
        materials = materials, durationMin = durationMin, setting = ActivitySetting.BOTH, noise = noise, light = light,
        instructions = listOf(t), origin = origin, originNote = originNote, sourceIds = sourceIds,
    )

    private fun bundle(activities: List<Activity> = emptyList(), support: SupportContent? = null) = ContentBundle(
        manifest = ContentManifest("0", 1, emptyList(), "s", "c", "k", "p", "b"),
        sources = listOf(official, other),
        scenarios = emptyList(),
        activities = activities,
        materials = listOf(Material("paper", t)),
        support = support,
    )

    @Test
    fun `original activity needs no source`() {
        assertEquals(emptyList<String>(), ContentRules.check(bundle(listOf(activity()))))
    }

    @Test
    fun `public domain activity needs an origin note`() {
        val errors = ContentRules.check(bundle(listOf(activity(origin = ActivityOrigin.PUBLIC_DOMAIN))))
        assertTrue(errors.toString(), errors.any { "originNote" in it })
    }

    @Test
    fun `guidance activity needs an official source`() {
        val errors = ContentRules.check(bundle(listOf(activity(origin = ActivityOrigin.GUIDANCE, sourceIds = listOf("x")))))
        assertTrue(errors.toString(), errors.any { "official" in it })
        assertEquals(
            emptyList<String>(),
            ContentRules.check(bundle(listOf(activity(origin = ActivityOrigin.GUIDANCE, sourceIds = listOf("dgs"))))),
        )
    }

    @Test
    fun `unknown material is an error`() {
        val errors = ContentRules.check(bundle(listOf(activity(materials = listOf("rope")))))
        assertTrue(errors.toString(), errors.any { "unknown material 'rope'" in it })
    }

    @Test
    fun `support steps must be health advice with an official source`() {
        fun support(kind: StepKind, src: String) = SupportContent(
            note = t,
            guides = listOf(ToolGuide("g", t, t, listOf(Step("g.01", t, kind = kind, urgency = Urgency.SOON, sourceIds = listOf(src))))),
        )
        assertTrue(ContentRules.check(bundle(support = support(StepKind.HEALTH, "x"))).isNotEmpty())
        assertTrue(ContentRules.check(bundle(support = support(StepKind.WELLBEING, "dgs"))).isNotEmpty())
        assertEquals(emptyList<String>(), ContentRules.check(bundle(support = support(StepKind.HEALTH, "dgs"))))
    }

    @Test
    fun `filters combine`() {
        val loudWithLight = activity(noise = Noise.LOUD, light = LightNeed.LIGHT, materials = listOf("paper"), peopleMin = 2, durationMin = 20, ageMin = 8)
        val calm = activity()
        assertTrue(ActivityFilter(dark = true, quiet = true, noMaterials = true, alone = true, short = true, age = 4).matches(calm))
        assertFalse(ActivityFilter(dark = true).matches(loudWithLight))
        assertFalse(ActivityFilter(quiet = true).matches(loudWithLight))
        assertFalse(ActivityFilter(noMaterials = true).matches(loudWithLight))
        assertFalse(ActivityFilter(alone = true).matches(loudWithLight))
        assertFalse(ActivityFilter(short = true).matches(loudWithLight))
        assertFalse(ActivityFilter(age = 4).matches(loudWithLight))
        assertTrue(ActivityFilter().matches(loudWithLight))
    }

    @Test
    fun `bundled calma content follows the approved proposal`() {
        val root = File(requireNotNull(System.getProperty("resilia.contentDir")))
        val b = ContentLoader { File(root, it).readText() }.load()
        assertEquals(24, b.activities.size)
        ActivityType.entries.forEach { type -> assertTrue(type.name, b.activities.any { it.type == type }) }
        assertTrue("emergency mode needs dark and quiet activities", b.activities.count { it.goodForDarkAndQuiet } >= 5)
        // Proposal A: no song lyrics, no public-domain texts.
        assertTrue(b.activities.none { it.origin == ActivityOrigin.PUBLIC_DOMAIN })
        val support = requireNotNull(b.support)
        assertEquals(
            listOf("support-you", "support-children", "support-older", "support-flashbacks", "support-warning"),
            support.guides.map { it.id },
        )
        assertTrue(support.note.resolve(ContentLanguage.PT_PT).contains("tempestades e inundações"))
        assertEquals(listOf("sns24", "suicide-1411", "emergency-112"), support.contactIds)
        assertEquals("1411", b.contact("suicide-1411")?.number)
    }
}
