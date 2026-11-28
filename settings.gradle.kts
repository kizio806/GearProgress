pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "PaperMC"
            content {
                includeGroup("io.papermc.paper")
                includeGroup("com.mojang")
                includeGroup("net.md-5")
            }
        }
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") { name = "Spigot" }
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") { name = "PAPI" }
    }
}

rootProject.name = "GearProgress"

include(
    "gearprogress-core",
    "gearprogress-application",
    "adapters:storage-sqlite",
    "platforms:bukkit",
    "platforms:paper:common",
    "platforms:spigot:common",
    "distributions:paper",
    "distributions:spigot",
)
