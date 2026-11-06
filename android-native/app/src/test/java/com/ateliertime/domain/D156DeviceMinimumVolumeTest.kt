package com.ateliertime.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * D156: Android's lowest legal stream index is authoritative.
 * These pure tests cover both Class and Free Timer, and Media and Alarm.
 */
class D156DeviceMinimumVolumeTest {
    @Test fun mediaMuteReachesZeroWhenDeviceAllowsZero() {
        assertEquals(0, VolumeSliderMath.selectedTarget(0, 0, 15))
    }

    @Test fun protectedAlarmMuteReachesAndroidMinimumRatherThanRefusing() {
        assertEquals(1, VolumeSliderMath.selectedTarget(0, 1, 15))
        assertEquals(2, VolumeSliderMath.selectedTarget(0, 2, 15))
    }

    @Test fun protectedMediaUsesExactlyTheSameMinimumRule() {
        assertEquals(3, VolumeSliderMath.selectedTarget(0, 3, 15))
    }

    @Test fun tinyPositiveSliderMoveEscapesZeroOnLowStepHardware() {
        assertEquals(1, VolumeSliderMath.selectedTarget(1, 0, 15))
        assertEquals(1, VolumeSliderMath.selectedTarget(1, 0, 3))
    }

    @Test fun positiveSliderMoveRespectsProtectedMinimum() {
        assertEquals(2, VolumeSliderMath.selectedTarget(1, 2, 15))
    }

    @Test fun regularSliderRangeAndMaximumArePreserved() {
        assertEquals(4, VolumeSliderMath.selectedTarget(25, 0, 15))
        assertEquals(15, VolumeSliderMath.selectedTarget(100, 0, 15))
        assertEquals(15, VolumeSliderMath.selectedTarget(100, 1, 15))
    }

    @Test fun requestedNegativePercentIsClampedToMuteFloor() {
        assertEquals(2, VolumeSliderMath.selectedTarget(-10, 2, 15))
        assertEquals(0, VolumeSliderMath.selectedTarget(-10, 0, 15))
    }

    @Test fun invalidDeviceStreamRangeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            VolumeSliderMath.selectedTarget(0, 1, 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            VolumeSliderMath.selectedTarget(0, -1, 15)
        }
        assertThrows(IllegalArgumentException::class.java) {
            VolumeSliderMath.selectedTarget(0, 16, 15)
        }
    }
}
