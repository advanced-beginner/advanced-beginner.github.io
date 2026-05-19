---
title: "Spring Boot Integration"
weight: 3
description: "Build a REST API with Kotlin + Spring Boot 3.2.x. Covers build.gradle.kts configuration, writing controllers/services/entities, and the JPA + Kotlin open-class issue and its solution."
lastmod: "2026-05-13"
---

> **Estimated time**: about 20 minutes

{{< callout type="tip" title="TL;DR" >}}
- Select Kotlin + Spring Web + Spring Data JPA on `start.spring.io`
- `kotlin-spring` and `kotlin-jpa` plugins are essential in `build.gradle.kts`
- The JPA entity `open` class issue → resolved automatically by the `kotlin-jpa` plugin
- You can write `@RestController`, `@Service`, `@Entity` in Kotlin style
{{< /callout >}}

**Target audience**: Developers with Kotlin basics and Spring Boot experience
**Prerequisites**: [Environment Setup](setup/), Spring Boot basics

---

Build a simple user-management REST API by integrating Kotlin with Spring Boot 3.2.x.

#### Step 1 — Project Creation

Generate the project on [start.spring.io](https://start.spring.io).

| Field | Value |
|-------|-------|
| Project | Gradle - Kotlin |
| Language | Kotlin |
| Spring Boot | 3.2.x |
| Java | 17 |
| Dependencies | Spring Web, Spring Data JPA, H2 Database, Spring Boot DevTools |

**Download and unzip the ZIP**, or create it directly via IntelliJ IDEA: **File** → **New** → **Project** → **Spring Initializr**.

#### Step 2 — build.gradle.kts Configuration

Configure the essential plugins and dependencies for Kotlin + Spring Boot.

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"   // Auto-opens @Component, etc.
    kotlin("plugin.jpa") version "2.0.0"      // Auto-opens JPA entities
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")  // Strengthen null safety
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("com.h2database:h2")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

{{< callout type="info" title="What the kotlin-spring plugin does" >}}
Spring's CGLIB proxies work by subclassing. Kotlin classes are `final` by default, which prevents subclassing. The `kotlin-spring` plugin automatically makes classes annotated with `@Component`, `@Service`, `@Repository`, `@Controller`, `@Configuration` `open`.
{{< /callout >}}

{{< callout type="warning" title="Real error you'll see if you forget the plugin" >}}
If you write a Kotlin `@Service` class without the `kotlin-spring` plugin and run it, you'll see:

```text
org.springframework.aop.framework.AopConfigException:
  Could not generate CGLIB subclass of class com.example.MyService:
  Common causes of this problem include using a final class
  or a non-visible class
```

**Fix**: Add `kotlin("plugin.spring") version "..."` to `build.gradle.kts`, and classes annotated with `@Component`/`@Service` will be made `open` automatically.
{{< /callout >}}

#### Step 3 — Application Entry Point

```kotlin
// src/main/kotlin/com/example/demo/DemoApplication.kt
package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
```

#### Step 4 — Entity Definition

```kotlin
// src/main/kotlin/com/example/demo/user/User.kt
package com.example.demo.user

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(                       // JPA entity needs to be open class — kotlin-jpa plugin handles this automatically
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,         // var + nullable so JPA can populate the ID

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var age: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER
)

enum class Role { USER, ADMIN, MODERATOR }
```

{{< callout type="info" title="Caveats when using JPA + Kotlin" >}}
JPA entities require a no-arg constructor. The `kotlin-jpa` plugin automatically adds a no-arg constructor to classes annotated with `@Entity`, `@MappedSuperclass`, or `@Embeddable`. It also makes classes and members `open` so Hibernate's Lazy Loading (proxies) works correctly.
{{< /callout >}}

{{< callout type="warning" title="Production pitfall ① — Don't use data class for JPA entities" >}}
The TL;DR notes "use a regular `class` for JPA entities," but you need to understand **why `data class` is dangerous** to prevent production incidents.

**Root cause**: For `data class`, the compiler generates `equals()`/`hashCode()` that compares **every property**. Hibernate injects a **proxy object** rather than the real one during Lazy Loading. Accessing that proxy tries to compare the uninitialized collections too, producing one of the following errors.

- `LazyInitializationException`: when a lazy collection is accessed after the transaction ends
- Infinite recursion: when `equals()` references each other across a bidirectional relationship
{{< /callout >}}

**Anti-pattern vs recommended pattern**

```kotlin
// ❌ Anti-pattern — data class as a JPA entity
@Entity
data class Order(
    @Id @GeneratedValue val id: Long? = null,
    val orderNo: String,
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    val items: MutableList<OrderItem> = mutableListOf()
    // equals/hashCode includes items, so proxy access risks LazyInitializationException
)

// ✅ Recommended pattern — regular class + id-based equals/hashCode
@Entity
class Order(
    @Id @GeneratedValue
    val id: Long? = null,
    val orderNo: String,
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    val items: MutableList<OrderItem> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Order) return false
        return id != null && id == other.id   // Compare by id only — proxy-safe
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "Order(id=$id, orderNo=$orderNo)"
}
```

{{< callout type="warning" title="Production pitfall ② — Lazy Loading and suspend function conflicts" >}}
Mixing Kotlin coroutines' `suspend` functions with JPA Lazy Loading can trigger `LazyInitializationException` because of Hibernate Session boundary issues.

**Cause**: `suspend` functions may switch threads, so the Hibernate Session bound by `@Transactional` may already be closed when the coroutine resumes.
{{< /callout >}}

```kotlin
// ❌ Bad pattern — accessing a Lazy collection outside the transaction
@Service
class OrderService(private val orderRepository: OrderRepository) {

    // suspend resumes after the transaction has ended → no Hibernate Session
    suspend fun getOrderWithItems(id: Long): OrderDetailResponse {
        val order = orderRepository.findById(id).orElseThrow()
        delay(10) // Hibernate Session is already closed when the coroutine resumes
        return OrderDetailResponse(
            id = order.id!!,
            items = order.items.map { it.toDto() }  // LazyInitializationException!
        )
    }
}

// ✅ Good pattern ① — fetch explicitly inside a @Transactional scope
@Service
class OrderService(private val orderRepository: OrderRepository) {

    @Transactional(readOnly = true)
    fun fetchOrderDetail(id: Long): OrderDetailResponse {
        val order = orderRepository.findById(id).orElseThrow()
        // Lazy collection access inside the transaction → safe
        val items = order.items.map { it.toDto() }
        return OrderDetailResponse(id = order.id!!, items = items)
    }

    // suspend function only processes DTOs returned by the completed transaction
    suspend fun processOrder(id: Long): ProcessedResponse {
        val detail = fetchOrderDetail(id)   // Blocking call to convert to DTO first
        delay(100)                          // No Hibernate Session needed here
        return ProcessedResponse(detail)
    }
}

// ✅ Good pattern ② — fetch everything at once using JPQL / fetch join
@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
    fun findByIdWithItems(id: Long): Order?
}
```

#### Step 5 — Request/Response DTOs

```kotlin
// src/main/kotlin/com/example/demo/user/UserDto.kt
package com.example.demo.user

// Request DTO — use data class
data class CreateUserRequest(
    val name: String,
    val email: String,
    val age: Int,
    val role: Role = Role.USER
)

data class UpdateUserRequest(
    val name: String?,
    val age: Int?
)

// Response DTO
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val role: Role
)

// Conversion extension functions
fun User.toResponse() = UserResponse(
    id = requireNotNull(id) { "Cannot convert a non-persisted User" },
    name = name, email = email, age = age, role = role
)
fun CreateUserRequest.toEntity() = User(name = name, email = email, age = age, role = role)
```

#### Step 6 — Repository

```kotlin
// src/main/kotlin/com/example/demo/user/UserRepository.kt
package com.example.demo.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByRole(role: Role): List<User>
    fun existsByEmail(email: String): Boolean
}
```

#### Step 7 — Service

```kotlin
// src/main/kotlin/com/example/demo/user/UserService.kt
package com.example.demo.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
    fun findAll(): List<UserResponse> = userRepository.findAll().map { it.toResponse() }

    fun findById(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { NoSuchElementException("User with ID $id not found") }
        return user.toResponse()
    }

    @Transactional
    fun create(request: CreateUserRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already in use: ${request.email}")
        }
        val user = userRepository.save(request.toEntity())
        return user.toResponse()
    }

    @Transactional
    fun update(id: Long, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { NoSuchElementException("User with ID $id not found") }
        request.name?.let { user.name = it }
        request.age?.let { user.age = it }
        return user.toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        if (!userRepository.existsById(id)) {
            throw NoSuchElementException("User with ID $id not found")
        }
        userRepository.deleteById(id)
    }
}
```

#### Step 8 — Controller

```kotlin
// src/main/kotlin/com/example/demo/user/UserController.kt
package com.example.demo.user

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping
    fun list(): List<UserResponse> = userService.findAll()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): UserResponse = userService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateUserRequest): UserResponse =
        userService.create(request)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateUserRequest
    ): UserResponse = userService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = userService.delete(id)
}
```

#### Step 9 — Exception Handling

```kotlin
// src/main/kotlin/com/example/demo/common/GlobalExceptionHandler.kt
package com.example.demo.common

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(val message: String, val code: String)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: NoSuchElementException) =
        ErrorResponse(ex.message ?: "Resource not found", "NOT_FOUND")

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequest(ex: IllegalArgumentException) =
        ErrorResponse(ex.message ?: "Bad request", "BAD_REQUEST")
}
```

#### Step 10 — Run and Test

```bash
# Run
./gradlew bootRun
```

```bash
# Create user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","age":30}'

# Response
# {"id":1,"name":"Alice","email":"alice@example.com","age":30,"role":"USER"}

# List
curl http://localhost:8080/api/users

# Single get
curl http://localhost:8080/api/users/1

# Update
curl -X PATCH http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice2","age":31}'

# Delete
curl -X DELETE http://localhost:8080/api/users/1
```

The H2 console is available at `http://localhost:8080/h2-console` (requires `spring.h2.console.enabled=true` in application.yml).

{{< callout type="info" title="Key takeaways" >}}
- `kotlin-spring` plugin — automatically opens Spring components
- `kotlin-jpa` plugin — adds a no-arg constructor to JPA entities automatically
- `data class` is great for DTOs; use a regular `class` for JPA entities
- Extension functions `toResponse()` / `toEntity()` keep DTO conversion logic clean
{{< /callout >}}

{{< callout type="warning" title="Production pitfall ③ — Spring MVC + suspend is not truly non-blocking" >}}
The controller in Step 8 was written without `suspend`, but **using a `suspend` function as a controller handler does not reduce thread occupancy in Spring MVC**.

Spring MVC is based on Servlet threads (the Tomcat thread pool). Even if you register a `suspend` function as a handler, it's internally wrapped with `runBlocking`, which **blocks the thread**. To get the coroutine benefits (thread release and resume), you need **Spring WebFlux**.
{{< /callout >}}

**Spring MVC vs WebFlux + coroutines**

| Aspect | Spring MVC + suspend | Spring WebFlux + coroutines |
|--------|---------------------|-----------------------------|
| Thread model | Tomcat thread pool (blocking) | Netty event loop (non-blocking) |
| Effect of suspend | None (wrapped in runBlocking) | Releases the thread and resumes |
| Existing code compatibility | Reuse Spring MVC as-is | Requires full review of blocking code |
| JPA/JDBC usage | Works naturally | Requires R2DBC or `Dispatchers.IO` |
| Migration cost | Low | High (dependencies, configuration change) |
| Best for | Typical CRUD APIs | High-concurrency, streaming, SSE |

```kotlin
// Spring MVC — suspend controller (not truly non-blocking)
@RestController
class UserController(private val userService: UserService) {
    @GetMapping("/api/users/{id}")
    suspend fun get(@PathVariable id: Long): UserResponse =
        userService.findById(id)   // Internally wrapped in runBlocking → thread is still occupied
}

// Spring WebFlux — truly non-blocking suspend controller
// build.gradle.kts: implementation("org.springframework.boot:spring-boot-starter-webflux")
@RestController
class UserController(private val userService: UserService) {
    @GetMapping("/api/users/{id}")
    suspend fun get(@PathVariable id: Long): UserResponse =
        userService.findById(id)   // Thread released and resumed on Netty event loop
}
```

{{< callout type="info" title="WebFlux migration considerations" >}}
You can't keep using JPA (blocking JDBC) as-is after moving to WebFlux. Two alternatives:

- **R2DBC**: switch to the reactive driver (Spring Data R2DBC, requires query rewrites)
- **`Dispatchers.IO`**: keep JPA, but wrap blocking DB calls with `withContext(Dispatchers.IO) { ... }` to run them on a dedicated thread pool

For most CRUD services, the **Spring MVC + `Dispatchers.IO`** combination is enough. Consider WebFlux when you need SSE, WebSockets, or large-scale concurrent streaming.
{{< /callout >}}

---

### Writing Tests

For controllers, use `@WebMvcTest` to isolate the web layer. For services, replace dependencies with MockK and write unit tests. MockK is a Kotlin-friendly mocking library. Add the following to `build.gradle.kts`.

```kotlin
// build.gradle.kts — dependencies block
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("io.mockk:mockk:1.13.10")
testImplementation("com.ninja-squad:springmockk:4.0.2")  // Provides @MockkBean
```

**Controller slice test (`@WebMvcTest`)**

`@WebMvcTest` loads only the web layer (controllers, filters, `@ControllerAdvice`). Replace the service with `@MockkBean` and verify HTTP response shape and status code.

```kotlin
// src/test/kotlin/com/example/demo/user/UserControllerTest.kt
package com.example.demo.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(UserController::class)
class UserControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {
    @MockkBean
    private lateinit var userService: UserService

    private val sampleResponse = UserResponse(1L, "Alice", "alice@example.com", 30, Role.USER)

    @Test
    fun `GET api-users returns the full list`() {
        every { userService.findAll() } returns listOf(sampleResponse)

        mockMvc.get("/api/users")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].name") { value("Alice") }
                jsonPath("$[0].email") { value("alice@example.com") }
            }
    }

    @Test
    fun `POST api-users creates a user and returns 201`() {
        val request = CreateUserRequest("Alice", "alice@example.com", 30)
        every { userService.create(request) } returns sampleResponse

        mockMvc.post("/api/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.role") { value("USER") }
        }

        verify(exactly = 1) { userService.create(request) }
    }

    @Test
    fun `GET api-users with non-existent ID returns 404`() {
        every { userService.findById(999L) } throws NoSuchElementException("User with ID 999 not found")

        mockMvc.get("/api/users/999")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.code") { value("NOT_FOUND") }
            }
    }
}
```

**Service unit test**

The service is unit-tested without a Spring context. Replace the repository with MockK for fast execution.

```kotlin
// src/test/kotlin/com/example/demo/user/UserServiceTest.kt
package com.example.demo.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class UserServiceTest {

    private val userRepository: UserRepository = mockk()
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository)
    }

    @Test
    fun `creating with an existing email throws an exception`() {
        val request = CreateUserRequest("Alice", "alice@example.com", 30)
        every { userRepository.existsByEmail("alice@example.com") } returns true

        assertThatThrownBy { userService.create(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Email already in use")

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `findById with non-existent ID throws NoSuchElementException`() {
        every { userRepository.findById(99L) } returns Optional.empty()

        assertThatThrownBy { userService.findById(99L) }
            .isInstanceOf(NoSuchElementException::class.java)
    }
}
```

Run the tests with:

```bash
./gradlew test
```

#### Next Steps

- [Kafka Integration](kafka-integration/) — implement Kafka Producer/Consumer in Kotlin with Spring Kafka
- [Practical Coroutines](coroutines-practical/) — build async APIs with suspend controllers

> Tip: **Further reading**: If you want to design a domain layer on top of a REST API, see [DDD Application Layer]({{< relref "/docs/ddd/examples/application-layer" >}}) and [Order Domain Example]({{< relref "/docs/ddd/examples/order-domain" >}}) to firm up controller–service–domain responsibility separation.
