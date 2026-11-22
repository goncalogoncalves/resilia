package com.goncalogoncalves.resilia.core.data.inventory

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.goncalogoncalves.resilia.core.domain.model.Expiry
import com.goncalogoncalves.resilia.core.domain.model.InventoryItem
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
class RoomInventoryRepositoryTest {

    private val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(), InventoryDatabase::class.java,
    ).allowMainThreadQueries().build()
    private val repo = RoomInventoryRepository(db.inventory())
    private val today = LocalDate.of(2026, 11, 20)

    @After fun close() = db.close()

    @Test
    fun `items are sorted by expiry with undated last`() = runTest {
        repo.save(InventoryItem(name = "Sem data", addedOn = today))
        repo.save(InventoryItem(name = "Tarde", expiresOn = today.plusDays(90), addedOn = today))
        repo.save(InventoryItem(name = "Cedo", expiresOn = today.plusDays(3), addedOn = today))
        assertEquals(listOf("Cedo", "Tarde", "Sem data"), repo.items.first().map { it.name })
    }

    @Test
    fun `update keeps the id and notified flags stop repeats`() = runTest {
        val id = repo.save(InventoryItem(name = "Água", expiresOn = today.plusDays(5), addedOn = today))
        val saved = repo.all().single()
        assertEquals(id, saved.id)

        val due = Expiry.dueReminders(repo.all(), today)
        assertEquals(1, due.soon.size)
        repo.markNotified(due.soon, due.expired)
        assertTrue(Expiry.dueReminders(repo.all(), today).isEmpty)

        // A new expiry date re-arms the reminder.
        repo.save(saved.copy(expiresOn = today.plusDays(10), notifiedSoonFor = repo.all().single().notifiedSoonFor))
        assertEquals(id, repo.all().single().id)
        assertEquals(1, Expiry.dueReminders(repo.all(), today).soon.size)

        repo.delete(id)
        assertTrue(repo.all().isEmpty())
    }
}
