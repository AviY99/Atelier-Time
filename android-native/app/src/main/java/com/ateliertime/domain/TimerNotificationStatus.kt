package com.ateliertime.domain

/**
 * Presentation-only notification text. Does not own a clock, scheduler, cue
 * playback or saved Class data. Platform adapters can reuse this formatting.
 */
data class TimerNotificationStatus(
    val title: String,
    val summary: String,
    val detail: String,
    val countdownMillis: Long?,
)

object TimerNotificationFormatter {
    fun forActiveClass(
        className: String,
        view: TimerView,
        classRemainingSeconds: Int,
    ): TimerNotificationStatus {
        require(view.status == TimerStatus.RUNNING ||
            view.status == TimerStatus.PAUSED ||
            view.status == TimerStatus.STOPPED)

        val state = when (view.status) {
            TimerStatus.RUNNING -> "Running"
            TimerStatus.PAUSED -> "Paused"
            TimerStatus.STOPPED -> "Stopped"
            else -> error("Only active classes have a status notification.")
        }
        val phaseTitle = when (view.phase?.kind) {
            PhaseKind.POSE -> "Session " + (view.phase?.poseNumber ?: "")
            PhaseKind.SHORT_BREAK -> if (view.phase.openingGroupId != null)
                "Opening break" else "Break"
            PhaseKind.SPECIAL_BREAK -> "Special break"
            PhaseKind.SUMMARY -> "Class summary"
            null -> "Class"
        }
        val phaseTime = duration(view.phaseRemainingSeconds)
        val classTime = duration(classRemainingSeconds)
        val safeName = className.ifBlank { "Class" }
        val summary = "$phaseTitle · $phaseTime left"
        return TimerNotificationStatus(
            title = "Atelier Time · $state",
            summary = summary,
            detail = "$safeName\n$summary\nClass remaining $classTime",
            countdownMillis = if (view.status == TimerStatus.RUNNING)
                (view.phaseRemainingMillis ?:
                    view.phaseRemainingSeconds.coerceAtLeast(0).toLong() * 1_000L)
                    .coerceAtLeast(0L)
            else null,
        )
    }

    private fun duration(seconds: Int): String {
        val remaining = seconds.coerceAtLeast(0)
        val hours = remaining / 3600
        val minutes = (remaining / 60) % 60
        val secs = remaining % 60
        return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, secs)
        else "%02d:%02d".format(minutes, secs)
    }
}
