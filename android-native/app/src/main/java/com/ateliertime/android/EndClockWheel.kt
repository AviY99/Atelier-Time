package com.ateliertime.android

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs

/**
 * A discrete hour/minute wheel with a HARD feasibility boundary.
 *
 * Every adjacent step is validated individually, also during a fast fling-like
 * drag. The first red (unreachable) value is visible, but cannot become the
 * selected value and the drag cannot jump over it to another green value.
 * Swipe back or tap a permitted neighboring value to move in the other
 * direction. This UI guard never replaces the Run planner's final validation.
 */
@Composable
internal fun EndClockWheel(
    label: String,
    value: Int,
    limit: Int,
    tag: String,
    canSelect: (Int) -> Boolean,
    onChange: (Int) -> Unit,
) {
    val currentValue = rememberUpdatedState(value)
    val currentCanSelect = rememberUpdatedState(canSelect)
    val currentOnChange = rememberUpdatedState(onChange)
    val validColor = if (atelierNightActive()) Color(0xFF91F0B7)
        else Color(0xFF166E39)
    val invalidColor = if (atelierNightActive()) Color(0xFFFF909C)
        else Color(0xFFB32631)
    val previous = (value + limit - 1) % limit
    val next = (value + 1) % limit
    val previousIsValid = canSelect(previous)
    val nextIsValid = canSelect(next)
    Column(
        modifier = Modifier.width(94.dp).testTag(tag)
            .pointerInput(limit) {
                var travel = 0f
                var chosenValue = currentValue.value
                val stepPx = 28.dp.toPx()
                detectVerticalDragGestures(
                    onDragStart = {
                        travel = 0f
                        chosenValue = currentValue.value
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        travel += dragAmount
                        // A drag with many pixels cannot skip an invalid value.
                        while (abs(travel) >= stepPx) {
                            val step = if (travel > 0f) -1 else 1
                            val candidate = (chosenValue + step + limit) % limit
                            if (!currentCanSelect.value(candidate)) {
                                // Hard stop at the FIRST red neighbor.
                                travel = 0f
                                break
                            }
                            chosenValue = candidate
                            currentOnChange.value(candidate)
                            travel -= if (step == -1) stepPx else -stepPx
                        }
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        TextButton(
            modifier = Modifier.testTag(tag + "_previous"),
            onClick = {
                if (previousIsValid) onChange(previous)
            },
        ) {
            Text("%02d".format(Locale.US, previous),
                color = if (previousIsValid) validColor else invalidColor,
                style = MaterialTheme.typography.bodyMedium)
        }
        Text("%02d".format(Locale.US, value),
            color = if (canSelect(value)) validColor else invalidColor,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag(tag + "_value"))
        TextButton(
            modifier = Modifier.testTag(tag + "_next"),
            onClick = {
                if (nextIsValid) onChange(next)
            },
        ) {
            Text("%02d".format(Locale.US, next),
                color = if (nextIsValid) validColor else invalidColor,
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}
