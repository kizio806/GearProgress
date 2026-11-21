# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- **Unified Global Versioning Standard**: Migrated versioning architecture to synchronized Semantic Versioning with Minecraft compatibility build metadata (`1.0.0+mc1.21.x`).
- **Deterministic Release Artifact Naming**: Standardized distribution JAR outputs to `gearprogress-1.0.0-mc1.21.x-paper-purpur-folia.jar` and `gearprogress-1.0.0-mc1.21.x-spigot-bukkit.jar`.

## [1.0.0] - 2026-11-20

### Added
- Initial release of GearProgress for Paper 1.21.11+.
- Hexagonal architecture with pure Java 21 `core` and `application` modules.
- Multi-tier equipment progression for Armor, Tools, Weapons, Shield, and Elytra.
- Flexible unlock conditions: `always`, `never`, `timer` (relative), `date` (ISO-8601), and `command`.
- Configurable per-tier restriction policies: craft, use, equip, pickup, inventory move.
- Configurable per-tier enchantment gating (`allow-enchantments`).
- High-performance SQLite persistence with single-writer async executor and WAL mode.
- Full Brigadier command tree (`/gearprogress`).
- PlaceholderAPI expansion integration.
- Adventure MiniMessage internationalization with bundled English and Polish catalogs.
