# ADR-004: Local-First Architecture

## Status

Accepted

## Context

Engram Studio should work primarily with local Engram installations, with potential for cloud support in the future.

## Decision

Studio will be local-first, connecting to local Engram instances. Cloud support will be added later as an optional data source.

## Consequences

- No internet required for core functionality
- Fast response times
- Privacy-friendly
- Simple deployment
- Future cloud support through repository pattern

## Alternatives Considered

- Cloud-first: Would require internet, latency issues
- Hybrid: Unnecessary complexity for MVP