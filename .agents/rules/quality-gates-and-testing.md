# Quality Gates and Testing Standards

## Purpose
GearProgress maintains rigorous testing standards and automated quality gates.

## Standard
- **Instruction Coverage Threshold:** Maintain at least 80%-90% instruction coverage across core, application, and persistence modules.
- **Null Safety:** All public APIs and internal methods must be null-safe (`Objects.requireNonNull`).
- **Static Analysis:** Spotless, Checkstyle, PMD, SpotBugs, and Error Prone run on every build. Zero warnings permitted.
