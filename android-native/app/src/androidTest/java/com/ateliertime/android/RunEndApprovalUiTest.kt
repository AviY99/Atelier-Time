package com.ateliertime.android

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateliertime.domain.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * D-115 Compose regression suite, preserving D-114 finish-wheel coverage.
 *
 * Only randomized Class/Sound preference namespaces are used. The device must
 * have NO real Class running: Compose instrumentation briefly owns the target
 * process and exercises an isolated running test Class/notification.
 * Installation and execution require separate device permission.
 */
@RunWith(AndroidJUnit4::class)
class RunEndApprovalUiTest {
    private val target = InstrumentationRegistry.getInstrumentation().targetContext
    private val presetNamespace = "atelier_qa_ui_preset_" + UUID.randomUUID()
    private val soundNamespace = "atelier_qa_ui_sound_" + UUID.randomUUID()
    private val store = AndroidPresetStore(target, presetNamespace)
    private val isolatedMelody = MelodyPreferences(target, soundNamespace)
    private val engine = ScheduleEngine()
    private val catalog = PresetCatalog(store, engine) { UUID.randomUUID().toString() }
    private lateinit var storedId: String
    private lateinit var savedSignature: String

    @get:Rule val compose = createComposeRule()

    @Before fun seedOnlyTestLibrary() {
        val approved = engine.approve(engine.generate(SessionRules(
            id = "qa-ui", name = "QA Test Class",
            totalSeconds = 180 * 60, summarySeconds = 15 * 60,
            poseSeconds = 25 * 60, specialBreakCount = 1,
        ), FinalPoseChoice.remainder()))
        val entry = catalog.save(approved, "QA Test Class")
        storedId = entry.id
        savedSignature = PresetCatalog.signature(catalog.open(storedId))
    }

    @After fun removeOnlyTestArtifacts() {
        ClassNotificationTaskService.dismiss(target)
        target.deleteSharedPreferences(presetNamespace)
        target.deleteSharedPreferences(soundNamespace)
    }

    private fun openRunApproval() {
        compose.setContent {
            MaterialTheme {
                SessionEditorScreen(
                    presetStoreOverride = store,
                    melodyPreferencesOverride = isolatedMelody,
                )
            }
        }
        compose.onNodeWithTag("top-tab-Programed Timer").performClick()
        compose.onNodeWithText("Run", useUnmergedTree = true)
            .performScrollTo().performClick()
        compose.onNodeWithTag("run_end_dialog").assertExists()
        compose.onNodeWithText("Confirm Class finish time").assertExists()
    }

    private fun textAt(tag: String): String =
        compose.onNodeWithTag(tag).fetchSemanticsNode()
            .config[SemanticsProperties.Text].joinToString("") { it.text }

    private fun wheelValue(tag: String): Int = textAt(tag + "_value").trim().toInt()

    private fun clickWheel(tag: String) {
        compose.onNodeWithTag(tag).performScrollTo().performClick()
    }

    private fun showWheels() {
        compose.onNodeWithTag("run_end_change_finish").performClick()
        compose.onNodeWithTag("run_end_wheels").assertExists()
        compose.onNodeWithTag("run_end_hour").assertExists()
        compose.onNodeWithTag("run_end_minute").assertExists()
    }

    private fun assertStoredClassUnchanged() {
        val reread = PresetCatalog(store, engine) { UUID.randomUUID().toString() }
        assertEquals(savedSignature, PresetCatalog.signature(reread.open(storedId)))
        assertEquals(1, reread.state().entries.size)
    }

    @Test fun freeCountdownStartPauseResumeExitDoesNotEditSavedClass() {
        compose.setContent {
            MaterialTheme {
                SessionEditorScreen(presetStoreOverride = store,
                    melodyPreferencesOverride = isolatedMelody)
            }
        }
        compose.onNodeWithTag("top-tab-Programed Timer").performClick()
        compose.onNodeWithTag("free_timer_section").assertDoesNotExist()
        compose.onNodeWithTag("top-tab-Free Timer").performClick()
        compose.onNodeWithTag("free_timer_section").assertExists()
        compose.onNodeWithTag("free_preset_60").performScrollTo().performClick()
        compose.onNodeWithTag("free_timer_start").performClick()
        compose.onNodeWithTag("free_preset_3600").assertIsNotEnabled()
        compose.onNodeWithTag("free_timer_pause_resume").performClick()
        compose.onNodeWithTag("free_timer_status").assertTextContains("Paused")
        compose.onNodeWithTag("free_timer_pause_resume").performClick()
        compose.onNodeWithTag("free_timer_status").assertTextContains("Running")
        compose.onNodeWithTag("free_timer_exit").performClick()
        compose.onNodeWithTag("free_timer_status").assertTextContains("Ready")
        compose.onNodeWithTag("free_keep_screen_awake").performClick()
        assertStoredClassUnchanged()
    }

    /** Guaranteed +1 minute, including HH:59 rollover. */
    private fun chooseOneMinuteLater() {
        val minute = wheelValue("run_end_minute")
        if (minute == 59) clickWheel("run_end_hour_next")
        clickWheel("run_end_minute_next")
    }

    /** -1 minute, including 00:00 -> 23:59 (same-day next occurrence). */
    private fun chooseOneMinuteEarlier() {
        val minute = wheelValue("run_end_minute")
        if (minute == 0) clickWheel("run_end_minute_previous")
        clickWheel(if (minute == 0) "run_end_hour_previous" else "run_end_minute_previous")
    }

    @Test fun predictedFinishIsLiveWithoutShowingWheelsByDefault() {
        openRunApproval()
        compose.onNodeWithTag("run_end_calculated").assertExists()
        compose.onNodeWithTag("run_end_selected").assertExists()
        compose.onNodeWithTag("run_end_change_finish").assertExists()
        compose.onNodeWithTag("run_end_wheels").assertDoesNotExist()
        compose.onNodeWithTag("run_end_hour").assertDoesNotExist()
        compose.onNodeWithTag("run_end_minute").assertDoesNotExist()
        val before = textAt("run_end_calculated")
        compose.waitUntil(timeoutMillis = 6_000L) {
            textAt("run_end_calculated") != before
        }
        assertStoredClassUnchanged()
    }

    @Test fun openingPickerWithoutMovingWheelsStillTracksLiveFinish() {
        openRunApproval()
        showWheels()
        val before = textAt("run_end_selected")
        compose.waitUntil(timeoutMillis = 6_000L) {
            textAt("run_end_selected") != before
        }
        compose.onNodeWithTag("run_end_blocked_reason").assertDoesNotExist()
        compose.onNodeWithTag("run_end_confirm").assertIsEnabled()
    }

    @Test fun cancelBeforeRunDoesNotActivateClassOrChangeSavedData() {
        openRunApproval()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithTag("run_end_dialog").assertDoesNotExist()
        compose.onNodeWithText("Class timer").assertDoesNotExist()
        assertTrue(catalog.state().activeId == null)
        assertStoredClassUnchanged()
    }

    @Test fun tappingRedEarlierHourStopsAtGreenAndShowsAttemptedClockReason() {
        openRunApproval()
        showWheels()
        val before = wheelValue("run_end_hour")
        clickWheel("run_end_hour_previous") // 180 -> about 120 minutes: impossible.
        assertEquals(before, wheelValue("run_end_hour"))
        compose.onNodeWithTag("run_end_blocked_reason").assertExists()
        assertTrue(textAt("run_end_blocked_reason").contains("Cannot select"))
        assertTrue(textAt("run_end_blocked_reason").contains("final Session(s)") ||
            textAt("run_end_blocked_reason").contains("too late"))
        compose.onNodeWithTag("run_end_confirm").assertIsEnabled()
        assertTrue(catalog.state().activeId == null)
    }

    @Test fun rapidDownwardSwipeCannotSkipTheFirstRedHour() {
        openRunApproval()
        showWheels()
        val before = wheelValue("run_end_hour")
        compose.onNodeWithTag("run_end_hour").performScrollTo()
            .performTouchInput { swipeDown() }
        assertEquals(before, wheelValue("run_end_hour"))
        compose.onNodeWithTag("run_end_blocked_reason").assertExists()
        assertTrue(catalog.state().activeId == null)
    }

    @Test fun permittedGreenMoveClearsBlockedReason() {
        openRunApproval()
        showWheels()
        clickWheel("run_end_hour_previous")
        compose.onNodeWithTag("run_end_blocked_reason").assertExists()
        // +1 hour is feasible for a 3-hour Class and safely clears feedback.
        clickWheel("run_end_hour_next")
        compose.onNodeWithTag("run_end_blocked_reason").assertDoesNotExist()
        compose.onNodeWithTag("run_end_confirm").assertIsEnabled()
        assertStoredClassUnchanged()
    }

    @Test fun resetHidesWheelsAndClearsBlockedReason() {
        openRunApproval()
        showWheels()
        clickWheel("run_end_hour_previous")
        compose.onNodeWithTag("run_end_blocked_reason").assertExists()
        compose.onNodeWithTag("run_end_reset_calculated")
            .performScrollTo().performClick()
        compose.onNodeWithTag("run_end_wheels").assertDoesNotExist()
        compose.onNodeWithTag("run_end_blocked_reason").assertDoesNotExist()
        compose.onNodeWithTag("run_end_change_finish").assertExists()
        compose.onNodeWithTag("run_end_confirm").assertIsEnabled()
    }

    @Test fun minuteWheelChangesOnlyAfterUserActionAndCanBeReset() {
        openRunApproval()
        showWheels()
        val before = wheelValue("run_end_minute")
        chooseOneMinuteLater()
        val after = wheelValue("run_end_minute")
        assertEquals((before + 1) % 60, after)
        compose.onNodeWithTag("run_end_confirm").assertIsEnabled()
        compose.onNodeWithTag("run_end_reset_calculated")
            .performScrollTo().performClick()
        compose.onNodeWithTag("run_end_wheels").assertDoesNotExist()
        assertStoredClassUnchanged()
    }

    @Test fun confirmingLiveCalculatedFinishStartsOnlyAfterApproval() {
        openRunApproval()
        assertTrue(catalog.state().activeId == null)
        compose.onNodeWithTag("run_end_confirm").performClick()
        compose.onNodeWithTag("run_end_dialog").assertDoesNotExist()
        compose.onNodeWithText("Class timer").assertExists()
        compose.onNodeWithText("Running").assertExists()
        assertStoredClassUnchanged()
    }

    @Test fun feasibleEarlierMinuteIsApprovedWithoutMutatingSavedClass() {
        openRunApproval()
        showWheels()
        chooseOneMinuteEarlier()
        compose.onNodeWithText("Final two Sessions:", substring = true).assertExists()
        compose.onNodeWithTag("run_end_confirm").assertIsEnabled()
        compose.onNodeWithTag("run_end_confirm").performClick()
        compose.onNodeWithTag("run_end_dialog").assertDoesNotExist()
        compose.onNodeWithText("Running").assertExists()
        assertStoredClassUnchanged()
    }

    @Test fun feasibleLaterMinuteIsApprovedWithoutMutatingSavedClass() {
        openRunApproval()
        showWheels()
        chooseOneMinuteLater()
        compose.onNodeWithText("Final two Sessions:", substring = true).assertExists()
        compose.onNodeWithTag("run_end_confirm").assertIsEnabled()
        compose.onNodeWithTag("run_end_confirm").performClick()
        compose.onNodeWithTag("run_end_dialog").assertDoesNotExist()
        compose.onNodeWithText("Running").assertExists()
        assertStoredClassUnchanged()
    }

    @Test fun cancelledCustomFinishNeverRunsOrChangesOriginalSchedule() {
        openRunApproval()
        showWheels()
        chooseOneMinuteLater()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithTag("run_end_dialog").assertDoesNotExist()
        assertFalse(catalog.state().activeId == storedId)
        assertStoredClassUnchanged()
    }
}
