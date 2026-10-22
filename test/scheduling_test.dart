import 'package:atelier_time_core/atelier_time.dart';
import 'package:test/test.dart';

int m(int minutes) => minutes * secondsPerMinute;

SessionRules exampleA({SpecialBreakPlacement? placement}) => SessionRules(
      id: 'a',
      name: 'Opening short poses',
      totalSeconds: m(180),
      summarySeconds: m(15),
      openingGroups: [
        OpeningPoseGroup(id: 'one', count: 4, poseSeconds: m(1)),
        OpeningPoseGroup(id: 'four', count: 4, poseSeconds: m(4)),
      ],
      poseSeconds: m(25),
      shortBreakSeconds: m(5),
      specialBreakSeconds: m(15),
      specialBreakCount: 1,
      placement: placement,
      cycleStart: CycleStart.breakFirst,
    );

SessionRules exampleB({
  int specialCount = 1,
  SpecialBreakPlacement? placement,
}) =>
    SessionRules(
      id: 'b',
      name: 'Long poses',
      totalSeconds: m(180),
      summarySeconds: m(15),
      poseSeconds: m(25),
      shortBreakSeconds: m(5),
      specialBreakSeconds: m(15),
      specialBreakCount: specialCount,
      placement: placement,
    );

List<String> timeline(ScheduleCandidate c) => [
      for (final p in c.phases)
        p.kind.name +
            ':' +
            (p.startSeconds ~/ secondsPerMinute).toString() +
            '-' +
            (p.endSeconds ~/ secondsPerMinute).toString(),
    ];

void expectContiguous(ScheduleCandidate candidate) {
  expect(candidate.hasBlockingIssues, isFalse,
      reason: candidate.issues.map((i) => i.message).join('; '));
  expect(candidate.phases, isNotEmpty);
  expect(candidate.phases.first.startSeconds, 0);
  expect(candidate.phases.last.endSeconds, candidate.rules.totalSeconds);
  for (var i = 0; i < candidate.phases.length; i++) {
    final p = candidate.phases[i];
    expect(p.durationSeconds, greaterThan(0));
    expect(p.id, 'phase_' + (i + 1).toString());
    if (i > 0) {
      expect(p.startSeconds, candidate.phases[i - 1].endSeconds);
    }
  }
}

void main() {
  final engine = ScheduleEngine();

  group('canonical Example A', () {
    test('exact phase-by-phase baseline, with replacement in slot 3', () {
      final c = engine.generate(
          exampleA(), choice: const FinalPoseChoice.remainder());
      expectContiguous(c);
      expect(c.finalPoseIsShort, isTrue);
      expect(c.needsFinalPoseChoice, isFalse);
      expect(timeline(c), [
        'pose:0-1',
        'pose:1-2',
        'pose:2-3',
        'pose:3-4',
        'pose:4-8',
        'pose:8-12',
        'pose:12-16',
        'pose:16-20',
        'shortBreak:20-25',
        'pose:25-50',
        'shortBreak:50-55',
        'pose:55-80',
        'specialBreak:80-95',
        'pose:95-120',
        'shortBreak:120-125',
        'pose:125-150',
        'shortBreak:150-155',
        'pose:155-165',
        'summary:165-180',
      ]);
      expect(c.phases.singleWhere((p) => p.kind == PhaseKind.specialBreak)
          .breakOrdinal, 3);
      expect(c.needsSafetyAcknowledgement, isFalse);
    });

    test('even ending uses 17+18; break still 5', () {
      final c =
          engine.generate(exampleA(), choice: const FinalPoseChoice.even());
      expectContiguous(c);
      expect(timeline(c).sublist(15), [
        'pose:125-142',
        'shortBreak:142-147',
        'pose:147-165',
        'summary:165-180',
      ]);
    });

    test('custom ending uses 20+15; break still 5', () {
      final c = engine.generate(
          exampleA(), choice: FinalPoseChoice.custom(m(20)));
      expectContiguous(c);
      expect(timeline(c).sublist(15), [
        'pose:125-145',
        'shortBreak:145-150',
        'pose:150-165',
        'summary:165-180',
      ]);
    });
  });

  group('canonical Example B', () {
    test('exact phase-by-phase baseline, with replacement in slot 3', () {
      final c = engine.generate(
          exampleB(), choice: const FinalPoseChoice.remainder());
      expectContiguous(c);
      expect(timeline(c), [
        'pose:0-25',
        'shortBreak:25-30',
        'pose:30-55',
        'shortBreak:55-60',
        'pose:60-85',
        'specialBreak:85-100',
        'pose:100-125',
        'shortBreak:125-130',
        'pose:130-155',
        'shortBreak:155-160',
        'pose:160-165',
        'summary:165-180',
      ]);
      expect(c.phases.singleWhere((p) => p.kind == PhaseKind.specialBreak)
          .breakOrdinal, 3);
      expect(c.needsSafetyAcknowledgement, isFalse);
    });

    test('even ending uses 15+15', () {
      final c = engine.generate(
          exampleB(), choice: const FinalPoseChoice.even());
      expectContiguous(c);
      expect(timeline(c).sublist(8), [
        'pose:130-145',
        'shortBreak:145-150',
        'pose:150-165',
        'summary:165-180',
      ]);
    });

    test('custom ending uses 18+12', () {
      final c = engine.generate(
          exampleB(), choice: FinalPoseChoice.custom(m(18)));
      expectContiguous(c);
      expect(timeline(c).sublist(8), [
        'pose:130-148',
        'shortBreak:148-153',
        'pose:153-165',
        'summary:165-180',
      ]);
    });
  });

  group('approval, placement and validation', () {
    test('a shortened final pose requires an explicit choice', () {
      final c = engine.generate(exampleB());
      expect(c.needsFinalPoseChoice, isTrue);
      expect(() => engine.approve(c, acknowledgeWarnings: false),
          throwsStateError);
      final resolved = engine.generate(
          exampleB(), choice: const FinalPoseChoice.remainder());
      expect(engine.approve(resolved, acknowledgeWarnings: false)
          .phases.last.endSeconds, m(180));
    });

    test('identical rules generate identical phase IDs and timeline', () {
      final a = engine.generate(
          exampleA(), choice: const FinalPoseChoice.remainder());
      final b = engine.generate(
          exampleA(), choice: const FinalPoseChoice.remainder());
      expect(timeline(a), timeline(b));
      expect(a.phases.map((p) => p.id).toList(),
          b.phases.map((p) => p.id).toList());
    });

    test('explicit break ordinal 3 has the canonical position', () {
      final c = engine.generate(
        exampleA(placement: SpecialBreakPlacement.manual([3])),
        choice: const FinalPoseChoice.remainder(),
      );
      expectContiguous(c);
      final special =
          c.phases.singleWhere((p) => p.kind == PhaseKind.specialBreak);
      expect(special.startSeconds, m(80));
      expect(special.endSeconds, m(95));
    });

    test('0 special count creates no special break, not a zero rest', () {
      final c = engine.generate(
        exampleB(specialCount: 0),
        choice: const FinalPoseChoice.remainder(),
      );
      expectContiguous(c);
      expect(c.phases.where((p) => p.kind == PhaseKind.specialBreak), isEmpty);
    });

    test('0 final summary is valid for a positive full session', () {
      final c = engine.generate(SessionRules(
        id: 'summary-zero',
        name: 'Summary optional',
        totalSeconds: m(25),
        poseSeconds: m(25),
      ));
      expectContiguous(c);
      expect(c.phases.map((p) => p.kind), [PhaseKind.pose]);
    });

    test('opening groups alone may fill work exactly', () {
      final c = engine.generate(SessionRules(
        id: 'open-only',
        name: 'Openings',
        totalSeconds: m(15),
        summarySeconds: m(5),
        poseSeconds: m(25),
        openingGroups: [
          OpeningPoseGroup(id: 'opening', count: 2, poseSeconds: m(5)),
        ],
      ));
      expectContiguous(c);
      expect(c.phases.map((p) => p.kind),
          [PhaseKind.pose, PhaseKind.pose, PhaseKind.summary]);
    });

    test('full session 0 is a hard error', () {
      final c = engine.generate(SessionRules(
        id: 'bad-total',
        name: 'Invalid',
        totalSeconds: 0,
        poseSeconds: m(25),
      ));
      expect(c.hasBlockingIssues, isTrue);
      expect(c.issues.any((i) => i.code == 'nonpositive_session'), isTrue);
    });

    test('both configured break duration fields must be positive', () {
      final badShort = engine.generate(SessionRules(
        id: 'bad-short',
        name: 'Invalid',
        totalSeconds: m(40),
        poseSeconds: m(25),
        shortBreakSeconds: 0,
      ));
      expect(badShort.hasBlockingIssues, isTrue);
      expect(badShort.issues.any((i) => i.code == 'nonpositive_break'), isTrue);

      final badSpecial = engine.generate(SessionRules(
        id: 'bad-special',
        name: 'Invalid',
        totalSeconds: m(40),
        poseSeconds: m(25),
        specialBreakSeconds: 0,
        specialBreakCount: 1,
      ));
      expect(badSpecial.hasBlockingIssues, isTrue);
    });

    test('unreachable manual ordinal cannot be silently relocated', () {
      final c = engine.generate(
        exampleB(placement: SpecialBreakPlacement.manual([99])),
        choice: const FinalPoseChoice.remainder(),
      );
      expect(c.hasBlockingIssues, isTrue);
    });

    test('custom split cannot make a zero-minute pose', () {
      final c =
          engine.generate(exampleA(), choice: const FinalPoseChoice.custom(0));
      expect(c.hasBlockingIssues, isTrue);
    });

    test('D-038 stays pending and is NOT falsely approved or invalidated', () {
      final c = engine.generate(SessionRules(
        id: 'd038',
        name: 'Length comparison undecided',
        totalSeconds: m(80),
        poseSeconds: m(25),
        shortBreakSeconds: m(5),
        specialBreakSeconds: m(5),
        specialBreakCount: 1,
      ));
      expect(c.issues.any((i) =>
          i.code == 'd038_special_length_unresolved' &&
          i.severity == IssueSeverity.pendingDecision), isTrue);
      expect(c.canApprove, isFalse);
    });
  });

  group('additional Stage 1.4 edge regressions', () {
    test('two automatically placed long breaks replace two slots', () {
      final c = engine.generate(SessionRules(
        id: 'two-auto',
        name: 'Two long breaks',
        totalSeconds: m(180),
        summarySeconds: m(15),
        poseSeconds: m(25),
        shortBreakSeconds: m(5),
        specialBreakSeconds: m(15),
        specialBreakCount: 2,
      ));
      expectContiguous(c);
      final special = c.phases
          .where((p) => p.kind == PhaseKind.specialBreak)
          .toList();
      expect(special.length, 2);
      expect(special[0].breakOrdinal, lessThan(special[1].breakOrdinal!));
      expect(c.phases.where((p) => p.kind == PhaseKind.summary).length, 1);
    });

    test('manual placement of two long breaks preserves chosen slots', () {
      final c = engine.generate(SessionRules(
        id: 'two-manual',
        name: 'Manually selected long breaks',
        totalSeconds: m(180),
        summarySeconds: m(15),
        poseSeconds: m(25),
        specialBreakCount: 2,
        placement: SpecialBreakPlacement.manual([2, 4]),
      ));
      expectContiguous(c);
      final special = c.phases
          .where((p) => p.kind == PhaseKind.specialBreak)
          .map((p) => p.breakOrdinal)
          .toList();
      expect(special, [2, 4]);
    });

    test('unfeasible special count blocks the candidate', () {
      final c = engine.generate(SessionRules(
        id: 'too-many',
        name: 'Not enough rest positions',
        totalSeconds: m(60),
        poseSeconds: m(25),
        specialBreakCount: 99,
      ));
      expect(c.hasBlockingIssues, isTrue);
    });

    test('break-first cannot finish on a trailing break', () {
      final c = engine.generate(SessionRules(
        id: 'trailing-break',
        name: 'No room after first break',
        totalSeconds: m(5),
        poseSeconds: m(25),
        shortBreakSeconds: m(5),
        cycleStart: CycleStart.breakFirst,
      ));
      expect(c.hasBlockingIssues, isTrue);
    });

    test('exactly fitted last pose has no incomplete choice prompt', () {
      final c = engine.generate(SessionRules(
        id: 'exact-fit',
        name: 'One complete pose',
        totalSeconds: m(25),
        poseSeconds: m(25),
      ));
      expectContiguous(c);
      expect(c.finalPoseIsShort, isFalse);
      expect(c.needsFinalPoseChoice, isFalse);
    });

    test('explicit custom split may exceed nominal pose length', () {
      final c = engine.generate(
          exampleA(), choice: FinalPoseChoice.custom(m(34)));
      expectContiguous(c);
      expect(c.phases[15].durationSeconds, m(34));
      expect(c.phases[17].durationSeconds, m(1));
      expect(c.needsSafetyAcknowledgement, isTrue);
    });

    test('20m posing then 3m rest then 6m posing warns', () {
      final c = engine.generate(SessionRules(
        id: 'cumulative-with-short-break',
        name: 'Short rest does not reset count',
        totalSeconds: m(29),
        poseSeconds: m(6),
        shortBreakSeconds: m(3),
        openingGroups: [
          OpeningPoseGroup(id: 'warmups', count: 4, poseSeconds: m(5)),
        ],
        cycleStart: CycleStart.breakFirst,
      ));
      expectContiguous(c);
      expect(c.needsSafetyAcknowledgement, isTrue);
    });
  });

  group('25m accumulated posing / 5m qualifying rest', () {
    test('a continuous 30m pose warns and must be acknowledged', () {
      final c = engine.generate(SessionRules(
        id: 'long-pose',
        name: 'Over 25',
        totalSeconds: m(30),
        poseSeconds: m(30),
      ));
      expectContiguous(c);
      expect(c.needsSafetyAcknowledgement, isTrue);
      expect(() => engine.approve(c, acknowledgeWarnings: false),
          throwsStateError);
      final approved = engine.approve(c, acknowledgeWarnings: true);
      expect(approved.acknowledgedWarningCodes, contains('rest_25_5'));
    });

    test('25m posing at the complete session end does not warn', () {
      final c = engine.generate(SessionRules(
        id: 'exact-25',
        name: 'Exactly 25',
        totalSeconds: m(25),
        poseSeconds: m(25),
      ));
      expectContiguous(c);
      expect(c.needsSafetyAcknowledgement, isFalse);
    });

    test('five consecutive 5m poses then 5m rest reset the counter', () {
      final c = engine.generate(SessionRules(
        id: 'five-poses',
        name: '25 accumulated',
        totalSeconds: m(55),
        openingGroups: [
          OpeningPoseGroup(id: 'five', count: 5, poseSeconds: m(5)),
        ],
        poseSeconds: m(25),
        cycleStart: CycleStart.breakFirst,
      ));
      expectContiguous(c);
      expect(c.needsSafetyAcknowledgement, isFalse);
    });

    test('a legal 3m break does not reset accumulated posing', () {
      final c = engine.generate(
        SessionRules(
          id: 'short-rest',
          name: '30 with 3m rest',
          totalSeconds: m(31),
          poseSeconds: m(25),
          shortBreakSeconds: m(3),
        ),
        choice: const FinalPoseChoice.remainder(),
      );
      expectContiguous(c);
      expect(c.needsSafetyAcknowledgement, isTrue);
    });
  });
}
