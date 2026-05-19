---
title: "Functions"
description: "Explains Kotlin's fun definitions, expression bodies, default/named arguments, vararg, lambdas and higher-order functions, and single-expression functions."
weight: 3
lastmod: "2026-05-13"
---

## Overall Analogy: Recipes and Cooking

Kotlin functions are easy to understand by analogy to **recipes and cooking**. Like a recipe, a function takes ingredients (parameters) and produces a result (return value). When ingredients have default values, you don't need to list them all every time. You can even pass a recipe as an ingredient — that's exactly what lambdas and higher-order functions are.

| Analogy | Kotlin Concept | Role |
|---------|----------------|------|
| Recipe | Function (`fun`) | A unit that takes input and produces output |
| Default ingredient | Default argument | Use the default value when an argument is omitted |
| "Sugar first" instruction | Named argument | Specify the argument name to change the order |
| "As much as you want" ingredient | vararg | Variable number of arguments |
| Recipe as an ingredient | Lambda / higher-order function | Pass a function as a value |

---

> **Target Audience**: Learners who have read [Variables and Types](../variables-types/)
> **Prerequisites**: Kotlin val/var, basic types
> **Estimated Time**: About 30 minutes
> **What You'll Learn**: You'll be able to declare functions in various forms and create lambdas to pass to higher-order functions.

{{< callout type="tip" title="TL;DR" >}}
- Define a function with the **`fun` keyword**; if it's a single expression, write it directly with `=`
- Use **default arguments** to support various call styles without overloading
- Use **named arguments** to call clearly regardless of parameter order
- A **lambda** is in the form `{ parameters -> body }` and is passed to higher-order functions
{{< /callout >}}

#### Why So Many Forms for Functions?

A function is the most basic unit of code reuse. Kotlin provides diverse function syntax to **express intent clearly while reducing boilerplate**. For simple transformations, write a single-expression function on one line; for complex logic, write a block-body function for clarity.

---

#### Basic fun Definition

```kotlin
// Basic form
fun functionName(parameterName: Type): ReturnType {
    // body
    return value
}

// Example
fun add(a: Int, b: Int): Int {
    return a + b
}
```

**Omitting the Return Type**

If the return type is `Unit` (no return value), it can be omitted.

```kotlin
fun printGreeting(name: String) {     // : Unit omitted
    println("Hello, $name!")
}
```

---

#### Single-Expression Functions (Expression Body)

If the function body is a single expression, you can write it concisely with `=`. The return type is also inferred and can be omitted.

```kotlin
// Block body — verbose form
fun double(n: Int): Int {
    return n * 2
}

// Single-expression function — concise form
fun double(n: Int): Int = n * 2

// Return type also inferred — even more concise
fun double(n: Int) = n * 2

// Complex expressions are also possible
fun max(a: Int, b: Int) = if (a > b) a else b
fun grade(score: Int) = when {
    score >= 90 -> "A"
    score >= 80 -> "B"
    else        -> "C"
}
```

---

#### Default Arguments

By setting default values for parameters, you can call the function while omitting arguments. Unlike Java, you don't need to create multiple overloaded functions.

```kotlin
fun connect(
    host: String,
    port: Int = 5432,
    ssl: Boolean = false,
    timeout: Int = 3000
) {
    println("$host:$port (ssl=$ssl, timeout=${timeout}ms)")
}

// Various call styles
connect("localhost")                         // All defaults applied
connect("db.example.com", 5433)              // port changed
connect("db.example.com", ssl = true)        // ssl changed
connect("db.example.com", 5433, true, 5000)  // All specified
```

{{< callout type="info" title="Default Arguments and Overloading" >}}
When calling a Kotlin function with default arguments from Java, you must always specify every argument. To allow overload-style calls from Java, add the `@JvmOverloads` annotation to the function.

```kotlin
@JvmOverloads
fun connect(host: String, port: Int = 5432, ssl: Boolean = false) { ... }
```
{{< /callout >}}

---

#### Named Arguments

When passing arguments, specifying the parameter name lets you call them regardless of order.

```kotlin
fun createUser(
    name: String,
    age: Int,
    email: String,
    isAdmin: Boolean = false
) {
    println("User created: $name ($age) - $email, admin: $isAdmin")
}

// Using named arguments
createUser(
    name = "John",
    email = "john@example.com",  // Order can be changed
    age = 30
)

// Mix of positional and named
createUser("John", 30, email = "john@example.com", isAdmin = true)
```

Using named arguments makes the **intent of the call site clear**. This is especially useful when using Boolean arguments like `true` or `false`.

---

#### vararg — Variable Arguments

With the `vararg` keyword, you can accept an unspecified number of arguments. Inside the function, they are treated like an array.

```kotlin
fun printAll(vararg messages: String) {
    for (msg in messages) {
        println(msg)
    }
}

printAll("First")
printAll("First", "Second", "Third")

// To pass an array as vararg, use the spread operator (*)
val lines = arrayOf("A", "B", "C")
printAll(*lines)
```

```kotlin
// When combining vararg with other parameters, vararg comes last
fun log(level: String, vararg messages: String) {
    messages.forEach { println("[$level] $it") }
}

log("INFO", "Server started", "Port: 8080")
```

---

#### Lambdas and Higher-Order Functions

A **lambda** is an unnamed function. It can be stored in a variable or passed as an argument to another function.

**Lambda Syntax**

```kotlin
// Basic lambda form: { parameters -> body }
val add: (Int, Int) -> Int = { a, b -> a + b }
val greet: (String) -> String = { name -> "Hello, $name!" }
val printLine: () -> Unit = { println("===") }

// Call
println(add(3, 5))       // 8
println(greet("John"))   // Hello, John!
printLine()              // ===
```

**The it Keyword — Omitting a Single Parameter**

If a lambda has a single parameter, you can automatically refer to it as `it`.

```kotlin
val double: (Int) -> Int = { it * 2 }
val isEven: (Int) -> Boolean = { it % 2 == 0 }

println(double(5))    // 10
println(isEven(4))    // true
```

**Higher-Order Functions**

A function that takes a function as a parameter or returns a function.

```kotlin
// Higher-order function taking a function as a parameter
fun transform(numbers: List<Int>, operation: (Int) -> Int): List<Int> {
    return numbers.map(operation)
}

val nums = listOf(1, 2, 3, 4, 5)
val doubled = transform(nums) { it * 2 }       // [2, 4, 6, 8, 10]
val squared = transform(nums) { it * it }       // [1, 4, 9, 16, 25]

// Higher-order function returning a function
fun multiplier(factor: Int): (Int) -> Int {
    return { number -> number * factor }
}

val triple = multiplier(3)
println(triple(5))   // 15
```

---

#### Trailing Lambdas

If a lambda is the **last argument** of a function, it can be pulled outside the parentheses. The Kotlin standard library leverages this pattern.

```kotlin
// Standard form
val result = listOf(1, 2, 3).map({ it * 2 })

// Trailing lambda — more natural form
val result = listOf(1, 2, 3).map { it * 2 }

// If the lambda is the only argument, parentheses can be omitted
val evens = listOf(1, 2, 3, 4).filter { it % 2 == 0 }

// Complex lambda — improved readability with trailing lambda
val total = listOf(1, 2, 3, 4, 5).fold(0) { acc, n ->
    println("$acc + $n = ${acc + n}")
    acc + n
}
```

---

#### Function Types and Function References

Functions can also be referenced like values. Create a function reference with `::functionName`.

```kotlin
fun isPositive(n: Int): Boolean = n > 0

val nums = listOf(-3, -1, 0, 2, 5)

// Pass as a lambda
val positives1 = nums.filter { it > 0 }

// Pass as a function reference — more concise
val positives2 = nums.filter(::isPositive)

// Member function reference
val strings = listOf("hello", "WORLD", "Kotlin")
val uppercased = strings.map(String::uppercase)
// ["HELLO", "WORLD", "KOTLIN"]
```

---

#### Code Example: Putting It All Together

```kotlin
package com.example.functions

// Single-expression function
fun square(n: Int) = n * n

// Default arguments
fun formatMessage(
    text: String,
    prefix: String = "[INFO]",
    suffix: String = ""
) = "$prefix $text$suffix"

// Higher-order function
fun applyTwice(value: Int, operation: (Int) -> Int): Int =
    operation(operation(value))

fun main() {
    // Single-expression function
    println(square(5))   // 25

    // Default arguments
    println(formatMessage("Server started"))                        // [INFO] Server started
    println(formatMessage("Error occurred", prefix = "[ERROR]"))   // [ERROR] Error occurred

    // Named arguments
    println(formatMessage(
        text = "Completed",
        prefix = "[SUCCESS]",
        suffix = " OK"
    ))  // [SUCCESS] Completed OK

    // Higher-order function + lambda
    println(applyTwice(3) { it * 2 })   // 12 (3 -> 6 -> 12)
    println(applyTwice(3, ::square))     // 81 (3 -> 9 -> 81)

    // vararg
    fun sum(vararg nums: Int) = nums.sum()
    println(sum(1, 2, 3, 4, 5))   // 15

    // Collection operations — using lambdas and higher-order functions
    val scores = listOf(85, 92, 67, 78, 95)
    val passing = scores.filter { it >= 70 }
    val grades = passing.map { score ->
        when {
            score >= 90 -> "A"
            score >= 80 -> "B"
            else        -> "C"
        }
    }
    println(grades)   // [B, A, B, A]
}
```

---

#### Key Points

{{< callout type="info" title="Key Takeaways" >}}
- Use **single-expression functions** like `fun f(x: Int) = x * 2` for conciseness
- Use **default arguments** to support varied calls without overloading
- Use **named arguments** to clarify Boolean or otherwise ambiguous arguments
- A **lambda** is `{ parameters -> body }`; a single parameter can be omitted as `it`
- **Trailing lambda** — when the last argument is a lambda, it can be pulled outside the parentheses
{{< /callout >}}

#### Next Steps

- [Null Safety](../null-safety/) - Learn nullable types and safe call operators
- [Classes and Objects](../classes-objects/) - Learn how to use functions as class members
