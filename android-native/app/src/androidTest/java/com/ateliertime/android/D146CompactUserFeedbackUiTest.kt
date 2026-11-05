package com.ateliertime.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ateliertime.domain.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * D-146 UI regression source. The Samsung local build COMPILES these tests
 * but does not install or execute instrumentation against owner's app data.
 */
@RunWith(AndroidJUnit4::class)
class D146CompactUserFeedbackUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun timerActionsAreDirectlyBelowFrameAndHelpIsUnderButtons() {
        val engine = ScheduleEngine()
        val rules = SessionRules(
            id = "d146-ui", name = "Compact timer",
            totalSeconds = 180 * 60, summarySeconds = 15 * 60,
            poseSeconds = 25 * 60,
        )
        val approved = engine.approve(
            engine.generate(rules, FinalPoseChoice.remainder()),
            acknowledgeWarnings = true,
        )
        var now = 0L
        val timer = MonotonicSessionTimer(approved) { now }
        val view = timer.start()
        compose.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    SessionTimerScreen(
                        session = approved, timer = timer, view = view,
                        wallClockMillis = 0L,
                        classClockTimes = ClassClockTimes(),
                        timerError = null, designPalette = DesignPalette.DAY,
                        eventHistory = emptyList(),
                        onPauseResume = {},
                        onRestartSession = {},
                        onRepeatLast = {},
                        onNextPhase = {},
                        onExit = {},
                    )
                }
            }
        }

        compose.onNodeWithText("Class controls").assertDoesNotExist()
        val face = compose.onNodeWithTag("class_timer_face").getUnclippedBoundsInRoot()
        val controls = compose.onNodeWithTag("class_controls_row")
            .performScrollTo().getUnclippedBoundsInRoot()
        val help = compose.onNodeWithTag("class_controls_help")
            .performScrollTo().getUnclippedBoundsInRoot()
        assertTrue("Class actions should immediately follow timer face",
            controls.top >= face.bottom && (controls.top - face.bottom).value <= 12f)
        assertTrue("Help must be below Class action buttons", help.top >= controls.bottom)

        val tags = listOf("class_control_pause_resume", "class_control_restart",
            "class_control_repeat_last", "class_control_next", "class_control_exit")
        val positions = tags.map { tag ->
            compose.onNodeWithTag(tag).getUnclippedBoundsInRoot()
        }
        assertTrue("Five controls must share a single row",
            positions.all { abs((it.top - positions.first().top).value) <= 1f })
        assertTrue("Controls must be in the agreed left-to-right order",
            positions.zipWithNext().all { (left, right) ->
                left.right <= right.left
            })
    }

    @Test fun soundPreviewPlayTrianglesRemainBesidePickersAndHaveSpokenLabels() {
        var previews = 0
        compose.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MelodySettingsSection(
                        settings = SoundSettings(),
                        onSettingsChange = {},
                        onChooseSystemSound = {},
                        onChooseAudioFile = {},
                        onPreview = { previews++ },
                        onError = {},
                    )
                }
            }
        }
        for (cue in listOf("pose_finished", "break_finished",
            "session_finished", "free_timer_finished")) {
            val picker = compose.onNodeWithTag("sound_picker_" + cue)
                .performScrollTo().getUnclippedBoundsInRoot()
            val button = compose.onNodeWithTag("sound_preview_" + cue)
                .performScrollTo().getUnclippedBoundsInRoot()
            assertTrue("Play icon must stay to right of its sound picker: " + cue,
                button.left >= picker.right)
            val spoken = compose.onNodeWithTag("sound_preview_" + cue)
                .fetchSemanticsNode()
                .config[SemanticsProperties.ContentDescription].joinToString()
            assertTrue("Missing spoken Play label for " + cue, spoken.contains("Play"))
            assertTrue("Missing preview duration for " + cue,
                spoken.contains("second"))
        }
        compose.onNodeWithText("Play 5s").assertDoesNotExist()
        compose.onNodeWithTag("sound_preview_pose_finished")
            .performScrollTo().performClick()
        assertEquals(1, previews)
    }
}
