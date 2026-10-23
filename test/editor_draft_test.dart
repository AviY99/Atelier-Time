import 'package:atelier_time_core/atelier_time.dart';
import 'package:test/test.dart';

int minutes(int value) => value * secondsPerMinute;

SessionEditorDraft draft({
  String name = ' My atelier ',
  String total = '180',
  String summary = '15',
  String pose = '25',
  String normal = '5',
  String special = '15',
  String count = '1',
  bool automatic = true,
  String ordinals = '3',
  CycleStart cycleStart = CycleStart.poseFirst,
  List<OpeningGroupDraft> openingGroups = const [],
}) =>
    SessionEditorDraft(
      id: 'form-preview',
      name: name,
      totalMinutes: total,
      summaryMinutes: summary,
      poseMinutes: pose,
      normalBreakMinutes: normal,
      specialBreakMinutes: special,
      specialBreakCount: count,
      automaticPlacement: automatic,
      manualBreakOrdinals: ordinals,
      cycleStart: cycleStart,
      openingGroups: openingGroups,
    );

void main() {
  final engine = ScheduleEngine();

  test('T19: editor parses defaults into exact approved Example B rules', () {
    final rules = draft().toRules();
    expect(rules.name, 'My atelier');
    expect(rules.totalSeconds, minutes(180));
    expect(rules.summarySeconds, minutes(15));
    expect(rules.poseSeconds, minutes(25));
    expect(rules.shortBreakSeconds, minutes(5));
    expect(rules.specialBreakSeconds, minutes(15));
    expect(rules.specialBreakCount, 1);
    expect(rules.cycleStart, CycleStart.poseFirst);
    final candidate = engine.generate(
      rules,
      choice: const FinalPoseChoice.remainder(),
    );
    expect(candidate.hasBlockingIssues, isFalse);
    final special = candidate.phases.singleWhere(
        (phase) => phase.kind == PhaseKind.specialBreak);
    expect(special.startSeconds, minutes(85));
    expect(special.endSeconds, minutes(100));
  });

  test('T19: changing name and minute fields is not silently corrected', () {
    final rules = draft(
      name: ' Sculpture ',
      total: '90',
      summary: '0',
      pose: '12',
      normal: '3',
      special: '1',
      count: '0',
    ).toRules();
    expect(rules.name, 'Sculpture');
    expect(rules.totalSeconds, minutes(90));
    expect(rules.summarySeconds, 0);
    expect(rules.poseSeconds, minutes(12));
    expect(rules.shortBreakSeconds, minutes(3));
    expect(rules.specialBreakSeconds, minutes(1));
    expect(rules.specialBreakCount, 0);
    expect(rules.workSeconds, minutes(90));
  });

  test('T19: zero full session is parsed then blocked by engine', () {
    final rules = draft(total: '0').toRules();
    final candidate = engine.generate(rules);
    expect(candidate.hasBlockingIssues, isTrue);
    expect(candidate.issues.any((i) => i.code == 'nonpositive_session'),
        isTrue);
  });

  test('T19: zero configured break is parsed then blocked by engine', () {
    final rules = draft(normal: '0', special: '0').toRules();
    final candidate = engine.generate(rules);
    expect(candidate.hasBlockingIssues, isTrue);
    expect(candidate.issues.any((i) => i.code == 'nonpositive_break'), isTrue);
  });

  test('T19: malformed numbers and empty session name do not become 0', () {
    expect(() => draft(total: '1.5').toRules(), throwsFormatException);
    expect(() => draft(total: '').toRules(), throwsFormatException);
    expect(() => draft(total: 'abc').toRules(), throwsFormatException);
    expect(() => draft(name: '   ').toRules(), throwsFormatException);
    expect(() => draft(count: 'two').toRules(), throwsFormatException);
  });

  test('T19: ordered opening group data passes through unchanged', () {
    final rules = draft(
      cycleStart: CycleStart.breakFirst,
      openingGroups: const [
        OpeningGroupDraft(id: 'first', count: '4', minutes: '1'),
        OpeningGroupDraft(id: 'second', count: '4', minutes: '4'),
      ],
    ).toRules();
    expect(rules.cycleStart, CycleStart.breakFirst);
    expect(rules.openingGroups.map((g) => g.id).toList(),
        ['first', 'second']);
    expect(rules.openingGroups.map((g) => g.count).toList(), [4, 4]);
    expect(rules.openingGroups.map((g) => g.poseSeconds).toList(),
        [minutes(1), minutes(4)]);
    final phases = engine
        .generate(rules, choice: const FinalPoseChoice.remainder())
        .phases;
    expect(phases.first.openingGroupId, 'first');
    expect(phases[4].openingGroupId, 'second');
  });

  test('T19: malformed opening group count blocks before generating', () {
    expect(
      () => draft(openingGroups: const [
        OpeningGroupDraft(id: 'bad', count: 'four', minutes: '1'),
      ]).toRules(),
      throwsFormatException,
    );
  });

  test('T19: manual special break ordinals preserve selection order', () {
    final rules = draft(
      count: '2',
      automatic: false,
      ordinals: ' 4 , 2 ',
    ).toRules();
    expect(rules.placement.automatic, isFalse);
    expect(rules.placement.breakOrdinals, [4, 2]);
    expect(
      () => rules.placement.breakOrdinals.clear(),
      throwsUnsupportedError,
    );
  });

  test('T19: malformed active manual ordinal blocks', () {
    expect(() => draft(automatic: false, ordinals: 'x').toRules(),
        throwsFormatException);
  });

  test('T19: inactive ordinals ignored when no special breaks requested', () {
    final rules = draft(
      count: '0',
      automatic: false,
      ordinals: 'unused bad text',
    ).toRules();
    expect(rules.specialBreakCount, 0);
    expect(engine.generate(rules).issues
        .any((issue) => issue.code == 'invalid_manual_ordinals'), isFalse);
  });

  test('D-044: positive shorter special break remains allowed', () {
    final rules = draft(
      total: '53',
      summary: '0',
      count: '1',
      normal: '5',
      special: '3',
    ).toRules();
    final candidate = engine.generate(rules);
    expect(candidate.hasBlockingIssues, isFalse);
    expect(candidate.needsSafetyAcknowledgement, isTrue);
  });
}
