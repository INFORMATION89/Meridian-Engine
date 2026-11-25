# Meridian

Meridian is an independent, local-first weather-data and forecasting engine intended to be embedded in consumer applications. It has no user interface and must not depend on Solstice; Solstice is one potential consumer, not the product boundary.

## Project status

**Experimental — not ready for production use.**

This repository currently contains a Kotlin Multiplatform compatibility spike and its supporting specifications. It is not yet a usable weather engine. There is no production weather-provider integration, forecast pipeline, stable public API, production cache, or published package.

Do not add this branch as a production dependency. The current probe exists to validate the platform and tooling direction before the engine is implemented.

## Intended platform strategy

The current proposal is:

- Android: Kotlin Multiplatform Android target.
- iOS: Kotlin/Native framework consumed from Swift.
- Windows, macOS, and Linux: one Desktop JVM artifact.

The desktop JVM approach means consuming applications need a compatible Java runtime or must bundle one in their own distribution. Meridian does not promise native Windows or Linux libraries in the initial platform strategy.

Android CI currently targets an x86_64 emulator. Android ARM64 runtime validation remains explicitly pending and must not be inferred from x86_64 results.

These are implementation candidates, not production support guarantees. See the [MER-0.1 verification record](docs/verification/MER-0_1_RESULTS.md) and [platform spike specification](docs/specs/MER-0_1_PLATFORM_SPIKE.md) for exact evidence and limitations.

## Architecture principles

- Meridian is a library, not a weather application.
- The core must remain independent of Solstice, UI frameworks, and any Meridian-hosted service.
- Weather values must retain units, timestamps, freshness, quality information, and source provenance.
- Provider-specific data stays inside provider adapters.
- Ordinary engine use must not require an AI provider, model, credential, or token spend.
- Persistent storage is optional and remains behind a consumer-neutral boundary.

## Roadmap

The master roadmap is [PLAN.md](PLAN.md). The next gates are:

1. Complete and review the MER-0.1 platform, dependency, and packaging evidence.
2. Approve the public API, storage boundary, errors, and cancellation contract in MER-0.2.
3. Complete reproducible forecast-verification and contributor-research design in MER-0.3.
4. Begin MER-1 only after the required owner decisions and acceptance gates are approved.

## Development

Read [RULES.md](RULES.md), [CLEAN_ROOM.md](CLEAN_ROOM.md), [AGENTS.md](AGENTS.md), and [AI_POLICY.md](AI_POLICY.md) before making changes. Each milestone has a specification under [docs/specs](docs/specs/).

The project is designed for deterministic, offline tests. Do not add live provider requests to ordinary tests or expose private provider credentials in code, fixtures, logs, or binaries.
