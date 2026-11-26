# MER-0.2 Contract validation evidence

- **Status:** Experimental public API and independent-consumer source prototypes added; no M0.2 build, test, or CI has been run yet.
- **Branch:** `work/m0.2-contract-design`.
- **Purpose:** Track proof for the M0.2 public API, lifecycle/networking, storage/cache, and compatibility contracts.

## 1. Current baseline

M0.2 is contract design and deterministic consumer validation. The repository's M0.1 platform spike is evidence for candidate platform paths, not proof that the production API or storage contract is complete.

Owner-approved direction carried into this milestone:
- Kotlin Multiplatform shared core.
- Ktor as the approved initial networking implementation behind an engine-owned abstraction.
- SQLDelight/SQLite as an optional persistence candidate, not a core requirement.
- Desktop JVM distribution on Windows, macOS, and Linux; no native desktop ABI promise.
- Independent consumers; no Solstice dependency.

## 2. Proof matrix (pending)

| Contract area | Required evidence | Status | Notes |
|---|---|---|---|
| Public API | Kotlin consumer compiles and exercises public API only | SOURCE ADDED — NOT RUN | Experimental API and Desktop JVM consumer source added; no stable API claim |
| Swift interop | Swift consumer constructs, queries, cancels, and closes | SOURCE ADDED — NOT RUN | Swift wrapper and test added; generated signatures/cancellation mapping unverified |
| Lifecycle | close idempotence; post-close rejection; in-flight cancellation | PARTIAL SOURCE — NOT RUN | Idempotent close/post-close rejection tests added; in-flight cancellation not implemented in no-source prototype |
| Networking | timeout, TLS configuration, transport/protocol/parse errors | NOT RUN | Local fixture only; no live providers |
| Partial results | independent successful component survives another component failure | MODEL SOURCE — NOT RUN | Domain model construction test added; aggregation behaviour not implemented |
| Storage disabled | construction/query works without opening storage | NOT RUN | Must be default path |
| Storage enabled | explicit adapter, cache round-trip, stale metadata preservation | NOT RUN | Temporary/in-memory adapter |
| Deletion | scoped clear/delete and ownership semantics | NOT RUN | No production retention policy selected |
| Schema migration | supported migration and unknown-future-schema rejection | NOT RUN | Schema version separate from API version |
| Compatibility | API/library/schema/algorithm version separation | NOT RUN | SemVer proposal only until stable API |
| Platform claims | Built/consumer-tested/CI-supported/release-supported labels | NOT RUN | Preserve M0.1 known gaps |

## 3. Prototype files added (not yet compiled)

- `engine/src/commonMain/kotlin/io/meridian/api/WeatherContracts.kt` — experimental query, value, provenance, freshness, quality, partial-result, and issue models.
- `engine/src/commonMain/kotlin/io/meridian/api/MeridianEngine.kt` — explicit factory, no-source unavailable result, idempotent close, and typed post-close exception.
- `engine/src/commonTest/kotlin/io/meridian/api/MeridianEngineContractTest.kt` — deterministic contract tests for validation, unavailable results, partial model construction, and close.
- `desktop-consumer/src/main/kotlin/io/meridian/consumer/PublicApiContractUse.kt` — independent Desktop JVM source importing only `io.meridian.api`.
- `ios-consumer/Sources/MeridianSwiftConsumer/PublicApiUse.swift` and `ios-consumer/Tests/MeridianSwiftConsumerTests/ProbeUseTests.swift` — Swift consumer source and test.

The no-source engine intentionally returns `UNAVAILABLE` with a per-variable `NO_DATA_SOURCE` issue. It performs no network or storage operation. This is contract scaffolding, not a weather capability. The prototype's known limitations are listed in `M0_2_PUBLIC_API.md`.

## 4. M0.1 inherited evidence and limitations

The M0.1 PR #15 description records CI run #222 as passing five jobs, including Android x86_64, iOS ARM64 simulator plus Swift consumer, macOS/Linux/Windows Desktop JVM, and dependency/license audit. This is inherited branch documentation, not a new M0.2 run.

Do not claim Android ARM64 runtime or physical Android/iOS device validation. Do not claim native desktop ABI. Mobile release-package legal-notice verification remains outstanding before public distribution.

## 5. Related implementation audit

See [M0.1 implementation reuse audit](MER-0.2-m0.1-implementation-reuse-audit.md) for the static review of existing probe code, transport helpers, storage probes, consumers, and build setup. It is not a runtime validation.

## 6. Commands and results

No M0.2 build, test, or CI command has been executed in this session. The repository workflow is pull-request-triggered and ignores Markdown-only changes; the current branch has not been opened as an M0.2 PR. Before opening a PR, statically review all source and workflow changes, then validate the exact PR head. Record exact commands, toolchain versions, host, artifacts, and test counts here when performed.

## 7. Acceptance gate

M0.2 is ready for owner review only when the public API and all three contract documents are internally consistent, independent Kotlin/Swift consumer proof is recorded, deterministic contract tests pass, and unresolved privacy/retention/compatibility decisions are explicitly listed. Owner approval is required before production weather-feature implementation.
