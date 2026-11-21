---
name: audit-codebase
description: Audit GearProgress architectural boundaries, concurrency, and quality metrics
---

# Audit Codebase Skill

Perform a comprehensive code review of the GearProgress repository:
1. **Hexagonal Boundaries:** Check that `gearprogress-core` and `gearprogress-application` contain zero imports of `org.bukkit`, `io.papermc`, or `net.minecraft`.
2. **Concurrency Safety:** Verify that database writes are strictly dispatched to `SingleWriterPersistenceExecutor`.
3. **Folia Compliance:** Verify that scheduled tasks utilize the scheduler abstraction rather than direct Bukkit scheduling.
4. **Source Cleanliness:** Check for uncommitted files, dead code, or disabled checks.
