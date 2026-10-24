package com.ateliertime.android

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ateliertime.domain.*

/**
 * A linear timeline is preferable to a pie chart here: the ordered sequence and
 * exact break placement matter as much as the overall time split.
 * Widths map to scheduled seconds, not to the number of phases.
 */
@Composable
fun TimelinePhaseChart(phases: List<SchedulePhase>) {
    if (phases.isEmpty()) return
    val total = phases.last().endSeconds
    if (total <= 0) return
    val poseTotal = phases.filter { it.kind == PhaseKind.POSE }
        .sumOf { it.durationSeconds }
    val normalTotal = phases.filter { it.kind == PhaseKind.SHORT_BREAK }
        .sumOf { it.durationSeconds }
    val specialTotal = phases.filter { it.kind == PhaseKind.SPECIAL_BREAK }
        .sumOf { it.durationSeconds }
    val summaryTotal = phases.filter { it.kind == PhaseKind.SUMMARY }
        .sumOf { it.durationSeconds }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Session timeline at a glance",
                style = MaterialTheme.typography.titleMedium)
            Text("Left to right: the complete session, proportional to actual duration.",
                style = MaterialTheme.typography.bodySmall)
            Canvas(modifier = Modifier.fillMaxWidth().height(44.dp)) {
                val full = size.width
                phases.forEach { phase ->
                    val x = full * phase.startSeconds.toFloat() / total
                    val right = full * phase.endSeconds.toFloat() / total
                    val width = (right - x).coerceAtLeast(0f)
                    drawRect(
                        color = phaseColor(phase.kind),
                        topLeft = Offset(x, 0f),
                        size = Size(width, size.height),
                    )
                    if (width > 3f && phase.startSeconds != 0) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.75f),
                            start = Offset(x, 0f), end = Offset(x, size.height),
                            strokeWidth = 1f,
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Start  00:00", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f))
                Text("End  " + chartTime(total),
                    style = MaterialTheme.typography.labelSmall)
            }
            Text("Time by activity", style = MaterialTheme.typography.titleSmall)
            ChartLegend("Poses", poseTotal, PhaseKind.POSE)
            ChartLegend("Regular breaks", normalTotal, PhaseKind.SHORT_BREAK)
            ChartLegend("Special breaks", specialTotal, PhaseKind.SPECIAL_BREAK)
            ChartLegend("End summary", summaryTotal, PhaseKind.SUMMARY)
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
