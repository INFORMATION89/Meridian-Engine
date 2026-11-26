# ADR-0003: Approved production technology direction

- **Status:** Accepted
- **Decision date:** 2026-11-26
- **Scope:** Shared engine language/runtime direction and initial networking/persistence candidates
- **Authority:** Explicit owner decision

## Context

M0.1 provided practical evidence for Kotlin Multiplatform across Android, iOS/Swift, and Desktop JVM on Windows, macOS, and Linux. The project needs one reusable engine for multiple independent consumers, with a maintainable cross-platform implementation and no dependency on Solstice. The owner reviewed the evidence and approved proceeding with the KMP direction and M0.2 contract design.

## Decision

1. Use **Kotlin Multiplatform (KMP)** for the shared engine core.
2. Use **Ktor** as the initial HTTP client implementation, hidden behind Meridian-owned networking contracts. Exact engine selection/configuration and public semantics remain subject to M0.2 proof.
3. Keep persistence **optional** and behind a Meridian-owned storage contract. **SQLDelight with SQLite is the preferred candidate**, not a mandatory dependency of the core and not an irrevocable choice until M0.2 validates the contract and target behaviour.
4. Use the accepted **JVM-first desktop** route from ADR-0001 for Windows, macOS, and Linux. Do not imply native desktop ABI support.
5. Keep the public API independent of Ktor, SQLDelight, SQLite/JDBC, platform handles, provider SDKs, UI frameworks, and consuming applications.

## What this decision approves

- KMP as the shared-core technology direction for the next implementation stages.
- Ktor as the initial networking implementation choice behind an abstraction.
- SQLDelight/SQLite as the preferred optional persistence candidate for M0.2 contract validation.
- Starting M0.2 public API, lifecycle/networking, storage/cache, and compatibility contract design and deterministic consumer proof.

## What this decision does not approve

- A stable public API or exact final public signatures.
- Mandatory persistence, a final SQLDelight driver matrix, or a persistent schema/retention policy.
- Production weather-provider integrations, live weather requests, forecast algorithms, blending, learning, or production data collection.
- Native Windows/macOS/Linux library artifacts or ABI guarantees.
- Automatic location, background scheduling, telemetry, analytics, accounts, hosted services, AI runtime, or external data sharing.
- M1/production weather-feature implementation before M0.2 is completed and its owner approval gate is passed.

## Consequences

The M0.1 KMP evidence can now be used as the baseline rather than reopening the language comparison without a concrete blocker. M0.2 must still validate the consumer-facing API, Swift interoperability, cancellation, lifecycle, storage boundary, schema/version separation, and target-specific dependency behaviour. If a chosen implementation fails those contracts, agents should present evidence and a bounded correction proposal rather than silently changing product direction.

## Revisit triggers

Revisit KMP if an actual supported consumer cannot integrate it, required platform/architecture support proves infeasible, measured runtime or packaging constraints violate an approved product budget, or a material security/maintenance issue emerges. Revisit Ktor or SQLDelight if deterministic target evidence, licence/security review, or consumer requirements show they cannot satisfy the approved contracts.
