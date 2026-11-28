# Installation & Platform Setup

This guide details how to install and configure **GearProgress** on modern Minecraft servers.

---

## System Requirements

- **Minecraft Server:** Paper, Purpur, Folia, or Spigot 26.1.x, 26.2, or 26.3.
- **Java Runtime:** Java 25 minimum (`java -version`).
- **Permissions Plugin:** Any standard Bukkit/Vault permissions manager (LuckPerms recommended).
- *(Optional)* [PlaceholderAPI](https://placeholderapi.com/) for scoreboard, tab, and menu placeholders.

---

## Distribution Selection

GearProgress ships with two targeted distribution artifacts:

1. **`gearprogress-<base>-<range>-paper-purpur-folia.jar`**:
   - For **Paper**, **Purpur**, and **Folia** servers.
   - Leverages native Brigadier command trees, asynchronous chunk loading, and Folia's regionized tick schedulers.
2. **`gearprogress-<base>-<range>-spigot-bukkit.jar`**:
   - For **Spigot** and **CraftBukkit** servers.
   - Includes standard Bukkit commands and relocated Adventure components.

> [!IMPORTANT]
> Install **only one** distribution JAR matching your server software.

---

## Installation Steps

1. Download the appropriate distribution JAR from [GitHub Releases](https://github.com/kizio806/GearProgress/releases).
2. Place the `.jar` into your server's `plugins/` directory.
3. Start or restart your Minecraft server.
4. GearProgress will generate its configuration tree:
   ```text
   plugins/GearProgress/
   ├── config.yml           # Global plugin settings
   ├── equipment.yml        # Equipment categories, tiers, and restrictions
   ├── gearprogress.db      # SQLite persistence database (WAL mode)
   └── locales/             # en-US.yml and pl-PL.yml language catalogs
   ```
5. Edit `equipment.yml` to define your SMP unlock timeline.
6. Run `/gearprogress reload` to apply configuration changes without downtime.
