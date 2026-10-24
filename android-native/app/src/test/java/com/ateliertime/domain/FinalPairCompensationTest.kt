package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class FinalPairCompensationTest {
    private val engine = ScheduleEngine()
    private fun m(minutes: Int) = 60 * minutes
    private fun session(choice: FinalPoseChoice): ApprovedSession {
        val rules = SessionRules(
            id = "b", name = "Example B", totalSeconds = m(180),
            summarySeconds = m(15), poseSeconds = m(25), specialBreakCount = 1
        )
        return engine.approve(engine.generate(rules, choice))
    }
    private fun verify(original: ApprovedSession, revised: CompensatedTimeline) {
        assertEquals(0, revised.phases.first().startSeconds)
        assertEquals(revised.totalSeconds, revised.phases.last().endSeconds)
        revised.phases.forEach { assertTrue(it.durationSeconds > 0) }
        revised.phases.zipWithNext().forEach { (a, b) ->
            assertEquals(a.endSeconds, b.startSeconds)
        }
        assertEquals(m(15), revised.phases.last().durationSeconds)
        val oldPair = original.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        val nextPair = revised.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        assertEquals(oldPair.first().startSeconds, nextPair.first().startSeconds)
        assertEquals(
            oldPair.sumOf { it.durationSeconds } - revised.recoveredSeconds,
            nextPair.sumOf { it.durationSeconds },
        )
    }

    @Test fun remainderChoiceShortensLastPoseFirst() {
        val original = session(FinalPoseChoice.remainder())
        val cut = FinalPairCompensation.adjust(original, m(3))
        verify(original, cut)
        assertEquals(m(25), cut.firstFinalPoseSeconds)
        assertEquals(m(2), cut.lastFinalPoseSeconds)
        assertEquals(m(177), cut.totalSeconds)
        assertEquals(m(15), cut.phases.last().durationSeconds)
    }

    @Test fun evenChoiceKeepsFinalPairBalanced() {
        val original = session(FinalPoseChoice.even())
        val cut = FinalPairCompensation.adjust(original, m(6))
        verify(original, cut)
        assertEquals(m(12), cut.firstFinalPoseSeconds)
        assertEquals(m(12), cut.lastFinalPoseSeconds)
    }

    @Test fun customChoicePreservesChosenPairRatio() {
        val original = session(FinalPoseChoice.custom(m(18)))
        val cut = FinalPairCompensation.adjust(original, m(5))
        verify(original, cut)
        assertEquals(m(15), cut.firstFinalPoseSeconds)
        assertEquals(m(10), cut.lastFinalPoseSeconds)
        assertEquals(m(175), cut.totalSeconds)
    }

    @Test fun zeroDelayDoesNotChangeApprovedPhaseBoundaries() {
        val original = session(FinalPoseChoice.custom(m(18)))
        val zero = FinalPairCompensation.adjust(original, 0)
        assertEquals(original.phases, zero.phases)
        assertEquals(original.rules.totalSeconds, zero.totalSeconds)
    }

    @Test fun cannotReduceFinalPosesBelowOneMinuteEach() {
        val original = session(FinalPoseChoice.remainder())
        try {
            FinalPairCompensation.adjust(original, m(29))
            fail("Two 1m final poses must be preserved")
        } catch (_: IllegalArgumentException) { }
    }
}
