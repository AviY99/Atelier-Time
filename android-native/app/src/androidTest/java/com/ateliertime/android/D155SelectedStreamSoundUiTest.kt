package com.ateliertime.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ateliertime.domain.SoundSettings
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * D155 Compose UI regression sources: compiled on the phone,
 * NOT executed by the normal APK build and never alter system audio.
 */
@RunWith(AndroidJUnit4::class)
class D155SelectedStreamSoundUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun hardwareAlarmMinimumDisablesOffWithoutHardcodedRouteAssumption() {
        var settings by mutableStateOf(
            SoundSettings().selectOutputAlarm(true).afterSelectedStreamVolumeChange(1))
        compose.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MelodySettingsSection(
                        settings = settings, onSettingsChange = { settings = it },
                        onChooseSystemSound = {}, onChooseAudioFile = {},
                        onPreview = {}, onError = {},
                        canMuteSelectedAudio = false,
                    )
                }
            }
        }
        compose.onNodeWithTag("sound_master_enabled")
            .performScrollTo().assertIsOn().assertIsNotEnabled()
    }

    @Test fun zeroCapableAlarmAllowsSameOffOnSwitchAsMedia() {
        var settings by mutableStateOf(
            SoundSettings().selectOutputAlarm(true).afterSelectedStreamVolumeChange(0))
        compose.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MelodySettingsSection(
                        settings = settings, onSettingsChange = { settings = it },
                        onChooseSystemSound = {}, onChooseAudioFile = {},
                        onPreview = {}, onError = {},
                        canMuteSelectedAudio = true,
                    )
                }
            }
        }
        compose.onNodeWithTag("sound_master_enabled")
            .performScrollTo().assertIsOff().assertIsEnabled().performClick()
        assertTrue(settings.timerSoundsEnabled)
    }
}
