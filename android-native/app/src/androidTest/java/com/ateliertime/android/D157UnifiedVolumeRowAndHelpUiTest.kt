package com.ateliertime.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ateliertime.domain.SoundSettings
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * D157 source regression tests. Samsung script compiles AndroidTest APK;
 * UI instrumentation is NOT claimed executed until a device run.
 */
@RunWith(AndroidJUnit4::class)
class D157UnifiedVolumeRowAndHelpUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun classMediaHasTwoLineLabelSliderAndLargePercentOnSameRow() {
        compose.setContent {
            MaterialTheme {
                RunningClassQuickControls(
                    keepScreenAwake = false, onKeepScreenAwakeChange = {},
                    selectedVolumePercent = 6, useAlarmVolume = false,
                    onSelectedVolumeChange = {}, onVolumeMax = {},
                    onSoundsOff = {},
                )
            }
        }
        compose.onNodeWithTag("running_volume_card").assertExists()
        compose.onNodeWithTag("running_class_volume_row").assertExists()
        compose.onNodeWithText("Media").assertExists()
        compose.onNodeWithTag("running_volume_stream_label").assertExists()
        compose.onNodeWithText("6%").assertExists()
        val label = compose.onNodeWithTag("running_volume_stream_label")
            .getUnclippedBoundsInRoot()
        val slider = compose.onNodeWithTag("running_media_volume_slider")
            .getUnclippedBoundsInRoot()
        val percent = compose.onNodeWithTag("running_volume_percent")
            .getUnclippedBoundsInRoot()
        assertTrue("Channel label must be left of the bar", label.right <= slider.left)
        assertTrue("Percent must be to right of the bar", slider.right <= percent.left)
        assertTrue("Full slider hit area preserved", slider.bottom - slider.top >= 56.dp)
        assertSixPercentThumbNearTrackStart()
    }

    @Test fun freeAlarmUsesIdenticalRowAndCorrectSixPercent() {
        compose.setContent {
            MaterialTheme {
                RunningClassQuickControls(
                    containerTag = "free_timer_quick_controls",
                    keepScreenAwake = false, onKeepScreenAwakeChange = {},
                    selectedVolumePercent = 6, useAlarmVolume = true,
                    onSelectedVolumeChange = {}, onVolumeMax = {},
                    onSoundsOff = {},
                )
            }
        }
        compose.onNodeWithTag("running_volume_card").assertExists()
        compose.onNodeWithTag("running_class_volume_row").assertExists()
        compose.onNodeWithText("Alarm").assertExists()
        compose.onNodeWithText("6%").assertExists()
        val label = compose.onNodeWithTag("running_volume_stream_label")
            .getUnclippedBoundsInRoot()
        val slider = compose.onNodeWithTag("running_alarm_volume_slider")
            .getUnclippedBoundsInRoot()
        val percent = compose.onNodeWithTag("running_volume_percent")
            .getUnclippedBoundsInRoot()
        assertTrue(label.right <= slider.left)
        assertTrue(slider.right <= percent.left)
        assertTrue(slider.bottom - slider.top >= 56.dp)
        assertSixPercentThumbNearTrackStart()
    }

    private fun assertSixPercentThumbNearTrackStart() {
        val track = compose.onNodeWithTag("running_volume_track_visual",
            useUnmergedTree = true).getUnclippedBoundsInRoot()
        val thumb = compose.onNodeWithTag("running_volume_thumb_visual",
            useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("Volume track width must be positive", track.width > 0f)
        val fraction = (thumb.center.x - track.left) / track.width
        assertTrue(
            "6% thumb must be at the LEFT of its track, not at right end: $fraction",
            fraction >= -0.08f && fraction <= 0.22f,
        )
    }

    @Test fun zeroWarningIsTheSameSingleCenteredLineInBothTimers() {
        compose.setContent {
            MaterialTheme {
                RunningClassQuickControls(
                    containerTag = "free_timer_quick_controls",
                    keepScreenAwake = false, onKeepScreenAwakeChange = {},
                    selectedVolumePercent = 0, useAlarmVolume = false,
                    onSelectedVolumeChange = {}, onVolumeMax = {},
                    onSoundsOff = {}, showZeroVolumeWarning = true,
                )
            }
        }
        compose.onNodeWithTag("free_selected_volume_zero_warning").assertExists()
        compose.onNodeWithText(
            "Sound volume is zero. Timer alert may be silent.").assertExists()
    }

    @Test fun lowerHelpOnlyExplainsMediaAlarmNotSoundLibraries() {
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
        compose.onNodeWithTag("sound_output_help").performScrollTo().performClick()
        compose.onNodeWithText("Media and Alarm").assertExists()
        compose.onNodeWithText(
            "Some devices enforce a positive Alarm minimum", substring = true
        ).assertExists()
        compose.onNodeWithText("Enable one or more sound libraries",
            substring = true).assertDoesNotExist()
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithTag("sound_choices_help").performScrollTo().performClick()
        compose.onNodeWithText("Enable one or more sound libraries",
            substring = true).assertExists()
        compose.onNodeWithText("Media and Alarm").assertDoesNotExist()
    }
}
