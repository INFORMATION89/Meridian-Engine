# MER-0.1 dependency license inventory review

**Evidence run:** [GitHub Actions #45781760705](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45781760705)  
**Audited commit:** `62ec625e83447591e9ab5ea0eff2c5ae2af04d8b` (audit source; documentation follow-up on current PR head)  
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

The report classifies the JNA and FastInfoset entries under Android lint / unified test-platform tooling configurations, not the non-test runtime category. The KXML entry is also confined to lint / unified test-platform tooling. The JGraphT coordinate is a transitive dependency of SQLDelight's Gradle plugin graph, not a match in a product runtime lockfile.

A follow-up graph trace found parent paths for **all 58** SBOM-only coordinates: they descend from the Android Gradle Plugin, Kotlin Gradle Plugin, or SQLDelight Gradle Plugin/buildscript dependency graphs. No unmatched coordinate was left without a parent path. This resolves the inventory-tracing question for this SBOM; it does not replace checking final packaged artifacts.

## Remaining review before calling licensing complete

1. Inspect each platform's actual distributable package and reconcile its contents with the runtime dependency configurations. The current run's Linux JVM app image was inspected: it contains `slf4j-api-2.0.19.jar` and `kotlinx-coroutines-slf4j-1.11.0.jar`.
2. Preserve the exact SLF4J license text shipped inside `slf4j-api-2.0.19.jar`; it is included in the repository's `THIRD-PARTY-NOTICES.md`. The upstream SLF4J licensing page confirms the project uses MIT: https://www.slf4j.org/license.html.
3. Verify the upstream license texts and notice files for the five multiple-license coordinates. Determine whether additional root or third-party notices are required for components actually shipped.
4. Inspect Android, iOS, and Windows distributable packages when those artifacts are produced; the current CI artifact set does not contain a release Android package or an iOS app package.

## Interpretation and limits

- A green audit job means the inventory and checks completed; it is **not** a legal compatibility approval or a release clearance.
- The four Meridian project components without license metadata are project records, not third-party dependencies.
- The SBOM includes plugin/build tooling and test components. Their presence in the SBOM does not mean they ship with Meridian.
- The committed Gradle lockfiles classify resolved configurations, not the final contents of every platform package.
- The CycloneDX audit plugin is temporary CI tooling and is not a Meridian runtime dependency.
- This evidence applies only to the audited commit above. Re-run the audit after dependency or packaging changes.

**Status:** SBOM generation and metadata analysis passed. All 58 unmatched coordinates were traced to plugin/buildscript dependency graphs. The Linux desktop package was inspected and its embedded SLF4J MIT license was preserved in `THIRD-PARTY-NOTICES.md`. Review of the five multiple-license entries and actual Android/iOS/Windows distribution packages remains open. Do not describe the dependency license review as complete until those items are resolved.
