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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Historical UI test adapted to D150. Compiled, NOT run on phone by build. */
@RunWith(AndroidJUnit4::class)
class D147RunningClassQuickControlsUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun awakeAndSingleSelectedVolumeBarStayCompact() {
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(340.dp)) {
                    RunningClassQuickControls(
                        keepScreenAwake = true,
                        onKeepScreenAwakeChange = {},
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
        compose.onNodeWithText("Media").assertExists()
        compose.onNodeWithText("Volumes").assertDoesNotExist()
        compose.onNodeWithTag("running_media_volume_slider").assertExists()
        compose.onNodeWithTag("running_alarm_volume_slider").assertDoesNotExist()
        val first = compose.onNodeWithTag("running_screen_awake_row")
            .getUnclippedBoundsInRoot()
        val second = compose.onNodeWithTag("running_volume_card")
            .getUnclippedBoundsInRoot()
        assertTrue("Top actions and selected bar should be adjacent",
            second.top >= first.bottom && second.top - first.bottom <= 4.dp)
        assertTrue("Action targets retain >=48dp height",
            first.bottom - first.top >= 48.dp)
    }

    @Test fun awakeMaxAndMuteActionsRemainIndependent() {
        var awake by mutableStateOf(false)
        var maxClicks = 0
        var muteClicks = 0
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(340.dp)) {
                    RunningClassQuickControls(
                        keepScreenAwake = awake,
                        onKeepScreenAwakeChange = { awake = it },
                        selectedVolumePercent = 40,
                        useAlarmVolume = true,
                        onSelectedVolumeChange = {},
                        onVolumeMax = { maxClicks++ },
                        onSoundsOff = { muteClicks++ },
                    )
                }
            }
        }
        compose.onNodeWithTag("running_alarm_volume_slider").assertExists()
        compose.onNodeWithTag("running_media_volume_slider").assertDoesNotExist()
        compose.onNodeWithTag("keep_screen_awake").performClick()
        assertTrue(awake)
        compose.onNodeWithTag("class_volume_max").performClick()
        compose.onNodeWithTag("class_volume_sounds_off").performClick()
        assertEquals(1, maxClicks)
        assertEquals(1, muteClicks)
    }
}
