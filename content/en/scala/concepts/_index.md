---
lastmod: "2026-01-06"
title: Concepts
weight: 2
---

Learn Scala's core concepts systematically. From basics to advanced, each concept covers both Scala 2 and Scala 3 syntax.

## Basics

Learn the fundamental building blocks of programming in Scala.

| Topic | Content | Key Keywords |
|-------|---------|--------------|
| [Basic Syntax](basics/) | Variables, constants, basic types | `val`, `var`, type inference |
| [Control Structures](control-structures/) | Conditionals, loops | `if`, `for`, `while`, `match` |
| [Functions and Methods](functions-methods/) | Function definition, lambdas | `def`, `=>`, default values |
| [Classes and Objects](classes-objects/) | OOP basics | `class`, `object`, `trait` |
| [Case Classes](case-classes/) | Immutable data models | `case class`, `copy` |
| [Pattern Matching](pattern-matching/) | Powerful branching | `match`, `case`, guards |

## Intermediate

Learn functional programming and Scala's distinctive features.

| Topic | Content | Key Keywords |
|-------|---------|--------------|
| [Collections](collections/) | Data structures | `List`, `Map`, `Set`, `Seq` |
| [Higher-Order Functions](higher-order-functions/) | Functional programming | `map`, `filter`, `fold` |
| [Generics](generics/) | Type parameters | `[T]`, type bounds |
| [For Comprehension](for-comprehensions/) | Monadic operations | `for-yield`, `flatMap` |
| [Implicit/Given](implicits/) | Contextual abstraction | `implicit`, `given`, `using` |

## Advanced

Advanced topics for professional Scala development.

| Topic | Content | Key Keywords |
|-------|---------|--------------|
| [Type Classes](type-classes/) | Ad-hoc polymorphism | Type class pattern |
| [Variance](variance/) | Generic type variance | `+T`, `-T`, invariant |
| [Advanced Types](type-system-advanced/) | Scala 3 type features | Union, Intersection, Match Types |
| [Macros](macros-metaprogramming/) | Compile-time code generation | `inline`, macros |
| [Concurrency](concurrency/) | Asynchronous programming | `Future`, `Promise` |
| [Functional Patterns](functional-patterns/) | FP design patterns | Functor, Monad |

## Learning Guide

### If You're Just Starting

```
Basic Syntax → Control Structures → Functions and Methods → Classes and Objects
```

Building a solid foundation is important. Especially understand the difference between `val` and `var`, and expression-based syntax.

### If You Want to Learn Functional Programming

```
Collections → Higher-Order Functions → For Comprehension → Functional Patterns
```

Mastering Scala's collection API will naturally develop your functional thinking.

### If You're Transitioning to Scala 3

```
Implicit/Given → Advanced Types → Macros
```

The biggest change in Scala 3 is the implicit system. Learn the given/using syntax first.

## Scala 2 vs Scala 3 Comparison

All documents in this guide cover both versions. Key differences:

| Feature | Scala 2 | Scala 3 |
|---------|---------|---------|
| Syntax style | Braces required | Indentation optional |
| Implicit values | `implicit val` | `given` |
| Implicit parameters | `implicit` | `using` |
| Enums | `sealed trait` + `case object` | `enum` |
| Extension methods | `implicit class` | `extension` |
| Type features | Limited | Union, Intersection, Match Types |

> Each document marks version differences with {{< badge >}}Scala 2{{< /badge >}} {{< badge >}}Scala 3{{< /badge >}} badges.
