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

/** D-147 compile-only AndroidTest source. No automatic device instrumentation. */
@RunWith(AndroidJUnit4::class)
class D147RunningClassQuickControlsUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun restoredFullLabelsFitInTwoAdjacentControlRows() {
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(360.dp)) {
                    RunningClassQuickControls(
                        keepScreenAwake = true,
                        onKeepScreenAwakeChange = {},
                        mediaPercent = 75,
                        alarmPercent = 40,
                        useAlarmVolume = false,
                        onMaxBoth = {},
                    )
                }
            }
        }
        compose.onNodeWithText("Keep screen awake during timers").assertExists()
        compose.onNodeWithText("Uses more battery").assertExists()
        compose.onNodeWithText("Class sound volume").assertExists()
        compose.onNodeWithText("Media 75% · Alarm 40% · using Media").assertExists()
        compose.onNodeWithText("Max both").assertExists()

        val first = compose.onNodeWithTag("running_screen_awake_row")
            .getUnclippedBoundsInRoot()
        val second = compose.onNodeWithTag("running_class_volume_row")
            .getUnclippedBoundsInRoot()
        assertTrue("Full labels must occupy two consecutive rows",
            second.top >= first.bottom && second.top - first.bottom <= 2.dp)
        assertTrue("Rows retain a readable touch size", first.bottom - first.top >= 48.dp &&
            second.bottom - second.top >= 48.dp)
    }

    @Test fun userCanStillToggleScreenAndOpenTheExistingMaxBothConfirmation() {
        var stayAwake by mutableStateOf(false)
        var maxRequests = 0
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(360.dp)) {
                    RunningClassQuickControls(
                        keepScreenAwake = stayAwake,
                        onKeepScreenAwakeChange = { stayAwake = it },
                        mediaPercent = 20, alarmPercent = 90,
                        useAlarmVolume = true,
                        onMaxBoth = { maxRequests++ },
                    )
                }
            }
        }
        compose.onNodeWithText("Media 20% · Alarm 90% · using Alarm").assertExists()
        compose.onNodeWithTag("keep_screen_awake").performClick()
        assertTrue(stayAwake)
        compose.onNodeWithTag("class_volume_max_both").performClick()
        assertEquals(1, maxRequests)
    }
}
