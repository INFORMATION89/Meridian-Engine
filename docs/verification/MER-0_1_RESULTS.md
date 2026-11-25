# MER-0.1 — Verified results and remaining gaps

**Branch:** `spike/m0.1-kmp-platform-proof`  
**PR:** [#2](https://github.com/INFORMATION89/Meridian-Engine/pull/2)  
**Verified workflow:** [Run #103](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45695023715)  
**Commit tested:** `8cb472db60920d425feb81dea89843a5be03aadf`  
**Recorded:** 2026-11-25

## 1. CI result

Run #103 completed successfully on one commit SHA. All four jobs and their relevant steps passed:

| Job | Runner / runtime | Result | Evidence |
|---|---|---:|---|
| Android | Ubuntu 24.04 x86_64 host; Android 35 x86_64 Google APIs emulator with KVM | PASS | Host-side tests and `:android-consumer:connectedDebugAndroidTest` both completed |
| iOS | macOS 26; iOS Simulator ARM64; Swift Package consumer | PASS | XCFramework build, simulator tests, native Swift consumer tests |
| Windows | Windows 2025; Desktop JVM | PASS | Desktop tests and packaged consumer launch |
| macOS | macOS 26; Desktop JVM | PASS | Desktop tests and packaged consumer launch |
| Linux | Ubuntu 24.04; Desktop JVM | PASS | Desktop tests and packaged consumer launch |

The Windows Gradle-cache warning was addressed by omitting Gradle cache setup only in the Windows job. The Windows build, tests, packaged consumer launch, and artifact upload remain enabled and passed in run #103.

## 2. What the current tests actually prove

| Capability | Evidence in repository | Current assessment |
|---|---|---|
| Shared deterministic operation | `MeridianProbeTest`, Android instrumentation test, Swift consumer test | PASS on the CI targets listed above |
| Typed failure | Common test; Android consumer test; Swift error-bridging test | PASS for the exercised cases |
| Suspend operation | Common test and Swift consumer callback test | PASS for completion |
| Cancellation | Common test cancels a suspended coroutine and joins it | PASS in common test execution; platform-specific cancellation propagation is not independently asserted by each consumer |
| SQLDelight transaction and reopen | Android, iOS, and desktop storage probes insert/update in a transaction, close, reopen, and read the updated value | PASS on Android x86_64 emulator, iOS simulator, and JVM desktop runners |
| Swift interoperability | Swift Package consumer imports framework and calls API, typed failure, suspend API, and storage probe | PASS on iOS simulator |
| Packaged desktop consumer | `:desktop-consumer:installDist` followed by launching the generated executable | PASS on Windows, macOS, and Linux |
| Ktor fixture request | Common test uses Ktor `MockEngine` and a local JSON fixture | PASS for common request/response path; does not exercise the platform engines |
| Ktor platform engines | Android, Darwin, and CIO dependencies are declared in platform source sets | Dependency/target integration is part of the build, but no test currently sends a request through each real platform engine |
| Ktor timeout behaviour | No explicit timeout test found in the current source tree | NOT TESTED |
| Ktor platform-specific error mapping | No dedicated test found that asserts transport/timeout failures from Android, Darwin, and CIO engines | NOT TESTED |

## 3. Explicitly deferred / not tested

- **Android ARM64 runtime:** NOT TESTED. Run #103 uses an x86_64 Android emulator. A physical ARM64 device or a hosted ARM64 environment with working virtualization is still required for that acceptance item.
- **Real Ktor engine requests:** the fixture test uses `MockEngine`, not Android/Darwin/CIO. Add deterministic engine-level tests where they can run without live provider access.
- **Timeout and transport error mapping:** add tests for configured timeout and representative transport failures, including the expected typed error boundary.
- **Platform-specific cancellation:** the shared coroutine cancellation test passes, but the consumer-facing cancellation behaviour should be asserted at platform boundaries before claiming full parity.

These gaps do not invalidate the successful platform jobs. They limit the claims that can be made from them.

## 4. Next work, still within MER-0.1

1. Add deterministic Ktor tests that exercise the actual platform engine integrations on Android, iOS, and JVM desktop, without live external services.
2. Add explicit timeout and transport-error mapping tests and document expected cancellation semantics at the consumer boundary.
3. Keep Android ARM64 as a separate deferred device validation.
4. Re-run the full matrix after code changes and record the exact tested SHA.
5. Obtain owner review of the evidence and toolchain recommendation before closing MER-0.1 or beginning MER-1.

## 5. Owner gate

PR #2 remains open and Draft. This results record does not approve a production dependency choice, close MER-0.1, authorize MER-1, or authorize merging the PR.
