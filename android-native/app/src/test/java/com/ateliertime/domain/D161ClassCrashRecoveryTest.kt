package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** D161 recovery must preserve an approved end and effective in-flight timeline. */
class D161ClassCrashRecoveryTest {
    private val engine = ScheduleEngine()
    private fun approved(): ApprovedSession {
        val rules = SessionRules(
            id = "recover", name = "Recovery test",
            totalSeconds = 180 * 60, summarySeconds = 15 * 60,
            poseSeconds = 25 * 60, specialBreakCount = 1,
            preparationSeconds = 0,
        )
        return engine.approve(engine.generate(rules, FinalPoseChoice.remainder()),
            acknowledgeWarnings = true)
    }

    @Test fun killedProcessRecoversFromCurrentTimeWithoutRestartingOriginalDeadline() {
        var now = 1_000_000L
        val session = approved()
        val running = MonotonicSessionTimer(session) { now }
        running.start()
        val originalEnd = now + 180 * 60_000L
        now += 77_000L
        running.update()
        val checkpoint = running.recoverySnapshot()

        // An Activity/process restart 93 seconds later, same monotonic boot.
        now += 93_000L
        val recovered = MonotonicSessionTimer(session) { now }
        val view = recovered.restoreRecovery(checkpoint, clockShiftMs = 0L)
        assertEquals(TimerStatus.RUNNING, view.status)
        assertEquals(170, view.elapsedSeconds)
        assertEquals(180 * 60 - 170, view.totalRemainingSeconds)
        assertEquals(checkpoint.activePhases, recovered.activePhasesSnapshot())
        assertEquals(originalEnd, recovered.recoverySnapshot().physicalDeadlineMs)
        assertNull(recovered.update().cue)
    }

    @Test fun pausedClassCanResumeAfterReopenWhileKeepingOriginalFinish() {
        var now = 5_000_000L
        val session = approved()
        val running = MonotonicSessionTimer(session) { now }
        running.start()
        val originalEnd = now + 180 * 60_000L
        now += 30_000L
        running.pause()
        val checkpoint = running.recoverySnapshot()
        assertEquals(TimerStatus.PAUSED, checkpoint.status)

        now += 16_000L
        val continued = MonotonicSessionTimer(session) { now }
        val view = continued.restoreRecovery(checkpoint, clockShiftMs = 0L)
        assertEquals(TimerStatus.RUNNING, view.status)
        assertTrue(view.elapsedSeconds >= 30)
        assertEquals(originalEnd, continued.recoverySnapshot().physicalDeadlineMs)
    }

    @Test fun refusesCheckpointAfterOriginalPhysicalEnd() {
        var now = 3_000_000L
        val session = approved()
        val running = MonotonicSessionTimer(session) { now }
        running.start()
        val saved = running.recoverySnapshot()
        now += 180 * 60_000L
        val recovered = MonotonicSessionTimer(session) { now }
        try {
            recovered.restoreRecovery(saved, 0L)
            fail("The physical Class finish must remain immutable")
        } catch (_: IllegalStateException) { }
    }

    @Test fun shortPreparationPhaseSurvivesCheckpoint() {
        var now = 1_000_000L
        val rules = SessionRules(
            id = "prep-recover", name = "Prep recovery",
            totalSeconds = 180 * 60, summarySeconds = 15 * 60,
            openingGroups = listOf(
                OpeningPoseGroup("intro", count = 4, poseSeconds = 60,
                    breakAfterSeconds = 300),
            ),
            poseSeconds = 25 * 60,
            preparationSeconds = 5,
            specialBreakCount = 0,
        )
        val session = engine.approve(engine.generate(rules, FinalPoseChoice.remainder()),
            acknowledgeWarnings = true)
        val running = MonotonicSessionTimer(session) { now }
        running.start()
        now += 62_000L
        running.update()
        assertEquals(PhaseKind.PREPARATION, running.view().phase?.kind)
        val saved = running.recoverySnapshot()
        now += 1_000L
        val continued = MonotonicSessionTimer(session) { now }
        assertEquals(PhaseKind.PREPARATION,
            continued.restoreRecovery(saved, 0L).phase?.kind)
        assertEquals(saved.activePhases, continued.activePhasesSnapshot())
    }
}
