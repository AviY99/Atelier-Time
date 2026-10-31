package com.ateliertime.android

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
            // Preserve the HH:MM:SS wheel clock both before and during the run.
            // While running/paused its displayed values follow the remaining
            // monotonic time; gestures and neighbor buttons cannot edit it.
            val displayHours = if (editable) hours else view.remainingSeconds / 3600
            val displayMinutes = if (editable) minutes else (view.remainingSeconds / 60) % 60
            val displaySeconds = if (editable) seconds else view.remainingSeconds % 60
            // Three 94dp wheels can exceed a small screen's inner card width.
            // Bound each wheel to the measured available width instead of
            // clipping the Seconds column; default phones keep the 94dp format.
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val wheelWidth = ((maxWidth - 12.dp) / 3).coerceAtMost(94.dp)
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically) {
                    EndClockWheel("Hours", displayHours, 100, "free_hours",
                        canSelect = { true }, onBlocked = {}, onChange = { hours = it },
                        readOnly = !editable, wheelWidth = wheelWidth, mainNumberScale = 2f)
                    EndClockWheel("Minutes", displayMinutes, 60, "free_minutes",
                        canSelect = { true }, onBlocked = {}, onChange = { minutes = it },
                        readOnly = !editable, wheelWidth = wheelWidth, mainNumberScale = 2f)
                    EndClockWheel("Seconds", displaySeconds, 60, "free_seconds",
                        canSelect = { true }, onBlocked = {}, onChange = { seconds = it },
                        readOnly = !editable, wheelWidth = wheelWidth, mainNumberScale = 2f)
                }
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
                Text("Quick presets",
                    style = MaterialTheme.typography.labelMedium)
                val largePresetText = LocalDensity.current.fontScale >= 1.3f
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FreeQuickPresets.entries.chunked(if (largePresetText) 2 else 4)
                        .forEach { choices ->
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
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                        .testTag("free_preset_${preset.durationSeconds}")
                                        .semantics { this.selected = selected },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected)
                                            MaterialTheme.colorScheme.secondaryContainer
                                        else MaterialTheme.colorScheme.surface,
                                    ),
                                ) { Text(preset.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center, maxLines = 2) }
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
        }
    }
}
