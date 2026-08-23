# ui-java-engram

JavaFX desktop client for visualizing and administering Engram memory through its HTTP API.

## Features

- Dashboard with memory, session, and prompt statistics
- Memory search and observation filters
- Session, project, prompt, and relation views
- Single-click detail panels in the main window
- `OPEN` buttons for full detail windows without covering the Windows taskbar
- Markdown rendering for memory content
- Engram connection, diagnostics, process, and setup controls
- Full-size embedded interactive Windows PowerShell console backed by pty4j (ConPTY on Windows) and xterm.js rendering
- Quick tabs for `engram tui`, Claude, Codex, OpenCode, Antigravity (`agy`), and custom commands
- Collapsible navigation sidebar (expanded labels or icon-only mode)

## Requirements

- Java 25
- Maven 3.9.x
- Engram available at `http://127.0.0.1:7437`; Studio can auto-start the local executable when enabled in Settings

The application does not access Engram's SQLite database directly. All data is read through the Engram HTTP API.

## Build

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-25.0.4.101-hotspot"
$env:MAVEN_HOME = "C:\Users\gmadariaga\maven\apache-maven-3.9.16"
& "$env:MAVEN_HOME\bin\mvn.cmd" clean verify
```

## Run

```powershell
.\run.ps1
```

Or run directly with Maven:

```powershell
mvn javafx:run
```

Agent executable paths can be configured from **Settings → Agent executables**.
The values are stored in:

```text
%USERPROFILE%\.engram-studio\engram-studio.properties
```

The keys include `agent.claude.executable`, `agent.opencode.executable`,
`agent.codex.executable`, and `agent.agy.executable`. **FIND** searches PATH; **BROWSE** accepts `.exe`,
`.cmd`, `.bat`, or `.ps1` files.

## Architecture

- JavaFX presentation layer
- Application use cases
- Domain models and repository ports
- Infrastructure adapters for the Engram HTTP API
- No direct SQLite access
- The embedded PowerShell session uses a native PTY; on Windows pty4j requests ConPTY and falls back to WinPTY when unavailable. Bundled xterm.js renders ANSI, cursor movement, colors, and UTF-8 in the JavaFX WebView without depending on a CDN or browser cache.

## Project Layout

```text
src/main/java/com/speed/engramstudio/
  application/       Use cases
  bootstrap/         Application wiring
  domain/            Domain models and ports
  infrastructure/    HTTP, configuration, mapping, and process adapters
  presentation/     JavaFX views and view models
```

## License

No license has been specified yet.
