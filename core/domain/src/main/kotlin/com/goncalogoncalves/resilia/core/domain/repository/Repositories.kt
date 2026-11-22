package com.goncalogoncalves.resilia.core.domain.repository

import com.goncalogoncalves.resilia.core.domain.model.AlertSettings
import com.goncalogoncalves.resilia.core.domain.model.AlertSnapshot
import com.goncalogoncalves.resilia.core.domain.model.AlertZone
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.FamilyPlan
import com.goncalogoncalves.resilia.core.domain.model.Household
import com.goncalogoncalves.resilia.core.domain.model.InventoryItem
import com.goncalogoncalves.resilia.core.domain.model.Member
import com.goncalogoncalves.resilia.core.domain.model.Place
import com.goncalogoncalves.resilia.core.domain.model.PlanContact
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ContentRepository {
    /** Loads (once) and returns all bundled content. Works fully offline. */
    suspend fun load(): ContentBundle
}

/** App state that must survive restarts. No personal data here. */
data class AppState(
    val disclaimerAcceptedVersion: Int = 0,
    val emergencyModeActive: Boolean = false,
    val activeScenarioId: String? = null,
)

interface AppStateRepository {
    val state: Flow<AppState>
    suspend fun acceptDisclaimer(version: Int)
    suspend fun setEmergencyMode(active: Boolean, scenarioId: String? = null)
    suspend fun setActiveScenario(scenarioId: String)
}

/** Favourite guides, by scenario id. Stored only on the device. */
interface FavoritesRepository {
    val favorites: Flow<Set<String>>
    suspend fun setFavorite(scenarioId: String, favorite: Boolean)
}

/** The user's inventory. Stored only on the device, excluded from cloud backups. */
interface InventoryRepository {
    val items: Flow<List<InventoryItem>>
    suspend fun all(): List<InventoryItem>
    suspend fun save(item: InventoryItem): Long
    suspend fun delete(id: Long)
    suspend fun markNotified(soon: List<InventoryItem>, expired: List<InventoryItem>)
}

/** Calculator answers, reminder switch and kit review dates. Stored only on the device. */
data class PreparednessState(
    val household: Household = Household(),
    val remindersEnabled: Boolean = false,
    val lastReviewedOn: LocalDate? = null,
    /** Review date for which the reminder was already sent. */
    val reviewNotifiedFor: LocalDate? = null,
    /** Ask for the phone's screen lock before showing medical sheets (decision §6.2, off by default). */
    val medicalLock: Boolean = false,
)

interface PreparednessRepository {
    val state: Flow<PreparednessState>
    suspend fun setHousehold(household: Household)
    suspend fun setRemindersEnabled(enabled: Boolean)
    suspend fun markReviewed(on: LocalDate)
    suspend fun markReviewNotified(forDate: LocalDate)
    suspend fun setMedicalLock(enabled: Boolean)
}

/** Family plan. Stored only on the device; medical sheets are encrypted at rest. */
interface FamilyPlanRepository {
    val plan: Flow<FamilyPlan>
    val members: Flow<List<Member>>
    val contacts: Flow<List<PlanContact>>
    suspend fun savePlan(plan: FamilyPlan)
    suspend fun saveMember(member: Member): Long
    suspend fun deleteMember(id: Long)
    suspend fun saveContact(contact: PlanContact): Long
    suspend fun deleteContact(id: Long)
}

/** Encrypts small secrets with a key that never leaves the device (Android Keystore). */
interface SecretCipher {
    fun encrypt(plain: ByteArray): ByteArray
    fun decrypt(data: ByteArray): ByteArray
}

/** Filters of the Calma tab, remembered on the device. */
interface ActivityFilterRepository {
    val filter: Flow<com.goncalogoncalves.resilia.core.domain.model.ActivityFilter>
    suspend fun setFilter(filter: com.goncalogoncalves.resilia.core.domain.model.ActivityFilter)
}

/**
 * IPMA warnings and fire risk (milestone 7). The zone and the cached data stay on the device;
 * the app downloads the national files and filters them locally, so the IPMA never learns the zone.
 */
interface AlertsRepository {
    val settings: Flow<AlertSettings>
    val snapshot: Flow<AlertSnapshot>
    suspend fun setZone(zone: AlertZone?)
    suspend fun setInterval(hours: Int)
    suspend fun setNotifyYellow(enabled: Boolean)

    /** Downloads the warnings (and the fire risk if the zone has a municipality). Keeps old data on failure. */
    suspend fun refresh(): Boolean
    suspend fun notifiedKeys(): Set<String>
    suspend fun markNotified(keys: Collection<String>)
}

/** The person's own places (milestone 8a). Stored only on the device, excluded from cloud backups. */
interface PlacesRepository {
    val places: Flow<List<Place>>
    suspend fun save(place: Place): Long
    suspend fun delete(id: Long)
}

/** Manual export / import of the person's data (Marco 9a). The file never leaves the phone by itself. */
interface BackupRepository {
    /** Returns the file text. [password] null = not encrypted (refused when there are medical sheets). */
    suspend fun export(password: CharArray?): String

    /** True when the file is encrypted (ask for the password). Throws BackupException if it is not a backup. */
    fun isEncrypted(text: String): Boolean

    /** Replaces the current data with the file's. Everything is read and checked before anything is deleted. */
    suspend fun import(text: String, password: CharArray?)
}
