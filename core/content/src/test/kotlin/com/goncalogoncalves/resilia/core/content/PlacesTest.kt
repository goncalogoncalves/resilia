package com.goncalogoncalves.resilia.core.content

import com.goncalogoncalves.resilia.core.domain.model.Compass
import com.goncalogoncalves.resilia.core.domain.model.CoordinateParser
import com.goncalogoncalves.resilia.core.domain.model.Geo
import com.goncalogoncalves.resilia.core.domain.model.LatLng
import com.goncalogoncalves.resilia.core.domain.model.UrgencyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PlacesTest {

    private val bundle by lazy {
        val root = File(requireNotNull(System.getProperty("resilia.contentDir")))
        ContentLoader { File(root, it).readText() }.load()
    }

    private fun assertNear(expected: LatLng, actual: LatLng?) {
        requireNotNull(actual) { "not parsed" }
        assertEquals(expected.lat, actual.lat, 1e-6)
        assertEquals(expected.lng, actual.lng, 1e-6)
    }

    @Test
    fun `reads typed and pasted coordinates`() {
        val lisbon = LatLng(38.7167, -9.1333)
        assertNear(lisbon, CoordinateParser.parse("38.7167, -9.1333"))
        assertNear(lisbon, CoordinateParser.parse(" 38.7167 -9.1333 "))
        assertNear(lisbon, CoordinateParser.parse("38,7167 -9,1333"))
        assertNear(lisbon, CoordinateParser.parse("38.7167;-9.1333"))
        assertNear(lisbon, CoordinateParser.parse("geo:38.7167,-9.1333?q=Casa"))
        assertNear(lisbon, CoordinateParser.parse("https://www.google.com/maps/place/X/@38.7167,-9.1333,17z/data=!3m1"))
        assertNear(lisbon, CoordinateParser.parse("https://maps.google.com/?q=38.7167,-9.1333"))
        assertNear(lisbon, CoordinateParser.parse("https://www.google.com/maps/search/?api=1&query=38.7167%2C-9.1333"))
        assertNear(lisbon, CoordinateParser.parse("https://www.openstreetmap.org/?mlat=38.7167&mlon=-9.1333#map=17/38.7167/-9.1333"))
        assertNear(lisbon, CoordinateParser.parse("https://www.openstreetmap.org/#map=17/38.7167/-9.1333"))
        assertNear(lisbon, CoordinateParser.parse("https://omaps.app/x?lat=38.7167&lon=-9.1333"))
    }

    @Test
    fun `rejects text that is not coordinates`() {
        listOf("", "Rua do Ouro 10, Lisboa", "91.0, 10.0", "38.7, 181.0", "0, 0", "1234-567", "geo:abc").forEach {
            assertNull(it, CoordinateParser.parse(it))
        }
    }

    @Test
    fun `distance and direction from home`() {
        val lisbon = LatLng(38.7223, -9.1393)
        val porto = LatLng(41.1579, -8.6291)
        val km = Geo.distanceMetres(lisbon, porto) / 1000
        assertTrue("Lisboa–Porto is about 274 km in a straight line, got $km", km in 270.0..278.0)
        assertEquals(Compass.N, Geo.fromHome(lisbon, porto)?.direction)
        assertEquals(Compass.S, Geo.fromHome(porto, lisbon)?.direction)
        assertEquals(Compass.E, Geo.compass(Geo.bearing(LatLng(0.0, 0.0), LatLng(0.0, 1.0))))
        assertEquals(Compass.NW, Geo.compass(315.0))
        assertEquals(Compass.N, Geo.compass(359.0))
        assertNull(Geo.fromHome(null, porto))
        assertNull(Geo.fromHome(lisbon, lisbon))
    }

    @Test
    fun `bundled SNS list has no coordinates and an official source`() {
        val h = requireNotNull(bundle.hospitals)
        assertEquals(94, h.hospitals.size)
        assertEquals("2026-06-17", h.dataModified)
        assertTrue(h.sourceIds.all { bundle.source(it)?.entity?.isOfficial == true })
        assertTrue(h.hospitals.all { it.postalCode.matches(Regex("\\d{4}-\\d{3}")) && it.locality.isNotBlank() })
        UrgencyLevel.entries.forEach { level -> assertTrue(level.name, h.hospitals.any { it.urgency == level }) }
        // Decision taken with the user: the SNS coordinates were inconsistent, so none are bundled.
        val raw = File(requireNotNull(System.getProperty("resilia.contentDir")), "places/sns-urgencias.json").readText()
        assertTrue("no coordinates in the SNS file", "\"lat\"" !in raw && "\"lon\"" !in raw)
    }

    @Test
    fun `hospital list rules`() {
        val h = requireNotNull(bundle.hospitals)
        val broken = bundle.copy(hospitals = h.copy(sourceIds = listOf("nope"), dataModified = "17/06/2026", hospitals = h.hospitals + h.hospitals.first()))
        val errors = ContentRules.check(broken)
        assertTrue(errors.toString(), errors.any { "unknown sourceId 'nope'" in it })
        assertTrue(errors.toString(), errors.any { "dataModified" in it })
        assertTrue(errors.toString(), errors.any { "duplicate hospital" in it })
    }
}
