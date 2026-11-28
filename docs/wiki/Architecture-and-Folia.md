# Architecture & Folia Thread Safety

GearProgress is built from the ground up using **Hexagonal Architecture** (Ports and Adapters) to isolate business rules from server-specific APIs and concurrency quirks.

---

## Modularity & Ports

1. **`gearprogress-core` (Pure Java 25 Domain)**:
   - Contains immutable models: `EquipmentCategory`, `EquipmentTier`, `TierKey`, `UnlockCondition`, `RestrictionPolicy`.
   - Pure mathematical duration engine: `RelativeDurationParser`.
   - Has zero dependencies on Bukkit, Paper, or Minecraft.
2. **`gearprogress-application` (Orchestration & Use Cases)**:
   - Orchestrates progression checks and configuration parsing.
   - Defines SPI interfaces: `ProgressionRepository`, `TaskSchedulerPort`, `EventPublisherPort`.
3. **`adapters:storage-sqlite` (Single-Writer Persistence)**:
   - Serializes all database writes via `SingleWriterPersistenceExecutor`.
   - Enables WAL mode (`PRAGMA journal_mode=WAL;`) for concurrent non-blocking reads.
4. **`platforms:bukkit` & `platforms:paper`**:
   - Implements high-performance event listeners and scheduler bindings.

---

## Folia Regional Thread Safety

Folia executes distinct chunk regions across independent threads rather than using a single global tick thread.

### Concurrency Guarantees in GearProgress:
- **Zero Cross-Region Access:** Event listeners only inspect items present in the triggering player's context.
- **Asynchronous Task Delegation:** Tasks scheduled via `TaskSchedulerPort` bind directly to the region owner or execute on the global async executor.
- **Single-Writer DB Serialization:** Because SQLite cannot handle concurrent write operations across multiple threads, writes are queued asynchronously to a dedicated daemon thread.
- **Zero Blocking I/O on Tick Loops:** File reads and SQL transactions never execute synchronously during player actions.
