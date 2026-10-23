package com.ateliertime.android

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateliertime.domain.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Device instrumentation checks run against a PRIVATE test preferences file.
 * This cannot delete or overwrite any real Atelier Time saved sessions.
 * Not yet run; requires an installed Android instrumentation runner.
 */
@RunWith(AndroidJUnit4::class)
class AndroidPresetStoreTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val testName = "atelier_time_instrumented_" + UUID.randomUUID().toString()
    private fun catalog(): PresetCatalog = PresetCatalog(
        AndroidPresetStore(context, testName),
        ScheduleEngine(),
    ) { UUID.randomUUID().toString() }

    @After
    fun cleanTestOnlyData() {
        context.deleteSharedPreferences(testName)
    }

    private fun approvedExample(): ApprovedSession {
        val rules = SessionRules(
            id = "preview",
            name = "Model B",
            totalSeconds = 180 * 60,
            summarySeconds = 15 * 60,
            poseSeconds = 25 * 60,
            specialBreakCount = 1,
        )
        val engine = ScheduleEngine()
        return engine.approve(engine.generate(rules, FinalPoseChoice.remainder()))
    }

    @Test
    fun savedPresetPersistsAcrossFreshStoreInstance() {
        val initial = catalog()
        val entry = initial.save(approvedExample(), "Afternoon atelier")
        initial.activate(entry.id)

        // Simulates Activity/process repository reconstruction with disk-backed data.
        val reopened = catalog()
        assertEquals(entry.id, reopened.state().activeId)
        assertEquals("Afternoon atelier", reopened.state().entries.single().title)
        assertEquals(180 * 60, reopened.open(entry.id).rules.totalSeconds)
        assertEquals(entry.timelineSignature, PresetCatalog.signature(reopened.open(entry.id)))
    }

    @Test
    fun renameDuplicateAndDeletePersistToPhoneStorage() {
        val initial = catalog()
        val first = initial.save(approvedExample(), "One")
        val duplicate = initial.duplicate(first.id, "Two")
        initial.rename(duplicate.id, "Renamed two")
        val reopened = catalog()
        assertEquals(2, reopened.state().entries.size)
        assertEquals("Renamed two", reopened.state().entries.last().title)
        reopened.delete(first.id)
        assertEquals(listOf("Renamed two"), catalog().state().entries.map { it.title })
    }

    @Test
    fun unreadableDataCannotBeSilentlyErasedOrReplaced() {
        val preferences = context.getSharedPreferences(testName, Context.MODE_PRIVATE)
        assertTrue(preferences.edit().putString("library_v1", "{bad json").commit())
        try {
            catalog().state()
            fail("Unreadable saved data must be reported")
        } catch (_: IllegalStateException) { }
        assertEquals("{bad json", preferences.getString("library_v1", null))
        try {
            catalog().save(approvedExample(), "Must not overwrite")
            fail("Saving over corrupt storage must be blocked")
        } catch (_: IllegalStateException) { }
        assertEquals("{bad json", preferences.getString("library_v1", null))
    }

    @Test
    fun wrongTypePreferenceReportsErrorAndPreservesOriginalValue() {
        val preferences = context.getSharedPreferences(testName, Context.MODE_PRIVATE)
        assertTrue(preferences.edit().putInt("library_v1", 42).commit())
        try {
            catalog().state()
            fail("A non-string storage value must be reported, not treated as empty.")
        } catch (_: IllegalStateException) { }
        assertEquals(42, preferences.getInt("library_v1", -1))
        try {
            AndroidPresetStore(context, testName).write(PresetCollection())
            fail("A direct write must never erase wrong-type stored data.")
        } catch (_: IllegalStateException) { }
        assertEquals(42, preferences.getInt("library_v1", -1))
    }

    @Test
    fun directWriteCannotReplaceCorruptOrFutureSchema() {
        val preferences = context.getSharedPreferences(testName, Context.MODE_PRIVATE)
        for (raw in listOf(
            "{bad json",
            """{"version":999,"presets":[],"activeId":null}""",
        )) {
            assertTrue(preferences.edit().putString("library_v1", raw).commit())
            try {
                AndroidPresetStore(context, testName).write(PresetCollection())
                fail("Direct storage writes must respect corruption/version guard.")
            } catch (_: IllegalStateException) { }
            assertEquals(raw, preferences.getString("library_v1", null))
        }
    }

    @Test
    fun futureVersionCannotBeDowngradedWithoutMigration() {
        val preferences = context.getSharedPreferences(testName, Context.MODE_PRIVATE)
        val raw = "{\"version\":999,\"presets\":[],\"activeId\":null}"
        assertTrue(preferences.edit().putString("library_v1", raw).commit())
        try {
            catalog().state()
            fail("Unsupported future storage version must be reported")
        } catch (_: IllegalStateException) { }
        assertEquals(raw, preferences.getString("library_v1", null))
    }
}
