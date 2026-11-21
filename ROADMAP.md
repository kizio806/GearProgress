# Project Roadmap — GearProgress

This roadmap outlines key milestones, active development tracks, and future vision for **GearProgress**.

---

## Current Release Track (v1.0.x — Foundation & Production Readiness)
- [x] **Hexagonal Architecture:** Decoupled pure Java domain (`gearprogress-core`) and orchestration ports (`gearprogress-application`) with 0 Minecraft dependencies.
- [x] **Dual Distribution Packaging:** Automated distribution artifacts for Paper/Purpur/Folia and Spigot/CraftBukkit.
- [x] **Single-Writer SQLite Engine:** Thread-safe asynchronous persistence using WAL mode and dedicated writer executor.
- [x] **Folia Concurrency Safety:** Native support for Folia's regionized tick scheduler without thread-pool starvation.
- [x] **Comprehensive Quality Gates:** 100% pass rate across Checkstyle, PMD, SpotBugs, ArchUnit, and JaCoCo coverage ($\ge 90\%$).

---

## Upcoming Milestones

### Phase 2: Distributed Persistence & Multi-Server Sync (v1.1.0)
- [ ] **Remote Storage Adapters:** Implement `adapters:storage-sql` supporting MySQL 8.0+, MariaDB, and PostgreSQL connection pooling (HikariCP).
- [ ] **Redis Invalidation Bus:** Real-time cross-server tier unlock synchronization for multi-proxy SMP networks.
- [ ] **Zero-Downtime Data Migration CLI:** Automated schema and state migrator tool between SQLite and remote databases.

### Phase 3: Interactive Visual Interfaces & Admin Experience (v1.2.0)
- [ ] **In-Game GUI Progress Browser:** Modern Adventure-backed interactive inventory GUI displaying unlocked and upcoming gear tiers.
- [ ] **Live Boss & Advancement Triggers:** Progress stages dynamically unlockable upon defeating custom bosses or completing advancements.
- [ ] **Config Hot-Reload Validation:** Comprehensive dry-run validator for YAML configuration edits before committing to live runtime.

### Phase 4: Observability & Developer SDK (v1.3.0)
- [ ] **OpenTelemetry Metrics:** Expose Prometheus/OpenTelemetry metrics for persistence latency, query cache hits, and restriction checks.
- [ ] **Public Event Bus API:** Rich Java lifecycle events (`GearTierUnlockEvent`, `GearRestrictionCheckEvent`) for third-party plugin integration.

---

## Long-Term Vision
- **Enterprise-Grade Progression Engine:** The industry-standard modular progression and gating engine for modern Minecraft SMP and RPG server networks.
