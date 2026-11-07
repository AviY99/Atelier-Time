package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/**
 * D-104 / Timer reliability gate: use an injected monotonic clock, never
 * Thread.sleep(), to exercise exact cue boundaries and long-run deadlines.
 * These JVM tests complement the existing interruption/compensation suite;
 * they do NOT prove Android background/locked-screen audio reliability.
 */
class TimerReliabilityGateTest {
    private val engine = ScheduleEngine()

    /** 00:00–01:00 Session; 01:00–02:00 Break; 02:00–03:00 Session. */
    private fun shortClass(): ApprovedSession {
        val rules = SessionRules(
            id = "reliability-short",
            name = "Reliability A/B/C",
            totalSeconds = 3 * SECONDS_PER_MINUTE,
            summarySeconds = 0,
            poseSeconds = SECONDS_PER_MINUTE,
            shortBreakSeconds = SECONDS_PER_MINUTE,
            specialBreakCount = 0,
            cycleStart = CycleStart.POSE_FIRST,
        )
        val candidate = engine.generate(rules)
        assertTrue(candidate.issues.toString(), candidate.canApprove)
        return engine.approve(candidate)
    }

    private fun longClass(): ApprovedSession {
        val rules = SessionRules(
            id = "reliability-long", name = "Three hour class",
            totalSeconds = 180 * SECONDS_PER_MINUTE,
            summarySeconds = 15 * SECONDS_PER_MINUTE,
            poseSeconds = 25 * SECONDS_PER_MINUTE,
            specialBreakCount = 1,
        )
        return engine.approve(engine.generate(rules, FinalPoseChoice.remainder()))
    }

    @Test fun threeMinuteClassGivesOnlyABThenCAtExactBoundariesWithTwoHundredMsTicks() {
        val session = shortClass()
        assertEquals(
            listOf(PhaseKind.POSE, PhaseKind.SHORT_BREAK, PhaseKind.POSE),
            session.phases.map { it.kind },
        )
        val savedSchedule = session.phases.toList()
        var monotonicMs = 0L
        val timer = MonotonicSessionTimer(session) { monotonicMs }
        timer.start()
        val observed = mutableListOf<Pair<Int, TimerCue>>()
        for (tick in 1..900) {
            monotonicMs = tick * 200L
            val event = timer.update()
            event.cue?.let { observed += (monotonicMs / 1_000L).toInt() to it }
            assertTrue("Remaining seconds must stay nonnegative",
                event.view.totalRemainingSeconds >= 0)
        }
        assertEquals(
            listOf(
                60 to TimerCue.POSE_FINISHED,
                120 to TimerCue.BREAK_FINISHED,
                180 to TimerCue.SESSION_FINISHED,
            ), observed,
        )
        assertEquals(TimerStatus.FINISHED, timer.view().status)
        assertEquals(0, timer.view().totalRemainingSeconds)
        assertNull(timer.update().cue)
        assertEquals(savedSchedule, session.phases)
    }

    @Test fun threeHourMonotonicClassFinishesAtOriginalDeadlineWithoutClockDrift() {
        val session = longClass()
        val savedSchedule = session.phases.toList()
        var monotonicMs = 21_000_000L
        val startMs = monotonicMs
        val startEpoch = 1_800_000_000_000L
        val timer = MonotonicSessionTimer(session) { monotonicMs }
        val tracker = ClassClockTracker()
        val begin = timer.start()
        val initial = tracker.newClass(begin, startEpoch)
        val finishEpoch = startEpoch + 180L * 60_000L
        assertEquals(finishEpoch, initial.classEndEpochMs)
        assertEquals(180 * 60, initial.classRemainingSeconds)

        // Jump the injected monotonic clock to several checkpoints, including
        // just before the deadline; the Class wall-time target cannot drift.
        for (second in listOf(1, 25 * 60, 60 * 60, 120 * 60, 179 * 60, 180 * 60 - 1)) {
            monotonicMs = startMs + second.toLong() * 1_000L
            val tick = timer.update()
            val clock = tracker.observe(tick.view, startEpoch +
                (monotonicMs - startMs))
            assertEquals(finishEpoch, clock.classEndEpochMs)
            assertEquals(180 * 60 - second, clock.classRemainingSeconds)
        }
        monotonicMs = startMs + 180L * 60_000L
        val done = timer.update()
        assertEquals(TimerCue.SESSION_FINISHED, done.cue)
        assertEquals(TimerStatus.FINISHED, done.view.status)
        val end = tracker.observe(done.view, finishEpoch)
        assertEquals(finishEpoch, end.classEndEpochMs)
        assertEquals(0, end.classRemainingSeconds)
        assertNull(timer.update().cue)
        assertEquals(savedSchedule, session.phases)
    }

    @Test fun delayedUiLoopDoesNotReplayMissedCuesButClassEndStillSoundsOnce() {
        val saved = shortClass()
        var now = 0L
        val timer = MonotonicSessionTimer(saved) { now }
        timer.start()
        now = 2L * 60_000L + 7_000L
        assertNull("Skipping over multiple boundaries must not replay stale sounds",
            timer.update().cue)
        assertNull(timer.update().cue)
        assertEquals(PhaseKind.POSE, timer.view().phase?.kind)
        now = 180_000L
        assertEquals(TimerCue.SESSION_FINISHED, timer.update().cue)
        assertNull(timer.update().cue)
        assertEquals(180, saved.phases.last().endSeconds)
    }

    @Test fun finishingAtDeadlineWhilePausedEmitsClassEndOnlyOnce() {
        val session = shortClass()
        var now = 0L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = 17_000L
        timer.pause()
        now = 180_000L
        assertNull(timer.update().cue)
        val finished = timer.finishAtDeadline()
        assertEquals(TimerStatus.FINISHED, finished.view.status)
        assertEquals(TimerCue.SESSION_FINISHED, finished.cue)
        assertNull(timer.finishAtDeadline().cue)
        assertNull(timer.update().cue)
        assertEquals(180, session.phases.last().endSeconds)
    }
}
