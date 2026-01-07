---
lastmod: "2026-01-06"
title: Quick Start
weight: 1
---

Install Scala and run your first program in 5 minutes.

> 🎯 **Run without installation:** Try Scala directly in your browser at [Scastie](https://scastie.scala-lang.org/)!

## 1. Installing Scala

### Install with Coursier (Recommended)

Coursier is the standard installation tool for the Scala ecosystem.

{{< tabs groupid="os" >}}
{{% tab title="macOS" %}}
```bash
# Install Coursier via Homebrew
brew install coursier/formulas/coursier

# Install Scala (Scala 3 is the default)
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

After installation, open a new terminal and verify the installation:

```bash
scala --version
# Scala code runner version 3.x.x

sbt --version
# sbt script version: 1.x.x
```

### Installing Specific Versions

```bash
# Latest Scala 3
cs install scala:3

# Scala 2.13 (needed for Spark, etc.)
cs install scala:2.13.12
```

## 2. Hello World

### Running in REPL

Run code directly in the Scala REPL (Read-Eval-Print Loop):

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

### Running from File

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

## 3. Creating an sbt Project

For real projects, use sbt (Scala Build Tool).

### Project Creation

```bash
# Create new directory
mkdir scala-quickstart && cd scala-quickstart

# Initialize sbt project
sbt new scala/scala3.g8
# Or for Scala 2: sbt new scala/hello-world.g8
```

Enter the project name at the prompt and a basic structure will be generated.

### Manual Project Setup

To set up manually:

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

> 💡 **Tip:** Check the latest sbt version at the [sbt releases page](https://github.com/sbt/sbt/releases).

**src/main/scala/Main.scala** (Scala 3):
```scala
@main def run(): Unit =
  println("Hello from sbt project!")
```

### Running

```bash
sbt run
# [info] running run
# Hello from sbt project!
```

### Common sbt Commands

| Command | Description |
|---------|-------------|
| `sbt run` | Run main class |
| `sbt compile` | Compile |
| `sbt test` | Run tests |
| `sbt console` | Launch REPL (with project dependencies) |
| `sbt ~compile` | Auto-compile on file change |

## 4. IDE Setup

### IntelliJ IDEA (Recommended)

1. Install [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (Community Edition is free)
2. **Plugins** → Search "Scala" → Install
3. **File** → **Open** → Select sbt project folder
4. Choose "Import as sbt project"

### VS Code + Metals

1. Install [VS Code](https://code.visualstudio.com/)
2. Search "Metals" in Extensions → Install
3. Open sbt project folder
4. Click "Import build"

## 5. Simple Examples

> 💻 Try these examples directly at [Scastie](https://scastie.scala-lang.org/)!

### Variables and Types

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

### Function Definition

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

### Collections Preview

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

## Next Steps

You've completed the Quick Start! Proceed to the following:

1. **[Basic Syntax](../concepts/basics/)** — Variables, types, type inference in detail
2. **[Control Structures](../concepts/control-structures/)** — if, for, match expressions
3. **[Functions and Methods](../concepts/functions-methods/)** — Function definition and advanced features

## Troubleshooting

### `scala` command not found

```bash
# Check PATH
echo $PATH | grep coursier

# Reset PATH
source ~/.bashrc  # or ~/.zshrc
```

### sbt is slow

First run may be slow due to dependency downloads. It will be faster from the second run.

```bash
# sbt memory settings (optional)
export SBT_OPTS="-Xmx2G"
```

### Red underlines in IntelliJ

1. **File** → **Invalidate Caches** → **Restart**
2. Or reimport project: **sbt** tab → **Reload**
