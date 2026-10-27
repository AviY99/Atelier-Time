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

/** The one runtime-reflow contract shared by Next, Pause/Resume and Restart. */
data class ReflowBoundary(
    /** All phases through this index have started or are otherwise protected. */
    val protectedThroughIndex: Int = -1,
    /** For Next, the immediately entered phase and its start must be preserved. */
    val nextIndex: Int? = null,
    /** Reuse current runtime layout after Next/rebalancing when feasible. */
    val preferActiveFuture: Boolean = false,
    /** First final work Session is already observed: adjust the last only. */
    val lastOnly: Boolean = false,
    /** Stable-7 policy: only Next may rebuild a relocated/inserted future.
     * Pause/Resume and Restart MUST preserve the existing future layout and
     * try source reflow, then last-only recovery (never adjustFutureTail).
     */
    val preserveExistingTail: Boolean = false,
    /** Physical time remaining until the immutable approved Class deadline. */
    val physicalRemainingMillis: Long? = null,
    /** Virtual cursor immediately after the proposed transition. */
    val virtualCursorMillis: Long? = null,
)

object FinalPairCompensation {
    /**
     * Single entry point for every proposed runtime correction. All strategies
     * are verified against the same locked-prefix/next-target constraints.
     * Failed proposals are discarded: the caller commits nothing until this
     * returns a complete valid future.
     *
     * The underlying normalization subcases are: untouched approved tail,
     * dynamic unstarted tail (possibly with added blocks), and last-only when
     * its penultimate work Session has already become fact. They no longer
     * choose their own call order in unrelated Timer event handlers.
     */
    fun reflow(
        session: ApprovedSession,
        active: List<SchedulePhase>,
        correctionSeconds: Int,
        allowExtraRegular: Boolean = false,
        boundary: ReflowBoundary = ReflowBoundary(),
    ): CompensatedTimeline {
        fun valid(candidate: CompensatedTimeline): Boolean {
            val phases = candidate.phases
            if (candidate.totalSeconds.toLong() !=
                session.rules.totalSeconds.toLong() - correctionSeconds.toLong()) return false
            val remaining = boundary.physicalRemainingMillis
            val cursor = boundary.virtualCursorMillis
            if (remaining != null || cursor != null) {
                if (remaining == null || cursor == null || remaining < 0L) return false
                val virtualRemaining = candidate.totalSeconds.toLong() * 1_000L - cursor
                // The phase engine has whole-second granularity; the approved
                // deadline and exact cumulative correction retain milliseconds.
                // Together their truncations may differ by less than 2 seconds.
                if (virtualRemaining < 0L ||
                    kotlin.math.abs(virtualRemaining - remaining) >= 2_000L) return false
            }
            if (phases.isEmpty() || phases.first().startSeconds != 0 ||
                phases.last().endSeconds != candidate.totalSeconds ||
                phases.any { it.durationSeconds <= 0 ||
                    (it.kind == PhaseKind.POSE && it.durationSeconds < SECONDS_PER_MINUTE) } ||
                phases.zipWithNext().any { (a, b) -> a.endSeconds != b.startSeconds } ||
                phases.filter { it.kind == PhaseKind.SUMMARY }
                    .sumOf { it.durationSeconds } != session.rules.summarySeconds) return false
            if (session.rules.specialBreakSeconds > session.rules.shortBreakSeconds) {
                val existing = active.filter { it.kind == PhaseKind.SPECIAL_BREAK &&
                    it.durationSeconds > session.rules.shortBreakSeconds }
                val proposed = phases.filter { it.kind == PhaseKind.SPECIAL_BREAK &&
                    it.durationSeconds > session.rules.shortBreakSeconds }
                if (existing.size != proposed.size) return false
                if (!session.rules.placement.automatic &&
                    existing.map { it.id } != proposed.map { it.id }) return false
            }
            val index = boundary.protectedThroughIndex
            if (index >= 0 && (index !in active.indices ||
                    candidate.phases.take(index + 1) != active.take(index + 1))) return false
            val target = boundary.nextIndex ?: return true
            val before = active.getOrNull(target) ?: return false
            val after = candidate.phases.getOrNull(target) ?: return false
            return before.id == after.id && before.kind == after.kind &&
                before.startSeconds == after.startSeconds
        }
        if (boundary.lastOnly) {
            return adjustLastOnly(session, active, correctionSeconds).also {
                require(valid(it)) { "The completed Class history cannot be changed." }
            }
        }
        val base = { adjust(session, correctionSeconds, allowExtraRegular) }
        val activeTail = {
            adjustFutureTail(session, active,
                boundary.nextIndex ?: boundary.protectedThroughIndex,
                correctionSeconds)
        }
        // Preserve D-119's tested preference: rebuild from approved source
        // first, then use the already-relocated future only if history forbids
        // a fresh source plan. D-120 reversed this order after the first Next.
        val ordered = if (boundary.preserveExistingTail) listOf(base)
        else listOf(base, activeTail)
        for (attempt in ordered) {
            val proposed = runCatching(attempt).getOrNull() ?: continue
            if (valid(proposed)) return proposed
        }
        val finalOnly = runCatching {
            adjustLastOnly(session, active, correctionSeconds)
        }.getOrNull()
        if (finalOnly != null && valid(finalOnly)) return finalOnly
        throw IllegalArgumentException(
            "The approved finish cannot fit the unstarted future without " +
                "rewriting completed Sessions or protected Breaks."
        )
    }

    /**
     * Incremental reflow after a previously relocated special Break / added
     * regular block. Rebuilding from the SAVED schedule then would overwrite
     * observed runtime history, so lock the current Next target and all
     * preceding phases, and normalize only the untouched adjustable ending.
     */
    fun adjustFutureTail(
        session: ApprovedSession,
        active: List<SchedulePhase>,
        protectedThroughIndex: Int,
        newCorrectionSeconds: Int,
    ): CompensatedTimeline {
        val originalPair = session.phases.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        require(originalPair.size == 2)
        val firstIndex = active.indexOfFirst { it.id == originalPair[0].id }
        val lastIndex = active.indexOfFirst { it.id == originalPair[1].id }
        require(firstIndex > protectedThroughIndex && lastIndex == firstIndex + 2) {
            "The final two Sessions are already in progress."
        }
        val anchor = (protectedThroughIndex + 1 until firstIndex).firstOrNull { i ->
            active[i].id.startsWith("runtime-added-pose-")
        } ?: firstIndex
        val previousTail = active.subList(anchor, lastIndex + 1)
        require(previousTail.count { it.kind == PhaseKind.SPECIAL_BREAK } <=
            1 + previousTail.count { it.id.startsWith("runtime-added-break-") })
        val specialCount = previousTail.count {
            it.kind == PhaseKind.SPECIAL_BREAK &&
                it.durationSeconds > session.rules.shortBreakSeconds
        }
        val prefix = active.take(anchor)
        val summary = active.drop(lastIndex + 1)
        require(summary.all { it.kind == PhaseKind.SUMMARY })
        val summarySeconds = summary.sumOf { it.durationSeconds }.toLong()
        val total = session.rules.totalSeconds.toLong() - newCorrectionSeconds
        val budget = total - active[anchor].startSeconds - summarySeconds
        val regular = session.rules.poseSeconds.toLong()
        val short = session.rules.shortBreakSeconds.toLong()
        val special = session.rules.specialBreakSeconds.toLong()
        val threshold = 2L * regular + short
        fun pairWork(blocks: Int): Long = budget -
            blocks.toLong() * (regular + short) - short -
            specialCount.toLong() * (special - short)
        var blocks = (specialCount - 1).coerceAtLeast(0)
        require(pairWork(blocks) >= 2L * SECONDS_PER_MINUTE) {
            "There is no safe unstarted ending to reflow."
        }
        if (newCorrectionSeconds < 0) {
            while (pairWork(blocks) > threshold &&
                pairWork(blocks + 1) >=
                    (if (specialCount > 0) special else short) +
                        2L * SECONDS_PER_MINUTE) {
                blocks++
                require(blocks <= 1_000) { "Too many additional Sessions." }
            }
        }
        val work = pairWork(blocks)
        require(work in (2L * SECONDS_PER_MINUTE)..Int.MAX_VALUE.toLong())
        val originalFirst = originalPair[0]
        val originalLast = originalPair[1]
        val firstLength = distributeFirst(session, originalFirst, originalLast,
            work.toInt())
        val lastLength = work.toInt() - firstLength
        val result = prefix.toMutableList()
        var cursor = active[anchor].startSeconds.toLong()
        val highestLockedAdded = prefix.mapNotNull {
            if (!it.id.startsWith("runtime-added-pose-")) null
            else it.id.removePrefix("runtime-added-pose-").toIntOrNull()
        }.maxOrNull() ?: 0
        // Keep the same number of special Breaks in the unstarted tail;
        // the rebalancer can subsequently move them toward the midpoint.
        val specialFromEnd = specialCount
        for (i in 1..blocks) {
            val number = highestLockedAdded + i
            val poseEnd = cursor + regular
            result.add(SchedulePhase("runtime-added-pose-$number", PhaseKind.POSE,
                cursor.toInt(), poseEnd.toInt(),
                poseNumber = (originalFirst.poseNumber ?: 0) + number - 1,
                poseSource = PoseSource.REGULAR))
            cursor = poseEnd
            val isSpecial = (blocks - i + 1) < specialFromEnd
            val breakEnd = cursor + if (isSpecial) special else short
            result.add(SchedulePhase("runtime-added-break-$number",
                if (isSpecial) PhaseKind.SPECIAL_BREAK else PhaseKind.SHORT_BREAK,
                cursor.toInt(), breakEnd.toInt(),
                breakOrdinal = (active[firstIndex + 1].breakOrdinal ?: 0) + number - 1))
            cursor = breakEnd
        }
        val firstEnd = cursor + firstLength
        result.add(active[firstIndex].shifted(cursor.toInt(), firstEnd.toInt()))
        cursor = firstEnd
        val finalSpecial = specialCount > 0
        val finalBreakEnd = cursor + if (finalSpecial) special else short
        result.add(active[firstIndex + 1].copy(
            kind = if (finalSpecial) PhaseKind.SPECIAL_BREAK else PhaseKind.SHORT_BREAK,
            startSeconds = cursor.toInt(), endSeconds = finalBreakEnd.toInt()))
        cursor = finalBreakEnd
        val lastEnd = cursor + lastLength
        result.add(active[lastIndex].shifted(cursor.toInt(), lastEnd.toInt()))
        cursor = lastEnd
        for (phase in summary) {
            val end = cursor + phase.durationSeconds
            result.add(phase.shifted(cursor.toInt(), end.toInt()))
            cursor = end
        }
        require(cursor == total && cursor in 0L..Int.MAX_VALUE.toLong()) {
            "The Class finish would not be preserved."
        }
        return CompensatedTimeline(result, cursor.toInt(), newCorrectionSeconds,
            firstLength, lastLength)
    }
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
        val protectedSpecial = rest.kind == PhaseKind.SPECIAL_BREAK &&
            rest.durationSeconds > session.rules.shortBreakSeconds
        // A short pair has one Session, not two one-minute minimums. The
        // deleted break's time is turned into Session time, so the end clock
        // and every phase after the pair retain their exact position.
        if (!protectedSpecial && mergedLength <= session.rules.poseSeconds.toLong()) {
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
