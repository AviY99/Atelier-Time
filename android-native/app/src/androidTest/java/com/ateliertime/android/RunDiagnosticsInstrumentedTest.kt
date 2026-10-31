package com.ateliertime.android

import android.content.ContextWrapper
import java.io.File
import java.util.UUID
import org.junit.After
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateliertime.domain.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Isolated QA reports live under a unique directory in the TARGET application's
 * cache. Instrumentation executes under the target app's UID, so the test APK's
 * own Context.filesDir is not writable even though its resources are readable.
 * Do not touch production atelier_run_reports or saved Classes/Sound preferences.
 */
@RunWith(AndroidJUnit4::class)
class RunDiagnosticsInstrumentedTest {
    private val target by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }
    private val isolatedRoot by lazy {
        File(target.cacheDir, "atelier_d126_instrumentation_" + UUID.randomUUID())
    }
    private fun isolatedArchive(): RunDiagnosticsArchive {
        val isolatedContext = object : ContextWrapper(target) {
            override fun getFilesDir(): File = isolatedRoot
        }
        return RunDiagnosticsArchive(isolatedContext)
    }
    @After fun removeOnlyIsolatedReports() {
        if (isolatedRoot.exists()) {
            check(isolatedRoot.deleteRecursively()) {
                "Cannot remove only the randomized instrumentation test reports."
            }
        }
    }
    @Test
    fun deleteAllRemovesEveryRunReportButPreservesUnrelatedPrivateFiles() {
        val archive = isolatedArchive()
        val directory = File(isolatedRoot, "atelier_run_reports")
        assertTrue(directory.mkdirs())
        val other = File(directory, "do-not-delete.txt")
        other.writeText("keep this file")
        repeat(8) { index ->
            File(directory, "run-170000000000${index}-isolated.jsonl")
                .writeText("{\"type\":\"run_start\"}\n")
        }
        assertEquals(8, archive.reports().size)
        assertEquals(
            "run-1700000000007-isolated.jsonl",
            archive.reports().first().name,
        )
        assertEquals(8, archive.deleteAll())
        assertTrue(archive.reports().isEmpty())
        assertEquals("keep this file", other.readText())
        assertEquals(0, archive.deleteAll())
    }

    @Test
    fun archivedRunHasCompleteEditorSnapshotOrderedEventsAndExplicitEnd() {
        val archive = isolatedArchive()
        val rules = SessionRules(
            id = "diagnostic-isolated", name = "isolated test class",
            totalSeconds = 660, summarySeconds = 60, poseSeconds = 120,
            shortBreakSeconds = 60, specialBreakSeconds = 120,
            specialBreakCount = 0, cycleStart = CycleStart.POSE_FIRST,
        )
        val engine = ScheduleEngine()
        val candidate = engine.generate(rules, FinalPoseChoice.remainder())
        assertTrue(candidate.issues.toString(), candidate.canApprove)
        val approved = engine.approve(candidate, acknowledgeWarnings = true)
        val before = archive.reports().map { it.name }.toSet()
        val recorder = archive.begin(
            approved, startEpochMs = 1_700_000_000_000L,
            endEpochMs = 1_700_000_660_000L,
            sounds = SoundSettings(), alertsEnabled = true,
        )
        val generated = archive.reports().first { it.name !in before }
        try {
            recorder.event("Next", before = null, after = null,
                phasesBefore = approved.phases,
                phasesAfter = approved.phases, cue = TimerCue.POSE_FINISHED,
                alertsEnabled = true, sounds = SoundSettings())
            recorder.event("Restart", before = null, after = null,
                phasesBefore = approved.phases,
                phasesAfter = approved.phases, alertsEnabled = true,
                sounds = SoundSettings(), rejectedReason = "protected")
            recorder.soundChange(SoundSettings(), alertsEnabled = false)
            recorder.finish("user_exit")

            val entries = generated.readLines().map { JSONObject(it) }
            assertEquals(5, entries.size)
            assertEquals(listOf("run_start", "event", "event",
                "sound_settings_change", "run_end"),
                entries.map { it.getString("type") })
            assertEquals(listOf(1, 2, 3, 4, 5),
                entries.map { it.getInt("sequence") })
            val start = entries.first()
            assertEquals("isolated test class",
                start.getJSONObject("editorRules").getString("name"))
            assertEquals(120, start.getJSONObject("editorRules").getInt("poseSeconds"))
            assertTrue(start.getJSONArray("approvedTimeline").length() > 0)
            assertEquals("requested_not_audibility_proof",
                entries[1].getString("cueRequest"))
            assertEquals("rejected", entries[2].getString("status"))
            assertEquals("protected", entries[2].getString("reason"))
            assertFalse(entries[3].getBoolean("alertsEnabled"))
            assertEquals("user_exit", entries[4].getString("reason"))
        } finally {
            archive.delete(generated.name)
        }
    }

    @Test
    fun unfinishedRunIsStillRetainedForCrashInvestigation() {
        val archive = isolatedArchive()
        val rules = SessionRules(
            id = "diagnostic-incomplete", name = "incomplete run",
            totalSeconds = 660, summarySeconds = 60, poseSeconds = 120,
            shortBreakSeconds = 60, specialBreakSeconds = 120,
            specialBreakCount = 0,
        )
        val engine = ScheduleEngine()
        val approved = engine.approve(
            engine.generate(rules, FinalPoseChoice.remainder()),
            acknowledgeWarnings = true)
        val old = archive.reports().map { it.name }.toSet()
        archive.begin(approved, 1_700_000_500_000L, 1_700_001_160_000L,
            SoundSettings(), true)
        val file = archive.reports().first { it.name !in old }
        try {
            val entries = file.readLines().map { JSONObject(it) }
            assertEquals(1, entries.size)
            assertEquals("run_start", entries[0].getString("type"))
        } finally {
            archive.delete(file.name)
        }
    }
    @Test
    fun longRunRetainsMoreThanThirtyEventsAndInitialReplannedSchedule() {
        val archive = isolatedArchive()
        val rules = SessionRules(
            id = "diagnostic-many-events", name = "many events",
            totalSeconds = 660, summarySeconds = 60,
            poseSeconds = 120, shortBreakSeconds = 60,
            specialBreakSeconds = 60, specialBreakCount = 0,
        )
        val engine = ScheduleEngine()
        val generated = engine.generate(rules, FinalPoseChoice.remainder())
        assertTrue(generated.issues.toString(), generated.canApprove)
        val approved = engine.approve(generated, acknowledgeWarnings = true)
        val changed = FinalPairCompensation.reflow(
            approved, approved.phases, correctionSeconds = -60)
        val old = archive.reports().map { it.name }.toSet()
        val recorder = archive.begin(approved, 1_700_000_000_000L,
            1_700_000_720_000L, SoundSettings(), true)
        val file = archive.reports().first { it.name !in old }
        try {
            recorder.event("Start", before = null, after = null,
                phasesBefore = approved.phases, phasesAfter = changed.phases,
                alertsEnabled = true, sounds = SoundSettings())
            repeat(50) {
                recorder.event("Next", before = null, after = null,
                    phasesBefore = changed.phases, phasesAfter = changed.phases,
                    alertsEnabled = true, sounds = SoundSettings())
            }
            recorder.finish("user_exit")
            val lines = file.readLines().map { JSONObject(it) }
            assertEquals(53, lines.size)
            assertEquals((1..53).toList(), lines.map { it.getInt("sequence") })
            assertEquals(50, lines.count { it.optString("action") == "Next" })
            assertEquals("run_start", lines.first().getString("type"))
            assertEquals("Start", lines[1].getString("action"))
            assertTrue(lines[1].getBoolean("structureChanged"))
            assertEquals(changed.totalSeconds,
                lines[1].getJSONArray("scheduleAfter")
                    .getJSONObject(changed.phases.lastIndex).getInt("endSeconds"))
            assertEquals(approved.rules.totalSeconds,
                lines.first().getJSONArray("approvedTimeline")
                    .getJSONObject(approved.phases.lastIndex).getInt("endSeconds"))
        } finally {
            archive.delete(file.name)
        }
    }

}
