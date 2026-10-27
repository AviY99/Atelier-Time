package com.ateliertime.android

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioManager
import android.media.AudioFocusRequest
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.ateliertime.domain.CueSound
import com.ateliertime.domain.MelodyPattern
import com.ateliertime.domain.MelodySelections
import com.ateliertime.domain.SoundSettings
import com.ateliertime.domain.SoundSource
import com.ateliertime.domain.SoundAssignmentMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

/**
 * Private sound preferences, completely separate from the saved Class store.
 * Original melody enum IDs/keys are preserved for a non-destructive upgrade.
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
            get("sessionEnd", MelodySelections().sessionEnd),
        )
    }

    /** Keep the original API for existing preferences/device regression tests. */
    fun save(selection: MelodySelections) {
        check(prefs.edit()
            .putString("poseEnd", selection.poseEnd.name)
            .putString("breakEnd", selection.breakEnd.name)
            .putString("sessionEnd", selection.sessionEnd.name)
            .commit()) { "Sound selections could not be saved." }
    }

    fun loadSettings(): SoundSettings {
        val legacy = load()
        val defaults = SoundSettings()
        fun source(key: String): SoundSource =
            runCatching {
                SoundSource.valueOf(prefs.getString(key, SoundSource.APP.name)!!)
            }.getOrDefault(SoundSource.APP)
        fun string(key: String): String? =
            runCatching { prefs.getString(key, null) }.getOrNull()?.takeIf { it.isNotBlank() }
        fun choice(key: String, melody: MelodyPattern): CueSound {
            val parsed = CueSound(
                source = source("${key}Source"), melody = melody,
                uri = string("${key}Uri"), displayName = string("${key}Title"),
            )
            return if (parsed.isPlayable) parsed else CueSound(melody = melody)
        }
        val a = choice("poseEnd", legacy.poseEnd)
        val b = choice("breakEnd", legacy.breakEnd)
        val c = choice("sessionEnd", legacy.sessionEnd)
        val freeMelody = runCatching {
            MelodyPattern.valueOf(prefs.getString("freeTimerMelody", null)
                ?: defaults.freeTimer.melody.name)
        }.getOrDefault(defaults.freeTimer.melody)
        val free = choice("freeTimer", freeMelody)
        // No mode key on D-100 installs: do not silently make their
        // three previously chosen sounds identical during an upgrade.
        val mode = runCatching {
            SoundAssignmentMode.valueOf(prefs.getString(
                "soundAssignmentMode", SoundAssignmentMode.SEPARATE.name)!!)
        }.getOrDefault(SoundAssignmentMode.SEPARATE)
        val sharedMelody = runCatching {
            MelodyPattern.valueOf(prefs.getString("sharedMelody", null) ?: a.melody.name)
        }.getOrDefault(a.melody)
        val shared = if (prefs.contains("sharedSource")) {
            choice("shared", sharedMelody)
        } else null
        val names = runCatching { prefs.getString("enabledSources", null) }.getOrNull()
        val enabled = if (names == null) setOf(SoundSource.APP) else
            names.split(",").mapNotNull { name ->
                SoundSource.entries.firstOrNull { it.name == name }
            }.toSet()
        fun seconds(key: String, fallback: Int, isClass: Boolean): Int {
            val value = runCatching { prefs.getInt(key, fallback) }.getOrDefault(fallback)
            return if (isClass && value in 5..60 ||
                !isClass && (value == 5 || value == 10)) value else fallback
        }
        return SoundSettings(
            // A dormant mode may retain sounds from a disabled library.
            // Restore only libraries required by the CURRENT mode on load.
            enabledSources = (enabled + (if (mode == SoundAssignmentMode.SHARED)
                listOf((shared ?: a).source)
            else listOf(a.source, b.source, c.source, free.source)))
                .ifEmpty { defaults.enabledSources },
            poseEnd = a, breakEnd = b, classEnd = c, freeTimer = free,
            assignmentMode = mode, sharedSound = shared,
            sharedSeconds = seconds("sharedSeconds", 5, false),
            poseEndSeconds = seconds("poseEndSeconds", 5, false),
            breakEndSeconds = seconds("breakEndSeconds", 5, false),
            classEndSeconds = seconds("classEndSeconds", 5, true),
            freeTimerSeconds = seconds("freeTimerSeconds", 5, true),
            classAlertsUseAlarmVolume = prefs.getBoolean("classAlertsUseAlarmVolume", false),
        )
    }

    fun saveSettings(settings: SoundSettings) {
        require(settings.activeSourcesEnabled()) {
            "Select a sound from an enabled source in the current mode."
        }
        // Keep dormant selections intact even if their libraries are disabled.
        require((listOf(settings.poseEnd, settings.breakEnd, settings.classEnd,
            settings.freeTimer) +
            listOfNotNull(settings.sharedSound)).all { it.isPlayable }) {
            "A saved sound selection is missing its audio file."
        }
        require(settings.sharedSeconds in setOf(5, 10) &&
            settings.poseEndSeconds in setOf(5, 10) &&
            settings.breakEndSeconds in setOf(5, 10) &&
            settings.classEndSeconds in 5..60 &&
            settings.freeTimerSeconds in 5..60) { "Invalid sound duration." }
        val edit = prefs.edit()
            // Save legacy keys so previously selected application melodies remain.
            .putString("poseEnd", settings.poseEnd.melody.name)
            .putString("breakEnd", settings.breakEnd.melody.name)
            .putString("sessionEnd", settings.classEnd.melody.name)
            .putString("enabledSources", SoundSource.entries
                .filter { it in settings.enabledSources }.joinToString(",") { it.name })
            .putString("soundAssignmentMode", settings.assignmentMode.name)
            .putInt("sharedSeconds", settings.sharedSeconds)
            .putInt("poseEndSeconds", settings.poseEndSeconds)
            .putInt("breakEndSeconds", settings.breakEndSeconds)
            .putInt("classEndSeconds", settings.classEndSeconds)
            .putInt("freeTimerSeconds", settings.freeTimerSeconds)
            .putBoolean("classAlertsUseAlarmVolume", settings.classAlertsUseAlarmVolume)
        fun write(key: String, value: CueSound) {
            edit.putString("${key}Source", value.source.name)
                .putString("${key}Uri", value.uri)
                .putString("${key}Title", value.displayName)
        }
        write("poseEnd", settings.poseEnd)
        write("breakEnd", settings.breakEnd)
        write("sessionEnd", settings.classEnd)
        write("freeTimer", settings.freeTimer)
        edit.putString("freeTimerMelody", settings.freeTimer.melody.name)
        if (settings.sharedSound == null) {
            edit.remove("sharedSource").remove("sharedUri").remove("sharedTitle")
                .remove("sharedMelody")
        } else {
            write("shared", settings.sharedSound)
            edit.putString("sharedMelody", settings.sharedSound.melody.name)
        }
        check(edit.commit()) { "Sound preferences could not be saved." }
    }
}

/**
 * All choices play for the selected number of seconds. Built-in five-second
 * rhythmic tunes repeat as needed; OS and user audio loop or stop at timeout.
 * Preview and actual cue dispatch share this implementation.
 *
 * As with the prior player, Android lock-screen/Doze foreground-service
 * reliability is NOT claimed until separately implemented and device tested.
 */
class MelodyAudioPlayer(context: Context) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var active: Job? = null

    fun play(
        choice: CueSound,
        durationSeconds: Int,
        useAlarmVolume: Boolean = false,
        onError: (String) -> Unit = {},
    ) {
        require(durationSeconds in 5..60)
        active?.cancel()
        active = scope.launch {
            var track: AudioTrack? = null
            var media: MediaPlayer? = null
            val focusRequest = AudioFocusRequest.Builder(
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(soundAttributes(useAlarmVolume))
                .setOnAudioFocusChangeListener(
                    { /* Short cue; Android manages ducking. */ },
                    Handler(Looper.getMainLooper()),
                )
                .build()
            try {
                // Request short-lived focus; never force media volume or
                // bypass an intentional mute / Do Not Disturb decision.
                val focusResult = audioManager.requestAudioFocus(focusRequest)
                if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    withContext(Dispatchers.Main) {
                        onError("Android audio focus was not granted; the cue may be inaudible.")
                    }
                }
                when (choice.source) {
                    SoundSource.APP -> {
                        val clip = render(choice.melody)
                        val repeats = durationSeconds / 5
                        val remainder = durationSeconds % 5
                        val sampleCount = SAMPLE_RATE * durationSeconds
                        val samples = ShortArray(sampleCount) { index ->
                            clip[index % clip.size]
                        }
                        // No change to the approved 5s PCM waveform.
                        check(repeats >= 1 && remainder in 0..4)
                        val output = AudioTrack.Builder()
                            .setAudioAttributes(soundAttributes(useAlarmVolume))
                            .setAudioFormat(AudioFormat.Builder()
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .build())
                            .setTransferMode(AudioTrack.MODE_STATIC)
                            .setBufferSizeInBytes(samples.size * 2)
                            .build()
                        track = output
                        // Static AudioTrack initially has STATE_NO_STATIC_DATA (2).
                        check(output.state != AudioTrack.STATE_UNINITIALIZED) {
                            "Android could not create an audio track."
                        }
                        check(output.write(samples, 0, samples.size,
                            AudioTrack.WRITE_BLOCKING) == samples.size) {
                            "Could not load built-in melody PCM."
                        }
                        check(output.state == AudioTrack.STATE_INITIALIZED) {
                            "Audio track did not initialize after PCM write."
                        }
                        output.play()
                    }
                    SoundSource.SYSTEM, SoundSource.FILE -> {
                        val uri = choice.uri?.takeIf { it.isNotBlank() }
                            ?: error("The selected sound is missing. Choose it again.")
                        val player = MediaPlayer()
                        media = player
                        player.setAudioAttributes(soundAttributes(useAlarmVolume))
                        player.setDataSource(appContext, Uri.parse(uri))
                        player.isLooping = true
                        player.prepare()
                        player.start()
                    }
                }
                delay(durationSeconds * 1_000L)
            } catch (failure: Exception) {
                if (failure !is CancellationException) {
                    withContext(Dispatchers.Main) {
                        onError(failure.message ?:
                            "Cannot play this sound. Choose an accessible audio file.")
                    }
                }
            } finally {
                runCatching { track?.stop() }
                runCatching { track?.release() }
                runCatching { media?.stop() }
                runCatching { media?.release() }
                runCatching { audioManager.abandonAudioFocusRequest(focusRequest) }
            }
        }
    }

    fun close() {
        stop()
        scope.cancel()
    }

    fun stop() {
        active?.cancel()
        active = null
    }

    companion object {
        private const val SAMPLE_RATE = 22_050
        private const val MELODY_MS = 5_000
        private const val SLOT_MS = 250

        private fun soundAttributes(useAlarmVolume: Boolean = false): AudioAttributes =
            AudioAttributes.Builder()
            .setUsage(if (useAlarmVolume) AudioAttributes.USAGE_ALARM
                else AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        private fun render(pattern: MelodyPattern): ShortArray {
            require(pattern.notesHz.size == MELODY_MS / SLOT_MS) {
                "Expected twenty rhythmic slots for the built-in five-second melody."
            }
            val framesPerSlot = SAMPLE_RATE * SLOT_MS / 1000
            val result = ShortArray(SAMPLE_RATE * MELODY_MS / 1000)
            pattern.notesHz.forEachIndexed { slot, frequency ->
                if (slot >= MELODY_MS / SLOT_MS || frequency <= 0) return@forEachIndexed
                for (i in 0 until framesPerSlot) {
                    val sec = i.toDouble() / SAMPLE_RATE
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
