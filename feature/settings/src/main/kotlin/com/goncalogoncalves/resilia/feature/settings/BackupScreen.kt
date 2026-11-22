package com.goncalogoncalves.resilia.feature.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.component.SectionTitle
import com.goncalogoncalves.resilia.core.domain.model.BackupCodec
import com.goncalogoncalves.resilia.core.domain.model.BackupException
import com.goncalogoncalves.resilia.core.domain.repository.BackupRepository
import com.goncalogoncalves.resilia.core.domain.repository.FamilyPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

/** What the screen is doing or has just done. */
sealed interface BackupStatus {
    data object Idle : BackupStatus
    data object Working : BackupStatus
    data object Exported : BackupStatus
    data object Imported : BackupStatus
    data class Failed(val error: Throwable) : BackupStatus
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backup: BackupRepository,
    plan: FamilyPlanRepository,
) : ViewModel() {

    val hasMedical = plan.members.map { list -> list.any { !it.medical.isEmpty } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _status = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val status = _status.asStateFlow()

    fun export(uri: Uri, password: CharArray?) = run {
        val text = backup.export(password)
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                ?: error("cannot write")
        }
        BackupStatus.Exported
    }

    /** Reads the chosen file; returns its text and whether it needs a password. */
    suspend fun read(uri: Uri): Pair<String, Boolean>? = try {
        val text = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
        } ?: error("cannot read")
        text to backup.isEncrypted(text)
    } catch (e: Exception) {
        _status.value = BackupStatus.Failed(e)
        null
    }

    fun import(text: String, password: CharArray?) = run {
        backup.import(text, password)
        BackupStatus.Imported
    }

    fun clear() {
        _status.value = BackupStatus.Idle
    }

    private fun run(block: suspend () -> BackupStatus) {
        _status.value = BackupStatus.Working
        viewModelScope.launch {
            _status.value = try {
                block()
            } catch (e: Exception) {
                BackupStatus.Failed(e)
            }
        }
    }
}

@Composable
internal fun BackupScreen(viewModel: BackupViewModel = hiltViewModel()) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val hasMedical by viewModel.hasMedical.collectAsStateWithLifecycle()
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    // Import flow: file read → (password) → confirm replace.
    var pendingText by remember { mutableStateOf<String?>(null) }
    var pendingEncrypted by remember { mutableStateOf(false) }
    var importPassword by remember { mutableStateOf("") }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val passwordOk = password.isEmpty() && !hasMedical ||
        password.length >= BackupCodec.MIN_PASSWORD && password == confirm
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) viewModel.export(uri, password.takeIf { it.isNotEmpty() }?.toCharArray())
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                viewModel.read(uri)?.let { (text, encrypted) ->
                    pendingText = text
                    pendingEncrypted = encrypted
                    importPassword = ""
                }
            }
        }
    }
    val working = status == BackupStatus.Working

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(stringResource(R.string.backup_intro), style = MaterialTheme.typography.bodyLarge) }
        // Result near the top (it was below the fold) and announced by TalkBack (live region).
        item {
            val msg = when (val st = status) {
                BackupStatus.Working -> stringResource(R.string.backup_working)
                BackupStatus.Exported -> stringResource(R.string.backup_exported)
                BackupStatus.Imported -> stringResource(R.string.backup_imported)
                is BackupStatus.Failed -> stringResource(st.error.messageRes())
                BackupStatus.Idle -> null
            }
            if (msg != null) {
                Text(
                    msg,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (status is BackupStatus.Failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
        item { SectionTitle(stringResource(R.string.backup_export_title)) }
        item {
            Text(
                stringResource(if (hasMedical) R.string.backup_password_required else R.string.backup_password_optional),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.backup_password)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                supportingText = { Text(pluralStringResource(R.plurals.backup_password_rule, BackupCodec.MIN_PASSWORD, BackupCodec.MIN_PASSWORD)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (password.isNotEmpty()) {
            item {
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text(stringResource(R.string.backup_password_confirm)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = confirm.isNotEmpty() && confirm != password,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Text(stringResource(R.string.backup_password_warning), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }
        item {
            Button(
                onClick = { exportLauncher.launch("resilia-dados-${LocalDate.now()}.json") },
                enabled = passwordOk && !working,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) { Text(stringResource(R.string.backup_export)) }
        }
        item { SectionTitle(stringResource(R.string.backup_import_title)) }
        item { Text(stringResource(R.string.backup_import_note), style = MaterialTheme.typography.bodyMedium) }
        item {
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/plain")) },
                enabled = !working,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) { Text(stringResource(R.string.backup_import)) }
        }
    }

    pendingText?.let { text ->
        val needsPassword = pendingEncrypted
        AlertDialog(
            onDismissRequest = { pendingText = null },
            title = { Text(stringResource(R.string.backup_replace_title)) },
            text = {
                androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.backup_replace_text))
                    if (needsPassword) {
                        OutlinedTextField(
                            value = importPassword,
                            onValueChange = { importPassword = it },
                            label = { Text(stringResource(R.string.backup_password)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !needsPassword || importPassword.isNotEmpty(),
                    onClick = {
                        pendingText = null
                        viewModel.import(text, importPassword.takeIf { needsPassword }?.toCharArray())
                    },
                ) { Text(stringResource(R.string.backup_replace_confirm)) }
            },
            dismissButton = { TextButton(onClick = { pendingText = null }) { Text(stringResource(R.string.backup_cancel)) } },
        )
    }
}

private fun Throwable.messageRes(): Int = when (this) {
    is BackupException.WrongPassword -> R.string.backup_error_password
    is BackupException.NotABackup -> R.string.backup_error_not_backup
    is BackupException.NewerVersion -> R.string.backup_error_newer
    is BackupException.MedicalNeedsPassword -> R.string.backup_password_required
    else -> R.string.backup_error_generic
}
