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
    classClockTimes: ClassClockTimes,
    timerError: String?,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onRestartSession: () -> Unit,
    onNextPhase: () -> Unit,
) {
    var confirmation by remember { mutableStateOf<String?>(null) }
    // Contextual help keeps the Timer compact on a phone. Nothing is hidden
    // from screen-reader users because each action has its own Help control.
    var helpFor by remember { mutableStateOf<String?>(null) }
    val time = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    fun showTime(value: Long?): String = value?.let {
        time.format(Date(it))
    } ?: "—"
    val segmentLabel = classClockTimes.currentPhaseLabel ?: "Current segment"
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Class timer", style = MaterialTheme.typography.titleLarge)
                Text(session.rules.name, style = MaterialTheme.typography.titleSmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Clock  " + showTime(wallClockMillis),
                    style = MaterialTheme.typography.labelSmall)
                Text("Class start  " + showTime(classClockTimes.classStartEpochMs),
                    style = MaterialTheme.typography.labelSmall)
                Text("Class end  " + showTime(classClockTimes.classEndEpochMs),
                    style = MaterialTheme.typography.labelSmall)
                Text("$segmentLabel start  " +
                    showTime(classClockTimes.currentPhaseStartEpochMs),
                    style = MaterialTheme.typography.labelSmall)
                Text("$segmentLabel end  " +
                    (if (classClockTimes.paused) "— (paused)"
                        else showTime(classClockTimes.currentPhaseEndEpochMs)),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        Text(
            when (view.status) {
                TimerStatus.READY -> "Ready to begin the approved class"
                TimerStatus.RUNNING -> "Running"
                TimerStatus.PAUSED -> "Paused — segment end time pending Resume"
                TimerStatus.FINISHED -> "Class finished"
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
                Text("Current session / break remaining")
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
        Text("Class remaining: " + formatTime(view.totalRemainingSeconds))
        LinearProgressIndicator(
            progress = { view.elapsedSeconds.toFloat() / view.totalSeconds.coerceAtLeast(1) },
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Class elapsed: " + formatTime(view.elapsedSeconds) +
            " / " + formatTime(view.totalSeconds))
        if (view.recoveredSeconds != 0) {
            Text(
                (if (view.recoveredSeconds > 0) "Time deducted from final two sessions: "
                else "Time added to final two sessions: ") +
                    formatTime(kotlin.math.abs(view.recoveredSeconds)),
                color = MaterialTheme.colorScheme.primary,
            )
            timer.finalTwoPoseDurations()?.let { pair ->
                Text("Adjusted final two sessions: " +
                    formatTime(pair.first) + " + " + formatTime(pair.second))
            }
            Text("Ending choice: " + (session.selectedChoice?.mode?.name ?: "REMAINDER") +
                ". The intervening break, class summary and saved preset remain unchanged.")
        }
        timerError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        when (view.status) {
            TimerStatus.READY, TimerStatus.FINISHED -> {
                Button(onClick = onStart) {
                    Text(if (view.status == TimerStatus.FINISHED) "Start new class"
                    else "Start class")
                }
            }
            TimerStatus.RUNNING, TimerStatus.PAUSED, TimerStatus.STOPPED -> {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onPauseResume) {
                        Text(if (view.status == TimerStatus.RUNNING) "Pause" else "Resume")
                    }
                    TextButton(onClick = { helpFor = "pause" }) { Text("Help") }
                }
            }
        }
        HorizontalDivider()
        Text("Class controls", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                enabled = view.status == TimerStatus.RUNNING ||
                    view.status == TimerStatus.PAUSED,
                modifier = Modifier.weight(1f),
                onClick = { confirmation = "restart" },
            ) { Text("Restart class") }
            TextButton(onClick = { helpFor = "restart" }) { Text("Help") }
        }
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                enabled = view.status == TimerStatus.RUNNING ||
                    view.status == TimerStatus.PAUSED,
                modifier = Modifier.weight(1f),
                onClick = onNextPhase,
            ) { Text("Next") }
            TextButton(onClick = { helpFor = "next" }) { Text("Help") }
        }
    }

    val choice = confirmation
    if (choice != null) {
        AlertDialog(
            onDismissRequest = { confirmation = null },
            title = {
                Text("Restart this class?")
            },
            text = {
                Text("Restart at the beginning of the same class. To keep the " +
                    "original finish time, the final two regular sessions are shortened " +
                    "according to the approved ending choice. If that is impossible, " +
                    "the running timer remains unchanged. The session-finish melody " +
                    "plays only if the restart succeeds.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmation = null
                    onRestartSession()
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { confirmation = null }) { Text("Cancel") }
            },
        )
    }
    val selectedHelp = helpFor
    if (selectedHelp != null) {
        val title = when (selectedHelp) {
            "pause" -> "Pause / Resume"
            "restart" -> "Restart class"
            else -> "Next phase"
        }
        val explanation = when (selectedHelp) {
            "pause" ->
                "Pause holds the current session or break. Resume continues from the same point. " +
                    "Time spent paused is compensated, where feasible, by shortening " +
                    "only the final two regular sessions according to your chosen ending."
            "restart" ->
                "Restart the SAME class from Session 1 and play the " +
                    "session-finish melody C. Keep the original clock finish time " +
                    "by deducting time already used from the two final regular " +
                    "poses, according to the approved ending choice. Both must " +
                    "remain at least one minute. If the change is impossible, " +
                    "the running timer stays unchanged without playing a cue."
            else ->
                "Next immediately finishes the current session or break and starts the " +
                    "very next phase of THIS class, whether it is a session, " +
                    "regular break, special break, or class summary. It plays the " +
                    "appropriate cue: A when leaving a session, B when leaving a " +
                    "break, and C when finishing the session. Skipped time is " +
                    "added to the final two regular sessions, or reduced if a pause " +
                    "previously used more time. Their original ending choice is " +
                    "preserved and the approved saved preset never changes. " +
                    "If the final pair has already started or cannot be adjusted " +
                    "safely, Next shows an explanation instead of changing the clock."
        }
        AlertDialog(
            onDismissRequest = { helpFor = null },
            title = { Text(title) },
            text = { Text(explanation) },
            confirmButton = {
                TextButton(onClick = { helpFor = null }) { Text("Close") }
            },
        )
    }

}

private fun phaseLabel(phase: SchedulePhase?): String = when (phase?.kind) {
    PhaseKind.POSE -> "Session " + (phase.poseNumber ?: "")
    PhaseKind.SHORT_BREAK -> if (phase.openingGroupId != null) "Break · opening group"
        else "Break · regular"
    PhaseKind.SPECIAL_BREAK -> "Break · special"
    PhaseKind.SUMMARY -> "Class summary"
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
