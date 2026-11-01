package com.ateliertime.android

import com.ateliertime.domain.*
import org.junit.Assert.*
import org.junit.Test

/** D-143: Reset Editor must return to the *original* fresh, unsaved Editor schema. */
class EditorResetD143Test {
    @Test fun originalStartingValuesAreIdenticalToInitialEditorAndReset() {
        val initial = freshEditorInput()
        val reset = freshEditorInput()
        assertEquals(initial, reset)
        assertEquals("Atelier class", reset.name)
        assertEquals("180", reset.totalMinutes)
        assertEquals("15", reset.summaryMinutes)
        assertEquals("25", reset.poseMinutes)
        assertEquals("5", reset.normalBreakMinutes)
        assertEquals("15", reset.specialBreakMinutes)
        assertEquals("1", reset.specialCount)
        assertTrue(reset.opening.isEmpty())
        assertEquals(CycleStart.POSE_FIRST, reset.cycleStart)
        assertTrue(reset.automaticPlacement)
        assertEquals("3", reset.manualOrdinals)
        assertEquals(0, reset.tailExtensionMinutes)
    }

    @Test fun freshStartClearsFormerTailChoiceAndOpeningGroupsWithoutChangingDefaultSchedule() {
        val modified = freshEditorInput().copy(
            name = "User's adjusted Class",
            summaryMinutes = "16",
            opening = listOf(OpeningDraft(3, "4", "1", "6")),
            automaticPlacement = false,
            cycleStart = CycleStart.BREAK_FIRST,
            tailExtensionMinutes = 1,
        )
        assertNotEquals(modified, freshEditorInput())

        val reset = freshEditorInput()
        val rules = reset.toRules()
        assertEquals(180 * SECONDS_PER_MINUTE, rules.totalSeconds)
        assertEquals(15 * SECONDS_PER_MINUTE, rules.summarySeconds)
        assertEquals(0, rules.tailPoseExtensionSeconds)
        assertEquals(CycleStart.POSE_FIRST, rules.cycleStart)
        assertTrue(rules.placement.automatic)
        assertTrue(rules.openingGroups.isEmpty())
        val schedule = ScheduleEngine().generate(rules)
        assertFalse(schedule.issues.toString(), schedule.hasBlockingIssues)
    }
}
