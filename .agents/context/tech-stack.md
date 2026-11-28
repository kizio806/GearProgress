# Tech Stack Context — GearProgress

## Language & JVM
- **Java Version:** Java 25 baseline for the active 26.x line. The `support/mc-1.21.x` branch remains on Java 21.
- **Compiler Options:** `-Xlint:all -Werror -parameters`.

## Build & Tooling
- **Build System:** Gradle 9.8.2 with Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`, `build-logic`).
- **Formatting:** Spotless with Google Java Format (2 spaces).
- **Code Analysis:** Checkstyle, PMD, SpotBugs, ArchUnit.
- **Coverage:** JaCoCo with $\ge 90\%$ instruction coverage enforcement per module and root aggregate.

## Server APIs & Libraries
- **Paper API:** `26.3.build.264-stable` (Mojang-mapped).
- **Spigot API:** `26.3-R0.1-SNAPSHOT`.
- **Text & Localization:** Adventure MiniMessage (`net.kyori:adventure-text-minimessage`). Relocated on Spigot.
- **Commands:** Paper Brigadier Command API (`io.papermc.paper:paper-brigadier`).
- **Persistence:** SQLite JDBC (`org.xerial:sqlite-jdbc`) in WAL mode.
- **Testing:** JUnit 5, AssertJ, Mockito, ArchUnit.
