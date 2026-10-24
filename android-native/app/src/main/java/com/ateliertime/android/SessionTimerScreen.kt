package com.ateliertime.android

import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ateliertime.domain.*
import kotlinx.coroutines.delay

/**
 * K04 Android timer screen. Uses exact approved phases and user-selected
 * five-second melodies. Lock-screen reliability still needs a foreground
 * service and real phone tests.
 */
@Composable
fun SessionTimerScreen(
    session: ApprovedSession,
    melodySelections: MelodySelections,
    onBackToEditor: () -> Unit,
) {
    val timer = remember(session) {
        MonotonicSessionTimer(session) { SystemClock.elapsedRealtime() }
    }
    var snapshot by remember(timer) { mutableStateOf(timer.view()) }
    var soundEnabled by remember { mutableStateOf(true) }
    var confirmStop by remember { mutableStateOf(false) }
    val audio = remember { MelodyAudioPlayer() }
    var audioError by remember { mutableStateOf<String?>(null) }
    var timerError by remember { mutableStateOf<String?>(null) }
    fun runTimerAction(action: () -> TimerView) {
        try {
            snapshot = action()
            timerError = null
        } catch (e: IllegalStateException) {
            timerError = e.message ?: "This interruption cannot fit within the final two poses."
        } catch (e: IllegalArgumentException) {
            timerError = e.message ?: "Invalid timing adjustment."
        }
    }
    DisposableEffect(audio) {
        onDispose { audio.close() }
    }

    fun tone(cue: TimerCue) {
        audio.play(melodySelections.forCue(cue)) { message -> audioError = message }
    }

    LaunchedEffect(timer, soundEnabled, audio) {
        while (true) {
            val tick = timer.update()
            snapshot = tick.view
            if (soundEnabled && tick.cue != null) tone(tick.cue)
            delay(200)
        }
    }

    val view = snapshot
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Atelier Time", style = MaterialTheme.typography.headlineMedium)
        Text("Session timer", style = MaterialTheme.typography.titleLarge)
        Text(session.rules.name, style = MaterialTheme.typography.titleMedium)
        Text(
            when (view.status) {
                TimerStatus.READY -> "Ready to begin the approved session"
                TimerStatus.RUNNING -> "Running"
                TimerStatus.PAUSED -> "Paused"
                TimerStatus.FINISHED -> "Session finished"
                TimerStatus.STOPPED -> "Stopped"
            },
            style = MaterialTheme.typography.bodyLarge,
        )

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(phaseLabel(view.phase), style = MaterialTheme.typography.titleLarge)
                Text(
                    durationLabel(view.phaseRemainingSeconds),
                    style = MaterialTheme.typography.displayLarge,
                )
                Text("Current phase remaining")
                val current = view.phase
                if (current != null) {
                    val done = current.durationSeconds - view.phaseRemainingSeconds
                    LinearProgressIndicator(
                        progress = { done.toFloat() / current.durationSeconds },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text("Next: " + phaseLabel(view.nextPhase))
            }
        }

        Text("Whole session remaining: " + durationLabel(view.totalRemainingSeconds))
        LinearProgressIndicator(
            progress = {
                view.elapsedSeconds.toFloat() / view.totalSeconds.coerceAtLeast(1)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Elapsed: " + durationLabel(view.elapsedSeconds) +
            " / " + durationLabel(view.totalSeconds))
        if (view.recoveredSeconds > 0) {
            Text("Interruption recovered from the final two poses: " +
                durationLabel(view.recoveredSeconds),
                color = MaterialTheme.colorScheme.primary)
            val pair = timer.finalTwoPoseDurations()
            if (pair != null) {
                Text("Revised final poses: " + durationLabel(pair.first) + " + " +
                    durationLabel(pair.second))
            }
            Text("Ending mode: " +
                (session.selectedChoice?.mode?.name ?: "REMAINDER") +
                ". Intervening rest and reserved end summary stay unchanged. " +
                "The saved preset is not overwritten.")
        }
        if (timerError != null) {
            Text(timerError ?: "", color = MaterialTheme.colorScheme.error)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (view.status) {
                TimerStatus.READY, TimerStatus.FINISHED -> {
                    Button(onClick = { runTimerAction { timer.start() } }) {
                        Text(if (view.status == TimerStatus.FINISHED) "New session" else "Start")
                    }
                }
                TimerStatus.STOPPED -> {
                    Button(onClick = { runTimerAction { timer.start() } }) {
                        Text("Continue session")
                    }
                }
                TimerStatus.RUNNING -> {
                    Button(onClick = { runTimerAction { timer.pause() } }) { Text("Pause") }
                }
                TimerStatus.PAUSED -> {
                    Button(onClick = { runTimerAction { timer.resume() } }) { Text("Resume") }
                }
            }
            OutlinedButton(
                enabled = view.status == TimerStatus.RUNNING ||
                    view.status == TimerStatus.PAUSED,
                onClick = { confirmStop = true },
            ) { Text("Stop") }
            TextButton(onClick = {
                timer.stop()
                onBackToEditor()
            }) { Text("Editor") }
        }
        if (view.status == TimerStatus.STOPPED || view.status == TimerStatus.PAUSED) {
            TextButton(onClick = {
                runTimerAction { timer.restartFromBeginning() }
            }) { Text("Start new full session (new end time)") }
        }

        HorizontalDivider()
        Text("Sound cues", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable sounds", modifier = Modifier.weight(1f))
            Switch(checked = soundEnabled, onCheckedChange = { soundEnabled = it })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { tone(TimerCue.POSE_FINISHED) }) { Text("Test A") }
            OutlinedButton(onClick = { tone(TimerCue.BREAK_FINISHED) }) { Text("Test B") }
            OutlinedButton(onClick = { tone(TimerCue.SESSION_FINISHED) }) { Text("Test C") }
        }
        Text("A: end of each pose · B: end of break · C: session finished")
        Text("Each event plays its five-second melody selected on the main editor.")
        if (audioError != null) {
            Text(audioError ?: "", color = MaterialTheme.colorScheme.error)
        }
        Text(
            "Melody volume uses device media volume. Playback while the phone is locked " +
                "or the app is suspended still requires foreground-service validation.",
            style = MaterialTheme.typography.bodySmall,
        )
    }

    if (confirmStop) {
        AlertDialog(
            onDismissRequest = { confirmStop = false },
            title = { Text("Stop this session?") },
            text = { Text("Stop keeps your elapsed progress. Continuing this session " +
                "will shorten the last two poses to preserve the original end time " +
                "using your previously approved ending choice. The saved preset is unchanged.") },
            confirmButton = {
                TextButton(onClick = {
                    runTimerAction { timer.stop() }
                    confirmStop = false
                }) { Text("Stop timer") }
            },
            dismissButton = {
                TextButton(onClick = { confirmStop = false }) { Text("Cancel") }
            },
        )
    }
}

private fun phaseLabel(phase: SchedulePhase?): String = when (phase?.kind) {
    PhaseKind.POSE -> "Pose " + (phase.poseNumber ?: "")
    PhaseKind.SHORT_BREAK -> "Regular break"
    PhaseKind.SPECIAL_BREAK -> "Special break"
    PhaseKind.SUMMARY -> "End-of-session summary"
    null -> "No active phase"
}

private fun durationLabel(seconds: Int): String {
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
