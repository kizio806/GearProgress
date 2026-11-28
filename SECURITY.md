# Security Policy

## Supported Versions

Security fixes and advisories are actively provided across all maintained support branches:

| Minecraft Line | Git Branch | Supported Server Platforms | Security Support Status |
|---|---|---|---|
| **Minecraft 1.21.x** | `main` | Paper, Purpur, Folia, Spigot, CraftBukkit 1.21–1.21.11 | ✅ Actively supported |
| **Minecraft 1.21.x** | `support/1.21.x` | Paper, Purpur, Folia, Spigot, CraftBukkit 1.21–1.21.11 | ✅ Actively supported |

---

## Reporting a Vulnerability

Do not open public GitHub issues for security vulnerabilities, exploit reports, or inventory dupe vectors.

Please report privately via GitHub Security Advisories or direct maintainer contact:

- **Repository Security Portal:** https://github.com/kizio806/GearProgress/security
- **Maintainer Contact:** `kizio.contact@gmail.com`

When reporting, please include:
- Affected plugin version (e.g. `1.0.0+mc1.21.x`)
- Distribution JAR used (`paper-purpur-folia` or `spigot-bukkit`)
- Server software and exact build (e.g. `Paper 1.21.4 build 120`, `Folia 1.21.4 build 28`)
- Detailed step-by-step reproduction instructions
- Expected vs actual behavior
- Impact assessment (e.g. item duplication, restriction bypass, denial of service, memory leak)

All vulnerability reports will be acknowledged within 48 hours, and a patch will be prepared and tested across all supported platforms prior to coordinated disclosure.

---

## Secret & Credential Management

- Never commit tokens, passwords, private keys, or credentials into source code, YAML configurations, or workflow files.
- Credentials and project identifiers must be managed exclusively through GitHub Actions Secrets or standard platform contexts.

### 1. Platform-Generated Token
- `GITHUB_TOKEN`: Ephemeral installation token provisioned automatically by GitHub Actions for each workflow execution. It is never manually configured. Used for PR labeling (`labeler.yml`), wiki synchronization (`wiki-sync.yml`), and publishing release tags (`release.yml`).

### 2. External Service Authentication Tokens (Sensitive Secrets)
- `MODRINTH_TOKEN`: Personal API token with version publishing permissions on Modrinth.
- `SONAR_TOKEN`: Authentication token for SonarCloud static analysis and code quality reporting.
- `CODECOV_TOKEN`: Repository upload token for Codecov test coverage reports.

### 3. Project Identifiers & Configuration Variables (Non-Token References)
- `MODRINTH_PROJECT_ID`: Unique project identifier (or slug) target on Modrinth.
