package com.ateliertime.domain

import kotlin.math.abs
import kotlin.math.min

private const val REST_THRESHOLD = 25 * SECONDS_PER_MINUTE
private const val QUALIFYING_REST = 5 * SECONDS_PER_MINUTE

/**
 * Deterministic Kotlin port of approved Dart scheduling rules (D-031/036/037/043/044).
 * Keep this layer free of Android dependencies so that KMP extraction stays practical.
 */
class ScheduleEngine {
    fun generate(rules: SessionRules, choice: FinalPoseChoice? = null): ScheduleCandidate {
        val issues = mutableListOf<ScheduleIssue>()
        fun error(code: String, description: String) {
            issues += ScheduleIssue(IssueSeverity.ERROR, code, description)
        }
        fun candidate(
            phases: List<SchedulePhase> = emptyList(),
            shortened: Boolean = false,
            canSplit: Boolean = false,
        ) = ScheduleCandidate(rules, phases, issues, shortened, canSplit, choice)

        if (rules.totalSeconds <= 0) {
            error("nonpositive_session", "Full session duration must be positive.")
        }
        if (rules.summarySeconds < 0 || rules.summarySeconds >= rules.totalSeconds) {
            error("invalid_summary", "Summary must leave a positive work period.")
        }
        if (rules.poseSeconds <= 0) error("nonpositive_pose", "Regular pose duration must be positive.")
        if (rules.shortBreakSeconds <= 0 || rules.specialBreakSeconds <= 0) {
            error("nonpositive_break", "Configured breaks must be positive.")
        }
        if (rules.specialBreakCount < 0) {
            error("negative_break_count", "Special break count cannot be negative.")
        }
        var openingSum = 0L
        rules.openingGroups.forEachIndexed { index, group ->
            if (group.count <= 0 || group.poseSeconds <= 0) {
                error("invalid_opening_group", "Opening groups need positive values.")
            } else {
                openingSum += group.count.toLong() * group.poseSeconds
            }
            val configured = group.breakAfterSeconds
            if (configured != null) {
                if (configured < 0) {
                    error("negative_opening_break", "Opening-group breaks cannot be negative.")
                }
                if (index == rules.openingGroups.lastIndex && configured <= 0) {
                    error("final_opening_break_required",
                        "A positive break is required after the last opening group.")
                }
                if (configured > 0) {
                    // A final explicit rest identical to BREAK_FIRST's initial regular
                    // rest shares that original slot; never count it twice.
                    if (!(index == rules.openingGroups.lastIndex &&
                          rules.cycleStart == CycleStart.BREAK_FIRST &&
                          configured == rules.shortBreakSeconds)) {
                        openingSum += configured.toLong()
                    }
                }
            }
        }
        val durations = listOf(
            rules.totalSeconds, rules.summarySeconds, rules.poseSeconds,
            rules.shortBreakSeconds, rules.specialBreakSeconds,
        ) + rules.openingGroups.flatMap { group ->
            listOfNotNull(group.poseSeconds, group.breakAfterSeconds)
        }
        if (durations.any { it % SECONDS_PER_MINUTE != 0 }) {
            error("minute_precision", "Initial session rules use whole minutes.")
        }
        if (openingSum > rules.workSeconds.toLong()) {
            error("opening_overrun", "Opening groups and their breaks exceed available work time.")
        }
        if (!rules.placement.automatic && rules.specialBreakCount > 0) {
            val ordinals = rules.placement.breakOrdinals
            if (ordinals.size != rules.specialBreakCount ||
                ordinals.any { it <= 0 } ||
                ordinals.distinct().size != ordinals.size
            ) {
                error("invalid_manual_ordinals",
                    "Choose distinct, positive ordinals for every special break.")
            }
        }
        if (issues.any { it.severity == IssueSeverity.ERROR }) return candidate()

        val attempt = when {
            rules.specialBreakCount == 0 -> build(rules, emptyList())
            !rules.placement.automatic -> build(rules, rules.placement.breakOrdinals.sorted())
            else -> chooseAutomatic(rules)
        }
        if (!attempt.valid) {
            error("unbuildable_timeline",
                attempt.invalidReason ?: "Cannot fit requested session phases.")
            return candidate(attempt.phases)
        }
        var phases = attempt.phases
        val regular = phases.indices.filter {
            phases[it].kind == PhaseKind.POSE &&
                phases[it].poseSource == PoseSource.REGULAR
        }
        val lastIndex = regular.lastOrNull() ?: -1
        val shortened = lastIndex >= 0 &&
            phases[lastIndex].durationSeconds < rules.poseSeconds
        val canSplit = shortened && lastIndex >= 2 &&
            phases[lastIndex - 1].kind != PhaseKind.POSE &&
            phases[lastIndex - 1].kind != PhaseKind.SUMMARY &&
            phases[lastIndex - 2].kind == PhaseKind.POSE &&
            phases[lastIndex - 2].poseSource == PoseSource.REGULAR &&
            phases[lastIndex - 2].durationSeconds +
                phases[lastIndex].durationSeconds >= 2 * SECONDS_PER_MINUTE

        if (shortened && choice != null) {
            if (choice.mode != FinalPoseMode.REMAINDER && !canSplit) {
                error("split_unavailable",
                    "No two eligible regular poses with an existing break to split.")
            } else if (choice.mode != FinalPoseMode.REMAINDER) {
                val firstIndex = lastIndex - 2
                val first = phases[firstIndex]
                val between = phases[firstIndex + 1]
                val last = phases[lastIndex]
                val available = first.durationSeconds + last.durationSeconds
                val firstDuration = when (choice.mode) {
                    FinalPoseMode.EVEN_SPLIT ->
                        (available / (2 * SECONDS_PER_MINUTE)) * SECONDS_PER_MINUTE
                    FinalPoseMode.CUSTOM_SPLIT -> choice.firstPoseSeconds ?: 0
                    FinalPoseMode.REMAINDER -> first.durationSeconds
                }
                if (firstDuration < SECONDS_PER_MINUTE ||
                    firstDuration > available - SECONDS_PER_MINUTE ||
                    firstDuration % SECONDS_PER_MINUTE != 0
                ) {
                    error("invalid_custom_split",
                        "Each adjusted pose needs at least one whole minute.")
                } else {
                    val start = first.startSeconds
                    val breakStart = start + firstDuration
                    val breakEnd = breakStart + between.durationSeconds
                    phases = phases.toMutableList().apply {
                        this[firstIndex] = first.shifted(start, breakStart)
                        this[firstIndex + 1] = between.shifted(breakStart, breakEnd)
                        this[lastIndex] = last.shifted(breakEnd, last.endSeconds)
                    }
                }
            }
        } else if (!shortened && choice != null) {
            error("unneeded_final_choice",
                "Final-pose adjustment is only available for a shortened pose.")
        }
        phases = phases.mapIndexed { index, phase ->
            phase.copy(id = "phase_" + (index + 1))
        }
        if (issues.none { it.severity == IssueSeverity.ERROR }) {
            issues.addAll(restWarnings(phases))
        }
        return candidate(phases, shortened, canSplit)
    }

    fun approve(candidate: ScheduleCandidate, acknowledgeWarnings: Boolean = false): ApprovedSession {
        check(candidate.canApprove) { "Cannot approve invalid or unresolved schedule." }
        check(!candidate.needsSafetyAcknowledgement || acknowledgeWarnings) {
            "Rest-warning acknowledgement is required."
        }
        return ApprovedSession(candidate)
    }

    private data class Attempt(
        val phases: List<SchedulePhase>,
        val invalidReason: String? = null,
    ) {
        val valid get() = invalidReason == null
    }
    private data class Scored(val score: Long, val ordinals: List<Int>)

    /** Exact O(count * slots) DP, including negative/zero special-break delta. */
    private fun chooseAutomatic(rules: SessionRules): Attempt {
        val count = rules.specialBreakCount
        val delta = rules.specialBreakSeconds - rules.shortBreakSeconds
        if (count <= 0) return Attempt(emptyList(), "Invalid automatic replacement count.")
        val adjustedWork = rules.workSeconds.toLong() - count.toLong() * delta
        if (adjustedWork <= 0L || adjustedWork > Int.MAX_VALUE) {
            return Attempt(emptyList(), "Special breaks leave no positive work time.")
        }
        val virtualRules = rules.copy(
            totalSeconds = rules.totalSeconds - count * delta,
            specialBreakCount = 0,
            placement = SpecialBreakPlacement(),
        )
        val virtual = build(virtualRules, emptyList())
        if (!virtual.valid) {
            return Attempt(emptyList(),
                "No feasible replacement positions: " + virtual.invalidReason)
        }
        val slots = virtual.phases.filter {
            it.kind == PhaseKind.SHORT_BREAK && it.openingGroupId == null
        }
        val maxOrdinal = slots.size
        if (count > maxOrdinal) {
            return Attempt(emptyList(),
                "Requested more special breaks than available ordinary slots.")
        }
        var previous = arrayOfNulls<Scored>(maxOrdinal + 1)
        previous[0] = Scored(0L, emptyList())
        for (selectedIndex in 1..count) {
            val current = arrayOfNulls<Scored>(maxOrdinal + 1)
            var bestEarlier: Scored? = null
            for (slotIndex in 1..maxOrdinal) {
                val preceding = previous[slotIndex - 1]
                if (preceding != null &&
                    (bestEarlier == null || isBetter(preceding, bestEarlier))
                ) bestEarlier = preceding
                val earlier = bestEarlier ?: continue
                val actualStart = slots[slotIndex - 1].startSeconds.toLong() +
                    (selectedIndex - 1).toLong() * delta
                val twiceCenter = 2L * actualStart + rules.specialBreakSeconds
                val targetError = abs(
                    twiceCenter * (count + 1).toLong() -
                        2L * rules.workSeconds * selectedIndex
                )
                current[slotIndex] = Scored(
                    earlier.score + targetError,
                    earlier.ordinals + slotIndex
                )
            }
            previous = current
        }
        val best = previous.filterNotNull().reduceOrNull { a, b ->
            if (isBetter(a, b)) a else b
        } ?: return Attempt(emptyList(), "No feasible replacement positions.")
        return build(rules, best.ordinals)
    }

    private fun isBetter(a: Scored, b: Scored): Boolean {
        if (a.score != b.score) return a.score < b.score
        for (index in a.ordinals.indices) {
            if (a.ordinals[index] != b.ordinals[index]) {
                return a.ordinals[index] < b.ordinals[index]
            }
        }
        return false
    }

    private fun build(rules: SessionRules, replacements: List<Int>): Attempt {
        val phases = mutableListOf<SchedulePhase>()
        var cursor = 0
        var poseNumber = 0
        var breakOrdinal = 0
        fun addPose(duration: Int, source: PoseSource, groupId: String? = null) {
            poseNumber++
            phases += SchedulePhase(
                id = "phase_" + (phases.size + 1),
                kind = PhaseKind.POSE,
                startSeconds = cursor,
                endSeconds = cursor + duration,
                poseNumber = poseNumber,
                poseSource = source,
                openingGroupId = groupId,
            )
            cursor += duration
        }
        val end = rules.workSeconds
        fun addOpeningGroupBreak(group: OpeningPoseGroup, duration: Int): Attempt? {
            // Unlike a repeating break, a group-boundary rest is NOT a numbered
            // regular-break replacement slot. It is a rest/sound phase nonetheless.
            if (cursor.toLong() + duration + SECONDS_PER_MINUTE > end) {
                return Attempt(phases,
                    "The last opening-group break must leave a regular pose afterward.")
            }
            phases += SchedulePhase(
                id = "phase_" + (phases.size + 1),
                kind = PhaseKind.SHORT_BREAK,
                startSeconds = cursor,
                endSeconds = cursor + duration,
                openingGroupId = group.id,
            )
            cursor += duration
            return null
        }

        var lastGroupExplicitRest: Int? = null
        rules.openingGroups.forEachIndexed { groupIndex, group ->
            for (index in 0 until group.count) {
                addPose(group.poseSeconds, PoseSource.OPENING, group.id)
            }
            val rest = group.breakAfterSeconds
            if (groupIndex != rules.openingGroups.lastIndex && rest != null && rest > 0) {
                // There are poses in the next group: a zero rest creates no phase.
                if (cursor.toLong() + rest > end) {
                    return Attempt(phases, "Opening-group rest exceeds work duration.")
                }
                phases += SchedulePhase(
                    id = "phase_" + (phases.size + 1),
                    kind = PhaseKind.SHORT_BREAK,
                    startSeconds = cursor,
                    endSeconds = cursor + rest,
                    openingGroupId = group.id,
                )
                cursor += rest
            } else if (groupIndex == rules.openingGroups.lastIndex) {
                lastGroupExplicitRest = rest
            }
        }
        // When the repeating pattern begins with a 5m regular break, the same
        // phase already satisfies a last-group 5m rest (Example A). A different
        // explicitly chosen rest replaces that initial slot and is not counted
        // as a replaceable normal-break ordinal. POSE_FIRST needs a new rest.
        val shareRegularFirstBreak = lastGroupExplicitRest != null &&
            rules.cycleStart == CycleStart.BREAK_FIRST &&
            lastGroupExplicitRest == rules.shortBreakSeconds
        if (lastGroupExplicitRest != null) {
            if (cursor >= end) {
                return Attempt(phases,
                    "The last opening group must leave time for its break and a regular pose.")
            }
            if (!shareRegularFirstBreak) {
                val reason = addOpeningGroupBreak(
                    rules.openingGroups.last(), lastGroupExplicitRest!!
                )
                if (reason != null) return reason
            }
        }
        if (cursor == end) {
            if (replacements.isNotEmpty()) {
                return Attempt(phases, "Cannot replace breaks in an opening-only session.")
            }
        } else {
            var nextIsBreak = rules.cycleStart == CycleStart.BREAK_FIRST &&
                (lastGroupExplicitRest == null || shareRegularFirstBreak)
            while (cursor < end) {
                if (nextIsBreak) {
                    breakOrdinal++
                    val special = breakOrdinal in replacements
                    val duration =
                        if (special) rules.specialBreakSeconds else rules.shortBreakSeconds
                    if (cursor.toLong() + duration + SECONDS_PER_MINUTE > end) {
                        return Attempt(phases,
                            "A break would not leave one full minute for a following pose.")
                    }
                    phases += SchedulePhase(
                        id = "phase_" + (phases.size + 1),
                        kind = if (special) PhaseKind.SPECIAL_BREAK else PhaseKind.SHORT_BREAK,
                        startSeconds = cursor,
                        endSeconds = cursor + duration,
                        breakOrdinal = breakOrdinal,
                    )
                    cursor += duration
                    nextIsBreak = false
                } else {
                    val duration = min(rules.poseSeconds, end - cursor)
                    if (duration < SECONDS_PER_MINUTE) {
                        return Attempt(phases, "A remaining pose would have less than one minute.")
                    }
                    addPose(duration, PoseSource.REGULAR)
                    nextIsBreak = cursor < end
                }
            }
        }
        if (replacements.any { it > breakOrdinal }) {
            return Attempt(phases, "Manual break ordinal does not exist.")
        }
        if (rules.summarySeconds > 0) {
            phases += SchedulePhase(
                id = "phase_" + (phases.size + 1),
                kind = PhaseKind.SUMMARY,
                startSeconds = end,
                endSeconds = rules.totalSeconds,
            )
        }
        if (phases.isEmpty() || phases.first().startSeconds != 0 ||
            phases.last().endSeconds != rules.totalSeconds
        ) return Attempt(phases, "Schedule does not cover the entire session.")
        return Attempt(phases)
    }

    private fun restWarnings(phases: List<SchedulePhase>): List<ScheduleIssue> {
        val warnings = mutableListOf<ScheduleIssue>()
        var accumulated = 0
        var warned = false
        for (phase in phases) {
            if (phase.kind == PhaseKind.POSE) {
                val prior = accumulated
                accumulated += phase.durationSeconds
                if (accumulated > REST_THRESHOLD && !warned) {
                    warnings += ScheduleIssue(
                        IssueSeverity.WARNING,
                        "rest_25_5",
                        "Posing exceeded 25 accumulated minutes without " +
                            "a qualifying rest of at least 5 minutes.",
                        phase.startSeconds + (REST_THRESHOLD - prior).coerceAtLeast(0),
                    )
                    warned = true
                }
            } else if (
                (phase.kind == PhaseKind.SHORT_BREAK ||
                    phase.kind == PhaseKind.SPECIAL_BREAK) &&
                phase.durationSeconds >= QUALIFYING_REST
            ) {
                accumulated = 0
                warned = false
            }
        }
        return warnings
    }
}
