# GearProgress Installation & Setup Guide

This guide provides step-by-step instructions for installing and configuring **GearProgress** on modern Minecraft servers.

---

## 📋 System Requirements

- **Minecraft Server:** Paper, Purpur, Folia, or Spigot **26.1.x, 26.2, or 26.3**.
- **Java Runtime:** **Java 25** minimum (`java -version`).
- **Permissions Plugin:** LuckPerms or any standard Bukkit permissions manager.
- *(Optional)* [PlaceholderAPI](https://placeholderapi.com/) for scoreboard, tab list, and menu placeholders.

---

## 📦 Distribution Selection

GearProgress ships with two targeted distribution artifacts per release:

| Target Platform | Distribution JAR | Description |
|---|---|---|
| **Paper / Purpur / Folia** | `gearprogress-<base>-<range>-paper-purpur-folia.jar` | Includes native Brigadier command trees, Folia regionized schedulers, and native Adventure MiniMessage. |
| **Spigot / CraftBukkit** | `gearprogress-<base>-<range>-spigot-bukkit.jar` | Includes standard Bukkit command routing, tab completion, and relocated Adventure BukkitAudiences. |

> [!IMPORTANT]
> Install **only one** matching JAR in your server's `plugins/` directory. Do not install both.

---

## 🚀 Installation Steps

### 1. Download the Artifact
Download the latest production JAR corresponding to your server platform from [GitHub Releases](https://github.com/kizio806/GearProgress/releases).

### 2. Install to Plugins Directory
Copy the downloaded JAR file into your server's `plugins/` directory:
```bash
cp gearprogress-1.1.0-mc26.x-paper-purpur-folia.jar /path/to/server/plugins/
```

### 3. Start the Server
Start your server. GearProgress will initialize, establish its SQLite persistence database in WAL mode, and generate default configuration files:
```text
plugins/GearProgress/
├── config.yml              # Server options and evaluation intervals
├── equipment.yml           # Equipment tier definitions and restrictions
├── timers.yml              # Server season start epoch timestamp
├── storage.yml             # SQLite database configuration
├── gearprogress.db         # Persistent SQLite database
├── gearprogress.db-wal     # SQLite Write-Ahead Log
├── gearprogress.db-shm     # SQLite shared memory file
└── locales/
    ├── en-US.yml           # English message catalog
    └── pl-PL.yml           # Polish message catalog
```

### 4. Configure Progression Timeline
Open `plugins/GearProgress/timers.yml` and set your server's season start epoch:
```yaml
schema-version: 1
server-start-epoch: "2026-11-20T20:00:00Z"
```
Then configure tier delays in `plugins/GearProgress/equipment.yml`:
```yaml
armor:
  diamond:
    unlock:
      type: timer
      after: 3d
    restrictions:
      equip: true
      use: true
```

### 5. Reload Configuration
Apply your configuration changes live without restarting the server:
```bash
/gearprogress reload
```

---

## 🔒 Permissions Configuration

Grant the following permissions using your permissions plugin (e.g. LuckPerms):

```bash
# Allow all players to view equipment progression status and open the GUI
/lp group default permission set gearprogress.info true
/lp group default permission set gearprogress.gui true

# Grant administrators full control
/lp group admin permission set gearprogress.admin true

# (Optional) Grant staff restriction bypass
/lp group staff permission set gearprogress.bypass true
```
