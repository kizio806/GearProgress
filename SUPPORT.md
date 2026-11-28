# Support Policy

Thank you for using **GearProgress**! This document provides guidelines on how to get assistance, report bugs, ask configuration questions, and engage with the maintainers.

---

## 📖 Getting Help

Before opening an issue, please consult the official documentation:
- [Installation Guide](docs/installation.md) — Platform requirements, distribution selection, and first setup.
- [Configuration Guide](docs/wiki/Configuration.md) — Comprehensive YAML configuration options for `config.yml`, `equipment.yml`, and `timers.yml`.
- [Commands & Permissions](docs/wiki/Commands-and-Permissions.md) — Full CLI command reference, syntax, and permission nodes.
- [Architecture Guide](docs/architecture.md) — Hexagonal design, ports, single-writer SQLite persistence, and Folia thread safety.
- [Troubleshooting](docs/troubleshooting.md) — Solutions for common issues (SQLite locking, bypass permissions, PlaceholderAPI integration).
- [Wiki Documentation](docs/wiki/Home.md) — Comprehensive progression presets and deployment examples.

---

## 💬 Communication Channels

### 1. GitHub Issues
GitHub Issues are reserved for:
- **Bug Reports:** Restriction bypasses, inventory event anomalies, parser failures, or database synchronization errors.
- **Feature Requests:** New unlock methods, restriction types, sound/particle feedback options, or command enhancements.
- **Platform Compatibility Reports:** Issues observed with specific Paper, Purpur, Folia, or Spigot builds.

Please use the appropriate [Issue Template](.github/ISSUE_TEMPLATE) when submitting tickets and provide server logs, plugin versions, and configuration files.

### 2. Security Vulnerabilities
If you discover a security vulnerability, inventory duplication vector, or denial-of-service hazard, please review our [Security Policy](SECURITY.md) and report it privately via [GitHub Security Advisories](https://github.com/kizio806/GearProgress/security) rather than filing a public issue.

### 3. Community Discussions
For configuration help, SMP progression design ideas, or general discussion, participate in [GitHub Discussions](https://github.com/kizio806/GearProgress/discussions).

---

## 🗺️ Supported Versions Matrix

Active bugfixes and patch releases are supported across our multi-version branches:

| Minecraft Line | Supported Minecraft Versions | Server Platforms | Git Branch | Java Baseline |
|---|---|---|---|---|
| **Minecraft 1.21.x** | 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.11 | Paper, Purpur, Folia, Spigot, CraftBukkit | [`main`](https://github.com/kizio806/GearProgress) | Java 21 |
| **Minecraft 1.21.x (LTS)** | 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.11 | Paper, Purpur, Folia, Spigot, CraftBukkit | [`support/1.21.x`](https://github.com/kizio806/GearProgress/tree/support/1.21.x) | Java 21 |
