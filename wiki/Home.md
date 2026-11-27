# Meridian

Meridian is an independent, reusable weather-data and meteorological intelligence engine. It is designed to be embedded by applications rather than presented as a standalone weather app.

Solstice is the first reference consumer, not a dependency or architectural authority. Other applications—including mobile and desktop products—must be able to consume Meridian without depending on Solstice.

## Purpose

Meridian aims to turn multiple weather sources and observations into structured, traceable, and more useful meteorological information. Its intended intelligence level builds on existing forecast models through source selection, multi-model comparison and blending, bias evaluation and correction, regional adaptation, uncertainty, and verification.

Meridian is not intended to create a new global numerical weather-prediction model.

## Product principles

- **Independent:** consumers depend on Meridian; Meridian does not depend on a consumer app.
- **Local-first:** no mandatory hosted backend or account.
- **Privacy-first:** no telemetry or external data sharing by default; sharing requires explicit opt-in and disclosure.
- **Evidence-led:** forecast changes must be reproducibly evaluated.
- **Transparent:** results preserve source provenance, freshness, transformations, quality, and uncertainty.
- **Cross-platform:** Windows, macOS, Linux, Android, and iOS are target platforms, with support claims tied to actual evidence.
- **AI-independent at runtime:** AI may assist contributors, but the shipped engine does not require an AI model or service.

## Scope boundary

Meridian provides structured meteorological facts, derived indicators, events, alerts, confidence, uncertainty, and provenance. Consuming applications own presentation and user-specific or lifestyle recommendations—for example clothing, umbrella, activity, and personal-routine advice.

## Explore the Wiki

- [Roadmap](Roadmap)
- [Research & Discoveries](Research-and-Discoveries)
- [Architecture & Engineering](Architecture-and-Engineering)
- [Scientific Validation](Scientific-Validation)
- [Development Updates](Development-Updates)
- [Ideas & Future Possibilities](Ideas-and-Future-Possibilities)

## Authoritative documentation

- [Master plan](https://github.com/INFORMATION89/Meridian-Engine/blob/main/PLAN.md)
- [Project rules](https://github.com/INFORMATION89/Meridian-Engine/blob/main/RULES.md)
- [Clean-room policy](https://github.com/INFORMATION89/Meridian-Engine/blob/main/CLEAN_ROOM.md)
- [Project overview document](https://github.com/INFORMATION89/Meridian-Engine/blob/main/PROJECT_OVERVIEW.md)

This Wiki is a readable project journal and guide, not a replacement for those documents.
