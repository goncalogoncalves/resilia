package com.goncalogoncalves.resilia.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.SaveAlt
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.goncalogoncalves.resilia.core.designsystem.component.HubCard
import com.goncalogoncalves.resilia.core.designsystem.component.SectionTitle
import kotlinx.serialization.Serializable

@Serializable
data object SettingsRoute

@Serializable
data object LegalRoute

@Serializable
data object AboutRoute

@Serializable
data object BackupRoute

@Serializable
data object PrivacyRoute

fun NavGraphBuilder.settingsScreens(
    onOpenLegal: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpen: (Any) -> Unit = {},
) {
    composable<SettingsRoute> { SettingsScreen(onOpenLegal, onOpenAbout, onOpenAlerts, onOpen) }
    composable<BackupRoute> { BackupScreen() }
    composable<PrivacyRoute> { PrivacyScreen() }
    composable<LegalRoute> { LegalScreen() }
    composable<AboutRoute> { AboutScreen() }
}

@Composable
internal fun SettingsScreen(
    onOpenLegal: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpen: (Any) -> Unit = {},
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { HubCard(stringResource(R.string.settings_alerts), Icons.Outlined.NotificationsActive, onClick = onOpenAlerts) }
        item {
            // Android 13+: the system per-app language screen (decision 4). Older: follows the system.
            val perApp = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            HubCard(
                stringResource(R.string.settings_language),
                Icons.Outlined.Language,
                subtitle = stringResource(if (perApp) R.string.settings_language_subtitle else R.string.settings_language_system),
                onClick = if (perApp) {
                    {
                        try {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_LOCALE_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                            )
                        } catch (_: ActivityNotFoundException) {
                            // Some builds hide the screen: nothing else to do; the app follows the system.
                        }
                    }
                } else {
                    {}
                },
            )
        }
        item {
            HubCard(
                stringResource(R.string.settings_backup),
                Icons.Outlined.SaveAlt,
                subtitle = stringResource(R.string.settings_backup_subtitle),
                onClick = { onOpen(BackupRoute) },
            )
        }
        item { HubCard(stringResource(R.string.settings_privacy), Icons.Outlined.PrivacyTip, onClick = { onOpen(PrivacyRoute) }) }
        item { HubCard(stringResource(R.string.settings_legal), Icons.Outlined.Gavel, onClick = onOpenLegal) }
        item { HubCard(stringResource(R.string.settings_about), Icons.Outlined.Info, onClick = onOpenAbout) }
    }
}

@Composable
internal fun LegalScreen() {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle(stringResource(R.string.disclaimer_title)) }
        item { LegalText() }
    }
}

@Composable
internal fun AboutScreen() {
    val context = LocalContext.current
    val version = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull().orEmpty()
    val appName = context.applicationInfo.loadLabel(context.packageManager).toString()

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("$appName $version") }
        item { Text(stringResource(R.string.about_purpose), style = MaterialTheme.typography.bodyLarge) }
        item { SectionTitle(stringResource(R.string.about_licenses_title)) }
        item { Text(stringResource(R.string.about_licenses), style = MaterialTheme.typography.bodyLarge) }
    }
}
