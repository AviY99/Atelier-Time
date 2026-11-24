package com.ateliertime.domain

/**
 * D161: independent, monotonic Pause warnings. Each Pause episode may alarm
 * once, from strictly after its first full minute until the user closes it.
 * This policy deliberately owns neither a clock nor Android audio.
 */
data class PauseWarningState(
    val classWarning: Boolean = false,
    val freeWarning: Boolean = false,
) {
    val showing: Boolean get() = classWarning || freeWarning
}

class PauseWarningTracker {
    private var classPausedAtMs: Long? = null
    private var freePausedAtMs: Long? = null
    private var classAcknowledged = false
    private var freeAcknowledged = false
    private val thresholdMs = 60_000L

    fun observe(
        classPaused: Boolean,
        freePaused: Boolean,
        nowMonotonicMs: Long,
    ): PauseWarningState {
        if (!classPaused) {
            classPausedAtMs = null
            classAcknowledged = false
        } else if (classPausedAtMs == null) {
            classPausedAtMs = nowMonotonicMs
        }
        if (!freePaused) {
            freePausedAtMs = null
            freeAcknowledged = false
        } else if (freePausedAtMs == null) {
            freePausedAtMs = nowMonotonicMs
        }
        val classDue = classPausedAtMs?.let { nowMonotonicMs - it > thresholdMs }
            ?: false
        val freeDue = freePausedAtMs?.let { nowMonotonicMs - it > thresholdMs }
            ?: false
        return PauseWarningState(
            classWarning = classDue && !classAcknowledged,
            freeWarning = freeDue && !freeAcknowledged,
        )
    }

    /** Dismiss EVERY currently sounding warning, without resuming its clock. */
    fun dismissVisible(state: PauseWarningState) {
        if (state.classWarning) classAcknowledged = true
        if (state.freeWarning) freeAcknowledged = true
    }

    /** App teardown must silence UI/audio without falsely resuming timers. */
    fun reset() {
        classPausedAtMs = null
        freePausedAtMs = null
        classAcknowledged = false
        freeAcknowledged = false
    }
}
