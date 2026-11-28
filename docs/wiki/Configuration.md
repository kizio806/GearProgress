# Configuration Guide

GearProgress provides granular, data-driven control over equipment gating through human-readable YAML configuration files.

---

## Configuration File Structure

Upon startup, GearProgress automatically creates the following files inside `plugins/GearProgress/`:

- **`config.yml`**: General operational parameters, default locale, and background evaluation intervals.
- **`equipment.yml`**: Equipment tier definitions, unlock mechanisms, restriction flags, and enchantment policies.
- **`timers.yml`**: Server epoch timestamps and timer runtime overrides.
- **`storage.yml`**: Persistence database settings (SQLite).
- **`locales/*.yml`**: Adventure MiniMessage message catalogs (`en-US.yml`, `pl-PL.yml`).

---

## Unlock Mechanisms

Every tier in `equipment.yml` defines an `unlock` block supporting five distinct strategies:

### 1. Timer (`type: timer`)
Unlocks automatically after a duration relative to the configured `server-start-epoch`:
```yaml
armor:
  diamond:
    unlock:
      type: timer
      after: 3d    # Supports d (days), w (weeks), h (hours), m (minutes), s (seconds)
```

### 2. Date (`type: date`)
Unlocks automatically at an exact ISO-8601 UTC timestamp:
```yaml
weapons:
  mace:
    unlock:
      type: date
      at: "2026-12-01T18:00:00Z"
```

### 3. Always (`type: always`)
Available immediately upon server creation with zero restrictions:
```yaml
armor:
  leather:
    unlock:
      type: always
```

### 4. Never (`type: never`)
Permanently disabled and uncraftable/unusable unless modified in configuration:
```yaml
weapons:
  trident:
    unlock:
      type: never
```

### 5. Command (`type: command`)
Locked by default until an administrator or automation script executes `/gearprogress unlock <category> <tier>`:
```yaml
elytra:
  wings:
    unlock:
      type: command
```

---

## Granular Restriction Flags

You can specify exactly which interactions are intercepted while a tier is locked:

```yaml
restrictions:
  craft: true           # Blocks vanilla crafting and smithing recipes
  equip: true           # Blocks equipping via right-click, shift-click, drag, hotbar swap, dispenser
  use: true             # Blocks swinging weapons, attacking, shooting bows, and breaking blocks
  pickup: true          # Blocks vacuuming up dropped items from the ground
  inventory-move: true  # Blocks moving items between containers and player slots
```

---

## Enchantment Gating

Tiers can permit wearing and using items while restricting powerful enchantments until a later phase:

```yaml
armor:
  diamond:
    unlock:
      type: timer
      after: 3d
    enchantments:
      allow-enchanted: false    # Blocks enchanting tables, anvils, and wearing pre-enchanted gear
```

---

## `config.yml` Reference

```yaml
schema-version: 1
default-locale: en-US
check-interval-seconds: 5

notifications:
  chat: true
  actionbar: true
```

---

## `timers.yml` Reference

```yaml
schema-version: 1
server-start-epoch: "2026-11-20T20:00:00Z"
```
The `server-start-epoch` serves as the baseline timestamp from which all relative `after` durations are calculated.
