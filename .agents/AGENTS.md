# GearProgress — AI Agent Guidelines & Architecture Manual

This document defines the mandatory operating standards, behavioral guidelines, and development lifecycle for AI agents and human engineers working on the **GearProgress** codebase.

For top-level engineering philosophy, see the root [`AGENTS.md`](../AGENTS.md).

---

## Agent Operating Structure

The `.agents/` directory defines specialized operational roles, context indexes, rules, skills, workflows, and operational guides:

### 1. Roles (`.agents/roles/`)
- [`architect.md`](roles/architect.md) — Hexagonal boundaries, port interfaces, and structural integrity.
- [`qa-engineer.md`](roles/qa-engineer.md) — Coverage enforcement ($\ge 90\%$), quality gates, and concurrency stress testing.
- [`plugin-specialist.md`](roles/plugin-specialist.md) — Bukkit/Paper/Folia APIs, event listeners, and scheduler adaptations.
- [`security-auditor.md`](roles/security-auditor.md) — Inventory dupe prevention, SQLite parameterization, and permission security.
- [`devops-engineer.md`](roles/devops-engineer.md) — CI/CD workflows, Golden Packaging Invariant, and release readiness.

### 2. Context (`.agents/context/`)
- [`architecture.md`](context/architecture.md) — Hexagonal module architecture diagram and port descriptions.
- [`domain-model.md`](context/domain-model.md) — Entities, value objects, unlock conditions, and restriction policies.
- [`matrix.md`](context/matrix.md) — Compatibility matrix, platform runtimes, and Golden Packaging Invariant.
- [`tech-stack.md`](context/tech-stack.md) — Java 21 baseline, Gradle 9.8.2, Adventure, Paper/Spigot APIs.

### 3. Rules (`.agents/rules/`)
- [`architecture-and-boundaries.md`](rules/architecture-and-boundaries.md) — Hexagonal isolation and zero server imports in core.
- [`quality-gates-and-testing.md`](rules/quality-gates-and-testing.md) — Static analysis, zero warnings, and test coverage standards.
- [`versioning-and-releases.md`](rules/versioning-and-releases.md) — Authoritative SemVer, multi-platform artifact packaging, and release standards.
- [`source-hygiene-and-comments.md`](rules/source-hygiene-and-comments.md) — Zero `TODO`, wildcard imports, or `@SuppressWarnings`.
- [`concurrency-and-folia.md`](rules/concurrency-and-folia.md) — Regionized scheduler safety and non-blocking tick threads.
- [`sqlite-and-transactions.md`](rules/sqlite-and-transactions.md) — Single-writer executor, WAL mode, and parameterized queries.
- [`localization-and-adventure.md`](rules/localization-and-adventure.md) — MiniMessage formatting and externalized message bundles.
- [`minecraft-versioning-and-matrix.md`](rules/minecraft-versioning-and-matrix.md) — Platform version lists, range syntax, and matrix verification.
- [`ci-cd-and-security.md`](rules/ci-cd-and-security.md) — Pinned GitHub Actions, CodeQL, SonarCloud, and Codecov standards.
- [`review-checklist-and-conventions.md`](rules/review-checklist-and-conventions.md) — Pre-commit review checklist and Conventional Commits.

### 4. Skills (`.agents/skills/`)
- [`verify-quality-gates`](skills/verify-quality-gates/SKILL.md) — Full automated quality pipeline execution.
- [`audit-codebase`](skills/audit-codebase/SKILL.md) — Architectural boundary and concurrency audits.
- [`new-feature`](skills/new-feature/SKILL.md) — End-to-end feature implementation guide.
- [`bug-fix`](skills/bug-fix/SKILL.md) — Systematic defect reproduction and fix protocol.
- [`release`](skills/release/SKILL.md) — Version bump, artifact verification, and release steps.
- [`multi-platform-adaptation`](skills/multi-platform-adaptation/SKILL.md) — Paper, Folia, and Spigot compatibility maintenance.
- [`storage-migration`](skills/storage-migration/SKILL.md) — Database schema migration and WAL safety.
- [`port-minecraft-version`](skills/port-minecraft-version/SKILL.md) — Upstream server version probing, metadata updating, and compilation adaptation.
- [`equipment-progression-tuning`](skills/equipment-progression-tuning/SKILL.md) — SMP pacing, duration tokens, and restriction policy calibration.

### 5. Workflows (`.agents/workflows/`)
- [`new-feature.md`](workflows/new-feature.md) — Structured process for implementing new features.
- [`bug-fix.md`](workflows/bug-fix.md) — Systematic process for resolving bugs.
- [`release.md`](workflows/release.md) — Complete release checklist and distribution procedure.
- [`port-minecraft-version.md`](workflows/port-minecraft-version.md) — Multi-phase procedure for upgrading to new Minecraft releases.
- [`dependency-upgrade.md`](workflows/dependency-upgrade.md) — Version catalog updates and stress verification.

### 6. AI Operational Guides (`.agents/guides/`)
- [`onboarding.md`](guides/onboarding.md) — Mental model and first-time setup for AI models and developers.
- [`troubleshooting.md`](guides/troubleshooting.md) — Decision trees for compilation, test, and SQLite issues.
- [`dependency-management.md`](guides/dependency-management.md) — Version catalogs and vulnerability scanning.
- [`review-checklist.md`](guides/review-checklist.md) — Code review and pull request checklist.
- [`prompts.md`](guides/prompts.md) — Pre-calibrated prompts for specialized AI roles.
- [`codeql-sonarcloud-codecov.md`](guides/codeql-sonarcloud-codecov.md) — Quality gate configuration and maintenance.
