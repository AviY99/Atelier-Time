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
enum class TimerCue { POSE_FINISHED, BREAK_FINISHED, SESSION_FINISHED, FREE_TIMER_FINISHED }

data class TimerView(
    val status: TimerStatus,
    val elapsedSeconds: Int,
    val totalSeconds: Int,
    val phase: SchedulePhase?,
    val nextPhase: SchedulePhase?,
    val phaseRemainingSeconds: Int,
    val totalRemainingSeconds: Int,
    val recoveredSeconds: Int = 0,
    /** Precise active phase time for stable wall-clock End (not rounded seconds). */
    val phaseRemainingMillis: Long? = null,
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
    // Initial user-approved end-time shift is part of every later correction.
    private var initialEndCorrectionSeconds = 0
    private var activePhases: List<SchedulePhase> = session.phases
    private var activeTotalMs = session.rules.totalSeconds.toLong() * 1000L
    private var lastPhaseIndex: Int? = null
    private var classFinishCueIssued = false
    private var nextHasOccurred = false
    private val observedFragments = mutableListOf<RuntimeTimelineSegment>()
    /** Virtual position where this visible attempt started (after Next/Restart/Pause). */
    private var fragmentStartMs = 0L

    private fun appendFragment(phase: SchedulePhase, untilMs: Long) {
        val seconds = ((untilMs - fragmentStartMs).coerceAtLeast(0L) / 1_000L)
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        if (seconds > 0) observedFragments.add(
            RuntimeTimelineSegment(phase.id, phase.kind, seconds, observed = true))
        fragmentStartMs = untilMs
    }

    private fun appendPause(durationMs: Long) {
        val seconds = ((durationMs.coerceAtLeast(0L) + 999L) / 1_000L)
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        if (seconds > 0) observedFragments.add(
            RuntimeTimelineSegment("paused-${observedFragments.size}", null,
                seconds, observed = true))
    }

    /** Never mistake the virtual jump of Next for minutes of work performed. */
    fun runtimeTimeline(): RuntimeTimeline {
        val fragments = observedFragments.toMutableList()
        val phase = view().phase
        val virtualNow = elapsedMs()
        if (phase != null) {
            val performed = ((virtualNow - fragmentStartMs).coerceAtLeast(0L) / 1_000L)
                .toInt()
            if (performed > 0) fragments.add(RuntimeTimelineSegment(
                phase.id, phase.kind, performed, observed = true))
        }
        if (status == TimerStatus.PAUSED || status == TimerStatus.STOPPED) {
            val pausedMs = (nowMillis() - (suspendedFromMs ?: nowMillis()))
                .coerceAtLeast(0L)
            val pausedSeconds = ((pausedMs + 999L) / 1_000L).toInt()
            if (pausedSeconds > 0) fragments.add(RuntimeTimelineSegment(
                "paused-live", null, pausedSeconds, observed = true))
        }
        val completed = fragments.sumOf { it.durationSeconds }
        val start = phase?.let { activePhases.indexOfFirst { current -> current.id == it.id } }
        if (phase != null && start != null && start >= 0) {
            val remaining = ((phase.endSeconds.toLong() * 1_000L - virtualNow)
                .coerceAtLeast(0L) / 1_000L).toInt()
            if (remaining > 0) fragments.add(RuntimeTimelineSegment(
                phase.id, phase.kind, remaining, observed = false))
            activePhases.drop(start + 1).forEach { future ->
                fragments.add(RuntimeTimelineSegment(future.id, future.kind,
                    future.durationSeconds, observed = false))
            }
        } else if (status == TimerStatus.READY) {
            activePhases.forEach { future -> fragments.add(RuntimeTimelineSegment(
                future.id, future.kind, future.durationSeconds, observed = false)) }
        }
        return RuntimeTimeline(fragments, completed)
    }

    fun start(initialCorrectionSeconds: Int = 0): TimerView {
        if (status == TimerStatus.STOPPED) {
            require(initialCorrectionSeconds == 0) { "Resume an existing Class before changing its end time." }
            return resume()
        }
        check(status == TimerStatus.READY || status == TimerStatus.FINISHED) {
            "Pause, continue or stop before starting again."
        }
        // Compute the entire adjusted plan before mutating timer state.
        // A rejected deadline leaves the Class READY, never half-started.
        val revised = if (initialCorrectionSeconds == 0)
            runCatching { FinalPairCompensation.adjust(session, 0) }.getOrNull()
        else FinalPairCompensation.adjust(session, initialCorrectionSeconds)
        elapsedBeforeRunMs = 0L
        initialEndCorrectionSeconds = initialCorrectionSeconds
        lostSeconds = initialCorrectionSeconds
        activePhases = revised?.phases ?: session.phases
        activeTotalMs = (revised?.totalSeconds ?: session.rules.totalSeconds).toLong() * 1000L
        runningFromMs = nowMillis()
        originalStartMs = runningFromMs
        suspendedFromMs = null
        lastPhaseIndex = 0
        classFinishCueIssued = false
        nextHasOccurred = false
        observedFragments.clear()
        fragmentStartMs = 0L
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
        val combinedCorrection = initialEndCorrectionSeconds.toLong() + secondsToRecover
        require(combinedCorrection in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            "The time change exceeds the supported finish-time adjustment range."
        }
        val adjusted = FinalPairCompensation.adjust(session, combinedCorrection.toInt(),
            allowExtraRegular = nextHasOccurred)
        // Seal the old attempt only AFTER successful feasibility validation.
        view().phase?.let { appendFragment(it, elapsedMs()) }
        if (status == TimerStatus.PAUSED || status == TimerStatus.STOPPED) {
            appendPause((now - (suspendedFromMs ?: now)).coerceAtLeast(0L))
        }
        activePhases = adjusted.phases
        activeTotalMs = adjusted.totalSeconds.toLong() * 1000L
        lostSeconds = adjusted.recoveredSeconds
        elapsedBeforeRunMs = 0L
        runningFromMs = now
        suspendedFromMs = null
        lastPhaseIndex = 0
        classFinishCueIssued = false
        fragmentStartMs = 0L
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
        val lastOriginalId = session.phases.getOrNull(regularIndices.lastOrNull() ?: -1)?.id
        val finalIsMerged = lastOriginalId != null && activePhases.none { it.id == lastOriginalId }
        val activeLastIndex = activePhases.indexOfFirst { it.id == lastOriginalId }
        check(regularIndices.size == 2 && (finalIsMerged || index < activeLastIndex) &&
            !(finalIsMerged && current.id == session.phases[regularIndices.first()].id)) {
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
        val fullReflow = runCatching {
            FinalPairCompensation.adjust(session, correction.toInt(),
                allowExtraRegular = nextHasOccurred)
        }.getOrNull()
        val revised = if (fullReflow != null &&
            fullReflow.phases.take(index + 1) == activePhases.take(index + 1))
            fullReflow else FinalPairCompensation.adjustLastOnly(
                session, activePhases, correction.toInt())
        val target = revised.phases[index]
        check(target.id == current.id && target.startSeconds == current.startSeconds) {
            "The current phase start would change. No restart was performed."
        }
        // Atomic commit: every feasibility check has passed.
        appendFragment(current, elapsedMs())
        if (status == TimerStatus.PAUSED) appendPause(pausedMs)
        activePhases = revised.phases
        activeTotalMs = revised.totalSeconds.toLong() * 1_000L
        lostSeconds = revised.recoveredSeconds
        elapsedBeforeRunMs = target.startSeconds.toLong() * 1_000L
        runningFromMs = now
        suspendedFromMs = null
        lastPhaseIndex = index
        fragmentStartMs = target.startSeconds.toLong() * 1_000L
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
            val current = view().phase
            if (current != null) appendFragment(current, elapsedMs())
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
            val originalFinal = session.phases.filter {
                it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
            }.takeLast(2)
            val firstFinal = activePhases.firstOrNull { it.id == originalFinal.firstOrNull()?.id }
            val lastFinal = activePhases.firstOrNull { it.id == originalFinal.lastOrNull()?.id }
            val insertedAlreadyStarted = activePhases.any { phase ->
                phase.id.startsWith("runtime-added-") &&
                    elapsedBeforeRunMs >= phase.startSeconds.toLong() * 1_000L
            }
            val revised = if (insertedAlreadyStarted || (firstFinal != null &&
                elapsedBeforeRunMs >= firstFinal.startSeconds.toLong() * 1_000L)) {
                // Once the penultimate Session begins it is an observed
                // phase. Keep it (and its intervening Break) unchanged; only
                // the not-yet-started final Session may absorb interruption.
                check(lastFinal != null && elapsedBeforeRunMs <
                    lastFinal.startSeconds.toLong() * 1_000L) {
                    "The final Session has already begun. Its time cannot be " +
                        "changed after an interruption."
                }
                FinalPairCompensation.adjustLastOnly(session, activePhases, nextLost.toInt())
            } else {
                FinalPairCompensation.adjust(session, nextLost.toInt(),
                    allowExtraRegular = nextHasOccurred)
            }
            // An interruption cannot silently rewrite a completed phase.
            check(activePhases.indices.all { index ->
                activePhases[index].endSeconds.toLong() * 1_000L > elapsedBeforeRunMs ||
                    activePhases[index] == revised.phases[index]
            }) { "A previously completed Session or Break cannot be changed." }
            activePhases = revised.phases
            activeTotalMs = revised.totalSeconds.toLong() * 1_000L
            lostSeconds = revised.recoveredSeconds
        }
        appendPause(interruptionMs)
        suspendedFromMs = null
        runningFromMs = now
        status = TimerStatus.RUNNING
        lastPhaseIndex = phaseIndex((elapsedBeforeRunMs / 1000L).toInt())
        return view()
    }

    fun view(): TimerView {
        val preciseElapsedMs = elapsedMs()
        val elapsed = (preciseElapsedMs / 1000L).toInt()
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
            phaseRemainingMillis = phase?.let {
                (it.endSeconds.toLong() * 1_000L - preciseElapsedMs).coerceAtLeast(0L)
            },
        )
    }

    /** Immutable snapshot of the ACTUAL runtime plan for the live chart. */
    fun activePhasesSnapshot(): List<SchedulePhase> = activePhases.toList()

    /** One item means the last pair and intervening break were merged. */
    fun finalEndingDurations(): List<Int> {
        val ids = session.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2).map { it.id }.toSet()
        return activePhases.filter { it.id in ids }.map { it.durationSeconds }
    }

    /** Values shown in the timer after a pause/stop changes the selected ending. */
    fun finalTwoPoseDurations(): Pair<Int, Int>? {
        val lengths = finalEndingDurations()
        return if (lengths.size == 2) lengths[0] to lengths[1] else null
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
     * In the final work pair, adjust ONLY the last not-yet-started work Session,
     * preserving observed earlier work; Next on the final work Session or
     * terminal summary completes the Class and returns to its saved-class home.
     * If preserving the finish target is impossible, Next still advances
     * without changing any approved or already-observed phase. The UI updates
     * the expected Class finish accordingly. One cue per successful action.
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
        // An optional Summary must never be skipped by Next on the final
        // regular Session. C is played ON ENTERING Summary, never twice.
        if (nextIndex >= activePhases.size) {
            appendFragment(current, elapsedMs())
            status = TimerStatus.FINISHED
            runningFromMs = null
            suspendedFromMs = null
            elapsedBeforeRunMs = activeTotalMs
            lastPhaseIndex = null
            val cue = if (classFinishCueIssued) null else TimerCue.SESSION_FINISHED
            classFinishCueIssued = true
            return TimerTick(view(), cue)
        }

        val enteringSummary = current.kind == PhaseKind.POSE &&
            current.poseSource == PoseSource.REGULAR &&
            activePhases[nextIndex].kind == PhaseKind.SUMMARY

        val now = nowMillis()
        val pausedMs = if (status == TimerStatus.PAUSED) {
            (now - (suspendedFromMs ?: now)).coerceAtLeast(0L)
        } else 0L
        val pausedSeconds = (pausedMs + 999L) / 1000L
        val skippedSeconds = previous.phaseRemainingSeconds.toLong()
        val corrected = lostSeconds.toLong() + pausedSeconds - skippedSeconds
        // Keep Next usable even when a very long pause exceeds the remaining
        // compensation capacity. Prefer original Class finish when feasible.
        // Never modify a final Session the user has already entered.
        val revision = if (!enteringSummary &&
            corrected in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            try {
                val fullReflow = runCatching {
                    FinalPairCompensation.adjust(session, corrected.toInt(),
                        allowExtraRegular = true)
                }.getOrNull()
                val adjusted = if (fullReflow != null &&
                    fullReflow.phases.take(nextIndex) == activePhases.take(nextIndex) &&
                    fullReflow.phases.getOrNull(nextIndex)?.let { next ->
                        next.startSeconds == activePhases[nextIndex].startSeconds
                    } == true) fullReflow else FinalPairCompensation.adjustLastOnly(
                    session, activePhases, corrected.toInt())
                check(adjusted.phases[nextIndex].startSeconds ==
                        activePhases[nextIndex].startSeconds)
                adjusted
            } catch (_: IllegalArgumentException) {
                null // Advance without compensation; Class finish will update.
            } catch (_: IllegalStateException) {
                null
            }
        } else null
        val target = revision?.phases?.get(nextIndex) ?: activePhases[nextIndex]
        appendFragment(current, elapsedMs())
        if (status == TimerStatus.PAUSED) appendPause(pausedMs)
        if (revision != null) {
            activePhases = revision.phases
            activeTotalMs = revision.totalSeconds.toLong() * 1000L
            lostSeconds = revision.recoveredSeconds
        }
        elapsedBeforeRunMs = target.startSeconds.toLong() * 1000L
        runningFromMs = now
        suspendedFromMs = null
        status = TimerStatus.RUNNING
        lastPhaseIndex = nextIndex
        fragmentStartMs = target.startSeconds.toLong() * 1_000L
        nextHasOccurred = true
        val cue = when {
            enteringSummary -> {
                classFinishCueIssued = true
                TimerCue.SESSION_FINISHED
            }
            current.kind == PhaseKind.POSE -> TimerCue.POSE_FINISHED
            current.kind == PhaseKind.SHORT_BREAK || current.kind == PhaseKind.SPECIAL_BREAK ->
                TimerCue.BREAK_FINISHED
            else -> null
        }
        return TimerTick(view(), cue)
    }

    /** Finish a running Class when its original wall-time deadline is reached. */
    fun finishAtDeadline(): TimerTick {
        if (status != TimerStatus.RUNNING && status != TimerStatus.PAUSED) {
            return TimerTick(view())
        }
        view().phase?.let { appendFragment(it, elapsedMs()) }
        status = TimerStatus.FINISHED
        runningFromMs = null
        suspendedFromMs = null
        elapsedBeforeRunMs = activeTotalMs
        lastPhaseIndex = null
        val cue = if (classFinishCueIssued) null else TimerCue.SESSION_FINISHED
        classFinishCueIssued = true
        return TimerTick(view(), cue)
    }

    /** One alert per observed phase transition; no stale multi-phase replay. */
    fun update(): TimerTick {
        if (status != TimerStatus.RUNNING) return TimerTick(view())
        val current = view()
        val newIndex = phaseIndex(current.elapsedSeconds)
        val oldIndex = lastPhaseIndex
        if (current.totalRemainingSeconds == 0) {
            if (oldIndex != null && oldIndex in activePhases.indices) {
                appendFragment(activePhases[oldIndex], activePhases[oldIndex].endSeconds.toLong() * 1_000L)
            }
            status = TimerStatus.FINISHED
            runningFromMs = null
            elapsedBeforeRunMs = activeTotalMs
            lastPhaseIndex = null
            val cue = if (classFinishCueIssued) null else TimerCue.SESSION_FINISHED
            classFinishCueIssued = true
            return TimerTick(view(), cue)
        }
        val cue = if (oldIndex != null && newIndex == oldIndex + 1) {
            val justEnded = activePhases[oldIndex]
            if (current.elapsedSeconds - justEnded.endSeconds > 5) null
            else when (justEnded.kind) {
                PhaseKind.POSE -> if (current.phase?.kind == PhaseKind.SUMMARY &&
                    justEnded.poseSource == PoseSource.REGULAR) {
                    classFinishCueIssued = true
                    TimerCue.SESSION_FINISHED
                } else TimerCue.POSE_FINISHED
                PhaseKind.SHORT_BREAK, PhaseKind.SPECIAL_BREAK -> TimerCue.BREAK_FINISHED
                PhaseKind.SUMMARY -> null
            }
        } else null
        if (oldIndex != null && oldIndex != newIndex) {
            val endIndex = (newIndex ?: activePhases.size).coerceAtMost(activePhases.size)
            for (i in oldIndex until endIndex) {
                val completed = activePhases[i]
                appendFragment(completed, completed.endSeconds.toLong() * 1_000L)
            }
            fragmentStartMs = newIndex?.let {
                activePhases[it].startSeconds.toLong() * 1_000L
            } ?: activeTotalMs
        }
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
