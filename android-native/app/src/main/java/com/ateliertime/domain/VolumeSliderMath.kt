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
}
