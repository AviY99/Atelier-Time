package com.ateliertime.android

import com.ateliertime.domain.*
import org.junit.Assert.*
import org.junit.Test

class EditorInputTest {
    private fun defaults() = EditorInput(
        name = " Drawing session ", totalMinutes = "180", summaryMinutes = "15",
        poseMinutes = "25", normalBreakMinutes = "5",
        specialBreakMinutes = "15", specialCount = "1",
        opening = emptyList(), cycleStart = CycleStart.POSE_FIRST,
        automaticPlacement = true, manualOrdinals = "3",
    )

    @Test fun exactMinuteFields() {
        val rules = defaults().toRules()
        assertEquals("Drawing session", rules.name)
        assertEquals(180 * 60, rules.totalSeconds)
        assertEquals(15 * 60, rules.summarySeconds)
        assertEquals(25 * 60, rules.poseSeconds)
        assertEquals(1, rules.specialBreakCount)
        assertTrue(rules.placement.automatic)
    }

    @Test fun openingGroupsAreInTheirEnteredOrder() {
        val rules = defaults().copy(opening = listOf(
            OpeningDraft(7, "4", "1"),
            OpeningDraft(8, "4", "4"),
        )).toRules()
        assertEquals(listOf("opening_7", "opening_8"), rules.openingGroups.map { it.id })
        assertEquals(listOf(60, 240), rules.openingGroups.map { it.poseSeconds })
    }

    @Test fun manualOrdinalsOnlyApplyWhenRequested() {
        val r = defaults().copy(
            automaticPlacement = false,
            specialCount = "0",
            manualOrdinals = "old invalid text",
        ).toRules()
        assertTrue(r.placement.automatic)
        val other = defaults().copy(
            automaticPlacement = false,
            specialCount = "2",
            manualOrdinals = "4, 2",
        ).toRules()
        assertEquals(listOf(4, 2), other.placement.breakOrdinals)
    }

    @Test fun malformedNumbersNeverBecomeDefaults() {
        try {
            defaults().copy(totalMinutes = "invalid").toRules()
            fail("Malformed total should be rejected")
        } catch (_: IllegalArgumentException) { }
        try {
            defaults().copy(name = " ").toRules()
            fail("Session name must be present")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun zeroConfiguredBreakReachesBlockingEngineValidation() {
        val rules = defaults().copy(normalBreakMinutes = "0").toRules()
        assertTrue(ScheduleEngine().generate(rules).hasBlockingIssues)
    }
    @Test fun hugeMinuteInputsCannotOverflowIntoDifferentSchedule() {
        try {
            defaults().copy(totalMinutes = "2147483647").toRules()
            fail("Minute-to-second overflow must not be accepted")
        } catch (_: IllegalArgumentException) { }
    }

}
