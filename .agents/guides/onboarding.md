# Developer & AI Agent Onboarding Guide

Welcome to the **GearProgress** development ecosystem. This guide ensures that new engineers and AI models operate with the same high-velocity, high-standard practices from minute zero.

---

## 🧭 Repository Mental Model

GearProgress is structured around three foundational pillars:
1. **Hexagonal Modularity:** Domain logic lives exclusively in `gearprogress-core` and `gearprogress-application`. Platform modules (`:platforms:*`) act purely as runtime adapters.
2. **Golden Packaging Invariant:** Every release produces exactly two production distribution JARs (`paper-purpur-folia` and `spigot-bukkit`).
3. **Deterministic Persistence:** All database write operations are queued and serialized on a single background thread using SQLite in Write-Ahead Logging (`WAL`) mode.

---

## ⚡ First-Time Setup Checklist

1. **Verify JDK Version:** Ensure Java 25 is active:
   ```bash
   java -version
   ```
2. **Execute Full Clean & Check:**
   ```bash
   ./gradlew clean check build
   ```
3. **Verify Git Hooks and Scripts:**
   ```bash
   ./scripts/validate-structure.sh
   ./scripts/validate-source-hygiene.sh
   ./scripts/test-version-scripts.sh
   ```

---

## 🚦 Golden Rules for AI Agents

1. **Never Assume Code is Correct:** Verify behavior through test executions.
2. **Never Suppress Warnings:** Zero `@SuppressWarnings` permitted across the entire codebase.
3. **Never Leave TODOs:** Every task must be implemented completely; no placeholders.
4. **Preserve Thread Safety:** Read queries must be lock-free; write mutations must route through `SingleWriterPersistenceExecutor`.
5. **Format with Spotless:** Run `./gradlew spotlessApply` before running verification gates.
