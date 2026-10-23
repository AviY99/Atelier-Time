package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class PresetCatalogTest {
    private val m = SECONDS_PER_MINUTE
    private val engine = ScheduleEngine()

    private class MemoryStore : PresetStore {
        var saved = PresetCollection()
        var failWrite = false
        override fun read(): PresetCollection = saved
        override fun write(value: PresetCollection) {
            if (failWrite) throw IllegalStateException("Simulated disk failure")
            saved = value
        }
    }

    private fun catalog(store: MemoryStore): PresetCatalog {
        var id = 0
        return PresetCatalog(store, engine) { "preset_" + (++id) }
    }

    private fun exampleB() = SessionRules(
        id = "working", name = "Original",
        totalSeconds = 180 * m, summarySeconds = 15 * m,
        poseSeconds = 25 * m, specialBreakCount = 1,
    )

    private fun approvedB(): ApprovedSession =
        engine.approve(engine.generate(exampleB(), FinalPoseChoice.remainder()))

    private fun warningApproved(): ApprovedSession {
        val rules = exampleB().copy(
            totalSeconds = 51 * m,
            summarySeconds = 0,
            shortBreakSeconds = 1 * m,
            specialBreakCount = 0,
        )
        val pending = engine.generate(rules)
        assertTrue(pending.needsSafetyAcknowledgement)
        return engine.approve(pending, acknowledgeWarnings = true)
    }

    @Test fun saveAndReloadApprovedExactTimeline() {
        val store = MemoryStore()
        val service = catalog(store)
        val original = approvedB()
        val saved = service.save(original, " Figure A ")
        assertEquals("Figure A", saved.title)
        assertEquals("Figure A", saved.rules.name)
        assertEquals(1, service.state().entries.size)
        assertEquals(original.phases, service.open(saved.id).phases)
        assertEquals(FinalPoseMode.REMAINDER, service.open(saved.id).selectedChoice?.mode)
    }

    @Test fun restartWithSameStoreKeepsData() {
        val store = MemoryStore()
        val first = catalog(store)
        val id = first.save(approvedB(), "Evening").id
        val restarted = catalog(store)
        assertEquals("Evening", restarted.state().entries.single().title)
        assertEquals(180 * m, restarted.open(id).rules.totalSeconds)
    }

    @Test fun duplicateRenameDeleteAndActivate() {
        val store = MemoryStore()
        val service = catalog(store)
        val first = service.save(approvedB(), "Original")
        val copy = service.duplicate(first.id, "Original copy")
        assertNotEquals(first.id, copy.id)
        assertEquals(2, service.state().entries.size)

        val renamed = service.rename(copy.id, "Renamed")
        assertEquals(copy.id, renamed.id)
        assertEquals("Renamed", service.open(copy.id).rules.name)
        val selected = service.activate(copy.id)
        assertEquals(renamed.timelineSignature, PresetCatalog.signature(selected))
        assertEquals(copy.id, service.state().activeId)

        service.delete(copy.id)
        assertNull(service.state().activeId)
        assertEquals(listOf(first.id), service.state().entries.map { it.id })
    }

    @Test fun updateRequiresExistingId() {
        val service = catalog(MemoryStore())
        try {
            service.save(approvedB(), "Bad update", overwriteId = "missing")
            fail("Cannot silently create when an update target is missing")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun cannotPersistEmptyNameOrDuplicateId() {
        val store = MemoryStore()
        val service = PresetCatalog(store, engine) { "same_id" }
        service.save(approvedB(), "First")
        try {
            service.save(approvedB(), "Second")
            fail("IDs must remain unique")
        } catch (_: IllegalStateException) { }
        try {
            service.save(approvedB(), "  ")
            fail("A name is required")
        } catch (_: IllegalArgumentException) { }
        assertEquals(1, store.saved.entries.size)
    }

    @Test fun warningAcknowledgementSurvivesReload() {
        val store = MemoryStore()
        val service = catalog(store)
        val original = warningApproved()
        val record = service.save(original, "Short rests")
        assertTrue(record.acknowledgedRestWarnings)
        val restored = service.open(record.id)
        assertEquals(original.acknowledgedWarnings, restored.acknowledgedWarnings)
        assertTrue(restored.acknowledgedWarnings.any { it.code == "rest_25_5" })
    }

    @Test fun tamperedAcknowledgementOrSignatureNeverSilentlyApproves() {
        val store = MemoryStore()
        val service = catalog(store)
        val record = service.save(warningApproved(), "Rest violation")
        store.saved = store.saved.copy(entries = listOf(
            record.copy(acknowledgedRestWarnings = false)
        ))
        try {
            service.open(record.id)
            fail("Missing warning acknowledgement must block")
        } catch (_: IllegalStateException) { }
        store.saved = store.saved.copy(entries = listOf(
            record.copy(timelineSignature = "wrong")
        ))
        try {
            service.state()
            fail("Changed schedule must not be accepted")
        } catch (_: IllegalStateException) { }
    }

    @Test fun savingDoesNotEraseDataOnStorageError() {
        val store = MemoryStore()
        val service = catalog(store)
        val first = service.save(approvedB(), "Safe")
        store.failWrite = true
        try {
            service.delete(first.id)
            fail("Failure should be visible")
        } catch (_: IllegalStateException) { }
        assertEquals(first.id, store.saved.entries.single().id)
    }

    @Test fun invalidActiveReferenceAndDuplicatesAreRejected() {
        val store = MemoryStore()
        val service = catalog(store)
        val record = service.save(approvedB(), "Valid")
        store.saved = store.saved.copy(activeId = "missing")
        try {
            service.state()
            fail("Active selection must exist")
        } catch (_: IllegalStateException) { }
        store.saved = PresetCollection(entries = listOf(record, record))
        try {
            service.state()
            fail("Duplicate records are not valid")
        } catch (_: IllegalStateException) { }
    }
    @Test fun unusedConfiguredFieldChangeIsDetectedEvenWhenPhasesMatch() {
        val store = MemoryStore()
        val service = catalog(store)
        val plan = exampleB().copy(specialBreakCount = 0)
        val approved = engine.approve(
            engine.generate(plan, FinalPoseChoice.remainder())
        )
        val record = service.save(approved, "No special breaks")
        // No special phase is present, so the timeline stays identical,
        // but changing even an inactive configured value is not approved.
        store.saved = store.saved.copy(entries = listOf(record.copy(
            rules = record.rules.copy(specialBreakSeconds = 7 * m)
        )))
        try {
            service.state()
            fail("Rule changes must be caught even with identical phases")
        } catch (_: IllegalStateException) { }
    }

}
