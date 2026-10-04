plugins {
    // Permite escribir plugins de Gradle como archivos .gradle.kts
    `kotlin-dsl`
    `maven-publish`
}

group = "ppc"
version = providers.gradleProperty("releaseVersion").getOrElse("0.1.0-SNAPSHOT")

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Los plugins que aplican las convenciones. Acá se fijan sus versiones, una sola vez:
    // los servicios los usan sin versión.
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    implementation("org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:2.3.21")
    implementation("org.jetbrains.kotlin.plugin.jpa:org.jetbrains.kotlin.plugin.jpa.gradle.plugin:2.3.21")
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:4.1.1")
    implementation("org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin:12.1.1")
    implementation("io.gitlab.arturbosch.detekt:io.gitlab.arturbosch.detekt.gradle.plugin:1.23.7")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/PPC-INGSIS/gradle-conventions")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}