# M0 — Repository foundation checklist

**Status:** repository/legal bootstrap and MER-0.1 candidate platform proof are substantially complete; production-stack approval remains open.  
**Status snapshot:** 2026-11-26  
**Master plan:** [`PLAN.md`](../../PLAN.md)  
**Agent instructions:** [`AGENTS.md`](../../AGENTS.md)

M0.1 evidence was merged to `main` through PR #15 on 2026-11-26. It is a compatibility and packaging probe, not a production weather engine or approval to begin M1. Root legal/project files and CI are present. Mobile release-notice packaging and owner approval of the complete production direction remain open.

## 1. Repository and governance

- [x] GitHub repository `INFORMATION89/Meridian-Engine` exists; default branch is `main`.
- [x] Product name is Meridian; repository name is Meridian-Engine.
- [x] Root `RULES.md`, `CLEAN_ROOM.md`, `AI_POLICY.md`, and `AGENTS.md` exist.
- [x] Root `PLAN.md` is approved and merged.
- [x] Root Apache-2.0 `LICENSE` is present on `main`.
- [x] Root public `README.md` identifies the project as experimental and not production-ready.
- [x] Cross-platform `.gitignore` is present.
- [x] Root `THIRD-PARTY-NOTICES.md` and the MER-0.1 probe dependency/licence inventory are present.
- [ ] Verify branch protection and required CI status rules for `main`; record the actual ruleset and check name.

## 2. Documentation and responsibility boundaries

- [x] `docs/ENGINE_SPEC.md` is retained as legacy weather-behavior research, subordinate to the root master plan and approved milestone specifications.
- [x] `docs/MERIDIAN_PLAN.md` is retained as a historical research snapshot; its older language, dependency, Room, API, and module proposals are not adopted decisions.
- [x] M1 forecast, M2 provenance, M3 alerts, M4 observations, M5 blending, and M7 learning specifications are available as scoped research/proposals, not blanket authorization to implement.
- [x] Consumer-specific feature adoption and user-facing recommendation logic belong in each consuming application's repository; Meridian does not plan or own an app's migration sequence.
- [x] M0.1 platform proof and M0.3 reproducible verification/research specifications are maintained in their scoped documents.

## 3. MER-0.1 platform and packaging evidence

- [x] Build the bounded multiplatform probe for Android, iOS framework/Swift, and Desktop JVM targets.
- [x] Run Android x86_64 emulator instrumentation tests (8/8).
- [x] Run iOS simulator/common tests (9/9) and native Swift consumer tests (10/10).
- [x] Build and launch Desktop JVM consumers on Windows, macOS, and Linux; macOS common/storage tests passed (11/11).
- [x] Exercise real Android, Darwin, and CIO fixture HTTP requests, error mapping, timeouts, cancellation, and client cleanup.
- [x] Validate Android, iOS Native, and desktop JVM SQLDelight candidate drivers as probe evidence.
- [x] Record JDK/runtime requirements, artifact sizes, package limits, build hosts, toolchains, and test outcomes.
- [x] Complete the MER-0.1 CycloneDX and dependency/licence review (402 coordinates; see the versioned evidence report).
- [x] Merge the MER-0.1 evidence package through PR #15; exact final PR-head CI #224 passed 5/5 jobs.
- [ ] Obtain owner review and approval of the complete production-stack recommendation before M1.

**Explicitly not tested in M0.1:** Android ARM64 runtime, physical Android/iOS devices, and native desktop ABIs. These cells remain NOT TESTED unless a separately approved requirement adds them.

## 4. M0.2 — Public API, lifecycle, and storage contracts

- [ ] Approve the consumer-neutral public request/result API and batch-first approach.
- [ ] Define optional cache and forecast/observation history interfaces, including retention, deletion, versioning, and atomicity.
- [ ] Define offline, freshness, partial-failure, typed-error, timeout, and cancellation semantics.
- [ ] Define HTTP client ownership, reuse, resource lifecycle, provider-specific limits, and disposal.
- [ ] Demonstrate the contract through an independent fixture-backed sample consumer without a Solstice dependency.
- [ ] Validate Android, iOS/Swift, and JVM desktop interoperability against the approved API.
- [ ] Obtain owner approval for API stability, storage boundary, and error model before M1.

## 5. M0.3 — Reproducible verification and research

- [ ] Create an independent sample consumer that builds and queries Meridian without requiring any particular app.
- [ ] Publish versioned machine-readable weather, error, freshness, and provenance schemas.
- [ ] Define bounded read-only weather query tools for forecasts, observations, alerts, sources, and provenance.
- [ ] Define minimized AI context and a human-reviewed evaluation set.
- [ ] Test indirect prompt injection from provider/alert fixtures and verify read-only tool permissions.
- [ ] Decide whether an optional MCP adapter is justified; do not add MCP to the core.
- [ ] Confirm deterministic weather features work without an LLM, provider credential, or AI service.

## 6. CI, release, and distribution

- [x] CI covers Gradle/Kotlin formatting, tests, Android emulator smoke tests, Apple/iOS simulator and Swift tests, Windows/Linux/macOS JVM consumers, and the dependency/licence audit.
- [x] Linux x64, Windows JVM, macOS/iOS, and Android x86_64 CI paths are represented in the current matrix.
- [x] README distinguishes experimental status and the tested platform/architecture matrix.
- [ ] Keep the workflow, support matrix, and dependency audit current when the production code or dependency set changes.
- [ ] Stage and verify root `LICENSE` and `THIRD-PARTY-NOTICES.md` alongside the iOS XCFramework and in Android AAR/APK/AAB packages before public mobile distribution.
- [ ] Confirm release artifact formats, generated language-binding versioning, signatures/attestations, and compatibility policy in the relevant approved milestone.

## 7. Production-code boundary

The merged M0.1 package is test/probe code only. It does not authorize production weather providers, forecast algorithms, persistent production storage, model blending, local learning, alert processing, radar/nowcasting, hosted services, or consumer application features. Those require approved specifications, reproducible evidence, and the corresponding owner gates.

## Done when

- The owner has explicitly approved the production technology direction and the M0.2 public API/storage/lifecycle contracts.
- Legal/project files and CI foundations are maintained on `main`.
- The project can state exactly which platform packages it can build, load, and test, and which runtime each requires.
- Release packages include required project and third-party notices.
- No production weather feature is implemented before its approved milestone and acceptance criteria.
