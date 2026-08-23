# ADR-001: JavaFX for UI

## Status

Accepted

## Context

Engram Studio needs a desktop UI framework for Windows. The application should be professional, engineering-oriented, and support CSS styling.

## Decision

We will use JavaFX 25 as the UI framework.

## Consequences

- Native desktop application
- CSS-based styling for dark theme
- Good Windows support
- Java 25 compatibility
- Rich component library
- FXML for layout separation

## Alternatives Considered

- Swing: Outdated, less maintainable
- SWT: Platform-specific, less portable
- Electron: Higher resource usage, not native Java