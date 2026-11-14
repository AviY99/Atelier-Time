package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class TimerNotificationStatusTest {
    private val pose = SchedulePhase(
        id = "phase_2",
        kind = PhaseKind.POSE,
        startSeconds = 60,
        endSeconds = 300,
        poseNumber = 2,
    )

    private fun view(
        status: TimerStatus,
        phase: SchedulePhase? = pose,
        phaseSeconds: Int = 90,
        precisionMs: Long? = 89_650L,
    ) = TimerView(
        status = status,
        elapsedSeconds = 210,
        totalSeconds = 3600,
        phase = phase,
        nextPhase = null,
        phaseRemainingSeconds = phaseSeconds,
        totalRemainingSeconds = 3390,
        phaseRemainingMillis = precisionMs,
    )

    @Test fun runningShowsCurrentSessionAndRealCountdownMillis() {
        val snapshot = TimerNotificationFormatter.forActiveClass(
            "Drawing", view(TimerStatus.RUNNING), 3390)
        assertEquals("Atelier Time · Running", snapshot.title)
        assertEquals("Session 2 · 01:30 left", snapshot.summary)
        assertTrue(snapshot.detail.contains("Drawing"))
        assertTrue(snapshot.detail.contains("Class remaining 56:30"))
        assertEquals(89_650L, snapshot.countdownMillis)
    }

    @Test fun pausedAndStoppedKeepReadableTimeButNeverRunningChronometer() {
        for (status in listOf(TimerStatus.PAUSED, TimerStatus.STOPPED)) {
            val snapshot = TimerNotificationFormatter.forActiveClass(
                "Clay", view(status), 90)
            assertTrue(snapshot.title.contains(
                if (status == TimerStatus.PAUSED) "Paused" else "Stopped"))
            assertEquals("Session 2 · 01:30 left", snapshot.summary)
            assertNull(snapshot.countdownMillis)
        }
    }

    @Test fun openingAndSpecialBreakAndSummaryHaveDistinctLabels() {
        val cases = listOf(
            PhaseKind.SHORT_BREAK to "Opening break",
            PhaseKind.SPECIAL_BREAK to "Special break",
            PhaseKind.SUMMARY to "Class summary",
        )
        cases.forEach { (kind, expected) ->
            val phase = pose.copy(
                kind = kind,
                openingGroupId = if (kind == PhaseKind.SHORT_BREAK) "group1" else null,
            )
            val snapshot = TimerNotificationFormatter.forActiveClass(
                "Class", view(TimerStatus.RUNNING, phase), 59)
            assertTrue(snapshot.summary, snapshot.summary.startsWith(expected))
        }
    }

    @Test fun longTimesAndMissingPrecisionUseSafeCountdownFallback() {
        val snapshot = TimerNotificationFormatter.forActiveClass(
            "", view(TimerStatus.RUNNING, phaseSeconds = 3661,
                precisionMs = null), 7201)
        assertEquals("Session 2 · 01:01:01 left", snapshot.summary)
        assertTrue(snapshot.detail.contains("Class remaining 02:00:01"))
        assertEquals(3_661_000L, snapshot.countdownMillis)
    }

    @Test fun finishedAndReadyHaveNoOngoingNotification() {
        for (status in listOf(TimerStatus.READY, TimerStatus.FINISHED)) {
            assertThrows(IllegalArgumentException::class.java) {
                TimerNotificationFormatter.forActiveClass(
                    "Drawing", view(status), 10)
            }
        }
    }
}
