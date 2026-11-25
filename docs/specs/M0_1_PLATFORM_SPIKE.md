# M0.1 — Five-platform Kotlin Multiplatform proof

**Status:** proposed execution spec; no production weather-engine implementation is authorized by this document.  
**Parent plan:** [`/PLAN.md`](../../PLAN.md), sections 4–5, 11–12, and 16.  
**Purpose:** prove that one shared KMP engine can be consumed on Windows, macOS, iOS, Android, and Linux using the intended runtime strategy.

## 1. Question to answer

Can Meridian keep its domain and weather-engine logic in Kotlin Multiplatform, target Android and iOS directly, and support Windows/macOS/Linux desktop through the stable JVM target?

The recommended strategy is:
- Android: KMP Android target.
- iOS: Kotlin/Native framework linked into a native Swift/Objective-C app.
- Windows, macOS, Linux: one JVM desktop artifact, run with a supported JDK/JRE. A desktop app may bundle a runtime to avoid requiring the user to install Java.

This is OS compatibility, not a promise of a native `.dll`, `.dylib`, or `.so` for every desktop OS. Native Windows/Linux libraries are a separate future requirement.

## 2. Scope

Build only a minimal compatibility probe. It must not contain weather models, provider adapters, live HTTP requests, production database schema, caching, forecast algorithms, blending, or application UI.

The probe should expose from shared code:
- a version string;
- one deterministic input/output operation using simple strings and integers;
- one typed error case;
- one asynchronous operation;
- predictable lifecycle and cancellation behaviour.

Use the same observable behaviour on all five operating systems.

## 3. KMP targets to validate

| OS | Build target | Required runtime check |
|---|---|---|
| Android | Android/Kotlin target, ARM64 first | Load library from an Android test app on an ARM64 emulator/device |
| iOS | `iosArm64` and `iosSimulatorArm64` | Build framework; call from Swift in an ARM64 simulator; device check where available |
| Windows | Desktop `jvm()` | Run the same JVM artifact on Windows x64 with the chosen JDK |
| macOS | Desktop `jvm()` | Run the same JVM artifact on Apple Silicon macOS; Intel macOS if available |
| Linux | Desktop `jvm()` | Run the same JVM artifact on Linux x64 and record the tested distribution |

Do not use `mingwX64` or `linuxX64` Kotlin/Native as the initial Windows/Linux release path. They are optional only if the project later needs native desktop libraries independent of a JVM. An optional `macosArm64` framework check is useful only if a native Swift macOS consumer is part of the v1 requirement.

## 4. Shared-code and dependency validation

### Kotlin Multiplatform

1. Create a minimal Gradle KMP workspace with one shared `commonMain` source set.
2. Expose the probe through the public common API.
3. Add Android and iOS targets.
4. Add `jvm()` for desktop. The same JVM artifact must run on Windows, macOS, and Linux.
5. Test typed error mapping and async/cancellation semantics at the platform boundary.

### Networking candidate — Ktor

Build a small isolated test that:
- compiles common request/response code;
- uses an Android-supported client engine;
- uses the Darwin client engine for iOS;
- uses a JVM engine (for example CIO) for desktop;
- serializes/deserializes a local fixture without requiring live network access.

Do not make a live provider request. Record the exact client engine and dependency version for each target.

### Storage candidate — SQLDelight/SQLite

Build a small isolated schema and one read/write test that:
- compiles generated common Kotlin query APIs;
- uses the Android driver on Android;
- uses the Native driver on iOS;
- uses the JVM driver on desktop Windows/macOS/Linux.

The Windows/Linux desktop check must use the JVM driver in this strategy. Native Windows/Linux SQLDelight drivers are not part of the v1 desktop promise.

### Rust fallback (conditional)

Do not build a Rust/UniFFI comparator by default. Only add a small Rust proof if:
- the owner requires native Windows/macOS/Linux libraries with no JVM, or
- the KMP proof shows a concrete blocker that justifies comparing a native implementation.

If used, keep it limited to the same compatibility probe and record the extra FFI and packaging costs.

## 5. Runtime, packaging, and test checks

For every OS, record separately:
- target and architecture;
- build host, OS version, toolchain, Kotlin/Gradle versions;
- compile and link result;
- whether the artifact was loaded and executed;
- typed error mapping and async/cancellation result;
- runtime requirement (Android, iOS framework, JDK/JRE);
- package format and artifact size;
- steps required to bundle a JRE for desktop;
- known limitations and untested architectures.

Apple framework builds and iOS simulator tests require macOS/Xcode. A Windows build must actually run on a Windows runner, and Linux must run on a Linux environment; do not infer runtime compatibility from another host's cross-compilation.

## 6. Comparison and evidence

Record:
- setup and implementation time;
- build/test time for each OS;
- artifact sizes;
- dependency and licence footprint;
- platform-specific code required;
- Swift interoperability ergonomics;
- JVM installation or bundling impact on desktop users;
- maintenance cost of Kotlin/Native versus JVM desktop;
- whether any actual consumer requires a native desktop ABI.

Use a results table in the pull request. Distinguish measured facts from estimates.

## 7. Acceptance criteria

M0.1 is complete only when:

1. The same shared probe is called successfully on Android, iOS, Windows, macOS, and Linux.
2. The iOS framework works through a Swift consumer in an iOS simulator.
3. The JVM desktop artifact works on Windows, macOS, and Linux with the documented runtime.
4. Ktor client engine candidates compile for Android, iOS, and JVM desktop.
5. SQLDelight read/write tests pass with Android, Native iOS, and JVM desktop drivers.
6. The PR lists exact commands, toolchain versions, build hosts, platform outcomes, and artifact sizes.
7. Desktop runtime requirements and any JRE bundling plan are clearly documented.
8. The recommendation states whether KMP meets the five-OS requirement and whether any consumer actually needs native desktop libraries.
9. The project owner approves the production language, driver matrix, and packaging approach before M1 implementation begins.


## 9. Current compatibility research (2026-11-25)

This section records a starting point for the spike, not a permanent version lock. Re-check official compatibility tables immediately before implementation and pin the exact versions in the spike branch.

### Kotlin and Gradle baseline

- Kotlin Multiplatform stable documentation currently identifies Kotlin 2.4.21 as stable.
- The official compatibility guide lists Kotlin Gradle Plugin 2.4.20–2.4.21 with Gradle 7.6.3–9.7.0, Android Gradle Plugin 8.5.2–9.3.1, and Xcode 26.4.
- Start the spike with the latest stable Kotlin patch, a Gradle wrapper inside its fully supported range, an AGP version inside the listed range, and an installed Xcode version explicitly compatible with that Kotlin release. Do not use a Kotlin 2.5 release candidate for the baseline.
- Record the JDK required by the selected Gradle/AGP combination separately from the JVM bytecode target and the runtime Meridian consumers need. These are different compatibility decisions.

Official reference: [Kotlin Multiplatform compatibility guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html) and [Gradle project configuration](https://kotlinlang.org/docs/gradle-configure-project.html).

### Platform support is not uniform

Kotlin's target tiers are not equivalent. Current Kotlin/Native documentation lists iOS simulator ARM64 and Linux x64 among targets that can be tested in CI, while Windows MinGW x64 is Tier 3. Android and JVM support should be validated through their actual Android/JVM toolchains, not inferred from Kotlin/Native tiers.

This reinforces the initial choice to use JVM for Windows/macOS/Linux desktop instead of making Windows/Linux Kotlin/Native a release requirement. It does not prove that every third-party dependency or packaging approach works.

Official reference: [Kotlin/Native supported targets and hosts](https://kotlinlang.org/docs/native-target-support.html).

### Ktor engine matrix to test

Use one common Ktor request/response implementation and inject the engine at the platform boundary. The current Ktor engine matrix documents:
- Android: Android or CIO engine (compare the Android engine with CIO in the spike; select one based on tests and footprint).
- iOS: Darwin engine, backed by Apple's URL loading stack.
- JVM desktop: CIO as the initial lightweight candidate; compare Java engine only if a concrete requirement justifies it.

Ktor also documents CIO on JVM, Android, and Native targets. Do not make an engine choice from documentation alone: exercise fixture requests, cancellation, timeout, TLS defaults, and error mapping on the real targets. Keep live provider calls out of M0.1.

Official reference: [Ktor client engines](https://ktor.io/docs/client-engines.html).

### SQLDelight driver matrix to test

SQLDelight 2.4.1 (the latest stable release checked on 2026-11-25) documentation explicitly demonstrates:
- Android: `android-driver`.
- iOS/Kotlin Native: `native-driver`.
- Desktop JVM: `sqlite-driver` using JDBC.

Use a tiny schema with a create/insert/select/update transaction test and reopen the database to prove persistence. Test the packaged JVM artifact on each desktop OS; do not assume a driver that compiles on one host behaves identically on all three.

Official references: [SQLDelight 2.4.1 documentation](https://sqldelight.github.io/sqldelight/2.4.1/), [Native SQLite driver](https://sqldelight.github.io/sqldelight/2.4.1/native_sqlite/), [Android SQLite driver](https://sqldelight.github.io/sqldelight/2.4.1/android_sqlite/), and [JVM SQLite driver](https://sqldelight.github.io/sqldelight/2.4.1/jvm_sqlite/).

### CI execution matrix

Use separate jobs on real OS runners:
- `ubuntu-24.04`: Linux JVM artifact and shared/JVM tests.
- `windows-2025`: Windows JVM artifact and persistence tests.
- `macos-15` (Apple Silicon runner where available): iOS framework, Swift simulator consumer, and macOS JVM artifact.
- Android emulator/device tests should run on an Android-capable Linux runner; record emulator image and ABI.

GitHub's runner catalogue currently lists public-repository standard runners for Linux, Windows, and macOS, including Apple Silicon macOS runners. Pin runner labels rather than relying indefinitely on `-latest`. Hosted runner availability, Xcode image contents, and included minutes are operational constraints to record. A successful cross-compile is not a substitute for executing the artifact on the target OS.

Official references: [GitHub Actions runner selection](https://docs.github.com/en/actions/how-tos/write-workflows/choose-where-workflows-run/choose-the-runner-for-a-job) and [hosted runner images](https://docs.github.com/en/actions/reference/runners/github-hosted-runners).

## 10. Required evidence matrix

The implementation PR must report each cell as **PASS**, **FAIL**, **BLOCKED**, or **NOT TESTED**, with a log/artifact link and exact command. Do not mark a platform PASS from compilation alone.

| Check | Android | iOS | Windows | macOS | Linux |
|---|---|---|---|---|---|
| Shared probe compiles | | | | | |
| Artifact/framework produced | | | | | |
| Real consumer loads it | | | | | |
| Deterministic operation and typed error | | | | | |
| Async operation and cancellation | | | | | |
| Ktor fixture request | | | | | |
| SQLDelight create/write/read/reopen | | | | | |
| Packaged consumer launch | | | | | |

For iOS, distinguish framework compilation from successful Swift import and simulator execution. For Android, distinguish Gradle compilation from an instrumented test on the declared ABI. For desktop, test the actual packaged JVM artifact on each OS, not only on the build host.

## 11. Out of scope

Do not implement Open-Meteo, METAR, CAP, WMO, FPAS, weather models, source selection, blending, SQLite production schema, cache policy, forecast learning, radar, Solstice feature integration, or any publishable production API during this spike.


### Version refresh before the first build (2026-11-25)

The initial draft named SQLDelight 2.1.0 because that was the version in the first compatibility research. A final release check found SQLDelight 2.4.1, released 2026-10-06, as the current stable version. The implementation spike therefore pins 2.4.1 instead. This is a test candidate, not a production dependency approval. If the plugin or any driver fails with Kotlin 2.4.21 / AGP 9.3.1, record the exact failing target and compare the supported version combination before changing the stack.

Sources checked: SQLDelight's official 2.4.1 release notes and platform-specific Android, Native, and JVM SQLite driver documentation.
