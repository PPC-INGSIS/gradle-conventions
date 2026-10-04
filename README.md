# Gradle Conventions

Plugins de Gradle con la configuración de build que comparten todos los servicios de **Snippet Searcher**.

En vez de copiar las mismas cien líneas en el `build.gradle.kts` de cada servicio, cada uno aplica un plugin y declara solo lo que le es propio.

## Qué resuelve

Todos los servicios se compilan, se testean y se verifican igual: misma versión de Kotlin y de Spring Boot, mismas reglas de formato y de análisis estático, misma cobertura mínima. Sin un lugar común, esa configuración se copia en cada repo y con el tiempo se desincroniza.

Con las convenciones:

- Las versiones se definen **una sola vez**, acá.
- Cambiar una regla es publicar una versión nueva de este repo, no editar cada servicio.
- El build de un servicio queda en pocas líneas, y lo que se lee ahí es lo que ese servicio tiene de particular.

## Plugins

| Plugin | Para qué servicio | Qué aporta |
|---|---|---|
| `ppc.kotlin-service` | Cualquier servicio | Kotlin sobre Java 21, Spring Boot (Web MVC y Actuator), ktlint, detekt, cobertura mínima del 80% con JaCoCo y la instalación de los git hooks |
| `ppc.kotlin-jpa-service` | Servicios que guardan datos en Postgres | Todo lo anterior, más Spring Data JPA, el driver de PostgreSQL, migraciones con Flyway y tests con Testcontainers |

`ppc.kotlin-jpa-service` aplica `ppc.kotlin-service` por dentro: un servicio con base de datos pide un solo plugin.

## Versiones que fija

| Herramienta | Versión |
|---|---|
| Kotlin | 2.3.21 |
| Spring Boot | 4.1.1 |
| Java | 21 |
| ktlint (plugin de Gradle) | 12.1.1 |
| detekt | 1.23.7 |

Las versiones de las librerías de Spring las define el BOM de Spring Boot.

## Cómo usarlo en un servicio

**1. `settings.gradle.kts`**: dónde buscar el plugin.

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/PPC-INGSIS/gradle-conventions")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "mi-servicio"
```

**2. `build.gradle.kts`**: aplicar el plugin y declarar lo propio.

```kotlin
plugins {
    id("ppc.kotlin-jpa-service") version "1.0.0"
}

group = "snippetsearcher"
version = "0.0.1-SNAPSHOT"

dependencies {
    // Solo las dependencias propias de este servicio
}
```

**3. Credenciales.** GitHub Packages pide autenticación para bajar paquetes, aunque sean públicos.

- **En tu máquina:** un token personal de GitHub con el permiso `read:packages`, en `~/.gradle/gradle.properties` (fuera de cualquier repo):

  ```properties
  gpr.user=tu-usuario-de-github
  gpr.key=tu-token
  ```

- **En el CI:** las variables de entorno `GITHUB_ACTOR` y `GITHUB_TOKEN`.

## Qué queda en cada servicio

Estos archivos los leen las herramientas desde la carpeta del proyecto, así que no viajan en el plugin:

| Archivo | Para qué |
|---|---|
| `.editorconfig` | Reglas de ktlint y del IDE |
| `config/detekt/detekt.yml` | Reglas de detekt, como diferencia contra las que trae por defecto |
| `.githooks/` | Los hooks de `pre-commit` y `pre-push`. El plugin los instala al correr `./gradlew check` |

## Desarrollo

```bash
./gradlew build                  # compila los plugins
./gradlew publishToMavenLocal    # los publica en ~/.m2, para probarlos sin subirlos
```

Para probar un cambio en un servicio antes de publicarlo, agregá `mavenLocal()` como primer repositorio en el `pluginManagement` de ese servicio y usá la versión `0.1.0-SNAPSHOT`. No subas ese cambio: `mavenLocal()` existe solo en tu máquina.

## Publicar una versión

1. Mergear los cambios a `main`.
2. Crear un release en GitHub con un tag `vX.Y.Z`.

El workflow `Publish` toma la versión del tag y publica en GitHub Packages. Después, cada servicio actualiza el número de versión en su `build.gradle.kts`.

| Cambio | Versión |
|---|---|
| Arreglo que no cambia el comportamiento | Patch: `1.0.0` → `1.0.1` |
| Regla o herramienta nueva, compatible | Minor: `1.0.0` → `1.1.0` |
| Cambio que obliga a tocar los servicios | Major: `1.0.0` → `2.0.0` |

## Estructura

```
src/main/kotlin/
├── ppc.kotlin-service.gradle.kts       ← el nombre del archivo es el id del plugin
└── ppc.kotlin-jpa-service.gradle.kts
build.gradle.kts                        ← versiones de los plugins que se aplican, y publicación
```
