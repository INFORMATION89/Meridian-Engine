# ADR-0004: Keep the Storage SPI internal in v1

- **Status:** Accepted
- **Date:** 2026-11-27
- **Decision owner:** Meridian project owner
- **Scope:** M0.2 and v1 storage boundary

## Context

Meridian must remain an independent, local-first weather engine whose public weather API is usable without persistent storage. The M0.2 prototype needs an internal storage abstraction to isolate cache/history behaviour from domain logic and to test deterministic no-storage and in-memory paths.

Publishing a host-implemented storage SPI in v1 would create a supported compatibility surface before Meridian has established its transaction, concurrency, cancellation, retention, deletion, migration, corruption-recovery, and cache-identity guarantees. It would also force the project to support and test arbitrary consumer adapters across Android, iOS/Swift, and JVM desktop.

The project already has an approved optional SQLDelight/SQLite technology baseline (ADR-0003). That approval does not require exposing storage implementation details to consumers.

## Decision

For v1, **the Storage SPI is internal to Meridian**.

1. The public weather API does not expose storage interfaces, cache keys, database records, SQLDelight types, SQLite types, drivers, file paths, or storage lifecycle methods.
2. Consumers cannot inject or implement custom storage adapters through the v1 public API.
3. Meridian owns internal storage interfaces, implementations, cache identity, schema/versioning, migrations, transaction boundaries, concurrency, cancellation, retention, deletion, and recovery semantics.
4. Persistent storage remains optional. The engine must support a no-persistence configuration.
5. An internal in-memory store may be used for deterministic tests and explicitly ephemeral operation.
6. An optional official SQLDelight/SQLite module may provide local persistence behind the internal contract. It is not a mandatory dependency of the public engine API.
7. The public weather API must not change meaning based on whether persistence is enabled or which official implementation is selected.

## Consequences

### Benefits

- Keeps the supported public API smaller and more stable.
- Avoids premature compatibility commitments for storage semantics that are not yet validated.
- Lets Meridian evolve internal cache/archive models and database migrations without breaking consumer implementations.
- Keeps transaction, cancellation, privacy, and retention guarantees under one implementation authority.
- Preserves offline/no-storage operation without requiring a host database.

### Costs and limitations

- Consumers cannot integrate a custom database directly into Meridian's storage layer in v1.
- Consumers needing custom persistence must keep that data outside Meridian or wait for a separately approved extension point.
- Meridian must maintain and test its own official persistence implementation if/when it is introduced.

## Reconsideration criteria

A public Storage SPI may be proposed only when a concrete independent consumer demonstrates a requirement that the official implementation cannot reasonably satisfy. A future proposal must include:

- a stable compatibility and versioning policy;
- explicit transaction, atomicity, concurrency, and cancellation guarantees;
- retention, deletion, export, and privacy/security requirements;
- deterministic conformance tests and a supported platform matrix;
- failure containment and corruption/recovery semantics;
- evidence that public extensibility provides material value beyond the maintenance and compatibility cost.

This ADR does not promise that a public SPI will be introduced.

## Scope exclusions

This decision does not approve a production SQLDelight schema, live weather providers, production forecast algorithms, blending, learning, remote storage, synchronization, telemetry, a hosted service, or M1 implementation. Those remain subject to their own milestone gates and owner approvals.

## References

- [ADR-0003: Approved production technology baseline](ADR-0003-approved-production-technology-baseline.md)
- [M0.2 public API and storage specification](../specs/M0_2_PUBLIC_API_STORAGE.md)
- [M0.2 public API and internal storage proposal](../recommendations/M0_2_PUBLIC_API_STORAGE_PROPOSAL.md)
- [Kotlin visibility modifiers](https://kotlinlang.org/docs/visibility-modifiers.html)
