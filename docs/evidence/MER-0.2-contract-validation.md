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
| Lifecycle | close idempotence; post-close rejection; in-flight cancellation | PARTIAL SOURCE — NOT RUN | Idempotent close/post-close rejection and caller cancellation through a fake executor are covered in source; engine-close cancellation of active work is not implemented |
| Networking | timeout, TLS configuration, transport/protocol/parse errors | NOT RUN | Local fixture only; no live providers |
| Partial results | independent successful component survives another component failure | TEST SOURCE — NOT RUN | Internal deterministic executor seam and pass-through test added; production source aggregation not implemented |
| Storage disabled | construction/query works without opening storage | NOT RUN | Must be default path |
| Storage enabled | explicit adapter, cache round-trip, stale metadata preservation | NOT RUN | Temporary/in-memory adapter |
| Deletion | scoped clear/delete and ownership semantics | NOT RUN | No production retention policy selected |
| Schema migration | supported migration and unknown-future-schema rejection | NOT RUN | Schema version separate from API version |
| Compatibility | API/library/schema/algorithm version separation | NOT RUN | SemVer proposal only until stable API |
| Platform claims | Built/consumer-tested/CI-supported/release-supported labels | NOT RUN | Preserve M0.1 known gaps |

## 3. Prototype files added (not yet compiled)

- `engine/src/commonMain/kotlin/io/meridian/api/WeatherContracts.kt` — experimental query, value, provenance, freshness, quality, partial-result, and issue models.
- `engine/src/commonMain/kotlin/io/meridian/api/MeridianEngine.kt` — explicit factory, no-source unavailable result, idempotent close, and typed post-close exception.
- `engine/src/commonTest/kotlin/io/meridian/api/MeridianEngineContractTest.kt` — deterministic test sources for validation, unavailable results, partial-result pass-through, caller cancellation, defensive collection copying, result invariants, and close.

`MeridianEngine.createForTesting` is internal and exists only to inject deterministic test execution; it is not visible to independent consumers and must not become a public provider SPI by accident.
- `desktop-consumer/src/main/kotlin/io/meridian/consumer/PublicApiContractUse.kt` — independent Desktop JVM source importing only `io.meridian.api`.
- `ios-consumer/Sources/MeridianSwiftConsumer/PublicApiUse.swift` and `ios-consumer/Tests/MeridianSwiftConsumerTests/ProbeUseTests.swift` — Swift consumer source and test.

The no-source engine intentionally returns `UNAVAILABLE` with a per-variable `NO_DATA_SOURCE` issue. It performs no network or storage operation. This is contract scaffolding, not a weather capability. The prototype's known limitations are listed in `M0_2_PUBLIC_API.md`.

## 4. M0.1 inherited evidence and limitations

The M0.1 PR #15 head is `009e5979ce63f8b169c542d24c654fa64b59c363`. Its latest full validation is [CI run #224](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45874428103), which passed all five jobs: dependency/license audit, Linux JVM, Windows JVM, macOS/iOS/Swift, and Android x86_64. Earlier run #222 remains the original platform proof on code commit `91996fb7c190f80899261eb2ff21630b5e564209`. CI #224 validates the M0.1 PR head, not the M0.2 prototype branch.

Do not claim Android ARM64 runtime or physical Android/iOS device validation. Do not claim native desktop ABI. Mobile release-package legal-notice verification remains outstanding before public distribution.

## 5. Related implementation audit

See [M0.1 implementation reuse audit](MER-0.2-m0.1-implementation-reuse-audit.md) for the static review of existing probe code, transport helpers, storage probes, consumers, and build setup. It is not a runtime validation.

## 6. Commands and results

No M0.2 build, test, or CI command has been executed in this session. The M0.2 branch was created from M0.1 documentation commit `c7ad3a9928ecf2bc9ac7b4a26e45f47312d0108e`; its working tree has since been synchronized with the two later M0.1 PR-head file changes, but it is not yet based on the PR's latest commit. PR #15 remains open and must not be merged without owner approval. After M0.1 is merged, rebase/synchronize M0.2 onto the resulting main and validate the exact M0.2 PR head. The repository workflow is pull-request-triggered and ignores Markdown-only changes. Record exact commands, toolchain versions, host, artifacts, and test counts here when performed.

## 7. Acceptance gate

M0.2 is ready for owner review only when the public API and all three contract documents are internally consistent, independent Kotlin/Swift consumer proof is recorded, deterministic contract tests pass, and unresolved privacy/retention/compatibility decisions are explicitly listed. Owner approval is required before production weather-feature implementation.
