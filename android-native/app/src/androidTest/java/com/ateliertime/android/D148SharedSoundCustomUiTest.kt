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

/** Compiled in the local phone build; device instrumentation is not automatically executed. */
@RunWith(AndroidJUnit4::class)
class D148SharedSoundCustomUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun customShowsOnlySoundLengthSliderWithoutCustomGain() {
        var settings by mutableStateOf(
            SoundSettings().setAssignmentMode(SoundAssignmentMode.SHARED))
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
        compose.onNodeWithText("Separate sounds").assertExists()
        compose.onNodeWithTag("sound_shared_mode_five").assertExists()
        compose.onNodeWithTag("sound_shared_mode_ten").assertExists()
        compose.onNodeWithTag("sound_shared_mode_custom").assertExists()
        compose.onNodeWithTag("sound_shared_custom_volume_slider").assertDoesNotExist()
        compose.onNodeWithTag("sound_shared_mode_custom")
            .performScrollTo().performClick()
        compose.onNodeWithText("Custom sound volume").assertDoesNotExist()
        compose.onNodeWithTag("sound_shared_custom_length_slider")
            .performScrollTo().assertExists()
        compose.onNodeWithTag("sound_shared_custom_volume_slider").assertDoesNotExist()
        assertEquals(15, settings.sharedSeconds)
        compose.onNodeWithTag("sound_shared_mode_ten")
            .performScrollTo().performClick()
        assertEquals(10, settings.sharedSeconds)
        compose.onNodeWithTag("sound_shared_custom_volume_slider").assertDoesNotExist()
    }

    @Test fun soundGlobalMasterAndMediaAlarmSelectorsApplyToSharedAndSeparate() {
        var settings by mutableStateOf(SoundSettings())
        compose.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MelodySettingsSection(
                        settings = settings,
                        onSettingsChange = { settings = it },
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
        assertEquals(true, settings.timerSoundsEnabled)
        compose.onNodeWithText("Same sound for all")
            .performScrollTo().performClick()
        assertEquals(SoundAssignmentMode.SHARED, settings.assignmentMode)
        assertEquals(true, settings.timerSoundsEnabled)
        assertEquals(true, settings.classAlertsUseAlarmVolume)
        compose.onNodeWithTag("sound_master_enabled").assertIsNotEnabled()
        compose.onNodeWithTag("sound_output_media")
            .performScrollTo().performClick()
        assertEquals(false, settings.classAlertsUseAlarmVolume)
    }

    @Test fun switchingBackToSeparateRetainsOriginalIndependentDurationControls() {
        var settings by mutableStateOf(
            SoundSettings().setAssignmentMode(SoundAssignmentMode.SHARED))
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
        compose.onNodeWithText("Separate sounds").performScrollTo().performClick()
        assertEquals(SoundAssignmentMode.SEPARATE, settings.assignmentMode)
        compose.onNodeWithText("Free timer sound length")
            .performScrollTo().assertExists()
        compose.onNodeWithTag("sound_shared_custom_volume_slider").assertDoesNotExist()
    }
}
