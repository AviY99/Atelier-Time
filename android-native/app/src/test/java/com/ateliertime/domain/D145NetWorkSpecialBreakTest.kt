package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** D-145: net work (not wall-clock) and ordered global special-break assignment. */
class D145NetWorkSpecialBreakTest {
    private fun m(minutes: Int): Int = minutes * 60

    private fun timeline(workMinutes: IntArray, specialSlots: Set<Int>): List<SchedulePhase> {
        val result = mutableListOf<SchedulePhase>()
        var cursor = 0
        for (i in workMinutes.indices) {
            val work = m(workMinutes[i])
            result += SchedulePhase("pose-" + i, PhaseKind.POSE, cursor, cursor + work,
                poseSource = PoseSource.REGULAR)
            cursor += work
            if (i < workMinutes.lastIndex) {
                val special = i + 1 in specialSlots
                val duration = m(if (special) 15 else 5)
                result += SchedulePhase("rest-" + (i + 1),
                    if (special) PhaseKind.SPECIAL_BREAK else PhaseKind.SHORT_BREAK,
                    cursor, cursor + duration, breakOrdinal = i + 1)
                cursor += duration
            }
        }
        result += SchedulePhase("summary", PhaseKind.SUMMARY, cursor, cursor + m(15))
        return result
    }

    private fun recenter(phases: List<SchedulePhase>, active: Int = 0,
        future: Int = 1, pastWork: Int = 0, automatic: Boolean = true
    ) = SpecialBreakRebalancer.rebalanceByWork(
        phases = phases, activeIndex = active, firstUnstartedIndex = future,
        workedSeconds = m(pastWork).toLong(), ordinaryBreakSeconds = m(5),
        specialBreakSeconds = m(15), automatic = automatic)

    private fun specialOrdinals(phases: List<SchedulePhase>) = phases.filter {
        it.kind == PhaseKind.SPECIAL_BREAK
    }.mapNotNull { it.breakOrdinal }

    private fun verify(before: List<SchedulePhase>, after: List<SchedulePhase>,
        protectedUntil: Int, specialCount: Int) {
        assertEquals(before.take(protectedUntil), after.take(protectedUntil))
        assertEquals(before.last().endSeconds, after.last().endSeconds)
        assertEquals(specialCount, after.count { it.kind == PhaseKind.SPECIAL_BREAK })
        assertEquals(before.sumOf { it.durationSeconds },
            after.sumOf { it.durationSeconds })
        assertTrue(after.zipWithNext().all { (a, b) ->
            a.endSeconds == b.startSeconds
        })
    }

    @Test fun twoSpecialsAreGloballyAssignedNearOneThirdAndTwoThirdsOfWork() {
        val before = timeline(intArrayOf(20, 20, 20, 20, 20), setOf(2, 4))
        // 100 minutes of work: choose AFTER 40m and 60m, not 40m and 80m.
        val after = recenter(before)
        assertEquals(listOf(2, 3), specialOrdinals(after))
        verify(before, after, 1, 2)
    }

    @Test fun threeSpecialsChooseOrderedQuarterHalfThreeQuarterWorkSlots() {
        val before = timeline(IntArray(8) { 10 }, setOf(2, 5, 7))
        val after = recenter(before)
        assertEquals(listOf(2, 4, 6), specialOrdinals(after))
        verify(before, after, 1, 3)
    }

    @Test fun consumedFirstSpecialIsUntouchedButSecondTargetsItsOriginalOrdinal() {
        val before = timeline(intArrayOf(20, 20, 20, 20, 20), setOf(2, 4))
        // First Special Break at index 3 was actually entered/consumed.
        // The new active Pose is index 4. Its full 20m is future, not past.
        val after = recenter(before, active = 4, future = 5, pastWork = 40)
        assertEquals(listOf(2, 3), specialOrdinals(after))
        verify(before, after, 5, 2)
    }

    @Test fun aSingleFutureSpecialMayRemainAtItsOnlyAvailableSlot() {
        val before = timeline(intArrayOf(20, 20, 20), setOf(2))
        val after = recenter(before, active = 4, future = 5, pastWork = 40)
        assertEquals(before, after)
    }

    @Test fun oneSpecialUsesHalfOfWorkRegardlessOfRestLengths() {
        val before = timeline(intArrayOf(10, 10, 10, 10, 10, 10), setOf(1))
        val after = recenter(before)
        // 60m work, half = 30m. Slot #3 is the exact work midpoint.
        assertEquals(listOf(3), specialOrdinals(after))
        verify(before, after, 1, 1)
    }

    @Test fun manualAndNoSpecialPlansAreUnchanged() {
        val manual = timeline(intArrayOf(20, 20, 20, 20), setOf(1, 3))
        assertEquals(manual, recenter(manual, automatic = false))
        val none = timeline(intArrayOf(20, 20, 20), emptySet())
        assertEquals(none, recenter(none))
    }

    @Test fun onlyFutureSlotsCanChangeEvenIfGlobalOptimumWasInThePast() {
        val before = timeline(intArrayOf(20, 20, 20, 20, 20), setOf(1, 3))
        val after = recenter(before, active = 4, future = 5, pastWork = 40)
        assertEquals(PhaseKind.SPECIAL_BREAK, after[1].kind)
        verify(before, after, 5, 2)
    }

    @Test fun scoresAreByWorkAndDoNotMoveOnDifferentWallClockElapsedAlone() {
        val before = timeline(intArrayOf(20, 20, 20, 20, 20), setOf(1, 4))
        val first = recenter(before, pastWork = 20)
        val second = recenter(before, pastWork = 20)
        assertEquals(first, second)
        verify(before, first, 1, 2)
    }
}
