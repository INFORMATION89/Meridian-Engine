# Meridian

Meridian is an independent, embeddable weather-data and forecasting engine. It has no user interface and must not depend on Solstice; Solstice is one possible consumer, not the product boundary.

## Project status

**Experimental — not ready for production use.**

The current active work is MER-0.1, a bounded compatibility and dependency-evidence spike. It validates a candidate Kotlin Multiplatform implementation across Android and iOS, with a Desktop JVM artifact consumed on Windows, macOS, and Linux.

This repository is not yet a usable weather engine. It does not contain production weather-provider integration, a forecast pipeline, a stable public API, a production cache, or a released package. Do not use this branch as a production dependency.

The last fully completed platform matrix is recorded at commit `aa5e86ec1cab1d302b9f2165327deddf299f48dc` in [CI run 45777929849](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45777929849). PR [#15](https://github.com/INFORMATION89/Meridian-Engine/pull/15) contains follow-up CI, license-audit, and documentation changes; only checks for the current PR head establish its current status.

The owner-approved desktop direction is recorded in [ADR-0001: JVM-first desktop distribution](docs/decisions/ADR-0001-jvm-first-desktop.md). This is a desktop packaging decision, not final approval of the complete production technology stack.

## Candidate platform strategy

- **Android:** Kotlin Multiplatform Android target; the accepted CI path is an x86_64 emulator.
- **iOS:** Kotlin/Native framework consumed from Swift; the accepted CI path is an ARM64 iOS simulator.
- **Windows, macOS, and Linux:** one Desktop JVM artifact, tested on each operating system.

This owner-approved JVM-first strategy provides desktop compatibility, not native `.dll`, `.dylib`, or `.so` libraries for every desktop OS. A consuming application may bundle a compatible Java runtime in its own distribution to avoid requiring users to install Java separately. Android ARM64 runtime and physical Android/iOS devices remain **NOT TESTED** under the current CI-only M0.1 acceptance.

Kotlin Multiplatform, Ktor, and SQLDelight remain candidates under evaluation. The spike does not approve the production language/runtime, networking engine, database driver, public API, or packaging commitment. See [PLAN.md](PLAN.md) and the [M0.1 platform proof specification](docs/specs/M0_1_PLATFORM_SPIKE.md).

## Product and architecture boundaries

- Meridian is a reusable library, not a weather application.
- Meridian returns meteorological facts, derived weather events, evidence, provenance, quality, and uncertainty; consuming applications own user-facing advice such as clothing, umbrella, UV-action, activity, and notification policies ([ADR-0002](docs/decisions/ADR-0002-application-owned-recommendations.md)).
- Meridian must build and pass its own tests without checking out Solstice.
- The engine core must not depend on UI frameworks, a consumer application's lifecycle, or any Meridian-hosted service.
- Weather values must eventually retain units, timestamps, freshness, quality information, and source provenance.
- Provider-specific data belongs inside provider adapters.
- Ordinary engine use must not require an AI provider, model, credential, or token spend.
- Persistent storage is optional and must remain behind a consumer-neutral boundary.
- Network access, telemetry, analytics, and external data sharing must be explicit, documented, and separately consented to; telemetry is not part of this M0.1 spike.

## Development and verification

Read [AGENTS.md](AGENTS.md), [RULES.md](RULES.md), [PLAN.md](PLAN.md), and the active milestone specification before making changes. Research documents and decision proposals are not production implementation approval.

The project is designed for deterministic tests. Do not add live weather-provider requests to ordinary tests or expose private credentials in code, fixtures, logs, or binaries.

## License

The Meridian project is licensed under the [Apache License, Version 2.0](LICENSE). Third-party dependency metadata is being inventoried separately. The presence or absence of a root `NOTICE` file must be decided only after reviewing applicable upstream notices and the actual distributed dependency set.
