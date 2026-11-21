# Workflow: New Feature Implementation

## Steps
1. **Requirements & Domain Analysis:**
   - Define domain concepts in `gearprogress-core`.
   - Update `EquipmentTier`, `RestrictionPolicy`, or `UnlockCondition` models without any Minecraft imports.
2. **Application Port & Service Design:**
   - Add use cases and SPI ports in `gearprogress-application`.
   - Update YAML configuration loader/parser if configuration changes are involved.
3. **Adapter Implementation:**
   - Update `adapters:storage-sqlite` if persistence state is modified.
   - Implement or update Bukkit listeners in `platforms:bukkit`.
4. **Platform Binding:**
   - Update Paper Brigadier commands and Folia schedulers in `platforms:paper`.
   - Update Spigot commands and schedulers in `platforms:spigot`.
5. **Quality Verification:**
   - Add unit tests achieving $\ge 90\%$ JaCoCo instruction coverage.
   - Run `./gradlew check`.
6. **Documentation & Changelog:**
   - Document new configurations in `docs/` and `docs/wiki/`.
   - Add entry to `CHANGELOG.md`.
