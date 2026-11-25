# Meridian weather intelligence strategy

**Status:** scientific and product-behaviour proposal for owner review  
**Prepared:** 2026-11-25  
**Purpose:** define how Meridian should interpret, compare, combine, and verify weather data without prematurely authorizing algorithms or provider code.

## 1. Product objective

Meridian should provide weather results that are useful because they are traceable, timely, and honest about uncertainty—not merely because they combine many APIs.

The engine should answer these questions for every requested feature:

1. What is the value?
2. Is it an observation, a forecast, a blend, or a derived/corrected value?
3. When does it apply, and when was it produced or measured?
4. Which provider, model, station, or authority supplied it?
5. How fresh and quality-controlled is it?
6. What uncertainty, spread, or limitations should a consumer preserve?
7. What happened when a source failed or could not cover the place?

Meridian must not hide uncertainty behind one polished number, silently substitute stale values, or claim scientific improvement without reproducible evidence.

## 2. Keep the milestone order

The current roadmap separates concerns sensibly. Preserve the boundaries:

| Milestone | Weather-intelligence responsibility | What it must not absorb early |
|---|---|---|
| MER-1 | One documented forecast path and normalized hourly/daily/current values | Blending, local learning, radar, production alerts |
| MER-2 | Per-value provenance, source eligibility, rate limits, cache/fallback semantics | Automatic skill weighting |
| MER-3 | Official alert ingestion and CAP lifecycle | Averaging or rewriting official alerts |
| MER-4 | Airport/station observations and variable-level observation selection | Assuming the nearest airport is always representative |
| MER-5 | Multiple model forecasts, blend, and spread | Adaptive skill weights before verified history exists |
| MER-6 | Independent consumer integration | Making Meridian depend on Solstice |
| MER-7 | Local forecast/observation matching, statistics, and any approved correction | Automatic/self-modifying production updates |
| MER-8 | Radar/satellite nowcasting feasibility and benchmark | Promising radar coverage before regional access is proven |

A specification can be researched in advance, but implementation remains gated by the approved milestone.

## 3. Current conditions: combine carefully

“Current conditions” can refer to different data types:

- a direct station observation;
- a model's current/analysis hour;
- a recent observation adjusted or interpolated to the requested point;
- a blended estimate.

They are not equivalent. Meridian should return the origin for each variable rather than select one station and treat its entire report as the current weather for a city.

Recommended approach for MER-4:

1. Discover candidate stations using geographic coverage and station metadata.
2. Reject candidates with unacceptable age, quality flags, missing variables, or incompatible measurement context.
3. Evaluate distance, elevation difference, exposure, terrain, urban/coastal context, and variable type.
4. Select a source independently per variable when justified.
5. If no acceptable observation exists, use an explicitly labelled model value or return unavailable according to the request policy.
6. Preserve the rejected-source reasons sufficiently for diagnostics.

The 15 km, 75-minute, and 300 m values in the existing MER-4 draft are candidate defaults, not universal meteorological truths. They need regional and variable-specific evidence. The draft's example that rejects a station at 40 km is consistent with a 15 km cutoff, but its two-hour example and any other inconsistent threshold language must be reconciled.

## 4. Apparent temperature and practical interpretation

Apparent temperature is a high-value user-facing metric and should be retained as a first-class value from the first forecast path when supported.

Scientific and product rules:

- Keep actual air temperature visible and independently available.
- Keep apparent temperature as a distinct provider-derived or Meridian-derived value.
- Preserve the source and method. Different providers may use different formulas or assumptions.
- Do not average unrelated apparent-temperature methods or silently replace one with another.
- Do not imply it is a personal measurement. It is an estimate of perceived conditions under the method's assumptions.
- Preserve the inputs that materially affect the method when available, such as humidity and wind.
- Heat index and wind chill should remain named quantities, not be hidden inside a generic field.
- If a provider has no apparent-temperature value, return it as unavailable or derive it only after a separate formula and validation decision.

This makes it possible for a consumer such as Solstice to emphasize “feels like” while still presenting the actual air temperature and source explanation.

## 5. Precipitation: amount, probability, and timing

Precipitation is a frequent source of misleading UI unless its semantics are explicit.

Keep separate:

- total precipitation amount;
- rain amount;
- showers;
- snowfall amount/depth;
- rain rate/intensity;
- precipitation probability;
- observed precipitation accumulation.

For each numeric amount, preserve the time interval it covers. “2 mm at 15:00” is ambiguous unless the record tells the consumer whether that is the hour ending at 15:00, beginning at 15:00, or another interval.

For probability, define the event before showing a percentage. At minimum, an event requires a threshold and a time window. The source method must also be retained.

Do not describe the share of deterministic models above a threshold as a calibrated probability without historical verification. It may be useful as inter-model agreement, but it answers a different question from a calibrated probability of rain. MER-5 should preserve individual model probabilities and ensemble data when available, and evaluate any derived probability separately.

Short-range rain on a 15-minute grid must declare whether it is native model output or interpolation from hourly values. Current Open-Meteo documentation says 15-minute data is limited to specified regions and interpolated from hourly output elsewhere; consumers must not mistake interpolation for true 15-minute forecast resolution.

## 6. Source selection and fallback

Source selection is per feature and location. A provider that is suitable for global temperature forecasts may not be suitable for official alerts or a nearby station observation.

Recommended eligibility and ranking dimensions:

1. Feature support and geographic coverage.
2. Provider terms and the consuming application's eligibility to use that endpoint.
3. Source/model/station quality and known limitations.
4. Freshness and update cadence.
5. Spatial and temporal resolution appropriate to the feature.
6. Availability and recent transport/parse health.
7. User/host configuration and credential availability.
8. Call cost, rate limits, and request budgets.
9. Attribution and licence requirements.

Do not use a single global “official > regional > global” rank for every feature. An official alert authority should outrank a generic aggregator for source authority; an appropriate station observation may be preferable for a measured value; a forecast model is not an observation at all.

Fallback behaviour:

- Never relabel a fallback as the primary source.
- Preserve the preferred source's failure/skip reason.
- Mark old cache data stale and preserve its original timestamps.
- Do not silently fall back to an unrelated variable, unit, or temporal interval.
- Do not retry continuously after rate limiting; respect provider backoff and retry guidance.
- If no source is eligible, return typed unavailability.

## 7. Forecast blending

The MER-5 proposal for equal initial weights is a defensible transparent baseline, provided the included models are actually available for the requested region, variable, and lead time.

Recommended invariants:

- Preserve every single-model forecast before computing the blend.
- Blend each variable and valid time using only eligible values available for that point.
- Renormalize the remaining weights when a model is legitimately missing, and record the actual contributors for that time.
- Do not treat missing data as zero.
- Average wind through vector components, not raw direction angles.
- Derive daily aggregates from the hourly blend only where the source temporal semantics permit it.
- Preserve model spread and ensemble spread separately.
- Publish spread and contributor count with the blend.
- Version the algorithm and configuration.

A blend beating one provider for one month or one city is not sufficient to claim general superiority. Evaluate it against the best individual model and a fixed baseline across relevant regions, variables, seasons, and lead times.

## 8. Forecast verification workflow

Use a reproducible sequence:

1. Select a permitted forecast archive and observation dataset.
2. Record service terms, data licence, archive endpoint, product/model, geographic coverage, and retrieval date.
3. Preserve forecasts as they were issued, including run/issue time and valid time.
4. Ingest observations with measurement time, station metadata, and quality flags.
5. Apply a deterministic, versioned matching rule.
6. Record matches and every exclusion reason.
7. Evaluate an unchanged baseline and candidate using metrics chosen before inspecting the candidate results.
8. Segment results by variable, lead time, region/station group, season, and relevant quality category.
9. Report sample counts, missing data, exclusions, and uncertainty alongside scores.
10. Keep tuning and final evaluation periods separate using chronological holdouts.
11. Have a human review the report and approve any proposed production change.

### Candidate metrics

| Task | Candidate metric | Notes |
|---|---|---|
| Continuous temperature/dew point/wind/pressure | MAE, RMSE, mean bias | Report by variable and lead time |
| Rain event probability | Brier score and reliability/calibration diagnostics | Only after event threshold/window are defined |
| Event detection | Precision/recall or categorical scores | Define event and sample population first |
| Model agreement | Range, standard deviation, contributor count | Not a calibrated probability by itself |
| Data availability | Coverage, freshness, missingness, successful-request rate | Keep operational quality separate from forecast skill |

A lower error score does not prove every region improved. Inspect segmented regressions and compare with each contributing source.

## 9. Archive choice and leakage controls

Forecast archives answer different questions:

- **Previous Runs:** values aligned to fixed lead-time offsets. Useful for aggregate skill by lead bucket; it does not reconstruct every complete individual model run.
- **Single Runs:** an individual model run identified by initialization time; suitable when a full run history is needed and the selected product's archive is accessible.
- **Historical Forecast:** a stitched historical time series. It is not equivalent to preserving a particular issued run.
- **Reanalysis:** a retrospective analysis product. It is not an issued forecast and must not be used as if it were one.

The report must identify which product it used. Do not substitute a stitched or reanalysis product for an issued forecast and then claim to have verified the original forecast.

Leakage rules:

- Observation time must be after the forecast issue time for ordinary forecast verification.
- Any data not available at issue time is excluded from the information set for that forecast.
- Thresholds and weights are selected on the tuning period only.
- Final evaluation is chronological and held out.
- The archive, matching rule, evaluation code, algorithm, and configuration are versioned.

Open-Meteo's [Previous Runs documentation](https://open-meteo.com/en/docs/previous-runs-api) describes its fixed lead-time offsets and directs users to Single Runs for complete individual runs. Its [Single Runs documentation](https://open-meteo.com/en/docs/single-runs-api) describes retrieval by model initialization time. Archive access and pricing must be checked separately before a large experiment.

## 10. Mexico-first evaluation strategy

Mexico is not one meteorological regime. A meaningful evaluation should include multiple environments instead of optimizing only one city.

Suggested initial test strata:

| Test region | Why it matters | Variables/scenarios to inspect |
|---|---|---|
| Mexico City / Valley of Mexico | High elevation, basin/terrain effects, urban heat, strong local gradients | Temperature, apparent temperature, pressure/elevation, convective rain, nearby station representativeness |
| Pacific and Gulf coasts | Marine influence, high humidity, sea breezes, tropical systems | Wind, humidity, precipitation, severe-weather alerts |
| Northern arid regions | Large diurnal temperature range and sparse station coverage | Temperature extremes, wind, precipitation probability/amount |
| Mountain corridors | Elevation and grid-cell mismatch | Temperature, precipitation phase, wind, surface pressure |
| Southern tropical regions | Deep convection and high rainfall variability | Precipitation timing/amount, thunderstorms, humidity |
| Yucatán / Caribbean-facing areas | Tropical-cyclone exposure and coastal effects | Official alerts, wind, rainfall, track-related context |

These are test strata, not a claim that Meridian currently has sufficient station coverage or source access in each region. Verify actual provider and station availability before selecting evaluation cases.

### Mexico City test fixture set

The first detailed geographic fixture set should include:

- a central urban coordinate;
- an elevated/foothill coordinate;
- a valley-edge coordinate;
- a point closer to a major airport station;
- a location with no acceptable nearby observation.

This exposes the difference between a model grid, airport observation, and requested point without assuming one source represents all of CDMX.

## 11. Alerts and official information

Alerts must remain a dedicated feature domain. For each alert preserve the issuing authority, source route, message identity, update/cancel links, severity, urgency, certainty, effective/onset/expiry, and affected area.

A portal or aggregator can transport an authority's alert, but it does not become the issuing authority. If WMO and FPAS carry the same message, deduplicate using CAP identity/reference semantics while preserving both delivery routes as provenance.

Never average severity or rewrite official instructions as if Meridian were the warning authority.

## 12. Learning and correction

MER-7 should start with descriptive, local, bounded statistics. Any later bias correction or skill weighting must be separately enabled, evaluated, and attributable.

Requirements before a correction ships:

- enough quality-controlled matched pairs for the relevant variable, region, and lead bucket;
- station representativeness validated;
- chronological evaluation against an unchanged baseline;
- protection against a few bad observations or station outages dominating the result;
- a fallback to the uncorrected forecast when sample size or quality is insufficient;
- visible correction provenance;
- user/consumer ability to disable the optional correction;
- bounded and independently deletable history;
- human approval for every algorithm or configuration change.

Do not introduce a learned model or adaptive weights because the project has data. Use the simplest method that demonstrates repeatable improvement.

## 13. Radar and nowcasting

Radar/satellite nowcasting remains research-only until model forecasts, observations, provenance, and source coverage work reliably.

A separate feasibility study should establish:

- licensed and technically accessible radar/satellite imagery;
- geographic coverage, latency, scan interval, and gaps;
- image georeferencing and calibration;
- precipitation-rate product availability;
- motion estimation and extrapolation method;
- deterministic benchmark against observed rainfall;
- graceful fallback outside coverage;
- compute, storage, and battery impact on consumer devices.

Do not advertise radar nowcasting for a region based only on the existence of a radar image on a public website.

## 14. Acceptance criteria for the intelligence layer

- Every value retains origin, time, unit, freshness, quality, and provenance.
- Apparent temperature remains separate from actual temperature and retains its method/source.
- Rain amount and event probability are distinct and time-bounded.
- Observation values are selected per variable using quality and representativeness, not distance alone.
- A model blend preserves inputs, contributors, weights, and spread.
- Verification reports identify data, code, parameters, exclusions, metrics, and sample counts.
- Tuning and final evaluation are separated chronologically.
- No algorithm is promoted automatically.
- Provider rights and costs are verified before collecting or distributing data.
- The core remains deterministic and has no runtime AI requirement.

## 15. Open decisions

- Which first-release variables are mandatory versus optional.
- Which apparent-temperature method(s) can be exposed consistently.
- Which precipitation probability event definitions are supported.
- Which Mexico regions and station networks form the initial evaluation set.
- What observation quality and station-matching thresholds are defensible per variable/region.
- Which archived products are accessible and affordable for repeatable verification.
- Which confidence/spread measures, if any, belong in the public API.
- What history retention and local learning are acceptable under the privacy/storage policy.

All remain proposals until approved in the relevant milestone.