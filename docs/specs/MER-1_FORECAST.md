# Meridian MER-1: first forecast (Open-Meteo)

Status: **spec, clean room step 1** (2026-11-23), written only from Open-Meteo's public documentation.
The build session works from this file alone. Context: ENGINE_SPEC.md, MERIDIAN_PLAN.md section 9.

## Scope

Given a place, return hourly, daily and current weather from Open-Meteo's best-match forecast, as
Meridian's own types, with provenance. One source, one model, no blending (that's MER-5), no storage
beyond an in-memory cache (MER-2).

## Provider service, licensing, and operating constraints

**Checked against the provider's published documentation on 2026-11-25. Re-check these terms before launch; they can change.**

The API service terms and the data licence are separate:

- The no-key free Forecast API is restricted to non-commercial use. Open-Meteo lists private/non-profit apps without subscriptions or advertising as examples of non-commercial use. Apps with subscriptions, advertising, or commercial-product use require a commercial API plan. Do not assume an app qualifies solely because it is free to download.
- The free tier currently lists limits of fewer than 10,000 calls/day, 5,000/hour, 600/minute, and 300,000/month. Limits are applied by IP address and application. The free tier has no uptime guarantee.
- A commercial subscription uses the customer endpoint and API key. The host app must supply provider configuration; Meridian must not embed a private developer credential in its library. A key distributed in a client app must not be treated as a confidential secret. Confirm the provider's permitted key-distribution model before release; do not silently add a Meridian-hosted proxy to conceal a key.
- Requests with more than 10 weather variables or a range longer than two weeks for one location can count as multiple, fractional API calls. The MER-1 request currently selects more than 10 variables across its current/hourly/daily groups, so do not assume one HTTP request equals one API call. Estimate usage with the provider's current call-weight rules and retain a safety margin. A per-install counter is only an estimate because provider limits are also enforced by IP and application.
- API data is CC BY 4.0. Display the required Open-Meteo credit and link, and indicate modifications. The data licence permitting reuse does not override the free API's non-commercial service restriction.

Archive endpoints are not interchangeable:

- **Previous Runs API:** fixed forecast lead offsets (day 0 through day 7), intended for aggregate run-to-run skill analysis. Most models are archived from January 2024, with model-specific exceptions and shorter histories.
- **Single Runs API:** full individual model runs identified by initialization time. ECMWF IFS HRES coverage begins in March 2024; most other models begin on 2026-04-02.
- **Historical Forecast API:** a stitched, continuous archive of earlier forecast periods; it is not the same data shape as the Previous Runs API. Current pricing documentation says historical forecast access requires Professional or higher. Do not assume it is available through the free tier.

Before production use, record the selected service plan, commercial-use eligibility, key-distribution approach, rate-limit assumptions, expected monthly call volume, attribution implementation, and the exact archive endpoint/plan used for verification research. If those conditions are not approved, Open-Meteo remains a prototype/test source and must not be treated as a cleared production dependency.

## Request

`GET https://api.open-meteo.com/v1/forecast` with:

| Parameter | Value | Why |
|---|---|---|
| `latitude`, `longitude` | the place | WGS84 |
| `elevation` | the place's, when known | Otherwise Open-Meteo uses a 90 m elevation model; passing ours keeps temperature right in mountains |
| `hourly` | list below | |
| `daily` | list below | Needs `timezone` |
| `current` | list below | |
| `timezone` | the place's IANA zone | Daily values follow local days |
| `timeformat` | `unixtime` | `utc_offset_seconds` is fixed for the whole response and ignores daylight-saving changes, so local times are computed from the zone, not the offset |
| `forecast_days` | configuration (default 10, Outlook's span) | Allowed 0 to 16 |
| `past_days` | configuration (default 0) | Allowed 0 to 92 |
| `wind_speed_unit` | `ms` | Already SI |
| `temperature_unit`, `precipitation_unit` | defaults (`celsius`, `mm`) | |
| `models` | not sent (best match) | MER-5 adds explicit models |
| `cell_selection` | default (`land`) | |

No key for the non-commercial free endpoint; commercial use requires the provider's customer endpoint and key. A custom User-Agent naming Meridian and its repo. Do not embed private keys in the library.

**Hourly:** `temperature_2m`, `apparent_temperature`, `relative_humidity_2m`, `dew_point_2m`,
`pressure_msl`, `surface_pressure`, `cloud_cover`, `visibility`, `wind_speed_10m`, `wind_direction_10m`,
`wind_gusts_10m`, `precipitation`, `rain`, `showers`, `snowfall`, `precipitation_probability`,
`weather_code`, `uv_index`, `is_day`.

**Daily:** `weather_code`, `temperature_2m_max`, `temperature_2m_min`, `apparent_temperature_max`,
`apparent_temperature_min`, `sunrise`, `sunset`, `daylight_duration`, `uv_index_max`,
`precipitation_sum`, `precipitation_probability_max`, `wind_speed_10m_max`, `wind_gusts_10m_max`,
`wind_direction_10m_dominant`. (Names to confirm against the docs' daily table while building.)

**Current:** `temperature_2m`, `apparent_temperature`, `relative_humidity_2m`, `weather_code`,
`wind_speed_10m`, `wind_direction_10m`, `wind_gusts_10m`, `precipitation`, `is_day`.

Variable lists live in configuration, not code.

## Response

```
latitude, longitude        the grid cell used (can be a few km from the request)
elevation                  of that cell
utc_offset_seconds, timezone, timezone_abbreviation
current  { time, interval, <variable>: value }       interval in seconds (900 = 15 minutes)
hourly   { time: [...], <variable>: [...] }           arrays aligned to time
hourly_units, daily, daily_units, current_units
```

- Values can be `null` (not available for that hour or model): an absent value, never zero.
- Arrays may be shorter or longer than expected: align by `time`, never by index assumptions.

## Mapping to Meridian

**Units** (all checked against `*_units`; a unit we don't expect is a problem, not a guess):

| Open-Meteo | Unit | Meridian |
|---|---|---|
| temperatures, dew point | °C | °C (kept; the app converts) |
| relative humidity, cloud cover, precipitation probability | % | fraction 0 to 1 |
| pressure | hPa | Pa |
| wind speed, gusts | m/s | m/s |
| wind direction | ° | degrees, meteorological (from) |
| precipitation, rain, showers | mm (preceding hour) | mm over the hour **ending** at the timestamp |
| snowfall | cm (preceding hour) | m of snow over the hour ending at the timestamp |
| visibility | m | m |
| UV index | index | index |
| daylight duration | s | duration |
| sunrise, sunset | unixtime | instant; absent during polar day or night |

Note the sums and maxima (precipitation, gusts, radiation) describe the **preceding** hour; instant
values (temperature) describe the moment. Each Meridian variable records which kind it is.

**Weather codes** (WMO, as Open-Meteo documents them) to Meridian's condition set, with day or night
from `is_day`:

| Codes | Meridian condition |
|---|---|
| 0 | clear |
| 1 | mostly clear |
| 2 | partly cloudy |
| 3 | overcast |
| 45, 48 | fog (48: rime fog) |
| 51, 53, 55 | drizzle (light, moderate, dense) |
| 56, 57 | freezing drizzle (light, dense) |
| 61, 63, 65 | rain (light, moderate, heavy) |
| 66, 67 | freezing rain (light, heavy) |
| 71, 73, 75 | snow (light, moderate, heavy) |
| 77 | snow grains |
| 80, 81, 82 | rain showers (light, moderate, violent) |
| 85, 86 | snow showers (light, heavy) |
| 95 | thunderstorm |
| 96, 99 | thunderstorm with hail (light, heavy); only models with explicit hail produce these |
| 97 | heavy thunderstorm |
| anything else | unknown, recorded as a problem |

The table is data in the source's descriptor, not code.

**Time:** timestamps become instants (UTC); daily entries become local dates in the place's zone.

**Provenance:** source `open-meteo`, model `best_match`, the grid cell's coordinates and elevation, and the
fetch time. Request-cost estimates belong in diagnostics/accounting metadata, not weather-value provenance.

## Errors

- HTTP 400 returns `{ "error": true, "reason": "..." }`: a problem with the reason, no values.
- HTTP 429 or other limits: a problem, and Meridian backs off (time from configuration).
- Timeouts and network failures: a problem; cached values are returned if still fresh, marked stale.
- Never an exception across the API.

## Usage accounting and caching

- Calculate a best-effort request-cost estimate from endpoint, variable count, range, and model count
  before sending a request. The consuming app may set a local usage budget and Meridian should avoid
  exceeding that configured budget.
- A local counter is not a global quota guarantee: provider enforcement is shared across the applicable
  IP/application scope and may include other devices or apps. Do not claim that Meridian can reserve or
  guarantee the free-tier allowance for an installation.
- Handle provider rate-limit responses explicitly, honor any retry guidance returned by the service,
  and use bounded backoff. A 429 must not trigger an immediate retry loop.
- For a commercial deployment, use the provider's approved customer endpoint/plan. Provider credentials
  are supplied by the host application through a secure configuration boundary; never compile a private
  developer key into Meridian or a sample app. Confirm the provider's terms permit the chosen client-side
  credential distribution model before shipping.
- Cache by rounded coordinates (precision from configuration) and the request's parameters; time to
  live from configuration and the source's actual update cadence. A short TTL that causes repeat calls
  without producing fresher data is not useful.

## Credit

"Weather data by Open-Meteo.com", linked, plus the CC BY 4.0 notice, wherever these values show.

## Fixtures (recorded once, then tests run offline)

Mexico City (high altitude, rainy season), Berlin (daylight-saving switch inside the range), New York,
Sydney (southern summer), Tromsø in June (no sunset), a response with `null`s, an HTTP 400 error.

## Tests

- Every unit conversion and every weather code row.
- Hours align by time across a daylight-saving change; daily dates follow the place's zone.
- Nulls stay absent; unexpected units become problems.
- Errors and timeouts return problems, never exceptions; stale cache is marked stale.
- The call-weight guard stops requests at its limit.

## Done when

The CLI prints Mexico City's forecast and it matches Open-Meteo's own page for the same point; all
tests pass on CI.

## Consulted

Open-Meteo Forecast API documentation (parameters, response, variables and units, WMO weather codes,
errors); Open-Meteo terms; Open-Meteo ensemble API page (call weights).
