package com.ateliertime.android

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ateliertime.domain.FreeQuickPresets
import com.ateliertime.domain.FreeTimerStatus
import com.ateliertime.domain.FreeTimerView

/** Free Timer tab. Its running countdown is owned above the tab navigation. */
@Composable
fun FreeCountdownSection(
    view: FreeTimerView,
    onStart: (Int, Int, Int) -> Unit,
    onPauseResume: () -> Unit,
    onExit: () -> Unit,
) {
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(5) }
    var seconds by remember { mutableIntStateOf(0) }
    val editable = view.status == FreeTimerStatus.READY ||
        view.status == FreeTimerStatus.FINISHED

    ElevatedCard(modifier = Modifier.fillMaxWidth().testTag("free_timer_section")) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Free countdown", style = MaterialTheme.typography.titleMedium)
            Text("Independent timer · hours / minutes / seconds",
                style = MaterialTheme.typography.bodySmall)
            // Preserve the HH:MM:SS wheel clock both before and during the run.
            // While running/paused its displayed values follow the remaining
            // monotonic time; gestures and neighbor buttons cannot edit it.
            val displayHours = if (editable) hours else view.remainingSeconds / 3600
            val displayMinutes = if (editable) minutes else (view.remainingSeconds / 60) % 60
            val displaySeconds = if (editable) seconds else view.remainingSeconds % 60
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                EndClockWheel("Hours", displayHours, 100, "free_hours",
                    canSelect = { true }, onBlocked = {}, onChange = { hours = it },
                    readOnly = !editable)
                EndClockWheel("Minutes", displayMinutes, 60, "free_minutes",
                    canSelect = { true }, onBlocked = {}, onChange = { minutes = it },
                    readOnly = !editable)
                EndClockWheel("Seconds", displaySeconds, 60, "free_seconds",
                    canSelect = { true }, onBlocked = {}, onChange = { seconds = it },
                    readOnly = !editable)
            }
            val statusText = when (view.status) {
                FreeTimerStatus.READY -> "Ready"
                FreeTimerStatus.RUNNING -> "Running"
                FreeTimerStatus.PAUSED -> "Paused"
                FreeTimerStatus.FINISHED -> "Finished"
            }
            Text(statusText, style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.testTag("free_timer_status"))
            if (editable) {
                Text("Quick presets · select a duration, then press Start",
                    style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FreeQuickPresets.entries.chunked(4).forEach { choices ->
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            choices.forEach { preset ->
                                val selected = hours == preset.hours &&
                                    minutes == preset.minutes && seconds == preset.seconds
                                OutlinedButton(
                                    onClick = {
                                        hours = preset.hours
                                        minutes = preset.minutes
                                        seconds = preset.seconds
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp)
                                        .testTag("free_preset_${preset.durationSeconds}"),
                                    contentPadding = PaddingValues(horizontal = 0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected)
                                            MaterialTheme.colorScheme.secondaryContainer
                                        else MaterialTheme.colorScheme.surface,
                                    ),
                                ) { Text(preset.label, fontSize = 11.sp, maxLines = 1) }
                            }
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (editable) {
                    Button(
                        onClick = { onStart(hours, minutes, seconds) },
                        enabled = hours + minutes + seconds > 0,
                        modifier = Modifier.weight(1f).testTag("free_timer_start"),
                    ) { Text("Start") }
                } else {
                    Button(onClick = onPauseResume, modifier = Modifier.weight(1f)
                        .testTag("free_timer_pause_resume")) {
                        Text(if (view.status == FreeTimerStatus.RUNNING) "Pause" else "Resume")
                    }
                }
                OutlinedButton(onClick = onExit,
                    enabled = view.status != FreeTimerStatus.READY,
                    modifier = Modifier.weight(1f).testTag("free_timer_exit")) {
                    Text("Exit")
                }
            }
            Text("Completion plays the Free countdown sound selected in Sounds. " +
                "The saved Class and its clock are unchanged.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
