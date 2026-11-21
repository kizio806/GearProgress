# Development Guide

## Prerequisites
- **JDK 21** or higher (Eclipse Temurin, Amazon Corretto, or OpenJDK)
- **Git** 2.40+
- Modern IDE: IntelliJ IDEA (recommended) or VS Code with Java Extension Pack

## Project Structure and Hexagonal Architecture

GearProgress strictly follows Hexagonal Architecture (Ports and Adapters):

```text
gearprogress/
├── gearprogress-core/           # Pure domain logic (zero framework dependencies)
├── gearprogress-application/    # Application use cases, ports (SPI), services, and config
├── adapters/
│   └── storage-sqlite/          # Secondary adapter: SQLite persistence (WAL mode)
├── platforms/
│   ├── bukkit/                  # Common Bukkit event listeners, item resolution, PAPI expansion
│   ├── paper/common/            # Primary adapter: Paper bootstrap, Brigadier commands, Folia scheduler
│   └── spigot/common/           # Primary adapter: Spigot bootstrap, legacy commands, Bukkit scheduler
└── distributions/
    ├── paper/                   # Final shaded distribution for Paper/Purpur/Folia
    └── spigot/                  # Final shaded distribution for Spigot/CraftBukkit (relocated Adventure)
```

### Module Boundaries and Constraints

- `gearprogress-core` and `gearprogress-application` must **never** import `org.bukkit`, `io.papermc`, `net.minecraft`, or any other platform-specific classes.
- Primary adapters (`platforms/*`) wire services via constructor injection in `PaperBootstrap` or `SpigotBootstrap`. No global singletons are permitted.
- `adapters/storage-sqlite` communicates solely through port interfaces (`ProgressionRepository`) defined in `gearprogress-application`.

## Building and Verification

### Standard Build
```bash
./gradlew clean build
```

This compiles all modules, runs all unit and integration tests, and generates production distribution JARs in:
- `distributions/paper/build/libs/GearProgress-<version>-paper.jar`
- `distributions/spigot/build/libs/GearProgress-<version>-spigot.jar`

### Code Formatting and Linting
```bash
# Auto-format Java code according to Google Java Format
./gradlew spotlessApply

# Run Checkstyle (Google Checks, maxErrors = 0)
./gradlew checkstyleMain checkstyleTest

# Run PMD static code analysis
./gradlew pmdMain pmdTest

# Run SpotBugs bug detection
./gradlew spotbugsMain

# Run architectural boundary and source hygiene verification scripts
./scripts/verification/validate-structure.sh
./scripts/verification/validate-source-hygiene.sh
```

### Running Test Servers Locally
The project includes tasks for spinning up isolated local development servers:
```bash
# Launch a local Paper server with GearProgress auto-installed
./gradlew :platforms:paper:runServer
```

## Testing Guidelines

- **Unit Tests:** Pure JUnit 5 tests covering core domain rules and application use cases. Fast, deterministic, and executed in parallel.
- **MockBukkit Tests:** Platform event listeners and commands are verified using MockBukkit without requiring a live Minecraft server instance.
- **Concurrency & Persistence Tests:** `SingleWriterPersistenceExecutorTest` verifies serialized execution, queue drainage on shutdown, and rejection policies.
- **Quality Gates:** JaCoCo enforces a strict minimum of **90%** instruction coverage across `gearprogress-core` and `gearprogress-application`.
