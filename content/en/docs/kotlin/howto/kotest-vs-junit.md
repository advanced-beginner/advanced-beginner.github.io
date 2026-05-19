---
title: "Kotest vs JUnit"
weight: 4
description: "Compare the philosophies and stacks of Kotest and JUnit 5, show common-scenario code examples, walk through migration steps, and demonstrate Spring Boot integration."
lastmod: "2026-05-13"
---

Compare the Kotest and JUnit 5 stacks and walk through choosing the right one for your project and migrating between them.

**Estimated time**: about 15-20 minutes

{{< callout type="tip" title="TL;DR" >}}
- JUnit 5 + AssertJ + MockK: easier for teams migrating from Java, mature Spring Boot integration
- Kotest: Kotlin-native style, BDD specs, built-in property-based testing
- Both work with Spring Boot and can even be mixed in the same project.
- For new pure-Kotlin projects, Kotest is recommended. For Java mix or teams in transition, JUnit 5.
{{< /callout >}}

---

## What This Guide Solves

Use this guide in the following situations:

- When choosing a test framework for a new Kotlin project
- When migrating JUnit 5 tests to Kotest
- When you want to use Kotest in a Spring Boot project
- When you want to use MockK with both JUnit and Kotest

---

## Before You Start

**JUnit 5 + AssertJ + MockK stack:**

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // Includes by default: JUnit 5, AssertJ, Mockito
        // We recommend MockK over Mockito
        exclude(group = "org.mockito")
    }
    testImplementation("io.mockk:mockk:1.13.10")
}
```

**Kotest stack:**

```kotlin
// build.gradle.kts
val kotestVersion = "5.8.1"

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")      // Property-based testing
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")  // Spring integration
    testImplementation("io.mockk:mockk:1.13.10")
}

tasks.test {
    useJUnitPlatform()  // Kotest also runs on JUnit 5 Platform
}
```

---

## Step 1: Philosophy and Approach

**JUnit 5 style:**

- Class- and `@Test`-method-based
- Annotation-centric (`@BeforeEach`, `@AfterAll`, `@ParameterizedTest`)
- Imperative style

**Kotest style:**

- Inherits Spec classes (several spec styles available)
- DSL-centric (nested lambda blocks)
- Declarative style

---

## Step 2: Common Scenario Code Comparisons

### Basic Test

**JUnit 5 style:**

```kotlin
import org.junit.jupiter.api.*
import org.assertj.core.api.Assertions.*

class UserServiceTest {
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userService = UserService()
    }

    @Test
    fun `returns the user name`() {
        val result = userService.getUserName(1)
        assertThat(result).isEqualTo("Alice")
    }

    @Test
    fun `non-existent user throws an exception`() {
        assertThatThrownBy { userService.getUserName(-1) }
            .isInstanceOf(NoSuchElementException::class.java)
            .hasMessage("User not found: -1")
    }

    @AfterEach
    fun teardown() {
        // Cleanup
    }
}
```

**Kotest style (StringSpec):**

```kotlin
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow

class UserServiceTest : StringSpec({
    val userService = UserService()

    "returns the user name" {
        userService.getUserName(1) shouldBe "Alice"
    }

    "non-existent user throws an exception" {
        shouldThrow<NoSuchElementException> {
            userService.getUserName(-1)
        }.message shouldBe "User not found: -1"
    }
})
```

**Kotest style (BehaviorSpec — BDD style):**

```kotlin
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class UserServiceBehaviorTest : BehaviorSpec({
    val userService = UserService()

    given("a valid user ID") {
        `when`("looking up the user name") {
            val result = userService.getUserName(1)
            then("Alice is returned") {
                result shouldBe "Alice"
            }
        }
    }

    given("an invalid user ID") {
        `when`("looking up the user name") {
            then("NoSuchElementException is thrown") {
                io.kotest.assertions.throwables.shouldThrow<NoSuchElementException> {
                    userService.getUserName(-1)
                }
            }
        }
    }
})
```

---

### Using MockK

**JUnit 5 + MockK:**

```kotlin
import io.mockk.*
import org.junit.jupiter.api.*
import org.assertj.core.api.Assertions.*

class OrderServiceTest {
    private val orderRepository: OrderRepository = mockk()
    private val paymentService: PaymentService = mockk()
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setup() {
        orderService = OrderService(orderRepository, paymentService)
        every { orderRepository.findById(any()) } returns null
    }

    @Test
    fun `order lookup succeeds`() {
        val order = Order("1", "userA", 10000)
        every { orderRepository.findById("1") } returns order

        val result = orderService.getOrder("1")
        assertThat(result).isEqualTo(order)
        verify(exactly = 1) { orderRepository.findById("1") }
    }

    @Test
    fun `processes order payment`() {
        val order = Order("2", "userB", 20000)
        every { orderRepository.findById("2") } returns order
        every { paymentService.process(any(), any()) } just runs

        orderService.pay("2")
        verify { paymentService.process(order, 20000) }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }
}
```

**Kotest + MockK (FunSpec):**

```kotlin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

class OrderServiceKotestTest : FunSpec({
    val orderRepository = mockk<OrderRepository>()
    val paymentService = mockk<PaymentService>()
    val orderService = OrderService(orderRepository, paymentService)

    beforeEach {
        every { orderRepository.findById(any()) } returns null
    }

    afterEach {
        clearAllMocks()
    }

    test("order lookup succeeds") {
        val order = Order("1", "userA", 10000)
        every { orderRepository.findById("1") } returns order

        orderService.getOrder("1") shouldBe order
        verify(exactly = 1) { orderRepository.findById("1") }
    }

    test("processes order payment") {
        val order = Order("2", "userB", 20000)
        every { orderRepository.findById("2") } returns order
        every { paymentService.process(any(), any()) } just runs

        orderService.pay("2")
        verify { paymentService.process(order, 20000) }
    }
})
```

---

### Coroutine Tests

**JUnit 5 + kotlinx-coroutines-test:**

```kotlin
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*

class CoroutineServiceTest {

    @Test
    fun `suspend function test`() = runTest {
        val service = CoroutineService()
        val result = service.fetchData()
        assertEquals("data", result)
    }

    @Test
    fun `time delay test`() = runTest {
        val service = TimedService()
        var called = false

        launch {
            delay(1000)
            called = true
        }

        advanceTimeBy(1001)
        assertTrue(called)
    }
}
```

**Kotest + coroutines (DescribeSpec):**

```kotlin
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.*

class CoroutineServiceKotestTest : DescribeSpec({
    describe("CoroutineService") {
        val service = CoroutineService()

        it("suspend function returns data") {
            // Kotest supports suspend tests out of the box
            val result = service.fetchData()
            result shouldBe "data"
        }
    }
})
```

---

### Property-based Testing (Kotest exclusive)

One of Kotest's strengths is property-based testing.

```kotlin
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.*
import io.kotest.property.arbitrary.*
import io.kotest.matchers.shouldBe

class PropertyTest : StringSpec({
    "reversing a string twice equals the original" {
        checkAll<String> { str ->
            str.reversed().reversed() shouldBe str
        }
    }

    "the sum of two positives is always positive" {
        checkAll(
            Arb.positiveInt(),
            Arb.positiveInt()
        ) { a, b ->
            (a + b) shouldBe a + b
            (a + b) > 0 shouldBe true
        }
    }

    "user IDs are always 1 or greater" {
        val userIdArb = Arb.int(1..Int.MAX_VALUE)
        checkAll(userIdArb) { id ->
            val user = createUser(id)
            user.id shouldBe id
        }
    }
})
```

---

## Step 3: Spring Boot Integration

**JUnit 5 (supported out of the box):**

```kotlin
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.junit.jupiter.api.*

@SpringBootTest
class UserControllerIntegrationTest {

    @Autowired
    private lateinit var userController: UserController

    @Test
    fun `controller is loaded`() {
        assertNotNull(userController)
    }

    @Test
    fun `list users`() {
        val users = userController.getAllUsers()
        assertNotNull(users)
    }
}
```

**Kotest + Spring Boot:**

```kotlin
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired

@SpringBootTest
class UserControllerKotestTest(
    @Autowired private val userController: UserController
) : FunSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        test("controller is loaded") {
            userController shouldNotBe null
        }

        test("list users") {
            val users = userController.getAllUsers()
            users shouldNotBe null
        }
    }
}
```

---

## Step 4: Migrate JUnit 5 → Kotest

For large codebases, migrate incrementally.

**Migration strategy:**

1. **Add dependencies**: add Kotest without removing JUnit.
2. **New tests**: write new test files in Kotest.
3. **Existing tests**: convert per module/class.
4. **Verify**: confirm coverage matches after conversion.

**Annotation mapping:**

| JUnit 5 | Kotest equivalent | Notes |
|---------|-------------------|-------|
| `@Test` | `test("name") { }` | FunSpec/StringSpec |
| `@BeforeEach` | `beforeEach { }` | Block inside the Spec |
| `@AfterEach` | `afterEach { }` | Block inside the Spec |
| `@BeforeAll` | `beforeSpec { }` | |
| `@AfterAll` | `afterSpec { }` | |
| `@Nested` | `describe / context { }` | DescribeSpec |
| `@ParameterizedTest` | `withData { }` or `checkAll` | Property test |
| `@Disabled` | `xtest("name") { }` | `x` prefix |

---

## Step 5: Selection Guide

| Situation | Recommendation |
|-----------|---------------|
| New pure-Kotlin project | Kotest |
| Mixed Java-Kotlin project | JUnit 5 + MockK |
| Existing JUnit 4 → upgrade | JUnit 5 (easier incremental migration) |
| Need BDD-style tests | Kotest BehaviorSpec |
| Need property-based testing | Kotest Property |
| Team familiar with JUnit | Stay on JUnit 5 |
| Prefer first-class Spring Boot support | JUnit 5 |

---

## Checklist

- [ ] Is `useJUnitPlatform()` set on the test task? (Required for Kotest too)
- [ ] Is `clearAllMocks()` / `unmockkAll()` placed in teardown for MockK?
- [ ] Are coroutine suspend functions tested with `runTest` (JUnit) or Kotest's built-in suspend support?
- [ ] Is `SpringExtension` registered in Spring Boot integration tests? (When using Kotest)

---

## Related Documents

- [Coroutine Debugging](coroutine-debugging/) — debugging coroutine tests
- [Gradle Kotlin DSL Tips](gradle-kotlin-dsl-tips/) — configuring test dependencies
- [Performance Profiling](performance-profiling/) — test execution performance
