# M0.1 implementation reuse audit for M0.2

- **Status:** Static source review; no code changed and no build/test executed for this audit.
- **Reviewed ref:** `work/m0.2-contract-design`, based on M0.1 evidence commit `c7ad3a9928ecf2bc9ac7b4a26e45f47312d0108e`.
- **Purpose:** Identify which M0.1 proof components are reusable as evidence or test infrastructure and which must not become M0.2 production contracts.

## 1. Executive finding

M0.1 is a platform/dependency spike, not a partially implemented production engine. Its Kotlin source is intentionally a probe. There is no production weather domain model, engine factory, public weather query/result contract, storage abstraction, cache, or stable cross-platform lifecycle API to preserve.

Reuse the proven target matrix, dependency setup, deterministic fixture server, and platform-specific transport/storage test techniques. Do not evolve the probe classes or generated probe database into Meridian's production public API or persistent schema.

## 2. Reviewed implementation

| Area | M0.1 files | Finding | M0.2 treatment |
|---|---|---|---|
| Shared probe | `engine/src/commonMain/kotlin/io/meridian/probe/MeridianProbe.kt` | String transform, sample error enum, and simple suspend operations. No meteorological semantics or engine lifecycle. | Keep as spike evidence only; design a separate engine-owned API from M0.2 contracts. |
| HTTP fixture helper | `engine/src/commonMain/kotlin/io/meridian/probe/FixtureHttpProbe.kt` | Accepts Ktor `HttpClient` directly, parses a deliberately tiny fixture response, and documents caller-owned client lifecycle. | Reuse fixture scenarios/test ideas only. Do not expose Ktor in public signatures; production transport must be engine-owned behind an internal boundary. |
| Android transport | `engine/src/androidMain/kotlin/io/meridian/probe/AndroidKtorProbe.kt` | Creates Android Ktor clients for each probe operation and closes them in the probe wrapper. Exercises real engine behaviour. | Reuse platform engine selection and local-fixture evidence; replace per-probe helpers with lifecycle owned by the actual engine abstraction. |
| iOS transport | `engine/src/iosMain/kotlin/io/meridian/probe/IosKtorProbe.kt` | Creates Darwin clients for probe calls and closes them in `finally`; tests error and cancellation mapping. | Reuse as platform proof, not as the final Swift-facing API or resource ownership model. |
| Desktop transport | `engine/src/desktopMain/kotlin/io/meridian/probe/DesktopKtorProbe.kt` | Uses CIO on Desktop JVM and closes clients around each probe. | Reuse CIO fixture test setup; keep Ktor types and engine selection internal. |
| Common tests | `engine/src/commonTest/kotlin/io/meridian/probe/MeridianProbeTest.kt` | Includes MockEngine tests for status, malformed/empty/partial response, transport, timeout, and caller cancellation. | Reuse deterministic scenarios. Add contract-level tests that prove engine lifecycle and transport cancellation; MockEngine alone is not transport proof. |
| SQLDelight schema | `engine/src/commonMain/sqldelight/io/meridian/probe/db/Probe.sq` | Tiny persistence probe schema. Not a weather cache or verification schema. | Do not reuse as production schema. Keep generated probe database isolated from public API. |
| Android storage | `engine/src/androidMain/kotlin/io/meridian/probe/AndroidStorageProbe.kt` | Requires Android `Context`, deletes a named probe DB, writes/updates, closes, reopens, and reads. | Reuse driver evidence only. Android Context must not enter common/public API. |
| iOS storage | `engine/src/iosMain/kotlin/io/meridian/probe/IosStorageProbe.kt` | Native SQLite driver reopen test with simulator file cleanup. | Reuse driver test technique only; not a storage contract or retention policy. |
| Desktop storage | `engine/src/desktopMain/kotlin/io/meridian/probe/DesktopStorageProbe.kt` | JDBC SQLite temporary-file reopen test. | Reuse JDBC driver evidence only; preserve optional storage and adapter boundaries. |
| Swift consumer | `ios-consumer/Sources/MeridianSwiftConsumer/ProbeUse.swift` and corresponding tests | Exercises a simple generated Kotlin class and transform method. It does not prove the proposed async, cancellation, error, storage, or close contract. | Extend only after a public M0.2 API prototype exists; review generated Swift signatures and cancellation ergonomics. |
| Desktop consumer | `desktop-consumer/src/main/kotlin/io/meridian/consumer/Main.kt` | Imports probe and platform helper classes, not a consumer-neutral production API. | Replace/augment with a standalone public-API-only consumer proof. |
| Build setup | `engine/build.gradle.kts`, `gradle/libs.versions.toml`, `settings.gradle.kts` | KMP targets, Android/Darwin/CIO engines, SQLDelight drivers, dependency versions, and project name `Meridian-MER-0.1`. | Reuse pinned toolchain/dependency evidence as starting point. Reassess module boundaries and dependency exposure during M0.2; do not assume M0.1 module layout is final. |

## 3. Architectural observations

1. `FixtureHttpProbe.fetch` has a public signature containing Ktor's `HttpClient`. This is acceptable for a bounded spike helper but violates the M0.2 public-boundary requirement if promoted unchanged.
2. The platform probes create and close clients within individual helper calls. M0.2 instead needs a consumer-owned engine instance with explicit resource ownership, idempotent close, bounded concurrency, and cancellation of in-flight operations.
3. `MeridianProbe` uses `Dispatchers.Default` directly for a trivial suspend operation. This is not a production concurrency contract and should not be copied as a blanket dispatcher policy.
4. The probe error enum combines sample input and transport fixture failures. M0.2 requires a stable semantic failure taxonomy and must preserve cancellation as control flow.
5. The SQLDelight probe proves a driver can write/reopen/read a tiny database on the tested paths. It does not prove optional-storage behaviour, migrations, stale-cache semantics, retention, deletion guarantees, or public adapter ergonomics.
6. The Android storage helper exposes `Context` because it is a platform test entry point. That type must remain outside common contracts.
7. The Swift consumer demonstrates basic framework import and synchronous invocation only. It is not evidence for async operations, typed failures, cancellation propagation, or resource disposal from Swift.
8. The current consumers import `probe` and platform-specific helper packages. M0.2's independent consumer must instead import only the deliberately exposed public API.

## 4. Reuse decisions

### Reuse as evidence/test infrastructure

- M0.1 platform CI matrix and target runners, subject to current workflow and branch checks.
- Local fixture server and deterministic fixture payloads.
- Tested Ktor platform engine candidates: Android, Darwin, and CIO.
- Tested SQLDelight driver candidates: Android, Native, and JDBC desktop.
- Dependency lockfiles, verification metadata, licence/SBOM process, and existing test-cost safeguards.
- Test scenarios for timeout, transport/protocol/parse failures, partial response, cancellation, and database reopen.

### Do not reuse as production contracts

- `MeridianProbe`, `ProbeFailure`, or `ProbeErrorCode` as the final domain/public API.
- `FixtureHttpProbe`'s Ktor-bearing public method signature.
- Probe-specific Android/iOS/Desktop transport objects as the final engine resource lifecycle.
- `ProbeDatabase` or its schema as production storage.
- Any assumption that a successful SQLDelight driver probe approves mandatory persistence or a retention period.
- Existing Swift/desktop probe consumer shape as proof of the M0.2 public API.

## 5. Recommended M0.2 implementation sequence

1. Prototype the smallest engine-owned public API and result/error types in common code, guided by `M0_2_PUBLIC_API.md`.
2. Add an internal transport abstraction and Ktor adapter; define engine-level client ownership, timeout, cancellation, and close semantics.
3. Create a Kotlin consumer that imports only public API symbols; verify no Ktor or platform types leak.
4. Add the Swift facade/consumer and review generated signatures before freezing declarations.
5. Prove storage-disabled construction and query path first.
6. Add an optional storage adapter prototype only after the public storage boundary is agreed; use temporary test data and no production retention defaults.
7. Add contract tests for partial results, freshness/provenance, cancellation, deletion, and schema version handling.
8. Record actual commands and results in `MER-0.2-contract-validation.md`. Do not infer test success from M0.1.

## 6. Verification limits

This was a static review of the files listed above through GitHub source contents. No local checkout/build environment was used, no commands were run, no M0.2 code was changed, and no CI was triggered. The audit does not establish that the current branch builds or that any M0.2 contract test passes.
