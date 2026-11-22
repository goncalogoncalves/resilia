package com.goncalogoncalves.resilia.core.data.alerts

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/** Last good response of each IPMA file (decision §4.4). [source] is "warnings", "rcm-d0", "rcm-d1" or "rcm-d2". */
@Entity(tableName = "alert_cache")
data class AlertCacheEntity(
    @PrimaryKey val source: String,
    /** Epoch milliseconds. */
    val fetchedAt: Long,
    val payload: String,
    val httpStatus: Int,
)

/** What was already notified, so the same warning notifies once. Pruned after a week. */
@Entity(tableName = "alert_notified")
data class AlertNotifiedEntity(
    @PrimaryKey val key: String,
    val notifiedAt: Long,
)

@Dao
interface AlertDao {
    @Query("SELECT * FROM alert_cache")
    fun observeCache(): Flow<List<AlertCacheEntity>>

    @Upsert
    suspend fun upsert(entry: AlertCacheEntity)

    @Query("DELETE FROM alert_cache WHERE source LIKE 'rcm-%'")
    suspend fun clearFireRisk()

    @Query("SELECT `key` FROM alert_notified")
    suspend fun notifiedKeys(): List<String>

    @Upsert
    suspend fun markNotified(entries: List<AlertNotifiedEntity>)

    @Query("DELETE FROM alert_notified WHERE notifiedAt < :before")
    suspend fun pruneNotified(before: Long)
}

/** v3: IPMA alerts cache (milestone 7). */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `alert_cache` (`source` TEXT NOT NULL, `fetchedAt` INTEGER NOT NULL,
            `payload` TEXT NOT NULL, `httpStatus` INTEGER NOT NULL, PRIMARY KEY(`source`))""",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `alert_notified` (`key` TEXT NOT NULL, `notifiedAt` INTEGER NOT NULL, PRIMARY KEY(`key`))",
        )
    }
}
