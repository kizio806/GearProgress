# Development & Release Process

This guide covers building GearProgress from source, running tests, and preparing releases.

---

## Prerequisites

- **Java Development Kit (JDK):** Version 21 or higher.
- **Gradle:** Wrapper included (`./gradlew`).

---

## Local Build & Verification

Execute all unit tests, static code analysis, and style checks:

```bash
# Apply Spotless formatting
./gradlew spotlessApply

# Run test suite and quality gates
./gradlew check

# Build distribution JARs
./gradlew build
```

Compiled release artifacts will be placed in:
- `distributions/paper/build/libs/GearProgress-<version>-paper.jar`
- `distributions/spigot/build/libs/GearProgress-<version>-spigot.jar`

---

## Quality Gate Invariants

Every pull request and build must pass:
- **Spotless:** Formatting across Java, Kotlin DSL, JSON, and Markdown.
- **Checkstyle & PMD:** Zero warnings or violations.
- **SpotBugs:** 0 bug patterns.
- **JaCoCo:** Minimum 90% instruction coverage across domain and application modules.
- **Source Hygiene:** Zero `@SuppressWarnings`, `TODO`, `FIXME`, or wildcard imports.
