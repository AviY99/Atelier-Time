package com.ateliertime.android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ateliertime.domain.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Presentation only: the root owns timer/clock/audio, allowing Sounds and
 * Editor tabs to be visited without interrupting the running countdown.
 */
@Composable
fun SessionTimerScreen(
    session: ApprovedSession,
    timer: MonotonicSessionTimer,
    view: TimerView,
    wallClockMillis: Long,
    deadlineEpochMillis: Long?,
    timerError: String?,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onStopAndRestart: () -> Unit,
    onStopAndNext: () -> Unit,
) {
    var confirmation by remember { mutableStateOf<String?>(null) }
    val time = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Session timer", style = MaterialTheme.typography.headlineSmall)
                Text(session.rules.name, style = MaterialTheme.typography.titleMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Now  " + time.format(Date(wallClockMillis)),
                    style = MaterialTheme.typography.labelMedium)
                Text("End  " + (deadlineEpochMillis?.let { time.format(Date(it)) } ?: "—"),
                    style = MaterialTheme.typography.labelMedium)
            }
        }
        Text(
            when (view.status) {
                TimerStatus.READY -> "Ready to begin the approved session"
                TimerStatus.RUNNING -> "Running"
                TimerStatus.PAUSED -> "Paused — current end time remains reserved"
                TimerStatus.FINISHED -> "Session finished"
                TimerStatus.STOPPED -> "Stopped"
            },
        )
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(phaseLabel(view.phase), style = MaterialTheme.typography.titleLarge)
                Text(formatTime(view.phaseRemainingSeconds),
                    style = MaterialTheme.typography.displayLarge)
                Text("Current phase remaining")
                view.phase?.let { phase ->
                    val done = phase.durationSeconds - view.phaseRemainingSeconds
                    LinearProgressIndicator(
                        progress = {
                            done.toFloat() / phase.durationSeconds.coerceAtLeast(1)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text("Next: " + phaseLabel(view.nextPhase))
            }
        }
        Text("Session remaining: " + formatTime(view.totalRemainingSeconds))
        LinearProgressIndicator(
            progress = { view.elapsedSeconds.toFloat() / view.totalSeconds.coerceAtLeast(1) },
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Elapsed: " + formatTime(view.elapsedSeconds) +
            " / " + formatTime(view.totalSeconds))
        if (view.recoveredSeconds > 0) {
            Text("Time recovered from final two poses: " +
                formatTime(view.recoveredSeconds), color = MaterialTheme.colorScheme.primary)
            timer.finalTwoPoseDurations()?.let { pair ->
                Text("Adjusted final two poses: " +
                    formatTime(pair.first) + " + " + formatTime(pair.second))
            }
            Text("Ending choice: " + (session.selectedChoice?.mode?.name ?: "REMAINDER") +
                ". The intervening break, end summary and saved preset remain unchanged.")
        }
        timerError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        when (view.status) {
            TimerStatus.READY, TimerStatus.FINISHED -> {
                Button(onClick = onStart) {
                    Text(if (view.status == TimerStatus.FINISHED) "Start new session" else "Start session")
                }
            }
            TimerStatus.RUNNING, TimerStatus.PAUSED -> {
                Button(onClick = onPauseResume) {
                    Text(if (view.status == TimerStatus.PAUSED) "Resume" else "Pause")
                }
            }
            TimerStatus.STOPPED -> {
                Button(onClick = onPauseResume) { Text("Resume") }
            }
        }
        HorizontalDivider()
        Text("Stop session", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(
            enabled = view.status == TimerStatus.RUNNING || view.status == TimerStatus.PAUSED,
            modifier = Modifier.fillMaxWidth(),
            onClick = { confirmation = "restart" },
        ) { Text("Stop and restart session") }
        OutlinedButton(
            enabled = view.status == TimerStatus.RUNNING || view.status == TimerStatus.PAUSED,
            modifier = Modifier.fillMaxWidth(),
            onClick = { confirmation = "next" },
        ) { Text("Stop and go to next saved session") }
        Text(
            "Pause freezes the current pose; Resume shortens the final two poses " +
                "as needed to keep the original finish time. Stop and restart begins " +
                "at pose 1 while preserving the same finish deadline (if feasible). " +
                "Next session loads the next stored preset without modifying either plan. " +
                "Change melodies anytime in the Sounds tab; automatic cues remain active " +
                "while you switch tabs.",
            style = MaterialTheme.typography.bodySmall,
        )
    }

    val choice = confirmation
    if (choice != null) {
        AlertDialog(
            onDismissRequest = { confirmation = null },
            title = {
                Text(if (choice == "restart") "Stop and restart?" else "Stop and select next session?")
            },
            text = {
                Text(if (choice == "restart")
                    "This begins again at the first pose. To preserve the original finish " +
                        "clock time, only the last two poses will be shortened. " +
                        "If that is impossible, your running session remains unchanged."
                else
                    "The current timer ends and the next saved preset opens ready to start. " +
                        "Saved session data will not be changed.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmation = null
                    if (choice == "restart") onStopAndRestart() else onStopAndNext()
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { confirmation = null }) { Text("Cancel") }
            },
        )
    }
}

private fun phaseLabel(phase: SchedulePhase?): String = when (phase?.kind) {
    PhaseKind.POSE -> "Pose " + (phase.poseNumber ?: "")
    PhaseKind.SHORT_BREAK -> if (phase.openingGroupId != null) "Opening-group break"
        else "Regular break"
    PhaseKind.SPECIAL_BREAK -> "Special break"
    PhaseKind.SUMMARY -> "End-of-session summary"
    null -> "No active phase"
}

private fun formatTime(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe / 60) % 60
    val remain = safe % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, remain)
    } else {
        "%02d:%02d".format(minutes, remain)
    }
}
