# GearProgress Wiki

Welcome to the official **GearProgress** documentation wiki. GearProgress is an enterprise-grade Minecraft server plugin for progressive equipment unlocking, built with hexagonal architecture and native support for Paper, Purpur, Folia, and Spigot.

---

## Documentation Index

- [Architecture Overview](../architecture.md) — Ports & Adapters, thread safety, and data flow.
- [Compatibility Matrix](../compatibility-matrix.md) — Supported platforms and Minecraft versions.
- [Installation Guide](../installation.md) — Step-by-step setup and database configuration.
- [Development Guide](../development.md) — Building from source, coding standards, and running checks.
- [Troubleshooting](../troubleshooting.md) — Diagnosing common runtime and configuration issues.

---

## Progression Presets & Configuration Examples

### 1. Hardcore SMP 14-Day Progression
A phased rollout where high-tier equipment unlocks gradually:
- **Days 1–3:** Leather, Chainmail, and Iron unrestricted; Diamond craft allowed, but Diamond armor equip/use locked until Day 3.
- **Days 3–7:** Diamond equipment fully unlocked; Netherite locked until Day 14.
- **Day 14:** Netherite crafting and equipment unlocked for the server.

```yaml
# plugins/GearProgress/equipment.yml
armor:
  diamond:
    unlock:
      type: timer
      after: 3d
    restrictions:
      equip: true
      use: true
    enchantments:
      allow-enchanted: false
  netherite:
    unlock:
      type: timer
      after: 14d
    restrictions:
      craft: true
      equip: true
      use: true
      pickup: true
      inventory-move: true
```

### 2. Event-Driven Unlock (Command / Quest Triggered)
In this model, tiers remain locked until an admin or automated script executes the unlock command:
```yaml
weapons:
  mace:
    unlock:
      type: command
    restrictions:
      use: true
      craft: true
```
Unlocking is performed via:
```bash
/gearprogress unlock weapons mace
```

---

## Commands & Permissions

| Command | Permission | Description |
|---|---|---|
| `/gearprogress info [cat] [tier]` | `gearprogress.info` | Display current tier unlock status and timer. |
| `/gearprogress gui` | `gearprogress.gui` | Open interactive visual progression chest GUI. |
| `/gearprogress unlock <cat> <tier>` | `gearprogress.admin` | Force-unlock a tier ahead of schedule. |
| `/gearprogress lock <cat> <tier>` | `gearprogress.admin` | Force-lock a tier. |
| `/gearprogress reset <cat> <tier>` | `gearprogress.admin` | Reset a tier to initial configuration. |
| `/gearprogress timer pause <cat> <tier>` | `gearprogress.admin` | Pause a countdown timer. |
| `/gearprogress timer resume <cat> <tier>` | `gearprogress.admin` | Resume a paused timer. |
| `/gearprogress timer add <cat> <tier> <dur>` | `gearprogress.admin` | Add duration to a timer (e.g. `2d`). |
| `/gearprogress timer set <cat> <tier> <dur>` | `gearprogress.admin` | Set exact remaining duration. |
| `/gearprogress reload` | `gearprogress.admin` | Reload configuration files without restarting. |

---

## PlaceholderAPI Integration

| Placeholder | Example Output | Description |
|---|---|---|
| `%gearprogress_current_stage_<category>%` | `diamond` | Currently unlocked tier in category. |
| `%gearprogress_next_unlock_<category>%` | `netherite` | Next upcoming tier to be unlocked. |
| `%gearprogress_time_remaining_<cat>_<tier>%` | `3d 14h 22m` | Formatted countdown until unlock. |
| `%gearprogress_is_unlocked_<cat>_<tier>%` | `true` / `false` | Boolean unlock status. |
| `%gearprogress_status_<cat>_<tier>%` | `UNLOCKED`, `LOCKED` | Current progression state enum. |
