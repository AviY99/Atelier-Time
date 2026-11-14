package com.ateliertime.android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import java.util.UUID
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import android.os.SystemClock
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.WindowManager
import android.graphics.drawable.ColorDrawable
import androidx.core.view.WindowCompat
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
private enum class TailRecoveryChoice { EDITOR, FINAL_SESSION, SUMMARY }

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
    val freeStatusNotification = remember(context) { ActiveFreeStatusNotification(context) }
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
    DisposableEffect(classStatusNotification, freeStatusNotification) {
        onDispose {
            classStatusNotification.clear()
            freeStatusNotification.clear()
        }
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
    val initialEditor = remember { freshEditorInput() }
    var selectedPresetId by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf(initialEditor.name) }
    var total by remember { mutableStateOf(initialEditor.totalMinutes) }
    var summary by remember { mutableStateOf(initialEditor.summaryMinutes) }
    var pose by remember { mutableStateOf(initialEditor.poseMinutes) }
    var normal by remember { mutableStateOf(initialEditor.normalBreakMinutes) }
    var special by remember { mutableStateOf(initialEditor.specialBreakMinutes) }
    var count by remember { mutableStateOf(initialEditor.specialCount) }
    var cycle by remember { mutableStateOf(initialEditor.cycleStart) }
    var automatic by remember { mutableStateOf(initialEditor.automaticPlacement) }
    var manualOrdinals by remember { mutableStateOf(initialEditor.manualOrdinals) }
    var ending by remember { mutableStateOf<FinalPoseChoice?>(null) }
    var tailExtensionMinutes by remember { mutableIntStateOf(0) }
    var showTailRecovery by remember { mutableStateOf(false) }
    var showResetEditorConfirm by remember { mutableStateOf(false) }
    var tailRecoveryChoice by remember { mutableStateOf(TailRecoveryChoice.EDITOR) }
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
    // D-149: Sound ON/OFF comes from persisted SoundSettings, shared by
    // Class, Free Timer, previews and the main Sounds tab.
    val soundEnabled = soundSettings.timerSoundsEnabled
    var keepScreenAwake by remember { mutableStateOf(false) }
    val classVolumeControl = remember(context) { ClassVolumeControl(context) }
    // D156: timer volume choices remain in Android until the user changes them.
    // No restore-on-exit/dispose, and Max no longer needs a confirmation.
    var classVolumeError by remember { mutableStateOf<String?>(null) }
    // Force immediate refresh after a user releases either Android volume slider;
    // the running wall clock refresh also picks up hardware-button changes.
    var volumeRevision by remember { mutableIntStateOf(0) }
    // D155: one device-volume source of truth for Class, Free and Sounds,
    // regardless of which stream the user selected. Android alone controls
    // whether the selected Media/Alarm channel can actually reach zero.
    fun syncSoundAfterTimerVolume(levels: ClassVolumeLevels) {
        val updated = soundSettings.afterSelectedStreamVolumeChange(
            levels.selected(soundSettings.classAlertsUseAlarmVolume))
        if (updated != soundSettings) {
            saveSoundSettings(updated)
            if (soundSettings != updated)
                classVolumeError = "Volume changed, but Sound state was not saved."
        }
    }

    fun onSoundTabChange(updated: SoundSettings) {
        val selectedAlarm = updated.classAlertsUseAlarmVolume
        if (selectedAlarm != soundSettings.classAlertsUseAlarmVolume) {
            // Changing output route never changes either phone stream.
            // The Sound toggle reflects the NEW route's actual hardware level.
            volumeRevision++
            saveSoundSettings(updated.afterSelectedStreamVolumeChange(
                classVolumeControl.levels().selected(selectedAlarm)))
        } else if (updated.timerSoundsEnabled != soundSettings.timerSoundsEnabled) {
            // Sound ON/OFF has EXACTLY the same volume effect as slider/Mute.
            // Do not save a false ON/OFF when Android rejects the volume.
            runCatching {
                classVolumeControl.setSelectedSoundEnabled(
                    selectedAlarm, updated.timerSoundsEnabled)
            }.onSuccess { actual ->
                volumeRevision++
                classVolumeError = null
                saveSoundSettings(updated.afterSelectedStreamVolumeChange(
                    actual.selected(selectedAlarm)))
            }.onFailure {
                melodyError = it.message ?: "Android did not change selected volume."
            }
        } else {
            saveSoundSettings(updated)
        }
    }

    val freeTimer = remember { FreeCountdownTimer { SystemClock.elapsedRealtime() } }
    var freeTimerView by remember { mutableStateOf(freeTimer.view()) }
    var freeTimerError by remember { mutableStateOf<String?>(null) }
    // A complete scrollable human history and a private append-only diagnostic
    // report are derived from the SAME event, not separate sets of callbacks.
    val eventHistory = remember { mutableStateListOf<String>() }
    val diagnosticArchive = remember(context, presetStoreOverride) {
        if (presetStoreOverride == null) RunDiagnosticsArchive(context) else null
    }
    var currentReport by remember { mutableStateOf<RunDiagnosticsRecorder?>(null) }
    var diagnosticError by remember { mutableStateOf<String?>(null) }
    val liveTimer = remember(playingSession) {
        playingSession?.let { session ->
            MonotonicSessionTimer(session) { SystemClock.elapsedRealtime() }
        }
    }
    fun humanPhase(phase: SchedulePhase?): String = when (phase?.kind) {
        PhaseKind.POSE -> "Session" + (phase.poseNumber?.let { " $it" } ?: "")
        PhaseKind.SHORT_BREAK -> "Regular Break"
        PhaseKind.SPECIAL_BREAK -> "Special Break"
        PhaseKind.SUMMARY -> "Summary"
        null -> "Class end"
    }
    fun recordClassEvent(
        source: String,
        before: TimerView?,
        after: TimerView?,
        cue: TimerCue? = null,
        beforePhases: List<SchedulePhase>? = null,
        rejectedReason: String? = null,
    ) {
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            .format(Date(System.currentTimeMillis()))
        val transition = if (before == null)
            " · ${humanPhase(after?.phase)}"
        else if (before.phase != after?.phase || before.status != after?.status)
            " · ${humanPhase(before.phase)} → ${humanPhase(after?.phase)}"
        else " · ${humanPhase(after?.phase)}"
        val cueNote = cue?.let {
            val label = when (it) {
                TimerCue.POSE_FINISHED -> "A"
                TimerCue.BREAK_FINISHED -> "B"
                TimerCue.SESSION_FINISHED -> "C"
                TimerCue.FREE_TIMER_FINISHED -> "Free"
            }
            " · Cue $label " + (if (soundEnabled) "requested" else "disabled")
        } ?: ""
        val result = rejectedReason?.let { " · Rejected: $it" } ?: ""
        eventHistory.add("$stamp · $source$transition$cueNote$result")
        try {
            currentReport?.event(
                action = source, before = before, after = after,
                phasesBefore = beforePhases,
                phasesAfter = liveTimer?.activePhasesSnapshot(),
                cue = cue, alertsEnabled = soundEnabled,
                sounds = soundSettings, rejectedReason = rejectedReason,
            )
        } catch (failure: Exception) {
            diagnosticError = "Diagnostic report cannot be saved: " +
                (failure.message ?: "storage error")
        }
    }
    fun closeRunReport(reason: String) {
        try { currentReport?.finish(reason) }
        catch (failure: Exception) {
            diagnosticError = "Diagnostic report cannot be completed: " +
                (failure.message ?: "storage error")
        }
        currentReport = null
    }
    var timerView by remember(liveTimer) { mutableStateOf(liveTimer?.view()) }

    // D156: ONE immediate Max action for Programmed/Class and Free timers.
    // It affects only the currently selected device audio channel, reads back
    // the actual level and preserves that level on every Timer exit.
    fun applySelectedVolumeMax() {
        val useAlarm = soundSettings.classAlertsUseAlarmVolume
        runCatching {
            classVolumeControl.maxSelected(useAlarm)
        }.onSuccess { changed ->
            volumeRevision++
            classVolumeError = null
            syncSoundAfterTimerVolume(changed)
            if (playingSession != null) {
                recordClassEvent("Volume max: selected " +
                    (if (useAlarm) "Alarm" else "Media"), timerView, timerView)
            }
        }.onFailure {
            classVolumeError = it.message ?: "Could not set selected sound volume to Max."
        }
    }

    var wallClockMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    // Recover from older versions where a saved Sound OFF did not change
    // hardware volume, and from device volume-button adjustments. Never move
    // either hardware stream without a direct user tap on an app control.
    LaunchedEffect(soundSettings.classAlertsUseAlarmVolume,
        wallClockMillis / 1_000L) {
        syncSoundAfterTimerVolume(classVolumeControl.levels())
    }

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
    // D-140: system navigation belongs to the CURRENT in-app design palette,
    // not to the one-time parchment splash. This also follows AUTO by clock and
    // the Design tab's live preview without requiring an Activity restart.
    LaunchedEffect(activity, designPalette) {
        val day = designPalette == DesignPalette.DAY
        val barColor = if (day) 0xFFF7F4ED.toInt() else 0xFF304D70.toInt()
        activity?.window?.let { window ->
            // Android 15/16 3-button navigation uses the window background if
            // no bar color is drawn. Keep its backing synchronized as well.
            window.setBackgroundDrawable(ColorDrawable(barColor))
            @Suppress("DEPRECATION")
            window.navigationBarColor = barColor
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Remove the default tinted 3-button contrast scrim so the
                // app's existing full-screen Surface shows its real palette.
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightNavigationBars = day
                isAppearanceLightStatusBars = day
            }
        }
    }
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
    val classAlertVolumeMuted = remember(wallClockMillis / 1_000L,
        soundSettings.classAlertsUseAlarmVolume, volumeRevision) {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val stream = if (soundSettings.classAlertsUseAlarmVolume)
            AudioManager.STREAM_ALARM else AudioManager.STREAM_MUSIC
        audio.getStreamVolume(stream) == 0
    }
    var helpSection by remember { mutableStateOf<Pair<String, String>?>(null) }
    var nextOpeningId by remember { mutableIntStateOf(1) }
    val opening = remember { mutableStateListOf<OpeningDraft>() }

    fun invalidate() {
        tailExtensionMinutes = 0
        showTailRecovery = false
        tailRecoveryChoice = TailRecoveryChoice.EDITOR
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

    /** Reset only the in-memory Editor draft. Never touch saved Classes, Sounds or a running clock. */
    fun resetEditorToStart() {
        selectedPresetId = null
        name = initialEditor.name
        total = initialEditor.totalMinutes
        summary = initialEditor.summaryMinutes
        pose = initialEditor.poseMinutes
        normal = initialEditor.normalBreakMinutes
        special = initialEditor.specialBreakMinutes
        count = initialEditor.specialCount
        cycle = initialEditor.cycleStart
        automatic = initialEditor.automaticPlacement
        manualOrdinals = initialEditor.manualOrdinals
        opening.clear()
        nextOpeningId = 1
        invalidate() // Drops old tail choice, final-pose split, warning consent and Save approval.
        showResetEditorConfirm = false
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
        tailExtensionMinutes = rules.tailPoseExtensionSeconds / SECONDS_PER_MINUTE
        showTailRecovery = false
        tailRecoveryChoice = TailRecoveryChoice.EDITOR
        ending = preset.ending
        acceptedWarning = preset.acknowledgedRestWarnings
        approved = restored
    }

    var parseError: String? = null
    val candidate = try {
        val rules = EditorInput(
            name, total, summary, pose, normal, special, count,
            opening.toList(), cycle, automatic, manualOrdinals,
            tailExtensionMinutes = tailExtensionMinutes,
        ).toRules()
        val firstPass = engine.generate(rules, ending)
        // Default to Even only when a legal split exists; saved approvals remain unchanged.
        if (ending == null && firstPass.finalPoseIsShort &&
            firstPass.canSplitFinalTwo && !firstPass.hasBlockingIssues
        ) engine.generate(rules, FinalPoseChoice.even()) else firstPass
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
                val view = clock.start(correction,
                    physicalDurationMillis = target - startEpochMs)
                eventHistory.clear()
                diagnosticError = null
                classClockTimes = classClockTracker.newClass(view, startEpochMs, target)
                currentReport = try {
                    diagnosticArchive?.begin(session, startEpochMs, target,
                        soundSettings, soundEnabled)
                } catch (failure: Exception) {
                    diagnosticError = "Could not create Run diagnostic report: " +
                        (failure.message ?: "storage error")
                    null
                }
                // Capture an initial Run-end adjustment as a real plan revision.
                // The approved Editor plan is already in run_start; the event
                // records the actual runtime plan whenever those differ.
                recordClassEvent("Start", null, view,
                    beforePhases = session.phases)
                timerView = view
                timerError = null
                // D-144: a quiet, short three-note sound/volume indicator
                // starts ONLY with a user-confirmed Class, never at splash.
                if (soundEnabled) {
                    val runReport = currentReport
                    melodyPlayer.playClassStartCheck(
                        useAlarmVolume = soundSettings.classAlertsUseAlarmVolume,
                    ) { message ->
                        melodyError = message
                        timerError = message
                        try { runReport?.note("opening_audio_check_failed", message) }
                        catch (ignored: Exception) { diagnosticError = "Audio check error not logged." }
                    }
                }
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

    LaunchedEffect(soundSettings, soundEnabled) {
        // Settings changed DURING a Run belong in BOTH representations.
        // The detailed file receives the full sanitized settings snapshot.
        if (currentReport != null) {
            val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(Date(System.currentTimeMillis()))
            eventHistory.add("$stamp · Sound settings changed")
            try { currentReport?.soundChange(soundSettings, soundEnabled) }
            catch (failure: Exception) {
                diagnosticError = "Could not save sound setting change."
            }
        }
    }

    // D-149: Sound OFF is immediate, even if a long Custom cue or preview
    // is already playing. This applies to both Class and Free players.
    LaunchedEffect(soundEnabled) {
        if (!soundEnabled) {
            melodyPlayer.stop()
            freeMelodyPlayer.stop()
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
                val phasesBefore = clock.activePhasesSnapshot()
                val deadlineReached = classClockTracker.hasReachedClassDeadline(nowEpochMs) &&
                    (before.status == TimerStatus.RUNNING ||
                        before.status == TimerStatus.PAUSED)
                val tick = if (deadlineReached) clock.finishAtDeadline()
                    else clock.update()
                if (before.phase?.id != tick.view.phase?.id ||
                    before.status != tick.view.status || tick.cue != null) {
                    recordClassEvent(if (deadlineReached) "Deadline" else "Automatic",
                        before, tick.view, tick.cue, beforePhases = phasesBefore)
                }
                timerView = tick.view
                classClockTimes = classClockTracker.observe(tick.view, nowEpochMs)
                classStatusNotification.update(
                    playingSession?.rules?.name ?: "Class",
                    tick.view,
                    classClockTimes.classRemainingSeconds ?: tick.view.totalRemainingSeconds,
                    nowEpochMs,
                    playingSession?.rules,
                )
                if (soundEnabled && tick.cue != null) {
                    // Capture the recorder for an asynchronous audio error:
                    // the Run may have finished by the time playback fails.
                    val cueReport = currentReport
                    melodyPlayer.play(soundSettings.soundFor(tick.cue),
                        soundSettings.secondsFor(tick.cue),
                        useAlarmVolume = soundSettings.classAlertsUseAlarmVolume,
                        playbackVolumePercent =
                            soundSettings.playbackVolumePercentFor(tick.cue)) { message ->
                        melodyError = message
                        timerError = message
                        val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            .format(Date(System.currentTimeMillis()))
                        eventHistory.add("$stamp · Audio error · $message")
                        try { cueReport?.note("audio_error", message) }
                        catch (ignored: Exception) {
                            diagnosticError = "Could not persist audio error."
                        }
                    }
                }
                if (tick.view.status == TimerStatus.FINISHED) {
                    volumeRevision++
                    closeRunReport(if (deadlineReached) "deadline" else "natural_finish")
                    classStatusNotification.clear()
                    // Return to Timer's saved-class HOME once the class ends.
                    playingSession = null
                    activeTab = MainTab.TIMER
                }
            }
            val freeTick = freeTimer.update()
            freeTimerView = freeTick.view
            freeStatusNotification.update(freeTick.view, nowEpochMs)
            if (freeTick.completedNow && soundEnabled) {
                freeMelodyPlayer.play(
                    soundSettings.soundFor(TimerCue.FREE_TIMER_FINISHED),
                    soundSettings.secondsFor(TimerCue.FREE_TIMER_FINISHED),
                    useAlarmVolume = soundSettings.classAlertsUseAlarmVolume,
                    playbackVolumePercent = soundSettings.playbackVolumePercentFor(
                        TimerCue.FREE_TIMER_FINISHED),
                ) { message -> freeTimerError = message }
            }
            delay(200)
        }
    }

    fun timerAction(block: () -> TimerView) {
        val before = liveTimer?.view()
        val phasesBefore = liveTimer?.activePhasesSnapshot()
        val source = if (before?.status == TimerStatus.RUNNING) "Pause" else "Resume"
        try {
            val updated = block()
            recordClassEvent(source, before, updated, beforePhases = phasesBefore)
            timerView = updated
            classClockTimes = classClockTracker.observe(updated, System.currentTimeMillis())
            timerError = null
        } catch (error: IllegalArgumentException) {
            timerError = error.message ?: "Cannot fit the interruption into the final Sessions."
            recordClassEvent(source, before, liveTimer?.view(), beforePhases = phasesBefore,
                rejectedReason = timerError)
        } catch (error: IllegalStateException) {
            timerError = error.message ?: "Cannot change a Session that has already started."
            recordClassEvent(source, before, liveTimer?.view(), beforePhases = phasesBefore,
                rejectedReason = timerError)
        }
    }

    /** The diagnostic report must never turn an otherwise legal Timer action into a failure. */
    fun runTimedCue(
        restartCurrentSegment: Boolean = false,
        repeatedPrevious: Boolean = false,
        advancedByNext: Boolean = false,
        action: () -> TimerTick,
    ) {
        val source = when {
            repeatedPrevious -> "Repeat Last"
            restartCurrentSegment -> "Restart"
            else -> "Next"
        }
        val before = liveTimer?.view()
        val phasesBefore = liveTimer?.activePhasesSnapshot()
        try {
            val event = action()
            recordClassEvent(source, before, event.view, event.cue,
                beforePhases = phasesBefore)
            timerView = event.view
            val nowEpochMs = System.currentTimeMillis()
            classClockTimes = if (restartCurrentSegment || repeatedPrevious) {
                classClockTracker.restartCurrentSegment(event.view, nowEpochMs)
            } else if (advancedByNext) {
                classClockTracker.afterNext(event.view, nowEpochMs)
            } else {
                classClockTracker.observe(event.view, nowEpochMs)
            }
            timerError = null
            if (soundEnabled && event.cue != null) {
                val cueReport = currentReport
                melodyPlayer.play(soundSettings.soundFor(event.cue),
                    soundSettings.secondsFor(event.cue),
                    useAlarmVolume = soundSettings.classAlertsUseAlarmVolume,
                    playbackVolumePercent =
                        soundSettings.playbackVolumePercentFor(event.cue)) { error ->
                    melodyError = error
                    val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        .format(Date(System.currentTimeMillis()))
                    eventHistory.add("$stamp · Audio error · $error")
                    try { cueReport?.note("audio_error", error) }
                    catch (ignored: Exception) { diagnosticError = "Audio error not logged." }
                }
            }
            if (event.view.status == TimerStatus.FINISHED) {
                volumeRevision++
                closeRunReport("natural_finish")
                classStatusNotification.clear()
                playingSession = null
                activeTab = MainTab.TIMER
            }
        } catch (error: IllegalArgumentException) {
            timerError = error.message ?: "The final Sessions cannot absorb this change."
            recordClassEvent(source, before, liveTimer?.view(), beforePhases = phasesBefore,
                rejectedReason = timerError)
        } catch (error: IllegalStateException) {
            timerError = error.message ?: "This phase cannot be skipped safely."
            recordClassEvent(source, before, liveTimer?.view(), beforePhases = phasesBefore,
                rejectedReason = timerError)
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
        // All six destinations remain visible. Longer labels get enough of the
        // available width; a fixed 46dp box with inherited bodyLarge lineHeight
        // clipped two-line titles on the actual Samsung phone. Supply an
        // explicit lineHeight and breathing room for font scaling.
        // LTR is intentional even when the device uses a Hebrew locale.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            // Keep the accepted compact layout under normal font scaling.
            // At large text settings allow horizontal tab scrolling instead
            // of shrinking readable labels to unusable font sizes.
            val largeNavigationText = LocalDensity.current.fontScale >= 1.3f
            Row(modifier = Modifier.fillMaxWidth().then(
                if (largeNavigationText) Modifier.horizontalScroll(rememberScrollState())
                else Modifier
            )) {
                MainTab.entries.forEach { tab ->
                    val label = when (tab) {
                        MainTab.EDITOR -> "Editor"
                        MainTab.TIMER -> "Programed Timer"
                        MainTab.FREE_TIMER -> "Free Timer"
                        MainTab.SOUNDS -> "Sound"
                        MainTab.DESIGN -> "Design"
                        MainTab.ABOUT -> "About"
                    }
                    val selected = activeTab == tab
                    val selectedColor = if (atelierNightActive()) Color.White
                        else MaterialTheme.colorScheme.primary
                    val contentColor = if (selected) selectedColor
                        else if (atelierNightActive()) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    val tabWeight = when (tab) {
                        MainTab.TIMER -> 1.60f
                        MainTab.FREE_TIMER -> 1.10f
                        MainTab.EDITOR, MainTab.DESIGN -> 0.85f
                        MainTab.SOUNDS, MainTab.ABOUT -> 0.80f
                    }
                    Column(
                        modifier = (if (largeNavigationText) Modifier.widthIn(min = 104.dp)
                            else Modifier.weight(tabWeight))
                            .testTag("top-tab-$label").selectable(
                            selected = selected, onClick = { activeTab = tab },
                            role = Role.Tab,
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                            .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center) {
                            val displayLabel = when (tab) {
                                MainTab.TIMER -> "Programed\nTimer"
                                MainTab.FREE_TIMER -> "Free\nTimer"
                                else -> label
                            }
                            if (largeNavigationText) {
                                Text(displayLabel, color = contentColor,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 2, softWrap = true,
                                    textAlign = TextAlign.Center)
                            } else {
                                Text(displayLabel, color = contentColor,
                                    modifier = Modifier.fillMaxWidth(),
                                    lineHeight = 14.sp,
                                    autoSize = TextAutoSize.StepBased(
                                        minFontSize = 8.sp, maxFontSize = 11.sp,
                                        stepSize = 0.5.sp,
                                    ),
                                    maxLines = 2, softWrap = false,
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Visible)
                            }
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(
                            if (selected) selectedColor else Color.Transparent))
                    }
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
                // Class and Free share the selected-stream slider, immediate Max
                // and Mute; volume never automatically reverts on exit.
                val freeVolumes = remember(wallClockMillis / 1_000L, volumeRevision) {
                    classVolumeControl.levels()
                }
                RunningClassQuickControls(
                    containerTag = "free_timer_quick_controls",
                    keepScreenAwake = keepScreenAwake,
                    onKeepScreenAwakeChange = { keepScreenAwake = it },
                    selectedVolumePercent = if (soundSettings.classAlertsUseAlarmVolume)
                        freeVolumes.alarmPercent() else freeVolumes.mediaPercent(),
                    useAlarmVolume = soundSettings.classAlertsUseAlarmVolume,
                    showZeroVolumeWarning = classAlertVolumeMuted,
                    onSelectedVolumeChange = { percent ->
                        runCatching {
                            if (soundSettings.classAlertsUseAlarmVolume)
                                classVolumeControl.setAlarmPercent(percent)
                            else classVolumeControl.setMediaPercent(percent)
                        }.onSuccess { changed ->
                            volumeRevision++
                            classVolumeError = null
                            syncSoundAfterTimerVolume(changed)
                        }.onFailure {
                            classVolumeError = it.message ?:
                                "Could not adjust selected sound volume."
                        }
                    },
                    onVolumeMax = { applySelectedVolumeMax() },
                    onSoundsOff = {
                        runCatching {
                            classVolumeControl.muteSelected(
                                soundSettings.classAlertsUseAlarmVolume)
                        }.onSuccess { changed ->
                            volumeRevision++
                            classVolumeError = null
                            syncSoundAfterTimerVolume(changed)
                        }.onFailure {
                            classVolumeError = it.message ?:
                                "Could not mute selected sound volume."
                        }
                    },
                )
                classVolumeError?.let {
                    Text(it, color = atelierErrorTextColor())
                }
                FreeCountdownSection(
                    view = freeTimerView,
                    onStart = { hours, minutes, seconds ->
                        requestClassStatusNotificationPermission()
                        freeMelodyPlayer.stop()
                        runCatching { freeTimer.start(hours, minutes, seconds) }
                            .onSuccess {
                                freeTimerView = it
                                freeStatusNotification.update(it, System.currentTimeMillis())
                                freeTimerError = null
                            }
                            .onFailure { freeTimerError = it.message }
                    },
                    onPauseResume = {
                        freeTimerView = if (freeTimerView.status == FreeTimerStatus.RUNNING)
                            freeTimer.pause() else freeTimer.resume()
                        freeStatusNotification.update(freeTimerView, System.currentTimeMillis())
                    },
                    onExit = {
                        freeMelodyPlayer.stop()
                        volumeRevision++
                        volumeRevision++
                        freeTimerView = freeTimer.exit()
                        freeStatusNotification.clear()
                        freeTimerError = null
                    },
                )
                freeTimerError?.let { Text(it, color = atelierErrorTextColor()) }
            }
            MainTab.EDITOR -> Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Class editor", style = MaterialTheme.typography.headlineMedium)
        // D-143: one compact row, Reset first with a distinct Day/Night-safe background.
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AtelierButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                modifier = Modifier.weight(1.2f).testTag("editor_reset_button"),
                onClick = { showResetEditorConfirm = true },
            ) { Text("Reset Editor", maxLines = 1,
                style = MaterialTheme.typography.labelMedium) }
            AtelierOutlinedButton(
                colors = atelierOutlinedColors(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f).testTag("editor_example_a_button"),
                onClick = { applyA() },
            ) { Text("Example A", maxLines = 1,
                style = MaterialTheme.typography.labelMedium) }
            AtelierOutlinedButton(
                colors = atelierOutlinedColors(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f).testTag("editor_example_b_button"),
                onClick = { applyB() },
            ) { Text("Example B", maxLines = 1,
                style = MaterialTheme.typography.labelMedium) }
        }
        OutlinedTextField(colors = atelierInputColors(),
            value = name, onValueChange = { name = it; invalidate() },
            label = { Text("Class title") }, modifier = Modifier.fillMaxWidth()
                .testTag("editor_class_title"),
            singleLine = true,
        )
        AdaptiveMinuteInputPair(
            "Total class duration (min)", total, { total = it; invalidate() },
            "End-of-class summary (min)", summary, { summary = it; invalidate() },
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Opening session groups", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f))
            AtelierTextButton(colors = atelierTextColors(), onClick = {
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
                AtelierTextButton(colors = atelierTextColors(), onClick = {
                    helpSection = "Opening session group " + (index + 1) to
                        "Number of sessions means how many consecutive work sessions this group contains. " +
                        "Each session duration sets the time of one work session in minutes. " +
                        "Set Break after group to 0 for no break between opening groups. " +
                        "The last group requires at least 1 minute of rest. If it equals " +
                        "the initial regular break, only one break occurs."
                }) { Text("Help") }
            }
            AdaptiveMinuteInputPair(
                "Number of sessions", group.count, {
                    opening[index] = group.copy(count = it); invalidate()
                },
                "Each session duration (min)", group.minutes, {
                    opening[index] = group.copy(minutes = it); invalidate()
                },
            )
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
                AtelierTextButton(colors = atelierTextColors(), onClick = {
                    if (index > 0) {
                        opening[index] = opening[index - 1].also { opening[index - 1] = group }
                        invalidate()
                    }
                }, enabled = index > 0) { Text("Up") }
                AtelierTextButton(colors = atelierTextColors(), onClick = {
                    if (index < opening.lastIndex) {
                        opening[index] = opening[index + 1].also { opening[index + 1] = group }
                        invalidate()
                    }
                }, enabled = index < opening.lastIndex) { Text("Down") }
                AtelierTextButton(colors = atelierTextColors(), onClick = { opening.removeAt(index); invalidate() }) {
                    Text("Remove")
                }
            }
        }
        AtelierOutlinedButton(colors = atelierOutlinedColors(), onClick = {
            opening.add(OpeningDraft(nextOpeningId++, breakAfterMinutes = normal)); invalidate()
        }) { Text("Add opening session group") }

        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Regular sessions", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f))
            AtelierTextButton(colors = atelierTextColors(), onClick = {
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
            AtelierTextButton(colors = atelierTextColors(), onClick = {
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
            AtelierFilterChip(colors = atelierChipColors(), 
                selected = cycle == CycleStart.POSE_FIRST,
                onClick = { cycle = CycleStart.POSE_FIRST; invalidate() },
                label = { Text("Session first") },
            )
            AtelierFilterChip(colors = atelierChipColors(), 
                selected = cycle == CycleStart.BREAK_FIRST,
                onClick = { cycle = CycleStart.BREAK_FIRST; invalidate() },
                label = { Text("Break first") },
            )
        }

        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Special breaks", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f))
            AtelierTextButton(colors = atelierTextColors(), onClick = {
                helpSection = "Special breaks" to
                    "Special breaks REPLACE selected numbered regular breaks; they are " +
                    "not extra breaks. Choose their own duration, count and automatic " +
                    "or manual placement. Manual positions refer only to numbered " +
                    "regular-break slots, not opening-group rests. While running, " +
                    "automatic special breaks can move to a future regular-break " +
                    "slot after Next to keep their centers near the physical Class " +
                    "midpoint (or evenly spaced for several). An already-started " +
                    "Break or a manually placed Break is never moved."
            }) { Text("Help") }
        }
        AdaptiveMinuteInputPair(
            "Special break duration (min)", special, { special = it; invalidate() },
            "Number of special breaks", count, { count = it; invalidate() },
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Place special breaks automatically", modifier = Modifier.weight(1f))
            AtelierOnOffButton(
                checked = automatic,
                onCheckedChange = { automatic = it; invalidate() },
                modifier = Modifier.testTag("editor_auto_special_break_toggle"),
                description = "Place special breaks automatically",
            )
        }
        if (!automatic && count.toIntOrNull() != 0) {
            OutlinedTextField(colors = atelierInputColors(), 
                value = manualOrdinals,
                onValueChange = { manualOrdinals = it; invalidate() },
                label = { Text("Regular breaks to replace (e.g. 2,4)") },
                supportingText = { Text("Enter positions, not a break count. Example: 3 replaces regular break #3 with a special break.") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Complete class timeline", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f))
            AtelierTextButton(colors = atelierTextColors(), onClick = {
                helpSection = "Complete class timeline" to
                    "All timestamps are elapsed time from class start, not wall-clock time. " +
                    "The total class includes the end-of-class summary. A rest advisory " +
                    "after more than 25 minutes of cumulative sessions without a qualifying " +
                    "5-minute break requires acknowledgment before Save."
            }) { Text("Help") }
        }
        if (tailExtensionMinutes > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Final Session: +$tailExtensionMinutes min (user-selected)",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                )
                AtelierTextButton(onClick = { invalidate() }) { Text("Remove") }
            }
        }
        if (parseError != null) {
            Text(parseError ?: "", color = atelierErrorTextColor())
        } else if (candidate != null) {
            candidate.issues.filter { it.severity == IssueSeverity.ERROR }.forEach {
                Text(it.message, color = atelierErrorTextColor())
            }
            candidate.tailBreakRemainder?.let { tail ->
                val minutes = tail.remainingSeconds / SECONDS_PER_MINUTE
                Text(
                    "The final $minutes min cannot fit the next " +
                        "${tail.attemptedBreakSeconds / SECONDS_PER_MINUTE}-min break " +
                        "and another 1-min Session.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                AtelierOutlinedButton(
                    colors = atelierOutlinedColors(),
                    modifier = Modifier.testTag("tail_remainder_options"),
                    onClick = {
                        tailRecoveryChoice = TailRecoveryChoice.EDITOR
                        showTailRecovery = true
                    },
                ) { Text("Choose how to resolve the final $minutes min") }
            }
            if (candidate.finalPoseIsShort && !candidate.hasBlockingIssues) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Final sessions", style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f))
                    AtelierTextButton(colors = atelierTextColors(), onClick = {
                        helpSection = "Final sessions" to
                            "Last gets remaining time preserves the first Session. " +
                            "Even divides the remaining work equally; Custom lets you " +
                            "choose the first Session length. Even is the default when possible."
                    }) { Text("Help") }
                }
                // Keep all three ending choices visible without horizontal scrolling.
                // The longest option deliberately occupies two text lines on phones.
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AtelierFilterChip(colors = atelierChipColors(), 
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
                    AtelierFilterChip(colors = atelierChipColors(), 
                        modifier = Modifier.weight(0.8f),
                        selected = (ending ?: candidate.selectedChoice)?.mode == FinalPoseMode.EVEN_SPLIT,
                        enabled = candidate.canSplitFinalTwo,
                        onClick = {
                            ending = FinalPoseChoice.even()
                            acceptedWarning = false; approved = null
                        },
                        label = { Text("Even") },
                    )
                    AtelierFilterChip(colors = atelierChipColors(), 
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
                        AtelierSlimSlider(
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
                AtelierButton(colors = atelierFilledColors(), 
                    onClick = { requestFinalSave() },
                    enabled = candidate.canApprove &&
                        (!candidate.needsSafetyAcknowledgement || acceptedWarning),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save class")
                }
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
                    onSettingsChange = { updated -> onSoundTabChange(updated) },
                    canMuteSelectedAudio = classVolumeControl.canMuteSelected(
                        soundSettings.classAlertsUseAlarmVolume),
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
                        if (!soundEnabled) {
                            melodyError = "Sound is OFF. Enable Sound to hear a preview."
                        } else {
                            melodyError = null
                            melodyPlayer.play(soundSettings.soundFor(cue),
                                soundSettings.secondsFor(cue),
                                useAlarmVolume = soundSettings.classAlertsUseAlarmVolume,
                                playbackVolumePercent =
                                    soundSettings.playbackVolumePercentFor(cue)) { message ->
                                melodyError = message
                            }
                        }
                    },
                    onError = { message -> melodyError = message },
                )
                if (melodyError != null) {
                    Text(melodyError ?: "", color = atelierErrorTextColor())
                }
                if (!soundEnabled) Text("Sound is OFF for all timers.",
                    style = MaterialTheme.typography.bodySmall)
            }
            MainTab.TIMER -> {
                val session = playingSession
                val timer = liveTimer
                val view = timerView
                val runningClass = view != null && (
                    view.status == TimerStatus.RUNNING ||
                        view.status == TimerStatus.PAUSED)
                Column(modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(
                        if (runningClass) 2.dp else 8.dp)) {
                // D-149: compact Sound-style controls. Class and Free Timer
                // share one audio output route; each slider changes just
                // the matching Android Media/Alarm volume.
                if (runningClass) {
                    val volumes = remember(wallClockMillis / 1_000L, volumeRevision) {
                        classVolumeControl.levels()
                    }
                    RunningClassQuickControls(
                        keepScreenAwake = keepScreenAwake,
                        onKeepScreenAwakeChange = { keepScreenAwake = it },
                        selectedVolumePercent = if (soundSettings.classAlertsUseAlarmVolume)
                            volumes.alarmPercent() else volumes.mediaPercent(),
                        useAlarmVolume = soundSettings.classAlertsUseAlarmVolume,
                        showZeroVolumeWarning = classAlertVolumeMuted,
                        onSelectedVolumeChange = { percent ->
                            runCatching {
                                if (soundSettings.classAlertsUseAlarmVolume)
                                    classVolumeControl.setAlarmPercent(percent)
                                else classVolumeControl.setMediaPercent(percent)
                            }.onSuccess { changed ->
                                volumeRevision++
                                classVolumeError = null
                                syncSoundAfterTimerVolume(changed)
                                recordClassEvent(
                                    "Selected sound volume " +
                                        (if (soundSettings.classAlertsUseAlarmVolume)
                                            changed.alarmPercent().toString() + "% Alarm"
                                        else changed.mediaPercent().toString() + "% Media"),
                                    timerView, timerView)
                            }.onFailure { failure ->
                                classVolumeError = failure.message ?:
                                    "Could not adjust selected sound volume."
                            }
                        },
                        onVolumeMax = { applySelectedVolumeMax() },
                        onSoundsOff = {
                            runCatching {
                                classVolumeControl.muteSelected(
                                    soundSettings.classAlertsUseAlarmVolume)
                            }.onSuccess { changed ->
                                volumeRevision++
                                classVolumeError = null
                                syncSoundAfterTimerVolume(changed)
                                recordClassEvent(
                                    "Mute: selected " +
                                        (if (soundSettings.classAlertsUseAlarmVolume)
                                            "Alarm" else "Media") + " volume " +
                                        changed.selected(soundSettings.classAlertsUseAlarmVolume) +
                                        " (Android permitted minimum)",
                                    timerView, timerView)
                            }.onFailure { failure ->
                                classVolumeError = failure.message ?:
                                    "Could not mute selected sound volume."
                            }
                        },
                    )
                } else {
                    // Preserve the detailed battery warning before Class start.
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Keep screen awake during timers",
                                style = MaterialTheme.typography.titleSmall)
                            Text("Uses more battery",
                                style = MaterialTheme.typography.labelSmall)
                        }
                        AtelierOnOffButton(
                            checked = keepScreenAwake,
                            onCheckedChange = { keepScreenAwake = it },
                            modifier = Modifier.testTag("keep_screen_awake"),
                            visualTag = "keep_screen_awake_visual",
                            description = "Keep screen awake during timers; uses more battery",
                        )
                    }
                }
                // During a Class run the warning is rendered right under the
                // shared slider. Before starting, retain the same one-line text.
                if (!runningClass && classAlertVolumeMuted) {
                    TimerVolumeZeroWarning(
                        tag = "class_selected_volume_zero_warning",
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (runningClass) {
                    classVolumeError?.let {
                        Text(it, color = atelierErrorTextColor(),
                            modifier = Modifier.padding(horizontal = 16.dp))
                    }
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
                        diagnosticArchive?.let { RunDiagnosticsLibrary(it) }
                    }
                } else {
                    SessionTimerScreen(
                        session = session,
                        timer = timer,
                        view = view,
                        wallClockMillis = wallClockMillis,
                        classClockTimes = classClockTimes,
                        timerError = timerError ?: diagnosticError,
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
                        onRestartSession = { restAcknowledged ->
                            val currentKind = timer.view().phase?.kind
                            runTimedCue(restartCurrentSegment = true) {
                                val updated = timer.restartCurrentSegmentKeepingEndTime(restAcknowledged)
                                val cue = when (currentKind) {
                                    PhaseKind.POSE -> TimerCue.POSE_FINISHED
                                    PhaseKind.SHORT_BREAK, PhaseKind.SPECIAL_BREAK ->
                                        TimerCue.BREAK_FINISHED
                                    else -> null
                                }
                                TimerTick(updated, cue)
                            }
                        },
                        onRepeatLast = { restAcknowledged ->
                            // No premature end-of-phase melody: repeat's cue will play
                            // when the replay actually finishes in the normal ticker.
                            runTimedCue(repeatedPrevious = true) {
                                TimerTick(timer.repeatLastFullKeepingEndTime(restAcknowledged))
                            }
                        },
                        onNextPhase = {
                            runTimedCue(advancedByNext = true) { timer.nextPhase() }
                        },
                        onExit = {
                            volumeRevision++
                            recordClassEvent("Exit", timer.view(), timer.view())
                            closeRunReport("user_exit")
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

    // D156: the Max action runs immediately from either shared Timer control,
    // without an alert dialog and without restoring the prior volume.

    // D-143: explicit confirmation protects unsaved draft edits; Saved Classes remain intact.
    if (showResetEditorConfirm) {
        AlertDialog(
            modifier = Modifier.testTag("editor_reset_dialog"),
            onDismissRequest = { showResetEditorConfirm = false },
            title = { Text("Reset Editor?") },
            text = { Text("Return every Editor field and choice to the original starting " +
                "values? Unsaved Editor changes will be lost. Saved Classes, Sounds " +
                "and a running timer will not be changed.") },
            confirmButton = {
                AtelierTextButton(
                    colors = atelierTextColors(),
                    modifier = Modifier.testTag("editor_reset_confirm"),
                    onClick = { resetEditorToStart() },
                ) { Text("Reset Editor") }
            },
            dismissButton = {
                AtelierTextButton(
                    colors = atelierTextColors(),
                    modifier = Modifier.testTag("editor_reset_cancel"),
                    onClick = { showResetEditorConfirm = false },
                ) { Text("Cancel") }
            },
        )
    }

    // D-142: show choices only for a diagnosed terminal-break mismatch.
    // Every proposed change is regenerated by the domain engine first.
    val tailProblem = candidate?.tailBreakRemainder
    if (showTailRecovery && tailProblem != null && candidate != null) {
        val rules = candidate.rules
        val residual = tailProblem.remainingSeconds
        val minutes = residual / SECONDS_PER_MINUTE
        val recommended = rules.openingGroups.indices.reversed().firstNotNullOfOrNull { index ->
            val old = rules.openingGroups[index].breakAfterSeconds ?: return@firstNotNullOfOrNull null
            if (old < 0 || old.toLong() + residual > Int.MAX_VALUE) {
                return@firstNotNullOfOrNull null
            }
            val revised = rules.copy(openingGroups = rules.openingGroups.mapIndexed { i, group ->
                if (i == index) group.copy(breakAfterSeconds = old + residual) else group
            })
            val trial = engine.generate(revised)
            if (!trial.hasBlockingIssues) Triple(index, old / 60, (old + residual) / 60)
            else null
        }
        val extended = engine.generate(rules.copy(tailPoseExtensionSeconds = residual))
        val newSummary = rules.summarySeconds.toLong() + residual
        val reallocated = if (newSummary >= 0 && newSummary < rules.totalSeconds) {
            engine.generate(rules.copy(summarySeconds = newSummary.toInt()))
        } else null
        val validExtension = !extended.hasBlockingIssues
        val validSummary = reallocated != null && !reallocated.hasBlockingIssues
        val selection = tailRecoveryChoice
        val preview = when (selection) {
            TailRecoveryChoice.EDITOR -> null
            TailRecoveryChoice.FINAL_SESSION -> extended.takeIf { validExtension }
            TailRecoveryChoice.SUMMARY -> reallocated?.takeIf { validSummary }
        }
        val canApply = selection == TailRecoveryChoice.EDITOR ||
            (selection == TailRecoveryChoice.FINAL_SESSION && validExtension) ||
            (selection == TailRecoveryChoice.SUMMARY && validSummary)
        AlertDialog(
            onDismissRequest = { showTailRecovery = false },
            title = { Text("Resolve final $minutes minute" + if (minutes == 1) "" else "s") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                        .testTag("tail_remainder_dialog"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "The next break needs ${tailProblem.attemptedBreakSeconds / 60} " +
                            "minutes plus at least 1 minute of Session. " +
                            "$minutes minute(s) remain. The class end time will not change."
                    )
                    AtelierOutlinedButton(
                        colors = atelierOutlinedColors(),
                        onClick = { tailRecoveryChoice = TailRecoveryChoice.EDITOR },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("1 · Return to Editor (manual update)") }
                    Text(
                        recommended?.let {
                            "Suggested: Opening group ${it.first + 1} break " +
                                "${it.second} → ${it.third} min (validated)."
                        } ?: "No safe one-field opening-break recommendation. " +
                            "Edit the settings manually and review the timeline.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    AtelierOutlinedButton(
                        colors = atelierOutlinedColors(),
                        enabled = validExtension,
                        onClick = { tailRecoveryChoice = TailRecoveryChoice.FINAL_SESSION },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("2 · Extend last Session by $minutes min")
                    }
                    if (!validExtension) Text(
                        "Last Session extension cannot preserve all selected breaks.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    AtelierOutlinedButton(
                        colors = atelierOutlinedColors(),
                        enabled = validSummary,
                        onClick = { tailRecoveryChoice = TailRecoveryChoice.SUMMARY },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("3 · Extend Summary by $minutes min")
                    }
                    if (!validSummary) Text(
                        "Summary change cannot preserve the required schedule.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "Selected: " + when (selection) {
                            TailRecoveryChoice.EDITOR -> "Return to Editor; nothing changes."
                            TailRecoveryChoice.FINAL_SESSION -> "Last Session +$minutes min."
                            TailRecoveryChoice.SUMMARY ->
                                "Summary ${rules.summarySeconds / 60} → " +
                                    "${rules.summarySeconds / 60 + minutes} min."
                        },
                        style = MaterialTheme.typography.titleSmall,
                    )
                    preview?.let { trial ->
                        Text("Preview — final phases:", style = MaterialTheme.typography.labelLarge)
                        trial.phases.takeLast(5).forEach { phase ->
                            Text(
                                formatOffset(phase.startSeconds) + "–" +
                                    formatOffset(phase.endSeconds) + " · " +
                                    phase.kind.name.replace('_', ' ') +
                                    " (${phase.durationSeconds / 60} min)",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (trial.needsSafetyAcknowledgement) {
                            Text(
                                "Rest warning: posing exceeds 25 cumulative minutes " +
                                    "without a 5-minute break. Acknowledgement is required " +
                                    "before Save.",
                                color = atelierErrorTextColor(),
                            )
                        }
                    }
                    Text(
                        "No change is saved until you confirm the new full timeline " +
                            "in the Editor and press Save class.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                AtelierTextButton(
                    enabled = canApply,
                    modifier = Modifier.testTag("tail_remainder_apply"),
                    onClick = {
                        if (selection == TailRecoveryChoice.FINAL_SESSION && validExtension) {
                            tailExtensionMinutes = minutes
                        } else if (selection == TailRecoveryChoice.SUMMARY && validSummary) {
                            summary = (rules.summarySeconds / 60 + minutes).toString()
                            tailExtensionMinutes = 0
                        }
                        ending = null
                        acceptedWarning = false
                        approved = null
                        saveError = null
                        overwriteTarget = null
                        showTailRecovery = false
                    },
                ) {
                    Text(if (selection == TailRecoveryChoice.EDITOR)
                        "Return to Editor" else "Apply to Editor")
                }
            },
            dismissButton = {
                AtelierTextButton(onClick = { showTailRecovery = false }) { Text("Cancel") }
            },
        )
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
                    if (classAlertVolumeMuted) {
                        Text((if (soundSettings.classAlertsUseAlarmVolume)
                            "Alarm" else "Media") + " volume is at zero. Class cues " +
                            "may be silent until you raise it.",
                            color = atelierErrorTextColor(),
                            style = MaterialTheme.typography.bodySmall)
                    }
                    // Restore the original uniform D158 Calculated finish line.
                    Text(
                        "Calculated finish: " + clockFormat.format(Date(calculatedFinish)),
                        modifier = Modifier.testTag("run_end_calculated"),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (finishPickerVisible) {
                        Text(
                            "Red values cannot be selected.",
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
                                    " Scroll back toward green or tap Back.",
                                modifier = Modifier.testTag("run_end_blocked_reason"),
                                color = atelierErrorTextColor(),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
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
                // All three choices are visible in a dedicated bottom action
                // area. A two-row arrangement prevents truncated button labels
                // on narrow phones; it also keeps actions out of wheel scrolling.
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .testTag("run_end_actions"),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    AtelierOutlinedButton(
                        modifier = Modifier.fillMaxWidth()
                            .testTag("run_end_change_finish"),
                        onClick = {
                            if (finishPickerVisible) {
                                // Back closes the wheels and resumes the live
                                // calculated finish, including after edits.
                                finishPickerVisible = false
                                customFinishEnabled = false
                            } else {
                                // Opening alone never freezes the live finish.
                                finishPickerVisible = true
                                customFinishEnabled = false
                            }
                            runApprovalError = null
                            runFinishBlockedReason = null
                        },
                    ) {
                        Text(if (finishPickerVisible) "Back" else "Change finish time",
                            maxLines = 1)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AtelierOutlinedButton(
                            modifier = Modifier.weight(1f)
                                .testTag("run_end_cancel"),
                            onClick = {
                                pendingRunApproval = null
                                runApprovalError = null
                            },
                        ) { Text("Cancel", maxLines = 1) }
                        AtelierButton(
                            modifier = Modifier.weight(1f)
                                .testTag("run_end_confirm"),
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
                        ) {
                            Text(
                                "Confirm & Run",
                                maxLines = 1,
                                softWrap = false,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 11.sp, maxFontSize = 14.sp,
                                    stepSize = 0.5.sp,
                                ),
                            )
                        }
                    }
                }
            },
            dismissButton = {},
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
                AtelierTextButton(colors = atelierTextColors(), onClick = { finalSave(target.id) }) {
                    Text("Overwrite class")
                }
            },
            dismissButton = {
                AtelierTextButton(colors = atelierTextColors(), onClick = { overwriteTarget = null }) {
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
                AtelierTextButton(colors = atelierTextColors(), onClick = { helpSection = null }) { Text("Close") }
            },
        )
    }
    }
    }
}

/**
 * Keep two-column Editor inputs on approved normal-width displays, but prevent
 * clipped long labels when Android font scaling is large or the screen is narrow.
 * Pure layout adaptation: values, callbacks, validation and persistence are unchanged.
 */
@Composable
private fun AdaptiveMinuteInputPair(
    firstLabel: String,
    firstValue: String,
    onFirstChange: (String) -> Unit,
    secondLabel: String,
    secondValue: String,
    onSecondChange: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val stackFields = LocalDensity.current.fontScale >= 1.3f || maxWidth < 320.dp
        if (stackFields) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MinuteInput(firstLabel, firstValue, onFirstChange, Modifier.fillMaxWidth())
                MinuteInput(secondLabel, secondValue, onSecondChange, Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MinuteInput(firstLabel, firstValue, onFirstChange, Modifier.weight(1f))
                MinuteInput(secondLabel, secondValue, onSecondChange, Modifier.weight(1f))
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
