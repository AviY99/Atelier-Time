package com.ateliertime.android

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
        TextButton(colors = atelierTextColors(), onClick = { showHelp = true }) { Text("Help") }
    }
    Text("Run starts the chosen class timer. Edit opens its approved plan " +
        "in Editor, where Save can replace it after confirmation.")
    error?.let { Text(it, color = atelierErrorTextColor()) }
    if (collection.entries.isEmpty()) {
        Text("No saved classes. Create a class in Editor and select Save class.")
    }
    collection.entries.forEach { preset ->
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(preset.title, style = MaterialTheme.typography.titleMedium)
                // The selected Class's commands stay visible on its own card
                // without horizontal scrolling or off-screen management actions.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(colors = atelierFilledColors(), 
                        onClick = { action { onRun(preset, catalog.activate(preset.id)) } },
                        modifier = Modifier.weight(1f),
                    ) { Text("Run", maxLines = 1) }
                    OutlinedButton(colors = atelierOutlinedColors(), 
                        onClick = { action { onEdit(preset, catalog.open(preset.id)) } },
                        modifier = Modifier.weight(1f),
                    ) { Text("Edit", maxLines = 1) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FilledTonalButton(colors = atelierTonalColors(), 
                        onClick = {
                            action {
                                val base = preset.title + " copy"
                                val used = collection.entries.map { it.title.lowercase() }.toSet()
                                val name = if (base.lowercase() !in used) base else
                                    generateSequence(2) { it + 1 }
                                        .map { "$base $it" }
                                        .first { it.lowercase() !in used }
                                catalog.duplicate(preset.id, name)
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) { Text("Duplicate", maxLines = 1, style = MaterialTheme.typography.labelMedium) }
                    OutlinedButton(colors = atelierOutlinedColors(), 
                        onClick = {
                            renameTarget = preset
                            renameText = preset.title
                        },
                        modifier = Modifier.weight(1.1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) { Text("Rename", maxLines = 1, style = MaterialTheme.typography.labelMedium) }
                    TextButton(
                        onClick = { pendingDelete = preset },
                        modifier = Modifier.weight(0.9f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (atelierNightActive()) androidx.compose.ui.graphics.Color.White
                                else MaterialTheme.colorScheme.error,
                        ),
                    ) { Text("Delete", maxLines = 1, style = MaterialTheme.typography.labelMedium) }
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
                TextButton(colors = atelierTextColors(), onClick = { showHelp = false }) { Text("Close") }
            },
        )
    }
    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename class") },
            text = {
                OutlinedTextField(colors = atelierInputColors(), value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Class name") }, singleLine = true)
            },
            confirmButton = {
                TextButton(colors = atelierTextColors(), onClick = {
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
                TextButton(colors = atelierTextColors(), onClick = { renameTarget = null }) { Text("Cancel") }
            },
        )
    }
    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${target.title}?") },
            text = { Text("The saved class will be removed from this phone.") },
            confirmButton = {
                TextButton(colors = atelierTextColors(), onClick = {
                    if (action {
                        catalog.delete(target.id)
                        onDeleted(target.id)
                    }) pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(colors = atelierTextColors(), onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}
