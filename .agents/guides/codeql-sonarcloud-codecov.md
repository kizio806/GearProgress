# CodeQL, SonarCloud & Codecov Integration Guide

This guide details the configuration, quality gates, and maintenance procedures for SonarCloud, Codecov, and CodeQL in **GearProgress**.

---

## ☁️ SonarCloud Configuration

SonarCloud performs automated static analysis on all branches and pull requests.

### Exclusions & Scopes
In `build.gradle.kts`:
```kotlin
sonar {
    properties {
        property("sonar.projectKey", "kizio806_GearProgress")
        property("sonar.organization", "kizio806")
        property("sonar.projectName", "GearProgress")
        property("sonar.host.url", "https://sonarcloud.io")
        val exclusions = listOf(
            "**/build/**",
            "**/build-logic/**",
            "**/distributions/**",
            "**/generated/**",
            "**/mock/**",
            "**/testutil/**",
            "**/wrapper/**",
            "**/src/main/resources/**",
            "**/src/test/**",
        ).joinToString(",")
        property("sonar.exclusions", exclusions)
        property("sonar.coverage.exclusions", exclusions)
        property("sonar.cpd.exclusions", exclusions)
    }
}
```

### Quality Gate Requirements:
- **Quality Gate:** Rating **A** across Reliability, Security, Maintainability.
- **Coverage on New Code:** $\ge 85\%$.
- **Duplicated Lines:** $< 3\%$.

---

## ☂️ Codecov Configuration

Codecov tracks test coverage trends and reports PR status.

### Specifications in `.codecov.yml`:
- **Target:** **90%** project and patch coverage.
- **Ignore Rules:** Ignores `distributions/**`, `build-logic/**`, generated sources, test directories, and static resources.
- **Components:** Tracks component coverage for `core`, `application`, `adapters`, and `platforms`.

---

## 🛡️ GitHub CodeQL Analysis

CodeQL performs semantic security analysis to discover potential injection vulnerabilities or concurrency hazards.

### Workflow: `.github/workflows/codeql.yml`
- Runs weekly on schedules and on every PR to `main`.
- **Alert Standard:** **0 open alerts** required.
