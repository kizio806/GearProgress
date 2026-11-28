# Compatibility Matrix Context

This document summarizes the server platform and version support matrix for **GearProgress**.

---

## 🗺️ Supported Platform Matrix

| Server Platform | Platform Family | Minecraft Compatibility | Scheduling Integration | Adventure Engine |
|---|---|---|---|---|
| **Paper** | Paper | 1.21 – 1.21.11 | `AsyncScheduler` | Native Adventure MiniMessage |
| **Purpur** | Paper | 1.21 – 1.21.11 | `AsyncScheduler` | Native Adventure MiniMessage |
| **Folia** | Paper | 1.21 – 1.21.11 | `GlobalRegionScheduler` | Native Adventure MiniMessage |
| **Spigot** | Spigot | 1.21 – 1.21.11 | `BukkitScheduler` | Relocated `BukkitAudiences` |
| **CraftBukkit** | Spigot | 1.21 – 1.21.11 | `BukkitScheduler` | Relocated `BukkitAudiences` |

---

## ☕ Runtime Requirements

- **Server JVM Runtime:** **Java 21 (LTS)** minimum.
- **Compilation Target:** Java 21 bytecode across all modules (`gearprogress-core`, `gearprogress-application`, `:adapters:*`, `:platforms:*`).
- **Golden Packaging Invariant:** Exactly two production distribution JARs:
  - `gearprogress-<base>-<range>-paper-purpur-folia.jar`
  - `gearprogress-<base>-<range>-spigot-bukkit.jar`
