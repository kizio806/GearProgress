---
name: release
description: Release packaging, validation, and versioning for GearProgress
---

# Release Skill

Perform release operations:
1. Ensure git tree is clean.
2. Bump version in `gradle.properties` and finalize `CHANGELOG.md`.
3. Run `./gradlew clean check build --no-daemon`.
4. Validate generated distribution jars (`GearProgress-<version>-paper.jar` and `GearProgress-<version>-spigot.jar`).
5. Tag and publish release.
