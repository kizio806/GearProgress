# Contributing to GearProgress

Thank you for your interest in contributing to **GearProgress**! This guide outlines our architecture, development setup, code hygiene standards, and pull request workflow to help you get started quickly.

---

## 🎯 Architecture & Invariants

GearProgress is an enterprise-grade progressive equipment unlocking engine for Minecraft servers. The codebase enforces strict hexagonal architecture (Ports and Adapters):

1. **`:gearprogress-core` (Pure Java 21 Domain):**
   - Contains immutable models: `EquipmentCategory`, `EquipmentTier`, `TierKey`, `UnlockCondition`, `RestrictionPolicy`, `EnchantmentPolicy`, and `TierProgressState`.
   - Pure mathematical duration engine: `RelativeDurationParser`.
   - **Strict Invariant:** Must remain 100% platform-neutral and Minecraft-free. Never import `org.bukkit.*`, `io.papermc.*`, `net.minecraft.*`, or `org.spongepowered.*`. This is permanently verified by `./gradlew verifyPlatformBoundaries`.

2. **`:gearprogress-application` (Orchestration & Hexagonal Ports):**
   - Coordinates progression lifecycles, timer evaluations, configuration loaders, YAML compilers, and locale management.
   - Defines SPI interfaces: `ProgressionRepository`, `TaskSchedulerPort`, `EventPublisherPort`, `LifecycleReporterPort`, and `TimePort`.
   - **Strict Invariant:** Must also remain 100% free of server APIs. Pure Java only.

3. **`:adapters:storage-sqlite` (Single-Writer Persistence):**
   - Asynchronous SQLite persistence with WAL mode enabled (`PRAGMA journal_mode=WAL;`).
   - Serializes all database writes via `SingleWriterPersistenceExecutor`.

4. **Platform Modules (`:platforms:bukkit`, `:platforms:paper:common`, `:platforms:spigot:common`):**
   - Implements event listeners (craft, use, equip, pickup, inventory move, enchantment), Brigadier commands (Paper/Purpur/Folia), standard Bukkit commands (Spigot/CraftBukkit), and scheduler adapters.
   - **Folia Regional Thread Safety:** Never make assumptions about a single server main thread. Dispatch tasks using `TaskSchedulerPort`.

5. **Single-JAR Multi-Version Distributions (`:distributions:paper`, `:distributions:spigot`):**
   - Every release compiles **exactly two distribution JARs**:
     - `gearprogress-<base>-<range>-paper-purpur-folia.jar`
     - `gearprogress-<base>-<range>-spigot-bukkit.jar`
   - Governed by strict Semantic Versioning (`MAJOR.MINOR.PATCH+mc<RANGE>`, e.g. `1.0.0+mc1.21.x`).

---

## 🗺️ Supported Branches & Version Matrix

Development occurs on `main` and dedicated support branches covering supported Minecraft server versions:

| Branch | Minecraft Line | Supported Server Platforms | Java Baseline | Status |
|---|---|---|---|---|
| **`main`** | 1.21.x | Paper, Purpur, Folia, Spigot, CraftBukkit 1.21–1.21.11 | **Java 21** | Active Development |
| **`support/1.21.x`** | 1.21.x | Paper, Purpur, Folia, Spigot, CraftBukkit 1.21–1.21.11 | **Java 21** | Long-Term Support |

---

## 🛠️ Local Development Setup

### 1. Prerequisites
- **Git**
- **JDK 21** or higher (e.g. Eclipse Temurin)
- **Bash** shell (for verification and release scripts)

### 2. Clone the Repository
```bash
git clone https://github.com/kizio806/GearProgress.git
cd GearProgress
```

### 3. Verification Commands Explained

Run the following Gradle commands to validate your changes:

| Command | Purpose & Checks Performed |
|---|---|
| `./gradlew spotlessApply` | Formats all Java source files to Google Java Format (2 spaces, Unix line endings, sorted imports). |
| `./gradlew test` | Executes all unit, integration, and platform mock tests across all modules. |
| `./gradlew check` | Executes complete quality gates: Spotless, Checkstyle, SpotBugs, PMD, ErrorProne, boundary verification, SemVer check, distribution artifact verification, and JaCoCo coverage verification ($\ge 90\%$). |
| `./gradlew :distributions:paper:build :distributions:spigot:build` | Compiles and packages shaded production distribution JARs. |
| `./scripts/verification/validate-structure.sh` | Validates package declaration consistency, source directories, and documentation links. |
| `./scripts/verification/validate-source-hygiene.sh` | Fails on any `@SuppressWarnings`, wildcard imports (`.*`), or task markers (`TODO`, `FIXME`, `XXX`). |

---

## 🧹 Code Hygiene Standards (Hard Gates)

Every pull request must pass the automated source hygiene checks (`./gradlew validateSourceHygiene`):

- **Zero Suppression Annotations:** `@SuppressWarnings` is strictly forbidden across all modules. Resolve deprecations, warnings, and type mismatches at the source.
- **Zero Wildcard Imports:** `import java.util.*;` is prohibited. Use explicit single-class imports.
- **Zero Task Markers:** No `TODO`, `FIXME`, or `XXX` tags in committed Java code.
- **Compiler Flags:** Compiles under Java 21 with `-Xlint:all` and `-Werror`. Do not introduce compiler warnings.
- **Meaningful Tests:** Never write tests solely to boost line coverage. Every test must validate real behavior, observable contracts, boundary values, error handling, or regressions.

---

## 📝 Commit Conventions

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <short description>
```

**Common types:**
- `feat`: New user-facing or runtime feature
- `fix`: Bug fix or edge-case correction
- `refactor`: Internal code improvement without behavior change
- `test`: Adding or strengthening behavioral tests
- `build`: Gradle build scripts, dependencies, or toolchains
- `ci`: GitHub Actions workflows or pipeline hardening
- `docs`: Documentation, wiki, or markdown guides

**Examples:**
```
fix(parser): prevent regex alternation ordering defect in RelativeDurationParser
feat(restrictions): add enchantment restriction policy for diamond armor
test(command): expand unit tests for reset and reload command dispatching
docs(readme): add architecture diagram and configuration examples
```

---

## 🚀 Pull Request Workflow

1. Fork the repository and create a feature branch (`git checkout -b feat/my-new-feature`).
2. Implement your changes adhering to our architectural boundaries and coding standards.
3. Write thorough tests ensuring JaCoCo instruction coverage remains $\ge 90\%$.
4. Run formatting and verification locally:
   ```bash
   ./gradlew spotlessApply
   ./gradlew clean check build
   ./scripts/verification/validate-structure.sh
   ./scripts/verification/validate-source-hygiene.sh
   ```
5. Commit your changes following Conventional Commits.
6. Push to your fork and submit a Pull Request to `main`.
7. Ensure all automated GitHub Actions CI checks pass.
