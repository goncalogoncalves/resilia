package com.goncalogoncalves.resilia.core.domain

/**
 * Single source for the app's name outside Android resources.
 * The only other place it may appear is `app_name` in strings.xml
 * (CI checks this — see scripts/check-app-name.sh).
 */
object AppInfo {
    const val NAME = "Resilia"
}
