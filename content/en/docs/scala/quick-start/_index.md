---
bookCollapseSection: true
lastmod: "2026-01-14"
title: Quick Start
description: "Step-by-step guide to Scala core concepts and practice"
weight: 1
---

> **Target audience**: Java/Kotlin developers new to Scala or developers interested in functional programming
> **Prerequisites**: Basic programming concepts, terminal/command-line experience
> **After reading this**: You'll be able to set up a Scala development environment and run simple code in REPL and sbt projects

{{< callout type="tip" title="TL;DR" >}}
- Install Scala and sbt all at once with Coursier (`cs setup`)
- `val` (immutable), `var` (mutable), `def` (function) are Scala's basic keywords
- sbt is Scala's standard build tool, run projects with `sbt run`
{{< /callout >}}

In 5 minutes, you'll install Scala and run your first program. This guide walks you through the process of setting up an environment ready for practical use, from installation to sbt project creation and IDE setup.

> 🎯 **Run without installation:** You can run Scala directly in your browser at [Scastie](https://scastie.scala-lang.org/)!

**Pre-installation checklist**

| Item | Check command | Expected result |
|------|-------------|-----------|
| Java (optional) | `java -version` | `openjdk version "17.x.x"` (sbt can auto-install) |
| Terminal | - | One of bash, zsh, PowerShell |

## Step 1/5: Install Scala (~2 minutes)

The easiest way to install Scala is to use Coursier. Coursier is the official installation tool that installs Scala, sbt, and various Scala tools all at once.

**Install with Coursier (recommended)**

Coursier is the standard installation tool for the Scala ecosystem.

{{< tabs groupid="os" >}}
{{% tab title="macOS" %}}
```bash
# Install Coursier with Homebrew
brew install coursier/formulas/coursier

# Install Scala (Scala 3 is default)
cs setup
```
{{% /tab %}}
{{% tab title="Linux" %}}
```bash
# Coursier installation script
curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > cs
chmod +x cs
./cs setup
```
{{% /tab %}}
{{% tab title="Windows" %}}
```powershell
# Run in PowerShell
Invoke-WebRequest -Uri "https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-win32.zip" -OutFile "cs.zip"
Expand-Archive -Path "cs.zip"
.\cs\cs.exe setup
```
{{% /tab %}}
{{< /tabs >}}

After installation, open a new terminal and check the version:

```bash
scala --version
# Scala code runner version 3.x.x

sbt --version
# sbt script version: 1.x.x
```

**Install specific version**

You may need to install a specific Scala version based on project requirements. In particular, Scala 2.13 is required when using Apache Spark.

```bash
# Latest Scala 3
cs install scala:3

# Scala 2.13 (required for Spark, etc.)
cs install scala:2.13.12
```

## Step 2/5: Hello World (~1 minute)

The best way to verify Scala's basic syntax is to write a simple Hello World program. You can run it directly in REPL or save it as a file and run it.

**Run in REPL**

Try running code directly in Scala REPL (Read-Eval-Print Loop):

```bash
scala
```

```scala
scala> println("Hello, Scala!")
Hello, Scala!

scala> val name = "World"
val name: String = World

scala> println(s"Hello, $name!")
Hello, World!

scala> :quit
```

**Run from file**

Saving code to a file enables reuse and version control. Scala 3 supports concise indentation-based syntax, and Scala 2 style brace-based syntax can still be used.

**Scala 3 (indentation-based):**

```scala
// hello.scala
@main def hello() =
  val message = "Hello, Scala 3!"
  println(message)
```

```bash
scala hello.scala
# Hello, Scala 3!
```

**Scala 2 style (brace-based):**

```scala
// Hello.scala
object Hello {
  def main(args: Array[String]): Unit = {
    val message = "Hello, Scala 2!"
    println(message)
  }
}
```

```bash
scala Hello.scala
# Hello, Scala 2!
```

## Step 3/5: Create sbt project (~3 minutes)

In real projects, you'll use sbt (Scala Build Tool). sbt is Scala's standard build tool that provides all the features needed for project builds, including dependency management, compilation, testing, and packaging.

**Create project**

```bash
# Create new directory
mkdir scala-quickstart && cd scala-quickstart

# Initialize sbt project
sbt new scala/scala3.g8
# Or for Scala 2: sbt new scala/hello-world.g8
```

Enter the project name at the prompt and the basic structure will be created.

**Manual project configuration**

Setting up a project manually without templates helps you understand the structure better. You only need the build.sbt file and source directory at minimum.

```bash
mkdir -p src/main/scala
```

**build.sbt** (Scala 3):
```scala
val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-quickstart",
    version := "0.1.0",
    scalaVersion := scala3Version
  )
```

**build.sbt** (Scala 2.13):
```scala
val scala2Version = "2.13.12"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-quickstart",
    version := "0.1.0",
    scalaVersion := scala2Version
  )
```

**project/build.properties**:
```properties
sbt.version=1.10.6
```

> 💡 **Tip:** Check the latest sbt version at [sbt releases page](https://github.com/sbt/sbt/releases).

**src/main/scala/Main.scala** (Scala 3):
```scala
@main def run(): Unit =
  println("Hello from sbt project!")
```

**Run**

Running with sbt automatically handles dependency resolution, compilation, and execution.

```bash
sbt run
# [info] running run
# Hello from sbt project!
```

**Commonly used sbt commands**

The table below shows the most frequently used sbt commands in everyday development. Prefixing commands with tilde (~) like `~compile` will automatically re-execute the command when files change.

| Command | Description |
|--------|------|
| `sbt run` | Run main class |
| `sbt compile` | Compile |
| `sbt test` | Run tests |
| `sbt console` | Run REPL (with project dependencies) |
| `sbt ~compile` | Auto-compile on file changes |

## Step 4/5: IDE setup (~2 minutes)

IDE setup is important for efficient Scala development. You'll get code completion, type checking, refactoring support, etc. There are two main options.

**IntelliJ IDEA (recommended)**

1. Install [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (Community Edition is free)
2. **Plugins** → Search "Scala" → Install
3. **File** → **Open** → Select sbt project folder
4. Select "Import as sbt project"

**VS Code + Metals**

VS Code is a lightweight and fast editor that can get Scala Language Server Protocol (LSP) support through the Metals extension.

1. Install [VS Code](https://code.visualstudio.com/)
2. Search for "Metals" in Extensions → Install
3. Open sbt project folder
4. Click "Import build"

## Step 5/5: Simple examples (~2 minutes)

Now that installation is complete, let's explore Scala's basic syntax with simple examples. The code below demonstrates Scala's core features: type inference, immutability, and functional collection operations.

> 💻 Try running the examples below in [Scastie](https://scastie.scala-lang.org/)!

**Variables and types**

```scala
// Immutable (recommended)
val name: String = "Scala"
val year = 2024  // Type inference

// Mutable (only when necessary)
var count = 0
count = count + 1

// Basic types
val number: Int = 42
val pi: Double = 3.14
val isScala: Boolean = true
val char: Char = 'S'
```

**Function definition**

In Scala, functions are defined with the `def` keyword. Return types are mostly inferred, but explicitly specifying them is good practice.

```scala
// Scala 3
def greet(name: String): String =
  s"Hello, $name!"

// Scala 2 style
def add(a: Int, b: Int): Int = {
  a + b
}

// Calling
println(greet("World"))  // Hello, World!
println(add(1, 2))       // 3
```

**Collections preview**

Scala collections demonstrate the power of functional programming. You can declaratively transform data with higher-order functions like `map`, `filter`, `reduce`.

```scala
val numbers = List(1, 2, 3, 4, 5)

// Transform
val doubled = numbers.map(n => n * 2)
// List(2, 4, 6, 8, 10)

// Filter
val evens = numbers.filter(n => n % 2 == 0)
// List(2, 4)

// Reduce
val sum = numbers.reduce(_ + _)
// 15
```

## Next steps

{{< callout type="success" title="🎉 Congratulations!" >}}
You've successfully completed the Scala Quick Start! Now you can:
- Set up a Scala development environment
- Run code in REPL
- Create and manage sbt projects
- Open and work on Scala projects in an IDE
{{< /callout >}}

Now that environment setup is complete, it's time to learn Scala's core concepts one by one.

1. **[Basic Syntax]({{< relref "/docs/scala/concepts/basics" >}})** — Learn variables, types, and type inference in detail
2. **[Control Structures]({{< relref "/docs/scala/concepts/control-structures" >}})** — if, for, match expressions
3. **[Functions and Methods]({{< relref "/docs/scala/concepts/functions-methods" >}})** — Function definition and advanced features

## Troubleshooting

If you encounter problems during installation or execution, refer to the solutions below. Most issues are related to environment variables or cache.

**Cannot find `scala` command**

```bash
# Check PATH
echo $PATH | grep coursier

# Reset PATH
source ~/.bashrc  # or ~/.zshrc
```

**sbt is slow**

The first run may be slow due to dependency downloads. It will be faster from the second run as it uses the cache. If memory shortage is the cause, you can increase JVM memory with the settings below.

```bash
# sbt memory settings (optional)
export SBT_OPTS="-Xmx2G"
```

**Red lines in IntelliJ**

This occurs when IntelliJ's index is corrupted or sbt project settings are not synchronized. Most cases are resolved by invalidating caches.

1. **File** → **Invalidate Caches** → **Restart**
2. Or reimport project: **sbt** tab → **Reload**
