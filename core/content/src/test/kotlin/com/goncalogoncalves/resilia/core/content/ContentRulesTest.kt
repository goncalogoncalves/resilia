package com.goncalogoncalves.resilia.core.content

import com.goncalogoncalves.resilia.core.domain.model.Contact
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.ContentManifest
import com.goncalogoncalves.resilia.core.domain.model.FamilyPlanGuide
import com.goncalogoncalves.resilia.core.domain.model.KitCatalog
import com.goncalogoncalves.resilia.core.domain.model.PlanSection
import com.goncalogoncalves.resilia.core.domain.model.KitItem
import com.goncalogoncalves.resilia.core.domain.model.KitList
import com.goncalogoncalves.resilia.core.domain.model.KitQuantity
import com.goncalogoncalves.resilia.core.domain.model.KitQuantityKind
import com.goncalogoncalves.resilia.core.domain.model.KitUnit
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Phases
import com.goncalogoncalves.resilia.core.domain.model.Review
import com.goncalogoncalves.resilia.core.domain.model.ReviewStatus
import com.goncalogoncalves.resilia.core.domain.model.Scenario
import com.goncalogoncalves.resilia.core.domain.model.Severity
import com.goncalogoncalves.resilia.core.domain.model.Source
import com.goncalogoncalves.resilia.core.domain.model.SourceEntity
import com.goncalogoncalves.resilia.core.domain.model.Step
import com.goncalogoncalves.resilia.core.domain.model.StepKind
import com.goncalogoncalves.resilia.core.domain.model.Urgency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentRulesTest {

    private val t = LocalizedText.of("pt", "en")

    private fun step(
        id: String = "s1.during.01",
        kind: StepKind = StepKind.PRACTICAL,
        sourceIds: List<String> = emptyList(),
        text: LocalizedText = t,
        review: Review = Review(),
    ) = Step(id = id, text = text, kind = kind, urgency = Urgency.NOW, sourceIds = sourceIds, review = review)

    private fun bundle(
        steps: List<Step> = listOf(step()),
        sources: List<Source> = emptyList(),
        emergencyNow: List<String> = emptyList(),
        related: List<String> = emptyList(),
        contacts: List<Contact> = emptyList(),
        kit: KitCatalog? = null,
        plan: FamilyPlanGuide? = null,
    ) = ContentBundle(
        kit = kit,
        plan = plan,
        manifest = ContentManifest("0.1.0", 1, emptyList(), "sources.json", "contacts.json", "kit.json", "plan.json", "battery.json"),
        sources = sources,
        contacts = contacts,
        scenarios = listOf(
            Scenario(
                id = "s1", title = t, summary = t, icon = "x", typicalSeverity = Severity.LOW,
                typicalDuration = t, keywords = mapOf("pt-PT" to listOf("a"), "en" to listOf("a")),
                phases = Phases(during = steps), emergencyNow = emergencyNow,
                relatedScenarioIds = related,
            ),
        ),
    )

    private fun source(id: String, entity: SourceEntity) =
        Source(id, entity, t, "https://example.org", "2026-11-20")

    @Test
    fun `valid bundle has no errors`() {
        assertEquals(emptyList<String>(), ContentRules.check(bundle()))
    }

    @Test
    fun `missing english text is an error`() {
        val errors = ContentRules.check(bundle(listOf(step(text = LocalizedText(mapOf("pt-PT" to "x"))))))
        assertTrue(errors.joinToString(), errors.any { "'en'" in it })
    }

    @Test
    fun `safety step without source is an error`() {
        val errors = ContentRules.check(bundle(listOf(step(kind = StepKind.SAFETY))))
        assertTrue(errors.any { "requires at least one official source" in it })
    }

    @Test
    fun `health step with only non-official source is an error`() {
        val errors = ContentRules.check(
            bundle(
                listOf(step(kind = StepKind.HEALTH, sourceIds = listOf("blog"))),
                sources = listOf(source("blog", SourceEntity.OTHER)),
            ),
        )
        assertTrue(errors.any { "requires at least one official source" in it })
    }

    @Test
    fun `safety step with official source passes`() {
        val errors = ContentRules.check(
            bundle(
                listOf(step(kind = StepKind.SAFETY, sourceIds = listOf("anepc-x"))),
                sources = listOf(source("anepc-x", SourceEntity.ANEPC)),
            ),
        )
        assertEquals(emptyList<String>(), errors)
    }

    @Test
    fun `broken references are errors`() {
        val errors = ContentRules.check(
            bundle(
                listOf(step(sourceIds = listOf("nope"))),
                emergencyNow = listOf("s1.during.99"),
                related = listOf("ghost"),
            ),
        )
        assertTrue(errors.any { "unknown sourceId 'nope'" in it })
        assertTrue(errors.any { "unknown step 's1.during.99'" in it })
        assertTrue(errors.any { "unknown relatedScenarioId 'ghost'" in it })
    }

    @Test
    fun `step id must be prefixed by scenario id`() {
        val errors = ContentRules.check(bundle(listOf(step(id = "other.during.01"))))
        assertTrue(errors.any { "must start with 's1.'" in it })
    }

    @Test
    fun `reviewed without metadata is an error`() {
        val errors = ContentRules.check(bundle(listOf(step(review = Review(ReviewStatus.REVIEWED)))))
        assertTrue(errors.any { "reviewedAt" in it })
        assertTrue(errors.any { "reviewedBy" in it })
    }

    @Test
    fun `duplicate step ids are errors`() {
        val errors = ContentRules.check(bundle(listOf(step(), step())))
        assertTrue(errors.any { "duplicate step id" in it })
    }

    private fun contact(number: String = "112", sourceIds: List<String> = listOf("src")) =
        Contact(id = "c1", name = t, number = number, purpose = t, sourceIds = sourceIds)

    @Test
    fun `contact without source is an error`() {
        val errors = ContentRules.check(bundle(contacts = listOf(contact(sourceIds = emptyList()))))
        assertTrue(errors.any { "at least one source" in it })
    }

    @Test
    fun `contact with valid source and number passes`() {
        val errors = ContentRules.check(
            bundle(sources = listOf(source("src", SourceEntity.INEM)), contacts = listOf(contact("808 24 24 24"))),
        )
        assertEquals(emptyList<String>(), errors)
    }

    @Test
    fun `contact number with letters is an error`() {
        val errors = ContentRules.check(
            bundle(sources = listOf(source("src", SourceEntity.INEM)), contacts = listOf(contact("80O 1"))),
        )
        assertTrue(errors.any { "must be digits" in it })
    }

    @Test
    fun `utility source does not count as official for safety steps`() {
        val errors = ContentRules.check(
            bundle(
                listOf(step(kind = StepKind.SAFETY, sourceIds = listOf("u"))),
                sources = listOf(source("u", SourceEntity.UTILITY)),
            ),
        )
        assertTrue(errors.any { "requires at least one official source" in it })
    }

    @Test
    fun `dial strips spaces`() {
        assertEquals("808242424", contact("808 24 24 24").dial)
    }

    private val dgs = source("dgs", SourceEntity.DGS)

    private fun kit(
        items: List<KitItem> = listOf(kitItem()),
        dayOptions: List<Int> = listOf(3, 7),
    ) = KitCatalog(3, listOf("dgs"), dayOptions, 6, listOf("dgs"), items)

    private fun kitItem(
        id: String = "home.water",
        list: KitList = KitList.HOME,
        quantity: KitQuantity? = KitQuantity(KitQuantityKind.PER_PERSON_PER_DAY, 2.0, KitUnit.LITRE),
        sourceIds: List<String> = listOf("dgs"),
    ) = KitItem(id = id, list = list, name = t, quantity = quantity, sourceIds = sourceIds)

    @Test
    fun `valid kit passes`() {
        assertEquals(emptyList<String>(), ContentRules.check(bundle(sources = listOf(dgs), kit = kit())))
    }

    @Test
    fun `kit item without official source is an error`() {
        val errors = ContentRules.check(
            bundle(sources = listOf(dgs, source("blog", SourceEntity.OTHER)), kit = kit(listOf(kitItem(sourceIds = listOf("blog"))))),
        )
        assertTrue(errors.joinToString(), errors.any { "kit item 'home.water'" in it && "official source" in it })
    }

    @Test
    fun `per person quantity without amount is an error`() {
        val errors = ContentRules.check(
            bundle(sources = listOf(dgs), kit = kit(listOf(kitItem(quantity = KitQuantity(KitQuantityKind.PER_PERSON_PER_DAY))))),
        )
        assertTrue(errors.any { "positive amount" in it })
    }

    @Test
    fun `day options below the official minimum are errors`() {
        val errors = ContentRules.check(bundle(sources = listOf(dgs), kit = kit(dayOptions = listOf(2, 3))))
        assertTrue(errors.any { "below the official minimum" in it })
    }

    @Test
    fun `kit item id must match its list`() {
        val errors = ContentRules.check(bundle(sources = listOf(dgs), kit = kit(listOf(kitItem(id = "bag.water")))))
        assertTrue(errors.any { "must start with 'home.'" in it })
    }

    @Test
    fun `plan hint without official source is an error`() {
        val plan = FamilyPlanGuide(listOf(PlanSection("meeting", hint = t, sourceIds = listOf("m"))))
        val errors = ContentRules.check(bundle(sources = listOf(source("m", SourceEntity.OTHER)), plan = plan))
        assertTrue(errors.joinToString(), errors.any { "plan section 'meeting'" in it && "official source" in it })
    }

    @Test
    fun `municipal civil protection counts as official`() {
        val plan = FamilyPlanGuide(listOf(PlanSection("meeting", hint = t, sourceIds = listOf("m"))))
        assertEquals(emptyList<String>(), ContentRules.check(bundle(sources = listOf(source("m", SourceEntity.MUNICIPAL)), plan = plan)))
    }
}
