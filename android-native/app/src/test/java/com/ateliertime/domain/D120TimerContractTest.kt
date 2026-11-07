package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

/** Regression gates for the product decisions approved after Stable 7. */
class D120TimerContractTest {
    private fun m(value: Int) = value * SECONDS_PER_MINUTE
    private val engine = ScheduleEngine()

    private fun ordinaryClass(): ApprovedSession = engine.approve(engine.generate(
        SessionRules("d120", "Three hours", m(180), m(15), poseSeconds = m(25),
            specialBreakCount = 1), FinalPoseChoice.remainder()))

    private fun finalSpecialClass(lengthMinutes: Int, totalMinutes: Int): ApprovedSession =
        engine.approve(engine.generate(SessionRules(
            id = "end-special", name = "Manual final break",
            totalSeconds = m(totalMinutes), poseSeconds = m(25),
            shortBreakSeconds = m(5), specialBreakSeconds = m(lengthMinutes),
            specialBreakCount = 1,
            placement = SpecialBreakPlacement.manual(listOf(2)),
        ), FinalPoseChoice.remainder()), acknowledgeWarnings = true)

    @Test fun repeatedSubsecondPausesChargeActualCombinedTimeOnly() {
        var now = 0L
        val timer = MonotonicSessionTimer(ordinaryClass()) { now }
        timer.start()
        repeat(10) {
            now += 100L // 0.1s work
            timer.pause()
            now += 100L // 0.1s paused
            timer.resume()
        }
        // Exactly one physical second paused, not ten individually rounded seconds.
        assertEquals(1, timer.view().recoveredSeconds)
        assertEquals(m(180) - 1, timer.view().totalSeconds)
        now = 10_791_000L
        assertEquals(TimerStatus.RUNNING, timer.update().view.status)
    }

    @Test fun randomizedShortPausesRoundOnlyTheAggregatePhysicalInterruption() {
        val random = Random(120)
        var now = 0L
        var totalPaused = 0L
        val timer = MonotonicSessionTimer(ordinaryClass()) { now }
        timer.start()
        repeat(200) {
            now += random.nextLong(10L, 300L)
            timer.pause()
            val interruption = random.nextLong(1L, 900L)
            now += interruption
            totalPaused += interruption
            timer.resume()
            assertEquals(((totalPaused + 999L) / 1_000L).toInt(),
                timer.view().recoveredSeconds)
        }
        assertTrue(totalPaused < m(3).toLong() * 1_000L)
        now = m(180).toLong() * 1_000L - 1L
        assertEquals(TimerStatus.RUNNING, timer.update().view.status)
    }

    @Test fun exactSubsecondPhysicalFinishIsNeverRoundedEarly() {
        var now = 0L
        val timer = MonotonicSessionTimer(ordinaryClass()) { now }
        timer.start(physicalDurationMillis = m(180).toLong() * 1_000L + 875L)
        now = m(180).toLong() * 1_000L
        assertEquals(TimerStatus.RUNNING, timer.update().view.status)
        now += 874L
        assertEquals(TimerStatus.RUNNING, timer.update().view.status)
        now += 1L
        assertEquals(TimerStatus.FINISHED, timer.update().view.status)
    }

    @Test fun classClockTrackerKeepsTheExactApprovedMillisecondDeadline() {
        var now = 0L
        val session = ordinaryClass()
        val timer = MonotonicSessionTimer(session) { now }
        val duration = m(180).toLong() * 1_000L + 875L
        val running = timer.start(physicalDurationMillis = duration)
        val tracker = ClassClockTracker { now }
        tracker.newClass(running, nowEpochMs = 100_000L,
            approvedFinishEpochMs = 100_000L + duration)
        now = duration - 1L
        assertFalse(tracker.hasReachedClassDeadline(100_000L + now))
        now++
        assertTrue(tracker.hasReachedClassDeadline(100_000L + now))
    }

    @Test fun genuineManualSpecialBreakCannotDisappearOnMerge() {
        val session = finalSpecialClass(lengthMinutes = 15, totalMinutes = 75)
        val originalSpecial = session.phases.single { it.kind == PhaseKind.SPECIAL_BREAK }
        val revised = FinalPairCompensation.adjust(session, m(25))
        assertEquals(m(50), revised.totalSeconds)
        assertEquals(1, revised.phases.count { it.kind == PhaseKind.SPECIAL_BREAK })
        assertEquals(originalSpecial.id,
            revised.phases.single { it.kind == PhaseKind.SPECIAL_BREAK }.id)
        assertEquals(2, revised.phases.takeLast(3).count { it.kind == PhaseKind.POSE })
        assertTrue(revised.phases.last().durationSeconds >= SECONDS_PER_MINUTE)
    }

    @Test fun shortConfiguredSpecialBreakMayMergeAsOrdinaryAtRuntime() {
        val session = finalSpecialClass(lengthMinutes = 3, totalMinutes = 63)
        assertEquals(1, session.phases.count { it.kind == PhaseKind.SPECIAL_BREAK })
        val revised = FinalPairCompensation.adjust(session, m(10))
        assertEquals(m(53), revised.totalSeconds)
        assertEquals(0, revised.phases.count { it.kind == PhaseKind.SPECIAL_BREAK })
        assertEquals(m(23), revised.phases.last().durationSeconds)
        // Runtime normalization never mutates the approved Editor timeline.
        assertEquals(1, session.phases.count { it.kind == PhaseKind.SPECIAL_BREAK })
    }

    @Test fun equalLengthSpecialUsesOrdinaryRuntimeMergePolicy() {
        val session = finalSpecialClass(lengthMinutes = 5, totalMinutes = 65)
        val revised = FinalPairCompensation.reflow(session, session.phases, m(10))
        assertEquals(m(55), revised.totalSeconds)
        assertEquals(0, revised.phases.count { it.kind == PhaseKind.SPECIAL_BREAK })
        assertEquals(m(25), revised.phases.last().durationSeconds)
    }

    @Test fun nextWithFeasibleCreditNeverFinishesBeforeRealClassDeadline() {
        var now = 0L
        val session = ordinaryClass()
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now = 60_000L
        assertEquals(PhaseKind.SHORT_BREAK, timer.nextPhase().view.phase?.kind)
        now = m(180).toLong() * 1_000L - 1L
        assertEquals(TimerStatus.RUNNING, timer.update().view.status)
        now++
        assertEquals(TimerStatus.FINISHED, timer.update().view.status)
    }

    @Test fun nextFromLastWorkCannotFinishBeforeHardPhysicalDeadline() {
        var now = 0L
        val session = ordinaryClass()
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        val finalWork = session.phases.last { it.kind == PhaseKind.POSE }
        now = (finalWork.startSeconds.toLong() + 5L) * 1_000L
        val before = timer.view()
        try {
            timer.nextPhase()
            fail("Next must not create an early automatic end")
        } catch (_: IllegalStateException) { }
        assertEquals(before.phase?.id, timer.view().phase?.id)
        assertEquals(TimerStatus.RUNNING, timer.view().status)
    }

    @Test fun noFeasibleCompensationRejectsNextWithoutChangingVirtualProgress() {
        var now = 0L
        val timer = MonotonicSessionTimer(ordinaryClass()) { now }
        timer.start()
        now = 60_000L
        timer.pause()
        now = 61L * SECONDS_PER_MINUTE * 1_000L
        val before = timer.view()
        try {
            timer.nextPhase()
            fail("Uncompensated Next must preserve the hard finish")
        } catch (_: IllegalStateException) { }
        assertEquals(before, timer.view())
    }
}
