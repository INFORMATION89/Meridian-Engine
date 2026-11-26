# Meridian: research and build plan

**Status:** historical supporting research snapshot (originally written 2026-11-23). The root [PLAN.md](../PLAN.md) is the sole master product and delivery plan. This document preserves source, library, and algorithm research; it is not authority for implementation language, toolchain, module layout, database, API, storage, or release strategy. The KMP platform proof is evidence for one candidate only; the master plan still requires an M0.1 evidence gate and owner approval before choosing the production stack. Revalidate all provider terms and technical details before implementation.

Clean-room research (CLEAN_ROOM.md): the original research was written from public docs and papers listed at the end, not from Breezy's implementation code.

## 1. In one paragraph

Meridian is an independent cross-platform weather engine (`INFORMATION89/Meridian-Engine`, Apache 2.0). The old Kotlin/Gradle module and Room proposals below are not binding. The listed providers and algorithms are research candidates, not approvals. Meridian must preserve provenance, combine sources only after the relevant gate, and add local learning only after reproducible verification. An earlier idea to consume Meridian in Solstice as a Git submodule was an integration convenience proposal, not the official distribution strategy. The current milestone order is defined by root `PLAN.md`; the historical sequence below is superseded.

## 2. Earlier dependency candidates (not adopted without M0.1/M0.2 validation)

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

## 3. Earlier module-layout candidate (historical only; superseded by root PLAN.md)

The following layout is preserved as research context. Do not create these modules or treat this directory structure as an approved architecture.

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

- **Historical Solstice integration idea:** a Git submodule plus Gradle composite build was considered for local development. Root `PLAN.md` says this may remain a developer convenience, not the official distribution requirement.
- **Historical publishing ideas:** Maven Central and GitHub Packages were considered; artifact and release strategy are governed by root `PLAN.md` and remain subject to M0.1 evidence.
- **CI:** JVM and Android tests on Linux for every pull request; a macOS build once that target
  starts; the verification job runs by hand (it needs open internet, see Saturday step 3).

## 4. Public API (historical sketch; not an approved contract)

M0.2 currently prefers a batch-first request/result API. Do not treat the Flow/streaming sketch below as adopted. Any streaming contract requires interop evidence and a concrete consumer need.

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

## 5. Sources (historical research snapshot; terms must be rechecked)

All provider limits, prices, coverage, endpoint details, licences, and access conditions below were researched on or around 2026-11-23. They are not current guarantees or integration approvals. Before each adapter, re-check official documentation and terms for the exact consumer deployment.

### Open-Meteo (forecast, models, spread, air quality, archives)
- **Endpoints:** `/v1/forecast` (best match, or chosen models with `models=`), `/v1/ensemble` and the
  ensemble mean API (mean and standard deviation from ensemble members; stored since about March
  2026), the air quality API (CAMS), and the historical forecast API for seeding and verification.
- **Terms:** free for non-commercial use, under 10,000 calls a day, 5,000 an hour and 600 a minute;
  data under CC BY 4.0, so the credit is required. No uptime guarantee. Solstice is free and open
  source, so it qualifies; a paid or commercial app would need Open-Meteo's paid plan.
- **Budget:** limits count per caller, and one request with several models or long ranges counts as
  more than one call (the ensemble docs show an example at 4.0). Meridian counts its own "call
  weight" per refresh and stays within a share of the limits set in configuration.

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

## 6. Methods (research hypotheses, not approved algorithms)

The following methods are candidates for evaluation, not implementation instructions. Root `PLAN.md` and the approved milestone specification determine which method, if any, may be implemented.

**Blending models**
- Start with **equal weights**: simple and hard to beat until there's local data. A comparison of
  weighting methods found that more complex Bayesian model averaging adds value only when the best
  model beats the equal-weight blend.
- Then **weights from recent skill**: each model's weight proportional to the inverse of its error
  (RMSE) over a rolling 30-day window, per variable and lead time. Recent studies (ECMWF and ACCESS
  blends, Australia's Bureau of Meteorology) found this consistently beats equal and fixed weights,
  especially at longer lead times. A floor keeps any model from dropping to zero (a token).
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

## 7. Historical storage proposal (superseded)

The old proposal to retain all forecast, observation, statistics, and cache data for 30 days is superseded. The current design distinguishes:

- Response cache, governed by source expiry, freshness, and provider terms.
- Recent local forecast/observation verification and learning records, initially a bounded 30-day rolling window where the M7 gate permits it.
- Historical forecast archives and climatological datasets, with separate provenance, licensing, access, and retention rules.
- Saved places, settings, permissions, and deletion UI, which remain consumer-application responsibilities.

Do not interpret the historical schema below as an approved storage contract. M0.2 defines the consumer-neutral storage boundary and deletion behaviour.

## 8. Historical Solstice integration sketch (not binding)

Solstice is one consumer, not an architectural authority or required dependency. Root `PLAN.md` and the current Solstice integration plan govern any migration sequence. This old proposal does not authorize feature migration or make a Git submodule the official distribution path.

1. Solstice switches to Meridian **feature by feature**, with no side-by-side comparison step: alerts
   first (where Meridian adds the most), then current conditions, then the forecast. Each switch is
   checked on the phone and against the archives before the next.
2. Breezy's own source code stays in the app as adapters implementing Meridian's source interface,
   rewritten clean over time (PLAN.md, Weather engine).

## 9. Milestones

Specs written so far: M1 `docs/specs/M1_FORECAST.md`, M2 `M2_PROVENANCE.md`, M3 `M3_ALERTS.md`
(complete, including the FOSS Public Alert Server), M4 `M4_OBSERVATIONS.md`, M5 `M5_BLEND.md`, M6
`M6_ALERTS_IN_SOLSTICE.md`, M7 `M7_LEARNING.md`; M0's checklist `M0_REPO.md`. M8 has none yet.

Each substantive capability needs an independently written specification, relevant public sources, and acceptance criteria before implementation. Agents may decide routine technical details within an approved milestone and specification; owner review is required for product scope, privacy, legal/licensing, paid costs, public contracts, strategic compatibility, or changes to the approved scientific method. Follow `PLAN.md` and `RULES.md`.

| # | Milestone | Done when |
|---|---|---|
| M0 | Repo set up: KMP project, licence, rules, CI, empty modules | CI green on an empty build |
| M1 | Core models, units, Open-Meteo forecast for one place | The CLI prints a correct forecast; fixture tests pass |
| M2 | Provenance, source selection per refresh, caching, storage interface | Results show their sources; no source called more often than allowed |
| M3 | Alerts: CAP parsing, WMO and FPAS, merging duplicates | Recorded real alerts (Mexico, US, Germany) merge correctly |
| M4 | Observations: nearest airport, current conditions rule | Current conditions in CDMX come from MMMX when fresh |
| M5 | Blend and spread: several models, ensemble spread | Blend beats or matches best match on the archive check |
| M6 | Solstice reads alerts from Meridian (first feature switched) | Alerts on the phone come from Meridian, checked against recorded real alerts |
| M7 | Learning on the phone: log, decaying-average correction, skill weights | Error drops on the archive check; shown in the sources sheet |
| M8 | macOS framework build (for the menu bar app) | A Swift sample prints a forecast |

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

## Historical decisions (superseded where they conflict with the master plan)

The 2026-11-23 preference for Room is not an adopted storage decision. M0.2 leaves concrete storage optional and subject to target/consumer proof. The old feature-by-feature Solstice migration sequence and original M0 start date are historical notes, not current instructions.

## Consulted

Room 2.7 and 3.0 release notes and Jetpack KMP support tiers (developer.android.com); kotlinx-datetime
releases (kotlinlang.org, GitHub); xmlutil docs; Kotlin Multiplatform publishing docs and Maven Central
tutorial (kotlinlang.org, JetBrains); Open-Meteo terms, ensemble and ensemble mean API docs; aviationweather
Data API page; WMO Severe Weather Information Centre notes and source list; WMO Register of Alerting
Authorities; KDE's FOSS Public Alert Server page; OASIS CAP 1.2; Glahn (2012) and NOAA office notes on
decaying-average bias correction; Cui et al. (2012); Kaas and Alerskans (EGU 2021); JMA Kalman filter
post-processing (JMSJ 2024); Ahrens on multimodel weighting; EMS 2026 abstract on dynamic weighting and
the Australian performance-based weighting study.
