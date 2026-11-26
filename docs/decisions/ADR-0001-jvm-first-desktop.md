# ADR-0001: JVM-first desktop distribution

- **Status:** Accepted
- **Decision date:** 2026-11-26
- **Scope:** Initial desktop distribution direction for Windows, macOS, and Linux
- **Authority:** Owner decision

## Context

Meridian targets Windows, macOS, Linux, Android, and iOS and must remain an independent engine usable by applications other than Solstice. M0.1 has exercised a desktop JVM consumer on Windows, macOS, and Linux. A native desktop library strategy would add platform ABI, packaging, FFI, and maintenance work before a concrete consumer requirement has demonstrated that this is necessary.

## Decision

Use the JVM desktop artifact as the initial desktop integration and distribution path on Windows, macOS, and Linux.

- Publish and test the desktop JVM artifact as part of the platform release matrix.
- Document the required Java runtime version and supported operating systems/architectures.
- For applications that need a self-contained end-user installation, the consuming application may bundle a compatible, preferably minimized Java runtime with its own installer.
- Meridian's generic library artifact does not bundle a runtime into every consumer application and does not require the end user to install Java if the consuming application bundles it.
- Keep the engine's public contracts consumer-neutral; do not expose JVM-specific types in the domain contract.

## Consequences

### Benefits

- Reuses the validated JVM desktop path and shared Kotlin code candidate.
- Reduces initial platform-specific packaging, FFI, and maintenance costs.
- Allows desktop app developers to ship a self-contained application by bundling the runtime.
- Keeps effort focused on meteorological correctness, cross-platform reliability, and stable consumer APIs.

### Costs and limitations

- A JVM artifact is not a native `.dll`, `.dylib`, or `.so` and cannot be loaded directly by arbitrary native consumers.
- Runtime bundling increases an application's distribution size and requires runtime/security update policy.
- Some consumers will prefer native ABI integration or may not be able to host a JVM.

## Explicitly deferred

Native desktop libraries, C ABI, Rust bindings, and direct integration from native C/C++/Rust/Swift desktop applications are not M0.1 requirements. Revisit them only when named consumer requirements, measured runtime/packaging constraints, or interoperability evidence justify the additional cost.

## What this decision does not approve

This ADR does not, by itself, select the final production language, approve Kotlin Multiplatform as the complete production stack, finalize storage/networking dependencies, define the public API, or authorize production weather-provider implementation. Those remain subject to the relevant M0.1/M0.2 evidence and approval gates.

## Revisit triggers

Reconsider this decision if:
- a target consumer cannot host a JVM and requires in-process native integration;
- measured startup, memory, package-size, or runtime-maintenance costs violate an agreed product budget;
- a stable native ABI becomes a strategic integration requirement;
- the native approach materially improves maintainability or total cost across the actual consumer matrix.
