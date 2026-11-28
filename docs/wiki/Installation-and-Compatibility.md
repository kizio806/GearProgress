# Installation & Platform Compatibility

This document provides detailed setup instructions and server platform compatibility guidelines for **GearProgress**.

---

## 🗺️ Compatibility Matrix

GearProgress supports modern Minecraft servers running **26.1.x, 26.2, and 26.3**:

| Server Software | Distribution Artifact | Scheduling Integration | Adventure Engine | Status |
|---|---|---|---|:---:|
| **Paper** | `...-paper-purpur-folia.jar` | Asynchronous (`getAsyncScheduler`) | Native Paper Adventure | ✅ Supported |
| **Purpur** | `...-paper-purpur-folia.jar` | Asynchronous (`getAsyncScheduler`) | Native Paper Adventure | ✅ Supported |
| **Folia** | `...-paper-purpur-folia.jar` | Regional (`getGlobalRegionScheduler`) | Native Paper Adventure | ✅ Supported |
| **Spigot** | `...-spigot-bukkit.jar` | Classic (`BukkitScheduler`) | Relocated `BukkitAudiences` | ✅ Supported |
| **CraftBukkit** | `...-spigot-bukkit.jar` | Classic (`BukkitScheduler`) | Relocated `BukkitAudiences` | ✅ Supported |

---

## ⚡ Folia Concurrency Architecture

Folia removes the single-threaded server tick loop in favor of independent, concurrent regional tick loops. GearProgress was engineered specifically to adhere to Folia's thread-safety contracts:

1. **`folia-supported: true`:** Explicitly declared in `plugin.yml` of the Paper distribution.
2. **Lock-Free Restriction Evaluation:** During player inventory clicks and attacks, event listeners query `RestrictionQueryService` using concurrent in-memory state with zero locks or blocking I/O on the regional tick thread.
3. **Region Scheduling:** Server-wide background timer checks use `GlobalRegionScheduler`.
4. **Single-Writer Database Serialization:** SQLite operations are dispatched to an asynchronous background worker (`SingleWriterPersistenceExecutor`), completely avoiding database concurrency conflicts across Folia threads.

---

## 🚀 Installation & First Boot

1. Download the JAR matching your server software from [GitHub Releases](https://github.com/kizio806/GearProgress/releases).
2. Place the JAR file into your server's `plugins/` directory.
3. Ensure your server runtime is running **Java 25** or higher (`java -version`).
4. Start your server. GearProgress will extract its default configurations into `plugins/GearProgress/`.
5. Customize your progression schedule in `equipment.yml` and `timers.yml`.
6. Run `/gearprogress reload` to apply your changes live.
