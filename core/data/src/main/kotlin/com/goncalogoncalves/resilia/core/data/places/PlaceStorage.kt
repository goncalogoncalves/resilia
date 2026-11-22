package com.goncalogoncalves.resilia.core.data.places

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.goncalogoncalves.resilia.core.domain.model.LatLng
import com.goncalogoncalves.resilia.core.domain.model.Place
import com.goncalogoncalves.resilia.core.domain.model.PlaceType
import com.goncalogoncalves.resilia.core.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "place")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val name: String,
    val address: String,
    val notes: String,
    val lat: Double?,
    val lng: Double?,
    /** Epoch milliseconds. */
    val createdAt: Long,
)

@Dao
interface PlaceDao {
    @Query("SELECT * FROM place ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<PlaceEntity>>

    @Upsert
    suspend fun upsert(place: PlaceEntity): Long

    @Query("DELETE FROM place WHERE id = :id")
    suspend fun delete(id: Long)
}

/** v4: the person's places (milestone 8a). */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `place` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `type` TEXT NOT NULL, `name` TEXT NOT NULL, `address` TEXT NOT NULL, `notes` TEXT NOT NULL,
            `lat` REAL, `lng` REAL, `createdAt` INTEGER NOT NULL)""",
        )
    }
}

/** Home first, then by type, then by name (the list order everywhere). */
private val TYPE_ORDER = PlaceType.entries

@Singleton
class RoomPlacesRepository @Inject constructor(private val dao: PlaceDao) : PlacesRepository {

    override val places: Flow<List<Place>> = dao.observeAll().map { rows ->
        rows.map { it.toDomain() }.sortedWith(compareBy<Place> { TYPE_ORDER.indexOf(it.type) })
    }

    override suspend fun save(place: Place): Long {
        val created = if (place.createdAt == 0L) System.currentTimeMillis() else place.createdAt
        val id = dao.upsert(place.copy(createdAt = created).toEntity())
        return if (place.id != 0L) place.id else id
    }

    override suspend fun delete(id: Long) = dao.delete(id)
}

internal fun PlaceEntity.toDomain(): Place {
    val la = lat
    val lo = lng
    return Place(
        id = id,
        type = PlaceType.entries.firstOrNull { it.name == type } ?: PlaceType.OTHER,
        name = name,
        address = address,
        notes = notes,
        position = if (la != null && lo != null) runCatching { LatLng(la, lo) }.getOrNull() else null,
        createdAt = createdAt,
    )
}

internal fun Place.toEntity() = PlaceEntity(
    id = id,
    type = type.name,
    name = name.trim(),
    address = address.trim(),
    notes = notes.trim(),
    lat = position?.lat,
    lng = position?.lng,
    createdAt = createdAt,
)
