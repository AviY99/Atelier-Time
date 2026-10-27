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
}
