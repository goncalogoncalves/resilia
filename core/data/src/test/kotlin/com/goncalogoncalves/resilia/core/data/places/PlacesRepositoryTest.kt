package com.goncalogoncalves.resilia.core.data.places

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.goncalogoncalves.resilia.core.data.alerts.MIGRATION_2_3
import com.goncalogoncalves.resilia.core.data.inventory.InventoryDatabase
import com.goncalogoncalves.resilia.core.data.plan.MIGRATION_1_2
import com.goncalogoncalves.resilia.core.domain.model.LatLng
import com.goncalogoncalves.resilia.core.domain.model.Place
import com.goncalogoncalves.resilia.core.domain.model.PlaceType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class PlacesRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val db = Room.inMemoryDatabaseBuilder(context, InventoryDatabase::class.java).allowMainThreadQueries().build()
    private val repo = RoomPlacesRepository(db.places())

    @After fun close() = db.close()

    @Test
    fun `places round-trip, home first, and can be deleted`() = runTest {
        repo.save(Place(type = PlaceType.MEETING, name = "  Jardim  ", address = "Largo 1"))
        val homeId = repo.save(Place(type = PlaceType.HOME, name = "Casa", position = LatLng(38.72, -9.14)))
        val list = repo.places.first()
        assertEquals(listOf("Casa", "Jardim"), list.map { it.name })
        assertEquals(LatLng(38.72, -9.14), list.first().position)
        assertNull(list.last().position)
        assertTrue(list.all { it.createdAt > 0 })

        val edited = list.first().copy(name = "Casa da avó")
        assertEquals(homeId, repo.save(edited))
        assertEquals("Casa da avó", repo.places.first().first().name)

        repo.delete(homeId)
        assertEquals(listOf("Jardim"), repo.places.first().map { it.name })
    }

    @Test
    fun `migration 3 to 4 adds the place table and keeps the data`() {
        val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), InventoryDatabase::class.java)
        val name = context.getDatabasePath("migration-places").absolutePath
        helper.createDatabase(name, 3).apply {
            execSQL("INSERT INTO alert_notified (`key`, notifiedAt) VALUES ('w|PTO', 1)")
            close()
        }
        val migrated = helper.runMigrationsAndValidate(name, 4, true, MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        migrated.query("SELECT COUNT(*) FROM alert_notified").use { c -> c.moveToFirst(); assertEquals(1, c.getInt(0)) }
        migrated.query("SELECT COUNT(*) FROM place").use { c -> c.moveToFirst(); assertEquals(0, c.getInt(0)) }
    }
}
