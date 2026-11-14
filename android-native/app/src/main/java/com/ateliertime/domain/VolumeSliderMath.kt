package com.ateliertime.domain

import kotlin.math.roundToInt

/** Pure device-stream scale conversion; Android stream steps are integer-valued. */
object VolumeSliderMath {
    fun percent(level: Int, max: Int): Int =
        if (max <= 0) 0 else (level.coerceIn(0, max) * 100 / max)

    fun level(percent: Int, max: Int): Int =
        if (max <= 0) 0 else
            (percent.coerceIn(0, 100) * max / 100.0).roundToInt()
                .coerceIn(0, max)

    /**
     * D156 shared Class/Free selected-channel hardware target.
     * The phone, not our app, defines the lowest legal stream index.
     * A 0% request targets the real Android minimum, not a forbidden zero.
     * A positive slider request reaches at least one actual volume step.
     * This avoids a slider appearing stuck at zero on low-step devices.
     */
    fun selectedTarget(percent: Int, min: Int, max: Int): Int {
        require(max > 0 && min in 0..max) { "Invalid device audio range." }
        val floor = if (percent.coerceIn(0, 100) > 0)
            maxOf(1, min) else min
        return level(percent, max).coerceIn(floor, max)
    }
}
