import org.springframework.boot.gradle.tasks.bundling.BootJar
plugins {

    id("io.kotest.multiplatform") version "5.0.2"
    kotlin("plugin.jpa") version "1.8.22"
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    api(project(":payment-service:payment-domain:payment-domain-service"))

    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Database
    // https://mvnrepository.com/artifact/org.postgresql/postgresql
    implementation("org.postgresql:postgresql:42.6.0")
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
