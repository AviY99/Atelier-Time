// Dependency-free smoke check. Run from repository root:
//   dart --enable-asserts run tool/smoke.dart
//
// Does not use GitHub Actions, Flutter, pub get or the external test package.
// Not executed here because this environment does not have the Dart SDK.

import '../lib/src/scheduling.dart';

int m(int minutes) => minutes * secondsPerMinute;

void check(bool condition, String message) {
  if (!condition) throw StateError(message);
}

void checkTimeline(ScheduleCandidate c, List<String> expected) {
  final actual = [
    for (final phase in c.phases)
      phase.kind.name +
          ':' +
          (phase.startSeconds ~/ 60).toString() +
          '-' +
          (phase.endSeconds ~/ 60).toString()
  ];
  check(actual.join('|') == expected.join('|'),
      'Expected ' + expected.join('|') + '; actual ' + actual.join('|'));
  check(!c.hasBlockingIssues, 'Candidate unexpectedly blocked.');
  check(c.phases.last.endSeconds == c.rules.totalSeconds,
      'Session duration did not match.');
}

void main() {
  final engine = ScheduleEngine();
  final a = SessionRules(
    id: 'a',
    name: 'Example A',
    totalSeconds: m(180),
    summarySeconds: m(15),
    openingGroups: [
      OpeningPoseGroup(id: '1m', count: 4, poseSeconds: m(1)),
      OpeningPoseGroup(id: '4m', count: 4, poseSeconds: m(4)),
    ],
    poseSeconds: m(25),
    shortBreakSeconds: m(5),
    specialBreakSeconds: m(15),
    specialBreakCount: 1,
    cycleStart: CycleStart.breakFirst,
  );
  final b = SessionRules(
    id: 'b',
    name: 'Example B',
    totalSeconds: m(180),
    summarySeconds: m(15),
    poseSeconds: m(25),
    shortBreakSeconds: m(5),
    specialBreakSeconds: m(15),
    specialBreakCount: 1,
  );
  checkTimeline(engine.generate(a, choice: const FinalPoseChoice.remainder()), [
    'pose:0-1', 'pose:1-2', 'pose:2-3', 'pose:3-4',
    'pose:4-8', 'pose:8-12', 'pose:12-16', 'pose:16-20',
    'shortBreak:20-25', 'pose:25-50', 'shortBreak:50-55',
    'pose:55-80', 'specialBreak:80-95', 'pose:95-120',
    'shortBreak:120-125', 'pose:125-150', 'shortBreak:150-155',
    'pose:155-165', 'summary:165-180',
  ]);
  checkTimeline(engine.generate(b, choice: const FinalPoseChoice.remainder()), [
    'pose:0-25', 'shortBreak:25-30', 'pose:30-55',
    'shortBreak:55-60', 'pose:60-85', 'specialBreak:85-100',
    'pose:100-125', 'shortBreak:125-130', 'pose:130-155',
    'shortBreak:155-160', 'pose:160-165', 'summary:165-180',
  ]);
  final evenA = engine.generate(a, choice: const FinalPoseChoice.even());
  check(evenA.phases[15].durationSeconds == m(17) &&
      evenA.phases[17].durationSeconds == m(18),
      'Example A even pair incorrect.');
  final evenB = engine.generate(b, choice: const FinalPoseChoice.even());
  check(evenB.phases[8].durationSeconds == m(15) &&
      evenB.phases[10].durationSeconds == m(15),
      'Example B even pair incorrect.');

  final invalid = engine.generate(SessionRules(
    id: 'invalid', name: 'Invalid 0m', totalSeconds: 0, poseSeconds: m(25),
  ));
  check(invalid.hasBlockingIssues, '0m total was not blocked.');
  final warning = engine.generate(SessionRules(
    id: 'warning', name: '30m pose',
    totalSeconds: m(30), poseSeconds: m(30),
  ));
  check(warning.needsSafetyAcknowledgement, 'Missing 25/5 warning.');
  print('Scheduling smoke checks succeeded.');
}
