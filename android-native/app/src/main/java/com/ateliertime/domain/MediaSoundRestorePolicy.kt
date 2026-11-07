package com.ateliertime.domain

/**
 * D155 - shared restore rule for Sound ON in the selected Media OR Alarm
 * Android stream, if that stream can actually reach zero. Restore the last
 * positive device index or 25% if none is remembered, never a zero step.
 * Hardware-specific minimum volume is applied by ClassVolumeControl.
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
