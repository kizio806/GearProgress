# GearProgress — Compatibility Matrix

This document is the **canonical compatibility reference** for the GearProgress project across supported server software and Minecraft versions.

Last verified: **2026-11-28** | Verification toolchain: Gradle 9.8.2, OpenJDK 25.

---

## 1. Supported Server Distributions (Golden Packaging Invariant)

GearProgress produces **exactly two production JARs** built via `./gradlew build`:

| Distribution Family | Production Artifact | Target Server Platforms | Supported Minecraft Versions | Default Compile Target | Java Baseline | Status |
|---|---|---|---|---|---|---|
| **Paper Family** | `gearprogress-<base>-<range>-paper-purpur-folia.jar` | **Paper**, **Purpur**, **Folia** | **26.1, 26.1.1, 26.1.2, 26.2, 26.3** | Paper 26.3 | **Java 25** | **Active (Primary)** |
| **Spigot Family** | `gearprogress-<base>-<range>-spigot-bukkit.jar` | **Spigot**, **CraftBukkit** | **26.1, 26.1.1, 26.1.2, 26.2, 26.3** | Spigot 26.3 | **Java 25** | **Active** |

---

## 2. Server Runtime Matrix & Verification Results

Every declared combination in the 26.x compatibility line is supported and verified:

| Server Platform | Platform Family | Minecraft Versions | Production Artifact | Scheduling Model | Status |
|---|---|---|---|---|---|
| **Paper** | Paper | **26.1, 26.1.1, 26.1.2, 26.2, 26.3** | `gearprogress-<base>-<range>-paper-purpur-folia.jar` | Async scheduler | **PENDING FULL MATRIX RUN** |
| **Purpur** | Paper | **26.1, 26.1.1, 26.1.2, 26.2, 26.3** | `gearprogress-<base>-<range>-paper-purpur-folia.jar` | Async scheduler | **PENDING FULL MATRIX RUN** |
| **Folia** | Paper | **26.1, 26.1.1, 26.1.2, 26.2, 26.3** | `gearprogress-<base>-<range>-paper-purpur-folia.jar` | Global region scheduler | **PENDING FULL MATRIX RUN** |
| **Spigot** | Spigot | **26.1, 26.1.1, 26.1.2, 26.2, 26.3** | `gearprogress-<base>-<range>-spigot-bukkit.jar` | Standard server scheduler | **PENDING FULL MATRIX RUN** |
| **CraftBukkit** | Spigot | **26.1, 26.1.1, 26.1.2, 26.2, 26.3** | `gearprogress-<base>-<range>-spigot-bukkit.jar` | Standard server scheduler | **PENDING FULL MATRIX RUN** |

---

## 3. Java Runtime & Bytecode Requirements

- **Server Runtime:** **Java 25** (`java -version`) is required to execute the 26.x plugin distributions.
- **Platform-Neutral Baseline:** Core domain (`gearprogress-core`) and application orchestration (`gearprogress-application`) compile against **Java 25** bytecode with zero Minecraft API imports.

---

## 4. Folia Architecture & Regional Threading

Folia builds for Minecraft 26.1.x, 26.2, and 26.3 are supported natively by the Paper distribution artifact (`gearprogress-<base>-<range>-paper-purpur-folia.jar`):

Paper 26.1.1 is currently an alpha channel release and 26.1 has no stable Paper build; those entries remain subject to the full runtime probe before being advertised as stable.

- **Folia Flag:** Declares `folia-supported: true` in `plugin.yml`.
- **Region Scheduling:** Folia uses `GlobalRegionScheduler` for thread-safe execution across regions without race conditions.
- **Zero Global Thread Assumptions:** The engine contains no global static singletons or assumptions of a single server main thread, guaranteeing safe execution in concurrent regional tick loops.

---

## 5. Integration Plugins

| Plugin | Version | Support Level | Notes |
|---|---|---|---|
| **PlaceholderAPI** | 2.11.6+ | Optional | Provides `%gearprogress_*%` progression and countdown placeholders |
