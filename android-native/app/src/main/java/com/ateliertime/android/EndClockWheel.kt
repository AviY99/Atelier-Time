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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
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
 * direction. A blocked gesture reports the rejected candidate to the
 * parent so it can show the exact reason immediately. This UI guard never
 * replaces the Run planner's final validation.
 */
@Composable
internal fun EndClockWheel(
    label: String,
    value: Int,
    limit: Int,
    tag: String,
    canSelect: (Int) -> Boolean,
    onBlocked: (Int) -> Unit,
    onChange: (Int) -> Unit,
    readOnly: Boolean = false,
    wheelWidth: Dp = 94.dp,
    mainNumberScale: Float = 1f,
) {
    val currentValue = rememberUpdatedState(value)
    val currentCanSelect = rememberUpdatedState(canSelect)
    val currentOnBlocked = rememberUpdatedState(onBlocked)
    val currentOnChange = rememberUpdatedState(onChange)
    val validColor = if (atelierNightActive()) Color(0xFF91F0B7)
        else Color(0xFF166E39)
    val invalidColor = if (atelierNightActive()) Color(0xFFFF909C)
        else Color(0xFFB32631)
    val previous = (value + limit - 1) % limit
    val next = (value + 1) % limit
    val previousIsValid = !readOnly && canSelect(previous)
    val nextIsValid = !readOnly && canSelect(next)
    val neighborColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier.width(wheelWidth).testTag(tag)
            .then(if (readOnly) Modifier else Modifier.pointerInput(limit) {
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
                                // Stop at the green edge and explain which red
                                // finish was refused; do not select it.
                                currentOnBlocked.value(candidate)
                                travel = 0f
                                break
                            }
                            chosenValue = candidate
                            currentOnChange.value(candidate)
                            travel -= if (step == -1) stepPx else -stepPx
                        }
                    },
                )
            }),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        TextButton(
            modifier = Modifier.testTag(tag + "_previous").semantics {
                contentDescription = "$label, previous value ${"%02d".format(Locale.US, previous)}" +
                    if (readOnly) ", read only" else if (!previousIsValid)
                        ", blocked, tap for explanation" else ""
            },
            enabled = !readOnly,
            onClick = {
                if (previousIsValid) onChange(previous)
                else onBlocked(previous)
            },
        ) {
            Text("%02d".format(Locale.US, previous),
                color = if (readOnly) neighborColor
                    else if (previousIsValid) validColor else invalidColor,
                style = MaterialTheme.typography.bodyMedium)
        }
        Text("%02d".format(Locale.US, value),
            color = if (canSelect(value)) validColor else invalidColor,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = MaterialTheme.typography.headlineMedium.fontSize * mainNumberScale,
                lineHeight = MaterialTheme.typography.headlineMedium.lineHeight * mainNumberScale,
            ),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.testTag(tag + "_value").semantics {
                contentDescription = "$label, current value ${"%02d".format(Locale.US, value)}" +
                    if (readOnly) ", read only" else ""
            })
        TextButton(
            modifier = Modifier.testTag(tag + "_next").semantics {
                contentDescription = "$label, next value ${"%02d".format(Locale.US, next)}" +
                    if (readOnly) ", read only" else if (!nextIsValid)
                        ", blocked, tap for explanation" else ""
            },
            enabled = !readOnly,
            onClick = {
                if (nextIsValid) onChange(next)
                else onBlocked(next)
            },
        ) {
            Text("%02d".format(Locale.US, next),
                color = if (readOnly) neighborColor
                    else if (nextIsValid) validColor else invalidColor,
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}
