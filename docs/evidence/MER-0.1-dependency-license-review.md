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

### Upstream license verification for the five multi-license coordinates

- **JNA 5.6.0 (`jna`, `jna-platform`)** declares two alternative licenses: LGPL 2.1-or-later **or** Apache-2.0. The project license file explicitly permits choosing either; this is not a requirement to comply with both simultaneously. These coordinates are in Android lint / unified test-platform tooling configurations, not the current product runtime configuration. If a future shipped artifact includes JNA, record the chosen Apache-2.0 option only after confirming the corresponding artifact's bundled license files and distribution terms.
- **FastInfoset 1.2.16** declares Apache-2.0 and Eclipse Distribution License 1.0. The published project metadata includes both; the EDL attribution/notice should be reviewed if this exact artifact is ever shipped. Its current path is Android lint / unified test-platform tooling, not product runtime.
- **kxml2 2.3.0** declares BSD-style terms for classes under `org.kxml2` and Public Domain for the XmlPull API classes under `org.xmlpull.v1`. Its current path is Android lint / unified test-platform tooling. If packaged in future, inspect the actual class/package contents and preserve the relevant license text/attribution.
- **JGraphT 1.5.3** is dual-licensed under LGPL-2.1-or-later or EPL-2.0. It is a transitive SQLDelight Gradle-plugin dependency, not a product runtime lockfile match; it is not currently expected in Meridian runtime artifacts.

References checked: JNA 5.6.0 Maven metadata and project license; FastInfoset 1.2.16 Maven metadata; kxml2 2.3.0 Maven metadata; JGraphT project licensing and 1.5.3 Maven metadata. The references establish declared options, not a legal opinion or proof of final package contents.

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


## Desktop package inspection follow-up (2026-11-26)

The Linux and Windows desktop app-image artifacts from [platform-proof run #45787082365](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45787082365), built from commit 8cf99826c8b81e028e5d2d3f025a658742fa875b, were inspected directly. Both contain the same third-party runtime JAR set, including slf4j-api-2.0.19.jar and sqlite-jdbc-3.53.4.0.jar. The SQLite JDBC JAR contains Apache-2.0 and Zentus BSD-style license texts, plus native SQLite libraries for Linux, macOS, and Windows; the SQLite engine is public domain. Both app images also contain 225 files under the bundled JDK runtime/legal directory.

This inspection identified a distribution-notice gap: the original app-image artifact did not include Meridian's root LICENSE or THIRD-PARTY-NOTICES.md, even though SLF4J's MIT text was already documented in the repository. The build now stages both documents in the Gradle desktop distribution and copies them into each Linux, Windows, and macOS app-image root. The third-party notice inventory now records SQLite JDBC's additional embedded license and the runtime component families observed in the actual packages.

The current package evidence covers Linux and Windows app images. The macOS job is configured to stage the same notices, but its resulting artifact must still be inspected after the updated workflow runs. Android and iOS application package distribution remains outside the artifacts produced by this workflow. This follow-up does not close the full cross-platform license review until those platform packages and the updated artifacts are inspected.

## Updated desktop package verification (2026-11-26)

The updated artifacts from [platform-proof run #45787834464](https://github.com/INFORMATION89/Meridian-Engine/actions/runs/45787834464), built from commit `e377096cf2491918fbb960ceff04d89e44ce2677`, were inspected directly:

- Linux app-image: contains root `LICENSE` and `THIRD-PARTY-NOTICES.md`.
- Windows app-image: contains root `LICENSE` and `THIRD-PARTY-NOTICES.md`.
- macOS app-image: contains root `LICENSE` and `THIRD-PARTY-NOTICES.md`.
- Gradle `installDist` output on all three desktop operating systems also contains both documents.
- Bundled OpenJDK runtime legal directories remain present in all three app-images. The platform-specific legal-file counts differ, as expected for the different runtime images; none were stripped by the packaging changes.

This closes the previously open desktop app-image notice-staging check for Linux, Windows, and macOS on the inspected commit. The artifacts are experimental CI app-images, not release installers.

The iOS artifact contains the built XCFramework and Swift consumer build output, but no Android or iOS application release package is produced by this workflow. Android's artifact is test/report output only. Therefore, legal packaging for an eventual Android APK/AAB and a distributable iOS app/framework package remains unverified and must not be inferred from the desktop results.

**Updated status:** desktop app-image notice inclusion is verified on all three desktop operating systems. The SBOM metadata audit passed with zero third-party coordinates missing license metadata. The five multi-license entries have been traced to lint/test/build-tool configurations rather than desktop runtime packages and their declared upstream license alternatives have been reviewed. Full mobile release-package notice verification remains open.
