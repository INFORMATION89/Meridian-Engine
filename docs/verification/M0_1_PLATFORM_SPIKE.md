# M0.1 platform spike — initial execution report

**Status: in progress.** This report records the Windows-hosted evidence collected so far. It is not a production stack recommendation or an approval to start M1.

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
| Windows / Desktop JVM / x64 | PASS: JVM library and consumer distribution built | PASS: packaged consumer launched on Windows with JDK 21 | PASS: common JSON fixture test; CIO instantiated | PASS: SQLite file write, close, reopen, read | Experimental; one local host, not CI |
| Android / Android-KMP / ARM64 | PASS: Android app/APK and instrumented tests compile | BLOCKED locally: emulator is x86_64 and Windows has no Android Emulator hypervisor driver | PASS: Android and CIO candidates compile; actual engine request not run | PASS: Android driver test compiles; device execution pending | Build-only |
| iOS / `iosSimulatorArm64` / ARM64 simulator | PASS: shared and simulator-test Kotlin code compile on Windows | NOT TESTED: framework link and Swift app require macOS/Xcode | PASS: Darwin candidate compiles; simulator execution pending | PASS: Native driver test compiles; simulator execution pending | Build-only |
| macOS / Desktop JVM / Apple Silicon | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | Not supported yet |
| Linux / Desktop JVM / x64 | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | Not supported yet |

**No platform is marked Supported.** The project definition reserves that label for a build and real-runtime check in CI. Android x86_64 emulator execution, iOS simulator execution, macOS JVM execution, and Linux JVM execution remain outstanding.

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
- Android library and smoke app: PASS compile/package. Instrumented test sources: PASS compile. The installed AVD is x86_64, and emulator startup reported that hardware acceleration is required but no emulator hypervisor driver is installed; device tests did not run.
- iOS simulator Kotlin source and test source: PASS cross-compilation. No Apple framework was linked and no Swift code was run on this Windows host.
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
| iOS device and simulator frameworks | Not produced | Requires macOS/Xcode and iOS SDKs |

The Desktop `installDist` directory contains launch scripts and runtime dependency jars, but does not bundle a JRE. No desktop application packaging or JRE bundling strategy has been selected.

## Outstanding platform checks

- Android ARM64 emulator/device: load the APK; run deterministic, typed-error, Android/CIO engine, and Android SQLite persistence checks.
- iOS ARM64 simulator: link framework; build and launch the Swift consumer; run the async/cancellation and Native SQLite tests; record the framework size.
- macOS Apple Silicon and Linux x64: run the packaged JVM consumer and record OS version, JDK, test result, and distribution size.
- Exercise Ktor fixture serialization through each selected real engine with a local fixture server. Current offline JSON coverage uses Ktor MockEngine; target engines have been compiled, and some have been constructed, but have not made a fixture request.
- Obtain CI logs and record measured execution time and sizes per OS before recommending the language/driver/package combination.

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
