# M0.3 — AI readiness and agent workflow specification

**Status:** proposed design milestone; requires M0.1 and M0.2 decisions.  
**Parent documents:** [`PLAN.md`](../../PLAN.md), [`AI_POLICY.md`](../../AI_POLICY.md), [`AGENTS.md`](../../AGENTS.md).  
**Scope:** make Meridian straightforward and safe to consume from AI applications and coding agents, without adding a model dependency or making the engine AI-dependent.

## 1. Objective

Prove that an independent consumer (not Solstice) can query Meridian through a stable, typed, machine-readable API and obtain weather data with enough metadata for an AI assistant to explain it accurately.

This milestone is about contracts and boundaries, not integrating a production LLM.

## 2. Required design outputs

1. **Independent-consumer proof:** a minimal sample application or CLI uses Meridian only through its public API. It must build and run without checking out, compiling, or importing Solstice.
2. **Machine-readable schemas:** document the public weather result, source/provenance, freshness, uncertainty, missing/stale/error states, and official alert representations.
3. **Read-only query contract:** define typed queries for current conditions, hourly/daily forecast, observations, alerts, source status, attribution, and provenance. Final names follow the approved M0.2 public API.
4. **AI context contract:** define how an application can assemble a minimized, typed context for a model without passing database rows, provider DTOs, credentials, unrelated location data, or hidden internal fields.
5. **Tool contract:** document a small allowlist of read-only weather functions. Each function must define schema, units, time semantics, permissions, bounded result sizes, typed errors, and versioning.
6. **Protocol decision:** assess whether an optional MCP adapter is useful. MCP or another agent protocol must remain outside the core domain API. Do not add a protocol dependency unless a concrete consumer needs it.
7. **Evaluation plan:** define a versioned, human-reviewed set of weather questions and expected evidence/claims. Include missing, stale, conflicting, regional, Spanish (Mexico), and official-alert cases.
8. **Threat model:** document indirect prompt injection through alert/provider text, data leakage, tool misuse, excessive agency, output handling, unbounded use, and fallback behaviour.
9. **Agent workflow:** ensure `AGENTS.md` and `AI_POLICY.md` define least privilege, untrusted repository content, branch/PR requirements, truthful test reporting, and human approval gates.

## 3. Suggested read-only tools (illustrative only)

The public API and M0.2 decision determine the final names. Candidate capabilities:

- query current conditions for a supplied place and feature set;
- query hourly/daily forecast over a bounded time window;
- list observations with station and measurement timestamps;
- list official alerts with issuing authority and full lifecycle metadata;
- inspect source candidates, status, freshness, and required attribution;
- retrieve provenance for a returned value or result.

Do not provide tools that write to provider configuration, alter alert state, change user settings, delete data, contact external parties, execute code, access arbitrary files, or browse arbitrary URLs.

## 4. Truth and provenance requirements

- The deterministic engine remains the source of numeric weather facts.
- An AI-generated explanation is a separate derived artifact, never a weather observation or authoritative forecast.
- Every weather claim in an AI output must map to the structured context and relevant source/provenance reference.
- Missing, stale, failed, uncertain, and contradictory inputs must be represented explicitly.
- An AI explanation must not alter official alert text, issuing authority, severity, urgency, certainty, onset, expiry, or cancellation state.
- AI failure, timeout, refusal, or schema-validation failure must fall back to ordinary deterministic weather results.

## 5. Security and privacy validation

Test the boundaries with deliberately adversarial fixtures, including provider and alert text containing instruction-like strings such as requests to reveal secrets, ignore rules, invoke tools, or send data elsewhere. Confirm that such text remains data and cannot grant tools or permissions.

Verify:
- tool calls are allowlisted and read-only;
- all tool arguments are schema-validated;
- exact location data is minimized for the task;
- credentials and unrelated personal data never enter the AI context;
- model output is validated and escaped before rendering;
- request size, time, retries, and cost can be bounded;
- no-LLM operation leaves core weather functions intact.

## 6. Acceptance criteria

M0.3 is complete only when:

1. A sample consumer queries Meridian without Solstice or any Solstice dependency.
2. The weather result and provenance contracts are machine-readable, versioned, and documented.
3. A read-only tool schema can retrieve forecasts, current conditions, alerts, and source/provenance details.
4. An AI context can be assembled with explicit data minimization and no credentials.
5. Adversarial provider/alert fixtures cannot expand tool permissions or override trusted instructions.
6. Official-alert fidelity and deterministic fallback are specified and covered by tests.
7. A versioned evaluation plan exists, with test cases and pass/fail thresholds reviewed by the owner.
8. The decision on MCP or another protocol is documented; no agent protocol has been coupled to the weather core.
9. No production LLM provider, prompt, model download, vector database, agent runtime, or AI inference is required for Meridian's core tests.

## 7. Explicitly out of scope

Do not select a production LLM vendor, embed API keys, fine-tune a model, build a vector database, add autonomous multi-agent orchestration, create a hosted AI backend, or make AI necessary for forecasts. Each such feature requires its own specification, privacy/security review, evaluation plan, and owner approval.
