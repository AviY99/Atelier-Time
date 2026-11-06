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
    onSoundsOff: () -> Unit,
    containerTag: String = "running_class_quick_controls",
    modifier: Modifier = Modifier,
) {
    val palette = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 8.dp)
            .testTag(containerTag),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // One plain, compact action row in Class Run AND Free Timer.
        // Neither Volume max nor Sounds off changes the saved Sound switch
        // or the selected Media/Alarm cue route.
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .testTag("running_screen_awake_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Keep screen awake",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            OutlinedButton(
                onClick = { onKeepScreenAwakeChange(!keepScreenAwake) },
                modifier = Modifier.testTag("keep_screen_awake")
                    .heightIn(min = 48.dp)
                    .semantics {
                        contentDescription =
                            "Keep screen awake during timers, " +
                            (if (keepScreenAwake) "ON" else "OFF") +
                            "; uses more battery"
                    },
                contentPadding = PaddingValues(horizontal = 7.dp, vertical = 0.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (keepScreenAwake) palette.primaryContainer
                        else palette.surface,
                    contentColor = if (keepScreenAwake) palette.onPrimaryContainer
                        else palette.onSurfaceVariant,
                ),
            ) {
                Text(if (keepScreenAwake) "ON" else "OFF",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
                onClick = onMaxBoth,
                modifier = Modifier.testTag("class_volume_max_both")
                    .heightIn(min = 48.dp),
                contentPadding = PaddingValues(horizontal = 7.dp, vertical = 0.dp),
                colors = atelierOutlinedColors(),
            ) {
                Text("Volume\\nmax".replace("\\n", "\n"),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2)
            }
            OutlinedButton(
                onClick = onSoundsOff,
                modifier = Modifier.testTag("class_volume_sounds_off")
                    .heightIn(min = 48.dp)
                    .semantics {
                        contentDescription =
                            "Sounds off, set both Media and Alarm volumes to zero"
                    },
                contentPadding = PaddingValues(horizontal = 7.dp, vertical = 0.dp),
                colors = atelierOutlinedColors(),
            ) {
                Text("Sounds\\noff".replace("\\n", "\n"),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().testTag("running_volume_card"),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // No redundant "Volumes" heading. Labels sit immediately
            // above narrower Sound-style bars, with no decorative card.
            Row(
                modifier = Modifier.fillMaxWidth().height(0.dp)
                    .testTag("running_class_volume_row"),
            ) {}
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
            modifier = Modifier.fillMaxWidth().heightIn(min = 22.dp)
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 9.dp)
                .heightIn(min = 32.dp).offset(y = (-4).dp)
                .testTag(sliderTag)
                .semantics {
                    contentDescription = "$name slider, $current percent"
                },
        )
    }
}
