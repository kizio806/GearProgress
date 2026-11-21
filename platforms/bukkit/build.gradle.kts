import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    id("gearprogress.java-library")
}

dependencies {
    api(project(":gearprogress-core"))
    api(project(":gearprogress-application"))
    compileOnly(libs.spigot.api)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.adventure.text.minimessage)
    testImplementation(libs.spigot.api)
    testImplementation(libs.placeholderapi)
    testImplementation(libs.adventure.text.minimessage)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

val testJar =
    tasks.register<Jar>("testJar") {
        archiveClassifier.set("tests")
        from(sourceSets.test.get().output)
    }

configurations {
    create("testArtifacts") {
        isCanBeConsumed = true
        isCanBeResolved = false
    }
}

artifacts {
    add("testArtifacts", testJar)
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.75".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
