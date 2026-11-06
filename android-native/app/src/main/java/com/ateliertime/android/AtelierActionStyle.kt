package com.ateliertime.android

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * D-152 unified Atelier Time action visual language:
 * rounded without pill/square extremes; a common 1dp outline;
 * 40dp visual button height and preserved 48dp touch areas in compact rows.
 *
 * Width remains proportional to the parent row/content, so localized labels
 * aren't truncated by an arbitrary fixed device-wide width. Uniform padding
 * and corner geometry apply across Editor, Saved Classes, Sounds and dialogs.
 */
internal object AtelierActionStyle {
    val shape = RoundedCornerShape(12.dp)
    val visibleHeight = 40.dp
    val touchHeight = 48.dp
    val minimumWidth = 56.dp
    val stroke = 1.dp
    val buttonPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
    val compactTextPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
}

@Composable
internal fun AtelierButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = atelierFilledColors(),
    contentPadding: PaddingValues = AtelierActionStyle.buttonPadding,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.widthIn(min = AtelierActionStyle.minimumWidth)
            .heightIn(min = AtelierActionStyle.visibleHeight),
        enabled = enabled,
        shape = AtelierActionStyle.shape,
        colors = colors,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
internal fun AtelierOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = atelierOutlinedColors(),
    contentPadding: PaddingValues = AtelierActionStyle.buttonPadding,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.widthIn(min = AtelierActionStyle.minimumWidth)
            .heightIn(min = AtelierActionStyle.visibleHeight),
        enabled = enabled,
        shape = AtelierActionStyle.shape,
        colors = colors,
        border = BorderStroke(AtelierActionStyle.stroke,
            if (enabled) MaterialTheme.colorScheme.outline
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
internal fun AtelierTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = atelierTonalColors(),
    contentPadding: PaddingValues = AtelierActionStyle.buttonPadding,
    content: @Composable RowScope.() -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.widthIn(min = AtelierActionStyle.minimumWidth)
            .heightIn(min = AtelierActionStyle.visibleHeight),
        enabled = enabled,
        shape = AtelierActionStyle.shape,
        colors = colors,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
internal fun AtelierTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = atelierTextColors(),
    contentPadding: PaddingValues = AtelierActionStyle.compactTextPadding,
    content: @Composable RowScope.() -> Unit,
) {
    // Text-only Help / dialog actions retain their quiet hierarchy but share
    // padding, height and corner geometry with framed action buttons.
    TextButton(
        onClick = onClick,
        modifier = modifier.widthIn(min = AtelierActionStyle.minimumWidth)
            .heightIn(min = AtelierActionStyle.visibleHeight),
        enabled = enabled,
        shape = AtelierActionStyle.shape,
        colors = colors,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
internal fun AtelierFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SelectableChipColors = atelierChipColors(),
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier.widthIn(min = AtelierActionStyle.minimumWidth)
            .heightIn(min = AtelierActionStyle.visibleHeight),
        enabled = enabled,
        shape = AtelierActionStyle.shape,
        colors = colors,
        border = BorderStroke(AtelierActionStyle.stroke,
            if (enabled) MaterialTheme.colorScheme.outline
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    )
}
