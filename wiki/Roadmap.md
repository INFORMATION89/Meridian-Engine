# Roadmap

This page is a high-level orientation. The authoritative scope, milestone gates, acceptance criteria, and current status live in [PLAN.md](https://github.com/INFORMATION89/Meridian-Engine/blob/main/PLAN.md).

## Current focus — M0.1: platform and production-stack proof

M0.1 is a bounded technical validation, not production weather-engine implementation. Its purpose is to evaluate shared-core and platform options, consumer interoperability, runtime behaviour, packaging, dependencies, and licensing.

The KMP spike provides evidence for a candidate; it does not by itself approve the production stack. The production language/shared-core decision and related owner-review gates remain explicit.

A current platform-proof pull request records CI results and remaining limitations. Until that evidence is reviewed and the milestone gate is accepted, do not treat the production stack as finalized or begin later production implementation.

## Following work

The master plan defines subsequent milestones and their dependencies. Broadly, work proceeds from the approved production foundation toward:

- stable, app-agnostic weather domain and public contracts;
- provider integrations and normalized source data;
- observations, alerts, provenance, and resilient source handling;
- multi-source forecast analysis, blending, uncertainty, and verification;
- regional evaluation and correction only when supported by evidence;
- additional capabilities such as radar/nowcasting research when their specifications and gates are approved.

These are roadmap areas, not permission to implement them ahead of their approved milestone.

## Platform support labels

Use the distinctions in the master plan: **Supported**, **Build-only**, **Experimental**, and **Not tested**. A successful compile alone does not establish runtime support.

See [PLAN.md](https://github.com/INFORMATION89/Meridian-Engine/blob/main/PLAN.md) for the complete milestone sequence and acceptance gates.
