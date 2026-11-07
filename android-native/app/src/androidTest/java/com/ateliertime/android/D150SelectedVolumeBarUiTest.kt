package com.ateliertime.android

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** D-150 source compiled locally; Android instrumentation NOT auto-executed. */
@RunWith(AndroidJUnit4::class)
class D150SelectedVolumeBarUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun whenSoundsUsesMediaOnlyMediaBarIsVisible() {
        var max = 0
        var mute = 0
        compose.setContent {
            MaterialTheme {
                RunningClassQuickControls(
                    keepScreenAwake = false,
                    onKeepScreenAwakeChange = {},
                    useAlarmVolume = false,
                    selectedVolumePercent = 72,
                    onSelectedVolumeChange = {},
                    onVolumeMax = { max++ },
                    onSoundsOff = { mute++ },
                )
            }
        }
        compose.onNodeWithText("Keep screen awake").assertExists()
        compose.onNodeWithText("Media").assertExists()
        compose.onNodeWithText("Alarm").assertDoesNotExist()
        compose.onNodeWithText("Volumes").assertDoesNotExist()
        compose.onNodeWithTag("running_media_volume_slider").assertExists()
        compose.onNodeWithTag("running_alarm_volume_slider").assertDoesNotExist()
        compose.onNodeWithTag("class_volume_max").performClick()
        compose.onNodeWithTag("class_volume_sounds_off").performClick()
        assertEquals(1, max)
        assertEquals(1, mute)
    }

    @Test fun changingExternalSoundsSelectionSwitchesTheOneBarWithoutOtherClicks() {
        var useAlarm by mutableStateOf(false)
        var sliderAction = 0
        compose.setContent {
            MaterialTheme {
                RunningClassQuickControls(
                    keepScreenAwake = false,
                    onKeepScreenAwakeChange = {},
                    useAlarmVolume = useAlarm,
                    selectedVolumePercent = if (useAlarm) 30 else 65,
                    onSelectedVolumeChange = { sliderAction++ },
                    onVolumeMax = {},
                    onSoundsOff = {},
                )
            }
        }
        compose.onNodeWithTag("running_media_volume_slider").assertExists()
        compose.runOnUiThread { useAlarm = true }
        compose.waitForIdle()
        compose.onNodeWithTag("running_media_volume_slider").assertDoesNotExist()
        compose.onNodeWithTag("running_alarm_volume_slider").assertExists()
        compose.onNodeWithText("Alarm").assertExists()
        assertEquals(0, sliderAction)
    }

    @Test fun freeTimerUsesExactlyTheSameSelectionOnlyComponent() {
        compose.setContent {
            MaterialTheme {
                RunningClassQuickControls(
                    containerTag = "free_timer_quick_controls",
                    keepScreenAwake = true,
                    onKeepScreenAwakeChange = {},
                    useAlarmVolume = true,
                    selectedVolumePercent = 22,
                    onSelectedVolumeChange = {},
                    onVolumeMax = {},
                    onSoundsOff = {},
                )
            }
        }
        compose.onNodeWithTag("free_timer_quick_controls").assertExists()
        compose.onNodeWithTag("running_alarm_volume_slider").assertExists()
        compose.onNodeWithTag("running_media_volume_slider").assertDoesNotExist()
        compose.onNodeWithTag("class_volume_max").assertExists()
        compose.onNodeWithTag("class_volume_sounds_off").assertExists()
    }
}
