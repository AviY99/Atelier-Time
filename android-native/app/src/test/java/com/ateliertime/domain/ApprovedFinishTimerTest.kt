package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class ApprovedFinishTimerTest {
    private val engine = ScheduleEngine()
    private fun m(n: Int) = 60 * n
    private fun session(choice: FinalPoseChoice = FinalPoseChoice.remainder()) =
        engine.approve(engine.generate(SessionRules(
            id = "approved-finish", name = "Approved finish test",
            totalSeconds = m(180), summarySeconds = m(15),
            poseSeconds = m(25), specialBreakCount = 1,
        ), choice))

    @Test fun startWithEarlierEndUsesPrevalidatedPhaseLengths() {
        val session = session()
        var elapsedMs = 70_000L
        val timer = MonotonicSessionTimer(session) { elapsedMs }
        val plan = RunEndTimePlanner.plan(session, 0L, m(177).toLong() * 1_000L)
        assertEquals(TimerStatus.READY, timer.view().status)
        val view = timer.start(plan.correctionSeconds)
        assertEquals(TimerStatus.RUNNING, view.status)
        assertEquals(m(177), view.totalSeconds)
        assertEquals(m(3), view.recoveredSeconds)
        assertEquals(session.phases.first().id, view.phase?.id)
        assertEquals(m(177), timer.finalTwoPoseDurations()?.let {
            // The pair sum is 27 minutes; original full class is unchanged.
            it.first + it.second + m(150)
        })
        assertEquals(m(180), session.rules.totalSeconds)
    }

    @Test fun startWithLaterEndExtendsLastPairAndFinishesOnSelectedClock() {
        val session = session()
        var monotonic = 0L
        val timer = MonotonicSessionTimer(session) { monotonic }
        val started = timer.start(-m(6))
        assertEquals(m(186), started.totalSeconds)
        assertEquals(-m(6), started.recoveredSeconds)
        monotonic = m(185).toLong() * 1_000L
        assertEquals(TimerStatus.RUNNING, timer.update().view.status)
        monotonic = m(186).toLong() * 1_000L
        val ended = timer.update()
        assertEquals(TimerStatus.FINISHED, ended.view.status)
        assertEquals(TimerCue.SESSION_FINISHED, ended.cue)
        assertEquals(m(180), session.rules.totalSeconds)
    }

    @Test fun classClockDeadlineMatchesApprovedDurationAndIgnoresWallClockChange() {
        val session = session()
        var monotonic = 20_000L
        val timer = MonotonicSessionTimer(session) { monotonic }
        val start = timer.start(m(3))
        val tracker = ClassClockTracker { monotonic }
        val epoch = 5_000_000L
        val initial = tracker.newClass(start, epoch)
        assertEquals(epoch + m(177) * 1_000L, initial.classEndEpochMs)
        monotonic += m(40) * 1_000L
        val forwardClockJump = tracker.observe(timer.update().view, epoch + m(42) * 1_000L)
        assertEquals(m(40), forwardClockJump.classElapsedSeconds)
        assertEquals(m(137), forwardClockJump.classRemainingSeconds)
        monotonic += m(137) * 1_000L
        assertTrue(tracker.hasReachedClassDeadline(epoch - 1_000_000L))
        assertEquals(TimerCue.SESSION_FINISHED, timer.finishAtDeadline().cue)
    }

    @Test fun displayedFinishMatchesApprovedMinuteEvenWithSubsecondRounding() {
        val session = session()
        var monotonic = 0L
        val timer = MonotonicSessionTimer(session) { monotonic }
        val startEpoch = 123L
        val absoluteFinish = startEpoch + m(180) * 1_000L - 1L
        val plan = RunEndTimePlanner.plan(session, startEpoch, absoluteFinish)
        val started = timer.start(plan.correctionSeconds)
        val tracker = ClassClockTracker { monotonic }
        val initial = tracker.newClass(started, startEpoch, absoluteFinish)
        assertEquals(absoluteFinish, initial.classEndEpochMs)
        assertEquals(m(180) - 1, started.totalSeconds)
        monotonic += (m(180) - 1) * 1_000L
        assertTrue(tracker.hasReachedClassDeadline(0L))
        assertEquals(absoluteFinish,
            tracker.observe(timer.update().view, -600_000L).classEndEpochMs)
    }

    @Test fun pauseResumesTowardChosenEndNotOriginal180MinuteEnd() {
        val session = session()
        var now = 0L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start(m(3))
        now = m(5).toLong() * 1_000L
        timer.pause()
        now += m(1).toLong() * 1_000L
        val resumed = timer.resume()
        assertEquals(m(4), resumed.recoveredSeconds)
        assertEquals(m(176), resumed.totalSeconds)
        assertEquals(m(180), session.rules.totalSeconds)
    }

    @Test fun restartWholeClassKeepingEndRetainsInitialApprovedDifference() {
        val session = session()
        var now = 1_000L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start(m(3))
        now += m(2).toLong() * 1_000L
        val restarted = timer.restartKeepingEndTime()
        assertEquals(m(5), restarted.recoveredSeconds)
        assertEquals(m(175), restarted.totalSeconds)
        assertEquals(0, restarted.elapsedSeconds)
    }

    @Test fun impossibleStartIsAtomicAndCanBeRetried() {
        val session = session()
        val timer = MonotonicSessionTimer(session) { 0L }
        try {
            timer.start(m(30))
            fail("Should not start an infeasible Class")
        } catch (_: IllegalArgumentException) { }
        val afterReject = timer.view()
        assertEquals(TimerStatus.READY, afterReject.status)
        assertEquals(m(180), afterReject.totalSeconds)
        assertEquals(0, afterReject.recoveredSeconds)
        assertEquals(m(177), timer.start(m(3)).totalSeconds)
    }

    @Test fun nextSkipsPhaseWithoutErasingInitialEndAdjustment() {
        val session = session()
        val timer = MonotonicSessionTimer(session) { 0L }
        timer.start(m(3))
        val next = timer.nextPhase()
        assertEquals(TimerStatus.RUNNING, next.view.status)
        assertEquals(m(3) - session.phases.first().durationSeconds,
            next.view.recoveredSeconds)
        assertEquals(m(180), session.rules.totalSeconds)
    }

    @Test fun newFreshRunAfterCompletionDoesNotInheritPreviousDeadline() {
        val session = session()
        val timer = MonotonicSessionTimer(session) { 0L }
        timer.start(m(3))
        timer.finishAtDeadline()
        val newRun = timer.start()
        assertEquals(m(180), newRun.totalSeconds)
        assertEquals(0, newRun.recoveredSeconds)
    }
}
