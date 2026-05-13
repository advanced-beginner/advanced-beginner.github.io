---
title: "Kotest vs JUnit"
weight: 4
description: "Kotest와 JUnit 5의 철학 차이, 스택 비교, 공통 시나리오 코드 예제, 마이그레이션 절차, Spring Boot 통합 방법을 안내합니다."
lastmod: "2026-05-13"
---

Kotest와 JUnit 5 스택을 비교하고, 프로젝트에 맞는 선택과 마이그레이션 절차를 안내합니다.

**소요 시간**: 약 15~20분

{{< callout type="tip" title="TL;DR" >}}
- JUnit 5 + AssertJ + MockK: 기존 Java 팀의 마이그레이션에 유리, Spring Boot 통합 성숙
- Kotest: Kotlin 네이티브 스타일, BDD 스펙, Property-based Testing 내장
- 둘 다 Spring Boot에서 사용 가능하며, 동시에 혼용할 수도 있습니다.
- 신규 순수 Kotlin 프로젝트라면 Kotest, Java 혼용 또는 팀 전환 중이라면 JUnit 5를 권장합니다.
{{< /callout >}}

---

## 이 가이드가 해결하는 문제

다음 상황에서 이 가이드를 사용하세요:

- 새 Kotlin 프로젝트에서 어떤 테스트 프레임워크를 선택할지 결정이 필요할 때
- JUnit 5로 작성된 테스트를 Kotest로 마이그레이션하려고 할 때
- Spring Boot 프로젝트에서 Kotest를 사용하고 싶을 때
- MockK를 JUnit과 Kotest 모두에서 사용하는 방법을 알고 싶을 때

---

## 시작하기 전에

**JUnit 5 + AssertJ + MockK 스택:**

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // 기본 포함: JUnit 5, AssertJ, Mockito
        // Mockito 대신 MockK 사용 권장
        exclude(group = "org.mockito")
    }
    testImplementation("io.mockk:mockk:1.13.10")
}
```

**Kotest 스택:**

```kotlin
// build.gradle.kts
val kotestVersion = "5.8.1"

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")      // Property-based Testing
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")  // Spring 통합
    testImplementation("io.mockk:mockk:1.13.10")
}

tasks.test {
    useJUnitPlatform()  // Kotest도 JUnit 5 Platform 위에서 실행됨
}
```

---

## 1단계: 철학과 접근 방식 비교

**JUnit 5 스타일:**

- 클래스와 `@Test` 메서드 기반
- 어노테이션 중심 (`@BeforeEach`, `@AfterAll`, `@ParameterizedTest`)
- 명령형(imperative) 스타일

**Kotest 스타일:**

- Spec 클래스 상속 기반 (여러 스펙 스타일 제공)
- DSL 중심 (람다 블록 중첩)
- 선언형(declarative) 스타일

---

## 2단계: 공통 시나리오 코드 비교

### 기본 테스트

**JUnit 5 방식:**

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
    fun `사용자 이름 반환`() {
        val result = userService.getUserName(1)
        assertThat(result).isEqualTo("Alice")
    }

    @Test
    fun `존재하지 않는 사용자 - 예외 발생`() {
        assertThatThrownBy { userService.getUserName(-1) }
            .isInstanceOf(NoSuchElementException::class.java)
            .hasMessage("사용자 없음: -1")
    }

    @AfterEach
    fun teardown() {
        // 정리
    }
}
```

**Kotest 방식 (StringSpec):**

```kotlin
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow

class UserServiceTest : StringSpec({
    val userService = UserService()

    "사용자 이름 반환" {
        userService.getUserName(1) shouldBe "Alice"
    }

    "존재하지 않는 사용자 - 예외 발생" {
        shouldThrow<NoSuchElementException> {
            userService.getUserName(-1)
        }.message shouldBe "사용자 없음: -1"
    }
})
```

**Kotest 방식 (BehaviorSpec - BDD 스타일):**

```kotlin
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class UserServiceBehaviorTest : BehaviorSpec({
    val userService = UserService()

    given("유효한 사용자 ID") {
        `when`("사용자 이름을 조회하면") {
            val result = userService.getUserName(1)
            then("Alice가 반환된다") {
                result shouldBe "Alice"
            }
        }
    }

    given("유효하지 않은 사용자 ID") {
        `when`("사용자 이름을 조회하면") {
            then("NoSuchElementException이 발생한다") {
                io.kotest.assertions.throwables.shouldThrow<NoSuchElementException> {
                    userService.getUserName(-1)
                }
            }
        }
    }
})
```

---

### MockK 사용

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
    fun `주문 조회 성공`() {
        val order = Order("1", "사용자A", 10000)
        every { orderRepository.findById("1") } returns order

        val result = orderService.getOrder("1")
        assertThat(result).isEqualTo(order)
        verify(exactly = 1) { orderRepository.findById("1") }
    }

    @Test
    fun `주문 결제 처리`() {
        val order = Order("2", "사용자B", 20000)
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

    test("주문 조회 성공") {
        val order = Order("1", "사용자A", 10000)
        every { orderRepository.findById("1") } returns order

        orderService.getOrder("1") shouldBe order
        verify(exactly = 1) { orderRepository.findById("1") }
    }

    test("주문 결제 처리") {
        val order = Order("2", "사용자B", 20000)
        every { orderRepository.findById("2") } returns order
        every { paymentService.process(any(), any()) } just runs

        orderService.pay("2")
        verify { paymentService.process(order, 20000) }
    }
})
```

---

### 코루틴 테스트

**JUnit 5 + kotlinx-coroutines-test:**

```kotlin
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*

class CoroutineServiceTest {

    @Test
    fun `suspend 함수 테스트`() = runTest {
        val service = CoroutineService()
        val result = service.fetchData()
        assertEquals("데이터", result)
    }

    @Test
    fun `시간 지연 테스트`() = runTest {
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

**Kotest + 코루틴 (DescribeSpec):**

```kotlin
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.*

class CoroutineServiceKotestTest : DescribeSpec({
    describe("CoroutineService") {
        val service = CoroutineService()

        it("suspend 함수가 데이터를 반환한다") {
            // Kotest는 suspend 테스트를 기본 지원
            val result = service.fetchData()
            result shouldBe "데이터"
        }
    }
})
```

---

### Property-based Testing (Kotest 전용)

Kotest의 강점 중 하나인 Property-based Testing입니다.

```kotlin
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.*
import io.kotest.property.arbitrary.*
import io.kotest.matchers.shouldBe

class PropertyTest : StringSpec({
    "문자열 뒤집기는 두 번 하면 원본과 같다" {
        checkAll<String> { str ->
            str.reversed().reversed() shouldBe str
        }
    }

    "양수끼리의 합은 항상 양수다" {
        checkAll(
            Arb.positiveInt(),
            Arb.positiveInt()
        ) { a, b ->
            (a + b) shouldBe a + b
            (a + b) > 0 shouldBe true
        }
    }

    "사용자 ID는 항상 1 이상이다" {
        val userIdArb = Arb.int(1..Int.MAX_VALUE)
        checkAll(userIdArb) { id ->
            val user = createUser(id)
            user.id shouldBe id
        }
    }
})
```

---

## 3단계: Spring Boot 통합

**JUnit 5 (기본 지원):**

```kotlin
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.junit.jupiter.api.*

@SpringBootTest
class UserControllerIntegrationTest {

    @Autowired
    private lateinit var userController: UserController

    @Test
    fun `컨트롤러 로드 확인`() {
        assertNotNull(userController)
    }

    @Test
    fun `사용자 목록 조회`() {
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
        test("컨트롤러 로드 확인") {
            userController shouldNotBe null
        }

        test("사용자 목록 조회") {
            val users = userController.getAllUsers()
            users shouldNotBe null
        }
    }
}
```

---

## 4단계: JUnit 5 → Kotest 마이그레이션

대규모 코드베이스는 점진적으로 마이그레이션합니다.

**마이그레이션 전략:**

1. **의존성 추가**: Kotest를 추가하되 JUnit을 제거하지 않습니다.
2. **신규 테스트**: 새 테스트 파일은 Kotest로 작성합니다.
3. **기존 테스트**: 모듈/클래스 단위로 변환합니다.
4. **검증**: 변환 후 기존과 동일한 커버리지를 확인합니다.

**어노테이션 대응표:**

| JUnit 5 | Kotest 대응 | 비고 |
|---------|-----------|------|
| `@Test` | `test("이름") { }` | FunSpec/StringSpec |
| `@BeforeEach` | `beforeEach { }` | Spec 내 블록 |
| `@AfterEach` | `afterEach { }` | Spec 내 블록 |
| `@BeforeAll` | `beforeSpec { }` | |
| `@AfterAll` | `afterSpec { }` | |
| `@Nested` | `describe / context { }` | DescribeSpec |
| `@ParameterizedTest` | `withData { }` 또는 `checkAll` | Property Test |
| `@Disabled` | `xtest("이름") { }` | x 접두사 |

---

## 5단계: 선택 가이드

| 상황 | 권장 |
|------|------|
| 신규 순수 Kotlin 프로젝트 | Kotest |
| Java-Kotlin 혼용 프로젝트 | JUnit 5 + MockK |
| 기존 JUnit 4 → 업그레이드 | JUnit 5 (점진적 전환 용이) |
| BDD 스타일 테스트 필요 | Kotest BehaviorSpec |
| Property-based Testing 필요 | Kotest Property |
| 팀이 JUnit에 익숙 | JUnit 5 유지 |
| Spring Boot 공식 지원 우선 | JUnit 5 |

---

## 체크리스트

- [ ] `useJUnitPlatform()`이 test task에 설정됐는가? (Kotest도 필요)
- [ ] MockK의 `clearAllMocks()` / `unmockkAll()`이 teardown에 있는가?
- [ ] 코루틴 suspend 함수는 `runTest`(JUnit) 또는 Kotest의 기본 suspend 지원으로 테스트하는가?
- [ ] Spring Boot 통합 테스트에서 `SpringExtension`이 등록됐는가? (Kotest 사용 시)

---

## 관련 문서

- [코루틴 디버깅](coroutine-debugging/) — 코루틴 테스트 디버깅
- [Gradle Kotlin DSL 팁](gradle-kotlin-dsl-tips/) — 테스트 의존성 설정
- [성능 프로파일링](performance-profiling/) — 테스트 실행 성능
