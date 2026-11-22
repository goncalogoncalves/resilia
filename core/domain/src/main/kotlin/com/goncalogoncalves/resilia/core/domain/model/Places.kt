package com.goncalogoncalves.resilia.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URLDecoder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Places (milestone 8a). The person's own places are stored on the device (Room);
 * the SNS hospital list is bundled content (content/places/sns-urgencias.json).
 */

// --- SNS hospitals (bundled content)

/** Highest emergency level of a hospital unit, as named by the SNS. Ordered from highest. */
@Serializable
enum class UrgencyLevel {
    @SerialName("polyvalent-trauma") POLYVALENT_TRAUMA,
    @SerialName("polyvalent") POLYVALENT,
    @SerialName("medical-surgical") MEDICAL_SURGICAL,
    @SerialName("basic") BASIC,
}

/**
 * One hospital unit with an emergency department. No coordinates on purpose: in the
 * SNS dataset they were inconsistent (Marco 8 report), so maps are searched by address.
 */
@Serializable
data class Hospital(
    val id: String,
    val name: String,
    val group: String,
    val urgency: UrgencyLevel,
    val address: String,
    val postalCode: String,
    val locality: String,
    /** As published, e.g. "ARS Norte", "SRS Açores". */
    val region: String,
) {
    /** "Av. X, 1234-567 Lisboa" — what we hand to the maps app as a search. */
    val fullAddress: String get() = "$address, $postalCode $locality"
}

@Serializable
data class HospitalsContent(
    val sourceIds: List<String>,
    /** "Last modified" of the SNS dataset (YYYY-MM-DD). */
    val dataModified: String,
    val retrievedAt: String,
    val hospitals: List<Hospital>,
    val review: Review = Review(),
)

// --- The person's own places

enum class PlaceType {
    HOME, WORK_SCHOOL, MEETING, HEALTH, PHARMACY, FIREFIGHTERS, SHELTER, OTHER,
}

data class LatLng(val lat: Double, val lng: Double) {
    init {
        require(lat in -90.0..90.0 && lng in -180.0..180.0) { "out of range" }
    }

    /** "38.71667, -9.13333": dot decimals, what every maps app accepts. */
    fun format(): String = "%.5f, %.5f".format(java.util.Locale.ROOT, lat, lng)
}

data class Place(
    val id: Long = 0,
    val type: PlaceType,
    val name: String,
    val address: String = "",
    val notes: String = "",
    val position: LatLng? = null,
    val createdAt: Long = 0,
)

/** Eight compass directions, for "2.3 km south-west". */
enum class Compass { N, NE, E, SE, S, SW, W, NW }

data class DistanceAndDirection(val metres: Int, val direction: Compass)

object Geo {
    private const val EARTH_RADIUS_M = 6_371_000.0

    fun distanceMetres(a: LatLng, b: LatLng): Double {
        val p1 = Math.toRadians(a.lat)
        val p2 = Math.toRadians(b.lat)
        val dp = p2 - p1
        val dl = Math.toRadians(b.lng - a.lng)
        val h = sin(dp / 2) * sin(dp / 2) + cos(p1) * cos(p2) * sin(dl / 2) * sin(dl / 2)
        return 2 * EARTH_RADIUS_M * atan2(sqrt(h), sqrt(1 - h))
    }

    /** Initial bearing from [a] to [b], degrees clockwise from north (0–360). */
    fun bearing(a: LatLng, b: LatLng): Double {
        val p1 = Math.toRadians(a.lat)
        val p2 = Math.toRadians(b.lat)
        val dl = Math.toRadians(b.lng - a.lng)
        val y = sin(dl) * cos(p2)
        val x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dl)
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    fun compass(bearing: Double): Compass = Compass.entries[((bearing / 45.0).roundToInt()) % 8]

    /** From home to a place; null when either has no coordinates or they are the same point. */
    fun fromHome(home: LatLng?, place: LatLng?): DistanceAndDirection? {
        if (home == null || place == null) return null
        val d = distanceMetres(home, place)
        if (d < 1) return null
        return DistanceAndDirection(d.roundToInt(), compass(bearing(home, place)))
    }
}

/**
 * Reads coordinates typed or pasted by the person (no location permission, decision 2):
 * "38.7167, -9.1333", "38,7167 -9,1333", geo: URIs, and share links from Google Maps,
 * OpenStreetMap and Organic Maps that carry the numbers in the URL.
 */
object CoordinateParser {
    private const val NUM = """-?\d{1,3}(?:[.]\d+)?"""
    private val patterns = listOf(
        Regex("""geo:($NUM),($NUM)"""),
        Regex("""[?&](?:q|query|ll|center|daddr|destination)=($NUM),\s*($NUM)"""),
        Regex("""@($NUM),($NUM)"""),
        Regex("""[?&]mlat=($NUM)&mlon=($NUM)"""),
        Regex("""#map=\d+(?:\.\d+)?/($NUM)/($NUM)"""),
        Regex("""[?&]lat=($NUM)&lon=($NUM)"""),
        Regex("""^\s*($NUM)\s*[,;]\s*($NUM)\s*$"""),
        Regex("""^\s*($NUM)\s+($NUM)\s*$"""),
    )

    fun parse(input: String): LatLng? {
        val text = runCatching { URLDecoder.decode(input.trim(), "UTF-8") }.getOrDefault(input.trim())
        // Portuguese decimal commas, only when the whole input is "lat lng" with commas inside numbers.
        val commaDecimals = Regex("""^\s*(-?\d{1,3},\d+)[\s;]+(-?\d{1,3},\d+)\s*$""").find(text)
        if (commaDecimals != null) {
            return build(commaDecimals.groupValues[1].replace(',', '.'), commaDecimals.groupValues[2].replace(',', '.'))
        }
        for (p in patterns) {
            val m = p.find(text) ?: continue
            build(m.groupValues[1], m.groupValues[2])?.let { return it }
        }
        return null
    }

    private fun build(lat: String, lng: String): LatLng? {
        val a = lat.toDoubleOrNull() ?: return null
        val b = lng.toDoubleOrNull() ?: return null
        if (a !in -90.0..90.0 || b !in -180.0..180.0) return null
        if (a == 0.0 && b == 0.0) return null
        return LatLng(a, b)
    }
}
