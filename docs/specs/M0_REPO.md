# M0 — Repository foundation checklist

**Status:** partially complete  
**Master plan:** [`/PLAN.md`](../../PLAN.md)  
**Agent instructions:** [`/AGENTS.md`](../../AGENTS.md)  
**Current status:** documentation and repository identity exist; no production engine code has been authorized.

## 1. Repository and governance

- [x] GitHub repository `INFORMATION89/Meridian-Engine` exists; default branch is `main`.
- [x] Product name is Meridian; repository name is Meridian-Engine.
- [x] Root `RULES.md` exists.
- [x] Root `CLEAN_ROOM.md` exists as a separate authoritative policy.
- [ ] Review and approve the root `PLAN.md`.
- [ ] Add Apache-2.0 `LICENSE` and `NOTICE`.
- [ ] Add a project `README.md` that says the project is early and not yet usable.
- [ ] Add a cross-platform `.gitignore` for IDE files, build outputs, local configuration, native artifacts, and secrets.
- [ ] Configure branch protection for `main` after the first CI build is green.

## 2. Documentation home

- [x] `docs/ENGINE_SPEC.md` copied as the functional weather-engine specification.
- [x] `docs/MERIDIAN_PLAN.md` copied as supporting research; its Kotlin/Gradle/Room proposal is explicitly marked historical and non-authoritative.
- [x] M1 forecast specification copied to `docs/specs/M1_FORECAST.md`.
- [x] M2 provenance specification copied to `docs/specs/M2_PROVENANCE.md`.
- [x] M3 alerts specification copied to `docs/specs/M3_ALERTS.md`.
- [x] M4 observations specification copied to `docs/specs/M4_OBSERVATIONS.md`.
- [x] M5 blending specification copied to `docs/specs/M5_BLEND.md`.
- [x] M7 learning specification copied to `docs/specs/M7_LEARNING.md`.
- [x] M6 consumer-app integration remains in Solstice; it does not belong in the engine's implementation specification set.
- [x] M0.1 platform/interoperability spike specification is maintained in `docs/specs/M0_1_PLATFORM_SPIKE.md`.

## 3. Technology gate — M0.1

- [ ] Build the minimal Rust + UniFFI proof of concept.
- [ ] Generate and call Kotlin bindings.
- [ ] Generate and call Swift bindings.
- [ ] Build the agreed Windows, macOS, iOS, Android, and Linux target artifacts.
- [ ] Run smoke tests through real bindings on available native runners/simulators.
- [ ] Build a small Kotlin Multiplatform comparator using the same platform-neutral function.
- [ ] Record artifact sizes, toolchains, build hosts, packaging constraints, and test outcomes.
- [ ] Present a written recommendation and obtain owner approval before choosing the production engine language.

## 4. CI and release infrastructure

- [ ] Add CI for Rust formatting, linting, unit tests, and offline fixture tests after the workspace is approved.
- [ ] Add Linux x64 and ARM64 build jobs as appropriate.
- [ ] Add Windows MSVC build and runtime test job.
- [ ] Add macOS jobs for macOS and iOS SDK targets.
- [ ] Add Android NDK jobs and emulator smoke tests.
- [ ] Publish the actual supported-platform matrix in the README; distinguish supported, build-only, and experimental.
- [ ] Confirm how native binaries and generated language bindings will be versioned and packaged.

## 5. Production-code boundary

No production weather adapter, forecast algorithm, database implementation, blend, learning model, radar processor, or consumer-app integration belongs to M0/M0.1. Those begin only after the relevant specification is reviewed and the technology gate is approved.

## Done when

- The owner has approved `PLAN.md` and the language/interop decision from M0.1.
- The repository has its license, README, ignore rules, and appropriate CI foundations.
- The project can state exactly which native target artifacts and language bindings it can build, load, and test.
- No production weather-engine implementation has been smuggled into the bootstrap milestone.
