package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** No persisted identifiers, class signatures or clock values are modified. */
class D144PhaseDisplayNamesTest {
    private val groups = listOf(
        OpeningPoseGroup("g1", 4, 60, 0),
        OpeningPoseGroup("g2", 2, 240, 300),
    )
    private val rules = SessionRules(
        id = "d144-display", name = "Opening labels",
        totalSeconds = 180 * 60, summarySeconds = 15 * 60,
        openingGroups = groups, poseSeconds = 25 * 60)

    private fun phase(number: Int, source: PoseSource, groupId: String? = null) =
        SchedulePhase("phase_$number", PhaseKind.POSE,
            (number - 1) * 60, number * 60,
            poseNumber = number, poseSource = source, openingGroupId = groupId)

    @Test fun firstOpeningFourSubsessionsShareOneNumber() {
        (1..4).forEach { sub ->
            assertEquals("Group 1 · Session $sub/4",
                PhaseDisplayNames.title(phase(sub, PoseSource.OPENING, "g1"), rules))
        }
    }

    @Test fun nextOpeningGroupGetsItsOwnNumberAndFraction() {
        assertEquals("Group 2 · Session 1/2",
            PhaseDisplayNames.title(phase(5, PoseSource.OPENING, "g2"), rules))
        assertEquals("Group 2 · Session 2/2",
            PhaseDisplayNames.title(phase(6, PoseSource.OPENING, "g2"), rules))
    }

    @Test fun regularSessionsFollowGroupNumberingWithoutRenumberingStoredPhases() {
        val regular = phase(7, PoseSource.REGULAR)
        assertEquals("Session 3", PhaseDisplayNames.title(regular, rules))
        assertEquals(7, regular.poseNumber)
        assertEquals("Session 7", PhaseDisplayNames.title(regular,
            rules.copy(openingGroups = emptyList())))
    }

    @Test fun breakAndSummaryNamesPreserved() {
        val rest = SchedulePhase("r", PhaseKind.SHORT_BREAK, 0, 300,
            openingGroupId = "g1")
        assertEquals("Group 1 · Break", PhaseDisplayNames.title(rest, rules))
        assertEquals("Group 2 · Break", PhaseDisplayNames.title(
            rest.copy(openingGroupId = "g2"), rules))
        assertEquals("Break · opening group", PhaseDisplayNames.title(
            rest.copy(openingGroupId = "legacy-unknown"), rules))
        assertEquals("Class end", PhaseDisplayNames.title(null, rules))
    }
}
