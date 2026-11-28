# Architecture and Module Boundaries

## Purpose
GearProgress is built on clean hexagonal architecture with strict modularity. Dependencies flow in a single direction: `platform -> application -> core`. Platform-neutral domain and use-case code must never couple to server or loader APIs.

## Standard
- **Core Domain Isolation:** `gearprogress-core` is pure Java 21. It defines equipment models, unlock conditions, duration math, and restriction policies. It must never depend on Bukkit, Paper, or NMS.
- **Application Orchestration:** `gearprogress-application` coordinates progression logic, timer evaluation, config parsing, and locale catalogs. It must remain platform-neutral (Java 21).
- **Platform Separation:** Minecraft APIs belong exclusively in platform modules (`:platforms:bukkit`, `:platforms:paper:common`, `:platforms:spigot:common`).
- **Distribution Packages:** Distributable shaded jars are produced exclusively by `:distributions:paper` and `:distributions:spigot`.
- **Hexagonal Ports:** Adapters implement SPI ports defined in `application` (`ProgressionRepository`, `TaskSchedulerPort`, `LifecycleReporterPort`, `EventPublisherPort`).
