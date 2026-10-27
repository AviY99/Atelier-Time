package com.ateliertime.domain

import kotlin.math.roundToInt

/**
 * Runtime-only end-time recovery. Never mutates a saved/approved preset.
 *
 * Signed correction: positive seconds shrink the last two regular poses to
 * recover lost wall time; negative seconds extend them to absorb time gained
 * by skipping a phase. Earlier phases and summary retain their exact approved
 * lengths. If the two remaining final Sessions AND their intervening break
 * fit within one regular Session, that break becomes work time and the three
 * phases collapse into one; the absolute finish stays unchanged.
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
    /**
     * Restarting the break BETWEEN the final two sessions is still allowed:
     * the first final Session is already completed and must never be rewritten.
     * Only the not-yet-started last Session can absorb the additional delay.
     *
     * Ordinary earlier restarts use adjust() and honor the original ending
     * distribution across BOTH future final Sessions. This late-stage fallback
     * deliberately retains the first final Session's already-observed length.
     */
    fun adjustLastOnly(
        session: ApprovedSession,
        active: List<SchedulePhase>,
        newCorrectionSeconds: Int,
    ): CompensatedTimeline {
        // Next may have inserted whole regular Session+Break blocks before
        // this tail. Only the not-yet-started LAST Session is rewritten here.
        val pair = active.indices.filter {
            active[it].kind == PhaseKind.POSE &&
                active[it].poseSource == PoseSource.REGULAR
        }.takeLast(2)
        require(pair.size == 2 && pair[1] == pair[0] + 2) {
            "A final pair and its intervening break are required."
        }
        val first = active[pair[0]]
        val last = active[pair[1]]
        val oldCorrection = session.rules.totalSeconds.toLong() -
            active.last().endSeconds.toLong()
        val addedCorrection = newCorrectionSeconds.toLong() - oldCorrection
        val newLastLength = last.durationSeconds.toLong() - addedCorrection
        require(newLastLength in SECONDS_PER_MINUTE.toLong()..Int.MAX_VALUE.toLong()) {
            "The final Session cannot absorb this break restart while retaining " +
                "its one-minute minimum."
        }
        val revised = active.toMutableList()
        var cursor = last.startSeconds.toLong() + newLastLength
        require(cursor <= Int.MAX_VALUE.toLong()) { "Class timeline exceeds supported range." }
        revised[pair[1]] = last.shifted(last.startSeconds, cursor.toInt())
        for (i in pair[1] + 1 until revised.size) {
            require(revised[i].kind == PhaseKind.SUMMARY) {
                "Unexpected phase after the final regular Session."
            }
            val duration = active[i].durationSeconds
            val end = cursor + duration
            require(end <= Int.MAX_VALUE.toLong()) { "Class timeline exceeds supported range." }
            revised[i] = active[i].shifted(cursor.toInt(), end.toInt())
            cursor = end
        }
        check(cursor == session.rules.totalSeconds.toLong() - newCorrectionSeconds.toLong())
        return CompensatedTimeline(
            phases = revised,
            totalSeconds = cursor.toInt(),
            recoveredSeconds = newCorrectionSeconds,
            firstFinalPoseSeconds = first.durationSeconds,
            lastFinalPoseSeconds = newLastLength.toInt(),
        )
    }
    fun adjust(
        session: ApprovedSession,
        lostSeconds: Int,
        allowExtraRegular: Boolean = false,
    ): CompensatedTimeline {
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
        val mergedLength = availableLong + rest.durationSeconds.toLong()
        // A short pair has one Session, not two one-minute minimums. The
        // deleted break's time is turned into Session time, so the end clock
        // and every phase after the pair retain their exact position.
        if (mergedLength <= session.rules.poseSeconds.toLong()) {
            require(mergedLength in SECONDS_PER_MINUTE.toLong()..Int.MAX_VALUE.toLong()) {
                "The merged final Session must last at least one minute."
            }
            val revised = originals.take(firstIndex).toMutableList()
            val end = first.startSeconds.toLong() + mergedLength
            require(end <= Int.MAX_VALUE.toLong()) { "Class timeline exceeds supported range." }
            revised.add(first.shifted(first.startSeconds, end.toInt()))
            var cursor = end.toInt()
            for (phase in originals.drop(lastIndex + 1)) {
                val next = cursor.toLong() + phase.durationSeconds
                require(next <= Int.MAX_VALUE.toLong()) { "Class timeline exceeds supported range." }
                revised.add(phase.shifted(cursor, next.toInt()))
                cursor = next.toInt()
            }
            check(cursor.toLong() == session.rules.totalSeconds.toLong() - lostSeconds)
            return CompensatedTimeline(revised, cursor, lostSeconds,
                mergedLength.toInt(), 0)
        }
        // One compact rule for new work gained by Next: only when the combined
        // two work Sessions STRICTLY exceed two regular Sessions + one regular
        // Break, consume one whole Session+Break block from the credit and
        // apply the usual chosen split to the remaining final two. Repeat if
        // more than one complete block fits. Past phases are never touched.
        // This does not apply to initial end approval or to positive delay.
        if (allowExtraRegular && lostSeconds < 0) {
            val regular = session.rules.poseSeconds.toLong()
            val regularBreak = session.rules.shortBreakSeconds.toLong()
            val threshold = 2L * regular + regularBreak
            var tailWork = availableLong
            var additional = 0
            while (tailWork > threshold &&
                tailWork - (regular + regularBreak) >=
                    rest.durationSeconds.toLong() + 2L * SECONDS_PER_MINUTE) {
                additional++
                require(additional <= 1_000) { "Too many additional Sessions." }
                tailWork -= regular + regularBreak
            }
            if (additional > 0) {
                val firstFinal = distributeFirst(session, first, last, tailWork.toInt())
                val lastFinal = tailWork.toInt() - firstFinal
                val revised = originals.take(firstIndex).toMutableList()
                var cursor = first.startSeconds.toLong()
                for (number in 1..additional) {
                    val finish = cursor + regular
                    require(finish <= Int.MAX_VALUE.toLong())
                    revised.add(SchedulePhase(
                        id = "runtime-added-pose-$number", kind = PhaseKind.POSE,
                        startSeconds = cursor.toInt(), endSeconds = finish.toInt(),
                        poseNumber = (first.poseNumber ?: 0) + number - 1,
                        poseSource = PoseSource.REGULAR))
                    cursor = finish
                    val endBreak = cursor + regularBreak
                    require(endBreak <= Int.MAX_VALUE.toLong())
                    revised.add(SchedulePhase(
                        id = "runtime-added-break-$number", kind = PhaseKind.SHORT_BREAK,
                        startSeconds = cursor.toInt(), endSeconds = endBreak.toInt(),
                        breakOrdinal = (rest.breakOrdinal ?: 0) + number - 1))
                    cursor = endBreak
                }
                val firstEnd = cursor + firstFinal
                val breakEnd = firstEnd + rest.durationSeconds
                val lastEnd = breakEnd + lastFinal
                require(lastEnd <= Int.MAX_VALUE.toLong())
                revised.add(first.shifted(cursor.toInt(), firstEnd.toInt()).copy(
                    poseNumber = (first.poseNumber ?: 0) + additional))
                revised.add(rest.shifted(firstEnd.toInt(), breakEnd.toInt()))
                revised.add(last.shifted(breakEnd.toInt(), lastEnd.toInt()).copy(
                    poseNumber = (first.poseNumber ?: 0) + additional + 1))
                cursor = lastEnd
                for (summary in originals.drop(lastIndex + 1)) {
                    val next = cursor + summary.durationSeconds
                    require(next <= Int.MAX_VALUE.toLong())
                    revised.add(summary.shifted(cursor.toInt(), next.toInt()))
                    cursor = next
                }
                check(cursor == session.rules.totalSeconds.toLong() - lostSeconds)
                return CompensatedTimeline(revised, cursor.toInt(), lostSeconds,
                    firstFinal, lastFinal)
            }
        }
        require(availableLong in (2L * SECONDS_PER_MINUTE)..Int.MAX_VALUE.toLong()) {
            "Final two Sessions cannot absorb this time change while keeping both " +
                "at least one minute. Approve a revised end time instead."
        }
        val available = availableLong.toInt()
        val firstNew = distributeFirst(session, first, last, available)
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

    private fun distributeFirst(
        session: ApprovedSession, first: SchedulePhase, last: SchedulePhase,
        available: Int,
    ): Int {
        require(available >= 2 * SECONDS_PER_MINUTE)
        val combined = first.durationSeconds.toLong() + last.durationSeconds
        return when (session.selectedChoice?.mode ?: FinalPoseMode.REMAINDER) {
            FinalPoseMode.REMAINDER ->
                minOf(first.durationSeconds, available - SECONDS_PER_MINUTE)
            FinalPoseMode.EVEN_SPLIT ->
                available / 2
            FinalPoseMode.CUSTOM_SPLIT ->
                ((available.toDouble() * first.durationSeconds) / combined).roundToInt()
        }.coerceIn(SECONDS_PER_MINUTE, available - SECONDS_PER_MINUTE)
    }
}
