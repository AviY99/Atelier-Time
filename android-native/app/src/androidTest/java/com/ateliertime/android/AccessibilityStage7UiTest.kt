package com.ateliertime.android

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ateliertime.domain.FreeTimerStatus
import com.ateliertime.domain.FreeTimerView
import com.ateliertime.domain.PhaseKind
import com.ateliertime.domain.SchedulePhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** D-127 Stage 7: isolated UI semantics/target-size tests; no real app data. */
@RunWith(AndroidJUnit4::class)
class AccessibilityStage7UiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun clockWheelButtonsHaveSpokenContextAndKeepBlockedBoundary() {
        var selected by mutableIntStateOf(10)
        var rejected by mutableStateOf<Int?>(null)
        compose.setContent {
            MaterialTheme {
                EndClockWheel(
                    label = "Minutes", value = selected, limit = 60, tag = "qa_wheel",
                    canSelect = { it in 10..11 },
                    onBlocked = { rejected = it },
                    onChange = { selected = it },
                )
            }
        }
        val previous = compose.onNodeWithTag("qa_wheel_previous")
        val spoken = previous.fetchSemanticsNode()
            .config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue(spoken.contains("Minutes, previous value 09"))
        previous.assertIsEnabled().performClick()
        compose.runOnIdle {
            assertEquals(10, selected)
            assertEquals(9, rejected)
        }
        compose.onNodeWithTag("qa_wheel_next").performClick()
        compose.runOnIdle { assertEquals(11, selected) }
        val currentSpoken = compose.onNodeWithTag("qa_wheel_value")
            .fetchSemanticsNode().config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue(currentSpoken.contains("Minutes, current value 11"))
    }

    @Test fun timelineHasEquivalentNonColorDescription() {
        val phases = listOf(
            SchedulePhase("s", PhaseKind.POSE, 0, 60),
            SchedulePhase("b", PhaseKind.SHORT_BREAK, 60, 90),
            SchedulePhase("x", PhaseKind.SUMMARY, 90, 120),
        )
        compose.setContent { MaterialTheme { TimelinePhaseChart(phases) } }
        val description = compose.onNodeWithTag("class_timeline_accessible_chart")
            .fetchSemanticsNode().config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue(description.contains("Session time 01:00"))
        assertTrue(description.contains("regular breaks 00:30"))
        assertTrue(description.contains("summary 00:30"))
    }

    @Test fun freePresetsHaveAtLeast48DpTouchHeight() {
        compose.setContent {
            MaterialTheme {
                FreeCountdownSection(
                    view = FreeTimerView(FreeTimerStatus.READY, 0, 0),
                    onStart = { _, _, _ -> },
                    onPauseResume = {},
                    onExit = {},
                )
            }
        }
        compose.onNodeWithTag("free_preset_60").assertHeightIsAtLeast(48.dp)
        compose.onNodeWithTag("free_preset_3600").assertHeightIsAtLeast(48.dp)
    }
}
