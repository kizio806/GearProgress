# GearProgress — AI Agent Guidelines & Architecture Manual

This document defines the mandatory operating standards, behavioral guidelines, and development lifecycle for AI agents and human engineers working on the **GearProgress** codebase.

Future AI agents working in this repository must operate as **experienced senior developers and principal software architects**: questioning assumptions, verifying facts against test execution, avoiding shortcuts, and upholding strict technical excellence.

---

## 1. Core Engineering Philosophy & Behavioral Rules

### Mindset & Responsibilities
- **Question Existing Decisions:** Never assume existing code or structure is correct merely because it is already committed. Always verify what the build system compiles and tests against actual test executions.
- **Maintain Architectural Purity:** Platform-independent logic belongs exclusively in `core` and `application`, while `:platforms:bukkit`, `:platforms:paper`, and `:platforms:spigot` provide runtime integrations.
- **Prefer Maintainable & Simple Solutions:** Do not introduce global singletons, service locators, NMS, reflection hacks, or giant manager classes when clean, testable hexagonal architectures suffice.
- **Explain Trade-offs Explicitly:** When proposing changes, outline the pros, cons, and performance implications (e.g. pure Java in `core` and `application` targeting Java 25 baseline, with platform adapters targeting Java 25 on Paper and Spigot).
- **Avoid Shortcuts:** Never bypass `./gradlew clean check build`, never disable quality gates, never suppress compiler warnings or SpotBugs checks, and never leave uncommitted scratch files.
- **Mandatory 100% Green Verification Before Commit & Push:** Never commit changes, push commits, or request PR merges without ensuring 100% green verification. Every quality gate — local unit & integration tests, Checkstyle, SpotBugs, PMD, JaCoCo instruction coverage ($\ge 90\%$ aggregated, $\ge 95\%$ on `core`), release dry-runs, GitHub Actions CI workflows, CodeQL analysis (0 open alerts), and SonarCloud Quality Gate (Rating A, 0 issues) — must pass cleanly without a single failure or warning.

### Critical "Do NOT" Rules
- **Do NOT blindly copy current architecture:** Maintain the strict dependency direction: `platform -> application -> core`.
- **Do NOT leak Minecraft APIs into domain modules:** `gearprogress-core` and `gearprogress-application` must never import Bukkit, Paper, Spigot, or NMS packages. This is permanently enforced by `./gradlew verifyPlatformBoundaries`.
- **Do NOT add unnecessary complexity:** GearProgress is a data-driven progression engine. Prefer reusable unlock conditions, restriction policies, and domain value objects over bespoke hardcoded tier logic.
- **Do NOT assume one global Minecraft main thread:** Preserve Folia regional thread safety. Never schedule server-wide tasks using Bukkit singletons on Folia.
- **Do NOT suppress warnings:** `@SuppressWarnings` and warning-silencing annotations of any kind are strictly forbidden across all modules. Resolve deprecations, type mismatches, and linter warnings at the source.
- **Do NOT add trivial or redundant comments:** Meaningful explanatory comments are permitted when clarifying non-obvious algorithms or contracts, but redundant comments that merely restate code, `TODO`/`FIXME`/`XXX` tags, and wildcard imports are prohibited.
- **Do NOT modify concurrency mechanisms carelessly:** Never alter `SingleWriterPersistenceExecutor` or Folia scheduling mechanisms without running `SingleWriterPersistenceExecutorTest` under high-concurrency stress. All SQLite I/O must remain serialized on the writer thread.

---

## 2. Before Coding: Discovery & Planning

Before touching any code, an AI agent must complete four distinct analysis steps:

### Step 1: Understand the Architecture
1. **Module Separation:**
   - `:gearprogress-core`: Pure Java 25 library. Platform-neutral and Minecraft-free. Contains core domain models (`EquipmentCategory`, `EquipmentTier`, `TierKey`, `UnlockCondition`, `RestrictionPolicy`, `EnchantmentPolicy`, `TierProgressState`) and mathematical parsing (`RelativeDurationParser`).
   - `:gearprogress-application`: Application orchestration and use cases (Java 25). Contains lifecycle management (`ProgressionService`, `TimerService`, `RestrictionQueryService`), YAML compilers (`EquipmentDefinitionCompiler`), configuration loaders (`YamlConfigLoader`), and SPI ports (`ProgressionRepository`, `TaskSchedulerPort`, `EventPublisherPort`, `LifecycleReporterPort`, `TimePort`).
   - `:adapters:storage-sqlite`: Asynchronous single-writer SQLite persistence implementing application ports with WAL mode.
   - `:platforms:bukkit`: Shared Bukkit adapter layer. Provides event listeners (`CraftRestrictionListener`, `UseRestrictionListener`, `EquipRestrictionListener`, `PickupRestrictionListener`, `InventoryMoveRestrictionListener`, `EnchantmentRestrictionListener`), GUI views (`ProgressionGuiView`), and PlaceholderAPI expansion (`GearProgressPlaceholderExpansion`).
   - `:platforms:paper:common`: Paper-specific adapters. Provides Folia region scheduler integration (`FoliaTaskScheduler`), native Brigadier command trees (`GearProgressMainCommand`), and native Adventure MiniMessage.
   - `:platforms:spigot:common`: Spigot/CraftBukkit adapters. Provides classic Bukkit command routing (`SpigotMainCommand`), tab completion, relocated Adventure (`BukkitAudiences`), and `SpigotTaskScheduler`.
   - `:distributions:paper`: Shaded release packaging (`gearprogress-<base>-<range>-paper-purpur-folia.jar`).
   - `:distributions:spigot`: Shaded release packaging (`gearprogress-<base>-<range>-spigot-bukkit.jar`).

2. **Execution Flow:**
   - Equipment configurations are loaded from `equipment.yml` and compiled into immutable `EquipmentDefinition` records.
   - Live gameplay actions (craft, equip, use, pickup, move, enchant) trigger Bukkit event listeners which query `RestrictionQueryService` lock-free from in-memory state.
   - State mutations (unlocking, locking, timer adjustments) are processed by `ProgressionService` and queued asynchronously to `SingleWriterPersistenceExecutor` for serialized disk persistence.

### Step 2: Inspect Existing Patterns & Caveats
- **Single-Writer SQLite Persistence:** All database write operations must execute asynchronously via `SingleWriterPersistenceExecutor`. Never perform blocking database writes on server tick threads.
- **Lock-Free Event Interception:** Restriction checks occur on hot server tick threads. Read queries must never perform disk I/O or acquire reentrant synchronization locks that could stall tick rates.
- **Folia Compatibility:** When Folia is active, tasks must be dispatched through `TaskSchedulerPort` to bind to appropriate region contexts.

### Step 3: Check Documentation & Repository Guidelines
- Read `docs/architecture.md`, `docs/development.md`, and `docs/releases.md`.
- Read wiki articles in `docs/wiki/`.
- Review `.agents/rules/` for detailed coding rules.

### Step 4: Create a Written Execution Plan
- Specify exact files to create or modify.
- Outline how backward compatibility with existing YAML configurations and database schemas will be maintained.
- Identify the verification commands that will validate the changes.

---

## 3. During Coding: Standards & Conventions

### 1. Source Hygiene (Hard Gate)
- **Zero Markers:** No `TODO`, `FIXME`, or `XXX` tags in Java files.
- **Zero Wildcard Imports:** `import java.util.*;` is strictly prohibited. Use explicit single-class imports.
- **Zero Warning Suppressions:** `@SuppressWarnings` is strictly forbidden across all modules. Resolve warnings and deprecations using modern non-deprecated APIs. Enforced by `validateSourceHygiene`.

### 2. Code Style & Clean Architecture
- **Formatting:** 2 spaces for Java indentation (Google Java Style via Spotless), UTF-8 encoding, Unix line endings (`\n`), no trailing whitespace.
- **Compiler Flags:** Compiles with `-Xlint:all` and `-Werror`. Do not introduce compiler warnings.
- **Boolean Predicates:** Name boolean methods and variables descriptively: `isUnlocked`, `isRestricted`, `allowEnchanted`.
- **Modularity:**
  - Never import Bukkit or Paper classes in `core`, `application`, or `adapters/*`.
  - Always keep platform adapters in `:platforms:*`.
- **Coverage Threshold:** Maintain at least **90%** aggregated instruction coverage (`jacocoRootCoverageVerification`) and $\ge 95\%$ on `gearprogress-core`.

### 3. Change Safety & Packaging Contract
- **Golden Packaging Invariant:** Every release builds **EXACTLY TWO production JARs** built via `./gradlew build`:
  1. **EXACTLY ONE Paper JAR** (`gearprogress-<base>-<range>-paper-purpur-folia.jar`)
  2. **EXACTLY ONE Spigot JAR** (`gearprogress-<base>-<range>-spigot-bukkit.jar`)
- **Versioning Policy:** Strict `MAJOR.MINOR.PATCH+mc<RANGE>` (e.g. `1.1.0+mc26.x`). Enforced by `verifySemverVersion`.

---

## 4. Before Finishing: Verification & Review

Before concluding any work, an AI agent must run the complete verification gauntlet:

```bash
# 1. Format code according to Spotless
./gradlew spotlessApply

# 2. Run unit and integration tests across all modules
./gradlew test

# 3. Validate structural layout and source hygiene
./scripts/verification/validate-structure.sh
./scripts/verification/validate-source-hygiene.sh

# 4. Verify distribution artifact packaging
./gradlew :distributions:paper:build :distributions:spigot:build verifyDistributionArtifacts

# 5. Execute full release readiness dry-run
./scripts/release.sh --dry-run --allow-existing-tag

# 6. Execute full quality gate
./gradlew clean check build
```

### Self-Review Checklist
- [ ] Run `git status` to ensure no untracked scratch files or unintended changes exist.
- [ ] Run `git diff` to review every changed line against project standards.
- [ ] Ensure `./scripts/validate-source-hygiene.sh` passes cleanly (0 `@SuppressWarnings`, no `TODO`/`FIXME`).
- [ ] Ensure `./scripts/validate-structure.sh` passes cleanly (canonical docs, no orphaned sources).
- [ ] Ensure `./scripts/verification/verify-markdown-links.sh` reports zero dead links.

---

## 5. AI Agent Operating System (`.agents/`)

The repository includes a modular, role-based AI instruction framework documented in [`.agents/AGENTS.md`](.agents/AGENTS.md):

- **Roles ([`.agents/roles/`](.agents/roles/)):** [`architect.md`](.agents/roles/architect.md), [`qa-engineer.md`](.agents/roles/qa-engineer.md), [`plugin-specialist.md`](.agents/roles/plugin-specialist.md), [`security-auditor.md`](.agents/roles/security-auditor.md), [`devops-engineer.md`](.agents/roles/devops-engineer.md).
- **Context ([`.agents/context/`](.agents/context/)):** [`architecture.md`](.agents/context/architecture.md), [`domain-model.md`](.agents/context/domain-model.md), [`matrix.md`](.agents/context/matrix.md), [`tech-stack.md`](.agents/context/tech-stack.md).
- **Rules ([`.agents/rules/`](.agents/rules/)):** [`architecture-and-boundaries.md`](.agents/rules/architecture-and-boundaries.md), [`quality-gates-and-testing.md`](.agents/rules/quality-gates-and-testing.md), [`versioning-and-releases.md`](.agents/rules/versioning-and-releases.md), [`source-hygiene-and-comments.md`](.agents/rules/source-hygiene-and-comments.md), [`concurrency-and-folia.md`](.agents/rules/concurrency-and-folia.md), [`sqlite-and-transactions.md`](.agents/rules/sqlite-and-transactions.md), [`localization-and-adventure.md`](.agents/rules/localization-and-adventure.md), [`minecraft-versioning-and-matrix.md`](.agents/rules/minecraft-versioning-and-matrix.md), [`ci-cd-and-security.md`](.agents/rules/ci-cd-and-security.md), [`review-checklist-and-conventions.md`](.agents/rules/review-checklist-and-conventions.md).
- **Skills ([`.agents/skills/`](.agents/skills/)):** [`verify-quality-gates`](.agents/skills/verify-quality-gates/SKILL.md), [`audit-codebase`](.agents/skills/audit-codebase/SKILL.md), [`new-feature`](.agents/skills/new-feature/SKILL.md), [`bug-fix`](.agents/skills/bug-fix/SKILL.md), [`release`](.agents/skills/release/SKILL.md), [`multi-platform-adaptation`](.agents/skills/multi-platform-adaptation/SKILL.md), [`storage-migration`](.agents/skills/storage-migration/SKILL.md), [`port-minecraft-version`](.agents/skills/port-minecraft-version/SKILL.md), [`equipment-progression-tuning`](.agents/skills/equipment-progression-tuning/SKILL.md).
- **Workflows ([`.agents/workflows/`](.agents/workflows/)):** [`new-feature.md`](.agents/workflows/new-feature.md), [`bug-fix.md`](.agents/workflows/bug-fix.md), [`release.md`](.agents/workflows/release.md), [`port-minecraft-version.md`](.agents/workflows/port-minecraft-version.md), [`dependency-upgrade.md`](.agents/workflows/dependency-upgrade.md).
- **Operational Guides ([`.agents/guides/`](.agents/guides/)):** [`onboarding.md`](.agents/guides/onboarding.md), [`troubleshooting.md`](.agents/guides/troubleshooting.md), [`dependency-management.md`](.agents/guides/dependency-management.md), [`review-checklist.md`](.agents/guides/review-checklist.md), [`prompts.md`](.agents/guides/prompts.md), [`codeql-sonarcloud-codecov.md`](.agents/guides/codeql-sonarcloud-codecov.md).
