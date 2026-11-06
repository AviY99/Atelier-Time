package com.ateliertime.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * D-149: unobtrusive controls for BOTH the Class Run and Free Timer screens.
 * No illustration, pill, filled card, emoji, heavy slider or decorative chrome.
 * Sliders share AtelierSlimSlider's visual language with the Sound tab.
 *
 * Tapping the source name selects the global cue channel; dragging a bar
 * changes ONLY the corresponding phone-wide volume after finger release.
 * Max both keeps its original explicit, loud-volume confirmation.
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
    val palette = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 10.dp)
            .testTag("running_class_quick_controls"),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .testTag("running_screen_awake_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Text("Keep Screen Awake",
                    style = MaterialTheme.typography.titleSmall, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text("Uses more battery",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.onSurfaceVariant)
            }
            Text(if (keepScreenAwake) "ON" else "OFF",
                style = MaterialTheme.typography.labelSmall,
                color = if (keepScreenAwake) palette.primary else palette.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold)
            Switch(
                checked = keepScreenAwake,
                onCheckedChange = onKeepScreenAwakeChange,
                modifier = Modifier.testTag("keep_screen_awake")
                    .semantics {
                        contentDescription =
                            "Keep screen awake during timers; uses more battery"
                    },
                colors = SwitchDefaults.colors(checkedTrackColor = palette.primary),
            )
        }
        HorizontalDivider(color = palette.outlineVariant)
        Column(
            modifier = Modifier.fillMaxWidth().testTag("running_volume_card"),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)
                    .testTag("running_class_volume_row"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Volumes",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall)
                OutlinedButton(
                    colors = atelierOutlinedColors(),
                    onClick = onMaxBoth,
                    modifier = Modifier.testTag("class_volume_max_both")
                        .heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 0.dp),
                ) { Text("Max both", style = MaterialTheme.typography.labelSmall) }
            }
            SimpleVolumeRow(
                name = "Media Volume",
                percent = mediaPercent,
                selected = !useAlarmVolume,
                onSelect = { onChooseVolume(false) },
                onVolumeChange = onMediaVolumeChange,
                rowTag = "running_media_volume_row",
                sliderTag = "running_media_volume_slider",
            )
            SimpleVolumeRow(
                name = "Alarm Volume",
                percent = alarmPercent,
                selected = useAlarmVolume,
                onSelect = { onChooseVolume(true) },
                onVolumeChange = onAlarmVolumeChange,
                rowTag = "running_alarm_volume_row",
                sliderTag = "running_alarm_volume_slider",
            )
        }
    }
}

@Composable
private fun SimpleVolumeRow(
    name: String,
    percent: Int,
    selected: Boolean,
    onSelect: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    rowTag: String,
    sliderTag: String,
) {
    val palette = MaterialTheme.colorScheme
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val current = (dragValue ?: percent.toFloat()).roundToInt().coerceIn(0, 100)
    Column(
        modifier = Modifier.fillMaxWidth().testTag(rowTag),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 28.dp)
                .clickable(onClick = onSelect)
                .semantics {
                    contentDescription = "$name, " +
                        if (selected) "Selected sound output" else "Select sound output"
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(name, modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) palette.onSurface else palette.onSurfaceVariant)
            Text("$current%", style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            if (selected) Text("Selected", style = MaterialTheme.typography.labelSmall,
                color = palette.primary, fontWeight = FontWeight.SemiBold)
        }
        AtelierSlimSlider(
            value = current.toFloat(),
            onValueChange = { dragValue = it.coerceIn(0f, 100f) },
            onValueChangeFinished = {
                val chosen = (dragValue ?: percent.toFloat())
                    .roundToInt().coerceIn(0, 100)
                dragValue = null
                onVolumeChange(chosen)
            },
            valueRange = 0f..100f,
            steps = 99,
            modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp)
                .testTag(sliderTag)
                .semantics {
                    contentDescription = "$name slider, $current percent"
                },
        )
    }
}
