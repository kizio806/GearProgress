import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.GradleException
import org.gradle.jvm.tasks.Jar
import java.util.zip.ZipFile

plugins {
    id("gearprogress.java-base")
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

group = "io.github.kizio806.gearprogress.distribution"

dependencies {
    implementation(project(":gearprogress-core"))
    implementation(project(":gearprogress-application"))
    implementation(project(":adapters:storage-sqlite"))
    implementation(project(":platforms:paper:common"))
}

tasks.processResources {
    val pluginVersion = project.version.toString()
    inputs.property("version", pluginVersion)
    filesMatching("plugin.yml") {
        expand("version" to pluginVersion)
    }
}

val fullVersion = project.version.toString()
val baseVersion = fullVersion.substringBefore('+')
val mcRange = if (fullVersion.contains('+')) fullVersion.substringAfter('+') else "mc1.21.x"
val artifactBaseName = providers.gradleProperty("gearprogress.artifactBaseName").getOrElse("gearprogress")

val shadowJar =
    tasks.named<ShadowJar>("shadowJar") {
        archiveFileName.set("$artifactBaseName-$baseVersion-$mcRange-paper-purpur-folia.jar")
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true

        relocate("org.yaml.snakeyaml", "io.github.kizio806.gearprogress.libs.snakeyaml")
        relocate("org.sqlite", "io.github.kizio806.gearprogress.libs.sqlite")

        manifest {
            attributes(
                "Implementation-Title" to "GearProgress-Paper",
                "Implementation-Version" to project.version.toString(),
                "Implementation-Vendor" to "kizio806",
            )
        }
        from(rootProject.file("LICENSE")) {
            into("META-INF")
            rename { "LICENSE.txt" }
        }
    }

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<Jar>("sourcesJar") {
    enabled = false
}

val verifyPluginArtifact =
    tasks.register("verifyPluginArtifact") {
        group = "verification"
        description = "Validates the shaded Paper distribution JAR."
        dependsOn(shadowJar)
        inputs.file(shadowJar.flatMap { it.archiveFile })

        doLast {
            val archive =
                shadowJar
                    .get()
                    .archiveFile
                    .get()
                    .asFile
            val entries =
                ZipFile(archive).use { zip ->
                    zip
                        .entries()
                        .asSequence()
                        .map { it.name }
                        .toList()
                }

            val requiredSuffixes =
                listOf(
                    "plugin.yml",
                    "io/github/kizio806/gearprogress/platform/paper/GearProgressPlugin.class",
                    "io/github/kizio806/gearprogress/platform/paper/PaperBootstrap.class",
                    "io/github/kizio806/gearprogress/application/GearProgressApplication.class",
                    "io/github/kizio806/gearprogress/adapter/storage/sqlite/SQLiteProgressionRepository.class",
                )

            val missing = requiredSuffixes.filter { required -> entries.none { it.endsWith(required) } }
            val forbidden =
                entries.filter { entry ->
                    entry.contains("/src/test/") ||
                        entry.endsWith("Test.class") ||
                        entry.contains(".idea/") ||
                        entry.contains(".gradle/")
                }

            val pluginDescriptor =
                ZipFile(archive).use { zip ->
                    val entry = zip.getEntry("plugin.yml") ?: throw GradleException("plugin.yml not found in JAR")
                    zip.getInputStream(entry).use { input ->
                        String(input.readAllBytes(), Charsets.UTF_8)
                    }
                }
            val descriptorValid =
                pluginDescriptor.contains(
                    "main: io.github.kizio806.gearprogress.platform.paper.GearProgressPlugin",
                ) &&
                    pluginDescriptor.contains("api-version: '26.1'") &&
                    pluginDescriptor.contains("folia-supported: true")

            if (missing.isNotEmpty() || forbidden.isNotEmpty() || !descriptorValid) {
                throw GradleException(
                    "Invalid Paper artifact. Missing=$missing, forbidden=$forbidden, descriptorValid=$descriptorValid",
                )
            }
        }
    }

tasks.named("assemble") {
    dependsOn(shadowJar)
}

tasks.named("check") {
    dependsOn(verifyPluginArtifact)
}
