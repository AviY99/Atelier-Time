package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class D161ClassCrashRecoverySnapshotTest {
    private fun session(): ApprovedSession {
        val rules = SessionRules(
            id = "recover", name = "Recovery",
            totalSeconds = 180 * 60,
            summarySeconds = 15 * 60,
            openingGroups = listOf(OpeningPoseGroup("group", 4, 60, 300)),
            preparationSeconds = 5,
            poseSeconds = 25 * 60,
        )
        return ScheduleEngine().approve(
            ScheduleEngine().generate(rules, FinalPoseChoice.remainder()),
            acknowledgeWarnings = true,
        )
    }

    @Test fun restoresOriginalClockAndPreparationAfterProcessDeath() {
        var clock = 10_000L
        val approved = session()
        val original = MonotonicSessionTimer(approved) { clock }
        original.start()
        clock += 20_000L
        val saved = original.recoverySnapshot()
        assertEquals(TimerStatus.RUNNING, saved.status)
        assertEquals(3, saved.activePhases.count {
            it.kind == PhaseKind.PREPARATION
        })
        clock += 25_000L // OS killed Activity for 25 more real seconds
        val current = original.view()
        val recreated = MonotonicSessionTimer(approved) { clock }
        val restored = recreated.restoreRecovery(saved, clockShiftMs = 0L)
        assertEquals(current.elapsedSeconds, restored.elapsedSeconds)
        assertEquals(current.phase, restored.phase)
        assertEquals(original.activePhasesSnapshot(), recreated.activePhasesSnapshot())
        assertEquals(current.totalRemainingSeconds, restored.totalRemainingSeconds)
    }

    @Test fun recoveryTranslatesMonotonicClockAcrossRebootUsingWallTime() {
        var oldClock = 500_000L
        val approved = session()
        val started = MonotonicSessionTimer(approved) { oldClock }
        started.start()
        oldClock += 30_000L
        val saved = started.recoverySnapshot()
        // Simulate device restart: new elapsedRealtime starts at just 3 seconds.
        var rebootedClock = 3_000L
        val sinceCheckpoint = 40_000L
        val shift = rebootedClock - oldClock - sinceCheckpoint
        val recovered = MonotonicSessionTimer(approved) { rebootedClock }
        val view = recovered.restoreRecovery(saved, shift)
        assertEquals(70, view.elapsedSeconds)
        assertEquals(approved.rules.totalSeconds - 70, view.totalRemainingSeconds)
    }

    @Test fun stoppedOrPausedRecoveryUsesOriginalDeadlineAndResumes() {
        var clock = 100_000L
        val approved = session()
        val timer = MonotonicSessionTimer(approved) { clock }
        timer.start()
        clock += 30_000L
        timer.pause()
        val checkpoint = timer.recoverySnapshot()
        clock += 20_000L
        val recovered = MonotonicSessionTimer(approved) { clock }
        val view = recovered.restoreRecovery(checkpoint, 0L)
        assertEquals(TimerStatus.RUNNING, view.status)
        assertEquals(30, view.elapsedSeconds)
        assertEquals(20, view.recoveredSeconds)
    }

    @Test fun expiredDeadlineDoesNotRestore() {
        var clock = 1_000L
        val approved = session()
        val original = MonotonicSessionTimer(approved) { clock }
        original.start()
        val checkpoint = original.recoverySnapshot()
        clock += approved.rules.totalSeconds * 1_000L + 1L
        val recovered = MonotonicSessionTimer(approved) { clock }
        try {
            recovered.restoreRecovery(checkpoint, 0L)
            fail("Past approved Class deadline must not be continued.")
        } catch (_: IllegalStateException) { }
    }
}
