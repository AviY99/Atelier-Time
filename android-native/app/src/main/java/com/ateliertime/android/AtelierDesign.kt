package com.ateliertime.android

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import com.ateliertime.domain.DesignClock
import com.ateliertime.domain.DesignMode
import com.ateliertime.domain.DesignPalette
import com.ateliertime.domain.DesignPreferences
import kotlin.math.roundToInt
import kotlin.math.cos
import kotlin.math.sin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Preferences live separately from the signed Class catalog. */
class DesignSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("atelier_design_v1", Context.MODE_PRIVATE)

    fun load(): DesignPreferences {
        val mode = runCatching {
            DesignMode.valueOf(prefs.getString("mode", DesignMode.DAY.name)!!)
        }.getOrDefault(DesignMode.DAY)
        val time = prefs.getInt("night_start_hour", 19)
            .coerceIn(DesignClock.MIN_NIGHT_HOUR, DesignClock.MAX_NIGHT_HOUR)
        return DesignPreferences(mode, time)
    }

    fun save(choice: DesignPreferences) {
        prefs.edit()
            .putString("mode", choice.mode.name)
            .putInt("night_start_hour", choice.nightStartsAtHour.coerceIn(
                DesignClock.MIN_NIGHT_HOUR, DesignClock.MAX_NIGHT_HOUR,
            ))
            .apply()
    }
}

/**
 * Day = Studio Linen; Night = Night Focus. Rounded, high-readability typography
 * is inspired by Flow. Android system sans avoids new downloadable font files
 * and preserves Hebrew glyph coverage; live time digits remain fixed width.
 */
@Composable
fun AtelierDesignTheme(palette: DesignPalette, content: @Composable () -> Unit) {
    val day = palette == DesignPalette.DAY
    val colors = if (day) lightColorScheme(
        primary = Color(0xFF996047),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEFDCD0),
        onPrimaryContainer = Color(0xFF43291D),
        secondary = Color(0xFF517A6F),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFDCECE4),
        onSecondaryContainer = Color(0xFF243B33),
        tertiary = Color(0xFFA16C32),
        background = Color(0xFFF7F4ED),
        onBackground = Color(0xFF29312D),
        surface = Color(0xFFFFFEF9),
        onSurface = Color(0xFF29312D),
        surfaceVariant = Color(0xFFEAE4DB),
        onSurfaceVariant = Color(0xFF4F5952),
        outline = Color(0xFFB0A79A),
        error = Color(0xFFB23F40),
        errorContainer = Color(0xFFF4D8D6),
        onErrorContainer = Color(0xFF70272B),
    ) else darkColorScheme(
        // Night is blue, not black. All Material3 tonal containers must be
        // specified: elevated cards/dialogs otherwise use nearly-black defaults.
        primary = Color(0xFF4F729C),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF365677),
        onPrimaryContainer = Color.White,
        secondary = Color(0xFF716C99),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF4D5B81),
        onSecondaryContainer = Color.White,
        tertiary = Color(0xFF78688B),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFF526384),
        onTertiaryContainer = Color.White,
        background = Color(0xFF304D70),
        onBackground = Color.White,
        surface = Color(0xFF3B5A7A),
        onSurface = Color.White,
        surfaceVariant = Color(0xFF4A6A8A),
        onSurfaceVariant = Color.White,
        surfaceDim = Color(0xFF304D70),
        surfaceBright = Color(0xFF526F90),
        surfaceContainerLowest = Color(0xFF304D70),
        surfaceContainerLow = Color(0xFF3B5A7A),
        surfaceContainer = Color(0xFF405F80),
        surfaceContainerHigh = Color(0xFF466587),
        surfaceContainerHighest = Color(0xFF4A6A8A),
        surfaceTint = Color(0xFF4F729C),
        inverseSurface = Color(0xFF405F80),
        inverseOnSurface = Color.White,
        outline = Color(0xFFAFC4DC),
        outlineVariant = Color(0xFF7793B0),
        error = Color(0xFFA34F70),
        onError = Color.White,
        errorContainer = Color(0xFF744B68),
        onErrorContainer = Color.White,
        scrim = Color(0xFF213A59),
    )
    val sans = FontFamily.SansSerif
    val text = Typography(
        displayLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold,
            fontSize = 58.sp, lineHeight = 66.sp),
        headlineMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold,
            fontSize = 29.sp, lineHeight = 35.sp),
        headlineSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 23.sp, lineHeight = 29.sp),
        titleLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold,
            fontSize = 21.sp, lineHeight = 27.sp),
        titleMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp, lineHeight = 23.sp),
        titleSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = sans, fontSize = 16.sp, lineHeight = 23.sp),
        bodyMedium = TextStyle(fontFamily = sans, fontSize = 14.sp, lineHeight = 21.sp),
        bodySmall = TextStyle(fontFamily = sans, fontSize = 12.sp, lineHeight = 17.sp),
        labelSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium,
            fontSize = 11.sp, lineHeight = 16.sp),
        labelMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp, lineHeight = 17.sp),
        labelLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp, lineHeight = 20.sp),
    )
    MaterialTheme(
        colorScheme = colors,
        typography = text,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(17.dp),
            large = RoundedCornerShape(22.dp),
        ),
        content = content,
    )
}

/**
 * Compose components do not all inherit onSurface text: Material3 defaults
 * give text/outlined buttons and active labels the primary accent color.
 * Route their Night content through white while keeping Day's defaults.
 */
@Composable
fun atelierNightActive(): Boolean =
    MaterialTheme.colorScheme.background == Color(0xFF304D70)

@Composable
fun atelierFilledColors(): ButtonColors {
    val colors = MaterialTheme.colorScheme
    return if (atelierNightActive()) ButtonDefaults.buttonColors(
        containerColor = colors.primary,
        contentColor = Color.White,
        disabledContainerColor = colors.surfaceVariant,
        disabledContentColor = Color.White.copy(alpha = 0.65f),
    ) else ButtonDefaults.buttonColors()
}

@Composable
fun atelierOutlinedColors(): ButtonColors =
    if (atelierNightActive()) ButtonDefaults.outlinedButtonColors(
        contentColor = Color.White,
        disabledContentColor = Color.White.copy(alpha = 0.65f),
    ) else ButtonDefaults.outlinedButtonColors()

@Composable
fun atelierTextColors(): ButtonColors =
    if (atelierNightActive()) ButtonDefaults.textButtonColors(
        contentColor = Color.White,
        disabledContentColor = Color.White.copy(alpha = 0.65f),
    ) else ButtonDefaults.textButtonColors()

@Composable
fun atelierTonalColors(): ButtonColors {
    val colors = MaterialTheme.colorScheme
    return if (atelierNightActive()) ButtonDefaults.filledTonalButtonColors(
        containerColor = colors.secondaryContainer,
        contentColor = Color.White,
        disabledContentColor = Color.White.copy(alpha = 0.65f),
    ) else ButtonDefaults.filledTonalButtonColors()
}

@Composable
fun atelierChipColors(): SelectableChipColors {
    val colors = MaterialTheme.colorScheme
    return if (atelierNightActive()) FilterChipDefaults.filterChipColors(
        labelColor = Color.White,
        selectedLabelColor = Color.White,
        selectedContainerColor = colors.secondaryContainer,
        iconColor = Color.White,
        selectedLeadingIconColor = Color.White,
        selectedTrailingIconColor = Color.White,
    ) else FilterChipDefaults.filterChipColors()
}

@Composable
fun atelierInputColors(): TextFieldColors =
    if (atelierNightActive()) OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White,
        disabledLabelColor = Color.White.copy(alpha = 0.6f),
    ) else OutlinedTextFieldDefaults.colors()

/** Preserve error semantics in button containers; Night messages remain white. */
@Composable
fun atelierErrorTextColor(): Color =
    if (atelierNightActive()) Color.White else MaterialTheme.colorScheme.error

/** Single solid tahini tone for both the progress bar and phase background. */
fun tahiniFill(palette: DesignPalette): Color = if (palette == DesignPalette.DAY) {
    Color(0xFFC5A576)
} else {
    Color(0xFFD5BD94)
}

/**
 * Illustrations mirror the exact approved Day and Night palette surfaces:
 * warm sun for Day, moon and stars for Night. No animation or extra assets.
 */
@Composable
private fun PaletteSymbol(
    palette: DesignPalette,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.21f
        if (palette == DesignPalette.DAY) {
            val gold = Color(0xFFB86B53)
            drawCircle(color = Color(0xFFE3B87B), radius = radius, center = center)
            repeat(8) { ray ->
                val angle = ray * kotlin.math.PI * 2.0 / 8.0
                val dirX = cos(angle).toFloat()
                val dirY = sin(angle).toFloat()
                drawLine(
                    color = gold,
                    start = Offset(center.x + dirX * radius * 1.35f,
                        center.y + dirY * radius * 1.35f),
                    end = Offset(center.x + dirX * radius * 1.8f,
                        center.y + dirY * radius * 1.8f),
                    strokeWidth = size.minDimension * 0.045f,
                    cap = StrokeCap.Round,
                )
            }
        } else {
            // Cresent cutout uses the palette's actual card surface.
            drawCircle(color = Color(0xFFD5BD94),
                center = Offset(center.x - radius * 0.16f, center.y),
                radius = radius * 1.16f)
            drawCircle(color = Color(0xFF3B5A7A),
                center = Offset(center.x + radius * 0.59f, center.y - radius * 0.38f),
                radius = radius * 1.06f)
            drawCircle(color = Color(0xFFF5F8FF), radius = radius * 0.12f,
                center = Offset(center.x + radius * 1.34f, center.y + radius * 0.47f))
            drawCircle(color = Color(0xFFC5B1FF), radius = radius * 0.09f,
                center = Offset(center.x - radius * 1.38f, center.y - radius * 1.12f))
        }
    }
}

@Composable
private fun PaletteChoiceCard(
    palette: DesignPalette,
    chosen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val day = palette == DesignPalette.DAY
    val surface = if (day) Color(0xFFFFFEF9) else Color(0xFF3B5A7A)
    val text = if (day) Color(0xFF29312D) else Color.White
    val borderColor = if (chosen) {
        if (day) Color(0xFFB86B53) else Color(0xFFD5BD94)
    } else if (day) Color(0xFFDCD4C8) else Color(0xFF607B9C)
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = surface),
        border = BorderStroke(if (chosen) 2.dp else 1.dp, borderColor),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            PaletteSymbol(palette, Modifier.size(72.dp))
            Text(if (day) "Day" else "Night",
                color = text, style = MaterialTheme.typography.titleMedium)
            Text(if (day) "Studio Linen" else "Night Blue",
                color = text.copy(alpha = 0.87f),
                style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf(
                    if (day) Color(0xFFF7F4ED) else Color(0xFF304D70),
                    surface,
                    if (day) Color(0xFFB86B53) else Color(0xFF4F729C),
                    if (day) Color(0xFFC5A576) else Color(0xFFD5BD94),
                ).forEach { shade ->
                    Box(
                        modifier = Modifier.size(15.dp)
                            .background(shade, RoundedCornerShape(5.dp)),
                    )
                }
            }
            Text(if (chosen) "Selected" else "Select",
                color = text,
                style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun DesignSettingsPanel(
    settings: DesignPreferences,
    currentPalette: DesignPalette,
    currentClockMillis: Long,
    onChange: (DesignPreferences) -> Unit,
) {
    // Dragging edits a *draft* time. Commit when the thumb is released:
    // the whole app palette should not abruptly flip under a user's finger.
    var draftNightStart by remember(settings.mode) {
        mutableIntStateOf(settings.nightStartsAtHour)
    }
    val deviceClock = remember(currentClockMillis / 60_000L) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(currentClockMillis))
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Design settings", style = MaterialTheme.typography.headlineSmall)
        Text("Choose a palette or let the time of day switch automatically.",
            style = MaterialTheme.typography.bodyMedium)

        // Explicit visual selections — no slider or clock control in the
        // Day and Night choices.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PaletteChoiceCard(
                palette = DesignPalette.DAY,
                chosen = settings.mode == DesignMode.DAY,
                onClick = { onChange(settings.copy(mode = DesignMode.DAY)) },
                modifier = Modifier.weight(1f),
            )
            PaletteChoiceCard(
                palette = DesignPalette.NIGHT,
                chosen = settings.mode == DesignMode.NIGHT,
                onClick = { onChange(settings.copy(mode = DesignMode.NIGHT)) },
                modifier = Modifier.weight(1f),
            )
        }
        FilterChip(
            modifier = Modifier.fillMaxWidth(),
            colors = atelierChipColors(),
            selected = settings.mode == DesignMode.AUTO,
            onClick = { onChange(settings.copy(mode = DesignMode.AUTO)) },
            label = { Text("Auto · Switch Day / Night by time") },
        )
        if (settings.mode == DesignMode.AUTO) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Night begins at %02d:00".format(draftNightStart),
                        style = MaterialTheme.typography.titleLarge)
                    Text("Choose the Night start hour. Day begins at 06:00. The choice is applied when you release the slider.",
                        style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        PaletteSymbol(DesignPalette.DAY, Modifier.size(35.dp))
                        Slider(
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = if (currentPalette == DesignPalette.DAY)
                                    Color(0xFFC5A576) else Color(0xFFD8CFBF),
                                inactiveTrackColor = Color(0xFF4F729C),
                                activeTickColor = Color(0xFF29312D),
                                inactiveTickColor = Color.White,
                            ),
                            value = draftNightStart.toFloat(),
                            onValueChange = {
                                draftNightStart = it.roundToInt().coerceIn(
                                    DesignClock.MIN_NIGHT_HOUR,
                                    DesignClock.MAX_NIGHT_HOUR,
                                )
                            },
                            onValueChangeFinished = {
                                if (draftNightStart != settings.nightStartsAtHour) {
                                    onChange(settings.copy(
                                        nightStartsAtHour = draftNightStart,
                                    ))
                                }
                            },
                            valueRange = DesignClock.MIN_NIGHT_HOUR.toFloat()..
                                DesignClock.MAX_NIGHT_HOUR.toFloat(),
                            steps = DesignClock.MAX_NIGHT_HOUR -
                                DesignClock.MIN_NIGHT_HOUR - 1,
                        )
                        PaletteSymbol(DesignPalette.NIGHT, Modifier.size(35.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Day · 12:00", style = MaterialTheme.typography.bodySmall)
                        Text("Night · 23:00", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Currently showing at $deviceClock: " +
                        (if (currentPalette == DesignPalette.DAY) "Day" else "Night"),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Text(
            "Session background fills; Break background drains. The tahini " +
                "progress bars show elapsed time. Timer digits remain still.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun AtelierAboutPanel() {
    val context = androidx.compose.ui.platform.LocalContext.current
    // Display actual installed version number, excluding its dev/suffix label.
    val version = androidx.compose.runtime.remember(context) {
        val installedVersion = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0)
                .versionName.orEmpty()
        }.getOrDefault("")
        Regex("""\d+(?:\.\d+)*""").find(installedVersion)?.value ?: "—"
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("About", modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Single English LTR identity line, even when the device is
                // configured for Hebrew RTL. The name shrinks if necessary;
                // version and copyright never disappear off-screen.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        // Identical title size for ALL three header values.
                        // Use one shared responsive size, not individual autosizing.
                        val identitySize = if (maxWidth < 300.dp) 13.sp else 15.sp
                        val identityStyle = MaterialTheme.typography.titleMedium.copy(
                            fontSize = identitySize,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Text(
                                "Atlier Time",
                                modifier = Modifier.weight(1f),
                                style = identityStyle,
                                maxLines = 1,
                                softWrap = false,
                            )
                            Text("Version $version",
                                style = identityStyle,
                                maxLines = 1, softWrap = false)
                            Text("© 2026",
                                style = identityStyle,
                                maxLines = 1, softWrap = false)
                        }
                    }
                }
                Text(
                    "Developed by Avi Yosfan with OpenAI",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 10.sp,
                        maxFontSize = 14.sp,
                        stepSize = 0.5.sp,
                    ),
                    maxLines = 1,
                    softWrap = false,
                )
                HorizontalDivider()
                Text(
                    "Atlier Time is a live studio timer that reflects the progress " +
                        "of an ongoing class—its work sessions, breaks and remaining " +
                        "time—and makes day-to-day class management easier. " +
                        "Timed sound cues help keep the class on track.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Justify,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
