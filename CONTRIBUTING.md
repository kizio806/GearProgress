# Contributing to GearProgress

Thank you for your interest in contributing to GearProgress!

## Code Quality Standards
- Pure Java 21 baseline for `core` and `application`.
- Google Java Format enforced via Spotless (`./gradlew spotlessApply`).
- 0 `@SuppressWarnings` annotations.
- 0 `TODO`/`FIXME` markers.
- 0 wildcard imports.
- High test coverage with JaCoCo verification.

## Building and Verification
```bash
./gradlew spotlessApply
./gradlew clean check build
./scripts/verification/validate-structure.sh
./scripts/verification/validate-source-hygiene.sh
```
