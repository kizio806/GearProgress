# Minecraft Versioning and Compatibility Matrix Rule

## Purpose
GearProgress delivers two release JARs targeting Minecraft 26.1.x, 26.2, and 26.3 while maintaining strict Semantic Versioning. The build system dynamically cross-checks all version properties to ensure that Paper and Spigot version lists agree, do not contain duplicates, and stay within supported release ranges.

## Standard
- **Golden Packaging Invariant:** Exactly two distribution JARs per release:
  - `gearprogress-<BASE_VERSION>-<MINECRAFT_RANGE>-paper-purpur-folia.jar`
  - `gearprogress-<BASE_VERSION>-<MINECRAFT_RANGE>-spigot-bukkit.jar`
- **SemVer Compliance:** `gearprogress.version` must strictly conform to `MAJOR.MINOR.PATCH+mc<RANGE>` (e.g. `1.1.0+mc26.x`). Enforced by Gradle task `verifySemverVersion`.
- **Matrix Non-Empty & Unique:** `paper.minecraftVersions` and `spigot.minecraftVersions` must be non-empty, comma-separated lists with zero duplicate versions. Enforced by Gradle task `verifyCompatibilityMatrix`.
- **Release Synchronization:** Base versions are synchronized across all support branches and never bumped independently.

## Do:
```bash
# Correct: Use the automated version update script
./scripts/set-minecraft-version.sh \
  --base-version 1.1.0 \
  --release-range 26.x \
  --mod-version 1.1.0+mc26.x \
  --supported-versions "26.1,26.1.1,26.1.2,26.2,26.3"
```

## Don't:
```properties
# Bad: Manual editing with malformed version strings or mismatched suffixes
gearprogress.version=1.0.0-SNAPSHOT  # ERROR: Must match MAJOR.MINOR.PATCH+mc<RANGE>
paper.minecraftVersions=26.3,26.3    # ERROR: Duplicate entries rejected!
```

## Verification:
```bash
./gradlew verifySemverVersion verifyCompatibilityMatrix verifyDistributionArtifacts
```
