# Configuration Guide

GearProgress provides granular, data-driven control over equipment gating through YAML configuration files.

---

## Configuration Architecture

- **`config.yml`**: General operational parameters, storage engine, locale preferences, and check intervals.
- **`equipment.yml`**: Equipment tier definitions, unlock mechanisms, and restriction flags.
- **`locales/*.yml`**: Adventure MiniMessage message catalogs (`en-US.yml`, `pl-PL.yml`).

---

## Unlock Types

Every tier in `equipment.yml` requires an `unlock` block:

### 1. Timer (`type: timer`)
Unlocks automatically after a duration relative to server season start:
```yaml
armor:
  netherite:
    unlock:
      type: timer
      after: 14d    # Supports s, m, h, d, w units
```

### 2. Date (`type: date`)
Unlocks at an absolute ISO-8601 instant:
```yaml
weapons:
  mace:
    unlock:
      type: date
      at: "2026-12-01T18:00:00Z"
```

### 3. Always (`type: always`)
Available immediately from day 0:
```yaml
armor:
  iron:
    unlock:
      type: always
```

### 4. Command (`type: command`)
Locked until unlocked by an administrator or automated server script:
```yaml
elytra:
  wings:
    unlock:
      type: command
```

---

## Granular Restriction Flags

Define exactly what actions are blocked while a tier remains locked:

```yaml
restrictions:
  craft: true           # Blocks crafting and smithing recipes
  equip: true           # Blocks putting on armor or offhand shield
  use: true             # Blocks swinging weapons or tool interaction
  pickup: true          # Blocks picking up items from ground
  inventory-move: true  # Blocks moving item in inventory slots
```

---

## Enchantment Gating

Tiers can allow items while restricting high-level enchantments:
```yaml
enchantments:
  allowed: false        # Prevents enchanting until full enchantment stage unlocks
```
