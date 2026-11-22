package com.goncalogoncalves.resilia.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * "Calma" tab: screen-free activities and emotional support (milestone 6b).
 * Mirrors content/schema/activities.schema.json, materials.schema.json and support.schema.json.
 */

@Serializable
enum class ActivityType {
    @SerialName("game") GAME,

    /** "Histórias e sons": making up stories, rhythm and sound games. Never song lyrics. */
    @SerialName("stories") STORIES,
    @SerialName("calm") CALM,
    @SerialName("challenge") CHALLENGE,
}

@Serializable
enum class ActivityOrigin {
    /** Written for Resilia. */
    @SerialName("original") ORIGINAL,

    /** A game everyone knows, described in our own words. */
    @SerialName("traditional") TRADITIONAL,

    /** Needs an `originNote` naming the work (validator rule). */
    @SerialName("public-domain") PUBLIC_DOMAIN,

    /** Based on official advice: needs an official source (validator rule). */
    @SerialName("guidance") GUIDANCE,
}

@Serializable
enum class Noise {
    @SerialName("quiet") QUIET,
    @SerialName("moderate") MODERATE,
    @SerialName("loud") LOUD,
}

@Serializable
enum class ActivitySetting {
    @SerialName("indoor") INDOOR,
    @SerialName("outdoor") OUTDOOR,
    @SerialName("both") BOTH,
}

/** How much light the activity needs. */
@Serializable
enum class LightNeed {
    /** Works in complete darkness. */
    @SerialName("none") NONE,

    /** Needs a torch (e.g. hand shadows), not room light. */
    @SerialName("torch") TORCH,
    @SerialName("light") LIGHT,
}

@Serializable
data class Material(val id: String, val name: LocalizedText)

@Serializable
data class Activity(
    val id: String,
    val type: ActivityType,
    val title: LocalizedText,
    val summary: LocalizedText,
    val ageMin: Int,
    val ageMax: Int? = null,
    val peopleMin: Int,
    val peopleMax: Int? = null,
    /** Ids from materials.json. Empty = nothing needed. */
    val materials: List<String> = emptyList(),
    val durationMin: Int,
    val setting: ActivitySetting,
    val noise: Noise,
    val light: LightNeed,
    val instructions: List<LocalizedText>,
    val variants: LocalizedText? = null,
    val origin: ActivityOrigin,
    val originNote: LocalizedText? = null,
    val sourceIds: List<String> = emptyList(),
    val review: Review = Review(),
) {
    /** Can be done with the lights out and without disturbing anyone (Emergency mode). */
    val goodForDarkAndQuiet: Boolean get() = light == LightNeed.NONE && noise == Noise.QUIET

    fun suitsAge(age: Int): Boolean = age >= ageMin && (ageMax == null || age <= ageMax)

    fun suitsPeople(n: Int): Boolean = n >= peopleMin && (peopleMax == null || n <= peopleMax)
}

/** Emotional support: official advice split into short guides (for you, children, older people...). */
@Serializable
data class SupportContent(
    /** Shown on top, e.g. "Official guide written for storms and floods". */
    val note: LocalizedText,
    val guides: List<ToolGuide>,
    /** Contacts offered at the end of every support guide. */
    val contactIds: List<String> = emptyList(),
)

/** Filters of the Calma tab. All off = show everything. */
data class ActivityFilter(
    val type: ActivityType? = null,
    val dark: Boolean = false,
    val quiet: Boolean = false,
    val noMaterials: Boolean = false,
    val alone: Boolean = false,
    val short: Boolean = false,
    val age: Int? = null,
) {
    fun matches(a: Activity): Boolean =
        (type == null || a.type == type) &&
            (!dark || a.light == LightNeed.NONE) &&
            (!quiet || a.noise == Noise.QUIET) &&
            (!noMaterials || a.materials.isEmpty()) &&
            (!alone || a.peopleMin <= 1) &&
            (!short || a.durationMin <= SHORT_MINUTES) &&
            (age == null || a.suitsAge(age))

    companion object {
        const val SHORT_MINUTES = 10

        /** Representative ages offered by the age chip. */
        val AGE_OPTIONS = listOf(4, 7, 10, 14)
    }
}
