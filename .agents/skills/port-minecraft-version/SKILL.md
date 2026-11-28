---
name: port-minecraft-version
description: Guide and execute the end-to-end upgrade or port of GearProgress to a new Minecraft version, updating version matrices, toolchains, and compatibility ranges.
---

# Port Minecraft Version Skill

## When to use it
Use this skill when:
- Adding support for a new Minecraft minor or patch release (e.g. 1.21.12 or 1.22.x).
- Updating Paper API or Spigot API dependency versions in `gradle/libs.versions.toml`.
- Probing whether candidate Minecraft versions compile cleanly against the current codebase.

## Required steps
1. **Probe Candidate Versions:**
   - Run the automated probe script against candidate versions in an isolated sandbox:
     ```bash
     ./scripts/probe-minecraft-versions.sh --versions "<candidate_version>"
     ```
2. **Determine API Changes:**
   - Inspect upstream Bukkit/Paper API changes affecting inventory events, material names, or command registration.
3. **Update Version Properties:**
   - Update `gradle.properties` using the automated script:
     ```bash
     ./scripts/set-minecraft-version.sh \
       --base-version 1.0.0 \
       --release-range <target_range> \
       --mod-version <new_version> \
       --supported-versions "<supported_csv>"
     ```
4. **Compile and Fix Breakages:**
   - Run `./gradlew clean compileJava compileTestJava`.
   - Update platform adapters in `platforms/bukkit`, `platforms/paper`, or `platforms/spigot` if server APIs changed.
5. **Verify Compatibility Gates:**
   - Run `./gradlew verifyCompatibilityMatrix verifySemverVersion`.
6. **Execute Full Test & Packaging Gauntlet:**
   - Run `./gradlew clean check build verifyDistributionArtifacts`.

## Validation checklist
- [ ] Base version is included in both `paper.minecraftVersions` and `spigot.minecraftVersions`.
- [ ] No duplicate version strings in matrix properties.
- [ ] Version adheres to `MAJOR.MINOR.PATCH+mc<RANGE>`.
- [ ] `./gradlew verifyCompatibilityMatrix` passes.
- [ ] Both production distribution JARs compile and package cleanly.
