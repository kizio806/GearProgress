---
name: multi-platform-adaptation
description: Maintain and test dual Paper/Folia and Spigot platform adaptations
---

# Multi-Platform Adaptation Skill

When adding or modifying platform behavior:
1. Identify shared Bukkit logic and keep it in `platforms:bukkit`.
2. Implement modern asynchronous or Brigadier commands in `platforms:paper:common`.
3. Implement legacy-compatible commands and BukkitScheduler integration in `platforms:spigot:common`.
4. Ensure shading and relocation rules in `distributions/` package artifacts without class collisions.
