# Meridian provider research catalog

**Status:** research snapshot and provider candidates; not an integration approval  
**Checked:** 2026-11-25  
**Scope:** public documentation, service terms, product semantics, data access, and research priorities.

## 1. How to read this catalog

A provider can be useful for one feature and unsuitable for another. Its API being publicly reachable does not mean that every consuming application is allowed to use it, that it has a stable service-level guarantee, or that its data represents the requested location.

Before an adapter is approved, record:

- provider/operator and authoritative role;
- official API and schema documentation;
- service terms, data licence, attribution, and redistribution rights;
- commercial/non-commercial eligibility per consumer deployment;
- geographic and feature coverage;
- cadence, temporal/spatial resolution, and archive semantics;
- authentication and whether a client-distributed credential is permitted;
- rate limits, request weights, cost, and retry guidance;
- freshness, quality, missing-data, and error behaviour;
- whether direct requests from Android, iOS, JVM desktop, and any future browser consumer are supported;
- last verified date and a fixture representing the actual response.

The engine must use documented APIs. Do not scrape an official website as a substitute for an API unless an explicit, separately approved best-effort integration says otherwise.

## 2. Candidate overview

| Source | Intended role | Current assessment | Main risk or limitation |
|---|---|---|---|
| Open-Meteo Forecast API | First forecast adapter; model-backed current/hourly/daily forecast | Strong candidate for MER-1, subject to service-plan and licence approval | Free endpoint is non-commercial only; model and archive availability vary by place/product |
| Open-Meteo Previous Runs | Forecast verification archive | Candidate for fixed-lead aggregate skill studies | Fixed lead offsets, not full individual model runs |
| Open-Meteo Single Runs | Individual model-run archive | Candidate when full issue/run records are necessary | Exact model coverage and plan access must be checked |
| AviationWeather.gov | METAR observations and station metadata | Strong airport-observation candidate | Airports are not representative of every neighbourhood; operational limits apply |
| WMO Severe Weather Information Centre | Discovery and reference for authoritative warnings | Useful authority-discovery portal | Do not assume the portal itself is a stable feed API |
| FOSS Public Alert Server | Secondary CAP aggregation/discovery route | Promising optional aggregator | Not the issuing authority; duplicates and service/API behaviour must be verified |
| Mexico SMN / CONAGUA | Official Mexican forecasts, bulletins, stations, historical records, and open datasets | High-priority regional research target | Product formats, latency, terms, identifiers, and machine-readable access differ by dataset |
| CONAGUA open-data portal | Historical/hourly climatological datasets and hydrological/weather files | Research source for fixtures and verification candidates | Need dataset-specific dictionary, time zone, update cadence, quality, and rights review |

No source in this table is approved for production use merely by appearing here.

## 3. Open-Meteo

Official references:

- [Forecast API documentation](https://open-meteo.com/en/docs)
- [Terms and privacy](https://open-meteo.com/en/terms)
- [Pricing](https://open-meteo.com/en/pricing)
- [Previous Runs API](https://open-meteo.com/en/docs/previous-runs-api)
- [Single Runs API](https://open-meteo.com/en/docs/single-runs-api)
- [Historical Forecast API](https://open-meteo.com/en/docs/historical-forecast-api)

### Forecast service and licence are separate

The current terms distinguish the free service from the data licence:

- The free Forecast API is restricted to non-commercial use.
- The terms give private/non-profit apps without subscriptions or advertising as examples of non-commercial use; subscriptions, ads, and commercial-product use are examples of commercial use.
- Published free-tier limits include fewer than 10,000 calls per day, 5,000 per hour, and 600 per minute; a monthly limit is also shown in the current pricing table.
- Limits are applied by IP address and application, not as a guaranteed per-install allowance.
- The API data is licensed under CC BY 4.0 and requires appropriate credit, a link to the licence, and an indication of modifications.
- A CC BY data licence does not override the terms governing access to the API service.

These terms were checked on 2026-11-25 and must be rechecked before implementation and release. Do not assume that a consumer's app is non-commercial merely because it is free to download. Meridian is a library that may be embedded in apps with different business models; provider eligibility must be evaluated per deployment.

### Client credentials

Commercial endpoint usage may involve a customer endpoint and API key. A key shipped in an ordinary client application cannot be treated as a confidential secret. The host app must supply approved provider configuration; Meridian must not compile a private developer key into its binary or examples.

Before selecting a paid client-side provider plan, confirm that the provider's terms permit the credential distribution model. Do not create a Meridian-hosted proxy to hide keys without a separate architecture, cost, privacy, and operational decision. This is especially important because the master plan intentionally avoids mandatory hosting.

### Model selection and spatial limits

The Forecast API's Best Match documentation describes a global backbone using DWD ICON, NOAA GFS, and ECMWF IFS, with regional models added in specific documented areas. It also explains that the highest-resolution eligible model is used per variable and forecast hour, and that another model can fill gaps.

The same documentation states that 15-minute data is available only in specified regions; elsewhere, 15-minute values may be interpolated from hourly model output. The published regional table lists North America with NOAA RRFS and 15-minute data, while other regions can have a different model stack.

Implications for Meridian:

- Do not claim that Best Match means the nearest or most accurate model everywhere.
- Preserve the returned grid coordinate/elevation and source/model provenance.
- Verify the exact model and variables at representative Mexico coordinates before promising 15-minute native resolution.
- Treat interpolation as a transformation and disclose it in metadata.
- Do not hard-code a global model list from a single city test.
- Model availability and valid forecast horizon can differ by variable and lead time.

### Request accounting

One HTTP request is not necessarily one provider call for billing/quotas. The provider's rules can weight requests according to selected variables, time range, and model count. Estimate usage before sending the request, enforce a local configurable budget, honour provider limits and retry guidance, and document that a local counter cannot reserve a shared IP/application quota.

### Archive semantics

- **Previous Runs** exposes values at fixed lead-time offsets (day 0 through day 7). This is useful for aggregate skill by lead bucket, not for reconstructing every complete individual model run.
- **Single Runs** exposes individual model runs by initialization time and is the better conceptual fit when the full run history is required. Exact model and date coverage must be verified.
- **Historical Forecast** is a stitched continuous historical product, not a substitute for preserving the original issued model run.

Do not treat these products as interchangeable. Record the exact endpoint, model, issue/run semantics, access plan, coverage, licence, retrieval date, call cost, and dataset version for every verification experiment.

## 4. AviationWeather.gov

Official references:

- [Data API](https://aviationweather.gov/data/api/)
- [OpenAPI description](https://aviationweather.gov/data/schema/openapi.yaml)

The current API documentation states:

- METAR products are available worldwide in several formats, including JSON and raw text.
- Station information is available worldwide.
- The database provides up to the previous 30 days of data.
- Requests should be scoped and infrequent; the published limit is 100 requests per minute.
- Most endpoints return a maximum of 400 entries.
- A custom User-Agent is requested to avoid automated filtering.
- Undocumented query parameters are restricted.
- HTTP 204 indicates a valid request with no data; 429 indicates rate limiting.
- CORS is not permitted by the service.

Implications:

1. METAR is useful as an airport/station observation, not an automatic substitute for neighbourhood conditions.
2. Preserve ICAO station identity, report time, raw report, quality fields, station coordinates/elevation, and missing values.
3. Evaluate station representativeness and age per variable.
4. Do not assume all global stations have equal cadence, sensor quality, or local exposure.
5. Respect the endpoint schema exactly and do not poll more often than the data's update cadence.
6. A browser-based consumer may be limited by CORS. Native Android/iOS/JVM consumers should still test direct network behaviour and provider terms.
7. Confirm data-use and redistribution rights separately; API availability alone is not a licence conclusion.

Use it as a candidate for airport observations and station metadata, not as a complete national surface-observation network.

## 5. WMO Severe Weather Information Centre

Official reference: [WMO Severe Weather Information Centre](https://severeweather.wmo.int/).

WMO describes the Severe Weather Information Centre as a core component of the Global Multi-hazard Alert System and a way to improve availability of authoritative warnings and information from WMO Members.

Use it to discover and validate national issuing authorities and warning coverage. Do not assume that the portal's interactive website is a supported machine-readable ingestion API. For production ingestion, identify the actual issuing authority feed, published format, update policy, access terms, and attribution. Preserve the national/local issuing authority as the alert source.

For Mexico, the national authority research begins with SMN/CONAGUA rather than treating an international portal as the issuer.

## 6. FOSS Public Alert Server

References:

- [FOSS Public Alert Server](https://alerts.kde.org/config/)
- [FOSSWarn feature documentation](https://docs.fosswarn.org/features/alert_server/)
- [Project repository](https://github.com/KDE/foss-public-alert-server)

The project documentation describes a CAP-based service that aggregates hundreds of alert feeds published by alerting authorities. The default KDE server is operated by the project, and the software can also be self-hosted.

Potential role:

- secondary source discovery;
- an additional route for retrieving public CAP messages;
- a coverage/status comparison against direct official feeds.

Constraints:

- FPAS is an aggregator, not the authority that issued a warning.
- The same CAP message may arrive through WMO, FPAS, or a direct feed. Deduplicate by CAP message identity/references while retaining every delivery route in provenance.
- Verify the current public API/schema, service terms, status endpoint, rate limits, uptime, geographic coverage, and expected update latency before integration.
- Do not add push subscription infrastructure or a Meridian-hosted relay to the weather core.
- The server software's AGPL licence is not, by itself, the licence for the alert content supplied by every authority. Verify content terms and attribution by source.

Keep FPAS optional and secondary unless evidence supports a stronger role.

## 7. Mexico: SMN and CONAGUA

Official references:

- [Servicio Meteorológico Nacional](https://smn.conagua.gob.mx/es)
- [SMN functions and objectives](https://smn.conagua.gob.mx/es/component/content/article?id=3%3Aacerca-del-smn)
- [SMN automatic weather stations (EMAS)](https://smn.conagua.gob.mx/tools/GUI/EMAS.php)
- [SMN climatological statistics](https://smn.conagua.gob.mx/Es/Climatologia/Informacion-Climatologica/Informacion-Estadistica-Climatologica)
- [CONAGUA open data portal](https://datos.conagua.gob.mx/)
- [CONAGUA open-data catalogue](https://datos.conagua.gob.mx/views/index_datos_abiertos.html)

### Why this matters

SMN is Mexico's national meteorological service under CONAGUA. Its published responsibilities include continuous atmospheric monitoring, informing civil protection, and publishing public forecasts and weather notices, especially during cyclone season.

The SMN site exposes forecast products and notices, including tropical-cyclone notices and other special warnings. This makes it a priority authority to evaluate for Mexican alert and forecast provenance, even if Meridian's first forecast adapter remains Open-Meteo.

### Automatic stations

The SMN EMAS information describes automatic stations and basic measured variables such as wind speed/direction, pressure, temperature, relative humidity, solar radiation, and precipitation.

The public station map includes stations administered by multiple networks and displays each administrator. Its notice says the information is published as received and each network administrator is responsible for its data. Consequently:

- preserve the actual station/network administrator;
- inspect station status and telemetry freshness;
- verify each feed's schema, time zone, units, and quality metadata;
- do not assume all stations are maintained by SMN or share one quality regime;
- test real data availability and latency before treating the map as a machine-readable feed.

The map is evidence of a station-discovery lead, not proof that a stable, documented public API is available for every station.

### Conventional climatological station history

SMN's climatological-statistics section describes more than 5,000 historical stations and variables including maximum/minimum temperature, 24-hour accumulated precipitation, evaporation, and climatological normals. This can be useful for historical research and local climatology, but it is not equivalent to a high-frequency current observation feed.

### Open data

CONAGUA's open-data catalogue lists datasets with names such as hourly air temperature, hourly precipitation, hourly wind, gusts, and accumulated precipitation. This is a valuable research lead for building Mexican fixtures and verification datasets.

Before any use, inspect each file's data dictionary and determine:

- station/product ID and administrative owner;
- units and time zone;
- observation interval and accumulation window;
- timestamp meaning and publication delay;
- missing-value encoding and quality flags;
- update cadence and archive completeness;
- licence, reuse, redistribution, and attribution terms.

A downloadable CSV is not automatically a low-latency API or a fully quality-controlled observation.

### Independent catalogues

The web currently also exposes independent station/catalogue projects that combine station histories with external gridded/reanalysis products. These may help discover candidate locations or long-term context, but a reanalysis-augmented value must never be mislabelled as a direct station observation or official SMN measurement. Independently audit ownership, methods, provenance, and licence before using such data.

## 8. Source metadata contract

Every source descriptor should include, at minimum:

| Field | Purpose |
|---|---|
| Stable source ID and display name | Internal and consumer identity |
| Operator/issuing authority | Legal and provenance context |
| Documentation, endpoint, terms, licence | Auditable source record |
| Attribution text/link | Consumer credit requirements |
| Features and variables | What the source can provide |
| Geographic coverage and exclusions | Eligibility by place |
| Model/station/product IDs | Provenance |
| Native spatial/temporal resolution | Interpretation and quality |
| Cadence and issue/measurement timing | Freshness |
| Authentication and credential model | Security |
| Client/server use restrictions | Deployment eligibility |
| Rate limit, request weight, retry rules, cost | Request planning |
| Data freshness and expiry rules | Cache behaviour |
| Error and missing-value semantics | Typed problem mapping |
| Last reviewed date and status | Maintenance |

Do not make a source globally eligible because it exists in the catalogue. Eligibility is per feature, place, consumer terms, and configured credentials.

## 9. Approval gates for adding a provider

No provider adapter should start until the owner-facing proposal includes:

1. The feature gap it fills and why existing approved sources are insufficient.
2. Official API schema and a recorded representative response fixture.
3. Current service terms, data licence, attribution, and commercial-use eligibility.
4. Coverage at the representative target coordinates.
5. Native time resolution, spatial resolution, and update cadence.
6. Authentication and direct-client credential risks.
7. Rate limits, call weights, expected usage, and bounded retry strategy.
8. Missing, stale, malformed, timeout, 204, 429, and partial-response behaviour.
9. Offline deterministic adapter tests.
10. A maintenance owner and a removal/fallback plan.

## 10. Research backlog

| Priority | Research task | Evidence needed |
|---|---|---|
| P0 | Open-Meteo at Mexico City and representative Mexican regions | Actual variable/model availability, grid coordinates/elevation, 15-minute semantics, time-zone/DST fixtures, call weights |
| P0 | Open-Meteo service eligibility for each planned consumer | Written plan/terms decision, commercial status, attribution, credential distribution |
| P0 | SMN/CONAGUA station feeds and terms | Documented feed/schema, latency, administrators, quality and rights |
| P1 | AviationWeather station candidates for Mexico | Coverage and real METAR fixture set; representativeness by region |
| P1 | WMO and direct Mexican warning feeds | Actual feed URLs, CAP format, alert authority and update cadence |
| P1 | FPAS coverage and API behaviour | API/schema, source list, duplicate behaviour, terms, operational status |
| P2 | Historical forecast/observation datasets | Archive rights, cost, model coverage, matching feasibility and reproducible manifests |
| P2 | Air quality and UV sources | Per-pollutant coverage, station/model method, scale, licence and attribution |

## 11. References checked on 2026-11-25

- Open-Meteo Forecast API: https://open-meteo.com/en/docs
- Open-Meteo terms: https://open-meteo.com/en/terms
- Open-Meteo pricing: https://open-meteo.com/en/pricing
- Open-Meteo Previous Runs: https://open-meteo.com/en/docs/previous-runs-api
- Open-Meteo Single Runs: https://open-meteo.com/en/docs/single-runs-api
- AviationWeather.gov Data API: https://aviationweather.gov/data/api/
- OASIS CAP 1.2: https://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.html
- WMO Severe Weather Information Centre: https://severeweather.wmo.int/
- FOSS Public Alert Server: https://alerts.kde.org/config/
- SMN / CONAGUA public materials: links in section 7.

All current terms, pricing, coverage, and endpoint behaviour must be checked again immediately before implementation or release.