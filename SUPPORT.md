# Support Policy

Thank you for using **GearProgress**. This document provides guidelines on how to get assistance, report bugs, ask configuration questions, and engage with the maintainers.

---

## Getting Help

Before opening an issue, please consult the project documentation:
- [Installation Guide](docs/installation.md) — Server requirements, distribution JAR selection (Paper vs Spigot), and initial setup.
- [Compatibility Matrix](docs/compatibility-matrix.md) — Supported Minecraft versions (1.21.x), server platforms (Paper, Purpur, Folia, Spigot), and Java runtime requirements (Java 21).
- [Architecture Guide](docs/architecture.md) — Hexagonal modularity, persistence layer, Folia scheduler integration, and domain isolation.
- [Troubleshooting](docs/troubleshooting.md) — Common configuration errors, SQLite write lock resolutions, Folia threading notes, and event precedence.
- [Wiki Documentation](docs/wiki/Home.md) — In-depth administrator manual, YAML schema references, command permissions, and PlaceholderAPI placeholders.

---

## Communication Channels

### 1. GitHub Issues
GitHub Issues are reserved for:
- **Bug Reports:** Incorrect restriction blocking, SQLite persistence errors, event listener conflicts, or Folia task exceptions.
- **Feature Requests:** Additional unlock conditions, new equipment category support, or storage backend adapters.
- **Compatibility Reports:** Issues encountered on specific server forks or build numbers.

Please use the appropriate [Issue Template](.github/ISSUE_TEMPLATE) when submitting tickets.

### 2. Security Vulnerabilities
If you identify a security issue or critical flaw, please review our [Security Policy](SECURITY.md) and submit a report privately.

### 3. Community Discussions
For configuration showcases, SMP server setups, or general help, visit our [GitHub Discussions](https://github.com/kizio806/GearProgress/discussions).

---

## Supported Server Platforms

Active support and testing are guaranteed for:
- **Paper / Purpur 1.21.x:** Supported via `GearProgress-<version>-paper.jar` (Brigadier commands, native asynchronous events).
- **Folia 1.21.x:** Supported via `GearProgress-<version>-paper.jar` (Regionized multi-threaded tick scheduler).
- **Spigot / CraftBukkit 1.21.x:** Supported via `GearProgress-<version>-spigot.jar` (Bukkit scheduler, standard commands).
