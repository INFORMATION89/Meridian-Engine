# MER-0 — Repository foundation checklist

**Status:** partially complete; README and ignore rules are now present  
**Master plan:** [`/PLAN.md`](../../PLAN.md)  
**Agent instructions:** [`/AGENTS.md`](../../AGENTS.md)  
**Current status:** documentation and repository identity exist; no production engine code has been authorized.

## 1. Repository and governance

- [x] GitHub repository `INFORMATION89/Meridian-Engine` exists; default branch is `main`.
- [x] Product name is Meridian; repository name is Meridian-Engine.
- [x] Root `RULES.md` exists.
- [x] Root `CLEAN_ROOM.md` exists as a separate authoritative policy.
- [x] Root `AI_POLICY.md` governs AI-assisted engineering and AI-enabled product features.
- [ ] Review and approve the root `PLAN.md`.
- [x] Add the Apache-2.0 `LICENSE` text.
- [ ] Add `NOTICE` only after confirming the copyright holder and completing the third-party attribution inventory; do not invent notices.
- [x] Add a project `README.md` that says the project is early and not yet usable.
- [x] Add a cross-platform `.gitignore` for IDE files, build outputs, local configuration, native artifacts, and secrets.
- [ ] Configure branch protection for `main` after the first CI build is green.

## 2. Documentation home

- [x] `docs/ENGINE_SPEC.md` copied as the functional weather-engine specification.
- [x] `docs/MERIDIAN_PLAN.md` copied as supporting research; its older dependency and Room proposals are supporting research, not adopted decisions.
- [x] MER-1 forecast specification copied to `docs/specs/MER-1_FORECAST.md`.
- [x] MER-2 provenance specification copied to `docs/specs/MER-2_PROVENANCE.md`.
- [x] MER-3 alerts specification copied to `docs/specs/MER-3_ALERTS.md`.
- [x] MER-4 observations specification copied to `docs/specs/MER-4_OBSERVATIONS.md`.
- [x] MER-5 blending specification copied to `docs/specs/MER-5_BLEND.md`.
- [x] MER-7 learning specification copied to `docs/specs/MER-7_LEARNING.md`.
- [x] MER-6 consumer-app integration remains in Solstice; it does not belong in the engine's implementation specification set.
- [x] MER-0.1 platform proof specification is maintained in `docs/specs/MER-0_1_PLATFORM_SPIKE.md`.
- [x] MER-0.3 reproducible verification and research specification is maintained in `docs/specs/MER-0_3_VERIFICATION_RESEARCH.md`.

## 3. Technology gate — MER-0.1

- [ ] Build the minimal KMP proof of concept with Android, iOS framework, and Desktop JVM targets.
- [ ] Run the same probe on Android and iOS, and through JVM desktop on Windows, macOS, and Linux.
- [ ] Validate Ktor HTTP-client and SQLDelight SQLite driver choices on the required source sets.
- [ ] Record JDK/runtime requirements, artifact sizes, toolchains, build hosts, packaging constraints, and test outcomes.
- [ ] Build a Rust/UniFFI comparator only if native desktop libraries without a JVM are required or the KMP proof exposes a blocker.
- [ ] Present a written recommendation and obtain owner approval before MER-1.

## 4. Public API and local storage — MER-0.2

- [ ] Approve the consumer-neutral public request/result API and batch-first approach.
- [ ] Define optional cache and forecast/observation history interfaces, including retention, deletion, versioning, and atomicity.
- [ ] Define offline, freshness, partial-failure, typed-error, and cancellation semantics.
- [ ] Demonstrate the API through an independent fixture-backed sample consumer with no Solstice dependency.
- [ ] Validate Android, iOS/Swift, and JVM desktop interop against MER-0.1 evidence.
- [ ] Obtain owner approval for API stability, storage boundary, and error model before MER-1.

## 5. Reproducible verification and research — MER-0.3

- [ ] Create a standalone sample consumer that builds and queries Meridian without Solstice.
- [ ] Publish versioned machine-readable weather, error, freshness, and provenance schemas.
- [ ] Define bounded read-only weather query tools for forecasts, observations, alerts, sources, and provenance.
- [ ] Define minimized AI context and a human-reviewed evaluation set.
- [ ] Test indirect prompt injection from provider/alert fixtures and verify read-only tool permissions.
- [ ] Decide whether an optional MCP adapter is justified; do not add MCP to the core.
- [ ] Confirm that all deterministic weather features work without an LLM, provider credential, or AI service.

## 6. CI and release infrastructure

- [ ] Add CI for Gradle/Kotlin formatting, tests, and offline fixtures after the workspace is approved.
- [ ] Add Linux x64 and ARM64 build jobs as appropriate.
- [ ] Add Windows JVM desktop build and runtime test job.
- [ ] Add macOS jobs for macOS and iOS SDK targets.
- [ ] Add Android build and emulator smoke tests.
- [ ] Publish the actual supported-platform matrix in the README; distinguish supported, build-only, and experimental.
- [ ] Confirm how native binaries and generated language bindings will be versioned and packaged.

## 7. Production-code boundary

No production weather adapter, forecast algorithm, database implementation, blend, learning model, radar processor, or consumer-app integration belongs to MER-0/MER-0.1. Those begin only after the relevant specification is reviewed and the technology gate is approved.

## Done when

- The owner has approved `PLAN.md` and the language/interop decision from MER-0.1.
- The repository has its license, README, ignore rules, and appropriate CI foundations.
- The project can state exactly which platform packages it can build, load, and test, and which runtime each requires.
- No production weather-engine implementation has been smuggled into the bootstrap milestone.
