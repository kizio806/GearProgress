---
name: verify-quality-gates
description: Run and verify all GearProgress automated quality gates and checks
---

# Verify Quality Gates Skill

Execute the complete quality verification pipeline for GearProgress:

```bash
./gradlew check --no-daemon
```

## Checks Performed
- **Spotless**: Code formatting across Java, Kotlin DSL, JSON, YAML, and Markdown.
- **Source Hygiene**: Ensures no `TODO`, `FIXME`, wildcard imports, or `@SuppressWarnings` in Java files.
- **Checkstyle & PMD**: Static code style and rule enforcement across all source sets.
- **SpotBugs**: Bug pattern and concurrency flaw detection.
- **JaCoCo**: Verification of minimum 90% instruction test coverage.
- **Platform Boundaries & Artifacts**: Architecture verification and shaded distribution packaging checks.
