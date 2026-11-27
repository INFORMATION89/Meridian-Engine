# M0.2 — Public API and local storage contract

**Status:** active design specification. The production technology baseline is approved by ADR-0003. The v1 storage visibility decision is owner-approved in ADR-0004: storage SPI remains internal; no host-supplied storage adapter is part of the v1 public API. The remaining public API, data semantics, and persistence implementation details remain subject to M0.2 acceptance and owner review. No production weather feature or production database schema is authorized by this document.  
**Parent plan:** [`/PLAN.md`](../../PLAN.md), sections 6–8, 11–16.  
**Depends on:** completed M0.1 platform proof and the owner-approved KMP + Ktor + optional SQLDelight/SQLite baseline in ADR-0003.  
**Purpose:** define a small, consumer-neutral API and local persistence boundary before implementing weather providers or algorithms.  
**Design recommendation:** [`M0_2_PUBLIC_API_STORAGE_PROPOSAL.md`](../recommendations/M0_2_PUBLIC_API_STORAGE_PROPOSAL.md) — proposed batch API, Swift facade, partial-result, storage, and versioning approach for owner review.

## 1. Objective

Specify how an application embeds Meridian, requests weather data, receives structured results, and optionally supplies local persistence. Meridian must remain a library running in the consumer's process. The normal path must not require a Meridian-hosted service, account, remote database, daemon, or AI provider.

M0.2 is a contract and compatibility decision milestone. It must not implement real weather providers, production forecasts, blending, or the production database.

## 2. Product and runtime boundaries

- Meridian is instantiated by the host application and runs in that application's process.
- The host owns application lifecycle, permissions, saved places, refresh scheduling, user settings, and secure credential storage.
- Meridian owns weather-domain semantics, request planning, source adapters, normalization, provenance, freshness, and deterministic result assembly.
- Network access occurs only in response to an explicit host request or an explicitly configured refresh operation. Meridian does not run a permanent background service.
- No remote Meridian endpoint, account, telemetry, synchronization, or cloud storage is assumed.
- The host may provide a storage implementation through a contract to be specified in M0.2, or use the optional SQLDelight/SQLite implementation. Core domain logic must remain usable without persistent storage.
- The same public concepts and result semantics must be available to Android, iOS, and JVM desktop consumers. Platform-specific factories may inject HTTP and database drivers without leaking those types into the public domain API.

## 3. Public API contract

Design and document a minimal API around these concepts:

1. **Engine configuration:** injected dependencies and policies, including source enablement, request limits, clock, optional cache/history stores, and host-supplied credentials. Configuration must be immutable after construction or have explicit, testable update semantics.
2. **Request:** a place represented by coordinates and requested features, with explicit units/time-zone presentation preferences only where required by the API. The request must not imply that Meridian owns or persists the user's saved-place list.
3. **Result:** a structured response containing requested features, per-feature outcome, values, units, valid/issue/fetch times, freshness, uncertainty/quality indicators where available, source attribution, provenance, and typed problems.
4. **Source diagnostics:** a read-only view of eligible, selected, skipped, failed, and fallback sources with machine-readable reasons. Do not expose provider DTOs.
5. **Cancellation:** cancellation must stop work where practical, propagate predictably across Kotlin/JVM/Swift boundaries, and never leave partially written cache records marked complete.
6. **Versioning:** public types and serialized persistence records need explicit compatibility/versioning rules. Provider response schemas must remain internal to adapters.

### Temporal series and per-value provenance

A requested feature can contain a time series, not just one scalar payload. The public contract must therefore represent time at sample/value granularity:

- Each sample carries its own valid instant or explicit valid interval. Interval quantities such as precipitation must preserve their exact accumulation window and declared boundary semantics.
- Preserve issue/run time, observation/measurement time, fetch time, and valid time as distinct facts. A feature-level timestamp must not be reused as the valid time for every sample in a forecast series.
- Preserve native source cadence and spatial resolution. Gaps remain gaps; interpolation, aggregation, and resampling are explicit transformations, never implicit list-index behaviour.
- Every meaningful value or sample must resolve to its own provenance, quality, freshness, and uncertainty. Shared metadata may be deduplicated internally only when the relationship remains unambiguous.
- A blended or corrected sample must retain the contributing source/model identities and versions, relevant contribution weights where the method uses them, and the transformation/algorithm version. One source label for an entire feature is insufficient for Level 2 results assembled from multiple sources.
- Keep observations, provider forecasts, blends, corrections, estimates, detections, predictions, and official alerts distinguishable. Apparent temperature remains separate from measured air temperature.
- Use unambiguous UTC instants for data semantics. Local calendar interpretation and daylight-saving behaviour use an explicit IANA time zone; the engine must not silently reinterpret instants as local wall-clock values.

The current M0.2 spike has one `validAt` and one provenance record for an entire `WeatherFeatureData`. This is acceptable as a deliberately narrow experiment, but it does not satisfy the stable-contract requirements above and must not be promoted unchanged.

Recommend a batch-oriented suspend request/response API for the first release. M0.1 demonstrated that a native Swift consumer can import and call the generated framework, but it did not prove the ergonomics of the final asynchronous weather API or cancellation semantics. M0.2 must prototype a narrow Swift-friendly facade and verify cancellation across Kotlin and Swift. PR #24 subsequently demonstrated Swift Task cancellation into a Kotlin suspend coroutine with KMP-NativeCoroutines 1.0.7, Kotlin 2.4.21, and the async-only SwiftPM product; see the updated M0.2 proposal and [CI #251 attempt 3](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45916840825). This is an experimental interoperability proof, not an automatic production dependency decision. Do not add streaming, Flow, callback, or multiple overlapping API styles unless a concrete consumer requirement justifies them.

Errors should be typed and distinguish invalid input, unsupported feature, no eligible source, provider failure, rate limiting, storage failure, cancellation, and stale/unavailable data. Do not collapse a partial feature failure into a total request failure when other requested features succeeded. Never fabricate missing weather values.

## 4. Local storage boundary

**Owner decision — 2026-11-27:** Keep the Storage SPI internal to Meridian for v1. Consumers use the stable Meridian weather API; they do not implement or inject a storage adapter. This decision is recorded in [ADR-0004](../decisions/ADR-0004-internal-storage-spi-v1.md).

### v1 architecture

- The public engine API is independent of storage technology and remains usable without persistent storage.
- Meridian owns its internal storage ports, cache identity, record lifecycle, migrations, and consistency guarantees.
- The initial persistent implementation may be provided by an optional official SQLDelight/SQLite module, subject to M0.2 schema and transaction design approval.
- An internal in-memory implementation remains available for deterministic tests and explicitly ephemeral operation; it is not a supported external extension point.
- A consumer cannot supply a custom database, SQLDelight driver, file path, or storage adapter through the v1 public API.
- No persistence configured means no persistent cache or history is implied. The engine must still execute supported requests without storage.

### Required internal storage concepts

- **Response cache:** provider response or normalized feature data, source/configuration identity, fetch time, expiry/freshness metadata, schema version, and integrity metadata.
- **Forecast snapshot history:** preserve issued forecasts independently from observations for later verification. Record issue/run time, fetch time, per-sample valid instant/interval, lead time, location/grid reference, variable, unit, provider/model/product identity, and source version where available.
- **Observation history:** source/station identity, measurement time, fetch time, variable, unit, quality flags, and only the spatial/elevation context needed for matching.
- **Retention and deletion:** separately bounded policies for response cache, forecast archive, observation history, verification records, and licensed datasets. Define explicit deletion/clear semantics and defaults before enabling persistence.
- **Transactions and concurrency:** define atomic write boundaries, idempotency/deduplication, concurrent refresh behaviour, and recovery from interrupted writes. Cancellation must never leave a record marked complete.

The public engine API must not expose SQLDelight, SQLite, Room, JDBC, file paths, database row types, or internal storage interfaces. SQLDelight/SQLite is the approved initial optional implementation behind Meridian's internal storage boundary; schema, driver lifecycle, migrations, and transaction guarantees remain M0.2 design work.

### Future reconsideration

A public storage extension point is not ruled out permanently. It requires a concrete independent-consumer requirement, a separate owner-approved decision, compatibility/versioning policy, conformance tests, and a security/privacy review. Do not design v1 around a hypothetical public SPI or promise that one will be added.

## 5. Offline and freshness semantics

- When online, the engine may retrieve eligible data directly from configured external weather providers, respecting provider terms, credentials, rate limits, and minimum request intervals.
- When offline, it may return cached values with their original issue/fetch/valid times and an explicit freshness state.
- Expired data must never be presented as current. The API must distinguish fresh, stale-but-usable, expired, and unavailable outcomes.
- If no suitable cached result exists, return a typed unavailable result; never invent or silently extrapolate a forecast.
- The host decides when to retry or schedule the next refresh. Meridian exposes enough metadata for that decision but does not schedule background work itself.
- The engine must be able to run deterministic tests with fake clocks, fake network transports, and in-memory stores.

## 6. Privacy and credentials

- Location and weather history remain on-device by default. Meridian must not create a saved-place list, background location tracking, or an external contribution path as a side effect of normal weather requests.
- Do not transmit location, history, diagnostics, or analytics to Meridian-operated infrastructure; none is required.
- The host owns secure storage and supplies credentials to the engine only for providers that legitimately require user-provided credentials.
- Never embed private developer keys in a distributed application or library.
- Logs and diagnostics must avoid raw coordinates and secrets unless a narrowly scoped local debug mode explicitly requires them.
- Never expose raw exception messages, request URLs, query parameters, provider response snippets, or coordinate-bearing strings in public problem details. Optional diagnostic detail must be sanitized and allowlisted; map untrusted exceptions to typed codes.
- Any optional sharing, sync, hosted proxy, remote MCP endpoint, or telemetry requires a separate design and a distinct, explicit opt-in. Before consent, disclose the exact data fields, purpose, destination, recipients, and retention; provide an inspectable payload example and controls to withdraw consent and delete eligible local data. Declining must not reduce ordinary weather functionality. A hosted proxy/endpoint also requires explicit owner approval and a separate privacy, legal, cost, and security review.

## 7. Consumer and compatibility proof

Before M0.2 approval, demonstrate the contract with a tiny independent sample consumer that does not check out, import, build, or test Solstice.

The sample should:
- instantiate Meridian with fake/in-memory dependencies;
- make a deterministic request against fixture-backed fake sources;
- receive a structured result with units, timestamps, provenance, freshness, and a typed partial failure;
- run the same contract test on Android, iOS/Swift, and JVM desktop according to the approved M0.1 matrix;
- demonstrate offline cache behaviour and cancellation;
- show that the core can be constructed and queried without persistent storage, provider credentials, a remote endpoint, or AI.

The sample is a compatibility test, not a user-facing weather application.

## 8. Current prototype evidence (not approval)

- [PR #20, CI #266](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45958135944) passed all five jobs for head `b32f5abbf67cfd62d6a7ed410caa3448d303e5cf`: Windows, Linux, Android x86_64 emulator, Apple/iOS simulator with native Swift consumer, and dependency/license audit. Native Swift XCTest passed 11/11. The experimental contract remains under the opt-in package and is not approved as the stable public API.
- [PR #24, CI #251 attempt 3](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45916840825) passed all five jobs. Native Swift XCTest passed 12/12, including the explicit Swift Task to Kotlin suspend cancellation probe; Android host and eight emulator instrumentation tests passed.
- The exact upstream KMP-NativeCoroutines v1.0.7 source tag has an MIT license. The experimental plugin and SwiftPM integration are documented in PR #24 and must remain separate from stable production dependencies until M0.2 owner review.
- The current storage proof is only no-persistence plus an internal in-memory test store. The v1 Storage SPI is internal by owner decision (ADR-0004). No SQLDelight schema, persistent transactions/migrations, or archive recovery has been implemented or validated.
- The current empty-response Swift fixture uses IPv4 loopback because the fixture server binds IPv4. The partial-response fixture still uses `localhost`; it passed CI #266, but normalizing it to IPv4 is a known cleanup item for a future approved Apple CI run.
- The current prototype still has one feature-level valid time/provenance record. The multi-sample forecast, explicit accumulation interval, and sample-level blended-contributor provenance proof remains outstanding and is required before M0.2 approval.
- Deterministic contract tests also cover explicit stale-age rejection, expired-cache refresh, cache read/write failures, cancellation during source/cache operations, concurrent request isolation, source/cache feature-identity validation, and redaction of source-supplied problem details. These current-head tests passed CI #266.

These results demonstrate prototype behaviour only. They do not validate forecast accuracy, live provider terms, production storage, public API compatibility, or the complete M0.2 acceptance gate.



M0.2 must present explicit recommendations and obtain approval for:

1. The minimal public request/result API and batch-first approach.
2. Confirm the internal storage boundary and optional official SQLDelight/SQLite implementation guarantees; host-supplied storage adapters are not part of v1 (ADR-0004).
3. The official cache/history record boundary and retention defaults.
4. The error and partial-result model.
5. The cancellation and Swift interoperability shape based on the direct-bridge failure and the successful NativeCoroutines prototype evidence.
6. The initial public API stability/versioning policy.

Do not begin M1 provider or forecast implementation until these decisions are approved.

## 10. Acceptance criteria

M0.2 is complete only when:

- Public API types and examples are documented independently of provider and database schemas.
- A dependency diagram confirms that core has no dependency on Solstice, UI frameworks, a hosted service, or an AI provider.
- Cache and forecast/observation history contracts include freshness, provenance, schema versioning, retention, deletion, and atomicity semantics.
- Offline, stale, unavailable, partial-failure, and cancellation behaviour are specified and exercised by deterministic contract tests.
- An independent sample consumer demonstrates the public API without Solstice.
- Android, iOS/Swift, and JVM desktop interop constraints are recorded against the M0.1 results.
- The owner approves the public API, storage boundary, error model, and versioning policy before M1.

## 11. Out of scope

Do not implement live weather providers, forecast algorithms, blending, official alert ingestion, production SQLite schemas, automatic forecast learning, radar, remote sync, telemetry, hosted services, MCP, LLMs, or UI. Do not create a Meridian server to make the local library easier to consume.
