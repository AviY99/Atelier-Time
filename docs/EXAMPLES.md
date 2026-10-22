# Approved reference schedules — 3-hour sessions

All times are elapsed from start, not wall-clock times. The schedules below are reference cases for Stage 1 implementation and regression tests.

## Example A: opening short poses, then 5-minute break / 25-minute pose

Rules: 180-minute total, final 15-minute summary, opening 4×1-minute poses followed by 4×4-minute poses, then (5-minute break, 25-minute pose) recurring. The 15-minute long break **replaces** the short break in the middle. Base schedule assumes option 1 (keep remaining duration for last pose).

| Time | Activity | Minutes |
|---|---|---:|
| 00:00–00:01 | Pose 1 | 1 |
| 00:01–00:02 | Pose 2 | 1 |
| 00:02–00:03 | Pose 3 | 1 |
| 00:03–00:04 | Pose 4 | 1 |
| 00:04–00:08 | Pose 5 | 4 |
| 00:08–00:12 | Pose 6 | 4 |
| 00:12–00:16 | Pose 7 | 4 |
| 00:16–00:20 | Pose 8 | 4 |
| 00:20–00:25 | Short break | 5 |
| 00:25–00:50 | Pose 9 | 25 |
| 00:50–00:55 | Short break | 5 |
| 00:55–01:20 | Pose 10 | 25 |
| 01:20–01:35 | Long break | 15 |
| 01:35–02:00 | Pose 11 | 25 |
| 02:00–02:05 | Short break | 5 |
| 02:05–02:30 | Pose 12 | 25 |
| 02:30–02:35 | Short break | 5 |
| 02:35–02:45 | Pose 13 | 10 |
| 02:45–03:00 | Final summary | 15 |

**Totals:** 130 posing + 35 break + 15 summary = 180 minutes.

### Example A — last-two choices
The prefix through 02:05 is unchanged. The last pair has 35 pose-minutes and the between-pose break is always 5 minutes.

| Choice | Pose 12 | Break | Pose 13 | Summary |
|---|---|---|---|---|
| 1. Remainder | 02:05–02:30 (25m) | 02:30–02:35 | 02:35–02:45 (10m) | 02:45–03:00 |
| 2. Even | 02:05–02:22 (17m) | 02:22–02:27 | 02:27–02:45 (18m) | 02:45–03:00 |
| 3. Custom example | 02:05–02:25 (20m) | 02:25–02:30 | 02:30–02:45 (15m) | 02:45–03:00 |

## Example B: 25-minute pose / 5-minute break

Rules: 180-minute total, final 15-minute summary, repeated 25-minute pose followed by 5-minute break, one 15-minute long break **replaces** a short break in the middle. Base schedule assumes option 1 (remainder).

| Time | Activity | Minutes |
|---|---|---:|
| 00:00–00:25 | Pose 1 | 25 |
| 00:25–00:30 | Short break | 5 |
| 00:30–00:55 | Pose 2 | 25 |
| 00:55–01:00 | Short break | 5 |
| 01:00–01:25 | Pose 3 | 25 |
| 01:25–01:40 | Long break | 15 |
| 01:40–02:05 | Pose 4 | 25 |
| 02:05–02:10 | Short break | 5 |
| 02:10–02:35 | Pose 5 | 25 |
| 02:35–02:40 | Short break | 5 |
| 02:40–02:45 | Pose 6 | 5 |
| 02:45–03:00 | Final summary | 15 |

**Totals:** 130 posing + 35 break + 15 summary = 180 minutes.

### Example B — last-two choices
The prefix through 02:10 is unchanged. The last pair has 30 pose-minutes and 5 break-minutes.

| Choice | Pose 5 | Break | Pose 6 | Summary |
|---|---|---|---|---|
| 1. Remainder | 02:10–02:35 (25m) | 02:35–02:40 | 02:40–02:45 (5m) | 02:45–03:00 |
| 2. Even | 02:10–02:25 (15m) | 02:25–02:30 | 02:30–02:45 (15m) | 02:45–03:00 |
| 3. Custom example | 02:10–02:28 (18m) | 02:28–02:33 | 02:33–02:45 (12m) | 02:45–03:00 |

Custom numerical splits above are illustrations of the slider behavior, not preselected defaults. In all alternatives total length and summary remain unchanged.
