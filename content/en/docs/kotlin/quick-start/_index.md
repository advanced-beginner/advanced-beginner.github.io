---
bookCollapseSection: true
lastmod: "2026-05-13"
title: Quick Start
description: "Set up a Kotlin development environment and run your first program."
weight: 1
---

> **Target Audience**: JVM developers new to Kotlin, or developers preparing to enter Android/backend development
> **Prerequisites**: Basic programming concepts, experience using the terminal/command line
> **After Reading This**: You can set up a Kotlin development environment and run simple code in the REPL and Gradle projects

{{< callout type="tip" title="TL;DR" >}}
- Install JDK 17 and Kotlin via SDKMAN or Homebrew
- `val` (immutable), `var` (mutable), and `fun` (function) are Kotlin's basic keywords
- Gradle Kotlin DSL is the de facto standard build tool (`./gradlew run`)
- Develop with IntelliJ IDEA Community or VS Code + Kotlin extension
{{< /callout >}}

Install Kotlin and run your first program in 5 minutes. This guide walks you through everything from installation to creating a Gradle project and setting up your IDE — a ready-to-use environment for real work.

> 🎯 **Run Without Installing:** Run Kotlin directly in your browser at [Kotlin Playground](https://play.kotlinlang.org/).

**Pre-flight Check**

| Item | Check Command | Expected Result |
|------|---------------|-----------------|
| Java | `java -version` | `openjdk version "17.x.x"` or higher |
| Terminal | - | bash, zsh, or PowerShell |

## Step 1/5: Install Kotlin (~2 min)

The easiest way to install Kotlin is to use SDKMAN (macOS/Linux) or Homebrew. Both tools make it easy to manage JDK and Kotlin versions.

**Install via SDKMAN (Recommended for macOS/Linux/WSL)**

SDKMAN is the de facto standard for managing JVM ecosystem tool versions.

```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install JDK 17
sdk install java 17.0.10-tem

# Install Kotlin
sdk install kotlin
```

**Install via Homebrew (macOS)**

If you use Homebrew, install with the following command.

```bash
brew install openjdk@17 kotlin
```

**Windows (Scoop)**

```powershell
scoop install openjdk17 kotlin
```

After installation, open a new terminal and verify the versions.

```bash
java -version
# openjdk version "17.x.x"

kotlin -version
# Kotlin version 2.x.x
```

## Step 2/5: Hello World (~1 min)

The best way to verify Kotlin's basic syntax is to write a simple Hello World program. You can run it directly in the REPL or save it to a file.

**Run in the REPL**

Run code directly with the Kotlin REPL (Read-Eval-Print Loop).

```bash
kotlinc
```

```kotlin
>>> println("Hello, Kotlin!")
Hello, Kotlin!

>>> val name = "World"
>>> println("Hello, $name!")
Hello, World!

>>> :quit
```

**Run from a File**

Saving code to a file enables reuse and version control.

```kotlin
// hello.kt
fun main() {
    val message = "Hello, Kotlin!"
    println(message)
}
```

```bash
# Compile + run
kotlinc hello.kt -include-runtime -d hello.jar
java -jar hello.jar
# Hello, Kotlin!

# Or run as a script directly (.kts)
kotlin hello.kt
```

## Step 3/5: Create a Gradle Project (~3 min)

Real projects use Gradle Kotlin DSL. Gradle provides everything needed for project builds — dependency management, compilation, testing, and packaging.

**Create the Project**

```bash
mkdir kotlin-quickstart && cd kotlin-quickstart

# Create a Kotlin application project with gradle init
gradle init \
  --type kotlin-application \
  --dsl kotlin \
  --test-framework junit-jupiter \
  --package com.example \
  --project-name kotlin-quickstart
```

The generated directory has the following structure.

```text
kotlin-quickstart/
├── build.gradle.kts            # Build script (Kotlin DSL)
├── settings.gradle.kts
├── gradle/
│   └── wrapper/
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/kotlin/com/example/App.kt
        └── test/kotlin/com/example/AppTest.kt
```

**Manual Project Setup**

Setting it up manually (without a template) helps you better understand the structure. At minimum, you only need a `build.gradle.kts` and a source directory.

```bash
mkdir -p src/main/kotlin
```

**build.gradle.kts**:

```kotlin
plugins {
    kotlin("jvm") version "2.0.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}

kotlin {
    jvmToolchain(17)
}
```

**src/main/kotlin/Main.kt**:

```kotlin
fun main() {
    println("Hello from Gradle project!")
}
```

**Run It**

Running the project with the Gradle Wrapper handles dependency resolution, compilation, and execution automatically.

```bash
./gradlew run
# > Task :run
# Hello from Gradle project!
```

**Frequently Used Gradle Commands**

The table below lists commands most frequently used in daily development. Adding the `--continuous` flag watches for file changes and automatically re-runs.

| Command | Description |
|---------|-------------|
| `./gradlew run` | Run the main class |
| `./gradlew build` | Compile + test + JAR packaging |
| `./gradlew test` | Run tests |
| `./gradlew clean` | Delete build artifacts |
| `./gradlew run --continuous` | Auto-run on file changes |

## Step 4/5: IDE Setup (~2 min)

A proper IDE setup is essential for efficient Kotlin development. You get code completion, type checking, and refactoring support.

**IntelliJ IDEA (Recommended)**

JetBrains, the creator of Kotlin, makes IntelliJ IDEA — which offers the best Kotlin support.

1. Install [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (Community Edition is free)
2. **File** → **Open** → select your Gradle project folder
3. Click "Trust Project" → Gradle sync runs automatically

The Kotlin plugin is bundled by default, so no extra installation is needed.

**VS Code + Kotlin Extension**

VS Code is a lightweight, fast editor that supports Kotlin via the Kotlin Language Server (LSP).

1. Install [VS Code](https://code.visualstudio.com/)
2. Search for "Kotlin Language" in Extensions → install
3. Open your Gradle project folder

## Step 5/5: A Simple Example (~2 min)

Now that installation is complete, let's look at Kotlin's basic syntax with simple examples. The code below demonstrates Kotlin's core features: type inference, Null Safety, and functional collection operations.

> 💻 Try running the examples below directly in [Kotlin Playground](https://play.kotlinlang.org/).

**Variables and Types**

```kotlin
// Immutable (recommended)
val name: String = "Kotlin"
val year = 2026  // Type inference

// Mutable (only when needed)
var count = 0
count = count + 1

// Basic types
val number: Int = 42
val pi: Double = 3.14
val isKotlin: Boolean = true
val char: Char = 'K'
```

**Function Definitions**

In Kotlin, functions are defined with the `fun` keyword. Using an expression body (`= value`) makes them more concise.

```kotlin
// Expression body
fun greet(name: String): String = "Hello, $name!"

// Block body
fun add(a: Int, b: Int): Int {
    return a + b
}

// Calls
println(greet("World"))  // Hello, World!
println(add(1, 2))       // 3
```

**Null Safety**

Kotlin's biggest feature is null safety. You must append `?` to a type to allow null.

```kotlin
val nonNull: String = "value"
// nonNull = null  // Compile error

val nullable: String? = null
val length = nullable?.length ?: 0  // Safe call + Elvis operator
println(length)  // 0
```

**A Taste of Collections**

Kotlin collections strongly support functional programming style. Higher-order functions like `map`, `filter`, and `reduce` let you transform data declaratively.

{{< callout type="info" title="What is `{ it * 2 }`?" >}}
It's the shorthand form of a single-parameter lambda (anonymous function). `it` refers to that parameter. Detailed lambda syntax is covered in [Functions](../concepts/functions/).
{{< /callout >}}

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// Transform
val doubled = numbers.map { it * 2 }
// [2, 4, 6, 8, 10]

// Filter
val evens = numbers.filter { it % 2 == 0 }
// [2, 4]

// Reduce
val sum = numbers.reduce { acc, n -> acc + n }
// 15
```

## Next Steps

{{< callout type="success" title="🎉 Congratulations!" >}}
You've successfully completed the Kotlin Quick Start. You now:
- Have a Kotlin development environment set up
- Can run code in the REPL
- Can create and manage Gradle Kotlin DSL projects
- Can open and work with Kotlin projects in an IDE
{{< /callout >}}

Now that your environment is ready, it's time to learn Kotlin's core concepts one by one.

1. **[Basic Syntax](../concepts/basics/)** — Expression-based syntax, package organization
2. **[Variables and Types](../concepts/variables-types/)** — `val`/`var`, type inference, basic types
3. **[Null Safety](../concepts/null-safety/)** — Kotlin's core safety feature
4. **[Functions](../concepts/functions/)** — Function definitions, lambdas, higher-order functions

## Troubleshooting

If you run into issues during installation or execution, refer to the solutions below.

**Cannot find `kotlin` or `kotlinc` command**

```bash
# Check PATH
echo $PATH | grep -E "sdkman|kotlin"

# Re-initialize PATH if using SDKMAN
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

**Permission error when running Gradle Wrapper (macOS/Linux)**

```bash
chmod +x gradlew
./gradlew --version
```

**Red underlines in IntelliJ**

Occurs when IntelliJ's index is corrupted or Gradle sync has broken.

1. **File** → **Invalidate Caches** → **Restart**
2. Or click **Reload All Gradle Projects** in the Gradle tab
