package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class MonotonicSessionTimerTest {
    private val minute = SECONDS_PER_MINUTE
    private val engine = ScheduleEngine()
    private fun approved(): ApprovedSession {
        val rules = SessionRules(
            id = "timer", name = "Clock", totalSeconds = 180 * minute,
            summarySeconds = 15 * minute, poseSeconds = 25 * minute,
            specialBreakCount = 1,
        )
        return engine.approve(engine.generate(rules, FinalPoseChoice.remainder()))
    }

    @Test fun startsFromApprovedTimelineAndAdvancesWithoutWallClock() {
        var now = 100_000L
        val timer = MonotonicSessionTimer(approved()) { now }
        assertEquals(TimerStatus.READY, timer.view().status)
        assertEquals(180 * minute, timer.view().totalRemainingSeconds)
        timer.start()
        now += 24_000L
        val view = timer.update().view
        assertEquals(24, view.elapsedSeconds)
        assertEquals(25 * minute - 24, view.phaseRemainingSeconds)
        assertEquals(PhaseKind.POSE, view.phase?.kind)
        assertEquals(PhaseKind.SHORT_BREAK, view.nextPhase?.kind)
    }

    @Test fun phaseRemainingMillisMaintainsSubsecondPrecisionForStableEndClock() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        val start = timer.start()
        val expected = 25L * minute * 1_000L
        assertEquals(expected, start.phaseRemainingMillis)
        now = 275L
        val first = timer.view()
        assertEquals(expected - 275L, first.phaseRemainingMillis)
        assertEquals(25 * minute, first.phaseRemainingSeconds)
        now = 1_125L
        val second = timer.view()
        assertEquals(expected - 1_125L, second.phaseRemainingMillis)
        assertEquals(25 * minute - 1, second.phaseRemainingSeconds)
    }

    @Test fun pausesAndResumesWithoutCountingPausedTime() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 50_000L
        timer.pause()
        now = 350_000L
        assertEquals(50, timer.update().view.elapsedSeconds)
        val resumed = timer.resume()
        assertEquals(300, resumed.recoveredSeconds)
        assertEquals(180 * minute - 300, resumed.totalSeconds)
        assertEquals((24 * minute) to (1 * minute),
            timer.finalTwoPoseDurations())
        now += 12_000L
        assertEquals(62, timer.update().view.elapsedSeconds)
        timer.stop()
        assertEquals(62, timer.view().elapsedSeconds)
        assertEquals(TimerStatus.STOPPED, timer.view().status)
        now += 120_000L
        val continued = timer.start()
        assertEquals(62, continued.elapsedSeconds)
        assertEquals(420, continued.recoveredSeconds)
        val newSession = timer.restartFromBeginning()
        assertEquals(0, newSession.elapsedSeconds)
        assertEquals(180 * minute, newSession.totalSeconds)
    }

    @Test fun stopAndRestartRetainsOriginalEndByShorteningFinalPair() {
        var now = 10_000L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now += 4L * minute * 1000L
        timer.update()
        val restart = timer.restartKeepingEndTime()
        assertEquals(TimerStatus.RUNNING, restart.status)
        assertEquals(0, restart.elapsedSeconds)
        assertEquals(180 * minute - 4 * minute, restart.totalSeconds)
        assertEquals(4 * minute, restart.recoveredSeconds)
        assertEquals((25 * minute) to (1 * minute), timer.finalTwoPoseDurations())
        now += (180L - 4L) * minute * 1000L
        assertEquals(TimerCue.SESSION_FINISHED, timer.update().cue)
    }

    @Test fun failedStopAndRestartDoesNotCorruptRunningClock() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now += 40L * minute * 1000L
        try {
            timer.restartKeepingEndTime()
            fail("Too much time for two at-least-one-minute final poses")
        } catch (_: IllegalArgumentException) { }
        assertEquals(TimerStatus.RUNNING, timer.view().status)
        assertEquals(40 * minute, timer.view().elapsedSeconds)
        assertEquals(180 * minute, timer.view().totalSeconds)
    }

    @Test fun restartCurrentSessionKeepsEarlierPhasesAndOriginalClassDeadline() {
        var now = 0L
        val session = approved()
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = 40L * minute * 1000L // Session 2 started at virtual 30m.
        val before = timer.update().view
        assertEquals(2, before.phase?.poseNumber)
        val restart = timer.restartCurrentSessionKeepingEndTime()
        assertEquals(TimerStatus.RUNNING, restart.status)
        assertEquals(before.phase?.id, restart.phase?.id)
        assertEquals(30 * minute, restart.elapsedSeconds)
        assertEquals(25 * minute, restart.phaseRemainingSeconds)
        assertEquals(10 * minute, restart.recoveredSeconds)
        assertEquals(170 * minute, restart.totalSeconds)
        assertEquals(session.phases[1].kind, PhaseKind.SHORT_BREAK)
        assertEquals(180 * minute, now.toInt() / 1000 +
            restart.totalRemainingSeconds)
        assertNull(timer.update().cue) // Restart cue emitted by UI only.
    }

    @Test fun pausedRestartCurrentSessionAlsoRecoversPausedWallTime() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 31L * minute * 1000L
        timer.pause()
        now = 33L * minute * 1000L
        val restart = timer.restartCurrentSessionKeepingEndTime()
        assertEquals(TimerStatus.RUNNING, restart.status)
        assertEquals(2, restart.phase?.poseNumber)
        assertEquals(30 * minute, restart.elapsedSeconds)
        assertEquals(3 * minute, restart.recoveredSeconds)
        assertEquals(177 * minute, restart.totalSeconds)
        assertEquals(180 * minute, now.toInt() / 1000 +
            restart.totalRemainingSeconds)
    }

    @Test fun afterNextGainedTimeRestartStillUsesSignedCompensation() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 10L * minute * 1000L
        assertEquals(PhaseKind.SHORT_BREAK, timer.nextPhase().view.phase?.kind)
        now = 18L * minute * 1000L // Five-minute break + 3 minutes into Session 2.
        val restart = timer.restartCurrentSessionKeepingEndTime()
        assertEquals(2, restart.phase?.poseNumber)
        assertEquals(30 * minute, restart.elapsedSeconds)
        assertEquals(-12 * minute, restart.recoveredSeconds)
        assertEquals(192 * minute, restart.totalSeconds)
        assertEquals(180 * minute, now.toInt() / 1000 +
            restart.totalRemainingSeconds)
    }

    @Test fun restartRejectsBreakAndPreservesRunningClockAtomically() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 26L * minute * 1000L
        val before = timer.view()
        assertEquals(PhaseKind.SHORT_BREAK, before.phase?.kind)
        try {
            timer.restartCurrentSessionKeepingEndTime()
            fail("Restart session cannot restart a break.")
        } catch (_: IllegalStateException) { }
        assertEquals(before, timer.view())
    }

    @Test fun firstFinalSessionMayRestartButLastFinalSessionCannotRewritePast() {
        var now = 0L
        val session = approved()
        val pair = session.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = (pair.first().startSeconds + minute).toLong() * 1000L
        val restarting = timer.restartCurrentSessionKeepingEndTime()
        assertEquals(pair.first().id, restarting.phase?.id)
        assertEquals(pair.first().startSeconds, restarting.elapsedSeconds)
        now = (pair.last().startSeconds + minute + 30).toLong() * 1000L
        val before = timer.view()
        assertEquals(pair.last().id, before.phase?.id)
        try {
            timer.restartCurrentSessionKeepingEndTime()
            fail("Last final session cannot change completed partner.")
        } catch (_: IllegalStateException) { }
        assertEquals(before, timer.view())
    }

    @Test fun restartRegularBreakKeepsEarlierWorkAndClassFinish() {
        var now = 0L
        val original = approved()
        val timer = MonotonicSessionTimer(original) { now }
        timer.start()
        now = 26L * minute * 1000L
        val previous = timer.update().view
        assertEquals(PhaseKind.SHORT_BREAK, previous.phase?.kind)
        val restarted = timer.restartCurrentSegmentKeepingEndTime()
        assertEquals(previous.phase?.id, restarted.phase?.id)
        assertEquals(25 * minute, restarted.elapsedSeconds)
        assertEquals(5 * minute, restarted.phaseRemainingSeconds)
        assertEquals(1 * minute, restarted.recoveredSeconds)
        assertEquals(179 * minute, restarted.totalSeconds)
        assertEquals(180 * minute, now.toInt() / 1000 +
            restarted.totalRemainingSeconds)
        assertEquals(original.phases.first().endSeconds, 25 * minute)
        assertNull(timer.update().cue)
    }

    @Test fun restartPausedBreakCountsBothElapsedAndPausedTime() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 26L * minute * 1000L
        timer.pause()
        now = 28L * minute * 1000L
        val restarted = timer.restartCurrentSegmentKeepingEndTime()
        assertEquals(TimerStatus.RUNNING, restarted.status)
        assertEquals(PhaseKind.SHORT_BREAK, restarted.phase?.kind)
        assertEquals(25 * minute, restarted.elapsedSeconds)
        assertEquals(3 * minute, restarted.recoveredSeconds)
        assertEquals(177 * minute, restarted.totalSeconds)
        assertEquals(180 * minute, now.toInt() / 1000 +
            restarted.totalRemainingSeconds)
    }

    @Test fun restartSpecialBreakDoesNotChangeBreakKindOrApprovedPlan() {
        var now = 0L
        val plan = approved()
        val special = plan.phases.first { it.kind == PhaseKind.SPECIAL_BREAK }
        val timer = MonotonicSessionTimer(plan) { now }
        timer.start()
        now = (special.startSeconds.toLong() + 30L) * 1000L
        val restarted = timer.restartCurrentSegmentKeepingEndTime()
        assertEquals(special.id, restarted.phase?.id)
        assertEquals(PhaseKind.SPECIAL_BREAK, restarted.phase?.kind)
        assertEquals(special.startSeconds, restarted.elapsedSeconds)
        assertEquals(special.durationSeconds, restarted.phaseRemainingSeconds)
        assertEquals(30, restarted.recoveredSeconds)
        assertEquals(plan.rules.totalSeconds,
            (now / 1000L + restarted.totalRemainingSeconds).toInt())
        assertEquals(special, plan.phases.first { it.id == special.id })
    }

    @Test fun restartBreakAfterNextCreditsSkippedTimeAgainstRepeat() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 10L * minute * 1000L
        val jumped = timer.nextPhase().view
        assertEquals(PhaseKind.SHORT_BREAK, jumped.phase?.kind)
        assertEquals(-15 * minute, jumped.recoveredSeconds)
        now = 11L * minute * 1000L
        val restarted = timer.restartCurrentSegmentKeepingEndTime()
        assertEquals(25 * minute, restarted.elapsedSeconds)
        assertEquals(-14 * minute, restarted.recoveredSeconds)
        assertEquals(194 * minute, restarted.totalSeconds)
        assertEquals(180 * minute,
            now.toInt() / 1000 + restarted.totalRemainingSeconds)
        assertNull(timer.update().cue)
    }

    @Test fun restartingInterFinalBreakLocksCompletedFirstFinalSession() {
        var now = 0L
        val plan = approved()
        val pair = plan.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        val rest = plan.phases.first { it.startSeconds == pair.first().endSeconds }
        assertTrue(rest.kind == PhaseKind.SHORT_BREAK ||
            rest.kind == PhaseKind.SPECIAL_BREAK)
        val timer = MonotonicSessionTimer(plan) { now }
        timer.start()
        now = (rest.startSeconds.toLong() + minute) * 1000L
        val restarted = timer.restartCurrentSegmentKeepingEndTime()
        assertEquals(rest.id, restarted.phase?.id)
        assertEquals(rest.startSeconds, restarted.elapsedSeconds)
        assertEquals(pair.first().durationSeconds, timer.finalTwoPoseDurations()?.first)
        assertEquals(pair.last().durationSeconds - minute,
            timer.finalTwoPoseDurations()?.second)
        assertEquals(180 * minute,
            now.toInt() / 1000 + restarted.totalRemainingSeconds)
    }

    @Test fun impossibleLateBreakRestartIsAtomicWithoutChangingProgress() {
        var now = 0L
        val plan = approved()
        val pair = plan.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        val rest = plan.phases.first { it.startSeconds == pair.first().endSeconds }
        val timer = MonotonicSessionTimer(plan) { now }
        timer.start()
        val consumed = pair.last().durationSeconds - 30
        assertTrue(consumed in 1 until rest.durationSeconds)
        now = (rest.startSeconds.toLong() + consumed) * 1000L
        val before = timer.view()
        try {
            timer.restartCurrentSegmentKeepingEndTime()
            fail("The last Session must retain its one-minute minimum.")
        } catch (_: IllegalArgumentException) { }
        assertEquals(before, timer.view())
        assertEquals(pair.first().durationSeconds to pair.last().durationSeconds,
            timer.finalTwoPoseDurations())
    }

    @Test fun nextFromPoseStartsBreakAndExtendsFinalPairWithoutChangingFinishClock() {
        var now = 0L
        val original = approved()
        val timer = MonotonicSessionTimer(original) { now }
        timer.start()
        now = 10L * minute * 1000L
        val result = timer.nextPhase()
        assertEquals(TimerCue.POSE_FINISHED, result.cue)
        assertEquals(PhaseKind.SHORT_BREAK, result.view.phase?.kind)
        assertEquals(25 * minute, result.view.elapsedSeconds)
        assertEquals(-15 * minute, result.view.recoveredSeconds)
        assertEquals(195 * minute, result.view.totalSeconds)
        assertEquals((25 * minute) to (20 * minute), timer.finalTwoPoseDurations())
        assertNull(timer.update().cue)
        now += result.view.totalRemainingSeconds.toLong() * 1000L
        assertEquals(TimerCue.SESSION_FINISHED, timer.update().cue)
        assertEquals(original.rules.totalSeconds.toLong() * 1000L, now)
        assertEquals(original.rules.totalSeconds, original.phases.last().endSeconds)
    }

    @Test fun nextFromBreakStartsPoseAndPlaysBreakEndOnlyOnce() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 25L * minute * 1000L
        assertEquals(TimerCue.POSE_FINISHED, timer.update().cue)
        now += 1L * minute * 1000L
        val result = timer.nextPhase()
        assertEquals(TimerCue.BREAK_FINISHED, result.cue)
        assertEquals(PhaseKind.POSE, result.view.phase?.kind)
        assertEquals(-4 * minute, result.view.recoveredSeconds)
        assertNull(timer.update().cue)
    }

    @Test fun nextDuringPauseOffsetsLostTimeAgainstSkippedTime() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 1L * minute * 1000L
        timer.pause()
        now = 3L * minute * 1000L
        val skipped = timer.nextPhase()
        assertEquals(TimerStatus.RUNNING, skipped.view.status)
        assertEquals(-22 * minute, skipped.view.recoveredSeconds)
        assertEquals(PhaseKind.SHORT_BREAK, skipped.view.phase?.kind)
        assertEquals(TimerCue.POSE_FINISHED, skipped.cue)
        now += 1L * minute * 1000L
        timer.pause()
        now += 5L * minute * 1000L
        val resumed = timer.resume()
        assertEquals(-17 * minute, resumed.recoveredSeconds)
        assertEquals(197 * minute, resumed.totalSeconds)
    }

    @Test fun nextAfterLongPauseShrinksFinalPairWhenDowntimeExceedsSkippedPose() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = minute.toLong() * 1000L
        timer.pause()
        now = 30L * minute * 1000L
        val skipped = timer.nextPhase()
        assertEquals(TimerStatus.RUNNING, skipped.view.status)
        assertEquals(TimerCue.POSE_FINISHED, skipped.cue)
        assertEquals(5 * minute, skipped.view.recoveredSeconds)
        assertEquals(175 * minute, skipped.view.totalSeconds)
        assertEquals((24 * minute) to (1 * minute), timer.finalTwoPoseDurations())
    }

    @Test fun nextSupportsConsecutiveOpeningPosesAndLaterOpeningBreak() {
        val rules = SessionRules(
            id = "steps", name = "Steps", totalSeconds = 60 * minute,
            openingGroups = listOf(OpeningPoseGroup("group", 2, minute, 5 * minute)),
            poseSeconds = 10 * minute, shortBreakSeconds = 5 * minute,
            specialBreakCount = 0, cycleStart = CycleStart.BREAK_FIRST,
        )
        // 60m total yields a shortened 8m final regular session, which
        // requires an explicit ending choice before approval.
        val unresolved = engine.generate(rules)
        assertTrue(unresolved.needsFinalPoseChoice)
        val approvedCandidate = engine.generate(rules, FinalPoseChoice.remainder())
        assertTrue(approvedCandidate.canApprove)
        val session = engine.approve(approvedCandidate)
        var now = 0L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now += 15_000L
        val first = timer.nextPhase()
        assertEquals(PhaseKind.POSE, first.view.phase?.kind)
        assertEquals(TimerCue.POSE_FINISHED, first.cue)
        now += 20_000L
        val second = timer.nextPhase()
        assertEquals(PhaseKind.SHORT_BREAK, second.view.phase?.kind)
        assertEquals(TimerCue.POSE_FINISHED, second.cue)
    }

    @Test fun nextFromEndSummaryFinishesWithMelodyCExactlyOnce() {
        val session = approved()
        var now = 0L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = (session.rules.totalSeconds - session.rules.summarySeconds).toLong() * 1000L
        timer.update()
        val finished = timer.nextPhase()
        assertEquals(TimerCue.SESSION_FINISHED, finished.cue)
        assertEquals(TimerStatus.FINISHED, finished.view.status)
        assertEquals(0, finished.view.totalRemainingSeconds)
        assertNull(timer.update().cue)
    }

    @Test fun nextFromFirstFinalSessionAdvancesWithoutRewritingObservedSession() {
        var now = 0L
        val session = approved()
        val pair = session.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        val first = pair.first()
        val rest = session.phases.first { it.startSeconds == first.endSeconds }
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = (first.startSeconds + 5).toLong() * 1000L
        val next = timer.nextPhase()
        assertEquals(TimerStatus.RUNNING, next.view.status)
        assertEquals(rest.id, next.view.phase?.id)
        assertEquals(TimerCue.POSE_FINISHED, next.cue)
        assertEquals(first.durationSeconds, timer.finalTwoPoseDurations()?.first)
        assertEquals(pair.last().durationSeconds + first.durationSeconds - 5,
            timer.finalTwoPoseDurations()?.second)
        assertEquals(session.rules.totalSeconds, now.toInt() / 1000 +
            next.view.totalRemainingSeconds)
        assertEquals(first, session.phases.first { it.id == first.id })
        assertNull(timer.update().cue)
    }

    @Test fun nextFromFinalPairBreakKeepsFirstAndExtendsLastSession() {
        var now = 0L
        val session = approved()
        val pair = session.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        val rest = session.phases.first { it.startSeconds == pair.first().endSeconds }
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = (rest.startSeconds + 10).toLong() * 1000L
        val next = timer.nextPhase()
        assertEquals(pair.last().id, next.view.phase?.id)
        assertEquals(TimerCue.BREAK_FINISHED, next.cue)
        assertEquals(pair.first().durationSeconds, timer.finalTwoPoseDurations()?.first)
        assertEquals(pair.last().durationSeconds + rest.durationSeconds - 10,
            timer.finalTwoPoseDurations()?.second)
        assertEquals(session.rules.totalSeconds, now.toInt() / 1000 +
            next.view.totalRemainingSeconds)
    }

    @Test fun nextOnLastWorkSessionFinishesClassAndEmitsCOnlyOnce() {
        var now = 0L
        val session = approved()
        val finalWork = session.phases.last {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }
        assertTrue(session.rules.summarySeconds > 0)
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = (finalWork.startSeconds + 5).toLong() * 1000L
        val next = timer.nextPhase()
        assertEquals(TimerStatus.FINISHED, next.view.status)
        assertEquals(TimerCue.SESSION_FINISHED, next.cue)
        assertNull(next.view.phase)
        assertNull(timer.update().cue)
        assertEquals(session.rules.totalSeconds, session.phases.last().endSeconds)
    }

    @Test fun nextAfterImpossiblePauseStillAdvancesWithoutMutatingSavedClass() {
        var now = 0L
        val session = approved()
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = 60_000L
        timer.pause()
        now = 61L * minute * 1000L
        val next = timer.nextPhase()
        assertEquals(TimerStatus.RUNNING, next.view.status)
        assertEquals(PhaseKind.SHORT_BREAK, next.view.phase?.kind)
        assertEquals(TimerCue.POSE_FINISHED, next.cue)
        assertEquals(session.rules.totalSeconds, next.view.totalSeconds)
        assertEquals(0, next.view.recoveredSeconds)
        assertEquals(session.rules.totalSeconds, session.phases.last().endSeconds)
        assertNull(timer.update().cue)
    }

    @Test fun emitsDifferentSingleCuesOnPoseAndBreakEdges() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 25L * minute * 1000L
        assertEquals(TimerCue.POSE_FINISHED, timer.update().cue)
        assertNull(timer.update().cue)
        now = 30L * minute * 1000L
        assertEquals(TimerCue.BREAK_FINISHED, timer.update().cue)
        assertNull(timer.update().cue)
    }

    @Test fun fixedClassDeadlineCompletesOnceEvenIfVirtualTimerWasPaused() {
        var now = 0L
        val saved = approved()
        val timer = MonotonicSessionTimer(saved) { now }
        timer.start()
        now = 2_500L
        timer.pause()
        now = saved.rules.totalSeconds.toLong() * 1_000L
        val finished = timer.finishAtDeadline()
        assertEquals(TimerStatus.FINISHED, finished.view.status)
        assertEquals(TimerCue.SESSION_FINISHED, finished.cue)
        assertNull(timer.finishAtDeadline().cue)
        assertNull(timer.update().cue)
        assertEquals(saved.rules.totalSeconds, saved.phases.last().endSeconds)
    }

    @Test fun skipsStaleTransitionsButFinishesOnce() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 121L * minute * 1000L
        assertNull(timer.update().cue)
        now = 180L * minute * 1000L
        val last = timer.update()
        assertEquals(TimerCue.SESSION_FINISHED, last.cue)
        assertEquals(TimerStatus.FINISHED, last.view.status)
        assertEquals(0, last.view.totalRemainingSeconds)
        assertNull(timer.update().cue)
        timer.start()
        assertEquals(TimerStatus.RUNNING, timer.view().status)
    }

    @Test fun consecutiveOpeningPoseTransitionsPlayPoseMelodyOnce() {
        val rules = SessionRules(
            id = "opening", name = "Opening", totalSeconds = 5 * minute,
            openingGroups = listOf(OpeningPoseGroup("o", 2, minute)),
            poseSeconds = minute, shortBreakSeconds = minute,
            specialBreakCount = 0,
        )
        val session = engine.approve(engine.generate(rules))
        var now = 0L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = minute.toLong() * 1000L
        assertEquals(TimerCue.POSE_FINISHED, timer.update().cue)
        assertNull(timer.update().cue)
    }

    @Test fun slightlyDelayedUiLoopStillEmitsCorrectAutomaticCue() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 25L * minute * 1000L + 2_200L
        assertEquals(TimerCue.POSE_FINISHED, timer.update().cue)
        assertNull(timer.update().cue)
        now = 30L * minute * 1000L + 1_000L
        assertEquals(TimerCue.BREAK_FINISHED, timer.update().cue)
    }

    @Test fun interruptionCannotSilentlyRewriteFinalPairAlreadyInProgress() {
        var now = 0L
        val evenRules = SessionRules(
            id = "even", name = "Even", totalSeconds = 180 * minute,
            summarySeconds = 15 * minute, poseSeconds = 25 * minute,
            specialBreakCount = 1,
        )
        val even = engine.approve(
            engine.generate(evenRules, FinalPoseChoice.even())
        )
        val timer = MonotonicSessionTimer(even) { now }
        timer.start()
        now = 140L * minute * 1000L
        timer.update()
        timer.pause()
        now += minute * 1000L
        try {
            timer.resume()
            fail("Must refuse to change an already-started final pose.")
        } catch (_: IllegalStateException) { }
        assertEquals(TimerStatus.PAUSED, timer.view().status)
    }

    @Test fun interruptionBeyondFinalPairCapacityRequiresNewApprovedPlan() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        timer.pause()
        now = 29L * minute * 1000L
        try {
            timer.resume()
            fail("Need at least one minute for each of two final poses.")
        } catch (_: IllegalArgumentException) { }
        assertEquals(TimerStatus.PAUSED, timer.view().status)
    }

    @Test fun defaultMelodiesAreDistinctAndFiveSecondsLong() {
        val choices = MelodySelections()
        assertEquals(6, MelodyPattern.entries.size)
        assertEquals(20, choices.poseEnd.notesHz.size)
        assertEquals(20, choices.breakEnd.notesHz.size)
        assertEquals(20, choices.sessionEnd.notesHz.size)
        assertNotEquals(choices.poseEnd.notesHz, choices.breakEnd.notesHz)
        assertNotEquals(choices.sessionEnd.notesHz, choices.breakEnd.notesHz)
        assertEquals(MelodyPattern.SOFT_CHIMES,
            choices.forCue(TimerCue.POSE_FINISHED))
        assertEquals(MelodyPattern.RISING_STEPS,
            choices.forCue(TimerCue.BREAK_FINISHED))
        assertEquals(MelodyPattern.CELEBRATION,
            choices.forCue(TimerCue.SESSION_FINISHED))
    }
}
