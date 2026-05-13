---
title: "Null Safety 마이그레이션"
description: "Java 코드를 Kotlin으로 이전할 때 플랫폼 타입 처리, @Nullable/@NotNull 활용, 점진적 마이그레이션 절차와 자주 발생하는 NPE 패턴을 안내합니다."
lastmod: "2026-05-13"
---

Java 코드를 Kotlin으로 이전할 때 null 관련 문제를 안전하게 처리하는 절차를 단계별로 안내합니다.

**소요 시간**: 약 15~20분

{{< callout type="tip" title="TL;DR" >}}
- Java에서 넘어온 타입은 Kotlin에서 **플랫폼 타입**(예: `String!`)으로 추론됩니다.
- `@Nullable`/`@NotNull` 어노테이션을 Java 코드에 추가하면 Kotlin이 올바른 타입을 추론합니다.
- 플랫폼 타입은 `!!` 대신 `?.` 또는 `?: 기본값`으로 방어적으로 처리합니다.
- 클래스 단위로 점진적으로 마이그레이션하고, 각 단계마다 테스트로 검증합니다.
{{< /callout >}}

---

## 이 가이드가 해결하는 문제

다음 상황에서 이 가이드를 사용하세요:

- Java 코드를 Kotlin으로 이전 중에 `NullPointerException`이 발생할 때
- Kotlin에서 Java 메서드를 호출할 때 반환 타입이 nullable인지 알 수 없을 때
- `!!` 연산자를 남발하게 되어 null 안전성의 의미가 없어질 때
- Java 라이브러리를 Kotlin 코드에서 안전하게 사용하는 방법이 필요할 때

---

## 시작하기 전에

| 항목 | 요구 사항 |
|------|----------|
| Kotlin | 1.9.x 이상 |
| 기존 Java 코드 | `@Nullable`/`@NotNull` 어노테이션 권장 |
| IntelliJ IDEA | Java to Kotlin 변환 지원 |

**어노테이션 의존성:**

```kotlin
// build.gradle.kts
dependencies {
    // JSR-305 어노테이션 (@Nullable, @Nonnull)
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    // 또는 JetBrains 어노테이션
    implementation("org.jetbrains:annotations:24.0.0")
}
```

---

## 1단계: 플랫폼 타입 이해하기

Kotlin은 Java 코드의 반환 타입을 **플랫폼 타입** 으로 처리합니다. 플랫폼 타입은 `String!`처럼 표기되며, nullable인지 아닌지 Kotlin이 알 수 없습니다.

```kotlin
// Java 코드 (UserService.java)
public class UserService {
    public String getUserName(int id) {  // null을 반환할 수도 있음
        if (id > 0) return "Alice";
        return null;
    }
}
```

```kotlin
// Kotlin에서 호출할 때
val service = UserService()
val name = service.getUserName(0)  // 타입: String! (플랫폼 타입)

// Kotlin이 null 여부를 모름 → 런타임 NPE 위험
println(name.length)  // id=0이면 NPE 발생!
```

**플랫폼 타입의 위험:**

```kotlin
val name: String = service.getUserName(0)   // 컴파일 성공, 런타임 NPE 가능
val name: String? = service.getUserName(0)  // 안전한 선택
```

---

## 2단계: Java 코드에 어노테이션 추가

Java 소스에 `@Nullable`/`@NotNull`을 추가하면 Kotlin이 정확한 타입을 추론합니다.

**JetBrains 어노테이션 사용 (권장):**

```java
// UserService.java (수정 후)
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class UserService {
    @Nullable  // null 반환 가능
    public String getUserName(int id) {
        if (id > 0) return "Alice";
        return null;
    }

    @NotNull  // null 절대 반환 안 함
    public String getDefaultName() {
        return "Guest";
    }

    public void processUser(@NotNull String name) {  // 파라미터도 명시
        // ...
    }
}
```

```kotlin
// 어노테이션 추가 후 Kotlin에서 호출
val service = UserService()
val name = service.getUserName(0)  // 이제 타입: String? (nullable 명확)
val default = service.getDefaultName()  // 타입: String (non-null)

// 컴파일러가 null 체크를 강제함
println(name?.length ?: 0)  // 안전하게 처리
println(default.length)     // null 체크 불필요
```

**JSR-305 어노테이션 사용:**

```java
import javax.annotation.Nullable;
import javax.annotation.Nonnull;

public class OrderService {
    @Nullable
    public Order findOrder(@Nonnull String orderId) {
        // orderId는 non-null, 반환값은 nullable
        return repository.findById(orderId);
    }
}
```

**Gradle에서 JSR-305 처리 활성화:**

```kotlin
// build.gradle.kts
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xjsr305=strict")
    }
}
```

---

## 3단계: 플랫폼 타입 방어 패턴

어노테이션을 추가할 수 없는 외부 라이브러리를 사용할 때는 방어적 코딩으로 null을 처리합니다.

**패턴 1: 안전 호출 연산자 `?.`**

```kotlin
// 외부 라이브러리의 반환값 (플랫폼 타입)
val result = externalService.getData()

// !! 대신 ?.로 안전하게 처리
val length = result?.length ?: 0
val upper  = result?.uppercase()
```

**패턴 2: Elvis 연산자 `?:` 로 기본값 제공**

```kotlin
fun processUser(userId: String): String {
    val name = userRepository.findName(userId)  // String! (플랫폼 타입)
    return name ?: "이름 없음"  // null이면 기본값
}
```

**패턴 3: requireNotNull / checkNotNull**

```kotlin
fun processOrder(orderId: String) {
    val order = orderService.find(orderId)  // 플랫폼 타입
    // null이면 즉시 예외 (NPE보다 명확한 오류 메시지)
    requireNotNull(order) { "주문을 찾을 수 없습니다: $orderId" }
    // 이 시점 이후 order는 non-null로 스마트 캐스트됨
    println(order.status)
}
```

**패턴 4: let으로 null 안전 블록**

```kotlin
val user = userService.getUser(id)  // 플랫폼 타입

// null이 아닐 때만 실행
user?.let { u ->
    println("이름: ${u.name}")
    println("이메일: ${u.email}")
}
```

**피해야 할 패턴:**

```kotlin
// !! 남발 — null safety의 의미 없음, 런타임 NPE 위험
val name = service.getName()!!       // 위험!
val length = service.getData()!!.length  // 위험!

// 플랫폼 타입을 non-null로 무조건 선언
val name: String = service.getName()  // 컴파일러 경고 없지만 위험
```

---

## 4단계: 자주 발생하는 NPE 패턴

**패턴 1: 컬렉션 내부 요소의 null**

```kotlin
// Java가 반환한 List (내부 요소가 null일 수 있음)
val javaList: List<String?> = javaService.getNames()  // List<String!>

// 안전하게 null 필터링
val safeNames: List<String> = javaList.filterNotNull()

// 또는 mapNotNull로 변환
val lengths: List<Int> = javaList.mapNotNull { it?.length }
```

**패턴 2: Map 조회 결과의 null**

```kotlin
val map: Map<String, String> = javaService.getMap()

// 잘못된 접근: Map.get()은 nullable 반환
val value = map["key"]!!  // 키 없으면 NPE

// 올바른 접근
val value = map["key"] ?: "기본값"
val value = map.getOrDefault("key", "기본값")
```

**패턴 3: 늦은 초기화 변수**

```kotlin
// 잘못된 예: lateinit은 초기화 전 접근 시 UninitializedPropertyAccessException 발생
class UserControllerLateinit {
    private lateinit var userService: UserService

    // 초기화 여부를 미리 확인할 수 있습니다
    fun isInitialized() = ::userService.isInitialized
}

// 올바른 예: nullable + 명시적 검증
class UserControllerNullable {
    private var userService: UserService? = null

    fun getService() = userService ?: throw IllegalStateException("서비스 미초기화")
}
```

**패턴 4: 제네릭 타입의 null**

```kotlin
// T가 nullable인지 non-null인지 Java 측에서 명확하지 않음
fun <T> process(value: T): String {
    // T가 String!일 때 value.toString()은 null이 들어오면 "null" 문자열 반환
    return value?.toString() ?: "null"
}
```

---

## 5단계: 점진적 마이그레이션 절차

전체 코드베이스를 한 번에 Kotlin으로 전환하는 것은 위험합니다. 클래스 단위로 점진적으로 진행합니다.

**마이그레이션 순서 (권장):**

```text
1. 의존성 없는 유틸리티 클래스부터 시작
2. 도메인 모델 (data class 변환)
3. 서비스 레이어
4. 컨트롤러/엔트리포인트
```

**단계 1: Java 클래스에 어노테이션 추가**

```java
// 먼저 Java 코드에 @Nullable/@NotNull 추가
@NotNull
public User findUser(@NotNull String id) {
    User user = repository.find(id);
    return Objects.requireNonNull(user, "사용자 없음: " + id);
}
```

**단계 2: IntelliJ의 Java to Kotlin 변환 사용**

1. 변환할 Java 파일 열기
2. `Code → Convert Java File to Kotlin File` (또는 `Ctrl+Alt+Shift+K`)
3. 자동 변환 결과 검토

**단계 3: 변환 결과 검토 체크리스트**

```kotlin
// 자동 변환 후 확인할 사항:

// 1. 플랫폼 타입이 적절한 nullable로 변환됐는가?
var name: String? = null      // OK
var name: String = ""         // Java에서 non-null이면 OK

// 2. !! 연산자가 과도하게 사용됐는가?
val result = javaMethod()!!   // 검토 필요 → ?: 또는 requireNotNull로 교체

// 3. lateinit이 적절히 사용됐는가?
lateinit var service: Service  // 의존성 주입 대상에만 사용

// 4. equals/hashCode가 올바르게 생성됐는가?
// data class로 변환됐다면 자동 생성됨
```

**단계 4: 테스트로 검증**

```kotlin
import kotlin.test.*

class UserServiceTest {

    @Test
    fun `null 반환 케이스 처리`() {
        val service = UserService()
        val name = service.getUserName(-1)  // null 반환
        assertNull(name)

        // Kotlin의 null 처리 확인
        val displayName = name ?: "게스트"
        assertEquals("게스트", displayName)
    }

    @Test
    fun `non-null 반환 케이스`() {
        val service = UserService()
        val name = service.getUserName(1)
        assertNotNull(name)
        assertEquals("Alice", name)
    }
}
```

---

## 6단계: Spring Boot에서의 주의사항

Spring Boot와 함께 사용할 때 특별히 주의할 점이 있습니다.

```kotlin
// @RequestParam, @PathVariable은 기본적으로 required=true
// Kotlin에서 String? 대신 String을 사용하면 더 안전
@GetMapping("/users/{id}")
fun getUser(@PathVariable id: String): ResponseEntity<User> {
    // id는 non-null (Spring이 보장)
    return ResponseEntity.ok(userService.findById(id))
}

// optional 파라미터는 String?으로 선언
@GetMapping("/users")
fun searchUsers(
    @RequestParam(required = false) name: String?
): List<User> {
    return if (name != null) {
        userService.findByName(name)
    } else {
        userService.findAll()
    }
}

// @Autowired 필드는 lateinit var 대신 생성자 주입 권장
@Service
class UserService(
    private val userRepository: UserRepository,  // non-null, final
    private val cacheService: CacheService
)
```

---

## 체크리스트

마이그레이션 완료 여부를 확인하세요:

- [ ] Java 코드에 `@Nullable`/`@NotNull` 어노테이션이 추가됐는가?
- [ ] Gradle에서 `-Xjsr305=strict` 옵션을 활성화했는가?
- [ ] `!!` 연산자 사용이 최소화됐는가? (필요한 곳에만 `requireNotNull` 사용)
- [ ] 외부 라이브러리의 플랫폼 타입을 `?.`와 `?:`로 방어적으로 처리했는가?
- [ ] 컬렉션 내부 요소의 null을 `filterNotNull()`로 처리했는가?
- [ ] 각 클래스 전환 후 테스트를 실행하여 NPE가 발생하지 않음을 확인했는가?

---

## 관련 문서

- [Null Safety](../concepts/null-safety/) — Kotlin null 처리 개념
- [코루틴 기초](../concepts/coroutines-basics/) — suspend 함수에서의 null 처리
- [Gradle Kotlin DSL 팁](gradle-kotlin-dsl-tips/) — 마이그레이션 빌드 설정
