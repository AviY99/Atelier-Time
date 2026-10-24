package com.ateliertime.android

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the Android audio stack, not a mocked state machine.
 * This specifically guards against rejecting STATE_NO_STATIC_DATA before
 * a MODE_STATIC PCM clip has been loaded (the real-phone K04 bug).
 *
 * Intentionally uses a short silent clip; audible 5-second melodies and
 * automatic phase alerts still require user observation on the device.
 */
@RunWith(AndroidJUnit4::class)
class StaticAudioTrackLifecycleTest {
    @Test fun staticTrackLoadsPcmBeforeItBecomesInitialized() {
        val clip = ShortArray(22_050 / 4)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(22_050)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(clip.size * 2)
            .build()
        try {
            assertNotEquals(AudioTrack.STATE_UNINITIALIZED, track.state)
            // STATE_NO_STATIC_DATA (2) before writing is NOT an audio failure.
            assertEquals(AudioTrack.STATE_NO_STATIC_DATA, track.state)
            assertEquals(
                clip.size,
                track.write(clip, 0, clip.size, AudioTrack.WRITE_BLOCKING),
            )
            assertEquals(AudioTrack.STATE_INITIALIZED, track.state)
            track.play()
        } finally {
            runCatching { track.stop() }
            track.release()
        }
    }
}
