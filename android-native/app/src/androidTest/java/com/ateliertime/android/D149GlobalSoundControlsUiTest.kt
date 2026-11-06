package com.ateliertime.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ateliertime.domain.SoundAssignmentMode
import com.ateliertime.domain.SoundSettings
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * D149 instrumented UI tests. The Samsung local runner COMPILES these but
 * does NOT execute them or touch app-owned saved Classes/preferences.
 */
@RunWith(AndroidJUnit4::class)
class D149GlobalSoundControlsUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun freeAndClassCanReuseTheIdenticalFlatThinVolumeControls() {
        var awake by mutableStateOf(false)
        var alarm by mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                RunningClassQuickControls(
                    containerTag = "free_timer_quick_controls",
                    keepScreenAwake = awake,
                    onKeepScreenAwakeChange = { awake = it },
                    selectedVolumePercent = 62,
                    useAlarmVolume = alarm,
                    onSelectedVolumeChange = {},
                    onVolumeMax = {},
                    onSoundsOff = {},
                )
            }
        }
        compose.onNodeWithTag("free_timer_quick_controls").assertExists()
        compose.onNodeWithText("Keep screen awake").assertExists()
        compose.onNodeWithText("Volumes").assertDoesNotExist()
        compose.onNodeWithTag("running_media_volume_slider").assertExists()
        compose.onNodeWithTag("running_alarm_volume_slider").assertDoesNotExist()
        compose.onNodeWithTag("class_volume_sounds_off").assertExists()
        compose.onNodeWithTag("class_volume_max_both").assertExists()
        compose.onNodeWithTag("keep_screen_awake").performClick()
        assertEquals(true, awake)
        compose.runOnUiThread { alarm = true }
        compose.waitForIdle()
        compose.onNodeWithText("Alarm Volume").assertExists()
        compose.onNodeWithTag("running_media_volume_slider").assertDoesNotExist()
    }

    @Test fun soundsTabHasGlobalOnOffAndOutputRouteWithoutCustomGainSlider() {
        var settings by mutableStateOf(SoundSettings())
        compose.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MelodySettingsSection(
                        settings = settings, onSettingsChange = { settings = it },
                        onChooseSystemSound = {}, onChooseAudioFile = {},
                        onPreview = {}, onError = {},
                    )
                }
            }
        }
        compose.onNodeWithTag("sound_master_enabled")
            .performScrollTo().performClick()
        assertEquals(false, settings.timerSoundsEnabled)
        compose.onNodeWithTag("sound_output_alarm")
            .performScrollTo().performClick()
        assertEquals(true, settings.classAlertsUseAlarmVolume)
        compose.onNodeWithText("Same sound for all")
            .performScrollTo().performClick()
        assertEquals(SoundAssignmentMode.SHARED, settings.assignmentMode)
        compose.onNodeWithTag("sound_shared_mode_custom")
            .performScrollTo().performClick()
        compose.onNodeWithTag("sound_shared_custom_length_slider")
            .performScrollTo().assertExists()
        compose.onNodeWithTag("sound_shared_custom_volume_slider").assertDoesNotExist()
        assertEquals(false, settings.timerSoundsEnabled)
        assertEquals(true, settings.classAlertsUseAlarmVolume)
    }
}
