import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    id("gearprogress.java-library")
}

dependencies {
    api(project(":gearprogress-application"))
    implementation(libs.sqlite.jdbc)
    testImplementation(libs.sqlite.jdbc)
    testImplementation(libs.junit.jupiter)
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
