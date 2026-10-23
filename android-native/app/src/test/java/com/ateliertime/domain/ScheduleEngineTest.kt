package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class ScheduleEngineTest {
    private val engine = ScheduleEngine()
    private fun m(n: Int) = n * SECONDS_PER_MINUTE
    private fun b() = SessionRules(
        "B", "Example B", m(180), m(15), poseSeconds = m(25),
        specialBreakCount = 1,
    )
    private fun a() = SessionRules(
        "A", "Example A", m(180), m(15),
        openingGroups = listOf(
            OpeningPoseGroup("one", 4, m(1)),
            OpeningPoseGroup("four", 4, m(4)),
        ),
        poseSeconds = m(25),
        specialBreakCount = 1,
        cycleStart = CycleStart.BREAK_FIRST,
    )
    private fun result(rules: SessionRules, choice: FinalPoseChoice? = null) =
        engine.generate(rules, choice)
    private fun spans(c: ScheduleCandidate): List<String> =
        c.phases.map {
            it.kind.name + ":" + it.startSeconds / 60 + "-" + it.endSeconds / 60
        }
    private fun contiguous(c: ScheduleCandidate) {
        assertFalse(c.hasBlockingIssues)
        assertTrue(c.phases.isNotEmpty())
        assertEquals(0, c.phases.first().startSeconds)
        assertEquals(c.rules.totalSeconds, c.phases.last().endSeconds)
        c.phases.forEach { assertTrue(it.durationSeconds > 0) }
        for (i in 1 until c.phases.size) {
            assertEquals(c.phases[i - 1].endSeconds, c.phases[i].startSeconds)
        }
    }

    @Test fun exampleBBaselineExact() {
        val c = result(b(), FinalPoseChoice.remainder())
        contiguous(c)
        assertEquals(listOf(
            "POSE:0-25", "SHORT_BREAK:25-30", "POSE:30-55",
            "SHORT_BREAK:55-60", "POSE:60-85",
            "SPECIAL_BREAK:85-100", "POSE:100-125",
            "SHORT_BREAK:125-130", "POSE:130-155",
            "SHORT_BREAK:155-160", "POSE:160-165",
            "SUMMARY:165-180",
        ), spans(c))
    }

    @Test fun exampleAOpeningAndBreakFirstExact() {
        val c = result(a(), FinalPoseChoice.remainder())
        contiguous(c)
        assertEquals(listOf(
            "POSE:0-1", "POSE:1-2", "POSE:2-3", "POSE:3-4",
            "POSE:4-8", "POSE:8-12", "POSE:12-16", "POSE:16-20",
            "SHORT_BREAK:20-25", "POSE:25-50",
            "SHORT_BREAK:50-55", "POSE:55-80",
            "SPECIAL_BREAK:80-95", "POSE:95-120",
            "SHORT_BREAK:120-125", "POSE:125-150",
            "SHORT_BREAK:150-155", "POSE:155-165",
            "SUMMARY:165-180",
        ), spans(c))
        assertEquals(8, c.phases.count { it.poseSource == PoseSource.OPENING })
    }

    @Test fun requiresExplicitEnding() {
        val c = result(b())
        assertTrue(c.needsFinalPoseChoice)
        assertTrue(c.canSplitFinalTwo)
        assertFalse(c.canApprove)
    }

    @Test fun finalSplitsB() {
        val even = result(b(), FinalPoseChoice.even())
        contiguous(even)
        assertEquals(listOf(
            "POSE:130-145", "SHORT_BREAK:145-150",
            "POSE:150-165", "SUMMARY:165-180",
        ), spans(even).takeLast(4))
        val custom = result(b(), FinalPoseChoice.custom(m(18)))
        contiguous(custom)
        assertEquals(listOf(
            "POSE:130-148", "SHORT_BREAK:148-153",
            "POSE:153-165", "SUMMARY:165-180",
        ), spans(custom).takeLast(4))
    }

    @Test fun finalSplitsA() {
        val even = result(a(), FinalPoseChoice.even())
        contiguous(even)
        assertEquals(listOf(
            "POSE:125-142", "SHORT_BREAK:142-147",
            "POSE:147-165", "SUMMARY:165-180",
        ), spans(even).takeLast(4))
        val custom = result(a(), FinalPoseChoice.custom(m(20)))
        contiguous(custom)
        assertEquals(listOf(
            "POSE:125-145", "SHORT_BREAK:145-150",
            "POSE:150-165", "SUMMARY:165-180",
        ), spans(custom).takeLast(4))
    }

    @Test fun extremeOneMinuteFirstWarns() {
        val c = result(a(), FinalPoseChoice.custom(m(1)))
        contiguous(c)
        assertEquals(listOf(
            "POSE:125-126", "SHORT_BREAK:126-131",
            "POSE:131-165", "SUMMARY:165-180",
        ), spans(c).takeLast(4))
        assertTrue(c.needsSafetyAcknowledgement)
        try {
            engine.approve(c)
            fail("Warning must not be bypassed")
        } catch (_: IllegalStateException) { }
        assertEquals("rest_25_5",
            engine.approve(c, acknowledgeWarnings = true).acknowledgedWarnings.first().code)
    }

    @Test fun extremeOneMinuteLastWarns() {
        val c = result(a(), FinalPoseChoice.custom(m(34)))
        contiguous(c)
        assertEquals(listOf(
            "POSE:125-159", "SHORT_BREAK:159-164",
            "POSE:164-165", "SUMMARY:165-180",
        ), spans(c).takeLast(4))
        assertTrue(c.needsSafetyAcknowledgement)
    }

    @Test fun zeroSessionOrBreakHardBlock() {
        assertTrue(result(b().copy(totalSeconds = 0)).hasBlockingIssues)
        assertTrue(result(b().copy(shortBreakSeconds = 0)).hasBlockingIssues)
        assertTrue(result(b().copy(specialBreakSeconds = 0)).hasBlockingIssues)
    }

    @Test fun zeroSpecialCountAndSummaryAllowed() {
        val c = result(
            b().copy(specialBreakCount = 0, summarySeconds = 0),
            FinalPoseChoice.remainder()
        )
        contiguous(c)
        assertFalse(c.phases.any { it.kind == PhaseKind.SPECIAL_BREAK })
        assertFalse(c.phases.any { it.kind == PhaseKind.SUMMARY })
    }

    @Test fun shortRestDoesNotResetAccumulatedPosing() {
        val c = result(b().copy(
            totalSeconds = m(51), summarySeconds = 0,
            shortBreakSeconds = m(1), specialBreakCount = 0,
        ))
        contiguous(c)
        assertTrue(c.needsSafetyAcknowledgement)
        assertTrue(c.issues.any { it.code == "rest_25_5" })
    }

    @Test fun positiveSpecialShorterThanNormalIsLegal() {
        val c = result(b().copy(
            totalSeconds = m(53), summarySeconds = 0,
            shortBreakSeconds = m(5), specialBreakSeconds = m(3),
        ))
        contiguous(c)
        assertEquals(1, c.phases.count { it.kind == PhaseKind.SPECIAL_BREAK })
        assertTrue(c.needsSafetyAcknowledgement)
    }

    @Test fun manualOrdinalAndDuplicateValidation() {
        val c = result(
            b().copy(placement = SpecialBreakPlacement.manual(listOf(2))),
            FinalPoseChoice.remainder()
        )
        contiguous(c)
        assertEquals(2,
            c.phases.single { it.kind == PhaseKind.SPECIAL_BREAK }.breakOrdinal)
        assertTrue(result(b().copy(
            specialBreakCount = 2,
            placement = SpecialBreakPlacement.manual(listOf(2, 2)),
        )).hasBlockingIssues)
    }

    @Test fun impossibleSummaryAndMissingChoiceCannotApprove() {
        assertTrue(result(b().copy(summarySeconds = m(180))).hasBlockingIssues)
        try {
            engine.approve(result(b()))
            fail("Unresolved choice must block approval")
        } catch (_: IllegalStateException) { }
    }

    @Test fun deterministicTimelineAndIssues() {
        val x = result(a(), FinalPoseChoice.even())
        val y = result(a(), FinalPoseChoice.even())
        contiguous(x)
        assertEquals(x.phases, y.phases)
        assertEquals(x.issues, y.issues)
    }
}
