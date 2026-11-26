#!/usr/bin/env python3
"""Summarize CycloneDX license metadata and classify Maven modules using Gradle lockfiles.

This report is an engineering review aid, not legal advice or a license-compatibility verdict.
"""
from __future__ import annotations

import argparse
import collections
import json
from pathlib import Path
from typing import Any

DEFAULT_LOCKFILES = (
    Path("engine/gradle.lockfile"),
    Path("android-consumer/gradle.lockfile"),
    Path("desktop-consumer/gradle.lockfile"),
)


def license_labels(component: dict[str, Any]) -> list[str]:
    labels: set[str] = set()
    for item in component.get("licenses", []) or []:
        license_obj = item.get("license") or {}
        label = license_obj.get("id") or license_obj.get("name") or item.get("expression")
        labels.add(str(label or "(license entry without identifier)"))
    return sorted(labels)


def module_key(component: dict[str, Any]) -> tuple[str, str, str] | None:
    group = component.get("group")
    name = component.get("name")
    version = component.get("version")
    if not group or not name or not version or str(version) == "unspecified":
        return None
    if str(group).startswith("Meridian-MER-0.1"):
        return None
    return str(group), str(name), str(version)


def load_lockfile_index(paths: list[Path]) -> dict[tuple[str, str, str], set[str]]:
    index: dict[tuple[str, str, str], set[str]] = collections.defaultdict(set)
    for path in paths:
        if not path.is_file():
            raise FileNotFoundError(f"Required Gradle lockfile missing: {path}")
        for line_number, raw_line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            line = raw_line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            coordinate, configuration_text = line.split("=", 1)
            parts = coordinate.rsplit(":", 2)
            if len(parts) != 3:
                raise ValueError(f"Malformed lock entry at {path}:{line_number}: {line}")
            group, name, version = parts
            for configuration in configuration_text.split(","):
                configuration = configuration.strip()
                if configuration:
                    index[(group, name, version)].add(configuration)
    return dict(index)


def classify_configurations(configurations: set[str]) -> set[str]:
    categories: set[str] = set()
    for configuration in configurations:
        lowered = configuration.lower()
        if "test" in lowered or "unified-test-platform" in lowered:
            categories.add("test")
        if lowered.endswith("runtimeclasspath") and "test" not in lowered:
            categories.add("runtime")
        if (
            "androidlinttool" in lowered
            or "compilerclasspath" in lowered
            or "buildtoolsapiclasspath" in lowered
            or "dialectclasspath" in lowered
            or "migrationenv" in lowered
            or "intellijenv" in lowered
            or "swiftpmdependencies" in lowered
            or (lowered.endswith("classpath") and "test" not in lowered and "runtime" not in lowered)
        ):
            categories.add("build/tooling")
        if any(token in lowered for token in ("cinterop", "compileklibraries", "compilationdependenciesmetadata")):
            categories.add("native compile")
        if not categories:
            categories.add("other configuration")
    return categories


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("sbom", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--lockfile", type=Path, action="append", default=[])
    args = parser.parse_args()

    lockfiles = args.lockfile or list(DEFAULT_LOCKFILES)
    lock_index = load_lockfile_index(lockfiles)

    with args.sbom.open(encoding="utf-8") as stream:
        bom = json.load(stream)

    raw_components = bom.get("components", []) or []
    dependency_edges = bom.get("dependencies", []) or []
    project_refs = {
        component.get("bom-ref")
        for component in raw_components
        if str(component.get("group", "")).startswith("Meridian-MER-0.1")
        or component.get("name") == "Meridian-MER-0.1"
    }
    root_ref = bom.get("metadata", {}).get("component", {}).get("bom-ref")
    if root_ref:
        project_refs.add(root_ref)

    direct_refs: set[str] = set()
    for edge in dependency_edges:
        if edge.get("ref") in project_refs:
            direct_refs.update(edge.get("dependsOn", []) or [])

    unique: dict[tuple[str, str, str], dict[str, Any]] = {}
    missing_project_metadata: list[dict[str, str]] = []

    for component in raw_components:
        key = module_key(component)
        if key is None:
            if not license_labels(component):
                missing_project_metadata.append({
                    "name": str(component.get("name") or "(unnamed project)"),
                    "version": str(component.get("version") or "(unknown version)"),
                    "purl": str(component.get("purl") or component.get("bom-ref") or "(no purl)"),
                })
            continue
        entry = unique.setdefault(key, {
            "labels": set(),
            "purls": set(),
            "refs": set(),
            "configurations": set(),
            "direct": False,
            "metadata_unresolved": False,
        })
        entry["labels"].update(license_labels(component))
        entry["purls"].add(str(component.get("purl") or component.get("bom-ref") or "(no purl)"))
        entry["refs"].add(str(component.get("bom-ref") or ""))
        entry["direct"] = entry["direct"] or component.get("bom-ref") in direct_refs
        entry["metadata_unresolved"] = entry["metadata_unresolved"] or any(
            prop.get("name") == "cdx:maven:package:metadata-unresolved"
            for prop in (component.get("properties", []) or [])
        )
        entry["configurations"].update(lock_index.get(key, set()))

    by_category: dict[str, list[tuple[tuple[str, str, str], dict[str, Any]]]] = collections.defaultdict(list)
    label_counts: dict[str, collections.Counter[str]] = collections.defaultdict(collections.Counter)
    all_label_counts: collections.Counter[str] = collections.Counter()
    unmatched: list[tuple[str, str, str]] = []
    missing_external: list[tuple[str, str, str]] = []
    multiple_license: list[tuple[tuple[str, str, str], dict[str, Any]]] = []
    unresolved_external: list[tuple[str, str, str]] = []

    for key, entry in sorted(unique.items()):
        labels = sorted(entry["labels"])
        configurations = set(entry["configurations"])
        if configurations:
            categories = classify_configurations(configurations)
        else:
            categories = {"SBOM-only/unmatched"}
            unmatched.append(key)
        for category in categories:
            by_category[category].append((key, entry))
            for label in labels:
                label_counts[category][label] += 1
        for label in labels:
            all_label_counts[label] += 1
        if not labels:
            missing_external.append(key)
        if len(labels) > 1:
            multiple_license.append((key, entry))
        if entry["metadata_unresolved"]:
            unresolved_external.append(key)

    non_apache_runtime = [
        (key, entry)
        for key, entry in sorted(unique.items())
        if "runtime" in classify_configurations(set(entry["configurations"]))
        and any(label != "Apache-2.0" for label in entry["labels"])
    ]

    lines = [
        "# Preliminary dependency-license inventory",
        "",
        "> Generated from the resolved CycloneDX SBOM plus committed Gradle lockfiles. This is an engineering inventory, not legal advice or a compatibility approval.",
        "",
        f"- Raw CycloneDX component records: **{len(raw_components)}** (includes POM/JAR and project records)",
        f"- Unique third-party Maven coordinates (group:name:version): **{len(unique)}**",
        f"- Unique third-party coordinates with no license metadata: **{len(missing_external)}**",
        f"- Meridian project components with no license metadata: **{len(missing_project_metadata)}**",
        f"- Unique third-party coordinates flagged with unresolved dependency metadata: **{len(unresolved_external)}**",
        f"- Unique third-party coordinates with multiple declared license entries: **{len(multiple_license)}**",
        f"- Distinct license labels across unique third-party coordinates: **{len(all_label_counts)}**",
        f"- Coordinates represented in Gradle lockfiles: **{len(unique) - len(unmatched)}**",
        f"- Coordinates absent from the three project lockfiles: **{len(unmatched)}** (review as buildscript/plugin/tool graph or inventory gap)",
        "",
        "## Unique third-party coordinates by license label",
        "",
    ]
    lines.extend(f"- {label} — {count} unique coordinate(s)" for label, count in sorted(all_label_counts.items()))

    lines.extend(["", "## Lockfile configuration classification", ""])
    for category in ("runtime", "test", "build/tooling", "native compile", "other configuration", "SBOM-only/unmatched"):
        entries = by_category.get(category, [])
        lines.append(f"### {category} — {len(entries)} unique coordinate(s)")
        if not entries:
            lines.append("- None.")
            lines.append("")
            continue
        lines.extend(f"- {label} — {count}" for label, count in sorted(label_counts[category].items()))
        lines.append("")

    lines.extend(["", "## Non-Apache dependencies reachable from runtime configurations", ""])
    if not non_apache_runtime:
        lines.append("- None identified by the SBOM-to-lockfile join.")
    else:
        for key, entry in non_apache_runtime:
            labels = ", ".join(sorted(entry["labels"])) or "(no license metadata)"
            configs = ", ".join(sorted(entry["configurations"]))
            direct = "direct from project graph" if entry["direct"] else "transitive/not marked direct"
            lines.append(f"- {':'.join(key)} — {labels}; {direct}; configurations: {configs}")

    lines.extend(["", "## Multiple license entries requiring upstream review", ""])
    if not multiple_license:
        lines.append("- None.")
    else:
        for key, entry in multiple_license:
            labels = ", ".join(sorted(entry["labels"]))
            configs = ", ".join(sorted(entry["configurations"])) or "(no lockfile match)"
            lines.append(f"- {':'.join(key)} — {labels}; configurations: {configs}")

    lines.extend(["", "## Coordinates without license metadata", ""])
    if not missing_external:
        lines.append("- None.")
    else:
        lines.extend(f"- {':'.join(key)}" for key in missing_external)

    lines.extend(["", "## Coordinates absent from Gradle lockfiles", ""])
    if not unmatched:
        lines.append("- None.")
    else:
        lines.extend(f"- {':'.join(key)}" for key in unmatched)

    lines.extend(["", "## Meridian project components without license metadata", ""])
    if not missing_project_metadata:
        lines.append("- None.")
    else:
        lines.extend(f"- {item['name']}:{item['version']} — {item['purl']}" for item in missing_project_metadata)

    lines.extend([
        "",
        "## Review limitations and notice handling",
        "",
        "- Lockfile categories show which Gradle configurations reference a coordinate; they do not by themselves prove whether a packaged binary contains that component.",
        "- Runtime means a non-test configuration ending in RuntimeClasspath; build/tooling, test, and native compile are separate configuration evidence, not legal conclusions.",
        "- A coordinate can appear in more than one category if Gradle resolves it in multiple configurations.",
        "- License identifiers and texts come from dependency metadata and require spot-checking against authoritative upstream license files and distribution archives.",
        "- Multiple declared license entries may represent alternatives or conjunctive terms; review upstream wording before selecting an interpretation.",
        "- SBOM-only/unmatched coordinates must be traced to buildscript/plugin/tool configurations or an inventory gap; they must not be silently treated as runtime or ignored.",
        "- Decide whether a root NOTICE or separate third-party notices are required only after reviewing runtime-reachable and actually packaged components and their upstream notices.",
        "- The temporary CycloneDX audit plugin is a tooling aid and is not part of Meridian's runtime or distribution dependency set.",
    ])

    args.output.parent.mkdir(parents=True, exist_ok=True)
    report = "\n".join(lines) + "\n"
    args.output.write_text(report, encoding="utf-8")
    print(report, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
