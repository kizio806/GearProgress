# Building from Source

This guide details how to clone, compile, test, and package **GearProgress** from source code.

---

## ☕ Prerequisites

- **Java Development Kit (JDK):** **Java 21 (LTS)** or higher. We recommend [Eclipse Temurin](https://adoptium.net/) or standard OpenJDK distributions.
- **Git:** Required to clone the repository and evaluate commit metadata.
- **Bash Shell:** Required for verification and automated release scripts.
- **Internet Access:** Required on initial build to download Gradle wrapper, Minecraft server APIs, and build plugins.

---

## 📥 Cloning the Repository

```bash
git clone https://github.com/kizio806/GearProgress.git
cd GearProgress
```

---

## 🔨 Gradle Build Lifecycle & Tasks

GearProgress uses Gradle with Kotlin DSL (`build.gradle.kts`) and precompiled convention plugins in `build-logic/`.

### 1. Compile & Format Code
```bash
# Apply Spotless code formatting (Google Java Format, 2-space indentation)
./gradlew spotlessApply

# Verify code formatting without making modifications
./gradlew spotlessCheck
```

### 2. Run Test Suites & Quality Gates
```bash
# Run unit and integration tests across all modules
./gradlew test

# Run complete quality pipeline (Spotless, Checkstyle, SpotBugs, PMD, ErrorProne, boundaries, JaCoCo)
./gradlew check
```

### 3. Build Production Distribution Artifacts
```bash
# Build shaded release JARs for Paper/Purpur/Folia and Spigot/CraftBukkit
./gradlew :distributions:paper:build :distributions:spigot:build

# Verify that exactly two distribution artifacts are produced matching Golden Packaging Invariant
./gradlew verifyDistributionArtifacts
```

Production artifacts are placed in:
- `distributions/paper/build/libs/gearprogress-<base>-<range>-paper-purpur-folia.jar`
- `distributions/spigot/build/libs/gearprogress-<base>-<range>-spigot-bukkit.jar`

---

## 🧪 Local Test Server Execution

You can launch a self-contained Paper development server with GearProgress automatically installed:

```bash
# Launch Paper test server
./gradlew :distributions:paper:runServer
```

This spins up an isolated server in `distributions/paper/run/` pre-configured for local testing.

---

## 🛠️ Verification Commands Table

| Command | Purpose | Checks Performed |
|---|---|---|
| `./gradlew spotlessApply` | Code style enforcement | Formats Java with Google Java Format (2 spaces) and build scripts with ktlint. |
| `./gradlew test` | Behavioral testing | Executes pure JUnit 5 domain tests, SQLite WAL concurrency tests, and MockBukkit listener tests. |
| `./gradlew check` | Root quality gate | Checkstyle, SpotBugs, PMD, ErrorProne, boundary verification, SemVer check, and JaCoCo ($\ge 90\%$). |
| `./scripts/validate-structure.sh` | Structural validation | Validates package declaration consistency, source directories, and documentation links. |
| `./scripts/validate-source-hygiene.sh` | Source hygiene gate | Rejects `@SuppressWarnings`, wildcard imports (`.*`), and `TODO`/`FIXME` tags. |
| `./scripts/release.sh --dry-run` | Release dry-run | Validates packaging, artifact existence, and SHA256 checksum generation. |
