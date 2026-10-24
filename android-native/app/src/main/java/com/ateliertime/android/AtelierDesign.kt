package com.ateliertime.android

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        primary = Color(0xFF83BCFF),
        onPrimary = Color(0xFF082844),
        primaryContainer = Color(0xFF285079),
        onPrimaryContainer = Color(0xFFE4F0FF),
        secondary = Color(0xFFC5B1FF),
        onSecondary = Color(0xFF2A1D4F),
        secondaryContainer = Color(0xFF42385E),
        onSecondaryContainer = Color(0xFFF0EBFF),
        tertiary = Color(0xFFF8C76D),
        background = Color(0xFF0E1624),
        onBackground = Color(0xFFF5F8FF),
        surface = Color(0xFF1D2B3D),
        onSurface = Color(0xFFF5F8FF),
        surfaceVariant = Color(0xFF26384C),
        onSurfaceVariant = Color(0xFFDBE5F2),
        outline = Color(0xFF8493A6),
        error = Color(0xFFFFA6AB),
        errorContainer = Color(0xFF573039),
        onErrorContainer = Color(0xFFFFE0E4),
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

/** Single solid tahini tone for both the progress bar and phase background. */
fun tahiniFill(palette: DesignPalette): Color = if (palette == DesignPalette.DAY) {
    Color(0xFFC5A576)
} else {
    Color(0xFFD5BD94)
}

@Composable
fun DesignSettingsPanel(
    settings: DesignPreferences,
    currentPalette: DesignPalette,
    onChange: (DesignPreferences) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        Text("Design settings", style = MaterialTheme.typography.headlineSmall)
        Text("Choose the look of Atelier Time. Your saved Classes and sound choices stay unchanged.")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(
                DesignMode.DAY to "Day",
                DesignMode.NIGHT to "Night",
                DesignMode.AUTO to "Auto",
            ).forEach { (mode, title) ->
                FilterChip(
                    selected = settings.mode == mode,
                    onClick = { onChange(settings.copy(mode = mode)) },
                    label = { Text(title) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Text("Automatic Day / Night", style = MaterialTheme.typography.titleMedium)
                Text("Day starts at 06:00. Set when Night begins.",
                    style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Night starts at %02d:00".format(settings.nightStartsAtHour),
                    style = MaterialTheme.typography.titleLarge,
                )
                Slider(
                    value = settings.nightStartsAtHour.toFloat(),
                    onValueChange = {
                        onChange(settings.copy(
                            nightStartsAtHour = it.roundToInt().coerceIn(
                                DesignClock.MIN_NIGHT_HOUR, DesignClock.MAX_NIGHT_HOUR,
                            ),
                        ))
                    },
                    valueRange = DesignClock.MIN_NIGHT_HOUR.toFloat()..
                        DesignClock.MAX_NIGHT_HOUR.toFloat(),
                    steps = DesignClock.MAX_NIGHT_HOUR - DesignClock.MIN_NIGHT_HOUR - 1,
                    enabled = settings.mode == DesignMode.AUTO,
                )
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("12:00", style = MaterialTheme.typography.bodySmall)
                    Text("23:00", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "Currently showing: " + (if (currentPalette == DesignPalette.DAY) "Day" else "Night"),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Text(
            "During a Session the timer background fills; during a Break it empties. " +
                "Both use a solid tahini-colored fill and matching bar. " +
                "Timer digits remain static: no flashing or font animation.",
            style = MaterialTheme.typography.bodyMedium,
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
        Regex("""\\d+(?:\\.\\d+)*""").find(installedVersion)?.value ?: "—"
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("About", style = MaterialTheme.typography.titleLarge)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Clear descending type hierarchy: version > name/credit >
                // copyright and description.
                Text("Version $version", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Atlier Time · Developed by Avi Yosfan with Open AI",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 10.sp,
                        maxFontSize = 14.sp,
                        stepSize = 0.5.sp,
                    ),
                    maxLines = 1,
                    softWrap = false,
                )
                Text("© 2026", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()
                Text(
                    "Atlier Time is a live studio timer that reflects the progress " +
                        "of an ongoing class—its work sessions, breaks and remaining " +
                        "time—and makes day-to-day class management easier. " +
                        "Timed sound cues help keep the class on track.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
