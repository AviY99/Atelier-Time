package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** Exhaustive minute-boundary property checks across valid two-Session plans. */
class RunEndTimePropertyTest {
    private val engine = ScheduleEngine()
    private fun m(n: Int) = n * 60
    private val choices = listOf(
        FinalPoseChoice.remainder(),
        FinalPoseChoice.even(),
        FinalPoseChoice.custom(m(18)),
    )

    @Test fun everyFeasibleWholeMinuteEndKeepsUnrelatedPhasesAndTargetExact() {
        for (choice in choices) {
            val approved = engine.approve(engine.generate(SessionRules(
                id = "property", name = "Property coverage",
                totalSeconds = m(180), summarySeconds = m(15),
                poseSeconds = m(25), specialBreakCount = 1,
            ), choice))
            val original = approved.phases.toList()
            val regular = original.indices.filter {
                original[it].kind == PhaseKind.POSE &&
                    original[it].poseSource == PoseSource.REGULAR
            }.takeLast(2)
            assertEquals(2, regular.size)
            for (signedMinutes in -120..34) {
                val targetSeconds = m(180 - signedMinutes)
                val plan = RunEndTimePlanner.plan(
                    approved, 50_000L, 50_000L + targetSeconds * 1000L
                )
                assertEquals(targetSeconds, plan.adjusted.totalSeconds)
                assertEquals(m(signedMinutes), plan.correctionSeconds)
                val revised = plan.adjusted.phases
                val addedCycles = revised.any {
                    it.id.startsWith("runtime-added-") ||
                        it.id.startsWith("runtime-tail-")
                }
                // With no inserted cycles the old protected-prefix contract
                // still holds. A large initial extension may instead move an
                // AUTOMATIC special Break into a newly added regular slot.
                if (!addedCycles) {
                    assertEquals(original.take(regular.first()),
                        revised.take(regular.first()))
                }
                val merged = plan.adjusted.lastFinalPoseSeconds == 0
                if (merged) {
                    assertFalse("A merged ending cannot have inserted cycles", addedCycles)
                    assertEquals(original.size - 2, revised.size)
                    assertTrue(plan.adjusted.firstFinalPoseSeconds <= approved.rules.poseSeconds)
                    assertEquals(original[regular.first()].id,
                        revised[regular.first()].id)
                } else if (!addedCycles) {
                    assertEquals(original[regular.first() + 1].durationSeconds,
                        revised[regular.first() + 1].durationSeconds)
                }
                // Initial extension can distribute work and insert breaks, but
                // the approved summary, the physical end, and genuine special
                // Break count/durations must remain exact.
                assertEquals(original.last().durationSeconds,
                    revised.last().durationSeconds)
                assertEquals(original.count { it.kind == PhaseKind.SPECIAL_BREAK },
                    revised.count { it.kind == PhaseKind.SPECIAL_BREAK })
                assertEquals(
                    original.filter { it.kind == PhaseKind.SPECIAL_BREAK }
                        .sumOf { it.durationSeconds },
                    revised.filter { it.kind == PhaseKind.SPECIAL_BREAK }
                        .sumOf { it.durationSeconds },
                )
                assertEquals(
                    approved.rules.summarySeconds,
                    revised.filter { it.kind == PhaseKind.SUMMARY }
                        .sumOf { it.durationSeconds },
                )
                assertTrue(revised.all { it.durationSeconds > 0 &&
                    (it.kind != PhaseKind.POSE || it.durationSeconds >= m(1)) })
                assertTrue(plan.adjusted.firstFinalPoseSeconds >= m(1))
                if (!merged) assertTrue(plan.adjusted.lastFinalPoseSeconds >= m(1))
                assertEquals(plan.adjusted.totalSeconds,
                    revised.last().endSeconds)
                revised.zipWithNext().forEach { (left, right) ->
                    assertEquals(left.endSeconds, right.startSeconds)
                }
                assertEquals(original, approved.phases)
            }
        }
    }

    @Test fun allEarlierTargetsBeyondMinimumAreRejectedWithoutMutation() {
        val approved = engine.approve(engine.generate(SessionRules(
            id = "invalid-property", name = "Invalid short target",
            totalSeconds = m(180), summarySeconds = m(15),
            poseSeconds = m(25), specialBreakCount = 1,
        ), FinalPoseChoice.remainder()))
        val before = approved.phases.toList()
        for (tooEarly in 35..60) {
            try {
                RunEndTimePlanner.plan(
                    approved, 0L, m(180 - tooEarly).toLong() * 1000L
                )
                fail("Expected rejection for shortening by $tooEarly minutes")
            } catch (_: IllegalArgumentException) { }
            assertEquals(before, approved.phases)
        }
    }
}
