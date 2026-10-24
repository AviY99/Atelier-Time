package com.ateliertime.android

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.ateliertime.domain.MelodyPattern
import com.ateliertime.domain.MelodySelections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Phone-private configuration. Does not touch the independently versioned
 * approved-session store or modify saved schedules on app updates.
 */
class MelodyPreferences(
    context: Context,
    storageName: String = "atelier_time_melody_preferences",
) {
    private val prefs = context.applicationContext.getSharedPreferences(
        storageName, Context.MODE_PRIVATE
    )
    fun load(): MelodySelections {
        fun get(key: String, fallback: MelodyPattern): MelodyPattern =
            runCatching { MelodyPattern.valueOf(prefs.getString(key, null) ?: fallback.name) }
                .getOrDefault(fallback)
        return MelodySelections(
            get("poseEnd", MelodySelections().poseEnd),
            get("breakEnd", MelodySelections().breakEnd),
            get("sessionEnd", MelodySelections().sessionEnd)
        )
    }
    fun save(selection: MelodySelections) {
        check(prefs.edit()
            .putString("poseEnd", selection.poseEnd.name)
            .putString("breakEnd", selection.breakEnd.name)
            .putString("sessionEnd", selection.sessionEnd.name)
            .commit()) { "Sound selections could not be saved." }
    }
}

/**
 * Each selected melody plays exactly ten half-second notes (five seconds),
 * through PCM AudioTrack instead of indistinguishable short DTMF beeps.
 *
 * Playback is non-blocking; the clock can tick while the melody plays.
 * Requires foreground-service notification support for reliable lock-screen
 * and force-background playback, which remains separately unverified.
 */
class MelodyAudioPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var active: Job? = null

    fun play(melody: MelodyPattern, onError: (String) -> Unit = {}) {
        active?.cancel()
        active = scope.launch {
            var track: AudioTrack? = null
            try {
                val samples = render(melody)
                val output = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                    .setAudioFormat(AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build())
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(samples.size * 2)
                    .build()
                track = output
                check(output.state == AudioTrack.STATE_INITIALIZED) {
                    "Android audio output is not initialized."
                }
                val wrote = output.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
                check(wrote == samples.size) { "Could not write the complete melody." }
                output.play()
                delay(MELODY_MS + 150L)
            } catch (failure: Exception) {
                if (failure !is kotlinx.coroutines.CancellationException) {
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        onError(failure.message ?: "Could not play melody.")
                    }
                }
            } finally {
                runCatching { track?.stop() }
                runCatching { track?.release() }
            }
        }
    }

    fun close() {
        active?.cancel()
        scope.cancel()
    }

    companion object {
        private const val SAMPLE_RATE = 22_050
        private const val MELODY_MS = 5_000
        private const val SLOT_MS = 500

        private fun render(pattern: MelodyPattern): ShortArray {
            val framesPerSlot = SAMPLE_RATE * SLOT_MS / 1000
            val result = ShortArray(SAMPLE_RATE * MELODY_MS / 1000)
            pattern.notesHz.forEachIndexed { slot, frequency ->
                if (slot >= MELODY_MS / SLOT_MS || frequency <= 0) return@forEachIndexed
                for (i in 0 until framesPerSlot) {
                    val sec = i.toDouble() / SAMPLE_RATE
                    val envelope = minOf(1.0, i.toDouble() / (SAMPLE_RATE * .025)) *
                        minOf(1.0, (framesPerSlot - 1 - i).toDouble() /
                            (SAMPLE_RATE * .08))
                    val wave = 0.72 * sin(2 * PI * frequency * sec) +
                        0.28 * sin(2 * PI * frequency * 2 * sec)
                    result[slot * framesPerSlot + i] =
                        (wave * envelope * 11_500.0).toInt().toShort()
                }
            }
            return result
        }
    }
}
