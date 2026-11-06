package com.ateliertime.android

import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.ateliertime.domain.MediaSoundRestorePolicy
import com.ateliertime.domain.VolumeSliderMath

internal data class ClassVolumeLevels(
    val media: Int, val mediaMax: Int, val alarm: Int, val alarmMax: Int,
) {
    fun mediaPercent(): Int = VolumeSliderMath.percent(media, mediaMax)
    fun alarmPercent(): Int = VolumeSliderMath.percent(alarm, alarmMax)
    fun selected(useAlarm: Boolean): Int = if (useAlarm) alarm else media
}

/**
 * Phone-wide Android audio streams, changed ONLY by explicit user actions:
 * a selected stream's slider, one-shot selected-stream mute or a confirmed
 * Volume max for that selected stream. It never changes the other channel.
 *
 * Each selected-stream Max owns an independent restorable baseline.
 * A subsequent explicit slider or mute change releases restoration ownership
 * of ONLY that stream. External volume changes prevent restoration when
 * the current level is no longer our requested maximum.
 */
internal class ClassVolumeControl(context: Context) {
    private val audio = context.applicationContext
        .getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaBaseline: Int? = null
    private var alarmBaseline: Int? = null

    private val history = context.applicationContext.getSharedPreferences(
        "atelier_time_selected_volume_restore_v1", Context.MODE_PRIVATE)

    private fun stream(useAlarm: Boolean): Int =
        if (useAlarm) AudioManager.STREAM_ALARM else AudioManager.STREAM_MUSIC

    private fun key(useAlarm: Boolean): String =
        if (useAlarm) "alarm_last_positive" else "media_last_positive"

    private fun minIndex(useAlarm: Boolean): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            audio.getStreamMinVolume(stream(useAlarm)) else 0

    fun canMuteSelected(useAlarm: Boolean): Boolean = minIndex(useAlarm) == 0

    private fun rememberPositive(useAlarm: Boolean, index: Int) {
        if (index > 0) history.edit().putInt(key(useAlarm), index).apply()
    }

    /**
     * D155: both output channels use exactly the same ON/OFF mechanism.
     * Android alone determines whether the selected channel permits zero.
     * ON restores the previous positive hardware step, otherwise 25%.
     * No other stream or saved app Sound state is changed by this controller.
     */
    fun setSelectedSoundEnabled(useAlarm: Boolean, enabled: Boolean): ClassVolumeLevels {
        val before = levels()
        val current = before.selected(useAlarm)
        val maxIndex = if (useAlarm) before.alarmMax else before.mediaMax
        check(maxIndex > 0) { "Selected device audio stream is unavailable." }
        val minimum = minIndex(useAlarm)
        if (!enabled && minimum > 0) {
            throw IllegalStateException(
                "Android enforces a positive minimum for this sound channel.")
        }
        val lastPositive = history.getInt(key(useAlarm), -1)
        val requested = if (!enabled) 0 else if (current > 0) current else
            MediaSoundRestorePolicy.restoreLevel(lastPositive, maxIndex)
                .coerceAtLeast(minimum).coerceAtMost(maxIndex)
        if (requested == current) {
            rememberPositive(useAlarm, current)
            return before
        }
        try {
            audio.setStreamVolume(stream(useAlarm), requested, 0)
            val after = levels()
            check(after.selected(useAlarm) == requested) {
                "Android did not accept the selected sound volume."
            }
            rememberPositive(useAlarm, current)
            rememberPositive(useAlarm, after.selected(useAlarm))
            if (useAlarm) alarmBaseline = null else mediaBaseline = null
            return after
        } catch (failure: Exception) {
            runCatching {
                if (levels().selected(useAlarm) == requested)
                    audio.setStreamVolume(stream(useAlarm), current, 0)
            }
            throw IllegalStateException(
                "Selected sound volume was not changed by Android: " +
                    (failure.message ?: "device restriction"), failure)
        }
    }


    fun levels(): ClassVolumeLevels = ClassVolumeLevels(
        media = audio.getStreamVolume(AudioManager.STREAM_MUSIC),
        mediaMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
        alarm = audio.getStreamVolume(AudioManager.STREAM_ALARM),
        alarmMax = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM),
    )

    init {
        val initial = levels()
        rememberPositive(false, initial.media)
        rememberPositive(true, initial.alarm)
    }

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
        val target = VolumeSliderMath.level(percent, max).coerceAtLeast(
            minIndex(!media)).coerceAtMost(max)
        if (target == current) {
            rememberPositive(!media, current)
            return before
        }
        try {
            audio.setStreamVolume(stream, target, 0)
            val after = levels()
            check((if (media) after.media else after.alarm) == target) {
                "Android volume protection blocked this level."
            }
            // This manual action takes precedence over any earlier Max-both
            // override and must not be silently undone on Class exit.
            rememberPositive(!media, current)
            rememberPositive(!media, after.selected(!media))
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

    /**
     * D-150: maximize ONLY the Sound-selected Media/Alarm channel. A separate
     * channel's level and baseline must never be modified by this action.
     * Retains the original safety policy: restore our Max on exit only if
     * nobody subsequently moved the chosen hardware channel.
     */
    fun maxSelected(useAlarm: Boolean): ClassVolumeLevels {
        val before = levels()
        val stream = if (useAlarm) AudioManager.STREAM_ALARM
            else AudioManager.STREAM_MUSIC
        val current = if (useAlarm) before.alarm else before.media
        val maximum = if (useAlarm) before.alarmMax else before.mediaMax
        check(maximum > 0) { "The selected volume channel is unavailable." }
        if (current == maximum) return before
        try {
            audio.setStreamVolume(stream, maximum, 0)
            val after = levels()
            check((if (useAlarm) after.alarm else after.media) == maximum) {
                "Android volume protection blocked maximum volume."
            }
            rememberPositive(useAlarm, current)
            rememberPositive(useAlarm, after.selected(useAlarm))
            if (useAlarm) {
                if (alarmBaseline == null) alarmBaseline = current
            } else if (mediaBaseline == null) mediaBaseline = current
            return after
        } catch (failure: Exception) {
            runCatching { audio.setStreamVolume(stream, current, 0) }
            throw IllegalStateException(
                "Could not maximize selected " +
                    (if (useAlarm) "Alarm" else "Media") +
                    " volume. Check Android restrictions. " +
                    (failure.message ?: ""), failure)
        }
    }

    /**
     * D-154: Mute lowers ONLY the selected Media/Alarm hardware stream
     * to zero. The calling Timer screen separately synchronizes this
     * successful change into the global Sounds ON/OFF preference (Media
     * becomes OFF when Android allows zero, regardless of channel).
     * The other hardware stream and selected output route stay unchanged.
     * Explicit mute releases any previously owned Max restoration for
     * THIS stream, even if the level had already reached zero.
     */
    fun muteSelected(useAlarm: Boolean): ClassVolumeLevels {
        check(canMuteSelected(useAlarm)) {
            "Android enforces a positive minimum on this sound channel."
        }
        val after = setPercent(
            if (useAlarm) AudioManager.STREAM_ALARM else AudioManager.STREAM_MUSIC,
            0,
        )
        if (useAlarm) alarmBaseline = null else mediaBaseline = null
        return after
    }

    /**
     * Restore ONLY our unchanged, user-confirmed selected-stream Max on exit.
     * A manually moved slider removes its stream's restorable baseline.
     */
    fun restoreIfUnchanged(): ClassVolumeLevels {
        val savedMedia = mediaBaseline
        val savedAlarm = alarmBaseline
        mediaBaseline = null
        alarmBaseline = null
        if (savedMedia == null && savedAlarm == null) return levels()
        val current = levels()
        if (savedMedia != null && current.media == current.mediaMax) runCatching {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, savedMedia, 0)
        }
        if (savedAlarm != null && current.alarm == current.alarmMax) runCatching {
            audio.setStreamVolume(AudioManager.STREAM_ALARM, savedAlarm, 0)
        }
        return levels()
    }
}
