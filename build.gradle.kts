plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "faang.school"
version = "1.0"

// Temporary CVE mitigation; remove after Spring Boot manages Tomcat 11.0.25+.
extra["tomcat.version"] = "11.0.25"

val javaVersion = 25
val springCloudVersion = "2025.1.3"
val testcontainersVersion = "2.0.5"
val mapstructVersion = "1.6.3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

repositories {
    mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    }
}

dependencies {
    /**
     * Spring boot starters
     */
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    /**
     * Kafka
     */
    implementation("org.springframework.kafka:spring-kafka")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")

    /**
     * Database
     */
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")
    implementation("redis.clients:jedis")


    /**
     * Utils & Logging
     */
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Exclude Vaadin's old android-json in favor of org.json
    implementation("org.json:json:20250517")
    configurations.all {
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }

    /**
     * Tests
     */
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}

jacoco {
    toolVersion = "0.8.15"
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-Xshare:off", "-javaagent:${mockitoAgent.asPath}")
}

// Unit tests only — integration tests are excluded by tag and run via `integrationTest`.
tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("integration")
    }
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    finalizedBy(tasks.named("jacocoTestReport"))
}

// Integration tests (Testcontainers) — run explicitly, not part of the unit gate.
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests (tagged 'integration')."
    group = "verification"
    dependsOn(tasks.named("testClasses"))
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    shouldRunAfter(tasks.named("test"))
    useJUnitPlatform {
        includeTags("integration")
    }
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}

// Coverage gate for application logic only. Thresholds are set from the measured
// unit-test baseline (2026-08-30, 78 unit tests) and ramp up non-decreasingly
// (DEVPLAN_UNITSTESTS-RULES.md §3). Measured per-class baseline:
//   AnalyticsEventService instr=1.00 line=1.00 branch=0.75 | listeners instr=1.00 line=1.00
//   controllers instr=1.00 line=1.00 | Interval instr=0.92 line=1.00 branch=1.00
//   EventContract/UserHeaderFilter/UserContext/FeignUserInterceptor instr=1.00 line=1.00
// Gate: INSTRUCTION >= 0.90 per class (floor below the weakest measured class, Interval).
// Documented exclusions (narrow, per rules): mapper.* (MapStruct-generated), dto.* (Lombok POJOs
// except Interval which contains logic), model.* (Lombok entity/enum), config.* (Spring wiring,
// property holders, Kafka bean factories), client.FeignConfig (bean wiring), exception.* (no logic).
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            element = "CLASS"
            includes = listOf(
                "faang.school.analytics.service.*",
                "faang.school.analytics.listener.*",
                "faang.school.analytics.controller.*",
                "faang.school.analytics.dto.Interval",
                "faang.school.analytics.events.EventContract",
                "faang.school.analytics.config.UserHeaderFilter",
                "faang.school.analytics.config.UserContext",
                "faang.school.analytics.client.FeignUserInterceptor"
            )
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.bootJar {
    archiveFileName.set("service.jar")
}
