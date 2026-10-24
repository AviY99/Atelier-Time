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

    @Test fun phaseLabelsDifferentiateKindsOfBreak() {
        assertEquals("Break · opening group", clockPhaseLabel(
            two.copy(openingGroupId = "g")))
        assertEquals("Break · regular", clockPhaseLabel(two))
        assertEquals("Break · special", clockPhaseLabel(
            two.copy(kind = PhaseKind.SPECIAL_BREAK)))
        assertEquals("Session 2", clockPhaseLabel(one.copy(poseNumber = 2)))
    }
}
