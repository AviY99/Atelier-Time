package com.ateliertime.domain

/**
 * Built-in five-second tunes: twenty 250ms rhythmic slots per melody.
 * A value of 0 is an intentional silent slot creating a syncopated pulse.
 * Enum identifiers MUST remain stable for existing on-phone sound preferences.
 */
enum class MelodyPattern(val title: String, val notesHz: List<Int>) {
    SOFT_CHIMES(
        "Bouncy chimes",
        listOf(523, 659, 784, 0, 784, 659, 1047, 0, 523, 659,
            784, 1047, 988, 784, 659, 0, 784, 988, 1047, 1319),
    ),
    RISING_STEPS(
        "Happy steps",
        listOf(392, 494, 587, 784, 587, 784, 988, 0, 523, 659,
            784, 1047, 784, 988, 1175, 0, 784, 988, 1175, 1319),
    ),
    WARM_BELLS(
        "Sunny bells",
        listOf(392, 523, 659, 0, 523, 659, 784, 0, 659, 784,
            988, 784, 523, 659, 784, 0, 523, 659, 784, 1047),
    ),
    BRIGHT_FANFARE(
        "Upbeat fanfare",
        listOf(523, 523, 784, 0, 659, 784, 1047, 0, 784, 988,
            1047, 1175, 1047, 784, 988, 0, 1047, 1175, 1319, 1319),
    ),
    CALM_DESCENT(
        "Playful bounce",
        listOf(880, 659, 784, 523, 880, 659, 1047, 0, 784, 587,
            698, 523, 784, 659, 988, 0, 659, 784, 1047, 1175),
    ),
    CELEBRATION(
        "Celebration beat",
        listOf(523, 659, 784, 1047, 0, 1047, 1175, 1319, 1047, 784,
            988, 1175, 0, 1175, 1319, 1568, 1319, 1175, 1319, 1568),
    ),
}

/** Global on-device sound selections, independent of saved session scheduling rules. */
data class MelodySelections(
    val poseEnd: MelodyPattern = MelodyPattern.SOFT_CHIMES,
    val breakEnd: MelodyPattern = MelodyPattern.RISING_STEPS,
    val sessionEnd: MelodyPattern = MelodyPattern.CELEBRATION,
) {
    fun forCue(cue: TimerCue): MelodyPattern = when (cue) {
        TimerCue.POSE_FINISHED -> poseEnd
        TimerCue.BREAK_FINISHED -> breakEnd
        TimerCue.SESSION_FINISHED -> sessionEnd
    }
}
