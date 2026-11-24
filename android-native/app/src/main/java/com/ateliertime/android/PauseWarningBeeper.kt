package com.ateliertime.android

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * D161 warning sound: the caller must beep repeatedly UNTIL the user closes
 * the red warning dialog. Uses the selected Android sound stream and always
 * releases native resources on dismissal / lifecycle disposal.
 */
internal class PauseWarningBeeper(useAlarmVolume: Boolean) : AutoCloseable {
    private val tone: ToneGenerator? = runCatching {
        ToneGenerator(
            if (useAlarmVolume) AudioManager.STREAM_ALARM else AudioManager.STREAM_MUSIC,
            100,
        )
    }.getOrNull()

    fun beep() {
        runCatching { tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 375) }
    }

    override fun close() {
        runCatching { tone?.stopTone() }
        runCatching { tone?.release() }
    }
}
