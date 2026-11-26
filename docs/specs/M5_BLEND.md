# Meridian M5: blend and spread

**Status:** historical design proposal (2026-11-23), not an approved blending algorithm or provider/model list. Root `PLAN.md` requires a verified baseline, source-dependency/correlation awareness, and archive evaluation before production use. Re-check model availability, terms, request weights, and product semantics before implementation.

## Scope

Ask several models for the same place, blend them into one forecast, and say how much they disagree.
Equal weights first; skill weights come once M7's statistics exist (the switch is configuration).

## Models

- `GET https://api.open-meteo.com/v1/forecast` with `models=` a comma list from configuration. Starting
  list for Mexico City, checked against the model test (Saturday step 3) before it's final: ECMWF IFS,
  GFS, ICON global, GEM global, and best match as the reference.
- Each variable comes back per model (suffixed by model id). A model without a variable, or past its
  range, is simply absent for those hours; the blend uses whoever is there.
- Call weight grows with models × variables × days (M2 counts it). One request for all models.
- Ensemble spread may be obtained from a provider's documented ensemble product when available. Preserve the ensemble product, member population, event threshold/window, and method. A member fraction may be a source-defined event probability, but its reliability/calibration must not be assumed; keep it distinct from deterministic-model agreement and report calibration status.

## Blending

Per variable, per hour:

| Variable kind | Blend |
|---|---|
| Temperature, dew point, pressure, humidity | weighted mean |
| Wind | weighted mean of u and v components, then back to speed and direction; gusts weighted mean |
| Precipitation amount | weighted mean |
| Rain-event model agreement | The share of eligible deterministic models above a predeclared precipitation threshold; report this as **model agreement**, not calibrated rain probability. Keep provider-supplied probabilities and any separately calibrated probability distinct, with event threshold, time window, method, source, and calibration status. |
| Cloud cover | weighted mean |
| Weather code | from the blended values (rain amount, cloud, temperature for snow), not a vote between codes |

Weights:
- **Phase 1 (M5):** equal.
- **Phase 2 (after M7):** inverse RMSE over a rolling 30 days, per variable and lead-time bucket
  (0–24 h, 24–72 h, 3–7 days, beyond), normalised, with a floor (configuration, default 0.05) so no
  model drops out. Fewer than 14 days of data for a bucket: equal weights for that bucket.

Daily values are made from the blended hours (max, min, sums), never blended separately.

## Spread shown to people

- **Model spread:** the range (min to max) and standard deviation across models, per hour.
- **Ensemble spread:** as above, when available.
- `spread` may retain appropriate dispersion and range statistics with clear provenance. Do not turn spread alone into a verbal confidence label. Any consumer-facing confidence category needs a documented definition, variable/lead-specific evidence, validated thresholds, and uncertainty limitations; thresholds in this draft are research candidates only.

## Tests

- Fixture with four models: hand-worked blend for a few hours.
- Wind: two models at 350° and 10° blend to 0°, not 180°.
- A model missing hours 120+: the blend uses the rest, weights renormalise.
- Model agreement: 3 of 4 eligible deterministic models above the threshold gives 75% agreement; it must not be labelled a 75% probability of rain unless independent calibration verifies that interpretation for the defined event.
- Weather code: blended light rain and 90 % cloud gives a rain code even if two models say cloudy.
- Spread: identical models give zero; one outlier widens the range.

## Done when

The candidate blend is compared with each eligible individual model and the previous Meridian baseline using a versioned, licensed archive and a documented chronological holdout. Report gains and regressions by variable, region, season, and forecast lead, plus sample counts and uncertainty. Use Brier/calibration scores only for a defined probability event; deterministic-model agreement is not a probability score.

## Consulted

Open-Meteo forecast and ensemble API pages; the weighting and bias-correction papers named in
MERIDIAN_PLAN.md section 6; WMO guidance on ensemble spread communication.
