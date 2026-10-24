package com.ateliertime.domain

/**
 * Monotonic elapsed-time engine. The approved plan is immutable; a separate
 * runtime copy compensates for pauses/stops before its last two regular poses.
 *
 * Pause freezes active phase time; Resume shrinks the last two regular poses
 * to retain the original finish clock. Next skips the CURRENT pose or break
 * and extends/shrinks the final pair according to time gained/lost.
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
    private var originalStartMs: Long? = null
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
        originalStartMs = runningFromMs
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

    /**
     * Stops the current run and begins the SAME session from its first phase.
     * The original monotonic deadline remains fixed; all elapsed wall time
     * since its first start is recovered ONLY from the final two regular poses.
     * Reject impossible compression without altering the live timer.
     */
    fun restartKeepingEndTime(): TimerView {
        val startAt = originalStartMs ?: return start()
        val now = nowMillis()
        val goneMs = (now - startAt).coerceAtLeast(0L)
        val secondsToRecover = ((goneMs + 999L) / 1000L)
        require(secondsToRecover <= Int.MAX_VALUE.toLong()) {
            "Too much time has elapsed to restart this session."
        }
        val adjusted = FinalPairCompensation.adjust(session, secondsToRecover.toInt())
        activePhases = adjusted.phases
        activeTotalMs = adjusted.totalSeconds.toLong() * 1000L
        lostSeconds = adjusted.recoveredSeconds
        elapsedBeforeRunMs = 0L
        runningFromMs = now
        suspendedFromMs = null
        lastPhaseIndex = 0
        status = TimerStatus.RUNNING
        return view()
    }

    /**
     * Repeats only the currently active work Session OR Break.
     *
     * The Class stays in progress and previously completed phases stay done.
     * Seconds already spent on this phase plus paused wall time are recovered
     * from the last two regular work Sessions, without changing the approved
     * stored plan. Existing Next credits are included in the signed correction.
     *
     * Before the final pair starts, honor the user's chosen split with both
     * future Sessions. While restarting the break BETWEEN the final pair,
     * the already-completed first Session is frozen and only the unfinished
     * last Session can be shortened. Reject any impossible change before
     * mutating the running timer. Summary and last final Session cannot restart.
     */
    fun restartCurrentSegmentKeepingEndTime(): TimerView {
        check(status == TimerStatus.RUNNING || status == TimerStatus.PAUSED) {
            "Start the class before restarting a Session or Break."
        }
        val current = view().phase ?: error("There is no active phase to restart.")
        check(current.kind == PhaseKind.POSE ||
            current.kind == PhaseKind.SHORT_BREAK ||
            current.kind == PhaseKind.SPECIAL_BREAK) {
            "The class summary cannot be restarted as a Session or Break."
        }
        val index = activePhases.indexOfFirst { it.id == current.id }
        check(index >= 0) { "Current phase is missing from the class timeline." }
        val regularIndices = session.phases.indices.filter {
            session.phases[it].kind == PhaseKind.POSE &&
                session.phases[it].poseSource == PoseSource.REGULAR
        }.takeLast(2)
        check(regularIndices.size == 2 && index < regularIndices.last()) {
            "The final regular Session has already started. Cannot rewrite " +
                "a previously completed final Session."
        }
        val now = nowMillis()
        val elapsedWithinMs = (elapsedMs() -
            current.startSeconds.toLong() * 1_000L).coerceAtLeast(0L)
        val pausedMs = if (status == TimerStatus.PAUSED) {
            (now - (suspendedFromMs ?: now)).coerceAtLeast(0L)
        } else 0L
        val addedSeconds = (elapsedWithinMs + pausedMs + 999L) / 1_000L
        val correction = lostSeconds.toLong() + addedSeconds
        require(correction in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            "The time change is too large to adjust the final two Sessions."
        }
        val revised = if (index == regularIndices[0] + 1) {
            FinalPairCompensation.adjustLastOnly(
                session, activePhases, correction.toInt(),
            )
        } else {
            FinalPairCompensation.adjust(session, correction.toInt())
        }
        val target = revised.phases[index]
        check(target.id == current.id && target.startSeconds == current.startSeconds) {
            "The current phase start would change. No restart was performed."
        }
        // Atomic commit: every feasibility check has passed.
        activePhases = revised.phases
        activeTotalMs = revised.totalSeconds.toLong() * 1_000L
        lostSeconds = revised.recoveredSeconds
        elapsedBeforeRunMs = target.startSeconds.toLong() * 1_000L
        runningFromMs = now
        suspendedFromMs = null
        lastPhaseIndex = index
        status = TimerStatus.RUNNING
        return view()
    }

    /** Backwards-compatible work-Session API; never use this to restart a Break. */
    fun restartCurrentSessionKeepingEndTime(): TimerView {
        val phase = view().phase
        check(phase?.kind == PhaseKind.POSE) {
            "This method restarts work Sessions only."
        }
        return restartCurrentSegmentKeepingEndTime()
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
        check(nextLost in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            "Interruption is too long to resume."
        }
        if (nextLost != lostSeconds.toLong()) {
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

    /**
     * Next means the next scheduled PHASE of this same approved session:
     * a pose, regular/special/opening-group break, or final summary.
     *
     * A click finishes the current phase and emits its normal cue (A pose,
     * B break). Skipping remaining phase time GAINs time, therefore the final
     * two regular poses are extended according to the chosen ending mode.
     * If previously paused, paused wall time is deducted first; the net can
     * be positive (shrink), zero or negative (extend).
     *
     * Once either final regular pose is in progress, changing both poses
     * would overwrite work already seen by the user; safely refuse instead.
     * The final summary can be ended early by Next: C sounds immediately.
     * All validations happen BEFORE touching any live runtime field.
     */
    fun nextPhase(): TimerTick {
        check(status == TimerStatus.RUNNING || status == TimerStatus.PAUSED) {
            "Start or resume the session before using Next."
        }
        val previous = view()
        val current = previous.phase
            ?: error("There is no active phase to skip.")
        val currentIndex = activePhases.indexOfFirst { it.id == current.id }
        check(currentIndex >= 0) { "Active phase no longer exists." }
        val nextIndex = currentIndex + 1
        // End-of-summary has no next phase. Complete immediately with C.
        if (nextIndex >= activePhases.size) {
            status = TimerStatus.FINISHED
            runningFromMs = null
            suspendedFromMs = null
            elapsedBeforeRunMs = activeTotalMs
            lastPhaseIndex = null
            return TimerTick(view(), TimerCue.SESSION_FINISHED)
        }

        val now = nowMillis()
        val pausedMs = if (status == TimerStatus.PAUSED) {
            (now - (suspendedFromMs ?: now)).coerceAtLeast(0L)
        } else 0L
        val pausedSeconds = (pausedMs + 999L) / 1000L
        val skippedSeconds = previous.phaseRemainingSeconds.toLong()
        val corrected = lostSeconds.toLong() + pausedSeconds - skippedSeconds
        require(corrected in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            "Too much time was gained or lost to adjust the final poses."
        }
        val revision = FinalPairCompensation.adjust(session, corrected.toInt())
        val finalFirstIndex = session.phases.indices.filter {
            session.phases[it].kind == PhaseKind.POSE &&
                session.phases[it].poseSource == PoseSource.REGULAR
        }.takeLast(2).first()
        check(currentIndex < finalFirstIndex || corrected == lostSeconds.toLong()) {
            "The final poses have already started. Skipping now cannot adjust " +
                "both without rewriting elapsed posing. Approve a revised end time."
        }
        // The start of the next phase is identical before/after a final-pair
        // adjustment until the first final pose itself starts.
        val target = revision.phases[nextIndex]
        check(target.startSeconds == activePhases[nextIndex].startSeconds) {
            "The next phase would move in time; refusing an unsafe skip."
        }

        activePhases = revision.phases
        activeTotalMs = revision.totalSeconds.toLong() * 1000L
        lostSeconds = revision.recoveredSeconds
        elapsedBeforeRunMs = target.startSeconds.toLong() * 1000L
        runningFromMs = now
        suspendedFromMs = null
        status = TimerStatus.RUNNING
        lastPhaseIndex = nextIndex
        val cue = when (current.kind) {
            PhaseKind.POSE -> TimerCue.POSE_FINISHED
            PhaseKind.SHORT_BREAK, PhaseKind.SPECIAL_BREAK -> TimerCue.BREAK_FINISHED
            PhaseKind.SUMMARY -> TimerCue.SESSION_FINISHED
        }
        return TimerTick(view(), cue)
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
