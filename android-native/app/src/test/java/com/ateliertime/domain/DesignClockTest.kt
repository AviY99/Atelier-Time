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

    @Test fun workFillIncreasesAndBreakFillDecreases() {
        assertEquals(0f, DesignMotion.fillFraction(PhaseKind.POSE, 100_000L, 100_000L))
        assertEquals(0.5f, DesignMotion.fillFraction(PhaseKind.POSE, 100_000L, 50_000L))
        assertEquals(1f, DesignMotion.fillFraction(PhaseKind.POSE, 100_000L, 0L))
        assertEquals(1f, DesignMotion.fillFraction(PhaseKind.SHORT_BREAK, 100_000L, 100_000L))
        assertEquals(0.5f, DesignMotion.fillFraction(PhaseKind.SHORT_BREAK, 100_000L, 50_000L))
        assertEquals(0f, DesignMotion.fillFraction(PhaseKind.SPECIAL_BREAK, 100_000L, 0L))
    }

    @Test fun zeroDurationAndSummaryHaveNoFillAndFractionsClamp() {
        assertEquals(0f, DesignMotion.fillFraction(PhaseKind.POSE, 0L, 0L))
        assertEquals(0f, DesignMotion.fillFraction(PhaseKind.SUMMARY, 100L, 20L))
        assertEquals(0f, DesignMotion.fillFraction(PhaseKind.POSE, 100L, 150L))
        assertEquals(1f, DesignMotion.fillFraction(PhaseKind.POSE, 100L, -10L))
        assertEquals(1f, DesignMotion.fillFraction(PhaseKind.SHORT_BREAK, 100L, 150L))
    }

    @Test fun horizontalPhaseBarStartsEmptyAndFillsWithElapsedTime() {
        // The bar tracks elapsed in BOTH session and break, never remaining.
        assertEquals(0f, DesignMotion.elapsedFraction(60_000L, 60_000L))
        assertEquals(0.25f, DesignMotion.elapsedFraction(60_000L, 45_000L))
        assertEquals(0.5f, DesignMotion.elapsedFraction(60_000L, 30_000L))
        assertEquals(1f, DesignMotion.elapsedFraction(60_000L, 0L))
        assertEquals(0f, DesignMotion.elapsedFraction(60_000L, 90_000L))
        assertEquals(1f, DesignMotion.elapsedFraction(60_000L, -1L))
        assertEquals(0f, DesignMotion.elapsedFraction(0L, 15_000L))
        // Background has deliberately different behavior per phase kind.
        assertEquals(0.25f, DesignMotion.fillFraction(PhaseKind.POSE, 60_000L, 45_000L))
        assertEquals(0.75f, DesignMotion.fillFraction(PhaseKind.SHORT_BREAK, 60_000L, 45_000L))
    }

    @Test fun corruptedHourClampsToValidRange() {
        val before = DesignPreferences(DesignMode.AUTO, 5)
        val after = DesignPreferences(DesignMode.AUTO, 27)
        assertEquals(DesignPalette.NIGHT, DesignClock.resolve(before, 12))
        assertEquals(DesignPalette.DAY, DesignClock.resolve(after, 22))
        assertEquals(DesignPalette.NIGHT, DesignClock.resolve(after, 23))
    }
}
