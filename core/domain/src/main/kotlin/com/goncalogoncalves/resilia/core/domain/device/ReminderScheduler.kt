package com.goncalogoncalves.resilia.core.domain.device

/** Runs the daily expiry/review check in the background while reminders are on. */
interface ReminderScheduler {
    fun setEnabled(enabled: Boolean)
}
