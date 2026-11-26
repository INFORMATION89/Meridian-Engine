# Meridian — AI Cost and Resource Efficiency Policy

**Status:** Approved project policy  
**Scope:** AI-assisted engineering, research, repository operations, verification, and test execution for Meridian.  
**Authority:** This policy complements `AI_POLICY.md`, `RULES.md`, and `AGENTS.md`. It governs efficient use of AI/tooling and verification resources; it does not weaken correctness, privacy, security, licensing, or release requirements.

## 1. Governing principle

**Use the least costly workflow that provides sufficient evidence for the task. Never reduce a required level of correctness or safety merely to save resources.**

Meridian must remain usable without any AI provider, AI runtime, model, credential, hosted inference service, or token expenditure. This policy applies to contributors and development agents only; it creates no AI dependency or AI cost for Meridian users.

Cost includes more than model tokens:

- AI input/output tokens, model calls, and paid tool credits;
- repository, search, browser, and external-service requests;
- GitHub Actions minutes, hosted runner usage, and repeated CI jobs;
- weather-provider calls, rate limits, historical-data downloads, and licensed datasets;
- local CPU, memory, disk, network, and battery use during tests or analysis;
- human review time caused by oversized, duplicated, or poorly scoped changes.

Do not invent a monetary budget or assume a service is free. Record actual usage when it is exposed by the tool; label estimates as estimates.

## 2. Default spending and authorization

1. Use existing approved tools, repository facilities, local scripts, cached data, and free CI capacity where they satisfy the task.
2. Do not create a new subscription, purchase credits, enable a paid model, consume a paid API tier, rent infrastructure, or incur a new recurring charge without explicit owner approval.
3. Do not bypass provider rate limits, usage budgets, terms, or attribution rules to complete a test faster.
4. Do not use production credentials or private developer keys to make routine tests work.
5. When a task approaches a quota, budget, or usage limit, stop optional work, preserve required checks, and report the constraint. Do not silently switch to a paid service.
6. A change to this policy, an increase in an approved budget, or a new paid capability requires an explicit decision recorded in the project's authoritative documentation.

## 3. Plan before using AI or tools

Before a non-trivial task:

1. State the exact outcome and acceptance criteria.
2. Identify the smallest relevant set of files, APIs, tests, and evidence.
3. Choose the least expensive suitable model, tool, and verification level.
4. Reuse current-session context and repository evidence when the branch, commit, toolchain, and relevant inputs have not changed.
5. Separate required work from optional exploration. Complete the required work first; perform optional research only when it can change the decision or materially reduce risk.

Stop investigating when the evidence is sufficient to make and verify the requested change. Do not continue searching merely to collect more examples or produce a longer report.

## 4. AI context and token efficiency

- Read the required project instructions, then inspect only the relevant files and line ranges. Do not load the entire repository or repeat large file reads without a reason.
- Prefer targeted search and relevant excerpts before fetching complete files.
- Reuse a verified result only when it still refers to the exact relevant commit, file version, toolchain, and environment. Re-check when any of those inputs change.
- Keep working context focused on the active task. Summarize necessary findings when handing work between sessions or agents; do not carry unrelated discussion, duplicate logs, or large raw outputs forward.
- Prefer concise prompts, structured findings, and small diffs. Avoid repeatedly restating instructions, regenerating unchanged documents, or producing large outputs that no reviewer needs.
- Use deterministic scripts, formatters, compiler diagnostics, and local repository tools for mechanical work instead of spending model calls on tasks those tools perform reliably.
- Use the least costly model/capability that can complete the task safely. Escalate to a more capable or costly model only for a concrete complexity or risk that the current method cannot handle.
- Do not fan out multiple agents for one task unless the subtasks are genuinely independent and parallel work reduces total cost or risk. Assign non-overlapping scopes and avoid duplicate repository exploration.
- Do not ask an AI to regenerate information already available in a trustworthy current source. Do not treat a model's confident answer as evidence.

### 4.1 Provider-aware model selection: ChatGPT and Claude

Choose the least costly model that can reliably complete the task. Model names and availability change; the examples below describe the current selection strategy, not permanent version locks. Check the provider's current official model catalogue and pricing before configuring an API workflow.

| Workload | ChatGPT guidance | Claude guidance |
|---|---|---|
| Routine, bounded work: formatting, extraction, short summaries, simple documentation or test edits | Use an efficient model such as the currently available Luna-class option | Use Haiku-class models |
| Standard coding: scoped implementation, ordinary debugging, focused code review | Use a balanced coding model such as the currently available Sol-class option | Use Sonnet-class models |
| Advanced engineering: cross-module architecture, difficult root-cause analysis, complex concurrency, high-risk refactors, security-sensitive review | Use the strongest suitable reasoning model, such as the current Astra-class option, with effort matched to risk | Use **Claude Opus 5.5** when its stronger reasoning materially reduces risk, uncertainty, or rework |
| Critical decisions: product architecture, privacy, legal, security, or irreversible changes | Use an advanced model to prepare evidence and alternatives; owner approval remains mandatory | Opus 5.5 may provide a specialist analysis or independent challenge; it does not replace owner approval |

#### ChatGPT-specific rules

- Distinguish ChatGPT web conversations, ChatGPT Work/Codex subscription usage, and OpenAI API-key billing. They have separate usage limits, credits, and pricing; API token rates must not be used to estimate subscription task allowances.
- Use Work/Codex when repository-aware, terminal, or multi-step execution materially reduces handoffs. Do not start an autonomous workflow for a trivial deterministic edit.
- Select reasoning effort proportionally. High effort is not the default for formatting, mechanical edits, or straightforward lookups.
- Check current account entitlements and model availability; do not assume a particular plan, credit balance, or model access.

#### Claude-specific rules

- Use Haiku for routine, bounded tasks; Sonnet for ordinary engineering; reserve **Claude Opus 5.5** for complex architecture, long-running coding agents, difficult debugging, large refactors, and independent review of consequential changes.
- Claude Code is appropriate when repository-aware execution is useful. Give it a bounded task, allowed scope, checkpoints, and a stop condition; do not leave it running indefinitely.
- Use adaptive thinking / effort controls proportionally. Do not request maximum effort by habit.
- If using the API, verify the current model ID and pricing, including input, output, cache writes/reads, batch pricing, long-context tiers, and any fast-mode multiplier. Do not infer API cost from Claude subscription usage.

#### Multi-agent coordination

- Assign one primary owner agent to each coherent task. That agent owns the plan, implementation, verification, diff review, and handoff.
- Add a second model only for a genuinely independent contribution: adversarial design review, a separate debugging hypothesis, a focused security/privacy review, or a final diff review where the risk warrants it.
- Do not ask ChatGPT and Claude to duplicate repository exploration, implement the same change independently, or run identical full test matrices by default.
- Give a specialist a concise brief with the relevant files/diff, constraints, acceptance criteria, and exact question. Do not resend the full project history.
- Resolve disagreements by checking code, official documentation, reproducible evidence, or a small discriminating test—not by model confidence or majority vote.
- A second model's approval is not proof of correctness. The primary agent remains responsible for validating all suggestions.

#### Paid usage boundary

The existing approval rule in section 2 applies: do not enable a paid API, paid model, credits, subscription, or higher-cost service tier without explicit owner approval. Existing subscription allowances must still be treated as finite resources. If a task approaches a usage limit, stop optional work and report it rather than silently switching to paid API billing.

Official references:
- [ChatGPT models](https://learn.chatgpt.com/docs/models)
- [ChatGPT Work and Codex pricing / usage](https://learn.chatgpt.com/docs/pricing)
- [OpenAI API pricing](https://platform.openai.com/pricing/)
- [Claude Opus 5.5 model overview](https://platform.claude.com/docs/en/models/opus-5-5/overview)
- [Claude API pricing](https://platform.claude.com/docs/en/about-claude/pricing)
- [Claude plans and pricing](https://claude.com/pricing)

## 5. Tool and request efficiency

1. Batch independent, read-only lookups when it reduces overhead and does not obscure dependencies.
2. Use narrow repository or web searches, then expand only when results are incomplete or contradictory.
3. Avoid repeated polling, repeated fetching, or rerunning commands when no relevant input or state has changed.
4. Retry only for a diagnosed transient failure or after changing the relevant condition. After a repeated failure, stop and investigate rather than entering an automatic retry loop.
5. Prefer local fixtures, cached artifacts, lockfiles, and reproducible reports over repeated live requests.
6. Do not download a complete historical archive when a small representative sample is sufficient for development. Use the full authorized dataset for the final scientific evaluation when the change requires it.
7. Keep tool output proportionate: capture the failure, summary, or evidence needed for the decision rather than copying thousands of unchanged lines into the working context.

## 6. Cost-aware test selection

Test quantity must be justified by distinct behaviours, boundaries, and failure risks—not by a target number of tests. Avoid both under-testing and redundant test volume.

### Select tests according to the changed surface

| Change type | Expected verification |
|---|---|
| Documentation-only | Markdown/link checks and focused review of the diff. Do not run the full platform matrix, weather archive evaluation, or live provider calls unless the documentation changes executable workflows. |
| Isolated deterministic logic | Focused unit tests, including relevant boundary and regression cases. |
| Provider adapter or parser | Fixture-based contract tests for valid, missing, malformed, stale, partial, and failure responses as applicable. Routine pull requests remain offline. |
| Public API, storage contract, concurrency, cancellation, or serialization | Relevant contract and integration tests at the affected consumer boundary; include migration or compatibility checks when persistent/public contracts change. |
| Shared core or platform driver | Tests for affected source sets and platform targets. Expand to the full supported matrix when shared behaviour or compatibility may be affected. |
| Forecast blend, calibration, weighting, observation matching, or other meteorological behaviour | Deterministic unit/fixture tests plus the relevant versioned historical verification. Use a small representative subset during iteration when valid, then run the complete approved evaluation for the final candidate. |
| Security-sensitive, licensing, supply-chain, release, or broad compatibility change | All checks required by the affected risk and release gate. Do not downgrade these to a small smoke test. |

### Avoid duplicate test work

- Each test should protect a meaningful behaviour, boundary, regression, or failure mode.
- Do not add several tests that assert the same outcome at the same abstraction boundary without a distinct reason.
- Prefer one strong contract test and focused boundary-specific tests over repeated copies of the same scenario across layers.
- Do not test trivial data holders, generated code, or framework behaviour already covered by a trusted dependency unless Meridian adds meaningful behaviour at that boundary.
- Keep test data minimal but representative. Reuse versioned fixtures when their provenance and purpose remain valid.
- Do not delete valuable tests merely to reduce counts. Remove or consolidate them only when equivalent coverage is demonstrated.

### CI and expensive verification

- Run fast, relevant checks for each change. Use path-aware or dependency-aware CI selection where it is reliable.
- Do not trigger five-platform builds for a documentation-only edit that cannot affect compilation or runtime.
- Run affected platform jobs for platform-specific changes and the full supported matrix when shared core, public API, platform drivers, dependency targets, release packaging, or advertised compatibility is affected.
- Run expensive archive evaluations when weather algorithms, data matching, weights, calibration, or forecast behaviour materially changes—not for unrelated formatting, documentation, or mechanical refactoring.
- Do not rerun a successful check for the same commit and environment without a reason. A new relevant commit, changed runner/toolchain, incomplete result, or diagnosed transient failure can justify a rerun.
- Use CI caching and existing artifacts when valid. Never let a cache conceal stale inputs or replace the release verification gate.
- Required checks still block merge and release. A cost-aware selection may omit irrelevant checks, but not a required check for the changed risk.

## 7. Weather-provider and dataset usage

- Normal automated tests must use deterministic fixtures and must not depend on live weather providers.
- Live-provider checks are limited to cases where an adapter's real external behaviour cannot be established through fixtures. Keep them manual or scheduled where appropriate, minimally scoped, and within provider terms and limits.
- Do not repeatedly query providers to obtain identical test data. Record permitted responses as fixtures with source, retrieval date, licence, and sanitization details.
- Historical archive collection and large evaluations must have a defined research question, permitted dataset, time/region/variable scope, storage estimate, and evaluation method before download.
- Reuse an authorized versioned dataset for repeated experiments. Do not silently expand its retention or redistribute data beyond its licence.
- Keep provider request cost separate from AI-token cost. A model or tool saving tokens does not justify wasting provider quotas or downloading unnecessary data.

## 8. Bounded iteration and stopping rules

- Iterate in small steps: inspect, make a focused change, run the relevant checks, review the diff, and stop when acceptance criteria are met.
- Do not run broad refactors, repeated regeneration, speculative benchmarks, or extra test suites outside the task scope.
- If a check fails, first identify whether the cause is the change, environment, dependency resolution, external service, or test harness. Fix the cause before repeating.
- If the remaining uncertainty is non-critical and further verification would be disproportionately costly, state the limitation and propose the narrowest follow-up check. Do not misrepresent the result as verified.
- If a required high-cost check cannot be run within existing approved capacity, leave the change unmerged or unreleased until an approved verification path is available.

## 9. Owner approval and escalation

Ask the owner before:

- using a paid AI model, paid agent/tool credit, paid API, subscription, hosted runner, cloud service, or new infrastructure that incurs a charge;
- exceeding an explicitly approved usage or spending budget;
- downloading or processing a large licensed dataset outside an already approved research scope;
- repeatedly running long or expensive test matrices without a clear acceptance need;
- introducing a recurring external dependency or background process with ongoing cost;
- disabling or weakening a required security, compatibility, privacy, or scientific release gate;
- taking an external, destructive, publishing, or irreversible action.

Routine choices—such as selecting the focused unit tests, narrowing a search, reusing a valid fixture, or choosing a low-cost local tool—do not require owner approval when they stay within the approved task and project rules.

## 10. Pull-request and task reporting

For non-trivial work, report:

- the task scope and acceptance criteria;
- the checks selected and why they cover the changed risk;
- the exact tests and CI jobs run, including commit SHA and relevant environment;
- expensive or external calls made, where the usage is known;
- tests or checks intentionally not run and why they were irrelevant, deferred, unavailable, or blocked;
- any retry, quota, budget, or provider limitation;
- remaining risks and the next required verification gate.

Never claim that an unrun test, platform, live provider, archive evaluation, or licence review passed. Do not report estimated token or monetary usage as measured usage.

## 11. Relationship to other project policies

- `AI_POLICY.md` defines what AI may and may not do during engineering/research and prohibits runtime AI dependencies.
- `AI_COST_POLICY.md` defines how contributors and agents minimize AI, tool, CI, provider, dataset, and compute costs while preserving necessary verification.
- `RULES.md` defines project-wide engineering, testing, security, and contribution requirements.
- `AGENTS.md` defines the operational instructions and mandatory reading order for coding agents.
- `CLEAN_ROOM.md` governs permitted research sources and independent implementation.

If policies appear to conflict, do not silently choose the cheaper path. Preserve safety, privacy, legal compliance, scientific integrity, and the required acceptance gate; report the conflict and seek owner review when the authority hierarchy does not resolve it.
