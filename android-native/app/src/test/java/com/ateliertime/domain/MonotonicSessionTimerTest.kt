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

    @Test fun nextSupportsConsecutiveOpeningPosesAndLaterOpeningBreak() {
        val rules = SessionRules(
            id = "steps", name = "Steps", totalSeconds = 60 * minute,
            openingGroups = listOf(OpeningPoseGroup("group", 2, minute, 5 * minute)),
            poseSeconds = 10 * minute, shortBreakSeconds = 5 * minute,
            specialBreakCount = 0, cycleStart = CycleStart.BREAK_FIRST,
        )
        val session = engine.approve(engine.generate(rules))
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

    @Test fun nextCannotRewriteAlreadyStartedFinalPairAndLeavesTimerIntact() {
        var now = 0L
        val session = approved()
        val firstFinal = session.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2).first()
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = (firstFinal.startSeconds + 5).toLong() * 1000L
        timer.update()
        try {
            timer.nextPhase()
            fail("Must preserve already started final pose, not silently rewrite it.")
        } catch (_: IllegalStateException) { }
        assertEquals(firstFinal.id, timer.view().phase?.id)
        assertEquals(0, timer.view().recoveredSeconds)
        assertEquals(TimerStatus.RUNNING, timer.view().status)
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
