/// Stage 2 editable text-to-rules adapter.
///
/// Kept independent of Flutter so Android Termux can test form parsing with
/// the already installed Dart SDK. Validation of mathematical scheduling
/// constraints remains entirely in ScheduleEngine.generate.
import 'scheduling.dart';

class OpeningGroupDraft {
  final String id;
  final String count;
  final String minutes;

  const OpeningGroupDraft({
    required this.id,
    required this.count,
    required this.minutes,
  });
}

class SessionEditorDraft {
  final String id;
  final String name;
  final String totalMinutes;
  final String summaryMinutes;
  final String poseMinutes;
  final String normalBreakMinutes;
  final String specialBreakMinutes;
  final String specialBreakCount;
  final List<OpeningGroupDraft> openingGroups;
  final CycleStart cycleStart;
  final bool automaticPlacement;
  final String manualBreakOrdinals;

  SessionEditorDraft({
    required this.id,
    required this.name,
    required this.totalMinutes,
    required this.summaryMinutes,
    required this.poseMinutes,
    required this.normalBreakMinutes,
    required this.specialBreakMinutes,
    required this.specialBreakCount,
    List<OpeningGroupDraft> openingGroups = const [],
    this.cycleStart = CycleStart.poseFirst,
    this.automaticPlacement = true,
    this.manualBreakOrdinals = '',
  }) : openingGroups = List.unmodifiable(openingGroups);

  int _readWhole(String raw, String label) {
    final value = int.tryParse(raw.trim());
    if (value == null) {
      throw FormatException('Enter a whole number for $label.');
    }
    return value;
  }

  /// Return precisely the supplied values in whole-minute seconds.
  ///
  /// This never silently replaces user input with defaults. The engine then
  /// reports blocking 0-minute/negative values, overflows or infeasibility.
  /// A zero requested special-break *count* is legal, and unused ordinal
  /// text must not block a schedule when zero breaks are requested.
  SessionRules toRules() {
    if (name.trim().isEmpty) {
      throw const FormatException('Enter a session name.');
    }
    final count = _readWhole(specialBreakCount, 'special break count');
    final opening = <OpeningPoseGroup>[];
    for (final group in openingGroups) {
      opening.add(OpeningPoseGroup(
        id: group.id,
        count: _readWhole(group.count, 'opening pose count'),
        poseSeconds:
            _readWhole(group.minutes, 'opening pose minutes') *
                secondsPerMinute,
      ));
    }

    SpecialBreakPlacement placement = const SpecialBreakPlacement.automatic();
    if (!automaticPlacement && count > 0) {
      final ordinals = <int>[];
      final typed = manualBreakOrdinals.trim();
      if (typed.isNotEmpty) {
        for (final token in typed.split(',')) {
          ordinals.add(_readWhole(token, 'manual break ordinal'));
        }
      }
      placement = SpecialBreakPlacement.manual(ordinals);
    }

    return SessionRules(
      id: id,
      name: name.trim(),
      totalSeconds: _readWhole(totalMinutes, 'total minutes') *
          secondsPerMinute,
      summarySeconds: _readWhole(summaryMinutes, 'summary minutes') *
          secondsPerMinute,
      openingGroups: opening,
      poseSeconds: _readWhole(poseMinutes, 'pose minutes') *
          secondsPerMinute,
      shortBreakSeconds: _readWhole(normalBreakMinutes, 'normal break minutes') *
          secondsPerMinute,
      specialBreakSeconds:
          _readWhole(specialBreakMinutes, 'special break minutes') *
              secondsPerMinute,
      specialBreakCount: count,
      cycleStart: cycleStart,
      placement: placement,
    );
  }
}
