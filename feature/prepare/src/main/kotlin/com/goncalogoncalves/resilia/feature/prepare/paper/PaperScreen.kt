package com.goncalogoncalves.resilia.feature.prepare.paper

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.designsystem.component.DisclaimerFooter
import com.goncalogoncalves.resilia.core.designsystem.component.currentContentLanguage
import com.goncalogoncalves.resilia.core.domain.model.ContentLanguage
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import com.goncalogoncalves.resilia.core.domain.repository.FamilyPlanRepository
import com.goncalogoncalves.resilia.core.domain.repository.PreparednessRepository
import com.goncalogoncalves.resilia.feature.prepare.R
import com.goncalogoncalves.resilia.feature.prepare.plan.rememberDeviceLock
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import com.goncalogoncalves.resilia.core.domain.repository.PlacesRepository
import javax.inject.Inject

data class PaperUiState(
    /** Someone chose to print their medical sheet on the A4 card. */
    val medicalOnPaper: Boolean,
    val medicalLock: Boolean,
)

@HiltViewModel
class PaperViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val content: ContentRepository,
    private val plan: FamilyPlanRepository,
    private val preparedness: PreparednessRepository,
    private val places: PlacesRepository,
) : ViewModel() {

    val uiState = combine(plan.members, preparedness.state) { members, prep ->
        PaperUiState(members.any { it.medicalOnPaper && !it.medical.isEmpty }, prep.medicalLock)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Writes the PDF to the app's cache (never backed up; replaced every time). */
    internal suspend fun generate(kind: PaperKind, language: ContentLanguage): File = withContext(Dispatchers.IO) {
        val data = PaperData(
            bundle = content.load(),
            language = language,
            plan = plan.plan.first(),
            members = plan.members.first(),
            contacts = plan.contacts.first(),
            places = places.places.first(),
            household = preparedness.state.first().household,
            today = LocalDate.now(),
        )
        val dir = File(context.cacheDir, PAPER_DIR).apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        File(dir, kind.fileName).also { PaperDocuments.write(context, kind, data, it) }
    }

    internal fun copyTo(file: File, uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } }
    }

    companion object {
        const val PAPER_DIR = "paper"
    }
}

private enum class PaperAction { PRINT, SHARE, SAVE }

@Composable
internal fun PaperScreen(viewModel: PaperViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val language = currentContentLanguage()
    val unlockTitle = stringResource(R.string.plan_unlock_title)
    val lock = rememberDeviceLock()
    var pendingSave by remember { mutableStateOf<File?>(null) }
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val file = pendingSave
        if (uri != null && file != null) viewModel.copyTo(file, uri)
        pendingSave = null
    }
    val s = state ?: return

    fun run(kind: PaperKind, action: PaperAction) {
        val go = {
            scope.launch {
                val file = viewModel.generate(kind, language)
                when (action) {
                    PaperAction.PRINT -> print(context, file)
                    PaperAction.SHARE -> share(context, file)
                    PaperAction.SAVE -> {
                        pendingSave = file
                        saveLauncher.launch(kind.fileName)
                    }
                }
            }
            Unit
        }
        // The A4 card may carry medical sheets: ask for the screen lock first if it is on.
        if (kind == PaperKind.A4 && s.medicalOnPaper && s.medicalLock && lock.isAvailable) lock.authenticate(unlockTitle) { go() } else go()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(stringResource(R.string.paper_intro), style = MaterialTheme.typography.bodyLarge) }
        item {
            PaperCard(R.string.paper_a4, R.string.paper_a4_desc) { run(PaperKind.A4, it) }
        }
        if (s.medicalOnPaper) {
            item {
                Text(
                    stringResource(R.string.plan_medical_on_paper_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        item { PaperCard(R.string.paper_wallet, R.string.paper_wallet_desc) { run(PaperKind.WALLET, it) } }
        item { PaperCard(R.string.paper_booklet, R.string.paper_booklet_desc) { run(PaperKind.BOOKLET, it) } }
        item { Text(stringResource(R.string.paper_privacy), style = MaterialTheme.typography.bodyMedium) }
        item { DisclaimerFooter() }
    }
}

@Composable
private fun PaperCard(title: Int, description: Int, onAction: (PaperAction) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            Text(stringResource(description), style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = { onAction(PaperAction.PRINT) }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.Print, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.paper_print))
                }
                OutlinedButton(onClick = { onAction(PaperAction.SHARE) }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.paper_share))
                }
                OutlinedButton(onClick = { onAction(PaperAction.SAVE) }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.paper_save))
                }
            }
        }
    }
}

private fun print(context: Context, file: File) {
    val manager = context.getSystemService(PrintManager::class.java) ?: return
    manager.print(file.nameWithoutExtension, FilePrintAdapter(file), PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build())
}

private fun share(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, context.packageName + ".paper", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
    } catch (_: ActivityNotFoundException) {
        // Nothing can receive a PDF: printing and saving still work.
    }
}

/** Hands an already-written PDF to the Android print service. */
private class FilePrintAdapter(private val file: File) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?, newAttributes: PrintAttributes?, cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback, extras: Bundle?,
    ) {
        if (cancellationSignal?.isCanceled == true) return callback.onLayoutCancelled()
        callback.onLayoutFinished(
            PrintDocumentInfo.Builder(file.name).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build(),
            true,
        )
    }

    override fun onWrite(
        pages: Array<out PageRange>?, destination: ParcelFileDescriptor, cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback,
    ) {
        try {
            file.inputStream().use { input ->
                ParcelFileDescriptor.AutoCloseOutputStream(destination).use { input.copyTo(it) }
            }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback.onWriteFailed(e.message)
        }
    }
}
