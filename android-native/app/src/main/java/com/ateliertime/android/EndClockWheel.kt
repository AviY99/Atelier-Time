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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs

/**
 * User-scrollable discrete hour/minute wheel; no keyboard or text entry.
 * Swipe up/down or tap the neighboring numbers. Wrapping hours/minutes is
 * intentional. The parent turns the combined finish green/red and prevents
 * an impossible choice from starting the Class.
 */
@Composable
internal fun EndClockWheel(
    label: String,
    value: Int,
    limit: Int,
    tag: String,
    onChange: (Int) -> Unit,
) {
    val currentValue = rememberUpdatedState(value)
    val currentOnChange = rememberUpdatedState(onChange)
    Column(
        modifier = Modifier.width(94.dp).testTag(tag)
            .pointerInput(limit) {
                var travel = 0f
                val stepPx = 28.dp.toPx()
                detectVerticalDragGestures(
                    onDragStart = { travel = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        travel += dragAmount
                        val steps = (abs(travel) / stepPx).toInt()
                        if (steps > 0) {
                            val delta = if (travel > 0f) -steps else steps
                            val next = ((currentValue.value + delta) % limit + limit) % limit
                            currentOnChange.value(next)
                            travel -= if (travel > 0f) steps * stepPx else -steps * stepPx
                        }
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        TextButton(
            modifier = Modifier.testTag(tag + "_previous"),
            onClick = { onChange((value + limit - 1) % limit) },
        ) {
            Text("%02d".format(Locale.US, (value + limit - 1) % limit),
                style = MaterialTheme.typography.bodyMedium)
        }
        Text("%02d".format(Locale.US, value),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag(tag + "_value"))
        TextButton(
            modifier = Modifier.testTag(tag + "_next"),
            onClick = { onChange((value + 1) % limit) },
        ) {
            Text("%02d".format(Locale.US, (value + 1) % limit),
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}
