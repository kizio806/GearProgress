# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

- Ported the active `main` line to Minecraft 26.1.x, 26.2, and 26.3.
- Raised the active runtime and compilation baseline to Java 25.
- Preserved the 1.21.x line on `support/mc-1.21.x`.
- Updated release metadata, CI matrices, documentation, and compatibility validation.

## [1.0.0+mc1.21.x] - 2026-11-28

### Added
- Comprehensive test suite expansion achieving **96.55% instruction coverage** and **94.88% line coverage** across the entire repository.
- Full interactive and behavioral unit tests for Paper Brigadier command routing and Spigot command handling (`GearProgressMainCommandTest`, `SpigotMainCommandTest`).
- Edge-case testing for duration parsing, YAML configuration fallbacks, progression service lifecycle, timer clamping, and MockBukkit event interception.
- Full documentation suite aligned with Kizio Labs reference standards: `ROADMAP.md`, `ARCHITECTURE.md`, `DECISIONS.md`, `FAQ.md`, `SUPPORT.md`, `CODE_OF_CONDUCT.md`.
- Automated SonarCloud and Codecov exclusion filters for generated sources, build artifacts, test utilities, and distributions.

### Fixed
- **RelativeDurationParser Alternation Defect:** Resolved regex pattern alternation order where short units (`s`, `m`, `h`, `d`, `w`) preceded full word units (`seconds`, `minutes`, `hours`, `days`, `weeks`), causing premature substring matching and unexpected parsing exceptions.
- **PMD Ruleset Configuration:** Removed stale package path references in `config/pmd/pmd.xml`.
- **Spotless Formatting:** Enforced Google Java Format across all newly added and refactored test suites.

### Changed
- **Unified Global Versioning Standard**: Synchronized Semantic Versioning with Minecraft compatibility build metadata (`1.0.0+mc1.21.x`).
- **Deterministic Release Artifact Naming**: Standardized distribution JAR outputs to `gearprogress-1.0.0-mc1.21.x-paper-purpur-folia.jar` and `gearprogress-1.0.0-mc1.21.x-spigot-bukkit.jar`.
- **Quality Gate Hardening:** Increased aggregated JaCoCo minimum instruction coverage gate to **90%** (`jacocoRootCoverageVerification`), with subprojects enforcing up to **95%** on `gearprogress-core`.

- Initial release of GearProgress for Paper, Purpur, Folia, Spigot, and CraftBukkit on Minecraft 1.21.x.
- Hexagonal architecture with pure Java 21 `core` and `application` modules.
- Multi-tier equipment progression for Armor, Tools, Weapons, Shield, and Elytra.
- Flexible unlock conditions: `always`, `never`, `timer` (relative), `date` (ISO-8601), and `command`.
- Configurable per-tier restriction policies: craft, use, equip, pickup, inventory move.
- Configurable per-tier enchantment gating (`allow-enchantments`).
- High-performance SQLite persistence with single-writer async executor and WAL mode.
- Full Brigadier command tree on Paper/Folia (`/gearprogress`).
- Dedicated command executor and tab completer on Spigot.
- PlaceholderAPI expansion integration (`%gearprogress_*%`).
- Interactive GUI menu overview (`/gearprogress gui`).
- Adventure MiniMessage internationalization with bundled English (`en-US`) and Polish (`pl-PL`) catalogs.
