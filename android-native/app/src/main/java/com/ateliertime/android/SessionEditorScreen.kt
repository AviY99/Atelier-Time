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
import androidx.compose.ui.platform.testTag
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
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.WindowManager
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.ateliertime.domain.DesignClock
import com.ateliertime.domain.*
import kotlinx.coroutines.delay

/**
 * Offline Editor is the final-authoring and Save interface.
 * Timer owns the saved-class library, Run and real-time countdown.
 * Style is a neutral Material 3 baseline, NOT final branding.
 */
private enum class MainTab { EDITOR, TIMER, FREE_TIMER, SOUNDS, DESIGN, ABOUT }

/** Pending approval is ephemeral: no Class clock or sound starts before Confirm. */
private data class PendingRunApproval(
    val presetId: String,
    val session: ApprovedSession,
)

@Composable
fun SessionEditorScreen(
    // Android UI tests may inject isolated stores; production defaults unchanged.
    presetStoreOverride: AndroidPresetStore? = null,
    melodyPreferencesOverride: MelodyPreferences? = null,
) {
    val engine = remember { ScheduleEngine() }
    val context = LocalContext.current
    val classStatusNotification = remember(context) { ActiveClassStatusNotification(context) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* If accepted, the live Timer tick posts the status notification. */ }
    fun requestClassStatusNotificationPermission() {
        // Instrumentation UI tests use isolated data and do not prompt for
        // runtime notification permissions on the user's actual app.
        if (presetStoreOverride != null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    DisposableEffect(classStatusNotification) {
        onDispose { classStatusNotification.clear() }
    }
    val catalog = remember(context, presetStoreOverride) {
        PresetCatalog(presetStoreOverride ?: AndroidPresetStore(context), engine) {
            UUID.randomUUID().toString()
        }
    }
    val designStore = remember(context) { DesignSettingsStore(context) }
    var designPreferences by remember(designStore) {
        mutableStateOf(designStore.load())
    }
    var autoPreviewHour by remember { mutableStateOf<Int?>(null) }
    val melodyPreferences = remember(context, melodyPreferencesOverride) {
        melodyPreferencesOverride ?: MelodyPreferences(context)
    }
    var soundSettings by remember(melodyPreferences) {
        mutableStateOf(melodyPreferences.loadSettings())
    }
    val melodyPlayer = remember(context) { MelodyAudioPlayer(context) }
    val freeMelodyPlayer = remember(context) { MelodyAudioPlayer(context) }
    var melodyError by remember { mutableStateOf<String?>(null) }
    DisposableEffect(melodyPlayer) {
        onDispose { melodyPlayer.close() }
    }
    DisposableEffect(freeMelodyPlayer) {
        onDispose { freeMelodyPlayer.close() }
    }
    fun saveSoundSettings(updated: SoundSettings) {
        try {
            melodyPreferences.saveSettings(updated)
            soundSettings = updated
            melodyError = null
        } catch (failure: Exception) {
            melodyError = failure.message ?: "Unable to save sound settings."
        }
    }

    // The system chooser and the document provider own their respective lists:
    // no storage scan, no filesystem path access, no broad media permission.
    var pickingCue by remember { mutableStateOf<TimerCue?>(null) }
    val androidSoundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val cue = pickingCue
        pickingCue = null
        if (result.resultCode == Activity.RESULT_OK && cue != null) {
            @Suppress("DEPRECATION")
            val uri: Uri? = result.data?.getParcelableExtra(
                RingtoneManager.EXTRA_RINGTONE_PICKED_URI
            )
            if (uri == null) {
                melodyError = "Choose an audible system sound."
            } else {
                val title = runCatching {
                    RingtoneManager.getRingtone(context, uri)?.getTitle(context)
                }.getOrNull() ?: "Android system sound"
                saveSoundSettings(soundSettings.chooseForVisibleMode(cue, CueSound(
                    source = SoundSource.SYSTEM,
                    melody = soundSettings.soundFor(cue).melody,
                    uri = uri.toString(), displayName = title,
                )))
            }
        }
    }
    val documentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        val cue = pickingCue
        pickingCue = null
        if (uri != null && cue != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val title = context.contentResolver.query(
                    uri, arrayOf(OpenableColumns.DISPLAY_NAME),
                    null, null, null,
                )?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                } ?: uri.lastPathSegment ?: "Audio file"
                saveSoundSettings(soundSettings.chooseForVisibleMode(cue, CueSound(
                    source = SoundSource.FILE,
                    melody = soundSettings.soundFor(cue).melody,
                    uri = uri.toString(), displayName = title,
                )))
            } catch (failure: Exception) {
                melodyError = failure.message ?: "Cannot retain access to this audio file."
            }
        }
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
    var runStartTargetEpochMs by remember { mutableStateOf<Long?>(null) }
    var runFollowSuggestedFinish by remember { mutableStateOf(false) }
    var pendingRunApproval by remember { mutableStateOf<PendingRunApproval?>(null) }
    var customFinishEnabled by remember { mutableStateOf(false) }
    var finishPickerVisible by remember { mutableStateOf(false) }
    var customFinishHour by remember { mutableIntStateOf(0) }
    var customFinishMinute by remember { mutableIntStateOf(0) }
    var runApprovalError by remember { mutableStateOf<String?>(null) }
    var runFinishBlockedReason by remember { mutableStateOf<String?>(null) }
    var playingSession by remember { mutableStateOf<ApprovedSession?>(null) }
    var activeTab by remember { mutableStateOf(MainTab.EDITOR) }
    var soundEnabled by remember { mutableStateOf(true) }
    var keepScreenAwake by remember { mutableStateOf(false) }
    val freeTimer = remember { FreeCountdownTimer { SystemClock.elapsedRealtime() } }
    var freeTimerView by remember { mutableStateOf(freeTimer.view()) }
    var freeTimerError by remember { mutableStateOf<String?>(null) }
    val eventHistory = remember { mutableStateListOf<String>() }
    fun recordClassEvent(source: String, before: TimerView?, after: TimerView,
                         cue: TimerCue? = null) {
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            .format(Date(System.currentTimeMillis()))
        val from = before?.phase?.id ?: "—"
        val to = after.phase?.id ?: "end"
        val cueNote = cue?.let {
            " · ${it.name} " + (if (soundEnabled) "play requested" else "alerts disabled")
        } ?: ""
        eventHistory.add("$stamp · ${SystemClock.elapsedRealtime()}ms · $source: " +
            "$from → $to$cueNote")
        while (eventHistory.size > 30) eventHistory.removeAt(0)
    }
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
    val realClockPalette = DesignClock.resolve(designPreferences, hourOfDay)
    // The slider must preview Day on the LEFT and Night on the RIGHT,
    // regardless of the real clock. Everywhere outside Design the app still
    // follows the user's saved automatic hour, including overnight.
    val designPalette = if (activeTab == MainTab.DESIGN &&
        designPreferences.mode == com.ateliertime.domain.DesignMode.AUTO) {
        DesignClock.sliderPreview(autoPreviewHour ?: designPreferences.nightStartsAtHour)
    } else realClockPalette
    val classClockTracker = remember(liveTimer) {
        ClassClockTracker { SystemClock.elapsedRealtime() }
    }
    var classClockTimes by remember(liveTimer) { mutableStateOf(ClassClockTimes()) }
    var timerError by remember(liveTimer) { mutableStateOf<String?>(null) }
    val activity = context as? Activity
    val activeClass = timerView?.status == TimerStatus.RUNNING ||
        timerView?.status == TimerStatus.PAUSED
    val activeFree = freeTimerView.status == FreeTimerStatus.RUNNING ||
        freeTimerView.status == FreeTimerStatus.PAUSED
    DisposableEffect(activity, keepScreenAwake, activeClass, activeFree) {
        if (keepScreenAwake && (activeClass || activeFree)) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    val mediaVolumeMuted = remember(wallClockMillis / 1_000L) {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
    }
    val classAlertVolumeMuted = remember(wallClockMillis / 1_000L,
        soundSettings.classAlertsUseAlarmVolume) {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val stream = if (soundSettings.classAlertsUseAlarmVolume)
            AudioManager.STREAM_ALARM else AudioManager.STREAM_MUSIC
        audio.getStreamVolume(stream) == 0
    }
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

    // Run starts only AFTER explicit end-clock approval. Never auto-start
    // while the approval dialog is visible or after its cancellation.
    LaunchedEffect(liveTimer, launchRunRequested) {
        val clock = liveTimer
        if (clock != null && launchRunRequested) {
            try {
                val startEpochMs = System.currentTimeMillis()
                val selectedTarget = runStartTargetEpochMs
                    ?: throw IllegalArgumentException("No confirmed Class finish time.")
                val session = playingSession
                    ?: throw IllegalArgumentException("No approved Class selected.")
                // The calculated finish keeps advancing while approval is open.
                // Recompute it at the actual physical start for ALL Class shapes.
                val target = if (runFollowSuggestedFinish)
                    RunEndTimePlanner.suggestedEndEpochMs(session, startEpochMs)
                else selectedTarget
                // Re-evaluate at ACTUAL start, not button press, to keep the
                // accepted wall-clock deadline after UI/scheduling latency.
                val correction = RunEndTimePlanner.plan(
                    session, startEpochMs, target
                ).correctionSeconds
                val view = clock.start(correction)
                eventHistory.clear()
                recordClassEvent("Start", null, view)
                timerView = view
                classClockTimes = classClockTracker.newClass(view, startEpochMs, target)
                timerError = null
            } catch (error: IllegalArgumentException) {
                val session = playingSession
                val presetId = selectedPresetId
                if (session != null && presetId != null) {
                    pendingRunApproval = PendingRunApproval(presetId, session)
                    runApprovalError = error.message ?: "Choose another finish time."
                }
                playingSession = null
            } finally {
                launchRunRequested = false
                runStartTargetEpochMs = null
                runFollowSuggestedFinish = false
            }
        }
    }

    LaunchedEffect(liveTimer, freeTimer, soundSettings, soundEnabled) {
        while (true) {
            val nowEpochMs = System.currentTimeMillis()
            wallClockMillis = nowEpochMs
            val clock = liveTimer
            if (clock != null) {
                // Physical Class deadline is immutable. If a long Pause or
                // uncompensated Next leaves virtual phases, finish exactly
                // when the originally approved wall-time deadline is reached.
                val before = clock.view()
                val deadlineReached = classClockTracker.hasReachedClassDeadline(nowEpochMs) &&
                    (before.status == TimerStatus.RUNNING ||
                        before.status == TimerStatus.PAUSED)
                val tick = if (deadlineReached) clock.finishAtDeadline()
                    else clock.update()
                if (before.phase?.id != tick.view.phase?.id ||
                    before.status != tick.view.status || tick.cue != null) {
                    recordClassEvent(if (deadlineReached) "Deadline" else "Automatic",
                        before, tick.view, tick.cue)
                }
                timerView = tick.view
                classClockTimes = classClockTracker.observe(tick.view, nowEpochMs)
                classStatusNotification.update(
                    playingSession?.rules?.name ?: "Class",
                    tick.view,
                    classClockTimes.classRemainingSeconds ?: tick.view.totalRemainingSeconds,
                    nowEpochMs,
                )
                if (soundEnabled && tick.cue != null) {
                    melodyPlayer.play(soundSettings.soundFor(tick.cue),
                        soundSettings.secondsFor(tick.cue),
                        useAlarmVolume = soundSettings.classAlertsUseAlarmVolume) { message ->
                        melodyError = message
                    }
                }
                if (tick.view.status == TimerStatus.FINISHED) {
                    classStatusNotification.clear()
                    // Return to Timer's saved-class HOME once the class ends.
                    playingSession = null
                    activeTab = MainTab.TIMER
                }
            }
            val freeTick = freeTimer.update()
            freeTimerView = freeTick.view
            if (freeTick.completedNow && soundEnabled) {
                freeMelodyPlayer.play(
                    soundSettings.soundFor(TimerCue.FREE_TIMER_FINISHED),
                    soundSettings.secondsFor(TimerCue.FREE_TIMER_FINISHED),
                ) { message -> freeTimerError = message }
            }
            delay(200)
        }
    }

    fun timerAction(block: () -> TimerView) {
        try {
            val before = liveTimer?.view()
            val updated = block()
            recordClassEvent("Pause/Resume", before, updated)
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
            val before = liveTimer?.view()
            val event = action()
            recordClassEvent(if (restartCurrentSegment) "Restart" else "Next",
                before, event.view, event.cue)
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
                melodyPlayer.play(soundSettings.soundFor(event.cue),
                    soundSettings.secondsFor(event.cue),
                    useAlarmVolume = soundSettings.classAlertsUseAlarmVolume) { error ->
                    melodyError = error
                }
            }
            if (event.view.status == TimerStatus.FINISHED) {
                classStatusNotification.clear()
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
    // A plain Column does NOT set Material3 LocalContentColor; text outside
    // Cards inherited the Android default (black) even on Night. Surface
    // supplies the correct onBackground color for all root sections.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        // Non-scrolling six-way navigation, LTR even in Hebrew/RTL:
        // Editor | Timer | Free | Sound | Design | About.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            TabRow(selectedTabIndex = activeTab.ordinal,
                modifier = Modifier.fillMaxWidth()) {
                MainTab.entries.forEach { tab ->
                    val label = when (tab) {
                        MainTab.EDITOR -> "Editor"
                        MainTab.TIMER -> "Timer"
                        MainTab.FREE_TIMER -> "Free"
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
                currentClockMillis = wallClockMillis,
                onPreviewHourChange = { autoPreviewHour = it },
                onChange = { updated ->
                    designPreferences = updated
                    designStore.save(updated)
                    autoPreviewHour = null
                },
            )
            MainTab.ABOUT -> AtelierAboutPanel()
            MainTab.FREE_TIMER -> Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Free timer", style = MaterialTheme.typography.headlineSmall)
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Keep screen awake during timers", modifier = Modifier.weight(1f))
                    Switch(checked = keepScreenAwake,
                        onCheckedChange = { keepScreenAwake = it },
                        modifier = Modifier.testTag("free_keep_screen_awake"))
                }
                if (mediaVolumeMuted) Text("Media volume is at zero. Free timer " +
                    "completion may be silent.", color = atelierErrorTextColor())
                FreeCountdownSection(
                    view = freeTimerView,
                    onStart = { hours, minutes, seconds ->
                        freeMelodyPlayer.stop()
                        runCatching { freeTimer.start(hours, minutes, seconds) }
                            .onSuccess { freeTimerView = it; freeTimerError = null }
                            .onFailure { freeTimerError = it.message }
                    },
                    onPauseResume = {
                        freeTimerView = if (freeTimerView.status == FreeTimerStatus.RUNNING)
                            freeTimer.pause() else freeTimer.resume()
                    },
                    onExit = {
                        freeMelodyPlayer.stop()
                        freeTimerView = freeTimer.exit()
                        freeTimerError = null
                    },
                )
                freeTimerError?.let { Text(it, color = atelierErrorTextColor()) }
                Text("Completion sound: Sounds → Same sound for all / Free countdown.",
                    style = MaterialTheme.typography.bodySmall)
            }
            MainTab.EDITOR -> Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Atelier Time", style = MaterialTheme.typography.headlineMedium)
        Text("Class editor · Android", style = MaterialTheme.typography.titleMedium)
        Text("Changes update the preview. Save approves and stores this class.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(colors = atelierOutlinedColors(), onClick = { applyA() }) { Text("Example A") }
            OutlinedButton(colors = atelierOutlinedColors(), onClick = { applyB() }) { Text("Example B") }
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
            TextButton(colors = atelierTextColors(), onClick = {
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
                TextButton(colors = atelierTextColors(), onClick = {
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
                TextButton(colors = atelierTextColors(), onClick = {
                    if (index > 0) {
                        opening[index] = opening[index - 1].also { opening[index - 1] = group }
                        invalidate()
                    }
                }, enabled = index > 0) { Text("Up") }
                TextButton(colors = atelierTextColors(), onClick = {
                    if (index < opening.lastIndex) {
                        opening[index] = opening[index + 1].also { opening[index + 1] = group }
                        invalidate()
                    }
                }, enabled = index < opening.lastIndex) { Text("Down") }
                TextButton(colors = atelierTextColors(), onClick = { opening.removeAt(index); invalidate() }) {
                    Text("Remove")
                }
            }
        }
        OutlinedButton(colors = atelierOutlinedColors(), onClick = {
            opening.add(OpeningDraft(nextOpeningId++, breakAfterMinutes = normal)); invalidate()
        }) { Text("Add opening session group") }

        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Regular sessions", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f))
            TextButton(colors = atelierTextColors(), onClick = {
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
            TextButton(colors = atelierTextColors(), onClick = {
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
            TextButton(colors = atelierTextColors(), onClick = {
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
            TextButton(colors = atelierTextColors(), onClick = {
                helpSection = "Complete class timeline" to
                    "All timestamps are elapsed time from class start, not wall-clock time. " +
                    "The total class includes the end-of-class summary. A rest advisory " +
                    "after more than 25 minutes of cumulative sessions without a qualifying " +
                    "5-minute break requires acknowledgment before Save."
            }) { Text("Help") }
        }
        if (parseError != null) {
            Text(parseError ?: "", color = atelierErrorTextColor())
        } else if (candidate != null) {
            candidate.issues.filter { it.severity == IssueSeverity.ERROR }.forEach {
                Text(it.message, color = atelierErrorTextColor())
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
                            color = atelierErrorTextColor(),
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
                // Keep the chronological text timeline compact without changing
                // its timestamps, phase labels, order, or the chart below it.
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                TimelinePhaseChart(candidate.phases)
                saveError?.let {
                    Text(it, color = atelierErrorTextColor())
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MelodySettingsSection(
                    settings = soundSettings,
                    onSettingsChange = { updated -> saveSoundSettings(updated) },
                    onChooseSystemSound = { cue ->
                        pickingCue = cue
                        val selected = soundSettings.soundFor(cue)
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                                RingtoneManager.TYPE_ALL)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                                "Select Android sound")
                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                if (selected.source == SoundSource.SYSTEM)
                                    selected.uri?.let { Uri.parse(it) }
                                else RingtoneManager.getDefaultUri(
                                    RingtoneManager.TYPE_NOTIFICATION))
                        }
                        runCatching { androidSoundPicker.launch(intent) }
                            .onFailure {
                                pickingCue = null
                                melodyError = it.message ?: "Android sound picker unavailable."
                            }
                    },
                    onChooseAudioFile = { cue ->
                        pickingCue = cue
                        runCatching { documentPicker.launch(arrayOf("audio/*")) }
                            .onFailure {
                                pickingCue = null
                                melodyError = it.message ?: "Audio file picker unavailable."
                            }
                    },
                    onPreview = { cue ->
                        melodyPlayer.play(soundSettings.soundFor(cue),
                            soundSettings.secondsFor(cue),
                            useAlarmVolume = cue != TimerCue.FREE_TIMER_FINISHED &&
                                soundSettings.classAlertsUseAlarmVolume) { message ->
                            melodyError = message
                        }
                    },
                    onError = { message -> melodyError = message },
                )
                if (melodyError != null) {
                    Text(melodyError ?: "", color = atelierErrorTextColor())
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Automatic timer alerts", modifier = Modifier.weight(1f))
                    Switch(checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it })
                }
                Text("Sound changes take effect on the next cue; Class timing is unchanged.",
                    style = MaterialTheme.typography.bodySmall)
            }
            MainTab.TIMER -> {
                val session = playingSession
                val timer = liveTimer
                val view = timerView
                Column(modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Keep screen awake during timers",
                            style = MaterialTheme.typography.titleSmall)
                        Text("While Atelier Time is visible. Uses more battery; " +
                            "manual locking still works.",
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Switch(checked = keepScreenAwake,
                        onCheckedChange = { keepScreenAwake = it },
                        modifier = Modifier.testTag("keep_screen_awake"))
                }
                if (classAlertVolumeMuted) {
                    Text((if (classAlertVolumeMuted)
                        "Class alert volume is at zero. " else "") +
                        "Raise the relevant volume if desired.",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = atelierErrorTextColor(),
                        style = MaterialTheme.typography.bodySmall)
                }
                if (session == null || timer == null || view == null) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                                // Ask for notification permission before showing
                                // the clock approval, never AFTER user chose a target.
                                requestClassStatusNotificationPermission()
                                // No frozen finish: the confirmation view is live.
                                pendingRunApproval = PendingRunApproval(record.id, restored)
                                customFinishEnabled = false
                                finishPickerVisible = false
                                runApprovalError = null
                                runFinishBlockedReason = null
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
                        eventHistory = eventHistory.toList(),
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
                            recordClassEvent("Exit", timer.view(), timer.view())
                            classStatusNotification.clear()
                            playingSession = null
                            launchRunRequested = false
                            activeTab = MainTab.TIMER
                        },
                    )
                }
                }
            }
        }
    }

    pendingRunApproval?.let { request ->
        val clockFormat = remember { SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()) }
        val now = wallClockMillis
        val calculatedFinish = RunEndTimePlanner.suggestedEndEpochMs(request.session, now)
        val calculatedClock = Calendar.getInstance().apply { timeInMillis = calculatedFinish }
        val hour = if (customFinishEnabled) customFinishHour
            else calculatedClock.get(Calendar.HOUR_OF_DAY)
        val minute = if (customFinishEnabled) customFinishMinute
            else calculatedClock.get(Calendar.MINUTE)
        val chosen = if (customFinishEnabled) {
            RunEndTimePlanner.nextOccurrence(
                "%02d:%02d".format(Locale.US, hour, minute), now
            )
        } else calculatedFinish
        val trial = runCatching { RunEndTimePlanner.plan(request.session, now, chosen) }
        val plan = trial.getOrNull()
        val isValid = plan != null
        // Validate each ADJACENT hour/minute step. Invalid (red) candidates
        // are visible at the wheel edge but cannot become selected, even by a
        // long swipe. The actual Run is revalidated again at confirmation.
        fun checkClock(candidateHour: Int, candidateMinute: Int): Result<RunEndTimePlan> =
            runCatching {
                val candidate = RunEndTimePlanner.nextOccurrence(
                    "%02d:%02d".format(Locale.US, candidateHour, candidateMinute),
                    now,
                )
                RunEndTimePlanner.plan(request.session, now, candidate)
            }
        fun canSelectClock(candidateHour: Int, candidateMinute: Int): Boolean =
            checkClock(candidateHour, candidateMinute).isSuccess
        fun explainBlockedClock(candidateHour: Int, candidateMinute: Int): String {
            val attempted = "%02d:%02d".format(Locale.US, candidateHour, candidateMinute)
            val why = checkClock(candidateHour, candidateMinute).exceptionOrNull()?.message
                ?: "The selected finish is outside this Class's adjustable range."
            return "Cannot select $attempted: $why"
        }
        val dark = atelierNightActive()
        val statusColor = if (isValid) {
            if (dark) Color(0xFF91F0B7) else Color(0xFF166E39)
        } else atelierErrorTextColor()
        val pickerBackground = if (isValid) {
            if (dark) Color(0xFF20543B) else Color(0xFFDEF3E4)
        } else {
            if (dark) Color(0xFF773342) else Color(0xFFFFE5E5)
        }
        AlertDialog(
            onDismissRequest = {
                pendingRunApproval = null
                runApprovalError = null
            },
            title = { Text("Confirm Class finish time") },
            text = {
                Column(
                    modifier = Modifier.testTag("run_end_dialog")
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(request.session.rules.name)
                    if (classAlertVolumeMuted) {
                        Text((if (soundSettings.classAlertsUseAlarmVolume)
                            "Alarm" else "Media") + " volume is at zero. Class cues " +
                            "may be silent until you raise it.",
                            color = atelierErrorTextColor(),
                            style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = {
                        melodyPlayer.play(soundSettings.soundFor(TimerCue.POSE_FINISHED),
                            soundSettings.secondsFor(TimerCue.POSE_FINISHED),
                            useAlarmVolume = soundSettings.classAlertsUseAlarmVolume) {
                            message -> melodyError = message
                        }
                    }) { Text("Test Class alert sound") }
                    Text(
                        "Calculated finish: " + clockFormat.format(Date(calculatedFinish)),
                        modifier = Modifier.testTag("run_end_calculated"),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (!finishPickerVisible) {
                        Text(
                            "Approve the live calculated finish, or choose to " +
                                "change it using hour and minute wheels.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        TextButton(
                            modifier = Modifier.testTag("run_end_change_finish"),
                            colors = atelierTextColors(),
                            onClick = {
                                // Reveal wheels without fixing/truncating the
                                // live finish until a number actually changes.
                                finishPickerVisible = true
                                customFinishEnabled = false
                                runApprovalError = null
                                runFinishBlockedReason = null
                            },
                        ) { Text("Change finish time") }
                    } else {
                        Text(
                            "Scroll hours and minutes. Red values are blocked; " +
                                "the wheel stops at the last green value.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Surface(
                            color = pickerBackground,
                            contentColor = if (dark) Color.White
                                else if (isValid) Color(0xFF155D30) else Color(0xFF941B27),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().testTag("run_end_wheels"),
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    if (isValid) "Valid finish" else "Invalid finish",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    EndClockWheel(
                                        label = "Hour", value = hour, limit = 24,
                                        tag = "run_end_hour",
                                        canSelect = { candidateHour ->
                                            canSelectClock(candidateHour, minute)
                                        },
                                        onBlocked = { candidateHour ->
                                            runFinishBlockedReason = explainBlockedClock(
                                                candidateHour, minute
                                            )
                                        },
                                        onChange = { next ->
                                            // First movement freezes the chosen clock,
                                            // not merely opening the picker.
                                            if (!customFinishEnabled) {
                                                customFinishMinute = minute
                                            }
                                            customFinishHour = next
                                            customFinishEnabled = true
                                            runApprovalError = null
                                            runFinishBlockedReason = null
                                        },
                                    )
                                    Text(":", style = MaterialTheme.typography.headlineMedium)
                                    EndClockWheel(
                                        label = "Minute", value = minute, limit = 60,
                                        tag = "run_end_minute",
                                        canSelect = { candidateMinute ->
                                            canSelectClock(hour, candidateMinute)
                                        },
                                        onBlocked = { candidateMinute ->
                                            runFinishBlockedReason = explainBlockedClock(
                                                hour, candidateMinute
                                            )
                                        },
                                        onChange = { next ->
                                            if (!customFinishEnabled) {
                                                customFinishHour = hour
                                            }
                                            customFinishMinute = next
                                            customFinishEnabled = true
                                            runApprovalError = null
                                            runFinishBlockedReason = null
                                        },
                                    )
                                }
                            }
                        }
                        runFinishBlockedReason?.let { reason ->
                            Text(
                                "Blocked at the red boundary. " + reason +
                                    " Scroll back toward green or use the calculated finish.",
                                modifier = Modifier.testTag("run_end_blocked_reason"),
                                color = atelierErrorTextColor(),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        TextButton(
                            modifier = Modifier.testTag("run_end_reset_calculated"),
                            colors = atelierTextColors(),
                            onClick = {
                                customFinishEnabled = false
                                finishPickerVisible = false
                                runApprovalError = null
                                runFinishBlockedReason = null
                            },
                        ) { Text("Use calculated finish") }
                    }
                    Text(
                        "Selected finish: " + clockFormat.format(Date(chosen)),
                        color = statusColor,
                        modifier = Modifier.testTag("run_end_selected"),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    if (plan != null) {
                        val adjusted = plan.adjusted
                        Text(
                            "Final two Sessions: " +
                                "%02d:%02d + %02d:%02d".format(
                                    Locale.US,
                                    adjusted.firstFinalPoseSeconds / 60,
                                    adjusted.firstFinalPoseSeconds % 60,
                                    adjusted.lastFinalPoseSeconds / 60,
                                    adjusted.lastFinalPoseSeconds % 60,
                                ),
                            color = statusColor,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        Text(
                            (trial.exceptionOrNull()?.message ?:
                                "Choose another finish time.") +
                                " Scroll back to green to continue.",
                            color = statusColor,
                            modifier = Modifier.testTag("run_end_invalid"),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    runApprovalError?.let { Text(it, color = atelierErrorTextColor()) }
                }
            },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag("run_end_confirm"),
                    colors = atelierTextColors(),
                    enabled = isValid,
                    onClick = {
                        val actualNow = System.currentTimeMillis()
                        try {
                            val actualTarget = if (customFinishEnabled)
                                RunEndTimePlanner.nextOccurrence(
                                    "%02d:%02d".format(
                                        Locale.US, customFinishHour, customFinishMinute
                                    ), actualNow,
                                )
                            else RunEndTimePlanner.suggestedEndEpochMs(
                                request.session, actualNow
                            )
                            val approvedPlan = RunEndTimePlanner.plan(
                                request.session, actualNow, actualTarget
                            )
                            // Never activate/run an invalid finish.
                            val activatedSession = catalog.activate(request.presetId)
                            selectedPresetId = request.presetId
                            runStartTargetEpochMs = approvedPlan.targetEndEpochMs
                            runFollowSuggestedFinish = !customFinishEnabled
                            playingSession = activatedSession
                            launchRunRequested = true
                            pendingRunApproval = null
                            runApprovalError = null
                            activeTab = MainTab.TIMER
                        } catch (error: IllegalArgumentException) {
                            runApprovalError = error.message ?: "Choose a feasible finish time."
                        } catch (error: IllegalStateException) {
                            runApprovalError = error.message ?: "This saved Class is no longer available."
                        }
                    },
                ) { Text("Confirm & Run") }
            },
            dismissButton = {
                TextButton(colors = atelierTextColors(), onClick = {
                    pendingRunApproval = null
                    runApprovalError = null
                }) { Text("Cancel") }
            },
        )
    }

    overwriteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { overwriteTarget = null },
            title = { Text("Replace saved class?") },
            text = { Text("A saved class named '${target.title}' already exists. " +
                "Saving now will overwrite that class and its approved timeline. " +
                "This cannot be undone. Continue?") },
            confirmButton = {
                TextButton(colors = atelierTextColors(), onClick = { finalSave(target.id) }) {
                    Text("Overwrite class")
                }
            },
            dismissButton = {
                TextButton(colors = atelierTextColors(), onClick = { overwriteTarget = null }) {
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
                TextButton(colors = atelierTextColors(), onClick = { helpSection = null }) { Text("Close") }
            },
        )
    }
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
