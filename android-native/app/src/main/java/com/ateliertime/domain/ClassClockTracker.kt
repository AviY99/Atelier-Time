package com.ateliertime.domain

/**
 * Clock display timestamps are real epoch milliseconds. The actual countdown
 * itself remains based on the injected monotonic elapsedRealtime clock.
 *
 * Class = the full lesson; Session = one work/pose phase; Break = one rest phase.
 * End-of-class summary is a distinct phase, not another session/break.
 */
data class ClassClockTimes(
    val classStartEpochMs: Long? = null,
    val classEndEpochMs: Long? = null,
    val currentPhaseStartEpochMs: Long? = null,
    val currentPhaseEndEpochMs: Long? = null,
    val currentPhaseLabel: String? = null,
    val paused: Boolean = false,
)

/**
 * Observe every tick, including manual Next and Restart transitions.
 * - Class start uses actual start time, never inferred from virtual phase offset.
 * - Class finish stays at the ORIGINAL wall-clock deadline during recovery.
 * - New phase start is actual wall time on Next and estimated transition time
 *   on a normal tick; pause keeps this start, but hides an uncertain phase end.
 * - All timestamps survive tab switches (tracker is owned by root composition).
 */
class ClassClockTracker {
    private var classStarted: Long? = null
    private var classDeadline: Long? = null
    private var phaseId: String? = null
    private var phaseStarted: Long? = null
    private var wasFinished = false

    fun newClass(view: TimerView, nowEpochMs: Long): ClassClockTimes {
        require(view.status == TimerStatus.RUNNING && view.elapsedSeconds == 0) {
            "A new class must start at its first phase."
        }
        classStarted = nowEpochMs
        classDeadline = nowEpochMs + view.totalSeconds.toLong() * 1_000L
        phaseId = null
        phaseStarted = null
        wasFinished = false
        return observe(view, nowEpochMs)
    }

    /** Restart preserves the original finish target but starts class again now. */
    fun restartClass(view: TimerView, nowEpochMs: Long): ClassClockTimes {
        require(view.status == TimerStatus.RUNNING && view.elapsedSeconds == 0)
        classStarted = nowEpochMs
        if (classDeadline == null) {
            classDeadline = nowEpochMs + view.totalSeconds.toLong() * 1_000L
        }
        phaseId = null
        phaseStarted = null
        wasFinished = false
        return observe(view, nowEpochMs)
    }

    /** Restart just the active Session OR typed Break, keeping Class clocks. */
    fun restartCurrentSegment(view: TimerView, nowEpochMs: Long): ClassClockTimes {
        require(view.status == TimerStatus.RUNNING &&
            (view.phase?.kind == PhaseKind.POSE ||
             view.phase?.kind == PhaseKind.SHORT_BREAK ||
             view.phase?.kind == PhaseKind.SPECIAL_BREAK)) {
            "A Session or Break must be running after restart."
        }
        phaseId = null
        phaseStarted = null
        return observe(view, nowEpochMs)
    }

    /** Compatibility for callers that still restart only a work Session. */
    fun restartCurrentSession(view: TimerView, nowEpochMs: Long): ClassClockTimes {
        require(view.phase?.kind == PhaseKind.POSE) { "Expected a work Session." }
        return restartCurrentSegment(view, nowEpochMs)
    }

    fun observe(view: TimerView, nowEpochMs: Long): ClassClockTimes {
        if (view.status == TimerStatus.READY && classStarted == null) {
            return ClassClockTimes()
        }
        if (view.status == TimerStatus.FINISHED) {
            if (!wasFinished) {
                // Early completion of summary via Next is a real earlier finish.
                classDeadline = nowEpochMs
                wasFinished = true
            }
            phaseId = null
            phaseStarted = null
            return ClassClockTimes(
                classStartEpochMs = classStarted,
                classEndEpochMs = classDeadline,
            )
        }
        val phase = view.phase
        if (phase != null && phase.id != phaseId) {
            phaseId = phase.id
            val elapsedWithinPhase = (view.elapsedSeconds - phase.startSeconds)
                .coerceIn(0, phase.durationSeconds)
            phaseStarted = nowEpochMs - elapsedWithinPhase.toLong() * 1_000L
        }
        val halted = view.status == TimerStatus.PAUSED ||
            view.status == TimerStatus.STOPPED
        return ClassClockTimes(
            classStartEpochMs = classStarted,
            classEndEpochMs = classDeadline,
            currentPhaseStartEpochMs = if (phase == null) null else phaseStarted,
            currentPhaseEndEpochMs = if (phase == null || halted) null
                else nowEpochMs + view.phaseRemainingSeconds.toLong() * 1_000L,
            currentPhaseLabel = phase?.let(::clockPhaseLabel),
            paused = halted,
        )
    }
}

fun clockPhaseLabel(phase: SchedulePhase): String = when (phase.kind) {
    PhaseKind.POSE -> "Session " + (phase.poseNumber ?: "")
    PhaseKind.SHORT_BREAK -> if (phase.openingGroupId != null) "Break · opening group"
        else "Break · regular"
    PhaseKind.SPECIAL_BREAK -> "Break · special"
    PhaseKind.SUMMARY -> "Class summary"
}
