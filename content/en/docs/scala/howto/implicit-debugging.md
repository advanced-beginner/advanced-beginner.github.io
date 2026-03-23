---
lastmod: "2026-01-16"
title: Implicit/Given Debugging
description: "How to diagnose and resolve Implicit/Given errors"
weight: 1
---

Learn how to diagnose and resolve issues when the compiler cannot find implicit values.

**Estimated time**: 10-15 minutes

{{< callout type="tip" title="TL;DR" >}}
- **Scala 2**: Use `-Xlog-implicits` flag to trace the search process
- **Scala 3**: Compiler messages are clearer and provide `import` suggestions
- **Common**: Check if implicit values are in scope and types match exactly
{{< /callout >}}

---

## What This Guide Solves

Use this guide when you encounter these compile errors:

{{< tabs "implicit-error-messages" >}}
{{< tab "Scala 2" >}}
```
could not find implicit value for parameter ord: Ordering[MyClass]
```
or
```
ambiguous implicit values
```
{{< /tab >}}
{{< tab "Scala 3" >}}
```
No given instance of type Ordering[MyClass] was found
```
or
```
Ambiguous given instances
```
{{< /tab >}}
{{< /tabs >}}

### What This Guide Does NOT Cover

- **How implicit conversions work**: See [Implicit/Given Concepts](../concepts/implicits/)
- **How to design type classes**: See [Type Classes Concepts](../concepts/type-classes/)
- **Implicit values in Cats/ZIO libraries**: Refer to respective library documentation

---

## Before You Begin

Ensure you have the following environment ready:

| Item | Requirement | How to Check |
|------|-------------|--------------|
| Scala version | 2.13.x or 3.x | `scala -version` |
| Build tool | sbt 1.x or Gradle 8.x | `sbt --version` |
| IDE (optional) | IntelliJ IDEA with Scala plugin or VS Code with Metals | - |

```bash
# Check Scala version
scala -version
# Example output: Scala code runner version 3.3.1

# Check sbt version
sbt --version
# Example output: sbt version in this project: 1.9.7
```

---

## Step 1: Analyze the Error Message

### 1.1 Identify the Required Type

Parse the error message to identify exactly which implicit type is needed:

```scala
// Error: could not find implicit value for parameter ord: Ordering[Person]
case class Person(name: String, age: Int)

val people = List(Person("Alice", 30), Person("Bob", 25))
people.sorted  // Requires Ordering[Person]
```

### 1.2 Check the Scope

Implicit values are searched in the following priority order:

| Priority | Search Location | Example |
|----------|-----------------|---------|
| 1 | Explicit definitions in current scope | `implicit val`, `given` |
| 2 | Explicit imports | `import MyImplicits._` |
| 3 | Companion object | `object Person { implicit val ord = ... }` |
| 4 | Supertype companion objects | Companion objects in inheritance hierarchy |
| 5 | Package object | `package object mypackage` |

---

## Step 2: Use Compiler Debugging Flags

{{< tabs "compiler-debug-flags" >}}
{{< tab "Scala 2" >}}
### -Xlog-implicits Flag

Add the following option in sbt:

```scala
// build.sbt
scalacOptions += "-Xlog-implicits"
```

After compilation, check for output like this:

```
[info] /path/to/file.scala:10: Ordering.ordered is not a valid implicit value
[info] for Ordering[Person] because:
[info] hasMatchingSymbol reported error: type mismatch;
[info]  found   : Ordering[Comparable[Person]]
[info]  required: Ordering[Person]
```

**Success check**: If you see detailed logs like above, the flag is working correctly.
{{< /tab >}}
{{< tab "Scala 3" >}}
### Enhanced Error Messages

Scala 3 provides more detailed error messages and suggestions by default:

```
-- Error: /path/to/file.scala:10:8 ------
10 |  people.sorted
   |         ^
   |No given instance of type Ordering[Person] was found
   |for parameter ord of method sorted in trait SeqOps
   |
   |The following import might fix the problem:
   |
   |  import scala.math.Ordering.Implicits._
```

**Success check**: If the compiler shows `import` suggestions, Scala 3's enhanced diagnostics are working.
{{< /tab >}}
{{< /tabs >}}

### IDE Settings

{{< tabs "ide-settings" >}}
{{< tab "IntelliJ IDEA" >}}
1. Go to **Preferences** → **Build, Execution, Deployment** → **Compiler** → **Scala Compiler**
2. Add `-Xlog-implicits` to **Additional compiler options** (Scala 2)
3. Press **Ctrl+Shift+P** (Windows/Linux) or **Cmd+Shift+P** (macOS) to toggle implicit hints

**Success check**: Implicit parameters appear as gray text in your code.
{{< /tab >}}
{{< tab "VS Code + Metals" >}}
1. Open the command palette (**Ctrl+Shift+P**) and run `Metals: Toggle inlay hints`
2. Set `metals.inlayHints.implicitArguments.enable` to `true` in settings

**Success check**: Implicit parameters appear as inline hints in your code.
{{< /tab >}}
{{< /tabs >}}

---

## Step 3: Solutions

### 3.1 Define Implicit Values Directly

{{< tabs "define-implicit" >}}
{{< tab "Scala 2" >}}
```scala
case class Person(name: String, age: Int)

object Person {
  implicit val ordering: Ordering[Person] = Ordering.by(_.age)
}

// Now it works
val people = List(Person("Alice", 30), Person("Bob", 25))
people.sorted  // List(Person("Bob", 25), Person("Alice", 30))
```
{{< /tab >}}
{{< tab "Scala 3" >}}
```scala
case class Person(name: String, age: Int)

object Person:
  given Ordering[Person] = Ordering.by(_.age)

// Now it works
val people = List(Person("Alice", 30), Person("Bob", 25))
people.sorted  // List(Person("Bob", 25), Person("Alice", 30))
```
{{< /tab >}}
{{< /tabs >}}

### 3.2 Leverage Existing Implicit Values

Combine existing implicit values:

{{< tabs "combine-implicit" >}}
{{< tab "Scala 2" >}}
```scala
case class Person(name: String, age: Int)

object Person {
  // Leverage String's Ordering
  implicit val ordering: Ordering[Person] = Ordering.by(_.name)
}
```
{{< /tab >}}
{{< tab "Scala 3" >}}
```scala
case class Person(name: String, age: Int)

object Person:
  // Leverage existing given instance
  given Ordering[Person] = Ordering.by(_.name)
```
{{< /tab >}}
{{< /tabs >}}

### 3.3 Pass Explicitly

When implicit search is complex, pass the value explicitly:

{{< tabs "explicit-passing" >}}
{{< tab "Scala 2" >}}
```scala
// Explicitly pass Ordering
people.sorted(Ordering.by[Person, Int](_.age))
```
{{< /tab >}}
{{< tab "Scala 3" >}}
```scala
// Explicit passing with using keyword
people.sorted(using Ordering.by[Person, Int](_.age))
```
{{< /tab >}}
{{< /tabs >}}

---

## Step 4: Common Mistakes and Solutions

### 4.1 Type Mismatch

{{< callout type="warning" title="Warning" >}}
Wildcard types (`_`) cannot be used as implicit values.
{{< /callout >}}

```scala
// Wrong: Missing type parameter
implicit val ord: Ordering[_] = Ordering.by(_.toString)  // Won't compile

// Correct: Specify exact type
implicit val ord: Ordering[Person] = Ordering.by(_.name)
```

### 4.2 Missing Import

```scala
// Common mistake with JSON libraries
import io.circe.generic.auto._  // This import is required

case class User(name: String)
val json = User("Alice").asJson  // Requires Encoder[User]
```

**Solution**: Check the library documentation for required imports.

### 4.3 Scope Issues

```scala
// Wrong: Defined inside method, not accessible outside
def process(): Unit = {
  implicit val ord: Ordering[Person] = Ordering.by(_.age)
}

// Correct: Define in appropriate scope
object Implicits {
  implicit val personOrdering: Ordering[Person] = Ordering.by(_.age)
}

import Implicits._
```

### 4.4 Implicit Conflict

Multiple implicit values of the same type cause conflicts:

```scala
// Error: ambiguous implicit values
implicit val byAge: Ordering[Person] = Ordering.by(_.age)
implicit val byName: Ordering[Person] = Ordering.by(_.name)

people.sorted  // Compiler doesn't know which one to use
```

**Solution**: Import only one or pass explicitly.

---

## Checklist

When implicit values are not found, check the following:

- [ ] **Does the type match exactly?** - Including generic type parameters
- [ ] **Is it in scope?** - Check imports or companion objects
- [ ] **Is the compilation order correct?** - Implicit definitions must compile before use
- [ ] **Are there conflicting implicit values?** - Multiple implicit values of same type
- [ ] **Did you use debugging flags?** - `-Xlog-implicits` (Scala 2)

**If the problem persists after checking all items**, create a minimal reproduction case and ask on Scala Discord or Stack Overflow.

---

## Related Documentation

- [Implicit/Given Concepts](../concepts/implicits/) - How implicit conversions work
- [Type Classes](../concepts/type-classes/) - Type class patterns
- [Future Error Handling](future-error-handling/) - ExecutionContext troubleshooting
