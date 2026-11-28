# Compatibility Matrix Context

This document summarizes the server platform and version support matrix for **GearProgress**.

---

## 🗺️ Supported Platform Matrix

| Server Platform | Platform Family | Minecraft Compatibility | Scheduling Integration | Adventure Engine |
|---|---|---|---|---|
| **Paper** | Paper | 26.1, 26.1.1, 26.1.2, 26.2, 26.3 | `AsyncScheduler` | Native Adventure MiniMessage |
| **Purpur** | Paper | 26.1, 26.1.1, 26.1.2, 26.2, 26.3 | `AsyncScheduler` | Native Adventure MiniMessage |
| **Folia** | Paper | 26.1, 26.1.1, 26.1.2, 26.2, 26.3 | `GlobalRegionScheduler` | Native Adventure MiniMessage |
| **Spigot** | Spigot | 26.1, 26.1.1, 26.1.2, 26.2, 26.3 | `BukkitScheduler` | Relocated `BukkitAudiences` |
| **CraftBukkit** | Spigot | 26.1, 26.1.1, 26.1.2, 26.2, 26.3 | `BukkitScheduler` | Relocated `BukkitAudiences` |

---

## ☕ Runtime Requirements

- **Server JVM Runtime:** **Java 25** minimum for the active line.
- **Compilation Target:** Java 25 bytecode across all runtime modules (`gearprogress-core`, `gearprogress-application`, `:adapters:*`, `:platforms:*`).
- **Golden Packaging Invariant:** Exactly two production distribution JARs:
  - `gearprogress-<base>-<range>-paper-purpur-folia.jar`
  - `gearprogress-<base>-<range>-spigot-bukkit.jar`
