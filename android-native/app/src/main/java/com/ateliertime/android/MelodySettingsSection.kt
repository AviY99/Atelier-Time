package com.ateliertime.android

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ateliertime.domain.*

/**
 * Main-editor global audible-cue selection. Three independent dropdown menus
 * with five-second previews, persisted privately without rewriting presets.
 */
@Composable
fun MelodySettingsSection(
    selections: MelodySelections,
    onSelectionsChange: (MelodySelections) -> Unit,
    onPreview: (MelodyPattern) -> Unit,
) {
    var showHelp by remember { mutableStateOf(false) }
    HorizontalDivider()
    Row {
        Text("Five-second sound melodies",
            style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        TextButton(onClick = { showHelp = true }) { Text("Help") }
    }
    Text("Upbeat five-second tunes with clear rhythms. Choose one melody for " +
        "each timer event; your choices are saved on this phone.")
    MelodyPicker("A · End of each pose", selections.poseEnd, { selected ->
        onSelectionsChange(selections.copy(poseEnd = selected))
    }, onPreview)
    MelodyPicker("B · End of each break", selections.breakEnd, { selected ->
        onSelectionsChange(selections.copy(breakEnd = selected))
    }, onPreview)
    MelodyPicker("C · End of entire session", selections.sessionEnd, { selected ->
        onSelectionsChange(selections.copy(sessionEnd = selected))
    }, onPreview)
    if (showHelp) AlertDialog(
        onDismissRequest = { showHelp = false },
        title = { Text("Sound settings") },
        text = { Text("Each timer event plays its selected five-second melody, using quick " +
            "notes, rhythmic rests and an accented beat. Preview uses the same " +
            "audio as the timer; volume follows Android media volume. " +
            "Change tunes here while the session runs; your approved timeline is unchanged.") },
        confirmButton = {
            TextButton(onClick = { showHelp = false }) { Text("Close") }
        },
    )
}

@Composable
private fun MelodyPicker(
    label: String,
    choice: MelodyPattern,
    onSelected: (MelodyPattern) -> Unit,
    onPreview: (MelodyPattern) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Text(label, style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()) {
                Text(choice.title, maxLines = 1)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                MelodyPattern.entries.forEach { melody ->
                    DropdownMenuItem(
                        text = { Text(melody.title) },
                        onClick = { onSelected(melody); expanded = false },
                    )
                }
            }
        }
        OutlinedButton(onClick = { onPreview(choice) }) { Text("Play 5s") }
    }
}
