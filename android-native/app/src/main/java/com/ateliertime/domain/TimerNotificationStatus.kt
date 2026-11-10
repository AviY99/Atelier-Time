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
        rules: SessionRules? = null,
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
        // Match the group-aware numbering already used by the running Class screen.
        val phaseTitle = rules?.let { PhaseDisplayNames.title(view.phase, it) }
            ?: when (view.phase?.kind) {
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

/** Presentation-only Free Timer notification; no independent clock or sound. */
object FreeTimerNotificationFormatter {
    fun forActiveFree(view: FreeTimerView): TimerNotificationStatus {
        require(view.status == FreeTimerStatus.RUNNING ||
            view.status == FreeTimerStatus.PAUSED)
        val state = if (view.status == FreeTimerStatus.RUNNING) "Running" else "Paused"
        val seconds = view.remainingSeconds.coerceAtLeast(0)
        val hours = seconds / 3600
        val minutes = (seconds / 60) % 60
        val remainder = seconds % 60
        val time = if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, remainder)
            else "%02d:%02d".format(minutes, remainder)
        return TimerNotificationStatus(
            title = "Atelier Time · Free Timer · $state",
            summary = "Free Timer · $time left",
            detail = "Free Timer\n$state · $time remaining",
            countdownMillis = if (view.status == FreeTimerStatus.RUNNING)
                seconds.toLong() * 1_000L else null,
        )
    }
}

