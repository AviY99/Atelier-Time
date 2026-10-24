package com.ateliertime.android

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateliertime.domain.MelodyPattern
import com.ateliertime.domain.MelodySelections
import com.ateliertime.domain.CueSound
import com.ateliertime.domain.SoundSource
import com.ateliertime.domain.TimerCue
import com.ateliertime.domain.SoundAssignmentMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Isolated preference namespace; never touches the user's real presets or melody settings. */
@RunWith(AndroidJUnit4::class)
class MelodyPreferencesTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val isolatedName = "atelier_melody_test_" + UUID.randomUUID().toString()

    @After fun cleanup() {
        context.deleteSharedPreferences(isolatedName)
    }

    @Test fun threeIndependentChoicesSurviveNewRepositoryInstance() {
        val initial = MelodyPreferences(context, isolatedName)
        val chosen = MelodySelections(
            poseEnd = MelodyPattern.CALM_DESCENT,
            breakEnd = MelodyPattern.WARM_BELLS,
            sessionEnd = MelodyPattern.BRIGHT_FANFARE,
        )
        initial.save(chosen)
        assertEquals(chosen, MelodyPreferences(context, isolatedName).load())
    }

    @Test fun legacyMelodiesBecomeBuiltInDefaultsWithFiveSecondPlayback() {
        val preferences = MelodyPreferences(context, isolatedName)
        val chosen = MelodySelections(
            poseEnd = MelodyPattern.CALM_DESCENT,
            breakEnd = MelodyPattern.WARM_BELLS,
            sessionEnd = MelodyPattern.BRIGHT_FANFARE,
        )
        preferences.save(chosen)
        val upgraded = MelodyPreferences(context, isolatedName).loadSettings()
        assertEquals(setOf(SoundSource.APP), upgraded.enabledSources)
        assertEquals(chosen.poseEnd, upgraded.poseEnd.melody)
        assertEquals(chosen.breakEnd, upgraded.breakEnd.melody)
        assertEquals(chosen.sessionEnd, upgraded.classEnd.melody)
        assertEquals(5, upgraded.classEndSeconds)
    }

    @Test fun multiSourceChoicesAndIndependentDurationsSurviveRestart() {
        val preferences = MelodyPreferences(context, isolatedName)
        val selected = preferences.loadSettings()
            .setSourceEnabled(SoundSource.SYSTEM, true)
            .setSourceEnabled(SoundSource.FILE, true)
            .choose(TimerCue.POSE_FINISHED,
                CueSound(source = SoundSource.SYSTEM,
                    uri = "content://settings/system/notification_sound",
                    displayName = "System notification"))
            .choose(TimerCue.BREAK_FINISHED,
                CueSound(source = SoundSource.FILE,
                    uri = "content://documents/document/audio123",
                    displayName = "Selected file"))
            .setDuration(TimerCue.POSE_FINISHED, 10)
            .setDuration(TimerCue.BREAK_FINISHED, 10)
            .setDuration(TimerCue.SESSION_FINISHED, 59)
        preferences.saveSettings(selected)
        assertEquals(selected, MelodyPreferences(context, isolatedName).loadSettings())
    }

    @Test fun upgradingD100KeepsSeparateSoundsAndNewSharedSettingSurvivesReload() {
        val prefs = MelodyPreferences(context, isolatedName)
        val original = prefs.loadSettings()
            .setDuration(TimerCue.SESSION_FINISHED, 37)
            .choose(TimerCue.BREAK_FINISHED,
                CueSound(melody = MelodyPattern.WARM_BELLS))
        prefs.saveSettings(original)
        val upgraded = MelodyPreferences(context, isolatedName).loadSettings()
        assertEquals(SoundAssignmentMode.SEPARATE, upgraded.assignmentMode)
        val shared = upgraded
            .setAssignmentMode(SoundAssignmentMode.SHARED)
            .setSharedDuration(10)
            .chooseForVisibleMode(TimerCue.SESSION_FINISHED,
                CueSound(melody = MelodyPattern.CELEBRATION))
        prefs.saveSettings(shared)
        val restored = MelodyPreferences(context, isolatedName).loadSettings()
        assertEquals(shared, restored)
        TimerCue.entries.forEach { cue ->
            assertEquals(10, restored.secondsFor(cue))
            assertEquals(MelodyPattern.CELEBRATION, restored.soundFor(cue).melody)
        }
        val separateAgain = restored.setAssignmentMode(SoundAssignmentMode.SEPARATE)
        assertEquals(37, separateAgain.secondsFor(TimerCue.SESSION_FINISHED))
        assertEquals(MelodyPattern.WARM_BELLS, separateAgain.breakEnd.melody)
    }


    @Test fun sharedModeDisabledDormantSourceStaysDisabledAfterRestart() {
        val store = MelodyPreferences(context, isolatedName)
        val file = CueSound(source = SoundSource.FILE,
            uri = "content://documents/document/audio3", displayName = "Dormant file")
        val settings = store.loadSettings()
            .setSourceEnabled(SoundSource.FILE, true)
            .choose(TimerCue.BREAK_FINISHED, file)
            .setAssignmentMode(SoundAssignmentMode.SHARED)
            .setSourceEnabled(SoundSource.FILE, false)
        store.saveSettings(settings)
        val loaded = MelodyPreferences(context, isolatedName).loadSettings()
        assertEquals(settings, loaded)
        assertEquals(setOf(SoundSource.APP), loaded.enabledSources)
        assertEquals(file, loaded.breakEnd)
        val activeSeparate = loaded.setAssignmentMode(SoundAssignmentMode.SEPARATE)
        assertEquals(file, activeSeparate.soundFor(TimerCue.BREAK_FINISHED))
        assertEquals(true, SoundSource.FILE in activeSeparate.enabledSources)
        store.saveSettings(activeSeparate)
        assertEquals(activeSeparate, MelodyPreferences(context, isolatedName).loadSettings())
    }

    @Test fun separateModeDisabledDormantSharedSourceStaysDisabledAfterRestart() {
        val store = MelodyPreferences(context, isolatedName)
        val system = CueSound(source = SoundSource.SYSTEM,
            uri = "content://settings/system/notification_sound",
            displayName = "Stored system alert")
        val settings = store.loadSettings()
            .setSourceEnabled(SoundSource.SYSTEM, true)
            .setAssignmentMode(SoundAssignmentMode.SHARED)
            .chooseForVisibleMode(TimerCue.POSE_FINISHED, system)
            .setAssignmentMode(SoundAssignmentMode.SEPARATE)
            .setSourceEnabled(SoundSource.SYSTEM, false)
        store.saveSettings(settings)
        val loaded = MelodyPreferences(context, isolatedName).loadSettings()
        assertEquals(settings, loaded)
        assertEquals(setOf(SoundSource.APP), loaded.enabledSources)
        assertEquals(system, loaded.sharedSound)
        val restored = loaded.setAssignmentMode(SoundAssignmentMode.SHARED)
        assertEquals(system, restored.soundFor(TimerCue.SESSION_FINISHED))
        assertEquals(true, SoundSource.SYSTEM in restored.enabledSources)
    }

}
