# M0.1 — Platform and language interoperability spike

**Status:** proposed execution spec; no production engine implementation is authorized by this document.  
**Parent plan:** [`/PLAN.md`](../../PLAN.md), sections 4–5, 11–12, and 16.  
**Purpose:** decide the engine implementation language and native binding strategy using build/run evidence for all five required operating systems.

## 1. Question to answer

Can Meridian ship one portable native engine to Windows, macOS, iOS, Android, and Linux, with a maintainable public API for Kotlin and Swift consumers?

The recommended candidate is a Rust library with UniFFI-generated Kotlin and Swift bindings. The comparator is a minimal Kotlin Multiplatform library using only Kotlin standard-library features. Do not decide from marketing claims or a successful compile alone.

## 2. Scope

Build only a minimal compatibility probe. It must not contain weather data models, provider adapters, HTTP requests, persistence, caching, algorithms, or application UI.

The probe should expose:
- a version string;
- one deterministic input/output operation using simple strings and integers;
- one typed error case;
- one asynchronous operation that can be cancelled or cleanly completed;
- explicit create/use/release or equivalent lifecycle behaviour.

Use the same observable behaviour for each candidate implementation.

## 3. Candidate A — Rust plus UniFFI

1. Create a minimal Cargo workspace or single crate sufficient for the probe.
2. Build a native library and define a small UniFFI interface (proc-macro or UDL; compare only if there is a concrete blocker).
3. Generate Kotlin bindings and compile a small Kotlin consumer.
4. Generate Swift bindings and compile a small Swift consumer.
5. Verify the Kotlin native-loading path and its JNA/coroutines dependencies. Verify that the same generated Kotlin API can be used on Android and a desktop JVM target without special-case business logic.
6. Verify the Swift binding on macOS and iOS. Test the generated error and async/cancellation mappings.
7. Record how each native binary is packaged alongside the generated language binding. UniFFI generates bindings; it does not replace building and shipping platform-native binaries.

Do not hand-edit generated bindings. Pin the Rust toolchain, UniFFI version, and binding dependencies used by the spike.

## 4. Candidate B — Kotlin Multiplatform comparator

Build the same probe using Kotlin Multiplatform and standard-library-only common code.

Attempt the targets relevant to the requirements:
- Android/JVM;
- iOS ARM64 device and ARM64 simulator;
- macOS ARM64;
- Linux x64;
- Windows x64 using the currently supported Kotlin/Native target.

Record compiler support tier, required host, whether tests execute, output artifact, and any limitation. Do not add Room, Ktor, or other Jetpack/provider libraries to this comparator; the point is to separate compiler capability from dependency compatibility.

## 5. Required target matrix

| OS | Target to attempt | Minimum evidence |
|---|---|---|
| Windows | x86_64 MSVC | Build DLL/library, load it from the intended consumer binding, call probe |
| macOS | ARM64 | Build and load native artifact, call Swift binding; Kotlin/JVM loading where proposed |
| iOS | ARM64 device + ARM64 simulator | Build with Xcode SDK, link generated Swift binding, run simulator/device smoke test where available |
| Android | ARM64 device + x86_64 emulator | Build native library with Android NDK, package with Kotlin bindings, call from emulator/device |
| Linux | x86_64 glibc | Build shared library, load through intended desktop binding, call probe |

Additional targets (Windows ARM64, Linux ARM64, macOS Intel, musl, x86 simulators) are useful but not allowed to delay the baseline decision. Record them as follow-up or experimental rather than claiming them supported.

Apple artifacts must use the official Apple SDK/Xcode toolchain on macOS. Windows MSVC artifacts must use an appropriate Windows runner/toolchain. Report cross-compilation constraints honestly.

## 6. Runtime and packaging checks

For each platform/consumer combination, separately record:

- build and link success;
- actual library loading;
- successful probe invocation;
- typed error mapping;
- async completion and cancellation behaviour;
- native library naming and lookup/loading behaviour;
- dependency on JNA, Kotlin coroutines, Swift runtime, system libraries, or other components;
- artifact size and packaging output;
- minimum OS version tested;
- any manual build or signing step.

For iOS, a successful archive build is not enough: compile and run the probe in an iOS simulator. A physical device run is desirable when available. For Android, use an emulator or device; do not treat an NDK build alone as runtime proof.

## 7. Measurements and comparison

Record for both candidates:

- setup and implementation time;
- build time per target;
- test execution time;
- artifact size per target;
- binding generation and packaging complexity;
- number of platform-specific workarounds;
- third-party dependency and licence footprint;
- maintainability and upgrade risks;
- ability to integrate with Solstice (Kotlin), a native Swift consumer, and desktop consumers.

Use a results table in the pull request. Distinguish measured facts from estimates.

## 8. Acceptance criteria

M0.1 is complete only when:

1. Both candidates have a documented comparison; if a candidate cannot be built, include the exact error and reason.
2. Rust target artifacts are compiled for the five baseline OS targets in section 5, or any unavailable target is explained with a concrete host/toolchain blocker.
3. Kotlin and Swift bindings are generated from the same Rust probe.
4. At least one Kotlin Android consumer and one Swift iOS/macOS consumer actually load and call the Rust library.
5. At least one Windows and one Linux consumer actually load and call a native library (Kotlin/JVM or C binding, as tested).
6. The artifact formats, native-loading mechanism, and packaging strategy are documented.
7. The PR contains exact commands, toolchain versions, build-host details, target outcomes, and artifact sizes.
8. A recommendation states whether Rust is viable, whether KMP is viable, and which trade-offs remain.
9. The project owner explicitly approves the production language and binding approach before M1 implementation begins.

## 9. Sources to consult

Use the official references linked in [PLAN.md](../../PLAN.md), plus the current official documentation for Rust, Cargo, UniFFI, Kotlin Multiplatform, Xcode, Android NDK, JNA, and the selected build runners. Re-check target tiers and binding requirements at execution time.

## 10. Explicitly out of scope

Do not implement Open-Meteo, METAR, CAP, WMO, FPAS, weather models, source selection, blending, SQLite, Room, cache policy, forecast learning, radar, Solstice integration, or a publishable production API during this spike.
