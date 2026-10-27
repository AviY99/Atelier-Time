package com.ateliertime.android

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * A slim 5dp track and small rounded thumb, with the normal Material slider
 * gesture/accessibility target. The underlying selections and increments stay
 * exactly the same in Sound and Editor, including the end-of-drag callbacks.
 */
@Composable
internal fun AtelierSlimSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    // Editor's allowed final-pair range changes with the approved schedule.
    // Recreate only that slider's range, not the user's saved selection.
    key(valueRange.start, valueRange.endInclusive, steps) {
        val state = rememberSliderState(
            value = value, steps = steps, trackRange = valueRange,
        )
        SideEffect {
            if (state.value != value) state.value = value
        }
        val interaction = remember { MutableInteractionSource() }
        val colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.secondary,
            activeTrackColor = MaterialTheme.colorScheme.secondary,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
        )
        Slider(
            state = state,
            onValueChange = { changed ->
                state.value = changed
                onValueChange(changed)
            },
            onValueChangeFinished = onValueChangeFinished,
            modifier = modifier,
            interactionSource = interaction,
            colors = colors,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interaction,
                    colors = colors,
                    thumbSize = DpSize(14.dp, 22.dp),
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(5.dp),
                    colors = colors,
                    drawStopIndicator = null,
                    drawTick = { _, _ -> },
                    thumbTrackGapSize = 0.dp,
                )
            },
        )
    }
}
