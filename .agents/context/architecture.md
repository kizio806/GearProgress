# Architecture Context — GearProgress

## Architectural Pattern
GearProgress is structured strictly according to **Hexagonal Architecture** (Ports and Adapters).

```mermaid
flowchart TD
    subgraph Core ["gearprogress-core (Pure Java Domain)"]
        DomainModels["EquipmentCategory, EquipmentTier, TierKey, State"]
        Rules["RestrictionPolicy, EnchantmentPolicy, UnlockCondition"]
        DurationEngine["RelativeDurationParser, TimeRemaining"]
    end

    subgraph Application ["gearprogress-application (Use Cases & Ports)"]
        Services["ProgressionService, RestrictionQueryService"]
        Ports["ProgressionRepository (SPI), TimePort, EventPublisherPort"]
        Loaders["YamlConfigurationLoader, LocaleCatalogLoader"]
    end

    subgraph Adapters ["adapters:storage-sqlite"]
        SQLiteRepo["SQLiteProgressionRepository"]
        WALWriter["SingleWriterPersistenceExecutor (WAL Mode)"]
    end

    subgraph Platforms ["platforms (Paper / Folia / Spigot)"]
        BukkitPlatform["platforms:bukkit (Shared Listeners, PAPI Expansion)"]
        PaperPlatform["platforms:paper:common (Brigadier Commands, Folia Scheduler)"]
        SpigotPlatform["platforms:spigot:common (Bukkit Commands, Bukkit Scheduler)"]
    end

    subgraph Distributions ["distributions"]
        DistPaper["distributions:paper (GearProgress-<version>-paper.jar)"]
        DistSpigot["distributions:spigot (GearProgress-<version>-spigot.jar)"]
    end

    Platforms --> Application
    Application --> Core
    Application --> Ports
    Adapters -. implements .-> Ports
    DistPaper --> PaperPlatform
    DistPaper --> BukkitPlatform
    DistPaper --> Adapters
    DistSpigot --> SpigotPlatform
    DistSpigot --> BukkitPlatform
    DistSpigot --> Adapters
```

## Module Isolation Invariants
1. `gearprogress-core`: Zero external dependencies, pure Java 21, no server APIs.
2. `gearprogress-application`: Depends only on `core`, defines SPI ports, zero server APIs.
3. `adapters:storage-sqlite`: Implements `ProgressionRepository` using SQLite JDBC and WAL mode.
4. `platforms:bukkit`: Shared Minecraft logic depending on Bukkit API and Adventure.
5. `platforms:paper` & `platforms:spigot`: Platform-specific execution layers.
6. `distributions`: Assembles shaded release JARs with relocation and manifest packaging.
