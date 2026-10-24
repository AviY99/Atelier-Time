package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** The 5-second sound contract is testable without Android audio hardware. */
class MelodyRhythmTest {
    @Test fun everyMelodyHasTwentyQuarterSecondRhythmicSlots() {
        MelodyPattern.entries.forEach { melody ->
            assertEquals(melody.name, 20, melody.notesHz.size)
            assertTrue(melody.name, melody.notesHz.all { it == 0 || it in 300..1800 })
            assertTrue(melody.name, melody.notesHz.any { it > 0 })
        }
        assertEquals(5_000, MelodyPattern.SOFT_CHIMES.notesHz.size * 250)
    }

    @Test fun melodiesHaveDistinctRhythmicAndPitchPatterns() {
        assertEquals(6, MelodyPattern.entries.size)
        assertEquals(6, MelodyPattern.entries.map { it.notesHz }.distinct().size)
        assertTrue(MelodyPattern.entries.count { 0 in it.notesHz } >= 4)
    }

    @Test fun storedEnumIdsRemainStableAcrossNewMelodyArrangements() {
        assertEquals(
            listOf("SOFT_CHIMES", "RISING_STEPS", "WARM_BELLS",
                "BRIGHT_FANFARE", "CALM_DESCENT", "CELEBRATION"),
            MelodyPattern.entries.map { it.name },
        )
        val chosen = MelodySelections(
            poseEnd = MelodyPattern.BRIGHT_FANFARE,
            breakEnd = MelodyPattern.WARM_BELLS,
            sessionEnd = MelodyPattern.CELEBRATION,
        )
        assertEquals(MelodyPattern.WARM_BELLS, chosen.forCue(TimerCue.BREAK_FINISHED))
    }
}
