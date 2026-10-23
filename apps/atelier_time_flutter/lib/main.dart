import 'package:flutter/material.dart';

import 'session_editor_page.dart';

void main() {
  runApp(const AtelierTimeApp());
}

/// Provisional, neutral Stage 2 visual shell; not final branding.
class AtelierTimeApp extends StatelessWidget {
  const AtelierTimeApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Atelier Time',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorSchemeSeed: Colors.blueGrey,
        inputDecorationTheme: const InputDecorationTheme(
          border: OutlineInputBorder(),
        ),
      ),
      home: const SessionEditorPage(),
    );
  }
}
