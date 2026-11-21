# GearProgress — Compatibility Matrix

This document is the **canonical compatibility reference** for the GearProgress project across supported server software and Minecraft versions.

Last verified: **2026-11-21** | Verification toolchain: Gradle 9.8.2, OpenJDK 21.

---

## 1. Supported Server Distributions (Golden Packaging Invariant)

GearProgress produces **exactly two production JARs** built via `./gradlew build`:

| Distribution Family | Production Artifact | Target Server Platforms | Supported Minecraft Versions | Default Compile Target | Java Baseline | Status |
|---|---|---|---|---|---|---|
| **Paper Family** | `gearprogress-<base>-<range>-paper-purpur-folia.jar` | **Paper**, **Purpur**, **Folia** | **1.21 – 1.21.11** | Paper 1.21.11 | **Java 21** | **Active (Primary)** |
| **Spigot Family** | `gearprogress-<base>-<range>-spigot-bukkit.jar` | **Spigot**, **CraftBukkit** | **1.21 – 1.21.11** | Spigot 1.21 | **Java 21** | **Active** |

---

## 2. Server Runtime Matrix & Verification Results

Every declared combination of server runtime and Minecraft version in the 1.21.x series (1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11) is supported and verified:

| Server Platform | Platform Family | Minecraft Versions | Production Artifact | Scheduling Model | Status |
|---|---|---|---|---|---|
| **Paper** | Paper | **1.21 – 1.21.11** | `gearprogress-<base>-<range>-paper-purpur-folia.jar` | Async scheduler (`getAsyncScheduler`) | **VERIFIED (PASS)** |
| **Purpur** | Paper | **1.21 – 1.21.11** | `gearprogress-<base>-<range>-paper-purpur-folia.jar` | Async scheduler (`getAsyncScheduler`) | **VERIFIED (PASS)** |
| **Folia** | Paper | **1.21 – 1.21.11** | `gearprogress-<base>-<range>-paper-purpur-folia.jar` | Global region scheduler (`getGlobalRegionScheduler`) | **VERIFIED (PASS)** |
| **Spigot** | Spigot | **1.21 – 1.21.11** | `gearprogress-<base>-<range>-spigot-bukkit.jar` | Standard server scheduler (`BukkitScheduler`) | **VERIFIED (PASS)** |
| **CraftBukkit** | Spigot | **1.21 – 1.21.11** | `gearprogress-<base>-<range>-spigot-bukkit.jar` | Standard server scheduler (`BukkitScheduler`) | **VERIFIED (PASS)** |

---

## 3. Java Runtime & Bytecode Requirements

- **Server Runtime:** **Java 21** (`java -version`) is strictly required to execute the plugin distributions on Paper, Purpur, Folia, and Spigot servers.
- **Platform-Neutral Baseline:** Core domain (`gearprogress-core`) and application orchestration (`gearprogress-application`) compile against **Java 21** bytecode with zero Minecraft API imports.

---

## 4. Folia Architecture & Regional Threading

Folia builds for Minecraft 1.21 – 1.21.11 are supported natively by the Paper distribution artifact (`gearprogress-<base>-<range>-paper-purpur-folia.jar`):

- **Folia Flag:** Declares `folia-supported: true` in `plugin.yml`.
- **Region Scheduling:** Folia uses `GlobalRegionScheduler` for thread-safe execution across regions without race conditions.
- **Zero Global Thread Assumptions:** The engine contains no global static singletons or assumptions of a single server main thread, guaranteeing safe execution in concurrent regional tick loops.

---

## 5. Integration Plugins

| Plugin | Version | Support Level | Notes |
|---|---|---|---|
| **PlaceholderAPI** | 2.11.6+ | Optional | Provides `%gearprogress_*%` progression and countdown placeholders |
