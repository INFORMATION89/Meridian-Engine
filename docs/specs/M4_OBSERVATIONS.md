# Meridian M4: observations and station representativeness

**Status:** historical provider-specific design proposal (2026-11-23), based on aviationweather.gov API documentation and published METAR standards. It does not approve a provider or fixed station-selection thresholds. Re-check provider schema, terms, coverage, update cadence, quality flags, and history availability before implementation.

## Scope

Find candidate observing stations for an explicit consumer request, assess the quality and spatial/temporal representativeness of their measurements, and use an observation only when it meets the variable-specific criteria. Airports are one observation source type, not a universal proxy for local conditions. Verification/learning use remains gated by M7.

## Stations

- The aviationweather.gov station-info endpoint is one researched candidate for station metadata. Query it only in response to host-controlled operations; the engine must not infer app saved-place lifecycle or start background place tracking. Re-check endpoint version, coverage, refresh cadence, and terms before use. Keep only station identity and the minimum metadata needed for quality/representativeness.
- For a requested point, gather a bounded set of candidates by geography, then evaluate each measurement by source quality, age, distance, elevation, exposure, terrain/urban/coastal context, and variable. The 30 km search radius and 300 m elevation difference in this draft are candidate parameters only, not universal thresholds; validate by region and variable.

## Request

`GET https://aviationweather.gov/api/data/metar?ids=MMMX,MMSM&format=json&hours=3`

- Only the chosen ids; a custom User-Agent with Meridian's name and a contact link (the API page asks
  for one so valid traffic isn't blocked).
- Parameters are only `ids`, `format`, `hours`, `bbox`, `date`, `taf` (documented in the OpenAPI
  file). **Undocumented parameters cause an error**, and going over the limits blocks access, so the
  adapter sends nothing else.
- `hours` from configuration (3 gives the last few reports, for trend and gaps). Most endpoints return
  at most 400 entries.
- The provider's historical endpoint/retention information is a dated research note. Re-check current availability, terms, update behaviour, and request accounting before using it for M7 verification.

## Mapping

From the official JSON schema (`aviationweather.gov/data/schema/metar1_0.json`, read 2026-11-24). The
response is an array of reports. Record a real MMMX response on Saturday as the fixture (it couldn't
be fetched from here) and check it against this table.

| Field (type) | Meridian | Unit change |
|---|---|---|
| `icaoId` (string) | `station` | |
| `obsTime` (integer, Unix seconds) | `time` (UTC) | |
| `metarType` (METAR, SPECI, …) | `special` when SPECI | |
| `temp`, `dewp` (number, °C; `dewp` nullable) | `temperature`, `dewPoint` | kept |
| `wdir` (integer degrees, or the string `"VRB"`) | `windDirection` | `VRB` → direction unknown |
| `wspd`, `wgst` (integer knots, nullable) | `windSpeed`, `windGust` | × 0.514444 → m/s |
| `visib` (number in statute miles, the string `"10+"`, or null) | `visibility` | × 1609.344 → m; `"10+"` → at least 16,093 m |
| `slp` (hPa, nullable), else `altim` (hPa) | `pressureSeaLevel` | kept (both already hPa) |
| `presTend` (hPa over 3 h, nullable) | `pressureTrend` | kept |
| `clouds` (list of `cover`: CLR, CAVOK, FEW, SCT, BKN, OVC, OVX; `base` in feet) | `cloudCover` (oktas), `cloudBase` | FEW 2, SCT 4, BKN 6, OVC/OVX 8; feet × 0.3048 → m |
| `wxString` (string, nullable) | `condition` | table below |
| `precip` (inches, last hour, nullable) | `precipitationLastHour` | × 25.4 → mm |
| `qcField` (bit flags) | `quality` | kept; a report flagged as sensor-off for a value drops that value |
| `rawOb` (string) | `raw` | kept for "nearby observations" (SOURCES.md) |
| `lat`, `lon`, `elev` (m) | station position | kept |

The schema lists every field as required but many as nullable: a missing value is `null`, never zero.

Present weather to condition (first match wins, intensity kept): `TS` → thunderstorm; `SN`, `SG` →
snow; `PL`, `GR`, `GS` → hail/ice; `FZ` → freezing; `RA`, `SH` → rain or showers; `DZ` → drizzle;
`FG` → fog; `BR` → mist; `HZ`, `FU` → haze/smoke; `DU`, `SA` → dust. No weather: from cloud cover
(clear, partly, mostly, overcast). The table is data, not code.

Missing values stay missing; never filled from the forecast.

## The current conditions rule

Current conditions may use an observation only when the documented, variable-specific quality and representativeness criteria are met. The following values are initial candidates for evaluation, not universal meteorological truths:
- the report is younger than 75 minutes (reports are normally hourly, plus specials);
- the station is within 15 km and within 300 m of the place's elevation;
- the value isn't missing and passes a sanity check (temperature −90 to 60 °C, a jump of more than
  10 °C in an hour flagged).

Otherwise the forecast's current values. Per variable: temperature can come from the airport while
the sky condition comes from the forecast. Provenance says which (M2). Night-time conditions (clear
night, not sunny) follow the place's sun times.

## Tests

- Fixture for MMMX (and one with `VRB`, gusts, missing temperature, a special report, `CAVOK`).
- Unit changes against hand-worked values.
- Rule: a 2-hour-old report isn't used; a station 40 km away isn't; mixed per variable works.
- Station picker: choose among eligible candidates using quality and variable-specific representativeness, not distance alone; material elevation or exposure differences must be reflected in the selection/rejection rationale.

## Done when

For the Mexico City fixture, a fresh MMMX METAR may be selected when it passes the approved quality and representativeness criteria; the nearest airport is not guaranteed to be the correct source for every CDMX point or variable.

## Consulted

aviationweather.gov Data API page (endpoints, limits, history, cache files), its OpenAPI file (version
3.12) and its METAR JSON schema (`metar1_0.json`); WMO Manual on Codes
(FM 15 METAR) and ICAO Annex 3 for the report fields; the standard atmosphere lapse rate.
