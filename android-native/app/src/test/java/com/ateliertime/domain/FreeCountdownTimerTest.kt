package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class FreeCountdownTimerTest {
    @Test fun countdownUsesMonotonicClockAndFinishesExactlyOnce() {
        var monotonic = 10_000L
        val timer = FreeCountdownTimer { monotonic }
        val start = timer.start(1, 2, 3)
        assertEquals(3723, start.remainingSeconds)
        monotonic += 1_000L
        assertEquals(3722, timer.update().view.remainingSeconds)
        monotonic += 3_722_000L
        val completed = timer.update()
        assertTrue(completed.completedNow)
        assertEquals(FreeTimerStatus.FINISHED, completed.view.status)
        assertEquals(0, completed.view.remainingSeconds)
        assertFalse(timer.update().completedNow)
    }

    @Test fun pauseResumePreserveRemainingMilliseconds() {
        var now = 0L
        val timer = FreeCountdownTimer { now }
        timer.start(0, 0, 3)
        now = 1_250L
        assertEquals(2, timer.pause().remainingSeconds)
        now = 50_000L
        assertEquals(2, timer.update().view.remainingSeconds)
        timer.resume()
        now += 1_749L
        assertFalse(timer.update().completedNow)
        now += 1L
        assertTrue(timer.update().completedNow)
    }

    @Test fun zeroRejectedAndExitNeverEmitsCompletion() {
        var now = 0L
        val timer = FreeCountdownTimer { now }
        assertTrue(runCatching { timer.start(0, 0, 0) }.isFailure)
        assertTrue(runCatching { timer.start(100, 0, 0) }.isFailure)
        timer.start(0, 0, 1)
        assertEquals(FreeTimerStatus.READY, timer.exit().status)
        now = 50_000L
        assertFalse(timer.update().completedNow)
        assertEquals(0, timer.view().durationSeconds)
    }

    @Test fun newStartAfterFinishIsIndependentOfPreviousTimer() {
        var now = 0L
        val timer = FreeCountdownTimer { now }
        timer.start(0, 0, 1)
        now = 1_000L
        assertTrue(timer.update().completedNow)
        timer.start(0, 1, 0)
        now += 59_999L
        assertFalse(timer.update().completedNow)
        now++
        assertTrue(timer.update().completedNow)
    }
}
