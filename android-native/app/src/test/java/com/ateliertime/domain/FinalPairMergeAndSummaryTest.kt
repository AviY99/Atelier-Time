package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class FinalPairMergeAndSummaryTest {
    private val engine = ScheduleEngine()
    private fun approved(summary: Int = 15 * 60): ApprovedSession =
        engine.approve(engine.generate(SessionRules(
            id = "merge-test", name = "Merge Test", totalSeconds = 180 * 60,
            summarySeconds = summary, poseSeconds = 25 * 60,
            specialBreakCount = 1,
        ), FinalPoseChoice.remainder()))

    private fun threshold(session: ApprovedSession): Int {
        val pair = session.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        val between = session.phases.first {
            it.startSeconds == pair[0].endSeconds
        }
        return pair[0].durationSeconds + between.durationSeconds +
            pair[1].durationSeconds - session.rules.poseSeconds
    }

    @Test fun greaterThanRegularRemainsSplitButEqualAndLessMerge() {
        val session = approved()
        val boundary = threshold(session)
        assertTrue(boundary > 0)
        val split = FinalPairCompensation.adjust(session, boundary - 1)
        val equal = FinalPairCompensation.adjust(session, boundary)
        val less = FinalPairCompensation.adjust(session, boundary + 1)
        val originalIds = session.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2).map { it.id }
        assertEquals(2, split.phases.count { it.id in originalIds })
        assertEquals(1, equal.phases.count { it.id in originalIds })
        assertEquals(1, less.phases.count { it.id in originalIds })
        assertEquals(session.rules.poseSeconds, equal.firstFinalPoseSeconds)
        assertEquals(session.rules.poseSeconds - 1, less.firstFinalPoseSeconds)
        assertEquals(0, equal.lastFinalPoseSeconds)
        assertEquals(session.rules.totalSeconds - boundary, equal.totalSeconds)
        assertEquals(session.rules.totalSeconds - boundary - 1, less.totalSeconds)
        assertEquals(session.phases, session.phases.toList())
    }

    @Test fun mergedSessionAbsorbsBreakTimeAndPreservesAbsoluteEnd() {
        val session = approved()
        val original = session.phases.toList()
        val loss = threshold(session) + 2 * 60
        val revised = FinalPairCompensation.adjust(session, loss)
        assertEquals(original.size - 2, revised.phases.size)
        val firstFinalId = original.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2).first().id
        val merged = revised.phases.single { it.id == firstFinalId }
        assertEquals(session.rules.poseSeconds - 2 * 60, merged.durationSeconds)
        assertEquals(session.rules.totalSeconds - loss, revised.phases.last().endSeconds)
        assertEquals(session.rules.summarySeconds, revised.phases.last().durationSeconds)
        revised.phases.zipWithNext().forEach { (a, b) ->
            assertEquals(a.endSeconds, b.startSeconds)
        }
        assertEquals(original, session.phases)
    }

    @Test fun mergedLastSessionRequiresOneMinuteNotTwo() {
        val session = approved()
        val pair = session.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        val rest = session.phases.first { it.startSeconds == pair[0].endSeconds }
        val full = pair.sumOf { it.durationSeconds } + rest.durationSeconds
        val maximumLoss = full - 60
        val oneMinute = FinalPairCompensation.adjust(session, maximumLoss)
        assertEquals(60, oneMinute.firstFinalPoseSeconds)
        assertTrue(runCatching {
            FinalPairCompensation.adjust(session, maximumLoss + 1)
        }.isFailure)
    }

    @Test fun classEndSoundPlaysBeforeSummaryAndNeverAtItsEndAgain() {
        val session = approved()
        var now = 0L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        val summary = timer.activePhasesSnapshot().last()
        assertEquals(PhaseKind.SUMMARY, summary.kind)
        now = summary.startSeconds * 1_000L
        // Observe the penultimate phase before the boundary.
        now -= 1_000L
        timer.update()
        now += 1_000L
        assertEquals(TimerCue.SESSION_FINISHED, timer.update().cue)
        assertEquals(PhaseKind.SUMMARY, timer.view().phase?.kind)
        now = summary.endSeconds * 1_000L
        assertNull(timer.update().cue)
    }

    @Test fun manualNextDoesNotSkipSummaryOrPlayCompletionTwice() {
        val session = approved()
        var now = 0L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        val summary = session.phases.last()
        now = (summary.startSeconds - 5).toLong() * 1_000L
        timer.update()
        val entered = timer.nextPhase()
        assertEquals(PhaseKind.SUMMARY, entered.view.phase?.kind)
        assertEquals(TimerCue.SESSION_FINISHED, entered.cue)
        val finished = timer.nextPhase()
        assertEquals(TimerStatus.FINISHED, finished.view.status)
        assertNull(finished.cue)
    }

    @Test fun mergedFinalSessionCannotBeRewrittenAfterItStarted() {
        val session = approved()
        val boundary = threshold(session)
        var now = 0L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start(boundary)
        val ending = timer.activePhasesSnapshot().last { it.kind == PhaseKind.POSE }
        now = (ending.startSeconds + 1).toLong() * 1_000L
        assertTrue(runCatching { timer.restartCurrentSegmentKeepingEndTime() }.isFailure)
        assertEquals(ending.id, timer.view().phase?.id)
    }
}
