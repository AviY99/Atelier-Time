package com.ateliertime.android

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ateliertime.domain.*

/**
 * Offline preset actions. Only an approved timeline can be saved. Every read
 * validates the saved signature with the current scheduling engine.
 * Activate selects a stored approved plan but does not start a timer yet.
 */
@Composable
fun PresetLibrarySection(
    catalog: PresetCatalog,
    approved: ApprovedSession?,
    selectedId: String?,
    suggestedName: String,
    onSelected: (StoredPreset, ApprovedSession) -> Unit,
    onSaved: (StoredPreset) -> Unit,
    onDeleted: (String) -> Unit,
) {
    var collection by remember(catalog) { mutableStateOf(PresetCollection()) }
    var error by remember { mutableStateOf<String?>(null) }
    var renameId by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    var showHelp by remember { mutableStateOf(false) }

    fun refresh() {
        try {
            collection = catalog.state()
            error = null
        } catch (failure: Exception) {
            error = failure.message ?: "Cannot read local sessions."
        }
    }
    fun action(block: () -> Unit): Boolean {
        return try {
            block()
            refresh()
            true
        } catch (failure: Exception) {
            error = failure.message ?: "Local session operation failed."
            false
        }
    }

    LaunchedEffect(catalog) { refresh() }

    HorizontalDivider()
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text("Saved session presets", style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f))
        TextButton(onClick = { showHelp = true }) { Text("Help") }
    }
    Text("Only an approved session timeline can be saved. Presets are stored offline.")
    if (error != null) {
        Text(
            error ?: "",
            color = MaterialTheme.colorScheme.error,
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            enabled = approved != null && selectedId == null,
            onClick = {
                val session = approved ?: return@Button
                action {
                    val saved = catalog.save(session, suggestedName)
                    onSaved(saved)
                }
            },
        ) { Text("Save approved session") }
        OutlinedButton(
            enabled = approved != null && selectedId != null,
            onClick = {
                val session = approved ?: return@OutlinedButton
                action {
                    val saved = catalog.save(session, suggestedName, selectedId)
                    onSaved(saved)
                }
            },
        ) { Text("Overwrite saved preset") }
        OutlinedButton(
            enabled = approved != null,
            onClick = {
                val session = approved ?: return@OutlinedButton
                action {
                    val saved = catalog.save(session, suggestedName)
                    onSaved(saved)
                }
            },
        ) { Text("Save as new preset") }
    }
    if (collection.entries.isEmpty()) Text("No saved sessions yet.")
    collection.entries.forEach { preset ->
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(preset.title, style = MaterialTheme.typography.titleMedium)
                if (collection.activeId == preset.id) {
                    Text("Selected preset (load to open its timer)")
                }
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = {
                        action { onSelected(preset, catalog.open(preset.id)) }
                    }) { Text("Load & edit") }
                    TextButton(onClick = {
                        action {
                            val selected = catalog.activate(preset.id)
                            onSelected(preset, selected)
                        }
                    }) { Text("Select preset") }
                    TextButton(onClick = {
                        action {
                            catalog.duplicate(preset.id, preset.title + " copy")
                        }
                    }) { Text("Duplicate") }
                    TextButton(onClick = {
                        renameId = preset.id
                        renameText = preset.title
                    }) { Text("Rename") }
                    TextButton(onClick = { pendingDelete = preset.id }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Saved session presets") },
            text = {
                Text("Save an explicitly approved timeline on this phone. " +
                    "Load & edit restores that exact approved session into the editor. " +
                    "Select preset marks it for timer use but does not start a clock; " +
                    "open the timer separately when ready. Duplicating or renaming " +
                    "preserves the approved timeline.")
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text("Close") }
            },
        )
    }
    val renameTarget = renameId
    if (renameTarget != null) {
        AlertDialog(
            onDismissRequest = { renameId = null },
            title = { Text("Rename saved session") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("New name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (action {
                        val renamed = catalog.rename(renameTarget, renameText)
                        if (selectedId == renameTarget) {
                            onSelected(renamed, catalog.open(renameTarget))
                        }
                    }) {
                        renameId = null
                    }
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { renameId = null }) { Text("Cancel") }
            },
        )
    }
    val deleteTarget = pendingDelete
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete saved session?") },
            text = { Text("This removes the stored preset from the phone.") },
            confirmButton = {
                TextButton(onClick = {
                    action {
                        catalog.delete(deleteTarget)
                        onDeleted(deleteTarget)
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
