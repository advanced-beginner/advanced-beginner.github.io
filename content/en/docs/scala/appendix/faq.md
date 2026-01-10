---
lastmod: "2026-01-06"
title: FAQ
weight: 3
---

Frequently asked questions and answers about Scala.

## General

### Should I learn Scala?

**Yes.** Scala is particularly useful in the following cases:

- Large-scale data processing (Apache Spark)
- Distributed systems (Akka)
- Projects where type safety is important
- When you want to learn functional programming

### Should I learn Scala 2 or Scala 3?

**It depends on your situation:**

- **New projects**: Scala 3 recommended
- **Using Spark**: Scala 2.12/2.13 (Spark doesn't support Scala 3 yet)
- **Maintaining existing projects**: Match the project version

The core concepts are the same in both versions, so learning one makes it easy to adapt to the other.

### Is Scala difficult?

**The basics are not difficult.** If you have Java experience, you can quickly learn the basic syntax.

**Difficult parts:**
- Advanced type system (variance, type classes)
- Complex uses of implicit features
- Macros/metaprogramming

Learn gradually. You don't need to know all advanced features from the start.

## Syntax

### What's the difference between val and var?

```scala
val x = 10  // Immutable (cannot be changed)
// x = 20   // Compile error!

var y = 10  // Mutable (can be changed)
y = 20      // OK
```

**Recommendation:** Always use `val` when possible.

### What's the difference between def, val, and lazy val?

```scala
// Computed every time
def computed = { println("computing"); 42 }
computed  // Prints "computing"
computed  // Prints "computing"

// Computed once (immediately)
val eager = { println("eager"); 42 }  // Prints "eager"
eager     // No output
eager     // No output

// Computed once (lazily)
lazy val deferred = { println("lazy"); 42 }
// No output yet
deferred  // Prints "lazy"
deferred  // No output
```

### Are semicolons required?

**Usually no.** Scala automatically infers semicolons at line endings.

```scala
val x = 1
val y = 2

// Multiple statements on one line need semicolons
val a = 1; val b = 2
```

### What is Unit?

`Unit` is similar to Java's `void`. It indicates no meaningful value is returned.

```scala
def printMessage(): Unit = println("Hello")

// The only value of Unit is ()
val unit: Unit = ()
```

## Functional Programming

### What are Option, Some, and None?

`Option` safely represents the presence or absence of a value:

```scala
def findUser(id: Int): Option[String] =
  if (id > 0) Some(s"User$id") else None

findUser(1)   // Some("User1")
findUser(-1)  // None

// Usage
findUser(1).getOrElse("Unknown")  // "User1"
findUser(-1).getOrElse("Unknown") // "Unknown"
```

**Use Option instead of null!**

### What's the difference between map and flatMap?

```scala
val opt = Some(5)

// map: A => B
opt.map(_ * 2)  // Some(10)

// flatMap: A => Option[B]
opt.flatMap(x => if (x > 0) Some(x * 2) else None)  // Some(10)

// Difference
val nested = Some(Some(5))
nested.map(_.map(_ * 2))      // Some(Some(10))
nested.flatMap(_.map(_ * 2))  // Some(10)
```

### How does for comprehension work?

For comprehension is transformed into `flatMap`, `map`, and `withFilter`:

```scala
// for comprehension
for {
  x <- Some(1)
  y <- Some(2)
} yield x + y

// Transforms to
Some(1).flatMap(x =>
  Some(2).map(y =>
    x + y
  )
)
// Result: Some(3)
```

## Troubleshooting

### "implicit not found" error

```
could not find implicit value for parameter ord: Ordering[MyClass]
```

**Solution:** Define or import the required implicit instance.

```scala
case class Person(name: String, age: Int)

// Define instance
implicit val personOrdering: Ordering[Person] = Ordering.by(_.age)

// Or define in companion object
object Person {
  implicit val ordering: Ordering[Person] = Ordering.by(_.age)
}
```

### "value xxx is not a member of yyy"

Extension methods may not be imported:

```scala
import cats.syntax.all._  // Cats extension methods
import zio.prelude._      // ZIO extension methods
```

### Compilation is too slow

- **Use incremental compilation**: `sbt ~compile`
- **Parallel compilation**: `parallelExecution := true`
- **Utilize cache**: Keep `.bsp` directory

### Red underlines in IntelliJ

1. **File** → **Invalidate Caches** → **Restart**
2. Click **Reload** in sbt tab
3. **Build** → **Rebuild Project**

## Performance

### Is Scala slower than Java?

**In most cases, similar.** Scala compiles to JVM bytecode, so runtime performance is similar to Java.

**Things to watch out for:**
- Lambdas and higher-order functions cause object creation
- Collection chaining can create intermediate collections
- Verify tail recursion optimization with `@tailrec`

### Do immutable collections affect performance?

Scala's immutable collections use **structural sharing** for efficiency:

```scala
val list1 = List(1, 2, 3)
val list2 = 0 :: list1  // Reuses list1's data
```

If performance is really critical, use `Array` or mutable collections.

## Tools

### Which IDE should I use?

- **IntelliJ IDEA + Scala plugin**: Most complete
- **VS Code + Metals**: Lightweight and fast

### Which build tool?

- **sbt**: Scala standard build tool
- **Mill**: Faster alternative
- **Gradle**: When mixing with Java projects

### Which testing framework?

- **ScalaTest**: Most widely used, various styles
- **MUnit**: Simple and lightweight (recommended for Scala 3)
- **Specs2**: BDD style

## More Questions

If you have more questions:

- [Scala Official Documentation](https://docs.scala-lang.org/)
- [Stack Overflow - Scala](https://stackoverflow.com/questions/tagged/scala)
- [Scala Users Forum](https://users.scala-lang.org/)
