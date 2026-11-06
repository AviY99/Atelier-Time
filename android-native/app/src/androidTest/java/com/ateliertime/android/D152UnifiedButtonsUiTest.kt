package com.ateliertime.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ateliertime.domain.SoundSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** D152 Android UI source: phone script COMPILES but does not EXECUTE it. */
@RunWith(AndroidJUnit4::class)
class D152UnifiedButtonsUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun primaryOutlineTonalTextAndSoundChipHaveOneShapeAndMinWidth() {
        var selected by mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                Column(Modifier.width(320.dp)) {
                    AtelierButton(onClick = {}, modifier = Modifier.testTag("a_filled")) {
                        Text("Run")
                    }
                    AtelierOutlinedButton(onClick = {},
                        modifier = Modifier.testTag("a_outlined")) {
                        Text("Edit")
                    }
                    AtelierTonalButton(onClick = {},
                        modifier = Modifier.testTag("a_tonal")) {
                        Text("Duplicate")
                    }
                    AtelierTextButton(onClick = {},
                        modifier = Modifier.testTag("a_text")) {
                        Text("Help")
                    }
                    AtelierFilterChip(
                        selected = selected,
                        onClick = { selected = !selected },
                        modifier = Modifier.testTag("a_chip"),
                        label = { Text("Media") },
                    )
                }
            }
        }
        assertEquals(RoundedCornerShape(12.dp), AtelierActionStyle.shape)
        assertEquals(1.dp, AtelierActionStyle.stroke)
        for (tag in listOf("a_filled","a_outlined","a_tonal","a_text","a_chip")) {
            val bounds = compose.onNodeWithTag(tag).getUnclippedBoundsInRoot()
            assertTrue("$tag should be at least 40dp tall",
                bounds.bottom - bounds.top >= AtelierActionStyle.visibleHeight)
            assertTrue("$tag should never be narrower than 56dp",
                bounds.right - bounds.left >= AtelierActionStyle.minimumWidth)
        }
        compose.onNodeWithTag("a_chip").performClick()
        assertEquals(true, selected)
    }

    @Test fun compactClassAndFreeControlsShareSameVisualHeightAnd48dpHitArea() {
        compose.setContent {
            MaterialTheme {
                Column(Modifier.width(360.dp)) {
                    RunningClassQuickControls(
                        keepScreenAwake = false,
                        onKeepScreenAwakeChange = {},
                        useAlarmVolume = false,
                        selectedVolumePercent = 45,
                        onSelectedVolumeChange = {},
                        onVolumeMax = {},
                        onSoundsOff = {},
                    )
                    SlimTimerAction(label = "Pause", tag = "a_timer", onClick = {})
                }
            }
        }
        val awake = compose.onNodeWithTag("keep_screen_awake_visual")
            .getUnclippedBoundsInRoot()
        val volume = compose.onNodeWithTag("running_volume_actions_border")
            .getUnclippedBoundsInRoot()
        val timer = compose.onNodeWithTag("a_timer_visual")
            .getUnclippedBoundsInRoot()
        for (bounds in listOf(awake,volume,timer)) {
            assertEquals(AtelierActionStyle.visibleHeight,
                bounds.bottom - bounds.top)
        }
        val hit = compose.onNodeWithTag("a_timer").getUnclippedBoundsInRoot()
        assertTrue(hit.bottom - hit.top >= AtelierActionStyle.touchHeight)
        compose.onNodeWithTag("running_media_volume_slider").assertExists()
        compose.onNodeWithTag("running_alarm_volume_slider").assertDoesNotExist()
    }

    @Test fun soundsPreviewTriangleSharesSameRounded40dpFrameAnd48dpHitArea() {
        compose.setContent {
            MaterialTheme {
                Column(Modifier.width(350.dp).verticalScroll(rememberScrollState())) {
                    MelodySettingsSection(
                        settings = SoundSettings(),
                        onSettingsChange = {},
                        onChooseSystemSound = {},
                        onChooseAudioFile = {},
                        onPreview = {},
                        onError = {},
                    )
                }
            }
        }
        val clickable = compose.onNodeWithTag("sound_preview_pose_finished")
            .performScrollTo().getUnclippedBoundsInRoot()
        val outline = compose.onNodeWithTag("sound_preview_visual_pose_finished")
            .getUnclippedBoundsInRoot()
        assertEquals(AtelierActionStyle.visibleHeight,
            outline.bottom - outline.top)
        assertEquals(AtelierActionStyle.touchHeight,
            clickable.bottom - clickable.top)
        assertEquals(AtelierActionStyle.visibleHeight,
            outline.right - outline.left)
    }

}
