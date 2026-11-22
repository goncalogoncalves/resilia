package com.goncalogoncalves.resilia.core.data.alerts

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.goncalogoncalves.resilia.core.data.inventory.InventoryDatabase
import com.goncalogoncalves.resilia.core.data.plan.MIGRATION_1_2
import com.goncalogoncalves.resilia.core.domain.model.AlertLevel
import com.goncalogoncalves.resilia.core.domain.model.AlertZone
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException
import java.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class AlertsRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val db = Room.inMemoryDatabaseBuilder(context, InventoryDatabase::class.java).allowMainThreadQueries().build()
    private val requested = mutableListOf<String>()
    private var answer: (String) -> Pair<Int, String> = { 200 to WARNINGS }
    private val repo = IpmaAlertsRepository(
        context,
        db.alerts(),
        HttpGetter { url -> requested += url; answer(url) },
        { Instant.parse("2026-11-21T19:00:00Z") },
    )

    @Before fun reset() = runTest { repo.setZone(null); repo.setNotifyYellow(false); repo.setInterval(3) }

    @After fun close() = db.close()

    @Test
    fun `refresh downloads the national file only, and stores it`() = runTest {
        repo.setZone(AlertZone("PTO"))
        assertTrue(repo.refresh())
        // The URL never says which zone (decision 4.3); no fire risk without a municipality.
        assertEquals(listOf("https://api.ipma.pt/open-data/forecast/warnings/warnings_www.json"), requested)
        val snap = repo.snapshot.first()
        assertEquals(AlertLevel.YELLOW, snap.warnings.single().level)
        assertEquals(Instant.parse("2026-11-21T19:00:00Z"), snap.fetchedAt)
        assertFalse(snap.lastAttemptFailed)
    }

    @Test
    fun `municipality adds the three fire risk days`() = runTest {
        answer = { url -> 200 to if ("rcm" in url) RCM else WARNINGS }
        repo.setZone(AlertZone("AVR", dico = "0104"))
        assertTrue(repo.refresh())
        assertEquals(4, requested.size)
        assertEquals(listOf(3), repo.snapshot.first().fireRisk.map { it.level }.distinct())
    }

    @Test
    fun `a failure keeps the last good reading`() = runTest {
        repo.setZone(AlertZone("PTO"))
        repo.refresh()
        answer = { throw IOException("offline") }
        assertFalse(repo.refresh())
        answer = { 503 to "" }
        assertFalse(repo.refresh())
        answer = { 200 to "<html>maintenance</html>" }
        assertFalse(repo.refresh())
        val snap = repo.snapshot.first()
        assertEquals(1, snap.warnings.size)
        assertTrue(snap.lastAttemptFailed)
    }

    @Test
    fun `settings round-trip and removing the zone clears it`() = runTest {
        repo.setZone(AlertZone("AVR", "0104"))
        repo.setInterval(6)
        repo.setNotifyYellow(true)
        val s = repo.settings.first()
        assertEquals(AlertZone("AVR", "0104"), s.zone)
        assertEquals(6, s.intervalHours)
        assertTrue(s.notifyYellow)
        assertTrue(s.backgroundEnabled)
        repo.setInterval(0)
        assertFalse(repo.settings.first().backgroundEnabled)
        repo.setZone(null)
        assertNull(repo.settings.first().zone)
        assertTrue(runCatching { repo.setInterval(2) }.isFailure)
    }

    @Test
    fun `notified keys are remembered`() = runTest {
        repo.markNotified(listOf("w|PTO|Vento|RED|x"))
        assertEquals(setOf("w|PTO|Vento|RED|x"), repo.notifiedKeys())
    }

    @Test
    fun `migration 2 to 3 adds the alert tables and keeps the data`() {
        val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), InventoryDatabase::class.java)
        val name = context.getDatabasePath("migration-alerts").absolutePath
        helper.createDatabase(name, 2).apply {
            execSQL(
                "INSERT INTO inventory (name, quantity, list, kitItemId, expiresOn, addedOn, notifiedSoonFor, notifiedExpiredFor) " +
                    "VALUES ('Água', '6 L', 'HOME', NULL, NULL, '2026-11-21', NULL, NULL)",
            )
            close()
        }
        val migrated = helper.runMigrationsAndValidate(name, 3, true, MIGRATION_1_2, MIGRATION_2_3)
        migrated.query("SELECT COUNT(*) FROM inventory").use { c -> c.moveToFirst(); assertEquals(1, c.getInt(0)) }
        migrated.query("SELECT COUNT(*) FROM alert_cache").use { c -> c.moveToFirst(); assertEquals(0, c.getInt(0)) }
    }

    private companion object {
        const val WARNINGS = """[{"text": "Aguaceiros fortes.", "awarenessTypeName": "Precipitação", "idAreaAviso": "PTO",
            "startTime": "2026-11-21T18:00:00", "awarenessLevelID": "yellow", "endTime": "2026-11-21T21:00:00"}]"""
        const val RCM = """{"dataPrev": "2026-11-21", "fileDate": "2026-11-21 09:35:01",
            "local": {"0104": {"data": {"rcm": 3}, "dico": "0104", "latitude": 40.93, "longitude": -8.26}}}"""
    }
}
