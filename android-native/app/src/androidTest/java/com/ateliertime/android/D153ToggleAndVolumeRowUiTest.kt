package com.ateliertime.android

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotEnabled
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

/** D153 UI source: compiled by phone script, instrumentation not automatically executed. */
@RunWith(AndroidJUnit4::class)
class D153ToggleAndVolumeRowUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun sharedOnOffHas40dpFrame48dpTouchAndChangesState() {
        var awake by mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                AtelierOnOffButton(
                    checked = awake, onCheckedChange = { awake = it },
                    modifier = Modifier.testTag("shared_toggle"),
                    visualTag = "shared_toggle_visual",
                )
            }
        }
        val hit = compose.onNodeWithTag("shared_toggle").getUnclippedBoundsInRoot()
        val face = compose.onNodeWithTag("shared_toggle_visual", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        assertEquals(AtelierActionStyle.touchHeight, hit.bottom - hit.top)
        assertEquals(AtelierActionStyle.visibleHeight, face.bottom - face.top)
        compose.onNodeWithTag("shared_toggle").performClick()
        assertTrue(awake)
    }

    @Test fun volumeIsPlainTextAndMaxMuteAreIndependentMatchingButtons() {
        var max by mutableIntStateOf(0)
        var mute by mutableIntStateOf(0)
        compose.setContent {
            MaterialTheme {
                RunningClassQuickControls(
                    keepScreenAwake = false, onKeepScreenAwakeChange = {},
                    useAlarmVolume = false, selectedVolumePercent = 0,
                    onSelectedVolumeChange = {},
                    onVolumeMax = { max++ }, onSoundsOff = { mute++ },
                )
            }
        }
        compose.onNodeWithTag("keep_screen_awake_label").assertExists()
        compose.onNodeWithTag("running_volume_label").assertExists()
        compose.onNodeWithTag("running_volume_actions_border").assertDoesNotExist()
        compose.onNodeWithTag("class_volume_max_visual", useUnmergedTree = true)
            .assertExists()
        compose.onNodeWithTag("class_volume_sounds_off_visual", useUnmergedTree = true)
            .assertExists()
        compose.onNodeWithText("Mute").assertExists()
        compose.onNodeWithTag("class_volume_max").performClick()
        compose.onNodeWithTag("class_volume_sounds_off").performClick()
        assertEquals(1, max)
        assertEquals(1, mute)
    }

    @Test fun disabledAlarmOnOffButtonStillShowsOnButCannotToggle() {
        compose.setContent {
            MaterialTheme {
                AtelierOnOffButton(
                    checked = true, onCheckedChange = {},
                    enabled = false, modifier = Modifier.testTag("alarm_locked"),
                )
            }
        }
        compose.onNodeWithTag("alarm_locked").assertIsNotEnabled()
        compose.onNodeWithText("ON").assertExists()
    }
}
