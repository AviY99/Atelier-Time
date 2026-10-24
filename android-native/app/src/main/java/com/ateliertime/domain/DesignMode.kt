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
