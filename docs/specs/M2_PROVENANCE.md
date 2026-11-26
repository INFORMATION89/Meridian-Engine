# Meridian M2: provenance, source selection, caching, storage

**Status:** historical design proposal from 2026-11-23. Root `PLAN.md` and the approved M0.2 storage/API contract supersede its implementation assumptions. Re-check all provider limits, call weights, endpoints, and terms before implementation; do not treat example sources or database choices below as approved.

## Scope

Every result says where each value came from. Each refresh asks only the sources that need asking.
No source gets called more often than it allows. Any persistent storage remains optional and behind the approved M0.2 storage contract. Room and other concrete database choices in this historical specification are not adopted.

## Provenance

Every value Meridian returns can be traced to:

| Field | Meaning |
|---|---|
| `source` | Source id (`open-meteo`, `aviationweather`, `wmo`, `fpas`) |
| `model` | Model id when the source has several (`best_match`, `ecmwf_ifs025`, …), else empty |
| `issued` | When the source made it (model run, observation time, alert `sent`) |
| `fetched` | When Meridian got it |
| `weight` | Share in the blend, 0–1 (1 when it's the only one) |
| `correction` | Applied correction id and amount, if any (M7) |
| `fallback` | `true` when the preferred source failed and this one stood in |

`Provenance` on a result holds one entry per source and model used, plus per variable the list of
contributors. The UI never reads values without being able to show this (SOURCES.md, transparency).

## Credits

Each source declares its credit as data: text, link, licence, and where it must appear (on screen,
on the about page, or both). `credits(result)` returns only the sources the result actually used.
Open-Meteo: CC BY 4.0, credit with link. WMO feeds: credit the issuing agency named in the alert.

## Source selection per refresh

1. Start from the features asked for (`forecast`, `current`, `alerts`, `airQuality`, …).
2. For each feature, take the sources that **cover the place** (coverage is data: a bounding box,
   country list or "worldwide", from configuration), are **turned on** (SOURCES.md, off until
   relevant) and have what they need (a key, consent for best-effort sources).
   Order them by tier, then resolution, then recent health; a source picked by hand for this place
   and feature wins while it covers the place (ENGINE_SPEC.md section 6).
3. Drop sources still **fresh** for that feature (below their wait, from configuration; defaults:
   forecast 15 min, current 10 min, alerts 5 min, air quality 1 h).
4. Drop sources **over their limit** (below).
5. Execute requests under the approved public request/result contract. M0.2 prefers batch-first for the first release; this old proposal to publish a stream as each source answers is not an adopted API.
6. A source that fails: its last good data may be returned only with original timestamps and an explicit stale state; a permitted next source may provide a fallback with its reason recorded. Represent partial feature outcomes without hiding failures. Cancellation must retain its cancellation semantics.

## Rate limits

One limiter per source, shared across places and callers (the CLI and the app count together):

| Source | Limits (from its docs) | Meridian's own ceiling |
|---|---|---|
| Open-Meteo | 600 / min, 5,000 / h, 10,000 / day, with call weights (more variables or days count as more calls) | half of each |
| aviationweather.gov | 100 / min | 20 / min |
| WMO feeds | none published | 1 per feed per 5 min |
| FPAS | congestion state in `/config/server_status` | 1 per 5 min, stop while congested |

All numbers live in configuration. When a limit is near, the limiter delays; when a source answers
429 or 503, it backs off (doubling from 1 min up to 1 h, honouring `Retry-After`). Call weights for
Open-Meteo are estimated from its documented rule and counted, not guessed later.

## Caching

- HTTP: the client's cache honours `Cache-Control`, `Expires`, `ETag` and `Last-Modified`.
- Meridian's own cache: last parsed result per source, place and feature, with its expiry, so a cold
  start shows something before any network call.
- Places that are close (configuration, default 1 km) and use the same source share cached forecasts.

## Storage boundary (proposal only)

The old Room-backed interface and single `deleteOlderThan`/30-day policy below are not adopted. M0.2 defines optional persistence and the host-provided storage boundary. Keep these categories distinct:

- Response cache with provider-aware expiry and freshness.
- Issued forecast snapshots and matched observations needed for verification.
- Recent local learning/verification records, initially a bounded 30-day rolling window when M7 is approved.
- Long-term historical/climatological datasets, with separate licence and retention rules.

The storage contract must support category-specific retention, clear/export/delete semantics, transactions, concurrency, and operation without persistent storage. The app owns saved places and user-facing privacy controls. Meridian must not persist a saved-place list or track locations in the background. Exact coordinate precision and keying are M0.2 decisions; retain only the minimum required for valid matching.

## Tests

- Selection: a fresh source isn't asked; an uncovered one isn't asked; a failing one falls back.
- Limiter: 1,000 simulated refreshes never exceed any ceiling; 429 backs off.
- Provenance: every value in a result has a contributor; credits list only used sources.
- Storage: the public contract works with in-memory/no-persistence operation; any optional store passes the same contract tests, including category-specific retention, atomicity, concurrency, and deletion.

## Done when

Results show their sources; no source is called more often than allowed (MERIDIAN_PLAN.md section 9).

## Consulted

Meridian's docs (ENGINE_SPEC.md sections 6 and 9); Open-Meteo's terms and API pages (limits, call weights); aviationweather.gov's Data
API page (limits); the FPAS OpenAPI description (server status).
