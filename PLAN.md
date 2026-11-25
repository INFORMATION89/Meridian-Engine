# Meridian — Master Plan

**Repository:** `INFORMATION89/Meridian-Engine`  
**Product/library:** Meridian  
**Status:** Draft for owner review; technical direction recommended, not irrevocably approved  
**Last reviewed:** 2026-11-25  
**Authority:** This file is the master plan for scope, architecture, technical decisions, milestones, and acceptance criteria. `docs/ENGINE_SPEC.md` defines weather behaviour; `docs/MERIDIAN_PLAN.md` is supporting research. Where their older technology assumptions conflict with this plan, this plan is the current proposal and the conflict must be resolved before implementation.

---

## 1. Executive direction

Meridian is an independent, reusable weather-data and forecasting engine. It accepts a place and requested weather features, obtains data from appropriate providers, normalizes and evaluates the inputs, and returns useful weather results with uncertainty, freshness, source attribution, and complete provenance.

Meridian is a library, not a weather application. It has no screens, app navigation, user-facing design system, saved-place manager, or app-specific behaviour. Solstice is its first consumer, not its owner. Other apps and tools should be able to use the same engine without depending on Solstice.

### Recommended technical direction

**Do not assume Kotlin Multiplatform is the engine's implementation language.** The five-platform requirement changes the trade-off. The current recommendation is:

- **Rust for the shared engine and provider integrations.**
- **UniFFI-generated Kotlin bindings** for Android and Kotlin/JVM consumers such as Solstice.
- **UniFFI-generated Swift bindings** for native Apple consumers.
- A small, documented native interoperability surface for other desktop consumers where needed.
- Platform-specific packaging and build pipelines for Windows, macOS, iOS, Android, and Linux.

This is a recommendation, not yet a final lock-in. M0.1 must prove that the native library, generated bindings, packaging, and test matrix work before full engine implementation begins. If the proof fails or the maintenance cost is unreasonable, revisit Kotlin Multiplatform and other options using the same acceptance tests.

### Why revisit Kotlin

Kotlin Multiplatform can produce Linux and Windows native targets, so Kotlin is not impossible. However, its official support tiers are uneven for this exact target set: `linuxX64` and `linuxArm64` are Tier 2, `mingwX64` is Tier 3, and `macosX64` is deprecated in the current Kotlin/Native target table. Jetpack's platform support also lists Linux as Tier 2 and Windows as Tier 3. That is a meaningful risk for a library whose stated goal is broad native portability.

Rust has strong first-class host-tool targets for Windows x64/ARM64, Linux x64/ARM64, and Apple Silicon macOS; its iOS and Android ARM64 targets are Tier 2. That is not a guarantee for every third-party crate, but it gives a better starting point for a portable native core. UniFFI can generate Kotlin and Swift bindings, while still requiring us to build, test, and package the native binaries ourselves.

Kotlin remains important at the integration boundary: Solstice can consume a Kotlin API without making the weather engine Kotlin-only.

---

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

The engine is expected to support these operating systems. A platform is not considered supported merely because a compiler can emit a binary: the native library must load, perform a real fixture-backed operation, handle cancellation/errors safely, and pass the platform's integration tests.

| Platform | Initial architecture target | Additional target / later |
|---|---|---|
| Windows | x86_64, MSVC ABI | ARM64; validate packaging and signing |
| macOS | Apple Silicon ARM64 | Intel x86_64 if demand justifies it |
| iOS | ARM64 device | ARM64 simulator; Intel simulator only if needed |
| Android | ARM64 device | x86_64 emulator; other ABIs only if required |
| Linux | x86_64, glibc baseline | ARM64; musl builds after packaging tests |

### Native release matrix

The M0.1 spike must produce a written matrix showing for each target:

- compiler target triple and toolchain version;
- native artifact type (`.dll`, `.dylib`, `.so`, static archive, or Android package);
- host machine required to build it;
- whether it compiles, links, loads, and runs tests;
- binding language and minimum runtime dependencies;
- packaging format and unresolved limitations.

Apple binaries must be built with the Apple SDK/Xcode toolchain on macOS. Windows MSVC artifacts must be built and tested on a supported Windows runner. Do not claim these can all be produced from one generic CI host.

### Support levels

Use explicit labels:

- **Supported:** built and tested in CI and exercised through its real binding/package.
- **Build-only:** compiles and links, but runtime integration has not been verified.
- **Experimental:** known target; not promised to consumers.
- **Not supported:** no tested artifact or integration.

Do not use a vague "cross-platform" badge without publishing this matrix.

---

## 5. Language and interoperability decision

### Candidate A — Rust core (recommended)

**Advantages**
- Mature native targets across Windows, macOS, Linux, Android, and iOS.
- One engine implementation can be compiled to native libraries for each architecture.
- Strong type system, memory safety, and good tooling for a long-lived native library.
- UniFFI provides generated Kotlin and Swift bindings.
- A separate C ABI can make the core usable by additional languages if there is a real consumer.

**Costs and risks**
- Adds Rust to a project whose first consumer is Kotlin.
- Native binaries must be built, versioned, packaged, and distributed for each supported ABI.
- FFI error handling, cancellation, threading, async calls, object lifetime, and memory ownership require a deliberately small API.
- UniFFI is not a replacement for native build/release pipelines; its own version and generated bindings must be pinned and verified.
- Some third-party crates may have weaker mobile or platform support than Rust itself.

### Candidate B — Kotlin Multiplatform core

**Advantages**
- Kotlin is already used by Solstice, so the first integration is simpler.
- Shared Kotlin business logic and familiar Gradle tooling.
- KMP can target Android/iOS/JVM and also has Kotlin/Native targets for Linux and Windows.

**Costs and risks**
- Official target support is uneven for Windows/Linux native use.
- Several native platform dependencies do not support the same targets or support them at the same quality level.
- Jetpack libraries such as Room cannot be assumed to offer equal Windows/Linux support.
- A macOS Intel target is deprecated in the current Kotlin/Native target list.
- Maintaining platform-specific HTTP, storage, and packaging implementations may erase much of the apparent simplicity.

### M0.1 decision gate

Build a tiny, non-weather proof of concept before committing to the full engine:

1. A Rust library exposes a versioned data structure and one deterministic function.
2. UniFFI generates Kotlin and Swift bindings from the same public interface.
3. The Kotlin binding loads and calls the native library on Android ARM64 and at least one desktop JVM target (Windows or Linux).
4. The Swift binding loads and calls it on an iOS simulator/device build and macOS.
5. Native artifacts build for Windows x64, macOS ARM64, Linux x64, iOS ARM64, and Android ARM64.
6. Async calls, error mapping, cancellation, and shutdown are tested at least once.
7. CI can reproduce the supported builds on the appropriate runners.
8. The proof records build times, artifact sizes, packaging complexity, and the maintenance burden of generated bindings.

**M0.1 exit:** recommend Rust or reject it with evidence. The project owner approves the final language decision before M1.

---

## 6. Proposed architecture

The following is a logical architecture. Exact crate names and dependency choices are finalized by M0.1/M0.2, not guessed during feature implementation.

### Core layers

1. **Domain model:** places, coordinates, variables, units, time series, observations, alerts, source descriptors, result types, and typed problems.
2. **Engine/orchestrator:** requested-feature planning, source eligibility, request coordination, rate limiting, fallback, cache policy, aggregation, and result assembly.
3. **Source adapters:** one isolated adapter per provider. Provider DTOs remain inside the adapter.
4. **Forecast algorithms:** blending, spread, local verification, and later correction. Algorithms receive normalized data and do not perform network requests.
5. **Storage boundary:** explicit interfaces for cache and local verification history. The engine must not expose database entities in its public API.
6. **Interop boundary:** small FFI-facing types and methods. Keep it separate from internal domain types.
7. **Developer tools:** CLI, fixture utilities, and reproducible archive verification.

### Candidate Rust workspace layout

```text
Meridian-Engine/
  Cargo.toml
  Cargo.lock
  crates/
    meridian-domain/
    meridian-engine/
    meridian-source-openmeteo/
    meridian-source-aviationweather/
    meridian-source-wmo/
    meridian-source-fpas/
    meridian-storage-sqlite/     # optional official local storage
    meridian-ffi-uniffi/
    meridian-cli/
    meridian-testkit/
  bindings/
    kotlin/                       # generated/packaged Kotlin API
    swift/                        # generated/packaged Swift API
  docs/
    specs/
    verification/
  .github/
    workflows/
```

This is a proposal, not an instruction to create every crate immediately. M0 creates only the minimal workspace required by the approved spike. Add each module when a milestone needs it.

### Dependency direction

- Domain must not depend on providers, storage implementation, bindings, or consumer applications.
- Engine may depend on domain and provider/storage interfaces.
- Provider adapters implement the source contract and map vendor data into domain types.
- Optional storage implements storage contracts; the core never depends on SQLite APIs directly.
- FFI depends on the public engine API, not on provider-specific DTOs.
- Solstice, iOS apps, desktop apps, and CLI depend on the public library surface, never on internal source modules.

---

## 7. Storage, cache, and privacy

The earlier Kotlin-oriented Room proposal must not be carried over as a cross-platform requirement.

### Current recommendation

- Keep storage behind an internal abstraction.
- Evaluate an optional **SQLite-backed storage implementation** for the Rust core, with a dependency that builds on all required targets.
- Let applications decide whether they enable persistence and where the database file lives.
- Do not make Room a required part of Meridian. Solstice may retain Room for its own app data; Meridian's own cache/history must not require access to Solstice's database.
- A custom consumer-provided storage backend is desirable, but it must not force complex callback/FFI interfaces into the first release. Prove the extension mechanism before promising it as public API.

### Data policy

- Meridian does not own the list of saved places and does not maintain location history.
- Store only data required for cache, local forecast verification, and approved correction.
- A 30-day rolling history is the current research proposal, not a hard-coded default until its scope and storage cost are approved.
- Cache TTL and request spacing follow provider cadence, expiry headers, terms, and rate limits.
- Never let stale cached data masquerade as fresh data.
- Data remains local by default. Optional sharing of aggregated forecast errors requires a separate privacy, security, retention, and legal review.

---

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

Errors should be typed result values at the public boundary. Do not leak Rust panics, exceptions, HTTP client types, SQLite rows, provider JSON types, or platform-specific objects.

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
| M0 — Project foundation | Repository policies, license, workspace, CI skeleton, coding-agent instructions | Clean repository; documented decisions; CI runs on PRs |
| M0.1 — Technology and FFI spike | Rust vs KMP proof; native binaries; Kotlin/Swift bindings; cross-platform loading | Target matrix and packaging verified; written recommendation approved |
| M0.2 — Public API and storage decision | Stable domain/FFI contract; SQLite feasibility; HTTP and async strategy | API and storage specs approved; no feature code yet |
| M1 — First forecast | Domain model and one Open-Meteo forecast path | CLI and binding sample return an hourly/daily forecast with source and timestamps; fixtures pass |
| M2 — Provenance and resilience | Per-value provenance, source catalog, source selection, typed problems, cache/rate limits | Every value traceable; failed/stale provider produces explicit fallback/problem; request limits tested |
| M3 — Official alerts | CAP parsing, WMO feeds, FPAS as an additional route | Mexico, US, and Germany fixtures parse/merge correctly; update/cancel and attribution tests pass |
| M4 — Observations | Airport station discovery and METAR mapping | Fresh nearby observation selected per variable; stale/distant/elevation-mismatched stations rejected |
| M5 — Blend and spread | Multi-model forecast, equal-weight blend, spread | Reproducible archive evaluation; wind direction and missing-model edge cases pass |
| M6 — Consumer integration | Solstice Kotlin integration; sample native integrations for Swift/desktop | Solstice bridge uses only public API; each advertised binding/package is tested |
| M7 — Local learning | Forecast/observation matching, rolling skill statistics, bias correction | Archive evaluation shows measured impact; privacy and retention rules pass |
| M8 — Radar nowcast research | Radar/satellite input, motion estimation, short-range extrapolation | Separate feasibility study and benchmark before implementation approval |

Milestones may be reordered only by an explicit decision recorded in `PLAN.md`. M6 contains app-side work; its app-specific implementation instructions remain in Solstice.

---

## 12. CI, tests, and release artifacts

### CI responsibilities

- Rust formatting and linting (for example, `cargo fmt --check` and `cargo clippy`) and workspace tests.
- Offline fixture tests; normal PR tests must not depend on live weather services.
- Build native targets on suitable runners.
- macOS runner for iOS/macOS SDK builds and tests.
- Windows runner for MSVC build and runtime tests.
- Linux runner for Linux and Android NDK builds.
- Binding generation must be reproducible; generated code must not be hand-edited.
- Integration tests must load the actual packaged library, not merely compile the Rust crate.

### Release artifacts to validate

| Consumer | Candidate artifact |
|---|---|
| Android/Kotlin | Kotlin binding plus per-ABI native library packaged in an Android library/AAR |
| iOS/Swift | Swift binding plus a signed/packaged static or dynamic framework/XCFramework |
| macOS | Native library and Swift/Kotlin/JVM integration package as required by consumer |
| Windows | Native DLL with matching binding/runtime package |
| Linux | Native shared library with documented libc baseline and matching binding/runtime package |
| Other languages | C ABI only if a real consumer justifies the maintenance cost |

Do not promise a package format until a proof of concept is built and loaded on its target.

---

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

1. Rust as the engine implementation language, with UniFFI for Kotlin and Swift bindings.
2. The five-platform native support matrix in section 4.
3. A portable SQLite implementation as the official optional local storage backend; no mandatory Room dependency.
4. A batch-oriented first public API; defer streaming partial updates.
5. M0.1 and M0.2 as mandatory gates before feature implementation.

### Questions for later milestones

- Should macOS Intel be part of the first supported release or a best-effort build?
- Which Linux baseline should be supported (glibc minimum; musl as optional)?
- Are Windows ARM64 and Linux ARM64 first-release requirements or follow-up targets?
- Which language(s) should the desktop sample target?
- Should apps be allowed to provide a custom storage backend in v1, or should the official SQLite backend be the only supported persistence path initially?
- Which Open-Meteo model set is defensible for Mexico and other target regions at the time M5 is implemented?

---

## 16. Immediate next task for a coding agent

**Task:** execute only M0.1, the technology and interoperability proof described in section 5.

The coding agent must:
1. Read `RULES.md`, `CLEAN_ROOM.md`, this `PLAN.md`, and the current approved M0.1 task specification.
2. Inspect the repository and report what exists before creating files.
3. Create a short-lived feature branch for the spike.
4. Build the smallest Rust library and generated Kotlin/Swift bindings needed to test the real native boundary.
5. Build and run the smoke test on the available host platforms; cross-build other targets and report which steps cannot be run on the current host.
6. Record exact commands, toolchain versions, artifact sizes, target triples, CI runner requirements, and failures.
7. Compare the measured result with a small Kotlin Multiplatform alternative or document why it was not practical to build in the same spike.
8. Open a pull request containing the proof, results, and a recommendation. Do not merge it or choose the final stack without owner approval.

**Prohibited during M0.1:** implementing weather providers, domain algorithms, production storage, blending, learning, alerts, radar, app UI, or an entire production workspace. Do not migrate all Kotlin/Gradle dependencies into the Rust workspace until the owner approves the technology gate.

---

## 17. Research references checked for the platform decision

- [Kotlin Multiplatform platform stability](https://kotlinlang.org/docs/multiplatform/supported-platforms.html)
- [Kotlin/Native target support and host requirements](https://kotlinlang.org/docs/native-target-support.html)
- [Android Jetpack KMP support tiers](https://developer.android.com/kotlin/multiplatform)
- [Rust target platform support](https://doc.rust-lang.org/stable/rustc/platform-support.html)
- [Rust iOS target requirements](https://doc.rust-lang.org/stable/rustc/platform-support/apple-ios.html)
- [UniFFI user guide and supported binding languages](https://mozilla.github.io/uniffi-rs/latest/)
- [UniFFI Kotlin/Gradle integration](https://mozilla.github.io/uniffi-rs/latest/kotlin/gradle.html)

Platform tiers describe compiler/toolchain support, not a guarantee that every third-party dependency, binding, or package works. Revalidate these references before each major platform release.
