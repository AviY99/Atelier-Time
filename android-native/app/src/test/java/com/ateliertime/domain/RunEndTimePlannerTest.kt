package com.ateliertime.domain

import java.util.GregorianCalendar
import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Test

class RunEndTimePlannerTest {
    private val engine = ScheduleEngine()
    private fun mins(value: Int) = value * SECONDS_PER_MINUTE

    private fun approved(choice: FinalPoseChoice = FinalPoseChoice.remainder()): ApprovedSession {
        val rules = SessionRules(
            id = "run-end", name = "Finish-clock test", totalSeconds = mins(180),
            summarySeconds = mins(15), poseSeconds = mins(25), specialBreakCount = 1,
        )
        return engine.approve(engine.generate(rules, choice))
    }

    @Test fun suggestedEndUsesExactApprovedDuration() {
        val session = approved()
        assertEquals(10_800_123L, RunEndTimePlanner.suggestedEndEpochMs(session, 123L))
    }

    @Test fun defaultConfirmationDoesNotMutateApprovedClass() {
        val session = approved()
        val original = session.phases.toList()
        val result = RunEndTimePlanner.plan(
            session, 1_000_000L, RunEndTimePlanner.suggestedEndEpochMs(session, 1_000_000L)
        )
        assertEquals(0, result.correctionSeconds)
        assertEquals(session.rules.totalSeconds, result.adjustedDurationSeconds)
        assertEquals(original, result.adjusted.phases)
        assertEquals(original, session.phases)
    }

    @Test fun earlierFinishShortensOnlyFinalPair() {
        val session = approved()
        val start = 1_000_000L
        val end = start + mins(177) * 1_000L
        val plan = RunEndTimePlanner.plan(session, start, end)
        assertEquals(mins(3), plan.correctionSeconds)
        assertEquals(mins(177), plan.adjustedDurationSeconds)
        assertEquals(mins(25), plan.adjusted.firstFinalPoseSeconds)
        assertEquals(mins(2), plan.adjusted.lastFinalPoseSeconds)
        val firstPair = session.phases.indexOfFirst { it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR }
        val lastTwoIndex = session.phases.indices.filter {
            session.phases[it].kind == PhaseKind.POSE && session.phases[it].poseSource == PoseSource.REGULAR
        }.takeLast(2).first()
        assertTrue(lastTwoIndex >= firstPair)
        assertEquals(session.phases.take(lastTwoIndex), plan.adjusted.phases.take(lastTwoIndex))
        assertEquals(session.phases.last().durationSeconds, plan.adjusted.phases.last().durationSeconds)
    }

    @Test fun laterFinishExtendsOnlyFinalPair() {
        val session = approved()
        val now = 1_000_000L
        val plan = RunEndTimePlanner.plan(session, now, now + mins(186) * 1_000L)
        assertEquals(-mins(6), plan.correctionSeconds)
        assertEquals(mins(186), plan.adjusted.totalSeconds)
        assertEquals(mins(25), plan.adjusted.firstFinalPoseSeconds)
        assertEquals(mins(11), plan.adjusted.lastFinalPoseSeconds)
    }

    @Test fun evenModeIsRespectedWhenTargetEarlierAndLater() {
        val session = approved(FinalPoseChoice.even())
        val before = RunEndTimePlanner.plan(session, 0L, mins(174) * 1_000L)
        val after = RunEndTimePlanner.plan(session, 0L, mins(186) * 1_000L)
        assertEquals(before.adjusted.firstFinalPoseSeconds, before.adjusted.lastFinalPoseSeconds)
        assertEquals(after.adjusted.firstFinalPoseSeconds, after.adjusted.lastFinalPoseSeconds)
    }

    @Test fun customSplitPreservesRatio() {
        val session = approved(FinalPoseChoice.custom(mins(18)))
        val later = RunEndTimePlanner.plan(session, 0L, mins(185) * 1_000L)
        assertEquals(mins(21), later.adjusted.firstFinalPoseSeconds)
        assertEquals(mins(14), later.adjusted.lastFinalPoseSeconds)
    }

    @Test fun impossibleEarlyFinishIsRejectedWithoutEditingSession() {
        val session = approved()
        val saved = session.phases.toList()
        try {
            RunEndTimePlanner.plan(session, 0L, mins(150) * 1_000L)
            fail("A final pair below two minutes must never be accepted")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message!!.contains("last two Sessions"))
        }
        assertEquals(saved, session.phases)
    }

    @Test fun pastAndExactPresentTargetsRejected() {
        val session = approved()
        listOf(999L, 1000L).forEach { invalid ->
            try {
                RunEndTimePlanner.plan(session, 1000L, invalid)
                fail("Finish must be in future")
            } catch (expected: IllegalArgumentException) {
                assertTrue(expected.message!!.contains("future"))
            }
        }
    }

    @Test fun subSecondTargetRoundsUpConservatively() {
        val session = approved()
        val p = RunEndTimePlanner.plan(session, 123L, 123L + mins(180) * 1000L - 1L)
        assertEquals(0, p.correctionSeconds)
        assertEquals(mins(180), p.adjustedDurationSeconds)
    }

    @Test fun clockTimeTodayAndTomorrowResolvedInUtc() {
        val utc = TimeZone.getTimeZone("UTC")
        val now = GregorianCalendar(utc).apply {
            set(2026, 9, 25, 18, 30, 10)
            set(GregorianCalendar.MILLISECOND, 0)
        }.timeInMillis
        val today = RunEndTimePlanner.nextOccurrence("21:30", now, utc)
        val tomorrow = RunEndTimePlanner.nextOccurrence("17:45", now, utc)
        assertEquals(3L * 3_600_000L - 10_000L, today - now)
        assertEquals(23L * 3_600_000L + 15L * 60_000L - 10_000L, tomorrow - now)
    }

    @Test fun midnightClockTimeUsesNextDate() {
        val utc = TimeZone.getTimeZone("UTC")
        val now = GregorianCalendar(utc).apply {
            set(2026, 9, 25, 23, 50, 0)
            set(GregorianCalendar.MILLISECOND, 0)
        }.timeInMillis
        val midnight = RunEndTimePlanner.nextOccurrence("00:15", now, utc)
        assertEquals(25L * 60_000L, midnight - now)
    }

    @Test fun originalFinishOfLegacySingleSessionStillRunsUnchanged() {
        val rules = SessionRules(
            id = "one-pose", name = "Short legacy Class",
            totalSeconds = mins(25), summarySeconds = 0,
            poseSeconds = mins(25), specialBreakCount = 0,
        )
        val session = engine.approve(engine.generate(rules))
        val suggested = RunEndTimePlanner.plan(session, 0L, mins(25) * 1_000L)
        assertEquals(0, suggested.correctionSeconds)
        assertEquals(session.phases, suggested.adjusted.phases)
        try {
            RunEndTimePlanner.plan(session, 0L, mins(26) * 1_000L)
            fail("A Class without an adjustable pair cannot change its finish")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun invalidClockInputsProvideCorrectableError() {
        listOf("", "24:00", "10:60", "4", "noon", "-1:20", "17:5").forEach { input ->
            try {
                RunEndTimePlanner.nextOccurrence(input, 0L, TimeZone.getTimeZone("UTC"))
                fail("Should reject invalid HH:mm: " + input)
            } catch (expected: IllegalArgumentException) {
                assertTrue(expected.message!!.contains("HH:mm"))
            }
        }
    }

    @Test fun endTargetCanCrossMidnightForLongClass() {
        val session = approved()
        val utc = TimeZone.getTimeZone("UTC")
        val now = GregorianCalendar(utc).apply {
            set(2026, 9, 25, 23, 20, 0)
            set(GregorianCalendar.MILLISECOND, 0)
        }.timeInMillis
        val end = RunEndTimePlanner.nextOccurrence("02:20", now, utc)
        assertEquals(0, RunEndTimePlanner.plan(session, now, end).correctionSeconds)
    }
}
