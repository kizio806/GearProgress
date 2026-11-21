# Troubleshooting & FAQ

This document covers diagnostics, resolution steps, and common edge cases for **GearProgress**.

---

## 🔍 Common Issues & Resolutions

### 1. Equipment Not Being Blocked
- **Check Bypass Permissions:** Players with `gearprogress.bypass` or OP status bypass restrictions. Test with a non-OP account.
- **Check Specific Restriction Flags:** Verify that the action being attempted (`craft`, `equip`, `use`, `pickup`, or `inventory-move`) is set to `true` in `equipment.yml`.
- **Check Unlock Condition:** Run `/gearprogress info <category> <tier>` to inspect the current state and evaluated unlock timestamp.

### 2. SQLite Database Locked Exceptions
- GearProgress uses `SingleWriterPersistenceExecutor` to prevent lock contention.
- If third-party backup tools or external SQL browsers open `gearprogress.db` in exclusive lock mode, SQLite may return `SQLITE_BUSY`.
- Ensure other software accesses SQLite in shared read mode and that WAL mode is active.

### 3. Placeholders Not Displaying Values
- Ensure **PlaceholderAPI** is installed and enabled.
- Verify the placeholder syntax: `%gearprogress_time_remaining_<category>_<tier>%`.
- Check console during server boot to confirm the GearProgress expansion registered successfully.

### 4. Configuration Reload Warnings
- If YAML syntax is invalid (e.g. incorrect indentation), the parser logs descriptive error diagnostics and keeps the previously active valid configuration in memory to prevent runtime crashes.
