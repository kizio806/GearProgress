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
# config/equipment.yml
armor:
  diamond:
    unlock:
      type: timer
      after: 3d
    restrictions:
      equip: true
      use: true
  netherite:
    unlock:
      type: timer
      after: 14d
    restrictions:
      craft: true
      equip: true
      use: true
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
| `/gearprogress info [category] [tier]` | `gearprogress.info` | Display current tier unlock status and timer. |
| `/gearprogress unlock <category> <tier>` | `gearprogress.admin` | Force-unlock a tier ahead of schedule. |
| `/gearprogress lock <category> <tier>` | `gearprogress.admin` | Force-lock a tier. |
| `/gearprogress reload` | `gearprogress.admin` | Reload configuration files without restarting. |

---

## PlaceholderAPI Integration

| Placeholder | Example Output | Description |
|---|---|---|
| `%gearprogress_unlocked_<category>_<tier>%` | `true` / `false` | Whether the specified tier is unlocked. |
| `%gearprogress_time_remaining_<category>_<tier>%` | `2d 14h 32m` | Human-readable time remaining until unlock. |
| `%gearprogress_state_<category>_<tier>%` | `LOCKED` / `UNLOCKED` | Current status enum string. |
