package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/**
 * D-111: deterministic, zero-sleep regression tests for the immutable end
 * clock. All clock jumps are injected; no device state or saved data is used.
 */
class ApprovedFinishRecoveryTest {
    private fun minutes(n: Int) = n * 60
    private val engine = ScheduleEngine()

    private fun approved(choice: FinalPoseChoice = FinalPoseChoice.remainder()) =
        engine.approve(engine.generate(SessionRules(
            id = "recovery", name = "Recovery at approved finish",
            totalSeconds = minutes(180), summarySeconds = minutes(15),
            poseSeconds = minutes(25), specialBreakCount = 1,
        ), choice))

    private fun pair(session: ApprovedSession): List<SchedulePhase> =
        session.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)

    private fun endingBreak(session: ApprovedSession) =
        session.phases.first { it.startSeconds == pair(session).first().endSeconds }

    @Test fun extraFinishUpToTwiceApprovedLengthIsAccepted() {
        val session = approved()
        val start = 5_000L
        val latest = start + 2L * session.rules.totalSeconds * 1_000L
        val accepted = RunEndTimePlanner.plan(session, start, latest)
        assertEquals(2 * minutes(180), accepted.adjustedDurationSeconds)
        assertEquals(-minutes(180), accepted.correctionSeconds)
        assertEquals(minutes(180), session.rules.totalSeconds)
    }

    @Test fun unrealisticOvernightExtensionIsRejectedAndLeavesSavedPlanUnchanged() {
        val session = approved()
        val saved = session.phases.toList()
        val start = 12_345L
        try {
            RunEndTimePlanner.plan(
                session, start, start + (2L * minutes(180) + 1L) * 1000L
            )
            fail("Longer than 2x the approved length must be rejected.")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message!!.contains("too late"))
        }
        assertEquals(saved, session.phases)
    }

    @Test fun earlyEndAtExactlyOneMinuteMergedFinalSessionIsAccepted() {
        val session = approved()
        val planned = RunEndTimePlanner.plan(session, 0L, minutes(146) * 1000L)
        assertEquals(minutes(1), planned.adjusted.firstFinalPoseSeconds)
        assertEquals(0, planned.adjusted.lastFinalPoseSeconds)
    }

    @Test fun earlyEndOneSecondBeforeMergedMinimumIsRejected() {
        val session = approved()
        try {
            RunEndTimePlanner.plan(session, 0L, (minutes(146) - 1L) * 1000L)
            fail("The merged final Session must be at least a minute.")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun pausingDuringPenultimateSessionShortensOnlyLastSession() {
        val session = approved()
        val final = pair(session)
        var monotonic = 0L
        val timer = MonotonicSessionTimer(session) { monotonic }
        timer.start()
        monotonic = (final.first().startSeconds + 15L) * 1000L
        timer.pause()
        monotonic += 25_000L
        val resumed = timer.resume()
        assertEquals(TimerStatus.RUNNING, resumed.status)
        assertEquals(
            final.first().durationSeconds to final.last().durationSeconds - 25,
            timer.finalTwoPoseDurations()
        )
        assertEquals(180L * 60L, monotonic / 1000L + resumed.totalRemainingSeconds)
        assertEquals(session.rules.totalSeconds, session.phases.last().endSeconds)
    }

    @Test fun pausingDuringInterFinalBreakPreservesEntireCompletedPenultimateSession() {
        val session = approved()
        val original = pair(session)
        val rest = endingBreak(session)
        var monotonic = 0L
        val timer = MonotonicSessionTimer(session) { monotonic }
        timer.start()
        monotonic = (rest.startSeconds + 5L) * 1000L
        val before = timer.pause()
        assertEquals(rest.id, before.phase?.id)
        monotonic += 40_000L
        val after = timer.resume()
        assertEquals(rest.id, after.phase?.id)
        assertEquals(
            original.first().durationSeconds to original.last().durationSeconds - 40,
            timer.finalTwoPoseDurations()
        )
        assertEquals(180L * 60L, monotonic / 1000L + after.totalRemainingSeconds)
    }

    @Test fun stopAndContinueInLastBreakAlsoUsesLastOnly() {
        val session = approved()
        val rest = endingBreak(session)
        val firstLength = pair(session).first().durationSeconds
        var monotonic = 0L
        val timer = MonotonicSessionTimer(session) { monotonic }
        timer.start()
        monotonic = (rest.startSeconds + 2L) * 1000L
        timer.stop()
        monotonic += 70_000L
        val resumed = timer.start()
        assertEquals(TimerStatus.RUNNING, resumed.status)
        assertEquals(firstLength, timer.finalTwoPoseDurations()?.first)
        assertEquals(70, resumed.recoveredSeconds)
    }

    @Test fun alreadyAcceptedEarlyEndRemainsFixedAfterLateBreakPause() {
        val session = approved()
        val initial = minutes(3)
        val plan = RunEndTimePlanner.plan(
            session, 0L, (session.rules.totalSeconds - initial) * 1000L
        )
        val rest = endingBreak(session)
        var monotonic = 0L
        val timer = MonotonicSessionTimer(session) { monotonic }
        timer.start(plan.correctionSeconds)
        monotonic = (rest.startSeconds + 2L) * 1000L
        timer.pause()
        monotonic += 50_000L
        val resumed = timer.resume()
        assertEquals(initial + 50, resumed.recoveredSeconds)
        assertEquals(pair(session).first().durationSeconds,
            timer.finalTwoPoseDurations()?.first)
        assertEquals(minutes(2) - 50, timer.finalTwoPoseDurations()?.second)
        assertEquals((minutes(180) - initial).toLong(),
            monotonic / 1000L + resumed.totalRemainingSeconds)
    }

    @Test fun alreadyAcceptedLateEndStillAllowsLastOnlyPauseCompensation() {
        val session = approved()
        val original = pair(session)
        val rest = endingBreak(session)
        var monotonic = 0L
        val timer = MonotonicSessionTimer(session) { monotonic }
        timer.start(-minutes(6))
        monotonic = (rest.startSeconds + 4L) * 1000L
        timer.pause()
        monotonic += 2L * 60_000L
        val resumed = timer.resume()
        assertEquals(original.first().durationSeconds,
            timer.finalTwoPoseDurations()?.first)
        assertEquals(original.last().durationSeconds + minutes(6) - minutes(2),
            timer.finalTwoPoseDurations()?.second)
        assertEquals(-minutes(4), resumed.recoveredSeconds)
        assertEquals(186L * 60L, monotonic / 1000L + resumed.totalRemainingSeconds)
    }

    @Test fun impossiblePauseInLastBreakIsRejectedAtomically() {
        val session = approved()
        val rest = endingBreak(session)
        var monotonic = 0L
        val timer = MonotonicSessionTimer(session) { monotonic }
        timer.start(minutes(3)) // last final Session has only 2 minutes
        monotonic = (rest.startSeconds + 1L) * 1000L
        timer.pause()
        val before = timer.view()
        val lengths = timer.finalTwoPoseDurations()
        monotonic += 61_000L // only 59 seconds would remain: impossible
        try {
            timer.resume()
            fail("Resume must not consume the last Session minimum.")
        } catch (_: IllegalArgumentException) { }
        assertEquals(TimerStatus.PAUSED, timer.view().status)
        assertEquals(before.totalSeconds, timer.view().totalSeconds)
        assertEquals(before.elapsedSeconds, timer.view().elapsedSeconds)
        assertEquals(lengths, timer.finalTwoPoseDurations())
    }

    @Test fun pauseAfterFinalSessionStartedCannotRewriteIt() {
        val session = approved()
        val final = pair(session).last()
        var monotonic = 0L
        val timer = MonotonicSessionTimer(session) { monotonic }
        timer.start()
        monotonic = (final.startSeconds + 10L) * 1000L
        timer.pause()
        val before = timer.view()
        monotonic += 1_000L
        try {
            timer.resume()
            fail("Do not rewrite already-started final Session.")
        } catch (_: IllegalStateException) { }
        assertEquals(TimerStatus.PAUSED, timer.view().status)
        assertEquals(before.totalSeconds, timer.view().totalSeconds)
        assertEquals(before.elapsedSeconds, timer.view().elapsedSeconds)
    }

    @Test fun unfinishedClassStillEndsOnApprovedPhysicalClockWhenPauseUnrecoverable() {
        val session = approved()
        var monotonic = 0L
        val timer = MonotonicSessionTimer(session) { monotonic }
        val tracker = ClassClockTracker { monotonic }
        val started = timer.start(minutes(3))
        val epochStart = 1_000_000_000L
        val targetEnd = epochStart + (minutes(177)).toLong() * 1000L
        tracker.newClass(started, epochStart, targetEnd)
        monotonic = 30_000L
        timer.pause()
        monotonic = minutes(177).toLong() * 1000L
        assertTrue(tracker.hasReachedClassDeadline(epochStart + monotonic))
        assertEquals(TimerCue.SESSION_FINISHED, timer.finishAtDeadline().cue)
        assertEquals(TimerStatus.FINISHED, timer.view().status)
        assertNull(timer.finishAtDeadline().cue)
        assertEquals(targetEnd, tracker.observe(timer.view(), epochStart + monotonic).classEndEpochMs)
    }

    @Test fun rejectedLateDeadlineLeavesTimerReadyAndRetryWorks() {
        val session = approved()
        val timer = MonotonicSessionTimer(session) { 0L }
        try {
            RunEndTimePlanner.plan(session, 0L, minutes(361) * 1000L)
            fail()
        } catch (_: IllegalArgumentException) { }
        assertEquals(TimerStatus.READY, timer.view().status)
        val good = RunEndTimePlanner.plan(session, 0L, minutes(190) * 1000L)
        assertEquals(-minutes(10), timer.start(good.correctionSeconds).recoveredSeconds)
    }
}
