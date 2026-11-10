// Un servicio que además guarda datos en Postgres: suma JPA, las migraciones
// con Flyway y los tests con una base real levantada por Testcontainers.
plugins {
    id("ppc.kotlin-service")
    kotlin("plugin.jpa")
}

// JPA necesita heredar de las entidades, y en Kotlin las clases son final por defecto
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
}

// --- Zona horaria ---

// El driver JDBC le manda a Postgres la zona del JVM al abrir la conexión. Java en
// Windows resuelve la de Argentina al alias viejo "America/Buenos_Aires", que
// Postgres 18 ya no acepta, y la conexión falla antes de correr un solo test.
// Más allá del bug: un test no debería depender de la zona de la máquina que lo corre.
tasks.withType<Test>().configureEach {
    systemProperty("user.timezone", "America/Argentina/Buenos_Aires")
}
