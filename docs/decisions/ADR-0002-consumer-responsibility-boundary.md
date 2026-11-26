# ADR-0002: Meteorological engine and consumer recommendation boundary

- **Status:** Accepted
- **Decision date:** 2026-11-26
- **Scope:** Product and API responsibility boundary between Meridian and consuming applications
- **Authority:** Owner decision

## Context

Meridian is intended to be an independent, reusable weather engine that can power Solstice and unrelated applications across supported platforms. Solstice has its own product identity and user experience, including practical recommendations such as what to wear, whether to carry an umbrella, which activities suit the forecast, and when to go outside.

Putting those application-specific recommendations in Meridian would couple a general-purpose engine to one consumer's product philosophy, personal preferences, and UX. At the same time, Meridian's Level 2 meteorological intelligence requires more than returning raw provider values: it must be able to derive, evaluate, and explain meteorological conditions.

## Decision

Keep the boundary explicit:

### Meridian owns meteorological intelligence

Meridian may provide:
- normalized observations and forecasts with units, timestamps, freshness, provenance, and quality;
- model/source selection, blending, calibration, local adaptation, and verification;
- probabilities, uncertainty, confidence, disagreement, and forecast limitations;
- meteorological derived variables and indicators;
- detection, timing, and explanation of weather phenomena and meteorological risks when supported by evidence;
- structured, consumer-neutral signals that describe meteorological conditions.

Examples include a likely precipitation window, elevated UV, heat-stress indicators, strong-wind conditions, a storm risk, or low forecast confidence. These are descriptions or interpretations of weather, not instructions about an individual's lifestyle.

### Consuming applications own practical recommendations

Each consuming application decides how to turn Meridian's neutral results into advice and product behaviour. Examples include:
- clothing and footwear suggestions;
- whether to carry an umbrella, sunscreen, or other items;
- activity, exercise, travel, or outdoor-planning recommendations;
- personalized routines, notification thresholds, wording, and delivery;
- user preference, accessibility, and context-specific policy.

Solstice may implement these experiences using Meridian's meteorological outputs. Another application may make different recommendations or omit them entirely.

## Architectural constraints

1. Meridian's core modules and public contracts must not contain Solstice-specific concepts, copy, UI models, personal preference models, or recommendation policies.
2. Meridian must not depend on Solstice or require a Solstice checkout, configuration, or service to build, test, or operate.
3. Meridian may expose the meteorological facts, estimates, confidence, provenance, and explanations needed by consumers to implement their own recommendations.
4. Consumer recommendation logic belongs in the consumer application or in a separate optional product-specific module that depends on Meridian, never in Meridian's core.
5. Personal preferences and application-specific user profiles remain with the consumer. Meridian must not collect them merely to produce generic meteorological outputs.
6. A consumer may choose to share explicitly configured context with Meridian only where a future engine capability genuinely requires it; that requires a separate specification and privacy review. This ADR does not authorize collection or telemetry.
7. Meridian documentation and examples must use neutral meteorological examples and standalone consumers rather than presenting Solstice's recommendation behaviour as part of the engine contract.

## Consequences

- Meridian remains useful to unrelated applications, libraries, tools, and platform integrations.
- Solstice can develop a distinct practical, personalized experience without shaping the engine's public domain around its UX.
- The engine still owns substantive meteorological interpretation; this decision does not reduce Meridian to a raw-data transport or provider wrapper.
- Consumer applications are responsible for recommendation quality, wording, preference handling, and any resulting user-facing action.
- The boundary should be reflected in future API design, specs, examples, and acceptance tests.

## Revisit triggers

Revisit this decision only if a concrete, independently justified consumer requirement demonstrates that a generic, opt-in recommendation capability belongs in Meridian without introducing consumer-specific policy or personal-data coupling. Such a proposal requires owner review and a new or superseding ADR.
