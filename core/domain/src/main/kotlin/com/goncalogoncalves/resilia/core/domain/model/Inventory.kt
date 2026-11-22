package com.goncalogoncalves.resilia.core.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Something the user keeps at home or in the bag. Stored only on the device. */
data class InventoryItem(
    val id: Long = 0,
    val name: String,
    /** Free text, e.g. "6 L" or "2 latas". */
    val quantity: String = "",
    val list: KitList = KitList.HOME,
    /** The catalogue item it was added from, if any. */
    val kitItemId: String? = null,
    val expiresOn: LocalDate? = null,
    val addedOn: LocalDate,
    /** Expiry date for which the "expires soon" reminder was already sent. */
    val notifiedSoonFor: LocalDate? = null,
    /** Expiry date for which the "expired" reminder was already sent. */
    val notifiedExpiredFor: LocalDate? = null,
)

sealed interface ExpiryStatus {
    data object NoDate : ExpiryStatus
    data class Ok(val days: Long) : ExpiryStatus
    data class Soon(val days: Long) : ExpiryStatus
    data object Today : ExpiryStatus
    data class Expired(val daysAgo: Long) : ExpiryStatus
}

object Expiry {
    /** Remind this many days before (decision §4.7 of marco-3-fontes.md; ours, not an official figure). */
    const val SOON_DAYS = 30L

    fun status(expiresOn: LocalDate?, today: LocalDate): ExpiryStatus {
        expiresOn ?: return ExpiryStatus.NoDate
        val days = ChronoUnit.DAYS.between(today, expiresOn)
        return when {
            days < 0 -> ExpiryStatus.Expired(-days)
            days == 0L -> ExpiryStatus.Today
            days <= SOON_DAYS -> ExpiryStatus.Soon(days)
            else -> ExpiryStatus.Ok(days)
        }
    }

    /** Items for which a reminder is due today and was not sent yet for their current date. */
    fun dueReminders(items: List<InventoryItem>, today: LocalDate): DueReminders {
        val soon = mutableListOf<InventoryItem>()
        val expired = mutableListOf<InventoryItem>()
        items.forEach { item ->
            val date = item.expiresOn ?: return@forEach
            when (status(date, today)) {
                is ExpiryStatus.Today, is ExpiryStatus.Expired ->
                    if (item.notifiedExpiredFor != date) expired += item
                is ExpiryStatus.Soon ->
                    if (item.notifiedSoonFor != date) soon += item
                else -> Unit
            }
        }
        return DueReminders(soon, expired)
    }
}

data class DueReminders(val soon: List<InventoryItem>, val expired: List<InventoryItem>) {
    val isEmpty: Boolean get() = soon.isEmpty() && expired.isEmpty()
}

/** Six-monthly kit review (CVP). */
object KitReview {
    /**
     * The next review date: [intervalMonths] after the last review or, if the kit was never
     * reviewed, after the first item was added. Null while the inventory is empty.
     */
    fun nextReview(lastReviewedOn: LocalDate?, firstAddedOn: LocalDate?, intervalMonths: Int): LocalDate? =
        (lastReviewedOn ?: firstAddedOn)?.plusMonths(intervalMonths.toLong())

    fun isDue(next: LocalDate?, today: LocalDate): Boolean = next != null && !today.isBefore(next)
}
