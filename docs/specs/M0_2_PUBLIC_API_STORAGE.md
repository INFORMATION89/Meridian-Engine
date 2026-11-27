# M0.2 — Public API and local storage contract

**Status:** active design specification. The production technology baseline is approved by ADR-0003; the public API and detailed storage contract remain subject to their M0.2 acceptance and owner review. No production weather feature or production database schema is authorized by this document.  
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

Recommend a batch-oriented suspend request/response API for the first release. M0.1 demonstrated that a native Swift consumer can import and call the generated framework, but it did not prove the ergonomics of the final asynchronous weather API or cancellation semantics. M0.2 must prototype a narrow Swift-friendly facade and verify cancellation across Kotlin and Swift. PR #24 subsequently demonstrated Swift Task cancellation into a Kotlin suspend coroutine with KMP-NativeCoroutines 1.0.7, Kotlin 2.4.21, and the async-only SwiftPM product; see the updated M0.2 proposal and [CI #251 attempt 3](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45916840825). This is an experimental interoperability proof, not an automatic production dependency decision. Do not add streaming, Flow, callback, or multiple overlapping API styles unless a concrete consumer requirement justifies them.

Errors should be typed and distinguish invalid input, unsupported feature, no eligible source, provider failure, rate limiting, storage failure, cancellation, and stale/unavailable data. Do not collapse a partial feature failure into a total request failure when other requested features succeeded. Never fabricate missing weather values.

## 4. Local storage boundary

Define storage as optional interfaces owned by the engine contract, with implementations injected by the host or supplied by an optional storage module.

### Required storage concepts

- **Response cache:** provider response or normalized feature data, source identity, fetch time, expiry/freshness metadata, schema version, and integrity metadata needed to avoid treating corrupt or incompatible data as valid.
- **Forecast snapshot history:** preserve what a model/provider forecast said at issue time so later verification is scientifically valid. Record issue/run time, fetch time, valid time, lead time, location/grid reference, variable, unit, provider/model/product identity, and source version where available.
- **Observation history:** source/station identity, measurement time, fetch time, variable, unit, quality flags, and spatial/elevation context at the minimum precision required for matching.
- **Retention and deletion:** configurable retention, bounded storage, explicit deletion/clear operations, and documented defaults. The host must be able to disable history while retaining ordinary in-memory or response-cache operation.
- **Transactions and concurrency:** define atomic write boundaries, duplicate handling, concurrent refresh behaviour, and recovery from interrupted writes.

The public engine API must not expose SQLDelight, SQLite, Room, JDBC, file paths, or database row types. SQLDelight/SQLite is the approved initial optional implementation behind the storage contract; the exact schema, driver lifecycle, and migrations remain M0.2 design work. A custom host-provided store remains an option to evaluate and specify without making database implementation types public.

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

- [PR #20, CI #247](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45912885533) passed all five platform/audit jobs for the experimental API contract and independent desktop, Android, and Swift consumers. All public-shape code remains under the experimental opt-in package.
- [PR #24, CI #251 attempt 3](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45916840825) passed all five jobs. Native Swift XCTest passed 12/12, including the explicit Swift Task to Kotlin suspend cancellation probe; Android host and eight emulator instrumentation tests passed.
- The exact upstream KMP-NativeCoroutines v1.0.7 source tag has an MIT license. The experimental plugin and SwiftPM integration are documented in PR #24 and must remain separate from stable production dependencies until M0.2 owner review.
- The current storage proof is only no-persistence plus an in-memory test store. It does not implement or validate the SQLDelight schema, a supported host storage SPI, persistent transactions/migrations, or archive recovery.
- Additional deterministic contract tests now cover explicit stale-age rejection, refresh of expired cache, cache read/write failures, and cancellation during cache read/write. Their new PR #20 head must pass CI before those cases count as verified.

These results demonstrate prototype behaviour only. They do not validate forecast accuracy, live provider terms, production storage, public API compatibility, or the complete M0.2 acceptance gate.



M0.2 must present explicit recommendations and obtain approval for:

1. The minimal public request/result API and batch-first approach.
2. Confirm the exact host-supplied storage extension contract and its supported guarantees, while preserving the approved requirement that persistence remains optional.
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
