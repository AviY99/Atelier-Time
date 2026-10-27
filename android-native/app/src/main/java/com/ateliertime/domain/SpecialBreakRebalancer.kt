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
    ): List<SchedulePhase> {
        if (!automatic || firstUnstartedIndex !in phases.indices ||
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
