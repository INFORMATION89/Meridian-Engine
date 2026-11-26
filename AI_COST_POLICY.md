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

## 3. Subscription limits and usage accounting

AI use may consume an included subscription allowance even when no per-token invoice is issued. Treat that allowance as a finite project resource, not as unlimited usage.

- Identify the real access path before estimating use: a subscription/seat allowance, prepaid or extra-usage credits, direct API billing, a third-party cloud-provider account, or local inference. These are different billing and limit systems.
- For subscription access, do not convert usage into dollars using public API token prices. Monitor the plan's actual usage and reset windows through the provider's own usage screen or supported usage command. Do not claim a precise remaining allowance unless the account exposes it.
- For direct API or third-party billing, use that account's usage meter and current pricing. Do not assume subscription credits cover API-key use or that API prompt-caching discounts apply to subscription limits.
- Never enable extra-usage credits, API fallback, auto-reload, paid overages, or a different paid provider to bypass an included-plan limit without explicit owner approval.
- A provider's model context window is not the same as the plan's usage allowance. A session may fit in the context window and still consume a substantial part of a subscription's rolling or weekly quota.
- Record actual quota/usage observations only when visible. Otherwise say that exact consumption is unknown; do not infer a percentage from message counts or token estimates.

## 4. Budget the work, not just the prompt

Before a long or multi-step coding task:

1. Confirm the objective, acceptance criteria, and smallest useful deliverable. Separate required work from optional cleanup or research.
2. Check the current plan/session allowance when the provider exposes it. Avoid repeated usage checks when nothing has changed.
3. Decide which assistant or tool owns each phase. When a human or another assistant has already researched and planned the task, give the execution agent a concise handoff instead of asking it to repeat the same repository exploration.
4. Split large work into bounded tasks or PRs. Do not keep one conversation open indefinitely to preserve context; do not start a new conversation for every tiny follow-up either.
5. Prefer one active agent session for a task. Parallel sessions and subagents multiply model usage and should be used only for genuinely independent work whose benefit justifies that extra allowance.
6. When the allowance is getting low, prioritize finishing and verifying the current scoped task. Defer optional research, cosmetic refactors, duplicate reviews, and unrelated follow-ups rather than spending the remaining quota on them.
7. Finish with a concise, durable handoff in the PR or project notes so another session can continue without replaying the whole conversation.

A task is efficient when it is correctly completed and verified with proportionate usage—not when it uses the fewest messages at the expense of rework.

## 5. Plan before using AI or tools

Before a non-trivial task:

1. State the exact outcome and acceptance criteria.
2. Identify the smallest relevant set of files, APIs, tests, and evidence.
3. Choose the least expensive suitable model, tool, and verification level.
4. Reuse current-session context and repository evidence when the branch, commit, toolchain, and relevant inputs have not changed.
5. Separate required work from optional exploration. Complete the required work first; perform optional research only when it can change the decision or materially reduce risk.

Stop investigating when the evidence is sufficient to make and verify the requested change. Do not continue searching merely to collect more examples or produce a longer report.

## 6. AI context and token efficiency

Context management is both a usage-control measure and a correctness measure. More context is not automatically better. A growing session can consume more of a subscription allowance or billable input, occupy more of the model's context window, and make important decisions harder to retrieve. The exact effect depends on the provider, product surface, caching, and account plan.

- Organize work into bounded tasks, normally one issue, one milestone subtask, or one coherent PR at a time. Keep the objective and acceptance criteria visible.
- Keep always-loaded agent instructions concise. Put detailed procedures in referenced policies, milestone specifications, or optional guides instead of duplicating them across every instruction file.
- Read mandatory instructions and only the relevant sections of long plans. Use targeted repository search and line ranges before fetching whole files. Do not load the full repository or an entire master plan for routine implementation tasks.
- Avoid repeatedly pasting or reading the same code, specifications, logs, generated files, build output, or conversation history. Refer to file paths and request focused excerpts.
- Reuse a verified result only when it refers to the exact relevant commit, file version, toolchain, and environment. Do not repeat repository exploration when nothing relevant has changed.
- Keep the active conversation focused. When switching to an unrelated task, start a fresh session/conversation when appropriate. When continuing a long task, use the provider's compaction or summary mechanism and verify that essential decisions, commit identifiers, tests, blockers, and next actions survived.
- At a task boundary, preserve a concise durable handoff: repository and branch, exact commit, objective, approved scope, decisions, files changed, verification, limitations, and next action. Put durable decisions in repository documents or PRs, not only in chat history.
- Context capacity, subscription allowance, API billing, and caching are separate concepts. Cached input may reduce repeated-input charges or count differently against some plans, but it still occupies context capacity. Never assume an API cache discount applies to subscription usage.
- Prefer concise prompts, structured findings, small diffs, and proportionate tool output. Avoid repeatedly restating instructions or producing long reports that no reviewer needs.
- Use deterministic scripts, formatters, compiler diagnostics, and local repository tools for mechanical work instead of asking a model to reason through output a tool can verify.
- Choose a model and reasoning/effort level appropriate to task risk. A lighter or default model is usually sufficient for routine implementation; reserve heavier reasoning for complexity that warrants it. Model names and effort levels are not directly comparable across providers.
- Measure efficiency by correctly completed and verified work per unit of available usage, not by token price alone. A cheaper model that needs repeated retries or extensive human repair may cost more overall.
- Do not fan out agents for a single task unless the subtasks are genuinely independent and the benefit outweighs the separate context and model usage of each agent.
- Do not ask an AI to regenerate information already available in a trustworthy current source. Do not treat a model's confident answer as evidence.

### Provider-aware model selection: ChatGPT and Claude

For standard engineering, **Claude Sonnet and a balanced ChatGPT coding model are interchangeable defaults**. Pick one primary agent based on repository/tool access, capability for the scoped task, and available usage. Do not run both in parallel on the same implementation. Use Opus only when the task's complexity or risk justifies escalation.

Choose the least costly model that can reliably complete the task. Model names and availability change; the examples below describe the current selection strategy, not permanent version locks. Check the provider's current official model catalogue and pricing before configuring an API workflow.

| Workload | ChatGPT guidance | Claude guidance |
|---|---|---|
| Routine, bounded work: formatting, extraction, short summaries, simple documentation or test edits | Use an efficient model such as the currently available Luna-class option | Use Haiku-class models |
| Standard coding: scoped implementation, ordinary debugging, focused code review | Balanced coding model | Sonnet-class models |
| Routine coding: clear components, endpoints, tests, scoped refactors, standard docs, fast iteration | Interchangeable with Sonnet for this tier | Default model for this tier; interchangeable with balanced ChatGPT coding models |
| Advanced engineering: complex architecture/trade-offs, cross-repository reasoning, long-context work, difficult concurrency/system bugs, complex specifications, stubborn failures | Use a suitable advanced reasoning model with effort matched to risk | Escalate to **Claude Opus** when its stronger reasoning materially reduces risk or rework; may plan, implement, or independently review as appropriate |
| Critical decisions: product architecture, privacy, legal, security, or irreversible changes | Use an advanced model to prepare evidence and alternatives; owner approval remains mandatory | Opus may provide an independent review; it does not replace owner approval or authorize additional spend |

#### ChatGPT-specific rules

- ChatGPT/Codex can be the primary agent for advanced work and should not be blocked while waiting for another model. When the task would materially benefit from a stronger independent pass, label the handoff **“Opus review pending”** and schedule it at a useful checkpoint or before merge.
- This is a recommendation, not a mandatory dependency or a guarantee that Opus must be used. If Opus is unavailable or would require unapproved paid usage, continue with ChatGPT/Codex and proportionate evidence-based review; record that the recommended pass was deferred or not performed.

- Distinguish ChatGPT web conversations, ChatGPT Work/Codex subscription usage, and OpenAI API-key billing. They have separate usage limits, credits, and pricing; API token rates must not be used to estimate subscription task allowances.
- Use Work/Codex when repository-aware, terminal, or multi-step execution materially reduces handoffs. Do not start an autonomous workflow for a trivial deterministic edit.
- Select reasoning effort proportionally. High effort is not the default for formatting, mechanical edits, or straightforward lookups.
- Check current account entitlements and model availability; do not assume a particular plan, credit balance, or model access.

#### Claude-specific rules

- Use Haiku for routine extraction/classification when that is sufficient, and Sonnet for everyday coding, clear feature work, routine fixes, tests, scoped refactors, standard documentation, and fast iteration. Sonnet and balanced ChatGPT coding models are interchangeable execution choices for this tier. Escalate to Opus for complex architecture, cross-repository reasoning, difficult bugs that resist normal debugging, complex specifications, or high-value independent review.
- Claude Code is appropriate when repository-aware execution is useful. Give it a bounded task, allowed scope, checkpoints, and a stop condition; do not leave it running indefinitely.
- Do not block implementation while waiting for Opus. Start with the suitable available model, then request the optional Opus review at a useful checkpoint or before merge when it can change the decision.
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
- [Claude Opus model overview](https://platform.claude.com/docs/en/about-claude/models/overview)
- [Claude API pricing](https://platform.claude.com/docs/en/about-claude/pricing)
- [Claude plans and pricing](https://claude.com/pricing)

### Optional advanced second review (Opus)

**Opus is recommended for tasks that benefit from an advanced second review, not as a requirement to begin work and not as automatic authorization to spend more.**

Use an Opus-class review selectively after a first implementation or analysis exists, when an independent deeper review can materially reduce risk. Good candidates include security-sensitive changes, architecture or public-contract decisions, concurrency/cancellation, cross-platform interoperability, data migrations, and scientific or forecast-algorithm changes.

For coding work:

1. Start with an appropriate available model and complete the scoped implementation or investigation. Do not wait for Opus before beginning routine work.
2. When risk justifies it, ask for a second review of the concise specification, changed diff, relevant tests/evidence, and unresolved questions—not the entire conversation or repository by default.
3. Ask the reviewer to look independently for correctness issues, hidden assumptions, missing boundary tests, security/privacy/licensing problems, compatibility regressions, and disagreements with the first result.
4. Verify every substantive finding. A second model's opinion is not proof, does not replace required tests, and does not waive owner approval for strategic decisions.
5. Use this review only within the already available and approved usage allowance. Do not activate paid overages, extra credits, an API fallback, a plan upgrade, or another paid provider to obtain it without explicit owner approval.
6. Skip the optional second review when the change is routine, low-risk, reversible, or when usage is scarce and the review is unlikely to change the decision. Record why it was or was not used for high-impact work.

The purpose is better assurance per unit of available usage—not more model calls for their own sake.

### Provider- and model-specific guides

Optional guides under `docs/agent-guides/` may describe practical usage meters, model choices, context controls, and billing behaviour for a specific product or coding environment. They must identify the provider, product surface, exact model/version where relevant, date reviewed, and official sources. They supplement this policy; they cannot override it or make one provider/model mandatory for Meridian. Do not put volatile prices, quotas, or model-specific commands in this policy.

## 7. Tool and request efficiency

1. Batch independent, read-only lookups when it reduces overhead and does not obscure dependencies.
2. Use narrow repository or web searches, then expand only when results are incomplete or contradictory.
3. Avoid repeated polling, repeated fetching, or rerunning commands when no relevant input or state has changed.
4. Retry only for a diagnosed transient failure or after changing the relevant condition. After a repeated failure, stop and investigate rather than entering an automatic retry loop.
5. Prefer local fixtures, cached artifacts, lockfiles, and reproducible reports over repeated live requests.
6. Do not download a complete historical archive when a small representative sample is sufficient for development. Use the full authorized dataset for the final scientific evaluation when the change requires it.
7. Keep tool output proportionate: capture the failure, summary, or evidence needed for the decision rather than copying thousands of unchanged lines into the working context.

## 8. Cost-aware test selection

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

## 9. Weather-provider and dataset usage

- Normal automated tests must use deterministic fixtures and must not depend on live weather providers.
- Live-provider checks are limited to cases where an adapter's real external behaviour cannot be established through fixtures. Keep them manual or scheduled where appropriate, minimally scoped, and within provider terms and limits.
- Do not repeatedly query providers to obtain identical test data. Record permitted responses as fixtures with source, retrieval date, licence, and sanitization details.
- Historical archive collection and large evaluations must have a defined research question, permitted dataset, time/region/variable scope, storage estimate, and evaluation method before download.
- Reuse an authorized versioned dataset for repeated experiments. Do not silently expand its retention or redistribute data beyond its licence.
- Keep provider request cost separate from AI-token cost. A model or tool saving tokens does not justify wasting provider quotas or downloading unnecessary data.

## 10. Bounded iteration and stopping rules

- Iterate in small steps: inspect, make a focused change, run the relevant checks, review the diff, and stop when acceptance criteria are met.
- Do not run broad refactors, repeated regeneration, speculative benchmarks, or extra test suites outside the task scope.
- If a check fails, first identify whether the cause is the change, environment, dependency resolution, external service, or test harness. Fix the cause before repeating.
- If the remaining uncertainty is non-critical and further verification would be disproportionately costly, state the limitation and propose the narrowest follow-up check. Do not misrepresent the result as verified.
- If a required high-cost check cannot be run within existing approved capacity, leave the change unmerged or unreleased until an approved verification path is available.

## 11. Owner approval and escalation

Ask the owner before:

- using a paid AI model, paid agent/tool credit, paid API, subscription, hosted runner, cloud service, or new infrastructure that incurs a charge;
- exceeding an explicitly approved usage or spending budget;
- downloading or processing a large licensed dataset outside an already approved research scope;
- repeatedly running long or expensive test matrices without a clear acceptance need;
- introducing a recurring external dependency or background process with ongoing cost;
- disabling or weakening a required security, compatibility, privacy, or scientific release gate;
- taking an external, destructive, publishing, or irreversible action.

Routine choices—such as selecting the focused unit tests, narrowing a search, reusing a valid fixture, or choosing a low-cost local tool—do not require owner approval when they stay within the approved task and project rules.

## 12. Pull-request and task reporting

For non-trivial work, report:

- the task scope and acceptance criteria;
- the checks selected and why they cover the changed risk;
- the exact tests and CI jobs run, including commit SHA and relevant environment;
- expensive or external calls made, where the usage is known;
- tests or checks intentionally not run and why they were irrelevant, deferred, unavailable, or blocked;
- any retry, quota, budget, or provider limitation;
- remaining risks and the next required verification gate.

Never claim that an unrun test, platform, live provider, archive evaluation, or licence review passed. Do not report estimated token or monetary usage as measured usage.

## 13. Relationship to other project policies

- `AI_POLICY.md` defines what AI may and may not do during engineering/research and prohibits runtime AI dependencies.
- `AI_COST_POLICY.md` defines how contributors and agents minimize AI, tool, CI, provider, dataset, and compute costs while preserving necessary verification.
- `RULES.md` defines project-wide engineering, testing, security, and contribution requirements.
- `AGENTS.md` defines the operational instructions and mandatory reading order for coding agents.
- `CLEAN_ROOM.md` governs permitted research sources and independent implementation.

If policies appear to conflict, do not silently choose the cheaper path. Preserve safety, privacy, legal compliance, scientific integrity, and the required acceptance gate; report the conflict and seek owner review when the authority hierarchy does not resolve it.
