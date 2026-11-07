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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * D-153: Run + Free Timer share the same ON/OFF and 40dp framed Max/Mute
 * buttons, with plain Keep screen awake / Volume labels and 48dp hits.
 *
 * Sounds is the ONLY location to choose Media vs Alarm. Volume max and
 * A successful selected-channel change is also synced with the global
 * Sound ON/OFF preference by the parent Timer screen (D154).
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
    showZeroVolumeWarning: Boolean = false,
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
        // D157: ONE horizontal row shared by Class and Free: two-line
        // output label, 3/4-thickness bar, double-size percentage.
        // Using one real Slider with a custom track removes the misleading
        // stop-indicator dot; both timers receive the same hardware percentage.
        Column(modifier = Modifier.fillMaxWidth().testTag("running_volume_card")) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .heightIn(min = 56.dp)
                .testTag("running_class_volume_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(
                modifier = Modifier.width(52.dp)
                    .testTag("running_volume_stream_label"),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Text(if (useAlarmVolume) "Alarm" else "Media",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1)
                Text("Volume",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1)
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
                steps = 0,
                compactVisual = false,
                volumeVisual = true,
                // The whole slider still has a >=56dp touch target at 0%.
                modifier = Modifier.weight(1f).padding(horizontal = 7.dp)
                    .heightIn(min = 56.dp)
                    .testTag(if (useAlarmVolume) "running_alarm_volume_slider"
                        else "running_media_volume_slider")
                    .semantics {
                        contentDescription = "$streamName slider, $percent percent"
                    },
            )
            Text(
                "$percent%",
                modifier = Modifier.widthIn(min = 58.dp)
                    .testTag("running_volume_percent"),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
        if (showZeroVolumeWarning) {
            TimerVolumeZeroWarning(
                tag = if (containerTag == "free_timer_quick_controls")
                    "free_selected_volume_zero_warning"
                else "class_selected_volume_zero_warning",
            )
        }
        }
    }
}

/**
 * The identical zero-volume message directly BELOW the one-line slider in
 * Class and Free. Kept short to fit one centered phone-width line.
 */
@Composable
internal fun TimerVolumeZeroWarning(
    tag: String,
    modifier: Modifier = Modifier,
) {
    Text(
        "Sound volume is zero. Timer alert may be silent.",
        modifier = modifier.fillMaxWidth().testTag(tag),
        color = atelierErrorTextColor(),
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
