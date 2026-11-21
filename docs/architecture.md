# GearProgress Architecture

GearProgress is structured around Hexagonal Architecture (Ports and Adapters) with multiplatform platform separation:

```
distributions:paper ───> platforms:paper:common ───┐
                                                   ├──> platforms:bukkit ───> gearprogress-application ───> gearprogress-core
distributions:spigot ──> platforms:spigot:common ─┘                                 │
                                                                                    ▼
                                                                        adapters:storage-sqlite
```

## Modules

- **`gearprogress-core`**: Pure Java 21 domain model (`EquipmentCategory`, `EquipmentTier`, `TierKey`, `UnlockCondition`, `RestrictionPolicy`, `EnchantmentPolicy`, `TierProgressState`). Zero external or platform dependencies.
- **`gearprogress-application`**: Application orchestration and use cases (`ProgressionService`, `TimerService`, `RestrictionQueryService`, YAML configuration compilers, locale loaders, SPI ports).
- **`adapters:storage-sqlite`**: Asynchronous single-writer SQLite persistence with WAL mode and transaction safety.
- **`platforms:bukkit`**: Shared Bukkit-level abstraction containing event listeners (craft, use, equip, pickup, inventory move, enchantment), item classification, audience-based notifications, and PlaceholderAPI expansion.
- **`platforms:paper:common`**: Paper/Purpur/Folia platform adapter with Brigadier command handling and Folia async/region task scheduling.
- **`platforms:spigot:common`**: Spigot/CraftBukkit platform adapter with Bukkit command execution, tab completion, and BukkitScheduler task scheduling.
- **`distributions:paper`**: Produces shaded release `GearProgress-<version>-paper.jar`.
- **`distributions:spigot`**: Produces shaded release `GearProgress-<version>-spigot.jar` with relocated Kyori Adventure libraries.
