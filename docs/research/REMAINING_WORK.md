# Meridian — remaining work and review checklist

**Status:** consolidated checklist for owner review  
**Prepared:** 2026-11-25  
**Applies to:** MER-0.1 stabilization, MER-0.2, MER-0.3, and the research-document PR  
**Authority:** PLAN.md, RULES.md, AGENTS.md, CLEAN_ROOM.md, and approved milestone specifications remain authoritative.

This file records what is still open after reviewing the MER-0.1 spike and the architecture/weather research. It is a checklist, not a new implementation authorization.

## 1. Current PR status

| Pull request | Purpose | State at review | What remains |
|---|---|---|---|
| [PR #2](https://github.com/INFORMATION89/Meridian-Engine/pull/2) | MER-0.1 KMP/platform proof | Open, Draft, not merged | Reconcile stale run records; close platform/HTTP/dependency evidence; obtain owner decision |
| [PR #4](https://github.com/INFORMATION89/Meridian-Engine/pull/4) | Architecture and weather research documents | Open, Draft, mergeable at last check | Review remaining research findings and decide whether to accept the proposals |

Do not merge either PR merely because the workflows are green. They have different purposes and separate owner gates.

## 2. MER-0.1 — outstanding engineering/evidence

### P0 — reconcile the evidence record

- [ ] Update continue.md on the spike branch. It still describes Run #147 as in progress and records an older branch SHA.
- [ ] Update docs/verification/MER-0_1_RESULTS.md with Run #152, its exact tested commit, job results, platform/runtime details, and the limits that remain.
- [ ] Update PR #2's description. It still describes Run #152 as pending and cites Run #104 as the last fully green baseline.
- [ ] Verify the branch head after workflow-generated metadata commits, and record the SHA actually exercised by the final matrix.
- [ ] Keep all platform cells explicit: PASS, FAIL, BLOCKED, or NOT TESTED.

Run #152 completed six jobs successfully on the inspected workflow: dependency metadata, Linux dependency metadata, Apple/iOS, Linux desktop, Windows desktop, and Android. Its logs show:

- Windows self-contained JVM app-image: **169.7 MiB**.
- Linux self-contained JVM app-image: approximately **183 MB**.
- macOS self-contained JVM app-image: approximately **184 MB**.
- The packaged desktop consumers launched after the runner Java environment was removed.
- Android x86_64 instrumentation passed. The log confirms that /dev/kvm was available and writable; therefore this run did **not** validate TCG/software acceleration without KVM.
- The Apple workflow ran an ARM64 iOS Simulator Swift Package consumer, not a physical iPhone/iPad.

These are test-run measurements, not release-package size guarantees.

### P0 — real Ktor behaviour before production stack approval

- [ ] Send deterministic fixture requests through the actual Android, Darwin, and JVM/CIO engines. A common MockEngine test does not exercise those engines.
- [ ] Assert connect/request/socket timeout behaviour where supported and document differences.
- [ ] Assert transport and parse failures map to the intended typed problem boundary.
- [ ] Assert cancellation from the consumer boundary on Android, Swift/iOS, and JVM. Common coroutine cancellation alone is not enough.
- [ ] Confirm that client close/disposal releases platform resources and define behaviour for in-flight work.
- [ ] Re-check Ktor 3.6.2 as a candidate only after the real-engine tests can run; do not claim it is validated until they pass.

The formal M0.1 specification asks for a minimal Ktor path. The PR's owner gate also calls for real request, timeout, transport-error, and platform-boundary cancellation evidence before full Ktor parity is claimed. The owner should confirm which of these are required to close M0.1 and which can be a narrowly scoped follow-up before MER-1.

### P0 — Android ARM64 and platform support claims

- [ ] Android runtime evidence currently covers x86_64 emulator only. Android ARM64 remains **NOT TESTED**.
- [ ] Obtain a physical ARM64 device run or a reproducible, provenance-reviewed ARM64 environment.
- [ ] If ARM64 cannot be tested before the first release, obtain an explicit owner decision to accept that limitation and document the support boundary. Do not relabel x86_64 evidence as ARM64 support.
- [ ] Keep Windows/macOS/Linux desktop claims scoped to JVM artifacts. No native .dll, .dylib, or .so library is proven.
- [ ] Keep physical iOS-device testing as a release/readiness check unless the owner explicitly makes it a milestone requirement. The current spike spec asks for it only where available; iOS Simulator + Swift consumer tests have passed.

### P1 — CI resilience and supply-chain documentation

- [ ] Test TCG/software fallback only on a runner where KVM is confirmed unavailable. Run #152 used KVM successfully, so fallback remains unverified.
- [ ] Decide whether no-KVM TCG is a CI resilience expectation or a required acceptance criterion. It is not proof of Android ARM64.
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
2. Accept or defer Android ARM64 validation for the first release; if deferred, approve an explicit support limitation.
3. Approve the batch-first public API with per-feature results and cancellation semantics.
4. Approve optional persistence and decide whether custom host storage is part of v1 or a later extension.
5. Keep history retention configurable and bounded; choose no fixed 30-day default until evidence supports one.
6. Approve the MER-0.3 verification policy: versioned archives/matching, chronological holdout, segmented metrics, human approval, no automatic promotion.
7. Decide whether optional MCP assessment is deferred; it is not needed by the engine.
8. Confirm the AI development policy's adoption status and keep runtime AI out of Meridian.
9. Confirm the first consumer deployment assumptions for provider access, especially Open-Meteo terms and credentials.

## 8. Recommended next sequence

1. Update MER-0.1 status files and PR #2 description from Run #152.
2. Close the real-engine Ktor tests and determine the Android ARM64 disposition.
3. Complete dependency licence/NOTICE review and get owner acceptance of the M0.1 platform recommendation.
4. Review and approve the MER-0.2 decision register; then build the independent fixture-backed contract consumer.
5. Review and approve MER-0.3 archive, matching, metrics, privacy, and human-approval decisions.
6. Only then authorize MER-1's first production forecast adapter.

**Stop condition:** no production forecast/provider implementation should start until the explicit owner gates in the master plan and milestone specifications are satisfied.