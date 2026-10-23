import 'package:atelier_time_app/main.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

/// Stage 2 T19–T23 widget tests. These are SOURCE ONLY until a Flutter SDK
/// is available. The Android Termux Dart core pass does not run this file.
Future<void> editMinutes(
  WidgetTester tester,
  String field,
  String value,
) async {
  final target = find.byKey(ValueKey<String>('field_$field'));
  await tester.ensureVisible(target);
  await tester.enterText(target, value);
  await tester.pumpAndSettle();
}

FilledButton approvalButton(WidgetTester tester) => tester.widget<FilledButton>(
      find.byKey(const ValueKey<String>('approve_schedule')),
    );

void main() {
  testWidgets('T19: input errors block approval and hide incomplete timeline',
      (tester) async {
    await tester.pumpWidget(const AtelierTimeApp());

    // Example B defaults to an unfinished pose; choice must be explicit.
    expect(approvalButton(tester).onPressed, isNull);

    await editMinutes(tester, 'total', '0');
    expect(find.text('Blocking validation error'), findsWidgets);
    expect(find.text('The inputs are blocked. No schedule can be approved.'),
        findsOneWidget);
    expect(approvalButton(tester).onPressed, isNull);

    // Reject text input rather than silently treating it as 0 or default.
    await editMinutes(tester, 'total', 'not a number');
    expect(find.text('Blocking input error'), findsOneWidget);

    await editMinutes(tester, 'total', '180');
    expect(find.text('Blocking input error'), findsNothing);
    expect(approvalButton(tester).onPressed, isNull);
  });

  testWidgets('T19: opening groups are dynamically editable and removable',
      (tester) async {
    await tester.pumpWidget(const AtelierTimeApp());
    final add = find.byKey(const ValueKey<String>('add_opening'));
    await tester.ensureVisible(add);
    await tester.tap(add);
    await tester.pumpAndSettle();

    final count = find.byKey(const ValueKey<String>('opening_count_opening_1'));
    final duration =
        find.byKey(const ValueKey<String>('opening_minutes_opening_1'));
    expect(count, findsOneWidget);
    expect(duration, findsOneWidget);

    await tester.ensureVisible(count);
    await tester.enterText(count, '2');
    await tester.enterText(duration, '5');
    await tester.pumpAndSettle();

    // Removal returns the editable form to its initial group count.
    final remove = find.byTooltip('Remove group');
    await tester.ensureVisible(remove);
    await tester.tap(remove);
    await tester.pumpAndSettle();
    expect(count, findsNothing);
  });

  testWidgets('T21/T22: last-pose choice is required and edits clear approval',
      (tester) async {
    await tester.pumpWidget(const AtelierTimeApp());
    final remainder =
        find.byKey(const ValueKey<String>('end_remainder'));
    await tester.ensureVisible(remainder);
    await tester.tap(remainder);
    await tester.pumpAndSettle();
    expect(approvalButton(tester).onPressed, isNotNull);

    final approve =
        find.byKey(const ValueKey<String>('approve_schedule'));
    await tester.ensureVisible(approve);
    await tester.tap(approve);
    await tester.pumpAndSettle();
    expect(find.textContaining('Approved for this preview only'),
        findsOneWidget);

    // Even a valid edit is a fresh schedule revision, not the old approval.
    await editMinutes(tester, 'total', '181');
    expect(find.textContaining('Approved for this preview only'), findsNothing);
    expect(approvalButton(tester).onPressed, isNull);
  });

  testWidgets('T21: both even and custom split controls can be selected',
      (tester) async {
    await tester.pumpWidget(const AtelierTimeApp());
    final even = find.byKey(const ValueKey<String>('end_evenSplit'));
    await tester.ensureVisible(even);
    await tester.tap(even);
    await tester.pumpAndSettle();
    expect(approvalButton(tester).onPressed, isNotNull);

    final custom = find.byKey(const ValueKey<String>('end_customSplit'));
    await tester.ensureVisible(custom);
    await tester.tap(custom);
    await tester.pumpAndSettle();
    expect(find.byKey(const ValueKey<String>('split_slider')),
        findsOneWidget);
  });

  testWidgets('T22: short positive rest requires separate acknowledgement',
      (tester) async {
    await tester.pumpWidget(const AtelierTimeApp());

    // 25m pose -> 1m rest -> 25m pose is mathematically valid but the 1m
    // rest cannot reset the accumulated posing counter (D-031/D-044).
    await editMinutes(tester, 'count', '0');
    await editMinutes(tester, 'normal', '1');
    await editMinutes(tester, 'total', '51');

    expect(find.text('25/5 rest recommendation'), findsWidgets);
    expect(approvalButton(tester).onPressed, isNull);
    final check =
        find.byKey(const ValueKey<String>('rest_acknowledgement'));
    await tester.ensureVisible(check);
    await tester.tap(check);
    await tester.pumpAndSettle();
    expect(approvalButton(tester).onPressed, isNotNull);

    final approve =
        find.byKey(const ValueKey<String>('approve_schedule'));
    await tester.ensureVisible(approve);
    await tester.tap(approve);
    await tester.pumpAndSettle();
    expect(find.textContaining('Approved for this preview only'),
        findsOneWidget);

    await editMinutes(tester, 'normal', '3');
    expect(find.textContaining('Approved for this preview only'), findsNothing);
    expect(approvalButton(tester).onPressed, isNull);
  });

  testWidgets('T23: wide viewport uses a two-column editor and preview',
      (tester) async {
    await tester.binding.setSurfaceSize(const Size(1120, 900));
    addTearDown(() async => tester.binding.setSurfaceSize(null));
    await tester.pumpWidget(const AtelierTimeApp());

    expect(find.byType(VerticalDivider), findsOneWidget);
    expect(find.text('Session rules'), findsOneWidget);
    expect(find.text('Live schedule preview'), findsOneWidget);
  });
}
