# Meridian — continuation state

**Recorded:** 2026-11-25  
**Repository:** [INFORMATION89/Meridian-Engine](https://github.com/INFORMATION89/Meridian-Engine)  
**Branch:** `spike/m0.1-kmp-platform-proof`  
**Current branch HEAD:** `3657c8f429661d8a4c480ebb015dbe8fa0bb6227`  
**PR:** [#2](https://github.com/INFORMATION89/Meridian-Engine/pull/2) — **OPEN · DRAFT · NOT MERGED**  
**Current phase:** **MER-0.1 stabilization**. MER-0.1 is not complete; MER-1 has not started.

This continuation record was added because no `continue.md` or other continuation-named file existed on the PR branch. The verified-results record remains at [docs/verification/MER-0_1_RESULTS.md](docs/verification/MER-0_1_RESULTS.md).

## Current CI state

The latest branch commit is `3657c8f429661d8a4c480ebb015dbe8fa0bb6227`. As of this record, [Run #147](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45714161690) is **in progress**, not a pass. The `dependency-metadata` job has checked out the commit and installed Android compile SDK packages; the Gradle metadata task has not yet completed. No claim is made that the Linux checksum has been added or that downstream platform jobs passed.

| Run | Tested commit | Observed result |
|---|---|---|
| [#145](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45712328566) | `4da50e2c21dc03bff8e4a756a0476d894997d9d0` | **FAIL** before SDK setup: checkout was given a full SHA as a branch ref and formed an invalid `refs/heads/<SHA>*` refspec. No Linux checksum was added. |
| [#146](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45713934656) | `e6f647205c53c962dbef2e7549a7f280a29860fd` | **FAIL**: Bash passed a literal backslash as an extra Gradle task (`Task '\\' not found`). Verification metadata was not modified. |
| [#147](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45714161690) | `3657c8f429661d8a4c480ebb015dbe8fa0bb6227` | **IN PROGRESS** at record time. It includes the corrected single-backslash task continuations; outcome unknown. |

Commits for the latest Linux metadata repair chain:
- `4da50e2c21dc03bff8e4a756a0476d894997d9d0` — add Linux-specific Android dependency checksum generation.
- `e6f647205c53c962dbef2e7549a7f280a29860fd` — fix Linux metadata job GitHub expressions/checkout reference.
- `3657c8f429661d8a4c480ebb015dbe8fa0bb6227` — fix Bash/Gradle line continuations; head tested by #147.

## Audit A–D

### A — CI reproducibility and dependency integrity

**Implemented, not yet fully verified on the combined current state.** GitHub Actions are pinned to immutable SHAs; Gradle dependency locking and SHA-256 verification metadata are committed; the Gradle wrapper distribution has checksum verification. The committed metadata has 486 components / 936 artifacts / 936 SHA-256 entries, but the Linux AAPT2 artifact `aapt2-9.3.1-15703166-linux.jar` was absent from metadata generated on macOS. Linux enrichment is underway in #147. Initial checksum generation is trust-on-first-use and does not independently authenticate publishers.

Relevant commits:
- `85c73bb6a8c428c650675be4c89d5843f225bcc7` — pin GitHub Actions.
- `3997af0212959e6d52c188cef248a09324a58cf3` — enable dependency locking.
- `02c4a0895c49418e2d914f0439f966f4d76cea2b` — generate lock and verification metadata.
- `422e4eb31f0d1993e6d91997009e5f6aea851140` — verify Gradle distribution checksum.
- `f252b551d86b85828810883f57506465df26195f` — lock resolved dependencies and verify checksums.
- Linux-specific follow-up commits are listed above; still awaiting a successful #147 result and normal-mode verification.

### B — Persistence and platform tests

The iOS SQLDelight probe now isolates and cleans its named database before/after execution, including failure paths: `ac8ebf0ceb0c8f640a3da56eb40846b7a433dfed`. Android, iOS, and desktop transaction/close/reopen probes exist. Run #131 passed Windows, Linux, and Apple/iOS, but the current combined-metadata commit must be rerun across the matrix before treating the repairs as jointly validated.

### C — Desktop distribution

Self-contained `jpackage` app-images with bundled Java were exercised on Windows, Linux, and macOS in Run #131. Sizes recorded there: Windows 169.7 MiB, Linux 183 MiB, macOS 184 MiB. The consumers launched with the runner Java environment removed. Repeat these checks on the combined-metadata SHA. This proves app-image launch for the tested runners; it is not a claim of native `.dll`, `.dylib`, or `.so` library support.

Relevant commits include `b0fcc00198f93ea8156bff937a28e6f21078cfba` (app-image matrix) and `1884aec07f52394589f761c61bbf17ac53138cab` (hardened checks).

### D — Repository foundations and decisions

The experimental README and Apache-2.0 LICENSE are present; MER-0.2 API/storage and MER-0.3 verification proposals are linked. The third-party dependency/licence inventory and decision on whether a project `NOTICE` is needed remain open; do not create a NOTICE without confirming attribution/copyright requirements. The toolchain, driver matrix, packaging approach, and proposals remain owner-review items.

Relevant commits: `6e89b913c4939759dd4ed4701e461558345e8885` (experimental README), `f747843485e25d9f189735de5906a40c673db113` (Apache-2.0 license), `3edd52db038535ec8246958e26a4efa1ff4972c8` (MER-0.2 proposal), `a83518ada3f65e40894da4a0c3c27616631163d3` (MER-0.3 proposal).

## Ktor, timeout, and cancellation

- Current tested spike candidate remains **Ktor 3.6.1**. Ktor **3.6.2 has not been adopted or validated**; reconsider it after CI metadata and platform matrix stabilize.
- Common fixture coverage uses `MockEngine`; platform lifecycle probes create/close Android, Darwin, and CIO clients. They do not yet prove real HTTP requests through each platform engine.
- Explicit real-engine timeout, transport-error mapping, and consumer/platform-boundary cancellation tests remain pending. Common coroutine cancellation coverage is not equivalent to platform-boundary cancellation coverage.
- Run #142 reached Android instrumentation after KVM emulator boot but failed during APK resource compilation: dependency verification lacked the Linux AAPT2 checksum. It did not validate TCG fallback.
- The workflow now explicitly requests TCG fallback (`ANDROID_I_WANT_MY_TCG=yes`) with a 900-second boot timeout. Fallback remains **UNVERIFIED** until instrumentation completes successfully with KVM unavailable. A KVM-enabled success cannot establish fallback behavior.

## Android ARM64

**NOT TESTED.** Current Android CI uses an Android 35 x86_64 emulator on a Linux x86_64 runner. Do not infer ARM64 runtime support from x86_64. Acceptance needs a physical ARM64 device or a separately validated, reproducible ARM64 emulator/host route. Do not use an unofficial emulator binary without provenance and licence review.

## Gates before MER-1

1. Finish #147; verify the Linux AAPT2 checksum was generated from the intended commit and committed to the PR branch.
2. Run the Android build in normal dependency-verification mode against the combined Linux/macOS metadata; then rerun instrumentation.
3. Rerun the complete Apple/iOS, Windows, Linux, and Android matrix on one recorded SHA, including Swift consumer, iOS database cleanup, and self-contained desktop app-images.
4. Separately prove TCG fallback only in a run where KVM is unavailable and Android instrumentation passes.
5. Add deterministic real-engine Ktor request, timeout, transport-error, and platform-boundary cancellation coverage for the selected runtime matrix; evaluate Ktor 3.6.2 without claiming it works before tests pass.
6. Complete Android ARM64 runtime validation, or keep it explicitly NOT TESTED and obtain owner acceptance of that gap under the milestone criteria.
7. Complete dependency/licence inventory and confirm whether NOTICE is required.
8. Obtain owner review/approval of MER-0.1 platform/runtime evidence and recommendations, plus MER-0.2 public API/storage/error/cancellation/versioning decisions and MER-0.3 verification/archive policy.

Until these gates and approvals are resolved, keep PR #2 open and Draft, do not merge, do not mark MER-0.1 complete, and do not begin production MER-1 implementation.