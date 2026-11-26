# MER-0.2 Lifecycle and networking contract

- **Status:** Design proposal for M0.2 review; not a stable public API declaration.
- **Baseline:** Kotlin Multiplatform shared core; Ktor behind an engine-owned boundary; Android, iOS/Swift, and Desktop JVM consumers.
- **Authority:** `PLAN.md`, ADR-0001, ADR-0002, ADR-0003, `M0_2_CONTRACT_DESIGN.md`, and `M0_2_PUBLIC_API.md`.

## 1. Goals and non-goals

Define deterministic ownership and behaviour for engine construction, requests, cancellation, networking resources, timeouts, errors, and shutdown. The contract must be independent of Solstice and usable without persistent storage, telemetry, a hosted backend, or an AI service.

M0.2 defines contracts and proves them with local deterministic fixtures. It does not add weather providers, live weather calls, production forecast logic, background refresh, automatic location, or a scheduler.

## 2. Construction and ownership

- An engine instance is explicitly constructed by its consumer. No process-wide singleton, hidden global mutable state, or implicit service locator.
- Configuration is immutable for the lifetime of an instance unless a separately reviewed reconfiguration operation is specified.
- The engine owns only resources it creates. Injected clocks, dispatchers/executors, storage, transport abstractions, and test fixtures retain clearly documented ownership; ownership transfer must never be implicit.
- Construction must be side-effect bounded. It must not request device permissions, discover location, start polling, schedule work, or open persistent storage unless the consumer explicitly enabled the corresponding capability.
- A failed construction must release any resources already acquired before reporting failure.
- `close`/`dispose` is idempotent. After close, new operations fail with a typed closed-engine outcome; already-running operations are cancelled and resources are released according to the contract.

## 3. Request execution

- Every operation is initiated explicitly by the consumer and has a finite, documented request budget.
- Requests must validate arguments before network or storage side effects.
- Each request has a bounded deadline/timeout policy. Connection, request, and overall operation timeouts must be distinguishable where the transport supports them.
- Concurrency limits are explicit and bounded. No unbounded fan-out or retry loop.
- Retries are not automatic by default. Any later retry policy must be idempotency-aware, bounded by the request budget, respect provider limits, and expose retry/fallback provenance.
- A caller may cancel an operation. Cancellation must propagate to the actual transport operation where supported, stop downstream processing, and release request-scoped resources. Merely abandoning the returned future/task is not proof of transport cancellation.
- Cancellation is control flow, not a network failure. Do not wrap cancellation as a generic error or silently convert it into an empty/partial success.
- Closing the engine cancels in-flight operations. The exact race semantics between completion, cancellation, and close must be tested and documented consistently across Kotlin and Swift consumers.

## 4. Networking boundary

- Keep Ktor types and engine implementations behind an internal transport adapter. They must not appear in public signatures or consumer-facing error types.
- Configure TLS certificate/hostname verification securely; no trust-all mode, insecure fallback, or plaintext downgrade.
- Credentials are supplied by the consumer through a narrowly scoped provider-credential boundary only when a future provider requires them. Never embed developer keys in the library, fixtures, test output, or sample applications.
- Do not log authorization headers, query secrets, precise locations, raw provider payloads, or other sensitive request data by default.
- Define user-agent, headers, redirect policy, proxy behaviour, compression, and request-size limits explicitly before production provider work.
- Enforce response-size and parsing limits to avoid unbounded memory use. Malformed or oversized responses become typed failures with safe diagnostics.
- Do not perform implicit telemetry, analytics, or data sharing.

## 5. Error taxonomy

The public contract should distinguish at least:

- invalid input / unsupported capability;
- engine closed / operation already completed where applicable;
- cancellation;
- timeout (with phase when known);
- transport connectivity failure;
- TLS/security failure;
- HTTP/protocol failure (status and safe metadata, not secret-bearing bodies);
- decoding / schema failure;
- storage failure (defined further in the storage contract);
- rate limited / provider unavailable when provider semantics exist;
- internal unexpected failure.

Errors must be stable semantic categories, not leaked Ktor exceptions, platform exceptions, SQL exceptions, or provider SDK classes. Preserve safe causal information for diagnostics without exposing secrets, raw coordinates, credentials, or full payloads. Cancellation remains distinct from all failure categories.

## 6. Partial results and isolation

A failure in one independent source or requested component must not erase valid results from another. A response may represent complete, partial, unavailable, or failed outcomes, with per-component reasons. Do not manufacture values to fill gaps. A stale cached value must retain its original freshness and provenance and must never be labelled current merely because it was returned successfully.

## 7. Diagnostics and observability

- Diagnostics are opt-in or locally inspectable and must not transmit data.
- Provide stable error codes and safe contextual metadata; avoid raw request/response dumps by default.
- Redact credentials, authorization material, precise coordinates, user-linked identifiers, and sensitive payload fields.
- Diagnostic callbacks, if offered, must have bounded execution and must not block or corrupt request execution.
- No global logging configuration or mutation of the consuming application's logging system.

## 8. Deterministic verification

M0.2 contract tests must use local fixtures/fakes and verify:

1. explicit construction and deterministic close;
2. idempotent close and rejection of post-close work;
3. timeout classification and bounded completion;
4. cancellation reaches the transport fake and prevents subsequent processing;
5. close cancels in-flight work and releases owned resources;
6. independent partial results survive a separate component failure;
7. TLS/security configuration cannot silently disable verification;
8. error mapping is equivalent at Kotlin and Swift consumer boundaries;
9. secrets and precise location values are absent from diagnostics;
10. no background request or hidden network activity occurs.

The test suite must not call live weather providers or require network access outside loopback/local deterministic fixtures.

## 9. Open items for prototype evidence

- Exact public async shape and cancellation mapping for Swift.
- Whether the public API exposes a request handle or only structured task cancellation.
- Default timeout values and concurrency ceilings; choose conservative documented defaults in the prototype, then review against measured consumer needs.
- Exact diagnostics interface and safe metadata allowlist.

These routine interface details should be resolved by a Kotlin + Swift consumer prototype. Changes to privacy commitments, credential sharing, or public compatibility require owner review.

## 10. Acceptance

- [ ] Resource ownership is explicit for every created/injected dependency.
- [ ] Close is idempotent and cancels in-flight work.
- [ ] Cancellation reaches the transport layer and is not classified as a network error.
- [ ] Timeouts, transport, TLS, protocol, parse, and closed-engine outcomes are distinct.
- [ ] Partial success is preserved without hiding component failures.
- [ ] Diagnostics are safe, bounded, and non-transmitting by default.
- [ ] Kotlin and Swift independent consumers exercise the same semantics.
- [ ] Tests are deterministic and offline.