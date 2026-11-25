# MER-0.3 — Forecast verification and research decision proposal

**Status:** design recommendation for owner review; no archive, retention policy, or learning algorithm is adopted by this proposal.  
**Parent:** [MER-0.3 specification](MER-0_3_VERIFICATION_RESEARCH.md)  
**Related:** [MER-0.2 API/storage proposal](MER-0_2_DECISION_PROPOSAL.md), [AI policy](../../AI_POLICY.md), [master plan](../../PLAN.md).

## Executive recommendation

Treat forecast verification as a reproducible research workflow, not an automatic learning system. Preserve what a forecast actually said when it was issued, match it later to quality-controlled observations, compare a candidate with a fixed baseline, and require human approval for every production change.

Start with simple descriptive metrics and a small, explicitly licensed dataset. Do not start by training an ML model, changing provider weights automatically, or building a cloud archive.

## 1. Separate the three kinds of data

Do not store or compare these as if they were interchangeable:

1. **Forecast snapshot:** what a provider/model predicted, including its issue/run time and valid time.
2. **Observation:** what a station or accepted observation source measured, including measurement time and quality information.
3. **Evaluation record:** the result of matching one forecast value with one eligible observation under a versioned rule set.

Reanalysis, retrospective analysis, and a stitched historical product are not automatically equivalent to an archived forecast that was available at the original issue time. A verification report must state exactly which product was used.

## 2. Forecast snapshot record

The minimum logical record should preserve:
- provider, model, product, and source schema/version;
- model initialization/issue time and the time Meridian fetched it;
- forecast valid time and lead time;
- variable, unit, and value (including explicit missing/null status);
- location/grid identifier and returned grid coordinates/elevation where applicable;
- product/endpoint identity, request parameters, and archive/licence metadata;
- collection timestamp and an immutable dataset/version identifier.

A record must not be rewritten when a newer model run arrives. Corrections or normalization should be stored as a separate, versioned transformation with links to the original provider value.

## 3. Observation record

The minimum logical record should preserve:
- observation provider, station/grid identity, and product version;
- measurement time and fetch/collection time as separate timestamps;
- variable, unit, value, and explicit missing/invalid status;
- quality/control flags and source-reported uncertainty where available;
- station coordinates, elevation, and spatial precision sufficient for the approved matching method;
- source version, licence, and immutable dataset/version identity.

Keep only the spatial precision required for matching and quality assessment. Do not create a saved-place list or general user location history in the verification store.

## 4. Matching rules

Matching must be deterministic and versioned. The matching record should identify:
- the forecast issue time, valid time, lead time, and source/model;
- the selected observation and measurement time;
- time tolerance, distance, elevation difference, and quality filters applied;
- every exclusion reason when no observation is accepted;
- the matching-rule version.

Do not choose a station solely because it is geographically nearest. Distance, elevation, station exposure, observation age, source quality, and variable type all affect representativeness. Exact thresholds must be approved and calibrated to the target region/variable; this proposal deliberately does not invent universal numbers.

For a valid evaluation, the observation must represent the same variable and compatible unit, and must be measured at or sufficiently close to the forecast's valid time under a rule set fixed before candidate results are inspected. A later observation cannot be treated as having been available to the model at forecast issue time.

## 5. Archive selection and legal access

Select an archive according to the evaluation question:

- Previous Runs is suitable for fixed-lead aggregate skill analysis, but it does not reconstruct every full model run.
- Single Runs preserves individual initialized runs and is preferable when the exact run history is needed and available.
- Historical Forecast is a stitched archive with different semantics; do not use it as a substitute for full run records without stating that limitation.

Open-Meteo currently distinguishes the archive endpoints and their access tiers. The free Forecast API service is restricted to non-commercial use, while parts of the archive offering require a paid tier. Data licensing and service access rights are separate. The experiment manifest must record the exact endpoint, model, region/time coverage, access tier, licence/terms, retrieval date, expected call cost, and attribution requirements. If the required archive cannot be accessed under approved terms, reduce the experiment scope or choose another permitted source; do not silently substitute reanalysis and claim forecast skill.

No large archive should be downloaded until its licence, terms, budget, geographic/model coverage, and retention/storage impact are reviewed.

## 6. Evaluation methodology

### Initial baseline

Start with transparent metrics:
- MAE and RMSE for continuous variables;
- mean bias for systematic over/under prediction;
- Brier score and calibration/reliability analysis for probabilistic precipitation when the probability data and event definition support it.

Metrics must be selected before viewing candidate outcomes. Report sample counts, exclusions, missing values, and relevant uncertainty alongside every score. A score without a denominator and evaluation period is incomplete.

### Segmentation

Where the dataset supports it, report separately by:
- variable and unit;
- forecast lead-time bucket;
- region/grid or station group;
- season/month;
- provider/model/product;
- relevant quality and representativeness category.

Do not allow an aggregate gain to conceal a regression for a variable, region, season, or lead time. Compare the candidate both with Meridian's unchanged baseline and with the contributing source model(s).

### Leakage and holdouts

Use chronological splits. Tuning and final evaluation periods must be separate. Do not tune a threshold, weight, or correction using the same cases used to claim final improvement. Forecasts are only scored against observations that occur after their issue time; no future observation may enter the information available to the forecast being evaluated.

Every report must identify the data versions, matching rules, source/model versions, algorithm/configuration versions, parameters, exclusions, evaluation code revision, sample counts, and metric definitions.

## 7. Human-reviewed improvement loop

1. Ingest a permitted, versioned forecast/observation dataset.
2. Validate schemas, timestamps, units, duplicates, missing values, and quality flags.
3. Run deterministic matching and evaluation scripts.
4. Compare against the unchanged baseline and inspect segmented regressions.
5. Optionally use AI as a contributor aid to investigate a hypothesis, subject to the project's AI policy.
6. Reproduce every material finding using deterministic code and the same versioned data.
7. Propose a narrowly scoped change with a specification and regression fixtures.
8. Obtain human approval; run the full evaluation again before merging.

No automatic weight update, self-modifying production engine, or automatic promotion from an analysis result is authorized by this workflow.

## 8. Storage and privacy recommendation

- Keep the verification workflow local-first. No Meridian-hosted data collection or always-on service is required.
- Forecast and observation history must be independently disableable and deletable.
- Keep retention configurable and bounded. The proposed 30-day local history from earlier notes remains a recommendation, not an adopted default, until owner approval and a storage-cost review.
- Preserve source attribution and licences for each dataset.
- Do not send precise user coordinates, personal location histories, credentials, or private user data to external AI tools.
- Aggregate or minimize location context where exact coordinates are not needed for analysis.
- Any voluntary data contribution is a separate feature requiring explicit opt-in and a separate privacy/security/legal design.

## 9. Optional MCP/chat assessment

MCP is not required to verify forecast accuracy and must not become a core dependency. If a future chat integration is justified, keep it outside the engine core and expose only explicit, read-only queries over the stable public API. A local chat host may be able to reach a local adapter; a cloud-hosted chat cannot normally reach a device's localhost without a separately approved relay or hosted service. Do not create that infrastructure under this milestone.

The owner may defer the MCP question entirely. Core forecast retrieval, results, verification, and application consumption must work without chat or AI.

## 10. Owner decisions required

| Decision | Recommendation | Owner action |
|---|---|---|
| Forecast archive | Use Previous Runs for fixed-lead skill metrics; Single Runs when individual run history is necessary and accessible | Approve or revise |
| Archive access | Select a source/plan only after terms, licence, coverage, and cost review | Select deployment assumptions |
| Matching policy | Versioned matching rules with explicit time, distance, elevation, and quality filters | Approve thresholds by variable/region later |
| Initial metrics | MAE, RMSE, bias; Brier/calibration for suitable probabilities | Approve or revise |
| Evaluation split | Chronological tuning/final holdout, fixed before comparing candidates | Approve or revise |
| Retention | Configurable, independently disableable; 30 days remains a proposal | Approve or revise |
| Automation | Human approval for all production changes; no automatic weight promotion | Approve or revise |
| MCP | Optional, outside core; may be deferred | Approve, defer, or reject |

MER-0.3 design can be accepted once these decisions and the experiment-record requirements are approved. It does not authorize production learning, ML, provider integration, or a production forecast algorithm. MER-1 begins only after the required MER-0.1 and MER-0.2 gates have also been approved.
