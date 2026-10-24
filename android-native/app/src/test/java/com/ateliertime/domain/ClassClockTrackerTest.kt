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
        assertEquals(start + 150_000L, done.classEndEpochMs)
        assertNull(done.currentPhaseLabel)
        assertEquals(start + 150_000L, tracker.observe(
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

    @Test fun phaseLabelsDifferentiateKindsOfBreak() {
        assertEquals("Break · opening group", clockPhaseLabel(
            two.copy(openingGroupId = "g")))
        assertEquals("Break · regular", clockPhaseLabel(two))
        assertEquals("Break · special", clockPhaseLabel(
            two.copy(kind = PhaseKind.SPECIAL_BREAK)))
        assertEquals("Session 2", clockPhaseLabel(one.copy(poseNumber = 2)))
    }
}
