# Domain Model Context — GearProgress

## Core Entities & Value Objects

### 1. EquipmentCategory
Represents the broad category of gated equipment:
- `ARMOR`
- `TOOLS`
- `WEAPONS`
- `SHIELD`
- `ELYTRA`

### 2. EquipmentTier & TierKey
- `TierKey`: Unique composite key `(category, name)` (e.g. `ARMOR/diamond`, `ARMOR/netherite`, `ELYTRA/elytra`).
- `EquipmentTier`: Encapsulates items belonging to the tier, unlock conditions, restriction policy, and enchantment policy.

### 3. UnlockCondition
Defines when and how a tier transitions from LOCKED to UNLOCKED:
- `AlwaysUnlockCondition`: Tier is available from day 0.
- `NeverUnlockCondition`: Tier cannot be unlocked automatically (manual admin unlock only).
- `TimerUnlockCondition`: Unlocked after relative duration from server start/reset (e.g. `3d`, `12h`).
- `DateUnlockCondition`: Unlocked at an absolute ISO-8601 instant (e.g. `2026-12-01T18:00:00Z`).
- `ManualUnlockCondition`: Unlocked exclusively via admin command.

### 4. RestrictionPolicy
Granular boolean flags controlling what actions are blocked when locked:
- `craft`: Prevent crafting or smithing recipe output.
- `use`: Prevent swinging, shooting, right-clicking, or using tool actions.
- `equip`: Prevent placing in armor slots or offhand.
- `pickup`: Prevent picking up dropped items from the ground.
- `inventoryMove`: Prevent moving item within containers or dragging across inventory slots.

### 5. EnchantmentPolicy
Controls whether items within an unlocked tier may be enchanted, or whether enchantments are gated behind subsequent stages.
