# MER-0.1 dependency license inventory review

**Evidence run:** [GitHub Actions #45781760705](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45781760705)  
**Audited commit:** `62ec625e83447591e9ab5ea0eff2c5ae2af04d8b`  
**Job:** `dependency-license-audit` — successful  
**Generated:** 2026-11-26

## Result

The CycloneDX SBOM was generated twice: first to bootstrap SHA-256 dependency verification metadata, then again with strict dependency verification enabled. Both Gradle invocations succeeded on their first attempt. The summary script completed and the report artifact was uploaded.

| Measure | Result |
|---|---:|
| Raw CycloneDX component records | 427 |
| Unique third-party Maven coordinates | 402 |
| Third-party coordinates without license metadata | 0 |
| Third-party coordinates with unresolved dependency metadata | 0 |
| Coordinates with multiple declared license entries | 5 |
| Distinct license labels | 13 |
| Coordinates matched to the three project Gradle lockfiles | 344 |
| SBOM coordinates not matched to those lockfiles | 58 |
| Non-test runtime coordinates | 54 |

The 54 coordinates classified as runtime consist of 53 Apache-2.0 coordinates and one MIT coordinate: `org.slf4j:slf4j-api:2.0.19`. This is a transitive dependency and appears in Android and desktop runtime configurations. Its upstream license and required attribution must be verified and included in third-party notices if it remains in distributed artifacts.

The five coordinates with multiple declared license entries are:

- `com.sun.xml.fastinfoset:FastInfoset:1.2.16` — Apache-2.0, BSD-3-Clause.
- `net.java.dev.jna:jna:5.6.0` — Apache-2.0, LGPL-2.1-only.
- `net.java.dev.jna:jna-platform:5.6.0` — Apache-2.0, LGPL-2.1-only.
- `net.sf.kxml:kxml2:2.3.0` — BSD style, Public Domain.
- `org.jgrapht:jgrapht-core:1.5.3` — EPL-2.0, LGPL-2.1-only; no match in the three committed lockfiles.

The report classifies the JNA and FastInfoset entries under Android lint / unified test-platform tooling configurations, not the non-test runtime category. That classification is useful evidence, but is not proof that a package is absent from every distributed artifact.

## Remaining review before calling licensing complete

1. Trace all 58 SBOM-only/unmatched coordinates to plugin/buildscript/tool configurations or identify and fix any inventory gap. Do not silently classify them as runtime or ignore them.
2. Confirm the exact dependency graph of each distributable Android, iOS, and desktop artifact. Lockfile configuration names alone do not prove packaged contents.
3. Verify the upstream license texts and notice files for the runtime MIT dependency and the five multiple-license coordinates. Determine whether a root `NOTICE` or a separate third-party notices file is required.
4. Re-check any license that offers multiple entries or ambiguous labels against authoritative upstream sources before interpreting the terms.

## Interpretation and limits

- A green audit job means the inventory and checks completed; it is **not** a legal compatibility approval or a release clearance.
- The four Meridian project components without license metadata are project records, not third-party dependencies.
- The SBOM includes plugin/build tooling and test components. Their presence in the SBOM does not mean they ship with Meridian.
- The committed Gradle lockfiles classify resolved configurations, not the final contents of every platform package.
- The CycloneDX audit plugin is temporary CI tooling and is not a Meridian runtime dependency.
- This evidence applies only to the audited commit above. Re-run the audit after dependency or packaging changes.

**Status:** SBOM generation and metadata analysis passed. Upstream notice verification, unmatched-coordinate tracing, and packaged-artifact review remain open. Do not describe the dependency license review as complete until those items are resolved.
