# Maki JavaScript Game

This directory contains a browser-based implementation of the Maki game and a simple in-memory score server.

## Running in the Browser

The HTML file relies on JavaScript files loaded over HTTP.
Opening `maki.html` directly from the filesystem with a `file://` URL will trigger browser security errors.

To play the game:

1. Start a simple web server from the repository root. One option using Python:
   ```bash
   python3 -m http.server
   ```
2. In your browser, navigate to `http://localhost:8000/maki/maki.html`.
3. Use the "New Game" and "Undo" buttons to control the game.

The score server keeps scores in memory for the duration of the browser session only.
