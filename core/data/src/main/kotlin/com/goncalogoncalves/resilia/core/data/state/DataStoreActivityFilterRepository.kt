package com.goncalogoncalves.resilia.core.data.state

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.goncalogoncalves.resilia.core.domain.model.ActivityFilter
import com.goncalogoncalves.resilia.core.domain.model.ActivityType
import com.goncalogoncalves.resilia.core.domain.repository.ActivityFilterRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreActivityFilterRepository @Inject constructor(
    @ApplicationContext context: Context,
) : ActivityFilterRepository {

    private val store = context.appStateStore

    override val filter: Flow<ActivityFilter> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            ActivityFilter(
                type = p[TYPE]?.let { name -> ActivityType.entries.firstOrNull { it.name == name } },
                dark = p[DARK] ?: false,
                quiet = p[QUIET] ?: false,
                noMaterials = p[NO_MATERIALS] ?: false,
                alone = p[ALONE] ?: false,
                short = p[SHORT] ?: false,
                age = p[AGE]?.takeIf { it > 0 },
            )
        }

    override suspend fun setFilter(filter: ActivityFilter) {
        store.edit {
            val type = filter.type
            if (type == null) it.remove(TYPE) else it[TYPE] = type.name
            it[DARK] = filter.dark
            it[QUIET] = filter.quiet
            it[NO_MATERIALS] = filter.noMaterials
            it[ALONE] = filter.alone
            it[SHORT] = filter.short
            it[AGE] = filter.age ?: 0
        }
    }

    private companion object {
        val TYPE = stringPreferencesKey("morale_type")
        val DARK = booleanPreferencesKey("morale_dark")
        val QUIET = booleanPreferencesKey("morale_quiet")
        val NO_MATERIALS = booleanPreferencesKey("morale_no_materials")
        val ALONE = booleanPreferencesKey("morale_alone")
        val SHORT = booleanPreferencesKey("morale_short")
        val AGE = intPreferencesKey("morale_age")
    }
}
