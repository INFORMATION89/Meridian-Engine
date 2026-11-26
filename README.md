# Meridian

Meridian is an independent, embeddable weather-data and forecasting engine. It has no user interface and must not depend on Solstice; Solstice is one possible consumer, not the product boundary.

## Project status

**Experimental — not ready for production use.**

The active milestone is **M0.2: public API, lifecycle/networking, optional storage/cache, and compatibility contract design with deterministic independent-consumer proof**. M0.1 established the platform/dependency evidence baseline; the owner-approved technology direction is recorded in [ADR-0003](docs/decisions/ADR-0003-approved-production-technology-direction.md).

This repository is not yet a usable weather engine. It does not contain production weather-provider integration, a forecast pipeline, a stable public API, a production cache, or a released package. Do not use this branch as a production dependency.

The original detailed M0.1 platform matrix is [CI run #222](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45872259210), on code commit `91996fb7c190f80899261eb2ff21630b5e564209`. The current PR [#15](https://github.com/INFORMATION89/Meridian-Engine/pull/15) head `009e5979ce63f8b169c542d24c654fa64b59c363` was revalidated by [CI run #224](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45874428103), with all five jobs passing. PR #15 remains open and requires owner approval before merge. Neither run validates M0.2 contracts.

The owner-approved technology direction is recorded in [ADR-0003](docs/decisions/ADR-0003-approved-production-technology-direction.md): KMP shared core, Ktor behind Meridian-owned networking contracts, and SQLDelight/SQLite as the preferred optional persistence candidate. [ADR-0001](docs/decisions/ADR-0001-jvm-first-desktop.md) defines the JVM-first desktop route.

## Current platform strategy

- **Android:** Kotlin Multiplatform Android target; the accepted CI path is an x86_64 emulator.
- **iOS:** Kotlin/Native framework consumed from Swift; the accepted CI path is an ARM64 iOS simulator.
- **Windows, macOS, and Linux:** one Desktop JVM artifact, tested on each operating system.

This owner-approved JVM-first strategy provides desktop compatibility, not native `.dll`, `.dylib`, or `.so` libraries for every desktop OS. A consuming application may bundle a compatible Java runtime in its own distribution to avoid requiring users to install Java separately. Android ARM64 runtime and physical Android/iOS devices remain **NOT TESTED** under the current CI-only M0.1 acceptance.

KMP and Ktor are approved technology directions; SQLDelight/SQLite remains an optional preferred candidate, not a mandatory core dependency. M0.2 is designing and validating the public API and storage/networking contracts. No stable public API, production provider integration, production forecast pipeline, or fixed data-retention window is approved yet. See [PLAN.md](PLAN.md), [ADR-0003](docs/decisions/ADR-0003-approved-production-technology-direction.md), and the [M0.2 contract-design specification](docs/specs/M0_2_CONTRACT_DESIGN.md).

## Product and architecture boundaries

- Meridian is a reusable library, not a weather application.
- Meridian returns meteorological facts, derived weather events, evidence, provenance, quality, and uncertainty; consuming applications own user-facing advice such as clothing, umbrella, UV-action, activity, and notification policies ([ADR-0002](docs/decisions/ADR-0002-application-owned-recommendations.md)).
- Meridian must build and pass its own tests without checking out Solstice.
- The engine core must not depend on UI frameworks, a consumer application's lifecycle, or any Meridian-hosted service.
- Weather values must eventually retain units, timestamps, freshness, quality information, and source provenance.
- Provider-specific data belongs inside provider adapters.
- Ordinary engine use must not require an AI provider, model, credential, or token spend.
- Persistent storage is optional and must remain behind a consumer-neutral boundary.
- Network access, telemetry, analytics, and external data sharing must be explicit, documented, and separately consented to; telemetry and external data contribution are off by default and require separate explicit approval/consent.

## Development and verification

Read [AGENTS.md](AGENTS.md), [RULES.md](RULES.md), [PLAN.md](PLAN.md), and the active milestone specification before making changes. Research documents and decision proposals are not production implementation approval.

The project is designed for deterministic tests. Do not add live weather-provider requests to ordinary tests or expose private credentials in code, fixtures, logs, or binaries.

## License

The Meridian project is licensed under the [Apache License, Version 2.0](LICENSE). The M0.1 dependency/license audit is recorded in [MER-0.1 dependency/license evidence](docs/evidence/MER-0.1-dependency-license-review.md) and third-party notices are in `THIRD-PARTY-NOTICES.md`. Mobile release-package notice staging and verification remain outstanding before public distribution. The presence or absence of a root `NOTICE` file must be decided only after reviewing applicable upstream notices and the actual distributed dependency set.
