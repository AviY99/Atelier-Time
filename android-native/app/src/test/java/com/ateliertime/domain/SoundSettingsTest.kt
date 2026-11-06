package com.ateliertime.domain

import org.junit.Assert.*
import org.junit.Test

class SoundSettingsTest {
    @Test fun defaultsRetainPreviouslyApprovedAppMelodiesAndFiveSecondAudio() {
        val settings = SoundSettings()
        assertEquals(setOf(SoundSource.APP), settings.enabledSources)
        assertEquals(MelodySelections().poseEnd, settings.poseEnd.melody)
        assertEquals(MelodySelections().breakEnd, settings.breakEnd.melody)
        assertEquals(MelodySelections().sessionEnd, settings.classEnd.melody)
        TimerCue.entries.forEach { cue ->
            assertEquals(SoundSource.APP, settings.soundFor(cue).source)
            assertEquals(5, settings.secondsFor(cue))
        }
    }

    @Test fun sharedSoundCoversFreeTimerButItsLengthAndSeparateChoiceSurvive() {
        val separate = SoundSettings().setSourceEnabled(SoundSource.SYSTEM, true)
            .choose(TimerCue.FREE_TIMER_FINISHED, CueSound(
                source = SoundSource.SYSTEM, uri = "content://tone/free"))
            .setDuration(TimerCue.FREE_TIMER_FINISHED, 37)
        val shared = separate.setAssignmentMode(SoundAssignmentMode.SHARED)
            .setSharedDuration(10)
        assertEquals(separate.poseEnd, shared.soundFor(TimerCue.FREE_TIMER_FINISHED))
        assertEquals(10, shared.secondsFor(TimerCue.FREE_TIMER_FINISHED))
        assertEquals(10, shared.secondsFor(TimerCue.SESSION_FINISHED))
        assertEquals(37, shared.setAssignmentMode(SoundAssignmentMode.SEPARATE)
            .secondsFor(TimerCue.FREE_TIMER_FINISHED))
        val changedFromFreePicker = shared.chooseForVisibleMode(
            TimerCue.FREE_TIMER_FINISHED, CueSound(melody = MelodyPattern.WARM_BELLS))
        TimerCue.entries.forEach { cue ->
            assertEquals(MelodyPattern.WARM_BELLS,
                changedFromFreePicker.soundFor(cue).melody)
        }
        assertEquals(separate.freeTimer, changedFromFreePicker
            .setAssignmentMode(SoundAssignmentMode.SEPARATE)
            .soundFor(TimerCue.FREE_TIMER_FINISHED))
        assertEquals(false, shared.classAlertsUseAlarmVolume)
        assertEquals(true, shared.copy(classAlertsUseAlarmVolume = true)
            .classAlertsUseAlarmVolume)
    }

    @Test fun enabledLibrariesProvideIndependentChoicesAcrossAllThreeCues() {
        val all = SoundSettings()
            .setSourceEnabled(SoundSource.SYSTEM, true)
            .setSourceEnabled(SoundSource.FILE, true)
            .choose(TimerCue.POSE_FINISHED,
                CueSound(source = SoundSource.SYSTEM, uri = "content://ringtone/1",
                    displayName = "System bell"))
            .choose(TimerCue.BREAK_FINISHED,
                CueSound(source = SoundSource.FILE, uri = "content://documents/42",
                    displayName = "My chime"))
            .choose(TimerCue.SESSION_FINISHED,
                CueSound(source = SoundSource.APP, melody = MelodyPattern.WARM_BELLS))
        assertEquals(SoundSource.entries.toSet(), all.enabledSources)
        assertEquals("System bell", all.soundFor(TimerCue.POSE_FINISHED).title)
        assertEquals("My chime", all.soundFor(TimerCue.BREAK_FINISHED).title)
        assertEquals("Sunny bells", all.soundFor(TimerCue.SESSION_FINISHED).title)
    }

    @Test fun durationsAreIndependentAndClassAcceptsEverySecondFiveThroughSixty() {
        val first = SoundSettings()
            .setDuration(TimerCue.POSE_FINISHED, 10)
            .setDuration(TimerCue.BREAK_FINISHED, 5)
        for (seconds in 5..60) {
            val current = first.setDuration(TimerCue.SESSION_FINISHED, seconds)
            assertEquals(10, current.secondsFor(TimerCue.POSE_FINISHED))
            assertEquals(5, current.secondsFor(TimerCue.BREAK_FINISHED))
            assertEquals(seconds, current.secondsFor(TimerCue.SESSION_FINISHED))
        }
        for (invalid in listOf(0, 4, 11, 60)) {
            assertThrows(IllegalArgumentException::class.java) {
                SoundSettings().setDuration(TimerCue.POSE_FINISHED, invalid)
            }
        }
        for (invalid in listOf(4, 61)) {
            assertThrows(IllegalArgumentException::class.java) {
                SoundSettings().setDuration(TimerCue.SESSION_FINISHED, invalid)
            }
        }
    }

    @Test fun disabledOrMissingSoundSourcesCannotSilentlyReplaceASelectedCue() {
        val initial = SoundSettings()
        assertThrows(IllegalArgumentException::class.java) {
            initial.setSourceEnabled(SoundSource.APP, false)
        }
        assertThrows(IllegalArgumentException::class.java) {
            initial.choose(TimerCue.POSE_FINISHED,
                CueSound(source = SoundSource.FILE, uri = "content://documents/42"))
        }
        val enabled = initial.setSourceEnabled(SoundSource.FILE, true)
        assertThrows(IllegalArgumentException::class.java) {
            enabled.choose(TimerCue.BREAK_FINISHED, CueSound(source = SoundSource.FILE))
        }
        val file = CueSound(source = SoundSource.FILE, uri = "content://documents/42")
        val mixed = enabled.choose(TimerCue.BREAK_FINISHED, file)
        assertThrows(IllegalArgumentException::class.java) {
            mixed.setSourceEnabled(SoundSource.FILE, false)
        }
        assertEquals(file, mixed.soundFor(TimerCue.BREAK_FINISHED))
    }

    @Test fun sharedSoundUsesOneFiveOrTenSecondLengthForAllFourEvents() {
        val original = SoundSettings()
            .setDuration(TimerCue.POSE_FINISHED, 10)
            .setDuration(TimerCue.BREAK_FINISHED, 5)
            .setDuration(TimerCue.SESSION_FINISHED, 57)
        assertEquals(SoundAssignmentMode.SEPARATE, original.assignmentMode)
        val shared = original
            .setAssignmentMode(SoundAssignmentMode.SHARED)
            .setSharedDuration(10)
        TimerCue.entries.forEach { cue ->
            assertEquals(original.poseEnd, shared.soundFor(cue))
            assertEquals(10, shared.secondsFor(cue))
        }
        val newSound = CueSound(melody = MelodyPattern.WARM_BELLS)
        val updated = shared.chooseForVisibleMode(TimerCue.SESSION_FINISHED, newSound)
        TimerCue.entries.forEach { cue ->
            assertEquals(newSound, updated.soundFor(cue))
            assertEquals(10, updated.secondsFor(cue))
        }
        val restored = updated.setAssignmentMode(SoundAssignmentMode.SEPARATE)
        assertEquals(original.poseEnd, restored.poseEnd)
        assertEquals(original.breakEnd, restored.breakEnd)
        assertEquals(original.classEnd, restored.classEnd)
        assertEquals(10, restored.secondsFor(TimerCue.POSE_FINISHED))
        assertEquals(5, restored.secondsFor(TimerCue.BREAK_FINISHED))
        assertEquals(57, restored.secondsFor(TimerCue.SESSION_FINISHED))
        assertEquals(newSound, restored.sharedSound)
        assertEquals(original.freeTimer, restored.freeTimer)
        assertEquals(5, restored.freeTimerSeconds)
    }

    @Test fun sharedPresetLengthStillOnlyAcceptsFiveOrTenSeconds() {
        for (length in listOf(4, 6, 11, 30, 60)) {
            assertThrows(IllegalArgumentException::class.java) {
                SoundSettings().setSharedDuration(length)
            }
        }
        assertEquals(5, SoundSettings().setSharedDuration(5).sharedSeconds)
        assertEquals(10, SoundSettings().setSharedDuration(10).sharedSeconds)
    }

    @Test fun sharedCustomLengthAppliesToEveryCueAndPreservesSeparateDurations() {
        val original = SoundSettings()
            .setDuration(TimerCue.SESSION_FINISHED, 47)
            .setDuration(TimerCue.FREE_TIMER_FINISHED, 31)
        val custom = original.setAssignmentMode(SoundAssignmentMode.SHARED)
            .selectSharedCustomDuration()
            .setSharedCustomDuration(26)
        assertEquals(SharedDurationMode.CUSTOM, custom.sharedDurationMode)
        TimerCue.entries.forEach { cue ->
            assertEquals(26, custom.secondsFor(cue))
        }
        val separate = custom.setAssignmentMode(SoundAssignmentMode.SEPARATE)
        assertEquals(47, separate.secondsFor(TimerCue.SESSION_FINISHED))
        assertEquals(31, separate.secondsFor(TimerCue.FREE_TIMER_FINISHED))
    }

    @Test fun sharedCustomLengthRestoresAfterUsingFiveOrTenPresets() {
        val start = SoundSettings().setAssignmentMode(SoundAssignmentMode.SHARED)
            .selectSharedCustomDuration()
            .setSharedCustomDuration(37)
        assertEquals(37, start.sharedSeconds)
        val five = start.setSharedDuration(5)
        assertEquals(SharedDurationMode.FIVE, five.sharedDurationMode)
        assertEquals(5, five.sharedSeconds)
        val ten = five.setSharedDuration(10)
        assertEquals(SharedDurationMode.TEN, ten.sharedDurationMode)
        assertEquals(10, ten.secondsFor(TimerCue.FREE_TIMER_FINISHED))
        val restored = ten.selectSharedCustomDuration()
        assertEquals(37, restored.sharedSeconds)
        assertEquals(37, restored.sharedCustomSeconds)
    }

    @Test fun legacyCustomGainIsIgnoredAndCannotMuteSoundAfterUpgrade() {
        val original = SoundSettings().setAssignmentMode(SoundAssignmentMode.SHARED)
            .selectSharedCustomDuration()
        for (percent in listOf(0, 1, 45, 100)) {
            val legacy = original.setSharedCustomVolumePercent(percent)
            TimerCue.entries.forEach { cue ->
                assertEquals(100, legacy.playbackVolumePercentFor(cue))
                assertEquals(100, legacy.setAssignmentMode(SoundAssignmentMode.SEPARATE)
                    .playbackVolumePercentFor(cue))
            }
            assertEquals(original.poseEnd, legacy.poseEnd)
            assertEquals(original.freeTimer, legacy.freeTimer)
        }
    }

    @Test fun sharedCustomDurationBoundariesAndPresetBehaviorStayValid() {
        val initial = SoundSettings().setAssignmentMode(SoundAssignmentMode.SHARED)
            .selectSharedCustomDuration()
        for (seconds in 5..60) {
            val next = initial.setSharedCustomDuration(seconds)
            assertEquals(SharedDurationMode.CUSTOM, next.sharedDurationMode)
            assertEquals(seconds, next.secondsFor(TimerCue.FREE_TIMER_FINISHED))
        }
        for (invalid in listOf(4, 61)) {
            assertThrows(IllegalArgumentException::class.java) {
                initial.setSharedCustomDuration(invalid)
            }
        }
    }

    @Test fun globalSoundOnOffDoesNotDeleteSelectionsOrChangeOtherMode() {
        val original = SoundSettings()
            .setDuration(TimerCue.SESSION_FINISHED, 30)
            .setAssignmentMode(SoundAssignmentMode.SHARED)
            .selectSharedCustomDuration().setSharedCustomDuration(23)
        assertTrue(original.timerSoundsEnabled)
        val off = original.copy(timerSoundsEnabled = false)
        assertFalse(off.timerSoundsEnabled)
        assertEquals(23, off.secondsFor(TimerCue.POSE_FINISHED))
        assertEquals(23, off.secondsFor(TimerCue.FREE_TIMER_FINISHED))
        assertFalse(off.setAssignmentMode(SoundAssignmentMode.SEPARATE).timerSoundsEnabled)
        val on = off.copy(timerSoundsEnabled = true)
        assertTrue(on.timerSoundsEnabled)
        assertEquals(original.poseEnd, on.poseEnd)
        assertEquals(original.classEnd, on.classEnd)
    }

    @Test fun mediaAlarmSelectionIsSharedByAllTimerCuesWithoutChangingSoundSettings() {
        val configured = SoundSettings()
            .setAssignmentMode(SoundAssignmentMode.SHARED)
            .copy(classAlertsUseAlarmVolume = true)
        assertTrue(configured.classAlertsUseAlarmVolume)
        assertTrue(configured.setAssignmentMode(SoundAssignmentMode.SEPARATE)
            .classAlertsUseAlarmVolume)
        assertEquals(5, configured.secondsFor(TimerCue.FREE_TIMER_FINISHED))
        assertEquals(100, configured.playbackVolumePercentFor(TimerCue.FREE_TIMER_FINISHED))
    }

    @Test fun choosingAlarmFromMutedMediaForcesGlobalSoundOn() {
        val mediaOff = SoundSettings().setTimerSoundsEnabled(false)
        assertFalse(mediaOff.timerSoundsEnabled)
        assertFalse(mediaOff.classAlertsUseAlarmVolume)
        val alarm = mediaOff.selectOutputAlarm(true)
        assertTrue(alarm.classAlertsUseAlarmVolume)
        assertTrue(alarm.timerSoundsEnabled)
        assertEquals(mediaOff.poseEnd, alarm.poseEnd)
        assertEquals(mediaOff.freeTimer, alarm.freeTimer)
    }

    @Test fun alarmSoundCannotBeTurnedOffButMediaAllowsOffAgain() {
        val alarm = SoundSettings().selectOutputAlarm(true)
        assertTrue(alarm.timerSoundsEnabled)
        assertTrue(alarm.setTimerSoundsEnabled(false).timerSoundsEnabled)
        val media = alarm.selectOutputAlarm(false)
        assertFalse(media.classAlertsUseAlarmVolume)
        assertTrue(media.timerSoundsEnabled)
        assertFalse(media.setTimerSoundsEnabled(false).timerSoundsEnabled)
    }

    @Test fun legacyAlarmModeWithMutedAppSoundNormalizesWithoutChangingOtherSettings() {
        val legacy = SoundSettings().copy(
            classAlertsUseAlarmVolume = true, timerSoundsEnabled = false,
            sharedCustomSeconds = 41,
        )
        val restored = legacy.normalizedAlarmSoundOn()
        assertTrue(restored.classAlertsUseAlarmVolume)
        assertTrue(restored.timerSoundsEnabled)
        assertEquals(41, restored.sharedCustomSeconds)
        assertEquals(restored, restored.normalizedAlarmSoundOn())
        assertEquals(SoundSettings(),
            SoundSettings().normalizedAlarmSoundOn())
    }

    @Test fun sharedAndSeparateModesPreserveExternalSourcesIndependently() {
        val source = CueSound(
            source = SoundSource.FILE, uri = "content://audio/42", displayName = "Bell")
        val sound = SoundSettings().setSourceEnabled(SoundSource.FILE, true)
            .choose(TimerCue.BREAK_FINISHED, source)
            .setAssignmentMode(SoundAssignmentMode.SHARED)
            .chooseForVisibleMode(TimerCue.POSE_FINISHED,
                CueSound(melody = MelodyPattern.CELEBRATION))
        assertEquals(MelodyPattern.CELEBRATION,
            sound.soundFor(TimerCue.SESSION_FINISHED).melody)
        assertEquals(source, sound.setAssignmentMode(SoundAssignmentMode.SEPARATE)
            .soundFor(TimerCue.BREAK_FINISHED))
    }


    @Test fun sharedModeCanDisableSourceUsedOnlyByHiddenSeparateChoices() {
        val file = CueSound(source = SoundSource.FILE, uri = "content://audio/abc")
        val settings = SoundSettings()
            .setSourceEnabled(SoundSource.FILE, true)
            .choose(TimerCue.BREAK_FINISHED, file)
            .setAssignmentMode(SoundAssignmentMode.SHARED)
        assertEquals(SoundSource.APP, settings.soundFor(TimerCue.POSE_FINISHED).source)
        val disabled = settings.setSourceEnabled(SoundSource.FILE, false)
        assertEquals(setOf(SoundSource.APP), disabled.enabledSources)
        assertTrue(disabled.activeSourcesEnabled())
        assertEquals(file, disabled.breakEnd)
        // Returning to Separate restores B and re-enables its required library.
        val restored = disabled.setAssignmentMode(SoundAssignmentMode.SEPARATE)
        assertTrue(SoundSource.FILE in restored.enabledSources)
        assertEquals(file, restored.soundFor(TimerCue.BREAK_FINISHED))
    }

    @Test fun separateModeCanDisableSourceUsedOnlyByHiddenSharedChoice() {
        val system = CueSound(source = SoundSource.SYSTEM,
            uri = "content://settings/system/notification_sound")
        val settings = SoundSettings()
            .setSourceEnabled(SoundSource.SYSTEM, true)
            .setAssignmentMode(SoundAssignmentMode.SHARED)
            .chooseForVisibleMode(TimerCue.POSE_FINISHED, system)
            .setAssignmentMode(SoundAssignmentMode.SEPARATE)
        val disabled = settings.setSourceEnabled(SoundSource.SYSTEM, false)
        assertEquals(setOf(SoundSource.APP), disabled.enabledSources)
        assertEquals(system, disabled.sharedSound)
        assertTrue(disabled.activeSourcesEnabled())
        val resumed = disabled.setAssignmentMode(SoundAssignmentMode.SHARED)
        assertTrue(SoundSource.SYSTEM in resumed.enabledSources)
        assertEquals(system, resumed.soundFor(TimerCue.SESSION_FINISHED))
    }

    @Test fun activeModeOnlySourceAndLastEnabledSourceStillCannotBeDisabled() {
        val file = CueSound(source = SoundSource.FILE, uri = "content://audio/abc")
        val sharedFile = SoundSettings().setSourceEnabled(SoundSource.FILE, true)
            .setAssignmentMode(SoundAssignmentMode.SHARED)
            .chooseForVisibleMode(TimerCue.SESSION_FINISHED, file)
        assertThrows(IllegalArgumentException::class.java) {
            sharedFile.setSourceEnabled(SoundSource.FILE, false)
        }
        val appOnly = sharedFile.choose(TimerCue.FREE_TIMER_FINISHED, file)
            .setSourceEnabled(SoundSource.APP, false)
        assertEquals(setOf(SoundSource.FILE), appOnly.enabledSources)
        assertThrows(IllegalArgumentException::class.java) {
            appOnly.setSourceEnabled(SoundSource.FILE, false)
        }
        // Unset sharedSound inherits A and must still protect A's source.
        val inherited = SoundSettings().setAssignmentMode(SoundAssignmentMode.SHARED)
        assertThrows(IllegalArgumentException::class.java) {
            inherited.setSourceEnabled(SoundSource.APP, false)
        }
    }

}
