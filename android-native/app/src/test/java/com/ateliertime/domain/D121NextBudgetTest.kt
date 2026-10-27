package com.ateliertime.domain

import org.junit.Test
import org.junit.Assert.*

class D121NextBudgetTest {
    @Test fun fixedEndSurvivesFirstNext() {
        val engine = ScheduleEngine()
        val session = engine.approve(engine.generate(
            SessionRules("budget", "Class", 10800, 900, poseSeconds = 1500,
                specialBreakCount = 1), FinalPoseChoice.remainder()))
        var now = 0L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = 30000L
        val moved = timer.nextPhase().view
        val virtualRemaining = (moved.totalSeconds - moved.elapsedSeconds) * 1000L
        assertTrue(kotlin.math.abs(virtualRemaining - (10800000L - now)) < 3000L)
    }

    @Test fun elevenRapidNextEventsPreserveClassFinish() {
        val engine = ScheduleEngine()
        val session = engine.approve(engine.generate(
            SessionRules("rapid", "Class", 10800, 900, poseSeconds = 1500,
                specialBreakCount = 1), FinalPoseChoice.remainder()))
        var now = 0L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        var successes = 0
        repeat(11) {
            now += 30000L
            val before = timer.view()
            val outcome = runCatching { timer.nextPhase() }
            if (outcome.isSuccess) successes++
            else assertEquals("Rejected Next must not skip", before, timer.view())
            val after = timer.view()
            val virtualRemaining = (after.totalSeconds - after.elapsedSeconds) * 1000L
            assertTrue("Next changed time budget at step $it",
                kotlin.math.abs(virtualRemaining - (10800000L - now)) < 3000L)
        }
        assertTrue(successes >= 4)
        now = 10800000L - 1L
        assertEquals(TimerStatus.RUNNING, timer.update().view.status)
        now += 1L
        assertEquals(TimerStatus.FINISHED, timer.update().view.status)
    }
}
