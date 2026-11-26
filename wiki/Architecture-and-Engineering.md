# Architecture & Engineering

## Architectural direction

Meridian is a reusable library/engine with no user interface. Consuming applications call its documented API and decide how to display results. The core must not depend on Android application context, a UI framework, Solstice, or another consumer.

The production language/shared-core choice remains subject to the M0.1 evidence gate and owner approval. Kotlin Multiplatform and Rust are candidates, not interchangeable assumptions about the final architecture.

## Intended boundaries

- **Weather domain:** platform-independent weather semantics, units, timestamps, provenance, quality, and uncertainty.
- **Source adapters:** provider-specific transport and response details stay behind adapter boundaries.
- **Storage:** core logic remains independent of a concrete database; persistent storage is optional and behind an abstraction.
- **Platform integration:** platform-specific capabilities are injected through narrow interfaces.
- **Consumers:** apps control refresh scheduling, presentation, user settings, and app-specific recommendations.

## Data integrity

- Use SI units internally and UTC for instants/time-series timestamps.
- Keep observations, forecasts, derived values, and corrected values distinguishable.
- Preserve native source resolution; any resampling or interpolation must be explicit.
- Missing, unavailable, stale, invalid, and zero values are different states.
- Every returned value must retain meaningful source lineage, issue/fetch times, transformations, freshness, and applicable blend/correction metadata.

## Engineering constraints

Normal tests should be deterministic and offline. External services must respect official terms, rate limits, attribution, and credentials handling. Avoid introducing paid services or new privacy boundaries without owner approval.

For binding rules and detailed architecture constraints, see [RULES.md](https://github.com/INFORMATION89/Meridian-Engine/blob/main/RULES.md), [AGENTS.md](https://github.com/INFORMATION89/Meridian-Engine/blob/main/AGENTS.md), and [PLAN.md](https://github.com/INFORMATION89/Meridian-Engine/blob/main/PLAN.md).
