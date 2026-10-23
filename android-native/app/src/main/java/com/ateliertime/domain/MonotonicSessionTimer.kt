package com.ateliertime.domain

/**
 * Platform-neutral runtime. Milliseconds come from an injected monotonic clock
 * (SystemClock.elapsedRealtime on Android), never wall-clock time or tick counts.
 *
 * Sound events are emitted exactly once for adjacent phase transitions observed
 * while running. Missed old transitions are NOT replayed after a long suspension.
 * Consecutive opening poses deliberately have no automatic cue pending D-016.
 */
enum class TimerStatus { READY, RUNNING, PAUSED, FINISHED, STOPPED }
enum class TimerCue { POSE_FINISHED, BREAK_FINISHED, SESSION_FINISHED }

data class TimerView(
    val status: TimerStatus,
    val elapsedSeconds: Int,
    val totalSeconds: Int,
    val phase: SchedulePhase?,
    val nextPhase: SchedulePhase?,
    val phaseRemainingSeconds: Int,
    val totalRemainingSeconds: Int,
)

data class TimerTick(val view: TimerView, val cue: TimerCue? = null)

class MonotonicSessionTimer(
    private val session: ApprovedSession,
    private val nowMillis: () -> Long,
) {
    private var status = TimerStatus.READY
    private var elapsedBeforeRunMs = 0L
    private var runningFromMs: Long? = null
    private var lastPhaseIndex: Int? = null
    private val totalMs = session.rules.totalSeconds.toLong() * 1000L

    fun start(): TimerView {
        check(status == TimerStatus.READY || status == TimerStatus.STOPPED ||
            status == TimerStatus.FINISHED) { "Pause or stop before starting again." }
        elapsedBeforeRunMs = 0L
        runningFromMs = nowMillis()
        lastPhaseIndex = 0
        status = TimerStatus.RUNNING
        return view()
    }

    fun pause(): TimerView {
        if (status == TimerStatus.RUNNING) {
            elapsedBeforeRunMs = elapsedMs()
            runningFromMs = null
            status = TimerStatus.PAUSED
        }
        return view()
    }

    fun resume(): TimerView {
        if (status == TimerStatus.PAUSED) {
            runningFromMs = nowMillis()
            status = TimerStatus.RUNNING
        }
        return view()
    }

    fun stop(): TimerView {
        elapsedBeforeRunMs = 0L
        runningFromMs = null
        status = TimerStatus.STOPPED
        lastPhaseIndex = null
        return view()
    }

    fun view(): TimerView {
        val elapsed = (elapsedMs() / 1000).toInt()
        val index = phaseIndex(elapsed)
        val phase = index?.let { session.phases[it] }
        return TimerView(
            status = status,
            elapsedSeconds = elapsed,
            totalSeconds = session.rules.totalSeconds,
            phase = phase,
            nextPhase = index?.let { session.phases.getOrNull(it + 1) },
            phaseRemainingSeconds = phase?.let { it.endSeconds - elapsed } ?: 0,
            totalRemainingSeconds = session.rules.totalSeconds - elapsed,
        )
    }

    /** Call from the UI clock loop; returns only the most recent due cue. */
    fun update(): TimerTick {
        if (status != TimerStatus.RUNNING) return TimerTick(view())
        val next = view()
        val newIndex = phaseIndex(next.elapsedSeconds)
        val oldIndex = lastPhaseIndex
        if (next.totalRemainingSeconds == 0) {
            status = TimerStatus.FINISHED
            runningFromMs = null
            elapsedBeforeRunMs = totalMs
            lastPhaseIndex = null
            return TimerTick(view(), TimerCue.SESSION_FINISHED)
        }
        val cue = if (oldIndex != null && newIndex == oldIndex + 1) {
            val justEnded = session.phases[oldIndex]
            // Avoid playing stale sounds for several crossed phases after wakeup.
            if (next.elapsedSeconds - justEnded.endSeconds > 1) null
            else when (justEnded.kind) {
                PhaseKind.POSE ->
                    if (next.phase?.kind != PhaseKind.POSE) TimerCue.POSE_FINISHED else null
                PhaseKind.SHORT_BREAK, PhaseKind.SPECIAL_BREAK -> TimerCue.BREAK_FINISHED
                PhaseKind.SUMMARY -> null
            }
        } else null
        lastPhaseIndex = newIndex
        return TimerTick(next, cue)
    }

    private fun elapsedMs(): Long {
        val clockDelta = runningFromMs?.let { (nowMillis() - it).coerceAtLeast(0L) } ?: 0L
        return (elapsedBeforeRunMs + clockDelta).coerceIn(0L, totalMs)
    }

    private fun phaseIndex(elapsedSeconds: Int): Int? =
        if (elapsedSeconds >= session.rules.totalSeconds) null
        else session.phases.indexOfFirst { elapsedSeconds < it.endSeconds }
            .takeIf { it >= 0 }
}
