# ADR-0002: Application-owned recommendations and user-facing policy

- **Status:** Accepted
- **Decision date:** 2026-11-26
- **Scope:** Product and API boundary between Meridian and consuming applications
- **Authority:** Owner decision

## Context

Meridian is an independent meteorological engine intended to serve many applications, not a weather app tailored to Solstice. Weather measurements, forecasts, source evaluation, event detection, provenance, and uncertainty can be shared across consumers. Advice about what a person should wear, carry, avoid, or do depends on each application's purpose, user experience, preferences, and product policy.

Putting such advice in Meridian would couple the engine to one consumer's product decisions and risk turning application policy into a universal meteorological rule.

## Decision

Meridian owns meteorological intelligence and evidence. Consuming applications own user-facing recommendations and action policy.

Meridian may provide:
- observations, forecast values, indices, official alerts, and derived meteorological events;
- event timing, intensity, probabilities, supporting variables, source provenance, data quality, and uncertainty;
- neutral explanations of meteorological results and the evidence behind them;
- consumer-neutral event-detection primitives based on validated meteorological methods.

Meridian must not generate or own:
- clothing/outfit advice, “what to wear”, umbrella or packing suggestions;
- sunscreen, shade, activity, or other lifestyle/action advice;
- personalized daily planning, app-specific recommendation text, or consumer profiles/preferences;
- application-specific alert thresholds, notification schedules, wording, channels, or cooldowns.

A consuming application maps Meridian's weather facts and events to its own recommendations, preferences, thresholds, UI, and notifications. If configurable event thresholds are needed, the consumer supplies the policy; Meridian does not silently apply one consumer's defaults.

For Solstice, “what to wear”, UV action guidance, precipitation alert policy, notification timing, and cooldowns remain Solstice-owned features. Solstice consumes Meridian's underlying weather data and event evidence.

## Consequences

### Benefits
- Meridian remains reusable across unrelated consumers and independent of Solstice.
- Meteorological algorithms and quality can be validated separately from product advice.
- Each application can create a distinct experience without forking the engine or imposing its preferences on other consumers.

### Costs and limitations
- Consumers must implement their own recommendation and notification policies.
- The same meteorological event may be presented differently by different applications; this is intentional.
- Meridian's public API must distinguish objective weather facts/events from consumer-owned actions and avoid app-specific recommendation types.

## Validation and enforcement

- Keep this boundary in Meridian's master plan and API specifications.
- Contract tests must build without Solstice and must not require recommendation/profile/notification modules.
- Solstice's own plan remains the source of truth for its clothing, UV action, alert thresholds, and notification behavior.
- Revisit only through an explicit product decision if Meridian's intended scope changes.
