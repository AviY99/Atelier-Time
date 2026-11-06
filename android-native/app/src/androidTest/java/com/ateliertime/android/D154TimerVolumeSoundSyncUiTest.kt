package com.ateliertime.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ateliertime.domain.SoundSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * D154 UI regression SOURCE; phone builder compiles AndroidTest APK but does
 * not run instrumentation or change the real device's system audio.
 * Simulate values returned after a successful hardware volume adjustment.
 */
@RunWith(AndroidJUnit4::class)
class D154TimerVolumeSoundSyncUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun mediaTimerVolumeZeroAndRaisedReflectOnSoundTabToggle() {
        var settings by mutableStateOf(SoundSettings())
        compose.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MelodySettingsSection(
                        settings = settings,
                        onSettingsChange = { settings = it },
                        onChooseSystemSound = {},
                        onChooseAudioFile = {},
                        onPreview = {},
                        onError = {},
                    )
                }
            }
        }
        compose.runOnUiThread {
            settings = settings.afterSelectedStreamVolumeChange(0)
        }
        compose.onNodeWithTag("sound_master_enabled")
            .performScrollTo().assertIsOff()
        assertFalse(settings.timerSoundsEnabled)

        compose.runOnUiThread {
            settings = settings.afterSelectedStreamVolumeChange(3)
        }
        compose.onNodeWithTag("sound_master_enabled")
            .performScrollTo().assertIsOn()
        assertTrue(settings.timerSoundsEnabled)
    }

    @Test fun alarmAtZeroKeepsMasterSoundOnAndOffUnavailable() {
        var settings by mutableStateOf(SoundSettings())
        compose.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MelodySettingsSection(
                        settings = settings,
                        onSettingsChange = { settings = it },
                        onChooseSystemSound = {},
                        onChooseAudioFile = {},
                        onPreview = {},
                        onError = {},
                    )
                }
            }
        }
        compose.onNodeWithTag("sound_output_alarm").performScrollTo().performClick()
        compose.runOnUiThread {
            settings = settings.afterSelectedStreamVolumeChange(0)
        }
        compose.onNodeWithTag("sound_master_enabled")
            .performScrollTo().assertIsOn().assertIsNotEnabled()
        assertTrue(settings.classAlertsUseAlarmVolume)
        assertTrue(settings.timerSoundsEnabled)
    }
}
