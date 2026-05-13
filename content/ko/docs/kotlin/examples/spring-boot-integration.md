---
title: "Spring Boot 연동"
weight: 3
description: "Kotlin + Spring Boot 3.2.x로 REST API를 구성합니다. build.gradle.kts 설정, 컨트롤러/서비스/엔티티 작성, JPA + Kotlin 시 open 클래스 이슈와 해결을 다룹니다."
lastmod: "2026-05-13"
---

> **소요 시간**: 약 20분

{{< callout type="tip" title="TL;DR" >}}
- `start.spring.io`에서 Kotlin + Spring Web + Spring Data JPA 선택
- `build.gradle.kts`에 `kotlin-spring`, `kotlin-jpa` 플러그인 필수
- JPA 엔티티에 `open` 클래스 이슈 → `kotlin-jpa` 플러그인이 자동 해결
- `@RestController`, `@Service`, `@Entity` 모두 Kotlin 방식으로 작성 가능
{{< /callout >}}

**대상 독자**: Kotlin 기초와 Spring Boot 경험이 있는 개발자
**선수 지식**: [환경 설정](setup/), Spring Boot 기초

---

Kotlin과 Spring Boot 3.2.x를 연동하여 간단한 사용자 관리 REST API를 구축합니다.

#### Step 1 — 프로젝트 생성

[start.spring.io](https://start.spring.io)에서 프로젝트를 생성합니다.

| 항목 | 선택값 |
|------|--------|
| Project | Gradle - Kotlin |
| Language | Kotlin |
| Spring Boot | 3.2.x |
| Java | 17 |
| Dependencies | Spring Web, Spring Data JPA, H2 Database, Spring Boot DevTools |

**ZIP을 다운로드하여 압축을 해제** 하거나, IntelliJ IDEA에서 **File** → **New** → **Project** → **Spring Initializr** 로 직접 생성합니다.

#### Step 2 — build.gradle.kts 설정

Kotlin + Spring Boot를 위한 핵심 플러그인과 의존성을 설정합니다.

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"   // @Component 등 자동 open
    kotlin("plugin.jpa") version "2.0.0"      // JPA 엔티티 자동 open
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")  // null 안전성 강화
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

{{< callout type="info" title="kotlin-spring 플러그인이 하는 일" >}}
Spring의 CGLIB 프록시는 클래스를 상속하여 동작합니다. Kotlin의 클래스는 기본적으로 `final`이므로 상속이 불가능합니다. `kotlin-spring` 플러그인은 `@Component`, `@Service`, `@Repository`, `@Controller`, `@Configuration` 등이 붙은 클래스를 자동으로 `open`으로 만들어 줍니다.
{{< /callout >}}

{{< callout type="warning" title="플러그인을 빠뜨리면 만나는 실제 에러" >}}
`kotlin-spring` 플러그인 없이 Kotlin으로 `@Service` 클래스를 작성하고 실행하면 다음과 같은 에러가 발생합니다.

```text
org.springframework.aop.framework.AopConfigException:
  Could not generate CGLIB subclass of class com.example.MyService:
  Common causes of this problem include using a final class
  or a non-visible class
```

**해결**: `build.gradle.kts`에 `kotlin("plugin.spring") version "..."`을 추가하면 `@Component`/`@Service` 등이 붙은 클래스가 자동으로 `open` 처리됩니다.
{{< /callout >}}

#### Step 3 — 애플리케이션 진입점

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

#### Step 4 — 엔티티 정의

```kotlin
// src/main/kotlin/com/example/demo/user/User.kt
package com.example.demo.user

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(                       // JPA 엔티티는 open class 필요 — kotlin-jpa 플러그인이 자동 처리
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,         // JPA가 ID를 채울 수 있도록 var + nullable 권장

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

{{< callout type="info" title="JPA + Kotlin 주의사항" >}}
JPA 엔티티는 기본 생성자가 필요합니다. `kotlin-jpa` 플러그인이 `@Entity`, `@MappedSuperclass`, `@Embeddable`이 붙은 클래스에 인수 없는 생성자를 자동으로 추가합니다. 또한 클래스와 멤버를 자동으로 `open`으로 만들어 Hibernate의 Lazy Loading(프록시)이 동작하도록 합니다.
{{< /callout >}}

{{< callout type="warning" title="운영 함정 ① — JPA 엔티티에 data class 사용 금지" >}}
TL;DR 에서는 "JPA 엔티티에는 일반 `class` 권장"이라고 언급하지만, **왜 `data class`를 쓰면 안 되는지** 명확히 이해해야 운영 장애를 예방할 수 있습니다.

**문제 원인**: `data class`는 컴파일러가 `equals()`/`hashCode()`를 **모든 프로퍼티를 비교**하는 방식으로 자동 생성합니다. Hibernate는 Lazy Loading 시 실제 객체 대신 **프록시 객체**를 주입하는데, 이 프록시에 접근하면 미초기화 컬렉션까지 비교를 시도하여 다음 두 가지 오류가 발생합니다.

- `LazyInitializationException`: 트랜잭션 종료 후 Lazy 컬렉션 접근 시
- 무한 재귀: 양방향 관계에서 `equals()`가 서로를 참조하는 경우
{{< /callout >}}

**안티 패턴 vs 권장 패턴**

```kotlin
// ❌ 안티 패턴 — data class를 JPA 엔티티로 사용
@Entity
data class Order(
    @Id @GeneratedValue val id: Long? = null,
    val orderNo: String,
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    val items: MutableList<OrderItem> = mutableListOf()
    // equals/hashCode가 items를 포함하므로, 프록시 접근 시 LazyInitializationException 위험
)

// ✅ 권장 패턴 — 일반 class + id 기반 equals/hashCode 직접 오버라이드
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
        return id != null && id == other.id   // id만 비교 — 프록시 안전
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "Order(id=$id, orderNo=$orderNo)"
}
```

{{< callout type="warning" title="운영 함정 ② — Lazy Loading과 suspend 함수 충돌" >}}
Kotlin 코루틴의 `suspend` 함수와 JPA Lazy Loading을 함께 사용하면 Hibernate Session 경계 문제로 `LazyInitializationException`이 발생할 수 있습니다.

**원인**: `suspend` 함수는 스레드를 전환할 수 있으므로, `@Transactional`이 바인딩한 Hibernate Session이 코루틴 재개 시점에 이미 닫혀 있을 수 있습니다.
{{< /callout >}}

```kotlin
// ❌ 잘못된 패턴 — 트랜잭션 밖에서 Lazy 컬렉션 접근
@Service
class OrderService(private val orderRepository: OrderRepository) {

    // 트랜잭션 종료 후 suspend 재개 → Hibernate Session 없음
    suspend fun getOrderWithItems(id: Long): OrderDetailResponse {
        val order = orderRepository.findById(id).orElseThrow()
        delay(10) // 코루틴 재개 후 Hibernate Session이 이미 닫힘
        return OrderDetailResponse(
            id = order.id!!,
            items = order.items.map { it.toDto() }  // LazyInitializationException 발생!
        )
    }
}

// ✅ 올바른 패턴 ① — @Transactional 범위 안에서 명시적으로 fetch
@Service
class OrderService(private val orderRepository: OrderRepository) {

    @Transactional(readOnly = true)
    fun fetchOrderDetail(id: Long): OrderDetailResponse {
        val order = orderRepository.findById(id).orElseThrow()
        // 트랜잭션 안에서 Lazy 컬렉션 접근 → 안전
        val items = order.items.map { it.toDto() }
        return OrderDetailResponse(id = order.id!!, items = items)
    }

    // suspend 함수는 트랜잭션이 완료된 DTO만 받아서 처리
    suspend fun processOrder(id: Long): ProcessedResponse {
        val detail = fetchOrderDetail(id)   // 블로킹 호출로 먼저 DTO 변환 완료
        delay(100)                          // 이 시점에는 Hibernate Session 불필요
        return ProcessedResponse(detail)
    }
}

// ✅ 올바른 패턴 ② — JPQL/fetch join으로 한 번에 조회
@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
    fun findByIdWithItems(id: Long): Order?
}
```

#### Step 5 — 요청/응답 DTO

```kotlin
// src/main/kotlin/com/example/demo/user/UserDto.kt
package com.example.demo.user

// 요청 DTO — data class 활용
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

// 응답 DTO
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val role: Role
)

// 변환 확장 함수
fun User.toResponse() = UserResponse(
    id = requireNotNull(id) { "영속화되지 않은 User는 변환할 수 없습니다" },
    name = name, email = email, age = age, role = role
)
fun CreateUserRequest.toEntity() = User(name = name, email = email, age = age, role = role)
```

#### Step 6 — 리포지토리

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

#### Step 7 — 서비스

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
            .orElseThrow { NoSuchElementException("ID $id 사용자를 찾을 수 없습니다") }
        return user.toResponse()
    }

    @Transactional
    fun create(request: CreateUserRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다: ${request.email}")
        }
        val user = userRepository.save(request.toEntity())
        return user.toResponse()
    }

    @Transactional
    fun update(id: Long, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { NoSuchElementException("ID $id 사용자를 찾을 수 없습니다") }
        request.name?.let { user.name = it }
        request.age?.let { user.age = it }
        return user.toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        if (!userRepository.existsById(id)) {
            throw NoSuchElementException("ID $id 사용자를 찾을 수 없습니다")
        }
        userRepository.deleteById(id)
    }
}
```

#### Step 8 — 컨트롤러

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

#### Step 9 — 예외 처리

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
        ErrorResponse(ex.message ?: "리소스를 찾을 수 없습니다", "NOT_FOUND")

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequest(ex: IllegalArgumentException) =
        ErrorResponse(ex.message ?: "잘못된 요청입니다", "BAD_REQUEST")
}
```

#### Step 10 — 실행 및 테스트

```bash
# 실행
./gradlew bootRun
```

```bash
# 사용자 생성
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","email":"hong@example.com","age":30}'

# 응답
# {"id":1,"name":"홍길동","email":"hong@example.com","age":30,"role":"USER"}

# 목록 조회
curl http://localhost:8080/api/users

# 단건 조회
curl http://localhost:8080/api/users/1

# 수정
curl -X PATCH http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동2","age":31}'

# 삭제
curl -X DELETE http://localhost:8080/api/users/1
```

H2 콘솔은 `http://localhost:8080/h2-console`에서 확인할 수 있습니다 (application.yml에 `spring.h2.console.enabled=true` 추가 필요).

{{< callout type="info" title="핵심 정리" >}}
- `kotlin-spring` 플러그인 — Spring 컴포넌트를 자동으로 `open`으로 설정
- `kotlin-jpa` 플러그인 — JPA 엔티티에 기본 생성자 자동 추가
- `data class`는 DTO에 적합, JPA 엔티티에는 일반 `class` 권장
- 확장 함수 `toResponse()`, `toEntity()`로 DTO 변환 로직을 깔끔하게 분리
{{< /callout >}}

{{< callout type="warning" title="운영 함정 ③ — Spring MVC + suspend는 진정한 non-blocking이 아닙니다" >}}
Step 8의 컨트롤러는 `suspend` 없이 작성했지만, `suspend` 함수를 컨트롤러 핸들러로 사용해도 **Spring MVC에서는 스레드 점유가 줄어들지 않습니다**.

Spring MVC는 Servlet 스레드(Tomcat 스레드 풀)를 기반으로 동작하므로, `suspend` 함수를 핸들러로 등록해도 내부적으로 `runBlocking`으로 래핑하여 스레드를 **블로킹 상태로 점유**합니다. 코루틴의 이점(스레드 해제 후 재개)을 얻으려면 **Spring WebFlux** 전환이 필요합니다.
{{< /callout >}}

**Spring MVC vs WebFlux + 코루틴 비교**

| 항목 | Spring MVC + suspend | Spring WebFlux + 코루틴 |
|------|---------------------|------------------------|
| 스레드 모델 | Tomcat 스레드 풀 (블로킹) | Netty 이벤트 루프 (non-blocking) |
| suspend 효과 | 없음 (runBlocking 래핑) | 스레드 반환 후 재개 |
| 기존 코드 호환성 | Spring MVC 그대로 사용 가능 | 블로킹 코드 전면 검토 필요 |
| JPA/JDBC 사용 | 자연스럽게 사용 가능 | R2DBC 또는 `Dispatchers.IO` 필요 |
| 전환 비용 | 낮음 | 높음 (의존성, 설정 전면 변경) |
| 적합 시나리오 | 일반적인 CRUD API | 고동시성·스트리밍·SSE |

```kotlin
// Spring MVC — suspend 컨트롤러 (진정한 non-blocking 아님)
@RestController
class UserController(private val userService: UserService) {
    @GetMapping("/api/users/{id}")
    suspend fun get(@PathVariable id: Long): UserResponse =
        userService.findById(id)   // 내부적으로 runBlocking 래핑 → 스레드 점유 그대로
}

// Spring WebFlux — 진정한 non-blocking suspend 컨트롤러
// build.gradle.kts: implementation("org.springframework.boot:spring-boot-starter-webflux")
@RestController
class UserController(private val userService: UserService) {
    @GetMapping("/api/users/{id}")
    suspend fun get(@PathVariable id: Long): UserResponse =
        userService.findById(id)   // Netty 이벤트 루프에서 스레드 반환 후 재개
}
```

{{< callout type="info" title="WebFlux 전환 시 고려사항" >}}
WebFlux로 전환하면 JPA(블로킹 JDBC)를 그대로 사용할 수 없습니다. 대안은 두 가지입니다.

- **R2DBC**: 리액티브 드라이버로 교체 (Spring Data R2DBC, 쿼리 방식 변경 필요)
- **`Dispatchers.IO`**: 기존 JPA를 유지하되, 블로킹 DB 호출을 `withContext(Dispatchers.IO) { ... }`로 감싸서 전용 스레드 풀에서 실행

대부분의 CRUD 서비스에서는 **Spring MVC + `Dispatchers.IO` 조합**으로 충분합니다. WebFlux는 SSE, WebSocket, 대규모 동시 스트리밍 처리가 필요한 경우에 도입을 검토하세요.
{{< /callout >}}

#### 다음 단계

- [Kafka 연동](kafka-integration/) — Kotlin + Spring Kafka로 메시지 Producer/Consumer 구현
- [코루틴 실무 사용](coroutines-practical/) — suspend 컨트롤러로 비동기 API 구성

> 💡 **함께 읽기**: REST API 위에 도메인 계층을 설계한다면 [DDD 애플리케이션 계층]({{< relref "/docs/ddd/examples/application-layer" >}})과 [주문 도메인 예제]({{< relref "/docs/ddd/examples/order-domain" >}})를 참고해 컨트롤러–서비스–도메인 책임 분리를 구체화할 수 있습니다.
