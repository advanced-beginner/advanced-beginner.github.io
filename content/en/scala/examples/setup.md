---
lastmod: "2026-01-06"
title: Environment Setup
weight: 1
---

Detailed guide to setting up your Scala development environment.

## Install sbt

### Coursier (Recommended)

```bash
# macOS
brew install coursier/formulas/coursier
cs setup

# Linux
curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > cs
chmod +x cs
./cs setup

# Windows
# Run cs setup after installing Coursier
```

### Direct Installation

```bash
# macOS
brew install sbt

# Linux (SDKMAN)
sdk install sbt
```

## Project Structure

```
my-project/
├── build.sbt                 # Build configuration
├── project/
│   ├── build.properties      # sbt version
│   └── plugins.sbt           # sbt plugins
├── src/
│   ├── main/
│   │   ├── scala/            # Main source
│   │   └── resources/        # Resource files
│   └── test/
│       ├── scala/            # Test source
│       └── resources/        # Test resources
└── target/                   # Build output
```

## build.sbt Configuration

### Scala 3

```scala
val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "my-project",
    version := "0.1.0",
    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )
```

### Scala 2.13

```scala
val scala2Version = "2.13.12"

lazy val root = project
  .in(file("."))
  .settings(
    name := "my-project",
    version := "0.1.0",
    scalaVersion := scala2Version,

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.17" % Test
    )
  )
```

### project/build.properties

```properties
sbt.version=1.10.6
```

> **Tip:** Check the latest sbt version at [sbt releases page](https://github.com/sbt/sbt/releases).

## Common sbt Commands

| Command | Description |
|---------|-------------|
| `sbt` | Start sbt shell |
| `compile` | Compile |
| `run` | Run main class |
| `test` | Run tests |
| `console` | Start REPL |
| `clean` | Delete build output |
| `reload` | Reload build.sbt |
| `~compile` | Auto-compile on file changes |
| `~run` | Auto-run on file changes |

### In sbt Shell

```bash
sbt
> compile
> run
> test
> ~compile
```

## IDE Setup

### IntelliJ IDEA

1. Install [IntelliJ IDEA](https://www.jetbrains.com/idea/)
2. **Plugins** → Search "Scala" → Install
3. **File** → **Open** → Select project folder
4. Select "Import as sbt project"
5. Verify JDK settings (Java 11+)

**Useful Shortcuts:**

| Shortcut (macOS) | Function |
|------------------|----------|
| ⌘ + ⇧ + Enter | Auto-complete semicolons/braces |
| ⌥ + Enter | Quick fix, add import |
| ⌘ + B | Go to definition |
| ⌘ + ⌥ + B | Go to implementation |
| ⌘ + ⇧ + T | Go to/create test file |
| ⌃ + ⇧ + R | Run current test |

### VS Code + Metals

1. Install [VS Code](https://code.visualstudio.com/)
2. Search "Metals" in Extensions → Install
3. Open project folder
4. Click "Import build" popup

**Useful Features:**

- Hover: Show type information
- Go to Definition: F12
- Find References: ⇧ + F12
- Rename Symbol: F2

## Adding Dependencies

### Find on Maven Central

Search libraries at [search.maven.org](https://search.maven.org/)

### sbt Format

```scala
libraryDependencies ++= Seq(
  // Regular dependency
  "org.typelevel" %% "cats-core" % "2.10.0",

  // Java library (single %)
  "com.google.guava" % "guava" % "32.1.3-jre",

  // Test only
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,

  // Compile only
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
)
```

### %% vs %

- `%%`: Auto-appends Scala version (e.g., `_3` or `_2.13`)
- `%`: Uses exact artifact name

## Multi-project Build

```scala
lazy val root = project
  .in(file("."))
  .aggregate(core, api)

lazy val core = project
  .in(file("core"))
  .settings(
    name := "my-project-core"
  )

lazy val api = project
  .in(file("api"))
  .dependsOn(core)
  .settings(
    name := "my-project-api"
  )
```

## Useful Plugins

### project/plugins.sbt

```scala
// Check dependency updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")

// Code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")

// Native packaging
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

// Assembly JAR
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.4")
```

## Troubleshooting

### "Unable to find matching Java version"

```bash
# Check Java version
java -version

# Set JAVA_HOME
export JAVA_HOME=/path/to/java
```

### Dependency resolution error

```bash
# Clear cache
rm -rf ~/.sbt/1.0/plugins/target
rm -rf ~/.cache/coursier
sbt clean reload
```

### Red underlines in IntelliJ

1. **File** → **Invalidate Caches** → **Restart**
2. Click **Reload** in sbt tab
3. **Build** → **Rebuild Project**

### "not found: type xxx" compile error

```bash
# Check import statements
# Correct examples:
import scala.collection.mutable.ListBuffer  # Specific type
import scala.collection.mutable.*            # Scala 3
import scala.collection.mutable._            # Scala 2
```

### sbt out of memory

```bash
# Create .sbtopts file (project root)
-J-Xmx4G
-J-XX:+UseG1GC

# Or environment variable
export SBT_OPTS="-Xmx4G -XX:+UseG1GC"
```

### Scala 3 migration errors

```bash
# Common issues:

# 1. Wildcard import change
# Scala 2: import foo._
# Scala 3: import foo.*

# 2. implicit → given/using
# Scala 2: implicit val x: Int = 1
# Scala 3: given x: Int = 1

# 3. Package object deprecation warning
# Use top-level definitions instead of package object
```

### Metals (VS Code) issues

```bash
# Regenerate metadata
rm -rf .metals .bloop .bsp
# Restart VS Code and click "Import build"
```

## Next Steps

- [Basic Examples](../basic/) — Core concept examples
- [Scala 2 vs 3 Comparison](../scala2-vs-scala3/) — Code comparison by version
