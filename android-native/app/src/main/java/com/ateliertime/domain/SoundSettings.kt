package com.ateliertime.domain

/**
 * Sound-source policy is pure Kotlin so platform pickers/playback can be
 * replaced for future iOS and desktop targets without changing timer rules.
 */
enum class SoundSource(val title: String) {
    APP("Atelier Time"),
    SYSTEM("System sounds"),
    FILE("Audio file"),
}

/** A single sound used for one cue; source options are enabled independently. */
data class CueSound(
    val source: SoundSource = SoundSource.APP,
    val melody: MelodyPattern = MelodyPattern.SOFT_CHIMES,
    val uri: String? = null,
    val displayName: String? = null,
) {
    val title: String get() = when (source) {
        SoundSource.APP -> melody.title
        SoundSource.SYSTEM -> displayName?.takeIf { it.isNotBlank() } ?: "System sound"
        SoundSource.FILE -> displayName?.takeIf { it.isNotBlank() } ?: "Audio file"
    }

    val isPlayable: Boolean get() = source == SoundSource.APP || !uri.isNullOrBlank()
}

/**
 * Global independent preferences. Never written into a saved Class or its
 * approved timeline. Existing installs default to the same 3 app melodies.
 */
data class SoundSettings(
    val enabledSources: Set<SoundSource> = setOf(SoundSource.APP),
    val poseEnd: CueSound = CueSound(melody = MelodyPattern.SOFT_CHIMES),
    val breakEnd: CueSound = CueSound(melody = MelodyPattern.RISING_STEPS),
    val classEnd: CueSound = CueSound(melody = MelodyPattern.CELEBRATION),
    val poseEndSeconds: Int = 5,
    val breakEndSeconds: Int = 5,
    val classEndSeconds: Int = 5,
) {
    fun soundFor(cue: TimerCue): CueSound = when (cue) {
        TimerCue.POSE_FINISHED -> poseEnd
        TimerCue.BREAK_FINISHED -> breakEnd
        TimerCue.SESSION_FINISHED -> classEnd
    }

    fun secondsFor(cue: TimerCue): Int = when (cue) {
        TimerCue.POSE_FINISHED -> poseEndSeconds
        TimerCue.BREAK_FINISHED -> breakEndSeconds
        TimerCue.SESSION_FINISHED -> classEndSeconds
    }

    fun choose(cue: TimerCue, choice: CueSound): SoundSettings {
        require(choice.source in enabledSources) { "Enable the sound source first." }
        require(choice.isPlayable) { "Choose an available sound or audio file." }
        return when (cue) {
            TimerCue.POSE_FINISHED -> copy(poseEnd = choice)
            TimerCue.BREAK_FINISHED -> copy(breakEnd = choice)
            TimerCue.SESSION_FINISHED -> copy(classEnd = choice)
        }
    }

    fun setDuration(cue: TimerCue, seconds: Int): SoundSettings {
        require(seconds in 5..60 && (cue == TimerCue.SESSION_FINISHED ||
            seconds == 5 || seconds == 10)) {
            "Session/Break duration must be 5 or 10 seconds; Class must be 5–60 seconds."
        }
        return when (cue) {
            TimerCue.POSE_FINISHED -> copy(poseEndSeconds = seconds)
            TimerCue.BREAK_FINISHED -> copy(breakEndSeconds = seconds)
            TimerCue.SESSION_FINISHED -> copy(classEndSeconds = seconds)
        }
    }

    fun setSourceEnabled(source: SoundSource, enabled: Boolean): SoundSettings {
        val next = if (enabled) enabledSources + source else enabledSources - source
        require(next.isNotEmpty()) { "Keep at least one sound source enabled." }
        require(listOf(poseEnd, breakEnd, classEnd).all { it.source in next }) {
            "Change the selected sounds before disabling this source."
        }
        return copy(enabledSources = next)
    }
}
