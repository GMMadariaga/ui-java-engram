# ADR-002: HTTP API Integration with Engram

## Status

Accepted

## Context

Engram Studio needs to communicate with the Engram memory system. Engram exposes a local HTTP API.

## Decision

We will integrate with Engram via its HTTP API at http://127.0.0.1:7437.

## Consequences

- Clean separation between Studio and Engram
- No direct database access
- Version-compatible integration
- Async HTTP calls using java.net.http.HttpClient
- JSON serialization with Jackson

## Alternatives Considered

- Direct SQLite access: Would couple Studio to Engram internals
- gRPC: More complex, not available in Engram
- File system: Not reliable, no real-time updates