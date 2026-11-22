package com.goncalogoncalves.resilia.core.data.state

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.goncalogoncalves.resilia.core.domain.model.Household
import com.goncalogoncalves.resilia.core.domain.repository.PreparednessRepository
import com.goncalogoncalves.resilia.core.domain.repository.PreparednessState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStorePreparednessRepository @Inject constructor(
    @ApplicationContext context: Context,
) : PreparednessRepository {

    private val store = context.appStateStore

    override val state: Flow<PreparednessState> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            PreparednessState(
                household = Household(
                    people = p[PEOPLE] ?: 1,
                    days = p[DAYS] ?: 3,
                    hasBaby = p[BABY] ?: false,
                    hasMedicalNeeds = p[MEDICAL] ?: false,
                    hasPets = p[PETS] ?: false,
                ),
                remindersEnabled = p[REMINDERS] ?: false,
                lastReviewedOn = p[LAST_REVIEW]?.toDateOrNull(),
                reviewNotifiedFor = p[REVIEW_NOTIFIED]?.toDateOrNull(),
                medicalLock = p[MEDICAL_LOCK] ?: false,
            )
        }

    override suspend fun setHousehold(household: Household) {
        store.edit {
            it[PEOPLE] = household.people
            it[DAYS] = household.days
            it[BABY] = household.hasBaby
            it[MEDICAL] = household.hasMedicalNeeds
            it[PETS] = household.hasPets
        }
    }

    override suspend fun setRemindersEnabled(enabled: Boolean) {
        store.edit { it[REMINDERS] = enabled }
    }

    override suspend fun markReviewed(on: LocalDate) {
        store.edit { it[LAST_REVIEW] = on.toString() }
    }

    override suspend fun markReviewNotified(forDate: LocalDate) {
        store.edit { it[REVIEW_NOTIFIED] = forDate.toString() }
    }

    override suspend fun setMedicalLock(enabled: Boolean) {
        store.edit { it[MEDICAL_LOCK] = enabled }
    }

    private fun String.toDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

    private companion object {
        val PEOPLE = intPreferencesKey("kit_people")
        val DAYS = intPreferencesKey("kit_days")
        val BABY = booleanPreferencesKey("kit_baby")
        val MEDICAL = booleanPreferencesKey("kit_medical")
        val PETS = booleanPreferencesKey("kit_pets")
        val REMINDERS = booleanPreferencesKey("reminders_enabled")
        val LAST_REVIEW = stringPreferencesKey("kit_last_review")
        val REVIEW_NOTIFIED = stringPreferencesKey("kit_review_notified")
        val MEDICAL_LOCK = booleanPreferencesKey("medical_lock")
    }
}
