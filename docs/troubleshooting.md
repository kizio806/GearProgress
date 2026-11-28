# Troubleshooting Guide

This guide covers common operational, configuration, and runtime issues in GearProgress, along with their resolutions.

---

## 1. Equipment Protection Issues

### Items are not being blocked when expected
1. **Check Bypass Permissions:**
   - Players with `gearprogress.bypass` or `gearprogress.admin` will bypass all progression restrictions.
   - Server operators (`op`) typically inherit all permissions unless permissions are explicitly handled by LuckPerms. Test with a standard non-op player account.
2. **Inspect Specific Restriction Flags:**
   - In `equipment.yml`, verify that the specific action is restricted for that tier:
     ```yaml
     restrictions:
       craft: true
       use: true
       equip: true
       pickup: true
       inventory-move: true
     ```
   - If `craft: false`, players can still craft the item even if `equip` and `use` are blocked.
3. **Inspect Realtime Tier State:**
   - Run `/gearprogress info <category> <tier>` in-game or from console to check if the unlock condition is active, relative time remaining, or if an administrator unlocked it early.

---

## 2. Timer and Relative Duration Issues

### Timers are not advancing after server restarts
1. **Timer State File (`timers.yml`):**
   - Verify that timers are configured and not marked as `paused: true`.
   - Ensure the server has write permissions to the plugin configuration directory (`plugins/GearProgress/`).
2. **System Clock Drift:**
   - Relative timers rely on UTC epoch timestamps stored in SQLite. Ensure NTP synchronization is enabled on your host server.

---

## 3. Database and Persistence (SQLite WAL)

### Database locks or I/O latency
GearProgress uses a dedicated `SingleWriterPersistenceExecutor` running SQLite in Write-Ahead Logging (`WAL`) mode to prevent blocking the Minecraft server main thread.

1. **File Locks on Windows or Network Drives:**
   - Do not run the server on network shares (NFS, SMB, OneDrive, Google Drive sync). SQLite locks will fail or cause severe latency.
2. **WAL File Size:**
   - Under normal operation, `gearprogress.db-wal` and `gearprogress.db-shm` exist alongside `gearprogress.db`. These are automatically checkpointed on shutdown and periodically during runtime.
3. **Clean Shutdown:**
   - Never force-kill the JVM (`kill -9`) unless completely unresponsive. A clean `/stop` command ensures the persistence executor drains all queued writes before closing the connection.

---

## 4. Platform and Compatibility Conflicts

### Folia Regional Thread Warnings
- GearProgress provides native Folia scheduling support (`FoliaTaskScheduler`). If you encounter errors relating to asynchronous entity or block access, ensure you are running the `gearprogress-<base>-<range>-paper-purpur-folia.jar` distribution, which automatically activates Folia handlers when Folia is detected.

### PlaceholderAPI (`%gearprogress_*%`) returns empty or unparsed placeholders
- Ensure PlaceholderAPI is installed and enabled.
- Verify that the tier or category key matches your configuration exactly (case-sensitive identifier).
- Example valid placeholder: `%gearprogress_unlocked_armor_diamond%` or `%gearprogress_time_remaining_armor_netherite%`.
