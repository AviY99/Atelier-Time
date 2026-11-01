package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** D-142: regression tests for an explicit, never silent, terminal-break resolution. */
class TailBreakRecoveryD142Test {
    private val engine = ScheduleEngine()
    private fun minutes(value: Int) = value * SECONDS_PER_MINUTE

    private fun reported(totalMinutes: Int = 180) = SessionRules(
        id = "testGoogle",
        name = "testGoogle",
        totalSeconds = minutes(totalMinutes),
        summarySeconds = minutes(15),
        openingGroups = listOf(
            OpeningPoseGroup("opening_1", 4, minutes(1), minutes(5)),
        ),
        poseSeconds = minutes(25),
        shortBreakSeconds = minutes(5),
        specialBreakSeconds = minutes(15),
        specialBreakCount = 1,
        placement = SpecialBreakPlacement.manual(listOf(3)),
    )

    private fun assertFullTimeline(candidate: ScheduleCandidate) {
        assertFalse(candidate.issues.toString(), candidate.hasBlockingIssues)
        assertTrue(candidate.phases.isNotEmpty())
        assertEquals(0, candidate.phases.first().startSeconds)
        assertEquals(candidate.rules.totalSeconds, candidate.phases.last().endSeconds)
        for (index in candidate.phases.indices) {
            assertTrue(candidate.phases[index].durationSeconds > 0)
            if (index > 0) {
                assertEquals(candidate.phases[index - 1].endSeconds,
                    candidate.phases[index].startSeconds)
            }
        }
    }

    @Test fun exactReportedCaseIsOneMinuteNotAnAutomatic25MinuteBan() {
        val original = engine.generate(reported())
        assertTrue(original.hasBlockingIssues)
        assertEquals("unbuildable_timeline", original.issues.first().code)
        val tail = original.tailBreakRemainder!!
        assertEquals(minutes(1), tail.remainingSeconds)
        assertEquals(minutes(5), tail.attemptedBreakSeconds)
        assertEquals(5, tail.breakOrdinal)
        assertFalse(tail.attemptedSpecial)
        assertEquals(minutes(164), original.phases.last().endSeconds)
    }

    @Test fun allOneToFiveMinuteTerminalRemaindersAndSummaryOrPoseFixes() {
        for (remainder in 1..5) {
            val original = reported(179 + remainder)
            val blocked = engine.generate(original)
            assertEquals(minutes(remainder), blocked.tailBreakRemainder?.remainingSeconds)
            assertTrue(blocked.hasBlockingIssues)

            val extend = engine.generate(original.copy(
                tailPoseExtensionSeconds = minutes(remainder)
            ))
            assertFullTimeline(extend)
            assertEquals(minutes(25 + remainder),
                extend.phases.last { it.kind == PhaseKind.POSE }.durationSeconds)
            assertEquals(1, extend.phases.count { it.kind == PhaseKind.SPECIAL_BREAK })
            assertEquals(3, extend.phases.single {
                it.kind == PhaseKind.SPECIAL_BREAK
            }.breakOrdinal)
            assertTrue("26+ minutes triggers a 25/5 warning",
                extend.issues.any { it.code == "rest_25_5" })
            assertTrue(extend.needsSafetyAcknowledgement)

            val summary = engine.generate(original.copy(
                summarySeconds = minutes(15 + remainder),
            ))
            assertFullTimeline(summary)
            assertEquals(minutes(15 + remainder),
                summary.phases.last().durationSeconds)
            assertEquals(PhaseKind.SUMMARY, summary.phases.last().kind)
            assertEquals(1, summary.phases.count { it.kind == PhaseKind.SPECIAL_BREAK })
        }
    }

    @Test fun fivePlusOneMinuteBoundaryIsValidWithoutAnyOverride() {
        val full = engine.generate(reported(185), FinalPoseChoice.remainder())
        assertFullTimeline(full)
        assertNull(full.tailBreakRemainder)
        assertEquals(minutes(1),
            full.phases.last { it.kind == PhaseKind.POSE }.durationSeconds)
    }

    @Test fun onlyTheExactApprovedTailCanBeExtended() {
        val baseline = reported()
        val incomplete = engine.generate(baseline.copy(
            tailPoseExtensionSeconds = minutes(2),
        ))
        assertTrue(incomplete.hasBlockingIssues)
        assertEquals(minutes(1), incomplete.tailBreakRemainder?.remainingSeconds)
        val unrelatedValid = engine.generate(reported(185).copy(
            tailPoseExtensionSeconds = minutes(1)
        ))
        assertTrue(unrelatedValid.hasBlockingIssues)
        assertTrue(unrelatedValid.issues.any { it.code == "unbuildable_timeline" })
    }

    @Test fun automaticSpecialPlacementAlsoReportsAndResolvesTerminalGap() {
        val automaticRules = reported().copy(placement = SpecialBreakPlacement())
        val blocked = engine.generate(automaticRules)
        assertEquals(minutes(1), blocked.tailBreakRemainder?.remainingSeconds)
        assertTrue(blocked.hasBlockingIssues)

        val lastPose = engine.generate(automaticRules.copy(
            tailPoseExtensionSeconds = minutes(1)
        ))
        assertFullTimeline(lastPose)
        assertEquals(1, lastPose.phases.count { it.kind == PhaseKind.SPECIAL_BREAK })
        val summary = engine.generate(automaticRules.copy(
            summarySeconds = minutes(16)
        ))
        assertFullTimeline(summary)
        assertEquals(1, summary.phases.count { it.kind == PhaseKind.SPECIAL_BREAK })
    }

    @Test fun manuallyRequiredSpecialCannotBeDroppedAtTheTail() {
        val base = SessionRules(
            id = "manualSpecial",
            name = "manualSpecial",
            totalSeconds = minutes(36),
            poseSeconds = minutes(10),
            shortBreakSeconds = minutes(5),
            specialBreakSeconds = minutes(15),
            specialBreakCount = 1,
            placement = SpecialBreakPlacement.manual(listOf(2)),
        )
        val blocked = engine.generate(base)
        assertEquals(minutes(11), blocked.tailBreakRemainder?.remainingSeconds)
        assertTrue(blocked.tailBreakRemainder!!.attemptedSpecial)
        val overridden = engine.generate(base.copy(
            tailPoseExtensionSeconds = minutes(11)
        ))
        assertTrue(overridden.hasBlockingIssues)
        val reallocated = engine.generate(base.copy(summarySeconds = minutes(11)))
        assertTrue(reallocated.hasBlockingIssues)
    }

    @Test fun specialBreakTerminalRemainderCanBeAnyOneToFifteenMinutes() {
        for (remainder in 1..15) {
            val rules = SessionRules(
                id = "lateSpecial",
                name = "lateSpecial",
                totalSeconds = minutes(25 + remainder),
                poseSeconds = minutes(10),
                shortBreakSeconds = minutes(5),
                specialBreakSeconds = minutes(15),
                specialBreakCount = 1,
                placement = SpecialBreakPlacement.manual(listOf(2)),
            )
            val blocked = engine.generate(rules)
            assertEquals(minutes(remainder), blocked.tailBreakRemainder?.remainingSeconds)
            assertEquals(minutes(15), blocked.tailBreakRemainder?.attemptedBreakSeconds)
            assertTrue(engine.generate(rules.copy(
                tailPoseExtensionSeconds = minutes(remainder)
            )).hasBlockingIssues)
        }
    }

    @Test fun savedExtendedSessionReopensWithoutChangingLegacySignatureRules() {
        val fixed = engine.generate(reported().copy(
            tailPoseExtensionSeconds = minutes(1)
        ))
        assertFullTimeline(fixed)
        val approved = engine.approve(fixed, acknowledgeWarnings = true)
        var state = PresetCollection()
        val store = object : PresetStore {
            override fun read() = state
            override fun write(value: PresetCollection) { state = value }
        }
        val catalog = PresetCatalog(store, engine) { "new-preset-id" }
        val saved = catalog.save(approved, "testGoogle")
        assertEquals(minutes(1), saved.rules.tailPoseExtensionSeconds)
        assertEquals(minutes(1), catalog.open(saved.id).rules.tailPoseExtensionSeconds)
        assertEquals(saved.timelineSignature, PresetCatalog.signature(catalog.open(saved.id)))
        val standard = engine.generate(reported(185), FinalPoseChoice.remainder())
        val defaultRules = reported(185).copy(tailPoseExtensionSeconds = 0)
        assertEquals(
            PresetCatalog.signature(engine.approve(standard)),
            PresetCatalog.signature(engine.approve(
                engine.generate(defaultRules, FinalPoseChoice.remainder())
            )),
        )
    }

    @Test fun changeOpeningBreakSixMinutesWorksWithoutTailOverride() {
        val original = reported()
        val revised = original.copy(openingGroups = original.openingGroups.map {
            it.copy(breakAfterSeconds = minutes(6))
        })
        val result = engine.generate(revised)
        assertFullTimeline(result)
        assertNull(result.tailBreakRemainder)
    }
}
