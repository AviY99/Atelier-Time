package com.ateliertime.domain

import kotlin.math.abs

/**
 * Runtime placement only: an automatic special Break follows the physical
 * Class midpoint (or evenly spaced centers for multiple special Breaks).
 *
 * When Next extends the end with extra regular Sessions, the original special
 * Break can become much too early. Swap its duration/type with an UNSTARTED
 * ordinary Break, then re-time only the future. No observed phase, current
 * deadline, total Break count, or saved Class is ever changed.
 * Manual break ordinals are authoritative and are deliberately not moved.
 */
object SpecialBreakRebalancer {
    fun rebalance(
        phases: List<SchedulePhase>,
        firstUnstartedIndex: Int,
        actualElapsedSeconds: Int,
        physicalClassSeconds: Int,
        automatic: Boolean,
        genuineSpecial: Boolean = true,
    ): List<SchedulePhase> {
        if (!automatic || !genuineSpecial || firstUnstartedIndex !in phases.indices ||
            physicalClassSeconds <= 0) return phases
        val overallSpecials = phases.count { it.kind == PhaseKind.SPECIAL_BREAK }
        if (overallSpecials == 0) return phases
        var updated = phases
        var priorPlaced = firstUnstartedIndex - 1
        val done = phases.take(firstUnstartedIndex).count {
            it.kind == PhaseKind.SPECIAL_BREAK
        }
        for (position in done + 1..overallSpecials) {
            val specialIndex = updated.indices.firstOrNull { index ->
                index > priorPlaced && updated[index].kind == PhaseKind.SPECIAL_BREAK
            } ?: break
            val nextSpecial = updated.indices.firstOrNull { index ->
                index > specialIndex && updated[index].kind == PhaseKind.SPECIAL_BREAK
            } ?: updated.size
            val candidates = (maxOf(firstUnstartedIndex, priorPlaced + 1)
                until nextSpecial).filter { index ->
                    index == specialIndex ||
                        (updated[index].kind == PhaseKind.SHORT_BREAK &&
                            updated[index].openingGroupId == null)
                }
            if (candidates.isEmpty()) break
            val base = updated
            var best = base
            var bestIndex = specialIndex
            var bestScore = score(base, specialIndex, firstUnstartedIndex,
                actualElapsedSeconds, physicalClassSeconds, position, overallSpecials)
            for (candidateIndex in candidates) {
                if (candidateIndex == specialIndex) continue
                val moved = moveSpecial(base, specialIndex, candidateIndex,
                    firstUnstartedIndex)
                val distance = score(moved, candidateIndex, firstUnstartedIndex,
                    actualElapsedSeconds, physicalClassSeconds, position, overallSpecials)
                if (distance < bestScore) {
                    best = moved
                    bestIndex = candidateIndex
                    bestScore = distance
                }
            }
            updated = best
            priorPlaced = bestIndex
        }
        check(updated.take(firstUnstartedIndex) == phases.take(firstUnstartedIndex))
        check(updated.last().endSeconds == phases.last().endSeconds)
        check(updated.count { it.kind == PhaseKind.SPECIAL_BREAK } == overallSpecials)
        return updated
    }


    /**
     * D-145 runtime-only placement by cumulative *performed + future* work.
     *
     * [workedSeconds] is real Pose time already observed, including the
     * abandoned part of an interrupted/repeated Pose, never skipped virtual
     * minutes. [activeIndex] is the phase now being entered/restarted; it
     * counts as FUTURE from its full start. [firstUnstartedIndex] is strictly
     * AFTER that entered phase, so even the phase the user just entered is
     * immutable. Earlier Special Breaks are considered consumed.
     *
     * Assign all remaining automatic special breaks jointly to distinct
     * ordinary slots using ordered dynamic programming; keep the ordinal
     * targets i/(N+1) of the ENTIRE class, not 1/(remaining+1).
     */
    fun rebalanceByWork(
        phases: List<SchedulePhase>,
        activeIndex: Int,
        firstUnstartedIndex: Int,
        workedSeconds: Long,
        ordinaryBreakSeconds: Int,
        specialBreakSeconds: Int,
        automatic: Boolean,
    ): List<SchedulePhase> {
        if (!automatic || specialBreakSeconds <= ordinaryBreakSeconds ||
            phases.isEmpty() || activeIndex !in phases.indices ||
            firstUnstartedIndex !in 0..phases.size ||
            firstUnstartedIndex <= activeIndex) return phases

        val totalSpecials = phases.count { it.kind == PhaseKind.SPECIAL_BREAK }
        val consumed = phases.take(firstUnstartedIndex).count {
            it.kind == PhaseKind.SPECIAL_BREAK
        }
        val remaining = totalSpecials - consumed
        if (remaining <= 0) return phases

        // Opening-group breaks have their own user-defined placement.
        // An unusual future special inside an opening group must remain fixed.
        if (phases.drop(firstUnstartedIndex).any {
                it.kind == PhaseKind.SPECIAL_BREAK && it.openingGroupId != null
            }) return phases

        val eligible = (firstUnstartedIndex until phases.size).filter { i ->
            val p = phases[i]
            (p.kind == PhaseKind.SPECIAL_BREAK ||
                (p.kind == PhaseKind.SHORT_BREAK && p.openingGroupId == null)) &&
                p.durationSeconds == (if (p.kind == PhaseKind.SPECIAL_BREAK)
                    specialBreakSeconds else ordinaryBreakSeconds)
        }
        if (eligible.size < remaining) return phases
        val originalSpecials = eligible.filter {
            phases[it].kind == PhaseKind.SPECIAL_BREAK
        }.toSet()
        if (originalSpecials.size != remaining) return phases

        val totalWork = workedSeconds +
            phases.drop(activeIndex).filter { it.kind == PhaseKind.POSE }
                .sumOf { it.durationSeconds.toLong() }
        if (totalWork <= 0) return phases

        data class Solution(val distance: Long, val changed: Int, val indices: List<Int>)
        fun better(a: Solution, b: Solution?): Boolean =
            b == null || a.distance < b.distance ||
                (a.distance == b.distance && a.changed < b.changed) ||
                (a.distance == b.distance && a.changed == b.changed &&
                    a.indices.toString() < b.indices.toString())

        // Work stays flat over breaks. Determine the cumulative work BEFORE
        // each possible future Break; do not count Pause, Summary or Break.
        val positions = mutableMapOf<Int, Long>()
        var work = workedSeconds
        for (i in activeIndex until phases.size) {
            val p = phases[i]
            if (i in eligible) positions[i] = work
            if (p.kind == PhaseKind.POSE) work += p.durationSeconds.toLong()
        }

        val dp = arrayOfNulls<Solution>(remaining + 1)
        dp[0] = Solution(0L, 0, emptyList())
        for (slot in eligible) {
            for (count in remaining downTo 1) {
                val previous = dp[count - 1] ?: continue
                val ordinal = consumed + count
                val distance = abs((positions.getValue(slot) * (totalSpecials + 1L)) -
                    totalWork * ordinal)
                val candidate = Solution(
                    previous.distance + distance,
                    previous.changed + if (slot in originalSpecials) 0 else 1,
                    previous.indices + slot,
                )
                if (better(candidate, dp[count])) dp[count] = candidate
            }
        }
        val selected = dp[remaining]?.indices?.toSet() ?: return phases
        if (selected == originalSpecials) return phases

        val updated = phases.toMutableList()
        for (i in eligible) {
            val p = updated[i]
            val isSpecial = i in selected
            val duration = if (isSpecial) specialBreakSeconds else ordinaryBreakSeconds
            updated[i] = p.copy(
                kind = if (isSpecial) PhaseKind.SPECIAL_BREAK else PhaseKind.SHORT_BREAK,
                endSeconds = p.startSeconds + duration,
            )
        }
        // Retiming is confined to not-yet-entered phases; their combined
        // duration is unchanged because count and lengths remain unchanged.
        var cursor = phases[firstUnstartedIndex].startSeconds.toLong()
        for (i in firstUnstartedIndex until updated.size) {
            val p = updated[i]
            val end = cursor + p.durationSeconds
            require(end <= Int.MAX_VALUE.toLong()) { "Class duration overflows." }
            updated[i] = p.shifted(cursor.toInt(), end.toInt())
            cursor = end
        }
        check(updated.take(firstUnstartedIndex) == phases.take(firstUnstartedIndex))
        check(updated.last().endSeconds == phases.last().endSeconds)
        check(updated.count { it.kind == PhaseKind.SPECIAL_BREAK } == totalSpecials)
        check(updated.zipWithNext().all { (a, b) -> a.endSeconds == b.startSeconds })
        return updated
    }

    private fun score(
        phases: List<SchedulePhase>, specialIndex: Int, first: Int,
        elapsed: Int, length: Int, ordinal: Int, count: Int,
    ): Long {
        val special = phases[specialIndex]
        // Match the CENTER of the special Break, not its starting edge.
        val twiceCenter = 2L * elapsed +
            2L * (special.startSeconds - phases[first].startSeconds) +
            special.durationSeconds
        return abs(twiceCenter * (count + 1) - 2L * length * ordinal)
    }

    private fun moveSpecial(
        phases: List<SchedulePhase>, oldIndex: Int, newIndex: Int, first: Int,
    ): List<SchedulePhase> {
        val special = phases[oldIndex]
        val ordinary = phases[newIndex]
        val result = phases.toMutableList()
        result[oldIndex] = special.copy(kind = PhaseKind.SHORT_BREAK,
            endSeconds = special.startSeconds + ordinary.durationSeconds)
        result[newIndex] = ordinary.copy(kind = PhaseKind.SPECIAL_BREAK,
            endSeconds = ordinary.startSeconds + special.durationSeconds)
        var cursor = phases[first].startSeconds
        for (i in first until result.size) {
            val p = result[i]
            result[i] = p.shifted(cursor, cursor + p.durationSeconds)
            cursor += p.durationSeconds
        }
        return result
    }
}
