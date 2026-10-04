import org.springframework.boot.gradle.plugin.SpringBootPlugin

// Todo lo que comparte cualquier servicio del proyecto: cómo se compila,
// se testea y se verifica. Lo propio de cada servicio queda en su build.
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    jacoco
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Versiones de Spring solo para las dependencias del servicio, no para ktlint ni detekt
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// --- Análisis estático ---

detekt {
    // Cada servicio tiene su config/detekt/detekt.yml
    config.setFrom(file("config/detekt/detekt.yml"))
    // El YAML propio es un diff contra el default, no un reemplazo
    buildUponDefaultConfig = true
}

// --- Git hooks ---

// .git/hooks no se versiona: los hooks viven en .githooks/ y se copian al correr check
tasks.register<Copy>("installGitHooks") {
    from(layout.projectDirectory.dir(".githooks"))
    into(layout.projectDirectory.dir(".git/hooks"))
    // Sin permiso de ejecución, Git ignora el hook sin avisar
    filePermissions { unix("0755") }
}

// --- Cobertura ---

// main() nunca corre en los tests: el test arma el contexto de Spring sin pasar por ahí
val coveredClasses =
    sourceSets.main.get().output.classesDirs.asFileTree.matching {
        exclude("**/*ApplicationKt.class")
    }

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    classDirectories.setFrom(coveredClasses)
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(coveredClasses)
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
    dependsOn("installGitHooks")
}
