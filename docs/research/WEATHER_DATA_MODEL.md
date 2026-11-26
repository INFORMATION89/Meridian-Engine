# Meridian weather data model

**Status:** domain-model proposal for owner and milestone review  
**Prepared:** 2026-11-25  
**Scope:** semantics and invariants; no Kotlin classes, database schema, or production API are approved by this document.

## 1. Purpose

Meridian must preserve what a weather value means, where it came from, when it applies, how it was obtained, and how trustworthy it is. A number without these details is not a complete weather result.

The data model must support different consumers without forcing them to understand provider-specific JSON, database records, HTTP clients, or UI conventions. It must also make later verification possible: the engine must be able to distinguish what a model predicted at issue time from what a station subsequently measured.

This proposal treats the following as separate concepts:

- observation;
- model forecast;
- blended forecast;
- derived value;
- corrected forecast;
- climatology or reanalysis;
- official alert.

Do not collapse them into a generic current-weather value.

## 2. Core record concepts

A normalized weather value should conceptually carry the following information. Exact type names and serialization formats belong to MER-0.2.

| Concept | Meaning | Required behaviour |
|---|---|---|
| Variable identifier | Stable Meridian identity for the quantity | Independent of provider field names |
| Value state | Numeric/text value, missing, unavailable, invalid, or suppressed | Never substitute zero for missing |
| Unit | Machine-readable canonical unit | Never infer a unit from magnitude |
| Temporal meaning | Instant, interval, accumulation, average, maximum, minimum, or other statistic | Preserve provider's interval semantics |
| Valid time | When the weather value applies | Use UTC instants; retain local calendar context separately |
| Issue/run time | When a forecast was produced | Do not substitute fetch time if unknown |
| Observation time | When a measurement was made | Preserve independently of fetch time |
| Fetch time | When Meridian retrieved the record | Useful for freshness and debugging |
| Origin kind | Observation, forecast, blend, derived, correction, climatology/reanalysis | Never present a model estimate as an observation |
| Source identity | Provider, authority, station or model/product identifiers | Keep provider DTOs internal |
| Spatial context | Requested coordinate and, where available, source grid/station coordinate and elevation | Preserve offsets and resolution |
| Measurement context | Sensor/measurement height, depth, exposure, or averaging period when available | Do not compare unlike measurement contexts without a rule |
| Quality | Validity/quality state and relevant flags | Independent of freshness |
| Freshness | Fresh, stale-but-usable, expired, or unavailable | Independent of data quality |
| Uncertainty | Spread, interval, distribution, or calibrated probability when available | Do not invent confidence |
| Provenance | Inputs and transformations that produced the value | Preserve lineage through every combination |
| Method metadata | Algorithm/version or provider-defined method | Important for derived variables such as apparent temperature |

Some fields will not be available from every provider. An unknown issue time, station elevation, or quality flag must remain explicitly unknown; it must not be fabricated.

## 3. Value origin and lifecycle

### Observation

A value measured by an instrument or accepted observation system.

Minimum useful context:

- station or sensor identity;
- observation/measurement time;
- station coordinates and elevation when published;
- variable and unit;
- sensor height or measurement depth where relevant;
- reported quality/control flags;
- fetch time;
- raw source report or a traceable reference when retention terms permit it.

An observation can be stale, suspect, or invalid. It does not become a forecast merely because it is the latest available value.

### Model forecast

A value predicted by a numerical weather prediction model.

Preserve:

- provider and model/product ID;
- model initialization/issue time when published;
- fetch time;
- valid time and forecast lead;
- requested point and returned grid point/elevation where supplied;
- variable, unit, and temporal statistic;
- model-native missing values and forecast horizon.

Do not invent model initialization time from the time Meridian fetched the response. If the provider does not expose an issue time for a particular product, record it as unavailable and evaluate whether that product is suitable for historical verification.

### Blended forecast

A result calculated from two or more eligible forecast inputs.

It must preserve:

- the exact input value identifiers;
- each input model/source and its contribution/weight;
- the algorithm and configuration version;
- the set of inputs missing or excluded for that time/variable;
- any normalization or unit conversion;
- a measure of input disagreement when meaningful.

A blend is a new derived result. It must not overwrite the original model forecasts.

### Derived value

A deterministic transformation from one or more input values, such as a weather-condition category derived from physical variables or a vector wind result derived from components.

Record the transformation name/version, input lineage, assumptions, and any thresholds. Do not imply that a derived category was directly observed.

### Corrected forecast

A model forecast transformed using verified bias or another explicitly approved correction.

Preserve both the uncorrected and corrected values, correction amount, source observations/statistics, applicable lead-time/region/variable, and algorithm/configuration version. A correction is not a new provider forecast.

### Climatology and reanalysis

These are reference products, not operational observations or an issued forecast. Their product type, time period, grid, and role must remain explicit. A reanalysis value must not be substituted for an observation when evaluating an issued forecast unless the experiment expressly defines that reference and labels the limitation.

## 4. Time semantics

Use UTC for instants inside the engine. Preserve the location's IANA time-zone identifier for local-day grouping and consumer calendar interpretation.

Keep these times distinct:

- **issuedAt/runAt:** when a forecast model run or source product was issued/initialized;
- **validAt:** the instant a value describes;
- **intervalStart/intervalEnd:** the period covered by an accumulation or statistic;
- **observedAt:** when an observation was measured;
- **fetchedAt:** when Meridian retrieved it;
- **effective/onset/expires:** alert lifecycle times, kept according to CAP semantics.

Never treat a fetch timestamp as the forecast issue time or observation time.

### Instants versus periods

Temperature at a timestamp may represent an instantaneous or sampled value. Precipitation may represent an amount accumulated over the preceding hour, following hour, or another provider-defined window. Wind gust may represent a maximum over a period; daily precipitation is a sum; daily temperature is usually an extreme or mean.

The normalized record must say which kind of temporal statistic it represents. The adapter must consult provider documentation and fixtures to map interval boundaries correctly. Do not assume that every value labelled with hour H covers the same interval.

A proposed internal interval convention is start-inclusive/end-exclusive, but it must be confirmed against every source's mapping and written into the approved contract.

### Local dates and daylight saving

Daily forecast values are calendar-day values in the requested place's IANA time zone. They are not necessarily fixed 24-hour UTC intervals. Handle daylight-saving transitions and locations whose UTC offset changes. Do not calculate daily values by adding 86,400 seconds to a local midnight.

## 5. Units and quantity identity

Store numeric quantities in stable canonical units. Keep provider-native values/units in adapter diagnostics or provenance only where necessary for auditability.

| Quantity | Suggested canonical unit | Semantic notes |
|---|---|---|
| Air temperature at 2 m | °C | Keep measurement/forecast height in metadata |
| Apparent temperature | °C | Always retain method/source; see section 6 |
| Dew point | °C | Distinct from air temperature |
| Relative humidity | Fraction 0–1 | Consumer may display percent |
| Mean sea-level pressure | Pa | Must be distinct from station/surface pressure |
| Surface pressure | Pa | Elevation and model-grid context matter |
| Wind speed | m/s | Preserve measurement height and averaging period |
| Wind gust | m/s | Preserve maximum/interval semantics |
| Wind direction | Degrees | Clockwise from true north and meteorological “from” direction when that is the source convention |
| Wind vector components | m/s | Use components for interpolation and averaging |
| Precipitation amount | mm | Must identify accumulation interval and precipitation type |
| Rain/snow rate or intensity | mm/h or an explicitly documented equivalent | Distinguish instantaneous rate from interval accumulation |
| Snow depth | m | Depth on the ground at an instant; never confuse with snowfall accumulated during an interval |
| Snowfall water equivalent | mm | Liquid-water equivalent, distinct from snow depth and snowfall depth |
| Precipitation probability | Fraction 0–1 | Must identify event threshold, time window, and probability method |
| Cloud cover | Fraction 0–1 | Keep source layer/height if provided |
| Visibility | m | Record source convention and lower/upper bounds such as “10+” |
| UV index | Unitless index | Preserve source/time statistic |
| Solar irradiance | W/m² | Instantaneous or averaged interval must be explicit |
| Solar energy/radiation accumulation | J/m² or documented energy unit | Preserve interval |
| Pollutant concentration | Unit per pollutant, e.g. µg/m³ | Do not collapse unlike pollutants into one field |
| Air-quality index | Named index and scale | Preserve methodology/version and jurisdiction |

These are recommended canonical units, not a mandate to expose unit strings in the public API. The API should use typed unit/quantity identifiers or an equivalent closed, documented representation. Do not accept arbitrary free-text unit names at the core boundary.

Variable identity must include more than a short label. Pressure at sea level and surface pressure are different quantities. Precipitation accumulation over 1 hour and 24 hours is not the same quantity. Wind at 10 m and wind at 80 m is not interchangeable.

## 6. Apparent temperature and perceived conditions

Apparent temperature should be a first-class weather variable, not an afterthought. It is practically meaningful to consumers, but it is not a universal direct measurement and is not calculated identically by every provider.

Requirements:

1. Preserve measured/forecast air temperature separately.
2. Preserve the provider's apparent-temperature value as a separate quantity when available.
3. Record the provider and calculation method when documented.
4. Do not silently replace one provider's formula with another and claim the values are identical.
5. Do not describe apparent temperature as a personal measurement. It is an estimate under a defined method and input set.
6. Do not create a generic “feels like” value by averaging provider values from different methods.
7. Keep wind chill, heat index, and other named indices distinguishable if they are exposed separately.
8. If Meridian later derives an apparent-temperature value, version the formula, document its applicable conditions and inputs, test boundaries, and compare it against provider products before using it in production.

A consumer application may make apparent temperature prominent in its presentation while keeping actual air temperature visible alongside it. Presentation priority does not change the data semantics.

## 7. Precipitation semantics

Never treat the following as interchangeable:

- precipitation amount;
- rain amount;
- snow amount or snowfall depth;
- rain rate/intensity;
- probability of a defined precipitation event;
- observed precipitation over an interval.

A probability must identify:

- event definition (for example, measurable precipitation at or above a specified amount);
- accumulation/valid-time window;
- spatial interpretation (point, grid cell, or area);
- method (ensemble member fraction, calibrated statistical probability, provider post-processing, or other documented source method);
- source/model and calibration status where available.

The fraction of deterministic models exceeding a threshold is an inter-model agreement statistic. It is not automatically a calibrated probability of rain. Keep it separate unless historical verification demonstrates that it is a defensible probability estimate for the declared event.

## 8. Quality, freshness, and availability are separate dimensions

Recommended independent dimensions:

### Availability/value state

- present numeric/text value;
- not provided by source;
- unavailable for requested place/time;
- invalid/unparseable;
- suppressed by a source policy.

### Quality state

- valid;
- suspect;
- invalid;
- source quality unknown.

The final enum and flags need to be chosen in the contract. Never turn a suspect or invalid observation into a normal-looking value.

### Freshness state

- fresh;
- stale but explicitly usable under the request policy;
- expired and not usable for ordinary results;
- unavailable.

Freshness must be calculated using the source's update cadence, response headers/expiry, issue/measurement time, and the feature's policy. A recently fetched old forecast is still old forecast data.

### Confidence and uncertainty

Do not conflate provider freshness, data quality, model agreement, and calibrated forecast probability. A fresh result can be uncertain; a stale result can still be physically plausible; a narrow model spread does not prove calibration.

Only expose confidence labels if their definition, inputs, thresholds, and validation have been documented and approved.

## 9. Provenance and transformation lineage

Every returned value should be traceable to:

- provider/operator and source ID;
- model, product, station, or authority ID;
- source run/issue time and fetch time where known;
- returned grid/station location and elevation where relevant;
- source-native unit and Meridian conversion;
- source-native resolution and any resampling/interpolation;
- transformations and corrections;
- blend contributors and weights;
- fallback and exclusion reasons;
- attribution text/link and licence reference.

Provenance must follow values through transformations. If a blend uses three models and one is missing for a later hour, the output must identify the three contributing models for the first hour and the actual contributors for the later hour.

The provenance record should not be a giant copy of every provider response. Keep a minimal, versioned lineage record and optionally a traceable raw-response reference where provider terms and storage policy allow it.

## 10. Alerts are a separate domain

Official alerts are not forecast variables and must not be averaged, blended, or casually rewritten.

For CAP 1.2, preserve message identity and lifecycle fields such as sender, identifier, sent time, status, message type, references, scope, info blocks, urgency, severity, certainty, effective/onset/expiry, instructions, and affected areas. Updates and cancellations must be applied using their explicit message references. Do not collapse urgency, severity, and certainty into one score.

The issuing authority remains the source of the warning even when a WMO portal or FPAS carries a copy.

Reference: [OASIS Common Alerting Protocol 1.2](https://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.html).

## 11. Spatial and station context

A coordinate is not a complete representation of a place's microclimate. Preserve relevant context when available:

- requested coordinate and elevation;
- provider-returned model grid coordinate/elevation;
- station coordinate/elevation;
- station distance and elevation difference from the request;
- measurement height and exposure metadata;
- spatial resolution or grid-cell identifier;
- region/coverage metadata and any geographic fallback.

For privacy, do not turn request coordinates into a saved-place history. If a bounded cache groups nearby requests, define a documented spatial precision and make clear that the result belongs to a grid/rounded key, not an exact street-level observation.

Station representativeness is variable-specific. Distance alone is not enough: a nearby airport may be less representative than a slightly more distant station with similar elevation and exposure. The final selection rules must be tested for each target region and variable.

## 12. Invariants for implementation and tests

1. A missing value is never zero.
2. Observation, forecast, blend, correction, reanalysis, and climatology remain distinguishable.
3. Issue time, valid time, observation time, and fetch time remain separate.
4. Every amount or statistical value declares its time interval and aggregation.
5. Unit conversion is explicit, tested, and reversible within expected precision.
6. Direction is not averaged as an ordinary scalar angle; use vector components where appropriate.
7. Every combined or corrected value retains its input lineage.
8. Freshness and quality are not the same flag.
9. A provider's apparent-temperature value retains its method/source and never overwrites air temperature.
10. Precipitation probability is tied to a defined event and method.
11. Unsupported or unknown source fields remain unknown, not inferred.
12. Local-day grouping uses the place's IANA time zone, including daylight-saving transitions.
13. Alerts preserve issuing-authority content and lifecycle semantics.
14. Storage and diagnostics do not silently become location tracking or telemetry.

## 13. Standards and source references

These references guide semantics; they do not replace the provider-specific mapping and validation required for each adapter.

- [BIPM — The International System of Units (SI), 9th edition, version 4.01](https://www.bipm.org/en/si-brochure-9): authoritative reference for SI units and quantity notation. Meridian should store typed quantity/unit identifiers and use explicit conversions rather than guessing from magnitudes.
- [IANA Time Zone Database](https://www.iana.org/time-zones): rules for location-based civil time and daylight-saving transitions. Time-zone rules change; use the platform/runtime tz database and test relevant changes rather than hard-coding UTC offsets.
- [WMO Guide to Instruments and Methods of Observation (WMO-No. 8)](https://community.wmo.int/site/knowledge-hub/programmes-and-initiatives/instruments-and-methods-of-observation-programme-imop/guide-instruments-and-methods-of-observation-wmo-no-8-0): reference for measurement methods, instrumentation, and observing practices. The provisional 2026 edition was published for submission to INFCOM-4 on 23–27 November 2026; re-check its approval status before treating it as the approved edition.
- [OASIS Common Alerting Protocol 1.2](https://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.html): normative alert message identity, lifecycle, and information semantics.
- [Open-Meteo Forecast API documentation](https://open-meteo.com/en/docs): provider-specific variable names, units, interval semantics, and availability; use only for Open-Meteo adapter mappings, not as a universal weather-domain standard.

## 14. Questions to resolve in MER-0.2 / later specifications

- Exact typed unit representation and public serialization.
- Whether a public value has one record type or a sealed family for instant and interval statistics.
- Exact missing/invalid/suspect enums and compatibility rules.
- Whether raw provider-response references are retained and for how long.
- Exact coordinate precision for cache/history keys.
- Which temporal statistic types are required for the first release.
- Whether source-specific method metadata for apparent temperature is always public or carried through a structured explanation field.
- Which probability/event definitions are supported in the first public release.
- Which alert fields are mandatory for the consumer-facing API and which remain source-preserved extensions.

No decision in this list is approved by this proposal.