package com.goncalogoncalves.resilia.feature.alerts

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.goncalogoncalves.resilia.core.domain.device.AlertScheduler
import com.goncalogoncalves.resilia.core.domain.model.AlertSettings
import com.goncalogoncalves.resilia.core.domain.model.Alerts
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.ContentLanguage
import com.goncalogoncalves.resilia.core.domain.model.FireRisk
import com.goncalogoncalves.resilia.core.domain.model.Warning
import com.goncalogoncalves.resilia.core.domain.repository.AlertsRepository
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * What a tap on an alert notification asks the app to do. Read by MainActivity.
 * Emergency mode is only ever *suggested*: the app asks for confirmation (decision 4).
 */
object AlertIntents {
    const val ACTION = "com.goncalogoncalves.resilia.action.ALERT"
    const val EXTRA_TARGET = "resilia.alert.target"
    const val EXTRA_SCENARIO = "resilia.alert.scenario"
    const val EXTRA_SUMMARY = "resilia.alert.summary"
    const val TARGET_ALERTS = "alerts"
    const val TARGET_SCENARIO = "scenario"
    const val TARGET_EMERGENCY = "emergency"

    fun intent(context: Context, target: String, scenarioId: String? = null, summary: String? = null): Intent? =
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            action = ACTION
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_TARGET, target)
            scenarioId?.let { putExtra(EXTRA_SCENARIO, it) }
            summary?.let { putExtra(EXTRA_SUMMARY, it) }
        }
}

/** Periodic IPMA check (decisions 2 and 3). Never activates Emergency mode by itself. */
@HiltWorker
class AlertsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val alerts: AlertsRepository,
    private val content: ContentRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = alerts.settings.first()
        val zone = settings.zone ?: return Result.success()
        if (!settings.backgroundEnabled) return Result.success()
        alerts.refresh()
        if (!AlertNotifier.canNotify(applicationContext)) return Result.success()

        val snapshot = alerts.snapshot.first()
        val notified = alerts.notifiedKeys()
        val now = Instant.now()
        val warnings = Alerts.warningsToNotify(Alerts.active(snapshot.warnings, zone.areaId, now), settings, notified)
        val fire = Alerts.fireRiskToNotify(
            Alerts.fireRiskFor(snapshot.fireRisk, zone.dico),
            LocalDate.now(),
            notified,
        ).take(1) // the highest-risk day is enough; the screen shows all three
        if (warnings.isEmpty() && fire.isEmpty()) return Result.success()

        val bundle = content.load()
        // Only what was actually shown is marked, so a blocked notification is retried next time.
        val shown = warnings.take(MAX_PER_RUN).filter { AlertNotifier.warning(applicationContext, bundle, it) }.map { it.key } +
            fire.filter { AlertNotifier.fireRisk(applicationContext, bundle, it) }.map { it.key }
        if (shown.isNotEmpty()) alerts.markNotified(shown)
        return Result.success()
    }

    private companion object {
        const val MAX_PER_RUN = 4
    }
}

internal object AlertNotifier {
    const val CHANNEL = "weather_alerts"

    fun canNotify(context: Context): Boolean {
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun language(context: Context): ContentLanguage {
        val locales = context.resources.configuration.locales
        return ContentLanguage.fromSystemLanguage(if (locales.isEmpty) "pt" else locales[0].language)
    }

    fun warning(context: Context, bundle: ContentBundle, w: Warning): Boolean {
        val lang = language(context)
        val labels = bundle.alerts
        val level = labels?.level(w.level)?.name?.resolve(lang) ?: context.getString(R.string.alerts_level_unknown)
        val type = labels?.type(w.type)?.name?.resolve(lang) ?: w.type
        val area = labels?.area(w.areaId)?.name?.resolve(lang) ?: w.areaId
        val title = context.getString(R.string.alerts_notif_warning_title, level, type)
        val period = AlertFormat.period(context, w.start, w.end, ZoneId.systemDefault())
        val text = listOf(context.getString(R.string.alerts_notif_where_when, area, period), w.text)
            .filter { it.isNotBlank() }.joinToString("\n")
        val scenario = Alerts.scenarioForWarning(bundle.scenarios, w.type)
        return post(
            context = context,
            id = w.key.hashCode(),
            title = title,
            text = text,
            scenarioId = scenario?.id,
            suggestEmergency = Alerts.suggestsEmergency(w.level),
            summary = "$title — $area",
        )
    }

    fun fireRisk(context: Context, bundle: ContentBundle, r: FireRisk): Boolean {
        val lang = language(context)
        val labels = bundle.alerts
        val level = labels?.fireRisk(r.level)?.name?.resolve(lang) ?: r.level.toString()
        val place = labels?.municipality(r.dico)?.name ?: r.dico
        val title = context.getString(R.string.alerts_notif_fire_title, level)
        val text = context.getString(R.string.alerts_notif_where_when, place, AlertFormat.day(context, r.day))
        val scenario = Alerts.scenarioForFireRisk(bundle.scenarios, r.level)
        return post(
            context = context,
            id = r.key.hashCode(),
            title = title,
            text = text,
            scenarioId = scenario?.id,
            suggestEmergency = false,
            summary = "$title — $place",
        )
    }

    @Suppress("MissingPermission") // checked in canNotify()
    private fun post(
        context: Context,
        id: Int,
        title: String,
        text: String,
        scenarioId: String?,
        suggestEmergency: Boolean,
        summary: String,
    ): Boolean {
        if (!canNotify(context)) return false
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, context.getString(R.string.alerts_notif_channel), NotificationManager.IMPORTANCE_HIGH)
                .apply { description = context.getString(R.string.alerts_notif_channel_description) },
        )
        fun pending(request: Int, intent: Intent?) = intent?.let {
            PendingIntent.getActivity(context, request, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        // The lock screen shows only a generic title: the area says where the person lives.
        val public = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(context.getString(R.string.alerts_notif_public))
            .build()
        val builder = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text + "\n" + context.getString(R.string.alerts_source_ipma)))
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(public)
            .setContentIntent(pending(id * 4, AlertIntents.intent(context, AlertIntents.TARGET_ALERTS)))
            .setAutoCancel(true)
        if (scenarioId != null) {
            builder.addAction(
                0,
                context.getString(R.string.alerts_action_what_to_do),
                pending(id * 4 + 1, AlertIntents.intent(context, AlertIntents.TARGET_SCENARIO, scenarioId)),
            )
        }
        if (suggestEmergency) {
            builder.addAction(
                0,
                context.getString(R.string.alerts_action_emergency),
                pending(id * 4 + 2, AlertIntents.intent(context, AlertIntents.TARGET_EMERGENCY, scenarioId, summary)),
            )
        }
        NotificationManagerCompat.from(context).notify(id, builder.build())
        return true
    }
}

@Singleton
class WorkAlertScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlertScheduler {
    override fun schedule(settings: AlertSettings) {
        val work = WorkManager.getInstance(context)
        if (!settings.backgroundEnabled) {
            work.cancelUniqueWork(WORK_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<AlertsWorker>(settings.intervalHours.toLong(), TimeUnit.HOURS)
            // Only with network; no wakelocks, no exact alarms (decision 2).
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        work.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private companion object {
        const val WORK_NAME = "ipma-alerts"
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AlertsModule {
    @Binds
    abstract fun scheduler(impl: WorkAlertScheduler): AlertScheduler
}
