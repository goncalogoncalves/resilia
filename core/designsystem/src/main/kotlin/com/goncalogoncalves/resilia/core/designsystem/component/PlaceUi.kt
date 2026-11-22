package com.goncalogoncalves.resilia.core.designsystem.component

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Roofing
import androidx.compose.ui.graphics.vector.ImageVector
import com.goncalogoncalves.resilia.core.designsystem.R
import com.goncalogoncalves.resilia.core.domain.model.Compass
import com.goncalogoncalves.resilia.core.domain.model.DistanceAndDirection
import com.goncalogoncalves.resilia.core.domain.model.LatLng
import com.goncalogoncalves.resilia.core.domain.model.PlaceType
import java.util.Locale

@StringRes
fun PlaceType.labelRes(): Int = when (this) {
    PlaceType.HOME -> R.string.ds_place_home
    PlaceType.WORK_SCHOOL -> R.string.ds_place_work_school
    PlaceType.MEETING -> R.string.ds_place_meeting
    PlaceType.HEALTH -> R.string.ds_place_health
    PlaceType.PHARMACY -> R.string.ds_place_pharmacy
    PlaceType.FIREFIGHTERS -> R.string.ds_place_firefighters
    PlaceType.SHELTER -> R.string.ds_place_shelter
    PlaceType.OTHER -> R.string.ds_place_other
}

fun PlaceType.icon(): ImageVector = when (this) {
    PlaceType.HOME -> Icons.Outlined.Home
    PlaceType.WORK_SCHOOL -> Icons.Outlined.Business
    PlaceType.MEETING -> Icons.Outlined.Groups
    PlaceType.HEALTH -> Icons.Outlined.LocalHospital
    PlaceType.PHARMACY -> Icons.Outlined.LocalPharmacy
    PlaceType.FIREFIGHTERS -> Icons.Outlined.LocalFireDepartment
    PlaceType.SHELTER -> Icons.Outlined.Roofing
    PlaceType.OTHER -> Icons.Outlined.Place
}

@StringRes
fun Compass.labelRes(): Int = when (this) {
    Compass.N -> R.string.ds_compass_n
    Compass.NE -> R.string.ds_compass_ne
    Compass.E -> R.string.ds_compass_e
    Compass.SE -> R.string.ds_compass_se
    Compass.S -> R.string.ds_compass_s
    Compass.SW -> R.string.ds_compass_sw
    Compass.W -> R.string.ds_compass_w
    Compass.NW -> R.string.ds_compass_nw
}

/** "2,3 km a sudoeste de casa" / "450 m a norte de casa". */
fun Context.describe(d: DistanceAndDirection): String {
    val locale = resources.configuration.locales.takeIf { !it.isEmpty }?.get(0) ?: Locale.getDefault()
    val distance = if (d.metres < 1000) {
        getString(R.string.ds_distance_m, d.metres)
    } else {
        getString(R.string.ds_distance_km, String.format(locale, "%.1f", d.metres / 1000.0))
    }
    return getString(R.string.ds_from_home, distance, getString(d.direction.labelRes()))
}

/**
 * Opens the person's maps app (decision 4.3): at the coordinates if we have them, otherwise
 * as a search for the address. Returns false when no app can open it.
 */
fun Context.openInMaps(label: String, position: LatLng?, address: String?): Boolean {
    val uri = when {
        position != null -> {
            val ll = "%.6f,%.6f".format(Locale.ROOT, position.lat, position.lng)
            Uri.parse("geo:$ll?q=" + Uri.encode("$ll($label)"))
        }
        !address.isNullOrBlank() -> Uri.parse("geo:0,0?q=" + Uri.encode(address))
        else -> return false
    }
    return try {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

fun Context.copyToClipboard(label: String, text: String) {
    getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText(label, text))
}
