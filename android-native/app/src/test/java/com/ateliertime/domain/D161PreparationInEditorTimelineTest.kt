package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class D161PreparationInEditorTimelineTest {
    private val engine = ScheduleEngine()
    private fun rules(transition: Int = 5, count: Int = 4) = SessionRules(
        id = "d161", name = "Class",
        totalSeconds = 180 * 60, summarySeconds = 15 * 60,
        openingGroups = listOf(
            OpeningPoseGroup("first", count, 60, breakAfterSeconds = 0),
            OpeningPoseGroup("second", 2, 240, breakAfterSeconds = 300),
        ),
        poseSeconds = 25 * 60,
        specialBreakCount = 1,
        cycleStart = CycleStart.BREAK_FIRST,
        preparationSeconds = transition,
    )

    @Test fun preparationExistsInApprovedEditorPlanNotAddedAtRuntime() {
        val approved = engine.generate(rules(), FinalPoseChoice.remainder())
        assertFalse(approved.issues.toString(), approved.hasBlockingIssues)
        val phases = approved.phases
        val prep = phases.filter { it.kind == PhaseKind.PREPARATION }
        assertEquals(4, prep.size) // 3 within first, 1 within second
        assertTrue(prep.all { it.durationSeconds == 5 && it.breakOrdinal == null })
        assertEquals(180 * 60, phases.last().endSeconds)
        assertEquals(phases.sumOf { it.durationSeconds }, phases.last().endSeconds)
        assertEquals(0, phases.first().startSeconds)
        phases.zipWithNext { a, b -> assertEquals(a.endSeconds, b.startSeconds) }
        val opening = phases.filter { it.poseSource == PoseSource.OPENING }
        assertEquals(6, opening.size)
        assertEquals(listOf(60, 60, 60, 60, 240, 240),
            opening.map { it.durationSeconds })
        val session = engine.approve(approved, acknowledgeWarnings = true)
        val timer = MonotonicSessionTimer(session) { 0L }
        assertEquals(4, timer.activePhasesSnapshot().count {
            it.kind == PhaseKind.PREPARATION
        })
    }

    @Test fun originalLegacyGroupScheduleIsUnmodifiedWhenTransitionDisabled() {
        val old = engine.generate(rules(transition = 0), FinalPoseChoice.remainder())
        assertFalse(old.hasBlockingIssues)
        assertTrue(old.phases.none { it.kind == PhaseKind.PREPARATION })
        assertEquals(180 * 60, old.phases.last().endSeconds)
    }

    @Test fun preparationNeverCountsAsOrdinaryBreakOrSpecialReplacement() {
        val preview = engine.generate(rules(), FinalPoseChoice.remainder())
        assertFalse(preview.hasBlockingIssues)
        val prep = preview.phases.filter { it.kind == PhaseKind.PREPARATION }
        assertTrue(prep.all { it.breakOrdinal == null })
        assertEquals(1, preview.phases.count { it.kind == PhaseKind.SPECIAL_BREAK })
        assertEquals(0, preview.phases.filter {
            it.kind == PhaseKind.PREPARATION
        }.sumOf { it.durationSeconds } % 5)
    }

    @Test fun editorRejectsInsufficientRoomInsteadOfMovingClassEnd() {
        val tooShort = rules(count = 5).copy(totalSeconds = 5 * 60,
            summarySeconds = 0)
        val preview = engine.generate(tooShort)
        assertTrue(preview.hasBlockingIssues)
    }

    @Test fun preparationTitleIsDistinctFromNormalGroupRest() {
        val phase = SchedulePhase("prep", PhaseKind.PREPARATION, 60, 65,
            openingGroupId = "first")
        assertEquals("Preparation · 5 seconds", PhaseDisplayNames.title(phase, rules()))
    }
}
