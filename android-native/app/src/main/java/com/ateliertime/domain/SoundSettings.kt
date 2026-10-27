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

/** Shared mode has one sound and one 5/10s duration; separate settings are retained. */
enum class SoundAssignmentMode { SEPARATE, SHARED }

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
    /** Always independent of Shared/Separate Class A/B/C preferences. */
    val freeTimer: CueSound = CueSound(melody = MelodyPattern.SOFT_CHIMES),
    // The migration-safe default preserves D-100's independent A/B/C choices.
    val assignmentMode: SoundAssignmentMode = SoundAssignmentMode.SEPARATE,
    // Null means the first shared choice inherits the current A sound.
    // Retained separately so switching modes never erases A/B/C selections.
    val sharedSound: CueSound? = null,
    val sharedSeconds: Int = 5,
    val poseEndSeconds: Int = 5,
    val breakEndSeconds: Int = 5,
    val classEndSeconds: Int = 5,
    val freeTimerSeconds: Int = 5,
    /** Opt-in: route Class A/B/C through Android's alarm volume, not media. */
    val classAlertsUseAlarmVolume: Boolean = false,
) {
    fun soundFor(cue: TimerCue): CueSound {
        if (cue == TimerCue.FREE_TIMER_FINISHED) return freeTimer
        if (assignmentMode == SoundAssignmentMode.SHARED) {
            return sharedSound ?: poseEnd
        }
        return when (cue) {
            TimerCue.POSE_FINISHED -> poseEnd
            TimerCue.BREAK_FINISHED -> breakEnd
            TimerCue.SESSION_FINISHED -> classEnd
            TimerCue.FREE_TIMER_FINISHED -> freeTimer
        }
    }

    /**
     * Inactive sound selections remain stored. When switching modes, enable
     * any library required by that mode's saved choices automatically.
     */
    fun setAssignmentMode(mode: SoundAssignmentMode): SoundSettings {
        if (mode == assignmentMode) return this
        val next = copy(assignmentMode = mode)
        return next.copy(enabledSources = enabledSources +
            next.activeSounds().map { it.source })
    }

    /** Only the sounds currently in use by the picker and timer matter. */
    fun activeSounds(): List<CueSound> = listOf(freeTimer) + when (assignmentMode) {
        SoundAssignmentMode.SHARED -> listOf(sharedSound ?: poseEnd)
        SoundAssignmentMode.SEPARATE -> listOf(poseEnd, breakEnd, classEnd)
    }

    fun activeSourcesEnabled(): Boolean =
        enabledSources.isNotEmpty() &&
            activeSounds().all { it.isPlayable && it.source in enabledSources }

    // Selecting in SHARED mode updates one sound only; the separate choices
    // remain untouched and are restored on switching back.
    fun chooseForVisibleMode(cue: TimerCue, choice: CueSound): SoundSettings {
        require(choice.source in enabledSources) { "Enable the sound source first." }
        require(choice.isPlayable) { "Choose an available sound or audio file." }
        return if (cue == TimerCue.FREE_TIMER_FINISHED) copy(freeTimer = choice)
        else if (assignmentMode == SoundAssignmentMode.SHARED) {
            copy(sharedSound = choice)
        } else {
            choose(cue, choice)
        }
    }

    fun secondsFor(cue: TimerCue): Int {
        if (cue == TimerCue.FREE_TIMER_FINISHED) return freeTimerSeconds
        if (assignmentMode == SoundAssignmentMode.SHARED) return sharedSeconds
        return when (cue) {
            TimerCue.POSE_FINISHED -> poseEndSeconds
            TimerCue.BREAK_FINISHED -> breakEndSeconds
            TimerCue.SESSION_FINISHED -> classEndSeconds
            TimerCue.FREE_TIMER_FINISHED -> freeTimerSeconds
        }
    }

    fun setSharedDuration(seconds: Int): SoundSettings {
        require(seconds == 5 || seconds == 10) {
            "Shared sound length must be 5 or 10 seconds."
        }
        return copy(sharedSeconds = seconds)
    }

    fun choose(cue: TimerCue, choice: CueSound): SoundSettings {
        require(choice.source in enabledSources) { "Enable the sound source first." }
        require(choice.isPlayable) { "Choose an available sound or audio file." }
        return when (cue) {
            TimerCue.POSE_FINISHED -> copy(poseEnd = choice)
            TimerCue.BREAK_FINISHED -> copy(breakEnd = choice)
            TimerCue.SESSION_FINISHED -> copy(classEnd = choice)
            TimerCue.FREE_TIMER_FINISHED -> copy(freeTimer = choice)
        }
    }

    fun setDuration(cue: TimerCue, seconds: Int): SoundSettings {
        require(seconds in 5..60 && (cue == TimerCue.SESSION_FINISHED ||
            cue == TimerCue.FREE_TIMER_FINISHED ||
            seconds == 5 || seconds == 10)) {
            "Session/Break duration must be 5 or 10 seconds; Class must be 5–60 seconds."
        }
        return when (cue) {
            TimerCue.POSE_FINISHED -> copy(poseEndSeconds = seconds)
            TimerCue.BREAK_FINISHED -> copy(breakEndSeconds = seconds)
            TimerCue.SESSION_FINISHED -> copy(classEndSeconds = seconds)
            TimerCue.FREE_TIMER_FINISHED -> copy(freeTimerSeconds = seconds)
        }
    }

    fun setSourceEnabled(source: SoundSource, enabled: Boolean): SoundSettings {
        val next = if (enabled) enabledSources + source else enabledSources - source
        require(next.isNotEmpty()) { "Keep at least one sound source enabled." }
        require(activeSounds().all { it.source in next }) {
            "Change the selected sound in the current mode before disabling this source."
        }
        return copy(enabledSources = next)
    }
}
