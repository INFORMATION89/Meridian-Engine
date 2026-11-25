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

## 8. Out of scope

Do not implement Open-Meteo, METAR, CAP, WMO, FPAS, weather models, source selection, blending, SQLite production schema, cache policy, forecast learning, radar, Solstice feature integration, or any publishable production API during this spike.
