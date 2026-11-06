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
 * D-153: Run + Free Timer share the same ON/OFF and 40dp framed Max/Mute
 * buttons, with plain Keep screen awake / Volume labels and 48dp hits.
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
    var dragging by remember(useAlarmVolume) { mutableStateOf<Float?>(null) }
    val percent = (dragging ?: selectedVolumePercent.toFloat())
        .roundToInt().coerceIn(0, 100)
    val streamName = if (useAlarmVolume) "Alarm Volume" else "Media Volume"
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp)
            .testTag(containerTag),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Plain labels like "Keep screen awake"; ONLY Max and Mute are
        // two separate, matching action buttons. 48dp touch targets.
        Row(
            modifier = Modifier.fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .heightIn(min = AtelierActionStyle.touchHeight)
                .testTag("running_screen_awake_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                "Keep screen awake",
                modifier = Modifier.widthIn(max = 72.dp)
                    .testTag("keep_screen_awake_label"),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
            )
            AtelierOnOffButton(
                checked = keepScreenAwake,
                onCheckedChange = onKeepScreenAwakeChange,
                modifier = Modifier.testTag("keep_screen_awake"),
                visualTag = "keep_screen_awake_visual",
                description = "Keep screen awake during timers, " +
                    (if (keepScreenAwake) "ON" else "OFF") + "; uses more battery",
            )
            Text(
                "Volume",
                modifier = Modifier.padding(start = 3.dp, end = 1.dp)
                    .testTag("running_volume_label"),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
            SlimTimerAction(
                label = "Max",
                tag = "class_volume_max",
                modifier = Modifier.width(56.dp),
                onClick = onVolumeMax,
            )
            SlimTimerAction(
                label = "Mute",
                tag = "class_volume_sounds_off",
                modifier = Modifier.width(62.dp),
                onClick = onSoundsOff,
            )
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
