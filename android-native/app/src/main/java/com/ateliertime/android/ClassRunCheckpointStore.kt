package com.ateliertime.android

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import com.ateliertime.domain.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

/** A private live-run checkpoint; separate from the immutable saved Class library. */
internal data class StoredClassRunCheckpoint(
    val presetId: String,
    val presetSignature: String,
    val classStartEpochMs: Long,
    val classDeadlineEpochMs: Long,
    val writtenEpochMs: Long,
    val writtenMonotonicMs: Long,
    val bootCount: Int,
    val timer: ClassTimerSnapshot,
) {
    fun eligibleFor(id: String, signature: String, nowEpochMs: Long): Boolean =
        id == presetId && signature == presetSignature &&
            classStartEpochMs <= nowEpochMs &&
            nowEpochMs < classDeadlineEpochMs &&
            classDeadlineEpochMs > writtenEpochMs &&
            writtenEpochMs >= classStartEpochMs

    /**
     * Cross-process elapsedRealtime anchor. On the same boot, reject wall-clock
     * jumps larger than 60 seconds; on reboot, use the real epoch delta.
     */
    fun clockShift(nowEpochMs: Long, nowMonotonicMs: Long, currentBootCount: Int): Long {
        val wallDelta = nowEpochMs - writtenEpochMs
        val monotonicDelta = nowMonotonicMs - writtenMonotonicMs
        val sameBoot = bootCount >= 0 && bootCount == currentBootCount
        if (sameBoot && abs(wallDelta - monotonicDelta) > 60_000L) {
            throw IllegalStateException("Device clock changed; Class recovery is unsafe.")
        }
        if (bootCount < 0 || currentBootCount < 0) {
            check(abs(wallDelta - monotonicDelta) <= 60_000L) {
                "Reboot or clock change cannot be verified for this Class."
            }
        }
        return monotonicDelta - wallDelta
    }
}

internal class ClassRunCheckpointStore(
    context: Context,
    storageName: String = "atelier_time_active_class_run",
) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences(storageName, Context.MODE_PRIVATE)

    fun bootCount(): Int = runCatching {
        Settings.Global.getInt(app.contentResolver, Settings.Global.BOOT_COUNT, -1)
    }.getOrDefault(-1)

    fun read(): StoredClassRunCheckpoint? {
        val data = prefs.getString(KEY, null) ?: return null
        try {
            val json = JSONObject(data)
            require(json.getInt("version") == VERSION) {
                "Unsupported active-run checkpoint format."
            }
            val stored = StoredClassRunCheckpoint(
                presetId = json.getString("presetId"),
                presetSignature = json.getString("presetSignature"),
                classStartEpochMs = json.getLong("classStartEpochMs"),
                classDeadlineEpochMs = json.getLong("classDeadlineEpochMs"),
                writtenEpochMs = json.getLong("writtenEpochMs"),
                writtenMonotonicMs = json.getLong("writtenMonotonicMs"),
                bootCount = json.optInt("bootCount", -1),
                timer = parseTimer(json.getJSONObject("timer")),
            )
            require(stored.presetId.isNotBlank() &&
                stored.presetSignature.isNotBlank() &&
                stored.classStartEpochMs < stored.classDeadlineEpochMs) {
                "Corrupt active-run checkpoint."
            }
            return stored
        } catch (error: Exception) {
            throw IllegalStateException(
                "Previous Class checkpoint could not be read. Saved classes were not changed.",
                error,
            )
        }
    }

    /** Synchronous commit before treating a user action as recoverable. */
    fun write(
        id: String,
        signature: String,
        classStartEpochMs: Long,
        classDeadlineEpochMs: Long,
        timer: ClassTimerSnapshot,
    ) {
        require(id.isNotBlank() && signature.isNotBlank() &&
            classStartEpochMs < classDeadlineEpochMs)
        val nowEpoch = System.currentTimeMillis()
        val nowMono = SystemClock.elapsedRealtime()
        val root = JSONObject()
            .put("version", VERSION)
            .put("presetId", id)
            .put("presetSignature", signature)
            .put("classStartEpochMs", classStartEpochMs)
            .put("classDeadlineEpochMs", classDeadlineEpochMs)
            .put("writtenEpochMs", nowEpoch)
            .put("writtenMonotonicMs", nowMono)
            .put("bootCount", bootCount())
            .put("timer", timerJson(timer))
        check(prefs.edit().putString(KEY, root.toString()).commit()) {
            "Active Class checkpoint could not be saved."
        }
    }

    fun clear() {
        check(prefs.edit().remove(KEY).commit()) {
            "Active Class checkpoint could not be cleared."
        }
    }

    private fun timerJson(s: ClassTimerSnapshot) = JSONObject()
        .put("status", s.status.name)
        .put("originalStartMs", s.originalStartMs)
        .put("elapsedBeforeRunMs", s.elapsedBeforeRunMs)
        .put("runningFromMs", s.runningFromMs ?: JSONObject.NULL)
        .put("suspendedFromMs", s.suspendedFromMs ?: JSONObject.NULL)
        .put("lostSeconds", s.lostSeconds)
        .put("lostMillis", s.lostMillis)
        .put("initialEndCorrectionSeconds", s.initialEndCorrectionSeconds)
        .put("physicalDeadlineMs", s.physicalDeadlineMs)
        .put("activePhases", JSONArray().apply {
            s.activePhases.forEach { put(phaseJson(it)) }
        })
        .put("activeTotalMs", s.activeTotalMs)
        .put("lastPhaseIndex", s.lastPhaseIndex ?: JSONObject.NULL)
        .put("classFinishCueIssued", s.classFinishCueIssued)
        .put("nextHasOccurred", s.nextHasOccurred)
        .put("repeatLockedPhaseId", s.repeatLockedPhaseId ?: JSONObject.NULL)
        .put("observedFragments", JSONArray().apply {
            s.observedFragments.forEach { segment ->
                put(JSONObject()
                    .put("id", segment.id)
                    .put("kind", segment.kind?.name ?: JSONObject.NULL)
                    .put("durationSeconds", segment.durationSeconds)
                    .put("observed", segment.observed))
            }
        })
        .put("fragmentStartMs", s.fragmentStartMs)
        .put("pauseRemainderMs", s.pauseRemainderMs)

    private fun phaseJson(p: SchedulePhase) = JSONObject()
        .put("id", p.id)
        .put("kind", p.kind.name)
        .put("startSeconds", p.startSeconds)
        .put("endSeconds", p.endSeconds)
        .put("poseNumber", p.poseNumber ?: JSONObject.NULL)
        .put("poseSource", p.poseSource?.name ?: JSONObject.NULL)
        .put("openingGroupId", p.openingGroupId ?: JSONObject.NULL)
        .put("breakOrdinal", p.breakOrdinal ?: JSONObject.NULL)

    private fun parseTimer(j: JSONObject): ClassTimerSnapshot {
        fun optionalLong(key: String): Long? =
            if (j.isNull(key)) null else j.getLong(key)
        fun optionalInt(key: String): Int? =
            if (j.isNull(key)) null else j.getInt(key)
        fun optionalString(key: String): String? =
            if (j.isNull(key)) null else j.getString(key)
        val phases = j.getJSONArray("activePhases")
        val fragments = j.getJSONArray("observedFragments")
        return ClassTimerSnapshot(
            status = TimerStatus.valueOf(j.getString("status")),
            originalStartMs = j.getLong("originalStartMs"),
            elapsedBeforeRunMs = j.getLong("elapsedBeforeRunMs"),
            runningFromMs = optionalLong("runningFromMs"),
            suspendedFromMs = optionalLong("suspendedFromMs"),
            lostSeconds = j.getInt("lostSeconds"),
            lostMillis = j.getLong("lostMillis"),
            initialEndCorrectionSeconds = j.getInt("initialEndCorrectionSeconds"),
            physicalDeadlineMs = j.getLong("physicalDeadlineMs"),
            activePhases = (0 until phases.length()).map { i ->
                val phase = phases.getJSONObject(i)
                fun optionalPhaseInt(key: String): Int? =
                    if (phase.isNull(key)) null else phase.getInt(key)
                fun optionalPhaseString(key: String): String? =
                    if (phase.isNull(key)) null else phase.getString(key)
                SchedulePhase(
                    id = phase.getString("id"),
                    kind = PhaseKind.valueOf(phase.getString("kind")),
                    startSeconds = phase.getInt("startSeconds"),
                    endSeconds = phase.getInt("endSeconds"),
                    poseNumber = optionalPhaseInt("poseNumber"),
                    poseSource = optionalPhaseString("poseSource")?.let { PoseSource.valueOf(it) },
                    openingGroupId = optionalPhaseString("openingGroupId"),
                    breakOrdinal = optionalPhaseInt("breakOrdinal"),
                )
            },
            activeTotalMs = j.getLong("activeTotalMs"),
            lastPhaseIndex = optionalInt("lastPhaseIndex"),
            classFinishCueIssued = j.getBoolean("classFinishCueIssued"),
            nextHasOccurred = j.getBoolean("nextHasOccurred"),
            repeatLockedPhaseId = optionalString("repeatLockedPhaseId"),
            observedFragments = (0 until fragments.length()).map { i ->
                val segment = fragments.getJSONObject(i)
                RuntimeTimelineSegment(
                    id = segment.getString("id"),
                    kind = if (segment.isNull("kind")) null
                        else PhaseKind.valueOf(segment.getString("kind")),
                    durationSeconds = segment.getInt("durationSeconds"),
                    observed = segment.getBoolean("observed"),
                )
            },
            fragmentStartMs = j.getLong("fragmentStartMs"),
            pauseRemainderMs = j.getLong("pauseRemainderMs"),
        )
    }

    private companion object {
        const val KEY = "checkpoint_v1"
        const val VERSION = 1
    }
}
