package com.ateliertime.domain

/** All durations and phase boundaries are integer seconds. No Android dependencies. */
const val SECONDS_PER_MINUTE = 60

enum class CycleStart { POSE_FIRST, BREAK_FIRST }
enum class PhaseKind { POSE, SHORT_BREAK, SPECIAL_BREAK, SUMMARY }
enum class PoseSource { OPENING, REGULAR }
enum class IssueSeverity { ERROR, WARNING }
enum class FinalPoseMode { REMAINDER, EVEN_SPLIT, CUSTOM_SPLIT }

/**
 * breakAfterSeconds: null is the pre-D071 legacy model; it retains the old
 * approved timeline for existing saved presets. New editor revisions always
 * provide an explicit 0+ duration (last opening group must be positive).
 */
data class OpeningPoseGroup(
    val id: String,
    val count: Int,
    val poseSeconds: Int,
    val breakAfterSeconds: Int? = null,
)
data class SpecialBreakPlacement(
    val automatic: Boolean = true,
    val breakOrdinals: List<Int> = emptyList(),
) {
    companion object {
        fun manual(ordinals: List<Int>) = SpecialBreakPlacement(false, ordinals.toList())
    }
}
data class SessionRules(
    val id: String,
    val name: String,
    val totalSeconds: Int,
    val summarySeconds: Int = 0,
    val openingGroups: List<OpeningPoseGroup> = emptyList(),
    val poseSeconds: Int,
    val shortBreakSeconds: Int = 5 * SECONDS_PER_MINUTE,
    val specialBreakSeconds: Int = 15 * SECONDS_PER_MINUTE,
    val specialBreakCount: Int = 0,
    val cycleStart: CycleStart = CycleStart.POSE_FIRST,
    val placement: SpecialBreakPlacement = SpecialBreakPlacement(),
) {
    val workSeconds: Int get() = totalSeconds - summarySeconds
}
data class ScheduleIssue(
    val severity: IssueSeverity,
    val code: String,
    val message: String,
    val offsetSeconds: Int? = null,
)
data class SchedulePhase(
    val id: String,
    val kind: PhaseKind,
    val startSeconds: Int,
    val endSeconds: Int,
    val poseNumber: Int? = null,
    val poseSource: PoseSource? = null,
    val openingGroupId: String? = null,
    val breakOrdinal: Int? = null,
) {
    val durationSeconds: Int get() = endSeconds - startSeconds
    fun shifted(start: Int, end: Int) = copy(startSeconds = start, endSeconds = end)
}
data class FinalPoseChoice(val mode: FinalPoseMode, val firstPoseSeconds: Int? = null) {
    companion object {
        fun remainder() = FinalPoseChoice(FinalPoseMode.REMAINDER)
        fun even() = FinalPoseChoice(FinalPoseMode.EVEN_SPLIT)
        fun custom(firstSeconds: Int) = FinalPoseChoice(FinalPoseMode.CUSTOM_SPLIT, firstSeconds)
    }
}
class ScheduleCandidate internal constructor(
    val rules: SessionRules,
    phases: List<SchedulePhase>,
    issues: List<ScheduleIssue>,
    val finalPoseIsShort: Boolean,
    val canSplitFinalTwo: Boolean,
    val selectedChoice: FinalPoseChoice?,
) {
    val phases: List<SchedulePhase> = phases.toList()
    val issues: List<ScheduleIssue> = issues.toList()
    val hasBlockingIssues get() = issues.any { it.severity == IssueSeverity.ERROR }
    val needsFinalPoseChoice get() = finalPoseIsShort && selectedChoice == null
    val needsSafetyAcknowledgement get() = issues.any { it.severity == IssueSeverity.WARNING }
    val canApprove get() = !hasBlockingIssues && !needsFinalPoseChoice
}
class ApprovedSession internal constructor(candidate: ScheduleCandidate) {
    val rules: SessionRules = candidate.rules
    val phases = candidate.phases.toList()
    val selectedChoice = candidate.selectedChoice
    val acknowledgedWarnings =
        candidate.issues.filter { it.severity == IssueSeverity.WARNING }.toList()
}
