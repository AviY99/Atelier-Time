package com.ateliertime.android

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ateliertime.domain.*
import kotlin.math.roundToInt

/**
 * One global list of enabled sound sources; independent A/B/C choices.
 * Each enabled source appears in every event's selection menu.
 */
@Composable
fun MelodySettingsSection(
    settings: SoundSettings,
    onSettingsChange: (SoundSettings) -> Unit,
    onChooseSystemSound: (TimerCue) -> Unit,
    onChooseAudioFile: (TimerCue) -> Unit,
    onPreview: (TimerCue) -> Unit,
    onError: (String) -> Unit,
) {
    var showHelp by remember { mutableStateOf(false) }
    HorizontalDivider()
    Row {
        Text("Sound choices", style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f))
        TextButton(colors = atelierTextColors(), onClick = { showHelp = true }) {
            Text("Help")
        }
    }
    Text("Choose one or more available sound libraries. Atelier Time melodies " +
        "are enabled by default. Every enabled library appears under A, B and C.")
    SoundSource.entries.forEach { source ->
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(
                checked = source in settings.enabledSources,
                onCheckedChange = { enabled ->
                    runCatching { settings.setSourceEnabled(source, enabled) }
                        .onSuccess(onSettingsChange)
                        .onFailure { onError(it.message ?: "Cannot disable this sound source.") }
                },
            )
            Text(source.title)
        }
    }
    SoundPicker("A · End of each session", TimerCue.POSE_FINISHED, settings,
        onSettingsChange, onChooseSystemSound, onChooseAudioFile, onPreview)
    DurationChips("A · Session sound length", TimerCue.POSE_FINISHED,
        settings, onSettingsChange)
    SoundPicker("B · End of each break", TimerCue.BREAK_FINISHED, settings,
        onSettingsChange, onChooseSystemSound, onChooseAudioFile, onPreview)
    DurationChips("B · Break sound length", TimerCue.BREAK_FINISHED,
        settings, onSettingsChange)
    SoundPicker("C · End of class", TimerCue.SESSION_FINISHED, settings,
        onSettingsChange, onChooseSystemSound, onChooseAudioFile, onPreview)
    var sliderSeconds by remember(settings.classEndSeconds) {
        mutableFloatStateOf(settings.classEndSeconds.toFloat())
    }
    Text("C · Class end sound length: ${sliderSeconds.roundToInt()} seconds",
        style = MaterialTheme.typography.titleSmall)
    Slider(
        value = sliderSeconds,
        onValueChange = { sliderSeconds = it.roundToInt().coerceIn(5, 60).toFloat() },
        onValueChangeFinished = {
            onSettingsChange(settings.setDuration(
                TimerCue.SESSION_FINISHED, sliderSeconds.roundToInt()))
        },
        valueRange = 5f..60f,
        steps = 54, // All integer-second positions, including both endpoints.
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("5 seconds", style = MaterialTheme.typography.bodySmall)
        Text("60 seconds", style = MaterialTheme.typography.bodySmall)
    }
    if (showHelp) AlertDialog(
        onDismissRequest = { showHelp = false },
        title = { Text("Sound settings") },
        text = { Text("Enable one or several libraries: Atelier Time, Android " +
            "system sounds and audio files. Select one sound separately for " +
            "each timer cue. Choosing System opens Android's sound picker; " +
            "choosing File opens the document picker. Saved files remain on " +
            "your device and must remain accessible. All enabled categories " +
            "appear in each dropdown. A and B play for 5 or 10 seconds; " +
            "Class end C plays for 5–60 seconds. Short sounds repeat to fill " +
            "the chosen time. Preview uses exactly the same sound and duration " +
            "as the timer. These settings never change approved Class schedules.") },
        confirmButton = {
            TextButton(colors = atelierTextColors(), onClick = { showHelp = false }) {
                Text("Close")
            }
        },
    )
}

@Composable
private fun SoundPicker(
    label: String,
    cue: TimerCue,
    settings: SoundSettings,
    onSettingsChange: (SoundSettings) -> Unit,
    onChooseSystemSound: (TimerCue) -> Unit,
    onChooseAudioFile: (TimerCue) -> Unit,
    onPreview: (TimerCue) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val choice = settings.soundFor(cue)
    Text(label, style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(colors = atelierOutlinedColors(), onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()) {
                Text("${choice.source.title} · ${choice.title}", maxLines = 1)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (SoundSource.APP in settings.enabledSources) {
                    MelodyPattern.entries.forEach { melody ->
                        DropdownMenuItem(
                            text = { Text("Atelier Time · ${melody.title}") },
                            onClick = {
                                onSettingsChange(settings.choose(cue,
                                    CueSound(source = SoundSource.APP, melody = melody)))
                                expanded = false
                            },
                        )
                    }
                }
                if (SoundSource.SYSTEM in settings.enabledSources) {
                    DropdownMenuItem(
                        text = { Text("Choose Android system sound…") },
                        onClick = { expanded = false; onChooseSystemSound(cue) },
                    )
                    settings.savedChoices(SoundSource.SYSTEM).forEach { saved ->
                        DropdownMenuItem(
                            text = { Text("System · ${saved.title}") },
                            onClick = {
                                onSettingsChange(settings.choose(cue, saved))
                                expanded = false
                            },
                        )
                    }
                }
                if (SoundSource.FILE in settings.enabledSources) {
                    DropdownMenuItem(
                        text = { Text("Choose audio file…") },
                        onClick = { expanded = false; onChooseAudioFile(cue) },
                    )
                    settings.savedChoices(SoundSource.FILE).forEach { saved ->
                        DropdownMenuItem(
                            text = { Text("File · ${saved.title}") },
                            onClick = {
                                onSettingsChange(settings.choose(cue, saved))
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
        OutlinedButton(colors = atelierOutlinedColors(), onClick = { onPreview(cue) }) {
            Text("Play ${settings.secondsFor(cue)}s")
        }
    }
}

private fun SoundSettings.savedChoices(source: SoundSource): List<CueSound> =
    listOf(poseEnd, breakEnd, classEnd)
        .filter { it.source == source && it.isPlayable }
        .distinctBy { it.uri }

@Composable
private fun DurationChips(
    label: String,
    cue: TimerCue,
    settings: SoundSettings,
    onSettingsChange: (SoundSettings) -> Unit,
) {
    Text(label, style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(5, 10).forEach { seconds ->
            FilterChip(
                colors = atelierChipColors(),
                selected = settings.secondsFor(cue) == seconds,
                onClick = { onSettingsChange(settings.setDuration(cue, seconds)) },
                label = { Text("${seconds}s") },
            )
        }
    }
}
