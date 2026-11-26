# MER-0.2 Public API contract

- **Status:** Draft for design review; no stable API is declared.
- **Baseline:** Kotlin Multiplatform shared core; Swift-facing iOS consumption; Desktop JVM.
- **Authority:** PLAN.md, ADR-0001, ADR-0002, ADR-0003, and M0.2 contract-design milestone.

## 1. Goals

Define a small, consumer-neutral API for constructing Meridian, submitting explicit meteorological queries, receiving typed results, and releasing resources. The API must be usable without Solstice, UI frameworks, a Meridian backend, persistent storage, telemetry, or AI services.

This document is a contract proposal. The first experimental Kotlin declarations now exist under `engine/src/commonMain/kotlin/io/meridian/api/`; the generated Swift signatures and all platform builds remain unverified until the independent consumer is compiled. The API is not stable.

## 2. Public surface boundaries

Keep the public surface conceptually separated into API/domain, runtime, optional storage integration, and safe diagnostics. Do not expose implementation modules, Ktor, SQLDelight-generated classes, coroutine scopes/jobs, Android Context, filesystem handles, or provider SDKs.

## 3. Consumer workflow

A consumer should be able to construct an engine with minimal explicit configuration, submit a query for a supplied location and time horizon, receive typed results with source/provenance and quality metadata, distinguish complete/partial/unavailable/failed outcomes, cancel an operation, and close the engine deterministically.

No automatic location permission request, location tracking, polling, notification, or background task is part of this workflow.

## 4. API design constraints

### Configuration
- Immutable after construction unless a separately specified reconfiguration operation is approved.
- No-storage mode is the default.
- Provider credentials, if later required, are supplied through a consumer-controlled credential boundary; never bundle private developer keys.
- Network policy, request budgets, locale/time-zone context, and diagnostics have explicit defaults and documented ownership.
- No implicit telemetry or external sharing.

### Queries
- Explicit location input; Meridian does not obtain device location itself.
- Explicit requested variables/capabilities and time range/horizon.
- Validate coordinate range, time range, resolution, and unsupported combinations before network work.
- Never encode app-specific recommendation intent such as clothing, umbrella, activity, UV action, or notifications.

### Results
Every value must carry or reference value and unit, valid time or interval, issue/observation time where applicable, source/model identity and provenance, freshness and quality, and a distinction between observed, forecast, derived, corrected, estimated, detected, and official-authority data.

Missing, unknown, unavailable, unsupported, stale, and invalid are distinct states. Never use numeric zero as a missing-value sentinel.

### Partial results
A request may produce usable data for some variables/sources and not others. Preserve successful independent values and report omissions/failures per component. One provider failure must not erase unrelated results.

### Time and units
- Use explicit physical units for all meteorological quantities.
- Define canonical internal units and conversion/rounding rules.
- Represent machine instants unambiguously in UTC; carry time-zone identifiers where civil-time interpretation is needed.
- Define interval boundary semantics and daylight-saving behaviour.

## 4.1 First executable prototype (experimental, not accepted)

The initial implementation is intentionally small and does not configure providers, networking, or persistence.

Kotlin consumer shape:

```kotlin
val engine = MeridianEngine.create()
try {
    val query = WeatherQuery(
        location = GeoLocation(19.4326, -99.1332),
        variables = listOf(WeatherVariable.AIR_TEMPERATURE),
    )
    val result = engine.query(query)
} finally {
    engine.close()
}
```

The Swift consumer uses `MeridianEngine.companion.create()`, `WeatherQuery.companion.airTemperatureAt(latitude:longitude:)`, and the generated completion-handler form of the suspend `query` method. This is a proposed ergonomic shape; compile-time confirmation is pending.

Prototype semantics:

- `GeoLocation` validates finite latitude/longitude ranges.
- `WeatherQuery` requires at least one unique requested variable and bounds the forecast horizon to 1–240 hours.
- `WeatherQueryResult` represents `COMPLETE`, `PARTIAL`, and `UNAVAILABLE`; exceptional request failures are thrown, not disguised as an unavailable result.
- `WeatherDatum` carries value, unit, valid time, optional issue/observation instants, data kind, source/method provenance, freshness, quality, and optional uncertainty.
- `ComponentIssue` reports per-variable omissions. The prototype returns `NO_DATA_SOURCE` for each requested variable because no production or fixture source is configured.
- `MeridianEngine.close()` is idempotent; a later query throws `MeridianEngineClosedException`.
- Construction and query perform no network, persistent storage, telemetry, location lookup, or background work.

Known design limitations to resolve before accepting this API:

- Units are strings for this first prototype; replace or constrain them with a typed unit contract and canonical conversion rules.
- Epoch-millisecond timestamps are a temporary representation; review a common instant/interval type and timezone semantics.
- The first query supports a forecast-hour horizon, not arbitrary time ranges/resolutions.
- The current no-source implementation proves consumer semantics only. It does not validate provider routing, partial-result aggregation, real transport cancellation, in-flight close behaviour, or cache semantics.
- `close` state is guarded for visibility, but full concurrent lifecycle and in-flight operation guarantees must be implemented and tested when the engine owns cancellable resources.
- The Swift helper factory is a convenience for this proof; review whether it belongs in the eventual public surface.

## 5. Meteorological event contract

Meridian may return neutral event evidence such as precipitation onset, severe-wind period, heat/cold event, or storm detection when validated methods and source coverage support it.

An event includes its type, interval, intensity/severity semantics, probability/confidence where meaningful, supporting variables, provenance, quality, and uncertainty. It is not an official warning unless issued by an identified authority and preserved as such.

Events must not include action directives, clothing/umbrella recommendations, personal routines, consumer notification thresholds, or presentation copy.

## 6. Swift interoperability

The API is not accepted until a Swift consumer can construct and close the engine, submit a query with documented async/cancellation behaviour, inspect values/units/timestamps/provenance/quality/failures without awkward generated wrapper leakage, distinguish cancellation from ordinary failures, and use optional storage without importing Kotlin implementation internals.

A small Swift facade is permitted if it preserves a stable cross-platform semantic contract. Avoid duplicating business logic in Swift.

## 7. Stability and open design decisions

Classify every public symbol as experimental/internal or stable. M0.2 must propose semantic-versioning and deprecation rules before any stable release. Do not mark the API stable merely because it compiles.

Resolve with prototypes and evidence: exact engine factory/query shape; result envelope versus typed per-capability result; Swift facade and async mapping; configuration immutability; diagnostics interface. Routine naming decisions do not require owner escalation.

## 8. Acceptance

- [ ] Kotlin consumer example compiles using public API only. (Prototype source added; build not yet run.)
- [ ] Swift consumer example compiles and is ergonomically reviewed. (Prototype source added; generated signatures not yet confirmed.)
- [ ] No provider/Ktor/SQLDelight/UI/Solstice implementation types leak into public contracts.
- [ ] Missing, partial, stale, and failed states are distinguishable.
- [ ] Units, time, provenance, quality, and uncertainty semantics are documented.
- [ ] Consumer recommendations and notification policy remain outside Meridian.
- [ ] API remains useful with persistence disabled.