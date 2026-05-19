---
title: "Extension Functions"
description: "Explains the principles and usage of Kotlin extension functions, which add new methods to existing classes without modifying them."
weight: 8
lastmod: "2026-05-13"
---

## Overall Analogy: Adding an Elevator to an Existing Building

Extension functions are easy to understand by analogy with **remodeling construction**. Without changing the structure of the existing building (the class), you can attach an elevator (new functionality) from the outside.

| Remodeling Analogy | Kotlin Concept | Role |
|--------------------|----------------|------|
| Existing building | Receiver type (`String`, `List`, etc.) | Target to add functionality to |
| Elevator design | `fun Type.method()` definition | Extension function declaration |
| Inside the elevator | `this` (receiver object) | Access members of the existing type |
| External contractor | File/module where the extension is defined | Add functionality without ownership |
| Building blueprint | Original class | Cannot be modified, only viewed |

Just as you can add functionality from the outside without changing the existing building blueprint, extension functions **add features to a type without requiring source code access**.

---

> **Target Audience**: Developers familiar with Kotlin basic syntax
> **Prerequisites**: Function definitions, basic classes
> **Estimated Time**: About 25 minutes
> **What You'll Learn**: You'll be able to define your own extension functions and confidently use the extension functions in the standard library.

{{< callout type="tip" title="TL;DR" >}}
- Add methods to existing types in the form `fun String.greet()`
- Inside the function, `this` refers to the receiver object (the extension target)
- Extension functions use **static dispatch** — they're resolved by the declared type, not the runtime type
- When a member function and an extension function have conflicting signatures, **the member function wins**
{{< /callout >}}

#### Why Do We Need Extension Functions?

You can't directly modify the source code of library classes. For example, if you want to add an email validation method to `String`, you previously had to create a utility class.

```kotlin
// Traditional approach: utility class
object StringUtils {
    fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}

// Call site — awkward readability
val valid = StringUtils.isValidEmail("user@example.com")
```

With extension functions, you can call them as if they were the original methods of `String`.

```kotlin
// Extension function
fun String.isValidEmail(): Boolean {
    return this.contains("@") && this.contains(".")
}

// Call site — natural
val valid = "user@example.com".isValidEmail()
```

#### Basic Syntax

The form of an extension function declaration is `fun ReceiverType.functionName(parameters): ReturnType { ... }`.

```kotlin
// Basic form — 'this' is the receiver object (the String instance)
fun String.shout(): String {
    return this.uppercase() + "!"
}

// 'this' can usually be omitted
fun String.wordCount(): Int {
    return trim().split("\\s+".toRegex()).size
}

// Also works for generic types
fun <T> List<T>.secondOrNull(): T? {
    return if (size >= 2) this[1] else null
}
```

> **Note:** Even if you define an extension function with the same signature as a member function, **the member function takes priority** when called. For example, even if you create a `fun String.repeat(n: Int)` extension, `"abc".repeat(3)` will call the standard library's `String.repeat` member. It's best to name extension functions so they don't conflict with members.

#### The Receiver Object (this)

Inside an extension function, `this` is the extension target instance. You can write it explicitly or omit it.

```kotlin
fun String.isPalindrome(): Boolean {
    val cleaned = this.replace(" ", "").lowercase()  // explicit this
    return cleaned == cleaned.reversed()
}

fun String.shout(): String {
    return uppercase() + "!!!"                       // this omitted (recommended)
}

// Usage
println("racecar".isPalindrome())   // true
println("hello".shout())            // HELLO!!!
```

#### Extension Properties

You can extend not only functions but also properties. However, since they cannot have a backing field, both `val` and `var` must define a getter (and setter).

```kotlin
// Extension property — getter required
val String.lastChar: Char
    get() = this[length - 1]

var StringBuilder.lastChar: Char
    get() = this[length - 1]
    set(value) {
        this.setCharAt(length - 1, value)
    }

// Usage
println("Kotlin".lastChar)          // n

val sb = StringBuilder("Kotlin")
sb.lastChar = '!'
println(sb)                         // Kotli!
```

{{< callout type="info" title="Constraints of Extension Properties" >}}
Extension properties cannot store state. Initializer expressions (`val x: Int = 0`) are not allowed; they must return a computed value through a getter/setter.
{{< /callout >}}

#### Static Dispatch of Extensions

For extension functions, **the receiver type is determined at compile time**. Since this is not virtual dispatch (override), the extension function called is determined by the declared type of the variable.

```kotlin
open class Animal
class Dog : Animal()

fun Animal.sound() = "..."
fun Dog.sound() = "Woof"

fun printSound(animal: Animal) {
    println(animal.sound())     // Calls Animal.sound() — "..."
}

printSound(Dog())               // "..." — Dog, but the parameter type is Animal
```

```mermaid
graph LR
    A["printSound(Dog())"] --> B["Parameter type: Animal"]
    B --> C["Calls Animal.sound()"]
    C --> D["Result: '...'"]
```

*Figure: Static dispatch behavior of extension functions — when printSound(Dog()) is called, the extension function is chosen based on the parameter type Animal, showing that polymorphism does not apply.*

Always remember that **this behavior may differ from your expectations**. If you need polymorphism, you must use a regular member function (override).

#### Member vs Extension Function Priority

**Member functions always take priority over extension functions.** When signatures match, the member function is called.

```kotlin
class Greeter {
    fun hello() = "member hello"
}

fun Greeter.hello() = "extension hello"

val g = Greeter()
println(g.hello())      // "member hello" — member function wins
```

Extension functions **cannot override member functions**. This characteristic prevents external code from secretly altering a class's existing behavior.

#### Nullable Receiver Types

By adding `?` to the receiver type, you can safely call the extension function even when the receiver is `null`.

```kotlin
fun String?.orEmpty(): String {
    return this ?: ""
}

fun Any?.isNull(): Boolean {
    return this == null
}

// Usage
val name: String? = null
println(name.orEmpty())    // "" — no NPE
println(name.isNull())     // true
```

The standard library's `String?.isNullOrEmpty()` and `String?.isNullOrBlank()` are defined this way.

#### Standard Library Examples

A significant portion of the Kotlin standard library is implemented as extension functions.

```kotlin
// Collection extension functions
val numbers = listOf(3, 1, 4, 1, 5, 9, 2, 6)

val result = numbers
    .filter { it > 3 }          // List<Int> extension
    .sortedDescending()         // List<Int> extension
    .take(3)                    // List<T> extension
    .sumOf { it }               // Collection<Int> extension

// String extension functions
val csv = "John,30,Seoul"
val parts = csv.split(",")             // String extension
val name = parts.first()              // List<T> extension
val trimmed = "  hello  ".trim()      // String extension

// File I/O extension functions
import java.io.File
val content = File("data.txt").readText()   // File extension
```

#### Practical Pattern: Domain-Specific Extensions

Extension functions are especially useful for writing readable code tailored to a specific business domain.

```kotlin
// Currency-related extensions
val Int.won: Long get() = this.toLong()
val Long.won: Long get() = this
val Int.tenThousandWon: Long get() = this * 10_000L

// Usage
val price = 5.tenThousandWon + 3000.won   // 53,000

// Date-related extensions
import java.time.LocalDate

fun LocalDate.isWeekend(): Boolean {
    val day = dayOfWeek
    return day == java.time.DayOfWeek.SATURDAY ||
           day == java.time.DayOfWeek.SUNDAY
}

fun LocalDate.nextWorkday(): LocalDate {
    var next = this.plusDays(1)
    while (next.isWeekend()) next = next.plusDays(1)
    return next
}

// Usage
val today = LocalDate.now()
if (today.isWeekend()) {
    println("Today is a weekend. Next workday: ${today.nextWorkday()}")
}
```

#### Where to Define Extension Functions?

| Location | Suitable Use Case |
|----------|-------------------|
| Top-level file | Used commonly across the entire project |
| Companion object | Factory role related to that class |
| Inside a class/function | Local extension used only within a specific scope |

```kotlin
// Local extension function — only valid within that function
fun processData(input: String): String {
    fun String.clean() = trim().lowercase().replace(" ", "_")
    return input.clean()
}
```

{{< callout type="info" title="Key Takeaways" >}}
- Use the form `fun Type.functionName()` to add functionality without modifying the existing type
- Inside the function, `this` refers to the receiver object and can be omitted
- Extension functions use **static dispatch** — determined by the declared type of the variable
- On conflicts with member functions, **the member function takes priority**
- Nullable receiver types like `String?` are also supported
{{< /callout >}}

#### Next Steps

- [Scope Functions](scope-functions/) — Standard library helpers like `let`, `run`, `apply` built on extension functions
- [Inline/Reified](inline-reified/) — How to optimize extension functions with inlining
