# Meridian — continuation state

**Recorded:** 2026-11-28  
**Base:** `main` at `86bc39e` (PR #27 merged)  
**Active milestone:** M0.2 public API and storage contracts  
**Authority:** this file is a working handoff. It is subordinate to `PLAN.md`, `AGENTS.md` and `AI_IMPLEMENTATION_PLAN.md` section 13, which defines the tasks, tiers and guardrails. Live GitHub state wins over the status below.

## How to start a coding session

1. Use the **Meridian-Engine** cloud environment, not Default. It allows `dl.google.com`, which the Android Gradle plugin needs.
2. Choose the model for the task's tier. With Opus unavailable, Sonnet runs O tasks under the fallback rule (see below); Haiku never runs O or S tasks.
3. Paste the task prompt. Every prompt begins with: read `AGENTS.md`, then `AI_IMPLEMENTATION_PLAN.md` section 13.
4. One session per task. Tasks that edit PR #20's branch run one at a time.
5. After a session, update the status table here, and run H1 if a PR merged or closed.

**Fallback header for O tasks run on Sonnet.** Put this in place of the prompt's tier line:

```text
Task <ID> (tier O). Opus is not available, so you are running it on Sonnet under the fallback rule in AI_IMPLEMENTATION_PLAN.md section 13. This is allowed; do not stop for the tier mismatch. In the PR, write "Tool and tier: Claude Code with Sonnet, tier O fallback (Opus review pending)" and add "Opus review pending" to the PR title.
```

## Status

| Task | What | Model | Status | Where |
|---|---|---|---|---|
| H2 | Gradle dependency reports | Haiku | Done; awaiting merge | [PR #29](https://github.com/INFORMATION89/Meridian-Engine/pull/29) |
| H5 | Clean M0.3 spec; inventory of old branch | Haiku | Done; awaiting review. Close PR #25 once PR #28 is accepted | [PR #28](https://github.com/INFORMATION89/Meridian-Engine/pull/28) |
| H3 | Link and format check of PRs #28 and #29 | Haiku | To do (round 1) | — |
| S1 | Fix PR #20 Apple/iOS CI failure | Sonnet | To do (round 1) | [PR #20](https://github.com/INFORMATION89/Meridian-Engine/pull/20) |
| O1 | Per-sample time and provenance design | Sonnet (fallback) | To do (round 1) | new branch `docs/m0-2-sample-provenance-design` |
| O2 | Lifecycle, cancellation and open PR #20 contract questions | Sonnet (fallback) | To do (round 1) | new branch `docs/m0-2-lifecycle-cancellation-design` |
| S5 | Dependency diagram from H2 output | Sonnet | To do (round 2; may start in round 1) | new branch |
| Owner | Approve or adjust the O1 and O2 designs | — | Waiting on O1, O2 | O1/O2 PRs |
| S4 | Freshness and offline contract tests | Sonnet | Blocked on S1 and an approved O2 | PR #20 |
| S3 | Lifecycle and cancellation implementation and tests | Sonnet | Blocked on S4 and an approved O2 | PR #20 |
| S2 | Per-sample time and provenance implementation | Sonnet | Blocked on S3 and an approved O1 | PR #20 |
| O3 | Owner decision register | Sonnet (fallback) | Blocked on S2–S4 | new branch |
| O4 | Final review of PR #20 | Opus preferred | Blocked on S1–S5 | PR #20 |
| H1 / H4 | Status sync; CI result tables | Haiku | As needed | — |

Order: **round 1** (S1, O1, O2, H3 in parallel) → **round 2** (owner approves O1/O2; S5) → **round 3** (S4, then S3, then S2, sequential on PR #20) → **round 4** (O3, H1/H4, then O4, then owner review of PR #20).

## Known facts the next session needs

- **PR #20 Apple/iOS job:** fails on head `d138dca` (run 46088942543); the last fully green run was CI #266. The fixture log shows BrokenPipe errors around `/empty` and `/cancel`, and the partial-response fixture still uses `localhost`.
- **Open contract gaps in PR #20** (decided by O1/O2, implemented by S2–S4):
  - freshness has only `FRESH`/`STALE`; the spec needs expired and unavailable too;
  - the public engine takes no store, so the public cache policies never take effect;
  - `WeatherSource` is a public provider plug-in point that nobody approved;
  - the stale limit is measured from fetch time;
  - every unexpected source exception becomes `TRANSPORT_FAILURE`;
  - there is one valid time and one provenance record per feature;
  - public `data class` types make compatible API evolution harder.
- **Swift cancellation:** direct Swift `async` does not propagate cancellation (PR #21). KMP-NativeCoroutines 1.0.7 does (draft PR #24). Adopting it is an owner decision.
- **ADR-0004** (no host-supplied storage SPI in v1) exists only on PR #20's branch.
- **Not tested:** Android ARM64 runtime, physical Android/iOS devices, native desktop ABIs, and final mobile release-package notice inclusion.

## Round 1 prompts

### S1 — Sonnet

```text
Repository: INFORMATION89/Meridian-Engine. Read AGENTS.md, then AI_IMPLEMENTATION_PLAN.md section 13.

Task S1 (tier S). Work on PR #20's branch, docs/m0-2-api-storage-proposal. Do not open a new branch.
Its apple-and-ios job fails on the current head (run 46088942543). The other 4 jobs pass, and CI #266 was the last fully green head.
1. Find the exact failing Swift test and its assertion in the job log.
2. Find the root cause. Suspects: the partial-response fixture still uses "localhost" (macOS resolves it to IPv6, but the fixture server binds only to IPv4), and a Darwin transport-error assertion. The fixture log shows BrokenPipe errors around the /empty and /cancel requests.
3. Fix the cause. Do not skip, weaken or disable any test.
4. Push in 1-3 commits with no [skip ci], and get one full green matrix.
5. Update PR #20's description with the root cause and the green run link.
Do not change public types, cancellation semantics or storage behaviour; if the fix needs that, stop and report. After two failed diagnoses, stop and report.
```

### H3 — Haiku

```text
Repository: INFORMATION89/Meridian-Engine. Read AGENTS.md, then AI_IMPLEMENTATION_PLAN.md section 13.
Task H3 (tier H, Haiku): for PR #28 and PR #29, check every relative link and file path in the changed .md files resolves on the PR branch, check numbered lists have no duplicate numbers, and check PR/CI links point to INFORMATION89/Meridian-Engine. Report findings in this session only. Do not push or comment unless I ask.
```

### O1 — Sonnet with the fallback header

```text
Repository: INFORMATION89/Meridian-Engine. Read AGENTS.md, then AI_IMPLEMENTATION_PLAN.md section 13, then the relevant parts of PLAN.md sections 8 and 10, docs/specs/M0_2_PUBLIC_API_STORAGE.md, AI_IMPLEMENTATION_PLAN.md section 6 and docs/research/WEATHER_DATA_MODEL.md (research only, not authority).

<FALLBACK HEADER WITH ID O1>
Design only: no production code, no providers, no blending algorithms.

Context: PR #20 (branch docs/m0-2-api-storage-proposal, read-only for you) prototypes the M0.2 API in engine/src/commonMain/kotlin/io/meridian/contractspike/WeatherContractSpike.kt. Its WeatherFeatureData has one validAt, one issuedAt and one SourceProvenance for a whole feature payload. That cannot represent an hourly or 10-day forecast, observations, or a later multi-source blend, where every sample can differ.

Design the public result shape so that each sample carries:
- its valid instant or interval, with explicit semantics: instantaneous, preceding or following interval, accumulation, mean, min/max, and daily values keyed to a local date with an IANA time zone;
- issue/run time, fetch time and forecast lead time, kept distinct from observation (measurement) time;
- source identity: provider, model/product and version, plus native resolution;
- contributors and transformation (native, interpolated, aggregated, resampled, derived, blended), so a future blend can be traced without designing the blend now;
- a typed unit or quantity. Air temperature and apparent temperature stay separate, and derived apparent temperature records its method;
- room for quality and uncertainty indicators, kept separate from freshness;
- missing values represented explicitly, never fabricated.

Also address:
- payload size for 10-day hourly data across several variables (shared provenance records referenced by samples, versus a copy per sample);
- Swift/Objective-C export and Java ergonomics of the shape;
- binary-compatibility risk of public Kotlin data classes, and what to use instead;
- epoch millis versus kotlinx-datetime. A new dependency is an owner decision: recommend, don't add.

Deliverable: a new branch from main, docs/m0-2-sample-provenance-design, adding docs/design/M0_2_SAMPLE_PROVENANCE.md with:
- the problem;
- the proposed types as a Kotlin sketch in markdown;
- worked examples: an hourly forecast, a daily min/max, an accumulated precipitation interval, an observation, and a hypothetical two-source blend;
- alternatives considered with their trade-offs;
- consequences for the cache/history records in spec section 4;
- a short "Owner decisions" list.

Do not edit WeatherContractSpike.kt or the M0.2 spec. Open a PR with .github/pull_request_template.md in 1-3 commits. Follow CLEAN_ROOM.md: public standards and documentation only (for example CF conventions, ISO 8601, WMO terminology), listed under Sources consulted.
Finish with the AGENTS.md report.
```

### O2 — Sonnet with the fallback header

```text
Repository: INFORMATION89/Meridian-Engine. Read AGENTS.md, then AI_IMPLEMENTATION_PLAN.md section 13, then the relevant parts of docs/specs/M0_2_PUBLIC_API_STORAGE.md (sections 3-5 and 7-8), PLAN.md section 16, and ADR-0003.

<FALLBACK HEADER WITH ID O2>
Design only: no production code.

Evidence to use (read-only):
- PR #20, branch docs/m0-2-api-storage-proposal: engine/src/commonMain/kotlin/io/meridian/contractspike/WeatherContractSpike.kt and its tests, plus ADR-0004 (no host-supplied storage SPI in v1) on that branch.
- PR #21 (closed): direct Swift async over the generated completion-handler bridge does NOT propagate Swift Task cancellation into Kotlin (CI #232).
- PR #24 (draft): KMP-NativeCoroutines 1.0.7 does propagate it (CI #251 attempt 3, 5/5 jobs). Read its description for the lockfile and licence-metadata caveats. SKIE's published range did not cover Kotlin 2.4.21.

Specify:
1. Lifecycle: create/close, idempotent close, what happens to in-flight requests on close (cancel or drain; recommend one), and use after close.
2. Concurrency: many requests on one engine, isolation, and whether identical in-flight requests are deduplicated.
3. Cancellation from Kotlin, Java/JVM, Android and Swift callers. Recommend the Swift facade shape, and whether KMP-NativeCoroutines should become a production dependency: footprint, licence metadata, toolchain pinning, and the alternative if it is not adopted.
4. Write atomicity: a cancelled or interrupted write is never visible as a complete record, including what a reader sees mid-write.
5. Timeout budget and typed error mapping: transport, parse, rate limit and storage failures, with programming errors kept separate (today every non-cancellation exception from a source becomes TRANSPORT_FAILURE).

Resolve these open contract questions in the PR #20 prototype:
a. Freshness has only FRESH and STALE; the spec requires fresh, stale-but-usable, expired and unavailable.
b. The public constructor takes no store (ADR-0004), so the public PREFER_FRESH and ALLOW_STALE policies can never take effect, and offline cache behaviour cannot be shown through the public API, which spec section 7 requires. Propose how consumers enable the built-in in-memory or optional SQLDelight cache without a public storage SPI.
c. Consumers must inject a public WeatherSource, which is effectively an unapproved public provider plug-in point. Recommend whether it stays public, becomes internal with fixture factories, or something else.
d. The stale-age limit is measured from fetch time, not expiry time. Pick one and justify it.

Deliverable: a new branch from main, docs/m0-2-lifecycle-cancellation-design, adding docs/design/M0_2_LIFECYCLE_CANCELLATION.md with:
- each item above as recommendation, alternatives and consequences;
- the test cases S3 and S4 must implement, one per behaviour with no duplicates;
- a short "Owner decisions" list. Adopting any new dependency and changing the public API are owner decisions.

Do not edit WeatherContractSpike.kt, the M0.2 spec or PR #24. Open a PR with .github/pull_request_template.md in 1-3 commits. Follow CLEAN_ROOM.md: official Kotlin, kotlinx.coroutines, Swift concurrency and Ktor documentation, listed under Sources consulted.
Finish with the AGENTS.md report.
```

Prompts for S2–S5, O3 and O4 are added here once their inputs exist.
