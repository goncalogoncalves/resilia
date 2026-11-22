package com.goncalogoncalves.resilia.core.content

import com.goncalogoncalves.resilia.core.domain.model.AlertLevel
import com.goncalogoncalves.resilia.core.domain.model.AlertRegion
import com.goncalogoncalves.resilia.core.domain.model.AlertSettings
import com.goncalogoncalves.resilia.core.domain.model.AlertSnapshot
import com.goncalogoncalves.resilia.core.domain.model.Alerts
import com.goncalogoncalves.resilia.core.domain.model.FireRisk
import com.goncalogoncalves.resilia.core.domain.model.Warning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.LocalDate

/** IPMA responses saved on 2026-11-21 (src/test/resources/ipma) and the bundled alerts content. */
class AlertsTest {

    private fun resource(name: String) = requireNotNull(javaClass.getResource("/ipma/$name")).readText()

    private val bundle by lazy {
        val root = File(requireNotNull(System.getProperty("resilia.contentDir")))
        ContentLoader { File(root, it).readText() }.load()
    }

    @Test
    fun `parses the real warnings file`() {
        val warnings = IpmaParser.warnings(resource("warnings_www-2026-11-21.json"))
        assertEquals(204, warnings.size)
        assertEquals(25, warnings.map { it.areaId }.toSet().size)
        val yellow = warnings.filter { it.level == AlertLevel.YELLOW }
        assertEquals(setOf("PTO", "VRL", "VCT", "BRG"), yellow.map { it.areaId }.toSet())
        val porto = yellow.single { it.areaId == "PTO" }
        assertEquals("Precipitação", porto.type)
        // No offset in the file: read as UTC (IPMA warnings page: "Toda a Informação em horas UTC").
        assertEquals(Instant.parse("2026-11-21T18:00:00Z"), porto.start)
        assertEquals(Instant.parse("2026-11-21T21:00:00Z"), porto.end)
        assertTrue(porto.text.startsWith("Aguaceiros"))
    }

    @Test
    fun `warnings parser is tolerant`() {
        val body = """[
            {"idAreaAviso":"LSB","awarenessTypeName":"Vento","awarenessLevelID":"purple","startTime":"2026-01-01T00:00:00","endTime":"2026-01-01T06:00:00","text":"x","new":1},
            {"idAreaAviso":"LSB","awarenessTypeName":"Vento","awarenessLevelID":"red","startTime":"not a date","endTime":"2026-01-01T06:00:00"},
            "junk"
        ]"""
        val warnings = IpmaParser.warnings(body)
        assertEquals(1, warnings.size)
        assertEquals(AlertLevel.UNKNOWN, warnings.single().level)
        assertTrue(runCatching { IpmaParser.warnings("{}") }.exceptionOrNull() is IpmaFormatException)
        assertTrue(runCatching { IpmaParser.warnings("<html>") }.exceptionOrNull() is IpmaFormatException)
    }

    @Test
    fun `parses the real fire risk file`() {
        val risks = IpmaParser.fireRisk(resource("rcm-d0-2026-11-21.json"))
        assertEquals(278, risks.size)
        assertTrue(risks.all { it.day == LocalDate.parse("2026-11-21") && it.level in 1..5 })
        // Every municipality in the file is in the bundled list, and vice versa.
        assertEquals(risks.map { it.dico }.toSet(), requireNotNull(bundle.alerts).municipalities.map { it.dico }.toSet())
    }

    @Test
    fun `bundled areas follow the IPMA warnings page`() {
        val alerts = requireNotNull(bundle.alerts)
        assertEquals(25, alerts.areas.size)
        assertEquals(18, alerts.areas.count { it.region == AlertRegion.CONTINENT })
        assertEquals(4, alerts.areas.count { it.region == AlertRegion.MADEIRA })
        assertEquals(3, alerts.areas.count { it.region == AlertRegion.AZORES })
        val warnings = IpmaParser.warnings(resource("warnings_www-2026-11-21.json"))
        assertEquals(warnings.map { it.areaId }.toSet(), alerts.areas.map { it.id }.toSet())
        assertEquals(warnings.map { it.type }.toSet(), alerts.warningTypes.map { it.id }.toSet())
        assertEquals("Arouca", alerts.municipality("0104")?.name)
        assertEquals("AVR", alerts.municipality("0104")?.areaId)
        assertTrue(alerts.sourceIds.all { bundle.source(it) != null })
    }

    @Test
    fun `alert triggers follow decision 5`() {
        fun warning(type: String) = Alerts.scenarioForWarning(bundle.scenarios, type)?.id
        assertEquals("storm", warning("Vento"))
        assertEquals("storm", warning("Trovoada"))
        assertEquals("storm", warning("Agitação Marítima"))
        assertEquals("flood", warning("Precipitação"))
        assertEquals("heat", warning("Tempo Quente"))
        assertEquals("cold", warning("Tempo Frio"))
        assertEquals("cold", warning("Neve"))
        assertNull(warning("Nevoeiro"))
        assertNull(Alerts.scenarioForFireRisk(bundle.scenarios, 3))
        assertEquals("wildfire", Alerts.scenarioForFireRisk(bundle.scenarios, 4)?.id)
        assertEquals("wildfire", Alerts.scenarioForFireRisk(bundle.scenarios, 5)?.id)
    }

    @Test
    fun `unknown trigger is a content error`() {
        val sc = bundle.scenarios.first()
        val broken = bundle.copy(scenarios = listOf(sc.copy(alertTriggers = listOf("ipma:Granizo", "rcm:9", "x:y"))) + bundle.scenarios.drop(1))
        val errors = ContentRules.check(broken)
        assertTrue(errors.toString(), errors.any { "ipma:Granizo" in it })
        assertTrue(errors.toString(), errors.any { "rcm:9" in it })
        assertTrue(errors.toString(), errors.any { "unknown kind 'x'" in it })
    }

    private val now = Instant.parse("2026-11-21T19:00:00Z")

    private fun w(level: AlertLevel, area: String = "PTO", start: String = "2026-11-21T18:00:00Z", end: String = "2026-11-21T21:00:00Z", type: String = "Vento") =
        Warning(area, type, level, Instant.parse(start), Instant.parse(end), "")

    @Test
    fun `active warnings are the zone's, not green, not over, most severe first`() {
        val list = listOf(
            w(AlertLevel.YELLOW),
            w(AlertLevel.GREEN),
            w(AlertLevel.RED, start = "2026-11-22T00:00:00Z", end = "2026-11-22T06:00:00Z"),
            w(AlertLevel.ORANGE, area = "LSB"),
            w(AlertLevel.ORANGE, end = "2026-11-21T18:30:00Z"), // already over
        )
        assertEquals(listOf(AlertLevel.RED, AlertLevel.YELLOW), Alerts.active(list, "PTO", now).map { it.level })
    }

    @Test
    fun `only orange and red notify by default, each once`() {
        val active = listOf(w(AlertLevel.RED), w(AlertLevel.ORANGE, type = "Precipitação"), w(AlertLevel.YELLOW), w(AlertLevel.UNKNOWN))
        val default = Alerts.warningsToNotify(active, AlertSettings(), emptySet())
        assertEquals(listOf(AlertLevel.RED, AlertLevel.ORANGE), default.map { it.level })
        val withYellow = Alerts.warningsToNotify(active, AlertSettings(notifyYellow = true), emptySet())
        assertEquals(3, withYellow.size)
        assertEquals(1, Alerts.warningsToNotify(active, AlertSettings(), setOf(active[0].key)).size)
        assertTrue(Alerts.suggestsEmergency(AlertLevel.ORANGE))
        assertFalse(Alerts.suggestsEmergency(AlertLevel.YELLOW))
        assertFalse(Alerts.suggestsEmergency(AlertLevel.UNKNOWN))
    }

    @Test
    fun `fire risk notifies from very high, not for past days`() {
        val today = LocalDate.parse("2026-11-21")
        val risks = listOf(
            FireRisk("0104", today.minusDays(1), 5, null),
            FireRisk("0104", today, 3, null),
            FireRisk("0104", today.plusDays(1), 4, null),
        )
        assertEquals(listOf(today.plusDays(1)), Alerts.fireRiskToNotify(risks, today, emptySet()).map { it.day })
    }

    @Test
    fun `stale after six hours, refresh on open after one`() {
        val snap = AlertSnapshot(fetchedAt = now.minusSeconds(5 * 3600))
        assertFalse(Alerts.isStale(snap, now))
        assertTrue(Alerts.isStale(snap, now.plusSeconds(2 * 3600)))
        assertTrue(Alerts.shouldRefreshOnOpen(snap, now))
        assertFalse(Alerts.shouldRefreshOnOpen(AlertSnapshot(fetchedAt = now.minusSeconds(600)), now))
        assertTrue(Alerts.shouldRefreshOnOpen(null, now))
    }
}
