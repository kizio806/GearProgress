# GearProgress — AI Agent Guidelines & Architecture Manual

This document defines the mandatory operating standards, behavioral guidelines, and development lifecycle for AI agents and human engineers working on the **GearProgress** codebase.

Future AI agents working in this repository must operate as **experienced senior developers and principal software architects**: questioning assumptions, verifying facts against test execution, avoiding shortcuts, and upholding strict technical excellence.

---

## 1. Core Engineering Philosophy & Behavioral Rules

### Mindset & Responsibilities
- **Question Existing Decisions:** Verify what the build system compiles and tests. Never assume code is correct merely because it is committed.
- **Maintain Architectural Purity:** Platform-independent logic belongs exclusively in `core` and `application`, while `:platforms:paper` provides Paper integration.
- **Prefer Maintainable & Simple Solutions:** Do not introduce global singletons, service locators, NMS, reflection hacks, or giant manager classes when clean, testable hexagonal architectures suffice.
- **Explain Trade-offs Explicitly:** Pure Java in `core` and `application` targets Java 21 baseline.
- **Avoid Shortcuts:** Never bypass `./gradlew clean check build`, never disable quality gates, never suppress compiler warnings or SpotBugs checks, and never leave uncommitted scratch files.
- **Mandatory 100% Green Verification:** Every quality gate — local unit & integration tests, Checkstyle, SpotBugs, PMD, JaCoCo instruction coverage ($\ge 90\%$), CodeQL, and GitHub Actions CI — must pass cleanly without a single failure or warning.

### Critical "Do NOT" Rules
- **Do NOT leak Minecraft APIs into domain modules:** `gearprogress-core` and `gearprogress-application` must never import Bukkit, Paper, Spigot, or NMS packages.
- **Do NOT suppress warnings:** `@SuppressWarnings` is strictly forbidden across all modules. Resolve deprecations, type mismatches, and linter warnings at the source.
- **Do NOT add trivial or redundant comments:** Meaningful explanatory comments are permitted when clarifying non-obvious algorithms or contracts, but redundant comments that merely restate code, `TODO`/`FIXME`/`XXX` tags, and wildcard imports are prohibited.
- **Do NOT modify concurrency mechanisms carelessly:** Never alter `SingleWriterPersistenceExecutor` or Folia scheduling mechanisms without running `SingleWriterPersistenceExecutorTest` under high-concurrency stress. All SQLite I/O must remain serialized on the writer thread.

---

## 2. Architecture & Modules

1. **Module Separation:**
   - `:gearprogress-core`: Pure Java 21 library. Platform-neutral and Minecraft-free. Contains core domain models (`EquipmentCategory`, `EquipmentTier`, `TierKey`, `UnlockCondition`, `RestrictionPolicy`, `EnchantmentPolicy`, `TierProgressState`).
   - `:gearprogress-application`: Application orchestration and use cases (Java 21). Contains progression lifecycle, timer evaluation, YAML config compiler, locale loader, and hexagonal SPI ports.
   - `:adapters:storage-sqlite`: Asynchronous single-writer SQLite persistence with WAL mode.
   - `:platforms:bukkit`: Shared Bukkit event listeners, item classification, audience-based messaging, and PlaceholderAPI expansion.
   - `:platforms:paper:common`: Paper/Purpur/Folia platform adapter. Brigadier commands, Folia-safe schedulers.
   - `:platforms:spigot:common`: Spigot/CraftBukkit platform adapter. Standard Bukkit commands, BukkitScheduler, relocated Adventure.
   - `:distributions:paper`: Shaded release packaging (`gearprogress-<base>-<range>-paper-purpur-folia.jar`).
   - `:distributions:spigot`: Shaded release packaging (`gearprogress-<base>-<range>-spigot-bukkit.jar`).
   - Versioning Standard: Strict `MAJOR.MINOR.PATCH+mc<RANGE>` (see `.agents/rules/versioning-and-releases.md`).

---

## 3. Standards & Conventions

### Source Hygiene (Hard Gate)
- **Zero Markers:** No `TODO`, `FIXME`, or `XXX` tags in Java files.
- **Zero Wildcard Imports:** `import java.util.*;` is strictly prohibited.
- **Zero Warning Suppressions:** `@SuppressWarnings` is strictly forbidden. Enforced by `validateSourceHygiene`.

### Code Style
- **Formatting:** 2 spaces for Java indentation (Google Java Style via Spotless), UTF-8 encoding, Unix line endings (`\n`).
- **Compiler Flags:** Compiles with `-Xlint:all` and `-Werror`.
