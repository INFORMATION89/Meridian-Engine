#!/usr/bin/env python3
"""Create a review-oriented summary from a CycloneDX JSON SBOM.

This reports metadata presence; it does not determine legal compatibility.
"""
from __future__ import annotations

import argparse
import collections
import json
from pathlib import Path
from typing import Any


def license_labels(component: dict[str, Any]) -> list[str]:
    labels: list[str] = []
    for item in component.get("licenses", []) or []:
        license_obj = item.get("license") or {}
        expression = item.get("expression")
        label = (
            license_obj.get("id")
            or license_obj.get("name")
            or expression
            or "(license entry without identifier)"
        )
        labels.append(str(label))
    return labels


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("sbom", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()

    with args.sbom.open(encoding="utf-8") as stream:
        bom = json.load(stream)

    components = bom.get("components", []) or []
    counts: collections.Counter[str] = collections.Counter()
    missing: list[dict[str, str]] = []
    unresolved: list[dict[str, str]] = []
    no_license_choice: list[dict[str, str]] = []

    for component in components:
        name = str(component.get("name") or "(unnamed component)")
        version = str(component.get("version") or "(unknown version)")
        purl = str(component.get("purl") or component.get("bom-ref") or "(no purl)")
        labels = license_labels(component)
        properties = component.get("properties", []) or []
        metadata_unresolved = any(
            prop.get("name") == "cdx:maven:package:metadata-unresolved"
            for prop in properties
        )
        if metadata_unresolved:
            unresolved.append({"name": name, "version": version, "purl": purl})
        if not labels:
            missing.append({"name": name, "version": version, "purl": purl})
        for label in labels:
            counts[label] += 1
        if len(labels) > 1:
            no_license_choice.append({"name": name, "version": version, "purl": purl, "licenses": ", ".join(labels)})

    dependency_edges = bom.get("dependencies", []) or []
    direct_refs = set()
    for edge in dependency_edges:
        direct_refs.update(edge.get("dependsOn", []) or [])

    lines = [
        "# Preliminary dependency-license metadata summary",
        "",
        "> This is a metadata inventory generated from Gradle's resolved dependency graph. It is not legal advice or a compatibility approval.",
        "",
        f"- Components listed: **{len(components)}**",
        f"- Distinct license labels: **{len(counts)}**",
        f"- Components with no license entry: **{len(missing)}**",
        f"- Components flagged with unresolved POM/module metadata: **{len(unresolved)}**",
        f"- Components with multiple license entries: **{len(no_license_choice)}**",
        f"- Dependency graph nodes: **{len(dependency_edges)}**",
        f"- Referenced dependency nodes: **{len(direct_refs)}**",
        "",
        "## License labels",
        "",
    ]
    if counts:
        lines.extend(f"- `{label}` — {count} component(s)" for label, count in sorted(counts.items()))
    else:
        lines.append("- No license entries found in the SBOM.")

    for heading, entries in (
        ("Components without license metadata", missing),
        ("Components with unresolved metadata", unresolved),
        ("Components with multiple license entries (review expressions/alternatives)", no_license_choice),
    ):
        lines.extend(["", f"## {heading}", ""])
        if not entries:
            lines.append("- None reported.")
            continue
        for item in entries:
            suffix = f" — licenses: {item['licenses']}" if "licenses" in item else ""
            lines.append(f"- `{item['name']}:{item['version']}` — `{item['purl']}`{suffix}")

    lines.extend([
        "",
        "## Review limitations",
        "",
        "- License identifiers and texts come from dependency metadata and require spot-checking against authoritative upstream license files.",
        "- Missing metadata must be investigated; it must not be inferred from a repository's license or from a similarly named artifact.",
        "- Multiple license entries may mean alternatives or conjunctive terms. Review the upstream declaration before making a decision.",
        "- Reconcile runtime, test, build, and packaging configurations before deciding whether a root `NOTICE` or separate third-party notices are required.",
        "- The temporary CycloneDX audit plugin is a tooling aid and is not part of Meridian's runtime or distribution dependency set.",
    ])

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("\n".join(lines))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
