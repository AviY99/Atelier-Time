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
}
