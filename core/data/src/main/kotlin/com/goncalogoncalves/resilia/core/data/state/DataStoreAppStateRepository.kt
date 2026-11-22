package com.goncalogoncalves.resilia.core.data.state

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.goncalogoncalves.resilia.core.domain.repository.AppState
import com.goncalogoncalves.resilia.core.domain.repository.AppStateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Single preferences file for small app state (favourites included). Excluded from backups. */
internal val Context.appStateStore: DataStore<Preferences> by preferencesDataStore(name = "app_state")

@Singleton
class DataStoreAppStateRepository @Inject constructor(
    @ApplicationContext context: Context,
) : AppStateRepository {

    private val store = context.appStateStore

    override val state: Flow<AppState> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            AppState(
                disclaimerAcceptedVersion = p[DISCLAIMER_VERSION] ?: 0,
                emergencyModeActive = p[EMERGENCY_ACTIVE] ?: false,
                activeScenarioId = p[ACTIVE_SCENARIO],
            )
        }

    override suspend fun acceptDisclaimer(version: Int) {
        store.edit { it[DISCLAIMER_VERSION] = version }
    }

    override suspend fun setEmergencyMode(active: Boolean, scenarioId: String?) {
        store.edit {
            it[EMERGENCY_ACTIVE] = active
            if (scenarioId != null) it[ACTIVE_SCENARIO] = scenarioId
        }
    }

    override suspend fun setActiveScenario(scenarioId: String) {
        store.edit { it[ACTIVE_SCENARIO] = scenarioId }
    }

    private companion object {
        val DISCLAIMER_VERSION = intPreferencesKey("disclaimer_accepted_version")
        val EMERGENCY_ACTIVE = booleanPreferencesKey("emergency_mode_active")
        val ACTIVE_SCENARIO = stringPreferencesKey("active_scenario_id")
    }
}
