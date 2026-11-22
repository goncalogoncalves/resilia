package com.goncalogoncalves.resilia.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Educational content model. Mirrors the JSON Schemas in /content/schema.
 * Any change here must be reflected in the schemas (and vice versa).
 */

@Serializable
enum class ReviewStatus {
    @SerialName("draft") DRAFT,
    @SerialName("reviewed") REVIEWED,
}

/** Shared review metadata. `reviewedBy` is kept in data but never shown in the UI (decision D10). */
@Serializable
data class Review(
    val status: ReviewStatus = ReviewStatus.DRAFT,
    val reviewedAt: String? = null,
    val reviewedBy: String? = null,
)

@Serializable
enum class SourceEntity(val isOfficial: Boolean) {
    ANEPC(true), INEM(true), CVP(true), DGS(true), IPMA(true), EU(true), SNS24(true),

    /** Government of Portugal (portugal.gov.pt, gov.pt). */
    GOV(true),

    /** Municipal civil protection services (câmaras municipais); name in `publisher`. */
    MUNICIPAL(true),

    /** AGIF, the rural-fire agency (SGIFR, sgifr.gov.pt). */
    AGIF(true),

    /** Electricity/water operators: authoritative for their own contacts, not for safety advice. */
    UTILITY(false),
    OTHER(false),
}

@Serializable
data class Source(
    val id: String,
    val entity: SourceEntity,
    val title: LocalizedText,
    val url: String,
    /** Date we read the page (YYYY-MM-DD). */
    val accessedAt: String,
    /** Display name of the publisher when [entity] alone is not enough, e.g. "E-REDES". */
    val publisher: String? = null,
    /** Date printed on the document, if any (YYYY-MM-DD). */
    val publishedAt: String? = null,
    val license: String = "unknown",
)

@Serializable
enum class Region {
    @SerialName("all") ALL,
    @SerialName("mainland") MAINLAND,
    @SerialName("azores") AZORES,
    @SerialName("madeira") MADEIRA,
}

/** A phone contact. Every number must cite at least one source (validator rule). */
@Serializable
data class Contact(
    val id: String,
    val name: LocalizedText,
    /** As displayed, e.g. "808 24 24 24". */
    val number: String,
    val purpose: LocalizedText,
    val region: Region = Region.ALL,
    val hours: LocalizedText? = null,
    val cost: LocalizedText? = null,
    val sourceIds: List<String> = emptyList(),
    val review: Review = Review(),
) {
    /** Digits only, for the dialer. */
    val dial: String get() = number.filter { it.isDigit() || it == '+' }
}

@Serializable
enum class Severity {
    @SerialName("low") LOW,
    @SerialName("medium") MEDIUM,
    @SerialName("high") HIGH,
    @SerialName("critical") CRITICAL,
}

@Serializable
enum class StepKind {
    /** Requires an official source (validator rule). */
    @SerialName("safety") SAFETY,

    /** Requires an official source (validator rule). */
    @SerialName("health") HEALTH,
    @SerialName("practical") PRACTICAL,
    @SerialName("wellbeing") WELLBEING;

    val requiresOfficialSource: Boolean get() = this == SAFETY || this == HEALTH
}

@Serializable
enum class Urgency {
    @SerialName("now") NOW,
    @SerialName("soon") SOON,
    @SerialName("later") LATER,
    @SerialName("info") INFO,
}

@Serializable
data class Step(
    val id: String,
    val text: LocalizedText,
    val detail: LocalizedText? = null,
    val kind: StepKind,
    val urgency: Urgency,
    val relatedKitItemIds: List<String> = emptyList(),
    val note: LocalizedText? = null,
    val sourceIds: List<String> = emptyList(),
    val review: Review = Review(),
)

@Serializable
data class Phases(
    val before: List<Step> = emptyList(),
    val during: List<Step> = emptyList(),
    val after: List<Step> = emptyList(),
) {
    val all: List<Step> get() = before + during + after
}

enum class Phase { BEFORE, DURING, AFTER }

fun Phases.of(phase: Phase): List<Step> = when (phase) {
    Phase.BEFORE -> before
    Phase.DURING -> during
    Phase.AFTER -> after
}

@Serializable
data class Scenario(
    val id: String,
    val title: LocalizedText,
    val summary: LocalizedText,
    /** Material Symbols name, e.g. "power_off". */
    val icon: String,
    val typicalSeverity: Severity,
    val typicalDuration: LocalizedText,
    val keywords: Map<String, List<String>> = emptyMap(),
    val phases: Phases,
    /** Step ids shown as "do this NOW" in Emergency mode. */
    val emergencyNow: List<String> = emptyList(),
    /** e.g. "ipma:Vento" — used for the automatic Emergency-mode suggestion (milestone 7). */
    val alertTriggers: List<String> = emptyList(),
    val relatedScenarioIds: List<String> = emptyList(),
    val sourceIds: List<String> = emptyList(),
    val review: Review = Review(),
) {
    /** A scenario is only as reviewed as its least-reviewed step. */
    val effectiveReviewStatus: ReviewStatus
        get() = if (review.status == ReviewStatus.REVIEWED &&
            phases.all.all { it.review.status == ReviewStatus.REVIEWED }
        ) ReviewStatus.REVIEWED else ReviewStatus.DRAFT

    fun emergencySteps(): List<Step> {
        val byId = phases.all.associateBy { it.id }
        return emergencyNow.mapNotNull { byId[it] }
    }
}

@Serializable
data class ContentManifest(
    val contentVersion: String,
    val schemaVersion: Int,
    val scenarios: List<String>,
    val sources: String,
    val contacts: String,
    val kit: String,
    val plan: String,
    val battery: String,
    val activities: String = "morale/activities.json",
    val materials: String = "morale/materials.json",
    val support: String = "morale/support.json",
    val alerts: String = "alerts/alerts.json",
    val hospitals: String = "places/sns-urgencias.json",
)

/** Everything loaded from /content, already parsed. */
data class ContentBundle(
    val manifest: ContentManifest,
    val sources: List<Source>,
    val scenarios: List<Scenario>,
    val contacts: List<Contact> = emptyList(),
    val kit: KitCatalog? = null,
    val plan: FamilyPlanGuide? = null,
    val battery: ToolGuide? = null,
    val activities: List<Activity> = emptyList(),
    val materials: List<Material> = emptyList(),
    val support: SupportContent? = null,
    val alerts: AlertsContent? = null,
    val hospitals: HospitalsContent? = null,
) {
    fun activity(id: String): Activity? = activities.firstOrNull { it.id == id }
    fun material(id: String): Material? = materials.firstOrNull { it.id == id }
    fun contact(id: String): Contact? = contacts.firstOrNull { it.id == id }

    fun scenario(id: String): Scenario? = scenarios.firstOrNull { it.id == id }
    fun source(id: String): Source? = sources.firstOrNull { it.id == id }
}

/** A short guide attached to a tool (e.g. "Make the battery last"). Same step rules as scenarios. */
@Serializable
data class ToolGuide(
    val id: String,
    val title: LocalizedText,
    val summary: LocalizedText,
    val steps: List<Step>,
    val sourceIds: List<String> = emptyList(),
    val review: Review = Review(),
)
