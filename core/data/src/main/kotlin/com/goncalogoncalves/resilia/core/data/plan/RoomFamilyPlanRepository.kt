package com.goncalogoncalves.resilia.core.data.plan

import com.goncalogoncalves.resilia.core.domain.model.FamilyPlan
import com.goncalogoncalves.resilia.core.domain.model.MedicalInfo
import com.goncalogoncalves.resilia.core.domain.model.Member
import com.goncalogoncalves.resilia.core.domain.model.PlanContact
import com.goncalogoncalves.resilia.core.domain.repository.FamilyPlanRepository
import com.goncalogoncalves.resilia.core.domain.repository.SecretCipher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomFamilyPlanRepository @Inject constructor(
    private val dao: FamilyPlanDao,
    private val cipher: SecretCipher,
) : FamilyPlanRepository {

    override val plan: Flow<FamilyPlan> = dao.observePlan().map { it?.toDomain() ?: FamilyPlan() }

    override val members: Flow<List<Member>> = dao.observeMembers().map { list -> list.map { it.toDomain() } }

    override val contacts: Flow<List<PlanContact>> = dao.observeContacts().map { list ->
        list.map { PlanContact(it.id, it.name, it.phone, it.onWalletCard) }
    }

    override suspend fun savePlan(plan: FamilyPlan) = dao.upsertPlan(plan.toEntity())

    override suspend fun saveMember(member: Member): Long {
        val rowId = dao.upsertMember(member.toEntity())
        return if (member.id != 0L) member.id else rowId
    }

    override suspend fun deleteMember(id: Long) = dao.deleteMember(id)

    override suspend fun saveContact(contact: PlanContact): Long {
        val rowId = dao.upsertContact(
            PlanContactEntity(contact.id, contact.name.trim(), contact.phone.trim(), contact.onWalletCard),
        )
        return if (contact.id != 0L) contact.id else rowId
    }

    override suspend fun deleteContact(id: Long) = dao.deleteContact(id)

    private fun MemberEntity.toDomain() = Member(
        id = id,
        name = name,
        relation = relation,
        phone = phone,
        notes = notes,
        medical = medicalCipher?.let(::decryptMedical) ?: MedicalInfo(),
        medicalOnPaper = medicalOnPaper,
    )

    private fun Member.toEntity() = MemberEntity(
        id = id,
        name = name.trim(),
        relation = relation.trim(),
        phone = phone.trim(),
        notes = notes.trim(),
        medicalCipher = if (medical.isEmpty) null else cipher.encrypt(json.encodeToString(MedicalInfo.serializer(), medical).toByteArray()),
        medicalOnPaper = medicalOnPaper,
    )

    /** If the key is gone (it never leaves the device), the sheet cannot be read: show it empty. */
    private fun decryptMedical(data: ByteArray): MedicalInfo? = runCatching {
        json.decodeFromString(MedicalInfo.serializer(), cipher.decrypt(data).decodeToString())
    }.getOrNull()

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

private fun FamilyPlanEntity.toDomain() = FamilyPlan(
    meetingNear, meetingFar, contactName, contactPhone, contactAddress, helpsOthers, bringsKit,
    callsEmergency, exitMain, exitAlternative, shutoffPower, shutoffWater, shutoffGas, shelter,
)

private fun FamilyPlan.toEntity() = FamilyPlanEntity(
    id = 1,
    meetingNear = meetingNear.trim(),
    meetingFar = meetingFar.trim(),
    contactName = contactName.trim(),
    contactPhone = contactPhone.trim(),
    contactAddress = contactAddress.trim(),
    helpsOthers = helpsOthers.trim(),
    bringsKit = bringsKit.trim(),
    callsEmergency = callsEmergency.trim(),
    exitMain = exitMain.trim(),
    exitAlternative = exitAlternative.trim(),
    shutoffPower = shutoffPower.trim(),
    shutoffWater = shutoffWater.trim(),
    shutoffGas = shutoffGas.trim(),
    shelter = shelter.trim(),
)
