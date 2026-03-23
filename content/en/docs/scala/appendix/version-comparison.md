---
lastmod: "2026-01-14"
title: Scala 2 vs Scala 3 Version Comparison
weight: 2
---

An overview of the main differences between Scala 2 and Scala 3. Scala 3, released in 2020, provides more concise syntax, a more powerful type system, and improved implicit features.

{{< callout type="tip" title="TL;DR" >}}
- **New Projects**: Scala 3 recommended (more concise syntax, improved type system)
- **Using Spark**: Maintain Scala 2.12/2.13 (Spark doesn't support Scala 3 yet)
- **Key Changes**: `implicit` → `given`/`using`, indentation-based syntax, `enum` added
- **Compatibility**: Scala 3 can use Scala 2.13 libraries
- **Migration**: Gradual transition with `-source:3.0-migration` option
{{< /callout >}}

#### New Features (Scala 3)

Major features added in Scala 3, organized by category.

**Syntax Improvements**

Scala 3 allows optional indentation-based syntax instead of braces, making code more concise. The table below shows the main syntax differences.

| Feature | Scala 2 | Scala 3 |
|---------|---------|---------|
| Block syntax | Braces required | Indentation-based option |
| if condition | `if (cond)` | `if cond then` |
| for loop | `for (x <- list)` | `for x <- list do` |
| match | Braces required | Indentation-based |
| Wildcard import | `import pkg._` | `import pkg.*` |

**Enumerations**

Scala 3's `enum` allows concise definition of enumerations. Scala 2 required a combination of sealed trait and case objects.

```scala
// Scala 3
enum Color:
  case Red, Green, Blue

enum Planet(val mass: Double):
  case Earth extends Planet(5.97e24)

// Scala 2
sealed trait Color
object Color {
  case object Red extends Color
  case object Green extends Color
  case object Blue extends Color
}
```

**Type System**

Scala 3 added powerful type features such as Union Types, Intersection Types, and Match Types. The table below shows the main differences in the type system.

| Feature | Scala 2 | Scala 3 |
|---------|---------|---------|
| Union Types | Use Either | `A \| B` |
| Intersection Types | `A with B` | `A & B` |
| Opaque Types | Value Class | `opaque type` |
| Match Types | Not available | Supported |
| Type Lambdas | Complex syntax | `[X] =>> F[X]` |

**Implicit Features**

Scala 3 redesigned implicit features with `given`/`using`, making intent clearer. The table below shows syntax changes in implicit features.

| Feature | Scala 2 | Scala 3 |
|---------|---------|---------|
| Implicit values | `implicit val` | `given` |
| Implicit parameters | `(implicit x: T)` | `(using x: T)` |
| Implicit lookup | `implicitly[T]` | `summon[T]` |
| Extension methods | `implicit class` | `extension` |
| Implicit conversions | `implicit def` | `given Conversion` |

**Metaprogramming**

Scala 3's macro system was completely redesigned. The `inline` keyword guarantees compile-time inlining, and quotes API enables type-safe macros.

| Feature | Scala 2 | Scala 3 |
|---------|---------|---------|
| Inlining | `@inline` (hint) | `inline` (guaranteed) |
| Macro API | scala.reflect | scala.quoted |
| Compile-time operations | Limited | compiletime package |

{{< callout type="info" title="Key Points" >}}
- **Syntax**: Indentation-based option, `if cond then`, `for x <- list do`
- **enum**: Concise replacement for `sealed trait` + `case object` combination
- **Types**: Union (`|`), Intersection (`&`), Opaque Types added
- **Implicits**: `implicit` → `given`/`using` for clearer intent
{{< /callout >}}

#### Changed Features

Existing features with changed syntax or behavior.

**Trait Parameters**

Traits in Scala 3 can have parameters. Patterns that previously required abstract classes can now be implemented with traits.

```scala
// Scala 3: traits can have parameters
trait Greeting(val name: String):
  def greet(): String = s"Hello, $name!"

class Person extends Greeting("World")

// Scala 2: not possible - abstract class required
```

**Entry Points**

Entry points can be defined simply with the `@main` annotation. Command-line arguments are also parsed in a type-safe manner.

```scala
// Scala 3
@main def hello(): Unit = println("Hello!")

@main def greet(name: String): Unit = println(s"Hello, $name!")

// Scala 2
object Hello {
  def main(args: Array[String]): Unit = println("Hello!")
}
```

**Creator Applications**

Scala 3 allows creating class instances without the `new` keyword. Even non-case classes can be directly called without an apply method.

```scala
// Scala 3: create class instance without new
class Person(name: String)
val p = Person("Alice")  // without new!

// Scala 2: new required (unless case class)
val p = new Person("Alice")
```

{{< callout type="info" title="Key Points" >}}
- **Trait Parameters**: Traits can have constructor parameters in Scala 3
- **@main**: Entry point definition simplified with automatic argument parsing
- **Creator Applications**: Regular classes can create instances without `new`
{{< /callout >}}

#### Removed Features

The following features were removed in Scala 3. Most have better alternatives, and those alternatives should be used during migration.

| Removed Feature | Alternative |
|-----------------|-------------|
| Procedure syntax (`def f() { }`) | `def f(): Unit = { }` |
| `do-while` | `while` + conditional variable |
| XML literals | Use library |
| Symbol literals (`'symbol`) | String |
| `DelayedInit` | Regular constructor |
| Auto-application of `()` | Explicit call |
| `private[this]` | `private` |
| `protected[this]` | `protected` |

{{< callout type="info" title="Key Points" >}}
- **Procedure syntax** (`def f() { }`): Explicitly use `: Unit =`
- **XML literals**: Replaced with separate library
- **Symbol literals** (`'symbol`): Use strings
{{< /callout >}}

#### Compatibility

Scala 3 provides binary compatibility with Scala 2.13. Existing libraries can be used as-is, reducing migration burden.

**Using Scala 2 Libraries**

Scala 2.13 libraries can be directly added as dependencies in Scala 3 projects.

Scala 2.13 libraries can be used in Scala 3 projects:

```scala
libraryDependencies += "org.typelevel" % "cats-core_2.13" % "2.10.0"
```

**Cross-building**

When developing libraries, multiple Scala versions can be supported simultaneously.

```scala
// build.sbt
scalaVersion := "3.3.1"
crossScalaVersions := Seq("2.13.12", "3.3.1")
```

**Migration Mode**

Using the `-source:3.0-migration` option allows gradual migration of Scala 2 code. When used with the `-rewrite` option, some transformations are applied automatically.

```scala
// build.sbt
scalacOptions ++= Seq(
  "-source:3.0-migration",
  "-rewrite"
)
```

{{< callout type="info" title="Key Points" >}}
- **Binary Compatibility**: Scala 2.13 libraries can be used directly in Scala 3
- **Cross-building**: Support multiple versions simultaneously with `crossScalaVersions`
- **Migration Mode**: Automatic conversion with `-source:3.0-migration -rewrite`
{{< /callout >}}

#### Recommendations

Guidelines for version selection.

**New Projects**

- **Scala 3 Recommended**: New features, better error messages, improved type inference

**Existing Projects**

Migration strategy for existing Scala 2 projects.

- **Gradual Migration**: Use `-source:3.0-migration` option
- **Check Dependencies**: Verify Scala 3 support of major libraries
- **Maintain Tests**: Ensure tests pass before and after migration

**Using Spark**

Projects using Apache Spark must maintain Scala 2.

- **Maintain Scala 2.12/2.13**: Spark doesn't support Scala 3 yet (as of 2024)

{{< callout type="info" title="Key Points" >}}
- **New Projects**: Choose Scala 3 (better syntax, error messages, type inference)
- **Existing Projects**: Gradual migration, verify dependency Scala 3 support
- **Spark**: Must maintain Scala 2, waiting for Scala 3 support
{{< /callout >}}

#### References

Official migration guide and reference documentation.

- [Scala 3 Migration Guide](https://docs.scala-lang.org/scala3/guides/migration/compatibility-intro.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/)
- [Changed Features](https://docs.scala-lang.org/scala3/reference/changed-features.html)
- [Dropped Features](https://docs.scala-lang.org/scala3/reference/dropped-features.html)
