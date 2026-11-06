package com.ateliertime.android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Compiled in D148 Samsung local QA; instrumentation is NOT run automatically. */
@RunWith(AndroidJUnit4::class)
class D148OptionAControlsUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun maxBothIsAtTheRightOfVolumeCardAndBothSlidersAreVisible() {
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(340.dp)) {
                    RunningClassQuickControls(
                        keepScreenAwake = true, onKeepScreenAwakeChange = {},
                        selectedVolumePercent = 75,
                        useAlarmVolume = false,
                        onSelectedVolumeChange = {},
                        onVolumeMax = {},
                        onSoundsOff = {},
                    )
                }
            }
        }
        compose.onNodeWithText("Keep screen awake").assertExists()
        compose.onNodeWithText("Volumes").assertDoesNotExist()
        compose.onNodeWithTag("running_media_volume_slider").assertExists()
        compose.onNodeWithTag("running_alarm_volume_slider").assertDoesNotExist()
        compose.onNodeWithTag("class_volume_sounds_off").assertExists()

    }

    @Test fun headerTapChoosesAlarmForCuesWithoutChangingEitherVolume() {
        var selectedAlarm by mutableStateOf(false)
        var mediaChanges = 0
        var alarmChanges = 0
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(340.dp)) {
                    RunningClassQuickControls(
                        keepScreenAwake = false, onKeepScreenAwakeChange = {},
                        selectedVolumePercent = 75,
                        useAlarmVolume = selectedAlarm,
                        onSelectedVolumeChange = { mediaChanges++ },
                        onVolumeMax = {},
                        onSoundsOff = {},
                    )
                }
            }
        }
        compose.onNodeWithText("Media Volume").assertExists()
        compose.onNodeWithTag("running_alarm_volume_slider").assertDoesNotExist()
        compose.runOnUiThread { selectedAlarm = true }
        compose.waitForIdle()
        compose.onNodeWithText("Alarm Volume").assertExists()
        compose.onNodeWithTag("running_media_volume_slider").assertDoesNotExist()
        assertEquals(0, mediaChanges)
        assertEquals(0, alarmChanges)
    }
}
