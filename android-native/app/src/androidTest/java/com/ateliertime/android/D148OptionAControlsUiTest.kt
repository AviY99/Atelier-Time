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
                        mediaPercent = 75, alarmPercent = 40,
                        useAlarmVolume = false, onChooseVolume = {},
                        onMediaVolumeChange = {}, onAlarmVolumeChange = {},
                        onMaxBoth = {},
                    )
                }
            }
        }
        compose.onNodeWithText("Keep Screen Awake").assertExists()
        compose.onNodeWithText("Volumes").assertExists()
        compose.onNodeWithTag("running_media_volume_slider").assertExists()
        compose.onNodeWithTag("running_alarm_volume_slider").assertExists()
        compose.onNodeWithText("Selected").assertExists()
        val card = compose.onNodeWithTag("running_volume_card")
            .getUnclippedBoundsInRoot()
        val max = compose.onNodeWithTag("class_volume_max_both")
            .getUnclippedBoundsInRoot()
        assertTrue("Max both should sit toward the right side of the volume card",
            max.left > card.left + (card.right - card.left) / 2f)
        assertTrue("Max both must remain inside card", max.right <= card.right)
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
                        mediaPercent = 75, alarmPercent = 40,
                        useAlarmVolume = selectedAlarm,
                        onChooseVolume = { selectedAlarm = it },
                        onMediaVolumeChange = { mediaChanges++ },
                        onAlarmVolumeChange = { alarmChanges++ },
                        onMaxBoth = {},
                    )
                }
            }
        }
        compose.onNodeWithText("Alarm Volume").performClick()
        assertTrue("Alarm stream must now be selected for Class cues", selectedAlarm)
        assertEquals(0, mediaChanges)
        assertEquals(0, alarmChanges)
    }
}
