import 'package:atelier_time_core/atelier_time.dart';
import 'package:flutter/material.dart';

/// Stage 2: a responsive rules editor, chronological preview and explicit
/// in-memory approval. This does NOT persist/activate a saved preset.
class SessionEditorPage extends StatefulWidget {
  const SessionEditorPage({super.key});

  @override
  State<SessionEditorPage> createState() => _SessionEditorPageState();
}

class _OpeningFields {
  _OpeningFields(String groupId, {int count = 4, int minutes = 1})
      : id = groupId,
        count = TextEditingController(text: count.toString()),
        minutes = TextEditingController(text: minutes.toString());

  final String id;
  final TextEditingController count;
  final TextEditingController minutes;

  void dispose() {
    count.dispose();
    minutes.dispose();
  }
}

class _SessionEditorPageState extends State<SessionEditorPage> {
  final ScheduleEngine _engine = ScheduleEngine();
  final TextEditingController _name = TextEditingController(
    text: 'New atelier session',
  );
  final TextEditingController _manualOrdinals = TextEditingController(
    text: '3',
  );

  late final Map<String, TextEditingController> _numbers;
  final List<_OpeningFields> _openings = [];
  // Keep removed controllers alive until dispose, because widgets referencing
  // them are unmounted after the current frame.
  final List<_OpeningFields> _ownedGroups = [];

  CycleStart _cycle = CycleStart.poseFirst;
  bool _automaticPlacement = true;
  FinalPoseChoice? _endingChoice;
  ScheduleCandidate? _candidate;
  ApprovedSession? _approved;
  bool _warningAcknowledged = false;
  String? _parseIssue;
  int _nextGroupId = 1;

  @override
  void initState() {
    super.initState();
    _numbers = {
      'total': TextEditingController(text: '180'),
      'summary': TextEditingController(text: '15'),
      'pose': TextEditingController(text: '25'),
      'normal': TextEditingController(text: '5'),
      'special': TextEditingController(text: '15'),
      'count': TextEditingController(text: '1'),
    };
    _name.addListener(_rulesChanged);
    _manualOrdinals.addListener(_rulesChanged);
    for (final controller in _numbers.values) {
      controller.addListener(_rulesChanged);
    }
    _recompute();
  }

  @override
  void dispose() {
    _name.dispose();
    _manualOrdinals.dispose();
    for (final controller in _numbers.values) {
      controller.dispose();
    }
    for (final group in _ownedGroups) {
      group.dispose();
    }
    super.dispose();
  }

  /// Parse the editable text using the Flutter-independent adapter.
  /// This is also exercised by pure Dart tests on Android Termux.
  SessionRules _readRules() => SessionEditorDraft(
        id: 'current-preview',
        name: _name.text,
        totalMinutes: _numbers['total']!.text,
        summaryMinutes: _numbers['summary']!.text,
        poseMinutes: _numbers['pose']!.text,
        normalBreakMinutes: _numbers['normal']!.text,
        specialBreakMinutes: _numbers['special']!.text,
        specialBreakCount: _numbers['count']!.text,
        openingGroups: [
          for (final group in _openings)
            OpeningGroupDraft(
              id: group.id,
              count: group.count.text,
              minutes: group.minutes.text,
            ),
        ],
        cycleStart: _cycle,
        automaticPlacement: _automaticPlacement,
        manualBreakOrdinals: _manualOrdinals.text,
      ).toRules();

  void _recompute() {
    try {
      _candidate = _engine.generate(_readRules(), choice: _endingChoice);
      _parseIssue = null;
    } on FormatException catch (e) {
      _candidate = null;
      _parseIssue = e.message;
    } catch (e) {
      // Unexpected generation failures are visible rather than approving a
      // stale preview or quietly rewriting a user's inputs.
      _candidate = null;
      _parseIssue = 'Unable to generate this preview: ' + e.toString();
    }
  }

  void _edit(VoidCallback apply) {
    setState(() {
      apply();
      // Every edit invalidates the previously chosen ending, warning
      // acknowledgment and approval; no old preview is ever approvable.
      _endingChoice = null;
      _warningAcknowledged = false;
      _approved = null;
      _recompute();
    });
  }

  void _rulesChanged() => _edit(() {});

  void _setEnding(FinalPoseChoice choice) {
    setState(() {
      _endingChoice = choice;
      _warningAcknowledged = false;
      _approved = null;
      _recompute();
    });
  }

  void _addOpening() {
    _edit(() {
      final group = _OpeningFields('opening_' + (_nextGroupId++).toString());
      group.count.addListener(_rulesChanged);
      group.minutes.addListener(_rulesChanged);
      _ownedGroups.add(group);
      _openings.add(group);
    });
  }

  void _removeOpening(int index) {
    _edit(() => _openings.removeAt(index));
  }

  void _moveOpening(int index, int direction) {
    final target = index + direction;
    if (target < 0 || target >= _openings.length) return;
    _edit(() {
      final group = _openings.removeAt(index);
      _openings.insert(target, group);
    });
  }

  void _approve() {
    final candidate = _candidate;
    if (candidate == null || !candidate.canApprove) return;
    if (candidate.needsSafetyAcknowledgement &&
        !_warningAcknowledged) {
      return;
    }
    setState(() {
      _approved = _engine.approve(
        candidate,
        acknowledgeWarnings: _warningAcknowledged,
      );
    });
  }

  Widget _numberInput(String key, String label) {
    return SizedBox(
      width: 155,
      child: TextField(
        key: ValueKey('field_' + key),
        controller: _numbers[key],
        keyboardType: const TextInputType.numberWithOptions(signed: true),
        decoration: InputDecoration(labelText: label),
      ),
    );
  }

  Widget _sectionTitle(String label) {
    return Padding(
      padding: const EdgeInsets.only(top: 18, bottom: 10),
      child: Text(label, style: Theme.of(context).textTheme.titleMedium),
    );
  }

  Widget _editPanel() {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Session rules',
              style: Theme.of(context).textTheme.headlineSmall),
          const SizedBox(height: 4),
          const Text(
            'Each change regenerates the preview and clears prior approval.',
          ),
          _sectionTitle('Session'),
          TextField(
            key: const ValueKey('session_name'),
            controller: _name,
            decoration: const InputDecoration(labelText: 'Session name'),
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 10,
            runSpacing: 12,
            children: [
              _numberInput('total', 'Total minutes'),
              _numberInput('summary', 'Summary minutes'),
            ],
          ),
          _sectionTitle('Opening pose groups'),
          const Text('Optional ordered groups of short starting poses.'),
          for (var i = 0; i < _openings.length; i++)
            Card(
              key: ValueKey(_openings[i].id),
              child: Padding(
                padding: const EdgeInsets.all(8),
                child: Column(
                  children: [
                    Row(
                      children: [
                        Expanded(child: Text('Group ' + (i + 1).toString())),
                        IconButton(
                          tooltip: 'Move group up',
                          onPressed:
                              i == 0 ? null : () => _moveOpening(i, -1),
                          icon: const Icon(Icons.arrow_upward),
                        ),
                        IconButton(
                          tooltip: 'Move group down',
                          onPressed: i == _openings.length - 1
                              ? null
                              : () => _moveOpening(i, 1),
                          icon: const Icon(Icons.arrow_downward),
                        ),
                        IconButton(
                          tooltip: 'Remove group',
                          onPressed: () => _removeOpening(i),
                          icon: const Icon(Icons.delete_outline),
                        ),
                      ],
                    ),
                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: [
                        SizedBox(
                          width: 120,
                          child: TextField(
                            key: ValueKey('opening_count_' + _openings[i].id),
                            controller: _openings[i].count,
                            keyboardType: TextInputType.number,
                            decoration: const InputDecoration(
                              labelText: 'Pose count',
                            ),
                          ),
                        ),
                        SizedBox(
                          width: 135,
                          child: TextField(
                            key: ValueKey('opening_minutes_' + _openings[i].id),
                            controller: _openings[i].minutes,
                            keyboardType: TextInputType.number,
                            decoration: const InputDecoration(
                              labelText: 'Minutes each',
                            ),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          OutlinedButton.icon(
            key: const ValueKey('add_opening'),
            onPressed: _addOpening,
            icon: const Icon(Icons.add),
            label: const Text('Add opening group'),
          ),
          _sectionTitle('Repeating poses and breaks'),
          Wrap(
            spacing: 10,
            runSpacing: 12,
            children: [
              _numberInput('pose', 'Pose minutes'),
              _numberInput('normal', 'Normal break min'),
              _numberInput('special', 'Special break min'),
              _numberInput('count', 'Special break count'),
            ],
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<CycleStart>(
            key: const ValueKey('cycle_order'),
            value: _cycle,
            decoration: const InputDecoration(labelText: 'Regular cycle starts'),
            items: const [
              DropdownMenuItem(
                value: CycleStart.poseFirst,
                child: Text('Pose first'),
              ),
              DropdownMenuItem(
                value: CycleStart.breakFirst,
                child: Text('Break first'),
              ),
            ],
            onChanged: (value) {
              if (value != null) {
                _edit(() => _cycle = value);
              }
            },
          ),
          _sectionTitle('Special break placement'),
          SwitchListTile(
            key: const ValueKey('automatic_placement'),
            contentPadding: EdgeInsets.zero,
            title: const Text('Automatic placement'),
            subtitle: const Text(
              'Closest to midpoint, or evenly across the session',
            ),
            value: _automaticPlacement,
            onChanged: (value) =>
                _edit(() => _automaticPlacement = value),
          ),
          if (!_automaticPlacement)
            TextField(
              key: const ValueKey('manual_ordinals'),
              controller: _manualOrdinals,
              keyboardType: TextInputType.text,
              decoration: const InputDecoration(
                labelText: 'Normal break ordinals to replace',
                helperText: 'Comma-separated, for example 2,4',
              ),
            ),
          const SizedBox(height: 16),
          const Text(
            'Valid session totals and active break durations must be positive. '
            'Special breaks may be shorter than normal breaks. '
            'Safety warnings require separate acknowledgement.',
          ),
        ],
      ),
    );
  }

  String _clock(int seconds) {
    final minutes = seconds ~/ secondsPerMinute;
    final hours = (minutes ~/ 60).toString().padLeft(2, '0');
    final remaining = (minutes % 60).toString().padLeft(2, '0');
    return hours + ':' + remaining;
  }

  String _phaseLabel(SchedulePhase phase) {
    switch (phase.kind) {
      case PhaseKind.pose:
        return phase.poseSource == PoseSource.opening
            ? 'Opening pose ' + phase.poseNumber.toString()
            : 'Pose ' + phase.poseNumber.toString();
      case PhaseKind.shortBreak:
        return 'Normal break #' + phase.breakOrdinal.toString();
      case PhaseKind.specialBreak:
        return 'Special break #' + phase.breakOrdinal.toString();
      case PhaseKind.summary:
        return 'Final summary';
    }
  }

  int _totalDuration(List<SchedulePhase> phases, PhaseKind kind) =>
      phases.where((p) => p.kind == kind).fold<int>(
            0,
            (total, p) => total + p.durationSeconds,
          );

  Widget _choiceChip(
      String label, FinalPoseMode mode, FinalPoseChoice choice,
      {bool enabled = true}) {
    return ChoiceChip(
      key: ValueKey('end_' + mode.name),
      label: Text(label),
      selected: _endingChoice?.mode == mode,
      onSelected: enabled ? (_) => _setEnding(choice) : null,
    );
  }

  Widget _endingOptions(ScheduleCandidate candidate) {
    if (!candidate.finalPoseIsShort) {
      return const SizedBox.shrink();
    }

    final regularPoses = candidate.phases
        .where((p) =>
            p.kind == PhaseKind.pose &&
            p.poseSource == PoseSource.regular)
        .toList();
    int availableMinutes = 0;
    if (regularPoses.length >= 2) {
      availableMinutes =
          (regularPoses[regularPoses.length - 2].durationSeconds +
                  regularPoses.last.durationSeconds) ~/
              secondsPerMinute;
    }
    final maxFirst = availableMinutes - 1;
    final customMinutes = _endingChoice?.mode == FinalPoseMode.customSplit
        ? (_endingChoice!.firstPoseSeconds! ~/ secondsPerMinute)
        : (availableMinutes ~/ 2);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _sectionTitle('Resolve shortened final pose'),
        const Text(
          'Choose one ending. Earlier phases and the intervening break '
          'retain their total allocated time.',
        ),
        const SizedBox(height: 10),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            _choiceChip('Keep remainder', FinalPoseMode.remainder,
                const FinalPoseChoice.remainder()),
            _choiceChip('Near equal', FinalPoseMode.evenSplit,
                const FinalPoseChoice.even(),
                enabled: candidate.canSplitFinalTwo),
            _choiceChip('Custom slider', FinalPoseMode.customSplit,
                FinalPoseChoice.custom(
                    (availableMinutes ~/ 2) * secondsPerMinute),
                enabled: candidate.canSplitFinalTwo),
          ],
        ),
        if (!candidate.canSplitFinalTwo)
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 8),
            child: Text('No eligible pair of positive regular poses with '
                'an existing positive break is available for splitting.'),
          ),
        if (candidate.canSplitFinalTwo &&
            _endingChoice?.mode == FinalPoseMode.customSplit &&
            maxFirst >= 2) ...[
          const SizedBox(height: 10),
          Text('First pose: ' + customMinutes.toString() +
              ' min; final pose: ' +
              (availableMinutes - customMinutes).toString() + ' min'),
          Slider(
            key: const ValueKey('split_slider'),
            min: 1,
            max: maxFirst.toDouble(),
            divisions: maxFirst - 1,
            label: customMinutes.toString() + ' min',
            value: customMinutes.clamp(1, maxFirst).toDouble(),
            onChanged: (value) => _setEnding(
              FinalPoseChoice.custom(value.round() * secondsPerMinute),
            ),
          ),
        ],
        if (_endingChoice == null)
          const Padding(
            padding: EdgeInsets.only(top: 8),
            child: Text('Select an ending before approval.'),
          ),
      ],
    );
  }

  Widget _previewPanel() {
    final candidate = _candidate;
    if (candidate == null) {
      return Padding(
        padding: const EdgeInsets.all(16),
        child: Card(
          child: ListTile(
            leading: const Icon(Icons.error_outline),
            title: const Text('Blocking input error'),
            subtitle: Text(_parseIssue ??
                'Enter valid whole-minute rules to preview a schedule.'),
          ),
        ),
      );
    }

    final errors = candidate.issues
        .where((issue) => issue.severity == IssueSeverity.error)
        .toList();
    final warnings = candidate.issues
        .where((issue) => issue.severity == IssueSeverity.warning)
        .toList();
    final poseTotal = _totalDuration(candidate.phases, PhaseKind.pose);
    final shortRest = _totalDuration(candidate.phases, PhaseKind.shortBreak);
    final specialRest = _totalDuration(candidate.phases, PhaseKind.specialBreak);
    final summary = _totalDuration(candidate.phases, PhaseKind.summary);
    final canApprove = candidate.canApprove &&
        (warnings.isEmpty || _warningAcknowledged);

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Live schedule preview',
              style: Theme.of(context).textTheme.headlineSmall),
          const SizedBox(height: 8),
          Text('Posing ' + _clock(poseTotal) +
              '  ·  Rest ' + _clock(shortRest + specialRest) +
              '  ·  Summary ' + _clock(summary)),
          const SizedBox(height: 8),
          for (final issue in errors)
            Card(
              child: ListTile(
                leading: const Icon(Icons.error_outline),
                title: const Text('Blocking validation error'),
                subtitle: Text(issue.message),
              ),
            ),
          for (final issue in warnings)
            Card(
              child: ListTile(
                leading: const Icon(Icons.warning_amber_outlined),
                title: const Text('25/5 rest recommendation'),
                subtitle: Text(issue.message +
                    (issue.offsetSeconds == null
                        ? ''
                        : ' At elapsed ' +
                            _clock(issue.offsetSeconds!) +
                            '.')),
              ),
            ),
          if (!candidate.hasBlockingIssues) _endingOptions(candidate),
          _sectionTitle('All scheduled phases'),
          if (candidate.hasBlockingIssues)
            const Text('The inputs are blocked. No schedule can be approved.'),
          if (!candidate.hasBlockingIssues && candidate.phases.isEmpty)
            const Text('No valid timeline can be generated with these inputs.'),
          for (final phase in candidate.hasBlockingIssues
              ? const <SchedulePhase>[]
              : candidate.phases)
            ListTile(
              dense: true,
              contentPadding: EdgeInsets.zero,
              title: Text(_phaseLabel(phase)),
              subtitle: Text(
                _clock(phase.startSeconds) + ' – ' +
                    _clock(phase.endSeconds),
              ),
              trailing: Text(_clock(phase.durationSeconds)),
            ),
          if (warnings.isNotEmpty)
            CheckboxListTile(
              key: const ValueKey('rest_acknowledgement'),
              contentPadding: EdgeInsets.zero,
              value: _warningAcknowledged,
              onChanged: (accepted) {
                setState(() {
                  _warningAcknowledged = accepted ?? false;
                  _approved = null;
                });
              },
              title: const Text('I acknowledge the 25/5 rest warning(s)'),
              subtitle: const Text(
                'Acknowledgement is separate from final approval.',
              ),
            ),
          const SizedBox(height: 12),
          FilledButton.icon(
            key: const ValueKey('approve_schedule'),
            onPressed: canApprove ? _approve : null,
            icon: const Icon(Icons.check_circle_outline),
            label: const Text('Approve this exact timeline'),
          ),
          if (_approved != null)
            const Padding(
              padding: EdgeInsets.only(top: 12),
              child: Text(
                'Approved for this preview only. Saving and activating '
                'presets will be added in Stage 3.',
              ),
            ),
          const SizedBox(height: 10),
          const Text(
            'Prototype: editing invalidates approval. No session is '
            'saved, activated or timed by this screen.',
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Atelier Time · Session editor')),
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) {
            if (constraints.maxWidth >= 850) {
              return Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  SizedBox(
                    width: 430,
                    child: SingleChildScrollView(child: _editPanel()),
                  ),
                  const VerticalDivider(width: 1),
                  Expanded(
                    child: SingleChildScrollView(child: _previewPanel()),
                  ),
                ],
              );
            }
            // Keep the editor and full preview mounted so invalidation
            // feedback and ending controls remain reachable by scrolling.
            // Later large-timeline optimization can virtualize phase rows.
            return SingleChildScrollView(
              child: Column(
                children: [_editPanel(), const Divider(), _previewPanel()],
              ),
            );
          },
        ),
      ),
    );
  }
}
