package com.ateliertime.android

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateliertime.domain.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * End-to-end Compose UI checks use separate randomized Class and Sound stores.
 * Nothing is written to the user's real saved Classes or Sound choices.
 * Only run when there is no actual Class running on the device.
 */
@RunWith(AndroidJUnit4::class)
class RunEndApprovalUiTest {
    private val target = InstrumentationRegistry.getInstrumentation().targetContext
    private val presetNamespace = "atelier_qa_ui_preset_" + UUID.randomUUID()
    private val soundNamespace = "atelier_qa_ui_sound_" + UUID.randomUUID()
    private val store = AndroidPresetStore(target, presetNamespace)
    private val isolatedMelody = MelodyPreferences(target, soundNamespace)

    @get:Rule val compose = createComposeRule()

    @Before fun seedOnlyTestLibrary() {
        val engine = ScheduleEngine()
        val approved = engine.approve(engine.generate(SessionRules(
            id = "qa-ui", name = "QA Test Class",
            totalSeconds = 180 * 60, summarySeconds = 15 * 60,
            poseSeconds = 25 * 60, specialBreakCount = 1,
        ), FinalPoseChoice.remainder()))
        val catalog = PresetCatalog(store, engine) { UUID.randomUUID().toString() }
        catalog.save(approved, "QA Test Class")
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
        compose.onNodeWithText("Timer", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Run", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("run_end_dialog").assertExists()
        compose.onNodeWithText("Confirm Class finish time").assertExists()
    }

    @Test fun runShowsPredictedFinishAndCancelDoesNotStartClass() {
        openRunApproval()
        compose.onNodeWithText("Calculated finish:", substring = true).assertExists()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithTag("run_end_dialog").assertDoesNotExist()
        compose.onNodeWithText("Class timer").assertDoesNotExist()
        compose.onNodeWithText("Saved classes").assertExists()
    }

    @Test fun invalidClockIsRejectedInDialogAndUserCanCorrectIt() {
        openRunApproval()
        compose.onNodeWithTag("run_end_override").performClick()
        compose.onNodeWithTag("run_end_clock").performTextInput("25:99")
        compose.onNodeWithTag("run_end_confirm").performClick()
        compose.onNodeWithText("Enter a valid finish time as HH:mm", substring = true).assertExists()
        compose.onNodeWithTag("run_end_dialog").assertExists()
        compose.onNodeWithText("Class timer").assertDoesNotExist()
        compose.onNodeWithText("Cancel").performClick()
    }

    @Test fun confirmingCalculatedFinishLaunchesClassAndExitReturnsToLibrary() {
        openRunApproval()
        compose.onNodeWithTag("run_end_confirm").performClick()
        compose.onNodeWithText("Class timer").assertExists()
        compose.onNodeWithText("Running").assertExists()
        compose.onNodeWithText("Exit", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Confirm").performClick()
        compose.onNodeWithText("Timer · saved classes").assertExists()
    }

    private fun clockFromNow(minutesAhead: Int): String =
        SimpleDateFormat("HH:mm", Locale.US).format(
            Calendar.getInstance().apply { add(Calendar.MINUTE, minutesAhead) }.time
        )

    private fun chooseFinish(minutesAhead: Int) {
        compose.onNodeWithTag("run_end_override").performClick()
        compose.onNodeWithTag("run_end_clock").performTextInput(clockFromNow(minutesAhead))
    }

    @Test fun impossibleEarlyFinishKeepsDialogOpenForCorrection() {
        openRunApproval()
        chooseFinish(10) // Requires more than the adjustable last pair can remove.
        compose.onNodeWithTag("run_end_confirm").performClick()
        compose.onNodeWithTag("run_end_dialog").assertExists()
        compose.onNodeWithText("last two Sessions", substring = true).assertExists()
        compose.onNodeWithText("Class timer").assertDoesNotExist()
    }

    @Test fun unrealisticLaterFinishCannotTurnIntoOvernightClass() {
        openRunApproval()
        chooseFinish(370) // More than twice the original three-hour Class.
        compose.onNodeWithTag("run_end_confirm").performClick()
        compose.onNodeWithTag("run_end_dialog").assertExists()
        compose.onNodeWithText("too late", substring = true).assertExists()
        compose.onNodeWithText("Class timer").assertDoesNotExist()
    }

    @Test fun validEarlierFinishCanBeApprovedAndStartsClass() {
        openRunApproval()
        chooseFinish(175)
        compose.onNodeWithText("Final two Sessions:", substring = true).assertExists()
        compose.onNodeWithTag("run_end_confirm").performClick()
        compose.onNodeWithTag("run_end_dialog").assertDoesNotExist()
        compose.onNodeWithText("Class timer").assertExists()
        compose.onNodeWithText("Running").assertExists()
    }

    @Test fun validLaterFinishCanBeApprovedAndStartsClass() {
        openRunApproval()
        chooseFinish(190)
        compose.onNodeWithText("Final two Sessions:", substring = true).assertExists()
        compose.onNodeWithTag("run_end_confirm").performClick()
        compose.onNodeWithTag("run_end_dialog").assertDoesNotExist()
        compose.onNodeWithText("Class timer").assertExists()
        compose.onNodeWithText("Running").assertExists()
    }

}
