# Meridian, the weather engine: spec

**Status:** legacy weather-behaviour specification under reconciliation. This file is supporting domain research, not an independent implementation authority. The root `PLAN.md` is the master source for product scope, architecture, milestones, and gates; approved milestone specifications govern implementation. The research and source notes below are historical starting points and must be revalidated before use.

This document was originally written from public sources under `CLEAN_ROOM.md`. Preserve the source record, but resolve any conflict in favour of `PLAN.md` and the current approved milestone specification.

## 1. What it is

**Meridian** (name decided 2026-11-23; repo `INFORMATION89/Meridian-Engine`) is a
weather library with no UI. Given a place, it returns a forecast, current conditions, next-hour
rain, alerts, air quality and UV, each with a record of where every number came from. Solstice is its
first consumer, not its owner or a required dependency. Meridian must build, test, and operate independently;
other applications on iOS, Android, Windows, macOS, and Linux must be able to consume the same public API.

- **Licence:** Apache 2.0.
- **Implementation language and bindings:** defined only by the master [PLAN.md](../PLAN.md). The current KMP spike is evidence for one candidate; Kotlin Multiplatform and Rust remain candidates until the M0.1 evidence gate and owner approval. This specification does not select the production language, runtime, or artifact strategy.
- **Platforms:** Windows, macOS, iOS, Android, and Linux are intended targets. Exact supported architectures, artifact formats, and support levels are maintained in [PLAN.md](../PLAN.md).
- **Not in it:** UI, app navigation, Android `Context` or other platform UI objects in the engine core, dependency-injection frameworks in the core, or any consumer application's code.

## 2. Platforms

The engine must keep weather-domain behaviour independent from the host operating system. Native libraries and language bindings are packaging/integration layers; they must not change the meaning of weather results.

The five target platforms are Windows, macOS, iOS, Android, and Linux. A target is not considered supported simply because a compiler emits a binary: the library must load through its intended binding, execute a deterministic operation, handle errors and cancellation safely, and pass platform integration tests.

The current M0.1 candidate target matrix, runtime requirements, dependency support, and platform build hosts are maintained in [PLAN.md](../PLAN.md). Revalidate target and dependency support before implementation and release; do not treat the KMP spike as a production-stack decision.

Do not require a single database, HTTP client, date-time library, or UI framework across all host applications. Choose dependencies only after the M0.1/M0.2 compatibility and packaging tests. The engine must isolate network, storage, clock, and platform capabilities behind narrow internal boundaries.

## AI use and optional chat integration

Meridian is a deterministic weather engine, not an AI product. It must fetch, normalize, evaluate, combine, and return weather results without calling a generative-AI service or requiring model files, inference runtimes, AI credentials, prompts, tokens, or AI-specific network access. No chatbot, generated briefing, or autonomous agent is part of the engine runtime.

An optional MCP adapter may be built outside the weather-domain core so an external chat client can query Meridian through approved read-only tools. MCP is only the connection between the chat host and Meridian; it does not itself provide a model or make AI part of the engine. The external chat host/provider is responsible for model behaviour and any model/token costs. The adapter must preserve structured values, units, timestamps, provenance, freshness, uncertainty, and official alerts, and must enforce authentication, input validation, bounded queries, and least privilege.

The engine's normal API remains the primary interface for apps and tools that do not use chat. AI-assisted engineering and manual research are contributor workflows only; any claimed forecast-quality improvement must be reproduced with deterministic code and versioned data, then reviewed by a human.

The contributor policy is defined in [AI_POLICY.md](../AI_POLICY.md). The evidence-based verification workflow and optional MCP assessment are defined in [PLAN.md](../PLAN.md) and its M0.3 specification.

## 3. Core concepts

**Place:** latitude, longitude, optional elevation, time zone, optional country and region codes
(ISO 3166). The engine never stores place history; the app owns saved places.

**Feature:** `forecastHourly`, `forecastDaily`, `current`, `nowcast` (next 1–2 hours of rain),
`alerts`, `airQuality`, `uv`, `pollen`, `normals`, `observations`.

**Source descriptor** (data, one file per source, loaded at start):
- `id`, display name, operator, homepage, terms URL, required credit text and link.
- `access`: `api` or `website` (reads pages; always best-effort).
- `needsKey`, `costs` (free, paid), rate limits and the minimum interval between calls.
- `coverage`: list of country codes, bounding boxes or polygons (GeoJSON-style).
- Per feature: `tier` (`official`, `regional`, `global`, `fallback`), update cadence, resolution.

**Source adapter:** code that turns a source's response into the engine's model. One per source.
Clean adapters live in the engine; until they're rewritten, Breezy-based adapters live in Solstice
and plug in through the same interface.

## 4. Data model

All values in SI units and all times in UTC internally; units and time zones are applied by the app.

- **Time series:** per variable, a list of `(time, value, provenance)` at the source's native step
  (minutes, 15 minutes, hours, days).
- **Variables:** temperature, apparent temperature, dew point, humidity, pressure, wind speed, gust and
  direction, precipitation amount and probability, precipitation type, cloud cover, visibility, UV
  index, weather condition (a small fixed set), air quality pollutants and index, pollen.
- **Observation:** a measured value with station id, distance from the place, elevation difference,
  and measurement time.
- **Alert:** follows the Common Alerting Protocol 1.2 (OASIS): `identifier`, `sender`, `sent`,
  `status`, `msgType` (Alert, Update, Cancel), `references`, and per info block `language`, `event`,
  `urgency`, `severity`, `certainty`, `onset`, `expires`, `headline`, `description`, `instruction`,
  and areas (`areaDesc`, polygons, geocodes). Non-CAP sources are mapped into the same shape.

## 5. Provenance

Every value carries:
- the source ids that produced it and their weights;
- any correction applied (amount, the observations it came from, the window);
- when each input was issued and fetched;
- whether it's a fallback because a better source failed.

Results also carry a summary per feature: which sources were used, which were skipped and why
(out of coverage, off, no key, failed, stale). The transparency graphic and the sources sheets read
only this.

## 6. Choosing sources (on every refresh)

For each feature of each place:
1. Candidates: sources whose coverage contains the place, that the person hasn't turned off, and that
   have what they need (a key, consent for best-effort sources).
2. Order by tier, then by resolution, then by recent health (failures, staleness).
3. A source picked by hand for this place and feature wins while it covers the place.
4. Call the chosen sources in parallel, within each source's rate limit and minimum interval.
5. If a call fails or returns stale data, use the next candidate and record the fallback.

## 7. Combining sources

**Forecast (blend):** the initial validated baseline may use neutral/equal weights where the approved M5 specification supports it. Adaptive weights and bias corrections are later capabilities; they require verified samples, chronological archive evaluation, provenance, bounded behaviour, and their own milestone gate. Preserve the individual inputs and per-step spread (range and suitable dispersion measure).

A weighted share or fraction of deterministic models above a rain threshold is **model agreement**, not automatically a calibrated precipitation probability. Preserve provider-supplied probabilities separately. Expose a probability only when its event threshold, time window, method, and calibration/evaluation status are documented.

**Next-hour rain:** use source-native temporal resolution only where the selected provider/model actually supplies it for the requested region. Distinguish native 15-minute values from interpolated or resampled hourly data; do not promise 15-minute coverage for Mexico or other regions without current source evidence. Radar/satellite nowcasting remains a later, gated capability (section 10).

**Current conditions:** use a quality-controlled, sufficiently fresh and representative observation when one is available. Evaluate distance, elevation, exposure, sensor/quality flags, and variable-specific representativeness; the nearest station is not automatically the best. Otherwise return an explicitly labelled model estimate or an unavailable result.

**Alerts:** normalize eligible official alert feeds while preserving the issuing authority, original message identity, source route, CAP references, updates, cancellations, validity, geography, and attribution. Deduplicate using CAP identity/reference semantics; do not replace official authority or instructions with an aggregator's interpretation merely because a copy is more local. Alerts are never blended or reworded as if Meridian issued them.

**Air quality and UV:** a local station when close and fresh, otherwise the global model.

## 8. Local verification and learning (future, gated capability)

- Meridian does not own or persist the consumer application's saved-place list and must not create background location tracking. Any recent verification/learning record is derived only from explicit consumer requests and retains the minimum spatial detail needed for valid forecast/observation matching.
- The initial 30-day rolling window applies to recent local learning/verification records only, when the M7 gate approves their use. It is configurable, bounded, and independently deletable. It is not a universal retention period for cache, forecast archives, observations, or historical/climatological datasets.
- Match forecasts only to quality-controlled, temporally appropriate and spatially representative observations. The nearest airport is not automatically representative of the requested point.
- M7 may begin with descriptive skill statistics. Adaptive weights and bias corrections are not automatically activated by new data; they require sufficient samples, an independently evaluated method, provenance, regression safeguards, and the relevant approval gate.
- Historical forecast archives are candidate research inputs, not a guaranteed day-one seed. Verify exact product/run semantics, coverage, terms, licence, attribution, availability, and request cost before use. A stitched historical series or reanalysis is not interchangeable with archived issued forecasts.
- **Optional sharing:** disabled by default and not required for local learning. Any future opt-in must separately explain the data fields, purpose, destination, recipients, and retention; provide an inspectable payload example and controls for withdrawal and deletion of eligible local data. Do not add a Meridian-hosted collection service by default.

## 9. Storage, caching and scheduling

- The core defines consumer-neutral contracts. Persistent storage is optional; the host may use a validated optional storage implementation or a host-supplied adapter only after the M0.2 contract is approved. Do not assume Room, SQLDelight, SQLite, or another database is selected for production.
- Keep response cache, issued-forecast snapshots, observation history, the short local learning/verification window, and long-term historical/climatological datasets as distinct data categories with separate retention, licensing, and deletion rules.
- The host owns the application lifecycle, settings, secure credential storage, saved places, and refresh scheduling. Meridian never stores a saved-place list or schedules background refreshes itself.
- Honour source update cadence, published cache/expiry metadata, rate limits, and terms; never call a source more frequently than permitted.
- Normal network access is used only to contact configured weather/observation/alert providers directly where their access and credential model permit. The engine does not call a Meridian-operated backend or require a persistent connection to a Meridian service.
- When offline, return valid cached data with its original timestamps and freshness state, or a typed unavailable/stale result. Never imply cached data is current.

## 10. Phase 2: radar nowcast (not before phase 1 works)

Radar and satellite nowcasting are research leads, not a committed implementation. Before processing, verify current source access rights, regional coverage, latency, scan/update cadence, georeferencing, calibration, quality, and redistribution terms. Keep radar observations distinct from satellite-derived precipitation estimates. Optical-flow extrapolation and blending require a regional benchmark and the M10 acceptance gate. Any Mexico-specific access claim must be rechecked against current SMN documentation and terms.

## 11. API for apps (historical sketch; M0.2 governs the contract)

- The first-release direction is a batch-oriented request/result API, as proposed in M0.2. This document's earlier streaming sketch is not an adopted contract. Streaming/partial updates may be reconsidered only if M0.1 interoperability evidence and a concrete consumer requirement justify them.
- Results should express per-feature success, partial failure, unavailable/stale states, provenance, units, timestamps, and relevant uncertainty. Provider-specific DTOs must remain internal.
- Source diagnostics and attribution data may be exposed through documented, consumer-neutral methods; do not make engine APIs depend on settings-screen or UI concepts.
- Preserve typed problem information for input/provider/storage failures. Cancellation remains cancellation and must not be swallowed as a normal successful result or ordinary source failure.
- Inter-application sharing, notifications, settings, and their consent UI are consumer-application responsibilities. This engine specification does not define Solstice-specific sharing behaviour.

## 12. Researched source candidates (not integration approval)

Open-Meteo, aviationweather.gov, direct official alert feeds identified through WMO, and FOSS Public Alert Server are research candidates for particular features. Inclusion here does not approve an adapter or imply universal coverage, commercial eligibility, stable uptime, or a single-source plan. Select each provider only after checking current terms, licence, attribution, coverage, authentication/credential distribution, request costs, rate limits, and failure behaviour for the intended consumer deployment.

## 13. Configuration and tokens

Every operational threshold, weight, distance, age, window, cell size and interval must be named, documented, validated, and traceable to an approved specification. Agents may choose routine internal parameters within an approved method and record their rationale/tests. A new consumer-configurable default, material change to forecast behaviour, or change to the approved scientific method requires owner review under `PLAN.md` and `RULES.md`.

## 14. Testing

- Each adapter is tested offline against recorded responses (fixtures), including error and empty
  responses.
- Blend and correction changes are checked against public archives (past forecasts against
  observations) before they ship, with the error before and after recorded in the pull request.

## Current decisions and open gates

This legacy specification is not an approval record. Reconcile remaining behaviour against `PLAN.md` and the active milestone specification. The production language/runtime, public API and storage contract, provider eligibility, historical archive, adaptive learning, and radar/nowcasting methods remain subject to their respective evidence and approval gates.

## Consulted (public sources only)

Kotlin Multiplatform platform stability (kotlinlang.org); Jetpack KMP support tiers
(developer.android.com); Kotlin/Native frameworks for Swift; Ktor client engines (ktor.io); Windhawk
mod structure (Windhawk wiki); OASIS CAP 1.2; Open-Meteo API docs (best match, 15-minute data,
historical forecasts); aviationweather.gov Data API; WMO Severe Weather source list; FOSS Public Alert
Server source list; NOAA Enterprise Rain Rate; pysteps paper (Pulkkinen et al., 2019); LibreWXR README;
Apple WeatherKit attribution; Gradient Weather privacy policy and store page; Acme Weather coverage.
