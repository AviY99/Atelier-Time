package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class ClassSaveTargetTest {
    private fun preset(id: String, title: String): StoredPreset = StoredPreset(
        id = id, title = title,
        rules = SessionRules(id = id, name = title,
            totalSeconds = 180 * 60, poseSeconds = 25 * 60),
        ending = null, acknowledgedRestWarnings = false,
        timelineSignature = "fixture",
    )

    @Test fun unchangedNameRequiresOverwritingExactSavedId() {
        val entries = listOf(preset("a", "Draw1425"), preset("b", "Sketch"))
        assertEquals("a", ClassSaveTarget.existingId(entries, "Draw1425"))
        assertEquals("a", ClassSaveTarget.existingId(entries, "  draw1425 "))
    }

    @Test fun changedUniqueNameCreatesNewClassRatherThanOverwritingOriginal() {
        assertNull(ClassSaveTarget.existingId(listOf(preset("a", "Draw1425")),
            "Draw1425 revision"))
    }

    @Test fun collisionWithOtherPresetAlwaysRequiresOverwriteConfirmation() {
        val entries = listOf(preset("a", "Draw1425"), preset("b", "Sketch"))
        assertEquals("b", ClassSaveTarget.existingId(entries, " SKETCH "))
    }

    @Test fun emptyClassTitleCannotBeSaved() {
        try {
            ClassSaveTarget.existingId(emptyList(), " ")
            fail("blank names should be blocked")
        } catch (_: IllegalArgumentException) { }
    }
}
