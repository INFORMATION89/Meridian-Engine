# MER-0.2 — Public API and storage decision proposal

**Status:** recommendation for owner review; nothing in this document is an adopted public contract.  
**Parent:** [MER-0.2 specification](MER-0_2_PUBLIC_API_STORAGE.md)  
**Authority:** [PLAN.md](../../PLAN.md) and [RULES.md](../../RULES.md) remain authoritative.  
**Purpose:** turn the existing MER-0.2 requirements into a small set of explicit decisions before any production weather API or provider code is written.

## Executive recommendation

Keep the first API batch-oriented, asynchronous, consumer-neutral, and independent of persistent storage. Make the host application responsible for lifecycle, permissions, saved places, scheduling, and credentials. Let Meridian own weather semantics, provider coordination, normalization, freshness, provenance, and typed result assembly.

Provide an optional SQLDelight/SQLite storage implementation behind interfaces, but do not make SQLite a prerequisite for constructing or querying the engine. Do not promise a user-supplied storage plug-in until the interface has been proven in an independent consumer.

## 1. Proposed public API shape

Use a deliberately small conceptual surface:

- WeatherEngine — one configured engine instance with explicit close()/resource lifecycle.
- WeatherRequest — coordinates/place context and a set of requested features. It does not create, own, or persist saved places.
- WeatherResult — per-feature outcomes, timestamps, freshness, provenance, and problems.
- SourceReport — read-only explanation of selected, skipped, failed, and fallback sources.
- Optional storage contracts — cache and forecast/observation history interfaces, injected at construction.

Illustrative Kotlin shape (names and exact signatures are not final):

~~~kotlin
class WeatherEngine(configuration: EngineConfiguration) {
    suspend fun fetch(request: WeatherRequest): WeatherResult
    fun inspectSources(request: WeatherRequest): SourceReport
    fun close()
}
~~~

The first public call should return a complete batch result. Do not promise Flow, streaming partial updates, callbacks as the primary Kotlin API, or a platform-specific object graph in v1. A later consumer requirement can justify streaming after Swift and Java interop are measured.

### Consumer boundaries

The host application owns:
- saved places and their identifiers;
- location permissions and user consent;
- refresh scheduling and app lifecycle;
- secure storage and delivery of user-supplied provider credentials;
- presentation, localization, and accessibility.

Meridian owns:
- weather feature semantics and normalized values;
- source selection and request coordination;
- freshness, fallback, partial failure, and provenance;
- deterministic aggregation and forecast calculations;
- resource use within one explicitly requested operation.

Meridian must not depend on a Solstice module, Android Context in common code, UI frameworks, or a hosted Meridian service.

## 2. Results, partial failures, and typed problems

A request may ask for multiple independent features. A failed alerts source must not erase a successful hourly forecast.

Recommended result structure:
- one outcome for each requested feature (available, partial, or unavailable);
- values that keep their units and valid time;
- source identity and value-level provenance where multiple sources or transformations contribute;
- issue/run time, observation time when applicable, fetch time, and freshness state;
- a list of machine-readable problems with stable codes and relevant source/feature context.

Keep invalid request/configuration failures separate from feature-level unavailability. Never substitute zero or a plausible default for missing data.

Recommended problem categories:
- invalid request or unsupported feature;
- no eligible source;
- provider transport, response, or parse failure;
- provider rate limit;
- stale or unavailable result;
- storage read/write failure;
- cancellation.

Cancellation is control flow, not a normal weather error. Kotlin cancellation must be rethrown/propagated and must not be converted into a successful partial result. A cancelled operation must not leave a cache record marked complete.

## 3. Freshness and offline behaviour

Use explicit states rather than a single isCached flag:
- fresh — within the applicable source/cache freshness window;
- staleButUsable — expired for normal refresh purposes but retained for an explicit fallback policy;
- expired — retained but not suitable to return as usable weather data;
- unavailable — no usable value exists.

Each returned value retains its original issue, observation, valid, and fetch times. Offline fallback may return stale-but-usable data only when that policy is explicitly enabled and must label it clearly. Meridian must never silently refresh in the background or present expired data as current.

Cache expiry should follow source cadence, provider headers/terms, and rate limits. Avoid one global TTL for all providers and features.

## 4. Storage recommendation

### Recommended boundary

Keep storage optional and behind consumer-neutral contracts:
- WeatherCache for response/normalized-value cache;
- ForecastHistoryStore for preserving forecast snapshots as issued;
- ObservationHistoryStore for observations used later in verification.

The engine must work with no disk storage, using in-memory behaviour when no store is configured. A host can provide an implementation. An optional separate SQLDelight/SQLite module can provide the default implementation for applications that want persistence.

Do not expose SQLDelight, SQLite, JDBC, Android database classes, file paths, or database rows through the public weather API.

### What each store preserves

A cache record should identify the request/feature, source, fetched time, source expiry/freshness, schema version, and integrity status.

A forecast snapshot should preserve provider/model/product, issue/run time, fetch time, valid time, forecast lead, variable, unit, location/grid context, and source version when available.

An observation record should preserve source/station identity, measurement time, fetch time, variable, unit, quality flags, and only the spatial/elevation precision needed for matching.

Writes that publish a completed record must be atomic. Interrupted or partial writes must not masquerade as valid cache/history entries. Duplicate handling, concurrency, schema migration, retention, and explicit deletion must be specified.

### Retention recommendation

- Response cache: source-specific expiry; bounded capacity; stale entries remain visibly stale.
- Forecast/observation verification history: configurable and independently disableable.
- Proposed initial history window: 30 days, but do not hard-code it until storage cost, data scope, and consumer control are accepted.
- No saved-place list or general location history inside Meridian.
- No telemetry, cloud sync, or remote sharing in the core. Any optional data contribution requires separate explicit consent and its own privacy/security design.

There is a current wording conflict: docs/ENGINE_SPEC.md describes 30-day local history as a default, while PLAN.md describes it as a research proposal that is not yet a hard-coded default. This proposal follows the master plan: treat 30 days as a recommendation pending owner approval, not as an adopted default.

## 5. Swift and other consumer interoperability

Keep the canonical contract in Kotlin. Do not expose provider DTOs, database types, Ktor request/response objects, or Android-specific types.

MER-0.1 currently demonstrates a Swift Package importing the framework and calling a Kotlin suspend operation through the generated completion-handler bridge. The first public API should retain suspend semantics in Kotlin and provide a documented Swift convenience wrapper if needed. Do not add an interop framework or change the public API merely to make the spike sample shorter.

For Java/JVM consumers, prefer ordinary Kotlin classes, interfaces, immutable value objects, and documented nullability. Consumer samples must import only the public API.

## 6. Versioning and compatibility

Recommended policy:
- no stability guarantee while the public API is experimental;
- adopt semantic versioning when the first stable public package is approved;
- avoid exposing provider and database implementation types so their evolution does not force public breaking changes;
- version persisted cache/history schemas separately from the public API;
- define migration or safe-discard behaviour for incompatible cache entries;
- document minimum runtime/toolchain requirements per published artifact.

Do not publish packages from MER-0.2. The milestone should prove the contract from source with an independent fixture-backed consumer.

## 7. Required contract tests

Before MER-0.2 can be accepted, deterministic tests should establish:

1. An engine can be constructed and queried with only in-memory/fake dependencies.
2. The same request produces the same structured fixture result across Android, iOS/Swift, and Desktop JVM.
3. One feature can succeed while another is explicitly unavailable.
4. Every returned value retains units, valid time, freshness, and provenance.
5. Fresh, stale-but-usable, expired, and unavailable are distinguishable.
6. Offline requests make no network call and return only permitted cache data or typed unavailability.
7. Cancellation propagates and never commits an incomplete record.
8. Cache/history persistence is optional; the engine works when no persistent store is supplied.
9. SQLDelight implementation can be tested independently behind the storage contract.
10. A standalone consumer uses Meridian without checking out or importing Solstice.

## 8. Owner decisions required

The recommended direction is ready for review, but these decisions remain open:

| Decision | Recommendation | Owner action |
|---|---|---|
| API style | Batch-first suspend request/result | Approve or revise |
| Persistent storage | Optional; never required for core use | Approve or revise |
| Storage extension | Permit a host-supplied store; keep SQLDelight as optional implementation | Approve or revise |
| History retention | Configurable; 30 days as an initial proposal, not a hard-coded rule | Approve or revise |
| Partial failures | Per-feature outcomes plus machine-readable problem list | Approve or revise |
| Cancellation | Preserve Kotlin cancellation semantics; incomplete writes are discarded | Approve or revise |
| Swift bridge | Kotlin suspend contract plus a thin Swift convenience wrapper if needed | Approve or revise |
| API/version stability | Experimental until first stable package; semantic versioning thereafter | Approve or revise |

No MER-1 weather provider or forecast algorithm should be implemented until the MER-0.1 platform decision and the MER-0.2 decisions above have been reviewed and approved.
