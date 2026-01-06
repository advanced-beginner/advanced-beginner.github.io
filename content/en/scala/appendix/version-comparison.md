---
lastmod: "2026-01-06"
title: Scala 2 vs Scala 3 Version Comparison
weight: 2
---

Summary of major differences between Scala 2 and Scala 3 at a glance.

## New Features (Scala 3)

### Syntax Improvements

| Feature | Scala 2 | Scala 3 |
|---------|---------|---------|
| Block syntax | Braces required | Indentation-based optional |
| if condition | `if (cond)` | `if cond then` |
| for loop | `for (x <- list)` | `for x <- list do` |
| match | Braces required | Indentation-based |
| Wildcard import | `import pkg._` | `import pkg.*` |

### Enumerations

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

### Type System

| Feature | Scala 2 | Scala 3 |
|---------|---------|---------|
| Union Types | Use Either | `A \| B` |
| Intersection Types | `A with B` | `A & B` |
| Opaque Types | Value Class | `opaque type` |
| Match Types | Not possible | Supported |
| Type Lambdas | Complex syntax | `[X] =>> F[X]` |

### Implicit Features

| Feature | Scala 2 | Scala 3 |
|---------|---------|---------|
| Implicit value | `implicit val` | `given` |
| Implicit parameter | `(implicit x: T)` | `(using x: T)` |
| Implicit lookup | `implicitly[T]` | `summon[T]` |
| Extension method | `implicit class` | `extension` |
| Implicit conversion | `implicit def` | `given Conversion` |

### Metaprogramming

| Feature | Scala 2 | Scala 3 |
|---------|---------|---------|
| Inlining | `@inline` (hint) | `inline` (guaranteed) |
| Macro API | scala.reflect | scala.quoted |
| Compile-time ops | Limited | compiletime package |

## Changed Features

### Trait Parameters

```scala
// Scala 3: traits can have parameters
trait Greeting(val name: String):
  def greet(): String = s"Hello, $name!"

class Person extends Greeting("World")

// Not possible in Scala 2 - needs abstract class
```

### Entry Point

```scala
// Scala 3
@main def hello(): Unit = println("Hello!")

@main def greet(name: String): Unit = println(s"Hello, $name!")

// Scala 2
object Hello {
  def main(args: Array[String]): Unit = println("Hello!")
}
```

### Creator Applications

```scala
// Scala 3: create class instances without new
class Person(name: String)
val p = Person("Alice")  // No new needed!

// Scala 2: new required (unless case class)
val p = new Person("Alice")
```

## Dropped Features

The following features have been removed in Scala 3:

| Dropped Feature | Alternative |
|-----------------|-------------|
| Procedural syntax (`def f() { }`) | `def f(): Unit = { }` |
| `do-while` | `while` + condition variable |
| XML literals | Use libraries |
| Symbol literals (`'symbol`) | Strings |
| `DelayedInit` | Regular constructors |
| Auto-apply `()` | Explicit calls |
| `private[this]` | `private` |
| `protected[this]` | `protected` |

## Compatibility

### Using Scala 2 Libraries

Scala 2.13 libraries can be used in Scala 3 projects:

```scala
libraryDependencies += "org.typelevel" % "cats-core_2.13" % "2.10.0"
```

### Cross-building

```scala
// build.sbt
scalaVersion := "3.3.1"
crossScalaVersions := Seq("2.13.12", "3.3.1")
```

### Migration Mode

```scala
// build.sbt
scalacOptions ++= Seq(
  "-source:3.0-migration",
  "-rewrite"
)
```

## Recommendations

### New Projects

- **Scala 3 recommended**: New features, better error messages, improved type inference

### Existing Projects

- **Gradual migration**: Use `-source:3.0-migration` option
- **Check dependencies**: Verify Scala 3 support for major libraries
- **Maintain tests**: Ensure tests pass before and after migration

### Using Spark

- **Keep Scala 2.12/2.13**: Spark doesn't support Scala 3 yet (as of 2024)

## References

- [Scala 3 Migration Guide](https://docs.scala-lang.org/scala3/guides/migration/compatibility-intro.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/)
- [Changed Features](https://docs.scala-lang.org/scala3/reference/changed-features.html)
- [Dropped Features](https://docs.scala-lang.org/scala3/reference/dropped-features.html)
