package com.ateliertime.android

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import com.ateliertime.domain.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Private, append-only event journal: one JSON object per line.
 * No periodic 200ms ticks, network requests, audio contents or file URIs.
 * Persist the approved Editor snapshot once at start, then action/transition
 * events and the actual future schedule whenever it changes.
 */
internal class RunDiagnosticsArchive(private val context: Context) {
    private val directory = File(context.filesDir, "atelier_run_reports")

    fun begin(
        session: ApprovedSession,
        startEpochMs: Long,
        endEpochMs: Long,
        sounds: SoundSettings,
        alertsEnabled: Boolean,
    ): RunDiagnosticsRecorder {
        check(directory.isDirectory || directory.mkdirs()) {
            "Could not create the private diagnostics directory."
        }
        val runId = UUID.randomUUID().toString()
        val file = File(directory, "run-${startEpochMs}-$runId.jsonl")
        val recorder = RunDiagnosticsRecorder(file, runId, startEpochMs, endEpochMs,
            session.rules)
        val version = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "unknown"
        recorder.append(JSONObject()
            .put("type", "run_start")
            .put("schema", 1)
            .put("runId", runId)
            .put("epochMs", startEpochMs)
            .put("monotonicMs", SystemClock.elapsedRealtime())
            .put("device", JSONObject()
                .put("androidApi", Build.VERSION.SDK_INT)
                .put("manufacturer", Build.MANUFACTURER)
                .put("model", Build.MODEL)
                .put("appVersion", version)
                .put("timezone", java.util.TimeZone.getDefault().id))
            .put("approvedClassEndEpochMs", endEpochMs)
            .put("className", session.rules.name)
            .put("editorRules", session.rules.toDiagnosticJson())
            .put("endingChoice", session.selectedChoice?.let {
                JSONObject().put("mode", it.mode.name)
                    .put("firstPoseSeconds", it.firstPoseSeconds ?: JSONObject.NULL)
            } ?: JSONObject.NULL)
            .put("acknowledgedWarnings", JSONArray().apply {
                session.acknowledgedWarnings.forEach {
                    put(JSONObject().put("code", it.code).put("message", it.message))
                }
            })
            .put("approvedTimeline", session.phases.toDiagnosticJson())
            .put("soundSettings", sounds.toDiagnosticJson())
            .put("alertsEnabled", alertsEnabled))
        return recorder
    }

    fun reports(): List<File> = directory.listFiles()
        ?.filter { it.isFile && it.name.startsWith("run-") &&
            it.name.endsWith(".jsonl") }
        ?.sortedByDescending { it.name } ?: emptyList()

    private fun report(name: String): File = reports().firstOrNull {
        it.name == name
    } ?: throw IllegalArgumentException("The selected diagnostic report does not exist.")

    fun export(name: String, target: Uri) {
        val source = report(name)
        val output = context.contentResolver.openOutputStream(target)
            ?: throw IllegalStateException("Could not open the selected destination.")
        output.use { out -> source.inputStream().use { it.copyTo(out) } }
    }

    fun delete(name: String) {
        check(report(name).delete()) { "Could not delete the selected report." }
    }
}

internal class RunDiagnosticsRecorder(
    private val file: File,
    val runId: String,
    val startedEpochMs: Long,
    val approvedEndEpochMs: Long,
    private val rules: SessionRules,
) {
    private var sequence = 0
    private var closed = false

    @Synchronized
    fun append(entry: JSONObject, allowAfterFinish: Boolean = false) {
        check(!closed || allowAfterFinish) { "Diagnostic report is already closed." }
        entry.put("sequence", ++sequence)
        // Each event is terminated and flushed immediately; interrupted runs
        // remain diagnosable and are NOT falsely marked as completed.
        FileOutputStream(file, true).bufferedWriter(Charsets.UTF_8).use { out ->
            out.append(entry.toString()).append('\n')
        }
    }

    fun event(
        action: String,
        before: TimerView?,
        after: TimerView?,
        phasesBefore: List<SchedulePhase>?,
        phasesAfter: List<SchedulePhase>?,
        cue: TimerCue? = null,
        alertsEnabled: Boolean,
        sounds: SoundSettings,
        rejectedReason: String? = null,
    ) {
        val now = System.currentTimeMillis()
        val changed = phasesBefore != null && phasesAfter != null &&
            phasesBefore != phasesAfter
        append(JSONObject()
            .put("type", "event")
            .put("action", action)
            .put("status", if (rejectedReason == null) "accepted" else "rejected")
            .put("reason", rejectedReason ?: JSONObject.NULL)
            .put("epochMs", now)
            .put("monotonicMs", SystemClock.elapsedRealtime())
            .put("physicalRemainingMs", approvedEndEpochMs - now)
            .put("before", before.toDiagnosticJson())
            .put("after", after.toDiagnosticJson())
            .put("cue", cue?.name ?: JSONObject.NULL)
            .put("cueRequest", when {
                cue == null -> "none"
                !alertsEnabled -> "disabled"
                else -> "requested_not_audibility_proof"
            })
            .put("soundSettings", if (cue != null)
                sounds.toDiagnosticJson() else JSONObject.NULL)
            .put("structureChanged", changed)
            .put("plannerConstraints", JSONObject()
                .put("regularSessionSeconds", rules.poseSeconds)
                .put("regularBreakSeconds", rules.shortBreakSeconds)
                .put("minimumWorkSessionSeconds", SECONDS_PER_MINUTE)
                .put("singleTailSplitThresholdSeconds",
                    rules.poseSeconds.toLong() + rules.shortBreakSeconds + SECONDS_PER_MINUTE)
                .put("pairInsertionStrictThresholdSeconds",
                    2L * rules.poseSeconds + rules.shortBreakSeconds)
                .put("summaryMustStaySeconds", rules.summarySeconds)
                .put("approvedPhysicalFinishEpochMs", approvedEndEpochMs))
            .put("addedPhaseIds", JSONArray(
                phasesAfter.orEmpty().filter { candidate ->
                    phasesBefore.orEmpty().none { it.id == candidate.id }
                }.map { it.id }))
            .put("removedPhaseIds", JSONArray(
                phasesBefore.orEmpty().filter { old ->
                    phasesAfter.orEmpty().none { it.id == old.id }
                }.map { it.id }))
            .put("scheduleBefore", if (changed) phasesBefore!!.toDiagnosticJson()
                else JSONObject.NULL)
            .put("scheduleAfter", if (changed) phasesAfter!!.toDiagnosticJson()
                else JSONObject.NULL))
    }

    fun soundChange(sounds: SoundSettings, alertsEnabled: Boolean) {
        append(JSONObject().put("type", "sound_settings_change")
            .put("epochMs", System.currentTimeMillis())
            .put("monotonicMs", SystemClock.elapsedRealtime())
            .put("soundSettings", sounds.toDiagnosticJson())
            .put("alertsEnabled", alertsEnabled))
    }

    fun note(category: String, message: String) {
        append(JSONObject().put("type", "diagnostic")
            .put("category", category).put("message", message)
            .put("epochMs", System.currentTimeMillis())
            .put("monotonicMs", SystemClock.elapsedRealtime()),
            allowAfterFinish = true)
    }

    @Synchronized
    fun finish(reason: String) {
        if (closed) return
        append(JSONObject().put("type", "run_end")
            .put("reason", reason)
            .put("epochMs", System.currentTimeMillis())
            .put("monotonicMs", SystemClock.elapsedRealtime()))
        closed = true
    }
}

private fun TimerView?.toDiagnosticJson(): Any {
    if (this == null) return JSONObject.NULL
    return JSONObject()
        .put("status", status.name)
        .put("elapsedSeconds", elapsedSeconds)
        .put("totalSeconds", totalSeconds)
        .put("totalRemainingSeconds", totalRemainingSeconds)
        .put("phaseRemainingSeconds", phaseRemainingSeconds)
        .put("phaseRemainingMillis", phaseRemainingMillis ?: JSONObject.NULL)
        .put("recoveredSeconds", recoveredSeconds)
        .put("phase", phase?.toDiagnosticJson() ?: JSONObject.NULL)
        .put("nextPhase", nextPhase?.toDiagnosticJson() ?: JSONObject.NULL)
}

private fun SessionRules.toDiagnosticJson(): JSONObject = JSONObject()
    .put("id", id).put("name", name)
    .put("totalSeconds", totalSeconds).put("summarySeconds", summarySeconds)
    .put("poseSeconds", poseSeconds)
    .put("shortBreakSeconds", shortBreakSeconds)
    .put("specialBreakSeconds", specialBreakSeconds)
    .put("specialBreakCount", specialBreakCount)
    .put("cycleStart", cycleStart.name)
    .put("automaticSpecialPlacement", placement.automatic)
    .put("manualSpecialOrdinals", JSONArray(placement.breakOrdinals))
    .put("openingGroups", JSONArray().apply {
        openingGroups.forEach { group -> put(JSONObject()
            .put("id", group.id)
            .put("count", group.count)
            .put("poseSeconds", group.poseSeconds)
            .put("breakAfterSeconds", group.breakAfterSeconds ?: JSONObject.NULL))
        }
    })

private fun SchedulePhase.toDiagnosticJson(): JSONObject = JSONObject()
    .put("id", id).put("kind", kind.name)
    .put("startSeconds", startSeconds).put("endSeconds", endSeconds)
    .put("durationSeconds", durationSeconds)
    .put("poseNumber", poseNumber ?: JSONObject.NULL)
    .put("poseSource", poseSource?.name ?: JSONObject.NULL)
    .put("openingGroupId", openingGroupId ?: JSONObject.NULL)
    .put("breakOrdinal", breakOrdinal ?: JSONObject.NULL)

private fun List<SchedulePhase>.toDiagnosticJson(): JSONArray {
    val result = JSONArray()
    forEach { result.put(it.toDiagnosticJson()) }
    return result
}

private fun SoundSettings.toDiagnosticJson(): JSONObject {
    fun cue(c: TimerCue): JSONObject {
        val sound = soundFor(c)
        return JSONObject()
            .put("source", sound.source.name)
            .put("melody", sound.melody.name)
            .put("displayName", sound.displayName ?: JSONObject.NULL)
            // No content URI: may reveal unrelated user documents.
            .put("durationSeconds", secondsFor(c))
    }
    return JSONObject()
        .put("mode", assignmentMode.name)
        .put("enabledSources", JSONArray(enabledSources.map { it.name }))
        .put("alarmVolume", classAlertsUseAlarmVolume)
        .put("A", cue(TimerCue.POSE_FINISHED))
        .put("B", cue(TimerCue.BREAK_FINISHED))
        .put("C", cue(TimerCue.SESSION_FINISHED))
        .put("free", cue(TimerCue.FREE_TIMER_FINISHED))
}
