package com.goncalogoncalves.resilia.core.domain

/**
 * Bump [VERSION] whenever the disclaimer text changes materially:
 * users are then asked to accept it again.
 */
object Disclaimer {
    const val VERSION = 1

    fun mustShow(acceptedVersion: Int): Boolean = acceptedVersion < VERSION
}

/** European emergency number. The only phone number hard-coded in the app. */
object EmergencyNumbers {
    const val EUROPEAN_EMERGENCY = "112"
}
