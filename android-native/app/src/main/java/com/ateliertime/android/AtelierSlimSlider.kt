package com.ateliertime.android

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * A visually slim slider using the stable value-based Material3 API already
 * exercised elsewhere in Atelier Time. Ordinary controls keep the compact
 * visual; the Class/Free volume bar opts OUT of vertical shrinking so the
 * thumb can actually be grabbed at zero, with a >=56dp hit strip.
 */
@Composable
internal fun AtelierSlimSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    compactVisual: Boolean = true,
    volumeVisual: Boolean = false,
) {
    val colors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.secondary,
        activeTrackColor = MaterialTheme.colorScheme.secondary,
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
    )
    if (volumeVisual) {
        // D157: same true Material3 Slider hit/drag semantics, but custom
        // slender 12dp track (3/4 of the former 16dp default). No misleading
        // Material3 end-stop dot or oversized thumb/track gap at low volume.
        // BOTH Free and Class render this exact same track and thumb.
        val activeColor = MaterialTheme.colorScheme.secondary
        val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            modifier = modifier,
            colors = colors,
            thumb = {
                Box(
                    Modifier.size(width = 10.dp, height = 24.dp)
                        .background(activeColor, RoundedCornerShape(6.dp))
                )
            },
            track = { state ->
                val span = (state.valueRange.endInclusive -
                    state.valueRange.start).coerceAtLeast(0.001f)
                val fraction = ((state.value - state.valueRange.start) / span)
                    .coerceIn(0f, 1f)
                Canvas(Modifier.fillMaxWidth().height(12.dp)) {
                    val radius = CornerRadius(size.height / 2f)
                    drawRoundRect(color = inactiveColor, cornerRadius = radius)
                    if (fraction > 0f) {
                        drawRoundRect(
                            color = activeColor,
                            size = Size(size.width * fraction, size.height),
                            cornerRadius = radius,
                        )
                    }
                }
            },
        )
    } else {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            modifier = if (compactVisual) modifier.graphicsLayer(scaleY = 0.55f)
                else modifier,
            colors = colors,
        )
    }
}
