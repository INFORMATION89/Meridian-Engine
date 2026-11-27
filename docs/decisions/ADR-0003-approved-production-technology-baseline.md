# ADR-0003: Approved production technology baseline

- **Status:** Accepted
- **Decision date:** 2026-11-26
- **Scope:** Initial production language, networking, persistence implementation, and desktop distribution
- **Authority:** Explicit project-owner approval following MER-0.1 evidence review

## Context

Meridian must remain an independent, reusable meteorological engine for unrelated consumers on Windows, macOS, Linux, Android, and iOS. The project completed the MER-0.1 platform proof, including real consumer-boundary tests for Android, iOS/Swift, and desktop JVM, and dependency/licence evidence.

CI run [#224](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45874428103) passed all five required jobs. The detailed evidence and limitations are recorded in [MER-0.1 platform validation](../evidence/MER-0.1-platform-validation.md) and [the dependency/license review](../evidence/MER-0.1-dependency-license-review.md).

The project compared Kotlin Multiplatform and Rust, and validated Ktor and SQLDelight candidate paths. The current consumer requirements do not justify the additional language, FFI, native packaging, and maintenance burden of introducing Rust as the initial core.

## Decision

Approve the following initial production baseline:

1. **Shared core: Kotlin Multiplatform.** Meteorological domain semantics, shared contracts, orchestration, validation, and platform-neutral algorithms belong in KMP common code wherever practical.
2. **Networking: Ktor.** Use target-appropriate Android, Darwin, and CIO engines behind internal boundaries. The M0.1 proof used Ktor 3.6.1. M0.2 defines lifecycle, timeouts, cancellation, TLS, redirects, proxy behaviour, and typed errors.
3. **Persistence: SQLDelight/SQLite, optional.** Use SQLDelight 2.4.1 as the initial implementation behind an engine-owned storage contract. Persistence must remain optional; a useful non-persistent mode is mandatory. Generated SQLDelight and database types must not leak into public API.
4. **Desktop: JVM-first.** Windows, macOS, and Linux use the Desktop JVM artifact as the initial integration path. A consumer may require a compatible JVM or bundle a minimized runtime for self-contained delivery. Meridian's library artifact does not itself bundle or impose an application runtime.
5. **iOS: Swift-friendly framework facade.** Consume the KMP Apple framework from Swift. Keep generated Kotlin wrapper types behind a small facade where that materially improves API ergonomics.
6. **Rust/UniFFI and native desktop ABIs: deferred.** Do not introduce Rust, C ABI, native desktop shared libraries, or native Windows/Linux targets in the initial implementation without new evidence and owner approval.

## Evidence and limitations

The M0.1 proof exercised Android x86_64 emulator instrumentation tests; ARM64 iOS simulator common Kotlin tests and a native Swift consumer; macOS desktop JVM consumer and common/storage tests; Windows and Linux desktop JVM consumers; Android, Darwin, and CIO HTTP engines against deterministic local fixtures; SQLDelight driver families on Android, iOS Native, and desktop JVM; and dependency inventory/licence verification.

The following remain explicitly **NOT TESTED**: Android ARM64 runtime, physical Android/iOS devices, native desktop ABIs, and final Android/iOS release-package inclusion of project and third-party notices. Mobile notice staging and verification are mandatory before public mobile distribution. The tested app-image sizes are probe measurements, not release-size guarantees.

## Consequences

- M0.1's production-stack decision gate is closed.
- M0.2 may define public API, lifecycle, storage, cache/history, offline/partial-result, cancellation, error, and versioning contracts on this baseline.
- This decision does not approve a final public API, storage schema, provider list, forecast algorithm, telemetry, hosted service, or production weather feature.
- Meridian remains local-first and must not require Solstice, a Meridian backend, accounts, telemetry, or an AI runtime.
- Future compatible dependency upgrades remain possible through normal maintenance and verification.

## Reconsideration triggers

Reconsider Rust/UniFFI or native desktop distribution only when a named consumer or measured requirement demonstrates a material limitation in KMP/JVM for native ABI access, runtime, performance, footprint, or interoperability. Any strategic replacement requires a written comparison, target proof, dependency/licence review, and explicit owner approval.
