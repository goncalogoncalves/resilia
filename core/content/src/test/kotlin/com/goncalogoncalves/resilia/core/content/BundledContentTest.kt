package com.goncalogoncalves.resilia.core.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Loads the real /content folder and checks every semantic rule. */
class BundledContentTest {

    private val root = File(
        requireNotNull(System.getProperty("resilia.contentDir")) { "resilia.contentDir not set" },
    )

    @Test
    fun `bundled content parses and passes all rules`() {
        val bundle = ContentLoader { File(root, it).readText() }.load()
        assertTrue("no scenarios bundled", bundle.scenarios.isNotEmpty())
        assertEquals(emptyList<String>(), ContentRules.check(bundle))
    }

    @Test
    fun `bundled kit uses the approved official figures`() {
        val kit = requireNotNull(ContentLoader { File(root, it).readText() }.load().kit)
        assertEquals(3, kit.minimumDays)
        assertEquals(6, kit.reviewIntervalMonths)
        val water = kit.items.single { it.id == "home.water" }
        assertEquals(2.0, water.quantity?.amount ?: 0.0, 0.0)
        assertTrue("water must cite DGS", "dgs-quebras-energeticas" in water.sourceIds)
        // Only water has a number: no official weight or calorie figure exists for anything else.
        assertEquals(listOf("home.water"), kit.items.filter { it.quantity?.amount != null }.map { it.id })
    }

    @Test
    fun `bundled plan guide defines every section the app uses`() {
        val plan = requireNotNull(ContentLoader { File(root, it).readText() }.load().plan)
        assertEquals(
            com.goncalogoncalves.resilia.core.domain.model.PlanSectionId.ALL.sorted(),
            plan.sections.map { it.id }.sorted(),
        )
    }

    @Test
    fun `battery guide safety steps cite an official source and phone settings never count as official`() {
        val bundle = ContentLoader { File(root, it).readText() }.load()
        val guide = requireNotNull(bundle.battery)
        val google = requireNotNull(bundle.source("google-android-bateria"))
        assertTrue(!google.entity.isOfficial)
        guide.steps.filter { it.kind.requiresOfficialSource }.forEach { step ->
            assertTrue(step.id, step.sourceIds.any { bundle.source(it)?.entity?.isOfficial == true })
        }
    }

    @Test
    fun `milestone 6a scenarios are bundled, each with essential steps for emergency mode`() {
        val bundle = ContentLoader { File(root, it).readText() }.load()
        assertEquals(
            listOf("power-outage", "storm", "flood", "earthquake", "heat", "cold", "wildfire"),
            bundle.scenarios.map { it.id },
        )
        bundle.scenarios.forEach { sc ->
            assertEquals(sc.id, sc.emergencyNow.size, sc.emergencySteps().size)
            assertTrue(sc.id, sc.emergencySteps().isNotEmpty())
        }
    }

    @Test
    fun `agif counts as official and flood advice taken from dam guidance says so`() {
        val bundle = ContentLoader { File(root, it).readText() }.load()
        assertTrue(requireNotNull(bundle.source("agif-autoprotecao")).entity.isOfficial)
        val flood = requireNotNull(bundle.scenario("flood"))
        val damSteps = flood.phases.all.filter { "anepc-riscos-tecnologicos" in it.sourceIds }
        assertTrue(damSteps.isNotEmpty())
        damSteps.forEach { step ->
            assertTrue(step.id, step.note?.resolve(com.goncalogoncalves.resilia.core.domain.model.ContentLanguage.PT_PT)?.contains("barragens") == true)
        }
    }
}
