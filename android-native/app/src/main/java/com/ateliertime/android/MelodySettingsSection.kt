package com.ateliertime.android

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ateliertime.domain.*
import kotlin.math.roundToInt

/**
 * Compact Sound tab: library checkboxes in one row, two explicit sound modes.
 * SHARED offers one sound and unified 5s / 10s / Custom duration for
 * Class A/B/C and Free, with per-sound gain in Custom; Separate restores
 * original A/B/C/Free pickers and saved lengths.
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
            SoundAssignmentMode.SEPARATE to "Separate sounds",
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
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Sound length · all", style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f), maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            listOf(SharedDurationMode.FIVE, SharedDurationMode.TEN,
                SharedDurationMode.CUSTOM).forEach { mode ->
                FilterChip(
                    colors = atelierChipColors(),
                    selected = settings.sharedDurationMode == mode,
                    onClick = {
                        onSettingsChange(when (mode) {
                            SharedDurationMode.FIVE -> settings.setSharedDuration(5)
                            SharedDurationMode.TEN -> settings.setSharedDuration(10)
                            SharedDurationMode.CUSTOM -> settings.selectSharedCustomDuration()
                        })
                    },
                    modifier = Modifier.testTag("sound_shared_mode_${mode.name.lowercase()}"),
                    label = { Text(when (mode) {
                        SharedDurationMode.FIVE -> "5s"
                        SharedDurationMode.TEN -> "10s"
                        SharedDurationMode.CUSTOM -> "Custom"
                    }) },
                )
            }
        }
        if (settings.sharedDurationMode == SharedDurationMode.CUSTOM) {
            // Custom is ONE sound for all (Class A/B/C and Free). Two settings
            // are independent: playback LENGTH and per-sound playback VOLUME.
            // Unlike the Run screen, this gain slider never changes the
            // phone-wide Media or Alarm volume stream.
            var customLengthDraft by remember(settings.sharedCustomSeconds) {
                mutableFloatStateOf(settings.sharedCustomSeconds.toFloat())
            }
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text("Custom sound length", style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f))
                Text("${customLengthDraft.roundToInt()}s",
                    style = MaterialTheme.typography.labelMedium)
            }
            AtelierSlimSlider(
                value = customLengthDraft,
                onValueChange = { customLengthDraft = it.roundToInt().coerceIn(5,60).toFloat() },
                onValueChangeFinished = {
                    onSettingsChange(settings.setSharedCustomDuration(
                        customLengthDraft.roundToInt().coerceIn(5,60)))
                },
                valueRange = 5f..60f, steps = 54,
                modifier = Modifier.fillMaxWidth()
                    .testTag("sound_shared_custom_length_slider")
                    .semantics {
                        contentDescription =
                            "Custom sound length ${customLengthDraft.roundToInt()} seconds"
                    },
            )
            var customGainDraft by remember(settings.sharedCustomVolumePercent) {
                mutableFloatStateOf(settings.sharedCustomVolumePercent.toFloat())
            }
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text("Custom sound volume", style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f))
                Text("${customGainDraft.roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium)
            }
            AtelierSlimSlider(
                value = customGainDraft,
                onValueChange = { customGainDraft = it.roundToInt().coerceIn(0,100).toFloat() },
                onValueChangeFinished = {
                    onSettingsChange(settings.setSharedCustomVolumePercent(
                        customGainDraft.roundToInt().coerceIn(0,100)))
                },
                valueRange = 0f..100f, steps = 99,
                modifier = Modifier.fillMaxWidth()
                    .testTag("sound_shared_custom_volume_slider")
                    .semantics {
                        contentDescription =
                            "Custom sound playback volume ${customGainDraft.roundToInt()} percent"
                    },
            )
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
    if (settings.assignmentMode == SoundAssignmentMode.SHARED) {
        // A single Shared sound and duration really apply to ALL four cues.
        // Retain the dormant, independent Free choice/length for Separate.
        Text("Free countdown uses the same sound, length and Custom volume.",
            style = MaterialTheme.typography.labelSmall)
    } else {
        Text("Free countdown · completion sound",
            style = MaterialTheme.typography.titleSmall)
        SoundPicker(
            cue = TimerCue.FREE_TIMER_FINISHED, settings = settings,
            onSettingsChange = onSettingsChange,
            onChooseSystemSound = onChooseSystemSound,
            onChooseAudioFile = onChooseAudioFile,
            onPreview = onPreview,
        )
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Free timer sound length",
                style = MaterialTheme.typography.labelMedium)
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
    }
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
            Text("Enable one or more sound libraries. 'Same sound for all' " +
                "uses one sound AND one length for Class A/B/C and Free countdown. " +
                "Choose 5s or 10s, or Custom to adjust sound length (5–60 seconds) " +
                "and per-sound playback volume (0–100%) with separate sliders. " +
                "Custom sound volume does not change the phone's Media/Alarm settings. " +
                "'Separate sounds' restores each cue's saved melody and duration. " +
                "Switching modes never deletes your previous choices. " +
                "System and audio-file sounds use Android pickers. " +
                "Class alerts can use Alarm or Media volume. " +
                "These sound settings never change approved Classes.")
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
                modifier = Modifier.fillMaxWidth()
                    .testTag("sound_picker_${cue.name.lowercase()}")) {
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
        // Compact triangular Play control; keep duration and cue context
        // in accessible semantics rather than a long visible button label.
        val previewCue = when (cue) {
            TimerCue.POSE_FINISHED -> "session"
            TimerCue.BREAK_FINISHED -> "break"
            TimerCue.SESSION_FINISHED -> "class end"
            TimerCue.FREE_TIMER_FINISHED -> "free timer"
        }
        val playTint = MaterialTheme.colorScheme.primary
        OutlinedIconButton(
            onClick = { onPreview(cue) },
            modifier = Modifier.size(48.dp)
                .testTag("sound_preview_${cue.name.lowercase()}")
                .semantics {
                    contentDescription =
                        "Play ${settings.secondsFor(cue)} second $previewCue sound preview"
                },
        ) {
            Canvas(modifier = Modifier.size(19.dp)) {
                val triangle = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.08f)
                    lineTo(size.width * 0.88f, size.height * 0.5f)
                    lineTo(size.width * 0.2f, size.height * 0.92f)
                    close()
                }
                drawPath(triangle, playTint)
            }
        }
    }
}

private fun SoundSettings.savedChoices(source: SoundSource): List<CueSound> =
    (listOf(poseEnd, breakEnd, classEnd, freeTimer) + listOfNotNull(sharedSound))
        .filter { it.source == source && it.isPlayable }
        .distinctBy { it.uri }
