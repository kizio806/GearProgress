# Release Process

GearProgress uses Semantic Versioning (`MAJOR.MINOR.PATCH`) and produces two distinct distribution artifacts per release.

## Distribution Artifacts

- **Paper Distribution**: `GearProgress-<version>-paper.jar`
  - Targets Paper, Purpur, and Folia servers running Minecraft 1.21.x.
  - Native Brigadier command tree and Folia region/async schedulers.
- **Spigot Distribution**: `GearProgress-<version>-spigot.jar`
  - Targets Spigot and CraftBukkit servers running Minecraft 1.21.x.
  - Standard Bukkit command executor and relocated Kyori Adventure runtime.

## Verification Checklist

1. Ensure all quality gates, platform boundaries, compatibility matrices, and tests pass:
   ```bash
   ./gradlew clean check build
   ./scripts/verification/validate-structure.sh
   ./scripts/verification/validate-source-hygiene.sh
   ```
2. Verify both distribution artifacts are created and validated:
   ```bash
   ./gradlew verifyDistributionArtifacts
   ```
3. Update `CHANGELOG.md` with release notes.
4. Update `gearprogress.version` in `gradle.properties`.
5. Tag the release: `git tag -a v<version> -m "Release v<version>"`.
6. Push the tag to GitHub. GitHub Actions builds and publishes both release artifacts.
