---
lastmod: "2026-01-14"
title: Glossary
weight: 1
---

Key Scala terms organized alphabetically. For detailed explanations, refer to the [Concepts](../concepts/) section. Each term includes a definition and links to related documentation.

{{% notice style="tip" title="TL;DR - Top 5 Terms" %}}
- **Case Class**: Immutable data class, auto-generates `equals`/`copy`
- **Option[A]**: Replacement for null, either `Some(value)` or `None`
- **Pattern Matching**: Structure analysis and data extraction (`match` expressions)
- **Trait**: Interface that can include implementations, supports mixin inheritance
- **given/using** (Scala 3): New syntax for implicit values/parameters
{{% /notice %}}

#### A

**ADT (Algebraic Data Type)**
: Algebraic data types. Combination of sum types and product types defined with `sealed trait` and [Case Class](#case-class). Can be defined more simply with `enum` in Scala 3. → Used with [Pattern Matching](../concepts/pattern-matching/)

**Applicative**
: A [Type Class](#type-class) that combines independent effects. Provides `pure` and `ap` operations. More powerful than [Functor](#functor), weaker than [Monad](#monad). → [Functional Patterns](../concepts/functional-patterns/)

**apply method**
: A special method that allows objects to be called like functions. `obj(args)` is interpreted as `obj.apply(args)`. Commonly used as a factory method in [Companion Object](#companion-object).

#### C

**Case Class**
: A special class for immutable data. Automatically generates `equals`, `hashCode`, `copy`, `unapply`, etc. Used with [Pattern Matching](#pattern-matching). → [Case Classes Details](../concepts/case-classes/)

**Companion Object**
: A singleton object with the same name as a class. Can access the class's `private` members. Implements factory pattern with [apply method](#apply-method). → [Classes and Objects](../concepts/classes-objects/)

**Context Bound**
: Syntax requiring the existence of a [Type Class](#type-class) instance, like `def f[A: Ordering]`. → [Implicits](../concepts/implicits/)

**Currying**
: A technique for transforming a function with multiple arguments into a chain of single-argument functions. Used with [Higher-Order Function](#higher-order-function). → [Functions and Methods](../concepts/functions-methods/)

{{% notice style="note" title="A-C Key Points" %}}
- **ADT**: Type-safe data modeling with `sealed trait` + `case class`
- **Case Class**: Optimized for immutable data, used with pattern matching
- **Companion Object**: Place for factory methods (`apply`) and utility functions
{{% /notice %}}

#### E

**Either[L, R]**
: A type that holds one of two types. Usually `Left` is failure, `Right` is success. Similar to [Option](#optiona) but can contain failure information. Can be chained with [flatMap](#flatmap).

**Extension Method**
: A technique for adding new methods to existing types. Use `extension` keyword in Scala 3. Used in [Type Class](#type-class) implementations. → [Scala 3 Feature Comparison](../appendix/version-comparison/)

**ExecutionContext**
: Context providing a thread pool for executing [Future](#futuret). → [Concurrency](../concepts/concurrency/)

#### F

**flatMap**
: An operation that transforms values inside a container and flattens the result. Core operation of [Monad](#monad). Can be elegantly expressed with [For Comprehension](#for-comprehension).

**For Comprehension**
: Syntactic sugar for elegantly expressing combinations of [flatMap](#flatmap), `map`, and `withFilter`. Used with [Option](#optiona), [Future](#futuret), [Either](#eitherl-r), etc. → [For Comprehension Details](../concepts/for-comprehensions/)

**Functor**
: A [Type Class](#type-class) with a `map` operation. Transforms values inside a container. Foundation of [Applicative](#applicative) and [Monad](#monad). → [Functional Patterns](../concepts/functional-patterns/)

**Future[T]**
: A type representing an asynchronous computation not yet completed. Requires [ExecutionContext](#executioncontext). Sequential execution possible with [For Comprehension](#for-comprehension). → [Concurrency](../concepts/concurrency/)

#### G

**Given (Scala 3)**
: Keyword for defining [Type Class](#type-class) instances. Replaces Scala 2's [implicit](#implicit-scala-2) `val`. → [Scala 2 vs 3 Comparison](../examples/scala2-vs-scala3/)

#### H

**Higher-Order Function**
: A function that takes functions as arguments or returns a function. Examples: [map](#flatmap), `filter`, `fold`. → [Higher-Order Functions Details](../concepts/higher-order-functions/)

**Higher-Kinded Type**
: A type that takes type constructors as arguments. Form of `F[_]`. Essential for defining [Functor](#functor) and [Monad](#monad). → [Advanced Type System](../concepts/type-system-advanced/)

#### I

**Immutable**
: Something whose state cannot be changed after creation. Scala recommends immutability. Use [Case Class](#case-class), [val](#val), and immutable [collections](../concepts/collections/).

**Implicit (Scala 2)**
: Keyword for defining implicit values, parameters, and conversions. Replaced by [given](#given-scala-3)/[using](#using-scala-3) in Scala 3. → [Implicits Details](../concepts/implicits/)

**Intersection Type (&)**
: A type satisfying multiple types. `A & B`. Opposite of [Union Type](#union-type-). → [Advanced Type System](../concepts/type-system-advanced/)

{{% notice style="note" title="E-I Key Points" %}}
- **Either/Option**: Used instead of null, functional approach to error handling
- **flatMap**: Core of Monad, elegantly expressed with for comprehension
- **given/using** (Scala 3): Clear syntax replacing `implicit`
{{% /notice %}}

#### L

**Lazy val**
: A value whose initialization is deferred until first access. Useful for expensive initialization. → [Basics](../concepts/basics/)

#### M

**Match Expression**
: An expression that branches based on value patterns. Powerful version of `switch`. Optimized for [Case Class](#case-class) and [Sealed](#sealed) traits. → [Pattern Matching Details](../concepts/pattern-matching/)

**Monad**
: A [Type Class](#type-class) with [flatMap](#flatmap) and `pure` operations. Composes sequential effects. [Option](#optiona), [Either](#eitherl-r), [Future](#futuret) are Monads. → [Functional Patterns](../concepts/functional-patterns/)

#### O

**Object**
: Keyword for defining singleton instances. See [Companion Object](#companion-object). → [Classes and Objects](../concepts/classes-objects/)

**Opaque Type (Scala 3)**
: A type that appears different externally but is identical to its base type internally. Type safety without runtime overhead. → [Scala 3 Feature Comparison](../appendix/version-comparison/)

**Option[A]**
: A type representing the presence (`Some`) or absence (`None`) of a value. Replacement for `null`. Safely handled with [flatMap](#flatmap) and [For Comprehension](#for-comprehension). → [Basics](../concepts/basics/)

{{% notice style="note" title="L-O Key Points" %}}
- **lazy val**: Defers expensive initialization until first use
- **Monad**: `flatMap` + `pure`, core abstraction for sequential effect composition
- **Option**: Type-safe handling with `Some`/`None` instead of null
{{% /notice %}}

#### P

**Partial Function**
: A function defined only for some inputs. Same form as [Pattern Matching](#pattern-matching) cases. Used with the `collect` method. → [Functions and Methods](../concepts/functions-methods/)

**Pattern Matching**
: A technique for analyzing value structure and extracting data. Used with [Case Class](#case-class) and [Sealed](#sealed) traits. → [Pattern Matching Details](../concepts/pattern-matching/)

**Promise[T]**
: A type allowing direct completion of a [Future](#futuret). Used for wrapping callback-based APIs. → [Concurrency](../concepts/concurrency/)

#### R

**Referential Transparency**
: The property that replacing an expression with its result value doesn't change program meaning. Key characteristic of pure functions. Related to [Immutable](#immutable) data. → [Functional Patterns](../concepts/functional-patterns/)

#### S

**Sealed**
: A modifier restricting inheritance to the same file only. Used for [Pattern Matching](#pattern-matching) exhaustiveness checking. Essential for [ADT](#adt-algebraic-data-type) definitions. → [Pattern Matching](../concepts/pattern-matching/)

**Singleton Object**
: A unique instance defined with the [Object](#object) keyword. See [Companion Object](#companion-object).

**summon (Scala 3)**
: A function to retrieve an implicit instance of a given type. Replaces [implicit](#implicit-scala-2) `implicitly`. → [Type Classes](../concepts/type-classes/)

#### T

**Tail Recursion**
: Recursion where the last operation is a call to itself. Can be optimized without stack overflow. Verified with `@tailrec` annotation. → [Functions and Methods](../concepts/functions-methods/)

**Trait**
: Similar to Java interfaces but can include implementations. Supports mixin inheritance. Forms [ADT](#adt-algebraic-data-type) with [Sealed](#sealed). → [Classes and Objects](../concepts/classes-objects/)

**Try[T]**
: A type containing the result of a computation that may throw exceptions. Either `Success` or `Failure`. Similar error handling pattern to [Either](#eitherl-r) and [Option](#optiona). → [Basics](../concepts/basics/)

**Type Class**
: A pattern for adding functionality to existing types. Implements ad-hoc polymorphism. Representative examples: [Functor](#functor), [Monad](#monad). → [Type Classes Details](../concepts/type-classes/)

**Type Inference**
: The compiler's ability to automatically infer types. Scala's powerful type inference reduces boilerplate. → [Basics](../concepts/basics/)

{{% notice style="note" title="P-T Key Points" %}}
- **Pattern Matching**: Destructure data structures and branch with `match` expressions
- **Sealed**: Restricts inheritance to same file, enables pattern matching exhaustiveness checking
- **Type Class**: Add functionality to existing types, implement ad-hoc polymorphism
{{% /notice %}}

#### U

**Union Type (|)**
: A type representing one of multiple types. `Int | String`. Opposite of [Intersection Type](#intersection-type-). Scala 3 only. → [Advanced Type System](../concepts/type-system-advanced/)

**Using (Scala 3)**
: Keyword for declaring implicit parameters. Replaces [Implicit](#implicit-scala-2). Pairs with [Given](#given-scala-3). → [Scala 2 vs 3 Comparison](../examples/scala2-vs-scala3/)

#### V

**val**
: Keyword for declaring [Immutable](#immutable) values. Compare with [var](#var). Default choice in Scala.

**var**
: Keyword for declaring mutable variables. Less recommended than [val](#val). → [Basics](../concepts/basics/)

**Variance**
: Subtyping relationship of type parameters. Covariant (`+A`), contravariant (`-A`), invariant. Important for collection design. → [Variance Details](../concepts/variance/)

#### Y

**yield**
: Keyword for generating values in [For Comprehension](#for-comprehension). Transformed to `map` calls. → [For Comprehension Details](../concepts/for-comprehensions/)

{{% notice style="note" title="U-Y Key Points" %}}
- **Union Type** (`|`): Scala 3 only, concise type expression instead of `Either`
- **val/var**: Prefer `val` (immutable), minimize `var` (mutable)
- **Variance**: Define type relationships with covariant (`+A`), contravariant (`-A`)
{{% /notice %}}

---

#### Next Steps

- [Concepts](../concepts/) - Core Scala concepts
- [Examples](../examples/) - Learn by coding
- [Spark Integration](../examples/spark-integration/) - Big data processing
- [References](references/) - Books, courses, community
- [FAQ](faq/) - Frequently asked questions
