package com.ateliertime.domain

/** D-144: UI-only numbering, preserving stored phase ids, pose numbers and signatures. */
object PhaseDisplayNames {
    fun title(phase: SchedulePhase?, rules: SessionRules): String = when (phase?.kind) {
        PhaseKind.POSE -> {
            val groups = rules.openingGroups
            val groupIndex = if (phase.poseSource == PoseSource.OPENING)
                groups.indexOfFirst { it.id == phase.openingGroupId } else -1
            if (groupIndex >= 0) {
                val group = groups[groupIndex]
                // The stored opening pose number is the sum of earlier group counts
                // plus the one-based index within this opening group.
                val preceding = groups.take(groupIndex).sumOf { it.count }
                val subIndex = (phase.poseNumber ?: (preceding + 1)) - preceding
                "Group ${groupIndex + 1} · Session ${subIndex.coerceIn(1, group.count)}/${group.count}"
            } else {
                val openingCount = groups.sumOf { it.count }
                val adjusted = (phase.poseNumber ?: 0) - openingCount + groups.size
                "Session " + if (groups.isNotEmpty() && adjusted > 0) adjusted
                    else phase.poseNumber?.toString().orEmpty()
            }
        }
        PhaseKind.SHORT_BREAK -> if (phase.openingGroupId != null) {
            val groupIndex = rules.openingGroups.indexOfFirst { it.id == phase.openingGroupId }
            if (groupIndex >= 0) "Group ${groupIndex + 1} · Break"
            else "Break · opening group"
        } else "Break · regular"
        PhaseKind.SPECIAL_BREAK -> "Break · special"
        PhaseKind.SUMMARY -> "Class summary"
        null -> "Class end"
    }
}
