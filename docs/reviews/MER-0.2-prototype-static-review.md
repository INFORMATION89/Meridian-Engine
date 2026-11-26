# MER-0.2 experimental API prototype — static review

- **Review status:** Static source review only; no build, test, generated Swift interface, or CI execution was performed.
- **Reviewed branch:** `work/m0.2-contract-design`
- **Scope:** Experimental API/domain models, engine lifecycle, common contract tests, Desktop JVM consumer, Swift consumer, and the M0.2 contract specifications.
- **Decision:** Keep the prototype experimental. Do not expand it into provider or production forecast implementation until the blocking contract issues below are resolved and the exact branch is built and tested.

## 1. Blocking contract issues

### 1.1 Result completeness is not tied to the request

`WeatherQueryResult` validates only broad envelope shape. A `COMPLETE` result can contain one value even when the query requested several variables; a `PARTIAL` result does not ensure that every requested variable is represented by either a value or an issue; duplicate or contradictory variable outcomes are not rejected.

**Required correction:** Make result construction validate coverage against the originating query, or introduce a request-scoped result model that retains the requested capabilities. Define duplicate, missing, and conflicting outcomes explicitly. Add tests for multi-variable COMPLETE/PARTIAL/UNAVAILABLE results and contradictory component coverage.

### 1.2 Engine close does not cancel in-flight work

The current volatile `closed` flag prevents a query that begins after close, but it does not own or cancel active operations. A query can pass the closed check and continue after another thread closes the engine. The current cancellation test proves only that caller coroutine cancellation reaches an injected suspending lambda.

**Required correction:** Introduce engine-owned lifecycle/cancellation semantics without exposing coroutine scopes/jobs in the public API. Define the close-versus-query race, ensure close prevents new work and cancels active operations, and test both race orderings with deterministic synchronization. Cancellation must remain cancellation rather than becoming a failure result.

### 1.3 The meteorological data-kind model is narrower than the documented contract

The specifications distinguish observed, forecast, derived, corrected, estimated, detected-event, and official-authority data. The current `WeatherDataKind` omits detected events and official alerts, so the model cannot express the full documented distinction. An official alert must also retain issuing authority identity and must not be confused with Meridian-detected events.

**Required correction:** Align the domain model with the event/authority contract before accepting it. Keep event evidence and official alerts distinguishable, with authority/source metadata and explicit temporal semantics.

## 2. High-priority contract gaps

- `unit` is an unconstrained string. Define canonical unit identifiers and conversion semantics before any production value can be interpreted safely.
- Epoch-millisecond fields are a temporary time representation. Define an unambiguous instant and interval contract, including issue/observation/valid times and timezone handling where civil-time interpretation is needed.
- `ComponentIssue.safeMessage` accepts blank or arbitrary text. Prefer stable codes plus structured, explicitly safe metadata; do not rely on callers to provide sanitized free-form messages.
- The model does not yet define a stable distinction between unsupported, missing, stale-only, invalid, and source-unavailable component outcomes. The issue taxonomy should match the specifications without overloading a single code.
- `WeatherDatum` validates finite values and non-negative uncertainty, but does not validate temporal relationships or variable/unit compatibility. Those checks depend on the eventual typed unit/time contracts.
- `WeatherQuery.forecastHours` is an intentionally provisional horizon, not a product decision. Do not expose it as a permanent limit.
- The Swift helper factory is convenient for the proof but should not be treated as an approved permanent API until the generated interface and ergonomic review are complete.

## 3. Consumer and build validation

- The Desktop consumer imports only `io.meridian.api`, which is the right independence direction.
- The Swift source exercises the experimental API, but the generated Kotlin/Native signatures have not been compiled or inspected. The callback shape, nullability, exception mapping, and async/cancellation ergonomics remain unverified.
- The current Swift test checks that a status string is non-nil; it should assert the expected semantic status and inspect the returned contract fields where the interop surface permits.
- The current common tests are source-only. No M0.2 Kotlin compilation, common tests, Desktop consumer execution, XCFramework build, or Swift test has been run.
- The engine's production query path intentionally has no configured data sources and returns UNAVAILABLE. This is valid scaffolding, not evidence of weather capability.

## 4. Recommended correction order

1. Fix request/result coverage invariants and add deterministic tests.
2. Design engine-owned lifecycle semantics and close/cancellation race tests.
3. Align event and official-alert data kinds with the documented meteorological contract.
4. Define typed unit and time contracts before adding more meteorological fields.
5. Tighten issue/error semantics and safe diagnostics.
6. Compile and execute the Kotlin/JVM consumer and common tests.
7. Build the iOS framework, inspect generated Swift signatures, and execute the Swift consumer/tests.
8. Only after those checks, decide whether the API shape is ready for owner review.

## 5. Branch and validation gate

The M0.2 branch was created from an earlier M0.1 documentation commit and has not yet been rebased onto the latest M0.1 PR head. PR #15 remains open and must not be merged without owner approval. After the M0.1 integration gate is satisfied, rebase/synchronize M0.2 onto the resulting `main`, then validate the exact M0.2 head. Review workflow triggers and the expected CI cost before any push that could start CI.

No production providers, live weather requests, persistence, telemetry, background work, or Solstice-specific API concepts are authorized by this review.
