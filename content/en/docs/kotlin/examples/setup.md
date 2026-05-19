---
title: "Environment Setup Deep Dive"
description: "Covers the core options of build.gradle.kts, dependency patterns, IntelliJ shortcuts, and ktlint integration — the deeper setup topics worth knowing after Quick Start."
weight: 1
lastmod: "2026-05-13"
---

> **Estimated time**: about 10 minutes

{{< callout type="tip" title="TL;DR" >}}
- Understand the meaning of the core blocks in `build.gradle.kts` (plugins, dependencies, kotlin, application)
- Difference between `jvmToolchain(17)` and `sourceCompatibility`
- Tune the collaboration environment with IntelliJ IDEA shortcuts and ktlint integration
- Troubleshooting patterns (class version, dependency refresh, cache invalidation)
{{< /callout >}}

**Target audience**: Developers who have finished the basics of Kotlin environment setup
**Prerequisites**: [Quick Start](../../quick-start/) completed (JDK 17 installation, `gradle init`, first `./gradlew run`)

{{< callout type="info" title="Basic installation is in Quick Start" >}}
First-time setup such as JDK 17 installation, creating a Kotlin DSL project with `gradle init`, and opening IntelliJ IDEA is covered in [Quick Start](../../quick-start/). This page goes one step further — summarizing the core build script options and additional settings useful for collaboration.
{{< /callout >}}

---

#### Step 1 — Core build.gradle.kts Settings

This is a build script written in Kotlin DSL. Let's understand the key options.

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.0.0"   // Kotlin JVM plugin
    application                      // Executable application
}

group = "com.example"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Standard library (auto-added by the kotlin("jvm") plugin)
    // implementation(kotlin("stdlib"))

    // Tests — JUnit 5
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)   // Use JDK 17 — auto-configured without JAVA_HOME
}

application {
    mainClass.set("com.example.AppKt")  // Main class (file name + Kt suffix)
}
```

**Dependency patterns**

```kotlin
dependencies {
    // Runtime dependencies
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Test-only
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.11")

    // Compile-time only
    compileOnly("org.projectlombok:lombok:1.18.32")
}
```

**settings.gradle.kts**

```kotlin
// settings.gradle.kts
rootProject.name = "my-kotlin-project"

// For multi-module
// include("core", "api", "infra")
```

{{< callout type="info" title="jvmToolchain vs sourceCompatibility" >}}
`jvmToolchain(17)` sets the compiler JDK version, bytecode version, and compile source version all at once. `sourceCompatibility = JavaVersion.VERSION_17` only affects Java sources. For Kotlin projects, prefer `jvmToolchain`.
{{< /callout >}}

#### Step 2 — IntelliJ IDEA Collaboration Settings

Opening IntelliJ IDEA for the first time is covered in [Quick Start](../../quick-start/), so here we focus on additional settings useful for collaboration and productivity.

**Verify SDK consistency**

In team projects, mismatched SDKs lead to different build results.

- **File** → **Project Structure** (⌘ + ;) → **SDK**: confirm JDK 17
- Click the "Gradle" icon at the bottom right → **Reload All Gradle Projects** to sync the build script with the IDE index

**Useful shortcuts (macOS)**

| Shortcut | Function |
|----------|----------|
| ⌘ + Shift + O | Quick file open |
| ⌘ + B | Go to definition |
| ⌥ + Enter | Quick fix / add import |
| ⌃ + Shift + R | Run |
| ⌘ + Shift + F | Find in all files |
| ⌃ + T | Refactoring menu |

#### Step 3 — Frequently Used Gradle Commands

Beyond `./gradlew run` from Quick Start, here are commands commonly used in practice.

```bash
# Build + test
./gradlew build

# Run tests only
./gradlew test

# Auto re-run on file changes (useful during development)
./gradlew run --continuous

# Show the dependency tree (see which libraries are pulled in)
./gradlew dependencies

# Rebuild with cache refresh (when dependency resolution fails)
./gradlew build --refresh-dependencies
```

#### Step 4 — Unifying Code Style (Recommended for Teams)

In team projects, use ktlint to enforce a consistent code style.

```kotlin
// Add to build.gradle.kts
plugins {
    kotlin("jvm") version "2.0.0"
    application
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

ktlint {
    version.set("1.3.1")
    android.set(false)
    outputToConsole.set(true)
}
```

```bash
# Style check
./gradlew ktlintCheck

# Auto-fix
./gradlew ktlintFormat
```

#### Troubleshooting

**`java.lang.UnsupportedClassVersionError`**

Occurs when the compile version and runtime JDK version differ. Verify with `java -version` that the runtime is JDK 17.

**`Could not resolve com.example:lib:1.0.0`**

This is likely a dependency cache problem.

```bash
./gradlew build --refresh-dependencies
```

**Import errors in IntelliJ**

Reload the Gradle configuration.

```bash
./gradlew --stop   # Stop the Gradle daemon
```

Then in IntelliJ, **File** → **Invalidate Caches** → **Invalidate and Restart**

{{< callout type="tip" title="Key takeaways" >}}
- JDK 17, `kotlin("jvm") 2.0.0`, `jvmToolchain(17)` — these three are the entirety of the basic setup
- `./gradlew` lets you build without installing Gradle separately
- Open the Gradle project in IntelliJ → Reload — the final step of environment configuration
{{< /callout >}}

#### Next Steps

- [Basic Examples](basic/) — Hello Kotlin, data classes, collection handling
- [Spring Boot Integration](spring-boot-integration/) — Kotlin + Spring Boot REST API
