import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.boot:spring-boot-starter-json")

    implementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")

    api(project(":order-service:order-domain:order-domain-core"))
    api(project(":infrastructure:saga"))
    api(project(":infrastructure:outbox"))
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
