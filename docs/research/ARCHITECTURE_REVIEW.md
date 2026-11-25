# Meridian architecture review

**Status:** design review and recommendations for owner approval  
**Prepared:** 2026-11-25  
**Scope:** repository architecture, M0.1 evidence, MER-0.2 API/storage decisions, MER-0.3 verification design  
**Authority:** `PLAN.md`, `RULES.md`, `CLEAN_ROOM.md`, `AGENTS.md`, and approved milestone specifications remain authoritative.

## 1. Executive assessment

The recommended direction is coherent: Meridian should remain an independent, local-first weather engine embedded in the consuming application. Its core should own weather semantics, provider coordination, normalization, freshness, provenance, and deterministic result assembly. The consuming application should own UI, saved places, permissions, scheduling, settings, secure credential storage, and presentation.

The current Kotlin Multiplatform proposal is a reasonable way to meet the five-OS requirement with the least architectural complexity:

- Android: Kotlin/Android target.
- iOS: Kotlin/Native framework consumed by Swift.
- Windows, macOS, and Linux: one Desktop JVM artifact, with a compatible runtime supplied or bundled by the application.

This is a five-operating-system strategy, not a promise of native `.dll`, `.dylib`, and `.so` libraries. A consumer that requires a native desktop ABI without a JVM would change the trade-off and justify a separate Rust/UniFFI comparison. Do not add that comparison unless a real consumer requirement or a concrete KMP blocker justifies it.

My main recommendation is to close the remaining MER-0.1 evidence gaps, then approve a small batch-first public API and optional persistence boundary in MER-0.2. In parallel, approve a reproducible, human-reviewed verification policy in MER-0.3. Do not begin production weather providers or forecast algorithms before those gates are resolved.

## 2. Current evidence and what it proves

### Latest inspected workflow

The latest inspected workflow for PR #2 was [Run #152](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45717462136), associated with the MER-0.1 spike branch. Its six jobs completed successfully:

| Job | Observed result | What it covers |
|---|---|---|
| Dependency metadata | PASS | Main dependency verification metadata generation |
| Linux dependency metadata | PASS | Linux-specific Android tool checksums and Android build in normal verification mode |
| Apple and iOS | PASS | iOS framework/simulator workflow, Swift Package consumer tests, macOS JVM consumer and packaged app image |
| Desktop Linux | PASS | JVM consumer and packaged app image on Linux |
| Desktop Windows | PASS | JVM consumer and packaged app image on Windows |
| Android | PASS | Host-side tests and Android x86_64 emulator instrumentation |

This is meaningful evidence that the current KMP/JVM proof and its packaging workflow are much more stable than the earlier failing runs.

### Limits that remain

A green workflow does not close every acceptance criterion:

1. **Android ARM64 runtime remains NOT TESTED.** The inspected CI run uses an Android x86_64 emulator. Do not infer ARM64 device compatibility from it.
2. **Physical iOS device execution remains unproven.** The inspected evidence covers an ARM64 iOS Simulator and Swift Package consumer, not a physical-device release validation.
3. **Actual HTTP requests through each production candidate engine remain unproven.** The current spike has a common fixture path and platform engine lifecycle probes; those are not the same as sending deterministic requests through Android, Darwin, and CIO.
4. **Timeout behaviour, transport-error mapping, and consumer-boundary cancellation need explicit tests.** Common coroutine cancellation alone does not prove Swift/Android/JVM boundary behaviour.
5. **Software-emulator fallback without KVM remains unproven.** A run with KVM available cannot prove a TCG/software fallback.
6. **The dependency/licence inventory and the decision about a project `NOTICE` remain open.** Confirm the copyright holder and notice obligations before adding one.
7. The tested dependency versions and driver choices remain spike candidates until the owner approves the M0.1 recommendation.

PR #2 remains open and Draft. Its own owner gate says not to merge it, close MER-0.1, or start MER-1 until the evidence and subsequent decisions have been reviewed. See the [MER-0.1 pull request](https://github.com/INFORMATION89/Meridian-Engine/pull/2) and its [verification record](../verification/MER-0_1_RESULTS.md).

### Recommended M0.1 completion checklist

- [ ] Record Run #152 and its exact tested commit in the verification record.
- [ ] Preserve Android ARM64 as NOT TESTED unless a physical ARM64 device or reproducible ARM64 emulator route is exercised.
- [ ] Add deterministic real-engine request tests for Android, Darwin, and JVM/CIO.
- [ ] Test timeout and transport-error mapping on the target engines.
- [ ] Assert cancellation from each public consumer boundary, including Swift.
- [ ] Test TCG fallback only in an environment where KVM is confirmed unavailable.
- [ ] Re-run the full matrix after relevant changes and record one exact SHA.
- [ ] Complete the dependency and licence inventory; decide whether NOTICE is required.
- [ ] Present the resulting recommendation for explicit owner approval.

Do not mark an unsupported cell as PASS to make the matrix appear complete. The owner can explicitly accept a documented limitation; the project must still label it honestly.

## 3. Target architecture

### Responsibility boundaries

| Boundary | Meridian owns | Consumer application owns |
|---|---|---|
| Weather domain | Variable meaning, units, time semantics, quality, uncertainty, provenance | Local formatting and presentation |
| Requests | Requested features, source eligibility, request coordination | User intent, place selection, refresh scheduling |
| Network | Provider adapters, bounded requests, parsing, rate limiting, provider fallback | Connectivity permissions and provider credentials |
| Storage | Optional contracts for cache and forecast/observation history | Storage location, host lifecycle, secure settings |
| Privacy | Data minimization, no unsolicited transmission, explicit deletion contracts | Consent UI and app-level privacy disclosures |
| Lifecycle | Operation resources and a documented engine close contract | Application lifecycle and when to create/close the engine |
| Platforms | Narrow platform factories/drivers | Platform integration and packaging of the consuming app |
| UI | No UI | Screens, navigation, accessibility, localization, saved places |

### Dependency direction

The intended dependency direction is:

- Public consumer API depends on stable weather-domain concepts.
- The engine orchestrator depends on internal source, clock, storage, and transport contracts.
- Provider adapters depend on core contracts; core never depends on provider DTOs.
- Optional storage implementations depend on storage contracts; core never depends on SQLDelight, SQLite, JDBC, Room, or a concrete database.
- Android/iOS/JVM platform factories supply the narrow platform-specific drivers.
- Samples and Solstice consume the public API only. Meridian must build and test without checking out Solstice.

Do not create every planned module during the spike. Create a module only when an approved milestone needs it.

## 4. Decision register

All decisions below are recommendations, not adopted contracts. Approval should be recorded in `PLAN.md` and the relevant milestone specification.

| ID | Decision | Recommendation | Why | Status |
|---|---|---|---|---|
| AR-01 | Desktop runtime | Accept JVM desktop for Windows/macOS/Linux in v1; do not promise native desktop libraries | Meets the OS goal without a second core implementation | Pending owner approval |
| AR-02 | Core language | Accept KMP only after reviewing M0.1 evidence and limitations | Shared Kotlin domain with Android/iOS and JVM consumers | Pending owner approval |
| AR-03 | Public API | Batch-first asynchronous request/result API | Smallest cross-platform contract; easier Swift and Java interop than premature streaming | Pending owner approval |
| AR-04 | Feature outcomes | Per-feature available/partial/unavailable outcome plus machine-readable problems | One failed source or feature must not erase unrelated successful data | Pending owner approval |
| AR-05 | Cancellation | Preserve cancellation as control flow; never turn cancellation into a successful weather result | Prevents incomplete operations from appearing complete | Pending owner approval |
| AR-06 | Engine lifecycle | Explicit, idempotent close; specify what happens to in-flight work and test it per binding | Avoid leaked HTTP/database resources and ambiguous shutdown | Pending owner approval |
| AR-07 | Persistence | Core must work without persistent storage; SQLite implementation remains optional | Allows small consumers and avoids a database prerequisite | Pending owner approval |
| AR-08 | Custom storage | Preserve a host-supplied storage extension point as a design goal; do not promise a stable plugin contract until independently proven | Avoid locking every consumer to SQLDelight or exposing immature abstractions | Pending owner approval |
| AR-09 | Retention | Configurable, bounded, and independently disableable history; no fixed 30-day promise yet | Storage size, privacy, archive cost, and verification needs must be measured | Pending owner approval |
| AR-10 | Versioning | Experimental API until first stable package; then semantic versioning; version persisted schemas separately | Separates consumer API compatibility from local-data migrations | Pending owner approval |
| AR-11 | Privacy | No telemetry, location history, cloud sync, or data contribution by default | Local-first and data minimization | Keep as project principle; new sharing requires separate approval |
| AR-12 | AI | No AI provider, model, credential, token, or inference runtime in the engine | Weather calculations and returned values must be deterministic and usable without AI | Keep as project principle |
| AR-13 | Provider access | Provider eligibility, terms, client credential model, cost, and attribution are per consuming deployment | A library cannot assume every host app is non-commercial or can safely distribute a key | Mandatory gate before each adapter |

### API shape to evaluate

The conceptual surface proposed for MER-0.2 is sufficient for the first contract:

- Engine configuration: injected clock, network/source configuration, policies, and optional stores.
- Request: coordinates/place context and explicitly requested features.
- Result: request metadata and one outcome for every requested feature.
- Source report: read-only selected, skipped, failed, and fallback source explanations.
- Resource lifecycle: operation cancellation and engine close.

Keep names and signatures illustrative until the public contract is approved. Avoid publishing provider DTOs, Ktor types, database records, platform objects, or UI-facing presentation strings.

### Feature-level result semantics

Recommended result behaviour:

- A forecast feature can succeed while alerts are unavailable.
- A result can be partial without pretending to be complete.
- Missing, null, invalid, stale, and numeric zero remain distinct.
- Each problem includes a stable code and enough context to identify its feature/source; sensitive data and raw coordinates should not be placed in logs by default.
- A cancelled operation is cancelled. Do not catch cancellation and convert it to an ordinary partial result.
- Invalid request/configuration should be distinguishable from a provider outage or a feature-level unavailable outcome.

The final specification must define whether a malformed request fails the whole operation or only the affected feature. My recommendation is to reject structurally invalid requests before network activity, but preserve feature-level partial failure for valid multi-feature requests.

## 5. Storage and history recommendation

Keep three logical data categories separate:

1. **Response cache:** used to avoid unnecessary provider calls and optionally serve explicitly labelled stale data.
2. **Forecast snapshots:** immutable records of what a provider/model forecast at a particular issue/run time.
3. **Observation history:** quality-controlled measurements used later to verify a forecast.

The core should be usable without a persistent database. Tests should use deterministic in-memory stores. SQLDelight/SQLite may be the optional official implementation after the exact driver matrix and persistence behaviour are approved.

Do not equate cache expiry with forecast-history retention. Cache expiry follows provider freshness and terms; verification history follows the approved scientific experiment and privacy policy. Provide bounded capacity, explicit deletion, schema versioning, atomic writes, duplicate handling, and recovery from interrupted writes.

A 30-day history window is a proposal only. Do not hard-code it or describe it as a settled project decision.

## 6. Lifecycle, concurrency, and cancellation

The public contract should explicitly answer:

- Can one engine instance serve concurrent requests?
- Does an operation have its own cancellation handle or use the caller's coroutine/task cancellation?
- What happens to sibling feature requests when one provider fails?
- What happens when the consumer cancels after some sources have returned?
- Can a cancelled request write a partial cache entry?
- Does closing the engine reject new work and cancel or drain in-flight work?
- Is close safe to call more than once?
- Can the engine be reconstructed safely after close?

Recommended invariants:

- Cancellation propagates across Kotlin, Swift, and JVM boundaries.
- A cancelled request cannot publish an incomplete cache/history record as complete.
- Provider failures are isolated; cancellation is not a provider failure.
- The engine creates no hidden background scheduler.
- A consumer controls engine lifetime and refresh scheduling.
- All time-sensitive tests use a fake clock.

These are contract decisions for MER-0.2, not implementation details to guess later.

## 7. Documentation conflicts and cleanup

The current documentation set contains legacy assumptions that should be reconciled before the corresponding production milestone:

| Area | Legacy wording or ambiguity | Required reconciliation |
|---|---|---|
| API delivery | `ENGINE_SPEC.md` describes partial streaming results; the current master direction proposes batch-first | Decide batch-first in MER-0.2; retain streaming only as a future, evidence-driven extension |
| Storage technology | Earlier research/specification material refers to Room; current master direction proposes optional SQLDelight/SQLite | Keep Room out of the cross-platform core; update legacy storage examples after approval |
| Storage contract | Older storage examples combine cache, statistics, retention, and app settings in one interface | Separate engine-owned weather records from app-owned settings and secure credentials |
| Retention | Older text describes 30 days as decided; newer direction says this is a research proposal | Remove any hard-coded or adopted 30-day claim until approved |
| Observation selection | The MER-4 draft includes a 75-minute/15-km/300-m candidate rule, while one test example mentions 40 km and two hours | Choose thresholds by variable/region and make test expectations consistent |
| Rain probability | A draft blend proposes the share of deterministic models exceeding a rain threshold as a probability | Do not call it calibrated probability without verification; preserve model agreement as a separate statistic |
| Model selection | A fixed list of models for Mexico City appears as a starting proposal | Verify each model's actual coverage, valid variables, horizon, issue time, and archive availability before finalizing |
| AI policy status | `AGENTS.md` treats `AI_POLICY.md` as mandatory; the file is marked proposed for owner review | Confirm adoption status and align document headers; mandatory reading and policy approval are different things |

These are not reasons to discard the architecture. They are reasons to make the authoritative documents agree before implementation.

## 8. Gates before MER-1

MER-1 should begin only after:

1. The owner reviews and approves the M0.1 language, target, driver, and packaging recommendation, including explicit acceptance or resolution of Android ARM64 NOT TESTED.
2. MER-0.2 approves the public API, feature-result semantics, lifecycle/cancellation, optional storage boundary, retention/deletion, and versioning.
3. MER-0.3 approves forecast/observation record semantics, archive/access assumptions, matching policy, evaluation metrics, data-leakage controls, and human review workflow.
4. Every selected provider has current terms, licence, feature/region coverage, rate limit, cost, attribution, credential, and direct-client-use status recorded.
5. The initial forecast specification has fixtures, deterministic failure cases, and acceptance criteria.

## 9. Review outcome

**Recommended:** continue MER-0.1 stabilization; approve no production provider or algorithm yet. Prepare the MER-0.2 and MER-0.3 owner decisions using the registers and invariants in this review. Then implement the smallest independent contract consumer before starting MER-1.

This document is a review aid. It does not itself approve any recommendation.