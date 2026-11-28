# AI Agent Role Prompts & Workflows

Reference task prompts and operational patterns for AI models collaborating on **GearProgress**.

---

## 🛠️ Prompt: Architectural Boundary Audit

> "Act as the Principal Software Architect for GearProgress. Review the dependencies and import statements across all modules. Verify that `gearprogress-core` and `gearprogress-application` contain zero references to `org.bukkit`, `io.papermc`, or `net.minecraft`. Run `./gradlew verifyPlatformBoundaries` and `./scripts/validate-structure.sh` to confirm structural compliance."

---

## 🐛 Prompt: Bug Reproduction & Fix Protocol

> "Act as a Senior Minecraft Plugin Engineer. We have a bug report stating that [describe bug].
> 1. Formulate a hypothesis and write a failing unit test in the appropriate test suite reproducing the issue.
> 2. Implement the minimal clean fix adhering to Google Java Format and zero-suppression rules.
> 3. Verify the fix passes the reproduction test.
> 4. Run `./gradlew check` to ensure no regressions."

---

## 🔒 Prompt: Security & Concurrency Audit

> "Act as the Security & Concurrency Auditor. Inspect all event listeners in `platforms/bukkit` and persistence paths in `adapters/storage-sqlite`. Ensure all SQLite queries are parameterized against injection, that no disk I/O occurs on hot server tick threads, and that `SingleWriterPersistenceExecutor` drains pending writes cleanly upon shutdown."

---

## 🚀 Prompt: Release Packaging Verification

> "Act as the DevOps Engineer. Perform a complete release readiness dry-run using `./scripts/release.sh --dry-run`. Verify that SemVer build metadata is compliant (`1.0.0+mc1.21.x`), that both distribution artifacts (`paper-purpur-folia` and `spigot-bukkit`) are generated, and that SHA256 checksums are verified."
