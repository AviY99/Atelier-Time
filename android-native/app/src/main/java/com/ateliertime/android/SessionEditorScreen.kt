package com.ateliertime.android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import java.util.UUID
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.os.SystemClock
import java.util.Calendar
import com.ateliertime.domain.DesignClock
import com.ateliertime.domain.*
import kotlinx.coroutines.delay

/**
 * Offline Editor is the final-authoring and Save interface.
 * Timer owns the saved-class library, Run and real-time countdown.
 * Style is a neutral Material 3 baseline, NOT final branding.
 */
private enum class MainTab { EDITOR, TIMER, SOUNDS, DESIGN, ABOUT }

@Composable
fun SessionEditorScreen() {
    val engine = remember { ScheduleEngine() }
    val context = LocalContext.current
    val catalog = remember(context) {
        PresetCatalog(AndroidPresetStore(context), engine) {
            UUID.randomUUID().toString()
        }
    }
    val designStore = remember(context) { DesignSettingsStore(context) }
    var designPreferences by remember(designStore) {
        mutableStateOf(designStore.load())
    }
    val melodyPreferences = remember(context) { MelodyPreferences(context) }
    var melodySelections by remember(melodyPreferences) {
        mutableStateOf(melodyPreferences.load())
    }
    val melodyPlayer = remember { MelodyAudioPlayer() }
    var melodyError by remember { mutableStateOf<String?>(null) }
    DisposableEffect(melodyPlayer) {
        onDispose { melodyPlayer.close() }
    }
    var selectedPresetId by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("Atelier class") }
    var total by remember { mutableStateOf("180") }
    var summary by remember { mutableStateOf("15") }
    var pose by remember { mutableStateOf("25") }
    var normal by remember { mutableStateOf("5") }
    var special by remember { mutableStateOf("15") }
    var count by remember { mutableStateOf("1") }
    var cycle by remember { mutableStateOf(CycleStart.POSE_FIRST) }
    var automatic by remember { mutableStateOf(true) }
    var manualOrdinals by remember { mutableStateOf("3") }
    var ending by remember { mutableStateOf<FinalPoseChoice?>(null) }
    var acceptedWarning by remember { mutableStateOf(false) }
    var approved by remember { mutableStateOf<ApprovedSession?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var overwriteTarget by remember { mutableStateOf<StoredPreset?>(null) }
    var libraryRevision by remember { mutableIntStateOf(0) }
    var launchRunRequested by remember { mutableStateOf(false) }
    var playingSession by remember { mutableStateOf<ApprovedSession?>(null) }
    var activeTab by remember { mutableStateOf(MainTab.EDITOR) }
    var soundEnabled by remember { mutableStateOf(true) }
    val liveTimer = remember(playingSession) {
        playingSession?.let { session ->
            MonotonicSessionTimer(session) { SystemClock.elapsedRealtime() }
        }
    }
    var timerView by remember(liveTimer) { mutableStateOf(liveTimer?.view()) }
    var wallClockMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val hourOfDay = remember(wallClockMillis / 60_000L) {
        Calendar.getInstance().apply { timeInMillis = wallClockMillis }
            .get(Calendar.HOUR_OF_DAY)
    }
    val designPalette = DesignClock.resolve(designPreferences, hourOfDay)
    val classClockTracker = remember(liveTimer) { ClassClockTracker() }
    var classClockTimes by remember(liveTimer) { mutableStateOf(ClassClockTimes()) }
    var timerError by remember(liveTimer) { mutableStateOf<String?>(null) }
    var helpSection by remember { mutableStateOf<Pair<String, String>?>(null) }
    var nextOpeningId by remember { mutableIntStateOf(1) }
    val opening = remember { mutableStateListOf<OpeningDraft>() }

    fun invalidate() {
        ending = null
        acceptedWarning = false
        approved = null
        saveError = null
        overwriteTarget = null
    }
    // Swap the Editor button examples only. Historical domain fixtures A/B and
    // already-approved saved class signatures must NEVER change.
    fun applyA() {
        selectedPresetId = null
        name = "Example A"
        total = "180"; summary = "15"; pose = "25"
        normal = "5"; special = "15"; count = "1"
        cycle = CycleStart.POSE_FIRST
        automatic = true
        opening.clear()
        invalidate()
    }
    fun applyB() {
        selectedPresetId = null
        name = "Example B"
        total = "180"; summary = "15"; pose = "25"
        normal = "5"; special = "15"; count = "1"
        cycle = CycleStart.BREAK_FIRST
        automatic = true
        opening.clear()
        opening.add(OpeningDraft(nextOpeningId++, "4", "1", "0"))
        opening.add(OpeningDraft(nextOpeningId++, "4", "4", "5"))
        invalidate()
    }

    fun loadPreset(preset: StoredPreset, restored: ApprovedSession) {
        val rules = preset.rules
        selectedPresetId = preset.id
        name = preset.title
        total = (rules.totalSeconds / 60).toString()
        summary = (rules.summarySeconds / 60).toString()
        pose = (rules.poseSeconds / 60).toString()
        normal = (rules.shortBreakSeconds / 60).toString()
        special = (rules.specialBreakSeconds / 60).toString()
        count = rules.specialBreakCount.toString()
        cycle = rules.cycleStart
        automatic = rules.placement.automatic
        manualOrdinals = rules.placement.breakOrdinals.joinToString(",")
        opening.clear()
        rules.openingGroups.forEach {
            opening.add(OpeningDraft(nextOpeningId++,
                it.count.toString(), (it.poseSeconds / 60).toString(),
                it.breakAfterSeconds?.div(60)?.toString(),
                originalGroupId = it.id))
        }
        ending = preset.ending
        acceptedWarning = preset.acknowledgedRestWarnings
        approved = restored
    }

    var parseError: String? = null
    val candidate = try {
        engine.generate(EditorInput(
            name, total, summary, pose, normal, special, count,
            opening.toList(), cycle, automatic, manualOrdinals,
        ).toRules(), ending)
    } catch (error: IllegalArgumentException) {
        parseError = error.message
        null
    }

    // Run from the Timer library starts the actual class without another
    // intermediate approval or 'Start' button.
    LaunchedEffect(liveTimer, launchRunRequested) {
        val clock = liveTimer
        if (clock != null && launchRunRequested) {
            val view = clock.start()
            timerView = view
            classClockTimes = classClockTracker.newClass(view, System.currentTimeMillis())
            timerError = null
            launchRunRequested = false
        }
    }

    LaunchedEffect(liveTimer, melodySelections, soundEnabled) {
        while (true) {
            val nowEpochMs = System.currentTimeMillis()
            wallClockMillis = nowEpochMs
            val clock = liveTimer
            if (clock != null) {
                // Physical Class deadline is immutable. If a long Pause or
                // uncompensated Next leaves virtual phases, finish exactly
                // when the originally approved wall-time deadline is reached.
                val before = clock.view()
                val deadline = classClockTimes.classEndEpochMs
                val deadlineReached = deadline != null && nowEpochMs >= deadline &&
                    (before.status == TimerStatus.RUNNING ||
                        before.status == TimerStatus.PAUSED)
                val tick = if (deadlineReached) clock.finishAtDeadline()
                    else clock.update()
                timerView = tick.view
                classClockTimes = classClockTracker.observe(tick.view, nowEpochMs)
                if (soundEnabled && tick.cue != null) {
                    melodyPlayer.play(melodySelections.forCue(tick.cue)) {
                        message -> melodyError = message
                    }
                }
                if (tick.view.status == TimerStatus.FINISHED) {
                    // Return to Timer's saved-class HOME once the class ends.
                    playingSession = null
                    activeTab = MainTab.TIMER
                }
            }
            delay(200)
        }
    }

    fun timerAction(block: () -> TimerView) {
        try {
            val updated = block()
            timerView = updated
            classClockTimes = classClockTracker.observe(updated, System.currentTimeMillis())
            timerError = null
        } catch (error: IllegalArgumentException) {
            timerError = error.message ?: "Cannot fit the interruption into the final two sessions."
        } catch (error: IllegalStateException) {
            timerError = error.message ?: "Cannot change a session that has already started."
        }
    }

    /** Play only an actually committed timer transition, never a failed action. */
    fun runTimedCue(
        restartCurrentSegment: Boolean = false,
        advancedByNext: Boolean = false,
        action: () -> TimerTick,
    ) {
        try {
            val event = action()
            timerView = event.view
            val nowEpochMs = System.currentTimeMillis()
            classClockTimes = if (restartCurrentSegment) {
                classClockTracker.restartCurrentSegment(event.view, nowEpochMs)
            } else if (advancedByNext) {
                classClockTracker.afterNext(event.view, nowEpochMs)
            } else {
                classClockTracker.observe(event.view, nowEpochMs)
            }
            timerError = null
            if (soundEnabled && event.cue != null) {
                melodyPlayer.play(melodySelections.forCue(event.cue)) {
                    error -> melodyError = error
                }
            }
            if (event.view.status == TimerStatus.FINISHED) {
                playingSession = null
                activeTab = MainTab.TIMER
            }
        } catch (error: IllegalArgumentException) {
            timerError = error.message ?: "The final sessions cannot absorb this time change."
        } catch (error: IllegalStateException) {
            timerError = error.message ?: "This phase cannot be skipped safely."
        }
    }

    fun finalSave(overwriteId: String?) {
        val current = candidate ?: return
        try {
            val exact = engine.approve(current, acknowledgeWarnings = acceptedWarning)
            val record = catalog.save(exact, name, overwriteId = overwriteId)
            selectedPresetId = record.id
            approved = catalog.open(record.id)
            overwriteTarget = null
            saveError = null
            libraryRevision += 1
            // The Timer tab is the home for saved classes, not an auto-run screen.
            activeTab = MainTab.TIMER
        } catch (failure: Exception) {
            saveError = failure.message ?: "The class could not be saved."
        }
    }

    fun requestFinalSave() {
        try {
            val id = ClassSaveTarget.existingId(catalog.state().entries, name)
            val existing = catalog.state().entries.firstOrNull { it.id == id }
            if (existing == null) finalSave(null)
            else overwriteTarget = existing
        } catch (failure: Exception) {
            saveError = failure.message ?: "Cannot read saved classes."
        }
    }

    // Keep the tab bar BELOW Android status icons/notch and content ABOVE
    // gesture navigation in edge-to-edge mode. All tabs share these insets.
    AtelierDesignTheme(designPalette) {
    Column(modifier = Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .safeDrawingPadding()) {
        // Non-scrolling 5-way navigation. LTR is deliberate, including on
        // Hebrew/RTL devices: Editor | Timer | Sound | Design | About.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            TabRow(selectedTabIndex = activeTab.ordinal,
                modifier = Modifier.fillMaxWidth()) {
                MainTab.entries.forEach { tab ->
                    val label = when (tab) {
                        MainTab.EDITOR -> "Editor"
                        MainTab.TIMER -> "Timer"
                        MainTab.SOUNDS -> "Sound"
                        MainTab.DESIGN -> "Design"
                        MainTab.ABOUT -> "About"
                    }
                    Tab(
                        selectedContentColor = if (atelierNightActive()) Color.White else MaterialTheme.colorScheme.primary,
                        unselectedContentColor = if (atelierNightActive()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        text = {
                            Text(label, fontSize = 11.sp,
                                maxLines = 1, softWrap = false,
                                overflow = TextOverflow.Clip)
                        },
                    )
                }
            }
        }
        when (activeTab) {
            MainTab.DESIGN -> DesignSettingsPanel(
                settings = designPreferences,
                currentPalette = designPalette,
                onChange = { updated ->
                    designPreferences = updated
                    designStore.save(updated)
                },
            )
            MainTab.ABOUT -> AtelierAboutPanel()
            MainTab.EDITOR -> Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Atelier Time", style = MaterialTheme.typography.headlineMedium)
        Text("Class editor · Android", style = MaterialTheme.typography.titleMedium)
        Text("Changes update the preview. Save approves and stores this class.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(colors = atelierFilledColors(), colors = atelierOutlinedColors(), onClick = { applyA() }) { Text("Example A") }
            OutlinedButton(colors = atelierFilledColors(), colors = atelierOutlinedColors(), onClick = { applyB() }) { Text("Example B") }
        }
        OutlinedTextField(colors = atelierInputColors(), 
            value = name, onValueChange = { name = it; invalidate() },
            label = { Text("Class title") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MinuteInput("Total class duration (min)", total, { total = it; invalidate() }, Modifier.weight(1f))
            MinuteInput("End-of-class summary (min)", summary, { summary = it; invalidate() }, Modifier.weight(1f))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Opening session groups", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f))
            TextButton(colors = atelierFilledColors(), colors = atelierTextColors(), onClick = {
                helpSection = "Opening session groups" to
                    "Each group is an ordered set of equal-duration opening sessions. " +
                    "Every group has a configurable break after it. Zero minutes is allowed " +
                    "between groups; the last group requires at least one minute. " +
                    "A break-first regular cycle can share its initial break with the " +
                    "last group's configured rest, so it is not counted twice."
            }) { Text("Help") }
        }
        opening.toList().forEachIndexed { index, group ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Opening session group " + (index + 1), modifier = Modifier.weight(1f))
                TextButton(colors = atelierFilledColors(), colors = atelierTextColors(), onClick = {
                    helpSection = "Opening session group " + (index + 1) to
                        "Number of sessions means how many consecutive work sessions this group contains. " +
                        "Each session duration sets the time of one work session in minutes. " +
                        "Set Break after group to 0 for no break between opening groups. " +
                        "The last group requires at least 1 minute of rest. If it equals " +
                        "the initial regular break, only one break occurs."
                }) { Text("Help") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MinuteInput("Number of sessions", group.count, {
                    opening[index] = group.copy(count = it); invalidate()
                }, Modifier.weight(1f))
                MinuteInput("Each session duration (min)", group.minutes, {
                    opening[index] = group.copy(minutes = it); invalidate()
                }, Modifier.weight(1f))
            }
            MinuteInput(
                "Break after group (min)" +
                    if (index == opening.lastIndex) " · required" else " · 0 allowed",
                group.breakAfterMinutes ?: if (
                    index == opening.lastIndex && cycle == CycleStart.BREAK_FIRST
                ) normal else "0",
                { opening[index] = group.copy(breakAfterMinutes = it); invalidate() },
                Modifier.fillMaxWidth(),
            )
            if (group.breakAfterMinutes == null) {
                Text(
                    "Legacy saved group: this value is a display suggestion. " +
                        "Edit the break field to apply the new rule.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(colors = atelierFilledColors(), colors = atelierTextColors(), onClick = {
                    if (index > 0) {
                        opening[index] = opening[index - 1].also { opening[index - 1] = group }
                        invalidate()
                    }
                }, enabled = index > 0) { Text("Up") }
                TextButton(colors = atelierFilledColors(), colors = atelierTextColors(), onClick = {
                    if (index < opening.lastIndex) {
                        opening[index] = opening[index + 1].also { opening[index + 1] = group }
                        invalidate()
                    }
                }, enabled = index < opening.lastIndex) { Text("Down") }
                TextButton(colors = atelierFilledColors(), colors = atelierTextColors(), onClick = { opening.removeAt(index); invalidate() }) {
                    Text("Remove")
                }
            }
        }
        OutlinedButton(colors = atelierFilledColors(), colors = atelierOutlinedColors(), onClick = {
            opening.add(OpeningDraft(nextOpeningId++, breakAfterMinutes = normal)); invalidate()
        }) { Text("Add opening session group") }

        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Regular sessions", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f))
            TextButton(colors = atelierFilledColors(), colors = atelierTextColors(), onClick = {
                helpSection = "Regular sessions" to
                    "One repeating work session lasts the configured number of minutes. " +
                    "It is separate from regular and special break settings."
            }) { Text("Help") }
        }
        MinuteInput("Regular session duration (min)", pose,
            { pose = it; invalidate() }, Modifier.fillMaxWidth())

        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Regular breaks", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f))
            TextButton(colors = atelierFilledColors(), colors = atelierTextColors(), onClick = {
                helpSection = "Regular breaks" to
                    "Regular breaks repeat between work sessions. Set their normal duration " +
                    "here, independently of special-break duration and placement. " +
                    "An opening-group break is configured next to its opening group."
            }) { Text("Help") }
        }
        MinuteInput("Regular break duration (min)", normal,
            { normal = it; invalidate() }, Modifier.fillMaxWidth())
        Text("Standard repeating order", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(colors = atelierChipColors(), 
                selected = cycle == CycleStart.POSE_FIRST,
                onClick = { cycle = CycleStart.POSE_FIRST; invalidate() },
                label = { Text("Session first") },
            )
            FilterChip(colors = atelierChipColors(), 
                selected = cycle == CycleStart.BREAK_FIRST,
                onClick = { cycle = CycleStart.BREAK_FIRST; invalidate() },
                label = { Text("Break first") },
            )
        }

        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Special breaks", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f))
            TextButton(colors = atelierFilledColors(), colors = atelierTextColors(), onClick = {
                helpSection = "Special breaks" to
                    "Special breaks REPLACE selected numbered regular breaks; they are " +
                    "not extra breaks. Choose their own duration, count and automatic " +
                    "or manual placement. Manual positions refer only to numbered " +
                    "regular-break slots, not opening-group rests."
            }) { Text("Help") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MinuteInput("Special break duration (min)", special,
                { special = it; invalidate() }, Modifier.weight(1f))
            MinuteInput("Number of special breaks", count,
                { count = it; invalidate() }, Modifier.weight(1f))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Automatic special-break placement", modifier = Modifier.weight(1f))
            Switch(checked = automatic,
                onCheckedChange = { automatic = it; invalidate() })
        }
        if (!automatic && count.toIntOrNull() != 0) {
            OutlinedTextField(colors = atelierInputColors(), 
                value = manualOrdinals,
                onValueChange = { manualOrdinals = it; invalidate() },
                label = { Text("Regular-break numbers, e.g. 2,4") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Complete class timeline", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f))
            TextButton(colors = atelierFilledColors(), colors = atelierTextColors(), onClick = {
                helpSection = "Complete class timeline" to
                    "All timestamps are elapsed time from class start, not wall-clock time. " +
                    "The total class includes the end-of-class summary. A rest advisory " +
                    "after more than 25 minutes of cumulative sessions without a qualifying " +
                    "5-minute break requires acknowledgment before Save."
            }) { Text("Help") }
        }
        if (parseError != null) {
            Text(parseError ?: "", color = MaterialTheme.colorScheme.error)
        } else if (candidate != null) {
            candidate.issues.filter { it.severity == IssueSeverity.ERROR }.forEach {
                Text(it.message, color = MaterialTheme.colorScheme.error)
            }
            if (candidate.finalPoseIsShort && !candidate.hasBlockingIssues) {
                Text("Final regular session: choose an ending", style = MaterialTheme.typography.titleMedium)
                Text("Last gets remaining time: keep the first of the final two " +
                    "sessions at its planned length when possible; give the " +
                    "remaining time to the last. Even splits the time equally; " +
                    "Custom lets you choose the split.",
                    style = MaterialTheme.typography.bodySmall)
                // Keep all three ending choices visible without horizontal scrolling.
                // The longest option deliberately occupies two text lines on phones.
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(colors = atelierChipColors(), 
                        modifier = Modifier.weight(1.5f),
                        selected = ending?.mode == FinalPoseMode.REMAINDER,
                        onClick = {
                            ending = FinalPoseChoice.remainder()
                            acceptedWarning = false; approved = null
                        },
                        label = {
                            Text("Last gets\nremaining time",
                                maxLines = 2, textAlign = TextAlign.Center)
                        },
                    )
                    FilterChip(colors = atelierChipColors(), 
                        modifier = Modifier.weight(0.8f),
                        selected = ending?.mode == FinalPoseMode.EVEN_SPLIT,
                        enabled = candidate.canSplitFinalTwo,
                        onClick = {
                            ending = FinalPoseChoice.even()
                            acceptedWarning = false; approved = null
                        },
                        label = { Text("Even") },
                    )
                    FilterChip(colors = atelierChipColors(), 
                        modifier = Modifier.weight(1.1f),
                        selected = ending?.mode == FinalPoseMode.CUSTOM_SPLIT,
                        enabled = candidate.canSplitFinalTwo,
                        onClick = {
                            val regular = candidate.phases.filter {
                                it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
                            }
                            val minutes = if (regular.size >= 2)
                                (regular.takeLast(2).sumOf { it.durationSeconds } / 120)
                            else 1
                            ending = FinalPoseChoice.custom(minutes * SECONDS_PER_MINUTE)
                            acceptedWarning = false; approved = null
                        },
                        label = { Text("Custom") },
                    )
                }
                if (ending?.mode == FinalPoseMode.CUSTOM_SPLIT &&
                    candidate.canSplitFinalTwo
                ) {
                    val regular = candidate.phases.filter {
                        it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
                    }
                    val available = regular.takeLast(2).sumOf { it.durationSeconds } / 60
                    if (available > 2) {
                        val first = ((ending?.firstPoseSeconds ?: 60) / 60)
                            .coerceIn(1, available - 1)
                        Text("First session: " + first + " min; last: " +
                            (available - first) + " min")
                        Slider(
                            value = first.toFloat(),
                            onValueChange = {
                                ending = FinalPoseChoice.custom(
                                    it.toInt().coerceIn(1, available - 1) * 60
                                )
                                acceptedWarning = false; approved = null
                            },
                            valueRange = 1f..(available - 1).toFloat(),
                            steps = (available - 2).coerceAtLeast(0),
                        )
                    }
                }
            }
            if (!candidate.hasBlockingIssues) {
                val warningCount = candidate.issues.count {
                    it.severity == IssueSeverity.WARNING
                }
                if (warningCount > 0) {
                    candidate.issues.filter { it.severity == IssueSeverity.WARNING }.forEach {
                        Text(
                            "Rest recommendation: " + it.message,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = acceptedWarning, onCheckedChange = {
                            acceptedWarning = it; approved = null
                        })
                        Text("I acknowledge the 25/5 rest warning")
                    }
                }
                Text(
                    "Sessions: " + candidate.phases.filter {
                        it.kind == PhaseKind.POSE
                    }.sumOf { it.durationSeconds } / 60 +
                        " min · Rest: " + candidate.phases.filter {
                            it.kind == PhaseKind.SHORT_BREAK ||
                                it.kind == PhaseKind.SPECIAL_BREAK
                        }.sumOf { it.durationSeconds } / 60 + " min",
                )
                candidate.phases.forEach { phase ->
                    val label = when (phase.kind) {
                        PhaseKind.POSE -> "Session " + phase.poseNumber
                        PhaseKind.SHORT_BREAK ->
                            if (phase.openingGroupId != null)
                                "Break · opening group"
                            else "Break · regular #" + phase.breakOrdinal
                        PhaseKind.SPECIAL_BREAK -> "Break · special #" + phase.breakOrdinal
                        PhaseKind.SUMMARY -> "End-of-class summary"
                    }
                    Text(
                        formatOffset(phase.startSeconds) + "–" +
                            formatOffset(phase.endSeconds) + "  " + label +
                            "  (" + phase.durationSeconds / 60 + " min)",
                    )
                }
                TimelinePhaseChart(candidate.phases)
                saveError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Button(colors = atelierFilledColors(), 
                    onClick = { requestFinalSave() },
                    enabled = candidate.canApprove &&
                        (!candidate.needsSafetyAcknowledgement || acceptedWarning),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save class")
                }
                Text("Save approves the exact timeline and stores the class offline. " +
                    "If another saved class already has this name, you must " +
                    "confirm before replacing it.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
            MainTab.SOUNDS -> Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MelodySettingsSection(
                    selections = melodySelections,
                    onSelectionsChange = { updated ->
                        try {
                            melodyPreferences.save(updated)
                            melodySelections = updated
                            melodyError = null
                        } catch (error: Exception) {
                            melodyError = error.message ?: "Unable to save sound settings."
                        }
                    },
                    onPreview = { melody ->
                        melodyPlayer.play(melody) { message -> melodyError = message }
                    },
                )
                if (melodyError != null) {
                    Text(melodyError ?: "", color = MaterialTheme.colorScheme.error)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Automatic timer alerts", modifier = Modifier.weight(1f))
                    Switch(checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it })
                }
                Text("You can change melodies while a timer is running. The clock " +
                    "continues in the Timer tab and the new choices apply at the next cue.")
            }
            MainTab.TIMER -> {
                val session = playingSession
                val timer = liveTimer
                val view = timerView
                if (session == null || timer == null || view == null) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Timer · saved classes",
                            style = MaterialTheme.typography.headlineSmall)
                        PresetLibrarySection(
                            catalog = catalog,
                            refreshKey = libraryRevision,
                            onEdit = { record, restored ->
                                loadPreset(record, restored)
                                activeTab = MainTab.EDITOR
                            },
                            onRun = { record, restored ->
                                selectedPresetId = record.id
                                playingSession = restored
                                launchRunRequested = true
                                activeTab = MainTab.TIMER
                            },
                            onDeleted = { id ->
                                if (selectedPresetId == id) {
                                    selectedPresetId = null
                                    approved = null
                                }
                                libraryRevision += 1
                            },
                        )
                    }
                } else {
                    SessionTimerScreen(
                        session = session,
                        timer = timer,
                        view = view,
                        wallClockMillis = wallClockMillis,
                        classClockTimes = classClockTimes,
                        timerError = timerError,
                        designPalette = designPalette,
                        onStart = {
                            timerAction { timer.start() }
                            timerView?.takeIf {
                                it.status == TimerStatus.RUNNING && it.elapsedSeconds == 0
                            }?.let { startingView ->
                                classClockTimes = classClockTracker.newClass(
                                    startingView, System.currentTimeMillis()
                                )
                            }
                        },
                        onPauseResume = {
                            timerAction {
                                if (timer.view().status == TimerStatus.PAUSED ||
                                    timer.view().status == TimerStatus.STOPPED)
                                    timer.resume()
                                else timer.pause()
                            }
                        },
                        onRestartSession = {
                            val currentKind = timer.view().phase?.kind
                            runTimedCue(restartCurrentSegment = true) {
                                val updated = timer.restartCurrentSegmentKeepingEndTime()
                                val cue = when (currentKind) {
                                    PhaseKind.POSE -> TimerCue.POSE_FINISHED
                                    PhaseKind.SHORT_BREAK, PhaseKind.SPECIAL_BREAK ->
                                        TimerCue.BREAK_FINISHED
                                    else -> null
                                }
                                TimerTick(updated, cue)
                            }
                        },
                        onNextPhase = {
                            runTimedCue(advancedByNext = true) { timer.nextPhase() }
                        },
                        onExit = {
                            playingSession = null
                            launchRunRequested = false
                            activeTab = MainTab.TIMER
                        },
                    )
                }
            }
        }
    }

    overwriteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { overwriteTarget = null },
            title = { Text("Replace saved class?") },
            text = { Text("A saved class named '${target.title}' already exists. " +
                "Saving now will overwrite that class and its approved timeline. " +
                "This cannot be undone. Continue?") },
            confirmButton = {
                TextButton(colors = atelierFilledColors(), colors = atelierTextColors(), onClick = { finalSave(target.id) }) {
                    Text("Overwrite class")
                }
            },
            dismissButton = {
                TextButton(colors = atelierFilledColors(), colors = atelierTextColors(), onClick = { overwriteTarget = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    val openHelp = helpSection
    if (openHelp != null) {
        AlertDialog(
            onDismissRequest = { helpSection = null },
            title = { Text(openHelp.first) },
            text = { Text(openHelp.second) },
            confirmButton = {
                TextButton(colors = atelierFilledColors(), colors = atelierTextColors(), onClick = { helpSection = null }) { Text("Close") }
            },
        )
    }
    }
}

@Composable
private fun MinuteInput(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(colors = atelierInputColors(), 
        value = value, onValueChange = onChange,
        label = { Text(label) }, modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
    )
}

private fun formatOffset(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds / 60) % 60
    return hours.toString().padStart(2, '0') + ":" +
        minutes.toString().padStart(2, '0')
}
