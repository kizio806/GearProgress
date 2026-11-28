# Modrinth Project & Publishing Guide

This document contains the official description markdown and release metadata for GearProgress on [Modrinth](https://modrinth.com/plugin/gearprogress).

---

## 📋 Modrinth Project Description

*(Copy the markdown below into the Modrinth project description editor)*

```markdown
# ⚔️ GearProgress

**GearProgress** is a progressive equipment gating engine for modern Minecraft servers. Built for **Paper**, **Purpur**, **Folia**, and **Spigot** servers running Minecraft **26.1.x, 26.2, and 26.3**!

Pace your SMP season, RPG progression, or competitive server by unlocking armor, weapons, tools, shields, and elytra automatically over time or via admin commands.

---

## ✨ Features

- 🛡️ **Comprehensive Equipment Progression:** Support for Armor, Tools, Weapons, Shields, and Elytra.
- 🕒 **Flexible Unlock Methods:** Gate equipment with countdown timers (`14d`, `2w`, `3h30m`), dates (ISO-8601), `always`, `never`, or admin commands.
- 🔒 **Granular Action Restrictions:** Independently toggle restrictions for crafting, equipping, using/attacking, picking up, or moving items in inventories.
- 🔮 **Enchantment Pacing:** Permit wearing gear early while delaying powerful enchantments until later in the season.
- ⚡ **Native Folia Support:** Built from the ground up for Folia's regional multithreading with zero race conditions.
- 💾 **SQLite WAL Persistence:** Asynchronous single-writer database prevents server lag and lock contention.
- 📊 **PlaceholderAPI & Interactive GUI:** Exposes rich countdown placeholders and an interactive in-game overview menu (`/gearprogress gui`).
- 🎨 **Adventure MiniMessage:** Beautiful out-of-the-box formatting in English and Polish.

---

## 📦 Which JAR to Download?

| Distribution File | Server Software | Minecraft Version |
|---|---|:---:|
| `gearprogress-<version>-paper-purpur-folia.jar` | **Paper**, **Purpur**, **Folia** | **26.1.x, 26.2, 26.3** |
| `gearprogress-<version>-spigot-bukkit.jar` | **Spigot**, **CraftBukkit** | **26.1.x, 26.2, 26.3** |

> Install **only one** matching JAR in your server's `plugins/` folder! Requires **Java 25**.
```
