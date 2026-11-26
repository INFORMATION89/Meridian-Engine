# MER-0.2: Public API, lifecycle, and storage contract design

- **Status:** Draft — design and deterministic contract-proof milestone; owner review required before production weather implementation.
- **Baseline:** Owner-approved Kotlin Multiplatform shared core, Ktor networking, SQLDelight as an optional persistence implementation, and JVM-first desktop distribution.
- **Parent evidence:** [MER-0.1 platform validation](../evidence/MER-0.1-platform-validation.md), CI #222.
- **Product authority:** [PLAN.md](../../PLAN.md), [ADR-0001](../decisions/ADR-0001-jvm-first-desktop.md), and [ADR-0002](../decisions/ADR-0002-application-owned-recommendations.md), and [ADR-0003](../decisions/ADR-0003-approved-production-technology-direction.md).

## 1. Purpose

M0.2 defines the consumer-facing contracts and runtime boundaries required before production engine implementation. It converts the approved technology direction into a reviewed, testable design for:

1. Public API and domain model boundaries.
2. Engine construction, configuration, lifecycle, and resource ownership.
3. Networking behaviour, cancellation, timeouts, and normalized failures.
4. Optional storage, cache semantics, retention, migrations, and deletion.
5. Freshness, provenance, quality, uncertainty, and versioning requirements.
6. Independent consumer interoperability and contract-test strategy.

M0.2 is not a weather-provider integration milestone. It must not implement production forecasts, source blending, local learning, alerts, radar, or user-facing recommendations.

## 2. Non-negotiable product boundaries

- Meridian is an independent embeddable meteorological engine, not an application or hosted service.
- The core must build, test, and run without Solstice or any other consumer repository.
- Public contracts are consumer-neutral and must not expose UI, Android lifecycle, JVM-only, Ktor, SQLDelight-generated, provider-specific, or Solstice-specific types.
- Meridian owns meteorological facts, derived meteorological events, evidence, provenance, quality, and uncertainty.
- Consuming applications own clothing, umbrella, UV action, activity, personal planning, profiles/preferences, notification thresholds, schedules, channels, wording, and cooldowns (ADR-0002).
- Normal operation is local-first. No account, Meridian backend, telemetry, analytics, remote database, generative-AI runtime, or external data contribution is required.
- Persistent storage is optional. A consumer must be able to use Meridian in a useful non-persistent mode.
- No private provider/developer credentials may be embedded in distributed artifacts.
- Provider adapters and implementation details remain behind internal boundaries.
- Every meaningful meteorological value must eventually be interpretable with units, valid time, issue/observation time as applicable, freshness, quality, and provenance.
- Never represent stale cached information as current. Never silently convert missing, unknown, or unsupported values into zero or certainty.

## 3. Scope and deliverables

M0.2 should produce reviewed specifications and executable contract proofs, not a production forecast implementation.

### Required documents

- `docs/specs/M0_2_PUBLIC_API.md` — public API surface, domain types, configuration, and consumer examples.
- `docs/specs/M0_2_LIFECYCLE_AND_NETWORKING.md` — construction, ownership, concurrency, cancellation, timeouts, and error mapping.
- `docs/specs/M0_2_STORAGE_AND_CACHE.md` — storage abstraction, cache semantics, retention, deletion, migration, and no-storage mode.
- `docs/specs/M0_2_COMPATIBILITY_AND_VERSIONING.md` — compatibility policy, platform matrix, artifact expectations, and API evolution.
- `docs/evidence/MER-0.2-contract-validation.md` — independent consumer proof, test evidence, limitations, and exact validated commit.
- ADRs for decisions that materially constrain architecture; do not create an ADR for routine implementation details.

### Required proof

At least one independent sample consumer, maintained within Meridian's repository, must compile and exercise the public API without importing internal engine packages or depending on Solstice. It should demonstrate Kotlin consumption and a Swift-facing iOS usage path. Desktop JVM consumption must remain covered. The proof must test the public contracts rather than only internal classes.

## 4. Public API design requirements

The API specification must define a small, coherent entry point and avoid exposing implementation machinery.

### 4.1 Engine entry point

Specify:
- A single documented construction/factory pattern.
- Immutable configuration after construction unless a clearly scoped runtime reconfiguration contract is justified.
- Explicit ownership and disposal/close semantics.
- Whether the engine is safe for concurrent use and the exact guarantees.
- How consumer-provided platform facilities (if any) are injected without exposing platform-specific types to common contracts.
- A no-persistence default and explicit opt-in for local persistence.
- No implicit background polling or hidden work after disposal.

Do not commit to names or signatures until Kotlin and Swift ergonomics have been reviewed together.

### 4.2 Domain result model

Specify neutral value types for:
- Quantity/value and unit.
- Valid time or interval, plus issue/observation time where applicable.
- Source identity and provenance.
- Data freshness and age.
- Quality flags and missing/unavailable states.
- Probability, uncertainty, and confidence, with precise distinctions.
- Derived meteorological events and their supporting evidence.
- Partial results and per-variable/per-source availability.

Use explicit types for unknown, missing, not-supported, and failed states; do not overload null or sentinel numeric values without documented semantics.

The model must distinguish:
- Direct observations.
- Provider/model forecasts.
- Meridian-derived values.
- Calibrated or corrected estimates.
- Detected meteorological events.
- Official alerts issued by an authority.

Meridian's event output is evidence, not an application action or notification instruction.

### 4.3 Units and time

The specification must establish:
- Canonical internal units and safe conversion rules.
- No unitless public meteorological quantities.
- UTC instants for machine comparison and explicit time-zone context where civil-time presentation is relevant.
- Interval boundary semantics (inclusive/exclusive) and forecast valid-time semantics.
- Handling of daylight-saving transitions and ambiguous/nonexistent local times.
- Precision and rounding rules that do not destroy source fidelity.

### 4.4 Public surface discipline

- Keep provider payloads, SQLDelight-generated classes, Ktor request/response types, coroutine implementation details, and platform engine objects out of public domain types.
- Do not expose mutable internal collections or internal cache state.
- Do not make Solstice a required test fixture, build dependency, or source of API semantics.
- Public Swift usage must be intentionally designed; do not leak awkward generated Kotlin wrapper types where a small facade can provide a stable ergonomic contract.
- Document every public symbol and its stability status.

## 5. Lifecycle, concurrency, and networking contract

The lifecycle/networking specification must define the following before implementation.

### 5.1 Ownership and lifecycle

- Which object owns the shared HTTP client, storage driver, and internal scopes.
- Whether dependencies can be injected for testing or host integration.
- Idempotent close/dispose behaviour.
- Behaviour of in-flight calls when the engine closes.
- No resource creation per individual weather request unless measured evidence justifies it.
- No hidden global singleton or process-wide mutable state.
- Clear thread-safety and concurrent-call guarantees.

### 5.2 Cancellation

- Cancellation must propagate from consumer invocation through Meridian orchestration to the active platform HTTP operation and storage operation.
- Cancellation is not a normal domain failure and must not be converted into a generic network error.
- Cleanup must occur on cancellation and exceptions.
- Closing the engine must cancel or otherwise deterministically settle owned in-flight work according to the documented contract.
- Tests must prove transport-level cancellation on actual Android, Darwin, and CIO engines using deterministic local fixtures; common MockEngine tests are additional unit coverage, not a substitute.

### 5.3 Timeouts and retry policy

Define distinct connect, request, and socket/read timeout semantics where supported. State which layer owns timeout configuration. Retry behaviour must be bounded, explicit, and sensitive to operation idempotency, provider policy, server retry hints, and remaining request budget. Do not retry cancellation, malformed payloads, or permanent client errors automatically.

Do not introduce a universal aggressive retry policy. Backoff, jitter, concurrency limits, request coalescing, and provider rate-limit handling must be specified and testable.

### 5.4 Normalized failures

Define a stable, consumer-neutral failure taxonomy, including at minimum:
- Invalid configuration or request.
- Unsupported capability/platform.
- No network/connectivity or transport failure.
- Timeout (with stage/context where safe).
- HTTP/provider rejection, retaining safe status/category information.
- Rate limiting.
- Malformed or semantically invalid provider data.
- No usable data / partial data.
- Storage unavailable or storage operation failure.
- Internal unexpected failure.

Failure objects must not expose secrets, authorization headers, raw credential-bearing URLs, or unredacted provider payloads. Preserve diagnostic cause information internally and offer safe structured diagnostics without leaking sensitive data.

Partial success must be representable: one unavailable source or variable must not automatically discard unrelated valid results.

## 6. Storage and cache contract

SQLDelight is the selected candidate implementation, not the public storage contract. The specification must keep storage optional and hide generated database types.

### 6.1 Modes

Define:
- No-storage mode: useful request/response behaviour without persistent writes.
- Engine-managed local storage mode: explicit opt-in and documented location/ownership rules.
- Any host-provided storage integration only if it can be expressed as a stable cross-platform contract without leaking platform-specific handles.

Do not require an account, remote database, synchronization service, or Meridian-owned backend.

### 6.2 Cache semantics

Specify independently:
- Cache key identity and normalization.
- Data type and source/model identity.
- Valid-time and issue-time dimensions.
- Freshness and expiry policy ownership.
- Stale-while-revalidate behaviour, if supported.
- Behaviour when offline, expired, partially cached, or cache reads fail.
- Cache invalidation after provider, schema, algorithm, or configuration changes.
- Protection against one consumer/configuration receiving another consumer's incompatible cached result.

A cache hit must retain original provenance and timestamps. Cache age must be visible. Expiry must not erase forecast/observation verification history by accident.

### 6.3 Retention and deletion

Keep operational cache separate from scientific verification/learning history and climatological archives. A bounded recent verification/learning window (30 days was previously discussed) is not an approved retention commitment. M0.2 defines separation and metadata only; M0.3 or a later reviewed scientific specification must propose a bounded duration with evidence and owner approval before persistent collection begins. Never imply unbounded history.

Define:
- Configurable bounded retention where appropriate.
- Explicit deletion semantics and what data is removed.
- Behaviour on database corruption, unavailable storage, and migration failure.
- Whether the engine can fall back to no-storage mode safely.
- No collection or external transmission of stored data by default.

### 6.4 Migrations and concurrency

Specify schema versioning, transactional migration, rollback/failure behaviour, concurrent readers/writers, process termination during writes, and safe database close. Migrations must never silently discard user data. Public API consumers must not depend on schema details.

## 7. Privacy and security contract

- No telemetry or analytics by default.
- No external data contribution by default.
- Optional sharing, if ever introduced, requires a separate explicit opt-in independent of core functionality and a separate approved scope.
- Disclose data, purpose, recipient, destination, retention, and withdrawal/deletion controls before any contribution.
- Location input must be purpose-limited; do not create a location-history feature in the engine.
- Logs and diagnostics must redact coordinates or other potentially sensitive request context unless explicitly enabled by the host under a documented policy.
- Credentials are supplied/stored by the consuming application or a clearly specified secure platform boundary; Meridian must not ship private developer keys.
- TLS verification must remain enabled; no insecure trust-all defaults.
- No production AI runtime or network calls to AI services.

## 8. Compatibility, versioning, and distribution

The compatibility specification must cover:
- Public API semantic compatibility and deprecation policy.
- Versioning for public API and persisted schema independently.
- Supported Kotlin/Native, Android, and Desktop JVM targets/architectures with tested versus untested states clearly distinguished.
- Swift facade and framework packaging expectations.
- Desktop Java runtime baseline and consumer responsibility for runtime bundling.
- Artifact naming, checksums/signatures, metadata, license and third-party notices.
- Mobile release notice staging and verification before public distribution.
- Behaviour when a consumer uses unsupported platform or architecture.

Do not claim physical-device or Android ARM64 support based solely on M0.1's emulator/simulator evidence. Do not claim native desktop ABI support.

## 9. Independent consumer contract tests

The proof consumer must demonstrate:
1. Construction with minimal configuration.
2. A request through the public API using deterministic injected fixtures.
3. Typed successful results and explicit missing/partial data.
4. Normalized errors without leaking secrets.
5. Cancellation and deterministic close semantics.
6. No-storage operation.
7. Optional storage behaviour and reopen/migration semantics where included in M0.2.
8. Swift-friendly consumption of the iOS framework.
9. Desktop JVM consumption on the validated OS matrix where practical.
10. No import or dependency on Solstice, UI frameworks, or internal implementation packages.

Tests must be deterministic and must not contact live weather providers. Keep test volume proportionate: prefer a focused contract suite over duplicating every internal unit test at every platform boundary. Follow repository test-budget and CI cost rules.

## 10. Out of scope

M0.2 must not implement:
- Production weather provider adapters or credentials.
- Live weather API calls in tests.
- Forecast aggregation, blending, bias correction, adaptive weighting, or local learning algorithms.
- Official alert ingestion or notification policy.
- Radar, satellite, nowcasting, or climatological archives.
- Clothing, umbrella, sunscreen, activity, lifestyle, or personalized recommendations.
- UI, widgets, maps, charts, saved places, or consumer lifecycle scheduling.
- Meridian-hosted services, accounts, telemetry, analytics, or mandatory external sharing.
- AI inference or generative-AI dependencies in the shipped engine.

M0.2 may use deterministic fake providers/fixtures and minimal sample data solely to validate public contracts.

## 11. Acceptance criteria

M0.2 is complete only when:

- [ ] The public API specification is reviewed and internally consistent.
- [ ] The lifecycle, concurrency, cancellation, timeout, and error contract is documented.
- [ ] The storage/cache/no-storage contract is documented, including retention, deletion, and migration boundaries.
- [ ] Compatibility, versioning, platform support, and packaging expectations are documented.
- [ ] Swift interop is reviewed against actual consumer ergonomics, not inferred from Kotlin compilation alone.
- [ ] An independent consumer builds without Solstice and uses only public API contracts.
- [ ] Contract tests cover success, partial/missing data, normalized failures, cancellation, close, and no-storage behaviour.
- [ ] Storage contract tests cover opt-in persistence, reopen, migration safety, and failure behaviour if persistence is included in the milestone proof.
- [ ] Tests use deterministic local fixtures and do not contact live providers.
- [ ] Public API contains no provider, Ktor, SQLDelight, UI, or Solstice leakage.
- [ ] Privacy defaults and diagnostic redaction are explicitly verified.
- [ ] CI validates the exact final commit; all untested platforms/architectures remain honestly documented.
- [ ] Documentation and implementation agree; no production weather capability is implied by the contract proof.
- [ ] Owner reviews the final evidence and approves the M0.2 exit before production weather implementation begins.

## 12. Work sequence

1. Audit current M0.1 code and repository rules against the approved stack; identify reusable proof code versus temporary spike code.
2. Draft the four contract specifications and identify unresolved decisions.
3. Review Kotlin and Swift public API ergonomics with a tiny independent consumer before committing to signatures.
4. Define lifecycle, cancellation, and normalized error behaviour.
5. Define storage abstraction and no-storage mode; verify that optional persistence does not infect public contracts.
6. Define compatibility/versioning and artifact expectations.
7. Implement only the smallest contract proof needed to validate the specifications.
8. Run static review and targeted tests first; group changes before any CI-triggering push. Check workflow triggers and use documented skip mechanisms only when appropriate; never weaken required verification.
9. Record exact commit, CI evidence, artifacts, tested matrix, untested matrix, and remaining risks.
10. Present M0.2 exit recommendation for owner review.

## 13. Decision log

The following are already constrained by owner-approved product/architecture decisions:
- KMP shared core, Ktor networking, SQLDelight as optional persistence candidate.
- JVM-first desktop distribution on Windows, macOS, and Linux.
- Meridian remains independent of Solstice and application-neutral.
- Recommendations and action policy belong to consuming applications (ADR-0002).
- Local-first operation and privacy-preserving defaults.
- No production provider/forecast implementation until the relevant specification and acceptance gates are approved.

Open design questions should be resolved by evidence and technical recommendation. Escalate only if a choice changes product scope, privacy, legal obligations, strategic compatibility, distribution commitments, or ongoing cost materially.
