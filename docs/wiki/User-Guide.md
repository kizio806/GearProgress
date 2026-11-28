# Player & Administrator User Guide

This guide walks through using **GearProgress** during live gameplay, understanding restrictions, and managing seasons as an administrator.

---

## 🎮 Player Experience

### Viewing Active Progression Status
Players can view current unlock stages and countdown timers in two ways:

1. **Interactive Progression GUI:**
   ```bash
   /gearprogress gui
   ```
   Opens a virtual chest interface displaying each equipment category (Armor, Tools, Weapons, Shield, Elytra) with custom item icons, color-coded unlock status, active restriction tags, and time remaining until the next tier unlocks.

2. **Chat Information Query:**
   ```bash
   /gearprogress info armor diamond
   ```
   Prints the evaluated status, restriction flags, and remaining duration directly in chat formatted with Adventure MiniMessage.

### Understanding Gated Equipment Actions
When an equipment tier is locked, attempting restricted actions displays informative feedback via action bar and chat warnings:
- **Crafting Blocked:** Crafting a locked recipe shows an empty or canceled result in the crafting table.
- **Equipping Blocked:** Shift-clicking, dragging, or right-clicking locked armor plays a restriction sound and returns the armor to your inventory.
- **Usage Blocked:** Striking an enemy or mining with a locked weapon or tool cancels the interaction.
- **Enchantment Blocked:** If enchantments are not yet permitted for that tier, enchanting tables and anvils reject the item.

---

## 🛡️ Administrator Commands & Pacing

Administrators (with `gearprogress.admin` or `OP`) have complete control over progression timelines:

### 1. Manual Tier Overrides
If you need to unlock or lock a tier early for a special event:
```bash
# Unlock Netherite armor immediately
/gearprogress unlock armor netherite

# Re-lock Netherite armor
/gearprogress lock armor netherite

# Reset Netherite armor back to original timers.yml configuration
/gearprogress reset armor netherite
```

### 2. Live Countdown Timer Adjustments
Adjust season countdowns without editing files or restarting the server:
```bash
# Pause the diamond tier timer during maintenance
/gearprogress timer pause armor diamond

# Resume the paused timer
/gearprogress timer resume armor diamond

# Extend the countdown by 2 days
/gearprogress timer add armor diamond 2d

# Set the remaining time exactly to 5 days
/gearprogress timer set armor diamond 5d
```

### 3. Hot Configuration Reloading
```bash
/gearprogress reload
```
Safely reloads all YAML configuration files and locale catalogs. If any YAML file contains formatting syntax errors, GearProgress logs actionable diagnostics and retains the previous valid configuration in memory.
