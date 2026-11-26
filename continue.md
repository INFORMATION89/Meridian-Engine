# Meridian — continuation state

**Recorded:** 2026-11-25  
**Repository:** [INFORMATION89/Meridian-Engine](https://github.com/INFORMATION89/Meridian-Engine)  
**Branch:** `spike/m0.1-kmp-platform-proof`  
**Last full-matrix workflow-tested commit:** `28817f6a09c0099ac340ff4df55ea5377ec5ed0a`  
**PR:** [#2](https://github.com/INFORMATION89/Meridian-Engine/pull/2) — **OPEN · DRAFT · NOT MERGED**  
**Current phase:** **MER-0.1 stabilization**. MER-0.1 is not complete; MER-1 has not started.

This continuation record was added because no `continue.md` or other continuation-named file existed on the PR branch. The verified-results record remains at [docs/verification/MER-0_1_RESULTS.md](docs/verification/MER-0_1_RESULTS.md).

## Owner decision — CI-only validation for now (2026-11-25)

A passing supported CI matrix is sufficient for current MER-0.1 validation. Physical-device testing is deferred because not all target platforms/devices are currently available to the owner. No manual device runs are required, and this is not an MER-0.1 blocker.

The current evidence covers Android x86_64 emulator, iOS ARM64 Simulator/Swift consumer, and Desktop JVM on Windows/macOS/Linux. Android ARM64 runtime and physical Android/iOS execution remain **NOT TESTED**; keep those limits explicit and do not imply that they passed. Any additional Ktor engine integration checks are CI work using deterministic local fixtures.

## Current CI state

**Latest inspected run:** [Run #152](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45717462136)  
**Tested commit:** 28817f6a09c0099ac340ff4df55ea5377ec5ed0a  
**Result:** all six jobs completed successfully.

| Job | Result | Evidence |
|---|---|---|
| dependency-metadata | PASS | Dependency graphs resolved and metadata files verified |
| dependency-metadata-linux | PASS | Linux-specific Android tool checksums added; Android build passed in normal verification mode |
| apple-and-ios | PASS | iOS frameworks, simulator tests, macOS JVM consumer, packaged app image, and Swift Package consumer tests |
| desktop-linux | PASS | Desktop JVM tests/consumer and self-contained app image launched |
| desktop-windows | PASS | Desktop JVM tests/consumer and self-contained app image launched |
| android | PASS | Host-side tests and Android 35 x86_64 emulator instrumentation |

Run #152 confirms that the combined dependency metadata and cross-platform workflow work together on the inspected commit. It does not close the platform and Ktor gaps below.

Historical repair runs:
- [#145](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45712328566) failed before SDK setup because checkout treated a commit SHA as a branch ref.
- [#146](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45713934656) failed because Bash passed a literal backslash as an extra Gradle task.
- [#147](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45714161690) generated the combined metadata but its metadata push lost a branch race; no force-push was attempted.
- [#152](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45717462136) completed successfully after those repairs.

The workflow-tested commit is 28817f6a09c0099ac340ff4df55ea5377ec5ed0a; later commits on this branch may update documentation only. Re-check the PR head and workflow if implementation or workflow code changes.

## Audit A–D

### A — CI reproducibility and dependency integrity

**The combined metadata path passed in Run #152.** GitHub Actions are pinned to immutable SHAs; Gradle dependency locking and SHA-256 verification metadata are committed; the Gradle wrapper distribution has checksum verification. The current metadata snapshot records 486 components / 938 artifacts / 938 SHA-256 entries. The Linux-specific metadata job added the missing Linux Android tool checksums and then passed the Android build in normal dependency-verification mode.

The earlier #145–#147 failures are historical repair steps and are superseded by the successful combined run #152. Initial checksum generation remains trust-on-first-use and does not independently authenticate publishers.

Relevant commits:
- 85c73bb6a8c428c650675be4c89d5843f225bcc7 — pin GitHub Actions.
- 3997af0212959e6d52c188cef248a09324a58cf3 — enable dependency locking.
- 02c4a0895c49418e2d914f0439f966f4d76cea2b — generate lock and verification metadata.
- 422e4eb31f0d1993e6d91997009e5f6aea851140 — verify Gradle distribution checksum.
- f252b551d86b85828810883f57506465df26195f — lock resolved dependencies and verify checksums.

### B — Persistence and platform tests

Run #152 passed the Android x86_64 instrumentation, iOS simulator/Swift consumer, and desktop JVM workflows. The platform SQLDelight probes exercise transaction and close/reopen behaviour. The iOS probe isolates and cleans its named database before/after execution, including failure paths: ac8ebf0ceb0c8f640a3da56eb40846b7a433dfed.

These results establish the tested probe/consumer/storage paths on the declared CI targets. They do not establish Android ARM64 runtime support, physical Android/iOS-device execution, or real HTTP requests through each Ktor platform engine. Under the owner's decision, the device-level gaps are deferred and are not current MER-0.1 blockers; any additional Ktor transport coverage belongs in CI.

### C — Desktop distribution

Run #152 successfully launched self-contained jpackage app-images on all three desktop runners, after removing the runner Java environment. The measured app-image sizes were approximately:
- Windows: 169.7 MiB.
- Linux: 183 MB.
- macOS: 184 MB.

These figures describe the test app-images, not a final release package or a native desktop library. This remains a JVM distribution strategy; no native .dll, .dylib, or .so is promised.

### D — Repository foundations and decisions

The experimental README and Apache-2.0 LICENSE are present; MER-0.2 API/storage and MER-0.3 verification proposals are linked. The third-party dependency/licence inventory and decision on whether a project `NOTICE` is needed remain open; do not create a NOTICE without confirming attribution/copyright requirements. The toolchain, driver matrix, packaging approach, and proposals remain owner-review items.

Relevant commits: `6e89b913c4939759dd4ed4701e461558345e8885` (experimental README), `f747843485e25d9f189735de5906a40c673db113` (Apache-2.0 license), `3edd52db038535ec8246958e26a4efa1ff4972c8` (MER-0.2 proposal), `a83518ada3f65e40894da4a0c3c27616631163d3` (MER-0.3 proposal).

## Ktor transport smoke test

- Current tested spike candidate remains **Ktor 3.6.1**. Ktor **3.6.2 has not been adopted or validated**; reconsider it only after the current dependency metadata and platform matrix stabilize.
- MER-0.1 requires one deterministic local-fixture HTTP request through each selected real engine: Android, Darwin, and JVM/CIO. The CI implementation now adds those smoke tests; their status must be read from the exact final workflow SHA.
- The common `MockEngine` fixture test remains useful for shared request/response logic, but it does not replace the platform-engine smoke tests.
- Timeout policy, detailed transport-error mapping, and consumer-boundary cancellation are not additional MER-0.1 acceptance gates. Define and test those behaviours with the MER-0.2 public API/error/cancellation contract. Do not add a larger transport test suite without a concrete blocker and owner approval.
- No-KVM TCG fallback is optional CI-resilience work and remains deferred. A KVM-enabled success does not establish TCG fallback behavior.

## Android ARM64 and physical-device coverage

**NOT TESTED.** Current Android CI uses an Android 35 x86_64 emulator on a Linux x86_64 runner. Physical Android/iOS device execution is also **NOT TESTED**; iOS coverage is the ARM64 Simulator and Swift consumer. The owner accepts the passing supported CI matrix for current MER-0.1 and explicitly defers physical-device/Android ARM64 testing. Do not infer ARM64 runtime support from x86_64 or claim physical-device execution. Revisit only when hardware is available or a concrete release/consumer requirement makes it necessary.

## Gates before MER-1

1. Keep the CI evidence tied to the exact tested commit and review the M0.1 language/target, driver matrix, and packaging recommendation. The owner has accepted CI-only validation and the deferral of physical-device/Android ARM64 testing; those paths remain NOT TESTED, not blockers for the current milestone.
2. Verify the single deterministic local-fixture request through Android, Darwin, and JVM/CIO engines in CI. Keep timeout policy, transport-error mapping, and consumer-boundary cancellation for the MER-0.2 contract unless a concrete blocker justifies additional MER-0.1 scope.
3. Re-check the branch head and workflow after every material change; keep one exact SHA tied to the final evidence matrix.
4. Keep no-KVM TCG fallback deferred for now. Run #152 used KVM; fallback remains unverified and is an optional CI-resilience question, not Android ARM64 evidence.
5. Complete the direct/transitive dependency licence inventory and confirm whether a NOTICE is required; do not create one before confirming copyright/notice obligations.
6. Obtain owner approval of the M0.1 language/target, driver matrix, and packaging recommendation.
7. Obtain owner approval of the MER-0.2 public API, storage boundary, error/partial-result model, cancellation/lifecycle, and versioning decisions.
8. Obtain owner approval of the MER-0.3 forecast/observation archive, matching, verification, privacy/retention, and human-review policy.

Until these gates and approvals are resolved, keep PR #2 open and Draft, do not merge, do not mark MER-0.1 complete, and do not begin production MER-1 implementation.
