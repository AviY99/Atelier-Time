package com.ateliertime.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/** Theme selection must not depend on Compose or the Android clock. */
class DesignClockTest {
    @Test fun dayModeOverridesEveryHour() {
        for (hour in 0..23) {
            assertEquals(DesignPalette.DAY,
                DesignClock.resolve(DesignPreferences(DesignMode.DAY, 19), hour))
        }
    }

    @Test fun nightModeOverridesEveryHour() {
        for (hour in 0..23) {
            assertEquals(DesignPalette.NIGHT,
                DesignClock.resolve(DesignPreferences(DesignMode.NIGHT, 19), hour))
        }
    }

    @Test fun autoSwitchesOnBoundaryAndReturnsToDayAtSix() {
        val config = DesignPreferences(DesignMode.AUTO, 19)
        assertEquals(DesignPalette.NIGHT, DesignClock.resolve(config, 5))
        assertEquals(DesignPalette.DAY, DesignClock.resolve(config, 6))
        assertEquals(DesignPalette.DAY, DesignClock.resolve(config, 18))
        assertEquals(DesignPalette.NIGHT, DesignClock.resolve(config, 19))
        assertEquals(DesignPalette.NIGHT, DesignClock.resolve(config, 23))
        assertEquals(DesignPalette.NIGHT, DesignClock.resolve(config, 0))
    }

    @Test fun changingSliderHourChangesOnlyNightTransition() {
        val early = DesignPreferences(DesignMode.AUTO, 16)
        val late = DesignPreferences(DesignMode.AUTO, 22)
        assertEquals(DesignPalette.NIGHT, DesignClock.resolve(early, 18))
        assertEquals(DesignPalette.DAY, DesignClock.resolve(late, 18))
        assertEquals(DesignPalette.NIGHT, DesignClock.resolve(late, 22))
    }

    @Test fun corruptedHourClampsToValidRange() {
        val before = DesignPreferences(DesignMode.AUTO, 5)
        val after = DesignPreferences(DesignMode.AUTO, 27)
        assertEquals(DesignPalette.NIGHT, DesignClock.resolve(before, 12))
        assertEquals(DesignPalette.DAY, DesignClock.resolve(after, 22))
        assertEquals(DesignPalette.NIGHT, DesignClock.resolve(after, 23))
    }
}
