# Meridian — Master Plan

**Repository:** `INFORMATION89/Meridian-Engine`  
**Product/library:** Meridian  
**Status:** Draft for owner review; technical direction recommended, not irrevocably approved  
**Last reviewed:** 2026-11-25  
**Authority:** This file is the master plan for scope, architecture, technical decisions, milestones, and acceptance criteria. `docs/ENGINE_SPEC.md` defines weather behaviour; `docs/MERIDIAN_PLAN.md` is supporting research. Where their older technology assumptions conflict with this plan, this plan is the current proposal and the conflict must be resolved before implementation.

---


## 1. Executive direction

Meridian is an independent, reusable, local-first weather-data and forecasting engine. It runs inside the consuming application on the user's device, accepts a place and requested weather features, obtains data directly from appropriate external providers, normalizes and evaluates the inputs, and returns useful weather results with uncertainty, freshness, source attribution, and complete provenance. Normal operation must not require a Meridian-hosted server, account, remote database, cloud function, or always-on service.

Meridian is a library, not a weather application. It has no screens, app navigation, user-facing design system, saved-place manager, or app-specific behaviour. Solstice is its first consumer, not its owner. Other apps and tools should be able to use the same engine without depending on Solstice.

### Recommended technical direction

**Kotlin Multiplatform is the recommended starting point, but desktop compatibility should use the stable JVM target rather than requiring Kotlin/Native on Windows and Linux.** This is the least complex route that meets the current five-OS requirement while reusing the Kotlin ecosystem already used by Solstice:

- **Shared engine logic:** Kotlin Multiplatform `commonMain`.
- **Android:** Android/Kotlin target.
- **iOS:** Kotlin/Native iOS framework, called from Swift/Objective-C.
- **Windows, macOS, and Linux:** Kotlin/JVM desktop artifact. Package the required Java runtime with a desktop application when the user should not need to install Java separately.
- **Storage candidate:** SQLDelight with SQLite, using the Android, Native, and JVM drivers as appropriate. Do not make Android Jetpack Room a cross-platform engine dependency.
- **Networking candidate:** Ktor, with a suitable Android client, Darwin client for Apple, and JVM client for desktop. Validate the exact target/dependency matrix before adoption.

This means the engine can run on all five operating systems, but it does **not** mean a single native `.dll`, `.dylib`, and `.so` is produced for every desktop OS. Windows/macOS/Linux desktop consumers need a JVM. If the requirement changes to a native library that arbitrary non-JVM apps can embed, reconsider Rust.

### When Rust would be the better choice

Rust is a strong alternative if Meridian must be a native library with no JVM runtime on Windows/macOS/Linux, or if C/C++/Swift and other non-Kotlin applications must embed the same native core. Rust's target support is attractive, and UniFFI can generate Kotlin and Swift bindings. However, native packaging, FFI, JNA, async/cancellation, binary distribution, and extra language/toolchain maintenance all add cost that is not justified by OS compatibility alone.

### Decision gate

M0.1 must prove the proposed Kotlin path on real target environments, including iOS framework integration and JVM execution on Windows, macOS, and Linux. It must also validate the likely Ktor and SQLDelight driver matrix. If those tests fail or the owner requires native desktop libraries without a JVM, compare a small Rust/UniFFI proof before committing to M1. The implementation language is not irrevocably locked until the owner approves the M0.1 report.

## Local-first and no-hosting principle

Meridian is embedded in the consuming application and runs on the user's device. The normal product path must not require the project owner or user to deploy, rent, operate, or maintain a Meridian backend.

- No mandatory Meridian cloud API, account, authentication server, remote database, synchronization service, telemetry collector, or background daemon.
- Weather, observation, and official-alert adapters make direct client-to-provider requests from the device, subject to provider terms, credentials, rate limits, and platform networking rules.
- Cache, forecast history, verification statistics, and user configuration are local by default. Use the approved on-device storage implementation; do not assume a central database.
- Offline operation returns clearly marked cached/stale results or typed unavailability. It must never disguise old data as current.
- Each app owns lifecycle and scheduling. Meridian is a library, not a long-running service.
- Any future sync, shared analytics, hosted proxy, or remote MCP endpoint is a separate optional product/infrastructure decision requiring explicit approval. Such services must never become prerequisites for the ordinary engine.

This does not promise that weather data is available without the Internet or that every external provider permits direct use from distributed clients. Provider selection must prioritize documented client-side access and must not embed private developer credentials in distributed binaries.
## 2. Product principles

1. **Independent core:** no dependency on Solstice, Breezy Weather, or another consumer app.
2. **No UI:** Meridian returns structured information; consuming apps decide how to display it.
3. **Cross-platform by design:** core weather behaviour must not depend on a particular operating system.
4. **Provenance by default:** every meaningful value must be explainable.
5. **Scientific honesty:** observations, forecasts, derived values, corrections, and uncertainty remain distinguishable.
6. **Resilient sources:** one provider's outage must not invalidate unrelated providers or features.
7. **Privacy by default:** no location history, telemetry, analytics, advertising, or remote sharing without a separate approved design and explicit consent.
8. **Reproducible verification:** deterministic fixtures for ordinary tests and historical archives for scientific changes.
9. **Small public API:** expose stable weather concepts, not provider DTOs, database records, or platform details.
10. **Specification before implementation:** each substantive milestone has an approved spec, a separate build task, and measurable acceptance criteria.
11. **AI-assisted development, never AI-dependent runtime:** Meridian itself does not call generative-AI services. Contributors may use AI selectively for engineering and manual research, but the shipped engine remains deterministic, independent, and free of AI-provider or token costs.
12. **Consumer neutrality:** Meridian must build, test, and run independently. Solstice is one consumer among many, never the product boundary.

---

## 3. Goals and non-goals

### Goals

- Current conditions, hourly and daily forecasts.
- Precipitation probability and amount, including short-range rain where data supports it.
- Weather alerts normalized to a common model while preserving the issuing authority's message.
- Air quality and UV when supported by a suitable source.
- Airport/station observations for current conditions and later forecast verification.
- Multiple forecast models, model spread, and an evidence-based blend.
- Source selection based on feature coverage, availability, freshness, quality, configuration, and provider limits.
- Transparent provenance, freshness, attribution, fallbacks, and typed problems.
- Local caching and an optional local history for forecast verification and bias correction.
- A library API that can be consumed by native and managed applications.

### AI-assisted development and research

AI is primarily a contributor tool, not a Meridian product capability. It may help developers research technical decisions, write and review code, generate test ideas, inspect sanitized historical forecast/observation data, explore error patterns, and draft analysis scripts. Its use should be deliberate and limited to tasks where it provides measurable value; ordinary deterministic scripts and manual analysis remain the default.

Meridian must not call an LLM or AI provider while fetching, calculating, blending, validating, caching, or returning weather results. No AI model, key, prompt, hosted service, inference runtime, MCP adapter, vector database, or token budget is required by the engine or by consumers using its ordinary API. An optional MCP adapter may later let an external chat client query Meridian; that adapter is a separate integration, not part of the core. Any model/token costs and generated response behaviour belong to the external chat host/provider. Users of ordinary Meridian consumers must not experience AI latency or AI token charges when asking for weather information.

Forecast quality improves through a reproducible loop: preserve forecast snapshots as issued, match them later to quality-controlled observations, evaluate errors by variable/lead time/region/season/source, compare candidate changes against an unchanged baseline, and have a human review and approve every algorithm or configuration change. AI may suggest hypotheses, but deterministic code and versioned data must reproduce every claimed result. Do not add runtime ML or learned correction models without a separate evidence-based specification and owner approval.

### Non-goals for the first usable release

- Building Solstice's UI or any other weather UI.
- Running a hosted weather API or mandatory Meridian cloud service.
- User accounts, social features, ads, telemetry, or location tracking.
- Reimplementing numerical weather prediction models.
- Building an entire radar processing network before the model-based engine is useful.
- Supporting every possible OS architecture on day one.
- Guaranteeing that all providers cover all locations or all features.

---


## 4. Platform compatibility requirements

The OS-level requirement can be met through this Kotlin Multiplatform target strategy:

| Operating system | Initial target | Consumer/runtime requirement | Optional later target |
|---|---|---|---|
| Android | Android/Kotlin target, ARM64 first | Android runtime | x86_64 emulator; other ABIs as needed |
| iOS | `iosArm64` and `iosSimulatorArm64` | Native Kotlin framework linked into an iOS app; Xcode build host | Intel simulator only if needed |
| Windows | Desktop `jvm()`, x64 first | A compatible JVM; bundle a runtime with a desktop app if needed | Kotlin/Native `mingwX64` only if a native Windows library becomes a requirement |
| macOS | Desktop `jvm()` | A compatible JVM; bundle a runtime if needed | `macosArm64` framework for a native Swift macOS consumer |
| Linux | Desktop `jvm()`, x64 first | A compatible JVM; document tested distributions | `linuxX64` / `linuxArm64` native only if a native Linux library becomes a requirement |

Kotlin Multiplatform lists Android, iOS, and Desktop (JVM) as stable. The Desktop JVM target can run on Windows, macOS, and Linux; the application must include or require a suitable JVM. This is the key to reaching all five OS without treating Windows/Linux Kotlin/Native targets as release blockers.

Kotlin/Native target tiers still matter for any future native desktop library: `linuxX64` and `linuxArm64` are Tier 2, `mingwX64` is Tier 3, and `macosX64` is deprecated in the current target table; `macosArm64` is Tier 1. Do not claim native Windows/Linux support unless those native artifacts are separately built and tested.

Jetpack's support tiers for Linux and Windows describe Jetpack libraries, not the whole KMP/JVM strategy. Avoid depending on Jetpack-specific storage or Android-only APIs from common engine code.

### Support labels

- **Supported:** built and tested in CI and exercised through its real runtime/framework.
- **Build-only:** compiles or links, but runtime integration has not been verified.
- **Experimental:** known target; not promised to consumers.
- **Not supported:** no tested artifact or integration.

Do not use a vague "cross-platform" badge without publishing this matrix.


## 5. Language and interoperability decision

### Candidate A — Kotlin Multiplatform (recommended)

**Advantages**
- Reuses the language and integration environment of Solstice.
- Shares domain models, source selection, provider adapters, provenance, and forecast algorithms in `commonMain`.
- Reaches Android and iOS through their supported KMP targets.
- Reaches Windows, macOS, and Linux through the stable Desktop JVM target without requiring native Windows/Linux KMP artifacts.
- Can expose an iOS framework to Swift consumers.
- SQLDelight documents SQLite support for Android, Native targets, and JVM; this is a better cross-platform storage candidate than making Jetpack Room mandatory.

**Costs and risks**
- Desktop JVM consumers need a compatible Java runtime. A distributable desktop app should bundle it or clearly document it.
- iOS integration uses Kotlin/Native framework interop; async APIs and Swift ergonomics need a real integration test.
- Ktor, SQLDelight, serialization, date/time, and other libraries must be checked for the exact Android/iOS/JVM target matrix; compiler support alone is not enough.
- Windows/Linux native libraries are not part of the initial KMP support promise. Native KMP targets are lower-tier than JVM desktop.
- If a future consumer is a native C/C++/Swift desktop app without a JVM, the architecture may need a separate native implementation or a Rust core.

### Candidate B — Rust native core

**Advantages**
- Strong native target support across Windows, macOS, Linux, iOS, and Android.
- One implementation can be compiled into native libraries for multiple architectures.
- Good choice for non-JVM consumers and a language-neutral native engine boundary.
- UniFFI generates Kotlin and Swift bindings.

**Costs and risks**
- Adds Rust and FFI to a project whose first consumer is Kotlin.
- Native binaries must be built, versioned, packaged, and distributed for each target ABI.
- FFI error handling, cancellation, threading, async calls, object lifetime, and memory ownership require a deliberately small API.
- UniFFI does not replace native build/release pipelines; Kotlin bindings depend on JNA and coroutines, which must be tested in the selected environments.
- Rust's iOS and Android ARM64 targets are Tier 2, and third-party crate support must be validated separately.

### M0.1 decision gate

Build a small, non-weather proof of concept before full engine implementation:

1. Create a KMP library with one shared deterministic function, one typed error, and one asynchronous operation.
2. Build the Android target and run a smoke test on an ARM64 emulator/device.
3. Build an iOS framework and run a Swift consumer in an ARM64 simulator; test on a device where available.
4. Build the Desktop JVM artifact and run the same probe on Windows, macOS, and Linux with a supported JDK.
5. Validate a minimal Ktor request/serialization slice for Android, iOS, and JVM desktop.
6. Validate a minimal SQLDelight SQLite slice using Android, Native iOS, and JVM desktop drivers.
7. Record build times, test results, artifact sizes, runtime requirements, and packaging steps.
8. If a native desktop library with no JVM is a firm requirement, build a small Rust/UniFFI comparator against the same matrix.
9. Recommend the production stack and obtain owner approval before M1.


## 6. Proposed architecture

The following is a logical architecture. Exact module names and dependency choices are finalized by M0.1/M0.2, not guessed during feature implementation.

### Core layers

1. **Domain model:** places, coordinates, variables, units, time series, observations, alerts, source descriptors, result types, and typed problems.
2. **Engine/orchestrator:** requested-feature planning, source eligibility, request coordination, rate limiting, fallback, cache policy, aggregation, and result assembly.
3. **Source adapters:** one isolated adapter per provider. Provider DTOs remain inside the adapter.
4. **Forecast algorithms:** blending, spread, local verification, and later correction. Algorithms receive normalized data and do not perform network requests.
5. **Storage boundary:** common contracts for cache and local verification history; platform drivers supplied by the approved storage implementation. The public API must not expose database records.
6. **Platform drivers:** only the narrow pieces that genuinely differ, such as HTTP engines and database drivers. They must not change domain semantics.
7. **Developer tools:** JVM CLI, fixture utilities, and reproducible archive verification.

### Candidate Kotlin Multiplatform layout

```text
Meridian-Engine/
  settings.gradle.kts
  build.gradle.kts
  gradle/libs.versions.toml
  core/                         # KMP domain and engine API
  sources/
    openmeteo/                  # Open-Meteo adapter
    aviationweather/            # METAR adapter
    wmo/                        # official alert feeds
    fpas/                       # additional CAP alert aggregation
  storage-sqldelight/           # optional shared SQLite storage
  testing/                      # fixtures, fake clock, fake HTTP
  cli/                          # JVM developer/verification tool
  samples/
    android/
    ios/
    desktop-jvm/                # Windows, macOS, Linux
  docs/
    specs/
    verification/
```

This is a proposal, not an instruction to create every module immediately. M0.1 creates only the minimal Gradle/KMP proof. Add each module when an approved milestone needs it.

### Verification and analysis boundary

- The engine exposes structured weather results with units, time semantics, source attribution, freshness, uncertainty, quality indicators, and provenance because these are fundamental library requirements for every consumer.
- Developer-facing verification tools may compare preserved forecast snapshots with later observations and generate reproducible reports.
- Each report identifies data versions, matching rules, algorithm/configuration versions, evaluation code, metrics, exclusions, and sample counts.
- AI-assisted interpretation is an occasional contributor workflow only. Analysis findings must be reproduced by deterministic code and reviewed by a human before any algorithm or configuration change.
- No AI adapter, prompt context, agent tool contract, MCP server, or model SDK belongs in the weather core. A future MCP connector may live as a separately scoped adapter that consumes only the stable public API.

### Dependency direction

- `core` owns weather-domain types and engine contracts.
- Source adapters depend on core contracts; core does not depend on provider DTOs.
- Storage implementation depends on core contracts; core does not depend on a storage vendor.
- Platform-specific HTTP/database drivers are injected at construction or selected in thin platform factories.
- Bindings and sample applications consume only the public core API.
- Solstice, iOS apps, desktop apps, and CLI must not import internal provider implementation types.


## 7. Storage, cache, and privacy

The earlier Kotlin-oriented plan proposed Room. Do not make Android Jetpack Room a cross-platform engine requirement.

### Current recommendation

- Keep persistence behind a storage boundary.
- Evaluate **SQLDelight + SQLite** as the official optional storage implementation. SQLDelight documents SQLite for Android, Native targets, and JVM; the initial five-OS path uses Android, Native iOS, and JVM desktop drivers as appropriate.
- Do not use the Windows/Linux Kotlin/Native SQLite drivers in the first release unless native desktop binaries become a requirement.
- Solstice may continue using Room for Solstice-owned app data. Meridian's cache/history must not require access to the Solstice database.
- Consumer-provided custom storage is desirable, but do not make it public API until its lifecycle and driver injection are proven in M0.2.

### Data policy

- Meridian does not own the list of saved places and does not maintain location history.
- Store only data required for cache, local forecast verification, and approved correction.
- A 30-day rolling history is a current research proposal, not a hard-coded default until its scope and storage cost are approved.
- Cache TTL and request spacing follow provider cadence, expiry headers, terms, and rate limits.
- Never let stale cached data masquerade as fresh data.
- Data remains local by default. Optional sharing of aggregated forecast errors requires a separate privacy, security, retention, and legal review.

## 8. Data and public API principles

### Normalized data

- SI units internally.
- UTC instants internally; preserve the place's time zone for local calendar/day aggregation.
- Keep valid time, issue time, observation time, and fetch time distinct.
- Missing, unavailable, invalid, stale, and zero are not interchangeable.
- Preserve source-native resolution; any resampling or aggregation is explicit.
- Keep observation, model forecast, blended forecast, and corrected forecast distinguishable.
- Retain quality indicators such as observation distance, elevation difference, age, source freshness, and model spread.

### Provenance

Every returned value must be traceable to:
- source/provider and model identifiers;
- issue and fetch time;
- source weight or contribution when values are combined;
- transformations, corrections, and their inputs;
- freshness and fallback status.

Results must also explain which candidate sources were selected, skipped, or failed and why. Provider attribution must be derived from source metadata and surfaced through the API.

### API contract

The public API should eventually offer these capabilities (names are illustrative):

- create/configure an engine instance;
- refresh a place for a requested set of features;
- retrieve current/last-known results;
- inspect available sources and source status for a place;
- obtain required credits/attribution;
- clear Meridian-owned local data;
- cancel a running operation and shut down resources safely.

Errors should be typed result values at the public boundary. Do not leak unhandled exceptions or panics, HTTP client types, SQLite rows, provider JSON types, or platform-specific objects.

Keep the first public API batch-oriented. Partial streaming updates may be added only after a real consumer requires them and the FFI behaviour is proven.

---

## 9. Weather providers and feature priorities

The current research shortlist is not an irrevocable provider commitment. Re-check current API documentation, terms, attribution, geographic coverage, cost, and limits before implementation.

| Provider | Initial role | Main validation |
|---|---|---|
| Open-Meteo | Forecast models, current/hourly/daily data, air quality, historical forecast archives | Terms and attribution, model availability by region, request weight and rate limits |
| aviationweather.gov | Airport METAR observations | Station coverage, schema, freshness, rate limits, user agent |
| WMO Severe Weather Information Centre / official feeds | Official severe weather alerts | Feed availability, authority and issuing-service attribution |
| FOSS Public Alert Server | Additional CAP alert aggregation | Operational maturity, terms, feed duplication, fallback-only status |

Provider rules:
- Prefer documented APIs over scraping.
- Read current terms before coding an adapter.
- Store source metadata declaratively: identity, operator, links, access method, coverage, features, key/cost requirements, cadence, attribution, rate limits.
- A source failure must be isolated and recorded.
- Website-reading sources, if any, are best-effort and never the only source for a critical feature.
- Test adapters offline with recorded fixtures.

---

## 10. Forecasting and scientific verification

1. Start with the simplest defensible baseline, not a complex adaptive model.
2. The first multi-model blend uses equal weights unless archive evidence says otherwise.
3. Preserve model spread and uncertainty; do not imply precision the inputs do not support.
4. Skill-based weighting and bias correction are later capabilities, not part of the first forecast adapter.
5. Any change to blending, interpolation, thresholds, station selection, or correction must be tested against a reproducible historical archive.
6. Report metrics by variable and forecast lead time. Use appropriate metrics, including MAE/RMSE for continuous values and Brier score for precipitation probabilities.
7. Compare with individual models and the previous baseline; do not claim improvement from anecdotal examples.
8. Rain/radar nowcasting is a later phase after the model-based engine is stable.

---


## 11. Milestones and acceptance criteria

Each milestone follows the same cycle: **research → specification → owner approval → implementation in a separate coding task → deterministic tests → platform check → documentation update.**

| Milestone | Scope | Acceptance criteria |
|---|---|---|
| M0 — Project foundation | Repository policies, license, README, KMP workspace skeleton, CI skeleton, coding-agent instructions | Clean repository; documented decisions; CI runs on PRs |
| M0.1 — Platform and dependency proof | KMP Android, iOS framework, JVM desktop on Windows/macOS/Linux; Ktor and SQLDelight driver checks | The same probe works on all five OS; runtime and packaging requirements documented; owner approves stack |
| [M0.2 — Public API and storage decision](docs/specs/M0_2_PUBLIC_API_STORAGE.md) | Stable domain/API contract, optional local storage boundary, cache/history semantics, offline behaviour, errors and cancellation | API and storage contracts tested with an independent sample consumer; owner approves decisions before M1 |
| [M0.3 — Reproducible verification, research, and optional chat integration](docs/specs/M0_3_VERIFICATION_RESEARCH.md) | Forecast/observation record contracts, matching rules, reproducible evaluation methodology, human-reviewed improvement workflow, contributor AI policy, optional MCP adapter assessment | Analysis is reproducible from versioned data and code; MCP remains an optional external adapter; the weather core needs no AI provider, model, credential, or token spend |
| M1 — First forecast | Domain model and one Open-Meteo forecast path | CLI and binding sample return hourly/daily forecast with source and timestamps; fixtures pass |
| M2 — Provenance and resilience | Per-value provenance, source catalog, source selection, typed problems, cache/rate limits | Every value traceable; failed/stale provider produces explicit fallback/problem; request limits tested |
| M3 — Official alerts | CAP parsing, WMO feeds, FPAS as an additional route | Mexico, US, and Germany fixtures parse/merge correctly; update/cancel and attribution tests pass |
| M4 — Observations | Airport station discovery and METAR mapping | Fresh nearby observation selected per variable; stale/distant/elevation-mismatched stations rejected |
| M5 — Blend and spread | Multi-model forecast, equal-weight blend, spread | Reproducible archive evaluation; wind direction and missing-model edge cases pass |
| M6 — Consumer integration | Solstice Kotlin integration; sample native Swift/iOS and desktop JVM integrations | Solstice bridge uses only public API; each advertised binding/package is tested |
| M7 — Local learning | Forecast/observation matching, rolling skill statistics, bias correction | Archive evaluation shows measured impact; privacy and retention rules pass |
| M8 — Radar nowcast research | Radar/satellite input, motion estimation, short-range extrapolation | Separate feasibility study and benchmark before implementation approval |

Milestones may be reordered only by an explicit decision recorded in `PLAN.md`. M6 contains app-side work; its app-specific implementation instructions remain in Solstice.


## 12. CI, tests, and release artifacts

### CI responsibilities

- Gradle/Kotlin build, common tests, lint/format checks, and offline fixture tests.
- Build and test Android targets on appropriate Linux/Android runners.
- Build iOS frameworks and run simulator tests on macOS/Xcode runners.
- Build and run the JVM desktop artifact on Windows, macOS, and Linux runners.
- Verify Ktor engines and SQLDelight drivers on the targets where they are used.
- Integration tests must exercise the actual packaged artifact, not only compile shared source.
- Native Kotlin/Native Windows/Linux jobs are optional and must not block the initial JVM-desktop release.

### Release artifacts to validate

| Consumer | Candidate artifact |
|---|---|
| Android | KMP Android/Gradle library artifact |
| iOS / Swift | Kotlin/Native framework or XCFramework with generated Objective-C/Swift-facing API |
| Windows | JVM desktop artifact, distributed with or requiring a compatible Java runtime |
| macOS | JVM desktop artifact; optional macOS ARM64 framework for a native Swift consumer |
| Linux | JVM desktop artifact, distributed with or requiring a compatible Java runtime |
| Native desktop apps without a JVM | Not part of the initial promise; revisit Rust or another native ABI if a real consumer requires it |

Do not promise a package format until a proof of concept is built and loaded on its target.

## 13. Security, privacy, and licensing

- Never commit provider credentials, tokens, local config, private data, or secrets.
- Never log raw coordinates or sensitive location context unnecessarily.
- Read provider terms and required attribution before integration.
- Review direct and transitive dependency licences, maintenance, and target support.
- Preserve Apache-2.0 notices and a clear third-party notices inventory.
- Do not add telemetry or remote sharing without explicit approval and separate legal/security review.
- Validate parser input sizes, malformed payloads, timeouts, redirects, and decompression limits.

---

## 14. Documentation map

| File | Single responsibility |
|---|---|
| `RULES.md` | General project and agent working rules |
| `CLEAN_ROOM.md` | Independent research and implementation policy |
| `PLAN.md` | Master plan, decisions, milestones, acceptance criteria |
| `AGENTS.md` | Instructions for coding agents operating in a code environment |
| `AI_POLICY.md` | Governance for AI-assisted engineering and occasional manual research; prohibits AI runtime dependencies |
| `docs/ENGINE_SPEC.md` | Weather engine behaviour and domain requirements |
| `docs/MERIDIAN_PLAN.md` | Supporting research and earlier design notes; not authority for the toolchain |
| `docs/specs/M*` | Milestone-specific specifications |
| `docs/verification/` | Reproducible archive evaluation reports and datasets documentation |
| `CHANGELOG.md` | Released changes |
| `README.md` | What Meridian is, current maturity, usage and supported platforms |

Do not copy the Solstice UI plan into Meridian. Solstice keeps its own consumer-integration plan.

---


## 15. Decisions and open questions

### Existing project decisions

- Product name: Meridian.
- Repository: `INFORMATION89/Meridian-Engine`.
- Licence target: Apache-2.0 (repository bootstrap must add the full licence and notice).
- Meridian is an independent library with no UI and no dependency on Solstice.
- Meridian does not own saved-place lists or refresh scheduling.
- Data provenance and source attribution are mandatory.
- Clean-room policy is maintained separately in `CLEAN_ROOM.md`.

### Recommendations awaiting owner approval

1. Kotlin Multiplatform as the engine implementation, with Android and iOS targets plus Desktop JVM on Windows/macOS/Linux.
2. Desktop applications may bundle or require a compatible JVM; no native Windows/Linux library is promised in v1.
3. SQLDelight with SQLite as the official optional local persistence implementation; no mandatory Room dependency.
4. A batch-oriented first public API; defer streaming partial updates.
5. M0.1 and M0.2 as mandatory gates before weather-feature implementation.
6. Meridian has no AI runtime integration. AI_POLICY.md governs AI-assisted development and research; any future AI/ML runtime proposal requires a separate specification and explicit owner approval.
7. Meridian must have an independent build, API smoke test, and example consumer that do not require Solstice.

### Questions for later milestones

- Must Windows/macOS/Linux consumers be able to embed a native library without a JVM, or is a JVM desktop artifact acceptable?
- Should a native Swift macOS consumer be supported in v1, or is the JVM desktop target enough?
- Which Java runtime version should desktop applications bundle or require?
- Which Linux distributions and architectures should be in the first support matrix?
- Are Windows ARM64 and Linux ARM64 desktop builds first-release requirements or follow-up targets?
- Should apps be allowed to provide a custom storage backend in v1, or should the official SQLDelight/SQLite implementation be the only supported persistence path initially?
- Which Open-Meteo model set is defensible for Mexico and other target regions at the time M5 is implemented?


## 16. Immediate next task for a coding agent

**Task:** execute only M0.1, the platform and dependency proof described in section 5 and specified in `docs/specs/M0_1_PLATFORM_SPIKE.md`.

The coding agent must:
1. Read `RULES.md`, `CLEAN_ROOM.md`, this `PLAN.md`, `AGENTS.md`, and the M0.1 task specification.
2. Inspect the actual repository and report what exists before creating files.
3. Create a short-lived feature branch for the spike.
4. Build the smallest KMP library possible with a shared deterministic probe, typed error, and async operation.
5. Build and test the Android target, an iOS framework/Swift consumer, and the Desktop JVM artifact on Windows, macOS, and Linux.
6. Validate one Ktor and one SQLDelight/SQLite path on the actual target source sets.
7. Record exact commands, toolchain versions, build-host details, target outcomes, runtime requirements, artifact sizes, and failures.
8. Do not build a Rust/UniFFI comparator unless the owner states that native desktop libraries without a JVM are required, or the KMP proof shows a blocker that merits comparison.
9. Open a pull request with the evidence and a recommendation. Do not merge it or begin M1 without owner approval.

**Prohibited during M0.1:** implementing weather providers, domain algorithms, production storage, blending, learning, alerts, radar, or app UI. The proof is not the production engine.

## 17. Research references checked for the platform decision

- [Kotlin Multiplatform compatibility guide (Kotlin/Gradle/AGP/Xcode)](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
- [Kotlin Multiplatform platform stability](https://kotlinlang.org/docs/multiplatform/supported-platforms.html)
- [Kotlin/Native target support and host requirements](https://kotlinlang.org/docs/native-target-support.html)
- [SQLDelight supported platforms and SQLite drivers](https://sqldelight.github.io/sqldelight/)
- [Ktor client engines](https://ktor.io/docs/client-engines.html)
- [GitHub Actions hosted runner selection and platform matrix](https://docs.github.com/en/actions/how-tos/write-workflows/choose-where-workflows-run/choose-the-runner-for-a-job)
- [Android Jetpack KMP support tiers](https://developer.android.com/kotlin/multiplatform)
- [Rust target platform support](https://doc.rust-lang.org/stable/rustc/platform-support.html)
- [Rust iOS target requirements](https://doc.rust-lang.org/stable/rustc/platform-support/apple-ios.html)
- [UniFFI user guide and supported binding languages](https://mozilla.github.io/uniffi-rs/latest/) (Rust alternative only)

Platform tiers describe compiler/toolchain support, not a guarantee that every third-party dependency, binding, or package works. Revalidate these references before each major platform release.
