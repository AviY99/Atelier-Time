package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** D-145 field semantics: one replay at a time, no entered Special replay. */
class D145RepeatOnceSpecialSafetyTest {
    private fun approved(specials: Int = 0): ApprovedSession {
        val engine = ScheduleEngine()
        val rules = SessionRules(
            id = "d145-runtime", name = "D145 repeat safety",
            totalSeconds = 180 * 60,
            summarySeconds = 15 * 60,
            poseSeconds = 25 * 60,
            shortBreakSeconds = 5 * 60,
            specialBreakSeconds = 15 * 60,
            specialBreakCount = specials,
        )
        return engine.approve(
            engine.generate(rules, FinalPoseChoice.remainder()),
            acknowledgeWarnings = true)
    }

    @Test fun repeatedPhaseLocksRepeatLastUntilSuccessfulNext() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 60_000L
        timer.nextPhase()
        assertEquals(PhaseKind.POSE, timer.repeatLastTarget()?.kind)

        val repeating = timer.repeatLastFullKeepingEndTime(
            acknowledgeRestWarning = true)
        assertEquals(PhaseKind.POSE, repeating.phase?.kind)
        assertNull("Repeat Last must not chain while replay is active",
            timer.repeatLastTarget())
        val before = timer.view()
        try {
            timer.repeatLastFullKeepingEndTime(acknowledgeRestWarning = true)
            fail("Back-to-back Repeat Last must be blocked")
        } catch (_: IllegalStateException) { }
        assertEquals(before, timer.view())

        now += 60_000L
        timer.nextPhase()
        assertNotNull("Next abandons replay and unlocks Repeat Last",
            timer.repeatLastTarget())
        assertEquals(PhaseKind.POSE, timer.repeatLastTarget()?.kind)
    }

    @Test fun naturalFinishOfReplayReenablesRepeatLastWithoutAnyNext() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 60_000L
        timer.nextPhase()
        timer.repeatLastFullKeepingEndTime(acknowledgeRestWarning = true)
        assertNull(timer.repeatLastTarget())
        now += 25L * 60_000L
        timer.update()
        assertEquals(PhaseKind.SHORT_BREAK, timer.view().phase?.kind)
        assertEquals(PhaseKind.POSE, timer.repeatLastTarget()?.kind)
    }

    @Test fun currentOrPreviousSpecialBreakCannotBeRepeatedOrRestarted() {
        var now = 0L
        val session = approved(specials = 1)
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        val special = timer.activePhasesSnapshot().first {
            it.kind == PhaseKind.SPECIAL_BREAK
        }
        now = special.startSeconds.toLong() * 1_000L
        timer.update()
        assertEquals(PhaseKind.SPECIAL_BREAK, timer.view().phase?.kind)
        assertNull("Cannot repeat a previous phase while special has started",
            timer.repeatLastTarget())
        val before = timer.view()
        try {
            timer.restartCurrentSegmentKeepingEndTime()
            fail("A special break may not be restarted.")
        } catch (_: IllegalStateException) { }
        assertEquals(before, timer.view())

        now += 60_000L
        timer.nextPhase()
        assertEquals(PhaseKind.POSE, timer.view().phase?.kind)
        assertNull("The previous special break must not be replayed",
            timer.repeatLastTarget())
        assertEquals(1, timer.activePhasesSnapshot().count {
            it.kind == PhaseKind.SPECIAL_BREAK
        })
    }

    @Test fun aRejectedRepeatDoesNotAlterRuntimeTimeline() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 60_000L
        timer.nextPhase()
        timer.repeatLastFullKeepingEndTime(acknowledgeRestWarning = true)
        val before = timer.runtimeTimeline()
        try {
            timer.repeatLastFullKeepingEndTime(acknowledgeRestWarning = true)
            fail("Repeat attempt must be refused while locked")
        } catch (_: IllegalStateException) { }
        assertEquals(before, timer.runtimeTimeline())
    }
}
