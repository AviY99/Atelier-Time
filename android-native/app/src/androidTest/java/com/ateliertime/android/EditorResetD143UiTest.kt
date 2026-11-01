package com.ateliertime.android

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateliertime.domain.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** D-143: isolated device UI regressions. Authored only; do not auto-run on owner's phone. */
@RunWith(AndroidJUnit4::class)
class EditorResetD143UiTest {
    private val target = InstrumentationRegistry.getInstrumentation().targetContext
    private val presetNamespace = "atelier_qa_d143_editor_" + UUID.randomUUID()
    private val soundNamespace = "atelier_qa_d143_sounds_" + UUID.randomUUID()
    private val store = AndroidPresetStore(target, presetNamespace)
    private val sounds = MelodyPreferences(target, soundNamespace)
    @get:Rule val compose = createComposeRule()

    @After fun cleanOnlyTestNamespaces() {
        target.deleteSharedPreferences(presetNamespace)
        target.deleteSharedPreferences(soundNamespace)
    }

    @Test fun threeButtonsShareOneLineWithResetFirstAndCancelKeepsDraft() {
        compose.setContent {
            MaterialTheme {
                SessionEditorScreen(presetStoreOverride = store,
                    melodyPreferencesOverride = sounds)
            }
        }
        val reset = compose.onNodeWithTag("editor_reset_button").getUnclippedBoundsInRoot()
        val exampleA = compose.onNodeWithTag("editor_example_a_button").getUnclippedBoundsInRoot()
        val exampleB = compose.onNodeWithTag("editor_example_b_button").getUnclippedBoundsInRoot()
        assertTrue("Reset must be first in the horizontal row", reset.right <= exampleA.left)
        assertTrue("Example B must follow Example A", exampleA.right <= exampleB.left)
        assertEquals(reset.top, exampleA.top)
        assertEquals(reset.top, exampleB.top)

        compose.onNodeWithTag("editor_example_b_button").performClick()
        compose.onNodeWithTag("editor_class_title")
            .assertTextContains("Example B", substring = true)
        compose.onNodeWithTag("editor_reset_button").performClick()
        compose.onNodeWithTag("editor_reset_dialog").assertExists()
        compose.onNodeWithTag("editor_reset_cancel").performClick()
        compose.onNodeWithTag("editor_reset_dialog").assertDoesNotExist()
        compose.onNodeWithTag("editor_class_title")
            .assertTextContains("Example B", substring = true)
        compose.onNodeWithTag("editor_reset_button").performClick()
        compose.onNodeWithTag("editor_reset_confirm").performClick()
        compose.onNodeWithTag("editor_reset_dialog").assertDoesNotExist()
        compose.onNodeWithTag("editor_class_title")
            .assertTextContains("Atelier class", substring = true)
        compose.onNodeWithTag("editor_example_a_button").assertExists()
        compose.onNodeWithTag("editor_example_b_button").assertExists()
    }

    @Test fun resetDoesNotTouchSavedClasses() {
        val engine = ScheduleEngine()
        val catalog = PresetCatalog(store, engine) { UUID.randomUUID().toString() }
        val rules = freshEditorInput().toRules().copy(name = "Keep saved")
        val before = engine.generate(rules)
        val candidate = if (before.needsFinalPoseChoice)
            engine.generate(rules, FinalPoseChoice.remainder()) else before
        val saved = catalog.save(engine.approve(candidate, acknowledgeWarnings = true), "Keep saved")
        val signature = saved.timelineSignature
        compose.setContent {
            MaterialTheme {
                SessionEditorScreen(presetStoreOverride = store,
                    melodyPreferencesOverride = sounds)
            }
        }
        compose.onNodeWithTag("editor_example_b_button").performClick()
        compose.onNodeWithTag("editor_reset_button").performClick()
        compose.onNodeWithTag("editor_reset_confirm").performClick()
        val checked = PresetCatalog(store, engine) { UUID.randomUUID().toString() }
        assertEquals(1, checked.state().entries.size)
        assertEquals(signature, checked.state().entries.single().timelineSignature)
        assertEquals("Keep saved", checked.open(saved.id).rules.name)
    }
}
