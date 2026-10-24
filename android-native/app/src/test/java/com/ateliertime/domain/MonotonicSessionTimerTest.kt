package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class MonotonicSessionTimerTest {
    private val minute = SECONDS_PER_MINUTE
    private val engine = ScheduleEngine()
    private fun approved(): ApprovedSession {
        val rules = SessionRules(
            id = "timer", name = "Clock", totalSeconds = 180 * minute,
            summarySeconds = 15 * minute, poseSeconds = 25 * minute,
            specialBreakCount = 1,
        )
        return engine.approve(engine.generate(rules, FinalPoseChoice.remainder()))
    }

    @Test fun startsFromApprovedTimelineAndAdvancesWithoutWallClock() {
        var now = 100_000L
        val timer = MonotonicSessionTimer(approved()) { now }
        assertEquals(TimerStatus.READY, timer.view().status)
        assertEquals(180 * minute, timer.view().totalRemainingSeconds)
        timer.start()
        now += 24_000L
        val view = timer.update().view
        assertEquals(24, view.elapsedSeconds)
        assertEquals(25 * minute - 24, view.phaseRemainingSeconds)
        assertEquals(PhaseKind.POSE, view.phase?.kind)
        assertEquals(PhaseKind.SHORT_BREAK, view.nextPhase?.kind)
    }

    @Test fun pausesAndResumesWithoutCountingPausedTime() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 50_000L
        timer.pause()
        now = 350_000L
        assertEquals(50, timer.update().view.elapsedSeconds)
        val resumed = timer.resume()
        assertEquals(300, resumed.recoveredSeconds)
        assertEquals(180 * minute - 300, resumed.totalSeconds)
        now += 12_000L
        assertEquals(62, timer.update().view.elapsedSeconds)
        timer.stop()
        assertEquals(62, timer.view().elapsedSeconds)
        assertEquals(TimerStatus.STOPPED, timer.view().status)
        now += 120_000L
        val continued = timer.start()
        assertEquals(62, continued.elapsedSeconds)
        assertEquals(420, continued.recoveredSeconds)
        val newSession = timer.restartFromBeginning()
        assertEquals(0, newSession.elapsedSeconds)
        assertEquals(180 * minute, newSession.totalSeconds)
    }

    @Test fun emitsDifferentSingleCuesOnPoseAndBreakEdges() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 25L * minute * 1000L
        assertEquals(TimerCue.POSE_FINISHED, timer.update().cue)
        assertNull(timer.update().cue)
        now = 30L * minute * 1000L
        assertEquals(TimerCue.BREAK_FINISHED, timer.update().cue)
        assertNull(timer.update().cue)
    }

    @Test fun skipsStaleTransitionsButFinishesOnce() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 121L * minute * 1000L
        assertNull(timer.update().cue)
        now = 180L * minute * 1000L
        val last = timer.update()
        assertEquals(TimerCue.SESSION_FINISHED, last.cue)
        assertEquals(TimerStatus.FINISHED, last.view.status)
        assertEquals(0, last.view.totalRemainingSeconds)
        assertNull(timer.update().cue)
        timer.start()
        assertEquals(TimerStatus.RUNNING, timer.view().status)
    }

    @Test fun consecutiveOpeningPoseTransitionsPlayPoseMelodyOnce() {
        val rules = SessionRules(
            id = "opening", name = "Opening", totalSeconds = 5 * minute,
            openingGroups = listOf(OpeningPoseGroup("o", 2, minute)),
            poseSeconds = minute, shortBreakSeconds = minute,
            specialBreakCount = 0,
        )
        val session = engine.approve(engine.generate(rules))
        var now = 0L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = minute.toLong() * 1000L
        assertEquals(TimerCue.POSE_FINISHED, timer.update().cue)
        assertNull(timer.update().cue)
    }

    @Test fun slightlyDelayedUiLoopStillEmitsCorrectAutomaticCue() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 25L * minute * 1000L + 2_200L
        assertEquals(TimerCue.POSE_FINISHED, timer.update().cue)
        assertNull(timer.update().cue)
        now = 30L * minute * 1000L + 1_000L
        assertEquals(TimerCue.BREAK_FINISHED, timer.update().cue)
    }

    @Test fun interruptionCannotSilentlyRewriteFinalPairAlreadyInProgress() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        now = 140L * minute * 1000L
        timer.update()
        timer.pause()
        now += minute * 1000L
        try {
            timer.resume()
            fail("Must refuse to change an already-started final pose.")
        } catch (_: IllegalStateException) { }
        assertEquals(TimerStatus.PAUSED, timer.view().status)
    }

    @Test fun interruptionBeyondFinalPairCapacityRequiresNewApprovedPlan() {
        var now = 0L
        val timer = MonotonicSessionTimer(approved()) { now }
        timer.start()
        timer.pause()
        now = 29L * minute * 1000L
        try {
            timer.resume()
            fail("Need at least one minute for each of two final poses.")
        } catch (_: IllegalArgumentException) { }
        assertEquals(TimerStatus.PAUSED, timer.view().status)
    }

    @Test fun defaultMelodiesAreDistinctAndFiveSecondsLong() {
        val choices = MelodySelections()
        assertEquals(6, MelodyPattern.entries.size)
        assertEquals(10, choices.poseEnd.notesHz.size)
        assertEquals(10, choices.breakEnd.notesHz.size)
        assertEquals(10, choices.sessionEnd.notesHz.size)
        assertNotEquals(choices.poseEnd.notesHz, choices.breakEnd.notesHz)
        assertNotEquals(choices.sessionEnd.notesHz, choices.breakEnd.notesHz)
        assertEquals(MelodyPattern.SOFT_CHIMES,
            choices.forCue(TimerCue.POSE_FINISHED))
        assertEquals(MelodyPattern.RISING_STEPS,
            choices.forCue(TimerCue.BREAK_FINISHED))
        assertEquals(MelodyPattern.CELEBRATION,
            choices.forCue(TimerCue.SESSION_FINISHED))
    }
}
