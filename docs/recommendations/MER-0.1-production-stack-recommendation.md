# MER-0.1 production-stack recommendation

**Status:** Historical recommendation — subsequent owner decision recorded in [ADR-0003](../decisions/ADR-0003-approved-production-technology-direction.md)  
**Prepared:** 2026-11-26  
**Evidence baseline:** [CI run #222](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45872259210), code commit `91996fb7c190f80899261eb2ff21630b5e564209`; latest M0.1 PR-head validation: [CI run #224](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45874428103)  
**Authority:** Historical recommendation. ADR-0003 subsequently records the owner's approval of KMP and Ktor, with SQLDelight/SQLite retained as an optional preferred persistence candidate. M0.2/M0.3 acceptance gates remain in force.

## Executive recommendation

Adopt **Kotlin Multiplatform (KMP) for the shared meteorological core, Ktor for HTTP, and SQLDelight for local SQL persistence**, using the already accepted **Desktop JVM-first** distribution strategy for Windows, macOS, and Linux.

This is the lowest-risk choice supported by the current five-platform proof. It reuses one shared Kotlin domain implementation, has real consumer evidence on Android and iOS/Swift, and runs the same JVM artifact on all three desktop operating systems. A Rust/UniFFI rewrite or native desktop ABI is not justified by current requirements or evidence.

This recommendation selects a candidate production technology baseline only. It does **not** approve the final public API, storage schema, provider list, forecast algorithms, telemetry, or production feature implementation. Those remain governed by M0.2/M0.3 and the owner approval gates in `PLAN.md`.

## Evidence

CI #222 passed all five jobs on the exact code commit:

- Android x86_64 emulator: 8/8 instrumentation tests.
- iOS ARM64 simulator: 9/9 common Kotlin tests; native Swift consumer: 10/10 tests.
- macOS desktop: 11/11 common/storage tests; JVM consumer launched.
- Linux and Windows desktop JVM consumers built and launched.
- Dependency/license audit succeeded.

The real Android, Darwin, and CIO HTTP engines were exercised against local fixtures, including request success, HTTP error, malformed, empty/partial response, timeout, transport failure, cancellation, and client cleanup. SQLDelight drivers were exercised on Android, iOS Native, and desktop JVM.

Platform and package limitations are recorded in [MER-0.1 platform validation](../evidence/MER-0.1-platform-validation.md) and [dependency/license review](../evidence/MER-0.1-dependency-license-review.md).

## Recommended components

### Shared core — Kotlin Multiplatform

**Recommend:** KMP with shared meteorological domain and application-neutral contracts.

Why:
- The same shared code was compiled and consumed on Android, iOS, and desktop JVM.
- iOS framework interoperability with a native Swift consumer passed.
- The current platform proof supports the owner's five-OS objective without requiring native Windows/Linux targets.
- It avoids maintaining separate meteorological implementations for Android, iOS, and desktop.

Constraints:
- Keep domain contracts free of Android, JVM, Swift, UI, and provider-specific types.
- Treat Swift interoperability as a first-class public API design requirement. KMP's generated Objective-C/Swift bridge exposes some Kotlin wrapper types; provide a small Swift-friendly facade where needed rather than leaking awkward generated types into consumer APIs.
- Keep the supported platform/architecture matrix explicit. Android ARM64 runtime and physical devices were not tested in M0.1.

### Desktop distribution — JVM first

**Recommend:** retain the owner's accepted JVM-first desktop decision for Windows, macOS, and Linux.

Evidence:
- The same desktop JVM consumer ran on all three operating systems.
- Self-contained app-images launched with the runner's Java environment removed.
- Measured debug/probe app-image sizes were Linux 183.0 MiB, Windows 169.7 MiB, and macOS 171.7 MiB.

Trade-off:
- Bundling a JRE makes app installation independent of a system Java install but adds roughly 170–183 MiB in this probe configuration. This is not a release-size guarantee.
- Meridian's reusable JVM library artifact should not bundle a runtime by itself. A consuming application chooses whether to require a compatible Java runtime or bundle a minimized one.
- Native `.dll`, `.dylib`, and `.so` artifacts, C ABI, Rust bindings, and Windows/Linux Kotlin/Native targets remain deferred unless a named consumer or measured constraint requires them.

### Networking — Ktor

**Recommend:** Ktor 3.6.1 as the candidate cross-platform HTTP client, with platform-specific engines selected by source set:

| Target | Engine tested |
|---|---|
| Android | Android engine |
| iOS | Darwin engine |
| Windows/macOS/Linux desktop | CIO engine |

The actual engines passed deterministic local-fixture tests, including failure mapping, timeouts, cancellation, and disposal. Keep a shared client lifecycle at the engine/application boundary in production; do not create a new client per weather request. Configure connect/request/socket timeouts, redirects, proxy behaviour, user agent, TLS policy, and provider-specific limits explicitly in the production networking specification.

Ktor is approved as the initial networking implementation direction behind Meridian-owned contracts (ADR-0003). Re-check the selected version and platform-specific behaviour when M0.2 defines the real client lifecycle and public error contract.

### Local storage — SQLDelight

**Recommend:** SQLDelight 2.4.1 as the candidate persistence layer, behind a consumer-neutral storage boundary.

| Target | Driver tested |
|---|---|
| Android | Android driver |
| iOS | Native driver |
| Windows/macOS/Linux desktop | SQLite JDBC driver |

Why:
- One SQL schema/query source can serve the supported KMP targets.
- The three driver families were exercised through consumer-facing tests.
- It avoids making Android Room a core dependency that cannot serve all target platforms.

Constraints:
- Persistent storage must remain optional; Meridian must have a useful non-persistent mode.
- Do not expose SQLDelight-generated types in the public API.
- Keep cache expiry separate from forecast/observation verification-history retention.
- Define schema versioning, migrations, deletion, concurrency, transactions, and host-owned storage options in M0.2 before production storage work.
- The desktop SQLite JDBC artifact bundles native libraries and carries additional licensing notices; preserve the reviewed notice files.

## Alternatives considered

### Rust + UniFFI

Not recommended as the initial implementation.

Potential benefits:
- Native library distribution and C ABI options.
- A language/runtime independent of the JVM for desktop.

Why it is not justified now:
- The owner accepted JVM-first desktop distribution.
- KMP already passed the actual five-OS consumer proof.
- Rust would introduce a second language/toolchain, FFI boundary, generated bindings, packaging work, and additional maintenance without a demonstrated blocker.
- A Rust comparator would be useful only if a real consumer requires native desktop embedding or KMP fails a measured performance, footprint, or interoperability requirement.

### Kotlin/JVM-only or platform-specific implementations

Not recommended:
- JVM-only would not provide the tested iOS Native/Swift framework path.
- Separate native implementations would duplicate the most important meteorological logic and increase divergence risk.
- KMP with a JVM desktop target meets the current requirements with one shared core and no native desktop ABI commitment.

### Room as core storage

Not recommended for the engine core because the product targets iOS and desktop as well as Android. Room may remain an application-owned Solstice choice; it must not become a Meridian core requirement.

## Release and privacy requirements that remain

- The engine remains local-first and must not require a Meridian-hosted backend, account, remote database, telemetry collector, or AI runtime.
- Provider requests go directly from the consumer to eligible providers; private developer credentials must not be embedded in distributed artifacts.
- Telemetry/data contribution remains off by default and separate from core functionality.
- Before public mobile distribution, stage and verify `LICENSE` and `THIRD-PARTY-NOTICES.md` alongside the iOS XCFramework and in Android AAR/APK/AAB packaging. Current M0.1 CI does not produce those mobile release packages.
- Keep clothing, umbrella, UV action, activity, and other user-facing recommendations in the consuming app, per [ADR-0002](../decisions/ADR-0002-application-owned-recommendations.md).

## Proposed next sequence after approval

1. Accept KMP + Ktor + SQLDelight as the candidate production baseline, retaining JVM-first desktop.
2. Complete M0 repository/legal bootstrap and reconcile stale research documents.
3. Write and review M0.2 public API, lifecycle, error, cancellation, storage, cache/history, and versioning contracts.
4. Prove those contracts with an independent consumer that does not depend on Solstice.
5. Complete M0.3 scientific verification/archive/matching safeguards before authorizing the first production forecast provider.
6. Begin production features only after the relevant milestone specifications and acceptance criteria are approved.

## Owner decision requested

Approve or reject this candidate baseline:

- **Shared core:** Kotlin Multiplatform.
- **Networking:** Ktor with Android, Darwin, and CIO engines.
- **Persistence candidate:** SQLDelight behind an optional consumer-neutral storage boundary.
- **Desktop:** JVM-first for Windows, macOS, and Linux; consumers may bundle a compatible minimized JRE.
- **Deferred:** Rust/UniFFI and native desktop libraries unless real consumer evidence justifies them.

Approval would authorize detailed M0.2 contract design on this baseline. It would not waive M0.2/M0.3 acceptance gates or authorize immediate production provider/forecast implementation.
