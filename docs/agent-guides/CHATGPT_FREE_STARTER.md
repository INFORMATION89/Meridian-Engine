# ChatGPT Free — Session Starter

**Scope:** Contributors using the free ChatGPT plan with GitHub repository access to work on Meridian.  
**Status:** Optional provider-specific supplement. It does not make ChatGPT or any model mandatory.  
**Last reviewed:** 2026-11-28  
**Authority:** `PLAN.md`, `AGENTS.md`, `RULES.md`, `AI_POLICY.md`, `AI_COST_POLICY.md` and `AI_IMPLEMENTATION_PLAN.md` section 13 remain authoritative. This guide cannot override them.

## Why this exists

ChatGPT Free can open branches, push commits and open pull requests. Work done this way during 2026-11-26/27 was technically useful, but it showed avoidable process problems:

- many small commits for one change (for example 14 commits for a 32-line Wiki edit);
- code commits tagged `[skip ci]`, so the code never ran in CI;
- dependency checksums typed in by hand;
- one PR stacked on another unmerged PR's commits while targeting `main`;
- an abandoned 127-commit branch with no PR.

The rules below prevent those problems. `.github/workflows/pr-hygiene.yml` enforces two of them automatically.

## Starter prompt

Paste this at the start of every ChatGPT Free session, filling in the task ID:

```text
You are working on the Meridian-Engine repository (INFORMATION89/Meridian-Engine).
Before doing anything, read AGENTS.md, then AI_IMPLEMENTATION_PLAN.md section 13.

Your task is: <TASK ID, e.g. H3 or F2>. Do only this task. If it is assigned to the O (Opus) tier,
stop and tell me instead of doing it.

Rules:
1. Work on one new branch created from the current main, unless the task names another base.
   Never push to main. Never merge a PR.
2. Plan the whole change first, then push it in as few commits as possible (normally 1-3).
   Do not push a commit per small fix.
3. Never use [skip ci] or any other CI-skip marker on a commit that changes anything other than
   documentation (*.md, docs/, wiki/).
4. Never hand-edit generated files: gradle.lockfile, gradle/verification-metadata.xml, generated
   bindings or build output. If they need to change, tell me which Gradle command regenerates them.
5. Do not change public API types, cancellation or concurrency behaviour, storage write semantics,
   dependencies or CI workflows unless the task explicitly says so.
6. Never paste, print or commit secrets, tokens, private data or precise personal locations.
7. Open the PR using .github/pull_request_template.md and fill in every section, including
   "Tool and tier: ChatGPT Free, tier F".
8. If CI fails, find the cause before pushing again. After two failed fixes, stop and report.
9. Finish with a short handoff: branch, commit SHA, what changed, checks run, what is not verified.
```

## Tasks it may take

Tier F tasks in `AI_IMPLEMENTATION_PLAN.md` section 13, plus Tier H tasks when Haiku is not in use. Tier S tasks only when the owner assigns them explicitly and the guardrails above are followed. Never tier O tasks.
