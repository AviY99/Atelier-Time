import 'dart:io';

/// Tiny Termux localhost server; no Node, Python or Flutter needed.
/// Run from spikes/android_dart_web/ after generating www/main.dart.js.
Future<void> main() async {
  final server = await HttpServer.bind(InternetAddress.loopbackIPv4, 8765);
  stdout.writeln(
      'Atelier Time proof: http://127.0.0.1:8765 (Ctrl+C to stop)');
  await for (final request in server) {
    final path =
        request.uri.path == '/' ? 'index.html' : request.uri.path.substring(1);
    // No traversal: this server is strictly for local test assets.
    if (path.contains('..') || path.contains('\\')) {
      request.response.statusCode = HttpStatus.badRequest;
      await request.response.close();
      continue;
    }
    final file = File('www/' + path);
    if (!await file.exists()) {
      request.response.statusCode = HttpStatus.notFound;
      await request.response.close();
      continue;
    }
    if (path.endsWith('.js')) {
      request.response.headers.contentType =
          ContentType('application', 'javascript', charset: 'utf-8');
    } else if (path.endsWith('.html')) {
      request.response.headers.contentType = ContentType.html;
    }
    await request.response.addStream(file.openRead());
    await request.response.close();
  }
}
