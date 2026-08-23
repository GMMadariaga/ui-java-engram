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

## Requirements

- Java 25
- Maven 3.9.x
- Engram running at `http://127.0.0.1:7437`

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

## Architecture

- JavaFX presentation layer
- Application use cases
- Domain models and repository ports
- Infrastructure adapters for the Engram HTTP API
- No direct SQLite access

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
