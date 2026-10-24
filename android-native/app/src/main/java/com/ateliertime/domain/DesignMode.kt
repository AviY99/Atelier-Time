package com.ateliertime.domain

/** User appearance choice. No UI or Android dependencies. */
enum class DesignMode { DAY, NIGHT, AUTO }
enum class DesignPalette { DAY, NIGHT }

data class DesignPreferences(
    val mode: DesignMode = DesignMode.DAY,
    /** Automatic NIGHT start hour (12:00–23:00). DAY resumes at 06:00. */
    val nightStartsAtHour: Int = 19,
)

object DesignClock {
    const val DAY_START_HOUR = 6
    const val MIN_NIGHT_HOUR = 12
    const val MAX_NIGHT_HOUR = 23

    /**
     * The Auto slider is a visual preview: left half = Day, right half =
     * Night. It must NOT use the real clock (which runs in the opposite
     * direction as the selected night-start threshold moves).
     * Automatic scheduling itself continues to use resolve().
     */
    fun sliderPreview(hour: Int): DesignPalette {
        val threshold = (MIN_NIGHT_HOUR + MAX_NIGHT_HOUR + 1) / 2
        return if (hour.coerceIn(MIN_NIGHT_HOUR, MAX_NIGHT_HOUR) < threshold)
            DesignPalette.DAY else DesignPalette.NIGHT
    }

    /**
     * Auto slider visual preview: left half = Day; right half = Night.
     * This is independent of actual clock-based Auto scheduling.
     */
    fun previewForSlider(startHour: Int): DesignPalette {
        val safeHour = startHour.coerceIn(MIN_NIGHT_HOUR, MAX_NIGHT_HOUR)
        val midpoint = (MIN_NIGHT_HOUR + MAX_NIGHT_HOUR) / 2.0
        return if (safeHour <= midpoint) DesignPalette.DAY else DesignPalette.NIGHT
    }

    fun resolve(preferences: DesignPreferences, hourOfDay: Int): DesignPalette {
        val hour = hourOfDay.coerceIn(0, 23)
        return when (preferences.mode) {
            DesignMode.DAY -> DesignPalette.DAY
            DesignMode.NIGHT -> DesignPalette.NIGHT
            DesignMode.AUTO -> {
                val sunset = preferences.nightStartsAtHour.coerceIn(
                    MIN_NIGHT_HOUR, MAX_NIGHT_HOUR,
                )
                if (hour >= DAY_START_HOUR && hour < sunset) {
                    DesignPalette.DAY
                } else DesignPalette.NIGHT
            }
        }
    }
}

/** Presentation-only fill level: work fills, rest drains. No clock is changed. */
object DesignMotion {
    fun fillFraction(kind: PhaseKind?, durationMillis: Long, remainingMillis: Long): Float {
        if (durationMillis <= 0L) return 0f
        val isBreak = kind == PhaseKind.SHORT_BREAK || kind == PhaseKind.SPECIAL_BREAK
        if (kind != PhaseKind.POSE && !isBreak) return 0f
        val remaining = remainingMillis.coerceIn(0L, durationMillis)
        val elapsedRatio = 1f - remaining.toFloat() / durationMillis.toFloat()
        return if (isBreak) 1f - elapsedRatio else elapsedRatio
    }
    /**
     * Both Session and Break progress bars represent ELAPSED phase time:
     * empty on start, progressively fill to 100% on phase completion.
     * The background is intentionally independent: Session fills, Break drains.
     */
    fun elapsedFraction(durationMillis: Long, remainingMillis: Long): Float =
        if (durationMillis <= 0L) 0f
        else 1f - remainingMillis.coerceIn(0L, durationMillis).toFloat() /
            durationMillis.toFloat()

}
