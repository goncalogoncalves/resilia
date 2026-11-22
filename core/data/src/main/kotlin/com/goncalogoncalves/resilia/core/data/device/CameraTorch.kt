package com.goncalogoncalves.resilia.core.data.device

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.goncalogoncalves.resilia.core.domain.device.Torch
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uses [CameraManager.setTorchMode], which does NOT need the CAMERA permission.
 * A torch callback keeps [isOn] in sync if the system or another app changes it.
 */
@Singleton
class CameraTorch @Inject constructor(
    @ApplicationContext context: Context,
) : Torch {

    private val manager = context.getSystemService(CameraManager::class.java)
    private val cameraId: String? = runCatching {
        manager?.cameraIdList?.firstOrNull { id ->
            manager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }.getOrNull()

    private val _isOn = MutableStateFlow(false)
    override val isOn: StateFlow<Boolean> = _isOn.asStateFlow()
    override val isAvailable: Boolean get() = cameraId != null

    init {
        if (cameraId != null) {
            manager?.registerTorchCallback(
                object : CameraManager.TorchCallback() {
                    override fun onTorchModeChanged(id: String, enabled: Boolean) {
                        if (id == cameraId) _isOn.value = enabled
                    }

                    override fun onTorchModeUnavailable(id: String) {
                        if (id == cameraId) _isOn.value = false
                    }
                },
                null,
            )
        }
    }

    override fun set(on: Boolean): Boolean {
        val id = cameraId ?: return false
        return try {
            manager?.setTorchMode(id, on)
            _isOn.value = on
            true
        } catch (_: CameraAccessException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}
