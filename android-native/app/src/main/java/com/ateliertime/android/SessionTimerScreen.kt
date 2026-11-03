package com.ateliertime.android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    designPalette: DesignPalette,
    eventHistory: List<String>,
    onPauseResume: () -> Unit,
    onRestartSession: (Boolean) -> Unit,
    onRepeatLast: (Boolean) -> Unit,
    onNextPhase: () -> Unit,
    onExit: () -> Unit,
) {
    var confirmation by remember { mutableStateOf<String?>(null) }
    var accept25RestWarning by remember { mutableStateOf(false) }
    // Contextual help keeps the Timer compact on a phone. Nothing is hidden
    // from screen-reader users because each action has its own Help control.
    var helpFor by remember { mutableStateOf<String?>(null) }
    // Class header clocks use minute precision; active Session/Break
    // boundary clocks retain seconds for exact timing review.
    val classTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val phaseTime = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    fun showClassTime(value: Long?): String =
        value?.let { classTime.format(Date(it)) } ?: "—"
    fun showPhaseTime(value: Long?): String =
        value?.let { phaseTime.format(Date(it)) } ?: "—"
    val isBreak = view.phase?.kind == PhaseKind.SHORT_BREAK ||
        view.phase?.kind == PhaseKind.SPECIAL_BREAK
    val canRestartSegment = view.phase?.kind == PhaseKind.POSE || isBreak
    val isWork = view.phase?.kind == PhaseKind.POSE
    val tahini = tahiniFill(designPalette)
    val durationMs = view.phase?.durationSeconds?.toLong()?.times(1_000L) ?: 0L
    val msLeft = view.phaseRemainingMillis ?:
        view.phaseRemainingSeconds.toLong() * 1_000L
    // Single source for presentation progress; never modify real timer math.
    val targetFill = DesignMotion.fillFraction(view.phase?.kind, durationMs, msLeft)
    val targetElapsed = DesignMotion.elapsedFraction(durationMs, msLeft)
    val smoothElapsed by animateFloatAsState(
        targetValue = targetElapsed,
        animationSpec = tween(durationMillis = 210, easing = LinearEasing),
        label = "phaseTimeElapsedFraction",
    )
    val smoothFill by animateFloatAsState(
        targetValue = targetFill.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 210, easing = LinearEasing),
        label = "solidTahiniPhaseProgress",
    )
    val restartTitle = if (isBreak) "Restart break" else "Restart session"
    val lastTarget = timer.repeatLastTarget()
    fun phaseLabel(phase: SchedulePhase?): String =
        PhaseDisplayNames.title(phase, session.rules)
    val trueClassLengthSeconds = run {
        val start = classClockTimes.classStartEpochMs
        val end = classClockTimes.classEndEpochMs
        if (start != null && end != null) ((end - start) / 1_000L).toInt()
        else view.totalSeconds
    }
    val currentRemainingLabel = when (view.phase?.kind) {
        PhaseKind.POSE -> "Current session remaining"
        PhaseKind.SHORT_BREAK, PhaseKind.SPECIAL_BREAK ->
            phaseLabel(view.phase) + " remaining"
        PhaseKind.SUMMARY -> "Class summary remaining"
        null -> "No current session / break"
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                Text("Clock  " + showClassTime(wallClockMillis),
                    style = MaterialTheme.typography.headlineSmall)
                // Three matching lines directly under Clock, without
                // extra vertical gaps between start, end and Class length.
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    WallClockRow(
                        "Class start",
                        showClassTime(classClockTimes.classStartEpochMs),
                        enlargeLabel = true, compact = true,
                    )
                    WallClockRow(
                        "Class end",
                        showClassTime(classClockTimes.classEndEpochMs),
                        enlargeLabel = true, compact = true,
                    )
                    WallClockRow(
                        "Class length",
                        formatClassLength(trueClassLengthSeconds),
                        enlargeLabel = true, compact = true,
                    )
                }
            }
        }
        TimelinePhaseChart(
            phases = timer.activePhasesSnapshot(),
            runtimeTimeline = timer.runtimeTimeline(),
            classRemainingSeconds = classClockTimes.classRemainingSeconds
                ?: view.totalRemainingSeconds,
            classElapsedSeconds = classClockTimes.classElapsedSeconds ?: 0,
            currentPhaseId = view.phase?.id,
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                // Increase contrast against the Night canvas only in the
                // active Timer card. White small text remains > 5:1.
                containerColor = if (designPalette == DesignPalette.NIGHT)
                    Color(0xFF4D7194) else MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Box(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)) {
                if (isWork || isBreak) {
                    Box(modifier = Modifier.matchParentSize()) {
                        Box(
                            modifier = Modifier.align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .fillMaxHeight(smoothFill.coerceIn(0f, 1f))
                                .background(tahini.copy(alpha = 0.23f)),
                        )
                    }
                }
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top) {
                    Text(phaseLabel(view.phase),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f))
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        // Keep the phase's Start/End/Length together as a
                        // compact time-information block with aligned digits.
                        WallClockRow(
                            "Start",
                            showPhaseTime(classClockTimes.currentPhaseStartEpochMs),
                            stabilizeTime = true, compact = true,
                        )
                        WallClockRow(
                            "End",
                            if (classClockTimes.paused) "— (paused)"
                            else showPhaseTime(classClockTimes.currentPhaseEndEpochMs),
                            stabilizeTime = true, compact = true,
                        )
                        view.phase?.let { active ->
                            val lengthLabel = when (active.kind) {
                                PhaseKind.POSE -> "Session length"
                                PhaseKind.SHORT_BREAK, PhaseKind.SPECIAL_BREAK -> "Break length"
                                else -> null
                            }
                            lengthLabel?.let { label ->
                                WallClockRow(
                                    label,
                                    formatTime(active.durationSeconds),
                                    stabilizeTime = true, compact = true,
                                )
                            }
                        }
                    }
                }
                Text(formatTime(view.phaseRemainingSeconds),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFeatureSettings = "tnum"
                    ))
                Text(currentRemainingLabel)
                view.phase?.let { phase ->
                    // Session AND Break bars show ELAPSED time, filling from
                    // zero to full. Unlike the bar, the Break background drains.
                    SolidProgressBar(
                        progressFraction = if (isWork || isBreak) smoothElapsed
                            else DesignMotion.elapsedFraction(
                                phase.durationSeconds.toLong(),
                                view.phaseRemainingSeconds.toLong(),
                            ),
                        fillColor = tahini,
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                    )
                }
                Text("Next: " + phaseLabel(view.nextPhase) +
                    (view.nextPhase?.let { " (" + formatTime(it.durationSeconds) + ")" } ?: ""))
            }
            }
        }
        // The chart and current-phase clock already explain the plan.
        // Keep scheduler internals in the Run diagnostic report only.
        timerError?.let { Text(it, color = atelierErrorTextColor()) }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Class controls", style = MaterialTheme.typography.titleSmall)
                val largeControlText = LocalDensity.current.fontScale >= 1.3f
                FlowRow(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    val underway = view.status == TimerStatus.RUNNING ||
                        view.status == TimerStatus.PAUSED
                    // Run is allowed ONLY after approving its Class finish
                    // in the saved-library dialog. Never expose a shortcut
                    // that starts a READY timer without its chosen deadline.
                    if (view.status == TimerStatus.READY ||
                        view.status == TimerStatus.FINISHED) {
                        Text(
                            if (view.status == TimerStatus.READY)
                                "Starting approved Class..."
                            else "Class finished",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    } else {
                        Button(colors = atelierFilledColors(), onClick = onPauseResume, modifier = if (largeControlText) Modifier.widthIn(min = 150.dp)
                                else Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 5.dp, vertical = 6.dp)) {
                            Text(if (view.status == TimerStatus.RUNNING) "Pause" else "Resume",
                                maxLines = 1)
                        }
                    }
                    OutlinedButton(colors = atelierOutlinedColors(), 
                        enabled = underway && canRestartSegment,
                        onClick = { accept25RestWarning = false; confirmation = "restart" },
                        modifier = if (largeControlText) Modifier.widthIn(min = 150.dp)
                            else Modifier.weight(1.45f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) { Text(restartTitle, style = MaterialTheme.typography.labelSmall) }
                    OutlinedButton(colors = atelierOutlinedColors(),
                        enabled = underway && lastTarget != null,
                        onClick = { accept25RestWarning = false; confirmation = "repeat" },
                        modifier = if (largeControlText) Modifier.widthIn(min = 150.dp)
                            else Modifier.weight(1.2f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) { Text("Repeat Last", maxLines = 1,
                        style = MaterialTheme.typography.labelSmall) }
                    OutlinedButton(colors = atelierOutlinedColors(), 
                        enabled = underway,
                        onClick = { confirmation = "next" },
                        modifier = if (largeControlText) Modifier.widthIn(min = 150.dp)
                            else Modifier.weight(0.8f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    ) { Text("Next", maxLines = 1) }
                    Button(
                        onClick = { confirmation = "exit" },
                        modifier = if (largeControlText) Modifier.widthIn(min = 150.dp)
                            else Modifier.weight(0.85f),
                        contentPadding = PaddingValues(horizontal = 3.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = if (atelierNightActive()) Color.White
                                else MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) { Text("Exit", maxLines = 1, style = MaterialTheme.typography.labelSmall) }
                }
                FlowRow(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(colors = atelierTextColors(), onClick = { helpFor = "pause" },
                        contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text("Pause help", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(colors = atelierTextColors(), onClick = { helpFor = "restart" },
                        contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text("Restart help", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(colors = atelierTextColors(), onClick = { helpFor = "repeat" },
                        contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text("Repeat help", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(colors = atelierTextColors(), onClick = { helpFor = "next" },
                        contentPadding = PaddingValues(horizontal = 2.dp)) {
                        Text("Next help", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(colors = atelierTextColors(), onClick = { helpFor = "exit" },
                        contentPadding = PaddingValues(horizontal = 2.dp)) {
                        Text("Exit help", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        if (eventHistory.isNotEmpty()) {
            // Show about five recent actions without expanding the whole page.
            // All events remain available: swipe UP within this list to browse
            // backwards to the Run's Start. It does not discard old entries.
            val actionScroll = rememberLazyListState()
            LaunchedEffect(eventHistory.size) {
                if (actionScroll.firstVisibleItemIndex == 0) {
                    actionScroll.scrollToItem(0)
                }
            }
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Recent Class actions · ${eventHistory.size}",
                        style = MaterialTheme.typography.titleSmall)
                    LazyColumn(
                        state = actionScroll,
                        modifier = Modifier.fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .testTag("class_action_history_scroll"),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        items(eventHistory.size,
                            key = { offset -> eventHistory.lastIndex - offset }) { offset ->
                            Text(
                                eventHistory[eventHistory.lastIndex - offset],
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 39.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    val choice = confirmation
    if (choice != null) {
        val restWarning = when (choice) {
            "restart" -> timer.restartCurrentRequiresRestWarning()
            "repeat" -> timer.repeatLastRequiresRestWarning()
            else -> false
        }
        AlertDialog(
            onDismissRequest = { confirmation = null },
            title = {
                Text(when (choice) {
                    "exit" -> "End class and return?"
                    "next" -> "Skip to " +
                        (view.nextPhase?.let(::phaseLabel) ?: "Class end") + "?"
                    "repeat" -> "Repeat previous " +
                        (lastTarget?.let(::phaseLabel) ?: "phase") + " in full?"
                    else -> "Restart current " + (if (isBreak) "break?" else "session?")
                })
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(when (choice) {
                        "exit" -> "End the current Run and return to saved Classes. " +
                            "The saved approved Class is unchanged."
                        "next" -> "Next immediately skips the remainder of the current " +
                            "phase and enters the next one. It is rejected if the " +
                            "approved fixed finish cannot be preserved."
                        "repeat" -> "Repeat the immediately previous Session/Break for its " +
                            "FULL original length, not just unused Next time. " +
                            "The current phase will be entered again after the replay. " +
                            "Time is compensated from the remaining final regular Sessions " +
                            "if possible. Saved Classes and past real activity are unchanged."
                        else -> "Restart the CURRENT Session/Break for its FULL length. " +
                            "Actual time already worked remains part of the 25/5 safety " +
                            "calculation. Future Sessions compensate where feasible."
                    })
                    if (restWarning) {
                        Text(
                            "25/5 REST WARNING: this full repeat would exceed 25 " +
                                "accumulated work minutes without an actual break of " +
                                "at least 5 minutes. Confirm that you want to continue.",
                            color = atelierErrorTextColor(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = accept25RestWarning,
                                onCheckedChange = { accept25RestWarning = it },
                                modifier = Modifier.testTag("runtime_25_5_acknowledge"),
                            )
                            Text("I understand and approve this 25/5 exception.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    colors = atelierTextColors(),
                    enabled = !restWarning || accept25RestWarning,
                    onClick = {
                        confirmation = null
                        when (choice) {
                            "exit" -> onExit()
                            "next" -> onNextPhase()
                            "repeat" -> onRepeatLast(accept25RestWarning)
                            else -> onRestartSession(accept25RestWarning)
                        }
                        accept25RestWarning = false
                    },
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(colors = atelierTextColors(), onClick = {
                    confirmation = null
                    accept25RestWarning = false
                }) { Text("Cancel") }
            },
        )
    }
    val selectedHelp = helpFor
    if (selectedHelp != null) {
        val title = when (selectedHelp) {
            "pause" -> "Pause / Resume"
            "restart" -> restartTitle
            "repeat" -> "Repeat previous Session/Break"
            "exit" -> "Exit class"
            else -> "Next phase"
        }
        val explanation = when (selectedHelp) {
            "pause" ->
                "Pause holds the current session or break. Resume continues from the same point. " +
                    "Time spent paused is compensated, where feasible, by shortening " +
                    "only the final two regular sessions according to your chosen ending."
            "exit" ->
                "Exit ends the currently running Class now and returns to the saved " +
                    "Class list in Timer. Your saved and approved Class, schedule, " +
                    "sound selections and other saved Classes are not deleted or changed. " +
                    "Exit asks for confirmation and does not play a completion melody."
            "repeat" ->
                "Repeat Last starts the immediately previous Session or Break from its " +
                    "beginning for its FULL length. This is not an Undo-Next action. " +
                    "It returns to the current phase afterward, compensating where " +
                    "possible without moving the approved Class finish. If actual " +
                    "consecutive work plus the repeated Session exceeds 25 minutes " +
                    "without 5 minutes of real rest, user acknowledgement is required. " +
                    "If compensation is impossible, no change is made."
            "restart" ->
                "Restart only the currently active " +
                    (if (isBreak) "Break" else "Session") +
                    ", not the entire Class. Its start clock resets to now, while " +
                    "the Class start and expected finish remain unchanged. The repeat " +
                    "time is balanced against the final two regular Sessions according " +
                    "to the approved ending choice. If the first final Session is already " +
                    "completed, only the second can change. A short future final " +
                    "pair may merge with the intervening break. On success the " +
                    (if (isBreak) "break-end melody B" else "session-end melody A") +
                    " plays. Restart is unavailable in Class summary and rejected " +
                    "without sound if a safe adjustment is impossible."
            else ->
                "Next advances to the next phase of this Class, not another saved Class. " +
                    "Use it even during the final two Sessions: earlier completed work is " +
                    "never rewritten. The live timeline keeps actual past duration; " +
                    "sufficient recovered time inserts a full regular Session and Break " +
                    "before the re-split final two. Next is rejected when it would " +
                    "make the Class finish early, rewrite observed work, or shorten " +
                    "the fixed Summary. Natural entry to Summary plays C once; " +
                    "other successful phase skips play A after a Session and B " +
                    "after a Break. To end the Class early on purpose use Exit. " +
                    "An impossible Next leaves your running and saved Classes unchanged."
        }
        AlertDialog(
            onDismissRequest = { helpFor = null },
            title = { Text(title) },
            text = { Text(explanation) },
            confirmButton = {
                TextButton(colors = atelierTextColors(), onClick = { helpFor = null }) { Text("Close") }
            },
        )
    }

}

/** Elapsed-time tahini bar without Material3's trailing stop-indicator dot. */
@Composable
private fun SolidProgressBar(
    progressFraction: Float,
    fillColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)),
    ) {
        Box(
            modifier = Modifier.fillMaxHeight()
                .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                .background(fillColor),
        )
    }
}

/** Fixed-sized phase digits prevent End seconds from making the card jump. */
@Composable
private fun WallClockRow(
    label: String,
    value: String,
    enlargeLabel: Boolean = false,
    stabilizeTime: Boolean = false,
    compact: Boolean = false,
) {
    val small = MaterialTheme.typography.labelSmall
    Row(
        modifier = Modifier.heightIn(min = if (compact) 20.dp else 24.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = (if (enlargeLabel)
                small.copy(fontSize = small.fontSize * 1.3f) else small)
                .let { if (compact) it.copy(lineHeight = 18.sp) else it },
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            value,
            style = (if (stabilizeTime) MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace
            ) else MaterialTheme.typography.bodyLarge)
                .let { if (compact) it.copy(lineHeight = 20.sp) else it },
            // Monospaced digits keep a ticking clock stable without forcing
            // a wide empty field before shorter values such as "05:00".
            // Every label stays directly beside its value (4dp gap).
            textAlign = TextAlign.End,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** Approved Class length, not the compensated live countdown (hours:minutes). */
private fun formatClassLength(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    return "%02d:%02d".format(safe / 3600, (safe / 60) % 60)
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
