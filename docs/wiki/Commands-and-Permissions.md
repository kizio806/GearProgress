# Commands & Permissions

GearProgress commands are exposed via Paper's native Brigadier engine (`gearprogress-<base>-<range>-paper-purpur-folia.jar`) or Bukkit command routing (`gearprogress-<base>-<range>-spigot-bukkit.jar`).

---

## Commands

### 1. Information Command
Displays the current progression status, active restriction flags, and remaining countdown for a gear tier:
```bash
/gearprogress info [category] [tier]
```
- **Permission:** `gearprogress.info` (granted to players by default).
- **Arguments:**
  - `[category]` *(optional)*: `armor`, `tools`, `weapons`, `shield`, `elytra`.
  - `[tier]` *(optional)*: Tier identifier (e.g. `diamond`, `netherite`).

### 2. Interactive GUI Overview
Opens an interactive inventory view displaying all equipment categories, current unlock stages, and countdown timers:
```bash
/gearprogress gui
```
- **Permission:** `gearprogress.gui` (granted to players by default).

### 3. Manual Unlock Command
Immediately unlocks a tier ahead of schedule:
```bash
/gearprogress unlock <category> <tier>
```
- **Permission:** `gearprogress.admin`

### 4. Manual Lock Command
Immediately locks an unlocked tier:
```bash
/gearprogress lock <category> <tier>
```
- **Permission:** `gearprogress.admin`

### 5. Reset Command
Resets a tier to its initial configuration state:
```bash
/gearprogress reset <category> <tier>
```
- **Permission:** `gearprogress.admin`

### 6. Timer Management Commands
Allows server administrators to inspect and manipulate active countdown timers in real time:
```bash
/gearprogress timer pause <category> <tier>
/gearprogress timer resume <category> <tier>
/gearprogress timer add <category> <tier> <duration>
/gearprogress timer set <category> <tier> <duration>
```
- **Permission:** `gearprogress.admin`
- **Duration syntax:** `14d`, `2w`, `3h30m`, `45s`, `10 minutes`.

### 7. Reload Command
Reloads YAML configuration files (`config.yml`, `equipment.yml`, `timers.yml`, `storage.yml`) and locale files from disk without restarting the server:
```bash
/gearprogress reload
```
- **Permission:** `gearprogress.admin`

---

## Permission Nodes Reference

| Node | Default | Description |
|---|---|---|
| `gearprogress.info` | `true` | Allows viewing gear unlock times and status via `/gearprogress info`. |
| `gearprogress.gui` | `true` | Allows opening the interactive progression chest GUI. |
| `gearprogress.admin` | `op` | Full administrative control: reload, lock, unlock, reset, and timer management. |
| `gearprogress.bypass` | `false` | Bypasses all equipment and enchantment restrictions for testing and staff. |
