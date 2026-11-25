# M0.3 — Reproducible verification, research, and optional chat integration

**Status:** Proposed design milestone; follows the M0.1 platform proof and M0.2 API/storage decisions.  
**Parent documents:** [`PLAN.md`](../../PLAN.md), [`AI_POLICY.md`](../../AI_POLICY.md), [`AGENTS.md`](../../AGENTS.md).  
**Scope:** prepare the project to improve weather quality through evidence, reproducible analysis, and human-reviewed changes. AI may assist contributors during development and research, but it is not part of Meridian's runtime.

## 1. Objective

Define how Meridian will collect, preserve, compare, and learn from forecast snapshots and later observations so that forecast quality can be improved deliberately.

The workflow must remain useful with ordinary code, scripts, fixtures, and manual analysis. A contributor may optionally use AI to explore a dataset or investigate a result, but every material finding must be independently reproducible and reviewed by a human.

## 2. Product boundary

M0.3 does not add an AI capability to Meridian.

The engine must not require:
- an LLM, generative model, ML inference service, model file, or AI provider;
- API keys, tokens, prompts, embeddings, vector databases, or an MCP server dependency in the weather core;
- background model requests or AI-generated user-facing text.

The public API remains structured and machine-readable because that is good library design for every consumer. Do not add prompts, model clients, or agent protocols to the weather domain. An optional MCP adapter outside the core may expose a controlled subset of the public API to a compatible chat client. Prefer an in-process or local adapter when the chat client runs on the same device and supports local MCP transports; this can avoid hosting a Meridian service. Important limitation: a cloud-hosted chat client cannot normally reach a phone's or laptop's localhost process. Connecting such a remote chat to a device-local Meridian instance would require a separately approved relay/tunnel or hosted endpoint, which violates the no-hosting default and must never be silently assumed. The intended local flow is chat host/model → local MCP client → optional local Meridian MCP adapter → Meridian public API → deterministic engine and weather sources. The adapter returns structured data and provenance; it does not invoke a model itself. The external chat host decides how to use the result and is responsible for generated wording and model costs.

## 3. Required design outputs

1. **Independent-consumer principle:** confirm that any sample or verification CLI consumes Meridian through its public API and never imports or builds Solstice.
2. **Forecast record contract:** define the metadata needed to preserve what a provider/model forecast actually said at a given issue time, including provider/model/product, issue/run time, fetch time, place/grid, variable, unit, forecast lead, valid time, and source version where available.
3. **Observation record contract:** define observation source, station/grid identity, coordinates or spatial reference at the minimum necessary precision, measurement time, fetch time, variable, unit, quality flags, and known representativeness limits.
4. **Matching rules:** specify how forecasts are paired with observations, including time-zone normalization, valid-time tolerance, station distance/elevation, duplicate handling, missing values, and quality exclusions.
5. **Evaluation methodology:** define baseline/candidate comparison and appropriate metrics, reported by variable, lead time, region, season, and source where sample sizes permit.
6. **Reproducibility record:** every experiment identifies dataset versions, algorithm/configuration versions, evaluation code, parameters, exclusions, sample counts, and results.
7. **Human approval workflow:** document how analysis findings become a reviewed algorithm/configuration change with regression tests; no automatic promotion or self-modifying production engine.
8. **Contributor AI workflow:** document permitted use of AI for coding and occasional manual data analysis, with data minimization, independent verification, least privilege, and explicit review under `AI_POLICY.md`.
9. **Optional chat/MCP assessment:** assess whether a local/in-process MCP adapter would be useful with chat clients that support local transports. Explicitly distinguish same-device clients from cloud-hosted clients that cannot reach localhost without a relay or hosted endpoint. Define allowed read-only tools, authentication, user-data exposure, provenance, failure behaviour, deployment, and costs. Do not make MCP a core dependency or assume it must be implemented in this milestone; do not introduce a relay or hosted endpoint under the no-hosting default.

## 4. Scientific and statistical safeguards

- Evaluate forecasts only against observations that were unavailable at the forecast's issue time.
- Keep tuning and final evaluation periods separate; prefer chronological holdouts for time-dependent weather data.
- Do not choose metrics after inspecting candidate results.
- Report sample counts and missing/excluded cases alongside scores.
- Compare candidates against the previous Meridian baseline and relevant individual source models.
- Inspect regressions as well as aggregate gains; do not let an aggregate score hide a poor region, variable, season, or forecast lead.
- Document observation quality, station representativeness, spatial matching, elevation differences, time alignment, and uncertainty.
- Treat AI-generated interpretations as hypotheses until deterministic analysis reproduces them.

## 5. Suggested analysis measures

Choose metrics according to variable and task, not convenience. Examples include:

- MAE and RMSE for continuous variables;
- mean error/bias for systematic over- or under-forecasting;
- categorical scores for threshold events when thresholds are defined in advance;
- probabilistic scores and calibration diagnostics when forecast distributions or probabilities are available.

No single score establishes universal forecast quality. Results should be segmented and accompanied by sample counts and uncertainty where practical.

## 6. Acceptance criteria

M0.3 design work is complete when:

1. Forecast and observation records have documented, versionable contracts with provenance and time semantics.
2. A deterministic forecast-verification methodology is documented, including matching, exclusions, data-leakage prevention, baseline comparison, and metrics.
3. An experiment can be reproduced from identified data, code, parameters, and algorithm/configuration versions.
4. The human review path from analysis report to algorithm/configuration change is explicit.
5. `AI_POLICY.md` and `AGENTS.md` govern coding agents and occasional AI-assisted research without authorizing runtime AI.
6. The milestone and architecture explicitly state that Meridian's core build, tests, runtime, and weather results require no AI provider, credentials, model, or token spend.
7. The optional MCP decision is documented separately from the engine API, with no MCP dependency in the core and no assumption that the user must use a chat to access weather data. Local MCP feasibility is distinguished from remote/cloud chat connectivity, including the hosting requirement of any relay.

## 7. Explicitly out of scope

Do not integrate an LLM, hosted AI backend, or MCP server into the weather core. A separately scoped, optional MCP adapter may be proposed after the public API is stable; its implementation is not implied by this milestone. Do not add a vector database, prompt framework, autonomous agent, AI weather assistant, or AI-generated briefing to Meridian itself. Do not automatically train, deploy, or promote a learned correction model. Do not implement production forecast algorithms or claim forecast improvements before the necessary forecast/observation archive and validation data exist.

Actual verification tooling and any later statistical/ML correction must be scoped by their own milestone after the source, observation, and forecast history contracts are stable.
