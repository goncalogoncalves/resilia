package com.goncalogoncalves.resilia.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Kit catalogue (content/kit/kit.json). Mirrors content/schema/kit.schema.json.
 * Every number here comes from an official source (validator rule): the app
 * only multiplies them, it never invents its own.
 */

/** EU SWD(2026) 826: keep supplies for staying at home apart from the bag for leaving. */
@Serializable
enum class KitList {
    @SerialName("home") HOME,
    @SerialName("bag") BAG,
}

/** When an item applies to a household. */
@Serializable
enum class KitCondition {
    @SerialName("always") ALWAYS,
    @SerialName("baby") BABY,
    @SerialName("medical") MEDICAL,
    @SerialName("pets") PETS,
}

@Serializable
enum class KitUnit {
    @SerialName("litre") LITRE,
}

@Serializable
enum class KitQuantityKind {
    /** amount × people × days, e.g. 2 L of water per person per day. */
    @SerialName("perPersonPerDay") PER_PERSON_PER_DAY,

    /** No amount: "enough for N people for N days" (no official weight or calories exist). */
    @SerialName("forDuration") FOR_DURATION,
}

@Serializable
data class KitQuantity(
    val kind: KitQuantityKind,
    val amount: Double? = null,
    val unit: KitUnit? = null,
)

@Serializable
data class KitItem(
    val id: String,
    val list: KitList,
    val condition: KitCondition = KitCondition.ALWAYS,
    val name: LocalizedText,
    val detail: LocalizedText? = null,
    val note: LocalizedText? = null,
    val quantity: KitQuantity? = null,
    val sourceIds: List<String> = emptyList(),
    val review: Review = Review(),
)

@Serializable
data class KitCatalog(
    /** Official minimum number of days (DGS, CVP, EU: at least 3). */
    val minimumDays: Int,
    val minimumDaysSourceIds: List<String>,
    /** Day options offered to the user; all must be ≥ [minimumDays]. */
    val dayOptions: List<Int>,
    /** How often to review the kit (CVP: every six months). */
    val reviewIntervalMonths: Int,
    val reviewSourceIds: List<String>,
    val items: List<KitItem>,
)

/** What the user tells the calculator. Stored only on the device. */
@Serializable
data class Household(
    val people: Int = 1,
    val days: Int = 3,
    val hasBaby: Boolean = false,
    val hasMedicalNeeds: Boolean = false,
    val hasPets: Boolean = false,
) {
    fun applies(condition: KitCondition): Boolean = when (condition) {
        KitCondition.ALWAYS -> true
        KitCondition.BABY -> hasBaby
        KitCondition.MEDICAL -> hasMedicalNeeds
        KitCondition.PETS -> hasPets
    }

    companion object {
        const val MAX_PEOPLE = 20
    }
}

/** One line of the calculated kit. */
data class KitLine(
    val item: KitItem,
    /** Total for [KitQuantityKind.PER_PERSON_PER_DAY]; null otherwise. */
    val total: Double? = null,
    val people: Int,
    val days: Int,
)

object KitCalculator {
    fun calculate(catalog: KitCatalog, household: Household, list: KitList): List<KitLine> {
        val people = household.people.coerceIn(1, Household.MAX_PEOPLE)
        val days = household.days.coerceAtLeast(catalog.minimumDays)
        return catalog.items
            .filter { it.list == list && household.applies(it.condition) }
            .map { item ->
                val q = item.quantity
                val total = if (q?.kind == KitQuantityKind.PER_PERSON_PER_DAY) {
                    requireNotNull(q.amount) * people * days
                } else null
                KitLine(item, total, people, days)
            }
    }
}
