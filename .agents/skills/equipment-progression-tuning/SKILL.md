---
name: equipment-progression-tuning
description: Design, calibrate, and validate equipment tier unlock schedules, restriction policies, enchantment gating, and SMP pacing.
---

# Equipment Progression Tuning Skill

## When to use it
Use this skill when:
- Designing new progression tiers or modifying duration schedules in `equipment.yml`.
- Calibrating SMP season pacing curves (e.g. 14-day, 30-day, or Hardcore progression).
- Configuring granular restriction policies (`craft`, `equip`, `use`, `pickup`, `inventory-move`).
- Setting up enchantment gating (`allow-enchanted: false`) for balance in competitive servers.

## Required steps
1. **Analyze Pacing Objective:**
   - Determine season length, player base activity, and desired milestone transitions (e.g. Iron -> Diamond -> Netherite).
2. **Draft YAML Configuration:**
   - Specify unlock method (`timer`, `date`, `command`, `always`, `never`).
   - For timers, specify duration using deterministic tokens (`14d`, `2w`, `3h30m`, `45s`).
   - Define restriction booleans for all 5 action interception vectors.
3. **Validate Compilation:**
   - Test YAML loading via `EquipmentDefinitionCompilerTest` or run `/gearprogress reload` in dev server.
   - Ensure `EquipmentDefinitionCompiler` emits zero validation warnings or errors.
4. **Inspect In-Game Presentation:**
   - Verify that item names, lore countdowns, and chest GUI icons in `/gearprogress gui` reflect the updated tiers.
5. **Verify Event Interception:**
   - Test restriction interception across crafting, equipping, swinging/mining, and picking up items.

## Validation checklist
- [ ] Tier names are lowercase alphanumeric strings (`leather`, `diamond`, `netherite`).
- [ ] Relative durations parse successfully with `RelativeDurationParser`.
- [ ] Restriction policies match intended gameplay balance.
- [ ] PlaceholderAPI outputs (%gearprogress_time_remaining_...%) format correctly.
- [ ] No compilation warnings logged during config loading.
