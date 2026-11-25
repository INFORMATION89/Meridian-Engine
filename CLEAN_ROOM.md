# Meridian Clean-room Policy

This applies to all implementation work in INFORMATION89/Meridian-Engine. It reduces copying risk but cannot guarantee that an AI model has never encountered similar public code.

Research may use public standards (WMO, OASIS CAP), official provider API documentation and terms, published scientific papers, official platform documentation, dependency public API documentation, and Meridian's own approved specifications. Record the sources actually consulted. Public source repositories are not implementation references merely because they are public.

Do not copy, translate, closely paraphrase, or imitate the distinctive internal implementation of Breezy Weather or another project. Do not paste dependency implementation code into Meridian; use dependencies through public APIs under their licences. Do not use snippets of uncertain provenance.

Before implementing a substantive feature, research permitted sources and write an independently worded specification covering behaviour, data contracts, edge cases, failures, and acceptance criteria. Review and approve it. Implement from that specification without inspecting third-party implementation code for the feature. Public API documentation may be used to integrate a dependency, but not its internals as a template.

Every implementation pull request must list the Meridian specification, relevant standards, papers, provider and platform documentation, and dependencies materially relied upon with versions and licences. Never claim to have consulted a source that was not used.

If new code appears unusually similar to another project, stop, identify the concern, revisit the design from the approved specification and permitted sources, rewrite independently where needed, and record the resolution. Do not justify suspicious similarity merely because a pattern is common or public.

Review direct and transitive dependency licences, maintenance, and target support. Preserve required notices and attribution. Keep Meridian's Apache-2.0 licence accurate.
