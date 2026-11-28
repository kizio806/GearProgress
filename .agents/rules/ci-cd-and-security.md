# CI/CD, Quality Pipeline & Security Standards

## Purpose
Defines the mandatory security, static analysis, and automated continuous integration pipeline standards governing GearProgress.

## Standard
1. **GitHub Actions Security:**
   - All third-party actions must be pinned to full immutable commit SHAs with version comments (e.g. `actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1 # v7.0.1`).
   - Workflows must declare minimum required permissions (`permissions: contents: read`).
   - `persist-credentials: false` must be set on `actions/checkout`.
2. **Quality Gates in CI:**
   - `./gradlew check` executes all linters and quality checks: Spotless, Checkstyle, SpotBugs, PMD, ErrorProne, boundary enforcement, and JaCoCo root verification ($\ge 90\%$).
   - Codecov coverage is uploaded on every PR and push; patch coverage must not decrease.
   - SonarCloud analysis is executed with zero open issues, rating A, and proper exclusions for build/distributions/generated directories.
   - CodeQL security analysis runs weekly and on PRs with zero open alerts.
3. **Secret Safety:**
   - Zero hardcoded tokens or API keys. Pinned Gitleaks scanner runs on all commits.
   - Secrets (`MODRINTH_TOKEN`, `SONAR_TOKEN`, `CODECOV_TOKEN`) accessed exclusively via repository secrets.
4. **Shell Script Preference:**
   - Automated scripts must be written in Bash (`set -euo pipefail`) rather than Python or Node.js wherever feasible.

## Verification:
```bash
# Validate formatting, linting, boundaries, and tests locally before pushing
./gradlew spotlessCheck check
./scripts/validate-structure.sh
./scripts/validate-source-hygiene.sh
```
