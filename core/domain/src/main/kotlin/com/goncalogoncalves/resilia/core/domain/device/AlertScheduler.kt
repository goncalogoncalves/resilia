package com.goncalogoncalves.resilia.core.domain.device

import com.goncalogoncalves.resilia.core.domain.model.AlertSettings

/** Runs the IPMA check in the background (WorkManager, only with network) while a zone is chosen. */
fun interface AlertScheduler {
    fun schedule(settings: AlertSettings)
}
