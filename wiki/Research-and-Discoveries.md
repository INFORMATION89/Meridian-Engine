# Research & Discoveries

This page collects concise findings and links to the evidence or decision that supports them. Research notes are not implementation approval.

## Research rules

Meridian follows a clean-room research process. Permitted sources include public standards, official provider/platform/dependency documentation, and published scientific literature. Third-party weather-project source code is not an implementation template.

Before a substantive feature is implemented, its behaviour and acceptance criteria must be specified independently. Sources actually consulted must be recorded.

See [CLEAN_ROOM.md](https://github.com/INFORMATION89/Meridian-Engine/blob/main/CLEAN_ROOM.md).

## Current findings and constraints

- **Cross-platform strategy:** Kotlin Multiplatform and Rust were evaluated as candidates; the M0.1 evidence gate and owner review determine the production direction. Candidate evidence is not final approval.
- **Consumer boundary:** Meridian owns meteorological interpretation and evidence. App-specific lifestyle recommendations remain with consuming applications. See the [decision record](https://github.com/INFORMATION89/Meridian-Engine/tree/main/docs/decisions).
- **Privacy:** local-first operation and no default telemetry or sharing are core product constraints, not optional deployment preferences.
- **Scientific claims:** apparent improvements must be tested against versioned forecast/observation archives and a documented baseline. Plausibility or a few examples are insufficient.

## Adding a finding

Keep each entry short: question, evidence/source, conclusion, limitations, and whether it is merely a hypothesis or an approved decision. Link to a detailed report or decision record rather than duplicating it here.
