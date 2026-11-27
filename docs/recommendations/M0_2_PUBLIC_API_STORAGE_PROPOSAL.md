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

### Time series and sample-level provenance

The current spike's `WeatherFeatureData` groups several `WeatherValue` items under one `validAtEpochMillis` and one `SourceProvenance`. That shape is intentionally small, but it is not sufficient for an hourly/daily forecast series or Meridian's Level 2 objective.

The recommended logical model is:

- A **feature outcome** groups the result for a requested capability (for example, current conditions, hourly forecast, daily forecast, or official alerts).
- A **series** identifies a variable and its spatial/temporal interpretation and contains explicitly time-indexed samples. It may be irregular; the timestamp is never inferred from the sample's array index.
- A **sample** carries its own valid instant or interval, normalized value and unit, data nature, quality state, freshness, and uncertainty representation.
- A **provenance trace** resolves for each sample/value. It records the source/operator and product/model version, relevant issue/run and fetch times, and any transformations. A blended/corrected value also retains its contributing source/model inputs and the algorithm/configuration version; contribution weights are recorded when the method uses them.
- **Observation metadata** preserves measurement time and station/platform identity independently of issue and fetch time. **Forecast metadata** preserves the exact issued run independently of each sample's valid instant.
- **Interval data** preserves the exact accumulation/validity interval. Instantaneous temperature, interval precipitation, wind gust maxima, and daily extrema must not be collapsed into an indistinguishable timestamp/value pair.
- **Quality and uncertainty** attach at the narrowest level at which they can differ. Model spread, ensemble distribution, provider probability, calibrated probability, and qualitative confidence remain distinct concepts.

Names above describe semantic responsibilities, not final public type names. Keep any shared series/run metadata compact, but make every sample's time and provenance unambiguous. Do not copy one provider/source label to a blended series when the samples have different contributors.

Before M0.2 approval, extend the deterministic contract proof with a multi-sample forecast fixture, a time-bounded accumulation example, and a sample whose blend provenance identifies multiple inputs and the transformation version. Assert that timestamps, intervals, and contributor traces survive result assembly and serialization/cache round-trips where applicable.

## 4. Swift and coroutine interoperability

M0.1 proved that a native Swift consumer can import and call the generated Apple framework, but it did **not** prove Swift `Task` cancellation through Kotlin's generated completion-handler bridge. PR #21's direct bridge cancellation test failed. PR #24 then tested KMP-NativeCoroutines 1.0.7 with the exact Kotlin 2.4.21 toolchain and the async-only Swift Package product. The final [CI #251, attempt 3](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45916840825) passed all five jobs. Native Swift XCTest passed 12/12, including `testNativeCoroutinesPropagatesSwiftTaskCancellation`; the cancelled Swift `Task` reached Kotlin's suspending cancellation probe and completed with `CancellationError` in 0.076 seconds.

**Updated recommendation:** retain the Kotlin `suspend` API as the source of truth and use a narrow Swift-facing facade with KMP-NativeCoroutines-generated async functions for the tested Swift boundary. Pin the proof combination to Gradle plugin/runtime 1.0.7 and SwiftPM tag `1.0.7-spm-async`; use only `KMPNativeCoroutinesAsync`, not Combine or RxSwift. Keep annotations limited to facade methods that genuinely need Swift async exposure. The exact upstream `v1.0.7` source tag contains `LICENSE.txt` under MIT. The experiment adds a Gradle compiler plugin, Kotlin NativeCoroutines artifacts, dependency locks/checksums, and a SwiftPM package dependency; it is not a zero-cost platform feature. Record its target/toolchain and package-footprint implications, and do not add it to the stable production dependency set until the owner reviews this evidence and the M0.2 API decision.

The proof establishes Swift `Task` cancellation into a Kotlin suspend coroutine for this exact combination. It does not by itself prove every Kotlin/Swift cancellation direction, every Xcode/Kotlin version, physical-device behaviour, or cancellation through every future provider/storage adapter. Keep those as explicit compatibility tests for the final facade.

Keep Flow/streaming out of the first public API unless a concrete consumer requirement proves a batch result insufficient; streaming adds lifecycle, ordering, backpressure, cancellation, and cross-language complexity. The current Apple framework uses Kotlin's Objective-C export path. Kotlin Swift Export remains a separate path and must not be adopted until its stability and target compatibility are acceptable.

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

Map untrusted exceptions to typed problem codes; never copy raw `Throwable.message`, request URLs, query parameters, response snippets, or coordinate-bearing strings into public diagnostics. Such messages can contain credentials or private location data. Optional problem detail must be explicitly sanitized and allowlisted before it crosses the public boundary. Apply this rule both to thrown exceptions and to `WeatherProblem` values returned by source adapters; a typed `Failed`, `Unavailable`, or `Partial` result is not automatically safe just because the adapter constructed it. Prefer no free-form detail by default, with only reviewed static/allowlisted diagnostics crossing the public API.

M0.2 must specify client creation/disposal, connect/request/socket timeout budgets, cancellation, TLS, redirects, proxy/user-agent policy, typed transport/status/parse/rate-limit failures, request concurrency, provider minimum intervals, and redaction of credentials/sensitive location data. The host owns secure credential storage. Never embed developer keys in distributed artifacts.

## 7. Versioning and compatibility

Recommend a pre-1.0 public API until semantics have survived independent consumer use. Version public artifacts explicitly, document breaking changes, and keep persistent-record schema versions separate from public API version. Migrate stored data explicitly; never silently reinterpret historical records. Do not promise ABI stability beyond tested artifacts and declared compatibility cells. Keep the standalone consumer in CI independently of Solstice.

## 8. Required proof

An isolated experimental implementation now exists under `io.meridian.contractspike`. All prototype types are guarded by a Kotlin `@RequiresOptIn` marker, and its names/semantics are explicitly not the stable public API. The fixture distinguishes an observation's measurement time from forecast issue time; forecast lead is derived from issue to valid time. The first standalone consumers exercise this proof from desktop JVM, Android instrumentation, and Swift without a Solstice checkout or dependency.

This implementation remains a bounded contract spike: fake source only, optional in-memory store only, no production persistence, providers, algorithms, telemetry, or UI. CI results are tracked in PR #20's description and PLAN.md.

Use fake sources, a fake clock, deterministic fixtures, and in-memory storage. Demonstrate:
- construction without persistent storage;
- a multi-feature request with one success and one unavailable/failed outcome;
- explicit freshness and provenance;
- cancellation through transport/orchestration, including the Swift `Task` to Kotlin suspend bridge;
- offline cached data retaining original issue/observation/fetch/valid timestamps;
- explicit stale-age limits and refusal to return older-than-allowed cache entries;
- source refresh when a cache entry has expired;
- storage-disabled and storage-enabled paths;
- cache read/write failures that preserve usable weather data while reporting storage problems;
- cancellation during cache read/write remaining cancellation rather than becoming a storage error;
- the same contract concepts from Kotlin/Android, Swift/iOS, and desktop JVM;
- no Solstice checkout or dependency.

The current proof covers most of these behaviours with fake sources and in-memory storage. It does **not** prove a SQLDelight production schema, host-supplied storage SPI, transaction/atomicity guarantees under a real persistent store, record migrations, archive export/restore, or final public API versioning. Those remain design and acceptance work within M0.2. No live weather provider is required or permitted for this contract proof.

## 9. Owner decisions still required

The technology baseline is already approved by ADR-0003. Remaining decisions:
1. Minimal public request/result shape and batch-first approach.
2. Public error and per-feature partial-result semantics.
3. Whether/how host-supplied storage is supported in v1.
4. Cache/history boundaries and retention defaults.
5. Swift facade/cancellation contract after the prototype.
6. Pre-1.0 API stability and persistent-schema versioning policy.

Present these with a working standalone sample and evidence, not as abstract technology questions.

### Suggested initial defaults (for owner review)

The following defaults make the proposal testable and give the owner a concrete decision surface. They are recommendations only; they do not authorize implementation of a stable public API or production storage schema.

| Area | Recommended initial default | Why / trade-off |
|---|---|---|
| Engine lifecycle | One immutable engine instance per host-application scope; construction performs no network I/O; the host closes it explicitly. | Reuses clients and resources without introducing a Meridian-owned scheduler or daemon. |
| Concurrency | An engine instance may serve concurrent requests; shared configuration is immutable and request state is isolated. Bound internal work and expose no executor or coroutine-scope types publicly. | Avoids forcing consumer threading choices into the API while keeping resource use controllable. |
| Request validation | Validate coordinates, feature selection, horizon, and limits before starting I/O. Invalid top-level input is a typed request error; unsupported or unavailable individual features remain feature outcomes. | Separates caller mistakes from independent source/feature failures. |
| Partial results | Return one explicit outcome for every requested feature: available, partial, unavailable, or failed. Include structured reasons and provenance where known. Never encode missing values as zero or an ambiguous null. | A source failure for one feature must not erase valid results for another. |
| Cancellation | Propagate coroutine cancellation unchanged. Cancellation is not a weather-provider failure and must not produce a successful/persisted result for work that did not finish. | Keeps Kotlin and Swift task cancellation predictable. |
| Freshness | Prefer fresh cache/network results. Use stale-but-usable data only when the request explicitly allows it and supplies a maximum age. Never relabel stale data as current; preserve original issue, observation, and fetch times. | Makes offline fallback useful without hiding age or uncertainty. |
| Persistence | No persistent store is required or silently created. Support a no-persistence engine and deterministic in-memory test store. SQLDelight/SQLite is an explicit optional module. | Makes privacy, storage footprint, and migration ownership visible to the host. |
| Host-supplied storage | Keep the internal storage port narrow, but defer a supported third-party storage SPI from the first stable API unless an independent consumer demonstrates a real requirement. Add it only with conformance tests for atomicity, concurrency, deletion, and migrations. | A public storage SPI is a long-lived compatibility promise and can constrain future schema evolution. |
| Retention | Do not apply one global retention period. Cache expiry follows provider cadence/HTTP metadata and terms; the agreed initial local learning/verification window is a configurable, bounded 30 days; forecast archives and licensed climatological datasets have separate explicit policies. | Prevents the learning window from deleting scientific archives or long-term datasets. |
| Swift integration | Keep the Kotlin suspend API canonical; expose a narrow Swift async facade using KMP-NativeCoroutines 1.0.7 and its async-only SwiftPM product for the tested Kotlin 2.4.21 toolchain. | The direct completion-handler bridge failed Task cancellation; the NativeCoroutines path passed the 12-test Apple consumer suite. Keep it experimental until owner review and compatibility coverage. |
| Versioning | Use pre-1.0 semantic versions while public semantics are still being validated. Version persisted-record schemas independently from library/API versions and require explicit migrations. | Allows controlled contract evolution without silently reinterpreting stored historical data. |

#### Proposed outcome and failure boundary

For the first contract proof, treat invalid top-level request/configuration and use-after-close as request-level typed errors; treat source, transport, parse, rate-limit, and optional-storage failures as feature/source diagnostics wherever unrelated requested features can still complete. Cancellation remains cancellation. A request with mixed feature outcomes is a valid result, not a top-level exception.

The proof should also establish that closing an engine releases its owned clients, that repeated close is harmless, and that concurrent requests cannot share mutable result state. Exact public type names and whether the request-level error surface is represented by a sealed result or typed exception remain subject to owner approval.

#### Decision emphasis

The highest-impact owner choice is whether a third-party host-supplied storage implementation is a first-release promise. The safest initial recommendation is to keep the internal abstraction from day one, ship the no-storage and official SQLDelight paths, and defer the public SPI until a real consumer and a complete conformance contract justify it. This does not prevent revisiting the choice before 1.0.

## 10. Out of scope

No live provider adapters, production forecast algorithms, source blending, adaptive weights, official-alert ingestion, radar/nowcasting, local learning, telemetry, hosted services, accounts, remote sync, MCP/LLM runtime, UI, or Solstice changes.

## Sources consulted

- Kotlin 2.4.0 release notes, including Swift Export status: https://kotlinlang.org/docs/whatsnew24.html
- Kotlin/Native Swift and Objective-C interoperability, including suspend-function mapping and Swift wrapper annotations: https://kotlinlang.org/docs/native-objc-interop.html
- Kotlin Multiplatform FAQ on calling coroutines from Swift and the current KMP-NativeCoroutines/SKIE options: https://kotlinlang.org/docs/multiplatform/faq.html
- KMP-NativeCoroutines v1.0.7 source tag and its MIT `LICENSE.txt`: https://github.com/rickclephas/KMP-NativeCoroutines/tree/v1.0.7
- KMP-NativeCoroutines v1.0.7 async-only Swift Package tag: https://github.com/rickclephas/KMP-NativeCoroutines/tree/v1.0.7-spm-async
- Meridian's platform/consumer/cancellation evidence: PR [#24](https://github.com/INFORMATION89/Meridian-Engine/pull/24) and [CI #251 attempt 3](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45916840825).
