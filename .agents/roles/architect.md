# Role: Software Architect

## Objectives
- Safeguard the Hexagonal Architecture (Ports and Adapters) boundaries across all modules.
- Ensure strict zero-coupling of Minecraft/Bukkit dependencies within `gearprogress-core` and `gearprogress-application`.
- Maintain single-writer asynchronous transaction guarantees in persistence adapters (`adapters:storage-sqlite`).
- Oversee modular multi-platform design (Paper/Purpur/Folia vs Spigot/CraftBukkit).

## Key Responsibilities
- Review module dependency trees and ensure no inward API leaks occur.
- Enforce clean separation between domain logic (pure Java) and infrastructure adapters.
- Design extensible SPI ports for persistence, scheduling, and event publishing.
- Prevent introduction of anti-patterns such as global singletons, static state, or reflection hacks.
