package com.ateliertime.domain

import kotlin.math.roundToInt

/**
 * Runtime-only end-time recovery. Never mutates a saved/approved preset.
 *
 * Signed correction: positive seconds shrink the last two regular poses to
 * recover lost wall time; negative seconds extend them to absorb time gained
 * by skipping a phase. Earlier phases, intervening break and summary retain
 * their exact approved lengths. The approved preset is NEVER mutated.
 *
 * The user's already chosen ending mode decides how remaining posing time is
 * distributed: REMAINDER uses the final pose first, EVEN splits approximately
 * equally, CUSTOM preserves the selected pair's initial ratio.
 */
data class CompensatedTimeline(
    val phases: List<SchedulePhase>,
    val totalSeconds: Int,
    val recoveredSeconds: Int,
    val firstFinalPoseSeconds: Int,
    val lastFinalPoseSeconds: Int,
)

object FinalPairCompensation {
    fun adjust(session: ApprovedSession, lostSeconds: Int): CompensatedTimeline {
        // A negative correction means a phase was intentionally skipped.
        // Preserve its saved approval; expand only the end pair at runtime.
        val originals = session.phases
        val pair = originals.indices.filter {
            originals[it].kind == PhaseKind.POSE &&
                originals[it].poseSource == PoseSource.REGULAR
        }.takeLast(2)
        require(pair.size == 2 && pair[1] == pair[0] + 2 &&
            (originals[pair[0] + 1].kind == PhaseKind.SHORT_BREAK ||
                originals[pair[0] + 1].kind == PhaseKind.SPECIAL_BREAK)) {
            "This session has no adjustable final two-pose pair."
        }
        val firstIndex = pair[0]
        val lastIndex = pair[1]
        check(originals.drop(lastIndex + 1).all { it.kind == PhaseKind.SUMMARY }) {
            "Cannot change other phases following the last regular pose."
        }
        val first = originals[firstIndex]
        val rest = originals[firstIndex + 1]
        val last = originals[lastIndex]
        val combined = first.durationSeconds.toLong() + last.durationSeconds
        val availableLong = combined - lostSeconds.toLong()
        require(availableLong in (2L * SECONDS_PER_MINUTE)..Int.MAX_VALUE.toLong()) {
            "Final two poses cannot absorb this time change while keeping both " +
                "at least one minute. Approve a revised end time instead."
        }
        val available = availableLong.toInt()
        val firstNew = when (session.selectedChoice?.mode ?: FinalPoseMode.REMAINDER) {
            FinalPoseMode.REMAINDER ->
                minOf(first.durationSeconds, available - SECONDS_PER_MINUTE)
            FinalPoseMode.EVEN_SPLIT ->
                available / 2
            FinalPoseMode.CUSTOM_SPLIT ->
                ((available.toDouble() * first.durationSeconds) / combined).roundToInt()
        }.coerceIn(SECONDS_PER_MINUTE, available - SECONDS_PER_MINUTE)
        val lastNew = available - firstNew

        val revised = originals.toMutableList()
        val firstEnd = first.startSeconds + firstNew
        val restEnd = firstEnd + rest.durationSeconds
        val lastEnd = restEnd + lastNew
        revised[firstIndex] = first.shifted(first.startSeconds, firstEnd)
        revised[firstIndex + 1] = rest.shifted(firstEnd, restEnd)
        revised[lastIndex] = last.shifted(restEnd, lastEnd)
        var cursor = lastEnd
        for (i in (lastIndex + 1) until revised.size) {
            val length = originals[i].durationSeconds
            revised[i] = originals[i].shifted(cursor, cursor + length)
            cursor += length
        }
        check(cursor.toLong() ==
            session.rules.totalSeconds.toLong() - lostSeconds.toLong())
        return CompensatedTimeline(
            revised, cursor, lostSeconds, firstNew, lastNew,
        )
    }
}
