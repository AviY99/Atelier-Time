package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** D-158: user-visible Class labels and two independent status streams. */
class D158BackgroundNotificationsTest {
    private val groups = listOf(
        OpeningPoseGroup("g1", count = 4, poseSeconds = 60, breakAfterSeconds = 300),
        OpeningPoseGroup("g2", count = 2, poseSeconds = 240, breakAfterSeconds = 300),
    )
    private val rules = SessionRules(
        id = "d158-notifications", name = "Portraits",
        totalSeconds = 180 * 60, summarySeconds = 15 * 60,
        openingGroups = groups, poseSeconds = 25 * 60,
    )
    private fun phase(number: Int, group: String?): SchedulePhase =
        SchedulePhase(
            id = "pose-$number", kind = PhaseKind.POSE,
            startSeconds = 0, endSeconds = 240, poseNumber = number,
            poseSource = if (group == null) PoseSource.REGULAR else PoseSource.OPENING,
            openingGroupId = group,
        )

    private fun view(
        status: TimerStatus = TimerStatus.RUNNING,
        current: SchedulePhase? = phase(1, "g1"),
    ) = TimerView(
        status = status, elapsedSeconds = 120, totalSeconds = 3600,
        phase = current, nextPhase = null,
        phaseRemainingSeconds = 75, totalRemainingSeconds = 3480,
        phaseRemainingMillis = 74_100L,
    )

    private fun classStatus(current: SchedulePhase?, state: TimerStatus =
        TimerStatus.RUNNING): TimerNotificationStatus =
        TimerNotificationFormatter.forActiveClass("Portraits", view(state, current),
            3480, rules)

    @Test fun groupedSessionsMatchClassScreenExactly() {
        (1..4).forEach { number ->
            val current = phase(number, "g1")
            assertTrue(classStatus(current).summary.startsWith(
                PhaseDisplayNames.title(current, rules)))
        }
        (5..6).forEach { number ->
            val current = phase(number, "g2")
            assertTrue(classStatus(current).summary.startsWith(
                PhaseDisplayNames.title(current, rules)))
        }
        assertEquals("Group 2 · Session 2/2 · 01:15 left",
            classStatus(phase(6, "g2")).summary)
    }

    @Test fun regularSessionNumbersAreDisplayNumbersNotCumulativePoseNumbers() {
        assertEquals("Session 3 · 01:15 left",
            classStatus(phase(7, null)).summary)
        assertEquals("Session 7 · 01:15 left",
            TimerNotificationFormatter.forActiveClass("No groups",
                view(current = phase(7, null)), 3480,
                rules.copy(openingGroups = emptyList())).summary)
    }

    @Test fun groupedRegularSpecialBreakAndSummaryUseScreenNames() {
        val cases = listOf(
            SchedulePhase("group", PhaseKind.SHORT_BREAK, 0, 300,
                openingGroupId = "g2") to "Group 2 · Break",
            SchedulePhase("regular", PhaseKind.SHORT_BREAK, 0, 300)
                to "Break · regular",
            SchedulePhase("special", PhaseKind.SPECIAL_BREAK, 0, 300)
                to "Break · special",
            SchedulePhase("summary", PhaseKind.SUMMARY, 0, 300)
                to "Class summary",
            SchedulePhase("legacy", PhaseKind.SHORT_BREAK, 0, 300,
                openingGroupId = "unknown") to "Break · opening group",
        )
        for ((current, expected) in cases) {
            assertEquals(expected, PhaseDisplayNames.title(current, rules))
            assertTrue(classStatus(current).summary.startsWith(expected))
        }
    }

    @Test fun pausedClassRetainsGroupButNoRunningChronometer() {
        val snapshot = classStatus(phase(5, "g2"), TimerStatus.PAUSED)
        assertEquals("Atelier Time · Paused", snapshot.title)
        assertEquals("Group 2 · Session 1/2 · 01:15 left", snapshot.summary)
        assertNull(snapshot.countdownMillis)
    }

    @Test fun freeRunningAndPausedAreClearlyLabeledAndDistinct() {
        val running = FreeTimerNotificationFormatter.forActiveFree(
            FreeTimerView(FreeTimerStatus.RUNNING, 180, 125))
        assertEquals("Atelier Time · Free Timer · Running", running.title)
        assertEquals("Free Timer · 02:05 left", running.summary)
        assertEquals(125_000L, running.countdownMillis)
        val paused = FreeTimerNotificationFormatter.forActiveFree(
            FreeTimerView(FreeTimerStatus.PAUSED, 180, 125))
        assertEquals("Atelier Time · Free Timer · Paused", paused.title)
        assertEquals("Free Timer · 02:05 left", paused.summary)
        assertNull(paused.countdownMillis)
    }

    @Test fun freeHourFormatAndInvalidFinishedReadyStates() {
        val hours = FreeTimerNotificationFormatter.forActiveFree(
            FreeTimerView(FreeTimerStatus.RUNNING, 3723, 3661))
        assertEquals("Free Timer · 01:01:01 left", hours.summary)
        for (state in listOf(FreeTimerStatus.READY, FreeTimerStatus.FINISHED)) {
            assertThrows(IllegalArgumentException::class.java) {
                FreeTimerNotificationFormatter.forActiveFree(
                    FreeTimerView(state, 0, 0))
            }
        }
    }

    @Test fun foregroundSlotsRemainIndependentWhenOneFinishesFirst() {
        assertEquals(107, ActiveTimerNotificationSlots.CLASS_ID)
        assertEquals(109, ActiveTimerNotificationSlots.FREE_ID)
        assertNotEquals(ActiveTimerNotificationSlots.CLASS_ID,
            ActiveTimerNotificationSlots.FREE_ID)
        assertNull(ActiveTimerNotificationSlots.foregroundId(false, false))
        assertEquals(ActiveTimerNotificationSlots.FREE_ID,
            ActiveTimerNotificationSlots.foregroundId(false, true))
        assertEquals(ActiveTimerNotificationSlots.CLASS_ID,
            ActiveTimerNotificationSlots.foregroundId(true, false))
        assertEquals(ActiveTimerNotificationSlots.CLASS_ID,
            ActiveTimerNotificationSlots.foregroundId(true, true))
        // When Class completes first, Free becomes the foreground slot.
        assertEquals(ActiveTimerNotificationSlots.FREE_ID,
            ActiveTimerNotificationSlots.foregroundId(false, true))
        // When Free completes first, Class remains the foreground slot.
        assertEquals(ActiveTimerNotificationSlots.CLASS_ID,
            ActiveTimerNotificationSlots.foregroundId(true, false))
    }

    @Test fun classAndFreeRemainIndependentPresentationSnapshots() {
        val classSnapshot = classStatus(phase(3, "g1"))
        val freeSnapshot = FreeTimerNotificationFormatter.forActiveFree(
            FreeTimerView(FreeTimerStatus.RUNNING, 600, 327))
        assertEquals("Group 1 · Session 3/4 · 01:15 left", classSnapshot.summary)
        assertEquals("Free Timer · 05:27 left", freeSnapshot.summary)
        assertEquals(74_100L, classSnapshot.countdownMillis)
        assertEquals(327_000L, freeSnapshot.countdownMillis)
    }
}
