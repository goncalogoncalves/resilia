package com.goncalogoncalves.resilia.feature.prepare

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.ContentManifest
import com.goncalogoncalves.resilia.core.domain.model.Household
import com.goncalogoncalves.resilia.core.domain.model.InventoryItem
import com.goncalogoncalves.resilia.core.domain.model.KitCatalog
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import com.goncalogoncalves.resilia.core.domain.repository.InventoryRepository
import com.goncalogoncalves.resilia.core.domain.repository.PreparednessRepository
import com.goncalogoncalves.resilia.core.domain.repository.PreparednessState
import com.goncalogoncalves.resilia.feature.prepare.reminders.ExpiryReminderWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en")
class ExpiryReminderWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val today = LocalDate.now()

    private class FakeInventory(initial: List<InventoryItem>) : InventoryRepository {
        val data = MutableStateFlow(initial)
        override val items: Flow<List<InventoryItem>> = data
        override suspend fun all() = data.value
        override suspend fun save(item: InventoryItem): Long = item.id
        override suspend fun delete(id: Long) = Unit
        override suspend fun markNotified(soon: List<InventoryItem>, expired: List<InventoryItem>) {
            data.value = data.value.map { i ->
                when {
                    soon.any { it.id == i.id } -> i.copy(notifiedSoonFor = i.expiresOn)
                    expired.any { it.id == i.id } -> i.copy(notifiedExpiredFor = i.expiresOn)
                    else -> i
                }
            }
        }
    }

    private class FakePrep(enabled: Boolean) : PreparednessRepository {
        val data = MutableStateFlow(PreparednessState(remindersEnabled = enabled))
        override val state: Flow<PreparednessState> = data.map { it }
        override suspend fun setHousehold(household: Household) = Unit
        override suspend fun setRemindersEnabled(enabled: Boolean) { data.value = data.value.copy(remindersEnabled = enabled) }
        override suspend fun markReviewed(on: LocalDate) { data.value = data.value.copy(lastReviewedOn = on) }
        override suspend fun markReviewNotified(forDate: LocalDate) { data.value = data.value.copy(reviewNotifiedFor = forDate) }
        override suspend fun setMedicalLock(enabled: Boolean) { data.value = data.value.copy(medicalLock = enabled) }
    }

    private val content = object : ContentRepository {
        override suspend fun load() = ContentBundle(
            manifest = ContentManifest("0", 1, emptyList(), "s", "c", "k", "p", "b"),
            sources = emptyList(),
            scenarios = emptyList(),
            kit = KitCatalog(3, listOf("x"), listOf(3), 6, listOf("x"), emptyList()),
        )
    }

    private fun worker(inventory: InventoryRepository, prep: PreparednessRepository) =
        TestListenableWorkerBuilder<ExpiryReminderWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker =
                    ExpiryReminderWorker(appContext, workerParameters, inventory, prep, content)
            })
            .build()

    private val notifications get() = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications

    @Before
    fun grant() {
        shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }

    @Test
    fun `sends one expiry notification and does not repeat it`() = runTest {
        val inventory = FakeInventory(
            listOf(
                InventoryItem(id = 1, name = "Water", expiresOn = today.plusDays(5), addedOn = today),
                InventoryItem(id = 2, name = "Tins", expiresOn = today, addedOn = today),
            ),
        )
        val prep = FakePrep(enabled = true)
        worker(inventory, prep).doWork()
        assertEquals(1, notifications.size)
        assertEquals(today.plusDays(5), inventory.data.value[0].notifiedSoonFor)
        assertEquals(today, inventory.data.value[1].notifiedExpiredFor)

        context.getSystemService(NotificationManager::class.java).cancelAll()
        worker(inventory, prep).doWork()
        assertEquals(0, notifications.size)
    }

    @Test
    fun `does nothing while reminders are off`() = runTest {
        val inventory = FakeInventory(listOf(InventoryItem(id = 1, name = "Water", expiresOn = today, addedOn = today)))
        worker(inventory, FakePrep(enabled = false)).doWork()
        assertEquals(0, notifications.size)
        assertEquals(null, inventory.data.value[0].notifiedExpiredFor)
    }

    @Test
    fun `review reminder is sent once when due`() = runTest {
        val inventory = FakeInventory(listOf(InventoryItem(id = 1, name = "Radio", addedOn = today.minusMonths(7))))
        val prep = FakePrep(enabled = true)
        worker(inventory, prep).doWork()
        assertEquals(1, notifications.size)
        assertEquals(today.minusMonths(7).plusMonths(6), prep.data.value.reviewNotifiedFor)

        context.getSystemService(NotificationManager::class.java).cancelAll()
        worker(inventory, prep).doWork()
        assertEquals(0, notifications.size)
    }
}
