# Meridian: research and build plan

Status: **supporting research draft** (originally written 2026-11-23). The root [PLAN.md](../PLAN.md) is now the master architecture and delivery plan. This document preserves earlier source, library, and algorithm research; it is not authority for the implementation language, toolchain, module layout, database, or release strategy. The old dependency and storage choices below are historical research and must not be treated as adopted decisions. Root PLAN.md currently recommends KMP with Desktop JVM for the five-OS target matrix. Revalidate provider terms and technical details before implementation.

Clean-room research (CLEAN_ROOM.md): the original research was written from public docs and papers listed at the end, not from Breezy's implementation code.

## 1. In one paragraph

Meridian is a cross-platform weather library in its own repo (`INFORMATION89/Meridian-Engine`, Apache 2.0). Its technical stack is governed by the root PLAN.md; the older Kotlin/Gradle module and Room proposals below are not binding. It starts with four researched source candidates (Open-Meteo, aviationweather.gov, WMO, FOSS Public Alert Server),
returns every value with where it came from, blends models, merges alerts, and later learns a local
correction on the phone. Solstice uses it as a git submodule, like Motion. It's built in nine
milestones, each with a spec section first, then a build session, then a check against real data.

## 2. Earlier dependency candidates (not adopted without MER-0.1/MER-0.2 validation)

| Need | Choice | Why | Watch out |
|---|---|---|---|
| Async | kotlinx.coroutines | Standard for KMP | |
| HTTP | Ktor client | Native engine per platform: OkHttp (Android), Darwin (Apple), WinHttp (Windows) | |
| JSON | kotlinx.serialization | Open-Meteo, aviationweather, WMO's source list | |
| XML (CAP alerts) | xmlutil (`io.github.pdvrieze.xmlutil`, 1.0.x, Apache 2.0) | The XML format listed for kotlinx.serialization; multiplatform | Native was beta in older versions; test on each target |
| Dates and times | kotlinx-datetime 0.7+ | Time zones on every target | Not 1.0 yet; `Instant` moved to the standard library in 0.7 |
| Database (log, cache) | **Room** 2.7+ (KMP), behind our own interface (decided 2026-11-23) | First stable Room with KMP; Android tier 1, macOS tier 2 | Keep it swappable (SQLDelight is the alternative) |
| Settings | DataStore (KMP) | Jetpack, multiplatform | |
| Geometry | Our own small point-in-polygon | CAP areas are polygons; a library is overkill | |

Everything above is Apache 2.0 or compatible.

## 3. Earlier module-layout candidate (superseded by root PLAN.md)

```
Meridian-Engine/
  core/                 models, units, provenance, source selection, blend, alert merge, correction
  sources/openmeteo/    adapter + source descriptor (data file)
  sources/aviationweather/
  sources/wmo/
  sources/fpas/
  storage-room/         Room implementation of the storage interface
  testing/              fixtures, fake clock, fake HTTP
  cli/                  JVM tool: print a place's result; run the verification against archives
```

- **Consumed by Solstice like Motion:** git submodule plus a Gradle composite build, so a change in
  Meridian shows up in Solstice at once with no publishing step.
- **Publishing later**, when another app needs it: Maven Central with a verified namespace (JitPack
  doesn't handle multiplatform well). GitHub Packages works but needs a token even to read.
- **CI:** JVM and Android tests on Linux for every pull request; a macOS build once that target
  starts; the verification job runs by hand (it needs open internet, see Saturday step 3).

## 4. Public API (sketch, names to agree)

```kotlin
class Meridian(config: MeridianConfig, storage: MeridianStorage, http: HttpClient)

suspend fun refresh(place: Place, features: Set<Feature>): MeridianResult
fun results(place: Place): Flow<MeridianResult>          // partial results as sources answer
fun sources(place: Place): List<SourceState>              // for the settings screen
fun credits(result: MeridianResult): List<Credit>         // what must be shown

class MeridianResult(
    val place: Place,
    val series: Map<Variable, TimeSeries>,                // values in SI, times in UTC
    val spread: Map<Variable, TimeSeries>,
    val current: Current?,
    val alerts: List<Alert>,                              // CAP 1.2 shaped
    val provenance: Provenance,                           // sources, weights, corrections, freshness
    val problems: List<Problem>,                          // failures, fallbacks; never exceptions
)
```

Solstice converts these to its UI models in one place (a small bridge in the app), so the UI never
depends on Meridian's types directly.

## 5. Sources

### Open-Meteo (forecast, models, spread, air quality, archives)
- **Endpoints:** `/v1/forecast` (best match, or chosen models with `models=`), `/v1/ensemble` and the
  ensemble mean API (mean and standard deviation from ensemble members), the air quality API (CAMS),
  the Previous Runs API for fixed-lead skill analysis, and the Single Runs API for full model-run
  records. The Historical Forecast API is a separate stitched archive; do not treat it as equivalent
  to Previous Runs or Single Runs.
- **Terms:** the no-key free service is non-commercial only; the published limits include 10,000
  calls/day, 5,000/hour, 600/minute, and 300,000/month. Limits are applied by IP and application.
  Data is CC BY 4.0 and requires credit plus indication of changes. Apps with subscriptions,
  advertising, or commercial-product use require a commercial plan. Re-check current terms before
  production and do not infer that every app embedding Meridian qualifies for the free service.
- **Budget:** call accounting is not always one HTTP request = one call; more than 10 weather variables
  or ranges longer than two weeks can count as multiple fractional calls. A local counter is only an
  estimate because provider enforcement also uses IP and application. The provider's current terms,
  usage weights, endpoint and access tier must be recorded per deployment.

### aviationweather.gov (airport observations)
- **Endpoint:** METAR in JSON by station id; a station info product (worldwide) for coordinates.
- **Rules:** 100 requests a minute, at most 400 results per query, a custom User-Agent recommended,
  30 days of history; bulk cache files update every minute (stations once a day).
- **Plan:** keep a station list on the phone (refreshed weekly from the station file), pick the
  nearest stations, ask only for those ids.

### WMO Severe Weather Information Centre (official alerts)
- **How:** WMO's source list (`sources.json`) names each official alerting authority with its CAP feed
  (an RSS list of CAP messages), whether it's official, and whether the feed is operating.
- **Plan:** from the place's country, pick that country's official feeds, read the RSS, fetch the CAP
  messages, keep those whose polygon contains the place. Alerts with only geocodes (no polygon) are
  kept with "area not precise" in their provenance.
- **Terms:** free to reuse, credited to the issuing service (for Mexico, SMN); WMO gives no warranty.

### FOSS Public Alert Server (alerts, civil protection, Taiwan, Japan's typhoons)
- **How:** aggregates hundreds of CAP feeds (server AGPL; we only call it). Its OpenAPI description
  is in its repo; read it in the spec session for milestone 3 (I couldn't open it from here).
- **Watch out:** still described as early testing upstream; always paired with WMO, never alone.

### Requests per refresh (one place, everything on)
About 4 to 6: forecast (multi-model), ensemble mean, air quality, one METAR call, plus alerts every
15 minutes or so (WMO feed and FPAS). Well within every limit above for a person's handful of places.

## 6. Methods (all from published work)

**Blending models**
- Start with **equal weights**: simple and hard to beat until there's local data. A comparison of
  weighting methods found that more complex Bayesian model averaging adds value only when the best
  model beats the equal-weight blend.
- Then **weights from recent skill**: a candidate approach is to weight each model in proportion to
  the inverse of its error (RMSE) over a configurable rolling window, initially 30 days for research.
  This window and weighting policy require archive evaluation and owner approval; a floor can keep a
  model from dropping to zero if the evidence supports it.
- **Spread** shown to people: from the disagreement between models, plus Open-Meteo's ensemble
  standard deviation where available.

**Correcting by local observations**
- **Phase A, decaying average:** NCEP has corrected forecasts this way since 2006; it's the simplest
  method, and NOAA's tests found it improves bias without making other scores worse. Bias is updated
  each day as a weighted mix of yesterday's bias and today's error (NCEP uses a weight of 0.02 for its
  ensembles; ours is a token, tuned on Mexico City data), kept per place, variable, lead time and hour
  of day (errors change through the day).
- **Phase B, short-term term:** a study on 100 stations in Denmark found that a moving average combined
  with a Kalman filter for the very short-term errors did best. Japan's weather agency uses a Kalman
  filter operationally for temperature. Added once phase A is verified.
- **Rain:** no correction at first; measured with the Brier score (how good the rain chance was), and
  the hit rate for "rain within 2 hours".

**Verification (before any change ships)**
Mean absolute error and RMSE for temperature, Brier score for rain chance, per model and for the blend,
before and after, on Mexico City's rainy season first (Saturday's model test is the first run). Results
go in the pull request.

## 7. What Meridian stores on the phone

- Forecasts: place, source, model, issue time, valid time, variable, value.
- Observations: station, time, variable, value.
- Statistics: place, source, variable, lead-time bucket, hour bucket, bias, error, count.
- Cache: the last responses with their expiry.

Retention is configurable, bounded, independently disableable, and explicitly deletable. A 30-day
window is a proposal for evaluation, not an adopted product default; the owner must approve it after
storage-cost and data-scope review.

## 8. How it reaches Solstice (decided 2026-11-23: switch directly)

1. Solstice switches to Meridian **feature by feature**, with no side-by-side comparison step: alerts
   first (where Meridian adds the most), then current conditions, then the forecast. Each switch is
   checked on the phone and against the archives before the next.
2. Breezy's own source code stays in the app as adapters implementing Meridian's source interface,
   rewritten clean over time (PLAN.md, Weather engine).

## 9. Milestones

Specs written so far: MER-1 `docs/specs/MER-1_FORECAST.md`, MER-2 `MER-2_PROVENANCE.md`, MER-3 `MER-3_ALERTS.md`
(complete, including the FOSS Public Alert Server), MER-4 `MER-4_OBSERVATIONS.md`, MER-5 `MER-5_BLEND.md`, MER-6
`M6_ALERTS_IN_SOLSTICE.md`, MER-7 `MER-7_LEARNING.md`; MER-0's checklist `MER-0_REPO.md`. MER-8 has none yet.

Each one: a spec section written from public sources, a separate build session (clean room), then a
check with real data. Nothing starts without Marco's OK.

| # | Milestone | Done when |
|---|---|---|
| MER-0 | Repo set up: KMP project, licence, rules, CI, empty modules | CI green on an empty build |
| MER-1 | Core models, units, Open-Meteo forecast for one place | The CLI prints a correct forecast; fixture tests pass |
| MER-2 | Provenance, source selection per refresh, caching, storage interface | Results show their sources; no source called more often than allowed |
| MER-3 | Alerts: CAP parsing, WMO and FPAS, merging duplicates | Recorded real alerts (Mexico, US, Germany) merge correctly |
| MER-4 | Observations: nearest airport, current conditions rule | Current conditions in CDMX come from MMMX when fresh |
| MER-5 | Blend and spread: several models, ensemble spread | Blend beats or matches best match on the archive check |
| MER-6 | Solstice reads alerts from Meridian (first feature switched) | Alerts on the phone come from Meridian, checked against recorded real alerts |
| MER-7 | Learning on the phone: log, decaying-average correction, skill weights | Error drops on the archive check; shown in the sources sheet |
| MER-8 | macOS framework build (for the menu bar app) | A Swift sample prints a forecast |

## 10. Risks

- **Open-Meteo is the backbone and has no guarantee:** Meridian must degrade cleanly (cache, then a
  clear "no data" state); keep the option of self-hosting Open-Meteo (its server is open source) or a
  second forecast source later.
- **Alert areas vary by country:** some feeds have polygons, some only codes; be honest in the
  provenance rather than guess.
- **Libraries not at 1.0** (kotlinx-datetime, Windows support in Jetpack): pin versions and update on
  purpose.
- **Clean-room discipline over many sessions:** the spec-then-build rule and "consulted" lists in each
  pull request.

## Decided (Marco, 2026-11-23)

Room for the database; switch directly, feature by feature; MER-0 starts at the end of Saturday's
session if time is left (CONTINUE.md, step 10).

## Consulted

Room 2.7 and 3.0 release notes and Jetpack KMP support tiers (developer.android.com); kotlinx-datetime
releases (kotlinlang.org, GitHub); xmlutil docs; Kotlin Multiplatform publishing docs and Maven Central
tutorial (kotlinlang.org, JetBrains); Open-Meteo terms, ensemble and ensemble mean API docs; aviationweather
Data API page; WMO Severe Weather Information Centre notes and source list; WMO Register of Alerting
Authorities; KDE's FOSS Public Alert Server page; OASIS CAP 1.2; Glahn (2012) and NOAA office notes on
decaying-average bias correction; Cui et al. (2012); Kaas and Alerskans (EGU 2021); JMA Kalman filter
post-processing (JMSJ 2024); Ahrens on multimodel weighting; EMS 2026 abstract on dynamic weighting and
the Australian performance-based weighting study.
