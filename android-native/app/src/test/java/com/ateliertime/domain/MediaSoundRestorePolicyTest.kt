package com.ateliertime.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MediaSoundRestorePolicyTest {
    @Test fun lastPositiveVolumeRestoresExactAndroidIndex() {
        assertEquals(6, MediaSoundRestorePolicy.restoreLevel(6, 15))
    }

    @Test fun noSavedVolumeDefaultsToQuarterOfStream() {
        assertEquals(4, MediaSoundRestorePolicy.restoreLevel(null, 15))
        assertEquals(5, MediaSoundRestorePolicy.restoreLevel(0, 20))
    }

    @Test fun noHistoryAlwaysReturnsAudibleIndexEvenWhenMaxIsSmall() {
        assertEquals(1, MediaSoundRestorePolicy.restoreLevel(null, 1))
        assertEquals(1, MediaSoundRestorePolicy.restoreLevel(0, 3))
    }

    @Test fun staleRememberedVolumeClampsToCurrentDeviceMaximum() {
        assertEquals(7, MediaSoundRestorePolicy.restoreLevel(15, 7))
    }

    @Test fun invalidNegativeMemoryFallsBackToQuarter() {
        assertEquals(3, MediaSoundRestorePolicy.restoreLevel(-1, 12))
    }

    @Test fun invalidMaximumIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            MediaSoundRestorePolicy.restoreLevel(5, 0)
        }
    }
}
