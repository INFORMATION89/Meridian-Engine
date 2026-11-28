# M0.3 — Forecast and observation verification foundation

**Status:** Draft specification for review; not implementation authorization.  
**Parent plan:** `PLAN.md`, milestone M0.3 and scientific safeguards.  
**Depends on:** M0.2 public API/storage decisions for the data contracts and retention boundaries.  
**Purpose:** make forecast-quality claims reproducible and scientifically defensible before Meridian introduces adaptive weights, bias corrections, local learning, or production forecast improvements.

## 1. Objective

M0.3 establishes an offline, versioned verification foundation that can answer:

- What exactly did a provider/model forecast, and when was that forecast issued?
- Which independent observation is suitable for verifying a particular forecast value?
- What matching, quality-control, and exclusion rules were applied?
- How does a forecast compare with a relevant baseline on the same cases?
- How much evidence supports the reported result, and where does it not generalize?
- Can another reviewer reproduce the result from immutable data, code, rules, and parameters?

The output is a reproducible evaluation capability and a baseline report. It is not a new forecast algorithm and does not change weather results returned by Meridian.

M0.3 must work without an AI provider, live weather request, hosted service, or paid infrastructure. AI may assist contributor research only under `AI_POLICY.md`; AI output is not scientific evidence.

## 2. Scope and exclusions

### In scope

1. A versioned archive representation for issued forecast snapshots.
2. A versioned observation representation preserving source, measurement time, units, quality, and representativeness.
3. Deterministic forecast-to-observation matching rules.
4. Explicit quality-control and exclusion records.
5. Chronological dataset splits and data-leakage safeguards.
6. Baseline selection and task-appropriate verification metrics.
7. Reproducible reports with sample counts, exclusions, uncertainty, and stratified results.
8. Dataset rights, attribution, provenance, retention, and integrity manifests.
9. Offline fixtures and a bounded, legally usable evaluation sample.

### Not in scope

- Live production provider adapters or user-facing forecast requests.
- Multi-model blending, adaptive weights, calibration changes, or bias correction.
- Training or promotion of machine-learning models.
- Local learning implementation or automatic algorithm updates.
- Production forecast persistence schema or an implementation that bypasses M0.2.
- Radar, nowcasting, official-alert ingestion, consumer UI, telemetry, or hosted collection.
- Claiming that a method improves forecast skill before the evaluation gate is met.

M0.3 may define formats and local evaluation tools. Any reusable production archive/storage contract must remain consistent with the M0.2-approved storage boundary.

## 3. Data integrity model

Forecasts and observations are different records with different temporal meaning. Never overwrite one with the other or use a corrected/reanalysis series as if it were the forecast that a consumer originally received.

### 3.1 Issued forecast snapshots

An archived forecast record must preserve, where supplied:

- Provider/operator, model, model version, product, and source version.
- Forecast run/issue identifier and issue time.
- Retrieval/fetch time and archive-ingestion time.
- Variable, value or probability distribution, unit, and native temporal resolution.
- Valid instant or explicit accumulation interval.
- Forecast lead time, derived from issue time to valid time using the declared temporal semantics.
- Grid cell, point reference, geometry, location/elevation context, and spatial resolution.
- Original source payload or a lossless source reference when licensing and storage rules permit it.
- Normalization version and any transformations needed to reproduce the archived value.

The archive is append-only for a given issued product. A later provider correction, reprocessing, revised model version, or reanalysis must create a distinguishable record/version; it must not silently replace the original issue snapshot.

A reforecast/hindcast is a distinct dataset with its own model version, initialization, and production process. Do not treat it as an operational forecast snapshot unless the evaluation explicitly studies that reforecast product.

### 3.2 Observation records

An observation record must preserve, where available:

- Observing source/operator, station or platform identifier, and dataset version.
- Measurement/observation time and fetch/availability time.
- Variable, value, unit, and native measurement or accumulation interval.
- Coordinates, elevation, station type, sensor/instrument metadata, and relevant siting metadata.
- Original source quality flags and the QC version applied by Meridian.
- Missing/trace/estimated/suspect states without silently converting them to numeric zero.
- Source attribution, licence, and the permitted retention/redistribution scope.

Do not collect more location precision or station metadata than is needed for verification. Do not include consumer saved places or private user location histories.

### 3.3 Immutable raw inputs and normalized records

Maintain a distinction between:

1. Source-native forecast/observation inputs.
2. Normalized records used for matching.
3. QC and matching decisions.
4. Evaluation samples.
5. Metrics and reports.

Each transformation must identify its code/rule version. Keep the source input immutable when retention/licensing allows. If raw redistribution is prohibited, retain an allowed reference, digest, metadata manifest, and enough permitted material to reproduce the evaluation without violating the source terms.

## 4. Quality control and matching

Forecast verification is only meaningful when the comparison record is appropriate. A nearby observation is not automatically representative of a grid forecast or another station.

### 4.1 Observation quality controls

Record, rather than silently discard, quality decisions. Candidate checks include:

- Original source quality flags and missing-value codes.
- Unit, encoding, and timestamp validity.
- Duplicate/conflicting records and station identity changes.
- Sensor/platform changes, known calibration issues, and metadata discontinuities.
- Physically implausible values and invalid temporal sequences.
- Suspicious persistence, abrupt discontinuities, and consistency with related measurements.
- Station representativeness, elevation difference, terrain/exposure, urban/rural context, and distance to the forecast reference.

Extreme weather is not invalid merely because it is rare. A QC rule must not systematically remove the severe events whose forecast skill Meridian needs to measure. Preserve the raw value and a machine-readable accepted/rejected/flagged reason for every decision.

### 4.2 Matching rules

A match must be deterministic and versioned. It must state:

- Forecast product/model and variable being verified.
- Forecast issue time, valid time, lead time, and any accumulation interval.
- Observation variable, measurement time, native interval, and quality status.
- Spatial method (for example, point-to-grid, station-to-grid, or a declared station aggregation).
- Temporal tolerance and spatial/elevation representativeness rules.
- Tie-breaking and duplicate resolution.
- Why each candidate observation was selected, rejected, or left unmatched.

Temporal and spatial tolerances must be named, unit-bearing configuration, justified for the variable and source, and tested at their boundaries. Do not introduce one unexplained global distance or time threshold for every variable.

For interval quantities such as precipitation, compare compatible accumulation windows. Do not match an hourly accumulation to an instantaneous measurement or silently redistribute accumulated precipitation across time steps.

A match is a verification association, not a claim that the station is the exact truth for every point in a model grid. The report must retain representativeness limitations.

## 5. Leakage safeguards and evaluation design

### 5.1 As-issued information boundary

A forecast may only be evaluated as the forecast that existed at its issue time. The archive must retain issue and fetch times so a reviewer can distinguish:

- information available to the model/consumer at issue time;
- information obtained later;
- observations used only for verification;
- subsequent provider revisions, reanalysis, and retrospective products.

Do not use later observations, revised forecasts, or future metadata as inputs to a historical forecast run. If a correction or selection method is eventually trained on historical data, its training inputs must be limited to information available before the evaluated forecast issue time.

### 5.2 Chronological splits

Use chronological training, tuning/validation, and final evaluation periods where method selection is involved. Do not randomly split autocorrelated weather time series and then report the result as an independent future test.

The split manifest must state:

- exact inclusive/exclusive date boundaries and time zone;
- region/stations/grid cells and variables covered;
- forecast horizons and accumulation windows;
- which period is used for baseline construction, tuning, and final evaluation;
- any purge/embargo applied to avoid overlapping target windows or leakage;
- how stations, products, and model versions are kept from unintentionally crossing a geographic or temporal holdout.

Freeze the final test set before tuning. Do not repeatedly inspect it to select parameters and still call it an independent test. If a final test set is reused after a method change, disclose that fact and create a new held-out period for a new independent claim.

### 5.3 Fair comparisons

Every baseline and candidate must be evaluated on the same matched cases, with the same observation QC, location/time matching, missing-data policy, lead-time slices, and exclusions. Report both absolute performance and relative skill against a relevant reference.

A candidate must be compared, as applicable, with:
- its unmodified input forecast;
- a simple persistence or climatological reference where appropriate;
- the best individual eligible source when evaluating a blend;
- the previous Meridian baseline when evaluating a subsequent Meridian change.

Do not compare scores computed on different stations, time windows, valid times, or missing-data subsets without explicitly showing the difference.

## 6. Metrics and reporting

Metric choice follows the variable, event definition, forecast type, and decision being evaluated. No one score is a universal ranking of forecast quality.

### 6.1 Continuous deterministic values

For continuous quantities such as air temperature, wind speed, and pressure, report at least:

- Mean absolute error (MAE).
- Root mean square error (RMSE).
- Mean error/bias.

A skill score relative to a declared reference may be included. Define its exact formula and direction. Correlation or anomaly metrics may supplement these measures but must not replace error magnitude and bias.

### 6.2 Probabilistic forecasts

Use probabilistic metrics only when the forecast actually supplies a probability distribution or a clearly defined event probability.

For an explicitly defined event, report Brier Score and reliability diagnostics such as reliability diagrams; ROC/ discrimination diagnostics may supplement them. For ensemble/distributional forecasts, evaluate appropriate distributional scores such as CRPS when the input distribution and observation support that interpretation.

Do not label the fraction of deterministic models exceeding a threshold as a calibrated probability. Keep model agreement, provider probability, ensemble probability, calibrated probability, uncertainty/spread, and qualitative confidence separate.

A calibration claim requires sufficient independent samples, reliability assessment, documented event/threshold definitions, and evaluation by region, season, and lead time where sample sizes support it.

### 6.3 Categorical events

For a defined event/threshold, report the contingency table and task-appropriate measures, such as probability of detection, false-alarm ratio, critical success index, and precision/recall. State the threshold, accumulation window, event matching, and base rate. Do not select or report only the score that favours the candidate.

### 6.4 Sample size, uncertainty, and stratification

Every metric must include its sample count and relevant denominators. Report missing cases, excluded observations, coverage, and the reasons for exclusions. Include uncertainty intervals or another justified uncertainty estimate when the sample supports it. Account for temporal/spatial dependence; do not automatically assume every station-hour is an independent sample.

Break down results, when sample size permits, by:

- variable and unit;
- forecast lead time and valid-time window;
- region/grid/station group and elevation/representativeness class;
- season/month and relevant weather regime;
- source/model/product/version;
- relevant event threshold or probability range.

A strong aggregate score must not conceal a material degradation in a region, variable, lead time, or severe-event category. Small samples must be labelled insufficient, not converted into confident rankings.

## 7. Reproducibility and artefact manifest

Each evaluation run must emit a machine-readable manifest and a human-readable report.

The manifest must identify:

- forecast and observation dataset IDs, versions, source URLs/operators, licence/terms, retrieval date, coverage, and checksums;
- raw and normalized input identities, or permitted immutable references;
- data conversion, QC, matching, and exclusion rule versions;
- train/validation/test boundaries and leakage/embargo policy;
- baseline and candidate identifiers, code commit, configuration, and parameters;
- metric definitions/implementation version, sample counts, and seeds for any randomized procedure;
- toolchain/runtime versions and the command/configuration used to reproduce the run.

A reviewer must be able to regenerate the same matched sample and numerically equivalent report offline from the versioned permitted inputs. If an upstream dataset changes, it creates a new dataset version and evaluation run; old reports remain traceable to the old version.

Reports should present:
- purpose and exact scope;
- data provenance, rights, coverage, and known quality limitations;
- matching/QC rules and exclusions;
- leakage controls and split dates;
- baseline definition and metric formulas;
- metrics and uncertainty by variable/lead/region/season;
- sample counts and coverage;
- improvements and regressions;
- limitations and conclusions that the evidence does **not** support.

No report may say "improved", "more accurate", or "calibrated" based only on plausible examples, an AI summary, an aggregate metric, or an unverified in-sample result.

## 8. Dataset admission and legal review

Before downloading or using an external archive, document:

- operator and authoritative documentation;
- data licence, terms of use, attribution, redistribution, derived-data, and retention requirements;
- geographic/temporal coverage, resolution, station/grid metadata, variables, quality flags, and known gaps;
- whether historical records represent actual operational forecast issues or a retrospective/reforecast product;
- how updates, corrections, missing values, and station changes are identified;
- transfer/storage size and permitted access method.

Do not assume a publicly downloadable dataset can be redistributed or embedded in Meridian's repository, tests, binary, or release package. Keep large or restricted datasets outside Git; use a small permitted fixture with its provenance/terms recorded. If terms are ambiguous, do not download or process beyond the permitted review and request legal/owner review.

Potential sources such as NOAA NCEI Integrated Surface Database may be assessed for station observations, but no source is selected by this specification. Re-check current availability, coverage in Mexico and target regions, quality/metadata, terms, and operational access before admission.

## 9. Bounded implementation sequence

After M0.3 specification approval:

1. Define engine-neutral verification record contracts consistent with M0.2-approved archive/storage boundaries.
2. Select and document a small legally usable regional dataset sample; do not build a general downloader.
3. Add immutable fixture/archive ingestion and manifests.
4. Implement observation QC and deterministic matching with boundary and rejection-reason tests.
5. Implement baseline metric calculations and a reproducible offline report.
6. Add leakage tests proving that post-issue information cannot enter a forecast's input set and that chronological held-out periods remain untouched during tuning.
7. Run a baseline evaluation and have an independent reviewer reproduce it.
8. Document what is not yet measured and keep all adaptive methods disabled.

Prefer a narrow initial validation in Mexico, with coverage and station representativeness disclosed. A reasonable first vertical slice is one continuous variable (for example, near-surface air temperature) and one event/accumulation variable (for example, precipitation) only where the archive and observation cadence genuinely support compatible matching. The final variable set must be decided from dataset rights and evidence, not forced to satisfy a checklist.

## 10. Acceptance criteria

M0.3 is complete only when:

- A legally usable, versioned sample of issued forecasts and observations exists with provenance and integrity manifest.
- Forecast snapshots preserve the original issue/valid/lead semantics and cannot be silently replaced by reanalysis or later revisions.
- Observation QC and matching are deterministic, versioned, and explain accepted/rejected/unmatched cases.
- Chronological splits, as-of issue-time rules, and leakage safeguards are implemented and tested.
- At least one appropriate baseline evaluation runs offline and reports the defined metrics, sample counts, exclusions, coverage, uncertainty, and stratified results.
- A separate reviewer can reproduce the baseline report from the documented inputs, code, and parameters.
- No production blending, adaptive weights, correction, local learning, or provider integration is promoted by the M0.3 work.
- AI is not required to ingest data, match records, calculate metrics, or reproduce reports.

Only after the M0.2 and M0.3 acceptance/approval gates can M1's first forecast provider path be authorized.

## 11. Sources consulted

- ECMWF Technical Memorandum 927, *Forecast verification using information and noise* (May 2025): discusses common verification scores, baseline comparisons, and why score changes can mix bias and uncertainty effects. https://www.ecmwf.int/en/elibrary/81662-forecast-verification-using-information-and-noise
- Bonavita and Geer, *Forecast verification using information and noise*, Quarterly Journal of the Royal Meteorological Society (first published 8 February 2026): discusses limitations in interpreting traditional scores across forecasts and post-processing methods. https://rmets.onlinelibrary.wiley.com/doi/10.1002/qj.70109
- ECMWF Forecast User Guide, *Using verification metrics with the output*: describes reliability diagrams, ROC diagnostics, and Brier Score interpretation for probabilistic forecasts. https://confluence.ecmwf.int/spaces/FUG/pages/673551584/Section+8.3.5+Using+verification+metrics+with+the+output
- NOAA NCEI, *Global Hourly — Integrated Surface Database*: describes the global hourly/synoptic station observation archive and its quality-control context. It is only a dataset candidate; its current cloud migration notices and regional coverage must be checked before use. https://www.ncei.noaa.gov/products/land-based-station/integrated-surface-database
