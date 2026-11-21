# GearProgress — AI Agent Guidelines & Architecture Manual

This document defines the mandatory operating standards, behavioral guidelines, and development lifecycle for AI agents and human engineers working on the **GearProgress** codebase.

For top-level engineering philosophy, see the root [`AGENTS.md`](../AGENTS.md).

---

## Agent Operating Structure

The `.agents/` directory defines specialized operational roles, context indexes, rules, skills, and workflows:

### 1. Roles (`.agents/roles/`)
- [`architect.md`](roles/architect.md) — Hexagonal boundaries, port interfaces, and structural integrity.
- [`qa-engineer.md`](roles/qa-engineer.md) — Coverage enforcement ($\ge 90\%$), quality gates, and concurrency stress testing.
- [`plugin-specialist.md`](roles/plugin-specialist.md) — Bukkit/Paper/Folia APIs, event listeners, and scheduler adaptations.
- [`security-auditor.md`](roles/security-auditor.md) — Inventory dupe prevention, SQLite parameterization, and permission security.

### 2. Context (`.agents/context/`)
- [`architecture.md`](context/architecture.md) — Hexagonal module architecture diagram and port descriptions.
- [`tech-stack.md`](context/tech-stack.md) — Java 21 baseline, Gradle 9.8.2, Adventure, Paper/Spigot APIs.
- [`domain-model.md`](context/domain-model.md) — Entities, value objects, unlock conditions, and restriction policies.

### 3. Rules (`.agents/rules/`)
- [`architecture-and-boundaries.md`](rules/architecture-and-boundaries.md) — Hexagonal isolation and zero server imports in core.
- [`quality-gates-and-testing.md`](rules/quality-gates-and-testing.md) — Static analysis, zero warnings, and test coverage standards.
- [`source-hygiene-and-comments.md`](rules/source-hygiene-and-comments.md) — Zero `TODO`, wildcard imports, or `@SuppressWarnings`.
- [`concurrency-and-folia.md`](rules/concurrency-and-folia.md) — Regionized scheduler safety and non-blocking tick threads.
- [`sqlite-and-transactions.md`](rules/sqlite-and-transactions.md) — Single-writer executor, WAL mode, and parameterized queries.
- [`localization-and-adventure.md`](rules/localization-and-adventure.md) — MiniMessage formatting and externalized message bundles.

### 4. Skills (`.agents/skills/`)
- [`verify-quality-gates`](skills/verify-quality-gates/SKILL.md) — Full automated quality pipeline execution.
- [`audit-codebase`](skills/audit-codebase/SKILL.md) — Architectural boundary and concurrency audits.
- [`new-feature`](skills/new-feature/SKILL.md) — End-to-end feature implementation guide.
- [`bug-fix`](skills/bug-fix/SKILL.md) — Systematic defect reproduction and fix protocol.
- [`release`](skills/release/SKILL.md) — Version bump, artifact verification, and release steps.
- [`multi-platform-adaptation`](skills/multi-platform-adaptation/SKILL.md) — Paper, Folia, and Spigot compatibility maintenance.
- [`storage-migration`](skills/storage-migration/SKILL.md) — Database schema migration and WAL safety.

### 5. Workflows (`.agents/workflows/`)
- [`new-feature.md`](workflows/new-feature.md) — Structured process for implementing new features.
- [`bug-fix.md`](workflows/bug-fix.md) — Systematic process for resolving bugs.
- [`release.md`](workflows/release.md) — Complete release checklist and distribution procedure.
