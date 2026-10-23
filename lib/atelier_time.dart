/// Flutter-independent scheduling API used by the future Atelier Time app.
///
/// Flutter UI, offline persistence, live timer and audio belong to later stages.
library atelier_time;

export 'src/scheduling.dart';

// Stage 2 text-to-rules form adapter (no Flutter dependency).
export 'src/editor_draft.dart';
