package com.ateliertime.domain

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Test

/**
 * D-114 Stage 2 deterministic clock-boundary regression tests.
 * No Android device, persisted preferences, system clock changes or sleeping.
 * UI drag/tap behavior is covered separately in RunEndApprovalUiTest.
 */
class RunEndStage2BoundaryTest {
    private fun minutes(n: Int) = n * 60
    private fun approved(): ApprovedSession {
        val engine = ScheduleEngine()
        return engine.approve(engine.generate(SessionRules(
            id = "stage2-boundary", name = "Stage 2 Clock",
            totalSeconds = minutes(180),
            summarySeconds = minutes(15),
            poseSeconds = minutes(25),
            specialBreakCount = 1,
        ), FinalPoseChoice.remainder()))
    }

    @Test fun liveSuggestedFinishFollowsEveryWallClockTickRatherThanInitialRunTap() {
        val saved = approved()
        val original = saved.phases.toList()
        val now = 1_000_123L
        val moved = now + 61_234L
        val first = RunEndTimePlanner.suggestedEndEpochMs(saved, now)
        val second = RunEndTimePlanner.suggestedEndEpochMs(saved, moved)
        assertEquals(61_234L, second - first)
        assertEquals(0, RunEndTimePlanner.plan(saved, moved, second).correctionSeconds)
        assertTrue(RunEndTimePlanner.plan(saved, moved, first).correctionSeconds > 0)
        assertEquals(original, saved.phases)
    }

    @Test fun earliestAllowedSecondIsAcceptedButEarlierSecondIsRejected() {
        val saved = approved()
        val start = 456L
        val earliest = start + minutes(152) * 1_000L
        val edge = RunEndTimePlanner.plan(saved, start, earliest)
        assertEquals(minutes(1), edge.adjusted.firstFinalPoseSeconds)
        assertEquals(minutes(1), edge.adjusted.lastFinalPoseSeconds)
        for (invalid in listOf(earliest - 1L, earliest - 1_000L)) {
            val error = runCatching { RunEndTimePlanner.plan(saved, start, invalid) }
                .exceptionOrNull()
            assertTrue("The first red second must be rejected", error is IllegalArgumentException)
            assertTrue(error!!.message!!.contains("last two Sessions"))
        }
        assertEquals(minutes(180), saved.rules.totalSeconds)
    }

    @Test fun plannerRechecksAbsoluteTargetAtActualStartTimeNotPreviewTime() {
        val saved = approved()
        val original = saved.phases.toList()
        val previewNow = 99_999L
        val chosen = previewNow + minutes(152) * 1_000L
        assertEquals(minutes(152),
            RunEndTimePlanner.plan(saved, previewNow, chosen).adjustedDurationSeconds)
        val afterDelay = runCatching {
            RunEndTimePlanner.plan(saved, previewNow + 1_000L, chosen)
        }.exceptionOrNull()
        assertTrue("An expired 1-minute final pair must not launch", afterDelay is IllegalArgumentException)
        assertEquals(original, saved.phases)
    }

    @Test fun maximumLateBoundaryDoesNotAllowOvernightExtension() {
        val saved = approved()
        val original = saved.phases.toList()
        val now = 2_345L
        val lastAllowed = now + minutes(360) * 1_000L
        assertEquals(minutes(360),
            RunEndTimePlanner.plan(saved, now, lastAllowed).adjustedDurationSeconds)
        val error = runCatching {
            RunEndTimePlanner.plan(saved, now, lastAllowed + 1_000L)
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertTrue(error!!.message!!.contains("too late"))
        assertEquals(original, saved.phases)
    }

    @Test fun neighboringMinuteMayBeGreenWhilePreviousHourIsRed() {
        val saved = approved()
        val utc = TimeZone.getTimeZone("UTC")
        val now = GregorianCalendar(utc).apply {
            set(2026, Calendar.OCTOBER, 25, 12, 15, 20)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val default = RunEndTimePlanner.suggestedEndEpochMs(saved, now)
        assertEquals(0, RunEndTimePlanner.plan(saved, now, default).correctionSeconds)
        val previousHour = RunEndTimePlanner.nextOccurrence("14:15", now, utc)
        val previousMinute = RunEndTimePlanner.nextOccurrence("15:14", now, utc)
        val nextMinute = RunEndTimePlanner.nextOccurrence("15:16", now, utc)
        assertTrue(runCatching {
            RunEndTimePlanner.plan(saved, now, previousHour)
        }.isFailure)
        assertTrue(RunEndTimePlanner.plan(saved, now, previousMinute).correctionSeconds > 0)
        assertTrue(RunEndTimePlanner.plan(saved, now, nextMinute).correctionSeconds < 0)
    }

    @Test fun hourAndMinuteRedBoundaryAtMidnightDoesNotMutateSavedTimeline() {
        val saved = approved()
        val original = saved.phases.toList()
        val utc = TimeZone.getTimeZone("UTC")
        val now = GregorianCalendar(utc).apply {
            set(2026, Calendar.OCTOBER, 25, 21, 42, 20)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        // Calculated finish is 00:42:20 tomorrow; prior hour (23:42)
        // is too early but next minute (00:43) remains feasible.
        val red = RunEndTimePlanner.nextOccurrence("23:42", now, utc)
        val green = RunEndTimePlanner.nextOccurrence("00:43", now, utc)
        assertTrue(runCatching { RunEndTimePlanner.plan(saved, now, red) }.isFailure)
        assertTrue(RunEndTimePlanner.plan(saved, now, green).correctionSeconds < 0)
        assertEquals(original, saved.phases)
    }
}
