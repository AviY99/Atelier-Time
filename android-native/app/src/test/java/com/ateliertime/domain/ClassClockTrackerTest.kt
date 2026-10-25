package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** Tests real clock labels independently from the Android Compose UI. */
class ClassClockTrackerTest {
    private val one = SchedulePhase("s1", PhaseKind.POSE, 0, 60, poseNumber = 1)
    private val two = SchedulePhase("b1", PhaseKind.SHORT_BREAK, 60, 120)
    private val summary = SchedulePhase("sum", PhaseKind.SUMMARY, 120, 180)
    private fun view(
        phase: SchedulePhase? = one,
        elapsed: Int = 0,
        status: TimerStatus = TimerStatus.RUNNING,
        remaining: Int = (phase?.endSeconds ?: 180) - elapsed,
    ) = TimerView(
        status = status,
        elapsedSeconds = elapsed,
        totalSeconds = 180,
        phase = phase,
        nextPhase = null,
        phaseRemainingSeconds = remaining,
        totalRemainingSeconds = 180 - elapsed,
    )

    @Test fun beforeStartAllTimesAreEmpty() {
        val tracker = ClassClockTracker()
        val result = tracker.observe(view(status = TimerStatus.READY), 2_000_000L)
        assertNull(result.classStartEpochMs)
        assertNull(result.classEndEpochMs)
        assertNull(result.currentPhaseStartEpochMs)
    }

    @Test fun startShowsCurrentClassAndSessionBoundaryClocks() {
        val tracker = ClassClockTracker()
        val start = 1_000_000L
        val result = tracker.newClass(view(), start)
        assertEquals(start, result.classStartEpochMs)
        assertEquals(start + 180_000L, result.classEndEpochMs)
        assertEquals(start, result.currentPhaseStartEpochMs)
        assertEquals(start + 60_000L, result.currentPhaseEndEpochMs)
        assertEquals("Session 1", result.currentPhaseLabel)
        val later = tracker.observe(view(elapsed = 20, remaining = 40), start + 20_000L)
        assertEquals(start, later.currentPhaseStartEpochMs)
        assertEquals(start + 60_000L, later.currentPhaseEndEpochMs)
    }

    @Test fun nextStartsBreakImmediatelyAndDoesNotShiftClassFinish() {
        val tracker = ClassClockTracker()
        val start = 1_000_000L
        tracker.newClass(view(), start)
        val jumped = tracker.observe(view(phase = two, elapsed = 60, remaining = 60),
            start + 15_000L)
        assertEquals(start + 15_000L, jumped.currentPhaseStartEpochMs)
        assertEquals(start + 75_000L, jumped.currentPhaseEndEpochMs)
        assertEquals(start + 180_000L, jumped.classEndEpochMs)
        assertEquals("Break · regular", jumped.currentPhaseLabel)
    }

    @Test fun pauseHidesUncertainSegmentEndAndResumeRecomputesIt() {
        val tracker = ClassClockTracker()
        val start = 10_000L
        tracker.newClass(view(), start)
        val paused = tracker.observe(view(elapsed = 12, status = TimerStatus.PAUSED,
            remaining = 48), start + 12_000L)
        assertEquals(start, paused.currentPhaseStartEpochMs)
        assertNull(paused.currentPhaseEndEpochMs)
        assertTrue(paused.paused)
        val resumed = tracker.observe(view(elapsed = 12, remaining = 48),
            start + 90_000L)
        assertEquals(start, resumed.currentPhaseStartEpochMs)
        assertEquals(start + 138_000L, resumed.currentPhaseEndEpochMs)
        assertEquals(start + 180_000L, resumed.classEndEpochMs)
    }

    @Test fun restartUpdatesClassStartButRetainsTargetEnd() {
        val tracker = ClassClockTracker()
        val start = 1_000_000L
        tracker.newClass(view(), start)
        val restarted = tracker.restartClass(view(), start + 10_000L)
        assertEquals(start + 10_000L, restarted.classStartEpochMs)
        assertEquals(start + 180_000L, restarted.classEndEpochMs)
        assertEquals(start + 10_000L, restarted.currentPhaseStartEpochMs)
    }

    @Test fun summaryIsNotMislabeledAsSessionAndFinishIsRecordedOnce() {
        val tracker = ClassClockTracker()
        val start = 1_000_000L
        tracker.newClass(view(), start)
        val running = tracker.observe(view(phase = summary, elapsed = 125, remaining = 55),
            start + 125_000L)
        assertEquals("Class summary", running.currentPhaseLabel)
        val done = tracker.observe(view(phase = null, elapsed = 180,
            status = TimerStatus.FINISHED, remaining = 0), start + 150_000L)
        assertEquals(start + 180_000L, done.classEndEpochMs)
        assertNull(done.currentPhaseLabel)
        assertEquals(start + 180_000L, tracker.observe(
            view(phase = null, elapsed = 180, status = TimerStatus.FINISHED,
                remaining = 0), start + 170_000L).classEndEpochMs)
    }

    @Test fun realTimerNextAndRestartUsePhysicalTimeNotSkippedVirtualOffsets() {
        val engine = ScheduleEngine()
        val rules = SessionRules(
            id = "clock", name = "Class", totalSeconds = 180 * 60,
            summarySeconds = 15 * 60, poseSeconds = 25 * 60,
            specialBreakCount = 1,
        )
        val approved = engine.approve(
            engine.generate(rules, FinalPoseChoice.remainder())
        )
        var monotonicMs = 0L
        val actualStart = 1_700_000_000_000L
        val timer = MonotonicSessionTimer(approved) { monotonicMs }
        val tracker = ClassClockTracker()
        tracker.newClass(timer.start(), actualStart)

        monotonicMs = 10L * 60_000L
        val jumped = timer.nextPhase().view
        val jumpedClock = tracker.observe(jumped, actualStart + monotonicMs)
        assertEquals("Break · regular", jumpedClock.currentPhaseLabel)
        assertEquals(actualStart + 10L * 60_000L,
            jumpedClock.currentPhaseStartEpochMs)
        assertEquals(actualStart + 15L * 60_000L,
            jumpedClock.currentPhaseEndEpochMs)
        assertEquals(actualStart + 180L * 60_000L,
            jumpedClock.classEndEpochMs)

        monotonicMs = 12L * 60_000L
        val restarted = timer.restartKeepingEndTime()
        val restartedClock = tracker.restartClass(restarted, actualStart + monotonicMs)
        assertEquals(actualStart + monotonicMs, restartedClock.classStartEpochMs)
        assertEquals(actualStart + 180L * 60_000L,
            restartedClock.classEndEpochMs)
        assertEquals("Session 1", restartedClock.currentPhaseLabel)
    }

    @Test fun restartingCurrentSessionResetsOnlyItsClockNotClassBoundary() {
        val engine = ScheduleEngine()
        val rules = SessionRules(
            id = "clock-restart", name = "Class", totalSeconds = 180 * 60,
            summarySeconds = 15 * 60, poseSeconds = 25 * 60,
            specialBreakCount = 1,
        )
        val plan = engine.approve(engine.generate(rules, FinalPoseChoice.remainder()))
        val clockStart = 1_700_000_000_000L
        var monotonic = 0L
        val timer = MonotonicSessionTimer(plan) { monotonic }
        val tracker = ClassClockTracker()
        tracker.newClass(timer.start(), clockStart)

        monotonic = 40L * 60_000L
        val running = timer.update().view
        val before = tracker.observe(running, clockStart + monotonic)
        assertEquals("Session 2", before.currentPhaseLabel)
        assertEquals(clockStart + 30L * 60_000L,
            before.currentPhaseStartEpochMs)
        val view = timer.restartCurrentSessionKeepingEndTime()
        val restarted = tracker.restartCurrentSession(view, clockStart + monotonic)
        assertEquals(clockStart, restarted.classStartEpochMs)
        assertEquals(clockStart + 180L * 60_000L, restarted.classEndEpochMs)
        assertEquals(clockStart + 40L * 60_000L,
            restarted.currentPhaseStartEpochMs)
        assertEquals("Session 2", restarted.currentPhaseLabel)
        assertEquals(clockStart + 65L * 60_000L,
            restarted.currentPhaseEndEpochMs)
    }

    @Test fun restartingRegularBreakResetsOnlyBreakClocksAndKeepsClassClocks() {
        val engine = ScheduleEngine()
        val rules = SessionRules(
            id = "clock-break-restart", name = "Class", totalSeconds = 180 * 60,
            summarySeconds = 15 * 60, poseSeconds = 25 * 60,
            specialBreakCount = 1,
        )
        val plan = engine.approve(engine.generate(rules, FinalPoseChoice.remainder()))
        val start = 1_700_000_000_000L
        var ticks = 0L
        val timer = MonotonicSessionTimer(plan) { ticks }
        val tracker = ClassClockTracker()
        tracker.newClass(timer.start(), start)
        ticks = 26L * 60_000L
        val running = timer.update().view
        val before = tracker.observe(running, start + ticks)
        assertEquals("Break · regular", before.currentPhaseLabel)
        assertEquals(start + 25L * 60_000L, before.currentPhaseStartEpochMs)
        val repeated = timer.restartCurrentSegmentKeepingEndTime()
        val afterward = tracker.restartCurrentSegment(repeated, start + ticks)
        assertEquals("Break · regular", afterward.currentPhaseLabel)
        assertEquals(start + ticks, afterward.currentPhaseStartEpochMs)
        assertEquals(start + 31L * 60_000L, afterward.currentPhaseEndEpochMs)
        assertEquals(start, afterward.classStartEpochMs)
        assertEquals(start + 180L * 60_000L, afterward.classEndEpochMs)
    }

    @Test fun restartingPausedSpecialBreakRestoresRunningClock() {
        val tracker = ClassClockTracker()
        val special = two.copy(kind = PhaseKind.SPECIAL_BREAK)
        val start = 8_000_000L
        val initial = tracker.newClass(view(), start)
        assertEquals(start, initial.classStartEpochMs)
        val paused = tracker.observe(
            view(phase = special, elapsed = 66,
                status = TimerStatus.PAUSED, remaining = 54), start + 66_000L,
        )
        assertEquals("Break · special", paused.currentPhaseLabel)
        assertNull(paused.currentPhaseEndEpochMs)
        val repeated = tracker.restartCurrentSegment(
            view(phase = special, elapsed = 60, remaining = 60), start + 80_000L,
        )
        assertEquals(start + 80_000L, repeated.currentPhaseStartEpochMs)
        assertEquals(start + 140_000L, repeated.currentPhaseEndEpochMs)
        assertEquals(start + 180_000L, repeated.classEndEpochMs)
    }

    @Test fun nextWithoutCompensationNeverChangesOriginalClassEndClock() {
        val tracker = ClassClockTracker()
        val start = 1_700_000_000_000L
        tracker.newClass(view(), start)
        // A forced Next during an unabsorbable pause advances the virtual
        // phase but leaves the remaining phase lengths unchanged.
        val next = tracker.afterNext(
            view(phase = two, elapsed = 60, remaining = 60),
            start + 50_000L,
        )
        assertEquals(start, next.classStartEpochMs)
        assertEquals(start + 180_000L, next.classEndEpochMs)
        assertEquals(start + 50_000L, next.currentPhaseStartEpochMs)
        assertEquals(start + 110_000L, next.currentPhaseEndEpochMs)
    }

    @Test fun compensatedNextPreservesOriginalEndClockWhenWithinTickTolerance() {
        val tracker = ClassClockTracker()
        val start = 1_700_000_000_000L
        tracker.newClass(view(), start)
        val next = tracker.afterNext(
            view(phase = two, elapsed = 60, remaining = 60),
            start + 60_500L,
        )
        assertEquals(start + 180_000L, next.classEndEpochMs)
    }

    @Test fun realElapsedContinuesDuringPauseAndDoesNotJumpOnNext() {
        val tracker = ClassClockTracker()
        val start = 1_000_000L
        tracker.newClass(view(), start)
        val paused = tracker.observe(
            view(elapsed = 15, remaining = 45, status = TimerStatus.PAUSED),
            start + 15_000L,
        )
        assertEquals(15, paused.classElapsedSeconds)
        assertEquals(165, paused.classRemainingSeconds)
        // The Session countdown stays paused while real Class elapsed continues.
        val stillPaused = tracker.observe(
            view(elapsed = 15, remaining = 45, status = TimerStatus.PAUSED),
            start + 45_000L,
        )
        assertEquals(45, stillPaused.classElapsedSeconds)
        assertEquals(135, stillPaused.classRemainingSeconds)
        val skipped = tracker.afterNext(
            view(phase = two, elapsed = 60, remaining = 60),
            start + 50_000L,
        )
        assertEquals(50, skipped.classElapsedSeconds)
        assertEquals(130, skipped.classRemainingSeconds)
        assertEquals(start + 180_000L, skipped.classEndEpochMs)
        assertEquals(start + 50_000L, skipped.currentPhaseStartEpochMs)
    }

    @Test fun millisecondPreciseRemainingPreventsEndClockOscillation() {
        val tracker = ClassClockTracker()
        val start = 1_000_000L
        tracker.newClass(view(), start)
        // Updates between second boundaries refer to the SAME real End.
        val a = tracker.observe(
            view(elapsed = 0, remaining = 60)
                .copy(phaseRemainingMillis = 59_775L),
            start + 225L,
        )
        val b = tracker.observe(
            view(elapsed = 0, remaining = 60)
                .copy(phaseRemainingMillis = 59_075L),
            start + 925L,
        )
        val d = tracker.observe(
            view(elapsed = 1, remaining = 59)
                .copy(phaseRemainingMillis = 58_825L),
            start + 1_175L,
        )
        assertEquals(start + 60_000L, a.currentPhaseEndEpochMs)
        assertEquals(a.currentPhaseEndEpochMs, b.currentPhaseEndEpochMs)
        assertEquals(b.currentPhaseEndEpochMs, d.currentPhaseEndEpochMs)
    }

    @Test fun phaseLabelsDifferentiateKindsOfBreak() {
        assertEquals("Break · opening group", clockPhaseLabel(
            two.copy(openingGroupId = "g")))
        assertEquals("Break · regular", clockPhaseLabel(two))
        assertEquals("Break · special", clockPhaseLabel(
            two.copy(kind = PhaseKind.SPECIAL_BREAK)))
        assertEquals("Session 2", clockPhaseLabel(one.copy(poseNumber = 2)))
    }

    @Test fun monotonicDeadlineSurvivesWallClockForwardAndBackwardCorrections() {
        var physicalNow = 50_000L
        val startEpoch = 1_800_000_000_000L
        val tracker = ClassClockTracker { physicalNow }
        val started = tracker.newClass(view(), startEpoch)
        assertEquals(startEpoch + 180_000L, started.classEndEpochMs)
        physicalNow += 30_000L
        val forward = tracker.observe(view(elapsed = 30, remaining = 30),
            startEpoch + 150_000L) // Wall clock suddenly advanced +120 seconds.
        assertEquals(30, forward.classElapsedSeconds)
        assertEquals(150, forward.classRemainingSeconds)
        assertFalse(tracker.hasReachedClassDeadline(startEpoch + 150_000L))
        val backward = tracker.observe(view(elapsed = 30, remaining = 30),
            startEpoch - 90_000L) // Wall clock suddenly moved back -120 seconds.
        assertEquals(30, backward.classElapsedSeconds)
        assertEquals(150, backward.classRemainingSeconds)
        assertFalse(tracker.hasReachedClassDeadline(startEpoch - 90_000L))
        physicalNow += 149_999L
        assertFalse(tracker.hasReachedClassDeadline(startEpoch - 90_000L))
        physicalNow += 1L
        assertTrue(tracker.hasReachedClassDeadline(startEpoch - 90_000L))
        val complete = tracker.observe(view(phase = null, elapsed = 180,
            status = TimerStatus.FINISHED, remaining = 0), startEpoch - 90_000L)
        assertEquals(180, complete.classElapsedSeconds)
        assertEquals(0, complete.classRemainingSeconds)
    }

    @Test fun monotonicClassClockAdvancesDuringPauseButNotByVirtualNext() {
        var physicalNow = 5_000L
        val epoch = 1_800_000_000_000L
        val tracker = ClassClockTracker { physicalNow }
        tracker.newClass(view(), epoch)
        physicalNow += 15_000L
        val paused = tracker.observe(view(elapsed = 15,
            status = TimerStatus.PAUSED, remaining = 45), epoch + 15_000L)
        assertEquals(15, paused.classElapsedSeconds)
        physicalNow += 30_000L
        val waiting = tracker.observe(view(elapsed = 15,
            status = TimerStatus.PAUSED, remaining = 45), epoch + 165_000L)
        assertEquals(45, waiting.classElapsedSeconds)
        assertEquals(135, waiting.classRemainingSeconds)
        val skipped = tracker.afterNext(
            view(phase = two, elapsed = 60, remaining = 60), epoch + 165_000L)
        assertEquals(45, skipped.classElapsedSeconds)
        assertEquals(135, skipped.classRemainingSeconds)
        assertEquals(epoch + 180_000L, skipped.classEndEpochMs)
    }

    @Test fun monotonicRestartPreservesOriginalPhysicalDeadline() {
        var physicalNow = 90_000L
        val epoch = 1_800_000_000_000L
        val tracker = ClassClockTracker { physicalNow }
        tracker.newClass(view(), epoch)
        physicalNow += 10_000L
        val restarted = tracker.restartClass(view(), epoch + 130_000L)
        assertEquals(epoch + 130_000L, restarted.classStartEpochMs)
        assertEquals(epoch + 180_000L, restarted.classEndEpochMs)
        assertEquals(0, restarted.classElapsedSeconds)
        assertEquals(170, restarted.classRemainingSeconds)
        physicalNow += 170_000L
        assertTrue(tracker.hasReachedClassDeadline(epoch - 120_000L))
    }
}
