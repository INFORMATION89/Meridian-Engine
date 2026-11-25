# MER-0.1 — Verified results and remaining gaps

**Branch:** `spike/m0.1-kmp-platform-proof`  
**PR:** [#2](https://github.com/INFORMATION89/Meridian-Engine/pull/2)  
**Baseline workflow:** [Run #103](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45695023715)  
**Latest full workflow:** [Run #152](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45717462136)  
**Latest full-matrix tested commit:** `28817f6a09c0099ac340ff4df55ea5377ec5ed0a`  
**Baseline Run #103 tested commit:** `8cb472db60920d425feb81dea89843a5be03aadf`  
**Recorded:** 2026-11-25

## Owner decision — CI-only validation for now

A passing supported CI matrix is sufficient for current MER-0.1 validation. Physical-device testing is deferred because the owner does not yet have access to all target platforms. No manual device runs are required from the owner, and lack of physical Android/iOS devices is not an MER-0.1 blocker.

The tested coverage must remain explicit: Android x86_64 emulator, iOS ARM64 Simulator/Swift consumer, and JVM Desktop on Windows/macOS/Linux. Android ARM64 runtime and physical Android/iOS execution remain **NOT TESTED** and are accepted deferred limitations. This does not imply those paths passed.

Any extra real-engine Ktor transport checks are CI tests using local deterministic fixtures, not tests requiring owner-provided hardware or live weather-provider access.

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

## 1A. Latest full-matrix run — #152

Run #152 completed all six jobs successfully on commit 28817f6a09c0099ac340ff4df55ea5377ec5ed0a:

| Job | Result | Evidence |
|---|---|---|
| Dependency metadata | PASS | Main dependency metadata generated and verified |
| Linux dependency metadata | PASS | Linux-specific Android checksums added; Android build passed in normal verification mode |
| Apple and iOS | PASS | iOS framework/simulator tests, macOS JVM consumer/app-image, native Swift Package consumer tests |
| Desktop Linux | PASS | JVM consumer and self-contained app-image launched |
| Desktop Windows | PASS | JVM consumer and self-contained app-image launched |
| Android | PASS | Host-side tests and Android 35 x86_64 emulator instrumentation |

Run #152 app-image measurements:
- Windows: 169.7 MiB.
- Linux: approximately 183 MB.
- macOS: approximately 184 MB.

The packaged JVM consumers launched after the runner Java environment was removed. These measurements are for the spike app-images, not final release packages.

The Android log confirms /dev/kvm was available and writable. Android instrumentation therefore passed with KVM; this run does not prove TCG/software fallback without KVM. Android ARM64 remains NOT TESTED. Apple/iOS validation used an ARM64 simulator and Swift Package consumer, not a physical device. Under the owner's decision above, physical-device and Android ARM64 validation are deferred and are not blockers for current MER-0.1 CI acceptance.


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

## 3. Deferred or not tested (not current physical-device acceptance blockers)

- **Android ARM64 runtime:** NOT TESTED. Run #152 uses an x86_64 Android emulator. The owner accepts this as a deferred limitation for current MER-0.1; no physical ARM64 device or hosted ARM64 run is required to satisfy the current CI-only policy.
- **Physical Android/iOS execution:** NOT TESTED. Android emulator and iOS Simulator/Swift consumer CI coverage are accepted for now; physical-device runs are deferred.
- **Real Ktor engine requests:** the current fixture test uses `MockEngine`, not Android/Darwin/CIO. Add deterministic engine-level tests in CI, without live provider access or owner-supplied hardware, before claiming full transport parity.
- **Timeout and transport error mapping:** add CI tests for configured timeout and representative transport failures, including the expected typed error boundary.
- **Platform-specific cancellation:** the shared coroutine cancellation test passes, but consumer-facing cancellation behaviour should be asserted in the relevant CI consumer boundaries before claiming full parity.

These gaps do not invalidate the successful platform jobs. They limit the claims that can be made from them; the physical-device gaps are explicitly accepted as deferred for the current milestone.

## 4. Remaining CI and repository work for MER-0.1

1. Add deterministic fixture requests through the actual Android, Darwin, and JVM/CIO engines in CI, without live external services.
2. Add CI assertions for timeout, transport-error mapping, resource close, and consumer-boundary cancellation.
3. Keep Android ARM64 and physical Android/iOS runs explicitly **NOT TESTED**. Physical-device testing is deferred and accepted under the owner's CI-only decision; it is not a current MER-0.1 blocker.
4. No-KVM TCG fallback remains optional and deferred; do not treat it as Android ARM64 evidence.
5. Complete the dependency/licence inventory and confirm whether NOTICE is required.
6. Re-run the full matrix after code/workflow changes and record the exact tested SHA.
7. Obtain owner review of the evidence and toolchain recommendation before closing MER-0.1 or beginning MER-1.

## 5. Owner gate

PR #2 remains open and Draft. This results record does not approve a production dependency choice, close MER-0.1, authorize MER-1, or authorize merging the PR.
