# MER-0.1 — CI failure analysis and consolidated repair plan

**Date:** 2026-11-25  
**Pull request:** [#2 — MER-0.1 platform proof](https://github.com/INFORMATION89/Meridian-Engine/pull/2)  
**Baseline reviewed:** commit `922fc1bad2580aa10939c49c042e208fa2926030`; runs #94 and #95  
**Status:** Apple/desktop repair verified; Android emulator strategy revised after run #97

This record explains the observed failures and the coordinated repair. It does not declare any platform supported. The authoritative acceptance criteria remain in [MER-0.1 platform proof](../specs/MER-0_1_PLATFORM_SPIKE.md).

## 1. Evidence from the failing runs

### Desktop Windows and Linux

Run #94 completed the Windows and Linux jobs successfully. Both jobs compiled the desktop target, executed `:engine:desktopTest`, built the packaged JVM consumer with `:desktop-consumer:installDist`, and launched that consumer. Their warning output did not represent failed desktop tests.

### Android

Run #94 used a standard Linux x86_64 runner without usable KVM permissions and explicitly enabled TCG software CPU emulation. The emulator eventually reported boot complete after approximately 582 seconds. ADB repeatedly returned `device offline`; after boot, system services returned broken-pipe errors and the action exited with ADB code 224. The action failed before its supplied Gradle instrumentation-test script ran.

The first repair moved Android instrumentation to the standard Apple Silicon `macos-15` runner with an Android 35 `arm64-v8a` image. Run #97 disproved the assumption that this hosted macOS VM exposes usable nested virtualization: the emulator logged `HVF error: HV_UNSUPPORTED`, followed by `qemu-system-aarch64-headless: failed to initialize HVF: Invalid argument`. ADB never discovered `emulator-5554`; the action timed out before the instrumentation script. Android host-side common tests did pass.

These are CI virtualization failures, not Android source/test failures. Do not retry either unsupported path: Linux TCG is too slow/unreliable, and this macOS hosted runner does not expose Hypervisor.framework to the nested emulator.

**Revised strategy:** run the Android job alone on `ubuntu-24.04`, install the Android 35 `x86_64` Google APIs image, explicitly grant the runner read/write access to `/dev/kvm`, verify KVM access before launching the emulator, and force hardware acceleration (`disable-linux-hw-accel: false`). GitHub documents hardware-accelerated Android virtualization on standard Linux runners, and the emulator action documents the KVM permission setup. Keep Android API 36 installed as the compile SDK and API 35 as the emulator platform.

**Scope limitation:** this produces a real Android instrumentation result on x86_64, not an ARM64 runtime result. Android ARM64 remains a separate NOT TESTED acceptance item until a physical ARM64 device or a hosted environment that actually exposes ARM64 virtualization is available. Do not label ARM64 runtime support PASS based on this x86_64 emulator.

### Apple / iOS

The Kotlin/Native iOS framework build and `iosSimulatorArm64Test` completed successfully. The independent Swift Package consumer then failed at final link with unresolved `_sqlite3_*` symbols from SQLDelight's Native driver.

Adding `-lsqlite3` to the Kotlin framework was necessary but insufficient for this static XCFramework consumer: the Swift package's final link also needs an explicit system-library declaration. The Swift consumer and its test target now declare `sqlite3` through Swift Package Manager's `linkerSettings`.

## 2. Consolidated changes

| Area | Repair |
|---|---|
| Android CI architecture | Run only the Android job on `ubuntu-24.04`; use x86_64 Google APIs emulator with explicitly enabled and verified KVM; remove both TCG and unsupported macOS HVF dependency |
| Android SDK completeness | Install both compile SDK API 36 and emulator API 35, including the x86_64 Google APIs image |
| Android test coverage | Enable KMP Android host tests and execute `:engine:testAndroidHostTest` before device instrumentation |
| Swift interoperability | Link system `sqlite3` from both Swift Package consumer and test targets |
| SQLite proof | Test insert + update inside a SQLDelight transaction, close the driver, reopen it, and verify the persisted updated value on Android, iOS, and desktop |
| Gradle DSL | Remove deprecated source-set delegate accessors and unused imports; enable `--warning-mode=all` in CI |
| CI maintenance | Update checkout, Java setup, Android SDK setup, and artifact actions; enable Gradle dependency caching |

The Kotlin/Gradle/AGP candidate versions remain unchanged. Kotlin 2.4.21, Gradle 9.7.0, and AGP 9.3.1 are within the published compatibility range; changing the stack would not address either observed platform failure.

## 3. Verification gates after the repair

A fresh PR run must establish each result from logs and uploaded artifacts:

1. **Windows, macOS, Linux desktop:** packaged JVM consumer launches and prints the MER-0.1 success marker; desktop storage transaction/reopen test passes.
2. **iOS:** Kotlin/Native XCFramework builds; simulator common tests pass; the Swift Package consumer imports the framework, exercises the API and typed error, and completes the SQLDelight SQLite reopen test.
3. **Android host:** `:engine:testAndroidHostTest` executes the common suite on the Android host-test target.
4. **Android device (CI):** `ubuntu-24.04` is confirmed as x86_64; `/dev/kvm` is readable and writable; the Android 35 `x86_64` emulator boots using KVM; `:android-consumer:connectedDebugAndroidTest` runs and passes against the actual consumer app. Record ARM64 runtime as NOT TESTED until a physical ARM64 device or supported ARM64 virtualization host is exercised.
5. **SQLite matrix:** each platform inserts and updates within a transaction, closes the driver, reopens the same database, and reads the updated value.
6. **Warnings:** inspect `--warning-mode=all` output. Do not hide warnings with blanket suppression; fix project-owned deprecations and separately record unavoidable third-party warnings.
7. **Evidence:** update the PR's platform table with PASS / FAIL / BLOCKED / NOT TESTED, exact runner/ABI/toolchain, command, and log/artifact link. Compilation alone is not a runtime PASS.

## 4. Sources consulted

- Kotlin Multiplatform compatibility guide: https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html
- Android KMP host/device test configuration: https://developer.android.com/kotlin/multiplatform/plugin
- Swift Package Manager target linker settings: https://docs.swift.org/package-manager/PackageDescription/PackageDescription.html
- GitHub-hosted runner architectures: https://docs.github.com/en/actions/reference/runners/github-hosted-runners
- ReactiveCircus Android Emulator Runner documentation and Apple Silicon ARM64 support: https://github.com/ReactiveCircus/android-emulator-runner
