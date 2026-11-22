package com.goncalogoncalves.resilia.core.data.backup

import android.content.Context
import com.goncalogoncalves.resilia.core.domain.device.AlertScheduler
import com.goncalogoncalves.resilia.core.domain.model.AlertSettings
import com.goncalogoncalves.resilia.core.domain.model.AlertZone
import com.goncalogoncalves.resilia.core.domain.model.BackupAlerts
import com.goncalogoncalves.resilia.core.domain.model.BackupCodec
import com.goncalogoncalves.resilia.core.domain.model.BackupData
import com.goncalogoncalves.resilia.core.domain.repository.AlertsRepository
import com.goncalogoncalves.resilia.core.domain.repository.BackupRepository
import com.goncalogoncalves.resilia.core.domain.repository.FamilyPlanRepository
import com.goncalogoncalves.resilia.core.domain.repository.InventoryRepository
import com.goncalogoncalves.resilia.core.domain.repository.PlacesRepository
import com.goncalogoncalves.resilia.core.domain.repository.PreparednessRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileBackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inventory: InventoryRepository,
    private val plan: FamilyPlanRepository,
    private val places: PlacesRepository,
    private val preparedness: PreparednessRepository,
    private val alerts: AlertsRepository,
    private val scheduler: AlertScheduler,
) : BackupRepository {

    override suspend fun export(password: CharArray?): String {
        val settings = alerts.settings.first()
        val data = BackupData(
            createdAt = LocalDateTime.now().withNano(0).toString(),
            appVersion = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty(),
            household = preparedness.state.first().household,
            inventory = BackupData.inventoryOf(inventory.all()),
            plan = plan.plan.first(),
            members = plan.members.first().map { it.copy(id = 0) },
            contacts = plan.contacts.first().map { it.copy(id = 0) },
            places = BackupData.placesOf(places.places.first()),
            alerts = BackupAlerts(settings.zone?.areaId, settings.zone?.dico, settings.intervalHours, settings.notifyYellow),
        )
        // Key derivation is deliberately slow: keep it off the main thread.
        return withContext(Dispatchers.Default) { BackupCodec.encode(data, password) }
    }

    override fun isEncrypted(text: String): Boolean = BackupCodec.inspect(text).encrypted

    override suspend fun import(text: String, password: CharArray?) {
        // 1. Read and check everything first: a wrong password or a broken file changes nothing.
        val data = withContext(Dispatchers.Default) { BackupCodec.decode(text, password) }
        val newInventory = data.inventoryItems()
        val newPlaces = data.placeItems()

        // 2. Replace.
        inventory.all().forEach { inventory.delete(it.id) }
        newInventory.forEach { inventory.save(it) }

        plan.members.first().forEach { plan.deleteMember(it.id) }
        plan.contacts.first().forEach { plan.deleteContact(it.id) }
        plan.savePlan(data.plan)
        data.members.forEach { plan.saveMember(it.copy(id = 0)) }
        data.contacts.forEach { plan.saveContact(it.copy(id = 0)) }

        places.places.first().forEach { places.delete(it.id) }
        newPlaces.forEach { places.save(it) }

        preparedness.setHousehold(data.household)

        val a = data.alerts
        alerts.setZone(a.areaId?.let { AlertZone(it, a.dico) })
        if (a.intervalHours in AlertSettings.INTERVAL_OPTIONS) alerts.setInterval(a.intervalHours)
        alerts.setNotifyYellow(a.notifyYellow)
        scheduler.schedule(alerts.settings.first())
    }
}
