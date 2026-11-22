package com.goncalogoncalves.resilia.core.domain.device

import kotlinx.coroutines.flow.StateFlow

/** Device flashlight. Implemented without the CAMERA permission (torch mode only). */
interface Torch {
    val isAvailable: Boolean
    val isOn: StateFlow<Boolean>

    /** Returns false if the torch could not be switched (e.g. camera in use). */
    fun set(on: Boolean): Boolean
}
