# MER-0.2 Compatibility and versioning contract

- **Status:** Design proposal for M0.2 review; no public API or persisted schema is declared stable.
- **Baseline:** Kotlin Multiplatform shared core; Android and iOS/Swift consumers; Desktop JVM on Windows, macOS, and Linux.
- **Authority:** `PLAN.md`, ADR-0001, ADR-0002, ADR-0003, M0.2 contract-design specification, and platform evidence.

## 1. Purpose

Define what Meridian means by compatibility, how public API and persisted data evolve independently, and how platform support claims are evidenced. The goal is to let unrelated applications consume Meridian without depending on internal implementation details or Solstice.

## 2. Compatibility dimensions

Track these separately; never collapse them into a single "supported" label:

1. **Source/API compatibility:** whether consumer source code compiles against a new release without changes.
2. **Binary/ABI compatibility:** whether previously compiled consumers can load/link the new artifact. This is platform/artifact-specific; JVM, Kotlin/Native frameworks, and Android artifacts have different rules.
3. **Behavioural/semantic compatibility:** whether the meaning, units, time semantics, error behaviour, cancellation, provenance, and defaults remain compatible.
4. **Persisted-data compatibility:** whether a new version can read/migrate data written by earlier schema versions.
5. **Platform/packaging compatibility:** supported OS, architecture, toolchain, runtime, artifact type, and consumer integration path.

A source-compatible change can still be behaviourally breaking. Changing a unit, timestamp meaning, missing-value state, uncertainty semantics, source attribution, or stale-data handling is a contract change even if the Kotlin signature is unchanged.

## 3. Version domains

Maintain independent version identifiers for:

- **Library release version:** distribution and consumer dependency selection.
- **Public API contract version:** semantic contract changes and deprecation policy.
- **Persisted schema version:** storage migration and on-disk compatibility.
- **Meteorological algorithm/configuration version:** reproducibility and interpretation of derived/corrected results.

Do not assume these versions advance together. Record algorithm/configuration versions in provenance where they affect returned data. Schema migrations must not be inferred from a library version alone.

## 4. Proposed release policy

Use Semantic Versioning for published library artifacts once the project declares a stable public API:

- **MAJOR:** incompatible public source/binary/behavioural contract change, or removal of a previously stable public symbol.
- **MINOR:** backward-compatible public capability or API addition.
- **PATCH:** backward-compatible bug fix or implementation correction that does not change documented semantics.

Before the first stable API release, mark public symbols and modules experimental/unstable and document that source or binary compatibility is not guaranteed. Do not imply that an internal milestone or a green consumer sample establishes a stable API.

A deprecation should identify the replacement, rationale, migration guidance, and earliest planned removal. Give consumers a reasonable migration period across at least one documented release cycle where practical. Security, legal, or data-integrity issues may require an accelerated correction, with an explicit release note.

## 5. Public API rules

- Expose engine-owned domain and contract types only.
- Do not expose Ktor, SQLDelight, SQLite/JDBC, Android Context, platform handles, provider DTOs, or coroutine implementation types as required public dependencies.
- Avoid public mutable fields and implicit global state.
- Additive fields must have defined default/unknown semantics and must not cause older consumers to misinterpret data.
- Unknown enum values and future capabilities must fail safely or be representable without unsafe assumptions; document forward-compatibility strategy for Kotlin and Swift.
- Preserve explicit units, temporal meaning, provenance, quality, uncertainty, and result completeness semantics.
- Keep consumer recommendation, profile, notification, and UI concepts outside the contract (ADR-0002).

## 6. Swift and multiplatform interop

- Treat generated Kotlin/Native-to-Swift signatures as a consumer-facing compatibility surface and inspect them in the independent Swift sample.
- Prefer a narrow Swift-friendly facade where generated names, generic types, suspend functions, sealed hierarchies, or cancellation wrappers produce awkward or unstable interfaces.
- Keep semantic logic in shared Kotlin; the facade adapts naming/async/error representation only.
- Do not expose Kotlin internal concurrency types as the contract. Document how Swift task cancellation reaches the shared operation and how cancellation differs from failures.
- Compile and execute a minimal Swift consumer for every material public API change in the supported iOS simulator CI path.

## 7. Platform support evidence labels

Every platform/architecture/artifact combination must be classified independently as:

- **Built:** toolchain successfully produced the artifact.
- **Consumer-tested:** an independent consumer imported/loaded and exercised it.
- **CI-supported:** the documented automated CI path runs and passes for that target.
- **Release-supported:** packaging, dependency notices, signing/distribution requirements, runtime prerequisites, and release checks are complete for that artifact.

These are cumulative evidence labels only when all lower levels are satisfied; do not infer one from another. Report OS, architecture, host, toolchain, runtime, artifact type, and test scope. Android x86_64 emulator evidence does not establish Android ARM64 or physical-device support; iOS simulator evidence does not establish physical-device execution; Desktop JVM evidence does not establish native desktop ABI support.

## 8. Independent consumer requirement

M0.2 must maintain a small consumer that depends only on Meridian's published/public project surface, not on Solstice, internal source sets, provider modules, or database implementation types. Exercise at least:

- Kotlin/JVM consumer;
- Swift consumer through the iOS framework;
- one Android consumer path where applicable.

The consumer must construct and close the engine, submit a deterministic query against local fixtures, inspect values and metadata, handle partial/failure/cancellation outcomes, and compile with persistence disabled. Storage-enabled behaviour is tested separately through the public storage boundary.

## 9. Compatibility testing and release discipline

- Compile public API samples on every supported consumer path for each contract-affecting change.
- Run semantic contract tests for units, time, missing/stale states, provenance, error taxonomy, partial results, and cancellation.
- Test supported schema migrations independently of library/API versions.
- Keep dependency/toolchain versions pinned and record tested matrices.
- Do not claim release support until legal notices and packaging requirements are verified for the exact artifact. Mobile notice packaging remains a known M0.1 gap until separately implemented and inspected.
- Breaking changes require migration notes and owner review before stable release. Do not merge or publish releases without explicit approval.

## 10. M0.2 acceptance

- [ ] API, library, schema, and algorithm/configuration versions are independent and documented.
- [ ] Experimental versus stable symbols are clearly classified.
- [ ] SemVer and deprecation policy is documented for the first stable release.
- [ ] Swift generated API is reviewed through a real consumer.
- [ ] Independent Kotlin, Swift, and relevant Android consumer samples use public surfaces only.
- [ ] Platform evidence labels are applied without overclaiming.
- [ ] Contract and migration tests cover the documented compatibility dimensions.
- [ ] Release support is not claimed before packaging and legal-notice evidence is complete.