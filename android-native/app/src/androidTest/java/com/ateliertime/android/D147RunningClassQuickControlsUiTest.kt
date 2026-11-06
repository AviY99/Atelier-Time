package com.ateliertime.android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
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

/**
 * Former D147 top-control test suite updated for D148's approved Option A.
 * This AndroidTest is compiled by the local build, NOT automatically run.
 */
@RunWith(AndroidJUnit4::class)
class D147RunningClassQuickControlsUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun compactAwakeAndVolumeCardsRemainImmediatelyAdjacent() {
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(360.dp)) {
                    RunningClassQuickControls(
                        keepScreenAwake = true,
                        onKeepScreenAwakeChange = {},
                        mediaPercent = 75, alarmPercent = 40,
                        useAlarmVolume = false,
                        onChooseVolume = {},
                        onMediaVolumeChange = {},
                        onAlarmVolumeChange = {},
                        onMaxBoth = {},
                    )
                }
            }
        }
        compose.onNodeWithText("Keep Screen Awake").assertExists()
        compose.onNodeWithText("ON").assertExists()
        compose.onNodeWithText("Volumes").assertExists()
        compose.onNodeWithText("Media Volume").assertExists()
        compose.onNodeWithText("Alarm Volume").assertExists()
        compose.onNodeWithText("Selected").assertExists()
        compose.onNodeWithText("Max both").assertExists()

        val first = compose.onNodeWithTag("running_screen_awake_row")
            .getUnclippedBoundsInRoot()
        val second = compose.onNodeWithTag("running_volume_card")
            .getUnclippedBoundsInRoot()
        assertTrue("Cards must be in two tightly adjacent sections",
            second.top >= first.bottom && second.top - first.bottom <= 6.dp)
        assertTrue("Switch and Max both must remain touch-sized",
            first.bottom - first.top >= 48.dp)
    }

    @Test fun screenToggleVolumeSelectionAndMaxBothUseRealCallbacks() {
        var awake by mutableStateOf(false)
        var alarmSelected by mutableStateOf(false)
        var maxRequests = 0
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(360.dp)) {
                    RunningClassQuickControls(
                        keepScreenAwake = awake,
                        onKeepScreenAwakeChange = { awake = it },
                        mediaPercent = 20, alarmPercent = 90,
                        useAlarmVolume = alarmSelected,
                        onChooseVolume = { alarmSelected = it },
                        onMediaVolumeChange = {},
                        onAlarmVolumeChange = {},
                        onMaxBoth = { maxRequests++ },
                    )
                }
            }
        }
        compose.onNodeWithTag("keep_screen_awake").performClick()
        assertTrue(awake)
        compose.onNodeWithText("Alarm Volume").performClick()
        assertTrue(alarmSelected)
        compose.onNodeWithTag("class_volume_max_both").performClick()
        assertEquals(1, maxRequests)
    }
}
