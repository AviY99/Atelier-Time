package com.ateliertime.android

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * D-150: one Sound-selected channel only. Both Class Run and Free Timer share
 * exactly these plain, tight controls. Select Media vs Alarm ONLY in Sounds.
 *
 * Volume max / Sounds off affect the shown Android stream's hardware LEVEL,
 * never the stored Sound ON/OFF preference or the Media/Alarm route.
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
    // Selecting another channel in Sounds creates a fresh draft: never push
    // an unfinished drag from Media onto the Alarm Android volume stream.
    var dragging by remember(useAlarmVolume) { mutableStateOf<Float?>(null) }
    val percent = (dragging ?: selectedVolumePercent.toFloat())
        .roundToInt().coerceIn(0, 100)
    val streamName = if (useAlarmVolume) "Alarm Volume" else "Media Volume"
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp)
            .testTag(containerTag),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .testTag("running_screen_awake_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Keep screen awake",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            OutlinedButton(
                onClick = { onKeepScreenAwakeChange(!keepScreenAwake) },
                modifier = Modifier.heightIn(min = 48.dp)
                    .testTag("keep_screen_awake").semantics {
                        contentDescription = "Keep screen awake during timers, " +
                            if (keepScreenAwake) "ON" else "OFF"
                    },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (keepScreenAwake) colors.primaryContainer
                        else colors.surface,
                    contentColor = if (keepScreenAwake) colors.onPrimaryContainer
                        else colors.onSurfaceVariant,
                ),
            ) { Text(if (keepScreenAwake) "ON" else "OFF",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold) }
            OutlinedButton(
                onClick = onVolumeMax,
                modifier = Modifier.heightIn(min = 48.dp)
                    .testTag("class_volume_max"),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                colors = atelierOutlinedColors(),
            ) {
                Text("Volume\nmax", textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall, maxLines = 2)
            }
            OutlinedButton(
                onClick = onSoundsOff,
                modifier = Modifier.heightIn(min = 48.dp)
                    .testTag("class_volume_sounds_off").semantics {
                        contentDescription = "Sounds off: set selected $streamName to zero"
                    },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                colors = atelierOutlinedColors(),
            ) {
                Text("Sounds\noff", textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall, maxLines = 2)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth()
                .testTag("running_volume_card"),
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
            // The only slider on this screen. The channel is chosen ONLY
            // in Sounds; unlike earlier versions this label is not clickable.
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
