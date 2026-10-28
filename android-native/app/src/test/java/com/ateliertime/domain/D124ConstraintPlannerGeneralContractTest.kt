package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

/**
 * General invariant and feasibility gates for one runtime constraint solver.
 * Tests vary work/break ratios, actual special duration, summary, cycle order,
 * and sequences of actions, not a single named saved Class.
 */
class D124ConstraintPlannerGeneralContractTest {
    private val engine = ScheduleEngine()

    private fun plan(
        pose: Int, rest: Int, special: Int, summary: Int,
        start: CycleStart, count: Int = 1,
    ): ApprovedSession? {
        val time = 6 * pose + 4 * maxOf(rest, special) + summary
        val rules = SessionRules(
            id = "matrix-$pose-$rest-$special-$summary-$start",
            name = "matrix", totalSeconds = time,
            summarySeconds = summary, poseSeconds = pose,
            shortBreakSeconds = rest, specialBreakSeconds = special,
            specialBreakCount = count, cycleStart = start,
        )
        val candidate = engine.generate(rules)
        return if (candidate.canApprove) engine.approve(candidate,
            acknowledgeWarnings = true) else null
    }

    private fun verify(
        original: ApprovedSession,
        timer: MonotonicSessionTimer,
        millis: Long,
    ) {
        val phases = timer.activePhasesSnapshot()
        assertTrue(phases.isNotEmpty())
        assertEquals(0, phases.first().startSeconds)
        assertTrue(phases.all { it.durationSeconds > 0 &&
            (it.kind != PhaseKind.POSE ||
                it.durationSeconds >= SECONDS_PER_MINUTE) })
        assertTrue(phases.zipWithNext().all { (a, b) ->
            a.endSeconds == b.startSeconds
        })
        assertEquals(phases.last().endSeconds, timer.view().totalSeconds)
        assertEquals(original.rules.summarySeconds,
            phases.filter { it.kind == PhaseKind.SUMMARY }
                .sumOf { it.durationSeconds })
        assertEquals(phases.size, phases.map { it.id }.distinct().size)
        if (original.rules.specialBreakSeconds >
            original.rules.shortBreakSeconds) {
            assertEquals(original.phases.count {
                it.kind == PhaseKind.SPECIAL_BREAK &&
                    it.durationSeconds > original.rules.shortBreakSeconds
            }, phases.count {
                it.kind == PhaseKind.SPECIAL_BREAK &&
                    it.durationSeconds > original.rules.shortBreakSeconds
            })
        }
        val physicalLeft = original.rules.totalSeconds * 1_000L - millis
        val virtualLeft = (timer.view().totalSeconds -
            timer.view().elapsedSeconds).toLong() * 1_000L
        assertTrue("Fixed physical finish mismatch: physical=$physicalLeft " +
            "virtual=$virtualLeft", abs(physicalLeft - virtualLeft) < 2_000L)
        // Approved/saved plan is never a mutable runtime scratch buffer.
        assertEquals(original.rules.totalSeconds, original.phases.last().endSeconds)
    }

    @Test fun exhaustivePairBudgetFeasibilityAcrossDurations() {
        var checked = 0
        for (p in listOf(60, 120, 300, 900, 1_500)) {
            for (b in listOf(60, 180, 300, 600)) {
                val rules = SessionRules(
                    id = "feasible-$p-$b", name = "matrix",
                    totalSeconds = 3 * p + 2 * b + 60,
                    summarySeconds = 60,
                    poseSeconds = p, shortBreakSeconds = b,
                    specialBreakSeconds = b, specialBreakCount = 0,
                )
                val approved = engine.approve(engine.generate(rules),
                    acknowledgeWarnings = true)
                val pair = approved.phases.filter {
                    it.kind == PhaseKind.POSE &&
                        it.poseSource == PoseSource.REGULAR
                }.takeLast(2)
                assertEquals(2, pair.size)
                val work0 = pair.sumOf { it.durationSeconds }
                for (work in 1..work0 + 240 step 7) {
                    val correction = work0 - work
                    val legal = work >= 120 ||
                        (work + b in 60..p)
                    val proposed = runCatching {
                        FinalPairCompensation.reflow(
                            approved, approved.phases,
                            correctionSeconds = correction)
                    }
                    assertEquals("p=$p b=$b work=$work",
                        legal, proposed.isSuccess)
                    if (proposed.isSuccess) {
                        val result = proposed.getOrThrow()
                        assertEquals(rules.totalSeconds - correction,
                            result.totalSeconds)
                        assertEquals(60, result.phases.last().durationSeconds)
                        assertTrue(result.phases.zipWithNext().all { (a, c) ->
                            a.endSeconds == c.startSeconds
                        })
                    }
                    checked++
                }
            }
        }
        assertTrue(checked > 2_000)
    }

    @Test fun crossProductNextReflowsProtectThePastAndPhysicalDeadline() {
        var cases = 0
        var successfulNexts = 0
        for (p in listOf(60, 120, 300, 1_500)) {
            for (b in listOf(60, 180, 300, 600)) {
                for (special in listOf(60, b, b + 180).distinct()) {
                    for (summary in listOf(0, 60)) {
                        for (start in CycleStart.entries) {
                            val approved = plan(p, b, special, summary, start)
                                ?: continue
                            var millis = 0L
                            val timer = MonotonicSessionTimer(approved) { millis }
                            timer.start()
                            verify(approved, timer, millis)
                            repeat(12) {
                                millis += 1_400L
                                val before = timer.view()
                                val prefix = timer.activePhasesSnapshot()
                                val index = prefix.indexOfFirst {
                                    it.id == before.phase?.id
                                }
                                val advanced = runCatching { timer.nextPhase() }
                                if (advanced.isSuccess) {
                                    successfulNexts++
                                    assertTrue(index >= 0)
                                    assertEquals(prefix.take(index + 1),
                                        timer.activePhasesSnapshot()
                                            .take(index + 1))
                                    verify(approved, timer, millis)
                                } else {
                                    assertEquals(before, timer.view())
                                    assertEquals(prefix,
                                        timer.activePhasesSnapshot())
                                }
                            }
                            cases++
                        }
                    }
                }
            }
        }
        assertTrue(cases > 80)
        assertTrue(successfulNexts > cases)
    }

    @Test fun randomizedMixedNextRestartResumeRejectionsAreAtomic() {
        val random = Random(124_2026)
        var operations = 0
        for (p in listOf(60, 300, 1_500)) {
            for (b in listOf(60, 180, 600)) {
                val session = plan(p, b, b + 180, 60,
                    CycleStart.POSE_FIRST) ?: continue
                var millis = 0L
                val timer = MonotonicSessionTimer(session) { millis }
                timer.start()
                repeat(35) {
                    millis += random.nextLong(100L, 2_400L)
                    val previous = timer.view()
                    val phases = timer.activePhasesSnapshot()
                    val operation = random.nextInt(3)
                    val action = runCatching {
                        when (operation) {
                            0 -> timer.nextPhase()
                            1 -> timer.restartCurrentSegmentKeepingEndTime()
                            else -> {
                                timer.pause()
                                millis += random.nextLong(100L, 1_900L)
                                timer.resume()
                            }
                        }
                    }
                    if (action.isFailure && operation != 2) {
                        assertEquals(previous, timer.view())
                        assertEquals(phases, timer.activePhasesSnapshot())
                    }
                    if (timer.view().status == TimerStatus.RUNNING) {
                        verify(session, timer, millis)
                    }
                    operations++
                }
            }
        }
        assertTrue(operations >= 200)
    }

    @Test fun equalLengthSpecialRetainsItsConfiguredDurationEvenWithNextCredit() {
        val session = plan(60, 180, 180, 60,
            CycleStart.POSE_FIRST)!!
        val original = session.phases.filter {
            it.kind == PhaseKind.SPECIAL_BREAK
        }
        assertEquals(1, original.size)
        assertEquals(180, original.single().durationSeconds)
        var millis = 0L
        val timer = MonotonicSessionTimer(session) { millis }
        timer.start()
        repeat(5) {
            millis += 2_000L
            runCatching { timer.nextPhase() }
            verify(session, timer, millis)
        }
        assertEquals(180, session.phases.single {
            it.kind == PhaseKind.SPECIAL_BREAK
        }.durationSeconds)
    }
}
