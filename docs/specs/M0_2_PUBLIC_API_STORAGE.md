# M0.2 — Public API and local storage contract

**Status:** proposed design specification; no production weather feature or storage implementation is authorized by this document.  
**Parent plan:** [`/PLAN.md`](../../PLAN.md), sections 6–8, 11–16.  
**Depends on:** M0.1 platform proof and owner approval of the supported target/runtime strategy.  
**Purpose:** define a small, consumer-neutral API and local persistence boundary before implementing weather providers or algorithms.

## 1. Objective

Specify how an application embeds Meridian, requests weather data, receives structured results, and optionally supplies local persistence. Meridian must remain a library running in the consumer's process. The normal path must not require a Meridian-hosted service, account, remote database, daemon, or AI provider.

M0.2 is a contract and compatibility decision milestone. It must not implement real weather providers, production forecasts, blending, or the production database.

## 2. Product and runtime boundaries

- Meridian is instantiated by the host application and runs in that application's process.
- The host owns application lifecycle, permissions, saved places, refresh scheduling, user settings, and secure credential storage.
- Meridian owns weather-domain semantics, request planning, source adapters, normalization, provenance, freshness, and deterministic result assembly.
- Network access occurs only in response to an explicit host request or an explicitly configured refresh operation. Meridian does not run a permanent background service.
- No remote Meridian endpoint, account, telemetry, synchronization, or cloud storage is assumed.
- The host may provide storage implementations or use a separately packaged optional SQLite implementation. Core domain logic must remain usable without persistent storage.
- The same public concepts and result semantics must be available to Android, iOS, and JVM desktop consumers. Platform-specific factories may inject HTTP and database drivers without leaking those types into the public domain API.

## 3. Public API contract

Design and document a minimal API around these concepts:

1. **Engine configuration:** injected dependencies and policies, including source enablement, request limits, clock, optional cache/history stores, and host-supplied credentials. Configuration must be immutable after construction or have explicit, testable update semantics.
2. **Request:** a place represented by coordinates and requested features, with explicit units/time-zone presentation preferences only where required by the API. The request must not imply that Meridian owns or persists the user's saved-place list.
3. **Result:** a structured response containing requested features, per-feature outcome, values, units, valid/issue/fetch times, freshness, uncertainty/quality indicators where available, source attribution, provenance, and typed problems.
4. **Source diagnostics:** a read-only view of eligible, selected, skipped, failed, and fallback sources with machine-readable reasons. Do not expose provider DTOs.
5. **Cancellation:** cancellation must stop work where practical, propagate predictably across Kotlin/JVM/Swift boundaries, and never leave partially written cache records marked complete.
6. **Versioning:** public types and serialized persistence records need explicit compatibility/versioning rules. Provider response schemas must remain internal to adapters.

Prefer a batch-oriented request/response API for the first release. Do not commit to streaming partial updates, Kotlin Flow, callback APIs, or Swift-specific wrappers until the M0.1 interop proof demonstrates an ergonomic and reliable approach.

Errors should be typed and distinguish invalid input, unsupported feature, no eligible source, provider failure, rate limiting, storage failure, cancellation, and stale/unavailable data. Do not collapse a partial feature failure into a total request failure when other requested features succeeded. Never fabricate missing weather values.

## 4. Local storage boundary

Define storage as optional interfaces owned by the engine contract, with implementations injected by the host or supplied by an optional storage module.

### Required storage concepts

- **Response cache:** provider response or normalized feature data, source identity, fetch time, expiry/freshness metadata, schema version, and integrity metadata needed to avoid treating corrupt or incompatible data as valid.
- **Forecast snapshot history:** preserve what a model/provider forecast said at issue time so later verification is scientifically valid. Record issue/run time, fetch time, valid time, lead time, location/grid reference, variable, unit, provider/model/product identity, and source version where available.
- **Observation history:** source/station identity, measurement time, fetch time, variable, unit, quality flags, and spatial/elevation context at the minimum precision required for matching.
- **Retention and deletion:** configurable retention, bounded storage, explicit deletion/clear operations, and documented defaults. The host must be able to disable history while retaining ordinary in-memory or response-cache operation.
- **Transactions and concurrency:** define atomic write boundaries, duplicate handling, concurrent refresh behaviour, and recovery from interrupted writes.

The public engine API must not expose SQLDelight, SQLite, Room, JDBC, file paths, or database row types. SQLDelight/SQLite remains a candidate implementation only if M0.1 proves the target driver matrix. A custom host-provided store must remain possible unless the owner explicitly chooses a narrower v1 contract.

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

Before approval, demonstrate the contract with a tiny independent sample consumer that does not check out, import, build, or test Solstice.

The sample should:
- instantiate Meridian with fake/in-memory dependencies;
- make a deterministic request against fixture-backed fake sources;
- receive a structured result with units, timestamps, provenance, freshness, and a typed partial failure;
- run the same contract test on Android, iOS/Swift, and JVM desktop according to the approved M0.1 matrix;
- demonstrate offline cache behaviour and cancellation;
- show that the core can be constructed and queried without persistent storage, provider credentials, a remote endpoint, or AI.

The sample is a compatibility test, not a user-facing weather application.

## 8. Decisions required from the owner

M0.2 must present explicit recommendations and obtain approval for:

1. The minimal public request/result API and batch-first approach.
2. Whether persistent storage is optional in the first release and whether a host-supplied storage interface is supported.
3. The official cache/history record boundary and retention defaults.
4. The error and partial-result model.
5. The cancellation and Swift interoperability shape based on M0.1 evidence.
6. The initial public API stability/versioning policy.

Do not begin M1 provider or forecast implementation until these decisions are approved.

## 9. Acceptance criteria

M0.2 is complete only when:

- Public API types and examples are documented independently of provider and database schemas.
- A dependency diagram confirms that core has no dependency on Solstice, UI frameworks, a hosted service, or an AI provider.
- Cache and forecast/observation history contracts include freshness, provenance, schema versioning, retention, deletion, and atomicity semantics.
- Offline, stale, unavailable, partial-failure, and cancellation behaviour are specified and exercised by deterministic contract tests.
- An independent sample consumer demonstrates the public API without Solstice.
- Android, iOS/Swift, and JVM desktop interop constraints are recorded against the M0.1 results.
- The owner approves the public API, storage boundary, error model, and versioning policy before M1.

## 10. Out of scope

Do not implement live weather providers, forecast algorithms, blending, official alert ingestion, production SQLite schemas, automatic forecast learning, radar, remote sync, telemetry, hosted services, MCP, LLMs, or UI. Do not create a Meridian server to make the local library easier to consume.
