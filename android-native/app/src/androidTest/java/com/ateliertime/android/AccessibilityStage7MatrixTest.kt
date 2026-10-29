package com.ateliertime.android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateliertime.domain.DesignMode
import com.ateliertime.domain.DesignPalette
import com.ateliertime.domain.DesignPreferences
import com.ateliertime.domain.FreeTimerStatus
import com.ateliertime.domain.FreeTimerView
import com.ateliertime.domain.PhaseKind
import com.ateliertime.domain.SchedulePhase
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * D-127 Stage 7 incremental accessibility MATRIX tests.
 * Only new coverage, not a rerun of AccessibilityStage7UiTest (already 6/6 PASS).
 * No system settings changes, network, device sound, active timer or real user stores.
 * Fake Compose font scale is isolated to the test composition; phone stays unchanged.
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityStage7MatrixTest {
    @get:Rule val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val presetNamespace = "atelier_matrix_presets_" + UUID.randomUUID()
    private val soundNamespace = "atelier_matrix_sounds_" + UUID.randomUUID()
    private val isolatedPresets = AndroidPresetStore(context, presetNamespace)
    private val isolatedSounds = MelodyPreferences(context, soundNamespace)

    @After fun cleanupOnlyMatrixPreferences() {
        context.deleteSharedPreferences(presetNamespace)
        context.deleteSharedPreferences(soundNamespace)
    }

    /** Scale the Compose typography without changing Android global font settings. */
    private fun render(scale: Float, content: @Composable () -> Unit) {
        compose.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(base.density, scale)
            ) {
                MaterialTheme { content() }
            }
        }
    }

    private fun tabs() = listOf(
        "Editor", "Programed Timer", "Free Timer", "Sound", "Design", "About"
    )


    @Test fun font100AllSixTabsRemainReachable() {
        render(1.0f) {
            SessionEditorScreen(
                presetStoreOverride = isolatedPresets,
                melodyPreferencesOverride = isolatedSounds,
            )
        }
        tabs().forEach { label ->
            compose.onNodeWithTag("top-tab-" + label).assertExists()
        }
        compose.onNodeWithTag("top-tab-Editor").assertIsSelected()
    }

    @Test fun font130CanScrollToLastTabAndSelectIt() {
        render(1.3f) {
            SessionEditorScreen(
                presetStoreOverride = isolatedPresets,
                melodyPreferencesOverride = isolatedSounds,
            )
        }
        tabs().forEach { label ->
            compose.onNodeWithTag("top-tab-" + label).assertExists()
        }
        compose.onNodeWithTag("top-tab-About").performScrollTo()
            .assertIsDisplayed().performClick().assertIsSelected()
        compose.onNodeWithTag("top-tab-Editor").performScrollTo()
            .assertIsDisplayed().performClick().assertIsSelected()
    }

    @Test fun font200CanScrollToLastTabAndSelectIt() {
        render(2.0f) {
            SessionEditorScreen(
                presetStoreOverride = isolatedPresets,
                melodyPreferencesOverride = isolatedSounds,
            )
        }
        compose.onNodeWithTag("top-tab-About").performScrollTo()
            .assertIsDisplayed().performClick().assertIsSelected()
        compose.onNodeWithTag("top-tab-Editor").performScrollTo()
            .assertIsDisplayed().performClick().assertIsSelected()
    }

    @Test fun font200NarrowFreeWheelsStayInsideCardAndPresetsRemainWide() {
        render(2.0f) {
            Box(Modifier.width(280.dp)) {
                FreeCountdownSection(
                    view = FreeTimerView(FreeTimerStatus.READY, 0, 0),
                    onStart = { _, _, _ -> },
                    onPauseResume = {},
                    onExit = {},
                )
            }
        }
        val card = compose.onNodeWithTag("free_timer_section")
            .getUnclippedBoundsInRoot()
        listOf("free_hours", "free_minutes", "free_seconds").forEach { tag ->
            val b = compose.onNodeWithTag(tag).getUnclippedBoundsInRoot()
            assertTrue(tag + " starts outside card", b.left >= card.left - 1.dp)
            assertTrue(tag + " exceeds card", b.right <= card.right + 1.dp)
        }
        val preset = compose.onNodeWithTag("free_preset_60")
            .getUnclippedBoundsInRoot()
        assertTrue("200% font requires two preset columns", preset.right - preset.left >= 100.dp)
        compose.onNodeWithTag("free_timer_start").assertExists()
        compose.onNodeWithTag("free_timer_exit").assertIsNotEnabled()
    }

    @Test fun font130NarrowFreePresetsUseTwoColumns() {
        render(1.3f) {
            Box(Modifier.width(280.dp)) {
                FreeCountdownSection(
                    view = FreeTimerView(FreeTimerStatus.READY, 0, 0),
                    onStart = { _, _, _ -> },
                    onPauseResume = {},
                    onExit = {},
                )
            }
        }
        val b = compose.onNodeWithTag("free_preset_3600").getUnclippedBoundsInRoot()
        assertTrue("130% font requires two preset columns", b.right - b.left >= 100.dp)
        listOf("free_hours", "free_minutes", "free_seconds").forEach {
            compose.onNodeWithTag(it).assertExists()
        }
    }

    @Test fun font200DesignPaletteAndAutoSliderHaveSpokenNames() {
        var preferences by mutableStateOf(DesignPreferences(mode = DesignMode.DAY))
        render(2.0f) {
            Box(Modifier.width(320.dp)) {
                DesignSettingsPanel(
                    settings = preferences,
                    currentPalette = DesignPalette.DAY,
                    currentClockMillis = 0L,
                    onPreviewHourChange = {},
                    onChange = { preferences = it },
                )
            }
        }
        compose.onNodeWithTag("design_palette_day").assertIsSelected()
        val label = compose.onNodeWithTag("design_palette_night")
            .fetchSemanticsNode().config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue(label.contains("Night"))
        compose.onNodeWithText("Auto · Switch Day / Night by time")
            .performScrollTo().performClick()
        val spoken = compose.onNodeWithTag("design_auto_night_start_slider")
            .fetchSemanticsNode().config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue(spoken.contains("Night begins at"))
    }

    @Test fun font200SoundSlidersRetainSpokenPurposeAndSeconds() {
        var soundSettings by mutableStateOf(isolatedSounds.loadSettings())
        render(2.0f) {
            Column(Modifier.width(320.dp).verticalScroll(rememberScrollState())) {
                MelodySettingsSection(
                    settings = soundSettings,
                    onSettingsChange = { soundSettings = it },
                    onChooseSystemSound = {},
                    onChooseAudioFile = {},
                    onPreview = {},
                    onError = {},
                )
            }
        }
        val classSlider = compose.onNodeWithTag("sound_class_end_length_slider")
            .performScrollTo().fetchSemanticsNode()
            .config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue(classSlider.contains("Class C end sound length"))
        assertTrue(classSlider.contains("seconds"))
        val freeSlider = compose.onNodeWithTag("sound_free_length_slider")
            .performScrollTo().fetchSemanticsNode()
            .config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue(freeSlider.contains("Free countdown completion sound length"))
        assertTrue(freeSlider.contains("seconds"))
    }

    @Test fun font200NarrowTimelineRetainsNonColorAlternative() {
        val phases = listOf(
            SchedulePhase("a", PhaseKind.POSE, 0, 60),
            SchedulePhase("b", PhaseKind.SHORT_BREAK, 60, 90),
            SchedulePhase("c", PhaseKind.SPECIAL_BREAK, 90, 120),
            SchedulePhase("d", PhaseKind.SUMMARY, 120, 150),
        )
        render(2.0f) {
            Box(Modifier.width(280.dp)) {
                TimelinePhaseChart(phases)
            }
        }
        val spoken = compose.onNodeWithTag("class_timeline_accessible_chart")
            .fetchSemanticsNode().config[SemanticsProperties.ContentDescription].joinToString()
        listOf("Session time", "regular breaks", "special breaks", "summary").forEach {
            assertTrue("Missing timeline alternative: " + it, spoken.contains(it))
        }
        compose.onNodeWithTag("class_timeline_color_legend").assertExists()
    }
}
