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

## 5. Approved production technology baseline

**Owner decision: approved on 2026-11-26.** The production baseline is Kotlin Multiplatform (KMP) for the shared meteorological core, Ktor for HTTP networking, and SQLDelight/SQLite as an optional local persistence implementation behind an engine-owned storage contract. Desktop distribution is JVM-first for Windows, macOS, and Linux. iOS consumers use the generated Apple framework through a deliberately Swift-friendly public facade where needed.

This approval closes the M0.1 production-stack decision gate and authorizes detailed M0.2 contract design on this baseline. It does **not** approve a final public API, storage schema, provider list, forecast algorithm, telemetry system, or production weather feature. Those remain subject to their own specifications, evidence, and approval gates.

### Shared core — Kotlin Multiplatform

- Put shared meteorological semantics, domain contracts, orchestration, validation, and platform-neutral algorithms in KMP common code wherever practical.
- Keep Android, iOS, and desktop-specific capabilities behind narrow platform interfaces/source sets.
- Keep public contracts free of Android, JVM, Swift, UI, provider DTO, and database-generated types.
- Treat Swift interoperability as a first-class design constraint. Add a small Swift-friendly facade where generated Kotlin wrappers would otherwise make the consumer API awkward.
- Preserve Meridian's independence: no Solstice dependency, no Solstice-specific types, and no requirement to check out Solstice to build, test, or consume the engine.

### Desktop distribution — JVM first

- Windows, macOS, and Linux use the Desktop JVM artifact as the initial supported integration path, subject to the explicit support matrix and tested runtime/architecture cells.
- Meridian's reusable library artifact does not bundle or impose an application runtime. A consuming application may require a compatible JVM or bundle a minimized runtime image for self-contained installation.
- The M0.1 probe app-images measured Linux 183.0 MiB, Windows 169.7 MiB, and macOS 171.7 MiB with bundled JDKs. These are probe measurements, not release-size guarantees.
- Native desktop shared libraries, C ABI, Rust bindings, and native Windows/Linux targets are deferred. Reconsider them only when a named consumer or measured interoperability, performance, or footprint requirement justifies the added maintenance and packaging cost.

### Networking — Ktor

- Ktor is the approved cross-platform HTTP client baseline. M0.1 validated Ktor 3.6.1 with Android, Darwin, and CIO engines on the tested targets.
- Select the platform engine through the appropriate source set; do not leak engine types into public contracts.
- M0.2 must specify client ownership/lifecycle, connect/request/socket timeouts, cancellation, redirects, proxy behaviour, TLS policy, typed error mapping, user-agent policy, and provider-specific limits.
- Keep one appropriately scoped client rather than constructing a new client per weather request. Reassess version upgrades through normal dependency maintenance and target verification; the approval is not a ban on future compatible updates.

### Local persistence — SQLDelight/SQLite

- SQLDelight 2.4.1 is the approved initial implementation baseline behind an engine-owned, consumer-neutral storage contract.
- Persistence remains optional. Meridian must provide a useful non-persistent mode and must not require a database merely to perform weather calculations.
- Do not expose SQLDelight-generated types or SQLite implementation details in the public API.
- M0.2 must specify schema ownership, migrations, transactions, concurrency, deletion/export, retention, cache expiry, verification-history retention, host-provided storage extension points, and failure behaviour.
- Preserve the reviewed dependency and licence notices, including SQLite JDBC/native components for desktop packaging.

### Platform evidence and explicit limitations

The five-job M0.1 matrix passed in [CI run #224](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45874428103) on the merged platform-proof branch; the underlying code validation is documented in [MER-0.1 platform validation](docs/evidence/MER-0.1-platform-validation.md). The proof exercised Android x86_64 emulator, iOS ARM64 simulator with a native Swift consumer, macOS desktop JVM, Windows desktop JVM, and Linux desktop JVM. The actual Android, Darwin, and CIO HTTP engines and the tested SQLDelight drivers were exercised through deterministic local fixtures.

The following remain **NOT TESTED** and must not be advertised as verified: Android ARM64 runtime, physical Android/iOS devices, native desktop ABIs, and final Android/iOS release-package inclusion of project and third-party notices. These are tracked as platform/release limitations, not reasons to reopen the selected baseline. Mobile release packaging and notice verification are required before public mobile distribution.

### Rust position

Rust/UniFFI is not part of the initial production implementation. It is deferred because the validated KMP path meets the currently approved five-OS integration objective with fewer languages, toolchains, bindings, and packaging paths. This is a pragmatic scope decision, not a claim that Rust is inferior. Reconsider only with concrete consumer evidence that KMP/JVM cannot meet a required native ABI, runtime, performance, footprint, or interoperability constraint; any proposed strategic replacement requires owner review.

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
4. **Local learning history:** recent forecast/observation comparisons and regional statistics used for source evaluation and corrections. The agreed initial rolling window is 30 days; it remains configurable and bounded.
5. **Long-term meteorological history:** historical weather datasets and climatological references with independent licensing, coverage, versioning, and retention policies. These are not limited automatically to the 30-day learning window.
6. **Derived summaries:** reproducible statistics, anomalies, calibration summaries, and model skill records that identify their source datasets and calculation versions.

### Storage rules

- Local storage is the default. No remote database or synchronization is mandatory.
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
| M0.1 — Platform proof | KMP five-OS proof, Ktor engines, SQLDelight drivers, consumer interoperability and packaging evidence | **Complete:** CI #224 passed all five jobs, limitations are documented, and the owner approved the production baseline in ADR-0003. Mobile release-package notices and untested device/ABI cells remain explicit pre-release or future validation items. |
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
- Learning history is local-first, with an initial 30-day rolling window for recent verification/learning records, configurable and bounded.
- Historical climatological records are separate from short rolling learning records.
- All relevant phenomena may be recognized when sources and methods support them; detections are not official alerts.
- Radar and nowcasting progress from detection/tracking toward motion and evolution only after regional feasibility and validation.
- No mandatory Meridian hosting, accounts, telemetry, AI service, or external sharing.
- Apache-2.0 is the selected project licence.
- Releases are one GitHub Release per version with separate platform-specific artifacts.
- Desktop distribution direction is JVM-first for Windows, macOS, and Linux; consuming applications may bundle a compatible runtime for self-contained delivery. Native desktop libraries remain a future option requiring evidence and approval.
- AI may assist engineering and research but is not a runtime dependency.
- Application-specific recommendations and user-facing action policies belong to consumers, not Meridian ([ADR-0002](docs/decisions/ADR-0002-application-owned-recommendations.md)).

### Approved technical baseline

- Shared core: Kotlin Multiplatform.
- Networking: Ktor, with Android, Darwin, and CIO engines selected by target.
- Optional local persistence: SQLDelight/SQLite behind an engine-owned storage contract.
- Desktop: JVM-first for Windows, macOS, and Linux; consumers choose whether to bundle a compatible runtime.
- iOS: Apple framework consumed through a Swift-friendly public facade.
- Rust/UniFFI and native desktop ABIs: deferred unless concrete consumer evidence justifies reconsideration.

See [ADR-0003](docs/decisions/ADR-0003-approved-production-technology-baseline.md). These choices authorize M0.2 contract design, not premature production feature implementation.

### Technical decisions still to be specified or validated

- Exact public API shape, cancellation, errors, lifecycle, and compatibility/versioning rules.
- Platform artifact/architecture matrix and Swift facade ergonomics.
- Storage schema, migrations, transactions, concurrency, retention, deletion/export, and host-provided storage extension contract.
- HTTP lifecycle, timeout budgets, TLS/proxy/redirect policy, and typed error mapping.
- Exact provider/model list and provider eligibility by region.
- Historical archive formats, datasets, licence, and retention.
- Event representation and validated detection algorithms.
- Radar sources and regional processing methods.

The responsible agent should research these, recommend a bounded solution, record trade-offs, run relevant proofs, and ask the owner only when the answer changes product scope, privacy, legal obligations, costs, strategic compatibility, or a fundamental architecture commitment.

## 16. Immediate next work and implementation gates

### Current repository status

- [PR #15](https://github.com/INFORMATION89/Meridian-Engine/pull/15) merged the MER-0.1 platform proof into main. CI [#224](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45874428103) passed all five jobs.
- The owner approved KMP + Ktor + optional SQLDelight/SQLite, JVM-first desktop, and a Swift-friendly iOS facade. The decision is recorded in [ADR-0003](docs/decisions/ADR-0003-approved-production-technology-baseline.md).
- [PR #19](https://github.com/INFORMATION89/Meridian-Engine/pull/19) records the formal approval, reconciles project instructions, and updates the M0.2 handoff. It is open for review; do not claim it is merged until GitHub confirms.
- [PR #16](https://github.com/INFORMATION89/Meridian-Engine/pull/16) was closed as superseded after its branch became stale. Its accepted meteorological-versus-consumer responsibility boundary has been preserved in ADR-0002 and the current master-plan/PR #19 changes.
- Android x86_64 emulator, ARM64 iOS simulator with Swift consumer, and JVM desktop consumers on Windows, macOS, and Linux were exercised. The dependency/license audit passed.
- Android ARM64 runtime, physical Android/iOS devices, native desktop ABIs, and final mobile release-package notice inclusion remain **NOT TESTED**. Mobile notice staging and verification are mandatory before public mobile distribution.
- The probe app-image sizes (Linux 183.0 MiB, Windows 169.7 MiB, macOS 171.7 MiB) are not release-size guarantees.
- [PR #17](https://github.com/INFORMATION89/Meridian-Engine/pull/17) remains a separate draft concerning AI model-routing guidance. It has not received its optional advanced review and is not a blocker for M0.2.
- [PR #18](https://github.com/INFORMATION89/Meridian-Engine/pull/18) concerns the versioned GitHub Wiki publisher and its one-time WIKI_TOKEN setup; it is separate from the engine architecture decision.
- [PR #20](https://github.com/INFORMATION89/Meridian-Engine/pull/20) now includes an isolated, opt-in M0.2 contract spike and fake-source consumers for desktop, Android, and Swift. Its API shapes remain experimental and subject to owner review. CI run [#233](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45909041572) passed Linux, Windows, Android (including x86_64 emulator), and dependency/license audit; its first Swift test was cancelled during branch synchronization. The latest bounded Swift-test commit is queued in [CI run 45909793668](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45909793668); do not claim the full matrix passed until it completes.
- [PR #21](https://github.com/INFORMATION89/Meridian-Engine/pull/21) adds a focused Swift async/coroutine-cancellation proof using the existing probe. CI run [45908971962](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45908971962) passed Linux, Windows, and Android (including the emulator); Apple/iOS native Swift tests remain active. Its dependency/license audit failed after Maven Central returned HTTP 429 during audit dependency resolution; retry only that failed job after the workflow completes. Do not accept the proof until all required checks pass.
- The M0.2 spike is marked with a Kotlin opt-in annotation so experimental contract types cannot be consumed silently from Kotlin production code. Its sample distinguishes an observation timestamp from forecast issue time and derives forecast lead from issue-to-valid time; these fields are prototype semantics, not yet an owner-approved public API.

### Active task: M0.2 public API and storage contracts

1. Define a small, consumer-neutral batch request/result API. Keep provider DTOs, database rows, platform-specific types, and application advice out of public contracts.
2. Specify request configuration, lifecycle ownership, cancellation propagation, typed errors, partial results, source diagnostics, and freshness semantics.
3. Specify the optional storage boundary: host-supplied implementation expectations, SQLDelight-backed implementation boundary, in-memory/no-persistence operation, schema/versioning, atomicity, concurrency, retention, deletion, and recovery.
4. Specify response-cache, issued-forecast archive, observation history, and longer-term meteorological datasets as separate categories. Preserve provenance and issue/measurement/fetch/valid times.
5. Design and test a Swift-friendly facade for async operations and cancellation; M0.1 proved basic framework consumption, not the ergonomics of the final weather API.
6. Build a standalone consumer that imports only Meridian's public API. It must not check out or depend on Solstice.
7. Run deterministic contract tests using fake clocks, fake transports, in-memory storage, and fixture sources. Verify offline, stale, unavailable, partial failure, and cancellation behaviour.
8. Prepare explicit recommendations for public API stability/versioning, host-supplied storage extension guarantees, and retention defaults. Request owner review only for those genuine contract decisions.

M0.2 must not implement live weather providers, production forecast algorithms, source blending, official-alert ingestion, local learning, radar, remote services, telemetry, or UI.

### After M0.2

1. Complete M0.3 reproducible forecast/observation verification and scientific safeguards.
2. Obtain the required M0.2 and M0.3 approvals before authorizing M1.
3. Implement the first forecast path only after its provider rights, credentials, API, source semantics, and acceptance criteria are approved.
4. Add later capabilities through their own milestones and evidence gates.

### Prohibited until the corresponding gate is approved

- Do not begin production weather providers, blending, local learning, alerts, radar, or application UI as part of M0.2.
- Do not claim Android ARM64, physical-device, native desktop ABI, or final mobile release-package notice verification based on the current CI evidence.
- Do not add Rust/UniFFI or native desktop libraries without a concrete consumer requirement, comparative evidence, and owner approval.
- Do not treat the technology approval as approval of the final public API, storage schema, provider list, forecast algorithms, telemetry, hosted services, or AI runtime.
- Do not change Solstice, Motion, or another consumer repository as part of Meridian work unless a separate task explicitly authorizes it.

