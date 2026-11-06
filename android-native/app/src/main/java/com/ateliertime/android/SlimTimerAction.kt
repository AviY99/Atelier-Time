package com.ateliertime.android

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * D-151: common visual weight for Free Timer quick seconds and Run controls.
 * Visual outline height 34dp, like the small Awake ON/OFF; interactive area
 * remains 48dp for accessibility and touch. Does not change timer actions.
 */
@Composable
internal fun SlimTimerAction(
    label: String,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    filled: Boolean = false,
    danger: Boolean = false,
) {
    val palette = MaterialTheme.colorScheme
    val shape = AtelierActionStyle.shape
    val fill = when {
        danger -> palette.errorContainer
        filled -> palette.primary
        selected -> palette.secondaryContainer
        else -> palette.surface
    }
    val textColor = when {
        !enabled -> palette.onSurface.copy(alpha = 0.40f)
        danger -> palette.onErrorContainer
        filled -> palette.onPrimary
        selected -> palette.onSecondaryContainer
        else -> palette.onSurface
    }
    Box(
        modifier = modifier.height(AtelierActionStyle.touchHeight)
            .testTag(tag)
            .clip(shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics {
                this.selected = selected
                contentDescription = label.replace("\n", " ")
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(AtelierActionStyle.visibleHeight)
                .background(fill, shape)
                .border(AtelierActionStyle.stroke, if (enabled) palette.outline else
                    palette.outlineVariant.copy(alpha = 0.55f), shape)
                .testTag(tag + "_visual"),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label, color = textColor, textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                lineHeight = 12.sp,
                maxLines = 2,
            )
        }
    }
}
