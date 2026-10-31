package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class RuntimeTimelineAndExtraRegularTest {
    private val engine = ScheduleEngine()
    private fun m(value: Int) = value * 60
    private fun session() = engine.approve(engine.generate(SessionRules(
        id = "timeline-d116", name = "Timeline QA", totalSeconds = m(180),
        summarySeconds = m(15), poseSeconds = m(25), shortBreakSeconds = m(5),
        specialBreakCount = 1,
    ), FinalPoseChoice.remainder()))

    @Test fun extraRegularRequiresStrictlyMoreThanTwoRegularSessionsPlusBreak() {
        val session = session()
        val baseline = session.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2).sumOf { it.durationSeconds }
        val threshold = m(2 * 25 + 5)
        val atEdge = FinalPairCompensation.adjust(session, baseline - threshold,
            allowExtraRegular = true)
        assertEquals(0, atEdge.phases.count { it.id.startsWith("runtime-added-pose-") })
        val oneSecondOver = FinalPairCompensation.adjust(
            session, baseline - threshold - 1, allowExtraRegular = true)
        assertEquals(1, oneSecondOver.phases.count {
            it.id.startsWith("runtime-added-pose-") })

        val beyond = FinalPairCompensation.adjust(session, baseline - threshold - m(1),
            allowExtraRegular = true)
        assertEquals(1, beyond.phases.count { it.id.startsWith("runtime-added-pose-") })
        assertEquals(m(25), beyond.phases.first {
            it.id == "runtime-added-pose-1" }.durationSeconds)
        assertEquals(m(5), beyond.phases.first {
            it.id == "runtime-added-break-1" }.durationSeconds)
        val finalTwo = beyond.phases.filter {
            it.id in session.phases.filter { original ->
                original.kind == PhaseKind.POSE && original.poseSource == PoseSource.REGULAR
            }.takeLast(2).map { original -> original.id }
        }
        assertEquals(m(26), finalTwo.sumOf { it.durationSeconds })
        assertEquals(session.rules.summarySeconds, beyond.phases.last().durationSeconds)
        assertEquals(session.rules.totalSeconds - (baseline - threshold - m(1)),
            beyond.totalSeconds)
        assertEquals(m(180), session.phases.last().endSeconds)
        beyond.phases.zipWithNext().forEach { (a, b) ->
            assertEquals(a.endSeconds, b.startSeconds)
        }
    }

    @Test fun repeatedCreditsCreateMultipleRegularBlocksWithoutTouchingSavedClass() {
        val session = session()
        val original = session.phases.toList()
        val baseline = original.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2).sumOf { it.durationSeconds }
        val credit = m(56)
        val result = FinalPairCompensation.adjust(session, -credit,
            allowExtraRegular = true)
        assertEquals(2, result.phases.count { it.id.startsWith("runtime-added-pose-") })
        assertEquals(2, result.phases.count { it.id.startsWith("runtime-added-break-") })
        assertEquals(baseline + credit - 2 * m(30),
            result.firstFinalPoseSeconds + result.lastFinalPoseSeconds)
        assertEquals(original, session.phases)
    }

    @Test fun automaticStartOrFinishCorrectionNeverAddsNewSessionsWithoutNext() {
        val session = session()
        val adjusted = FinalPairCompensation.adjust(session, -m(56))
        assertFalse(adjusted.phases.any { it.id.startsWith("runtime-added-") })
    }

    @Test fun nextAfterOneMinuteSealsExactlyOneMinuteOfOldSessionInChart() {
        var nowMs = 0L
        val session = session()
        val timer = MonotonicSessionTimer(session) { nowMs }
        timer.start()
        nowMs = 60_000L
        timer.nextPhase()
        val projection = timer.runtimeTimeline()
        val history = projection.segments.filter { it.observed }
        assertEquals(1, history.size)
        assertEquals(m(1), history.single().durationSeconds)
        assertEquals(PhaseKind.POSE, history.single().kind)
        assertEquals(m(180), projection.totalSeconds)
        assertEquals(m(1), projection.elapsedSeconds)
    }

    @Test fun restartShowsOldMinuteAndAFullNewAttemptWithoutInflatingClassTime() {
        var nowMs = 0L
        val session = session()
        val timer = MonotonicSessionTimer(session) { nowMs }
        timer.start()
        nowMs = 60_000L
        timer.restartCurrentSegmentKeepingEndTime()
        val projection = timer.runtimeTimeline()
        assertEquals(m(1), projection.elapsedSeconds)
        assertEquals(m(1), projection.segments.first().durationSeconds)
        assertTrue(projection.segments.first().observed)
        assertEquals(m(25), projection.segments[1].durationSeconds)
        assertFalse(projection.segments[1].observed)
        assertEquals(m(180), projection.totalSeconds)
    }

    @Test fun nextCanAddRegularBlockWithoutBreakingActualTimeline() {
        var nowMs = 0L
        val timer = MonotonicSessionTimer(session()) { nowMs }
        timer.start()
        // 25m Session skipped, exactly at strict boundary; then 5m Break
        // skipped, crossing the threshold and inserting one regular block.
        timer.nextPhase()
        assertEquals(0, timer.activePhasesSnapshot().count {
            it.id.startsWith("runtime-added-pose-") })
        timer.nextPhase()
        assertEquals(1, timer.activePhasesSnapshot().count {
            it.id.startsWith("runtime-added-pose-") })
        assertEquals(m(180), timer.runtimeTimeline().totalSeconds)
        assertEquals(0, timer.runtimeTimeline().elapsedSeconds)
    }

    @Test fun nextAndRestartInsideAddedBlockNeverRewriteItsActualHistory() {
        var nowMs = 0L
        val timer = MonotonicSessionTimer(session()) { nowMs }
        timer.start()
        timer.nextPhase()
        timer.nextPhase()
        var guard = 0
        while (timer.view().phase?.id != "runtime-added-pose-1") {
            val seconds = timer.view().phaseRemainingSeconds
            assertTrue("Added block should be reachable", ++guard < 30)
            nowMs += seconds.toLong() * 1_000L
            timer.update()
        }
        nowMs += 60_000L
        val afterNext = timer.nextPhase()
        assertEquals("runtime-added-break-1", afterNext.view.phase?.id)
        val completedAdded = timer.runtimeTimeline().segments.last {
            it.id == "runtime-added-pose-1" && it.observed
        }
        assertEquals(m(1), completedAdded.durationSeconds)
        nowMs += 60_000L
        timer.restartCurrentSegmentKeepingEndTime()
        val timeline = timer.runtimeTimeline()
        assertEquals(m(1), timeline.segments.last {
            it.id == "runtime-added-break-1" && it.observed
        }.durationSeconds)
        assertEquals("runtime-added-break-1", timer.view().phase?.id)
        assertEquals(m(180), timeline.totalSeconds)
    }

    @Test fun pausedTimeAppearsAsHistoryAndDoesNotMasqueradeAsWork() {
        var nowMs = 0L
        val timer = MonotonicSessionTimer(session()) { nowMs }
        timer.start()
        nowMs = 60_000L
        timer.pause()
        nowMs = 180_000L
        timer.nextPhase()
        val projection = timer.runtimeTimeline()
        assertEquals(listOf(PhaseKind.POSE, null), projection.segments
            .filter { it.observed }.map { it.kind })
        assertEquals(listOf(m(1), m(2)), projection.segments
            .filter { it.observed }.map { it.durationSeconds })
        assertEquals(m(3), projection.elapsedSeconds)
        assertEquals(m(180), projection.totalSeconds)
    }

    @Test fun automaticBoundarySealsCompletedPoseAtItsRealLength() {
        var nowMs = 0L
        val timer = MonotonicSessionTimer(session()) { nowMs }
        timer.start()
        nowMs = m(25).toLong() * 1_000L
        timer.update()
        val projection = timer.runtimeTimeline()
        assertEquals(m(25), projection.segments.first().durationSeconds)
        assertTrue(projection.segments.first().observed)
        assertEquals(PhaseKind.POSE, projection.segments.first().kind)
        assertEquals(m(180), projection.totalSeconds)
    }
}
