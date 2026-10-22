/// Pure scheduling core for Atelier Time.
///
/// Inputs and phase offsets are integer seconds. Current editor/test fixtures
/// use whole minutes; no Flutter dependencies or platform services.
///
/// Approved design: docs/STEP_1_3_REVIEW.md (D-037); zero-length session or
/// configured break prohibited (D-036); 25/5 warnings (D-031).
///
/// D-038 is still open: positive special break <= regular break is reported as
/// a pending policy decision rather than being silently accepted or rejected.
library;

import 'dart:math' as math;

const int secondsPerMinute = 60;
const int _maxUnbrokenPoseSeconds = 25 * secondsPerMinute;
const int _qualifyingRestSeconds = 5 * secondsPerMinute;

enum CycleStart { poseFirst, breakFirst }
enum PhaseKind { pose, shortBreak, specialBreak, summary }
enum PoseSource { opening, regular }
enum IssueSeverity { error, warning, pendingDecision }
enum FinalPoseMode { remainder, evenSplit, customSplit }

class OpeningPoseGroup {
  final String id;
  final int count;
  final int poseSeconds;

  const OpeningPoseGroup({
    required this.id,
    required this.count,
    required this.poseSeconds,
  });
}

class SpecialBreakPlacement {
  final bool automatic;
  final List<int> breakOrdinals;

  const SpecialBreakPlacement.automatic()
      : automatic = true,
        breakOrdinals = const [];

  SpecialBreakPlacement.manual(List<int> ordinals)
      : automatic = false,
        breakOrdinals = List.unmodifiable(ordinals);
}

class SessionRules {
  final String id;
  final String name;
  final int totalSeconds;
  final int summarySeconds;
  final List<OpeningPoseGroup> openingGroups;
  final int poseSeconds;
  final int shortBreakSeconds;
  final int specialBreakSeconds;
  final int specialBreakCount;
  final CycleStart cycleStart;
  final SpecialBreakPlacement placement;

  SessionRules({
    required this.id,
    required this.name,
    required this.totalSeconds,
    this.summarySeconds = 0,
    List<OpeningPoseGroup> openingGroups = const [],
    required this.poseSeconds,
    this.shortBreakSeconds = 5 * secondsPerMinute,
    this.specialBreakSeconds = 15 * secondsPerMinute,
    this.specialBreakCount = 0,
    this.cycleStart = CycleStart.poseFirst,
    SpecialBreakPlacement? placement,
  })  : openingGroups = List.unmodifiable(openingGroups),
        placement = placement ?? const SpecialBreakPlacement.automatic();

  int get workSeconds => totalSeconds - summarySeconds;
}

class ScheduleIssue {
  final IssueSeverity severity;
  final String code;
  final String message;
  final int? offsetSeconds;

  const ScheduleIssue(this.severity, this.code, this.message,
      {this.offsetSeconds});
}

class SchedulePhase {
  final String id;
  final PhaseKind kind;
  final int startSeconds;
  final int endSeconds;
  final int? poseNumber;
  final PoseSource? poseSource;
  final String? openingGroupId;
  final int? breakOrdinal;

  const SchedulePhase({
    required this.id,
    required this.kind,
    required this.startSeconds,
    required this.endSeconds,
    this.poseNumber,
    this.poseSource,
    this.openingGroupId,
    this.breakOrdinal,
  });

  int get durationSeconds => endSeconds - startSeconds;

  SchedulePhase shifted(int start, int end) => SchedulePhase(
        id: id,
        kind: kind,
        startSeconds: start,
        endSeconds: end,
        poseNumber: poseNumber,
        poseSource: poseSource,
        openingGroupId: openingGroupId,
        breakOrdinal: breakOrdinal,
      );

  SchedulePhase withId(String value) => SchedulePhase(
        id: value,
        kind: kind,
        startSeconds: startSeconds,
        endSeconds: endSeconds,
        poseNumber: poseNumber,
        poseSource: poseSource,
        openingGroupId: openingGroupId,
        breakOrdinal: breakOrdinal,
      );
}

class FinalPoseChoice {
  final FinalPoseMode mode;
  final int? firstPoseSeconds;

  const FinalPoseChoice.remainder()
      : mode = FinalPoseMode.remainder,
        firstPoseSeconds = null;

  const FinalPoseChoice.even()
      : mode = FinalPoseMode.evenSplit,
        firstPoseSeconds = null;

  const FinalPoseChoice.custom(int value)
      : mode = FinalPoseMode.customSplit,
        firstPoseSeconds = value;
}

class ScheduleCandidate {
  final SessionRules rules;
  final List<SchedulePhase> phases;
  final List<ScheduleIssue> issues;
  final bool finalPoseIsShort;
  final bool canSplitFinalTwo;
  final FinalPoseChoice? selectedChoice;

  ScheduleCandidate({
    required this.rules,
    required List<SchedulePhase> phases,
    required List<ScheduleIssue> issues,
    required this.finalPoseIsShort,
    required this.canSplitFinalTwo,
    required this.selectedChoice,
  })  : phases = List.unmodifiable(phases),
        issues = List.unmodifiable(issues);

  bool get hasBlockingIssues => issues.any((issue) =>
      issue.severity == IssueSeverity.error ||
      issue.severity == IssueSeverity.pendingDecision);

  bool get needsFinalPoseChoice => finalPoseIsShort && selectedChoice == null;
  bool get needsSafetyAcknowledgement =>
      issues.any((issue) => issue.severity == IssueSeverity.warning);

  bool get canApprove => !hasBlockingIssues && !needsFinalPoseChoice;
}

class ApprovedSession {
  final SessionRules rules;
  final List<SchedulePhase> phases;
  final FinalPoseChoice? selectedChoice;
  final List<String> acknowledgedWarningCodes;

  ApprovedSession._(
    this.rules,
    List<SchedulePhase> phases,
    this.selectedChoice,
    List<String> warningCodes,
  )   : phases = List.unmodifiable(phases),
        acknowledgedWarningCodes = List.unmodifiable(warningCodes);
}

class _Attempt {
  final List<SchedulePhase> phases;
  final String? invalidReason;

  const _Attempt(this.phases, [this.invalidReason]);

  bool get valid => invalidReason == null;
}

class ScheduleEngine {
  /// Builds a review candidate without mutating SessionRules.
  ///
  /// No choice means a shortened last pose remains explicitly unresolved.
  /// Returning a candidate does NOT save or authorize it.
  ScheduleCandidate generate(SessionRules rules, {FinalPoseChoice? choice}) {
    final issues = <ScheduleIssue>[];
    void error(String code, String message) =>
        issues.add(ScheduleIssue(IssueSeverity.error, code, message));

    if (rules.totalSeconds <= 0) {
      error('nonpositive_session', 'Full session duration must be positive.');
    }
    if (rules.summarySeconds < 0 ||
        rules.summarySeconds >= rules.totalSeconds) {
      error('invalid_summary', 'Summary must leave a positive work period.');
    }
    if (rules.poseSeconds <= 0) {
      error('nonpositive_pose', 'Regular pose duration must be positive.');
    }
    if (rules.shortBreakSeconds <= 0 ||
        rules.specialBreakSeconds <= 0) {
      error('nonpositive_break', 'Configured breaks must be positive.');
    }
    if (rules.specialBreakCount < 0) {
      error('negative_break_count', 'Special break count cannot be negative.');
    }
    final durations = <int>[
      rules.totalSeconds,
      rules.summarySeconds,
      rules.poseSeconds,
      rules.shortBreakSeconds,
      rules.specialBreakSeconds,
    ];
    var openingSum = 0;
    for (final group in rules.openingGroups) {
      if (group.count <= 0 || group.poseSeconds <= 0) {
        error('invalid_opening_group', 'Opening groups need positive values.');
      } else {
        openingSum += group.count * group.poseSeconds;
      }
      durations.add(group.poseSeconds);
    }
    if (durations.any((value) => value % secondsPerMinute != 0)) {
      error('minute_precision', 'Initial session rules use whole minutes.');
    }
    if (openingSum > rules.workSeconds) {
      error('opening_overrun', 'Opening poses exceed available work time.');
    }
    if (!rules.placement.automatic && rules.specialBreakCount > 0) {
      final ordinals = rules.placement.breakOrdinals;
      if (ordinals.length != rules.specialBreakCount ||
          ordinals.any((value) => value <= 0) ||
          ordinals.toSet().length != ordinals.length) {
        error('invalid_manual_ordinals',
            'Choose distinct, positive ordinals for every special break.');
      }
    }
    // Important: D-038 has no approved behavior yet; this is NOT a claim
    // that such a combination is invalid. We explicitly defer its decision.
    if (rules.specialBreakCount > 0 &&
        rules.specialBreakSeconds <= rules.shortBreakSeconds &&
        rules.specialBreakSeconds > 0 &&
        rules.shortBreakSeconds > 0) {
      issues.add(const ScheduleIssue(
        IssueSeverity.pendingDecision,
        'd038_special_length_unresolved',
        'Ask the user whether a positive special break may be no longer '
            'than a normal break. This case has no approved policy.',
      ));
    }
    if (issues.any((i) => i.severity != IssueSeverity.warning)) {
      return _candidate(rules, const [], issues, false, false, choice);
    }

    late final _Attempt schedule;
    if (rules.specialBreakCount == 0) {
      schedule = _build(rules, const []);
    } else if (!rules.placement.automatic) {
      final sorted = [...rules.placement.breakOrdinals]..sort();
      schedule = _build(rules, sorted);
    } else {
      schedule = _chooseAutomatic(rules);
    }
    if (!schedule.valid) {
      error('unbuildable_timeline',
          schedule.invalidReason ?? 'Cannot fit requested session phases.');
      return _candidate(rules, schedule.phases, issues, false, false, choice);
    }

    var phases = schedule.phases;
    final regularPoses = <int>[];
    for (var i = 0; i < phases.length; i++) {
      if (phases[i].kind == PhaseKind.pose &&
          phases[i].poseSource == PoseSource.regular) {
        regularPoses.add(i);
      }
    }
    final lastPoseIndex = regularPoses.isEmpty ? -1 : regularPoses.last;
    final shortened = lastPoseIndex >= 0 &&
        phases[lastPoseIndex].durationSeconds < rules.poseSeconds;
    var canSplit = false;
    if (shortened &&
        lastPoseIndex >= 2 &&
        phases[lastPoseIndex - 1].kind != PhaseKind.pose &&
        phases[lastPoseIndex - 1].kind != PhaseKind.summary &&
        phases[lastPoseIndex - 2].kind == PhaseKind.pose &&
        phases[lastPoseIndex - 2].poseSource == PoseSource.regular) {
      final availablePoseSeconds =
          phases[lastPoseIndex].durationSeconds +
          phases[lastPoseIndex - 2].durationSeconds;
      canSplit = availablePoseSeconds >= 2 * secondsPerMinute;
    }

    if (shortened && choice != null) {
      if (choice.mode != FinalPoseMode.remainder && !canSplit) {
        error('split_unavailable',
            'No two eligible regular poses with an existing break to split.');
      } else if (choice.mode != FinalPoseMode.remainder) {
        final firstIndex = lastPoseIndex - 2;
        final first = phases[firstIndex];
        final between = phases[firstIndex + 1];
        final last = phases[lastPoseIndex];
        final totalPoseTime = first.durationSeconds + last.durationSeconds;
        int firstDuration;
        if (choice.mode == FinalPoseMode.evenSplit) {
          firstDuration =
              (totalPoseTime ~/ (2 * secondsPerMinute)) * secondsPerMinute;
        } else {
          firstDuration = choice.firstPoseSeconds ?? 0;
        }
        if (firstDuration < secondsPerMinute ||
            firstDuration > totalPoseTime - secondsPerMinute ||
            firstDuration % secondsPerMinute != 0) {
          error('invalid_custom_split',
              'Each adjusted pose needs at least one whole minute.');
        } else {
          final start = first.startSeconds;
          final breakStart = start + firstDuration;
          final breakEnd = breakStart + between.durationSeconds;
          final modified = [...phases];
          modified[firstIndex] = first.shifted(start, breakStart);
          modified[firstIndex + 1] =
              between.shifted(breakStart, breakEnd);
          modified[lastPoseIndex] =
              last.shifted(breakEnd, last.endSeconds);
          phases = modified;
        }
      }
    } else if (!shortened && choice != null) {
      error('unneeded_final_choice',
          'Final-pose adjustment is only available for a shortened pose.');
    }
    phases = _renumber(phases);
    if (!issues.any((i) => i.severity == IssueSeverity.error)) {
      issues.addAll(_restWarnings(phases));
    }
    return _candidate(
        rules, phases, issues, shortened, canSplit, choice);
  }

  ApprovedSession approve(ScheduleCandidate candidate,
      {required bool acknowledgeWarnings}) {
    if (!candidate.canApprove) {
      throw StateError('Cannot approve invalid or unresolved schedule.');
    }
    if (candidate.needsSafetyAcknowledgement && !acknowledgeWarnings) {
      throw StateError('Rest-warning acknowledgement is required.');
    }
    return ApprovedSession._(
      candidate.rules,
      candidate.phases,
      candidate.selectedChoice,
      candidate.issues
          .where((i) => i.severity == IssueSeverity.warning)
          .map((i) => i.code)
          .toList(),
    );
  }

  ScheduleCandidate _candidate(
    SessionRules rules,
    List<SchedulePhase> phases,
    List<ScheduleIssue> issues,
    bool shortened,
    bool canSplit,
    FinalPoseChoice? choice,
  ) =>
      ScheduleCandidate(
        rules: rules,
        phases: phases,
        issues: issues,
        finalPoseIsShort: shortened,
        canSplitFinalTwo: canSplit,
        selectedChoice: choice,
      );

  /// Each candidate is generated with its replacements already applied,
  /// so actual interval centers (not unmodified break positions) are scored.
  /// This exact exhaustive optimizer is adequate for small atelier schedules;
  /// runtime scalability for very large multi-break inputs is NOT yet proven.
  _Attempt _chooseAutomatic(SessionRules rules) {
    final ordinary = _build(rules, const []);
    final maxOrdinal = ordinary.phases
        .where((phase) => phase.kind == PhaseKind.shortBreak)
        .length;
    if (rules.specialBreakCount > maxOrdinal) {
      return const _Attempt([],
          'Requested more special breaks than available ordinary slots.');
    }
    _Attempt? best;
    int? bestScore;
    List<int>? bestOrdinals;
    final chosen = <int>[];
    void explore(int next) {
      if (chosen.length == rules.specialBreakCount) {
        final candidate = _build(rules, chosen);
        if (!candidate.valid) return;
        final special = candidate.phases
            .where((p) => p.kind == PhaseKind.specialBreak)
            .toList();
        if (special.length != rules.specialBreakCount) return;
        var score = 0;
        final countPlusOne = rules.specialBreakCount + 1;
        for (var i = 0; i < special.length; i++) {
          final actualTwiceCenter =
              special[i].startSeconds + special[i].endSeconds;
          score += (actualTwiceCenter * countPlusOne -
                  2 * rules.workSeconds * (i + 1))
              .abs();
        }
        if (bestScore == null ||
            score < bestScore! ||
            (score == bestScore &&
                _ordinalsEarlier(chosen, bestOrdinals!))) {
          best = candidate;
          bestScore = score;
          bestOrdinals = [...chosen];
        }
        return;
      }
      final remaining = rules.specialBreakCount - chosen.length;
      for (var ordinal = next;
          ordinal <= maxOrdinal - remaining + 1;
          ordinal++) {
        chosen.add(ordinal);
        explore(ordinal + 1);
        chosen.removeLast();
      }
    }

    explore(1);
    return best ??
        const _Attempt([],
            'No feasible replacement positions for the requested breaks.');
  }

  bool _ordinalsEarlier(List<int> a, List<int> b) {
    for (var i = 0; i < a.length; i++) {
      if (a[i] != b[i]) return a[i] < b[i];
    }
    return false;
  }

  _Attempt _build(SessionRules rules, List<int> replacements) {
    final phases = <SchedulePhase>[];
    var cursor = 0;
    var poseNumber = 0;
    var breakOrdinal = 0;

    void addPose(int duration, PoseSource source, [String? groupId]) {
      poseNumber++;
      phases.add(SchedulePhase(
        id: 'phase_' + (phases.length + 1).toString(),
        kind: PhaseKind.pose,
        startSeconds: cursor,
        endSeconds: cursor + duration,
        poseNumber: poseNumber,
        poseSource: source,
        openingGroupId: groupId,
      ));
      cursor += duration;
    }

    for (final group in rules.openingGroups) {
      for (var i = 0; i < group.count; i++) {
        addPose(group.poseSeconds, PoseSource.opening, group.id);
      }
    }
    final end = rules.workSeconds;
    if (cursor == end) {
      if (replacements.isNotEmpty) {
        return _Attempt(phases,
            'Cannot replace breaks in an opening-only session.');
      }
    } else {
      var nextIsBreak = rules.cycleStart == CycleStart.breakFirst;
      while (cursor < end) {
        if (nextIsBreak) {
          breakOrdinal++;
          final special = replacements.contains(breakOrdinal);
          final duration =
              special ? rules.specialBreakSeconds : rules.shortBreakSeconds;
          if (cursor + duration + secondsPerMinute > end) {
            return _Attempt(phases,
                'A break would not leave one full minute for a following pose.');
          }
          phases.add(SchedulePhase(
            id: 'phase_' + (phases.length + 1).toString(),
            kind: special ? PhaseKind.specialBreak : PhaseKind.shortBreak,
            startSeconds: cursor,
            endSeconds: cursor + duration,
            breakOrdinal: breakOrdinal,
          ));
          cursor += duration;
          nextIsBreak = false;
        } else {
          final duration = math.min(rules.poseSeconds, end - cursor);
          if (duration < secondsPerMinute) {
            return _Attempt(phases,
                'A remaining pose would have less than one minute.');
          }
          addPose(duration, PoseSource.regular);
          nextIsBreak = cursor < end;
        }
      }
    }
    if (replacements.any((ordinal) => ordinal > breakOrdinal)) {
      return _Attempt(phases, 'Manual break ordinal does not exist.');
    }
    if (rules.summarySeconds > 0) {
      phases.add(SchedulePhase(
        id: 'phase_' + (phases.length + 1).toString(),
        kind: PhaseKind.summary,
        startSeconds: end,
        endSeconds: rules.totalSeconds,
      ));
    }
    if (phases.isEmpty ||
        phases.first.startSeconds != 0 ||
        phases.last.endSeconds != rules.totalSeconds) {
      return _Attempt(phases, 'Schedule does not cover the entire session.');
    }
    return _Attempt(phases);
  }

  List<SchedulePhase> _renumber(List<SchedulePhase> source) {
    return [
      for (var i = 0; i < source.length; i++)
        source[i].withId('phase_' + (i + 1).toString()),
    ];
  }

  List<ScheduleIssue> _restWarnings(List<SchedulePhase> phases) {
    final warnings = <ScheduleIssue>[];
    var accumulated = 0;
    var warned = false;
    for (final phase in phases) {
      if (phase.kind == PhaseKind.pose) {
        accumulated += phase.durationSeconds;
        if (accumulated > _maxUnbrokenPoseSeconds && !warned) {
          warnings.add(ScheduleIssue(
            IssueSeverity.warning,
            'rest_25_5',
            'Posing exceeded 25 accumulated minutes without '
                'a qualifying rest of at least 5 minutes.',
            offsetSeconds: phase.startSeconds,
          ));
          warned = true;
        }
      } else if ((phase.kind == PhaseKind.shortBreak ||
              phase.kind == PhaseKind.specialBreak) &&
          phase.durationSeconds >= _qualifyingRestSeconds) {
        accumulated = 0;
        warned = false;
      }
    }
    return warnings;
  }
}
