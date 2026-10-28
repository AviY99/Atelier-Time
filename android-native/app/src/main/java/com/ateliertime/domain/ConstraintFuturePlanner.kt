package com.ateliertime.domain

import kotlin.math.roundToInt

/**
 * ONE constraint-driven runtime allocation path.
 *
 * Inputs: an immutable observed prefix, an optional Next entry boundary,
 * the CURRENT runtime schedule, and the signed monotonic Class budget.
 *
 * Output: a complete, contiguous proposed schedule, or rejection without
 * changing the running Class. It never reads a user-specific preset name.
 *
 * Next may add regular work/rest cycles in the UNSTARTED suffix. Restart and
 * Resume may change the remaining length of the ending only, not its topology.
 * A genuinely long Special Break is always protected, including a manually
 * placed one. L<=B retains its configured duration but has ordinary merging
 * privileges.
 */
internal object ConstraintFuturePlanner {
    fun plan(
        session: ApprovedSession,
        active: List<SchedulePhase>,
        correctionSeconds: Int,
        allowExtraRegular: Boolean,
        boundary: ReflowBoundary,
    ): CompensatedTimeline {
        require(active.isNotEmpty()) { "The Class has no future to plan." }
        val total = session.rules.totalSeconds.toLong() - correctionSeconds.toLong()
        require(total in 0L..Int.MAX_VALUE.toLong()) {
            "The requested Class adjustment exceeds the supported time range."
        }
        val delta = total - active.last().endSeconds.toLong()
        val regularPositions = active.indices.filter {
            active[it].kind == PhaseKind.POSE &&
                active[it].poseSource == PoseSource.REGULAR
        }
        require(regularPositions.isNotEmpty()) {
            "No regular Session is available for a safe ending adjustment."
        }
        val last = regularPositions.last()
        val first = regularPositions.getOrNull(regularPositions.size - 2)
        val rest = first?.let { active.getOrNull(it + 1) }
        val pairAvailable = first != null && last == first + 2 &&
            rest != null && rest.kind in setOf(PhaseKind.SHORT_BREAK,
                PhaseKind.SPECIAL_BREAK)
        val lock = boundary.protectedThroughIndex
        val entering = boundary.nextIndex
        val mayRebuild = entering != null && !boundary.noActiveTailRebuild &&
            !boundary.preserveExistingTail && allowExtraRegular
        val pairFlexible = pairAvailable && first!! > lock &&
            (entering == null || entering < first)
        val proposed = when {
            pairFlexible -> allocatePair(session, active, first!!, last, rest!!,
                delta, correctionSeconds, mayRebuild)
            last > lock || entering == last -> allocateLast(session, active, last,
                delta, mayRebuild)
            delta == 0L -> active
            else -> throw IllegalArgumentException(
                "No unstarted Session remains to absorb the requested change.")
        }
        validate(session, active, proposed, correctionSeconds, total, boundary)
        val finalRegular = proposed.filter {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        }.takeLast(2)
        return CompensatedTimeline(
            phases = proposed, totalSeconds = total.toInt(),
            recoveredSeconds = correctionSeconds,
            firstFinalPoseSeconds = finalRegular.firstOrNull()?.durationSeconds ?: 0,
            lastFinalPoseSeconds = finalRegular.getOrNull(1)?.durationSeconds ?: 0,
        )
    }

    private fun allocatePair(
        session: ApprovedSession,
        active: List<SchedulePhase>,
        firstIndex: Int,
        lastIndex: Int,
        between: SchedulePhase,
        delta: Long,
        correction: Int,
        mayRebuild: Boolean,
    ): List<SchedulePhase> {
        val first = active[firstIndex]
        val last = active[lastIndex]
        val work = first.durationSeconds.toLong() + last.durationSeconds + delta
        val p = session.rules.poseSeconds.toLong()
        val b = session.rules.shortBreakSeconds.toLong()
        val min = SECONDS_PER_MINUTE.toLong()
        val genuine = between.kind == PhaseKind.SPECIAL_BREAK &&
            between.durationSeconds > session.rules.shortBreakSeconds

        // The ONLY allowed merge: an ordinary/equal-length intervening Break
        // becomes work inside one legal regular Session. Never merge a genuine
        // Special Break or an already started phase.
        if (!genuine && work + between.durationSeconds <= p &&
            work + between.durationSeconds >= min) {
            val merged = first.copy(endSeconds =
                (first.startSeconds.toLong() + work + between.durationSeconds).toInt())
            return retime(active.take(firstIndex) + merged +
                active.drop(lastIndex + 1), firstIndex)
        }
        require(work >= 2L * min && work <= Int.MAX_VALUE.toLong()) {
            "The two remaining Sessions cannot keep their one-minute minimum."
        }

        // The strict > threshold is an approved product rule for adding whole
        // Session+Break cycles BEFORE the ending pair. No double charge for the
        // already existing intervening Break: work excludes that Break.
        var blocks = 0
        var tailWork = work
        if (mayRebuild && correction < 0) {
            while (tailWork > 2L * p + b &&
                tailWork - (p + b) >= 2L * min) {
                blocks++
                require(blocks <= 1_000) { "Too many additional Sessions." }
                tailWork -= p + b
            }
        }

        val firstLength = distributeFirst(session, first, last,
            tailWork.toInt())
        val lastLength = tailWork.toInt() - firstLength
        require(firstLength >= min && lastLength >= min)
        val result = active.take(firstIndex).toMutableList()
        val used = active.map { it.id }.toMutableSet()
        var cursor = first.startSeconds.toLong()
        var serial = 1
        repeat(blocks) {
            while ("runtime-added-pose-$serial" in used ||
                "runtime-added-break-$serial" in used) serial++
            val poseId = "runtime-added-pose-$serial"
            val breakId = "runtime-added-break-$serial"
            used += poseId
            used += breakId
            val poseEnd = cursor + p
            val breakEnd = poseEnd + b
            result += SchedulePhase(
                id = poseId, kind = PhaseKind.POSE,
                startSeconds = cursor.toInt(), endSeconds = poseEnd.toInt(),
                poseNumber = (first.poseNumber ?: 1) + it,
                poseSource = PoseSource.REGULAR,
            )
            result += SchedulePhase(
                id = breakId, kind = PhaseKind.SHORT_BREAK,
                startSeconds = poseEnd.toInt(), endSeconds = breakEnd.toInt(),
            )
            cursor = breakEnd
            serial++
        }
        result += first.copy(startSeconds = cursor.toInt(),
            endSeconds = (cursor + firstLength).toInt(),
            poseNumber = first.poseNumber?.plus(blocks))
        cursor += firstLength
        result += between.copy(startSeconds = cursor.toInt(),
            endSeconds = (cursor + between.durationSeconds).toInt())
        cursor += between.durationSeconds
        result += last.copy(startSeconds = cursor.toInt(),
            endSeconds = (cursor + lastLength).toInt(),
            poseNumber = last.poseNumber?.plus(blocks))
        result += active.drop(lastIndex + 1)
        return retime(result, firstIndex)
    }

    private fun allocateLast(
        session: ApprovedSession,
        active: List<SchedulePhase>,
        index: Int,
        delta: Long,
        mayRebuild: Boolean,
    ): List<SchedulePhase> {
        val last = active[index]
        val length = last.durationSeconds.toLong() + delta
        val min = SECONDS_PER_MINUTE.toLong()
        require(length in min..Int.MAX_VALUE.toLong()) {
            "The last unstarted Session cannot absorb the change."
        }
        val p = session.rules.poseSeconds.toLong()
        val b = session.rules.shortBreakSeconds.toLong()
        val result = active.take(index).toMutableList()
        var cursor = last.startSeconds.toLong()
        var remaining = length
        val used = active.map { it.id }.toMutableSet()
        var serial = 1
        // All cycles are produced by this same allocator, not by a late-Next
        // repair pass. Preserve the entered phase's ID and original start.
        var firstPiece = true
        while (mayRebuild && remaining >= p + b + min && p >= min && b > 0) {
            require(serial <= 1_000) { "Too many additional Sessions." }
            while ("runtime-tail-pose-$serial" in used ||
                "runtime-tail-break-$serial" in used) serial++
            val id = if (firstPiece) last.id else "runtime-tail-pose-$serial"
            result += last.copy(
                id = id, startSeconds = cursor.toInt(),
                endSeconds = (cursor + p).toInt())
            cursor += p
            remaining -= p
            result += SchedulePhase(
                id = "runtime-tail-break-$serial", kind = PhaseKind.SHORT_BREAK,
                startSeconds = cursor.toInt(), endSeconds = (cursor + b).toInt())
            cursor += b
            remaining -= b
            used += "runtime-tail-pose-$serial"
            used += "runtime-tail-break-$serial"
            firstPiece = false
            serial++
        }
        val endId = if (firstPiece) last.id else {
            while ("runtime-tail-pose-$serial" in used ||
                "runtime-tail-break-$serial" in used) serial++
            "runtime-tail-pose-$serial"
        }
        result += last.copy(id = endId, startSeconds = cursor.toInt(),
            endSeconds = (cursor + remaining).toInt())
        result += active.drop(index + 1)
        return retime(result, index)
    }

    private fun distributeFirst(
        session: ApprovedSession,
        first: SchedulePhase,
        last: SchedulePhase,
        budget: Int,
    ): Int {
        val minimum = SECONDS_PER_MINUTE
        require(budget >= 2 * minimum)
        val sum = first.durationSeconds.toLong() + last.durationSeconds.toLong()
        return when (session.selectedChoice?.mode ?: FinalPoseMode.REMAINDER) {
            FinalPoseMode.REMAINDER ->
                minOf(first.durationSeconds, budget - minimum)
            FinalPoseMode.EVEN_SPLIT -> budget / 2
            FinalPoseMode.CUSTOM_SPLIT ->
                (budget.toDouble() * first.durationSeconds / sum).roundToInt()
        }.coerceIn(minimum, budget - minimum)
    }

    /** Re-time only the proposed suffix. The observed prefix is unchanged. */
    private fun retime(phases: List<SchedulePhase>, from: Int): List<SchedulePhase> {
        val result = phases.toMutableList()
        var cursor = if (from == 0) 0L else result[from - 1].endSeconds.toLong()
        for (i in from until result.size) {
            val phase = result[i]
            val end = cursor + phase.durationSeconds
            require(end in 0L..Int.MAX_VALUE.toLong()) {
                "Class timeline exceeds the supported time range."
            }
            result[i] = phase.shifted(cursor.toInt(), end.toInt())
            cursor = end
        }
        return result
    }

    private fun validate(
        session: ApprovedSession,
        active: List<SchedulePhase>,
        phases: List<SchedulePhase>,
        correction: Int,
        total: Long,
        boundary: ReflowBoundary,
    ) {
        require(phases.isNotEmpty() && phases.first().startSeconds == 0 &&
            phases.last().endSeconds.toLong() == total &&
            phases.map { it.id }.distinct().size == phases.size &&
            phases.all { it.durationSeconds > 0 &&
                (it.kind != PhaseKind.POSE ||
                    it.durationSeconds >= SECONDS_PER_MINUTE) } &&
            phases.zipWithNext().all { (a, b) ->
                a.endSeconds == b.startSeconds
            }) { "The future plan is not a continuous legal Class timeline." }
        require(phases.filter { it.kind == PhaseKind.SUMMARY }
            .sumOf { it.durationSeconds } == session.rules.summarySeconds) {
            "The approved Summary duration must be preserved."
        }
        val locked = boundary.protectedThroughIndex
        require(locked < 0 || (locked in active.indices &&
            phases.take(locked + 1) == active.take(locked + 1))) {
            "Previously entered Class phases cannot be rewritten."
        }
        boundary.nextIndex?.let { target ->
            val before = active.getOrNull(target)
            val after = phases.getOrNull(target)
            require(before != null && after != null &&
                before.id == after.id && before.kind == after.kind &&
                before.startSeconds == after.startSeconds) {
                "Next must enter the same phase at the same virtual start."
            }
        }
        if (boundary.preserveExistingTail) {
            require(phases.size == active.size &&
                phases.indices.all { index ->
                    phases[index].id == active[index].id &&
                    phases[index].kind == active[index].kind &&
                    (phases[index].kind == PhaseKind.POSE ||
                        phases[index].durationSeconds ==
                            active[index].durationSeconds)
                }) {
                "Restart/Resume cannot rebuild a previously accepted future."
            }
        }
        if (session.rules.specialBreakSeconds > session.rules.shortBreakSeconds) {
            val old = active.filter { it.kind == PhaseKind.SPECIAL_BREAK &&
                it.durationSeconds > session.rules.shortBreakSeconds }
            val future = phases.filter { it.kind == PhaseKind.SPECIAL_BREAK &&
                it.durationSeconds > session.rules.shortBreakSeconds }
            require(old.size == future.size &&
                (!session.rules.placement.automatic ||
                    old.map { it.id } == future.map { it.id })) {
                "A protected Special Break cannot disappear or change identity."
            }
        }
        val remaining = boundary.physicalRemainingMillis
        val cursor = boundary.virtualCursorMillis
        if (remaining != null || cursor != null) {
            require(remaining != null && cursor != null && remaining >= 0L &&
                kotlin.math.abs(total * 1_000L - cursor - remaining) < 2_000L) {
                "The approved physical Class finish must stay fixed."
            }
        }
        require(total == session.rules.totalSeconds.toLong() - correction) {
            "Invalid signed time correction."
        }
    }
}
