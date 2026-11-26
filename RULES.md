# Meridian-Engine — Rules

**Project:** Meridian (the reusable weather engine)  
**Repository:** INFORMATION89/Meridian-Engine  
**Status:** Working rules baseline, 2026-11-25

These rules govern the design, implementation, testing, and maintenance of Meridian. They take priority over speed and convenience. Changes to these rules require the project owner's approval and a corresponding decision record.

## Adopted project decisions

1. **Repository identity:** the repository is INFORMATION89/Meridian-Engine. The product and library name remains Meridian.
2. **Storage boundary:** the core must remain independent of concrete databases. The current cross-platform proposal is an optional SQLDelight/SQLite implementation behind a storage boundary; Room is not a Meridian-wide requirement. Final storage and extension decisions are governed by PLAN.md M0.2.
3. **Clean-room policy:** CLEAN_ROOM.md is a separate, authoritative policy. This file explains when it applies but does not duplicate its detailed requirements.

## 1. Read before every session

1. Read this file and CLEAN_ROOM.md before changing code or project documentation. Read AI_POLICY.md before any coding-agent session or when using AI tools with project data.
2. Read the current decision, status, roadmap, and history documents when they exist.
3. Read the approved specification for the task. A research note or draft is not automatically an approved implementation requirement.
4. Inspect the actual repository, code, tests, and build configuration. Never assume a planned feature already exists.
5. Report contradictions, missing decisions, and outdated documentation before relying on them.

## 2. Meridian's purpose and boundaries

1. Meridian is a reusable weather-data and forecasting engine exposed as a library.
2. Meridian has no user interface: no screens, navigation, visual styling, presentation components, or app-specific copy.
3. Consuming applications depend on Meridian. Meridian must never depend on Solstice or another consuming application. Solstice is only the first reference consumer, never an architectural authority or a required build/test dependency. Meridian must build and pass its own tests without checking out Solstice.
4. The core must not depend on Android Context, an application's lifecycle, or a UI framework.
5. Meridian does not schedule refreshes. The consuming application decides when to refresh and supplies scheduling facilities.
6. Meridian returns structured, documented data. Each application decides how to present it.
7. Accounts, analytics, advertising, tracking, social features, AI providers, model downloads, and remote services require explicit approval.

## 3. Changes that require approval

1. Do not independently add a weather feature, variable, source, platform, dependency, algorithm, storage capability, public API, or persistent-data format.
2. Before a material change, explain the problem, proposed change, alternatives, trade-offs, maintenance cost, licensing implications, and affected modules. Wait for approval.
3. A possible improvement is not permission to implement it. This includes replacing a library, changing model weights, adding a provider, or expanding platform support.
4. If an agreed approach proves unsuitable, stop and report the evidence. Do not quietly substitute a different design.
5. Small implementation choices may be made independently only when they do not change approved behaviour, public contracts, data meaning, security, licensing, or project scope.

## 4. Architecture and dependency boundaries

1. Keep domain models, units, provenance, source selection, blending, alert normalization, and platform-independent logic in the portable engine core selected by PLAN.md. Kotlin Multiplatform with Android/iOS targets and Desktop JVM is the current recommendation pending M0.1 approval.
2. Keep provider-specific request and response details inside that provider's adapter. Provider DTOs must not leak into the public domain model.
3. Put platform-specific implementations behind narrow interfaces or in the appropriate platform source sets. Do not add platform dependencies to common code for convenience.
4. Access storage, HTTP, time, and other external capabilities through explicit interfaces or injected dependencies. Core logic must be testable without a real network, database, device, or wall clock.
5. Do not introduce a dependency-injection framework into the core without approval. Prefer explicit construction.
6. Keep module responsibilities narrow: shared weather semantics in the core; provider adapters in their own modules; optional concrete storage behind an interface; language bindings isolated from engine internals; deterministic fixtures and fakes in test support.
7. The core must not depend on provider adapters, storage implementations, or consuming applications. Avoid circular dependencies.
8. Do not expose transport DTOs, database entities, vendor SDK types, or platform-specific types in the stable public API.
9. Keep the public API small and application-neutral. Existing API sketches remain proposals until approved.

## 5. Weather data integrity

1. Use SI units internally. Unit conversion and locale-specific formatting belong to consuming applications.
2. Represent instants and time-series timestamps internally in UTC. Apply local time zones only where calendar interpretation or presentation requires them.
3. Never silently mix observations, forecasts, derived values, and corrected values. Their nature must be explicit.
4. Preserve a source's native temporal resolution. Any interpolation, aggregation, or resampling must be explicit, tested, and represented in metadata or provenance.
5. Missing, unavailable, stale, invalid, and zero values are distinct states. Never use a plausible numeric default to conceal missing data.
6. Keep variable identity, units, valid time, issue time, and measurement time unambiguous.
7. Preserve quality information that affects interpretation, such as station distance, elevation difference, observation age, source freshness, and model spread.
8. Never present a model estimate as a direct observation.

## 6. Provenance is mandatory

1. Every returned weather value must retain enough provenance to explain where it came from.
2. Identify contributing sources and, where applicable, model identifiers and blend weights.
3. Record relevant issue and fetch times, freshness, transformations, corrections, and whether a fallback was used.
4. A correction must identify its amount, the observations or statistics that informed it, and its applicable window or lead time.
5. Results must explain which sources were selected, skipped, or unavailable and why (for example: outside coverage, disabled, missing key, stale, rate-limited, or failed).
6. Applications obtain source names and required credits from Meridian's result or source metadata. Never guess or hardcode attribution in a consuming application.
7. When values are combined, preserve the lineage of their inputs. Do not remove provenance merely to simplify an API.

## 7. Weather sources and external services

Before adding or changing a source:

1. Read its official API documentation, applicable terms, licence, attribution requirements, and rate-limit policy.
2. Record its operator, documentation and terms links, access type, coverage, supported features, update cadence, resolution, key requirements, costs, limits, and minimum request interval.
3. Prefer documented APIs. Website-reading adapters are best-effort and must never be the sole source for a critical feature.
4. Keep source metadata declarative where practical. Do not scatter coverage, attribution, tier, and rate-limit rules through unrelated code.
5. Keep credentials out of source control, build files, fixtures, logs, crash reports, and public artifacts. Use the consuming application's approved secure configuration mechanism.
6. Respect rate limits, minimum intervals, cache headers, expiry information, and source terms. A refresh request must never bypass a provider's limits.
7. Isolate source failures. Use only approved fallbacks and record them; never silently present stale or lower-quality data as if it came from the preferred source.
8. Document known shared upstream data or dependencies before treating multiple models as independent inputs to a blend.
9. Test adapters offline against recorded responses. Live-service checks supplement but never replace deterministic tests.
10. Re-check terms and behaviour when an integration changes materially. Do not assume an endpoint or licence remains unchanged forever.

## 8. Forecasting, observations, and alerts

1. Scientific or statistical changes require a written rationale and reproducible verification. Plausibility alone is not evidence of better forecast skill.
2. Establish and retain a baseline before changing source selection, model weights, bias correction, interpolation, or other forecast logic.
3. Evaluate changes against appropriate historical forecasts and observations. Report relevant metrics by variable and forecast lead time; examples include MAE and RMSE for continuous values and Brier score for precipitation probabilities.
4. Compare a proposed blend or correction with the previous Meridian result and a reasonable reference, such as the best individual model. Claim improvement only when evidence supports it.
5. Do not introduce adaptive weights, local learning, or statistical correction without sufficient data, documented safeguards, deterministic tests, and archive-based evaluation.
6. Keep uncertainty and model disagreement available to consumers. Do not imply precision unsupported by the input data.
7. Select observations using approved quality criteria such as distance, elevation difference, and age. Thresholds must be named configuration, not unexplained constants.
8. Preserve the meaning and lifecycle of official alerts. Where CAP is used, respect identifiers, sender, status, message type, references, updates, cancellations, severity, urgency, certainty, validity, and geographic areas.
9. Alerts are not forecast values. Do not average or casually rewrite official warning messages. Deduplicate and resolve updates or cancellations according to the approved alert specification while preserving source and authority.
10. Do not implement later-phase capabilities, such as radar nowcasting, ahead of their approved milestone and specification.

## 9. Configuration and dependency versions

1. Operational values must have named, documented configuration: weights, thresholds, distances, age limits, time windows, cache lifetimes, call intervals, rate limits, and retention periods.
2. Do not scatter unexplained numeric constants through adapters or core algorithms.
3. Each configurable default must state its unit, purpose, and rationale. Validate configuration at the boundary and fail clearly when invalid.
4. Keep configuration separate from algorithms so applications can configure supported behaviour without forking the engine.
5. A new threshold or tunable parameter requires approval and tests for boundary behaviour.
6. Centralize and lock dependency versions using the package/build system selected in PLAN.md (Gradle version catalog and dependency locking for KMP; Cargo only if a Rust component is later approved). Do not scatter unpinned versions across modules.

## 10. Storage, caching, privacy, and retention

1. The core defines storage contracts; it does not depend on a concrete database.
2. An optional SQLDelight/SQLite implementation may provide the official local storage backend. It depends on the core contract, never the reverse. Do not make Android Jetpack Room a required engine dependency.
3. A consuming application may use Meridian's supported storage implementation or provide an adapter only through an extension mechanism proven and documented in M0.2. The choice must not change the meaning of the core API.
4. Document what is stored, why it is needed, where it is stored, how long it is retained, and how it is deleted.
5. Do not store saved-place history or introduce location tracking into Meridian. Persist only the minimum data required by explicitly approved engine functions.
6. Apply the approved retention period to forecasts, observations, statistics, and cached responses. Do not extend retention silently.
7. Cache according to each provider's cadence, expiry, and terms. A cache must never cause stale data to appear fresh.
8. Do not transmit location history, raw coordinates, forecast logs, or diagnostics to a server by default.
9. Optional sharing of forecast errors requires explicit opt-in, data minimization, a documented retention policy, an approved server design, and legal review before implementation.
10. Do not build a remote learning or aggregation service as a shortcut for local functionality.

## 11. Clean-room development and licensing

CLEAN_ROOM.md is the authoritative policy for permitted research sources, independent specifications, implementation sessions, source records, and code-similarity concerns. Read and follow it before writing or changing implementation code. If its requirements cannot be met, stop and report the blocker.

Review every new dependency for maintenance, supported targets, and licence compatibility before adding it. Keep Meridian's Apache-2.0 licence and notices accurate.

## AI-assisted development and optional chat adapters

Contributor use of AI for engineering and occasional manual data analysis is governed by `AI_POLICY.md`. Read it before using a coding agent or sharing project data with an AI tool. Meridian has no AI runtime integration: the shipped engine must not call a model, require an AI provider or credentials, or incur token costs. An optional MCP adapter may be considered outside the weather core to expose approved read-only queries to an external chat client; it must not become a core dependency and requires its own specification and owner approval.

## 12. Testing and verification

1. Core logic must have deterministic unit tests. Use fake clocks, fake HTTP clients, and fixtures where applicable.
2. Test each source adapter offline with representative recorded responses, including empty, malformed, partial, stale, and error responses.
3. Test rate-limit enforcement, minimum intervals, cache expiry, fallback selection, provenance propagation, and partial-source failures.
4. Test units, time zones, daylight-saving transitions, UTC conversion, and date boundaries explicitly.
5. Test CAP parsing, alert updates, cancellations, deduplication, and geographic matching with recorded examples.
6. Changes to blending, weighting, observations, or correction logic require reproducible archive verification. Record the baseline, dataset, period, metrics, and result in the pull request.
7. Ordinary unit tests and the normal build must not require network access.
8. Test every supported target to the level promised by the project. A single successful compile is not sufficient to advertise platform support.
9. Report scientific verification separately from compilation and unit-test results. A passing build does not prove meteorological correctness.
10. Plan verification before running it. Define the acceptance criteria, the smallest relevant checks, and any expensive checks (full CI matrices, emulator/device runs, Apple-hosted builds, or repeated integration runs). Reuse valid results and do not rerun checks that the change cannot affect.
11. Batch a coherent set of implementation and documentation changes before triggering expensive CI. Use focused local checks while iterating, then run the full required matrix once when the batch is ready. If only one job fails, inspect its logs and retry only that job when possible; do not rerun successful jobs without a technical reason.
12. For environment or infrastructure failures, diagnose the evidence and try at most one materially different setup after the first failure. If the second setup fails for the same requirement, stop repeated attempts, mark that requirement BLOCKED, and report the evidence and the host/tool access needed. Never relabel an untested acceptance criterion as passed to save time.
13. Before substantially expanding the agreed verification scope or spending another full CI matrix after a failed run, summarize the proposed check and why it is needed to the project owner. Continue only after the owner agrees when the new check exceeds the task's agreed verification plan.

## 13. Documentation and decisions

1. Maintain one source of truth for each topic. Do not duplicate decisions across documents and allow them to drift.
2. The project decision document records decisions in force. When a decision changes, update it and add a dated history entry.
3. Specifications define intended behaviour and acceptance criteria before implementation begins.
4. The roadmap records remaining work; the continuation document records the current state and next concrete step; the history log records completed changes.
5. Update the relevant specification, decision record, roadmap, and continuation notes when implementation changes their truth.
6. Resolve contradictions by correcting the authoritative document. Do not leave conflicting versions appearing valid.
7. Mark ideas, research findings, and proposals as such until approved.

## 14. Git and pull requests

1. Work on a feature branch. Merge development work into main through a pull request; do not push ordinary development work directly to main.
2. The initial repository bootstrap may create the first commit on main because an empty repository has no commit from which to branch. After bootstrap, use branches and pull requests.
3. Keep commits focused and explain what changed. Do not combine unrelated refactors with feature work.
4. Never commit credentials, API keys, private datasets, local configuration, IDE state, generated build output, or machine-specific paths.
5. A pull request must state its purpose, affected modules, design decisions, sources consulted, tests run, archive-verification results when relevant, licence implications, and known limitations.
6. Do not add a dependency without explaining why the standard library or an existing dependency is insufficient and checking maintenance, platform support, and licence compatibility.
7. CI must pass before merge. Once CI is established, protect main and require the checks to pass.

## 15. Done means verified

A change is complete only when all applicable conditions are met:

1. The implementation follows the approved specification and these rules.
2. Relevant unit, fixture, integration, and platform tests pass.
3. Scientific changes have the required archive verification and reported metrics.
4. Source attribution, provenance, error states, and fallback behaviour are correct.
5. Public API and persistent-data compatibility have been considered and documented.
6. Documentation and decision records reflect the actual implementation.
7. CI passes, licensing has been checked, and the pull request describes what was and was not verified.
8. Any untested platform, live source, or device behaviour is explicitly labelled as unverified.
