package com.ateliertime.android

import android.content.Context
import android.media.AudioManager

/** D-144: explicit user-approved, phone-wide volume adjustment — never automatic. */
internal data class ClassVolumeLevels(
    val media: Int, val mediaMax: Int, val alarm: Int, val alarmMax: Int,
) {
    fun mediaPercent(): Int = if (mediaMax <= 0) 0 else media * 100 / mediaMax
    fun alarmPercent(): Int = if (alarmMax <= 0) 0 else alarm * 100 / alarmMax
}

/** Original stream levels are held only while this active Class owns an override. */
internal class ClassVolumeControl(context: Context) {
    private val audio = context.applicationContext
        .getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var original: ClassVolumeLevels? = null

    fun levels(): ClassVolumeLevels = ClassVolumeLevels(
        media = audio.getStreamVolume(AudioManager.STREAM_MUSIC),
        mediaMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
        alarm = audio.getStreamVolume(AudioManager.STREAM_ALARM),
        alarmMax = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM),
    )

    /** User confirmed Max Both. An OS/DND restriction causes a visible failure. */
    fun maxBoth(): ClassVolumeLevels {
        val before = levels()
        try {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, before.mediaMax, 0)
            audio.setStreamVolume(AudioManager.STREAM_ALARM, before.alarmMax, 0)
            val after = levels()
            check(after.media == after.mediaMax && after.alarm == after.alarmMax) {
                "Android did not allow full Media and Alarm volume."
            }
            if (original == null) original = before
            return after
        } catch (failure: Exception) {
            // Best-effort transactional rollback when a later stream is blocked.
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
     * Restore on Class Exit/finish only while a stream still equals the level
     * we set. Never overwrite an intermediate explicit user volume change.
     */
    fun restoreIfUnchanged() {
        val saved = original ?: return
        original = null
        val current = levels()
        if (current.media == current.mediaMax) runCatching {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, saved.media, 0)
        }
        if (current.alarm == current.alarmMax) runCatching {
            audio.setStreamVolume(AudioManager.STREAM_ALARM, saved.alarm, 0)
        }
    }
}
