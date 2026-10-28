package com.ateliertime.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateliertime.domain.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Test-package private storage only. Never accesses real saved Classes or Sound data. */
@RunWith(AndroidJUnit4::class)
class RunDiagnosticsInstrumentedTest {
    @Test
    fun archivedRunHasCompleteEditorSnapshotOrderedEventsAndExplicitEnd() {
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val archive = RunDiagnosticsArchive(testContext)
        val rules = SessionRules(
            id = "diagnostic-isolated", name = "isolated test class",
            totalSeconds = 600, summarySeconds = 60, poseSeconds = 120,
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
            endEpochMs = 1_700_000_600_000L,
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
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val archive = RunDiagnosticsArchive(testContext)
        val rules = SessionRules(
            id = "diagnostic-incomplete", name = "incomplete run",
            totalSeconds = 600, summarySeconds = 60, poseSeconds = 120,
            shortBreakSeconds = 60, specialBreakSeconds = 120,
            specialBreakCount = 0,
        )
        val engine = ScheduleEngine()
        val approved = engine.approve(
            engine.generate(rules, FinalPoseChoice.remainder()),
            acknowledgeWarnings = true)
        val old = archive.reports().map { it.name }.toSet()
        archive.begin(approved, 1_700_000_500_000L, 1_700_001_100_000L,
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
}
