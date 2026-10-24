package com.ateliertime.domain

/** Built-in five-second melodies. Notes are semitone-independent frequencies in Hz;
 * a zero is a half-second rest. Kept Android-free for later platform reuse.
 */
enum class MelodyPattern(val title: String, val notesHz: List<Int>) {
    SOFT_CHIMES("Soft chimes", listOf(523, 659, 784, 1047, 784, 659, 523, 659, 784, 1047)),
    RISING_STEPS("Rising steps", listOf(392, 440, 494, 523, 587, 659, 698, 784, 880, 988)),
    WARM_BELLS("Warm bells", listOf(392, 523, 659, 523, 784, 659, 523, 392, 523, 659)),
    BRIGHT_FANFARE("Bright fanfare", listOf(523, 523, 784, 659, 784, 1047, 784, 1047, 1175, 1047)),
    CALM_DESCENT("Calm descent", listOf(880, 784, 698, 659, 587, 523, 494, 440, 392, 392)),
    CELEBRATION("Celebration", listOf(523, 659, 784, 1047, 988, 1047, 1175, 1319, 1175, 1319)),
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
