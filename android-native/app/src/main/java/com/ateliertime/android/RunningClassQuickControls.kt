package com.ateliertime.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * D-147 field UX: restore the full, already familiar control labels in
 * TWO tightly packed rows. The switch and Max both retain >=48dp touch size.
 * Larger accessibility fonts may expand row height rather than clip labels.
 * This composable is presentation-only: volume/awake behavior is owned by
 * SessionEditorScreen and is unchanged.
 */
@Composable
internal fun RunningClassQuickControls(
    keepScreenAwake: Boolean,
    onKeepScreenAwakeChange: (Boolean) -> Unit,
    mediaPercent: Int,
    alarmPercent: Int,
    useAlarmVolume: Boolean,
    onMaxBoth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
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
                Text(
                    "Keep screen awake during timers",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "Uses more battery",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
            Switch(
                checked = keepScreenAwake,
                onCheckedChange = onKeepScreenAwakeChange,
                modifier = Modifier.testTag("keep_screen_awake")
                    .semantics {
                        contentDescription =
                            "Keep screen awake during timers; uses more battery"
                    },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .testTag("running_class_volume_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Text(
                    "Class sound volume",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                )
                Text(
                    "Media ${mediaPercent}% · Alarm ${alarmPercent}%" +
                        if (useAlarmVolume) " · using Alarm" else " · using Media",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                )
            }
            OutlinedButton(
                colors = atelierOutlinedColors(),
                onClick = onMaxBoth,
                modifier = Modifier.testTag("class_volume_max_both")
                    .heightIn(min = 48.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            ) {
                Text("Max both", style = MaterialTheme.typography.labelSmall,
                    maxLines = 1)
            }
        }
    }
}
