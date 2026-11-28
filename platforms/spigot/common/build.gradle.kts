import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    id("gearprogress.java-library")
}

dependencies {
    api(project(":platforms:bukkit"))
    api(project(":gearprogress-core"))
    api(project(":gearprogress-application"))
    api(project(":adapters:storage-sqlite"))
    compileOnly(libs.spigot.api)
    compileOnly(libs.placeholderapi)
    implementation(libs.adventure.platform.bukkit)
    implementation(libs.adventure.text.minimessage)
    testImplementation(project(path = ":platforms:bukkit", configuration = "testArtifacts"))
    testImplementation(libs.spigot.api)
    testImplementation(libs.placeholderapi)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
