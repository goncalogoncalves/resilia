package com.goncalogoncalves.resilia.feature.alerts

import android.content.Context
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Local times for IPMA data (which is published in UTC). */
internal object AlertFormat {
    private val TIME = DateTimeFormatter.ofPattern("HH:mm")

    private fun locale(context: Context): Locale =
        context.resources.configuration.locales.takeIf { !it.isEmpty }?.get(0) ?: Locale.getDefault()

    private fun date(context: Context, day: LocalDate): String =
        day.format(DateTimeFormatter.ofPattern("EEE, d/M", locale(context)))

    fun time(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String = instant.atZone(zone).format(TIME)

    /** "sáb, 21/11, das 18:00 às 21:00" or "sáb, 21/11 18:00 até dom, 22/11 06:00". */
    fun period(context: Context, start: Instant, end: Instant, zone: ZoneId): String {
        val s = start.atZone(zone)
        val e = end.atZone(zone)
        return if (s.toLocalDate() == e.toLocalDate()) {
            context.getString(R.string.alerts_period_same_day, date(context, s.toLocalDate()), s.format(TIME), e.format(TIME))
        } else {
            context.getString(
                R.string.alerts_period_days,
                date(context, s.toLocalDate()), s.format(TIME),
                date(context, e.toLocalDate()), e.format(TIME),
            )
        }
    }

    fun day(context: Context, day: LocalDate, today: LocalDate = LocalDate.now()): String = when (day) {
        today -> context.getString(R.string.alerts_today)
        today.plusDays(1) -> context.getString(R.string.alerts_tomorrow)
        else -> date(context, day)
    }

    /** "há 2 h" / "há 15 min" for the "downloaded" line. */
    fun ago(context: Context, then: Instant, now: Instant): String {
        val minutes = Duration.between(then, now).toMinutes().coerceAtLeast(0)
        return if (minutes < 1) {
            context.getString(R.string.alerts_ago_now)
        } else if (minutes < 60) {
            context.getString(R.string.alerts_ago_minutes, minutes)
        } else {
            context.getString(R.string.alerts_ago_hours, minutes / 60)
        }
    }
}
