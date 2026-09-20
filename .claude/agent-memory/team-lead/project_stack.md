---
name: screentimelens-stack
description: androidscreentimelens is a native Kotlin/Jetpack Compose app (not Flutter) and the user wants zero code comments
metadata:
  type: project
---

androidscreentimelens is a **native Kotlin + Jetpack Compose** Android app (UsageStatsManager based screen-time dashboard), not a Flutter project. UI language is English, strings live in `res/values/strings.xml`.

**Why:** The standing team-lead workflow assumes Flutter; Flutter agents/skills (flutter analyze, l10n/ARB, feature-developer Flutter defaults) do not apply here. Verification is `./gradlew assembleDebug`.

**How to apply:** For this repo skip Flutter-specific tooling; build/verify with Gradle. Package visibility (`<queries>` in AndroidManifest) is required for app labels/icons to resolve on Android 11+ — a recurring trap here.
