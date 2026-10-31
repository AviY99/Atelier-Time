package com.ateliertime.android

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ateliertime.domain.*
import kotlin.math.roundToInt

/**
 * Compact Sound tab: library checkboxes in one row, two explicit sound modes.
 * SHARED shows only one picker and one 5/10s duration; separate mode alone
 * exposes the original independent A/B/C pickers and lengths.
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Sound choices", style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f))
        TextButton(colors = atelierTextColors(), onClick = { showHelp = true }) {
            Text("Help")
        }
    }

    // One compact horizontal checkbox strip rather than three tall rows.
    Text("Available libraries", style = MaterialTheme.typography.titleSmall)
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        SoundSource.entries.forEach { source ->
            Column(modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Checkbox(
                    checked = source in settings.enabledSources,
                    modifier = Modifier.size(36.dp)
                        .semantics { contentDescription = source.title },
                    onCheckedChange = { enabled ->
                        runCatching { settings.setSourceEnabled(source, enabled) }
                            .onSuccess(onSettingsChange)
                            .onFailure {
                                onError(it.message ?: "Cannot disable this sound source.")
                            }
                    },
                )
                Text(
                    when (source) {
                        SoundSource.APP -> "Atelier Time"
                        SoundSource.SYSTEM -> "OS sounds"
                        SoundSource.FILE -> "Audio files"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    // Both choices are visible side-by-side; changes never overwrite
    // saved separate settings or the shared sound/duration.
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(
            SoundAssignmentMode.SHARED to "Same sound for all",
            SoundAssignmentMode.SEPARATE to "Separate A/B/C/Free",
        ).forEach { (mode, title) ->
            FilterChip(
                colors = atelierChipColors(),
                modifier = Modifier.weight(1f),
                selected = settings.assignmentMode == mode,
                onClick = { onSettingsChange(settings.setAssignmentMode(mode)) },
                label = {
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
            )
        }
    }

    if (settings.assignmentMode == SoundAssignmentMode.SHARED) {
        SoundPicker(
            cue = TimerCue.POSE_FINISHED, settings = settings,
            onSettingsChange = onSettingsChange,
            onChooseSystemSound = onChooseSystemSound,
            onChooseAudioFile = onChooseAudioFile,
            onPreview = onPreview,
        )
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Class A/B/C · Length", style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f))
            listOf(5, 10).forEach { seconds ->
                FilterChip(
                    colors = atelierChipColors(),
                    selected = settings.sharedSeconds == seconds,
                    onClick = { onSettingsChange(settings.setSharedDuration(seconds)) },
                    label = { Text("${seconds}s") },
                )
            }
        }
    } else {
        CompactCue(
            title = "A · End of session", cue = TimerCue.POSE_FINISHED,
            settings = settings, onSettingsChange = onSettingsChange,
            onChooseSystemSound = onChooseSystemSound,
            onChooseAudioFile = onChooseAudioFile, onPreview = onPreview,
        )
        CompactCue(
            title = "B · End of break", cue = TimerCue.BREAK_FINISHED,
            settings = settings, onSettingsChange = onSettingsChange,
            onChooseSystemSound = onChooseSystemSound,
            onChooseAudioFile = onChooseAudioFile, onPreview = onPreview,
        )
        var sliderSeconds by remember(settings.classEndSeconds) {
            mutableFloatStateOf(settings.classEndSeconds.toFloat())
        }
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            Text("C · End of class", style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f))
            Text("${sliderSeconds.roundToInt()}s",
                style = MaterialTheme.typography.titleSmall)
        }
        SoundPicker(
            cue = TimerCue.SESSION_FINISHED, settings = settings,
            onSettingsChange = onSettingsChange,
            onChooseSystemSound = onChooseSystemSound,
            onChooseAudioFile = onChooseAudioFile, onPreview = onPreview,
        )
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            Text("5s", style = MaterialTheme.typography.labelSmall)
            AtelierSlimSlider(
                value = sliderSeconds,
                onValueChange = {
                    sliderSeconds = it.roundToInt().coerceIn(5, 60).toFloat()
                },
                onValueChangeFinished = {
                    onSettingsChange(settings.setDuration(
                        TimerCue.SESSION_FINISHED, sliderSeconds.roundToInt()))
                },
                valueRange = 5f..60f,
                steps = 54,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    .testTag("sound_class_end_length_slider")
                    .semantics {
                        contentDescription = "Class C end sound length, ${sliderSeconds.roundToInt()} seconds"
                    },
            )
            Text("60s", style = MaterialTheme.typography.labelSmall)
        }
    }

    HorizontalDivider()
    Text("Free countdown · completion sound",
        style = MaterialTheme.typography.titleSmall)
    if (settings.assignmentMode != SoundAssignmentMode.SHARED) {
        SoundPicker(
            cue = TimerCue.FREE_TIMER_FINISHED, settings = settings,
            onSettingsChange = onSettingsChange,
            onChooseSystemSound = onChooseSystemSound,
            onChooseAudioFile = onChooseAudioFile,
            onPreview = onPreview,
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Free timer sound length", style = MaterialTheme.typography.labelMedium)
        Text("${settings.freeTimerSeconds}s")
    }
    AtelierSlimSlider(
        value = settings.freeTimerSeconds.toFloat(),
        onValueChange = {
            onSettingsChange(settings.setDuration(TimerCue.FREE_TIMER_FINISHED,
                it.roundToInt().coerceIn(5, 60)))
        },
        valueRange = 5f..60f, steps = 54,
        modifier = Modifier.testTag("sound_free_length_slider")
            .semantics {
                contentDescription = "Free countdown completion sound length, ${settings.freeTimerSeconds} seconds"
            },
    )
    HorizontalDivider()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Use alarm volume for Class A/B/C",
                style = MaterialTheme.typography.titleSmall)
        }
        Switch(checked = settings.classAlertsUseAlarmVolume,
            onCheckedChange = {
                onSettingsChange(settings.copy(classAlertsUseAlarmVolume = it))
            })
    }

    if (showHelp) AlertDialog(
        onDismissRequest = { showHelp = false },
        title = { Text("Sound settings") },
        text = {
            Text("Enable one or more libraries. Use 'Same sound for all' " +
                "for a single A/B/C sound with one 5 or 10-second length. " +
                "Choose 'Separate A/B/C' for independent sounds, A/B lengths " +
                "of 5 or 10 seconds and a Class-end C length of 5–60 seconds. " +
                "You can switch modes without losing previous choices. " +
                "System and file choices use Android pickers; files must " +
                "remain accessible. Short sounds repeat for the selected time. " +
                "Same sound for all includes the Free countdown melody. " +
                "Separate restores its individual choice. Its completion " +
                "length remains independently adjustable. " +
                "Class cues may optionally use the alarm volume stream. " +
                "Changing sound preferences never changes approved Classes.")
        },
        confirmButton = {
            TextButton(colors = atelierTextColors(), onClick = { showHelp = false }) {
                Text("Close")
            }
        },
    )
}

@Composable
private fun CompactCue(
    title: String,
    cue: TimerCue,
    settings: SoundSettings,
    onSettingsChange: (SoundSettings) -> Unit,
    onChooseSystemSound: (TimerCue) -> Unit,
    onChooseAudioFile: (TimerCue) -> Unit,
    onPreview: (TimerCue) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f), maxLines = 1)
        listOf(5, 10).forEach { seconds ->
            FilterChip(
                colors = atelierChipColors(),
                selected = settings.secondsFor(cue) == seconds,
                onClick = { onSettingsChange(settings.setDuration(cue, seconds)) },
                label = { Text("${seconds}s") },
            )
        }
    }
    SoundPicker(cue, settings, onSettingsChange,
        onChooseSystemSound, onChooseAudioFile, onPreview)
}

@Composable
private fun SoundPicker(
    cue: TimerCue,
    settings: SoundSettings,
    onSettingsChange: (SoundSettings) -> Unit,
    onChooseSystemSound: (TimerCue) -> Unit,
    onChooseAudioFile: (TimerCue) -> Unit,
    onPreview: (TimerCue) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val choice = settings.soundFor(cue)
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(colors = atelierOutlinedColors(), onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()) {
                Text("${choice.source.title} · ${choice.title}",
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (SoundSource.APP in settings.enabledSources) {
                    MelodyPattern.entries.forEach { melody ->
                        DropdownMenuItem(
                            text = { Text("Atelier Time · ${melody.title}") },
                            onClick = {
                                onSettingsChange(settings.chooseForVisibleMode(cue,
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
                                onSettingsChange(settings.chooseForVisibleMode(cue, saved))
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
                                onSettingsChange(settings.chooseForVisibleMode(cue, saved))
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
        OutlinedButton(colors = atelierOutlinedColors(), onClick = { onPreview(cue) }) {
            Text("Play ${settings.secondsFor(cue)}s", maxLines = 1)
        }
    }
}

private fun SoundSettings.savedChoices(source: SoundSource): List<CueSound> =
    (listOf(poseEnd, breakEnd, classEnd, freeTimer) + listOfNotNull(sharedSound))
        .filter { it.source == source && it.isPlayable }
        .distinctBy { it.uri }
