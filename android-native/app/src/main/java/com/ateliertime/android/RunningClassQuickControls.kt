package com.ateliertime.android

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * D-151: one slim top row shared by Run + Free Timer. 34dp VISUAL buttons,
 * but 48dp interactive wrappers. The Volume group has the same border
 * weight/visible height as the ON/OFF button; no cartoonish cards.
 *
 * Sounds is the ONLY location to choose Media vs Alarm. Volume max and
 * Sounds off change only the selected Android stream, not Sound ON/OFF.
 */
@Composable
internal fun RunningClassQuickControls(
    keepScreenAwake: Boolean,
    onKeepScreenAwakeChange: (Boolean) -> Unit,
    selectedVolumePercent: Int,
    useAlarmVolume: Boolean,
    onSelectedVolumeChange: (Int) -> Unit,
    onVolumeMax: () -> Unit,
    onSoundsOff: () -> Unit,
    containerTag: String = "running_class_quick_controls",
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val controlShape = RoundedCornerShape(7.dp)
    var dragging by remember(useAlarmVolume) { mutableStateOf<Float?>(null) }
    val percent = (dragging ?: selectedVolumePercent.toFloat())
        .roundToInt().coerceIn(0, 100)
    val streamName = if (useAlarmVolume) "Alarm Volume" else "Media Volume"
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp)
            .testTag(containerTag),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Single horizontal row even at increased fonts: scroll horizontally
        // rather than wrap or squeeze the interactive target below 48dp.
        Row(
            modifier = Modifier.fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .heightIn(min = 48.dp)
                .testTag("running_screen_awake_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Keep screen awake",
                modifier = Modifier.width(83.dp),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
            )
            // ON/OFF label is immediately next to its explanatory text.
            Box(
                modifier = Modifier.width(42.dp).height(48.dp)
                    .testTag("keep_screen_awake")
                    .clip(controlShape)
                    .clickable(
                        role = Role.Button,
                        onClick = { onKeepScreenAwakeChange(!keepScreenAwake) },
                    )
                    .semantics {
                        contentDescription = "Keep screen awake during timers, " +
                            (if (keepScreenAwake) "ON" else "OFF") +
                            "; uses more battery"
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(34.dp)
                        .background(if (keepScreenAwake) colors.primaryContainer
                            else colors.surface, controlShape)
                        .border(1.dp, colors.outline, controlShape)
                        .testTag("keep_screen_awake_visual"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (keepScreenAwake) "ON" else "OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (keepScreenAwake) colors.onPrimaryContainer
                            else colors.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            // One thin frame holds "Volume" and both independent commands.
            // Only these two segments are clickable. Same visible 34dp
            // height and 1dp outline as the ON/OFF button.
            Box(
                modifier = Modifier.width(171.dp).height(48.dp)
                    .testTag("running_volume_actions"),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(34.dp)
                        .border(1.dp, colors.outline, controlShape)
                        .testTag("running_volume_actions_border"),
                )
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Volume",
                        modifier = Modifier.padding(start = 6.dp, end = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                    Box(
                        modifier = Modifier.width(1.dp).height(22.dp)
                            .background(colors.outlineVariant),
                    )
                    Box(
                        modifier = Modifier.weight(0.85f).fillMaxHeight()
                            .testTag("class_volume_max")
                            .clickable(role = Role.Button, onClick = onVolumeMax)
                            .semantics {
                                contentDescription =
                                    "Volume max: maximize selected $streamName"
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Max",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                    }
                    Box(
                        modifier = Modifier.width(1.dp).height(22.dp)
                            .background(colors.outlineVariant),
                    )
                    Box(
                        modifier = Modifier.weight(1.4f).fillMaxHeight()
                            .testTag("class_volume_sounds_off")
                            .clickable(role = Role.Button, onClick = onSoundsOff)
                            .semantics {
                                contentDescription =
                                    "Sounds off: set selected $streamName to zero"
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Sounds off",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            lineHeight = MaterialTheme.typography.labelSmall.lineHeight,
                        )
                    }
                }
            }
        }
        // Exactly ONE Sound-selected volume bar; keep its label close to it.
        Column(
            modifier = Modifier.fillMaxWidth().testTag("running_volume_card"),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 23.dp)
                    .testTag("running_class_volume_row"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(streamName, modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold)
                Text("$percent%", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            AtelierSlimSlider(
                value = percent.toFloat(),
                onValueChange = { dragging = it.coerceIn(0f, 100f) },
                onValueChangeFinished = {
                    val target = (dragging ?: selectedVolumePercent.toFloat())
                        .roundToInt().coerceIn(0, 100)
                    dragging = null
                    onSelectedVolumeChange(target)
                },
                valueRange = 0f..100f,
                steps = 99,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    .heightIn(min = 32.dp)
                    .testTag(if (useAlarmVolume) "running_alarm_volume_slider"
                        else "running_media_volume_slider")
                    .semantics {
                        contentDescription = "$streamName slider, $percent percent"
                    },
            )
        }
    }
}
