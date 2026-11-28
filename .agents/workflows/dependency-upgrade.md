# Workflow: Dependency Upgrade & Maintenance

A disciplined workflow for updating Gradle dependencies, build-logic plugins, and third-party libraries while preserving 100% green verification.

---

## Phase 1: Dependency Identification
1. Review Dependabot alerts, Renovate PRs, or check for outdated libraries in `gradle/libs.versions.toml`.
2. Inspect upstream changelogs for breaking changes or deprecations (e.g. SQLite JDBC, Kyori Adventure, MockBukkit).

## Phase 2: Updating Version Catalogs
1. Edit `gradle/libs.versions.toml` to increment target library version.
2. If updating Gradle wrapper:
   ```bash
   ./gradlew wrapper --gradle-version <new_version>
   ```

## Phase 3: Compilation & Warning Resolution
1. Run `./gradlew clean compileJava compileTestJava`.
2. Fix all compiler warnings resulting from the update.
3. Remember: `-Xlint:all` and `-Werror` are enforced; `@SuppressWarnings` is forbidden.

## Phase 4: Full Quality Pipeline Execution
1. Run Spotless formatting:
   ```bash
   ./gradlew spotlessApply
   ```
2. Run full check:
   ```bash
   ./gradlew check
   ```
3. Run structure and hygiene validations:
   ```bash
   ./scripts/validate-structure.sh
   ./scripts/validate-source-hygiene.sh
   ```

## Phase 5: Concurrency & Stress Testing
1. If SQLite JDBC was updated, run `SingleWriterPersistenceExecutorTest` under stress to verify WAL mode stability.
2. If Kyori Adventure was updated, verify MiniMessage deserialization and locale formatting.
