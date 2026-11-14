package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** New D-144 runtime actions: previous full vs current full vs Next forward. */
class D144RuntimeRepeatSafetyTest {
    private fun approved(): ApprovedSession {
        val engine = ScheduleEngine()
        val rules = SessionRules(
            id = "d144", name = "QA repeated Class",
            totalSeconds = 180 * 60, summarySeconds = 15 * 60,
            poseSeconds = 25 * 60, shortBreakSeconds = 5 * 60,
            specialBreakCount = 0,
        )
        return engine.approve(
            engine.generate(rules, FinalPoseChoice.remainder()),
            acknowledgeWarnings = true)
    }

    @Test fun restartCurrentRequiresExplicit25_5AfterPartiallyWorkedPose() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        assertFalse("Exactly 25m is not above 25m", timer.restartCurrentRequiresRestWarning())
        now = 20 * 60_000L
        assertTrue(timer.restartCurrentRequiresRestWarning())
        val before = timer.view()
        try {
            timer.restartCurrentSegmentKeepingEndTime()
            fail("25/5 warning may not be silently bypassed")
        } catch (_: IllegalStateException) { }
        assertEquals(before, timer.view())
        val after = timer.restartCurrentSegmentKeepingEndTime(acknowledgeRestWarning = true)
        assertEquals(PhaseKind.POSE, after.phase?.kind)
        assertEquals(25 * 60, after.phaseRemainingSeconds)
    }

    @Test fun mistakenNextDoesNotConvertToUndoOnlyRemainder() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 2 * 60_000L
        timer.nextPhase()
        val previous = timer.repeatLastTarget()
        assertEquals(PhaseKind.POSE, previous?.kind)
        assertEquals(25 * 60, previous?.durationSeconds)
        assertTrue(timer.repeatLastRequiresRestWarning())
        val before = timer.view()
        try {
            timer.repeatLastFullKeepingEndTime()
            fail("25/5 waiver must be explicitly accepted")
        } catch (_: IllegalStateException) { }
        assertEquals(before, timer.view())
        val repeated = timer.repeatLastFullKeepingEndTime(acknowledgeRestWarning = true)
        assertEquals(previous?.id, repeated.phase?.id)
        assertEquals(25 * 60, repeated.phaseRemainingSeconds)
        assertTrue(timer.runtimeTimeline().segments.any {
            it.observed && it.kind == PhaseKind.POSE && it.durationSeconds >= 2 * 60
        })
    }

    @Test fun fullCompleted25MinutePoseCannotRepeatWithoutWarning() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 25 * 60_000L
        timer.update()
        assertEquals(PhaseKind.SHORT_BREAK, timer.view().phase?.kind)
        assertTrue(timer.repeatLastRequiresRestWarning())
    }

    @Test fun aRealFiveMinuteBreakResetsAccumulatedWorkForRestart() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 25 * 60_000L
        timer.update()
        now = 30 * 60_000L
        timer.update()
        assertEquals(PhaseKind.POSE, timer.view().phase?.kind)
        assertFalse(timer.restartCurrentRequiresRestWarning())
        assertEquals(PhaseKind.SHORT_BREAK, timer.repeatLastTarget()?.kind)
        assertFalse(timer.repeatLastRequiresRestWarning())
    }

    @Test fun restartBreakDoesNotTriggerPoseOnlyWarning() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 25 * 60_000L
        timer.update()
        now += 60_000L
        assertFalse(timer.restartCurrentRequiresRestWarning())
        val again = timer.restartCurrentSegmentKeepingEndTime()
        assertEquals(PhaseKind.SHORT_BREAK, again.phase?.kind)
        assertEquals(5 * 60, again.phaseRemainingSeconds)
    }
}
