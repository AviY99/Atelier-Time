package com.ateliertime.domain

/**
 * Monotonic elapsed-time engine. The approved plan is immutable; a separate
 * runtime copy compensates for pauses/stops before its last two regular poses.
 *
 * Pause/Stop freeze active phase time. When continued, the original wall-clock
 * finishing deadline is preserved by compressing only the selected final pair.
 * A restart after FINISHED begins a fresh whole session and new deadline.
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
    val recoveredSeconds: Int = 0,
)

data class TimerTick(val view: TimerView, val cue: TimerCue? = null)

class MonotonicSessionTimer(
    private val session: ApprovedSession,
    private val nowMillis: () -> Long,
) {
    private var status = TimerStatus.READY
    private var elapsedBeforeRunMs = 0L
    private var runningFromMs: Long? = null
    private var suspendedFromMs: Long? = null
    private var lostSeconds = 0
    private var activePhases: List<SchedulePhase> = session.phases
    private var activeTotalMs = session.rules.totalSeconds.toLong() * 1000L
    private var lastPhaseIndex: Int? = null

    fun start(): TimerView {
        if (status == TimerStatus.STOPPED) return resume()
        check(status == TimerStatus.READY || status == TimerStatus.FINISHED) {
            "Pause, continue or stop before starting again."
        }
        // A completed session starts over with a new finish deadline.
        elapsedBeforeRunMs = 0L
        lostSeconds = 0
        activePhases = session.phases
        activeTotalMs = session.rules.totalSeconds.toLong() * 1000L
        runningFromMs = nowMillis()
        suspendedFromMs = null
        lastPhaseIndex = 0
        status = TimerStatus.RUNNING
        return view()
    }

    /** Explicitly abandon old progress/deadline and begin a new full session. */
    fun restartFromBeginning(): TimerView {
        status = TimerStatus.READY
        runningFromMs = null
        suspendedFromMs = null
        return start()
    }

    fun pause(): TimerView {
        if (status == TimerStatus.RUNNING) {
            elapsedBeforeRunMs = elapsedMs()
            runningFromMs = null
            suspendedFromMs = nowMillis()
            status = TimerStatus.PAUSED
        }
        return view()
    }

    /**
     * Stop keeps the current progress; Continue after Stop honors the original
     * deadline via final-pair compression, as does Resume after Pause.
     * No approved/saved rules are rewritten.
     */
    fun stop(): TimerView {
        if (status == TimerStatus.RUNNING) pause()
        if (status == TimerStatus.PAUSED) status = TimerStatus.STOPPED
        return view()
    }

    fun resume(): TimerView {
        if (status != TimerStatus.PAUSED && status != TimerStatus.STOPPED) return view()
        val now = nowMillis()
        val interruptionMs = (now - (suspendedFromMs ?: now)).coerceAtLeast(0L)
        val addedSeconds = ((interruptionMs + 999L) / 1000L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val nextLost = lostSeconds.toLong() + addedSeconds
        check(nextLost <= Int.MAX_VALUE) { "Interruption is too long to resume." }
        if (nextLost > 0L) {
            val revised = FinalPairCompensation.adjust(session, nextLost.toInt())
            val firstAdjusted = revised.phases.indexOfFirst {
                it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR &&
                    it.endSeconds != session.phases.find { source -> source.id == it.id }?.endSeconds
            }
            // Never rewrite phases already reached/seen by the user.
            check(firstAdjusted < 0 ||
                elapsedBeforeRunMs < revised.phases[firstAdjusted].startSeconds * 1000L) {
                "The last two poses have already begun. Choose a revised end time " +
                    "and approve a new timeline instead of silently changing them."
            }
            activePhases = revised.phases
            activeTotalMs = revised.totalSeconds.toLong() * 1000L
            lostSeconds = revised.recoveredSeconds
        }
        suspendedFromMs = null
        runningFromMs = now
        status = TimerStatus.RUNNING
        lastPhaseIndex = phaseIndex((elapsedBeforeRunMs / 1000L).toInt())
        return view()
    }

    fun view(): TimerView {
        val elapsed = (elapsedMs() / 1000L).toInt()
        val index = phaseIndex(elapsed)
        val phase = index?.let { activePhases[it] }
        val totalSeconds = (activeTotalMs / 1000L).toInt()
        return TimerView(
            status = status,
            elapsedSeconds = elapsed,
            totalSeconds = totalSeconds,
            phase = phase,
            nextPhase = index?.let { activePhases.getOrNull(it + 1) },
            phaseRemainingSeconds = phase?.let { it.endSeconds - elapsed } ?: 0,
            totalRemainingSeconds = totalSeconds - elapsed,
            recoveredSeconds = lostSeconds,
        )
    }

    /** Values shown in the timer after a pause/stop changes the selected ending. */
    fun finalTwoPoseDurations(): Pair<Int, Int>? {
        val final = activePhases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        return if (final.size == 2)
            final[0].durationSeconds to final[1].durationSeconds
        else null
    }

    /** One alert per observed phase transition; no stale multi-phase replay. */
    fun update(): TimerTick {
        if (status != TimerStatus.RUNNING) return TimerTick(view())
        val current = view()
        val newIndex = phaseIndex(current.elapsedSeconds)
        val oldIndex = lastPhaseIndex
        if (current.totalRemainingSeconds == 0) {
            status = TimerStatus.FINISHED
            runningFromMs = null
            elapsedBeforeRunMs = activeTotalMs
            lastPhaseIndex = null
            return TimerTick(view(), TimerCue.SESSION_FINISHED)
        }
        val cue = if (oldIndex != null && newIndex == oldIndex + 1) {
            val justEnded = activePhases[oldIndex]
            if (current.elapsedSeconds - justEnded.endSeconds > 5) null
            else when (justEnded.kind) {
                PhaseKind.POSE -> TimerCue.POSE_FINISHED
                PhaseKind.SHORT_BREAK, PhaseKind.SPECIAL_BREAK -> TimerCue.BREAK_FINISHED
                PhaseKind.SUMMARY -> null
            }
        } else null
        lastPhaseIndex = newIndex
        return TimerTick(current, cue)
    }

    private fun elapsedMs(): Long {
        val delta = runningFromMs?.let { (nowMillis() - it).coerceAtLeast(0L) } ?: 0L
        return (elapsedBeforeRunMs + delta).coerceIn(0L, activeTotalMs)
    }

    private fun phaseIndex(elapsed: Int): Int? =
        if (elapsed * 1000L >= activeTotalMs) null else
            activePhases.indexOfFirst { elapsed < it.endSeconds }.takeIf { it >= 0 }
}
