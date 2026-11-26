# M0.2 proposal: public API and optional storage contract

**Status:** Proposed for owner review; not an approved public contract.  
**Milestone:** M0.2 — Public API and storage contract.  
**Authority:** PLAN.md, RULES.md, ADR-0002, and ADR-0003.

## 1. Recommendation

Adopt a **batch-first, suspend-based request/response API** with structured per-feature outcomes. Keep Ktor and SQLDelight behind implementation boundaries. Make persistence optional and support a no-persistence engine instance. Keep the public contract meteorological: facts, forecasts, observations, official alerts, derived indicators, evidence, provenance, freshness, quality, and uncertainty. Consuming applications own lifestyle recommendations, user profiles, notification policies, and presentation.

This is a design recommendation, not permission to implement production providers, a production forecast pipeline, or a final database schema.

## 2. Public API

### Lifecycle

Recommend one engine instance per consuming-application scope:
- Construct with immutable configuration and explicitly supplied capabilities.
- Reuse its HTTP client, orchestration, and optional storage handles across requests.
- Expose one clear close/dispose operation owned by the host.
- Do not schedule refreshes, run a permanent background service, or retain location history.
- Allow construction without persistent storage, credentials, remote services, or AI.

Keep Ktor HttpClient, SQLDelight-generated types, database drivers, Android Context, and platform-specific objects out of the public API. Platform factories and implementation modules choose actual drivers.

### Request/response

Recommend one primary asynchronous operation:

`suspend fun fetch(request: WeatherRequest): WeatherResult`

The immutable request should contain only:
- Coordinate-based location supplied by the host; optional altitude only when a feature needs it.
- Explicit requested feature groups.
- Bounded forecast horizon and temporal detail where supported.
- Explicit cache/freshness policy and resource limits only where host control is genuinely needed.

Do not include saved-place IDs, user profiles, clothing preferences, UI state, notification settings, or consumer-specific advice.

The result should include request identity, resolved location context, an outcome for every requested feature, typed meteorological data, units, issue/observation/fetch/valid times, freshness, quality, uncertainty, source provenance, and structured source diagnostics.

### Partial results and failures

One provider or feature failing must not erase successful unrelated features. Represent outcomes per requested feature as available, partial, unavailable, or failed, with machine-readable reasons.

Distinguish invalid request, unsupported feature, no eligible source, provider/transport/parse/rate-limit failure, storage failure, cancellation, and stale cached data. Cancellation must propagate and must not be converted into an ordinary provider failure. Storage failure must not silently claim data was persisted. Never use nulls or numeric sentinels for missing values; zero is valid.

## 3. Meteorological semantics

Preserve SI-normalized quantities, explicit units, UTC instants, and distinct issue, observation/measurement, fetch, valid, and lead times. Keep observed, forecast, blended, corrected, estimated, detected, predicted, and official-alert information distinguishable. Preserve native source resolution; resampling must be explicit.

Keep actual air temperature separate from apparent/feels-like temperature, including method/source where available. Every meaningful result should retain source, freshness, quality, transformations, uncertainty, and contributor information. Do not claim calibrated probability without historical reliability verification.

Meridian may expose neutral meteorological indicators (for example, a likely precipitation window or elevated heat-stress conditions). It must not produce clothing, umbrella, sunscreen, activity, packing, personal-routine, or other lifestyle advice; those remain consumer-owned under ADR-0002.

## 4. Swift and coroutine interoperability

M0.1 proved that a native Swift consumer can import and call the generated Apple framework. It did **not** prove the ergonomics of the final asynchronous weather API, cancellation propagation, or stable public error mapping.

Recommend a coroutine-native Kotlin API and a small Swift-facing facade that presents natural async/await calls and maps public outcomes/errors without exposing awkward generated wrapper types. Verify cancellation in both directions with a real Swift consumer. Do not add a third-party Swift interop plugin until a prototype demonstrates a need and its licence, maintenance, and target support are reviewed.

Keep Flow/streaming out of the first public API unless a concrete consumer requirement proves a batch result insufficient; streaming adds lifecycle, ordering, backpressure, cancellation, and cross-language complexity. The current Apple framework uses Kotlin's Objective-C export path. Kotlin 2.4.0 introduced Swift Export in Alpha; do not switch the production framework to that path until its stability and target compatibility are acceptable. First test suspend-to-Swift async through the existing framework. If direct interop is insufficient, compare KMP-NativeCoroutines and SKIE against the exact Kotlin toolchain before adding either.

## 5. Optional storage architecture

Keep storage contracts in a small engine-owned abstraction. Core must not depend on SQLDelight or SQLite types.

- No storage configured: requests still execute; no persistent cache/history is implied.
- In-memory store: deterministic tests and explicitly ephemeral use.
- Optional SQLDelight/SQLite module: default local persistent implementation, depending on the core storage contract.
- Host-provided store: support only through a documented stable contract with explicit guarantees; no database-specific types leak into core.

Recommended dependency direction:

```text
consumer application
       |
public Meridian API
       |
   engine core  <------ provider adapters
       |
storage contract
       ^
optional SQLDelight module
       |
  SQLite drivers
```

The M0.1 probe groups proof dependencies in one Gradle module. That validates target compatibility, not the final module boundary. M0.2 should define contracts first and split implementation modules only where it improves dependency direction and packaging.

### Separate data categories

1. **Response cache:** short-lived data with source identity, fetch time, expiry, freshness, schema version, and integrity metadata.
2. **Issued forecast archive:** immutable forecast snapshots keyed by provider/model, issue time, valid time, location/grid, variable, and source version.
3. **Observation history:** measurement/fetch times, station/source identity, variable/unit, quality flags, and only the spatial precision needed for matching.
4. **Verification/learning records:** forecast-observation pairs and regional statistics with bounded configurable retention; the current plan's initial recent window is 30 days.
5. **Long-term historical/climatological datasets:** separately licensed and versioned, not subject to the short learning-window limit.
6. **Derived summaries:** reproducible statistics with input dataset and calculation-version provenance.

Specify atomic writes, idempotency/deduplication, concurrent refresh, corruption recovery, schema migrations, retention, deletion, and export/restore. Cache expiry is separate from archive retention. A cancelled/failed write must never leave a record marked complete. The host owns saved places, preferences, and refresh scheduling.

## 6. Networking boundary

Ktor is an implementation detail behind the transport boundary. Recommend one appropriately scoped client per engine instance, reused across requests, with deterministic test injection. Do not expose Ktor types in public API.

M0.2 must specify client creation/disposal, connect/request/socket timeout budgets, cancellation, TLS, redirects, proxy/user-agent policy, typed transport/status/parse/rate-limit failures, request concurrency, provider minimum intervals, and redaction of credentials/sensitive location data. The host owns secure credential storage. Never embed developer keys in distributed artifacts.

## 7. Versioning and compatibility

Recommend a pre-1.0 public API until semantics have survived independent consumer use. Version public artifacts explicitly, document breaking changes, and keep persistent-record schema versions separate from public API version. Migrate stored data explicitly; never silently reinterpret historical records. Do not promise ABI stability beyond tested artifacts and declared compatibility cells. Keep the standalone consumer in CI independently of Solstice.

## 8. Required proof

Use fake sources, a fake clock, deterministic fixtures, and in-memory storage. Demonstrate:
- construction without persistent storage;
- a multi-feature request with one success and one unavailable/failed outcome;
- explicit freshness and provenance;
- cancellation through transport/orchestration;
- offline cached data retaining original timestamps;
- storage-disabled and storage-enabled paths;
- atomicity under cancellation/failure;
- the same public concepts from Kotlin/Android, Swift/iOS, and desktop JVM;
- no Solstice checkout or dependency.

No live weather provider is required or permitted for this contract proof.

## 9. Owner decisions still required

The technology baseline is already approved by ADR-0003. Remaining decisions:
1. Minimal public request/result shape and batch-first approach.
2. Public error and per-feature partial-result semantics.
3. Whether/how host-supplied storage is supported in v1.
4. Cache/history boundaries and retention defaults.
5. Swift facade/cancellation contract after the prototype.
6. Pre-1.0 API stability and persistent-schema versioning policy.

Present these with a working standalone sample and evidence, not as abstract technology questions.

## 10. Out of scope

No live provider adapters, production forecast algorithms, source blending, adaptive weights, official-alert ingestion, radar/nowcasting, local learning, telemetry, hosted services, accounts, remote sync, MCP/LLM runtime, UI, or Solstice changes.

## Sources consulted

- Kotlin 2.4.0 release notes, including Swift Export status: https://kotlinlang.org/docs/whatsnew24.html
- Kotlin/Native Swift and Objective-C interoperability, including suspend-function mapping and Swift wrapper annotations: https://kotlinlang.org/docs/native-objc-interop.html
- Kotlin Multiplatform FAQ on calling coroutines from Swift and the current KMP-NativeCoroutines/SKIE options: https://kotlinlang.org/docs/multiplatform/faq.html
