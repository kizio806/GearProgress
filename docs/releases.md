# Release Management & Versioning Standards

This document defines the authoritative versioning architecture, multi-platform packaging invariants, platform publishing standards, and validation rules for **GearProgress**.

---

## 1. Authoritative Versioning Policy

GearProgress follows an engineering-grade versioning policy based on [Semantic Versioning 2.0.0](https://semver.org/) combined with SemVer build metadata for Minecraft compatibility lines.

### Version Format

```text
MAJOR.MINOR.PATCH+mc<MINECRAFT_RANGE>
```

Example for the active `main` branch:
```text
1.1.0+mc26.x
```

### Increment Semantics

- **MAJOR (`X.0.0`)**: Incremented when introducing breaking changes affecting operators, equipment rule YAML schemas, public APIs, or database storage layouts.
- **MINOR (`x.Y.0`)**: Incremented when introducing backward-compatible new features, progression triggers, or integrations.
- **PATCH (`x.y.Z`)**: Incremented for backward-compatible bug fixes, performance optimizations, and security patches.

*Note:* Minecraft version changes alone do **NOT** modify `MAJOR.MINOR.PATCH`. Minecraft compatibility is captured exclusively in the `+mc<RANGE>` metadata suffix.

---

## 2. Critical Global Synchronization Rule

The defining principle of this versioning architecture is **global base version synchronization**:

> **Every active support branch must use the exact same base project version.**

The active compatibility branches are maintained as follows:
- All active compatibility lines must share the identical base version (`1.1.0`).
- Base versions are never incremented independently on a single branch.
- Version upgrades are applied across all active support branches in lockstep.

---

## 3. Golden Packaging Invariant (Universal Rule)

Every official release produces **EXACTLY TWO production distribution JARs** built via Gradle:

1. **Paper Family JAR**:
   `gearprogress-<BASE_VERSION>-<MINECRAFT_RANGE>-paper-purpur-folia.jar`
   - Example: `gearprogress-1.1.0-mc26.x-paper-purpur-folia.jar`
   - Platform Compatibility: **Paper**, **Purpur**, and **Folia** (Minecraft 26.1.x, 26.2, and 26.3).
   - Features: Native Adventure MiniMessage, asynchronous event routing, Folia region scheduling via `RegionTaskScheduler`, and Brigadier commands.

2. **Spigot Family JAR**:
   `gearprogress-<BASE_VERSION>-<MINECRAFT_RANGE>-spigot-bukkit.jar`
   - Example: `gearprogress-1.1.0-mc26.x-spigot-bukkit.jar`
   - Platform Compatibility: **Spigot** and **CraftBukkit** (Minecraft 26.1.x, 26.2, and 26.3).
   - Features: Dedicated Spigot task scheduling, Spigot PDC persistence keys, and relocated Kyori Adventure components.

Never bundle both server platforms into a single monolithic JAR, and never produce separate fragmented JARs for Folia or Purpur.

---

## 4. Platform Publishing Standards (Modrinth & CurseForge)

Every official release deploys with **EXACTLY TWO distribution version entries**:

### Main Plugin Project Releases
1. **Paper, Purpur, and Folia**:
   - Version ID / Number: `<BASE_VERSION>+mc<MINECRAFT_RANGE>.paper` (e.g. `1.0.0+mc1.21.x.paper`)
   - Supported Loaders: `paper`, `purpur`, `folia`
   - Supported Game Versions: `26.1`, `26.1.1`, `26.1.2`, `26.2`, and `26.3`
   - File: `gearprogress-1.1.0-mc26.x-paper-purpur-folia.jar`
2. **Spigot and CraftBukkit**:
   - Version ID / Number: `<BASE_VERSION>+mc<MINECRAFT_RANGE>.spigot` (e.g. `1.0.0+mc1.21.x.spigot`)
   - Supported Loaders: `spigot`, `bukkit`
   - Supported Game Versions: `26.1`, `26.1.1`, `26.1.2`, `26.2`, and `26.3`
   - File: `gearprogress-1.1.0-mc26.x-spigot-bukkit.jar`

---

## 5. Git Tags and GitHub Releases

Every release is triggered by pushing an annotated Git tag:

### Git Tag Convention
```text
v<BASE_VERSION>+mc<MINECRAFT_RANGE>
```
Example: `v1.0.0+mc1.21.x`

### GitHub Release Title Convention
```text
v<BASE_VERSION> — Minecraft <MINECRAFT_RANGE>
```
Example: `v1.0.0 — Minecraft 1.21.x`

Each GitHub Release contains:
- 1 Paper/Purpur/Folia JAR (`gearprogress-1.0.0-mc1.21.x-paper-purpur-folia.jar`)
- 1 Spigot/Bukkit JAR (`gearprogress-1.0.0-mc1.21.x-spigot-bukkit.jar`)
- Verified SHA-256 checksums file (`SHA256SUMS.txt`)

---

## 6. Automated Validation & Safety Gates

Continuous integration enforces release readiness and prevents accidental divergence:

```bash
# 1. Run full verification gates and test suites
./gradlew clean check

# 2. Build production distribution artifacts
./gradlew :distributions:paper:build :distributions:spigot:build

# 3. Test release readiness in dry-run mode
./scripts/release.sh --dry-run
```

The verification pipeline automatically enforces:
- SemVer compliance with mandatory `+mc<RANGE>` metadata (`verifySemverVersion`).
- Compatibility matrix configuration (`verifyCompatibilityMatrix`).
- Platform boundary isolation (`verifyPlatformBoundaries`).
- Deterministic production distribution artifact naming and existence (`verifyDistributionArtifacts`).
- Source hygiene, comment standards, and spotless formatting (`validateSourceHygiene`, `spotlessCheck`).
- Aggregated test coverage thresholds (`jacocoRootCoverageVerification`).
