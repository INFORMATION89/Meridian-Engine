# Meridian — Complete Roadmap

**Last reviewed:** 2026-11-26  
**Current stage:** M0.2 — Public API and storage contracts  
**Product direction:** Owner-approved independent, local-first, multiplatform meteorological engine.

This page presents the complete milestone sequence, its purpose, and the completion gates. It is an orientation and progress view; the repository's [`PLAN.md`](https://github.com/INFORMATION89/Meridian-Engine/blob/main/PLAN.md) remains the master source for product scope, architecture boundaries, detailed acceptance criteria, and implementation authority. Milestone specifications provide the scoped details for each stage.

## Product destination

Meridian is an independent, reusable weather engine—not a weather application and not a Solstice-specific library. It will serve unrelated applications on Android, iOS, Windows, macOS, and Linux through a stable, consumer-neutral API.

Its core objective is **Level 2 meteorological intelligence** built on existing forecasts and observations: evaluate sources, select suitable models, combine forecasts, measure disagreement and uncertainty, verify results against observations, and apply regional corrections only when repeatable evidence demonstrates improvement.

Meridian will not build a new global numerical weather-prediction model. It will not own UI, maps, charts, saved places, notifications, accounts, or application lifecycle. Consuming applications own those concerns and decide how to present meteorological facts and events.

### Product and architecture commitments

- **Independent core:** no dependency on Solstice or another consuming application.
- **Approved shared technology direction:** Kotlin Multiplatform for the shared engine, Ktor for networking, and SQLDelight/SQLite as an optional persistence implementation behind engine-owned interfaces.
- **Desktop:** JVM-first for Windows, macOS, and Linux. A consuming application may bundle a compatible Java runtime when it needs self-contained delivery.
- **Native Rust/UniFFI and native desktop libraries:** deferred; reconsider only if a demonstrated consumer requirement justifies their additional complexity.
- **Local-first:** no mandatory Meridian backend, account, telemetry, remote database, synchronization, AI service, or always-on daemon.
- **Privacy:** telemetry and external data contribution are off by default. Any future sharing requires separate explicit consent, transparent payloads, purpose and recipient disclosure, and withdrawal controls.
- **AI boundary:** AI may assist contributors; the shipped engine must work without AI, prompts, model files, AI credentials, or inference services.
- **Scientific integrity:** values retain source, timestamps, quality, freshness, provenance, and uncertainty. Forecasts, observations, estimates, derived events, and official alerts remain distinguishable.
- **Consumer-owned recommendations:** Meridian returns meteorological facts and structured events. Lifestyle advice and application-specific action policies belong to each consuming app.

## Roadmap at a glance

| Milestone | Focus | Current status |
|---|---|---|
| M0 | Repository foundation and governance | Completed baseline; maintain as required |
| M0.1 | Multiplatform and production-stack proof | Completed and merged |
| M0.2 | Public API and optional storage contracts | **In progress** |
| M0.3 | Reproducible verification and research foundation | Planned; follows M0.2 |
| M1 | First eligible-provider forecast path | Planned; gated by M0.2 and M0.3 |
| M2 | Provenance, source selection, resilience, and cache behaviour | Planned |
| M3 | Official weather alerts | Planned |
| M4 | Quality-controlled observations | Planned |
| M5 | Multi-model forecast blending and uncertainty | Planned |
| M6 | Independent consumer compatibility | Planned |
| M7 | Level 2 regional intelligence and evidence-based learning | Planned |
| M8 | Weather phenomena and event tracking | Planned |
| M9 | Historical and climatological context | Planned |
| M10 | Radar, satellite, and nowcasting | Planned; regional feasibility-gated |

Research for later milestones may happen earlier. Production implementation must follow the relevant specification, evidence, and approval gates; a roadmap entry is not blanket authorization to implement it.

---

## Foundation — M0 through M0.3

### M0 — Repository foundation

**Purpose:** establish a maintainable, secure, legally coherent project before building the engine.

**Scope:**
- Repository identity, README, documentation hierarchy, and contribution/agent rules.
- Apache License 2.0 and required third-party notices.
- CI baseline, test conventions, dependency review, and security practices.
- Clear authority between the master plan, rules, AI policy, milestone specifications, research, and derived documentation.

**Completion gate:** project identity, licence, notices, rules, documentation authority, and CI baseline are consistent and verifiable. Continue maintaining this foundation as the project evolves.

### M0.1 — Multiplatform and production-stack proof

<badge color="success">Completed</badge>

**Purpose:** prove the candidate platform path and consumer boundaries before committing to production weather features.

**Validated scope:**
- Kotlin Multiplatform exercised through Android, iOS/Swift, and JVM desktop consumers.
- Desktop JVM consumers exercised on Windows, macOS, and Linux.
- Platform-specific HTTP engines and SQLDelight candidate drivers tested at their intended boundaries.
- Cancellation, timeout, partial/empty response, packaging, dependency, and licence evidence reviewed.
- The platform proof was merged in [PR #15](https://github.com/INFORMATION89/Meridian-Engine/pull/15).

**Known limitations that must remain visible:**
- Android ARM64 runtime and physical Android/iOS device execution were not part of the accepted CI-only proof.
- Native desktop ABIs were not established.
- Mobile public-release packaging still needs project and third-party licence/notice staging and verification alongside the relevant mobile artifacts.

**Completion gate:** platform evidence and limitations reviewed, with the production direction explicitly approved. The current approved direction is KMP + Ktor + optional SQLDelight/SQLite, with JVM-first desktop. Rust/UniFFI and native desktop artifacts are deferred unless real requirements justify them.

### M0.2 — Public API and optional storage contracts

<badge color="info">In progress</badge>

**Purpose:** define how any application embeds Meridian, requests weather information, and optionally supplies local persistence—before production providers or algorithms are implemented.

**Scope:**
- Engine construction/configuration with injected dependencies and explicit policies.
- Consumer-neutral request and result types; no provider DTOs in the public API.
- Per-feature outcomes, values, units, issue/fetch/valid times, freshness, quality, uncertainty, provenance, and typed problems.
- Read-only source diagnostics, including selected, skipped, failed, and fallback sources with machine-readable reasons.
- Predictable cancellation across Kotlin, JVM, and Swift consumer boundaries.
- Explicit API and persisted-record versioning rules.
- Optional storage interfaces for response cache, forecast snapshots, observation history, retention/deletion, transactions, and concurrency.
- Host-provided storage remains possible; the optional SQLite implementation must not leak SQLDelight, SQLite, Room, JDBC, file paths, or database row types into the public API.
- Batch request/response is the initial API direction; streaming and platform-specific callback abstractions are not assumed without evidence.

**Out of scope:** real weather providers, production forecasts, blending algorithms, and production database implementation.

**Completion gate:** independent consumer contract tests pass; API, cancellation, error, compatibility, and storage decisions are reviewed and approved before weather-feature implementation.

### M0.3 — Reproducible verification and research foundation

**Purpose:** establish how forecast quality will be measured and improved without guesswork or data leakage.

**Scope:**
- Preserve forecast snapshots as they were issued and pair them with later quality-controlled observations.
- Define matching rules, source/station quality, data provenance, exclusions, and versioned datasets.
- Select metrics appropriate to continuous variables, precipitation probabilities, and event detection.
- Separate tuning data from independent evaluation periods; prevent future-observation leakage.
- Produce reproducible reports from versioned data, code, and parameters.
- Define a human-reviewed workflow for accepting or rejecting improvements.
- AI may assist research but every material finding must be independently reproducible; AI is not part of the engine runtime.
- An optional local MCP adapter may be considered outside the core API, subject to its own scope and privacy constraints. A cloud chat service must not be assumed to reach a device-local engine.

**Completion gate:** a forecast evaluation can be reproduced from versioned inputs and code, with clear safeguards and human review. The workflow remains useful without AI.

---

## Weather-engine milestones — M1 through M6

### M1 — First forecast path

**Purpose:** deliver the first useful, normalized forecast path from one eligible provider.

**Scope:**
- Select one provider only after reviewing current access, coverage, terms, attribution, request limits, licensing, and deployment eligibility.
- Normalize current conditions, hourly forecast, and daily forecast into Meridian-owned types.
- Target a 10-day horizon where the provider genuinely supports it.
- Preserve units, time semantics, source identity, fetch/issue/valid times, freshness, and typed failures.
- Provide a CLI and an independent consumer path for exercising the API.

**Out of scope:** multi-model blending and adaptive corrections.

**Completion gate:** CLI and independent consumer return correct normalized values, units, time semantics, source attribution, freshness, and typed problems; provider eligibility is documented.

### M2 — Provenance and source resilience

**Purpose:** make every result explainable and ensure one failing provider does not invalidate unrelated data.

**Scope:**
- Source catalog and eligibility by region, variable, forecast horizon, freshness, quality, coverage, terms, and request limits.
- Request planning that avoids unnecessary calls and respects provider quotas.
- Per-value provenance and source-selection explanations.
- Cache freshness, stale-data disclosure, fallback behaviour, partial success, and failure isolation.
- Explicit diagnostics for unavailable, skipped, rate-limited, stale, and failed sources.

**Completion gate:** tests demonstrate correct behaviour under provider failure, stale data, rate limits, missing fields, partial success, and fallback; source-selection reasons remain inspectable.

### M3 — Official weather alerts

**Purpose:** expose official warnings without altering their meaning or confusing them with model-derived detections.

**Scope:**
- Parse and normalize CAP-compatible alert data.
- Preserve issuing authority, identifier, status, scope, validity, instructions, references, and attribution.
- Handle updates, cancellations, deduplication, and geographic coverage.
- Keep official alerts separate from Meridian's own weather-event detections.

**Completion gate:** recorded alert lifecycles preserve authority, identity, validity, status, instructions, and attribution; updates and cancellations are handled deterministically.

### M4 — Quality-controlled observations

**Purpose:** select observations that actually represent the requested place and variable.

**Scope:**
- Discover candidate stations and observation sources in response to consumer requests.
- Evaluate measurement quality, timestamps, station metadata, elevation, distance, exposure, and spatial/temporal representativeness.
- Make selection variable-specific; proximity alone is not sufficient.
- Preserve quality flags and traceable reasons for rejecting or selecting observations.
- Avoid background place tracking; the consumer owns saved places and refresh scheduling.

**Completion gate:** unrepresentative, stale, or poor-quality observations are rejected with traceable reasons; selected observations retain source and quality context.

### M5 — Multi-model forecast blending

**Purpose:** combine suitable existing forecast models and communicate their agreement and disagreement honestly.

**Scope:**
- Establish a reproducible baseline before adaptive processing.
- Select models by region, variable, horizon, coverage, availability, and evidence.
- Begin with neutral/equal weights only where justified.
- Preserve contributors, weights, missing-data decisions, spread, and disagreement.
- Use correct variable semantics (for example, vector components for wind direction).
- Compare the blend with the best relevant individual model and the previous Meridian baseline.
- Do not claim calibrated probabilities without appropriate validation.

**Completion gate:** reproducible archive evaluation demonstrates the blend's behaviour against individual models and baseline; contributor and uncertainty semantics are correct.

### M6 — Independent consumer compatibility

**Purpose:** prove Meridian is a reusable engine rather than a library that only works inside its first app.

**Scope:**
- Validate public API integration in more than one consumer shape.
- Include Solstice as one consumer, not as an architectural dependency or source of truth.
- Provide non-Solstice contract samples and validate platform-specific artifacts and public API behaviour.
- Document supported consumer languages, runtimes, packaging, and limitations.

**Completion gate:** Meridian builds and runs without Solstice; independent consumer integrations and every advertised artifact path are tested.

---

## Meteorological intelligence milestones — M7 through M10

### M7 — Level 2 regional intelligence and evidence-based learning

**Purpose:** improve forecasts for specific regions and conditions only when independent evidence supports the improvement.

**Scope:**
- Measure model/source skill by region, variable, lead time, season, and weather regime where sample sizes permit.
- Calibrate probabilities and evaluate systematic bias.
- Apply verified model weighting and bounded local corrections only after independent validation.
- Use quality-controlled observations and protect against sensor faults, station changes, outliers, correlated data, leakage, and overfitting.
- Keep recent verification/learning records local by default, bounded and configurable; the initial recent rolling window is 30 days.
- Preserve provenance, disable/fallback paths, and rollback for every correction.
- Never automatically promote new weights or algorithms merely because observations arrive.

**Completion gate:** measured improvements on independent regional, variable, and forecast-horizon evaluations; unsafe or unvalidated corrections remain disabled.

### M8 — Weather phenomena and event tracking

**Purpose:** represent meaningful meteorological phenomena as structured, traceable events.

**Scope:**
- Detect and track phenomena such as precipitation, snow, storms, extreme wind, heat, cold, fog, and severe conditions when sources and validated methods permit.
- Represent event identity, timing, evolution, supporting variables, source provenance, quality, probability, and uncertainty.
- Distinguish observations, estimates, detections, and predictions.
- Keep Meridian event detections separate from official alerts.
- Return meteorological facts and events, not user-facing lifestyle advice or app-specific recommendations.

**Completion gate:** event lifecycle and evidence semantics pass deterministic and meteorological validation; unsupported regions or phenomena are explicitly identified.

### M9 — Historical and climatological context

**Purpose:** provide reliable historical context without confusing it with current forecasts.

**Scope:**
- Evaluate and version historical datasets by licence, quality, temporal/geographic coverage, resolution, and methodology.
- Produce regional reference statistics, anomalies, and comparisons where data supports them.
- Keep climatological records separate from the short rolling verification/learning window.
- Apply appropriate periods and uncertainty to regional trend analysis.
- Avoid causal claims that the data cannot support.

**Completion gate:** dataset rights, quality, coverage, and statistical methods are documented and reproducible; limitations and uncertainty are explicit.

### M10 — Radar, satellite, and nowcasting

**Purpose:** progressively add short-term precipitation analysis where regional data quality and coverage justify it.

**Scope:**
- Establish access rights, coverage, latency, scan interval, georeferencing, calibration, quality control, precipitation-rate semantics, compute cost, and benchmark data for each region.
- Normalize and evaluate differences between radar sources before combining them.
- Begin with precipitation detection and tracking.
- Add motion, intensity, development, and dissipation only when validated.
- Keep satellite-derived precipitation distinct from radar observations.
- Communicate confidence and fallbacks; outside coverage or when imagery is poor, disclose limitations rather than imply universal nowcasting.

**Completion gate:** regional feasibility and benchmarks demonstrate value, coverage, freshness, accuracy, resource cost, and safe fallback behaviour before release.

---

## Stable-release gate

A stable release is not defined by the completion of a particular number of milestones. It requires, at minimum:

- Supported artifact and consumer evidence for every advertised OS/runtime.
- A reliable current/hourly/daily forecast path and documented provider rights.
- Multi-model forecast combination with traceable inputs and honest spread/uncertainty.
- Adequate regional, variable, and forecast-horizon evaluation for every claimed capability.
- Validated source selection and any enabled correction or adaptation.
- Clear distinction between observations, model outputs, blends, corrections, detections, estimates, predictions, and official warnings.
- Secure local storage, freshness, cache, cancellation, and failure behaviour.
- Privacy, credentials, dependencies, licensing, and supply-chain review.
- Stable, versioned public API and tested independent consumer integration.
- Complete documentation, diagnostics, declared limitations, rollback, and support policy.

Radar, climatology, and advanced phenomena may remain explicitly experimental or unsupported where their individual gates are not met. The stable engine must nevertheless deliver the core Level 2 objective through validated multi-source intelligence and regional evaluation.

## How milestones are delivered

Every milestone follows the same cycle:

1. Research and verify current evidence, provider terms, platform support, and licensing.
2. Write a scoped specification with explicit non-goals and acceptance criteria.
3. Review and approve the specification and any strategic decisions.
4. Implement only the authorized scope.
5. Run targeted tests and applicable platform checks.
6. Publish an evidence report, including gaps and tests not run.
7. Update documentation and this roadmap without overstating completion.

The order may change only when dependency evidence justifies it and the master plan is updated. A green compile, plausible output, or AI-generated hypothesis is not scientific validation.

## Current next steps

1. Finish M0.2's public API, error/partial-result, cancellation, lifecycle, and optional storage contracts.
2. Validate the contracts through Kotlin/JVM and native Swift consumer boundaries.
3. Review and approve the M0.2 acceptance evidence.
4. Complete M0.3's reproducible forecast/observation verification design and safeguards.
5. Only after those gates, authorize M1's first eligible provider and production forecast path.

## Authoritative project documents

- [Master plan — `PLAN.md`](https://github.com/INFORMATION89/Meridian-Engine/blob/main/PLAN.md)
- [M0.2 — Public API and storage contract](https://github.com/INFORMATION89/Meridian-Engine/blob/main/docs/specs/M0_2_PUBLIC_API_STORAGE.md)
- [M0.3 — Verification and research foundation](https://github.com/INFORMATION89/Meridian-Engine/blob/main/docs/specs/M0_3_VERIFICATION_RESEARCH.md)
- [M0.1 — Platform validation evidence](https://github.com/INFORMATION89/Meridian-Engine/blob/main/docs/evidence/MER-0.1-platform-validation.md)
- [Decision: JVM-first desktop](https://github.com/INFORMATION89/Meridian-Engine/blob/main/docs/decisions/ADR-0001-jvm-first-desktop.md)
- [Decision: application-owned recommendations](https://github.com/INFORMATION89/Meridian-Engine/blob/main/docs/decisions/ADR-0002-application-owned-recommendations.md)

The roadmap is updated as milestone status changes. Detailed product requirements and acceptance criteria remain in the master plan and approved milestone specifications.
