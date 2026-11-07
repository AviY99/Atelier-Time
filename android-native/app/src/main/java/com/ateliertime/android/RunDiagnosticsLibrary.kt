package com.ateliertime.android

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Reports are private app files until the USER selects a SAF destination.
 * Export does not erase a report. Delete requires explicit confirmation.
 */
@Composable
internal fun RunDiagnosticsLibrary(archive: RunDiagnosticsArchive) {
    var reports by remember(archive) { mutableStateOf<List<File>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingExport by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    var pendingDeleteAll by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/x-ndjson")
    ) { uri ->
        val selected = pendingExport
        pendingExport = null
        if (uri != null && selected != null) {
            try {
                archive.export(selected, uri)
                error = null
            } catch (failure: Exception) {
                error = "Export failed: " + (failure.message ?: "Storage error")
            }
        }
    }
    fun refresh() {
        reports = archive.reports()
    }
    LaunchedEffect(archive) { refresh() }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Run diagnostic reports", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { refresh() }) { Text("Refresh") }
            }
            Text("Private, complete reports saved automatically for each Run. " +
                "Export only when you choose a destination; no online upload.",
                style = MaterialTheme.typography.bodySmall)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(
                    "${reports.size} reports · newest first" +
                        if (reports.size > 5) " · scroll for older" else "",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = { pendingDeleteAll = true },
                    enabled = reports.isNotEmpty(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Delete all") }
            }
            if (reports.isEmpty()) {
                Text("No recorded Runs yet.", style = MaterialTheme.typography.bodySmall)
            } else {
                // Keep exactly the most recent five rows in view at normal
                // font scale; ALL older reports remain in the bounded list.
                // Height is bounded because the parent screen also scrolls.
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                ) {
                    items(reports, key = { it.name }) { file ->
                        val millis = file.name.removePrefix("run-")
                            .substringBefore("-").toLongOrNull()
                        val title = millis?.let {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(Date(it))
                        } ?: file.name
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, style = MaterialTheme.typography.labelMedium)
                                Text("${file.length() / 1024L} KB · JSONL",
                                    style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(onClick = {
                                pendingExport = file.name
                                picker.launch(file.name)
                            }) { Text("Export") }
                            TextButton(onClick = { pendingDelete = file.name },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error)) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
    if (pendingDeleteAll) {
        AlertDialog(
            onDismissRequest = { pendingDeleteAll = false },
            title = { Text("Delete all Run diagnostic reports?") },
            text = {
                Text("Permanently delete all ${reports.size} Run diagnostic reports " +
                    "from this device, including older reports not visible in " +
                    "the list? This cannot be undone. Saved Classes and Sound " +
                    "settings will not be affected.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            archive.deleteAll()
                            error = null
                        } catch (failure: Exception) {
                            error = failure.message ?: "Could not delete all reports."
                        }
                        refresh()
                        pendingDeleteAll = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Delete all reports") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteAll = false }) {
                    Text("Cancel")
                }
            },
        )
    }
    pendingDelete?.let { name ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete Run diagnostic report?") },
            text = { Text("This permanently deletes only the selected report. " +
                "Saved Classes and Sound settings are unaffected.") },
            confirmButton = {
                TextButton(onClick = {
                    try { archive.delete(name); refresh(); error = null }
                    catch (failure: Exception) {
                        error = failure.message ?: "Could not delete report."
                    }
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}
