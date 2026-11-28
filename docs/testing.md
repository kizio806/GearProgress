# Testing Standards & Quality Gates

This document defines testing strategies, architectural boundaries, and test coverage policies for **GearProgress**.

---

## 🎯 Testing Philosophy

We uphold strict enterprise testing standards to guarantee rock-solid stability across modern Paper, Purpur, Folia, and Spigot server runtimes:

- **Meaningful Behavioral Tests:** Every test must validate real observable contracts, domain logic, boundary conditions, or regression scenarios. Tests written solely to inflate line coverage metrics without asserting behavior are strictly prohibited.
- **Fast JVM Execution:** Domain logic in `gearprogress-core` and `gearprogress-application` is 100% decoupled from Minecraft and Bukkit runtime classes. The full unit test suite runs in seconds without starting a Minecraft server.
- **MockBukkit Platform Verification:** Event listeners and commands are thoroughly tested using lightweight, in-memory MockBukkit mocks.
- **Concurrency & SQLite Stress Testing:** `SingleWriterPersistenceExecutorTest` validates serialized execution, task draining during shutdown, and rejection policies under high-concurrency loads.

---

## 🧪 Test Suites Overview

### 1. `gearprogress-core` Unit Tests
- `RelativeDurationParserTest`: Verifies deterministic parsing of days, weeks, hours, minutes, seconds, combined units, ISO fallbacks, and malformed inputs.
- Domain record value equality and immutability validation.

### 2. `gearprogress-application` Unit & Integration Tests
- `EquipmentDefinitionCompilerTest`: Validates YAML compiler diagnostics, warning emissions for unknown actions, duration parsing failures, and record mappings.
- `YamlConfigLoaderTest`: Tests configuration loading, missing file fallbacks, empty inputs, and date parsing.
- `ProgressionServiceTest`: Tests unlock, lock, reset, initial state caching, and listener notifications.
- `TimerServiceTest`: Tests remaining duration calculations, timer pause/resume states, elapsed time additions, and clock edge cases.
- `RestrictionQueryServiceTest`: Tests lock-free memory queries across all restriction flags and enchantment policies.

### 3. `adapters:storage-sqlite` Persistence Tests
- `SQLiteProgressionRepositoryTest`: Validates schema creation, transactional reads/writes, WAL mode verification, and persistence recovery.
- `SingleWriterPersistenceExecutorTest`: Validates serialized queue processing and graceful shutdown.

### 4. Platform Adapter Tests (`platforms/bukkit`, `platforms/paper`, `platforms/spigot`)
- `EquipRestrictionListenerTest`, `CraftRestrictionListenerTest`, `UseRestrictionListenerTest`, `PickupRestrictionListenerTest`, `InventoryMoveRestrictionListenerTest`, `EnchantmentRestrictionListenerTest`: Validates event cancellation, non-player entity filtering, hotbar swapping, and bypass permissions.
- `GearProgressMainCommandTest` & `SpigotMainCommandTest`: Full coverage of command routing, permission checks, error messages, and tab completions.
- `ProgressionGuiViewTest`: Validates virtual inventory generation, icon resolution, and click listeners.
- `GearProgressPlaceholderExpansionTest`: Validates all `%gearprogress_*%` placeholders and fallback outputs.

---

## 📊 Coverage Thresholds (Hard Gates)

Coverage is measured via JaCoCo and enforced during `./gradlew check`:

| Module | Metric | Enforced Minimum | Current Status |
|---|---|:---:|:---:|
| `gearprogress-core` | Instruction Coverage | **95.0%** | **98.37%** |
| `gearprogress-application` | Instruction Coverage | **90.0%** | **95.53%** |
| `adapters:storage-sqlite` | Instruction Coverage | **90.0%** | **96.43%** |
| `platforms:bukkit` | Instruction Coverage | **90.0%** | **97.42%** |
| `platforms:paper:common` | Instruction Coverage | **90.0%** | **95.94%** |
| `platforms:spigot:common` | Instruction Coverage | **85.0%** | **94.35%** |
| **Aggregated Root** | **Instruction Coverage** | **90.0%** | **96.55%** |

---

## 🛡️ Static Analysis & Linting

All builds enforce zero warnings:
- **Spotless (Google Java Format):** 2 spaces indentation, Unix LF line endings.
- **Checkstyle:** Zero style errors.
- **PMD:** Zero code smells or complexity violations.
- **SpotBugs:** Zero bug patterns detected.
- **ErrorProne:** Zero compiler error-prone warnings.
- **Platform Boundaries (`verifyPlatformBoundaries`):** Fails build if `org.bukkit` or `io.papermc` are imported in domain modules.
