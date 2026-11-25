# Meridian — remaining work and review checklist

**Status:** consolidated checklist for owner review  
**Prepared:** 2026-11-25  
**Applies to:** MER-0.1 stabilization, MER-0.2, MER-0.3, and the research-document PR  
**Authority:** PLAN.md, RULES.md, AGENTS.md, CLEAN_ROOM.md, and approved milestone specifications remain authoritative.

This file records what is still open after reviewing the MER-0.1 spike and the architecture/weather research. It is a checklist, not a new implementation authorization.

## Owner decision — CI-only validation for now (2026-11-25)

The owner accepts a passing supported CI matrix as sufficient platform validation for the current MER-0.1 stage. **Physical-device testing is deferred** because not all target platforms/devices are currently available. No manual device runs are required from the owner, and the absence of physical Android/iOS hardware is not an MER-0.1 blocker.

Keep the evidence precise: the current run covers Android x86_64 emulator, iOS ARM64 simulator/Swift consumer, and JVM desktop on Windows, macOS, and Linux. Android ARM64 runtime and physical Android/iOS device execution remain **NOT TESTED**; this decision accepts those limitations for now and does not claim those paths were proven. Revisit device coverage when hardware is available or when a concrete release/consumer requirement makes it necessary.

Any additional transport/integration checks (including real Ktor platform-engine requests) are CI work and must use deterministic local fixtures, not live provider access or a device supplied by the owner.

## 1. Current PR status

| Pull request | Purpose | State at review | What remains |
|---|---|---|---|
| [PR #2](https://github.com/INFORMATION89/Meridian-Engine/pull/2) | MER-0.1 KMP/platform proof | Open, Draft, not merged | Complete any required CI-level HTTP coverage and dependency/licence review; physical-device testing is deferred by owner decision |
| [PR #4](https://github.com/INFORMATION89/Meridian-Engine/pull/4) | Architecture and weather research documents | Open, Draft, mergeable at last check | Review remaining research findings and decide whether to accept the proposals |

Do not merge either PR merely because the workflows are green. They have different purposes and separate owner gates.

## 2. MER-0.1 — outstanding engineering/evidence

### P0 — reconcile the evidence record

- [x] Update continue.md on the spike branch with Run #152 and the latest inspected head.
- [x] Update docs/verification/MER-0_1_RESULTS.md with Run #152, its exact tested commit, job results, platform/runtime details, and the limits that remain.
- [x] Update PR #2's description to record Run #152 and current remaining gaps.
- [x] Record that Run #152 tested commit 28817f6a09c0099ac340ff4df55ea5377ec5ed0a; documentation-only commits then advanced the PR head to 3575f51752b15fecff76c4405610b44fdafeea2c.
- [x] Keep all platform cells explicit: PASS, FAIL, BLOCKED, or NOT TESTED.

The evidence-record synchronization is complete. Re-run the relevant workflows and update these records whenever implementation or workflow code changes.

Run #152 completed six jobs successfully on the inspected workflow: dependency metadata, Linux dependency metadata, Apple/iOS, Linux desktop, Windows desktop, and Android. Its logs show:

- Windows self-contained JVM app-image: **169.7 MiB**.
- Linux self-contained JVM app-image: approximately **183 MB**.
- macOS self-contained JVM app-image: approximately **184 MB**.
- The packaged desktop consumers launched after the runner Java environment was removed.
- Android x86_64 instrumentation passed. The log confirms that /dev/kvm was available and writable; therefore this run did **not** validate TCG/software acceleration without KVM.
- The Apple workflow ran an ARM64 iOS Simulator Swift Package consumer, not a physical iPhone/iPad.

These are test-run measurements, not release-package size guarantees.

### P0 — Ktor behaviour exercised in CI before production stack approval

- [ ] Send deterministic fixture requests through the actual Android, Darwin, and JVM/CIO engines. A common MockEngine test does not exercise those engines.
- [ ] Assert connect/request/socket timeout behaviour where supported and document differences.
- [ ] Assert transport and parse failures map to the intended typed problem boundary.
- [ ] Assert cancellation from the consumer boundary on Android, Swift/iOS, and JVM. Common coroutine cancellation alone is not enough.
- [ ] Confirm that client close/disposal releases platform resources and define behaviour for in-flight work.
- [ ] Re-check Ktor 3.6.2 as a candidate only after the real-engine tests can run; do not claim it is validated until they pass.

The formal M0.1 specification asks for a minimal Ktor path. Any additional real-engine request, timeout, transport-error, and platform-boundary cancellation checks should run in CI against deterministic local fixtures; they do not require the owner to have physical devices or to provide live provider access. Keep the claims scoped to the engine/targets actually exercised, and do not claim full parity until those CI checks pass.

### Accepted platform coverage and explicit limits

- [x] Owner accepts the passing supported CI matrix as sufficient for current MER-0.1 validation; physical-device testing is deferred and is not a milestone blocker.
- [ ] Android runtime evidence currently covers x86_64 emulator only. Android ARM64 remains **NOT TESTED**; preserve that limitation and do not infer ARM64 runtime support from x86_64.
- [ ] Physical iOS-device execution remains **NOT TESTED**; the current evidence is iOS ARM64 Simulator + Swift consumer tests. No physical iOS run is required for MER-0.1 under the current owner decision.
- [ ] Keep Windows/macOS/Linux desktop claims scoped to JVM artifacts. No native .dll, .dylib, or .so library is proven.
- Device coverage may be revisited when hardware is available or a concrete release/consumer requirement calls for it; it is not part of the current CI acceptance gate.

### P1 — optional CI resilience and supply-chain documentation

- [x] Defer the no-KVM TCG/software-emulation fallback for now. Run #152 used KVM successfully, so the fallback remains unverified; this is not a current MER-0.1 blocker and is not proof of Android ARM64.
- [ ] Complete the direct and transitive dependency licence/notice inventory.
- [ ] Confirm the correct copyright holder and whether a project NOTICE file is needed before creating it.
- [ ] Record exact supported runtime/JDK requirements and tested Linux distributions.
- [ ] Review the Gradle disabled-native-target warnings on Linux/Windows and decide whether they are expected benign target-skips or require an explicit configuration note.

## 3. MER-0.2 — owner decisions and contract work

The draft proposal is conceptually sound, but these choices must be explicit before implementation.

- [ ] **Public API style:** approve or revise batch-first asynchronous request/result. Do not add streaming/Flow without a demonstrated consumer need.
- [ ] **Feature outcomes:** define available, partial, and unavailable outcomes for each requested feature and stable machine-readable problem codes.
- [ ] **Invalid input:** decide which invalid requests reject the entire request before network activity versus which unsupported features can be reported as per-feature unavailability.
- [ ] **Cancellation:** define Kotlin suspend cancellation, Swift bridging/cancellation, Java/JVM behaviour, and the rule that cancelled writes cannot be committed as complete.
- [ ] **Engine lifecycle:** define create/close, idempotence, in-flight operations, concurrency, and resource disposal.
- [ ] **Storage optionality:** confirm core operation without persistent storage.
- [ ] **Custom storage:** decide whether host-supplied storage is a v1 requirement or a design goal pending proof. If supported, define a small conformance test suite and ownership of transactions, concurrency, schema versioning, and deletion.
- [ ] **Cache vs history:** keep response-cache expiry separate from forecast/observation verification-history retention.
- [ ] **Retention:** no 30-day default is adopted. Decide whether history is disabled by default, configurable and bounded, and independently deletable.
- [ ] **Persistence compatibility:** define schema-version and safe-discard/migration behaviour independently from public API versioning.
- [ ] **Public API stability:** keep experimental until a stable package is approved; define semver and deprecation policy for that release.
- [ ] **Time/units contract:** approve typed quantity/unit identifiers, interval semantics, UTC instants, and IANA local-day handling.
- [ ] **Privacy:** keep data on-device by default; no telemetry or sharing. Optional sharing remains a separate explicit-consent design.

A minimum independent sample consumer should prove the approved contract without Solstice, a real weather provider, persistent storage, provider credentials, a hosted service, or AI.

## 4. MER-0.3 — scientific verification decisions

- [ ] Select the first forecast archive based on the exact research question: Previous Runs for fixed-lead aggregates; Single Runs when full individual runs are necessary and accessible.
- [ ] Record archive endpoint, model/product coverage, terms, data licence, attribution, access tier, request cost, retrieval time, and dataset version.
- [ ] Do not silently replace issued forecasts with Historical Forecast or reanalysis and claim that they are the same product.
- [ ] Define the observation source, station quality flags, spatial/elevation/temporal matching method, and exclusions.
- [ ] Define versioned matching rules before inspecting candidate results.
- [ ] Approve initial metrics before evaluation: MAE/RMSE/bias for continuous quantities; Brier score and calibration/reliability checks only for a defined probability event.
- [ ] Freeze tuning and final evaluation into separate chronological periods; report samples, exclusions, and missingness.
- [ ] Require a human review and approval for every production algorithm/configuration change. No automatic weight promotion or self-modifying engine.
- [ ] Decide whether MCP assessment is deferred. It is unrelated to forecast verification and must remain outside the weather core.
- [ ] Keep AI_POLICY.md status consistent with AGENTS.md: mandatory to read does not by itself mean every proposed policy clause has been formally adopted.

## 5. Weather-intelligence and provider research still needed

### Weather data model

- [ ] Finalize the first-release variable inventory and the distinction between required and optional values.
- [ ] Validate temporal semantics for each source: instantaneous, preceding/following interval, accumulation, mean, min/max, and daily local date.
- [ ] Keep snow depth, snowfall accumulation, and snowfall water equivalent distinct.
- [ ] Keep rain rate/intensity separate from accumulated precipitation.
- [ ] Define supported precipitation-event probability semantics. Model agreement is not automatically calibrated probability.
- [ ] Decide how provider-specific apparent-temperature methods are retained and how any future Meridian-derived value is versioned.
- [ ] Define quality, freshness, availability, and uncertainty as separate dimensions.
- [ ] Define a privacy-safe spatial precision policy for cache/history and station diagnostics.

### Providers

- [ ] Open-Meteo: validate variables, best-match model availability, native/interpolated 15-minute data, grid coordinates/elevation, request weights, attribution, service plan, commercial eligibility, and client-key distribution for each intended consumer deployment.
- [ ] AviationWeather.gov: validate Mexico station coverage and real METAR fixtures; treat airports as observations with representativeness limits, not universal neighbourhood measurements.
- [ ] SMN/CONAGUA: map each dataset separately. The general SMN terms permit open use with attribution but distinguish exact reproduction from genuine value-added derivative products and require responsible automated access. Obtain legal review for the exact redistribution/UI use; confirm each dataset's schema, owner, time zone, latency, update cadence, quality flags, and data dictionary.
- [ ] WMO/direct authority feeds: identify the actual feed for each issuing authority; do not treat the SWIC website itself as a universal ingestion API.
- [ ] FPAS: verify current API/schema, service terms, update latency, source attribution, duplication behaviour, and availability before integration.
- [ ] Air quality and UV: identify pollutant/scale methodology, station/model origins, geographic coverage, freshness, and attribution.
- [ ] Radar: keep as research-only until access, coverage, latency, georeferencing, and regional verification are demonstrated.

## 6. Research-document review

Before PR #4 is accepted:

- [ ] Review the five documents as proposals, not approved implementation requirements.
- [ ] Check the authority map and relative links.
- [ ] Check each current provider claim against the linked official documentation and the recorded review date.
- [ ] Decide whether the architecture recommendations should be incorporated into PLAN.md and the MER-0.2/MER-0.3 specs after approval.
- [ ] Resolve legacy documentation conflicts only in the authoritative files, not by creating competing sources of truth.
- [ ] Keep the research branch and PR #2 independent.

## 7. Owner decisions — concise review set

The following decisions materially determine the next stage:

1. Accept the five-OS route as **Android + iOS native framework + Windows/macOS/Linux JVM**, not native desktop libraries, for the first release.
2. **Testing scope — decided:** accept the passing supported CI matrix for current MER-0.1 validation. Physical Android/iOS device testing and Android ARM64 runtime validation are deferred; keep them marked NOT TESTED and revisit only when hardware or a concrete requirement makes them necessary.
3. Approve the batch-first public API with per-feature results and cancellation semantics.
4. Approve optional persistence and decide whether custom host storage is part of v1 or a later extension.
5. Keep history retention configurable and bounded; choose no fixed 30-day default until evidence supports one.
6. Approve the MER-0.3 verification policy: versioned archives/matching, chronological holdout, segmented metrics, human approval, no automatic promotion.
7. Decide whether optional MCP assessment is deferred; it is not needed by the engine.
8. Confirm the AI development policy's adoption status and keep runtime AI out of Meridian.
9. Confirm the first consumer deployment assumptions for provider access, especially Open-Meteo terms and credentials.

## 8. Recommended next sequence

1. Complete any remaining deterministic real-engine Ktor, timeout, error-mapping, and cancellation checks in CI; no owner-supplied physical devices are required.
2. Complete dependency licence/NOTICE review and get owner acceptance of the M0.1 language/target and packaging recommendation; the CI-only testing scope and physical-device deferral are already accepted.
3. Review and approve the MER-0.2 decision register; then build the independent fixture-backed contract consumer.
4. Review and approve MER-0.3 archive, matching, metrics, privacy, and human-approval decisions.
5. Only then authorize MER-1's first production forecast adapter.

**Stop condition:** no production forecast/provider implementation should start until the explicit owner gates in the master plan and milestone specifications are satisfied.