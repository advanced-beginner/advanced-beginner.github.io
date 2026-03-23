---
bookCollapseSection: true
lastmod: "2026-01-14"
title: Appendix
description: "Scala appendix resources and reference documents"
weight: 4
---

Reference materials to help with Scala learning. We've collected auxiliary materials needed during the learning process, including glossary, version comparison, FAQ, and reference links.

#### Appendix list

The table below lists the documents provided in the appendix. Each document is useful as a reference during learning or when you need quick information on specific topics.

| Document | Description |
|------|------|
| [Glossary](glossary/) | Scala core term definitions |
| [Version Comparison](version-comparison/) | Summary of Scala 2 vs Scala 3 differences |
| [FAQ](faq/) | Frequently asked questions and answers |
| [References](references/) | Official documentation, books, course links |

#### Quick reference

We've collected commonly used commands and code patterns in everyday Scala development.

**Commonly used sbt commands**

sbt is Scala's standard build tool. The table below shows the most frequently used commands.

| Command | Description |
|--------|------|
| `sbt run` | Run application |
| `sbt compile` | Compile |
| `sbt test` | Run tests |
| `sbt console` | Run REPL |
| `sbt clean` | Delete build artifacts |
| `sbt ~compile` | Auto-compile on file changes |
| `sbt update` | Update dependencies |

**Common type conversions**

These are frequently used type conversion patterns in Scala. Using methods that return Option like `toIntOption` allows safe handling by receiving None instead of exceptions on failure.

```scala
// String → Int
"42".toInt           // 42
"42".toIntOption     // Some(42)
"abc".toIntOption    // None

// Int → String
42.toString          // "42"

// Collection conversions
List(1, 2, 3).toSet  // Set(1, 2, 3)
Set(1, 2, 3).toList  // List(1, 2, 3)
```

**Working with Option**

Option is a type-safe way to use instead of null. Various methods allow elegant handling of value presence.

```scala
val maybeValue: Option[Int] = Some(42)

// Extract value
maybeValue.getOrElse(0)      // 42
maybeValue.map(_ * 2)        // Some(84)
maybeValue.filter(_ > 50)    // None
maybeValue.foreach(println)  // prints 42

// Pattern matching
maybeValue match
  case Some(v) => s"Value: $v"
  case None    => "No value"
```

**Key collection operations**

These are core operations on Scala collections. You can transform, filter, and aggregate data in functional programming style.

```scala
val nums = List(1, 2, 3, 4, 5)

nums.map(_ * 2)          // List(2, 4, 6, 8, 10)
nums.filter(_ % 2 == 0)  // List(2, 4)
nums.reduce(_ + _)       // 15
nums.foldLeft(0)(_ + _)  // 15
nums.find(_ > 3)         // Some(4)
nums.exists(_ > 3)       // true
nums.forall(_ > 0)       // true
nums.take(2)             // List(1, 2)
nums.drop(2)             // List(3, 4, 5)
nums.grouped(2).toList   // List(List(1, 2), List(3, 4), List(5))
```
