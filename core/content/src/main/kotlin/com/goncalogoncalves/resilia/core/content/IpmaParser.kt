package com.goncalogoncalves.resilia.core.content

import com.goncalogoncalves.resilia.core.domain.model.AlertLevel
import com.goncalogoncalves.resilia.core.domain.model.FireRisk
import com.goncalogoncalves.resilia.core.domain.model.Warning
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Reads the IPMA open-data files documented at https://api.ipma.pt/ (decision §4.5: tolerant).
 * Unknown fields are ignored and a single bad entry is skipped; only a file that is not the
 * expected shape at all throws [IpmaFormatException].
 */
object IpmaParser {

    const val WARNINGS_URL = "https://api.ipma.pt/open-data/forecast/warnings/warnings_www.json"

    /** [day] 0 = today, 1 = tomorrow, 2 = the day after. */
    fun fireRiskUrl(day: Int): String {
        require(day in 0..2) { "IPMA only publishes 3 days of fire risk" }
        return "https://api.ipma.pt/open-data/forecast/meteorology/rcm/rcm-d$day.json"
    }

    private val json = Json { ignoreUnknownKeys = true }

    fun warnings(body: String): List<Warning> {
        val root = parse(body) as? JsonArray ?: throw IpmaFormatException("warnings: expected a JSON array")
        return root.mapNotNull { element ->
            val o = element as? JsonObject ?: return@mapNotNull null
            val area = o.string("idAreaAviso") ?: return@mapNotNull null
            val type = o.string("awarenessTypeName") ?: return@mapNotNull null
            val start = o.string("startTime")?.let(::utc) ?: return@mapNotNull null
            val end = o.string("endTime")?.let(::utc) ?: return@mapNotNull null
            Warning(
                areaId = area,
                type = type,
                level = AlertLevel.fromIpma(o.string("awarenessLevelID").orEmpty()),
                start = start,
                end = end,
                text = o.string("text").orEmpty().trim(),
            )
        }
    }

    fun fireRisk(body: String): List<FireRisk> {
        val root = parse(body) as? JsonObject ?: throw IpmaFormatException("fire risk: expected a JSON object")
        val day = root.string("dataPrev")?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: throw IpmaFormatException("fire risk: missing dataPrev")
        val updated = root.string("fileDate")
        val local = root["local"] as? JsonObject ?: throw IpmaFormatException("fire risk: missing local")
        return local.mapNotNull { (dico, value) ->
            val level = (value as? JsonObject)?.get("data")?.let { it as? JsonObject }
                ?.get("rcm")?.let { (it as? JsonPrimitive)?.intOrNull }
                ?: return@mapNotNull null
            if (level !in 1..5) null else FireRisk(dico = dico, day = day, level = level, updatedAt = updated)
        }
    }

    private fun parse(body: String): JsonElement = try {
        json.parseToJsonElement(body)
    } catch (e: Exception) {
        throw IpmaFormatException("not JSON: ${e.message}", e)
    }

    private fun JsonObject.string(key: String): String? = (get(key) as? JsonPrimitive)?.takeIf { it.isString }?.content

    /** The IPMA times carry no offset; the warnings page states they are UTC. */
    private fun utc(text: String) = runCatching { LocalDateTime.parse(text).toInstant(ZoneOffset.UTC) }.getOrNull()
}

class IpmaFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)
