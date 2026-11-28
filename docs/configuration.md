# Configuration Reference

GearProgress provides a human-readable, data-driven YAML configuration system. All configuration files are automatically created upon initial server boot within `plugins/GearProgress/`.

---

## 📁 Configuration File Map

```text
plugins/GearProgress/
├── config.yml              # Primary server options, check interval, and notification toggles
├── equipment.yml           # Equipment categories, tier definitions, unlock conditions, and restrictions
├── timers.yml              # Server season start epoch and live runtime overrides
├── storage.yml             # SQLite persistence configuration
├── gearprogress.db         # Persistent SQLite database (WAL mode)
└── locales/
    ├── en-US.yml           # English message catalog (MiniMessage format)
    └── pl-PL.yml           # Polish message catalog (MiniMessage format)
```

---

## ⚙️ `config.yml` Specifications

```yaml
schema-version: 1
default-locale: en-US
check-interval-seconds: 5

notifications:
  chat: true
  actionbar: true
```

- **`schema-version`**: Internal schema identifier. Do not modify.
- **`default-locale`**: Default language catalog to load (`en-US` or `pl-PL`).
- **`check-interval-seconds`**: Frequency in seconds at which background timer tasks evaluate pending tier unlocks.
- **`notifications.chat`**: Whether to broadcast tier unlock announcements in player chat.
- **`notifications.actionbar`**: Whether to display restriction warnings on the player's action bar.

---

## 🛡️ `equipment.yml` Specifications

The heart of GearProgress. Controls progression for `armor`, `tools`, `weapons`, `shield`, and `elytra`.

```yaml
schema-version: 1

categories:
  armor:
    leather:
      unlock:
        method: always
    diamond:
      unlock:
        method: timer
        after: 3d
      restrictions:
        craft: false
        equip: true
        use: true
        pickup: false
        inventory-move: false
      enchantments:
        allow-enchanted: false
    netherite:
      unlock:
        method: timer
        after: 14d
      restrictions:
        craft: true
        equip: true
        use: true
        pickup: true
        inventory-move: true
```

### Unlock Methods
1. **`always`**: Unlocked from the start.
2. **`never`**: Permanently blocked unless altered in configuration.
3. **`timer`**: Automatically unlocks after duration relative to `server-start-epoch` in `timers.yml`.
   - Syntax: `14d`, `2w`, `3h30m`, `45s`, `10 minutes`.
4. **`date`**: Automatically unlocks at an exact ISO-8601 UTC timestamp (`at: "2026-12-01T18:00:00Z"`).
5. **`command`**: Locked by default until unlocked via `/gearprogress unlock <category> <tier>`.

### Restriction Flags
- **`craft`**: Intercepts vanilla crafting and smithing tables.
- **`equip`**: Intercepts right-clicking, shift-clicking, drag-and-drop, hotbar key swaps, and dispensers.
- **`use`**: Intercepts block breaking, weapon attacks, bow shooting, and interaction.
- **`pickup`**: Cancels item pickup from the ground.
- **`inventory-move`**: Prevents moving locked items between inventories.

### Enchantment Policy
- **`allow-enchanted: false`**: Restricts enchanting on enchanting tables and anvils, and blocks wearing pre-enchanted gear until permitted.

---

## 🕒 `timers.yml` Specifications

```yaml
schema-version: 1
server-start-epoch: "2026-11-20T20:00:00Z"
```

The baseline ISO-8601 UTC timestamp from which all relative `after` durations are calculated.
