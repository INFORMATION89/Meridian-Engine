# Scientific Validation

Meridian must demonstrate meteorological quality with reproducible evidence. A passing build, plausible output, or successful unit test does not establish forecast accuracy.

## Validation principles

1. Preserve a versioned baseline before changing source selection, blending, weighting, interpolation, or correction.
2. Use versioned forecast archives and corresponding observations with documented origins, licences, matching rules, and quality controls.
3. Prevent data leakage: evaluation must use only observations that would legitimately be available for the forecast being assessed.
4. Separate tuning data from final evaluation periods, preferably with chronological holdouts.
5. Report results by variable, forecast lead time, region, season, and source where sample sizes permit.
6. Compare candidate results with the previous Meridian baseline and relevant individual source models.
7. Report regressions, uncertainty, sample counts, exclusions, and limitations—not only aggregate improvements.

## Metrics

Choose metrics appropriate to the variable and task before evaluating candidates. Examples include MAE, RMSE, and mean bias for continuous variables, and suitable categorical or probabilistic scores for event forecasts.

## No unsupported adaptation

Adaptive weights, local learning, and statistical corrections require sufficient data, documented safeguards, deterministic tests, and archive-based evaluation. Do not promote a candidate method based on anecdotal examples or AI-generated analysis.

## Current status

M0.1 platform and dependency checks are engineering evidence, not scientific forecast validation. Meteorological evaluation belongs to the relevant later milestone and must follow its approved specification.

See the scientific and testing requirements in [RULES.md](https://github.com/INFORMATION89/Meridian-Engine/blob/main/RULES.md) and the milestone gates in [PLAN.md](https://github.com/INFORMATION89/Meridian-Engine/blob/main/PLAN.md).
