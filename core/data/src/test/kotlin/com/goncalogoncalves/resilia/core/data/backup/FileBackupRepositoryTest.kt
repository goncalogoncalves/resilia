package com.goncalogoncalves.resilia.core.data.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.goncalogoncalves.resilia.core.data.inventory.InventoryDatabase
import com.goncalogoncalves.resilia.core.data.inventory.RoomInventoryRepository
import com.goncalogoncalves.resilia.core.data.places.RoomPlacesRepository
import com.goncalogoncalves.resilia.core.data.plan.RoomFamilyPlanRepository
import com.goncalogoncalves.resilia.core.domain.device.AlertScheduler
import com.goncalogoncalves.resilia.core.domain.model.AlertSettings
import com.goncalogoncalves.resilia.core.domain.model.AlertSnapshot
import com.goncalogoncalves.resilia.core.domain.model.AlertZone
import com.goncalogoncalves.resilia.core.domain.model.BackupException
import com.goncalogoncalves.resilia.core.domain.model.FamilyPlan
import com.goncalogoncalves.resilia.core.domain.model.Household
import com.goncalogoncalves.resilia.core.domain.model.InventoryItem
import com.goncalogoncalves.resilia.core.domain.model.MedicalInfo
import com.goncalogoncalves.resilia.core.domain.model.Member
import com.goncalogoncalves.resilia.core.domain.model.Place
import com.goncalogoncalves.resilia.core.domain.model.PlaceType
import com.goncalogoncalves.resilia.core.domain.repository.AlertsRepository
import com.goncalogoncalves.resilia.core.domain.repository.PreparednessRepository
import com.goncalogoncalves.resilia.core.domain.repository.PreparednessState
import com.goncalogoncalves.resilia.core.domain.repository.SecretCipher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class FileBackupRepositoryTest {

    private class FakeCipher : SecretCipher {
        override fun encrypt(plain: ByteArray) = byteArrayOf(9) + plain
        override fun decrypt(data: ByteArray) = data.drop(1).toByteArray()
    }

    private class FakePreparedness : PreparednessRepository {
        val flow = MutableStateFlow(PreparednessState())
        override val state: Flow<PreparednessState> = flow
        override suspend fun setHousehold(household: Household) { flow.value = flow.value.copy(household = household) }
        override suspend fun setRemindersEnabled(enabled: Boolean) = Unit
        override suspend fun markReviewed(on: LocalDate) = Unit
        override suspend fun markReviewNotified(forDate: LocalDate) = Unit
        override suspend fun setMedicalLock(enabled: Boolean) = Unit
    }

    private class FakeAlerts : AlertsRepository {
        val flow = MutableStateFlow(AlertSettings())
        override val settings: Flow<AlertSettings> = flow
        override val snapshot: Flow<AlertSnapshot> = MutableStateFlow(AlertSnapshot())
        override suspend fun setZone(zone: AlertZone?) { flow.value = flow.value.copy(zone = zone) }
        override suspend fun setInterval(hours: Int) { flow.value = flow.value.copy(intervalHours = hours) }
        override suspend fun setNotifyYellow(enabled: Boolean) { flow.value = flow.value.copy(notifyYellow = enabled) }
        override suspend fun refresh() = true
        override suspend fun notifiedKeys() = emptySet<String>()
        override suspend fun markNotified(keys: Collection<String>) = Unit
    }

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private fun db() = Room.inMemoryDatabaseBuilder(context, InventoryDatabase::class.java).allowMainThreadQueries().build()
    private val dbA = db()
    private val dbB = db()

    private fun repo(db: InventoryDatabase, prep: FakePreparedness, alerts: FakeAlerts, scheduled: MutableList<AlertSettings>) =
        FileBackupRepository(
            context,
            RoomInventoryRepository(db.inventory()),
            RoomFamilyPlanRepository(db.familyPlan(), FakeCipher()),
            RoomPlacesRepository(db.places()),
            prep,
            alerts,
            AlertScheduler { scheduled += it },
        )

    @After fun close() {
        dbA.close()
        dbB.close()
    }

    @Test
    fun `export from one phone and import on another replaces everything`() = runTest {
        val prepA = FakePreparedness()
        val alertsA = FakeAlerts()
        val a = repo(dbA, prepA, alertsA, mutableListOf())
        RoomInventoryRepository(dbA.inventory()).save(InventoryItem(name = "Água", quantity = "6 L", addedOn = LocalDate.of(2026, 11, 22)))
        val planA = RoomFamilyPlanRepository(dbA.familyPlan(), FakeCipher())
        planA.savePlan(FamilyPlan(meetingNear = "Jardim"))
        planA.saveMember(Member(name = "Rui", medical = MedicalInfo(allergies = "Penicilina")))
        RoomPlacesRepository(dbA.places()).save(Place(type = PlaceType.HOME, name = "Casa"))
        prepA.setHousehold(Household(people = 4))
        alertsA.setZone(AlertZone("PTO", "1312"))

        // Medical sheet → a password is required.
        assertTrue(runCatching { a.export(null) }.exceptionOrNull() is BackupException.MedicalNeedsPassword)
        val file = a.export("frase-secreta-longa".toCharArray())

        // Phone B already has other data, which must be replaced.
        val prepB = FakePreparedness()
        val alertsB = FakeAlerts()
        val scheduled = mutableListOf<AlertSettings>()
        val b = repo(dbB, prepB, alertsB, scheduled)
        RoomInventoryRepository(dbB.inventory()).save(InventoryItem(name = "Velas", addedOn = LocalDate.of(2026, 1, 1)))
        assertTrue(b.isEncrypted(file))
        assertTrue(runCatching { b.import(file, "errada-mas-longa".toCharArray()) }.exceptionOrNull() is BackupException.WrongPassword)
        assertEquals("wrong password changes nothing", listOf("Velas"), RoomInventoryRepository(dbB.inventory()).all().map { it.name })

        b.import(file, "frase-secreta-longa".toCharArray())
        assertEquals(listOf("Água"), RoomInventoryRepository(dbB.inventory()).all().map { it.name })
        val planB = RoomFamilyPlanRepository(dbB.familyPlan(), FakeCipher())
        assertEquals("Jardim", planB.plan.first().meetingNear)
        assertEquals("Penicilina", planB.members.first().single().medical.allergies)
        assertEquals(listOf("Casa"), RoomPlacesRepository(dbB.places()).places.first().map { it.name })
        assertEquals(4, prepB.flow.value.household.people)
        assertEquals(AlertZone("PTO", "1312"), alertsB.flow.value.zone)
        assertEquals("alerts rescheduled", 1, scheduled.size)
    }
}
