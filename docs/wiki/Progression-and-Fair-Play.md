# Progression & Fair Play Guide

This guide discusses SMP season pacing strategies, PvP fair play, restriction bypass vectors, and competitive balance with **GearProgress**.

---

## ⚖️ Pacing Principles for SMP Seasons

In modern vanilla Minecraft, skilled players can obtain full Diamond armor and Netherite tools within several hours of a server launch. This rapid escalation creates a steep power imbalance between dedicated grinders and casual community members, often leading to early season burnout.

GearProgress restores healthy gameplay pacing through phased equipment tiers:

1. **Phase 1: The Iron Age (Days 1–3)**
   - Unlocks Leather, Chainmail, and Iron.
   - Diamond crafting may be permitted (allowing miners to stockpile resources), while Diamond equip/use is restricted until Day 3.
   - Fosters cooperative mining, community base building, and vanilla threat scaling.

2. **Phase 2: The Diamond Era (Days 3–14)**
   - Diamond armor and tools become usable.
   - High-tier enchantments can be restricted (`allow-enchanted: false`) for the first few days of this era to prevent overpowered Protection IV sets.

3. **Phase 3: The Netherite Pinnacle (Day 14+)**
   - Netherite gear and ancient debris forging unlock.
   - Provides an exciting server-wide milestone for the mid-season transition.

---

## 🛡️ Anti-Bypass & Exploit Prevention

GearProgress intercepts all known vanilla interaction vectors to guarantee that gated items cannot be used unintentionally or exploited:

| Interaction Vector | Interception Mechanism |
|---|---|
| **Inventory Hotbar Swapping (0–9 keys)** | `EquipRestrictionListener` listens to `InventoryClickEvent` with `ClickType.NUMBER_KEY` and cancels swaps into armor slots. |
| **Shift-Click Armor Fast Equip** | Intercepted and canceled; armor item remains in the player's inventory. |
| **Right-Click Air / Block Armor Equip** | Intercepted via `PlayerInteractEvent` and canceled before the packet equips the item. |
| **Dispenser Automatic Armor Equipping** | `BlockDispenseArmorEvent` is intercepted when targeting a player who has not unlocked the tier. |
| **Offhand Shield Equipping** | `PlayerSwapHandItemsEvent` and offhand inventory clicks are blocked if shields are locked. |
| **Anvil Pre-Enchanted Item Fusion** | `PrepareAnvilEvent` cancels combine operations if the tier prohibits enchantments. |
| **Dropping & Vacuuming Items** | `PickupRestrictionListener` cancels `EntityPickupItemEvent` if `pickup: true` is configured. |

---

## 🔑 Bypass Permissions for Testing & Moderation

- **`gearprogress.bypass`**: Players with this permission node completely bypass all progression gating rules.
- **`gearprogress.admin`**: Wildcard admin permission for managing timers and commands.
- **OP Account Behavior:** Server operators (`OP`) typically inherit all permissions. To test progression gameplay accurately, server owners should always test using an alternate non-OP account without bypass permissions.
