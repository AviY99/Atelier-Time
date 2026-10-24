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
 * Each melody plays twenty rhythmic quarter-second slots (five seconds),
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
                // MODE_STATIC intentionally starts at STATE_NO_STATIC_DATA (2):
                // that is successful initialization, not an error. After the
                // PCM clip is written, Android transitions to STATE_INITIALIZED.
                // Checking for STATE_INITIALIZED *before* write() made every
                // melody fail on the user's real Android phone.
                check(output.state != AudioTrack.STATE_UNINITIALIZED) {
                    "Android could not create an audio track (state 0)."
                }
                val wrote = output.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
                check(wrote == samples.size) {
                    "Could not load five-second melody PCM: wrote $wrote of ${samples.size} samples."
                }
                check(output.state == AudioTrack.STATE_INITIALIZED) {
                    "Audio track did not initialize after PCM write (state ${output.state})."
                }
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
        private const val SLOT_MS = 250

        private fun render(pattern: MelodyPattern): ShortArray {
            require(pattern.notesHz.size == MELODY_MS / SLOT_MS) {
                "Expected exactly twenty rhythmic notes/rests for a five-second melody."
            }
            val framesPerSlot = SAMPLE_RATE * SLOT_MS / 1000
            val result = ShortArray(SAMPLE_RATE * MELODY_MS / 1000)
            pattern.notesHz.forEachIndexed { slot, frequency ->
                if (slot >= MELODY_MS / SLOT_MS || frequency <= 0) return@forEachIndexed
                for (i in 0 until framesPerSlot) {
                    val sec = i.toDouble() / SAMPLE_RATE
                    // Short articulation and an accented downbeat on every fourth
                    // rhythmic slot: clearly livelier than the old 500ms tones.
                    val envelope = minOf(1.0, i.toDouble() / (SAMPLE_RATE * .012)) *
                        minOf(1.0, (framesPerSlot - 1 - i).toDouble() /
                            (SAMPLE_RATE * .045))
                    val accent = if (slot % 4 == 0) 1.0 else 0.76
                    val wave = 0.72 * sin(2 * PI * frequency * sec) +
                        0.28 * sin(2 * PI * frequency * 2 * sec)
                    result[slot * framesPerSlot + i] =
                        (wave * envelope * accent * 11_500.0).toInt().toShort()
                }
            }
            return result
        }
    }
}
