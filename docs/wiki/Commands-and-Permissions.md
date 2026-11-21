# Commands & Permissions

GearProgress commands are exposed via Paper's native Brigadier engine (`GearProgress-paper.jar`) or Bukkit command routing (`GearProgress-spigot.jar`).

---

## Commands

### 1. Information Command
Displays the current status, restrictions, and time remaining for a gear tier:
```bash
/gearprogress info [category] [tier]
```
- **Permission:** `gearprogress.info` (granted to players by default).

### 2. Manual Unlock Command
Immediately unlocks a tier ahead of schedule:
```bash
/gearprogress unlock <category> <tier>
```
- **Permission:** `gearprogress.admin`

### 3. Manual Lock Command
Immediately locks an unlocked tier:
```bash
/gearprogress lock <category> <tier>
```
- **Permission:** `gearprogress.admin`

### 4. Reload Command
Reloads YAML configuration and locale files from disk:
```bash
/gearprogress reload
```
- **Permission:** `gearprogress.admin`

---

## Permission Nodes Reference

| Node | Default | Description |
|---|---|---|
| `gearprogress.info` | `true` | Allows viewing gear unlock times and status. |
| `gearprogress.admin` | `op` | Full administrative control: reload, lock, unlock. |
| `gearprogress.bypass` | `false` | Bypasses all equipment restrictions for testing/staff. |
