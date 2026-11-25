# Meridian research documents

**Status:** supplementary research and design proposals for owner review  
**Prepared:** 2026-11-25  
**Repository:** INFORMATION89/Meridian-Engine

These documents prepare work that can proceed alongside MER-0.1 without changing the production engine or authorizing implementation. They are intentionally non-authoritative.

## Reading order

1. [Architecture review](ARCHITECTURE_REVIEW.md) — current architecture, evidence, risks, conflicts, and decisions that need owner approval.
2. [Weather data model](WEATHER_DATA_MODEL.md) — proposed semantics for observations, forecasts, derived values, time, units, quality, freshness, and provenance.
3. [Weather intelligence](WEATHER_INTELLIGENCE.md) — meteorological rules, source selection, blending, verification, and regional test strategy.
4. [Provider research](PROVIDER_RESEARCH.md) — provider capabilities, access conditions, limitations, and research tasks.
5. [Remaining work](REMAINING_WORK.md) — review checklist, unresolved evidence, owner decisions, and gates before MER-1.

## Authority and change control

These documents do not override:

- `PLAN.md` — master scope, architecture, milestones, and technical direction.
- `RULES.md` — project rules.
- `CLEAN_ROOM.md` — permitted research and independent implementation policy.
- `AGENTS.md` — coding-agent instructions.
- An approved milestone specification and an explicit owner decision.

A recommendation in these documents is not an implementation instruction. Before a recommendation becomes binding, record the decision in the authoritative project document, update the relevant milestone specification, and obtain the required owner approval. Keep the proposal marked as pending until that happens.

## Research discipline

- Use public standards, scientific publications, official provider documentation, API specifications, and provider terms.
- Do not inspect another weather project's implementation as a template.
- Record the date on which changing terms, pricing, endpoints, coverage, and rate limits were checked.
- Distinguish provider claims, measured evidence, engineering recommendations, and open questions.
- Treat all numeric thresholds as proposals until they have a stated rationale and validation.
- Do not include credentials, user location histories, or private datasets.
- Do not treat reanalysis, climatology, an observation, and an issued forecast as interchangeable.

## Current boundary

The current engineering work remains MER-0.1 stabilization. These documents prepare the decisions and research needed for MER-0.2, MER-0.3, and later weather milestones. They do not authorize production provider adapters, production persistence, forecast algorithms, blending, alerts, learning, radar, telemetry, hosting, or AI runtime integration.

## Research snapshot

External material in these documents was checked on 2026-11-25. Provider terms and operational facts can change; re-check them immediately before implementation or release.