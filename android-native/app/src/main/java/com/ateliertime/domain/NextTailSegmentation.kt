package com.ateliertime.domain

/**
 * A Next may create more work than one unfinished regular Session can hold.
 * Split ONLY a future final Session into full regular Session+Break blocks and
 * a >= 60-second residual final Session. In particular, this works AFTER the
 * original first final Session and its Break have become observed history,
 * when rebuilding the old final pair is no longer legal.
 *
 * Never used by Restart or Resume: these retain the existing runtime layout.
 * The original last Session ID stays on the final residual Session so all
 * "last final Session" and protected-history rules keep their meaning.
 */
object NextTailSegmentation {
    fun splitUnstartedFinal(
        session: ApprovedSession,
        proposed: List<SchedulePhase>,
        firstUnstartedIndex: Int,
        signedCorrectionSeconds: Int,
    ): List<SchedulePhase> {
        // No Next credit means there is no earned work to turn into new blocks.
        if (signedCorrectionSeconds >= 0 || firstUnstartedIndex !in proposed.indices) {
            return proposed
        }
        val originalLast = session.phases.lastOrNull {
            it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
        } ?: return proposed
        val lastIndex = proposed.indexOfFirst { it.id == originalLast.id }
        if (lastIndex < firstUnstartedIndex) return proposed
        val last = proposed[lastIndex]
        if (last.kind != PhaseKind.POSE || last.poseSource != PoseSource.REGULAR ||
            proposed.drop(lastIndex + 1).any { it.kind != PhaseKind.SUMMARY }) return proposed

        val regular = session.rules.poseSeconds.toLong()
        val breakSeconds = session.rules.shortBreakSeconds.toLong()
        val block = regular + breakSeconds
        val residualMin = SECONDS_PER_MINUTE.toLong()
        if (regular < residualMin || breakSeconds <= 0L || block <= 0L ||
            last.durationSeconds.toLong() < block + residualMin) return proposed

        val blocks = ((last.durationSeconds.toLong() - residualMin) / block)
            .coerceAtMost(1_000L).toInt()
        if (blocks == 0) return proposed

        val existingIds = proposed.map { it.id }.toHashSet()
        var serial = 1
        fun newPairId(prefix: String): String {
            while ("runtime-tail-pose-$serial" in existingIds ||
                "runtime-tail-break-$serial" in existingIds) serial++
            return "$prefix$serial"
        }

        val result = proposed.take(lastIndex).toMutableList()
        var cursor = last.startSeconds.toLong()
        val firstNumber = last.poseNumber ?: 1
        repeat(blocks) { i ->
            val poseId = newPairId("runtime-tail-pose-")
            val breakId = "runtime-tail-break-$serial"
            existingIds += poseId
            existingIds += breakId
            serial++
            val poseEnd = cursor + regular
            val breakEnd = poseEnd + breakSeconds
            require(breakEnd <= Int.MAX_VALUE.toLong()) {
                "An additional regular block exceeds the supported timeline."
            }
            result += SchedulePhase(
                id = poseId,
                kind = PhaseKind.POSE,
                startSeconds = cursor.toInt(),
                endSeconds = poseEnd.toInt(),
                poseNumber = firstNumber + i,
                poseSource = PoseSource.REGULAR,
            )
            result += SchedulePhase(
                id = breakId,
                kind = PhaseKind.SHORT_BREAK,
                startSeconds = poseEnd.toInt(),
                endSeconds = breakEnd.toInt(),
            )
            cursor = breakEnd
        }
        val lastEnd = last.endSeconds.toLong()
        require(lastEnd - cursor >= residualMin) {
            "The final regular Session must retain its one-minute minimum."
        }
        result += last.copy(startSeconds = cursor.toInt(),
            poseNumber = firstNumber + blocks)
        result += proposed.drop(lastIndex + 1)

        // Only future phase structure changes; no elapsed work, original Class
        // finish, duration total, or protected genuine Special Break is touched.
        check(result.take(firstUnstartedIndex) ==
            proposed.take(firstUnstartedIndex))
        check(result.last().endSeconds == proposed.last().endSeconds)
        check(result.zipWithNext().all { (a, b) -> a.endSeconds == b.startSeconds })
        check(result.map { it.id }.distinct().size == result.size)
        return result
    }
}
