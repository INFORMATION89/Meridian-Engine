# MER-0.1 platform validation evidence

**Latest green run:** [GitHub Actions #222](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45872259210)  
**Code commit validated:** `91996fb7c190f80899261eb2ff21630b5e564209`  
**Validation date:** 2026-11-26  
**Scope:** Compatibility and dependency-evidence spike only; not a production-stack approval or release certification.

## Result

All five CI jobs passed on the exact code commit above.

| CI job | Result | Evidence |
|---|---|---|
| Android | PASS | Android x86_64 emulator; 8/8 instrumentation tests, 0 failures/errors/skips |
| Apple/iOS | PASS | ARM64 iOS Simulator; 9/9 common Kotlin tests and 10/10 native Swift consumer tests |
| Linux desktop | PASS | Desktop JVM consumer built, launched, and returned the expected probe success marker |
| Windows desktop | PASS | Desktop JVM consumer built, launched, and returned the expected probe success marker |
| Dependency/license audit | PASS | CycloneDX inventory and strict dependency verification completed successfully |

The Apple job also ran the macOS desktop JVM consumer. Its artifact contains 11/11 common and storage tests with zero failures.

## Real-engine behaviour

The consumer-boundary fixture suite exercised the selected engines against a local deterministic HTTP server; no live weather provider was contacted.

- Android Ktor engine: fixture request, HTTP 503, malformed response, empty and partial response, timeout, transport failure, cancellation, client lifecycle, and SQLite driver reopen.
- iOS Darwin Ktor engine: equivalent real-engine request/error/cancellation cases through the Swift consumer.
- Desktop CIO engine: real fixture request, timeout/error/cancellation paths and storage reopen.
- Common MockEngine tests remain separate unit-level coverage; they are not substituted for real platform-engine tests.

The Android artifact confirms all 8 instrumentation tests passed. The iOS simulator artifact confirms all 9 common tests passed; the Swift XCTest log confirms 10 tests passed with zero failures. The macOS desktop artifact contains 9 probe tests and 2 storage tests, all passing.

A previous run (#221) had one intermittent failure in the Darwin empty-response assertion. The same code passed that test in #222 (2.082 seconds) without a code change; the complete Swift suite passed 10/10. The earlier failure was not reproduced, so no speculative transport workaround was introduced.

## Desktop packaging measurements

The CI artifacts were downloaded and inspected. Sizes below are sums of the uncompressed files inside each self-contained app-image directory, not compressed GitHub artifact sizes.

| Target | Artifact strategy | App-image size | Legal files |
|---|---|---:|---|
| Linux x86_64 | Desktop JVM + bundled JDK | 183.0 MiB | Root `LICENSE` and `THIRD-PARTY-NOTICES.md`; 225 files under bundled JDK legal directory |
| Windows x86_64 | Desktop JVM + bundled JDK | 169.7 MiB | Root `LICENSE` and `THIRD-PARTY-NOTICES.md`; 225 files under bundled JDK legal directory |
| macOS ARM64 | Desktop JVM + bundled JDK | 171.7 MiB | Root `LICENSE` and `THIRD-PARTY-NOTICES.md`; 224 files under bundled JDK legal directory |

The three app-images launched with the runner's Java environment removed, demonstrating self-contained execution for this probe. The sizes are evidence for the tested probe configuration, not release-size promises. The JVM desktop strategy remains the owner's accepted initial direction; consumers may bundle a compatible runtime when they need standalone installation.

## Apple framework and mobile distribution limits

- The CI builds an XCFramework with `ios-arm64` and `ios-arm64-simulator` slices and consumes the simulator slice from a Swift package consumer.
- The Apple artifact is a CI build/test artifact, not a published iOS app or a release-ready SDK package. It currently does not stage the root `LICENSE` and `THIRD-PARTY-NOTICES.md` alongside the XCFramework; release packaging must do so.
- The Android artifact contains instrumentation reports and test logs, not a release APK/AAB.
- Therefore, mobile release-package notice inclusion and final Android/iOS distribution formats remain **NOT VERIFIED**.

## Explicitly untested

- Android ARM64 runtime.
- Physical Android and iOS devices.
- Native Windows `.dll`, macOS `.dylib`, or Linux `.so` consumer ABIs.
- Android APK/AAB and distributable iOS application package notice contents.
- Production weather providers, forecast pipeline, public stable API, production cache/storage schema, and production weather algorithms.

The owner's CI-only acceptance permits the current M0.1 platform proof to proceed without physical-device runs. It does not convert the untested cells above into supported claims.

## Interpretation

This evidence establishes that the current KMP candidate can be compiled and consumed through the tested Android, iOS/Swift, and desktop JVM paths, with the tested HTTP and storage probes. It does not approve Kotlin Multiplatform, Ktor, or SQLDelight as the final production stack. The owner must review the architecture recommendation and remaining distribution boundaries before M1 begins.
