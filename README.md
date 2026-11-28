# Meridian

Meridian is an independent, embeddable weather-data and forecasting engine. It has no user interface and must not depend on Solstice; Solstice is one possible consumer, not the product boundary.

## Project status

**Experimental — not ready for production use.**

The MER-0.1 platform proof is complete for its owner-approved CI-only acceptance. The owner has approved Kotlin Multiplatform for the shared core, Ktor for HTTP, optional SQLDelight/SQLite behind a storage contract, JVM-first desktop distribution, and a Swift-friendly iOS framework facade. The active milestone is M0.2: public API and storage contracts.

This repository is not yet a usable weather engine. It does not contain production weather-provider integration, a forecast pipeline, a stable public API, a production cache, or a released package. Do not use this branch as a production dependency.

The final MER-0.1 platform matrix passed all five jobs in [CI run #224](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45874428103). The code-validation results and limitations are recorded in [MER-0.1 platform validation](docs/evidence/MER-0.1-platform-validation.md). The formal production technology decision is recorded in [ADR-0003](docs/decisions/ADR-0003-approved-production-technology-baseline.md) and documented for review in [PR #19](https://github.com/INFORMATION89/Meridian-Engine/pull/19).

The owner-approved desktop direction is recorded in [ADR-0001: JVM-first desktop distribution](docs/decisions/ADR-0001-jvm-first-desktop.md). The complete initial technology baseline is now owner-approved in ADR-0003.

## Approved initial platform strategy

- **Android:** Kotlin Multiplatform Android target; the accepted CI path is an x86_64 emulator.
- **iOS:** Kotlin/Native framework consumed from Swift; the accepted CI path is an ARM64 iOS simulator.
- **Windows, macOS, and Linux:** one Desktop JVM artifact, tested on each operating system.

This owner-approved JVM-first strategy provides desktop compatibility, not native `.dll`, `.dylib`, or `.so` libraries for every desktop OS. A consuming application may bundle a compatible Java runtime in its own distribution to avoid requiring users to install Java separately. Android ARM64 runtime, physical Android/iOS devices, native desktop ABIs, and final mobile release-package notice inclusion remain **NOT TESTED**. Mobile notice inclusion must be verified before public mobile distribution.

The initial production baseline is KMP + Ktor + optional SQLDelight/SQLite, with JVM-first desktop distribution and a Swift-friendly iOS framework facade. Rust/UniFFI and native desktop ABIs are deferred unless a concrete consumer requirement justifies revisiting them. This approval does not finalize the public API, storage schema, provider list, forecast algorithms, telemetry, or production feature implementation. See [PLAN.md](PLAN.md), [ADR-0003](docs/decisions/ADR-0003-approved-production-technology-baseline.md), and the active [M0.2 contract specification](docs/specs/M0_2_PUBLIC_API_STORAGE.md).

## Product and architecture boundaries

- Meridian is a reusable library, not a weather application.
- Meridian returns meteorological facts, derived weather events, evidence, provenance, quality, and uncertainty; consuming applications own user-facing advice such as clothing, umbrella, UV-action, activity, and notification policies ([ADR-0002](docs/decisions/ADR-0002-application-owned-recommendations.md)).
- Meridian must build and pass its own tests without checking out Solstice.
- The engine core must not depend on UI frameworks, a consumer application's lifecycle, or any Meridian-hosted service.
- Weather values must eventually retain units, timestamps, freshness, quality information, and source provenance.
- Provider-specific data belongs inside provider adapters.
- Ordinary engine use must not require an AI provider, model, credential, or token spend.
- Persistent storage is optional and remains behind Meridian's internal storage boundary in v1; consumers cannot inject custom storage adapters. See [ADR-0004](docs/decisions/ADR-0004-internal-storage-spi-v1.md).
- Network access, telemetry, analytics, and external data sharing must be explicit, documented, and separately consented to; telemetry is not part of this M0.1 spike.

## Development and verification

Read [AGENTS.md](AGENTS.md), [RULES.md](RULES.md), [PLAN.md](PLAN.md), and the active milestone specification before making changes. Research documents and decision proposals are not production implementation approval.

The project is designed for deterministic tests. Do not add live weather-provider requests to ordinary tests or expose private credentials in code, fixtures, logs, or binaries.

## License

The Meridian project is licensed under the [Apache License, Version 2.0](LICENSE). Third-party dependency metadata is being inventoried separately. The presence or absence of a root `NOTICE` file must be decided only after reviewing applicable upstream notices and the actual distributed dependency set.
