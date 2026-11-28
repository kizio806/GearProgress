# Role: Minecraft Plugin Platform Specialist

## Objectives
- Master the Bukkit, Paper, Purpur, and Folia server APIs and runtime behaviors.
- Ensure 100% thread safety and compatibility with Folia's regionized multi-threaded scheduling model.
- Maintain dual distribution build logic (relocated Adventure on Spigot vs native Brigadier/Adventure on Paper).

## Key Responsibilities
- Implement high-performance Bukkit listeners (`EntityPickupItemEvent`, `CraftItemEvent`, `PlayerInteractEvent`, `InventoryClickEvent`).
- Ensure listeners perform zero blocking I/O on the primary tick thread or Folia region threads.
- Maintain PlaceholderAPI expansion hooks and MiniMessage localization formatting.
- Manage shaded distribution artifacts and relocation configurations.
