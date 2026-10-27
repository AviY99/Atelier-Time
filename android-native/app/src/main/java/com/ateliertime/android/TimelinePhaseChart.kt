package com.ateliertime.android

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ateliertime.domain.*

/**
 * A linear timeline is preferable to a pie chart here: the ordered sequence and
 * exact break placement matter as much as the overall time split.
 * Widths map to scheduled seconds, not to the number of phases.
 */
@Composable
fun TimelinePhaseChart(
    phases: List<SchedulePhase>,
    runtimeTimeline: RuntimeTimeline? = null,
    classRemainingSeconds: Int? = null,
    classElapsedSeconds: Int? = null,
    currentPhaseId: String? = null,
) {
    if (phases.isEmpty()) return
    val displaySegments = runtimeTimeline?.segments
        ?: phases.map { RuntimeTimelineSegment(it.id, it.kind,
            it.durationSeconds, observed = false) }
    val total = displaySegments.sumOf { it.durationSeconds }
    if (total <= 0) return
    val poseTotal = displaySegments.filter { it.kind == PhaseKind.POSE }
        .sumOf { it.durationSeconds }
    val normalTotal = displaySegments.filter { it.kind == PhaseKind.SHORT_BREAK }
        .sumOf { it.durationSeconds }
    val specialTotal = displaySegments.filter { it.kind == PhaseKind.SPECIAL_BREAK }
        .sumOf { it.durationSeconds }
    val summaryTotal = displaySegments.filter { it.kind == PhaseKind.SUMMARY }
        .sumOf { it.durationSeconds }
    val pausedTotal = displaySegments.filter { it.kind == null }
        .sumOf { it.durationSeconds }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Class timeline at a glance",
                style = MaterialTheme.typography.titleMedium)
            if (classRemainingSeconds != null) {
                val elapsed = (classElapsedSeconds ?: 0).coerceAtLeast(0)
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Class elapsed  " + chartTime(elapsed),
                        style = MaterialTheme.typography.labelMedium)
                    Text("Class remaining  " + chartTime(classRemainingSeconds),
                        style = MaterialTheme.typography.labelMedium)
                }
            }
            Text(if (runtimeTimeline != null)
                    "Elapsed (thin bar) · Actual past + revised future (colors)."
                else "Left to right: the complete class, proportional to duration.",
                style = MaterialTheme.typography.bodySmall)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // Independent elapsed bar, one third as tall as the schedule bar.
            // Both are adjacent, not mixed into the same colored segments.
            if (runtimeTimeline != null) {
                val physicalElapsed = classElapsedSeconds ?: runtimeTimeline.elapsedSeconds
                val physicalTotal = (classElapsedSeconds ?: runtimeTimeline.elapsedSeconds) +
                    (classRemainingSeconds ?: (total - runtimeTimeline.elapsedSeconds))
                Canvas(modifier = Modifier.fillMaxWidth().height(11.dp)) {
                    drawRect(color = Color(0xFFB9C0C6).copy(alpha = 0.45f))
                    val progress = if (physicalTotal > 0)
                        physicalElapsed.toFloat().div(physicalTotal).coerceIn(0f, 1f)
                    else 0f
                    drawRect(color = Color(0xFFD1A34C),
                        size = Size(size.width * progress, size.height))
                }
            }
            // D-116 schedule 44 dp -> 33 dp (three-quarters of its height).
            Canvas(modifier = Modifier.fillMaxWidth().height(33.dp)) {
                val full = size.width
                var cursorSeconds = 0
                displaySegments.forEach { phase ->
                    val x = full * cursorSeconds.toFloat() / total
                    cursorSeconds += phase.durationSeconds
                    val right = full * cursorSeconds.toFloat() / total
                    val width = (right - x).coerceAtLeast(0f)
                    drawRect(
                        color = (phase.kind?.let(::phaseColor) ?: Color(0xFF7C7C7C))
                            .copy(alpha = if (runtimeTimeline != null && !phase.observed)
                                0.78f else 1f),
                        topLeft = Offset(x, 0f),
                        size = Size(width, size.height),
                    )
                    if (currentPhaseId != null && phase.id == currentPhaseId &&
                        !phase.observed) {
                        drawRect(color = Color.White, topLeft = Offset(x, 0f),
                            size = Size(width, size.height), style = Stroke(width = 3f))
                    }
                    if (width > 3f && cursorSeconds != phase.durationSeconds) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.75f),
                            start = Offset(x, 0f), end = Offset(x, size.height),
                            strokeWidth = 1f,
                        )
                    }
                }
                // Past is its actual measured length; later segments are the
                // CURRENT plan. The thin elapsed bar above is separate.
                runtimeTimeline?.let { actual ->
                    val completedWidth = full * actual.elapsedSeconds.coerceIn(0, total)
                        .toFloat() / total
                    drawLine(Color(0xFF182631), Offset(completedWidth, 0f),
                        Offset(completedWidth, size.height), strokeWidth = 3f)
                }
            }
            }
            // Compact, always single-line color key immediately below the
            // bars. Horizontal scroll is a font-accessibility fallback only;
            // the standard Samsung layout fits all visible swatches at once.
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .testTag("class_timeline_color_legend"),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically) {
                if (runtimeTimeline != null) {
                    TimelineColorKey("Elapsed", Color(0xFFD1A34C))
                }
                TimelineColorKey("Session", phaseColor(PhaseKind.POSE))
                TimelineColorKey("Reg. break", phaseColor(PhaseKind.SHORT_BREAK))
                TimelineColorKey("Special", phaseColor(PhaseKind.SPECIAL_BREAK))
                TimelineColorKey("Summary", phaseColor(PhaseKind.SUMMARY))
                if (pausedTotal > 0) {
                    TimelineColorKey("Pause", Color(0xFF7C7C7C))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Start  00:00", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f))
                // Display the approved PHYSICAL duration, not a sum of many
                // individually truncated observed/forecast fragments.
                Text("End  " + chartTime(
                    if (runtimeTimeline != null &&
                        classElapsedSeconds != null && classRemainingSeconds != null)
                        classElapsedSeconds + classRemainingSeconds else total),
                    style = MaterialTheme.typography.labelSmall)
            }
            if (runtimeTimeline == null) {
                Text("Time by activity", style = MaterialTheme.typography.titleSmall)
                ChartLegend("Sessions", poseTotal, PhaseKind.POSE)
                ChartLegend("Break · regular", normalTotal, PhaseKind.SHORT_BREAK)
                ChartLegend("Break · special", specialTotal, PhaseKind.SPECIAL_BREAK)
                ChartLegend("Class summary", summaryTotal, PhaseKind.SUMMARY)
            } else if (pausedTotal > 0) {
                Text("Paused · " + chartTime(pausedTotal),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun TimelineColorKey(label: String, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(7.dp)) { drawRect(color) }
        Text(label, fontSize = 9.sp, lineHeight = 11.sp,
            maxLines = 1, softWrap = false)
    }
}

@Composable
private fun ChartLegend(label: String, seconds: Int, kind: PhaseKind) {
    if (seconds <= 0) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawRect(color = phaseColor(kind))
        }
        Text(label, modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall)
        Text(chartTime(seconds), style = MaterialTheme.typography.bodySmall)
    }
}

private fun phaseColor(kind: PhaseKind): Color = when (kind) {
    PhaseKind.POSE -> Color(0xFF6750A4)
    PhaseKind.SHORT_BREAK -> Color(0xFF66A5A2)
    PhaseKind.SPECIAL_BREAK -> Color(0xFFCF8C36)
    PhaseKind.SUMMARY -> Color(0xFF9AA3B0)
}

private fun chartTime(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds / 60) % 60
    val s = seconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}
