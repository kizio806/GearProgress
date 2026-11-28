# Development & Release Process

This guide covers building GearProgress from source, running tests, and preparing releases.

---

## Prerequisites

- **Java Development Kit (JDK):** Version 21 or higher (Eclipse Temurin recommended).
- **Gradle:** Wrapper included (`./gradlew`).
- **Bash Shell:** For automated verification and release scripts.

---

## Local Build & Verification

Execute all unit tests, static code analysis, and style checks:

```bash
# Apply Spotless formatting (Google Java Format)
./gradlew spotlessApply

# Run complete quality pipeline and test suites
./gradlew check

# Build production distribution JARs
./gradlew :distributions:paper:build :distributions:spigot:build
```

Compiled release artifacts will be placed in:
- `distributions/paper/build/libs/gearprogress-<base>-<range>-paper-purpur-folia.jar`
- `distributions/spigot/build/libs/gearprogress-<base>-<range>-spigot-bukkit.jar`

---

## Automated Verification Scripts

The repository includes standalone verification scripts executed during local builds and CI:

```bash
# Verify directory structure, package statements, and markdown links
./scripts/verification/validate-structure.sh

# Verify source hygiene (zero @SuppressWarnings, zero TODOs, zero wildcard imports)
./scripts/verification/validate-source-hygiene.sh

# Test release packaging in dry-run mode
./scripts/release.sh --dry-run
```

---

## Quality Gate Invariants

Every build and pull request must strictly pass:
- **Spotless:** Enforces Google Java Format on Java and ktlint on build scripts.
- **Checkstyle & PMD:** Zero warnings or violations.
- **SpotBugs:** Zero bug patterns detected.
- **JaCoCo:** Minimum **90%** instruction coverage across domain and application modules.
- **Platform Boundaries:** Zero Bukkit/Paper imports in `core` and `application`.
- **Source Hygiene:** Hard gate rejecting any `@SuppressWarnings`, `TODO`, `FIXME`, or wildcard imports.
