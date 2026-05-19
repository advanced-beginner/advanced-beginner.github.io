---
title: "DSL Builders"
description: "Explains how to design Kotlin DSLs using lambdas with receiver, the type-safe builder pattern, and isolating context with @DslMarker."
weight: 16
lastmod: "2026-05-13"
---

## Overall Analogy: A LEGO Assembly Manual

A LEGO assembly manual only lets you attach blocks within specific rules. When assembling a castle set, only castle blocks fit; for a spaceship set, only spaceship blocks fit. `@DslMarker` is exactly this "set distinction rule".

| LEGO Analogy | Kotlin DSL | Role |
|---------|-----------|------|
| Assembly manual block | Lambda with receiver | Access the receiver's members inside the block |
| Set distinction sticker | `@DslMarker` | Prevents mixing different DSL contexts |
| Block connection point | Builder function | A function in a parent DSL that starts a child DSL |
| Completed LEGO model | Builder result object | Immutable data structure or configuration object |

---

> **Target Audience**: Intermediate or higher developers who understand Kotlin lambdas, extension functions, and scope functions
> **Prerequisites**: Extension functions (`fun Type.method()`), lambdas, higher-order functions
> **Time Required**: About 30-40 minutes
> **After Reading**: You will be able to design type-safe builders yourself and prevent DSL misuse at compile time with `@DslMarker`.

{{< callout type="tip" title="TL;DR" >}}
- `A.() -> Unit` (lambda with receiver) lets you call `A`'s members inside the lambda without `this`.
- A type-safe builder nests lambdas with receiver to create hierarchical DSLs.
- Attaching an annotation marked with `@DslMarker` to a DSL class prevents accidental calls to outer DSL methods from inner lambdas.
- Real-world examples: `buildString`, `buildList`, Gradle Kotlin DSL, Spring Boot Kotlin Router DSL.
{{< /callout >}}

---

#### Why Do We Need a DSL?

DSLs (Domain-Specific Languages) are useful when expressing **syntax for a specific domain** with a programming language. Instead of representing configurations in XML or JSON, you can write type-safe configurations in Kotlin code with IDE auto-completion support.

```kotlin
// XML style (no type safety, typos -> runtime errors)
// <server port="8080"><database url="jdbc:..." /></server>

// Kotlin DSL style (compile-time verification, auto-completion supported)
server {
    port = 8080
    database {
        url = "jdbc:postgresql://localhost:5432/mydb"
    }
}
```

---

#### Understanding Lambda with Receiver

First understand the difference between a regular lambda and a lambda with receiver.

**Regular Lambda:**

```kotlin
// (Int, Int) -> Int : 2 parameters, returns Int
val add: (Int, Int) -> Int = { a, b -> a + b }
```

**Lambda with Receiver:**

```kotlin
// Int.() -> Int : Int is the receiver, returns Int
// Inside the lambda, directly access this(Int)'s members
val double: Int.() -> Int = { this * 2 }

println(5.double())  // 10
println(3.double())  // 6
```

You can omit `this`, which makes it even more natural:

```kotlin
val describe: Int.() -> String = {
    when {
        this < 0 -> "negative"
        this == 0 -> "zero"
        else -> "positive"
    }
}

println((-3).describe())  // negative
println(0.describe())     // zero
println(7.describe())     // positive
```

---

#### Type-Safe Builder Pattern

Combining a builder class with a lambda with receiver lets you create hierarchical DSLs.

**Step 1: Define the Builder Class**

```kotlin
// Final result data class
data class ServerConfig(
    val host: String,
    val port: Int,
    val maxConnections: Int
)

// Builder class
class ServerConfigBuilder {
    var host: String = "localhost"
    var port: Int = 8080
    var maxConnections: Int = 100

    fun build(): ServerConfig = ServerConfig(host, port, maxConnections)
}
```

**Step 2: Define the DSL Entry Function**

```kotlin
// Top-level function that accepts a lambda with receiver
fun server(block: ServerConfigBuilder.() -> Unit): ServerConfig {
    val builder = ServerConfigBuilder()
    builder.block()       // Run the lambda with the builder as receiver
    return builder.build()
}
```

**Step 3: Use the DSL**

```kotlin
val config: ServerConfig = server {
    host = "0.0.0.0"      // Same as this.host = "0.0.0.0"
    port = 9090
    maxConnections = 500
}

println(config)
// ServerConfig(host=0.0.0.0, port=9090, maxConnections=500)
```

---

#### Nested DSL Structure

Real DSLs are nested across multiple levels. Let's add a database configuration example.

```kotlin
data class DatabaseConfig(
    val url: String,
    val username: String,
    val password: String,
    val poolSize: Int
)

data class AppConfig(
    val server: ServerConfig,
    val database: DatabaseConfig
)

class DatabaseBuilder {
    var url: String = ""
    var username: String = "root"
    var password: String = ""
    var poolSize: Int = 10

    fun build() = DatabaseConfig(url, username, password, poolSize)
}

class AppConfigBuilder {
    private var serverConfig: ServerConfig = ServerConfig("localhost", 8080, 100)
    private var dbConfig: DatabaseConfig = DatabaseConfig("", "root", "", 10)

    fun server(block: ServerConfigBuilder.() -> Unit) {
        serverConfig = ServerConfigBuilder().apply(block).build()
    }

    fun database(block: DatabaseBuilder.() -> Unit) {
        dbConfig = DatabaseBuilder().apply(block).build()
    }

    fun build() = AppConfig(serverConfig, dbConfig)
}

fun application(block: AppConfigBuilder.() -> Unit): AppConfig {
    return AppConfigBuilder().apply(block).build()
}

// Usage
val appConfig = application {
    server {
        host = "0.0.0.0"
        port = 8080
    }
    database {
        url = "jdbc:postgresql://localhost:5432/mydb"
        username = "admin"
        poolSize = 20
    }
}
```

---

#### @DslMarker — Context Isolation

In nested DSLs, you might accidentally call an outer DSL's method from an inner DSL. `@DslMarker` prevents this.

**Problem Situation:**

```kotlin
// Without @DslMarker, this kind of mistake can happen
application {
    server {
        database {  // database is a method of AppConfigBuilder
            // You could accidentally call outer DSL methods inside
        }
        host = "0.0.0.0"  // Intent: host of the server block
    }
}
```

**Applying @DslMarker:**

```kotlin
// 1. Define the marker annotation
@DslMarker
annotation class AppDsl

// 2. Attach the marker annotation to all DSL builder classes
@AppDsl
class AppConfigBuilder { /* ... */ }

@AppDsl
class ServerConfigBuilder { /* ... */ }

@AppDsl
class DatabaseBuilder { /* ... */ }
```

Now, calling outer scope methods from inner lambdas produces a **compile error**:

```kotlin
application {
    server {
        host = "0.0.0.0"   // OK: ServerConfigBuilder's host
        // database { }    // Compile error! database belongs to AppConfigBuilder
    }
    database {
        url = "jdbc:..."   // OK: DatabaseBuilder's url
    }
}
```

---

#### Real-world Example: A Mini HTML Builder

```kotlin
@DslMarker
annotation class HtmlDsl

@HtmlDsl
class Tag(val name: String) {
    private val children = mutableListOf<Tag>()
    private val attributes = mutableMapOf<String, String>()
    var text: String = ""

    fun attr(key: String, value: String) {
        attributes[key] = value
    }

    fun tag(name: String, block: Tag.() -> Unit): Tag {
        val child = Tag(name).apply(block)
        children.add(child)
        return child
    }

    fun render(indent: Int = 0): String = buildString {
        val pad = "  ".repeat(indent)
        val attrs = attributes.entries.joinToString(" ") { (k, v) -> "$k=\"$v\"" }
        val attrStr = if (attrs.isNotEmpty()) " $attrs" else ""
        append("$pad<$name$attrStr>")

        if (text.isNotEmpty()) {
            append(text)
            append("</$name>")
        } else {
            children.forEach { append("\n${it.render(indent + 1)}") }
            if (children.isNotEmpty()) append("\n$pad")
            append("</$name>")
        }
    }
}

fun html(block: Tag.() -> Unit): Tag = Tag("html").apply(block)

fun Tag.head(block: Tag.() -> Unit) = tag("head", block)
fun Tag.body(block: Tag.() -> Unit) = tag("body", block)
fun Tag.div(block: Tag.() -> Unit) = tag("div", block)
fun Tag.p(block: Tag.() -> Unit) = tag("p", block)
fun Tag.h1(block: Tag.() -> Unit) = tag("h1", block)
fun Tag.a(href: String, block: Tag.() -> Unit) = tag("a") {
    attr("href", href)
    block()
}

// Usage
fun main() {
    val page = html {
        head {
            tag("title") { text = "Kotlin DSL Example" }
        }
        body {
            h1 { text = "Welcome" }
            div {
                attr("class", "content")
                p { text = "Generated HTML with a Kotlin DSL." }
                a("https://kotlinlang.org") {
                    text = "Kotlin official site"
                }
            }
        }
    }

    println(page.render())
}
```

Output:
```html
<html>
  <head>
    <title>Kotlin DSL Example</title>
  </head>
  <body>
    <h1>Welcome</h1>
    <div class="content">
      <p>Generated HTML with a Kotlin DSL.</p>
      <a href="https://kotlinlang.org">Kotlin official site</a>
    </div>
  </body>
</html>
```

---

#### Real-world Example: A Mini Spring Boot Kotlin Router DSL

A pattern similar to Spring Boot's Kotlin Router DSL.

```kotlin
// Request/Response simulation
data class Request(val method: String, val path: String)
data class Response(val status: Int, val body: String)

typealias Handler = (Request) -> Response

@DslMarker
annotation class RouterDsl

@RouterDsl
class RouterBuilder {
    private val routes = mutableListOf<Pair<Pair<String, String>, Handler>>()

    fun GET(path: String, handler: Handler) {
        routes.add(Pair("GET", path) to handler)
    }

    fun POST(path: String, handler: Handler) {
        routes.add(Pair("POST", path) to handler)
    }

    fun handle(request: Request): Response {
        val route = routes.find { (key, _) ->
            key.first == request.method && key.second == request.path
        }
        return route?.second?.invoke(request)
            ?: Response(404, "Not Found")
    }
}

fun router(block: RouterBuilder.() -> Unit): RouterBuilder =
    RouterBuilder().apply(block)

// Usage
fun main() {
    val app = router {
        GET("/health") {
            Response(200, """{"status":"UP"}""")
        }

        GET("/users") {
            Response(200, """[{"id":1,"name":"Alice"}]""")
        }

        POST("/users") { req ->
            Response(201, """{"message":"user created"}""")
        }
    }

    println(app.handle(Request("GET", "/health")))
    println(app.handle(Request("GET", "/users")))
    println(app.handle(Request("GET", "/unknown")))
}
```

---

#### Using Standard Library DSLs

The Kotlin standard library also has DSL patterns built in.

```kotlin
// buildString: StringBuilderScope
val result = buildString {
    append("Hello")
    append(", ")
    append("World")
    appendLine("!")
    repeat(3) { append("Kotlin ") }
}
println(result)

// buildList: immutable list builder
val numbers = buildList {
    add(1)
    addAll(listOf(2, 3, 4))
    if (true) add(5)
}

// buildMap
val config = buildMap<String, Any> {
    put("host", "localhost")
    put("port", 8080)
    putAll(mapOf("debug" to true, "timeout" to 30))
}
```

---

#### DSL Design Checklist

{{< callout type="info" title="Principles of Good DSL Design" >}}
- **Minimal Principle**: A DSL should expose only what is needed. Hide internal implementation with `internal` or `private`.
- **Compile-time Verification**: Invalid combinations should be found at compile time, not runtime.
- **Apply `@DslMarker`**: Always isolate context in nested DSLs.
- **Immutable Results**: Design objects built by the builder to be immutable, like `data class`.
- **Provide Defaults**: Provide reasonable defaults for all options so they work with minimal configuration.
{{< /callout >}}

---

#### Key Points

{{< callout type="info" title="Key Takeaways" >}}
- `A.() -> Unit` is a type that lets you call `A`'s members without `this` inside the lambda.
- Builder class + lambda with receiver = the basic structure of the type-safe builder pattern.
- `@DslMarker` blocks calls to outer scope methods in nested DSLs at compile time.
- `buildString`, `buildList`, `buildMap` are DSL examples from the standard library.
{{< /callout >}}

---

#### Next Steps

- [Multiplatform Overview](../multiplatform-overview/) — Platform-specific DSL implementations with expect/actual in KMP
- [Inline/Reified](../inline-reified/) — Inline functions used for DSL performance optimization
- [Gradle Kotlin DSL Tips](../../howto/gradle-kotlin-dsl-tips/) — Real-world build script DSLs
