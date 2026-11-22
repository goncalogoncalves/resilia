package com.goncalogoncalves.resilia.feature.prepare.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.goncalogoncalves.resilia.core.domain.device.ReminderScheduler
import com.goncalogoncalves.resilia.core.domain.model.Expiry
import com.goncalogoncalves.resilia.core.domain.model.KitReview
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import com.goncalogoncalves.resilia.core.domain.repository.InventoryRepository
import com.goncalogoncalves.resilia.core.domain.repository.PreparednessRepository
import com.goncalogoncalves.resilia.feature.prepare.R
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Daily check (decision §5.2): expiry dates and the six-monthly review. No exact alarms,
 * no network. Each reminder is sent once per date; if notifications are not allowed,
 * nothing is marked as sent, so it is shown once they are.
 */
@HiltWorker
class ExpiryReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val inventory: InventoryRepository,
    private val preparedness: PreparednessRepository,
    private val content: ContentRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val state = preparedness.state.first()
        if (!state.remindersEnabled || !Notifier.canNotify(applicationContext)) return Result.success()

        val today = LocalDate.now()
        val items = inventory.all()
        val due = Expiry.dueReminders(items, today)
        if (!due.isEmpty) {
            Notifier.expiry(applicationContext, expired = due.expired.size, soon = due.soon.size)
            inventory.markNotified(due.soon, due.expired)
        }

        val interval = content.load().kit?.reviewIntervalMonths
        val next = interval?.let { KitReview.nextReview(state.lastReviewedOn, items.minOfOrNull { it.addedOn }, it) }
        if (next != null && KitReview.isDue(next, today) && state.reviewNotifiedFor != next) {
            Notifier.review(applicationContext)
            preparedness.markReviewNotified(next)
        }
        return Result.success()
    }
}

internal object Notifier {
    private const val CHANNEL = "kit_reminders"
    private const val ID_EXPIRY = 1001
    private const val ID_REVIEW = 1002

    fun canNotify(context: Context): Boolean {
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun expiry(context: Context, expired: Int, soon: Int) {
        val res = context.resources
        val lines = buildList {
            if (expired > 0) add(res.getQuantityString(R.plurals.prepare_notif_expired, expired, expired))
            if (soon > 0) add(res.getQuantityString(R.plurals.prepare_notif_soon, soon, soon))
        }
        post(context, ID_EXPIRY, context.getString(R.string.prepare_notif_expiry_title), lines.joinToString(" "))
    }

    fun review(context: Context) = post(
        context,
        ID_REVIEW,
        context.getString(R.string.prepare_notif_review_title),
        context.getString(R.string.prepare_notif_review_text),
    )

    @Suppress("MissingPermission") // checked in canNotify()
    private fun post(context: Context, id: Int, title: String, text: String) {
        if (!canNotify(context)) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, context.getString(R.string.prepare_notif_channel), NotificationManager.IMPORTANCE_DEFAULT),
        )
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(context, id, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        // Lock screen shows only the generic title: product names stay private.
        val public = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(context.getString(R.string.prepare_notif_public))
            .build()
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(public)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}

@Singleton
class WorkReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderScheduler {
    override fun setEnabled(enabled: Boolean) {
        val work = WorkManager.getInstance(context)
        if (enabled) {
            work.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<ExpiryReminderWorker>(1, TimeUnit.DAYS).build(),
            )
        } else {
            work.cancelUniqueWork(WORK_NAME)
        }
    }

    private companion object {
        const val WORK_NAME = "kit-reminders"
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {
    @Binds
    abstract fun scheduler(impl: WorkReminderScheduler): ReminderScheduler
}
