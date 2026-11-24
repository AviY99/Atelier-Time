package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

/** Earlier approved saved Classes must never be silently re-authored. */
class D161PreparationPresetCompatibilityTest {
    private class MemoryStore : PresetStore {
        var value = PresetCollection()
        override fun read() = value
        override fun write(value: PresetCollection) { this.value = value }
    }
    private val engine = ScheduleEngine()
    private fun rules(preparation: Int) = SessionRules(
        id = "draft", name = "Opening",
        totalSeconds = 180 * 60, summarySeconds = 15 * 60,
        poseSeconds = 25 * 60,
        openingGroups = listOf(OpeningPoseGroup("g", 4, 60, 300)),
        preparationSeconds = preparation,
    )

    @Test fun oldApprovedPlanOpensByteEquivalentScheduleAfterUpgrade() {
        val storage = MemoryStore()
        val catalog = PresetCatalog(storage, engine) { "saved-id" }
        val original = engine.approve(
            engine.generate(rules(0), FinalPoseChoice.remainder()),
            acknowledgeWarnings = true,
        )
        val record = catalog.save(original, "Old Class")
        val restored = catalog.open(record.id)
        assertEquals(0, restored.rules.preparationSeconds)
        assertEquals(original.phases, restored.phases)
        assertTrue(restored.phases.none { it.kind == PhaseKind.PREPARATION })
    }

    @Test fun explicitlySavedNewPreparationPhasesSurviveRoundTripAndAreSigned() {
        val storage = MemoryStore()
        val catalog = PresetCatalog(storage, engine) { "new-id" }
        val planned = engine.approve(
            engine.generate(rules(5), FinalPoseChoice.remainder()),
            acknowledgeWarnings = true,
        )
        val saved = catalog.save(planned, "Updated Class")
        val reopened = catalog.open(saved.id)
        assertEquals(5, reopened.rules.preparationSeconds)
        assertEquals(3, reopened.phases.count { it.kind == PhaseKind.PREPARATION })
        assertEquals(180 * 60, reopened.phases.last().endSeconds)
        assertEquals(saved.timelineSignature, PresetCatalog.signature(reopened))

        storage.value = storage.value.copy(entries = listOf(
            saved.copy(rules = saved.rules.copy(preparationSeconds = 0))))
        try {
            catalog.open(saved.id)
            fail("A changed preparation schedule must invalidate the approved signature.")
        } catch (_: IllegalStateException) { }
    }
}
