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
            for (signedMinutes in -120..28) {
                val targetSeconds = m(180 - signedMinutes)
                val plan = RunEndTimePlanner.plan(
                    approved, 50_000L, 50_000L + targetSeconds * 1000L
                )
                assertEquals(targetSeconds, plan.adjusted.totalSeconds)
                assertEquals(m(signedMinutes), plan.correctionSeconds)
                assertEquals(original.take(regular.first()),
                    plan.adjusted.phases.take(regular.first()))
                assertEquals(original[regular.first() + 1].durationSeconds,
                    plan.adjusted.phases[regular.first() + 1].durationSeconds)
                assertEquals(original.last().durationSeconds,
                    plan.adjusted.phases.last().durationSeconds)
                assertTrue(plan.adjusted.firstFinalPoseSeconds >= m(1))
                assertTrue(plan.adjusted.lastFinalPoseSeconds >= m(1))
                assertEquals(plan.adjusted.totalSeconds,
                    plan.adjusted.phases.last().endSeconds)
                plan.adjusted.phases.zipWithNext().forEach { (left, right) ->
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
        for (tooEarly in 29..60) {
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
