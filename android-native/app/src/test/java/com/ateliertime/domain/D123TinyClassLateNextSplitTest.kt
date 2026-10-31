package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** User-reported six-minute saved Class "test": after consuming the last
 * planned Break with Next, the remaining time must not become one giant pose.
 * Exact whole-second clock is injected; no Android hardware is required.
 */
class D123TinyClassLateNextSplitTest {
    private fun tiny(): ApprovedSession {
        val rules = SessionRules(
            id = "qa-d123-tiny", name = "test",
            totalSeconds = 6 * SECONDS_PER_MINUTE,
            summarySeconds = SECONDS_PER_MINUTE,
            openingGroups = listOf(OpeningPoseGroup(
                id = "opening-1", count = 1,
                poseSeconds = SECONDS_PER_MINUTE,
                breakAfterSeconds = SECONDS_PER_MINUTE)),
            poseSeconds = SECONDS_PER_MINUTE,
            shortBreakSeconds = SECONDS_PER_MINUTE,
            specialBreakSeconds = SECONDS_PER_MINUTE,
            specialBreakCount = 1,
            cycleStart = CycleStart.BREAK_FIRST,
            placement = SpecialBreakPlacement(automatic = true),
        )
        return ScheduleEngine().approve(ScheduleEngine().generate(rules),
            acknowledgeWarnings = true)
    }

    private fun afterLastPlannedBreakNext(
        now: () -> Long, advance: (Long) -> Unit,
    ): MonotonicSessionTimer {
        val timer = MonotonicSessionTimer(tiny(), now)
        timer.start()
        val expected = listOf(
            PhaseKind.SPECIAL_BREAK, PhaseKind.POSE,
            PhaseKind.SHORT_BREAK, PhaseKind.POSE)
        expected.forEach { phase ->
            advance(2_000L)
            assertEquals("Each rapid Next should reach its following phase",
                phase, timer.nextPhase().view.phase?.kind)
        }
        return timer
    }

    private fun assertTimelineSafe(timer: MonotonicSessionTimer, now: Long) {
        val phases = timer.activePhasesSnapshot()
        assertEquals(PhaseKind.SUMMARY, phases.last().kind)
        assertEquals(SECONDS_PER_MINUTE, phases.last().durationSeconds)
        assertEquals(phases.last().endSeconds, timer.view().totalSeconds)
        assertTrue(phases.zipWithNext().all { (a, b) -> a.endSeconds == b.startSeconds })
        assertTrue(phases.all { it.durationSeconds > 0 &&
            (it.kind != PhaseKind.POSE || it.durationSeconds >= SECONDS_PER_MINUTE) })
        val physicalRemaining = 6 * 60_000L - now
        val virtualRemaining = timer.view().totalRemainingSeconds * 1000L
        assertTrue("Physical Class finish moved after Next/Restart: " +
                "virtual=$virtualRemaining physical=$physicalRemaining",
            kotlin.math.abs(virtualRemaining - physicalRemaining) < 2_000L)
    }

    @Test fun nextThroughLastPlannedBreakCreatesRegularFutureSessionAndBreak() {
        var now = 0L
        val initial = tiny()
        assertEquals(
            listOf(PhaseKind.POSE, PhaseKind.SPECIAL_BREAK, PhaseKind.POSE,
                PhaseKind.SHORT_BREAK, PhaseKind.POSE, PhaseKind.SUMMARY),
            initial.phases.map { it.kind })
        assertEquals(SECONDS_PER_MINUTE, initial.rules.specialBreakSeconds)
        val timer = afterLastPlannedBreakNext({ now }) { now += it }
        val phases = timer.activePhasesSnapshot()
        val active = timer.view().phase
        assertEquals(initial.phases[4].id, active?.id)
        assertEquals(SECONDS_PER_MINUTE, active?.durationSeconds)
        assertEquals(PhaseKind.SHORT_BREAK, phases[5].kind)
        assertEquals(PhaseKind.POSE, phases[6].kind)
        assertTrue("The saved final Session must be split, not grow giant",
            phases.filter { it.kind == PhaseKind.POSE }.size >= 4)
        assertEquals(initial.phases.take(4), phases.take(4))
        assertEquals(initial.phases.last().id, phases.last().id)
        assertTimelineSafe(timer, now)
        // Source plan stays immutable; even equal-length Special retains its
        // Editor name and slot, independently of the runtime split.
        assertEquals(6, initial.phases.size)
        assertEquals(PhaseKind.SPECIAL_BREAK, initial.phases[1].kind)
    }

    @Test fun moreNextOnSplitTailPreservesPastAndInsertsFurtherCycles() {
        var now = 0L
        val timer = afterLastPlannedBreakNext({ now }) { now += it }
        val before = timer.activePhasesSnapshot()
        now += 1_000L
        val breakNext = timer.nextPhase().view
        assertEquals(PhaseKind.SHORT_BREAK, breakNext.phase?.kind)
        val after = timer.activePhasesSnapshot()
        assertEquals(before.take(5), after.take(5))
        assertEquals(before[5].id, breakNext.phase?.id)
        assertTrue("Skipped work credit must split further future, not pile up",
            after.count { it.id.startsWith("runtime-tail-break-") } >= 2)
        assertTimelineSafe(timer, now)

        val observedPrefix = after.take(6)
        now += 1_000L
        val poseNext = timer.nextPhase().view
        assertEquals(PhaseKind.POSE, poseNext.phase?.kind)
        assertEquals(observedPrefix, timer.activePhasesSnapshot().take(6))
        assertTimelineSafe(timer, now)
    }

    @Test fun restartEarlySplitWorkOnlyConsumesUnusedTailCredit() {
        var now = 0L
        val timer = afterLastPlannedBreakNext({ now }) { now += it }
        val before = timer.activePhasesSnapshot()
        assertEquals(PhaseKind.POSE, timer.view().phase?.kind)
        now += 4_000L
        val restarted = timer.restartCurrentSegmentKeepingEndTime()
        assertEquals(before[4].id, restarted.phase?.id)
        assertEquals(before.take(5), timer.activePhasesSnapshot().take(5))
        assertEquals(before.size, timer.activePhasesSnapshot().size)
        assertEquals(before[6].durationSeconds - 4,
            timer.activePhasesSnapshot()[6].durationSeconds)
        assertTimelineSafe(timer, now)
    }

    @Test fun minuteSessionsWithThreeMinuteBreaksSplitAsSoonAsBudgetAllows() {
        // Same break-first topology as saved "test", but Breaks are 3x longer
        // than work Sessions. Do not wait until ALL planned Breaks are consumed
        // to convert an oversized final Session into regular cycles.
        val rules = SessionRules(
            id = "qa-long-break", name = "long break tiny sessions",
            totalSeconds = 10 * SECONDS_PER_MINUTE,
            summarySeconds = SECONDS_PER_MINUTE,
            openingGroups = listOf(OpeningPoseGroup(
                "opening-1", 1, SECONDS_PER_MINUTE, 3 * SECONDS_PER_MINUTE)),
            poseSeconds = SECONDS_PER_MINUTE,
            shortBreakSeconds = 3 * SECONDS_PER_MINUTE,
            specialBreakSeconds = 3 * SECONDS_PER_MINUTE,
            specialBreakCount = 1,
            cycleStart = CycleStart.BREAK_FIRST,
        )
        val session = ScheduleEngine().approve(ScheduleEngine().generate(rules),
            acknowledgeWarnings = true)
        assertEquals(
            listOf(PhaseKind.POSE, PhaseKind.SPECIAL_BREAK, PhaseKind.POSE,
                PhaseKind.SHORT_BREAK, PhaseKind.POSE, PhaseKind.SUMMARY),
            session.phases.map { it.kind })
        var now = 0L
        val timer = MonotonicSessionTimer(session) { now }
        timer.start()
        now += 2_000L
        assertEquals(PhaseKind.SPECIAL_BREAK, timer.nextPhase().view.phase?.kind)
        now += 2_000L
        assertEquals(PhaseKind.POSE, timer.nextPhase().view.phase?.kind)
        now += 2_000L
        assertEquals(PhaseKind.SHORT_BREAK, timer.nextPhase().view.phase?.kind)
        // This is BEFORE the final programmed Break was skipped. Skipping the
        // 3-minute Special Break already banked enough for a new work/rest
        // cycle; keeping all time in the last work Session is incorrect.
        val phases = timer.activePhasesSnapshot()
        val lastPlanned = session.phases[4]
        val lastIndex = phases.indexOfFirst { it.id == lastPlanned.id }
        assertEquals(SECONDS_PER_MINUTE, phases[lastIndex].durationSeconds)
        assertEquals(PhaseKind.SHORT_BREAK, phases[lastIndex + 1].kind)
        assertEquals(3 * SECONDS_PER_MINUTE,
            phases[lastIndex + 1].durationSeconds)
        assertEquals(PhaseKind.POSE, phases[lastIndex + 2].kind)
        assertEquals(session.phases.take(4), phases.take(4))
        val virtualRemaining = timer.view().totalRemainingSeconds * 1000L
        val physicalRemaining = 10 * 60_000L - now
        assertTrue(kotlin.math.abs(virtualRemaining - physicalRemaining) < 2_000L)
        assertEquals(PhaseKind.SUMMARY, phases.last().kind)
        assertEquals(SECONDS_PER_MINUTE, phases.last().durationSeconds)

        now += 2_000L
        assertEquals(PhaseKind.POSE, timer.nextPhase().view.phase?.kind)
        assertTrue(timer.activePhasesSnapshot().take(4) == phases.take(4))
        now += 2_000L
        assertEquals(PhaseKind.SHORT_BREAK, timer.nextPhase().view.phase?.kind)
        assertTrue("A long tail must keep splitting during later Next actions",
            timer.activePhasesSnapshot().count {
                it.id.startsWith("runtime-tail-break-")
            } >= 2)
    }

    @Test fun withoutNextTinyClassRestartMustNotRewriteMinimumLengthPair() {
        var now = 0L
        val timer = MonotonicSessionTimer(tiny()) { now }
        timer.start()
        now = 8_000L
        val before = timer.view()
        val phases = timer.activePhasesSnapshot()
        assertTrue(runCatching {
            timer.restartCurrentSegmentKeepingEndTime()
        }.isFailure)
        assertEquals(before, timer.view())
        assertEquals(phases, timer.activePhasesSnapshot())
    }
    @Test fun thresholdAtExactOneBlockBudgetDoesNotCountOldBreakTwice() {
        val approved = tiny()
        // The original two regular Sessions contain 120 seconds of work.
        // A 120-second Next credit funds one 60s Session plus one 60s Break.
        // The existing 60s final-pair Break is already outside that work sum.
        val revised = FinalPairCompensation.adjust(
            approved, lostSeconds = -120, allowExtraRegular = true)
        assertEquals(480, revised.totalSeconds)
        assertEquals(1, revised.phases.count {
            it.id.startsWith("runtime-added-pose-")
        })
        assertEquals(1, revised.phases.count {
            it.id.startsWith("runtime-added-break-")
        })
        assertEquals(2, revised.phases.count {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR &&
                it.id in approved.phases.map { phase -> phase.id }
        })
        assertEquals(SECONDS_PER_MINUTE, revised.phases.last().durationSeconds)
    }

    @Test fun restartCurrentBreakAfterFirstSplitKeepsNewCyclesIntact() {
        var now = 0L
        val timer = MonotonicSessionTimer(tiny()) { now }
        timer.start()
        repeat(3) {
            now += 2_000L
            timer.nextPhase()
        }
        assertEquals(PhaseKind.SHORT_BREAK, timer.view().phase?.kind)
        val before = timer.activePhasesSnapshot()
        assertTrue(before.any { it.id.startsWith("runtime-tail-break-") })
        val oldLast = before.last {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }
        now += 5_000L
        val restarted = timer.restartCurrentSegmentKeepingEndTime()
        assertEquals(PhaseKind.SHORT_BREAK, restarted.phase?.kind)
        val after = timer.activePhasesSnapshot()
        assertEquals(before.size, after.size)
        assertEquals(before.dropLast(2).map { it.id },
            after.dropLast(2).map { it.id })
        assertEquals(before.dropLast(2).map { it.kind },
            after.dropLast(2).map { it.kind })
        assertEquals(oldLast.durationSeconds - 5,
            after.last { it.kind == PhaseKind.POSE &&
                it.poseSource == PoseSource.REGULAR }.durationSeconds)
        assertTimelineSafe(timer, now)
    }

    @Test fun pauseResumeAfterFirstSplitPreservesFutureCycleAndDeadline() {
        var now = 0L
        val timer = MonotonicSessionTimer(tiny()) { now }
        timer.start()
        repeat(3) {
            now += 2_000L
            timer.nextPhase()
        }
        val before = timer.activePhasesSnapshot()
        assertTrue(before.any { it.id.startsWith("runtime-tail-break-") })
        timer.pause()
        now += 5_000L
        timer.resume()
        val after = timer.activePhasesSnapshot()
        assertEquals(before.map { it.id }, after.map { it.id })
        assertEquals(before.map { it.kind }, after.map { it.kind })
        assertEquals(before.last {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.durationSeconds - 5, after.last {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.durationSeconds)
        assertTimelineSafe(timer, now)
    }

}
