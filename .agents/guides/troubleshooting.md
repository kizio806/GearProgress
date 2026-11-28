# AI & Developer Troubleshooting Guide

Diagnostic procedures and common resolutions for build, compilation, test, and runtime issues.

---

## 🔍 Diagnostic Decision Trees

### 1. Build Fails on `spotlessJavaCheck`
- **Cause:** Source files violate Google Java Format (e.g. indentation, line wrapping, import order).
- **Resolution:**
  ```bash
  ./gradlew spotlessApply
  ```

### 2. Compilation Fails with `-Werror`
- **Cause:** Compiler warning treated as error (e.g. unchecked conversion, deprecated API call).
- **Resolution:**
  - Inspect the specific compiler warning output.
  - Fix the underlying type safety or deprecation at the source.
  - **Do NOT** add `@SuppressWarnings`.

### 3. Build Fails on `verifyPlatformBoundaries`
- **Cause:** A class in `gearprogress-core`, `gearprogress-application`, or `adapters/storage-sqlite` imports a forbidden Minecraft class (`org.bukkit.*`, `io.papermc.*`, `net.minecraft.*`).
- **Resolution:**
  - Move the platform-specific code into `platforms/bukkit` or create a port SPI interface in `gearprogress-application/port/`.

### 4. Build Fails on `validateSourceHygiene`
- **Cause:** File contains `@SuppressWarnings`, wildcard imports (`.*`), or task markers (`TODO`, `FIXME`, `XXX`).
- **Resolution:**
  - Remove wildcard imports and import exact classes.
  - Implement uncompleted work and remove task markers.
  - Fix linter warnings rather than silencing them.

### 5. JaCoCo Coverage Below Threshold (`jacocoRootCoverageVerification`)
- **Cause:** New or modified code is not covered by tests, dropping instruction coverage below 90% (or 95% on core).
- **Resolution:**
  - Open `build/reports/jacoco/jacocoRootReport/html/index.html`.
  - Identify unexercised branches, edge cases, or exception handlers.
  - Add meaningful unit tests asserting those paths.

### 6. SQLite Lock Contention (`SQLITE_BUSY`)
- **Cause:** Direct database writes performed outside `SingleWriterPersistenceExecutor`.
- **Resolution:**
  - Ensure all repository write mutations (`save()`, `delete()`, `executeWrite()`) are routed through the executor.
  - Verify that WAL mode is enabled (`PRAGMA journal_mode=WAL;`).
