# Meridian — Master Plan

**Repository:** INFORMATION89/Meridian-Engine  
**Product:** Meridian  
**Status:** Owner-approved product direction and master-plan baseline. Technical implementation choices remain subject to the evidence gates below; this plan does not authorize production implementation before those gates are met.  
**Last reviewed:** 2026-11-26  
**Authority:** This file is the single master source for product scope, architectural direction, delivery stages, and acceptance criteria. Milestone specifications define implementation details only after their gates are approved. Research documents provide evidence and options; they do not independently authorize implementation. Older documents and consumer-repository copies must be reconciled with this plan before agents treat them as current instructions.

## 1. Executive direction

Meridian is an **independent, reusable, local-first meteorological engine**. It obtains weather information from suitable external sources, normalizes and evaluates that information, combines available models and observations, applies evidence-based processing, and returns structured meteorological results with uncertainty, freshness, quality, provenance, and explanations.

Meridian is a library/engine, not a weather application. It does not own screens, visualizations, navigation, saved-place management, notifications, user accounts, or application lifecycle. Consuming applications decide how to present results and when to request updates. Solstice is an important first consumer, but Meridian must be independently buildable, testable, distributable, and useful to unrelated applications.

### The product's meteorological intelligence

Meridian's differentiating goal is **Level 2 weather intelligence built on existing forecasts and observations**. It should improve the usefulness of available information through:

- selecting relevant sources and models for each region, variable, and forecast horizon;
- evaluating source quality, coverage, freshness, reliability, and historical performance;
- combining multiple existing forecast models without assuming that more sources always mean better results;
- correcting systematic errors and adapting to local conditions when verified evidence supports it;
- learning from eligible observations while keeping personal data local by default;
- estimating and communicating uncertainty, disagreement, probability, and confidence honestly;
- detecting and tracking weather phenomena when the available evidence supports it.

Meridian will **not build its own fundamental numerical weather-prediction or global climate models**. Its own algorithms belong in data quality, source selection, statistical/physical/hybrid post-processing, blending, calibration, regional adaptation, event analysis, and verification. A new method is valuable only when repeatable evidence shows that it improves results against a relevant baseline.

This intelligence is a core product objective, not an optional unrelated experiment. It will be delivered progressively: early prototypes may have fewer capabilities, but Meridian must not claim its intended mature intelligence until source combination, regional evaluation, uncertainty, and validated improvements meet the stable-release criteria.

### Local-first and no-hosting principle

Normal Meridian operation must not require the project owner or a consumer to operate, rent, or maintain a Meridian backend.

- The engine runs in the consuming application's process or through an explicitly chosen local integration.
- Weather, observation, and official-alert requests go directly from the device to eligible providers, subject to provider terms, credentials, platform rules, rate limits, and network availability.
- There is no mandatory Meridian cloud API, account, authentication service, remote database, synchronization system, telemetry collector, AI service, or always-on daemon.
- Forecast cache, local verification, learning statistics, settings supplied to the engine, and optional historical records remain local by default.
- Apps own permissions, lifecycle, refresh scheduling, saved places, credential storage, presentation, and notification policy.
- Offline operation can use suitable cached data and local calculations, but must disclose age, freshness, coverage, and limitations. Old data must never be presented as current.
- Optional external contribution or sharing is separate from normal operation and off by default. Before a separate, explicit opt-in, explain exactly what data is collected, why it is needed, where it is sent, who receives it, and how long it is retained; provide an inspectable payload example and controls to withdraw consent and delete eligible local data. Declining must not reduce ordinary weather functionality. Sharing does not authorize creating a Meridian-hosted server. Any hosted collection service would require a separate strategic decision and privacy/legal review.

This does not promise that weather information is available without the Internet or that every provider permits direct requests from distributed apps. Provider eligibility and credential handling must be checked before integration. Private developer credentials must never be embedded in distributed artifacts.

### Platform and distribution objective

The design target is Windows, macOS, Linux, Android, and iOS. A common product API and shared meteorological semantics are required, but this does not imply that one binary format runs everywhere or that every programming language can load every artifact directly.

There will be **one GitHub Release per Meridian version**, containing platform-appropriate libraries, frameworks, packages, SDK material, source, documentation, checksums, and signatures as supported by the validated toolchain. These are engine integration artifacts, not necessarily standalone user-facing executables. Native and managed application compatibility must be stated honestly per target.

### AI boundary

AI is a tool for contributors, not a runtime requirement. The shipped engine must remain deterministic and usable without a generative-AI provider, model, prompt, AI credential, inference runtime, MCP server, vector database, or token budget. Contributor AI use is governed by the repository's AI policy and must be independently verified. AI-generated hypotheses are not meteorological evidence.

## 2. Product principles

1. **Independent core:** Meridian has no dependency on Solstice, Breezy Weather, or any consuming application.
2. **Consumer neutrality:** one public weather contract serves unrelated applications, tools, and platforms.
3. **No UI:** Meridian returns structured data; consumers own maps, charts, cards, text, notifications, and visual explanations.
4. **Local-first privacy:** no accounts, tracking, telemetry, location history, or external sharing by default.
5. **Scientific honesty:** observations, model forecasts, blends, estimates, derived values, corrections, detections, and predictions remain distinguishable.
6. **Provenance by default:** every meaningful value can be traced to its inputs, source, time, and transformations.
7. **Evidence before activation:** experimental capabilities do not become stable merely because they compile or produce plausible examples.
8. **Resilience:** one provider failure must not invalidate unrelated sources or features; fallbacks remain visible.
9. **Efficiency:** requests, tests, background work, storage, CPU, memory, and battery are budgeted and justified.
10. **Reproducibility:** scientific and technical claims can be repeated from versioned data, code, parameters, and documented environments.
11. **No fundamental forecast-model project:** Meridian improves existing weather information; it does not attempt to replace numerical weather prediction centers.
12. **Specification before implementation:** every substantive capability has scope, evidence, acceptance criteria, and a reviewed implementation task.
13. **Human authority over strategic boundaries:** product, privacy, legal, infrastructure, licensing, and major compatibility changes require owner review.
14. **Technical autonomy within approved boundaries:** agents and maintainers should be able to choose reasonable implementation details, internal structures, test approaches, and supporting dependencies when justified, documented, and verified. They should not interrupt the owner for routine technical choices. The detailed permissions must be reconciled into RULES.md and AGENTS.md before ordinary implementation is delegated under this model.

## 3. Scope, goals, and non-goals

### Product goals

- Current conditions and forecasts up to the configured horizon, with a default target of 10 days.
- Hourly forecast information for ordinary variables; finer precipitation timing, targeting 15-minute resolution where a source genuinely provides it and the data supports it.
- Multiple forecast models and meteorological sources, selected according to region, variable, horizon, freshness, quality, coverage, availability, and terms.
- A combined forecast with contributors, weights, disagreement, uncertainty, and provenance.
- Current observations where appropriate, selected per variable using quality and representativeness rather than distance alone.
- Official alerts preserved with their issuing authority and CAP lifecycle.
- Detection and tracking of relevant weather phenomena, including precipitation, snow, storms, extreme wind, heat, cold, fog, and severe phenomena, when data and validated methods permit.
- Local learning and correction from verified observations, with safeguards against overfitting, bad sensors, and regional bias.
- Historical forecast and observation records for reproducible verification.
- Separate historical meteorological and climatological context, including statistical references, anomalies, and regional trend analysis where datasets support them.
- Radar/satellite integration and progressive nowcasting research, with honest geographic coverage and confidence.
- Structured API/SDK results for native and managed applications, plus local developer diagnostics and CLI tools.
- Structured meteorological outputs and derived weather-event data (for example, precipitation onset, severe-wind periods, and heat/cold events), with supporting variables, timing, source provenance, quality, probability, and uncertainty. Meridian does not generate user-facing lifestyle advice or application-specific recommendations.
- Global design and provider assessment, with early quality validation prioritized in Mexico and then expanded across different meteorological regions.

### Product non-goals

- Building a new global numerical weather-prediction model or fundamental climate model.
- A Meridian-owned weather application, visual interface, map renderer, chart renderer, notification manager, or saved-place UI.
- Mandatory hosting, cloud processing, remote synchronization, accounts, or central telemetry.
- Generative AI in the production weather request path.
- Automatic self-modifying algorithms or unreviewed promotion of new weights, corrections, or models.
- Guaranteeing equal source coverage, forecast quality, radar access, or 15-minute resolution everywhere.
- Treating a provider's model output, a radar image, model agreement, or an estimate as a verified observation or official warning.

### First stable release versus the complete vision

A narrow internal prototype may precede the full product. The first stable release, however, must include a reliable multiplatform foundation, documented sources, a multi-model forecast combination, uncertainty and source provenance, meaningful regional evaluation, validated source-selection and post-processing behaviour, privacy controls, and a stable consumer API.

The mature Level 2 goal remains the direction of the product. More advanced local corrections, event tracking, climatological analysis, and radar nowcasting can be introduced in later validated stages. Where evidence is insufficient for a region or feature, Meridian must clearly identify the limitation rather than claim universal intelligence or accuracy.

## 4. Platform compatibility and support promises

### Required design targets

| Platform | Candidate integration path | Important constraint to validate |
|---|---|---|
| Android | Multiplatform Android library or native library with bindings | Supported ABIs, Android lifecycle, networking, storage, and artifact consumption |
| iOS | Framework/XCFramework or native library with Swift-facing bindings | Swift API ergonomics, cancellation, Xcode toolchain, simulator/device architectures |
| Windows | JVM library/package or native library with bindings | Whether target applications can host the required runtime or need a native ABI |
| macOS | JVM library/package or native framework | Swift/native desktop integration may differ from JVM application integration |
| Linux | JVM library/package or native library | Supported distributions, architectures, runtime dependencies, packaging |

A platform is **Supported** only when the artifact is built, loaded, exercised through its intended consumer boundary, and tested in CI on the declared target. **Build-only** means it compiles but runtime integration is not verified. **Experimental** means the path is known but not promised. **Not tested** must remain explicit.

A successful JVM test on Windows does not prove that an arbitrary native Windows application can embed Meridian. A successful iOS framework build does not prove physical-device compatibility. Publish a compatibility matrix by OS, architecture, artifact type, runtime requirement, and tested consumer language.

## 5. Technology and interoperability decision

### Desktop distribution direction: JVM-first (owner-approved)

The owner selected the JVM-packaged desktop path for the initial desktop distribution direction. Windows, macOS, and Linux desktop consumers will use the JVM artifact. Meridian's application-level distribution guidance should support bundling a compatible Java runtime (for example, a minimized runtime image) so end users of an application do not need to install Java separately. A generic Meridian library artifact does not itself contain or impose an application runtime; the consuming application controls its final packaging.

This decision prioritizes shared implementation, lower initial build and maintenance cost, and reuse of the validated desktop JVM path. It does **not** mean every desktop integration must use JVM forever. Native `.dll`, `.dylib`, and `.so` artifacts, C ABI, Rust bindings, and direct native-language integration remain future options if concrete consumer requirements justify their cost. Do not build or promise those native artifacts in M0.1.

### Production language and shared-core strategy: owner-approved

The owner has approved **Kotlin Multiplatform (KMP)** as Meridian's shared-core technology direction after reviewing the M0.1 evidence. This decision is recorded in [ADR-0003](docs/decisions/ADR-0003-approved-production-technology-direction.md). It is an independent product/engineering choice, not a consequence of Solstice using Kotlin. Rust remains a possible future interoperability or replacement option only if a concrete consumer requirement or measured blocker justifies revisiting the decision.

The owner also approved **Ktor** as the initial HTTP implementation behind Meridian-owned networking contracts. **SQLDelight/SQLite** is the preferred optional persistence candidate, not a mandatory core dependency or a final irreversible storage commitment. Exact engine configuration, public contracts, storage schema, drivers, migrations, and retention remain subject to M0.2 design and evidence. The JVM-first desktop route remains governed by ADR-0001.

### What the current M0.1 evidence establishes

The active canonical M0.1 proof is [PR #15](https://github.com/INFORMATION89/Meridian-Engine/pull/15), targeting `main` from `work/m0.1-platform-proof-refresh`. Earlier PRs were superseded during reconstruction; their evidence must not be confused with the current branch's final validation.

Run [#222](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45872259210) passed all five jobs on commit `91996fb7c190f80899261eb2ff21630b5e564209`: Android x86_64, Apple/iOS, Linux desktop, Windows desktop, and dependency-license audit. It is the first complete green matrix after the expanded real-engine timeout, transport, empty/partial-response, and cancellation tests. Android's artifact confirms 8/8 instrumentation tests; iOS confirms 9/9 common simulator tests and 10/10 native Swift consumer tests; macOS desktop confirms 11/11 common/storage tests. The real Android, Darwin, and CIO fixture requests and consumer-boundary cancellation checks all passed.

This is the platform and dependency evidence baseline supporting the owner's subsequent KMP direction approval recorded in ADR-0003. Android ARM64 runtime and physical Android/iOS device execution remain **NOT TESTED** and are deferred under the owner's CI-only M0.1 acceptance. The exact platform results, package sizes, artifact contents, and distribution limits are recorded in [MER-0.1 platform validation](docs/evidence/MER-0.1-platform-validation.md).

### KMP implementation considerations

Potential advantages:
- shared Kotlin domain and engine logic;
- Android and iOS framework integration;
- JVM desktop execution across Windows, macOS, and Linux;
- direct fit for Kotlin consumers such as Solstice.

Costs and limitations:
- a desktop JVM artifact is not a native library for arbitrary C, C++, Swift, C#, or Rust desktop applications;
- desktop runtime packaging and size must be evaluated;
- Swift interoperability, asynchronous calls, cancellation, and native resource lifecycle need consumer-boundary tests;
- every dependency must support the exact Android/iOS/JVM target matrix;
- KMP platform support does not automatically guarantee third-party driver or package compatibility.

### Deferred alternative — Rust native core

Potential advantages:
- native libraries can be produced for supported target architectures;
- a clearer common native boundary for non-JVM consumers;
- generated bindings can help Kotlin and Swift integration.

Costs and limitations:
- native packaging, FFI, memory ownership, async/cancellation, threading, and cross-language error mapping;
- separate build and test environments for platform ABIs;
- additional language/toolchain and dependency maintenance;
- third-party crate support must be validated rather than inferred from Rust target availability.

### Decision criteria

The original M0.1 comparison criteria are retained as historical rationale; the production direction is now approved. Reopen the choice only if new evidence meets a revisit trigger in ADR-0003. Do not run a Rust comparator by default.

Historical decision criteria:
1. all five OS targets and declared architectures;
2. the types of applications expected to consume Meridian, not only Solstice;
3. integration from Kotlin, Swift, and at least one non-Kotlin desktop consumer where required;
4. native versus managed runtime requirements;
5. artifact size, startup, CPU, memory, battery, and packaging;
6. build reliability and CI cost;
7. dependency support, licences, maintenance, security, and supply-chain risk;
8. API ergonomics, cancellation, threading, and failure isolation;
9. release automation and long-term maintainability.

Do not build a Rust comparator by default. If evidence exposes a concrete interoperability or runtime blocker, document it and seek owner review before changing the approved direction.

### Approved M0.2 technology baseline

- KMP is the approved shared-core direction (ADR-0003).
- Ktor is the approved initial HTTP implementation, hidden behind Meridian-owned contracts.
- SQLDelight/SQLite is the preferred optional storage candidate; the core must remain storage-independent and no-storage is the default.
- Android Room is not a Meridian core dependency. Solstice may use Room for Solstice-owned app data.
- Host-provided storage remains possible if the contract and lifecycle can be proven.
- Desktop consumers use the JVM artifact under ADR-0001; native desktop ABI is not promised.
- Exact Ktor engines, storage drivers, serializers, date/time libraries, public API signatures, schema, and retention policy are resolved through M0.2 evidence and applicable owner review.

### M0.2 current execution status

M0.2 is authorized for contract design and deterministic independent-consumer validation. The design documents are `docs/specs/M0_2_CONTRACT_DESIGN.md`, `docs/specs/M0_2_PUBLIC_API.md`, and the separate lifecycle/networking, storage/cache, and compatibility/versioning specifications. The evidence ledger is `docs/evidence/MER-0.2-contract-validation.md`; the static reuse review is `docs/evidence/MER-0.2-m0.1-implementation-reuse-audit.md`.

M0.2 is not production weather implementation. It must not add live provider integrations, forecast algorithms, background scheduling, automatic location, telemetry, accounts, hosted services, or consumer recommendations. Completion requires contract tests and independent consumer evidence, followed by owner review before M1.

## 6. Proposed architecture

The architecture is logical and modular. It does not require creating every module at once.

### Core responsibilities

1. **Domain and contracts:** places, coordinates, variables, units, time semantics, source descriptors, observations, forecast records, events, alerts, result states, uncertainty, provenance, and typed problems.
2. **Engine orchestration:** plan requested features, select eligible sources, coordinate requests, enforce budgets, reuse valid results, manage fallbacks, and assemble partial or complete results.
3. **Provider adapters:** isolate one provider's protocol, schema, credentials, licence, attribution, rate limits, coverage, and failure modes.
4. **Data quality:** validate structure, units, physical ranges, time consistency, spatial context, source quality flags, and anomalies.
5. **Forecast intelligence:** normalize model outputs, compare candidates, blend forecasts, estimate spread, calibrate probabilities, and apply only validated post-processing.
6. **Observation processing:** assess stations and measurements per variable, including age, calibration evidence, location, elevation, representativeness, and reliability history.
7. **Event analysis:** distinguish observations, current detections, estimates, and future predictions; maintain event identity and evolution only when continuity is supported.
8. **Historical and climate context:** preserve licensed historical datasets and produce statistical references, anomalies, and regional trend analyses separately from current forecasts.
9. **Storage boundary:** optional cache, forecast archive, observation history, local statistics, and historical datasets, with clear retention and deletion semantics.
10. **Platform integrations:** narrow HTTP, storage, secure credential, time, and concurrency implementations where operating systems genuinely differ.
11. **Developer tooling:** local CLI, diagnostics, fixture utilities, request inspection, and reproducible verification reports.

### Dependency direction

- The domain/core owns normalized contracts and does not depend on providers, databases, UI frameworks, or consuming apps.
- Provider adapters depend on core contracts; core never depends on provider DTOs.
- Processing and algorithms receive validated normalized data and do not perform direct network requests.
- Storage implementations depend on storage contracts; core does not depend on a database vendor.
- Platform-specific drivers are injected or selected through narrow factories.
- Consumer applications use the public API only. They must not import private adapter or persistence types.
- Tests and verification tools must run without checking out or building Solstice.

A minimal independent consumer is required as a compatibility test. It is not a separate user-facing sample application for every platform.

## 7. Data storage, history, cache, and privacy

Meridian must distinguish different kinds of stored data instead of applying one retention rule to everything.

### Data categories

1. **Response cache:** short-lived provider responses or normalized feature data, with source identity, fetch time, expiry, freshness, schema version, and integrity information.
2. **Issued forecast archive:** snapshots of what a source/model forecast at a particular issue time, retained so later verification does not substitute a stitched historical series or reanalysis for an actual forecast run.
3. **Observation history:** measured values with station/source identity, measurement and fetch times, quality flags, units, and only the spatial precision needed for matching.
4. **Local learning history:** recent forecast/observation comparisons and regional statistics used for source evaluation and corrections. A 30-day rolling window has been discussed but is not an approved retention commitment; a bounded duration must be proposed with evidence and owner review before persistent collection.
5. **Long-term meteorological history:** historical weather datasets and climatological references with independent licensing, coverage, versioning, and retention policies. These remain separate from any bounded recent learning window.
6. **Derived summaries:** reproducible statistics, anomalies, calibration summaries, and model skill records that identify their source datasets and calculation versions.

### Storage rules

- Local-first operation is the default, but persistent storage is disabled unless the consumer explicitly enables and configures it. No remote database or synchronization is mandatory.
- The host owns saved places and application settings; Meridian must not become a location-history tracker.
- Every dataset records provenance, licence, source, time coverage, geographic coverage, resolution, quality, and restrictions.
- Cache expiry follows provider cadence, HTTP metadata, terms, and rate limits.
- Stale data retains its original timestamps and freshness state.
- Retention is configurable per data category, with bounded defaults, explicit deletion, export/restore where appropriate, atomic writes, schema versioning, and recovery.
- Historical data redistribution is prohibited unless its licence permits it.
- Optional user contributions require separate explicit consent and minimal data. Before consent, disclose the data fields, purpose, destination, recipients, and retention; provide inspectable payload examples, local controls, withdrawal, and eligible deletion. Declining must not reduce ordinary weather functionality.

## 8. Normalized data and public API contract

The public API should expose stable, typed meteorological concepts rather than provider response objects or database rows.

### Data semantics

- Use normalized internal units and explicit verified conversions. Consumers decide display units and locale formatting.
- Store instants unambiguously and use IANA time zones for local calendar interpretation and daylight-saving transitions.
- Keep issue time, observation/measurement time, fetch time, valid time, and forecast lead time distinct.
- Missing, unavailable, invalid, stale, expired, estimated, and zero values are different states.
- Preserve the source's native temporal and spatial resolution. Interpolation, aggregation, and resampling must be explicit.
- Keep observed, modelled, blended, corrected, estimated, detected, and predicted values distinguishable.
- Keep actual air temperature and apparent/feels-like temperature as separate values. Preserve the method and source for apparent temperature; do not imply it is a personal measurement. Heat index and wind chill remain distinct named quantities where available.

### Provenance and explanations

Every meaningful result should expose, where available:
- provider, model, station, official authority, or dataset identity;
- required attribution and relevant licence metadata;
- issue, observation, fetch, and valid times;
- requested and actual geographic reference, grid, station distance, elevation, and resolution;
- contributors, weights, transformations, corrections, and algorithm/configuration versions;
- source quality, freshness, coverage, fallback, and failure/skip reasons;
- uncertainty, model disagreement, sample counts, and validation status.

The API returns structured explanations and diagnostics. It does not render graphs, maps, cards, panels, or visualizations. Developers can inspect information through the CLI or structured API/SDK; each application chooses its own presentation.

### Consumer-owned recommendations and policies

Meridian returns **meteorological facts and engine-derived weather events**, not application advice. Its contract may expose observations, forecast values, indices, official alerts, event detections, probabilities, timing, supporting variables, provenance, quality, uncertainty, and neutral explanations of how a meteorological result was derived.

Meridian must not generate or own user-facing lifestyle recommendations such as what to wear, whether to carry an umbrella, sunscreen/shade advice, packing lists, activity suggestions, or personalized daily planning. It must not own consumer profiles, preferences, notification schedules, alert wording, UI, or application-specific defaults. These require context and product choices that belong to the consuming application.

Each consumer owns the policy that maps meteorological evidence to user action:
- recommendation content and presentation, including clothing, umbrella, sunscreen, shade, and activity advice;
- user preferences, profiles, schedules, notification channels, wording, and cooldowns;
- app-specific alert thresholds and rules.

Meridian may detect and describe meteorological events using validated, consumer-neutral methods. Where a consumer needs a configurable event threshold, the consumer supplies that policy; Meridian must not silently apply Solstice-specific defaults. Official alerts remain source-authored records with their authority and lifecycle preserved; consumers decide how to present or notify them.

**Solstice ownership:** Solstice's “what to wear” experience, UV action guidance, precipitation alert thresholds, notification timing, and cooldown rules remain Solstice features. Meridian supplies the underlying weather values, event evidence, and uncertainty only. The authoritative application behavior belongs in Solstice's own plan and implementation, not in Meridian's API or core.

### Probability and confidence

Do not confuse:
- the percentage of models that agree or cross a threshold;
- an individual provider's precipitation probability;
- an ensemble-derived probability;
- a calibrated probability verified against observations;
- uncertainty or spread across model outcomes;
- a qualitative confidence label.

The share of deterministic models above a rain threshold is model agreement, not automatically a calibrated probability of rain. Preserve individual probabilities and ensemble information where available. A probability can be described as calibrated only after a suitable historical reliability evaluation.

Confidence must be contextualized by variable, region, season, phenomenon, lead time, source coverage, and sample size. Lack of data must not be translated into unjustified certainty or an automatic numerical penalty without a defined method.

### Events and phenomena

An event result must identify its information type explicitly:
- **Observation:** a source directly measured or reported a condition.
- **Current detection:** Meridian's method identifies a phenomenon in available current evidence.
- **Estimate:** a derived value or inferred condition, not a direct measurement.
- **Prediction:** a possible future condition with a stated horizon and uncertainty.

Where continuity can be supported, an event should preserve identity and evolution over time, including location/trajectory, intensity, timing, changes, sources, evidence, uncertainty, and lifecycle status. If evidence is insufficient to confirm an event or connect successive detections, return indications and limitations rather than inventing continuity.

Official alerts remain distinct from Meridian's own detections and predictions. Meridian does not become an issuing authority.

## 9. Sources, providers, and request strategy

The provider shortlist in research documents is a starting point, not a permanent commitment. Each source must be reviewed at the time of integration.

### Provider admission criteria

Evaluate:
- operator and source identity;
- documented API and terms of service;
- licence, attribution, redistribution, and commercial-use restrictions;
- geographic coverage and supported variables;
- spatial and temporal resolution and update cadence;
- freshness, historical reliability, and known limitations;
- authentication and credential handling;
- request cost, rate limits, minimum intervals, and stability;
- privacy implications and whether direct client requests are permitted;
- overlap or shared upstream dependencies with other sources.

Prefer documented APIs. A website-reading adapter is best-effort and must not be the only source for a critical feature. Do not treat two models as independent when they share data, assimilation, or upstream dependencies without recording that relationship.

### Dynamic source selection

Choose sources per requested feature and location, considering coverage, data type, quality, freshness, resolution, lead time, reliability, user/host configuration, legal eligibility, and request budget. A single global rank such as official, regional, global, fallback is not sufficient for every variable.

If a source fails, use an eligible alternative only when its data semantics match. Preserve the failed source and reason, fallback identity, changes in coverage/quality, and any remaining gap. Never silently relabel stale or fallback data as primary-source data.

### Request efficiency

- Query only what the requested features need.
- Reuse compatible fresh results and avoid duplicate in-flight requests.
- Respect provider minimum intervals, budgets, cache headers, concurrency, backoff, and platform constraints.
- Prioritize essential weather and safety-relevant information over secondary enrichment when budgets are constrained.
- At a usage limit, reduce nonessential requests, use valid cache or alternatives, and disclose reduced capabilities.
- Do not assume a single API call has a fixed cost when provider accounting depends on requested models, variables, or forecast range.

Initial research candidates include Open-Meteo for forecasts and model data, AviationWeather.gov for aviation observations, official WMO/national authority feeds for alerts, and additional CAP aggregators where their maturity and terms justify their use. No source is permanently approved by appearing in this table or in a research document.

## 10. Forecast intelligence, verification, and scientific safeguards

### Baseline and blending

- Establish a simple, reproducible baseline before adaptive processing.
- Begin with neutral/equal model weights where model availability and evidence justify it.
- Evaluate models independently by region, variable, forecast horizon, season, and relevant weather regime.
- Adapt weights only when adequate verified evidence supports an improvement.
- Preserve every input model value, actual contributor list, normalized weights, missing-data decisions, and spread.
- Wind direction must be handled with vector components, not arithmetic averaging of angles.
- Daily summaries should be derived from hourly data only where temporal semantics support it.
- A blend must be compared with the best relevant individual model and the previous Meridian baseline.

### Calibration and regional learning

- Compare issued forecasts with later quality-controlled observations using versioned matching rules.
- Prevent data leakage: observations unavailable at forecast issue time cannot be used as inputs to that forecast's evaluation.
- Keep tuning and final evaluation periods separate, preferably with chronological holdouts.
- Evaluate errors by region, variable, lead time, season, and weather condition where sample sizes allow.
- Apply local corrections only when station representativeness, observation quality, sample size, and independent validation are sufficient.
- Guard against overfitting, sensor faults, station changes, outliers, correlated data, and sudden performance degradation.
- Every correction has provenance, bounded influence, a disable/fallback path, and a route to rollback.
- Never automatically promote an algorithm or weights solely because a new observation arrived.

### Metrics

Choose metrics before evaluating the candidate and use measures appropriate to the task:
- continuous variables: MAE, RMSE, bias, and suitable distribution metrics;
- precipitation probabilities: Brier score and calibration/reliability diagnostics for a clearly defined event;
- event detection: precision/recall and categorical scores with explicit event definitions;
- model agreement: spread, range, standard deviation, and contributor count;
- source operations: coverage, freshness, missingness, latency, errors, and request success.

Report sample counts, exclusions, missing data, confidence intervals or uncertainty where appropriate, and regressions as well as gains. An aggregate score must not hide a degraded region or variable.

### Historical and climate analysis

Historical weather patterns provide statistical context and reference behaviour; they do not replace current model forecasts. Historical datasets must be versioned and evaluated by licence, quality, temporal and geographic coverage, resolution, and source methodology.

Regional trend analysis must use appropriate periods, adequate data quality, and uncertainty. Meridian must not assert causality from a statistical trend without evidence capable of supporting that conclusion. Distinguish observations, forecasts, historical analyses, and reanalysis products.

### Radar and nowcasting safeguards

Radar/satellite nowcasting is progressive research, not a universal first-release promise. Before implementation for a region, establish data access and rights, coverage, latency, scan interval, georeferencing, calibration, quality control, precipitation-rate semantics, compute cost, and benchmark data.

Combine available radar sources only after correcting and evaluating their differences. Preserve each observation's origin and quality. Start with precipitation detection and tracking, then motion, intensity, development, and dissipation when validated. Satellite-derived precipitation must not be labelled as radar observation. Confidence is evaluated by region, radar coverage, precipitation type, and forecast horizon. Outside coverage or when imagery is poor, use a disclosed alternative or return a limitation.

## 11. Milestones and acceptance criteria

Every milestone follows the same cycle: **research → written specification → review/approval → scoped implementation → targeted tests → applicable platform checks → evidence report → documentation update**. A proposal or research document is not an implementation authorization.

| Milestone | Scope | Completion gate |
|---|---|---|
| M0 — Repository foundation | Governance, Apache-2.0 licence and notices, README, CI, coding-agent instructions, documentation hierarchy | Repository identity, licence file, rules, and CI baseline are consistent and verifiable |
| M0.1 — Platform proof | KMP five-OS proof, Ktor engines, SQLDelight drivers, consumer interoperability and packaging evidence | Evidence reviewed; production technology direction approved in ADR-0003; remaining packaging gaps stay explicit and PR #15 remains subject to owner review |
| M0.2 — Public API and storage | Consumer-neutral API for meteorological facts/events only (no application recommendations), storage boundary, caching, history, offline states, partial results, errors, cancellation, versioning | Independent consumer contract tests pass; API/storage decisions approved before weather-feature implementation |
| M0.3 — Verification foundation | Forecast/observation records, matching, data quality, archived datasets, metrics, leakage controls, reproducible reports and human-reviewed improvement workflow | A forecast evaluation can be reproduced from versioned data/code; AI is not required |
| M1 — First forecast path | Normalized current, hourly, and daily forecast from one eligible provider; 10-day default where supported | CLI and independent consumer return correct units, time semantics, source, freshness, and typed problems |
| M2 — Provenance and source resilience | Source catalog, eligibility, provider limits, request planning, cache/fallback, per-value provenance | Failure, stale data, rate limits, partial success, and source-selection reasons are correctly exposed |
| M3 — Official alerts | CAP parsing, official authorities, updates, cancellations, deduplication, geographic scope | Recorded official alerts retain authority, identity, validity, status, instructions, and attribution |
| M4 — Observations | Station discovery, measurement quality, spatial/elevation representativeness, variable-level selection | Unrepresentative or poor observations are rejected with traceable reasons |
| M5 — Multi-model forecast | Model selection, initial blend, spread, disagreement, probability semantics | Reproducible archive comparison against individual models and prior baseline; no false calibrated-probability claims |
| M6 — Independent consumer compatibility | Public API integration in more than one consumer shape, including Solstice as one consumer and non-Solstice contract samples | Meridian builds and runs without Solstice; packages and public APIs are validated for each advertised consumer path |
| M7 — Level 2 regional intelligence | Regional source/model skill, calibration, verified weighting, bounded bias correction, local adaptation | Measured improvements on independent regional/variable/horizon evaluations; unsafe corrections remain disabled |
| M8 — Phenomena and event tracking | Current detections, estimates, predictions, event identity, evolution, uncertainty, and distinction from official alerts | Event lifecycle and evidence semantics pass deterministic and meteorological validation |
| M9 — Historical and climatological context | Versioned historical datasets, regional reference statistics, anomalies, comparisons, trend analysis | Dataset rights/quality/coverage and statistical methods are documented; causality is not overclaimed |
| M10 — Radar and nowcasting | Multi-radar/satellite quality handling, precipitation detection/tracking, movement and short-term evolution | Separate regional feasibility and benchmark prove value, coverage, freshness, accuracy, resource cost, and fallbacks before release |

The milestone order may be adjusted only when dependency evidence justifies it and the plan is updated. Research for a later stage may happen earlier, but implementation remains gated. Solstice integration is a consumer milestone; it never changes the engine's independence.

### Stable-release gate

A stable release requires:
- supported artifact and consumer evidence for each advertised OS/runtime;
- reliable current/hourly/daily forecast path and documented provider rights;
- multiple-model forecast combination with traceable inputs and honest spread/uncertainty;
- regional/variable/horizon evaluation adequate for every claimed capability;
- validated source-selection and any enabled correction/adaptation;
- clear distinction between observation, model output, blend, correction, detection, estimate, prediction, and official warning;
- secure local storage, freshness, cache and failure behaviour;
- privacy, credential, dependency, licence, and supply-chain review;
- stable, versioned public API and tested independent consumer integration;
- complete documentation, CLI diagnostics, declared limitations, rollback and support policy.

Radar, climatology, and every advanced phenomenon need not be falsely marked complete just to publish a stable engine. They must be explicitly experimental, unsupported, or enabled only where their own acceptance criteria are met. The stable release must still deliver the core Level 2 objective through validated multi-source intelligence and regional evaluation.

## 12. CI, tests, and release artifacts

### Cost-aware verification

`AI_COST_POLICY.md` is the approved authority for AI, tooling, CI, provider, dataset, and compute efficiency. Choose verification according to changed risk, reuse valid evidence, and avoid irrelevant or duplicate work; never weaken a required security, compatibility, scientific, or release gate to save cost.

- Use layered tests: unit, contract, adapter, integration, platform, meteorological/archive, security, and release checks.
- Run fast, relevant checks on each change; use path-aware selection where safe.
- Run full platform matrices when shared core, public API, platform drivers, dependencies, or release artifacts are affected, and before publishing a version.
- Schedule or manually trigger expensive historical/archive evaluations when algorithms, weights, data matching, or forecast behaviour change.
- Avoid duplicate tests that prove the same behaviour at the same boundary. Prefer one strong contract test plus focused boundary-specific tests.
- Do not skip security, compatibility, or meteorological checks merely to save CI. Risk-based test selection must be documented and must preserve a full release gate.
- Tests should use deterministic fixtures and fakes by default. Live provider requests are not part of routine pull-request verification.
- A failed required check blocks merge/release. Any exception must be explicit, scoped, justified, and approved.
- Report tests not run, platform cells not tested, and scientific checks separately from compilation.

### Release strategy

Publish one GitHub Release per version. Each release should contain, as validated:
- platform-specific library/framework/package assets;
- source code and build metadata;
- SDK/API documentation and integration snippets;
- CLI and developer diagnostics tools where applicable;
- hashes, provenance, and signatures or attestations supported by the release process;
- dependency and third-party licence notices;
- compatibility matrix, migration notes, changelog, support status, and known limitations.

A desktop library artifact and a desktop application installer are not the same thing. Do not call a JAR a universal native library or promise a native ABI unless the build produces and tests one. If a desktop package needs a bundled JVM, state that requirement and measure the package. Consumers already installed must remain functional if GitHub is unavailable; the project must not rely on GitHub as a runtime service.

## 13. Security, privacy, licensing, and supply chain

- Meridian's selected project licence is Apache License 2.0. The complete root LICENSE and required notices must be present before the first public release. The repository currently needs this bootstrap state to be verified on main.
- Review direct and transitive dependencies for licence compatibility, security, provenance, maintenance, supported targets, size, and necessity.
- Complete a third-party licence inventory for runtime, test, build, and packaging dependencies. Add NOTICE or other notices when required by licences.
- Never commit API keys, tokens, user data, private datasets, environment files, signing credentials, or local configuration.
- Provider credentials belong to the consumer application and its secure storage unless a provider's supported model explicitly differs. Never ship private developer keys in public packages.
- External communications must be necessary, documented, secure, and visible in diagnostics where appropriate.
- Logs should minimize personal data and avoid raw coordinates or secrets unless a narrowly scoped local diagnostic explicitly requires them.
- Protect local data with platform-appropriate permissions and secure storage where relevant; apply retention and deletion rules.
- Updates and artifacts must be traceable and verifiable through hashes, signatures/attestations, dependency records, and reproducible build evidence where practical.
- Isolate adapters and extensions with least privilege, bounded inputs, timeouts, resource limits, and failure containment.
- Vulnerabilities require a responsible disclosure path, severity assessment, verified fixes, notices, and security releases.
- No telemetry or contribution sharing is enabled by default. Any future remote collection requires a separate approved architecture, consent model, legal review, and operational-cost assessment.

## 14. Documentation authority and cleanup

Maintain one source of truth per topic.

| Document | Responsibility and authority |
|---|---|
| PLAN.md | Master product direction, architecture boundaries, milestones, and acceptance gates |
| RULES.md | Project-wide working, testing, security, and contribution rules; must be reconciled with the approved technical-autonomy model |
| AGENTS.md | Operational instructions for coding agents; must point to the master plan and current milestone |
| AI_POLICY.md | Contributor AI use and the prohibition on runtime AI requirements |
| AI_COST_POLICY.md | Approved policy for cost-aware AI, tools, CI, providers, datasets, and compute without weakening verification |
| `docs/agent-guides/CLAUDE_CODE_USAGE.md` | Optional operational guide for managing Claude Code subscription usage, context, and bounded sessions; not a model requirement |
| AI_IMPLEMENTATION_PLAN.md | Detailed, task-oriented execution guide for coding agents; subordinate to this master plan and its approved milestone gates |
| PROJECT_OVERVIEW.md | Concise human-readable explanation of Meridian's purpose, scope, roadmap, and status; derived from this master plan, not an independent authority |
| CLEAN_ROOM.md | Independent research and implementation policy |
| docs/specs/M* | Scoped milestone specifications; proposals until their approval gate is met |
| docs/research/* | Evidence, alternatives, and technical/scientific analysis; not independent implementation authority |
| docs/ENGINE_SPEC.md | Existing weather-behaviour specification; reconcile against this plan before using it as current implementation direction |
| docs/MERIDIAN_PLAN.md | Earlier supporting research and proposals; superseded where it conflicts with this plan |
| README.md | Public identity, maturity, supported platforms, installation/integration, licence, and limitations |
| CHANGELOG.md | Released changes only |
| docs/verification/* | Versioned reproducible technical and meteorological verification records |

### Required documentation reconciliation

Before the first production weather milestone:
1. Reconcile docs/ENGINE_SPEC.md and docs/MERIDIAN_PLAN.md with the approved scope, particularly language assumptions, Room versus SQLDelight, streaming versus batch API, history retention, source selection, learning, and radar stages.
2. Ensure Solstice's copy of the Meridian specification remains an integration reference only and does not become Meridian's source of truth.
3. Clarify that a Git submodule or composite build may be a developer convenience, not the official distribution requirement.
4. Replace stale implementation instructions that conflict with current milestones or owner decisions.
5. Reconcile RULES.md, AGENTS.md, and AI_POLICY.md with technical autonomy, strategic approval boundaries, current PR/CI workflow, and cost-aware testing/context management.
6. Keep provider/model-specific guides under docs/agent-guides/ optional, concise, and dated; update or retire them when the provider's product, usage meter, context controls, model, or pricing changes.
6. Keep `AI_IMPLEMENTATION_PLAN.md` and `PROJECT_OVERVIEW.md` synchronized with this master plan. They are derived views for different audiences, not independent sources of requirements; update them whenever an approved master-plan change affects their content.

Do not copy Solstice's UI plan into Meridian. Solstice keeps its own application and consumer-integration plan.

## 15. Decisions in force and remaining technical gates

### Product and scope decisions in force

- Meridian is an independent meteorological engine; Solstice is only one consumer.
- Windows, macOS, Linux, Android, and iOS are target platforms.
- The engine has no UI or visualizations and does not own app-side notifications, accounts, saved places, or lifecycle.
- The default forecast horizon is 10 days where supported; ordinary forecasts are hourly and precipitation detail may be 15-minute where genuine source data supports it.
- The product objective is Level 2 intelligence over existing weather models and observations, not a new fundamental numerical weather-prediction model.
- Initial model weights may be neutral/equal; adaptive selection, weights, calibration, and corrections require evidence and validation.
- Uncertainty, probabilities, provenance, freshness, and source differences are first-class outputs.
- Learning/verification history is local-first and must have a bounded, purpose-specific retention policy. A 30-day window is a proposal only until the relevant scientific/storage specification and owner review approve it.
- Historical climatological records are separate from short rolling learning records.
- All relevant phenomena may be recognized when sources and methods support them; detections are not official alerts.
- Radar and nowcasting progress from detection/tracking toward motion and evolution only after regional feasibility and validation.
- No mandatory Meridian hosting, accounts, telemetry, AI service, or external sharing.
- Apache-2.0 is the selected project licence.
- Releases are one GitHub Release per version with separate platform-specific artifacts.
- Desktop distribution direction is JVM-first for Windows, macOS, and Linux; consuming applications may bundle a compatible runtime for self-contained delivery. Native desktop libraries remain a future option requiring evidence and approval.
- AI may assist engineering and research but is not a runtime dependency.
- Application-specific recommendations and user-facing action policies belong to consumers, not Meridian ([ADR-0002](docs/decisions/ADR-0002-application-owned-recommendations.md)).

### Technical decisions not yet locked

These are evidence gates, not reasons to interrupt the owner with routine programming questions:
- any future change to the owner-approved KMP shared-core direction (ADR-0003);
- non-JVM consumer bindings and future native desktop interoperability; desktop distribution is owner-approved as JVM-first with a bundled-runtime option for applications that need self-contained delivery; native desktop libraries remain a future interoperability option, not a current requirement;
- platform artifact and packaging matrix;
- storage implementation and host-provided storage contract;
- exact public API shape, cancellation, errors, and compatibility rules;
- HTTP engine matrix, timeouts, lifecycle, and error mapping;
- exact provider/model list and provider eligibility by region;
- historical archive formats, datasets, licence, and retention;
- event representation and validated detection algorithms;
- radar sources and regional processing methods.

The responsible agent should research these, recommend a bounded solution, record trade-offs, run relevant proofs, and ask the owner only when the answer changes product scope, privacy, legal obligations, costs, strategic compatibility, or a fundamental architecture commitment.

## 16. Immediate next work and implementation gates

### Current repository status at this revision

- [PR #4](https://github.com/INFORMATION89/Meridian-Engine/pull/4) merged the architecture and weather research documents as reference material. Their recommendations remain proposals unless adopted here or in an approved milestone.
- [PR #6](https://github.com/INFORMATION89/Meridian-Engine/pull/6) merged the approved `AI_COST_POLICY.md` into `main`; its requirements apply to all subsequent AI-assisted work and verification.
- Earlier proof PRs were closed/superseded during reconstruction; their closure did not waive M0.1 acceptance criteria.
- [PR #15](https://github.com/INFORMATION89/Meridian-Engine/pull/15) is the active canonical MER-0.1 PR. Its current working branch is `work/m0.1-platform-proof-refresh`; the branch head must be checked against GitHub before any evidence is accepted. Do not merge it until the current-head CI and all evidence/review gates are complete.
- A completed platform matrix on [run 45777929849](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45777929849), commit `aa5e86ec1cab1d302b9f2165327deddf299f48dc`, passed Linux, Windows, macOS/iOS, and Android. It records an Android x86_64 emulator, an ARM64 iOS simulator with a Swift consumer, and the same desktop JVM consumer on Windows, macOS, and Linux. Android ARM64 runtime and physical Android/iOS device execution remain **NOT TESTED**; CI-only validation is accepted for this M0.1 stage and those cells must stay explicit.
- The optimized CI removed two serial dependency-metadata jobs, added the pinned open-source Gradle cache path, and avoided duplicate Android emulator downloads. The measured baseline was 21m15s and the first completed optimized run was 12m38s (40.5% lower wall-clock time); this comparison predates the final SBOM and Android SDK cleanup changes and is not a performance guarantee.
- A preliminary CycloneDX audit completed successfully on [run 45779632538](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45779632538), commit `faaeb43fd08fa0d9f1865b1d8c8a800e484b95d6`. The initial SBOM had zero unresolved module metadata, four project records without license metadata, and 13 distinct license labels; the report still needs Gradle configuration classification and upstream review. A later run on that commit had Linux/Android Gradle plugin-resolution failures; do not treat that run as a green platform matrix.
- [CI #222](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45872259210) passed all five jobs on code commit `91996fb7c190f80899261eb2ff21630b5e564209`; Android instrumentation passed 8/8, iOS simulator/common passed 9/9, native Swift passed 10/10, and macOS desktop common/storage passed 11/11. See [platform validation evidence](docs/evidence/MER-0.1-platform-validation.md).
- The latest CycloneDX audit in CI #222 reports 402 unique third-party Maven coordinates, zero missing third-party license metadata, zero unresolved dependency metadata, 54 non-test runtime coordinates (53 Apache-2.0 and one MIT), and five multi-license coordinates. All 58 SBOM-only coordinates were traced to plugin/buildscript dependency graphs; the five multi-license entries were reviewed as lint/test/build tooling rather than desktop runtime.
- Linux, Windows, and macOS JVM app-images from CI #222 were inspected directly. All contain root `LICENSE` and `THIRD-PARTY-NOTICES.md` and retain their bundled JDK legal directories. Measured app-image sizes: Linux 183.0 MiB, Windows 169.7 MiB, macOS 171.7 MiB.
- The CI artifact does not produce an Android APK/AAB or an iOS app distribution package. The iOS XCFramework is built and tested, but its current test artifact does not yet stage root `LICENSE` and `THIRD-PARTY-NOTICES.md` alongside the framework; mobile release-package notice inclusion remains a release-packaging requirement, not a claim established by the desktop checks.

### Current next task: complete M0.2 contract design and independent proof

1. Preserve CI #222 as inherited M0.1 platform evidence for code commit `91996fb7c190f80899261eb2ff21630b5e564209`; do not treat it as validation of M0.2 contracts or as a newer run.
2. Keep Android ARM64 runtime, physical Android/iOS devices, and native desktop ABIs explicitly **NOT TESTED**. Mobile release-package notice staging/inspection remains required before public distribution.
3. Keep PR #15 open until its exact head, CI, evidence, and owner review are checked. This is a merge/review gate, not a reason to block the already approved M0.2 design work. Do not merge it without explicit owner approval.
4. Keep the four M0.2 contracts aligned with the experimental API prototype. The static M0.1 reuse audit is recorded; keep probe-only code out of the production API. Retention and persistent-data policy remain unresolved; do not silently adopt a 30-day window.
5. Compile and validate the new Kotlin common API, common tests, Desktop JVM consumer, and Swift consumer on the exact branch head. The source exists, but no M0.2 build/test/CI result is claimed yet. Resolve any compiler or generated Swift API issues before expanding lifecycle/networking behaviour.
6. Extend deterministic contract proof for in-flight cancellation, close races, normalized failures, true partial-result aggregation, storage-disabled operation, optional storage semantics, and version separation. Update the evidence ledger with exact commits and results.
6. Submit the complete M0.2 package for owner review. Do not begin M1 weather-provider or forecast implementation until the M0.2 acceptance gate is passed.

### After M0.2 approval

1. Complete M0.3 reproducible forecast/observation verification and research safeguards.
2. Present the first weather-feature specification with source terms, data quality, scientific baseline, and deterministic acceptance criteria.
3. Only then begin the first production weather capability under its own approved scope and evidence gate.
4. Add each later capability through its own milestone; do not create a large all-at-once production workspace or enable every planned feature prematurely.

### Prohibited until the corresponding gate is approved

- Do not begin production weather providers, blending, local learning, alerts, radar, or application UI as part of the M0.1 spike.
- Do not merge PR #15 or authorize M1 based solely on a green CI run for an earlier commit.
- Do not treat deferred alternatives as selected: Rust native core, mandatory SQLDelight persistence, Room in Meridian core, streaming APIs, MCP, or hosted services require their own evidence and approval. KMP and Ktor are approved directions; SQLDelight remains an optional preferred candidate.
- Do not change Solstice, Motion, or another consumer repository as part of this Meridian master-plan revision.

## 17. Research references

Re-check official documentation, terms, compatibility tables, and licence status immediately before implementation; these links are research entry points, not permanent version locks.

### Platform and tooling

- [Kotlin Multiplatform compatibility guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
- [Kotlin Multiplatform supported platforms](https://kotlinlang.org/docs/multiplatform/supported-platforms.html)
- [Kotlin/Native target support](https://kotlinlang.org/docs/native-target-support.html)
- [Ktor client engines](https://ktor.io/docs/client-engines.html)
- [SQLDelight documentation](https://sqldelight.github.io/sqldelight/)
- [Rust target platform support](https://doc.rust-lang.org/stable/rustc/platform-support.html)
- [UniFFI user guide](https://mozilla.github.io/uniffi-rs/latest/)
- [GitHub Actions runner selection](https://docs.github.com/en/actions/how-tos/write-workflows/choose-where-workflows-run/choose-the-runner-for-a-job)

### Meteorology, observations, alerts, and data

- [WMO Guide to Instruments and Methods of Observation](https://community.wmo.int/site/knowledge-hub/programmes-and-initiatives/instruments-and-methods-of-observation-programme-imop/guide-instruments-and-methods-of-observation-wmo-no-8-0)
- [OASIS Common Alerting Protocol](https://www.oasis-open.org/standard/cap/)
- [Open-Meteo forecast API](https://open-meteo.com/en/docs)
- [Open-Meteo Previous Runs API](https://open-meteo.com/en/docs/previous-runs-api)
- [Open-Meteo Single Runs API](https://open-meteo.com/en/docs/single-runs-api)
- [AviationWeather.gov Data API](https://aviationweather.gov/data/api/)
- [WMO Severe Weather Information Centre](https://severeweather.wmo.int/)

### Internal research baselines

- [Architecture review](docs/research/ARCHITECTURE_REVIEW.md)
- [Weather data model](docs/research/WEATHER_DATA_MODEL.md)
- [Weather intelligence strategy](docs/research/WEATHER_INTELLIGENCE.md)
- [Provider research](docs/research/PROVIDER_RESEARCH.md)
- [Remaining-work checklist](docs/research/REMAINING_WORK.md)

Research references and internal notes must be refreshed when an implementation milestone begins. Do not claim that a dataset, model, service, endpoint, licence, or platform is currently available without rechecking its current terms and status.
