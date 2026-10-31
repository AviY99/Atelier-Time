package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class SpecialBreakRebalancerTest {
    private val engine = ScheduleEngine()
    private fun m(minutes: Int) = minutes * 60
    private fun session(manual: Boolean = false): ApprovedSession {
        val rules = SessionRules(
            id = "rebalance", name = "Rebalance", totalSeconds = m(180),
            summarySeconds = m(15), poseSeconds = m(25),
            shortBreakSeconds = m(5), specialBreakSeconds = m(15),
            specialBreakCount = 1,
            placement = if (manual) SpecialBreakPlacement.manual(listOf(2))
                else SpecialBreakPlacement(),
        )
        return engine.approve(engine.generate(rules, FinalPoseChoice.remainder()))
    }

    private fun specialCenter(phases: List<SchedulePhase>, unlocked: Int,
        actualSeconds: Int): Long {
        val special = phases.first { it.kind == PhaseKind.SPECIAL_BREAK }
        return actualSeconds.toLong() +
            special.startSeconds - phases[unlocked].startSeconds +
            special.durationSeconds / 2L
    }

    @Test fun futureSpecialMovesTowardPhysicalMidpointWithoutChangingThePastOrFinish() {
        val session = session()
        val before = FinalPairCompensation.adjust(session, -m(56),
            allowExtraRegular = true).phases
        val unlocked = 1
        val elapsed = m(1)
        val rebalanced = SpecialBreakRebalancer.rebalance(before, unlocked,
            elapsed, session.rules.totalSeconds, automatic = true)
        val center = m(90)
        val oldError = abs(specialCenter(before, unlocked, elapsed) - center)
        val newError = abs(specialCenter(rebalanced, unlocked, elapsed) - center)
        assertTrue("Actual special midpoint should move closer to physical center",
            newError < oldError)
        assertEquals(before.take(unlocked), rebalanced.take(unlocked))
        assertEquals(before.last().endSeconds, rebalanced.last().endSeconds)
        assertEquals(1, rebalanced.count { it.kind == PhaseKind.SPECIAL_BREAK })
        assertEquals(before.count { it.kind == PhaseKind.SHORT_BREAK },
            rebalanced.count { it.kind == PhaseKind.SHORT_BREAK })
        assertEquals(before.sumOf { it.durationSeconds },
            rebalanced.sumOf { it.durationSeconds })
        rebalanced.zipWithNext().forEach { (a, b) ->
            assertEquals(a.endSeconds, b.startSeconds)
        }
        assertEquals(session.rules.totalSeconds, session.phases.last().endSeconds)
    }

    @Test fun manualAndAlreadyObservedSpecialBreakAreNeverMoved() {
        val manual = session(manual = true)
        val manuallyChosen = FinalPairCompensation.adjust(manual, -m(35),
            allowExtraRegular = true).phases
        assertEquals(manuallyChosen, SpecialBreakRebalancer.rebalance(
            manuallyChosen, 1, m(1), m(180), automatic = false))

        val automatic = session()
        val phases = FinalPairCompensation.adjust(automatic, -m(56),
            allowExtraRegular = true).phases
        val specialIndex = phases.indexOfFirst { it.kind == PhaseKind.SPECIAL_BREAK }
        val rebalanced = SpecialBreakRebalancer.rebalance(phases, specialIndex + 1,
            m(90), m(180), automatic = true)
        assertEquals(phases.take(specialIndex + 1),
            rebalanced.take(specialIndex + 1))
        assertEquals(phases[specialIndex], rebalanced[specialIndex])
    }

    @Test fun futureTailReflowKeepsProtectedRuntimePrefixAndSpecialBreakCount() {
        val session = session()
        val initial = FinalPairCompensation.adjust(session, -m(56),
            allowExtraRegular = true)
        val moved = SpecialBreakRebalancer.rebalance(initial.phases, 1,
            m(1), m(180), automatic = true)
        val result = FinalPairCompensation.adjustFutureTail(session, moved,
            protectedThroughIndex = 2, newCorrectionSeconds = -m(60))
        assertEquals(moved.take(3), result.phases.take(3))
        assertEquals(m(240), result.totalSeconds)
        assertEquals(1, result.phases.count { it.kind == PhaseKind.SPECIAL_BREAK })
        result.phases.zipWithNext().forEach { (a, b) ->
            assertEquals(a.endSeconds, b.startSeconds)
        }
    }

    @Test fun manyNextActionsKeepReplanningFutureAndNeverRepaintPast() {
        var now = 0L
        val session = session()
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        repeat(7) {
            if (timer.view().status == TimerStatus.RUNNING &&
                timer.view().nextPhase != null) {
                now += 60_000L
                timer.nextPhase()
                val projection = timer.runtimeTimeline()
                assertEquals("Physical end is unchanged after each Next", m(180),
                    projection.totalSeconds)
                assertEquals(it + 1, projection.elapsedSeconds / 60)
                assertEquals(1, timer.activePhasesSnapshot().count { p ->
                    p.kind == PhaseKind.SPECIAL_BREAK
                })
                assertTrue(projection.segments.filter { p -> p.observed }
                    .all { p -> p.durationSeconds > 0 })
            }
        }
        val observed = timer.runtimeTimeline().segments.filter { it.observed }
        assertTrue(observed.size >= 4)
        assertEquals(m(7), observed.sumOf { it.durationSeconds })
        assertEquals(session.rules.totalSeconds, session.phases.last().endSeconds)
    }
}
