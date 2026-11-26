# MER-0.2 Contract validation evidence

- **Status:** Planning/evidence template; no M0.2 implementation or contract test has been run yet.
- **Branch:** `work/m0.2-contract-design`.
- **Purpose:** Track proof for the M0.2 public API, lifecycle/networking, storage/cache, and compatibility contracts.

## 1. Current baseline

M0.2 is contract design and deterministic consumer validation. The repository's M0.1 platform spike is evidence for candidate platform paths, not proof that the production API or storage contract is complete.

Owner-approved direction carried into this milestone:
- Kotlin Multiplatform shared core.
- Ktor as the networking implementation candidate behind an engine-owned abstraction.
- SQLDelight/SQLite as an optional persistence candidate, not a core requirement.
- Desktop JVM distribution on Windows, macOS, and Linux; no native desktop ABI promise.
- Independent consumers; no Solstice dependency.

## 2. Proof matrix (pending)

| Contract area | Required evidence | Status | Notes |
|---|---|---|---|
| Public API | Kotlin consumer compiles and exercises public API only | NOT RUN | No stable API claim |
| Swift interop | Swift consumer constructs, queries, cancels, and closes | NOT RUN | Review generated signatures and cancellation mapping |
| Lifecycle | close idempotence; post-close rejection; in-flight cancellation | NOT RUN | Deterministic fake transport |
| Networking | timeout, TLS configuration, transport/protocol/parse errors | NOT RUN | Local fixture only; no live providers |
| Partial results | independent successful component survives another component failure | NOT RUN | Deterministic fixture |
| Storage disabled | construction/query works without opening storage | NOT RUN | Must be default path |
| Storage enabled | explicit adapter, cache round-trip, stale metadata preservation | NOT RUN | Temporary/in-memory adapter |
| Deletion | scoped clear/delete and ownership semantics | NOT RUN | No production retention policy selected |
| Schema migration | supported migration and unknown-future-schema rejection | NOT RUN | Schema version separate from API version |
| Compatibility | API/library/schema/algorithm version separation | NOT RUN | SemVer proposal only until stable API |
| Platform claims | Built/consumer-tested/CI-supported/release-supported labels | NOT RUN | Preserve M0.1 known gaps |

## 3. M0.1 inherited evidence and limitations

The M0.1 PR #15 description records CI run #222 as passing five jobs, including Android x86_64, iOS ARM64 simulator plus Swift consumer, macOS/Linux/Windows Desktop JVM, and dependency/license audit. This is inherited branch documentation, not a new M0.2 run.

Do not claim Android ARM64 runtime or physical Android/iOS device validation. Do not claim native desktop ABI. Mobile release-package legal-notice verification remains outstanding before public distribution.

## 4. Commands and results

No M0.2 build, test, or CI command has been executed as part of creating this evidence baseline. Record exact commands, toolchain versions, host, artifacts, and test counts here when performed.

## 5. Acceptance gate

M0.2 is ready for owner review only when the public API and all three contract documents are internally consistent, independent Kotlin/Swift consumer proof is recorded, deterministic contract tests pass, and unresolved privacy/retention/compatibility decisions are explicitly listed. Owner approval is required before production weather-feature implementation.
