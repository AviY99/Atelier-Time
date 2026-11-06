package com.ateliertime.android

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** D156 instrumentation SOURCE: compiled locally, not automatically executed. */
@RunWith(AndroidJUnit4::class)
class D156VolumeHitTargetUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun programmedTimerVolumeCanBeDraggedAwayFromZeroWithLargeHitTarget() {
        var chosen by mutableIntStateOf(0)
        compose.setContent {
            MaterialTheme {
                RunningClassQuickControls(
                    keepScreenAwake = false,
                    onKeepScreenAwakeChange = {},
                    selectedVolumePercent = chosen,
                    useAlarmVolume = false,
                    onSelectedVolumeChange = { chosen = it },
                    onVolumeMax = {},
                    onSoundsOff = {},
                )
            }
        }
        val slider = compose.onNodeWithTag("running_media_volume_slider")
        val bounds = slider.getUnclippedBoundsInRoot()
        assertTrue(bounds.bottom - bounds.top >= 56.dp)
        slider.performTouchInput {
            swipe(
                start = Offset(12f, center.y),
                end = Offset(width * 0.6f, center.y),
                durationMillis = 300,
            )
        }
        compose.waitForIdle()
        assertTrue("Dragging from zero must request positive volume", chosen > 0)
    }

    @Test fun freeTimerAlarmHasSameLargeSliderHitTarget() {
        compose.setContent {
            MaterialTheme {
                RunningClassQuickControls(
                    containerTag = "free_timer_quick_controls",
                    keepScreenAwake = false,
                    onKeepScreenAwakeChange = {},
                    selectedVolumePercent = 0,
                    useAlarmVolume = true,
                    onSelectedVolumeChange = {},
                    onVolumeMax = {},
                    onSoundsOff = {},
                )
            }
        }
        val bounds = compose.onNodeWithTag("running_alarm_volume_slider")
            .getUnclippedBoundsInRoot()
        assertTrue(bounds.bottom - bounds.top >= 56.dp)
    }
}
