package com.goncalogoncalves.resilia.feature.prepare.plan

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_WEAK
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * The phone's own screen lock (PIN, pattern, password or fingerprint) before showing
 * medical sheets (decision §6.2). Uses only platform APIs (no androidx.biometric, which
 * would pull in AppCompat). The app never sees the PIN or the fingerprint.
 *
 * - Android 10+: system BiometricPrompt, which also accepts the PIN/pattern/password.
 * - Android 8–9: the system "confirm your screen lock" screen.
 */
internal class DeviceLock(private val context: Context, private val launchConfirm: (String, () -> Unit) -> Unit) {

    /** False when the phone has no screen lock: the lock cannot be turned on. */
    val isAvailable: Boolean
        get() = context.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

    fun authenticate(title: String, onSuccess: () -> Unit) {
        if (!isAvailable) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val builder = BiometricPrompt.Builder(context).setTitle(title)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                builder.setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            } else {
                @Suppress("DEPRECATION")
                builder.setDeviceCredentialAllowed(true)
            }
            builder.build().authenticate(
                CancellationSignal(),
                context.mainExecutor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
                },
            )
        } else {
            launchConfirm(title, onSuccess)
        }
    }
}

@Composable
internal fun rememberDeviceLock(): DeviceLock {
    val context = LocalContext.current
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) pending?.invoke()
        pending = null
    }
    return remember(context) {
        DeviceLock(context) { title, onSuccess ->
            @Suppress("DEPRECATION") // replaced by BiometricPrompt on Android 10+, used only below it
            val intent = context.getSystemService(KeyguardManager::class.java)
                ?.createConfirmDeviceCredentialIntent(title, null)
            if (intent == null) {
                onSuccess() // no lock set: nothing to confirm
            } else {
                pending = onSuccess
                launcher.launch(intent)
            }
        }
    }
}
