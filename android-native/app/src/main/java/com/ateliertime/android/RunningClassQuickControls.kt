package com.ateliertime.android

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * D-148 / Option A approved: compact framed awake toggle and volume card.
 *
 * The selected, darker row sets the CLASS CUE OUTPUT STREAM, not the volume
 * level. Both independent sliders always change the respective Android
 * device volume ONLY after the user finishes dragging. No silent increases.
 * The existing Max-both action stays at the right of the volume card and
 * still invokes its separate confirmation dialog.
 */
@Composable
internal fun RunningClassQuickControls(
    keepScreenAwake: Boolean,
    onKeepScreenAwakeChange: (Boolean) -> Unit,
    mediaPercent: Int,
    alarmPercent: Int,
    useAlarmVolume: Boolean,
    onChooseVolume: (Boolean) -> Unit,
    onMediaVolumeChange: (Int) -> Unit,
    onAlarmVolumeChange: (Int) -> Unit,
    onMaxBoth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val cardShape = RoundedCornerShape(13.dp)
    Column(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 10.dp)
            .testTag("running_class_quick_controls"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().testTag("running_screen_awake_row"),
            shape = cardShape,
            color = if (keepScreenAwake) scheme.primaryContainer.copy(alpha = 0.7f)
                else scheme.surface,
            border = BorderStroke(1.dp, scheme.outlineVariant),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text("🔒", fontSize = 16.sp, color = scheme.onSurface)
                Text(
                    "Keep Screen Awake",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (keepScreenAwake) "ON" else "OFF",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (keepScreenAwake) scheme.primary else scheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
                Switch(
                    checked = keepScreenAwake,
                    onCheckedChange = onKeepScreenAwakeChange,
                    modifier = Modifier.testTag("keep_screen_awake").semantics {
                        contentDescription =
                            "Keep screen awake during timers; uses more battery"
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = scheme.primary,
                        checkedThumbColor = scheme.onPrimary,
                    ),
                )
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().testTag("running_volume_card"),
            shape = cardShape,
            color = scheme.surface,
            border = BorderStroke(1.dp, scheme.outlineVariant),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .padding(start = 8.dp, end = 2.dp)
                        .testTag("running_class_volume_row"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Volumes",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(
                        onClick = onMaxBoth,
                        modifier = Modifier.testTag("class_volume_max_both")
                            .heightIn(min = 48.dp),
                        colors = atelierOutlinedColors(),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 0.dp),
                    ) {
                        Text(
                            "Max both", maxLines = 1,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                VolumeChoiceRow(
                    name = "Media Volume",
                    icon = "🔊",
                    currentPercent = mediaPercent,
                    selected = !useAlarmVolume,
                    onSelect = { onChooseVolume(false) },
                    onVolumeChange = onMediaVolumeChange,
                    rowTag = "running_media_volume_row",
                    sliderTag = "running_media_volume_slider",
                )
                VolumeChoiceRow(
                    name = "Alarm Volume",
                    icon = "🔔",
                    currentPercent = alarmPercent,
                    selected = useAlarmVolume,
                    onSelect = { onChooseVolume(true) },
                    onVolumeChange = onAlarmVolumeChange,
                    rowTag = "running_alarm_volume_row",
                    sliderTag = "running_alarm_volume_slider",
                )
            }
        }
    }
}

@Composable
private fun VolumeChoiceRow(
    name: String,
    icon: String,
    currentPercent: Int,
    selected: Boolean,
    onSelect: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    rowTag: String,
    sliderTag: String,
) {
    val colors = MaterialTheme.colorScheme
    val foreground = if (selected) colors.onPrimary else colors.onSurface
    val secondaryText = if (selected) colors.onPrimary else colors.onSurfaceVariant
    var dragPercent by remember { mutableStateOf<Float?>(null) }
    val shownPercent = (dragPercent ?: currentPercent.toFloat()).roundToInt()
        .coerceIn(0, 100)
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) colors.primary else Color.Transparent)
            .testTag(rowTag)
            .padding(start = 7.dp, end = 7.dp, top = 2.dp, bottom = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 26.dp)
                .clickable(onClick = onSelect)
                .semantics {
                    contentDescription = "$name, " +
                        if (selected) "Selected class alert stream" else "Select for class alerts"
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(icon, fontSize = 15.sp, color = foreground)
            Text(
                name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "$shownPercent%",
                style = MaterialTheme.typography.labelLarge,
                color = foreground,
                fontWeight = FontWeight.SemiBold,
            )
            if (selected) {
                Text(
                    "Selected",
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.onPrimary.copy(alpha = 0.16f))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryText,
                    maxLines = 1,
                )
            }
        }
        Slider(
            value = shownPercent.toFloat(),
            onValueChange = { dragPercent = it.coerceIn(0f, 100f) },
            onValueChangeFinished = {
                val chosen = (dragPercent ?: currentPercent.toFloat())
                    .roundToInt().coerceIn(0, 100)
                dragPercent = null
                onVolumeChange(chosen)
            },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp)
                .testTag(sliderTag).semantics {
                    contentDescription = "$name slider, $shownPercent percent"
                },
            colors = SliderDefaults.colors(
                thumbColor = if (selected) colors.onPrimary else colors.primary,
                activeTrackColor = if (selected) colors.onPrimary else colors.primary,
                inactiveTrackColor = if (selected)
                    colors.onPrimary.copy(alpha = 0.28f)
                    else colors.outlineVariant,
            ),
        )
    }
}
