# Meridian M7: local verification and evidence-based learning

**Status:** historical method proposal (2026-11-24), not an approved learning algorithm or automatic adaptation policy. Root `PLAN.md` defines the M7 Level 2 regional-intelligence gate. The methods below are candidates for evaluation only; no correction or adaptive weight is activated merely because data has accumulated.

## Scope

Use verified forecast/observation pairs from explicit consumer requests to calculate local skill statistics where the source, station quality, and spatial/temporal matching are adequate. The consumer owns the saved-place list and refresh schedule; Meridian must not track places in the background. Store only the minimum spatial context needed for verification. Keep the initial recent learning/verification window bounded and configurable at 30 days; this does not set retention for cache, historical archives, or climatological records. All data remains local by default.

## What gets logged

- **Forecasts:** on each refresh, for each model and variable, the values for lead times in the
  buckets that will be verified (configuration; default the hours at +1, +3, +6, +12, +24, +48, +72
  and then daily to +7 days), with issue time. Not every hour of every refresh: one row per model,
  variable, valid hour and lead bucket, the latest issue winning.
- **Observations:** from M4's quality-controlled, variable-specific representative observation selection; nearest airport is not automatically acceptable.
- **Variables at first:** temperature and dew point (corrected); rain (verified only, Brier score,
  never corrected at first).

## Matching

A forecast for hour H is verified only against an observation that meets the approved quality, measurement-time, spatial representativeness, elevation, and matching-tolerance criteria for that variable. The ±30-minute tolerance in this draft is a candidate to validate, not a universal default. Missing or rejected observations produce no error sample; they are never filled or treated as zero.

## Statistics

Kept per the privacy-safe spatial key approved in M0.2, model, variable, lead bucket and any justified temporal bucket. The precise spatial resolution and keying strategy remain an M0.2 contract decision; do not create a saved-place history.

- `bias`: decaying average of error (forecast − observation):
  `bias ← (1 − w) · bias + w · error`, with `w = max(w_min, 1 / n)` so the first values are a plain
  running mean and later ones decay. `w_min` from configuration, NCEP's 0.02 as the starting value,
  tuned on the Mexico City archive.
- `rmse` over the rolling 30 days, for M5's weights.
- `count`: number of verified pairs. Fewer than the minimum (configuration, default 10) means no
  correction for that bucket.

## Correction

- Applied to each model's value before blending: `value − bias`, for corrected variables only.
- Fades out with lead time (ENGINE_SPEC.md section 8): full up to 48 h, then linearly to zero at 7
  days (both from configuration).
- Never pushes a value outside what the models span by more than the bias itself (a guard against a
  bad station).
- Expose the correction method, amount, supporting sample/window, and algorithm/configuration version in result provenance. The consuming app decides how to present the explanation and provide a supported opt-out control.

## Optional historical initialization (research candidate, not an automatic background task)

The earlier proposal to fetch 30 days of archive data automatically when a place is added is not adopted. A future M7 experiment may use a suitable historical forecast archive and matched observations only after verifying access terms, licence, attribution, product/run semantics, regional coverage, request cost, and data quality. The host must explicitly authorize any potentially large download. Do not substitute stitched historical series or reanalysis for archived issued runs without documenting the change in research question and methodology.

## Skill weights

M5's phase 2 reads `rmse` per model, variable and lead bucket from these statistics. Weights and the
correction switch on separately, each only after its own archive check.

## Storage

M2's `StatRow` holds one row per statistic key. Recent local learning/verification records older than the configured rolling window (initially 30 days) are deleted or compacted according to the approved M0.2/M7 storage contract. This does not apply to separately licensed historical/climatological datasets. The host must expose appropriate deletion/clear controls and Meridian must not own the privacy/settings UI.

## Tests

- Decaying average: hand-worked sequence; `1/n` warm-up then `w_min`.
- Matching: a report 40 minutes off isn't used; a missing hour adds nothing.
- Fade: full at 24 h, half at the midpoint, zero at 7 days.
- Guard: a station 5° off for a day doesn't move the forecast past the models' range plus the bias.
- Archive check: on the Mexico City rainy-season archive, corrected temperature MAE is lower than
  uncorrected for 0–24 h and 24–72 h; Brier score for rain unchanged (not corrected).

## Done when

Any enabled correction or weighting method demonstrates repeatable improvement on the approved independent regional/variable/lead-time evaluation without unacceptable regressions. Results and correction provenance remain available to consumers; no automatic promotion occurs without the M7 acceptance gate and owner review of the scientific method.

## Consulted

MERIDIAN_PLAN.md section 6 and the papers it cites (NCEP decaying average, inverse-RMSE blending,
Kalman filter study for later); Open-Meteo's Previous Runs API page; aviationweather.gov's Data API
page (30-day history).
