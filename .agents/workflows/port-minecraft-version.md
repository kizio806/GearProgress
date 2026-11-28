# Workflow: Porting to a New Minecraft Version

A systematic, phase-driven process for upgrading GearProgress to support new Minecraft server releases.

---

## Phase 1: Upstream Discovery & API Diff Analysis
1. Inspect the Paper and Spigot release notes for the target Minecraft version.
2. Identify changes in Bukkit events (`PrepareItemCraftEvent`, `InventoryClickEvent`, `PlayerInteractEvent`).
3. Check for deprecated or removed materials and sound keys.

## Phase 2: Sandbox Probing
1. Run `./scripts/probe-minecraft-versions.sh --versions "<candidate_version>"`.
2. Inspect probe logs to ensure dependencies resolve cleanly.

## Phase 3: Version Configuration
1. Update `gradle.properties` using `./scripts/set-minecraft-version.sh`.
2. Ensure `gearprogress.releaseRange` and `gearprogress.version` reflect the new range.
3. Update `paper.minecraftVersions` and `spigot.minecraftVersions`.

## Phase 4: Platform Adaptation & Compilation
1. Run `./gradlew clean compileJava compileTestJava`.
2. Resolve any compiler warnings or deprecations in `:platforms:*`.
3. Never introduce `@SuppressWarnings`.

## Phase 5: Verification & Packaging Gauntlet
1. Run `./gradlew check`.
2. Run `./gradlew verifyCompatibilityMatrix verifySemverVersion verifyDistributionArtifacts`.
3. Build both distribution artifacts:
   ```bash
   ./gradlew :distributions:paper:build :distributions:spigot:build
   ```
4. Perform release dry-run:
   ```bash
   ./scripts/release.sh --dry-run
   ```

## Phase 6: Documentation & Matrix Updates
1. Update `docs/compatibility-matrix.md`.
2. Document version support additions in `CHANGELOG.md` under `[Unreleased]`.
