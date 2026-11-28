# Dependency Management & Governance Guide

This document defines dependency architecture, version catalog conventions, and vulnerability scanning procedures for **GearProgress**.

---

## 📦 Version Catalogs (`gradle/libs.versions.toml`)

All dependencies, plugins, and toolchains are centrally declared in `gradle/libs.versions.toml`:

```toml
[versions]
adventure = "4.18.0"
sqlite = "3.47.1.0"
junit = "5.11.3"
mockito = "5.14.2"
...
```

### Guidelines for Adding or Updating Dependencies:
1. **Never Hardcode Coordinates:** Subprojects must declare dependencies via `libs.<name>`, never using hardcoded string literals.
2. **Minimize External Footprint:** Domain modules (`core`, `application`) must have zero framework dependencies.
3. **Relocation & Shadowing:** In `distributions:spigot`, external libraries (such as Adventure) must be cleanly relocated to avoid classpath collisions with other plugins.
4. **Vulnerability Scanning:** Automated scanners (OSV-Scanner, Dependabot, Renovate) run periodically. Any dependency with a high/critical CVE must be upgraded immediately.
