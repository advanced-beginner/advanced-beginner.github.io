---
title: "Basic Examples"
weight: 2
description: "Practice Kotlin's core features — from Hello Kotlin to data classes, collection handling, extension functions, and a small CLI program."
lastmod: "2026-05-13"
---

> **Estimated time**: about 15 minutes

{{< callout type="tip" title="TL;DR" >}}
- Define concise data models with `data class`
- Use collection functions like `filter`, `map`, `sortedBy`
- Add domain-specific behavior to existing types via extension functions
- Make active use of Kotlin expressions like `when`, `?:`, `?.`
{{< /callout >}}

**Target audience**: Developers who have completed Kotlin environment setup
**Prerequisites**: [Environment Setup](setup/)

---

Run Kotlin's core features directly in your installed environment. Each example creates a file under `src/main/kotlin/` and runs it.

#### Step 1 — Hello Kotlin

Start with the simplest Kotlin program.

```kotlin
// src/main/kotlin/hello/Hello.kt
package hello

fun main() {
    println("Hello, Kotlin!")

    // Variables
    val name = "Kotlin"          // val: read-only
    var version = "2.0"          // var: mutable

    // String templates
    println("Language: $name $version")
    println("Length: ${name.length} chars")

    // Conditional expression — if is an expression
    val message = if (version >= "2.0") "Latest version" else "Older version"
    println(message)

    // when — enhanced switch
    val score = 85
    val grade = when {
        score >= 90 -> "A"
        score >= 80 -> "B"
        score >= 70 -> "C"
        else -> "F"
    }
    println("Score: $score, Grade: $grade")
}
```

```bash
./gradlew run --args="hello"
# Or click the ▶ next to main() in IntelliJ
```

#### Step 2 — Modeling Users with a Data Class

`data class` automatically generates `equals`, `hashCode`, `toString`, and `copy`.

```kotlin
// src/main/kotlin/model/User.kt
package model

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val role: Role = Role.USER   // Default value
)

enum class Role { USER, ADMIN, MODERATOR }

// Usage example
fun main() {
    val user1 = User(1, "Alice", "alice@example.com", 30)
    val user2 = User(2, "Bob", "bob@example.com", 25, Role.ADMIN)

    println(user1)
    // User(id=1, name=Alice, email=alice@example.com, age=30, role=USER)

    // copy — a copy with selected fields changed
    val promoted = user1.copy(role = Role.MODERATOR)
    println(promoted.role)    // MODERATOR

    // Destructuring
    val (id, name, email) = user1
    println("$id: $name <$email>")

    // Equality — value-based
    val same = user1.copy()
    println(user1 == same)    // true
}
```

#### Step 3 — Collection Handling

Kotlin collections process data in a functional style.

```kotlin
// src/main/kotlin/collection/CollectionExample.kt
package collection

import model.Role
import model.User

fun main() {
    val users = listOf(
        User(1, "Alice", "alice@example.com", 30),
        User(2, "Bob", "bob@example.com", 25, Role.ADMIN),
        User(3, "Charlie", "charlie@example.com", 35),
        User(4, "Diana", "diana@example.com", 28, Role.ADMIN),
        User(5, "Eve", "eve@example.com", 22)
    )

    // filter — only items that match a condition
    val admins = users.filter { it.role == Role.ADMIN }
    println("Admins: ${admins.map { it.name }}")
    // Admins: [Bob, Diana]

    // map — transform
    val names = users.map { it.name }
    println("Names: $names")

    // sortedBy — sort
    val byAge = users.sortedBy { it.age }
    println("By age: ${byAge.map { "${it.name}(${it.age})" }}")

    // find — first item matching a condition (null if none)
    val youngest = users.minByOrNull { it.age }
    println("Youngest: ${youngest?.name}")

    // groupBy — group
    val byRole = users.groupBy { it.role }
    byRole.forEach { (role, list) ->
        println("$role: ${list.map { it.name }}")
    }

    // any, all, none — condition checks
    println("Any 30+: ${users.any { it.age >= 30 }}")
    println("All adults: ${users.all { it.age >= 18 }}")

    // count
    println("Admin count: ${users.count { it.role == Role.ADMIN }}")

    // Chaining — combine multiple operations
    val result = users
        .filter { it.age >= 25 }
        .sortedByDescending { it.age }
        .take(3)
        .map { "${it.name}(${it.age})" }
    println("Top 3 aged 25+: $result")

    // Mutable collections
    val mutableList = mutableListOf<User>()
    mutableList.add(users[0])
    mutableList.addAll(users.filter { it.role == Role.ADMIN })

    // Map usage
    val userMap: Map<Long, User> = users.associateBy { it.id }
    val found = userMap[2L]
    println("ID 2: ${found?.name}")
}
```

#### Step 4 — Using Extension Functions

Write domain-specific extension functions to improve readability.

```kotlin
// src/main/kotlin/extension/Extensions.kt
package extension

import model.Role
import model.User

// Extend User with business logic
fun User.isAdult() = age >= 18
fun User.hasAdminAccess() = role == Role.ADMIN || role == Role.MODERATOR
fun User.displayName() = "[$role] $name"
fun User.maskEmail(): String {
    val parts = email.split("@")
    return "${parts[0].take(3)}***@${parts[1]}"
}

// Domain-specific extensions on List<User>
fun List<User>.admins() = filter { it.role == Role.ADMIN }
fun List<User>.averageAge() = map { it.age }.average()
fun List<User>.findByEmail(email: String) = find { it.email == email }

// String extensions
fun String.toSlug() = lowercase().replace(" ", "-").replace("[^a-z0-9-]".toRegex(), "")

fun main() {
    val users = listOf(
        User(1, "Alice", "alice@example.com", 30),
        User(2, "Bob", "bob@example.com", 25, Role.ADMIN),
        User(3, "Charlie", "charlie@example.com", 17)
    )

    users.forEach { user ->
        println("${user.displayName()} - adult: ${user.isAdult()}, admin access: ${user.hasAdminAccess()}")
        println("  masked email: ${user.maskEmail()}")
    }

    println("\nAdmin list: ${users.admins().map { it.name }}")
    println("Average age: ${users.averageAge()}")
    println("Email lookup: ${users.findByEmail("bob@example.com")?.name}")

    val title = "Hello Kotlin World"
    println("Slug: ${title.toSlug()}")   // hello-kotlin-world
}
```

#### Step 5 — Null Safety Practice

Practice Kotlin's null-safe handling.

```kotlin
// src/main/kotlin/nullsafety/NullExample.kt
package nullsafety

data class Address(val street: String, val city: String, val zipCode: String?)
data class Person(val name: String, val address: Address?)

fun findPerson(id: Int): Person? {
    return when (id) {
        1 -> Person("Alice", Address("Jongno-gu", "Seoul", "03000"))
        2 -> Person("Bob", null)    // No address
        else -> null
    }
}

fun main() {
    // Safe call operator ?.
    val person1 = findPerson(1)
    println(person1?.address?.city)    // Seoul

    val person2 = findPerson(2)
    println(person2?.address?.city)    // null (no exception)

    val person3 = findPerson(999)
    println(person3?.name)             // null

    // Elvis operator ?:
    val city = findPerson(2)?.address?.city ?: "Address not registered"
    println(city)   // Address not registered

    // Null-safe handling with let
    findPerson(1)?.let { person ->
        println("${person.name}'s zip code: ${person.address?.zipCode ?: "none"}")
    }

    // requireNotNull, checkNotNull
    val id = System.getenv("USER_ID")?.toIntOrNull() ?: 1
    val user = requireNotNull(findPerson(id)) { "User with ID $id not found" }
    println("User: ${user.name}")

    // Smart cast
    val address = person1?.address
    if (address != null) {
        println(address.street)   // Smart-cast to Address
    }
}
```

#### Step 6 — A Small CLI Program

A simple user-management CLI that applies the concepts we've learned.

```kotlin
// src/main/kotlin/cli/UserCli.kt
package cli

import model.Role
import model.User

class UserManager {
    private val users = mutableListOf<User>()
    private var nextId = 1L

    fun addUser(name: String, email: String, age: Int, role: Role = Role.USER): User {
        val user = User(nextId++, name, email, age, role)
        users.add(user)
        println("User added: ${user.name} (ID: ${user.id})")
        return user
    }

    fun listUsers() {
        if (users.isEmpty()) {
            println("No users registered.")
            return
        }
        println("\n=== User list (${users.size}) ===")
        users.sortedBy { it.id }.forEach { user ->
            println("  ${user.id}. [${user.role}] ${user.name} <${user.email}> (age ${user.age})")
        }
    }

    fun search(keyword: String): List<User> {
        return users.filter { it.name.contains(keyword) || it.email.contains(keyword) }
    }

    fun stats() {
        println("\n=== Stats ===")
        println("  Total: ${users.size}")
        println("  Average age: ${"%.1f".format(users.averageOf { it.age.toDouble() })}")
        val roleGroups = users.groupBy { it.role }
        roleGroups.forEach { (role, list) ->
            println("  $role: ${list.size}")
        }
    }

    private fun List<User>.averageOf(selector: (User) -> Double): Double {
        return if (isEmpty()) 0.0 else sumOf(selector) / size
    }
}

fun main() {
    val manager = UserManager()

    // Add users
    manager.addUser("Alice", "alice@example.com", 30, Role.ADMIN)
    manager.addUser("Bob", "bob@example.com", 25)
    manager.addUser("Charlie", "charlie@example.com", 35)
    manager.addUser("Diana", "diana@example.com", 28, Role.MODERATOR)

    // Print list
    manager.listUsers()

    // Search
    val searchResult = manager.search("Bob")
    println("\nSearch result for 'Bob': ${searchResult.map { it.name }}")

    // Stats
    manager.stats()
}
```

Run:

```bash
./gradlew run
```

Expected output:

```text
User added: Alice (ID: 1)
User added: Bob (ID: 2)
User added: Charlie (ID: 3)
User added: Diana (ID: 4)

=== User list (4) ===
  1. [ADMIN] Alice <alice@example.com> (age 30)
  2. [USER] Bob <bob@example.com> (age 25)
  3. [USER] Charlie <charlie@example.com> (age 35)
  4. [MODERATOR] Diana <diana@example.com> (age 28)

Search result for 'Bob': [Bob]

=== Stats ===
  Total: 4
  Average age: 29.5
  ADMIN: 1
  USER: 2
  MODERATOR: 1
```

{{< callout type="info" title="Key takeaways" >}}
- `data class` — concise model definition that auto-generates `toString`, `equals`, `copy`
- Collection functions `filter`, `map`, `groupBy`, `sortedBy` — declarative data processing
- Extension functions — add domain-specific methods to existing types
- `?.`, `?:`, `?.let { }` — the basic null-safety patterns
{{< /callout >}}

---

### Writing Tests

We'll validate the `data class` and extension functions written above using JUnit 5 and AssertJ. Both libraries are included in `spring-boot-starter-test`, so no additional dependencies are needed.

```kotlin
// src/test/kotlin/model/UserTest.kt
package model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

@DisplayName("User data class tests")
class UserTest {

    private val user = User(1L, "Alice", "alice@example.com", 30, Role.USER)

    @Test
    fun `data class provides value-based equality`() {
        val copy = user.copy()
        assertThat(copy).isEqualTo(user)
        assertThat(copy).isNotSameAs(user)  // Reference differs
    }

    @Test
    fun `copy creates a new instance with selected fields changed`() {
        val promoted = user.copy(role = Role.ADMIN)
        assertAll(
            { assertThat(promoted.id).isEqualTo(user.id) },
            { assertThat(promoted.name).isEqualTo(user.name) },
            { assertThat(promoted.role).isEqualTo(Role.ADMIN) }
        )
    }

    @Test
    fun `toString contains all fields`() {
        assertThat(user.toString()).contains("Alice", "alice@example.com", "30")
    }
}
```

Extension functions are also verified with unit tests. Pure functions are easy to test.

```kotlin
// src/test/kotlin/extension/ExtensionsTest.kt
package extension

import model.Role
import model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExtensionsTest {

    @Test
    fun `isAdult considers 18+ as adult`() {
        val adult = User(1L, "Adult", "a@test.com", 18)
        val minor = User(2L, "Minor", "b@test.com", 17)

        assertThat(adult.isAdult()).isTrue()
        assertThat(minor.isAdult()).isFalse()
    }

    @Test
    fun `hasAdminAccess allows only ADMIN and MODERATOR`() {
        val admin = User(1L, "Admin", "a@test.com", 30, Role.ADMIN)
        val mod   = User(2L, "Mod",   "b@test.com", 30, Role.MODERATOR)
        val user  = User(3L, "User",  "c@test.com", 30, Role.USER)

        assertThat(admin.hasAdminAccess()).isTrue()
        assertThat(mod.hasAdminAccess()).isTrue()
        assertThat(user.hasAdminAccess()).isFalse()
    }

    @Test
    fun `maskEmail keeps only the first three characters of the local part`() {
        val user = User(1L, "Test", "alice@example.com", 25)
        assertThat(user.maskEmail()).isEqualTo("ali***@example.com")
    }

    @Test
    fun `toSlug converts to lowercase hyphenated form`() {
        assertThat("Hello Kotlin World".toSlug()).isEqualTo("hello-kotlin-world")
    }
}
```

{{< callout type="tip" title="Running tests" >}}
```bash
./gradlew test
# Or just one class
./gradlew test --tests "model.UserTest"
```
{{< /callout >}}

#### Next Steps

- [Spring Boot Integration](spring-boot-integration/) — build a REST API server in Kotlin
- [Extension Functions](../concepts/extension-functions/) — deep dive into extension function mechanics
