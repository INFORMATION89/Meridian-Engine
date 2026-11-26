# Meridian — Project Overview

**Document type:** Human-readable project summary  
**Status:** Derived from PLAN.md; reflects the project direction and recorded status as of 2026-11-26  
**Authority:** PLAN.md is the authoritative master plan. This overview summarizes it and does not replace its technical decisions or acceptance criteria.

## What is Meridian?

Meridian is an independent weather engine designed to power weather applications and other products. It gathers information from suitable weather sources, evaluates the quality of that information, combines forecasts and observations, and returns structured results with source information, freshness, uncertainty, and explanations.

Meridian is not a weather app. It does not own screens, maps, charts, saved locations, notifications, or user-facing presentation. Applications choose how to display and use the information they receive.

Solstice is intended to be an important first consumer of Meridian, but Meridian must be useful to other apps without depending on Solstice.

## What makes Meridian different?

The central goal is **Level 2 weather intelligence**: improve the usefulness of existing weather models and observations instead of attempting to build a new fundamental weather-prediction model.

Over time, Meridian is intended to:

- Choose suitable weather sources for each place, variable, and forecast period.
- Evaluate their quality, coverage, freshness, and historical performance.
- Combine multiple existing forecast models when doing so improves the result.
- Learn from verified observations and correct systematic regional errors when there is enough evidence.
- Explain uncertainty, source disagreement, and the reasons behind important results.
- Detect and track weather phenomena when available evidence and validated methods support it.
- Provide historical and climatological context without confusing it with a current forecast.

Meridian will not build its own fundamental numerical weather-prediction model or global climate model. Its intelligence will come from better selection, combination, validation, and processing of existing information.

## Where should it run?

The design target includes:

- Windows
- macOS
- Linux
- Android
- iOS

The goal is a consistent weather API and shared meteorological behaviour across those platforms. The approved initial technology is Kotlin Multiplatform for the shared core, Ktor for HTTP, and optional SQLDelight/SQLite behind a storage contract. Desktop uses JVM-first distribution on Windows, macOS, and Linux; iOS is consumed through a Swift-friendly framework facade. Rust/UniFFI and native desktop libraries are deferred unless real consumer requirements justify them.

A platform is not considered supported just because the code compiles. Its actual library or framework must be loaded and exercised by the kind of application expected to use it.

## Privacy and operation

Meridian is local-first. Its normal use should not require the project owner or app developer to operate a Meridian server, database, account system, telemetry service, or AI service.

Weather requests go directly from the user's device to eligible providers when their terms and technical requirements permit it. Local cache and learning data remain on the device by default.

Telemetry and data contribution are off by default. Any future sharing must be optional and require clear, separate consent. Before a person opts in, the app must explain what information is collected, why it is needed, where it is sent, who receives it, and how long it is kept. Users must be able to inspect an example of the data, withdraw consent, and delete eligible local data.

AI may help contributors build the project, but the released engine must not need an AI model, AI account, credential, inference service, or token budget.

## How the weather intelligence will develop

The roadmap is staged so that each capability is supported by evidence before it becomes a product promise.

| Stage | What it establishes |
|---|---|
| M0 — Foundation | Repository governance, licensing, documentation, and CI |
| M0.1 — Platform proof | Confirm a viable way to build and integrate Meridian across the target platforms |
| M0.2 — Public API and storage | Define how applications request weather data and how local cache/history are handled |
| M0.3 — Verification foundation | Build reliable ways to compare forecasts with later observations |
| M1 — First forecast | Deliver a narrow forecast path from one eligible provider |
| M2 — Source resilience | Add source selection, provenance, cache, fallbacks, and clear failure reporting |
| M3 — Official alerts | Normalize alerts while preserving the authority that issued them |
| M4 — Observations | Assess station measurements and their local representativeness |
| M5 — Multi-model forecast | Combine existing models and communicate their disagreement and uncertainty |
| M6 — Independent consumers | Prove that different applications can integrate Meridian without Solstice |
| M7 — Regional intelligence | Improve forecasts using verified local performance, calibration, and bounded corrections |
| M8 — Weather events | Detect and track supported weather phenomena |
| M9 — Historical context | Add licensed historical and climatological information |
| M10 — Radar and nowcasting | Build regional precipitation detection and short-term evolution where data supports it |

The first stable release must deliver meaningful, validated intelligence from multiple sources and regional evaluation. Advanced radar, climatology, and other capabilities can remain experimental until their own evidence gates are met.

## How Meridian relates to Solstice

Solstice is a consumer application. It owns its interface, saved places, settings, refresh schedule, and notification decisions. Meridian supplies structured weather information and explanations through a public API.

Meridian must not copy Solstice's UI or application-specific behaviour into the engine. Other consumers must be able to use the same weather contracts independently.

## Current project status

**Meridian is still experimental and is not ready for production use.**

The current work has established a cross-platform compatibility proof, not a complete weather engine. The canonical platform-proof PR #2 has six successful CI jobs on its recorded head, covering an Android x86_64 emulator, an iOS simulator/Swift consumer, and JVM desktop consumers on Windows, macOS, and Linux.

That evidence does not finish the platform decision. Actual platform-specific network behaviour, timeout and cancellation handling, packaging implications, and the complete dependency licence review still need to be completed. Android ARM64 runtime and physical Android/iOS device execution were not part of the accepted CI-only M0.1 proof.

There is not yet an authorized production weather-provider pipeline, stable public API, or production storage contract. The Apache-2.0 LICENSE and draft README are currently present on the PR #2 branch but have not yet been merged into main.

## What happens next?

The immediate priority is to finish and review the platform proof, choose the production integration strategy using evidence, define the public API and local storage boundaries, and build a reproducible forecast-verification foundation.

Only after those gates are satisfied should production weather providers and forecast intelligence be implemented. This keeps Meridian independent, portable, scientifically honest, and privacy-preserving as it grows.

## Project documents

- [Master plan](PLAN.md) — authoritative scope, architecture boundaries, milestones, and acceptance gates.
- [AI implementation plan](AI_IMPLEMENTATION_PLAN.md) — detailed execution instructions for coding agents.
- [Project rules](RULES.md) and [agent instructions](AGENTS.md) — repository working and automation requirements.
- [AI policy](AI_POLICY.md) and [AI cost policy](AI_COST_POLICY.md) — contributor AI boundaries and resource-efficiency rules.
