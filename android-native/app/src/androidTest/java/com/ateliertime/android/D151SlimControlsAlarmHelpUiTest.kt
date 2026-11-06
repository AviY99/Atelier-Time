package com.ateliertime.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ateliertime.domain.SoundSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** D151 instrumentation source is compiled only by phone build. */
@RunWith(AndroidJUnit4::class)
class D151SlimControlsAlarmHelpUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun awakeAndVolumeActionsShareUnified40dpVisualHeightAnd48dpTouch() {
        compose.setContent {
            MaterialTheme {
                RunningClassQuickControls(
                    keepScreenAwake = false,
                    onKeepScreenAwakeChange = {},
                    selectedVolumePercent = 65,
                    useAlarmVolume = false,
                    onSelectedVolumeChange = {},
                    onVolumeMax = {},
                    onSoundsOff = {},
                )
            }
        }
        val awakeHit = compose.onNodeWithTag("keep_screen_awake")
            .getUnclippedBoundsInRoot()
        val volumeHit = compose.onNodeWithTag("class_volume_max")
            .getUnclippedBoundsInRoot()
        val awakeVisual = compose.onNodeWithTag("keep_screen_awake_visual", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val volumeVisual = compose.onNodeWithTag("class_volume_max_visual", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        assertTrue(awakeHit.bottom - awakeHit.top >= 48.dp)
        assertTrue(volumeHit.bottom - volumeHit.top >= 48.dp)
        assertEquals(AtelierActionStyle.visibleHeight, awakeVisual.bottom - awakeVisual.top)
        assertEquals(AtelierActionStyle.visibleHeight, volumeVisual.bottom - volumeVisual.top)
        assertTrue("Awake label and Volume actions occupy one horizontal row",
            (awakeVisual.top - volumeVisual.top).value in -1f..1f)
        compose.onNodeWithText("Volume").assertExists()
        compose.onNodeWithText("Max").assertExists()
        compose.onNodeWithText("Mute").assertExists()
    }

    @Test fun timerButtonHasShared40dpVisualAnd48dpTouch() {
        var presses = 0
        compose.setContent {
            MaterialTheme {
                SlimTimerAction(
                    label = "Repeat\nLast", tag = "slim_repeat",
                    onClick = { presses++ },
                )
            }
        }
        val pressArea = compose.onNodeWithTag("slim_repeat")
            .getUnclippedBoundsInRoot()
        val visible = compose.onNodeWithTag("slim_repeat_visual")
            .getUnclippedBoundsInRoot()
        assertTrue(pressArea.bottom - pressArea.top >= 48.dp)
        assertEquals(AtelierActionStyle.visibleHeight, visible.bottom - visible.top)
        compose.onNodeWithTag("slim_repeat").performClick()
        assertEquals(1, presses)
    }

    @Test fun alarmForcesSoundOnShowsBottomHelpAndDisablesOff() {
        var settings by mutableStateOf(SoundSettings().setTimerSoundsEnabled(false))
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
        compose.onNodeWithTag("sound_output_alarm")
            .performScrollTo().performClick()
        assertEquals(true, settings.classAlertsUseAlarmVolume)
        assertEquals(true, settings.timerSoundsEnabled)
        compose.onNodeWithTag("sound_master_enabled")
            .performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag("sound_output_help")
            .performScrollTo().performClick()
        compose.onNodeWithText("Sound settings").assertExists()
        compose.onNodeWithText(
            "Alarm uses the phone's default alarm", substring = true
        ).assertExists()
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithTag("sound_output_media")
            .performScrollTo().performClick()
        compose.onNodeWithTag("sound_master_enabled")
            .performScrollTo().assertIsEnabled().performClick()
        assertEquals(false, settings.timerSoundsEnabled)
    }
}
