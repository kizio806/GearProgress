# Review Checklist & Commit Conventions

## Purpose
Establishes the review standards, pre-commit checklists, and Conventional Commits conventions required of both human engineers and AI assistants.

---

## 📝 Commit Conventions

All commits must follow the [Conventional Commits](https://www.conventionalcommits.org/) format:

```text
<type>(<scope>): <short description in imperative present tense>
```

### Allowed Types
- `feat`: New user-facing feature or domain capability.
- `fix`: Bug fix, error handling correction, or regression protection.
- `refactor`: Structural code improvement without observable behavior change.
- `test`: Adding or strengthening unit, integration, or MockBukkit tests.
- `build`: Gradle dependencies, build logic, or toolchain updates.
- `ci`: GitHub Actions workflows or pipeline updates.
- `docs`: Documentation, wiki, or markdown guides.

### Common Scopes
- `core`: Domain models, policies, duration parser.
- `application`: Services, compilers, loaders, ports.
- `storage`: SQLite repository, persistence executor, migrations.
- `platform`: Bukkit, Paper, or Spigot adapters.
- `command`: Brigadier or Bukkit command implementations.
- `gui`: Progression chest inventory views.
- `release`: Version bump or artifact verification.

---

## 📋 Pre-Commit & PR Review Checklist

Before creating a commit or requesting a PR review, verify each of the following:

- [ ] **Zero Warnings:** Code compiles with `-Xlint:all` and `-Werror` without warnings.
- [ ] **Zero Suppressions:** `@SuppressWarnings` is not used anywhere in the codebase.
- [ ] **Zero Markers:** No `TODO`, `FIXME`, or `XXX` tags in committed Java code.
- [ ] **Zero Wildcard Imports:** `import java.util.*;` is prohibited; single-class imports used.
- [ ] **Platform Boundary Purity:** `core` and `application` do not import Bukkit, Paper, or Spigot classes.
- [ ] **Test Coverage:** Aggregated instruction coverage remains $\ge 90\%$ (and $\ge 95\%$ on `core`).
- [ ] **Concurrency Safety:** SQLite write operations remain strictly serialized on the background executor thread.
- [ ] **Folia Compatibility:** No server-wide scheduler tasks scheduled via Bukkit singletons on Folia.
- [ ] **Documentation Links:** All relative links verified with `./scripts/verification/verify-markdown-links.sh` (0 broken links).
- [ ] **Changelog Entry:** User-visible changes documented in `CHANGELOG.md` under `[Unreleased]`.
