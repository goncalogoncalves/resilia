package com.goncalogoncalves.resilia.core.data.state

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.goncalogoncalves.resilia.core.domain.repository.FavoritesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreFavoritesRepository @Inject constructor(
    @ApplicationContext context: Context,
) : FavoritesRepository {

    private val store = context.appStateStore

    override val favorites: Flow<Set<String>> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[FAVORITE_SCENARIOS].orEmpty() }

    override suspend fun setFavorite(scenarioId: String, favorite: Boolean) {
        store.edit {
            val current = it[FAVORITE_SCENARIOS].orEmpty()
            it[FAVORITE_SCENARIOS] = if (favorite) current + scenarioId else current - scenarioId
        }
    }

    private companion object {
        val FAVORITE_SCENARIOS = stringSetPreferencesKey("favorite_scenarios")
    }
}
