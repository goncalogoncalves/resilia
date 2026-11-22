package com.goncalogoncalves.resilia.core.domain.model

import kotlinx.serialization.Serializable

/*
 * Family plan.
 * - FamilyPlanGuide: bundled content (content/plan/plan.json): the hint shown in each
 *   section and its sources. Mirrors content/schema/plan.schema.json.
 * - FamilyPlan, Member, MedicalInfo: what the user writes. Stored only on the device.
 */

@Serializable
data class PlanSection(
    /** One of [PlanSectionId]. */
    val id: String,
    /** Advice shown under the section title. Requires an official source (validator rule). */
    val hint: LocalizedText? = null,
    val sourceIds: List<String> = emptyList(),
    val review: Review = Review(),
)

@Serializable
data class FamilyPlanGuide(val sections: List<PlanSection>) {
    fun section(id: String): PlanSection? = sections.firstOrNull { it.id == id }
}

/** Section ids used by the app (the guide must define each one). */
object PlanSectionId {
    const val MEMBERS = "members"
    const val MEETING = "meeting"
    const val CONTACT_PERSON = "contact-person"
    const val ROLES = "roles"
    const val EXITS = "exits"
    const val SHUTOFF = "shutoff"
    const val CONTACTS = "contacts"
    const val SHELTER = "shelter"
    const val REVIEW = "review"
    val ALL = listOf(MEMBERS, MEETING, CONTACT_PERSON, ROLES, EXITS, SHUTOFF, CONTACTS, SHELTER, REVIEW)
}

/** Free-text answers (decision D12: everything optional). */
@Serializable
data class FamilyPlan(
    val meetingNear: String = "",
    val meetingFar: String = "",
    val contactName: String = "",
    val contactPhone: String = "",
    val contactAddress: String = "",
    val helpsOthers: String = "",
    val bringsKit: String = "",
    val callsEmergency: String = "",
    val exitMain: String = "",
    val exitAlternative: String = "",
    val shutoffPower: String = "",
    val shutoffWater: String = "",
    val shutoffGas: String = "",
    val shelter: String = "",
) {
    val isEmpty: Boolean get() = this == FamilyPlan()
}

/** Medical sheet. Encrypted at rest; never on paper unless the user opts in per person. */
@Serializable
data class MedicalInfo(
    val bloodType: String = "",
    val allergies: String = "",
    val medication: String = "",
    val conditions: String = "",
    val doctor: String = "",
) {
    val isEmpty: Boolean get() = this == MedicalInfo()
}

@Serializable
data class Member(
    val id: Long = 0,
    val name: String,
    val relation: String = "",
    val phone: String = "",
    val notes: String = "",
    val medical: MedicalInfo = MedicalInfo(),
    /** Print this person's medical sheet on the A4 card. Off by default (decision §6.5). */
    val medicalOnPaper: Boolean = false,
)

/** A contact the user adds (school, family doctor, neighbour...). */
@Serializable
data class PlanContact(
    val id: Long = 0,
    val name: String,
    val phone: String,
    /** Shown on the wallet card (space for 3). */
    val onWalletCard: Boolean = false,
) {
    companion object {
        const val WALLET_MAX = 3
    }
}
