import 'package:atelier_time_core/atelier_time.dart';
import 'package:web/web.dart';

/// Minimal compiled-to-JavaScript proof, NOT a production session UI.
///
/// This exercises the SAME Dart scheduler verified by Android Termux tests.
/// A successful Android browser display proves code reuse without Flutter.
void main() {
  final status = document.querySelector('#status');
  final timeline = document.querySelector('#timeline');

  try {
    final rules = SessionEditorDraft(
      id: 'android-phone-spike',
      name: 'Example B - Android-phone proof',
      totalMinutes: '180',
      summaryMinutes: '15',
      poseMinutes: '25',
      normalBreakMinutes: '5',
      specialBreakMinutes: '15',
      specialBreakCount: '1',
    ).toRules();

    final candidate = ScheduleEngine().generate(
      rules,
      choice: const FinalPoseChoice.remainder(),
    );

    if (candidate.hasBlockingIssues || candidate.phases.isEmpty) {
      final details = candidate.issues.map((i) => i.message).join('; ');
      throw StateError('Scheduler rejected fixture: ' + details);
    }

    final longBreaks = candidate.phases
        .where((phase) => phase.kind == PhaseKind.specialBreak)
        .toList();
    final summaries = candidate.phases
        .where((phase) => phase.kind == PhaseKind.summary)
        .toList();

    final correctBreak = longBreaks.length == 1 &&
        longBreaks.single.startSeconds == 85 * secondsPerMinute &&
        longBreaks.single.endSeconds == 100 * secondsPerMinute;
    final correctSummary = summaries.length == 1 &&
        summaries.single.startSeconds == 165 * secondsPerMinute &&
        summaries.single.endSeconds == 180 * secondsPerMinute;

    if (!correctBreak || !correctSummary) {
      throw StateError('Unexpected Example B phase timestamps');
    }

    if (status != null) {
      status.textContent =
          'PASS: existing Dart scheduling core executed in the Android browser.';
    }
    if (timeline != null) {
      timeline.textContent = [
        'Example B | 180 minutes | ' +
            candidate.phases.length.toString() + ' real phases',
        'Special break: 01:25-01:40',
        'Reserved final summary: 02:45-03:00',
        '',
        for (final phase in candidate.phases)
          _stamp(phase.startSeconds) +
              '-' + _stamp(phase.endSeconds) + ' ' +
              phase.kind.name + ' | ' +
              (phase.durationSeconds ~/ secondsPerMinute).toString() + 'm',
      ].join('\n');
    }
  } catch (error) {
    if (status != null) {
      status.textContent = 'FAIL: ' + error.toString();
    }
  }
}

String _stamp(int seconds) {
  final minute = seconds ~/ secondsPerMinute;
  final hour = (minute ~/ 60).toString().padLeft(2, '0');
  final rest = (minute % 60).toString().padLeft(2, '0');
  return hour + ':' + rest;
}
