package com.ateliertime.domain

/**
 * D155 - sound ON in Media mode restores the last real, positive device
 * stream index. When no valid history exists, pick one quarter of the real
 * stream's maximum, clamped to a positive Android volume step.
 *
 * Keep stream INDEX rather than a UI percentage to restore exact hardware
 * levels (the UI percent can be rounded by Android's volume step size).
 */
object MediaSoundRestorePolicy {
    const val DEFAULT_PERCENT = 25

    fun restoreLevel(lastPositive: Int?, streamMax: Int): Int {
        require(streamMax > 0) { "Media stream maximum must be positive." }
        return if (lastPositive != null && lastPositive > 0) {
            lastPositive.coerceAtMost(streamMax)
        } else {
            VolumeSliderMath.level(DEFAULT_PERCENT, streamMax).coerceIn(1, streamMax)
        }
    }
}
