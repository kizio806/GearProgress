import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    id("gearprogress.java-library")
}

dependencies {
    api(project(":gearprogress-core"))
    implementation(libs.snakeyaml)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.archunit.junit5)
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
