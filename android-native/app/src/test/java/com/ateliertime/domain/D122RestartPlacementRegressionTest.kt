package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** Regressions for the D-121 Restart case, after multiple Next insertions. */
class D122RestartPlacementRegressionTest {
    private fun approved(): ApprovedSession {
        val engine = ScheduleEngine()
        return engine.approve(engine.generate(
            SessionRules("d122", "Three hours", totalSeconds = 180 * 60,
                poseSeconds = 25 * 60, specialBreakSeconds = 15 * 60,
                specialBreakCount = 1), FinalPoseChoice.remainder()))
    }

    private fun expanded(): Pair<ApprovedSession, CompensatedTimeline> {
        val session = approved()
        // ~3h58m credit, as in the user screenshot: several additional
        // full regular Sessions + Breaks exist before final two Sessions.
        val plan = FinalPairCompensation.adjust(session, -14_301,
            allowExtraRegular = true)
        assertTrue(plan.phases.count {
            it.id.startsWith("runtime-added-pose-")
        } >= 4)
        return session to plan
    }

    @Test fun restartEightSecondsLaterDoesNotRelocateAnySpecialBreakOrAddedBlock() {
        val (session, plan) = expanded()
        val active = SpecialBreakRebalancer.rebalance(
            plan.phases, firstUnstartedIndex = 1,
            actualElapsedSeconds = 20 * 60, physicalClassSeconds = 180 * 60,
            automatic = true)
        val currentBreakIndex = active.indexOfFirst {
            it.id.startsWith("runtime-added-break-")
        }
        assertTrue("Need an inserted regular break", currentBreakIndex > 0)
        val originalSpecials = active.withIndex().filter {
            it.value.kind == PhaseKind.SPECIAL_BREAK
        }.map { it.index to it.value.id }
        assertEquals(1, originalSpecials.size)

        val revised = FinalPairCompensation.reflow(
            session, active, correctionSeconds = -14_293,
            allowExtraRegular = true,
            boundary = ReflowBoundary(
                protectedThroughIndex = currentBreakIndex,
                preserveExistingTail = true))
        val lastIndex = active.indexOfLast {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }
        assertTrue(lastIndex > currentBreakIndex)
        // This is the D-119 late Restart fallback: ONLY the unstarted
        // last work Session absorbs eight seconds.
        assertEquals(active.take(lastIndex), revised.phases.take(lastIndex))
        assertEquals(active[lastIndex].durationSeconds - 8,
            revised.phases[lastIndex].durationSeconds)
        assertEquals(active.size, revised.phases.size)
        assertEquals(originalSpecials, revised.phases.withIndex().filter {
            it.value.kind == PhaseKind.SPECIAL_BREAK
        }.map { it.index to it.value.id })
        assertEquals(plan.totalSeconds - 8, revised.totalSeconds)
    }

    @Test fun resumeDoesNotRebuildPostNextFutureOrMoveSpecialBreak() {
        val (session, plan) = expanded()
        val active = plan.phases
        val currentBreak = active.indexOfFirst {
            it.id.startsWith("runtime-added-break-")
        }
        assertTrue(currentBreak >= 0)
        val revised = FinalPairCompensation.reflow(
            session, active, correctionSeconds = -14_295,
            allowExtraRegular = true,
            boundary = ReflowBoundary(protectedThroughIndex = currentBreak,
                preserveExistingTail = true))
        val firstFinal = active.indexOfLast {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        } - 2
        assertEquals(active.take(firstFinal), revised.phases.take(firstFinal))
        assertEquals(active.map { it.id }, revised.phases.map { it.id })
        assertEquals(active.map { it.kind }, revised.phases.map { it.kind })
        assertEquals(plan.totalSeconds - 6, revised.totalSeconds)
    }

    @Test fun existingTailModeNeverRebuildsInsertedPhaseCount() {
        val (session, plan) = expanded()
        val current = plan.phases.indexOfFirst {
            it.id.startsWith("runtime-added-break-")
        }
        val after = FinalPairCompensation.reflow(session, plan.phases,
            correctionSeconds = -14_293, allowExtraRegular = true,
            boundary = ReflowBoundary(protectedThroughIndex = current,
                preserveExistingTail = true))
        assertEquals(plan.phases.count { it.id.startsWith("runtime-added-") },
            after.phases.count { it.id.startsWith("runtime-added-") })
    }
}
