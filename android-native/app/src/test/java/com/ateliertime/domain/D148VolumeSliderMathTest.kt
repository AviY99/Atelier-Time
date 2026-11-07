package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** D-148: the two actual device streams have different discrete max steps. */
class D148VolumeSliderMathTest {
    @Test fun zeroMeansSilentForBothStreams() {
        assertEquals(0, VolumeSliderMath.level(0, 15))
        assertEquals(0, VolumeSliderMath.level(0, 7))
    }

    @Test fun fullVolumeMeansTheActualAndroidStreamMaximum() {
        assertEquals(15, VolumeSliderMath.level(100, 15))
        assertEquals(7, VolumeSliderMath.level(100, 7))
    }

    @Test fun seventyFivePercentRoundsToNearestHardwareStep() {
        assertEquals(11, VolumeSliderMath.level(75, 15))
        assertEquals(5, VolumeSliderMath.level(75, 7))
    }

    @Test fun slidersClampRequestedVolumeToValidPercentRange() {
        assertEquals(0, VolumeSliderMath.level(-50, 15))
        assertEquals(15, VolumeSliderMath.level(150, 15))
    }

    @Test fun slidersHandleUnavailableStreamsWithoutArithmeticFailure() {
        assertEquals(0, VolumeSliderMath.level(80, 0))
        assertEquals(0, VolumeSliderMath.level(80, -5))
        assertEquals(0, VolumeSliderMath.percent(3, 0))
    }

    @Test fun sourcePercentRespectsTheDiscreteDeviceMax() {
        assertEquals(46, VolumeSliderMath.percent(7, 15))
        assertEquals(42, VolumeSliderMath.percent(3, 7))
        assertEquals(100, VolumeSliderMath.percent(7, 7))
    }

    @Test fun sourcePercentClampsOutOfRangeHardwareValues() {
        assertEquals(0, VolumeSliderMath.percent(-5, 15))
        assertEquals(100, VolumeSliderMath.percent(30, 15))
    }

    @Test fun displayedVolumeLevelsRoundTripForBothAndroidStreams() {
        for (max in listOf(1, 3, 7, 15, 25, 100)) {
            for (value in 0..max) {
                val displayed = VolumeSliderMath.percent(value, max)
                assertEquals("max=$max value=$value",
                    value, VolumeSliderMath.level(displayed, max))
            }
        }
    }
}
