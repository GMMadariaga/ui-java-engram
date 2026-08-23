# ADR-003: No Direct SQLite Access

## Status

Accepted

## Context

Engram stores data in SQLite. Direct database access would create tight coupling.

## Decision

Engram Studio will NOT access Engram's SQLite database directly. All data access will go through the HTTP API.

## Consequences

- Studio remains independent of Engram's internal schema
- Updates to Engram don't break Studio
- Clear API contract
- No risk of corrupting Engram's data
- Consistent with local-first architecture

## Alternatives Considered

- Read-only SQLite access: Still couples to schema changes
- Replicated database: Unnecessary complexity