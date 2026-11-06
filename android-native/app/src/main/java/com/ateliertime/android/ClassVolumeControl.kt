package com.ateliertime.android

import android.content.Context
import android.media.AudioManager
import com.ateliertime.domain.VolumeSliderMath

internal data class ClassVolumeLevels(
    val media: Int, val mediaMax: Int, val alarm: Int, val alarmMax: Int,
) {
    fun mediaPercent(): Int = VolumeSliderMath.percent(media, mediaMax)
    fun alarmPercent(): Int = VolumeSliderMath.percent(alarm, alarmMax)
}

/**
 * Phone-wide Android audio streams, changed ONLY by explicit user actions:
 * an individual D-148 slider release, or the existing confirmed Max both.
 *
 * Each Max-both override owns an independent restorable pre-override baseline.
 * A subsequent explicit slider change releases restoration ownership of ONLY
 * that stream, even if the user chooses maximum again. Hardware/other-app
 * changes also prevent restoration if the stream no longer equals our target.
 */
internal class ClassVolumeControl(context: Context) {
    private val audio = context.applicationContext
        .getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaBaseline: Int? = null
    private var alarmBaseline: Int? = null

    fun levels(): ClassVolumeLevels = ClassVolumeLevels(
        media = audio.getStreamVolume(AudioManager.STREAM_MUSIC),
        mediaMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
        alarm = audio.getStreamVolume(AudioManager.STREAM_ALARM),
        alarmMax = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM),
    )

    fun setMediaPercent(percent: Int): ClassVolumeLevels =
        setPercent(AudioManager.STREAM_MUSIC, percent)

    fun setAlarmPercent(percent: Int): ClassVolumeLevels =
        setPercent(AudioManager.STREAM_ALARM, percent)

    private fun setPercent(stream: Int, percent: Int): ClassVolumeLevels {
        val before = levels()
        val media = stream == AudioManager.STREAM_MUSIC
        val max = if (media) before.mediaMax else before.alarmMax
        val current = if (media) before.media else before.alarm
        check(max > 0) { "The Android volume stream is unavailable." }
        val target = VolumeSliderMath.level(percent, max)
        if (target == current) return before
        try {
            audio.setStreamVolume(stream, target, 0)
            val after = levels()
            check((if (media) after.media else after.alarm) == target) {
                "Android volume protection blocked this level."
            }
            // This manual action takes precedence over any earlier Max-both
            // override and must not be silently undone on Class exit.
            if (media) mediaBaseline = null else alarmBaseline = null
            return after
        } catch (failure: Exception) {
            // If a device partially accepts a level before reporting a
            // failure, try to preserve the previous user's selected level.
            runCatching {
                val actual = levels()
                if ((if (media) actual.media else actual.alarm) == target)
                    audio.setStreamVolume(stream, current, 0)
            }
            throw IllegalStateException(
                "Could not adjust ${if (media) "Media" else "Alarm"} volume. " +
                    "Check Android volume or Do Not Disturb settings. " +
                    (failure.message ?: ""), failure)
        }
    }

    /** User-confirmed Max both. A DND restriction causes a visible failure. */
    fun maxBoth(): ClassVolumeLevels {
        val before = levels()
        try {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, before.mediaMax, 0)
            audio.setStreamVolume(AudioManager.STREAM_ALARM, before.alarmMax, 0)
            val after = levels()
            check(after.media == after.mediaMax && after.alarm == after.alarmMax) {
                "Android did not allow full Media and Alarm volume."
            }
            if (mediaBaseline == null) mediaBaseline = before.media
            if (alarmBaseline == null) alarmBaseline = before.alarm
            return after
        } catch (failure: Exception) {
            runCatching {
                if (levels().media == before.mediaMax)
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, before.media, 0)
                if (levels().alarm == before.alarmMax)
                    audio.setStreamVolume(AudioManager.STREAM_ALARM, before.alarm, 0)
            }
            throw IllegalStateException(
                "Could not set both volumes. Check Android volume/DND settings. " +
                    (failure.message ?: ""), failure)
        }
    }

    /**
     * Restore ONLY our unchanged Max-both overrides on Class exit/finish.
     * A manually moved slider removes its stream's restorable baseline.
     */
    fun restoreIfUnchanged() {
        val savedMedia = mediaBaseline
        val savedAlarm = alarmBaseline
        mediaBaseline = null
        alarmBaseline = null
        if (savedMedia == null && savedAlarm == null) return
        val current = levels()
        if (savedMedia != null && current.media == current.mediaMax) runCatching {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, savedMedia, 0)
        }
        if (savedAlarm != null && current.alarm == current.alarmMax) runCatching {
            audio.setStreamVolume(AudioManager.STREAM_ALARM, savedAlarm, 0)
        }
    }
}
