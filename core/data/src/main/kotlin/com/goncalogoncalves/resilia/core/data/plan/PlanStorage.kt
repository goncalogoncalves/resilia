package com.goncalogoncalves.resilia.core.data.plan

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/** The plan's free-text answers. A single row (id = 1). */
@Entity(tableName = "family_plan")
data class FamilyPlanEntity(
    @PrimaryKey val id: Int = 1,
    val meetingNear: String,
    val meetingFar: String,
    val contactName: String,
    val contactPhone: String,
    val contactAddress: String,
    val helpsOthers: String,
    val bringsKit: String,
    val callsEmergency: String,
    val exitMain: String,
    val exitAlternative: String,
    val shutoffPower: String,
    val shutoffWater: String,
    val shutoffGas: String,
    val shelter: String,
)

@Entity(tableName = "family_member")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relation: String,
    val phone: String,
    val notes: String,
    /** AES-GCM ciphertext of the medical sheet (JSON); null when empty. Never stored in clear. */
    val medicalCipher: ByteArray?,
    val medicalOnPaper: Boolean,
) {
    // Room compares entities by value; ByteArray needs content equality.
    override fun equals(other: Any?): Boolean =
        other is MemberEntity && id == other.id && name == other.name && relation == other.relation &&
            phone == other.phone && notes == other.notes && medicalOnPaper == other.medicalOnPaper &&
            (medicalCipher?.contentEquals(other.medicalCipher) ?: (other.medicalCipher == null))

    override fun hashCode(): Int = id.hashCode() * 31 + name.hashCode()
}

@Entity(tableName = "plan_contact")
data class PlanContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val onWalletCard: Boolean,
)

@Dao
interface FamilyPlanDao {
    @Query("SELECT * FROM family_plan WHERE id = 1")
    fun observePlan(): Flow<FamilyPlanEntity?>

    @Upsert
    suspend fun upsertPlan(plan: FamilyPlanEntity)

    @Query("SELECT * FROM family_member ORDER BY id")
    fun observeMembers(): Flow<List<MemberEntity>>

    @Upsert
    suspend fun upsertMember(member: MemberEntity): Long

    @Query("DELETE FROM family_member WHERE id = :id")
    suspend fun deleteMember(id: Long)

    @Query("SELECT * FROM plan_contact ORDER BY id")
    fun observeContacts(): Flow<List<PlanContactEntity>>

    @Upsert
    suspend fun upsertContact(contact: PlanContactEntity): Long

    @Query("DELETE FROM plan_contact WHERE id = :id")
    suspend fun deleteContact(id: Long)
}

/** Version 1 (Marco 3: inventory) → 2 (Marco 4: family plan). Inventory is untouched. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `family_plan` (`id` INTEGER NOT NULL, `meetingNear` TEXT NOT NULL,
            `meetingFar` TEXT NOT NULL, `contactName` TEXT NOT NULL, `contactPhone` TEXT NOT NULL,
            `contactAddress` TEXT NOT NULL, `helpsOthers` TEXT NOT NULL, `bringsKit` TEXT NOT NULL,
            `callsEmergency` TEXT NOT NULL, `exitMain` TEXT NOT NULL, `exitAlternative` TEXT NOT NULL,
            `shutoffPower` TEXT NOT NULL, `shutoffWater` TEXT NOT NULL, `shutoffGas` TEXT NOT NULL,
            `shelter` TEXT NOT NULL, PRIMARY KEY(`id`))""",
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `family_member` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `name` TEXT NOT NULL, `relation` TEXT NOT NULL, `phone` TEXT NOT NULL, `notes` TEXT NOT NULL,
            `medicalCipher` BLOB, `medicalOnPaper` INTEGER NOT NULL)""",
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `plan_contact` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `name` TEXT NOT NULL, `phone` TEXT NOT NULL, `onWalletCard` INTEGER NOT NULL)""",
        )
    }
}
