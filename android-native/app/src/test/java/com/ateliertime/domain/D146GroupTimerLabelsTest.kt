package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** D-146: the Timer's current and next labels use this single formatter. */
class D146GroupTimerLabelsTest {
    private val groups = listOf(
        OpeningPoseGroup("intro-a", count = 4, poseSeconds = 60, breakAfterSeconds = 300),
        OpeningPoseGroup("intro-b", count = 2, poseSeconds = 240, breakAfterSeconds = 300),
    )
    private val rules = SessionRules(
        id = "d146-labels", name = "Opening groups",
        totalSeconds = 180 * 60, summarySeconds = 15 * 60,
        openingGroups = groups, poseSeconds = 25 * 60,
    )
    private fun session(number: Int, group: String?) =
        SchedulePhase("pose-$number", PhaseKind.POSE, 0, 60,
            poseNumber = number,
            poseSource = if (group != null) PoseSource.OPENING else PoseSource.REGULAR,
            openingGroupId = group)
    private fun rest(group: String?) = SchedulePhase(
        "rest", PhaseKind.SHORT_BREAK, 0, 300, openingGroupId = group)

    @Test fun openingGroupOneDisplaysExplicitGroupAndFraction() {
        assertEquals("Group 1 · Session 1/4",
            PhaseDisplayNames.title(session(1, "intro-a"), rules))
        assertEquals("Group 1 · Session 4/4",
            PhaseDisplayNames.title(session(4, "intro-a"), rules))
    }

    @Test fun openingGroupTwoResetsFractionButAdvancesGroupNumber() {
        assertEquals("Group 2 · Session 1/2",
            PhaseDisplayNames.title(session(5, "intro-b"), rules))
        assertEquals("Group 2 · Session 2/2",
            PhaseDisplayNames.title(session(6, "intro-b"), rules))
    }

    @Test fun groupBreakNamesIncludeTheMatchingGroupNumber() {
        assertEquals("Group 1 · Break", PhaseDisplayNames.title(rest("intro-a"), rules))
        assertEquals("Group 2 · Break", PhaseDisplayNames.title(rest("intro-b"), rules))
    }

    @Test fun laterOrdinarySessionsKeepExistingContinuousVisibleNumbering() {
        val last = session(7, null)
        assertEquals("Session 3", PhaseDisplayNames.title(last, rules))
        assertEquals(7, last.poseNumber)
        assertEquals("Session 7", PhaseDisplayNames.title(last,
            rules.copy(openingGroups = emptyList())))
    }

    @Test fun unknownLegacyGroupAndRegularRestStillHaveSafeLabels() {
        assertEquals("Break · opening group", PhaseDisplayNames.title(rest("old-id"), rules))
        assertEquals("Break · regular", PhaseDisplayNames.title(rest(null), rules))
    }

    @Test fun summaryAndEndRemainUnchanged() {
        val summary = SchedulePhase("summary", PhaseKind.SUMMARY, 0, 60)
        assertEquals("Class summary", PhaseDisplayNames.title(summary, rules))
        assertEquals("Class end", PhaseDisplayNames.title(null, rules))
    }
}
