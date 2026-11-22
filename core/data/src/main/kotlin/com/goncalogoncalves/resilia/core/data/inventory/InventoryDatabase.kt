package com.goncalogoncalves.resilia.core.data.inventory

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import com.goncalogoncalves.resilia.core.data.alerts.AlertCacheEntity
import com.goncalogoncalves.resilia.core.data.alerts.AlertDao
import com.goncalogoncalves.resilia.core.data.alerts.AlertNotifiedEntity
import com.goncalogoncalves.resilia.core.data.places.PlaceDao
import com.goncalogoncalves.resilia.core.data.places.PlaceEntity
import com.goncalogoncalves.resilia.core.data.plan.FamilyPlanDao
import com.goncalogoncalves.resilia.core.data.plan.FamilyPlanEntity
import com.goncalogoncalves.resilia.core.data.plan.MemberEntity
import com.goncalogoncalves.resilia.core.data.plan.PlanContactEntity
import com.goncalogoncalves.resilia.core.domain.model.InventoryItem
import com.goncalogoncalves.resilia.core.domain.model.KitList
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Room database "resilia.db". Lives in the app's private storage; excluded from cloud backups.
 * v1: inventory (Marco 3) · v2: family plan (Marco 4) · v3: IPMA alerts cache (Marco 7) · v4: places (Marco 8a).
 */
@Database(
    entities = [
        InventoryEntity::class, FamilyPlanEntity::class, MemberEntity::class, PlanContactEntity::class,
        AlertCacheEntity::class, AlertNotifiedEntity::class, PlaceEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun inventory(): InventoryDao
    abstract fun familyPlan(): FamilyPlanDao
    abstract fun alerts(): AlertDao
    abstract fun places(): PlaceDao

    companion object {
        const val NAME = "resilia.db"
    }
}

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: String,
    val list: String,
    val kitItemId: String?,
    val expiresOn: LocalDate?,
    val addedOn: LocalDate,
    val notifiedSoonFor: LocalDate?,
    val notifiedExpiredFor: LocalDate?,
)

@Dao
interface InventoryDao {
    /** Soonest expiry first; items without a date at the end. */
    @Query("SELECT * FROM inventory ORDER BY expiresOn IS NULL, expiresOn, name COLLATE NOCASE")
    fun observeAll(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory")
    suspend fun all(): List<InventoryEntity>

    @Upsert
    suspend fun upsert(item: InventoryEntity): Long

    @Query("DELETE FROM inventory WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE inventory SET notifiedSoonFor = expiresOn WHERE id IN (:ids)")
    suspend fun markSoonNotified(ids: List<Long>)

    @Query("UPDATE inventory SET notifiedExpiredFor = expiresOn WHERE id IN (:ids)")
    suspend fun markExpiredNotified(ids: List<Long>)
}

class Converters {
    /** ISO dates (YYYY-MM-DD) sort correctly as text. */
    @TypeConverter fun toText(date: LocalDate?): String? = date?.toString()
    @TypeConverter fun toDate(text: String?): LocalDate? = text?.let(LocalDate::parse)
}

internal fun InventoryEntity.toDomain() = InventoryItem(
    id = id,
    name = name,
    quantity = quantity,
    list = KitList.entries.firstOrNull { it.name == list } ?: KitList.HOME,
    kitItemId = kitItemId,
    expiresOn = expiresOn,
    addedOn = addedOn,
    notifiedSoonFor = notifiedSoonFor,
    notifiedExpiredFor = notifiedExpiredFor,
)

internal fun InventoryItem.toEntity() = InventoryEntity(
    id = id,
    name = name.trim(),
    quantity = quantity.trim(),
    list = list.name,
    kitItemId = kitItemId,
    expiresOn = expiresOn,
    addedOn = addedOn,
    notifiedSoonFor = notifiedSoonFor,
    notifiedExpiredFor = notifiedExpiredFor,
)
