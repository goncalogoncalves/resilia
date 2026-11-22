package com.goncalogoncalves.resilia.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/*
 * Official alerts (milestone 7): IPMA weather warnings and rural fire risk.
 * Bundled content (areas, municipalities, labels) lives in content/alerts/alerts.json;
 * live data comes from the IPMA open-data API and is parsed in core:content (IpmaParser).
 */

@Serializable
enum class AlertRegion {
    @SerialName("continent") CONTINENT,
    @SerialName("madeira") MADEIRA,
    @SerialName("azores") AZORES,
}

/** An IPMA warning area (`idAreaAviso`), e.g. "PTO" (Porto) or "MCN" (Madeira, North Coast). */
@Serializable
data class WarningArea(val id: String, val name: LocalizedText, val region: AlertRegion)

/** A mainland municipality, by its DICO code. Used only for the fire risk. */
@Serializable
data class Municipality(val dico: String, val name: String, val areaId: String)

/** Label for an IPMA warning type; [id] is the exact `awarenessTypeName`, e.g. "Vento". */
@Serializable
data class WarningTypeInfo(val id: String, val name: LocalizedText)

@Serializable
enum class AlertLevel(val rank: Int) {
    @SerialName("green") GREEN(0),
    @SerialName("yellow") YELLOW(1),
    @SerialName("orange") ORANGE(2),
    @SerialName("red") RED(3),

    /** A colour the app does not know (the IPMA may add one). Shown without colour. */
    @SerialName("unknown") UNKNOWN(1),
    ;

    companion object {
        fun fromIpma(id: String): AlertLevel = when (id.lowercase()) {
            "green" -> GREEN
            "yellow" -> YELLOW
            "orange" -> ORANGE
            "red" -> RED
            else -> UNKNOWN
        }
    }
}

@Serializable
data class AlertLevelInfo(val level: AlertLevel, val name: LocalizedText, val meaning: LocalizedText)

@Serializable
data class FireRiskLevelInfo(val level: Int, val name: LocalizedText)

@Serializable
data class AlertsContent(
    val sourceIds: List<String>,
    val areas: List<WarningArea>,
    val municipalities: List<Municipality>,
    val warningTypes: List<WarningTypeInfo>,
    val levels: List<AlertLevelInfo>,
    val fireRiskLevels: List<FireRiskLevelInfo>,
    val review: Review = Review(),
) {
    fun area(id: String): WarningArea? = areas.firstOrNull { it.id == id }
    fun municipality(dico: String): Municipality? = municipalities.firstOrNull { it.dico == dico }
    fun type(id: String): WarningTypeInfo? = warningTypes.firstOrNull { it.id == id }
    fun level(level: AlertLevel): AlertLevelInfo? = levels.firstOrNull { it.level == level }
    fun fireRisk(level: Int): FireRiskLevelInfo? = fireRiskLevels.firstOrNull { it.level == level }
}

// --- Live data

/** One IPMA warning. Times are UTC (IPMA warnings page: "Toda a Informação em horas UTC"). */
data class Warning(
    val areaId: String,
    val type: String,
    val level: AlertLevel,
    val start: Instant,
    val end: Instant,
    /** Filled by the IPMA only for yellow, orange and red; Portuguese only. */
    val text: String,
) {
    /** Same warning, same notification: area, type, level and start. */
    val key: String get() = "w|$areaId|$type|${level.name}|$start"
}

/** Fire risk (1 low … 5 maximum) for one mainland municipality on one day. */
data class FireRisk(val dico: String, val day: LocalDate, val level: Int, val updatedAt: String?) {
    val key: String get() = "r|$dico|$day|$level"
}

/** The user's zone. [dico] only exists on the mainland (fire risk). */
data class AlertZone(val areaId: String, val dico: String? = null)

data class AlertSettings(
    val zone: AlertZone? = null,
    /** Hours between background checks; 0 = off (decision 2). */
    val intervalHours: Int = DEFAULT_INTERVAL,
    /** Yellow warnings also notify (decision 3; off by default). */
    val notifyYellow: Boolean = false,
) {
    val backgroundEnabled: Boolean get() = zone != null && intervalHours > 0

    companion object {
        const val DEFAULT_INTERVAL = 3
        val INTERVAL_OPTIONS = listOf(1, 3, 6, 0)
    }
}

/** What the app last managed to read from the IPMA, plus the last error if the newest attempt failed. */
data class AlertSnapshot(
    val warnings: List<Warning> = emptyList(),
    val fireRisk: List<FireRisk> = emptyList(),
    /** When the app downloaded the warnings (null = never). */
    val fetchedAt: Instant? = null,
    /** Last attempt failed (no network, server error, unreadable file). */
    val lastAttemptFailed: Boolean = false,
    val lastAttemptAt: Instant? = null,
)

object Alerts {
    /** Older than this, the warnings are marked "out of date" (decision 8). */
    val STALE_AFTER: Duration = Duration.ofHours(6)

    /** Opening the app refreshes when the data is older than this (decision 2). */
    val REFRESH_ON_OPEN_AFTER: Duration = Duration.ofHours(1)

    const val FIRE_NOTIFY_FROM = 4

    /** Warnings for the zone that are not green and have not ended, most severe first, then soonest. */
    fun active(warnings: List<Warning>, areaId: String, now: Instant): List<Warning> =
        warnings
            .filter { it.areaId == areaId && it.level != AlertLevel.GREEN && it.end.isAfter(now) }
            .sortedWith(compareByDescending<Warning> { it.level.rank }.thenBy { it.start })

    fun fireRiskFor(risks: List<FireRisk>, dico: String?): List<FireRisk> =
        if (dico == null) emptyList() else risks.filter { it.dico == dico }.sortedBy { it.day }

    fun isStale(snapshot: AlertSnapshot, now: Instant): Boolean =
        snapshot.fetchedAt?.let { Duration.between(it, now) > STALE_AFTER } ?: false

    fun shouldRefreshOnOpen(snapshot: AlertSnapshot?, now: Instant): Boolean =
        snapshot?.fetchedAt?.let { Duration.between(it, now) > REFRESH_ON_OPEN_AFTER } ?: true

    /** Warnings that deserve a notification (decision 3), not yet notified. */
    fun warningsToNotify(active: List<Warning>, settings: AlertSettings, notified: Set<String>): List<Warning> {
        val min = if (settings.notifyYellow) AlertLevel.YELLOW.rank else AlertLevel.ORANGE.rank
        return active.filter { it.level != AlertLevel.UNKNOWN && it.level.rank >= min && it.key !in notified }
    }

    fun fireRiskToNotify(risks: List<FireRisk>, today: LocalDate, notified: Set<String>): List<FireRisk> =
        risks.filter { it.level >= FIRE_NOTIFY_FROM && !it.day.isBefore(today) && it.key !in notified }

    /** Scenario suggested for an IPMA warning type (`alertTriggers`, decision 5). */
    fun scenarioForWarning(scenarios: List<Scenario>, type: String): Scenario? =
        scenarios.firstOrNull { "ipma:$type" in it.alertTriggers }

    fun scenarioForFireRisk(scenarios: List<Scenario>, level: Int): Scenario? =
        scenarios.firstOrNull { "rcm:$level" in it.alertTriggers }

    /** Orange and red suggest Emergency mode (decision 4); never activates on its own. */
    fun suggestsEmergency(level: AlertLevel): Boolean = level.rank >= AlertLevel.ORANGE.rank && level != AlertLevel.UNKNOWN
}
