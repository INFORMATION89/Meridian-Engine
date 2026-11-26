# Meridian

Meridian is an independent, embeddable weather-data and forecasting engine. It has no user interface
and must not depend on Solstice; Solstice is one possible consumer, not the product boundary.

## Project status

**Experimental — not ready for production use.**

The MER-0.1 compatibility and dependency-evidence probe has been merged to `main` through
[PR #15](https://github.com/INFORMATION89/Meridian-Engine/pull/15) (2026-11-26). The exact final
PR-head [CI run #224](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45874428103)
passed all five jobs: Android emulator, Apple/iOS, Linux desktop, Windows desktop, and the
dependency/licence audit. This validates a candidate multiplatform test harness and packaging evidence,
not a production forecast engine.

This repository is **not yet a usable weather engine**. It does not contain production
weather-provider integration, a forecast pipeline, a stable public weather API, a production cache,
or a released consumer package. Do not use the probe as a production dependency.

The probe tested an Android x86_64 emulator, an ARM64 iOS simulator with a native Swift consumer,
and Desktop JVM consumers on Windows, macOS, and Linux. Android ARM64 runtime and physical
Android/iOS devices remain **NOT TESTED**. The desktop distribution direction is owner-approved
JVM-first; this does not approve the complete production language, networking, storage, public API,
provider, or release-packaging choices.

The KMP/Ktor/SQLDelight recommendation remains **proposed, not approved**. The owner must review
the evidence and approve the production direction before M1. See [PLAN.md](PLAN.md), the
[M0.1 platform proof specification](docs/specs/M0_1_PLATFORM_SPIKE.md), the
[platform validation evidence](docs/evidence/MER-0.1-platform-validation.md), and the
[dependency/licence review](docs/evidence/MER-0.1-dependency-license-review.md).

The owner-approved desktop direction is recorded in
[ADR-0001: JVM-first desktop distribution](docs/decisions/ADR-0001-jvm-first-desktop.md). This is a
desktop packaging decision, not approval of the complete production technology stack.

## Candidate platform strategy

- **Android:** Kotlin Multiplatform Android target; the accepted CI path is an x86_64 emulator.
- **iOS:** Kotlin/Native framework consumed from Swift; the accepted CI path is an ARM64 iOS simulator.
- **Windows, macOS, and Linux:** one Desktop JVM artifact, tested on each operating system.

This owner-approved JVM-first strategy provides desktop compatibility, not native `.dll`, `.dylib`,
or `.so` libraries for every desktop OS. A consuming application may bundle a compatible Java
runtime in its own distribution to avoid requiring users to install Java separately. Native desktop
ABIs remain deferred unless a concrete consumer requirement justifies them.

The M0.1 test workflow does not create an Android APK/AAB or an iOS application release package.
Before public mobile distribution, the project and third-party licence notices must be staged and
verified alongside the iOS XCFramework and in Android AAR/APK/AAB packaging.

## Product and architecture boundaries

- Meridian is a reusable library, not a weather application.
- Meridian returns meteorological facts, derived weather events, evidence, provenance, quality, and
  uncertainty; consuming applications own user-facing advice such as clothing, umbrella, UV-action,
  activity, and notification policies ([ADR-0002](docs/decisions/ADR-0002-application-owned-recommendations.md)).
- Meridian must build and pass its own tests without checking out Solstice or another consumer.
- The engine core must not depend on UI frameworks, a consumer application's lifecycle, or any
  Meridian-hosted service.
- Weather values must retain units, timestamps, freshness, quality information, and source provenance.
- Provider-specific data belongs inside provider adapters.
- Ordinary engine use must not require an AI provider, model, credential, or token spend.
- Persistent storage is optional and must remain behind a consumer-neutral boundary.
- Network access, telemetry, analytics, and external data sharing must be explicit, documented, and
  separately consented to; telemetry is not part of the MER-0.1 probe.

## Development and verification

Read [AGENTS.md](AGENTS.md), [RULES.md](RULES.md), [PLAN.md](PLAN.md), and the active milestone
specification before making changes. Research documents and decision proposals are not production
implementation approval.

The project is designed for deterministic tests. Do not add live weather-provider requests to
ordinary tests or expose private credentials in code, fixtures, logs, or binaries.

## License

The Meridian project is licensed under the [Apache License, Version 2.0](LICENSE). Third-party
dependency metadata is inventoried separately; keep required licence and notice files in each
distributed artifact.
