---
lastmod: "2026-01-06"
title: Glossary
weight: 1
---

Core Scala terminology organized alphabetically.

## A

**ADT (Algebraic Data Type)**
: A combination of sum types and product types defined using `sealed trait` and `case class`. In Scala 3, can be defined more simply with `enum`.

**Applicative**
: A type class that combines independent effects. Provides `pure` and `ap` operations.

**apply method**
: A special method that allows objects to be called like functions. `obj(args)` is interpreted as `obj.apply(args)`.

## C

**Case Class**
: A special class for immutable data. Automatically generates `equals`, `hashCode`, `copy`, `unapply`, etc.

**Companion Object**
: A singleton object with the same name as a class. Can access the class's `private` members.

**Context Bound**
: Syntax that requires the existence of a type class instance, like `def f[A: Ordering]`.

**Currying**
: A technique of transforming a function with multiple arguments into a chain of single-argument functions.

## E

**Either[L, R]**
: A type that holds a value of one of two types. Usually `Left` for failure, `Right` for success.

**Extension Method**
: A technique for adding new methods to existing types. Uses `extension` keyword in Scala 3.

**ExecutionContext**
: A context that provides a thread pool for `Future` execution.

## F

**flatMap**
: An operation that transforms values inside a container and flattens the result. Core operation of Monad.

**For Comprehension**
: Syntactic sugar that elegantly expresses combinations of `flatMap`, `map`, and `withFilter`.

**Functor**
: A type class with a `map` operation. Transforms values inside a container.

**Future[T]**
: A type representing an asynchronous computation that is not yet complete.

## G

**Given (Scala 3)**
: Keyword for defining type class instances. Replaces Scala 2's `implicit val`.

## H

**Higher-Order Function**
: A function that takes functions as arguments or returns functions.

**Higher-Kinded Type**
: A type that takes type constructors as arguments. Has the form `F[_]`.

## I

**Immutable**
: Something whose state cannot be changed after creation. Scala encourages immutability.

**Implicit (Scala 2)**
: Keyword for defining implicit values, parameters, and conversions.

**Intersection Type (&)**
: A type that satisfies multiple types. `A & B`.

## L

**Lazy val**
: A value whose initialization is deferred until first access.

## M

**Match Expression**
: An expression that branches based on value patterns. A powerful version of `switch`.

**Monad**
: A type class with `flatMap` and `pure` operations. Combines sequential effects.

## O

**Object**
: Keyword for defining singleton instances.

**Opaque Type (Scala 3)**
: A type that appears as a different type externally but is identical to the underlying type internally.

**Option[A]**
: A type representing the presence (`Some`) or absence (`None`) of a value. Replaces `null`.

## P

**Partial Function**
: A function defined only for some inputs.

**Pattern Matching**
: A technique for analyzing the structure of values and extracting data.

**Promise[T]**
: A type that allows directly completing a `Future`.

## R

**Referential Transparency**
: The property where an expression can be replaced with its result value without changing program meaning.

## S

**Sealed**
: A modifier that restricts inheritance to the same file. Used for pattern matching exhaustiveness checking.

**Singleton Object**
: A unique instance defined with the `object` keyword.

**summon (Scala 3)**
: A function that retrieves an implicit instance of a given type. Replaces `implicitly`.

## T

**Tail Recursion**
: Recursion where the last operation of a function is a call to itself. Can be optimized without stack overflow.

**Trait**
: A type similar to Java interfaces but can contain implementations.

**Try[T]**
: A type that holds the result of a computation that may throw exceptions. `Success` or `Failure`.

**Type Class**
: A pattern for adding functionality to existing types. Implements ad-hoc polymorphism.

**Type Inference**
: A feature where the compiler automatically infers types.

## U

**Union Type (|)**
: A type representing one of several types. `Int | String`.

**Using (Scala 3)**
: Keyword for declaring implicit parameters. Replaces `implicit`.

## V

**val**
: Keyword for declaring immutable values.

**var**
: Keyword for declaring mutable variables.

**Variance**
: Subtyping relationships of type parameters. Covariant (+), contravariant (-), invariant.

## Y

**yield**
: Keyword for producing values in for comprehensions.
