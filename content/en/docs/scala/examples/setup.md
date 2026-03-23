---
lastmod: "2026-01-14"
title: Environment Setup
description: "Step-by-step Scala development environment setup"
weight: 1
---

{{< callout type="info" title="TL;DR" >}}
- Install JDK, sbt, and Scala CLI all at once with **Coursier**
- **sbt** is Scala's standard build tool for compilation, testing, and dependency management
- Configure IDE with **IntelliJ IDEA** or **VS Code + Metals**
- Project structure: `build.sbt` (configuration) + `src/main/scala/` (source) + `src/test/scala/` (tests)
{{< /callout >}}

**Target Audience**: Developers new to Scala programming

**Prerequisites**:
- Basic terminal/command-line usage
- Java or other JVM language experience (recommended)
- IDE usage experience

---

This document provides detailed instructions for setting up a Scala development environment. To start Scala programming, you need JDK, sbt (Scala Build Tool), and an IDE. This guide walks through the installation and configuration process step by step.

#### Installing sbt

sbt is Scala's standard build tool. It handles project compilation, test execution, and dependency management. Installation via Coursier is recommended, and you can choose the appropriate installation method for your operating system.

**Coursier (Recommended)**

Coursier is a universal installation tool for managing Scala tools. A single `cs setup` command automatically installs all necessary tools including JDK, sbt, and Scala CLI.

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

**Direct Installation**

You can also install sbt directly through package managers instead of Coursier. In this case, you need to install JDK separately.

```bash
# macOS
brew install sbt

# Linux (SDKMAN)
sdk install sbt
```

{{< callout type="tip" title="Key Points" >}}
- Coursier (`cs setup`) is the most convenient method for automatically installing all required tools
- Direct installation requires separate JDK installation
{{< /callout >}}

#### Project Structure

sbt projects follow a defined directory structure. Understanding this structure helps you easily identify the role of files within a project. Below is a typical sbt project directory structure.

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
└── target/                   # Build artifacts
```

`build.sbt` is the core configuration file that defines project name, Scala version, and dependencies. The `project/` directory contains meta-configuration for the build itself. `src/main/scala/` contains application source code, while `src/test/scala/` contains test code. The `target/` directory is where compiled artifacts are generated and should be excluded from version control.

{{< callout type="tip" title="Key Points" >}}
- `build.sbt`: Project metadata and dependency definitions
- `src/main/scala/`: Main source code location
- `src/test/scala/`: Test code location
- `target/`: Build artifacts (add to `.gitignore`)
{{< /callout >}}

#### build.sbt Configuration

Configure basic project information and dependencies in the build.sbt file. The configuration differs slightly between Scala 3 and Scala 2.13, so apply settings appropriate for your version.

**Scala 3**

Basic build.sbt configuration for a Scala 3 project. Uses MUnit as the test framework.

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

**Scala 2.13**

Basic build.sbt configuration for a Scala 2.13 project. Uses ScalaTest as the test framework.

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

**project/build.properties**

This file specifies the sbt version to use for the project. It ensures team members use the same sbt version and must be included in version control.

```properties
sbt.version=1.10.6
```

> 💡 **Tip:** Check the latest sbt version at the [sbt releases page](https://github.com/sbt/sbt/releases).

{{< callout type="tip" title="Key Points" >}}
- Scala 3: `scalaVersion := "3.3.1"`, MUnit test framework recommended
- Scala 2.13: `scalaVersion := "2.13.12"`, ScalaTest framework recommended
- Specify sbt version in `project/build.properties` for team consistency
{{< /callout >}}

#### Commonly Used sbt Commands

Commands used in the sbt shell. Running sbt enters an interactive shell where you can execute various commands. The table below summarizes the most frequently used commands.

| Command | Description |
|---------|-------------|
| `sbt` | Start sbt shell |
| `compile` | Compile |
| `run` | Run main class |
| `test` | Run tests |
| `console` | Start REPL |
| `clean` | Delete build artifacts |
| `reload` | Reload build.sbt |
| `~compile` | Auto-compile on file changes |
| `~run` | Auto-run on file changes |

Commands with the `~` prefix automatically repeat the task whenever files change. Useful for immediately seeing results while modifying code during development.

**In sbt Shell**

Running `sbt` in the terminal enters the interactive shell. Commands execute faster within the shell.

```bash
sbt
> compile
> run
> test
> ~compile
```

{{< callout type="tip" title="Key Points" >}}
- `compile`, `run`, `test`: Most frequently used basic commands
- `~` prefix: Auto-rerun on file change detection (essential during development)
- Command execution faster within sbt shell
{{< /callout >}}

#### IDE Setup

IntelliJ IDEA or VS Code are commonly used for Scala development. Both IDEs provide excellent Scala support, so choose the tool you're comfortable with.

**IntelliJ IDEA**

IntelliJ IDEA is the most complete Scala IDE. JetBrains provides an official Scala plugin.

1. Install [IntelliJ IDEA](https://www.jetbrains.com/idea/)
2. **Plugins** → Search "Scala" → Install
3. **File** → **Open** → Select project folder
4. Choose "Import as sbt project"
5. Verify JDK settings (Java 11+)

Knowing frequently used shortcuts for Scala development in IntelliJ significantly improves productivity. Below are shortcuts for macOS.

| Shortcut (macOS) | Function |
|------------------|----------|
| ⌘ + ⇧ + Enter | Auto-complete semicolons/parentheses |
| ⌥ + Enter | Quick fix, add import |
| ⌘ + B | Go to definition |
| ⌘ + ⌥ + B | Go to implementation |
| ⌘ + ⇧ + T | Go to/create test file |
| ⌃ + ⇧ + R | Run current test |

**VS Code + Metals**

VS Code is a lightweight and fast editor that supports Scala through the Metals extension. Free to use with simple setup.

1. Install [VS Code](https://code.visualstudio.com/)
2. Search "Metals" in Extensions → Install
3. Open project folder
4. Click "Import build" popup

Useful features provided by Metals: Hover for type information, F12 to go to definition, ⇧ + F12 to find references, F2 to rename symbols.

{{< callout type="tip" title="Key Points" >}}
- **IntelliJ IDEA**: Most complete Scala IDE, requires Scala plugin installation
- **VS Code + Metals**: Lightweight and fast, free, configure project by clicking "Import build"
- Both provide excellent support - choose the familiar tool
{{< /callout >}}

#### Adding Dependencies

To add external libraries to your project, add dependencies to libraryDependencies in build.sbt. You can search for desired libraries on Maven Central and copy them in sbt format.

**Finding on Maven Central**

Search for desired libraries at [search.maven.org](https://search.maven.org/) to get sbt format dependency declarations.

**sbt Format**

Dependencies consist of three parts: groupID, artifactID, and version. Add `% Test` for test-only dependencies and `% Provided` for compile-only.

```scala
libraryDependencies ++= Seq(
  // Regular dependency
  "org.typelevel" %% "cats-core" % "2.10.0",

  // Java library (single %)
  "com.google.guava" % "guava" % "32.1.3-jre",

  // Test-only
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,

  // Compile-only
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
)
```

**%% vs %**

Understanding the difference between `%%` and `%` is important. Using `%%` automatically appends the Scala version to the artifact name. For example, with Scala 3 it becomes `cats-core_3`, and with Scala 2.13 it becomes `cats-core_2.13`. Java libraries are version-independent, so use single `%`.

{{< callout type="tip" title="Key Points" >}}
- `%%`: Scala libraries (Scala version automatically appended)
- `%`: Java libraries (version-independent)
- `% Test`: Test-only, `% Provided`: Compile-only
{{< /callout >}}

#### Multi-Project

In large projects, code is separated into multiple sub-projects. sbt natively supports multi-project builds and makes it easy to configure inter-project dependencies.

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

In this example, the `root` project aggregates `core` and `api`. The `api` project depends on (`dependsOn`) `core`, allowing it to use `core`'s classes.

{{< callout type="tip" title="Key Points" >}}
- `aggregate`: Build multiple sub-projects at once
- `dependsOn`: Enable usage of other project's classes
- Useful for code separation in large projects
{{< /callout >}}

#### Useful Plugins

sbt plugins extend build functionality. Add plugins to the `project/plugins.sbt` file.

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

sbt-updates checks for new versions of dependencies. sbt-scalafmt formats code to a consistent style. sbt-native-packager generates Docker images or native packages, and sbt-assembly creates a single JAR file with all dependencies.

{{< callout type="tip" title="Key Points" >}}
- **sbt-updates**: Check dependency updates
- **sbt-scalafmt**: Auto-format code
- **sbt-assembly**: Generate single JAR for deployment
- Add plugins to `project/plugins.sbt`
{{< /callout >}}

#### Troubleshooting

Common problems and solutions that may occur during environment setup.

**"Unable to find matching Java version"**

Occurs when JDK is not installed or JAVA_HOME environment variable is not set.

```bash
# Check Java version
java -version

# Set JAVA_HOME
export JAVA_HOME=/path/to/java
```

**Dependency Resolution Errors**

Occurs when cache is corrupted or dependency information is outdated. Most issues are resolved by clearing cache and reloading.

```bash
# Clear cache
rm -rf ~/.sbt/1.0/plugins/target
rm -rf ~/.cache/coursier
sbt clean reload
```

**Red Lines in IntelliJ**

Occurs when IntelliJ doesn't properly recognize project structure. Resolved by invalidating caches and rebuilding the project.

1. **File** → **Invalidate Caches** → **Restart**
2. **Reload** in sbt tab
3. **Build** → **Rebuild Project**

**"not found: type xxx" Compile Errors**

Occurs when required import statements are missing. Note that wildcard import syntax differs between Scala 3 and 2.

```bash
# Check import statements
# Correct examples:
import scala.collection.mutable.ListBuffer  # Specific type
import scala.collection.mutable.*            # Scala 3
import scala.collection.mutable._            # Scala 2
```

**sbt Out of Memory**

Large projects may run out of memory during compilation. Create a `.sbtopts` file in the project root to adjust memory settings.

```bash
# Create .sbtopts file (project root)
-J-Xmx4G
-J-XX:+UseG1GC

# Or environment variable
export SBT_OPTS="-Xmx4G -XX:+UseG1GC"
```

**Scala 3 Migration Errors**

Common issues when migrating Scala 2 projects to Scala 3. Check syntax changes and modify code accordingly.

```bash
# Common issues:

# 1. Wildcard import change
# Scala 2: import foo._
# Scala 3: import foo.*

# 2. implicit → given/using
# Scala 2: implicit val x: Int = 1
# Scala 3: given x: Int = 1

# 3. Package object deprecation warnings
# Recommended to use top-level definitions instead of package object
```

**Metals (VS Code) Issues**

If Metals doesn't properly recognize the project in VS Code, deleting metadata and reimporting resolves it.

```bash
# Regenerate metadata
rm -rf .metals .bloop .bsp
# Restart VS Code and click "Import build"
```

{{< callout type="tip" title="Key Points" >}}
- **JDK issues**: Check `java -version`, set `JAVA_HOME`
- **Dependency issues**: Clear cache (`rm -rf ~/.cache/coursier`)
- **IDE issues**: Invalidate caches and rebuild project
- **Out of memory**: Add `-J-Xmx4G` to `.sbtopts`
{{< /callout >}}

#### Next Steps

Once environment setup is complete, practice Scala's core concepts through basic examples.

- [Basic Examples](basic/) — Examples using core concepts
- [Scala 2 vs 3 Comparison](scala2-vs-scala3/) — Version-specific code comparison
