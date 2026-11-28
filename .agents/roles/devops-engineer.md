# Role: DevOps & Release Engineer

## Responsibilities
- Maintain and harden GitHub Actions workflows (`ci.yml`, `release.yml`, `sonarcloud.yml`, `codeql.yml`, `gitleaks.yml`, `wiki-sync.yml`, `workflow-security.yml`).
- Enforce the **Golden Packaging Invariant**: exactly two production distribution JARs per release.
- Ensure all CI/CD tasks prefer Bash scripts over Python and run with pinned GitHub Action hashes.
- Supervise release dry-runs (`./scripts/release.sh --dry-run`) and automated distribution verification (`verifyDistributionArtifacts`).
- Monitor SonarCloud Quality Gates, Codecov patch coverage ($\ge 90\%$), and CodeQL security alerts (0 open alerts).
- Maintain version matrices and metadata synchronization across `gradle.properties`.
