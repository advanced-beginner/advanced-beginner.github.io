---
title: "Gradle Kotlin DSL Tips"
weight: 3
description: "Practical know-how for Gradle Kotlin DSL: Version Catalog, buildSrc, convention plugins, and common patterns for multi-module projects."
lastmod: "2026-05-13"
---

Practical know-how and frequently used patterns for using Gradle Kotlin DSL effectively.

**Estimated time**: about 10-15 minutes

{{< callout type="tip" title="TL;DR" >}}
- Manage all dependency versions in one place with `libs.versions.toml` (Version Catalog).
- Reuse build logic via `buildSrc` or composite builds.
- The `plugins { }` block is the modern alternative to `apply(plugin = "...")`.
- Keep multi-module configuration DRY with convention plugins.
{{< /callout >}}

---

## What This Guide Solves

Use this guide in the following situations:

- When you keep copy-pasting the same Gradle configuration across modules
- When dependency versions are scattered across files and updates are tedious
- When you're migrating from Groovy DSL to Kotlin DSL and the syntax is confusing

---

## Step 1: Set Up Version Catalog (libs.versions.toml)

`gradle/libs.versions.toml` is the centralized dependency-version file supported since Gradle 7.4.

**File structure:**

```toml
# gradle/libs.versions.toml

[versions]
kotlin         = "2.0.0"
springBoot     = "3.2.5"
coroutines     = "1.8.1"
serialization  = "1.6.3"
ktor           = "2.3.10"
kotest         = "5.8.1"
mockk          = "1.13.10"

[libraries]
kotlin-stdlib       = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
kotlin-reflect      = { module = "org.jetbrains.kotlin:kotlin-reflect", version.ref = "kotlin" }
coroutines-core     = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-reactor  = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor", version.ref = "coroutines" }
coroutines-debug    = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-debug", version.ref = "coroutines" }
serialization-json  = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
spring-boot-web     = { module = "org.springframework.boot:spring-boot-starter-web" }
spring-boot-webflux = { module = "org.springframework.boot:spring-boot-starter-webflux" }
spring-boot-test    = { module = "org.springframework.boot:spring-boot-starter-test" }
kotest-runner       = { module = "io.kotest:kotest-runner-junit5", version.ref = "kotest" }
kotest-assertions   = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }
mockk               = { module = "io.mockk:mockk", version.ref = "mockk" }

[bundles]
# Bundle multiple libraries together
testing = ["kotest-runner", "kotest-assertions", "mockk"]

[plugins]
kotlin-jvm          = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-spring       = { id = "org.jetbrains.kotlin.plugin.spring", version.ref = "kotlin" }
spring-boot         = { id = "org.springframework.boot", version.ref = "springBoot" }
spring-dependency   = { id = "io.spring.dependency-management", version = "1.1.5" }
```

**How to use (build.gradle.kts):**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.serialization.json)
    implementation(libs.spring.boot.web)

    // Use a bundle
    testImplementation(libs.bundles.testing)
    testImplementation(libs.spring.boot.test)
}
```

---

## Step 2: plugins Block vs apply

```kotlin
// Modern (recommended): plugins { } block
// - Classpath is configured automatically
// - Trackable in build scans
// - IDs and versions managed in one place
plugins {
    kotlin("jvm") version "2.0.0"
    id("org.springframework.boot") version "3.2.5"
}

// Legacy: apply
// - Requires buildscript { classpath } first
// - Less clear error messages
buildscript {
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:3.2.5")
    }
}
apply(plugin = "org.springframework.boot")
```

**Applying plugins in subprojects without specifying versions:**

```kotlin
// Declare versions in settings.gradle.kts and reuse without versions in subprojects
plugins {
    id("org.springframework.boot") apply false  // Declare version only, apply in subproject
}

// subproject/build.gradle.kts
plugins {
    id("org.springframework.boot")  // Version can be omitted
}
```

---

## Step 3: Share Build Logic with buildSrc

`buildSrc` is a special directory that holds Gradle build code shared by all modules.

**Directory structure:**

```text
project root/
├── buildSrc/
│   ├── build.gradle.kts        # buildSrc's own build configuration
│   └── src/main/kotlin/
│       ├── kotlin-base.gradle.kts   # Convention plugin for shared Kotlin settings
│       └── spring-app.gradle.kts   # Convention plugin for Spring Boot apps
├── api-module/
│   └── build.gradle.kts
└── domain-module/
    └── build.gradle.kts
```

**buildSrc/build.gradle.kts:**

```kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.2.5")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.5")
}
```

**Convention plugin — kotlin-base.gradle.kts:**

```kotlin
// buildSrc/src/main/kotlin/kotlin-base.gradle.kts
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
}

repositories {
    mavenCentral()
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-Dkotlinx.coroutines.debug")
}
```

**Convention plugin — spring-app.gradle.kts:**

```kotlin
// buildSrc/src/main/kotlin/spring-app.gradle.kts
plugins {
    id("kotlin-base")  // Reuse the convention plugin defined above
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}
```

**Use in a subproject:**

```kotlin
// api-module/build.gradle.kts
plugins {
    id("spring-app")  // Apply the full Spring Boot app setup in one line
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
}
```

---

## Step 4: Common Task Patterns

**Separating tests (unit / integration):**

```kotlin
// build.gradle.kts
val integrationTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "Run integration tests"
    group = "verification"
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    useJUnitPlatform()

    // Integration tests are slow; disable cache
    outputs.upToDateWhen { false }
}

// Exclude integration tests from regular tests
tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }
}
```

**JAR options:**

```kotlin
// Executable JAR settings
tasks.bootJar {
    archiveFileName.set("my-app.jar")
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}

// Create a fat JAR (without Spring Boot)
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.example.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
```

**Custom tasks:**

```kotlin
// Code quality check task
tasks.register("codeQuality") {
    dependsOn("ktlintCheck", "detekt", "test")
    description = "Run full code quality checks"
    group = "verification"
}

// Copy environment-specific application.yml
tasks.register<Copy>("copyConfig") {
    val env = project.findProperty("env") as? String ?: "local"
    from("src/main/resources/config/$env")
    into("src/main/resources")
    description = "Apply environment-specific config (env=$env)"
}
```

---

## Step 5: Multi-Module Configuration

**settings.gradle.kts:**

```kotlin
rootProject.name = "my-project"

// Enable Version Catalog
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":domain",
    ":application",
    ":infrastructure",
    ":api",
    ":batch"
)
```

**Root build.gradle.kts:**

```kotlin
// Apply shared settings to all subprojects
subprojects {
    group = "com.example"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}
```

**Module dependencies (using type-safe accessors):**

```kotlin
// api/build.gradle.kts
dependencies {
    implementation(projects.domain)       // :domain module (type-safe)
    implementation(projects.application)  // :application module
    // String version: implementation(project(":domain"))
}
```

---

## Major Differences: Groovy DSL → Kotlin DSL

| Groovy DSL | Kotlin DSL | Description |
|-----------|-----------|-------------|
| `apply plugin: 'java'` | `plugins { java }` | Apply plugin |
| `'groupId:artifactId:1.0'` | `"groupId:artifactId:1.0"` | Strings use double quotes |
| `sourceCompatibility = 17` | `java { sourceCompatibility = JavaVersion.VERSION_17 }` | Java version |
| `test { useJUnitPlatform() }` | `tasks.test { useJUnitPlatform() }` | Task configuration |
| `def myVar = ...` | `val myVar = ...` | Variable declaration |
| `ext.myProp = 'value'` | `extra["myProp"] = "value"` | Extra properties |

---

## Checklist

- [ ] Are all dependency versions defined in `libs.versions.toml`?
- [ ] Is shared build configuration extracted into `buildSrc` convention plugins?
- [ ] Are you using the `plugins { }` block instead of `apply(plugin = "...")`?
- [ ] Are module-to-module dependencies using type-safe accessors (`projects.xxx`)?
- [ ] Are unit tests and integration tests separated?

---

## Related Documents

- [Multiplatform Overview](../concepts/multiplatform-overview/) — KMP Gradle configuration
- [Kotest vs JUnit](kotest-vs-junit/) — test framework Gradle configuration
- [Performance Profiling](performance-profiling/) — measuring Gradle build performance
