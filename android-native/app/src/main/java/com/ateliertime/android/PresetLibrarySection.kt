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
 * The Timer's HOME (no class running): manage only SAVED approved classes.
 * Editing is deliberately in Editor, not inside the running clock.
 * All storage operations use the existing signed, offline PresetCatalog.
 */
@Composable
fun PresetLibrarySection(
    catalog: PresetCatalog,
    refreshKey: Int,
    onEdit: (StoredPreset, ApprovedSession) -> Unit,
    onRun: (StoredPreset, ApprovedSession) -> Unit,
    onDeleted: (String) -> Unit,
) {
    var collection by remember(catalog) { mutableStateOf(PresetCollection()) }
    var error by remember { mutableStateOf<String?>(null) }
    var renameTarget by remember { mutableStateOf<StoredPreset?>(null) }
    var renameText by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<StoredPreset?>(null) }
    var showHelp by remember { mutableStateOf(false) }

    fun refresh() {
        try {
            collection = catalog.state()
            error = null
        } catch (failure: Exception) {
            error = failure.message ?: "Could not read saved classes."
        }
    }
    fun action(block: () -> Unit): Boolean = try {
        block()
        refresh()
        true
    } catch (failure: Exception) {
        error = failure.message ?: "Saved class operation failed."
        false
    }

    LaunchedEffect(catalog, refreshKey) { refresh() }
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("Saved classes", style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f))
        TextButton(onClick = { showHelp = true }) { Text("Help") }
    }
    Text("Run starts the chosen class timer. Edit opens its approved plan " +
        "in Editor, where Save can replace it after confirmation.")
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    if (collection.entries.isEmpty()) {
        Text("No saved classes. Create a class in Editor and select Save class.")
    }
    collection.entries.forEach { preset ->
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(preset.title, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    Button(onClick = {
                        action { onRun(preset, catalog.activate(preset.id)) }
                    }) { Text("Run") }
                    OutlinedButton(onClick = {
                        action { onEdit(preset, catalog.open(preset.id)) }
                    }) { Text("Edit") }
                    TextButton(onClick = {
                        action {
                            val base = preset.title + " copy"
                            val used = collection.entries.map { it.title.lowercase() }.toSet()
                            val name = if (base.lowercase() !in used) base else
                                generateSequence(2) { it + 1 }
                                    .map { "$base $it" }
                                    .first { it.lowercase() !in used }
                            catalog.duplicate(preset.id, name)
                        }
                    }) { Text("Duplicate") }
                    TextButton(onClick = {
                        renameTarget = preset
                        renameText = preset.title
                    }) { Text("Rename") }
                    TextButton(onClick = { pendingDelete = preset }) { Text("Delete") }
                }
            }
        }
    }
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Managing saved classes") },
            text = { Text("Run starts the class in Timer. Edit opens its plan in " +
                "Editor and requires Save before changes are stored. Duplicate " +
                "makes a separate saved copy. Rename changes only the class name; " +
                "Delete permanently removes the selected class after confirmation.") },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text("Close") }
            },
        )
    }
    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename class") },
            text = {
                OutlinedTextField(value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Class name") }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    if (action {
                        require(renameText.isNotBlank()) { "Enter a class name." }
                        require(collection.entries.none {
                            it.id != target.id &&
                                it.title.equals(renameText.trim(), ignoreCase = true)
                        }) { "Another saved class already uses that name." }
                        catalog.rename(target.id, renameText)
                    }) renameTarget = null
                }) { Text("Save name") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            },
        )
    }
    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${target.title}?") },
            text = { Text("The saved class will be removed from this phone.") },
            confirmButton = {
                TextButton(onClick = {
                    if (action {
                        catalog.delete(target.id)
                        onDeleted(target.id)
                    }) pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}
