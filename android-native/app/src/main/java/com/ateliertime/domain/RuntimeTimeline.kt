package com.ateliertime.domain

/** A retrospective fragment is never expanded again after Next or Restart. */
data class RuntimeTimelineSegment(
    val id: String,
    val kind: PhaseKind?, // null denotes time spent paused, not a scheduled Break.
    val durationSeconds: Int,
    val observed: Boolean,
)

/** Chronological work already done followed by the CURRENT revised future. */
data class RuntimeTimeline(
    val segments: List<RuntimeTimelineSegment>,
    val elapsedSeconds: Int,
) {
    val totalSeconds: Int get() = segments.sumOf { it.durationSeconds }
}
