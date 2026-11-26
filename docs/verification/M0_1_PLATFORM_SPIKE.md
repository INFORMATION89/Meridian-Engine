# M0.1 platform spike — initial execution report

**Status: in progress.** This report records local Windows and hosted CI evidence collected so far. It is not a production stack recommendation or an approval to start M1.

## Local host and pinned candidate versions

| Item | Value |
|---|---|
| Build host | Windows 11 x64, build 26300 |
| JDK | Eclipse Temurin 21.0.13.1+2 LTS |
| Installed Gradle | 9.7.0 |
| Gradle wrapper | 9.7.0 |
| Kotlin / KMP Gradle plugin | 2.4.21 |
| Android Gradle Plugin | 9.3.1 with the Android-KMP library plugin |
| Android compile SDK | API 36; emulator image available locally is API 37 x86_64 |
| Ktor | 3.6.0 |
| SQLDelight | 2.1.0 |
| kotlinx.coroutines | 1.11.0 |
| kotlinx.serialization | 1.11.0 |
| JVM bytecode target | 11; local runtime check used JDK 21 |

The official compatibility guide lists Kotlin 2.4.21 with Gradle through 9.7 and AGP through 9.3.1. AGP 9 requires the Android-KMP library plugin alongside the KMP plugin; the legacy `com.android.library` plugin failed when first tried with Gradle 9.7. The current build uses the Android-KMP plugin.

## Results matrix

| OS / target / architecture | Compile or link | Consumer loaded and executed | Ktor | SQLDelight | Current label |
|---|---|---|---|---|---|
| Windows / Desktop JVM / x64 | PASS: JVM library and consumer distribution built | PASS: packaged consumer launched locally and in CI | PASS: common JSON fixture test; CIO instantiated | PASS: SQLite file write, close, reopen, read | Experimental |
| Android / Android-KMP / ARM64 required; x86_64 supplemental | PASS: Android app/APK and instrumented tests compile | BLOCKED: emulator runtime still pending; the hosted ARM64 runner could not install the emulator package, so CI now runs x86_64 as a supplemental Android check | PASS: Android and CIO candidates compile; actual engine request not run | BUILD-ONLY: Android driver test compiles; device execution pending | Build-only |
| iOS / `iosSimulatorArm64` / ARM64 simulator | PASS: device and simulator frameworks linked in CI | PASS: Swift consumer ran in iPhone simulator; shared transform/error and async callback returned PASS; Native SQLite test passed | BUILD-ONLY: Darwin candidate compiles; real fixture request not run | PASS: Native driver test ran on simulator | Experimental |
| macOS / Desktop JVM / Apple Silicon | PASS: JVM tests and consumer distribution built in CI | PASS: packaged consumer launched in CI with JDK 21 | PASS: common JSON fixture test; CIO instantiated | PASS: SQLite file write, close, reopen, read | Experimental |
| Linux / Desktop JVM / x64 | PASS: JVM tests and consumer distribution built in CI | PASS: packaged consumer launched in CI with JDK 21 | PASS: common JSON fixture test; CIO instantiated | PASS: SQLite file write, close, reopen, read | Experimental |

**No platform is marked Supported.** The project definition reserves that label for a build and real-runtime check in CI; these are experimental spike results. Android ARM64 runtime execution remains outstanding. Ktor's target engines still need local-fixture requests and cancellation checks on their real targets.

## Windows evidence

Commands run from the repository root:

```powershell
gradle --no-daemon --no-build-cache --console=plain :probe:jvmTest :probe:jvmJar :desktop-smoke:run :desktop-smoke:installDist
& .\desktop-smoke\build\install\desktop-smoke\bin\desktop-smoke.bat
$env:ANDROID_HOME = 'C:\Users\marco\AppData\Local\Android\Sdk'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
gradle --no-daemon --no-build-cache --console=plain :android-smoke:assembleDebug
gradle --no-daemon --no-build-cache --console=plain :android-smoke:compileDebugAndroidTestKotlin
gradle --no-daemon --no-build-cache --console=plain :probe:compileKotlinIosSimulatorArm64 :probe:compileTestKotlinIosSimulatorArm64
```

Results:

- JVM test suites: 5 tests passed, 0 failures. These cover the deterministic transform, typed error, async success/cancellation, Ktor MockEngine JSON fixture, CIO construction, and SQLDelight persistence after reopening a file database.
- Packaged Windows consumer: PASS; prints `m0.1: JVM consumer PASS`.
- Android library and smoke app: PASS compile/package. Instrumented test sources: PASS compile. The local Windows AVD is x86_64, and emulator startup reported that hardware acceleration is required but no emulator hypervisor driver is installed; device tests did not run. The first hosted CI attempt used an x64 Linux runner with an ARM64 image; the emulator did not appear in ADB and timed out. A second attempt on `ubuntu-24.04-arm` could not find the Android emulator package for that host architecture. CI now uses an x86_64 emulator on Ubuntu as a supplemental Android runtime check; it does not satisfy the required ARM64 runtime check.
- iOS simulator Kotlin source and test source: PASS cross-compilation. No Apple framework was linked and no Swift code was run on this Windows host.
- Hosted CI (all jobs except Android): macOS 15.8.2 Apple Silicon with Temurin 21.0.13.1+2 linked `iosArm64` and `iosSimulatorArm64`, passed `iosSimulatorArm64Test` including the Native SQLite test, built the Swift consumer using Xcode 16.4 / iOS Simulator SDK 18.5, launched it on an iPhone 16 Pro simulator, and observed the `PASS` result marker. Exact workflow run: `45724007519`.
- Hosted CI Desktop JVM: Windows Server 2025 x64, macOS 15.8.2 Apple Silicon, and Ubuntu 24.04.5 x64 each passed `:probe:jvmTest`, `:desktop-smoke:run`, and `:desktop-smoke:installDist`; the packaged consumer printed `m0.1: JVM consumer PASS` on all three. CI used Temurin JDK 21.0.13.1+2 (runner builds identify it as 21.0.13). Run: `45724007519`.
- The first Android CI attempt on Ubuntu x64 failed because the ARM64 emulator never appeared in ADB. A second attempt on `ubuntu-24.04-arm` failed before Gradle because the emulator package is unavailable for that host architecture. An x86_64 emulator on Ubuntu x64 also failed to boot because the runner lacks a hardware accelerator and the current Android emulator requires explicit TCG opt-in when hardware acceleration is disabled. CI now enables `ANDROID_I_WANT_MY_TCG=yes` and allows 20 minutes for boot; that supplemental retry is pending. It cannot satisfy the required ARM64 runtime check.
- With corrected task ordering, CI measured the JVM library at 27,237 bytes and the Desktop `installDist` runtime jars at 24,328,424 bytes across 38 files on all three desktop runners. Warm job durations were 1m 7s on Ubuntu, 1m 21s on macOS, and 1m 45s on Windows; these include setup and are not isolated Gradle task timings.
- iOS CI linked the ARM64 device debug framework (65,486,938 bytes) and simulator debug framework (65,437,765 bytes). The simulator job ran the Kotlin/Native test suite and Swift consumer successfully on an iPhone 16 Pro, using Xcode 16.4 and iOS Simulator SDK 18.5. The framework-and-test job took 4m 20s. Run: `45724007519`.
- A JDK 21 Gradle invocation without `--no-build-cache` first hit an unreadable Kotlin incremental cache file. Re-running with `--no-build-cache` succeeded. This is recorded as an environment issue, not a source failure.
- One early AndroidX coordinate `androidx.test:core:1.6.2` did not resolve; it was corrected to the stable `1.7.0` release documented by Android.
- Warm local task times were about 24 seconds for the first successful JVM test build, 27 seconds for the desktop consumer/distribution, 35 seconds for the Android debug APK, and 17–18 seconds to compile Android instrumented and iOS simulator test sources. These are single Windows host measurements, not cross-OS benchmarks. Setup and implementation elapsed time was not tracked.

### Artifact sizes and runtime needs

Measured on the Windows host after building:

| Artifact | Size | Runtime |
|---|---:|---|
| `probe-jvm.jar` | 27,237 bytes | JVM; bytecode target 11 |
| `desktop-smoke.jar` | 4,456 bytes | JVM; app runtime dependencies are separate jars |
| Desktop `installDist` runtime jars | 24,328,424 bytes total across 38 jars | JRE 11+ is the current candidate floor; tested with JDK 21 |
| Android debug APK | 3,846,437 bytes | Android; debug APK, not a release package |
| iOS ARM64 device debug framework | 65,486,938 bytes (hosted CI) | iOS device; framework must be embedded by a native app |
| iOS ARM64 simulator debug framework | 65,437,765 bytes (hosted CI) | iOS simulator; Swift smoke app successfully embedded and ran it |

The Desktop `installDist` directory contains launch scripts and runtime dependency jars, but does not bundle a JRE. No desktop application packaging or JRE bundling strategy has been selected.

## Outstanding platform checks

- Android x86_64 emulator: load the APK and run instrumented checks as supplemental Android runtime evidence. The TCG retry is pending.
- Android ARM64 emulator/device: still required by the target matrix; obtain a compatible ARM64 Android-capable host and run deterministic, typed-error, Android/CIO engine, and Android SQLite persistence checks.
- Collect Android runtime and APK size results from the supplemental x86_64 CI run; ARM64 Android remains required.
- Exercise Ktor fixture serialization through each selected real engine with a local fixture server. Current offline JSON coverage uses Ktor MockEngine; target engines have been compiled, and some have been constructed, but have not made a fixture request.
- Record the supplemental Android CI outcome and continue pursuing the required Android ARM64 runtime check before recommending the language/driver/package combination.

## Dependencies and licences

Direct libraries introduced by the spike:

| Dependency | Version | Licence |
|---|---:|---|
| Kotlin / Kotlin Multiplatform Gradle plugin | 2.4.21 | Apache-2.0 |
| Android Gradle Plugin / Android-KMP library plugin | 9.3.1 | Apache-2.0 |
| Ktor client core, CIO, Android, Darwin, MockEngine, JSON negotiation | 3.6.0 | Apache-2.0 |
| SQLDelight runtime, Android, Native, SQLite drivers and Gradle plugin | 2.1.0 | Apache-2.0 |
| kotlinx.coroutines core and test | 1.11.0 | Apache-2.0 |
| kotlinx.serialization JSON and compiler plugin | 1.11.0 | Apache-2.0 |
| AndroidX Test runner | 1.7.0 | Apache-2.0 |
| AndroidX Test JUnit extension | 1.2.1 | Apache-2.0 |
| AndroidX Test core | 1.7.0 | Apache-2.0 |

The 36 third-party jars in the measured desktop runtime distribution resolve to the transitive set below. The license list covers the desktop runtime, but the full Android/iOS and Gradle build/test dependency inventory remains outstanding. The workflow uses `actions/checkout@v7` (MIT), `actions/setup-java@v6` (GPL-3.0 with project-provided additional permissions), and `reactivecircus/android-emulator-runner@v2` (Apache-2.0). Re-check versions and licences before finalizing the PR.

| Transitive runtime family | Resolved versions | Licence |
|---|---|---|
| JetBrains annotations and Kotlin stdlib | annotations 23.0.0; stdlib 2.4.21 | Apache-2.0 |
| Kaml YAML serialization | 0.79.0 | Apache-2.0 |
| kotlinx.coroutines core and SLF4J bridge | 1.11.0 | Apache-2.0 |
| kotlinx-datetime | 0.8.0-0.6.x-compat | Apache-2.0 |
| kotlinx-io bytestring and core | 0.9.1 | Apache-2.0 |
| kotlinx.serialization core, JSON, and JSON IO | 1.11.0 | Apache-2.0 |
| Ktor client, CIO, HTTP, network, serialization, SSE, and WebSocket modules | 3.6.0 | Apache-2.0 |
| Okio | 3.12.0 | Apache-2.0 |
| SQLDelight JDBC/runtime/SQLite driver | 2.1.0 | Apache-2.0 |
| SLF4J API | 2.0.19 | MIT |
| SnakeYAML Engine KMP | 3.1.1 | Apache-2.0 |
| Xerial SQLite JDBC, including bundled native SQLite libraries | 3.49.1.0 | Apache-2.0 |
| URL encoder | 1.6.0 | Apache-2.0 |

## Sources consulted

- Meridian M0.1 specification: [`docs/specs/M0_1_PLATFORM_SPIKE.md`](../specs/M0_1_PLATFORM_SPIKE.md).
- Kotlin Multiplatform compatibility and AGP 9 migration: [compatibility guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html), [AGP 9 migration](https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html), and [Android-KMP plugin setup](https://developer.android.com/kotlin/multiplatform/plugin).
- Ktor engine matrix: [Ktor client engines](https://ktor.io/docs/client-engines.html).
- SQLDelight drivers: [SQLDelight 2.1.0 multiplatform SQLite guide](https://sqldelight.github.io/sqldelight/2.1.0/multiplatform_sqlite/).
- Swift interop and error handling: [Kotlin/Native Objective-C and Swift interop](https://kotlinlang.org/docs/native-objc-interop.html).
- AndroidX Test versions: [AndroidX Test release notes](https://developer.android.com/jetpack/androidx/releases/test).
- GitHub runner OS/architecture labels: [GitHub-hosted runner reference](https://docs.github.com/en/actions/reference/runners/github-hosted-runners).
- Android emulator workflow: [android-emulator-runner README](https://github.com/ReactiveCircus/android-emulator-runner).
- CI action source and licence files: [checkout](https://github.com/actions/checkout), [setup-java](https://github.com/actions/setup-java), and [android-emulator-runner](https://github.com/ReactiveCircus/android-emulator-runner).
- Transitive runtime artifacts include Kotlinx IO, Kaml, URL encoder, SnakeYAML Engine, Okio, SLF4J, and Xerial SQLite JDBC. Their upstream repository or published POM license information was checked; SQLite JDBC bundles native SQLite libraries for multiple operating systems in one jar.

## Recommendation and decision gate

There is not enough five-platform runtime evidence for a stack recommendation yet. KMP/JVM is working on Windows; the source and test code compile for Android and the iOS simulator target. Do not start M1. Complete the missing real-host checks, compare the selected and alternate HTTP engines with a local fixture on real targets, measure native/JVM packaging, and obtain owner approval for the language, driver matrix, and runtime packaging approach.
