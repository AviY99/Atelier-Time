package com.ateliertime.android

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
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
    runtimeElapsedSeconds: Int? = null,
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
                    "Left: actual history · Right: updated future (proportional time)."
                else "Left to right: the complete class, proportional to duration.",
                style = MaterialTheme.typography.bodySmall)
            Canvas(modifier = Modifier.fillMaxWidth().height(44.dp)) {
                val full = size.width
                var cursorSeconds = 0
                displaySegments.forEach { phase ->
                    val x = full * cursorSeconds.toFloat() / total
                    cursorSeconds += phase.durationSeconds
                    val right = full * cursorSeconds.toFloat() / total
                    val width = (right - x).coerceAtLeast(0f)
                    drawRect(
                        color = phase.kind?.let(::phaseColor) ?: Color(0xFF7C7C7C),
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
                // One layered bar: chronological segments below, progress
                // shading over ACTUAL history, updated future remains ahead.
                runtimeTimeline?.let { actual ->
                    val completedWidth = full * actual.elapsedSeconds.coerceIn(0, total)
                        .toFloat() / total
                    drawRect(Color(0xFFD1A34C).copy(alpha = 0.32f),
                        size = Size(completedWidth, size.height))
                }
                (runtimeTimeline?.elapsedSeconds ?: runtimeElapsedSeconds)?.let { elapsed ->
                    val x = full * elapsed.coerceIn(0, total).toFloat() / total
                    drawLine(Color(0xFF182631), Offset(x, 0f),
                        Offset(x, size.height), strokeWidth = 4f)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Start  00:00", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f))
                Text("End  " + chartTime(total),
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
