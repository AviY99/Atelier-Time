package com.ateliertime.domain

import java.util.Calendar
import java.util.TimeZone

/**
 * Converts an explicitly approved Run finish into a signed adjustment of the
 * final two regular Sessions. An impossible plan never starts the Class.
 *
 * Clock-only input refers to the next occurrence of that time (today or
 * tomorrow); the UI presents the resolved calendar date before confirmation.
 * Saved Classes and the approved schedule are never modified.
 */
data class RunEndTimePlan(
    val targetEndEpochMs: Long,
    val adjustedDurationSeconds: Int,
    val correctionSeconds: Int,
    val adjusted: CompensatedTimeline,
)

object RunEndTimePlanner {
    fun suggestedEndEpochMs(session: ApprovedSession, nowEpochMs: Long): Long =
        Math.addExact(nowEpochMs, session.rules.totalSeconds.toLong() * 1_000L)

    fun nextOccurrence(
        input: String,
        nowEpochMs: Long,
        zone: TimeZone = TimeZone.getDefault(),
    ): Long {
        val match = Regex("""^([01]?\d|2[0-3]):([0-5]\d)$""")
            .matchEntire(input.trim()) ?: throw IllegalArgumentException(
            "Enter a valid finish time as HH:mm (00:00–23:59)."
        )
        val hour = match.groupValues[1].toInt()
        val minute = match.groupValues[2].toInt()
        val calendar = Calendar.getInstance(zone).apply {
            timeInMillis = nowEpochMs
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= nowEpochMs) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis
    }

    fun plan(
        session: ApprovedSession,
        nowEpochMs: Long,
        targetEndEpochMs: Long,
    ): RunEndTimePlan {
        val remainingMillis = targetEndEpochMs - nowEpochMs
        require(remainingMillis > 0) {
            "The finish time must be in the future. Choose another time."
        }
        // The whole-second phase plan is rounded down; the caller separately
        // passes the exact millisecond target to the runtime timer so the
        // physical Class deadline is never rounded early.
        val plannedSeconds = remainingMillis / 1_000L
        require(plannedSeconds in 1L..Int.MAX_VALUE.toLong()) {
            "The selected finish time is outside the supported range."
        }
        // Explicit feasibility policy for late clock choices: a 3-hour
        // Class cannot silently turn into an overnight marathon because a
        // user entered a clock that has already passed today. The maximum
        // full Class length is twice its approved length; the actual added
        // time still goes ONLY into the final two regular Sessions.
        require(plannedSeconds <= session.rules.totalSeconds.toLong() * 2L) {
            "This finish is too late: a Class may be extended by at most " +
                "its original duration. Choose a closer finish time."
        }
        val difference = session.rules.totalSeconds.toLong() - plannedSeconds
        require(difference in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            "The selected finish time is outside the supported range."
        }
        // Preview and real start call the SAME initial constraint planner.
        // Added time can form complete regular Sessions and Breaks, rather
        // than stretching just the final pair. A no-pair legacy Class still
        // runs at its original end without changing its saved schedule.
        val adjustment = if (difference == 0L) {
            val pair = session.phases.filter {
                it.kind == PhaseKind.POSE && it.poseSource == PoseSource.REGULAR
            }.takeLast(2)
            runCatching { FinalPairCompensation.reflowInitialFinish(session, 0) }
                .getOrNull() ?: CompensatedTimeline(
                phases = session.phases,
                totalSeconds = session.rules.totalSeconds,
                recoveredSeconds = 0,
                firstFinalPoseSeconds = pair.getOrNull(0)?.durationSeconds ?: 0,
                lastFinalPoseSeconds = pair.getOrNull(1)?.durationSeconds ?: 0,
            )
        } else try {
            FinalPairCompensation.reflowInitialFinish(session, difference.toInt())
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException(
                "This finish time cannot fit the final Session(s) and Breaks. " +
                    "Choose another finish time."
            )
        }
        require(adjustment.totalSeconds.toLong() == plannedSeconds) {
            "The selected finish time cannot be reached by adjusting only " +
                "the final two Sessions."
        }
        return RunEndTimePlan(
            targetEndEpochMs = targetEndEpochMs,
            adjustedDurationSeconds = adjustment.totalSeconds,
            correctionSeconds = difference.toInt(),
            adjusted = adjustment,
        )
    }
}
