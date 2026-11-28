import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    id("gearprogress.java-library")
}

dependencies {
    testImplementation(libs.junit.jupiter)
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.95".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
