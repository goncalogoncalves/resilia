package com.goncalogoncalves.resilia.core.data.plan

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.goncalogoncalves.resilia.core.data.inventory.InventoryDatabase
import com.goncalogoncalves.resilia.core.domain.model.FamilyPlan
import com.goncalogoncalves.resilia.core.domain.model.MedicalInfo
import com.goncalogoncalves.resilia.core.domain.model.Member
import com.goncalogoncalves.resilia.core.domain.model.PlanContact
import com.goncalogoncalves.resilia.core.domain.repository.SecretCipher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class FamilyPlanStorageTest {

    /** Robolectric has no Android Keystore: a reversible stand-in that clearly changes the bytes. */
    private class FakeCipher : SecretCipher {
        override fun encrypt(plain: ByteArray) = byteArrayOf(9) + plain.map { (it.toInt() xor 0x5A).toByte() }
        override fun decrypt(data: ByteArray) = data.drop(1).map { (it.toInt() xor 0x5A).toByte() }.toByteArray()
    }

    private val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(), InventoryDatabase::class.java,
    ).allowMainThreadQueries().build()
    private val repo = RoomFamilyPlanRepository(db.familyPlan(), FakeCipher())

    @After fun close() = db.close()

    @Test
    fun `plan is empty until saved and round-trips`() = runTest {
        assertTrue(repo.plan.first().isEmpty)
        val plan = FamilyPlan(meetingNear = "Jardim em frente", contactName = "Tia Ana", contactPhone = "910 000 000")
        repo.savePlan(plan)
        assertEquals(plan, repo.plan.first())
    }

    @Test
    fun `medical sheet is never stored in clear`() = runTest {
        val medical = MedicalInfo(bloodType = "A+", allergies = "Penicilina")
        val id = repo.saveMember(Member(name = "Rui", medical = medical))

        val raw = db.familyPlan().observeMembers().first().single()
        val stored = requireNotNull(raw.medicalCipher).decodeToString()
        assertFalse("allergy stored in clear", "Penicilina" in stored)

        val read = repo.members.first().single()
        assertEquals(id, read.id)
        assertEquals(medical, read.medical)
        assertFalse(read.medicalOnPaper)
    }

    @Test
    fun `empty medical sheet stores nothing`() = runTest {
        repo.saveMember(Member(name = "Ana"))
        assertNull(db.familyPlan().observeMembers().first().single().medicalCipher)
    }

    @Test
    fun `contacts save update and delete`() = runTest {
        val id = repo.saveContact(PlanContact(name = "Escola", phone = "210 000 000"))
        repo.saveContact(repo.contacts.first().single().copy(onWalletCard = true))
        assertEquals(id, repo.contacts.first().single().id)
        assertTrue(repo.contacts.first().single().onWalletCard)
        repo.deleteContact(id)
        assertTrue(repo.contacts.first().isEmpty())
    }

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), InventoryDatabase::class.java)

    @Test
    fun `migration 1 to 2 keeps the inventory`() {
        // Robolectric: the helper expects the full path of the database file.
        val dbName = ApplicationProvider.getApplicationContext<android.content.Context>()
            .getDatabasePath("migration-test").absolutePath
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                "INSERT INTO inventory (name, quantity, list, kitItemId, expiresOn, addedOn, notifiedSoonFor, notifiedExpiredFor) " +
                    "VALUES ('Água', '6 L', 'HOME', 'home.water', '2026-12-01', '2026-11-20', NULL, NULL)",
            )
            close()
        }
        val migrated = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)
        migrated.query("SELECT name FROM inventory").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Água", c.getString(0))
        }
        migrated.query("SELECT COUNT(*) FROM family_member").use { c ->
            c.moveToFirst()
            assertEquals(0, c.getInt(0))
        }
    }
}
