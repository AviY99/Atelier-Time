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
    onExit: () -> Unit,
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
    val currentRemainingLabel = when (view.phase?.kind) {
        PhaseKind.POSE -> "Current session remaining"
        PhaseKind.SHORT_BREAK, PhaseKind.SPECIAL_BREAK ->
            phaseLabel(view.phase) + " remaining"
        PhaseKind.SUMMARY -> "Class summary remaining"
        null -> "No current session / break"
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Class timer", style = MaterialTheme.typography.titleLarge)
                Text(session.rules.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    when (view.status) {
                        TimerStatus.RUNNING -> "Running"
                        TimerStatus.PAUSED -> "Paused"
                        TimerStatus.READY -> "Ready"
                        TimerStatus.FINISHED -> "Finished"
                        TimerStatus.STOPPED -> "Stopped"
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Column(horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
                Spacer(modifier = Modifier.height(6.dp))
                Text("Class remaining  " + formatTime(view.totalRemainingSeconds),
                    style = MaterialTheme.typography.labelSmall)
                LinearProgressIndicator(
                    progress = {
                        view.elapsedSeconds.toFloat() /
                            view.totalSeconds.coerceAtLeast(1)
                    },
                    modifier = Modifier.width(178.dp).height(4.dp),
                )
                Text("Class elapsed  " + formatTime(view.elapsedSeconds) +
                    " / " + formatTime(view.totalSeconds),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(phaseLabel(view.phase), style = MaterialTheme.typography.titleLarge)
                Text(formatTime(view.phaseRemainingSeconds),
                    style = MaterialTheme.typography.displayLarge)
                Text(currentRemainingLabel)
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

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Class controls", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    val underway = view.status == TimerStatus.RUNNING ||
                        view.status == TimerStatus.PAUSED
                    if (view.status == TimerStatus.READY ||
                        view.status == TimerStatus.FINISHED) {
                        Button(onClick = onStart, modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 5.dp, vertical = 6.dp)) {
                            Text("Start", maxLines = 1)
                        }
                    } else {
                        Button(onClick = onPauseResume, modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 5.dp, vertical = 6.dp)) {
                            Text(if (view.status == TimerStatus.RUNNING) "Pause" else "Resume",
                                maxLines = 1)
                        }
                    }
                    OutlinedButton(
                        enabled = underway,
                        onClick = { confirmation = "restart" },
                        modifier = Modifier.weight(1.3f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) { Text("Restart class", style = MaterialTheme.typography.labelSmall) }
                    OutlinedButton(
                        enabled = underway,
                        onClick = onNextPhase,
                        modifier = Modifier.weight(0.85f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) { Text("Next", maxLines = 1) }
                }
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(onClick = { helpFor = "pause" },
                        contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text("Pause help", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = { helpFor = "restart" },
                        contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text("Restart help", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = { helpFor = "next" },
                        contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text("Next help", style = MaterialTheme.typography.labelSmall)
                    }
                }
                TextButton(onClick = { confirmation = "exit" },
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) { Text("End & return to saved classes") }
            }
        }
    }

    val choice = confirmation
    if (choice != null) {
        AlertDialog(
            onDismissRequest = { confirmation = null },
            title = {
                Text(if (choice == "exit") "End class and return?"
                    else "Restart this class?")
            },
            text = {
                Text(if (choice == "exit")
                    "End the current run and return to your saved classes in Timer. " +
                        "The saved approved plan is not changed."
                else
                    "Restart at the beginning of the same class. To keep the " +
                        "original finish time, the final two regular sessions are shortened " +
                        "according to the approved ending choice. If that is impossible, " +
                        "the running timer remains unchanged. The class-finish melody " +
                        "plays only if restart succeeds.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmation = null
                    if (choice == "exit") onExit() else onRestartSession()
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
                    "class-finish melody C. Keep the original clock finish time " +
                    "by deducting time already used from the two final regular " +
                    "sessions, according to the approved ending choice. Both must " +
                    "remain at least one minute. If the change is impossible, " +
                    "the running timer stays unchanged without playing a cue."
            else ->
                "Next immediately finishes the current session or break and starts the " +
                    "very next phase of THIS class, whether it is a session, " +
                    "regular break, special break, or class summary. It plays the " +
                    "appropriate cue: A when leaving a session, B when leaving a " +
                    "break, and C when finishing the class. Skipped time is " +
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
