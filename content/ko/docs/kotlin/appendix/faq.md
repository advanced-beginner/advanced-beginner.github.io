---
title: "FAQ"
description: "Kotlin을 처음 접하는 개발자가 자주 묻는 질문 15개에 답합니다."
weight: 3
lastmod: "2026-05-13"
---

Kotlin을 처음 접하거나 실무에 도입할 때 자주 나오는 질문을 모았습니다.

---

#### Q1. Kotlin과 Java를 한 프로젝트에서 함께 쓸 수 있나요?

**A.** 네, 완전히 가능합니다. Kotlin과 Java는 같은 JVM 바이트코드로 컴파일되므로 동일 프로젝트에서 자유롭게 혼용할 수 있습니다.

```kotlin
// Kotlin 파일
class KotlinService {
    fun getGreeting(): String = "Hello from Kotlin"
}
```

```java
// Java 파일에서 Kotlin 클래스 사용
KotlinService service = new KotlinService();
System.out.println(service.getGreeting());
```

{{< callout type="info" title="Java 상호운용 팁" >}}
- Kotlin `data class`는 Java에서 일반 클래스처럼 사용합니다
- `@JvmOverloads`로 기본값 인자를 Java 오버로딩으로 노출합니다
- `@JvmStatic`으로 `companion object` 멤버를 Java 정적 메서드처럼 호출합니다
- Kotlin top-level 함수는 Java에서 `파일명Kt.함수명()` 형태로 호출됩니다
{{< /callout >}}

---

#### Q2. 코루틴과 Thread의 차이는 무엇인가요?

**A.** 코루틴은 **협력적(cooperative)** 으로 실행되는 경량 실행 단위입니다. 스레드는 OS가 관리하는 무거운 자원이지만, 코루틴은 스레드 안에서 실행되며 수천 개를 동시에 만들어도 부담이 없습니다.

| 항목 | Thread | Coroutine |
|------|--------|-----------|
| 자원 | 약 1MB 스택 | 몇 KB |
| 생성 비용 | 상대적으로 높음 | 매우 낮음 |
| 전환 방식 | OS가 선점 전환 | 협력적 일시 중단 |
| 블로킹 | 스레드 블로킹 | suspend (스레드 해제) |
| 동시 실행 수 | 수백~수천 | 수만~수십만 |

```kotlin
import kotlinx.coroutines.*

// 스레드 1000개 대신 코루틴 1000개
fun main() = runBlocking {
    repeat(1_000) {
        launch {
            delay(1000)
            print(".")
        }
    }
}
```

---

#### Q3. `data class`를 JPA Entity로 써도 되나요?

**A.** 권장하지 않습니다. JPA는 내부적으로 **인수 없는 생성자** 와 **가변 상태** 를 요구하는데, `data class`는 둘 다 기본적으로 지원하지 않습니다. 또한 `data class`의 `equals`/`hashCode`는 프록시 객체와 충돌할 수 있습니다.

```kotlin
// 권장하지 않음
@Entity
data class UserEntity(val id: Long, val name: String)

// 권장 패턴
@Entity
class UserEntity(
    @Id
    var id: Long? = null,
    var name: String = ""
) {
    // equals/hashCode를 id 기반으로 직접 구현하거나
    // 기본 참조 equals 사용
}

// DTO/값 객체에는 data class 사용
data class UserDto(val id: Long, val name: String)
```

---

#### Q4. `lateinit var`와 `by lazy`의 차이는 무엇인가요?

**A.** 둘 다 초기화를 나중으로 미루지만 목적과 사용 방식이 다릅니다.

| 항목 | `lateinit var` | `by lazy { }` |
|------|----------------|---------------|
| 변경자 | `var` | `val` |
| 초기화 시점 | 수동 (코드에서 직접) | 첫 접근 시 자동 |
| null 허용 | 불가 | 불가 |
| primitive 타입 | 불가 | 가능 |
| 초기화 확인 | `::field.isInitialized` | 항상 초기화됨 |
| 스레드 안전 | 수동 처리 필요 | 기본값으로 동기화됨 |
| 주 사용처 | DI 프레임워크, 테스트 setup | 비용 큰 계산, 설정 로드 |

```kotlin
class UserService {
    lateinit var repository: UserRepository  // 외부(DI)에서 주입

    val cache: Map<Long, User> by lazy {
        println("캐시 초기화 중...")
        loadInitialCache()  // 처음 cache에 접근할 때만 실행
    }
}
```

---

#### Q5. 확장 함수는 Java에서 어떻게 보이나요?

**A.** 확장 함수는 Java에서 **수신 객체를 첫 번째 매개변수로 받는 정적 메서드** 로 컴파일됩니다.

```kotlin
// Kotlin (StringExtensions.kt)
fun String.addPrefix(prefix: String): String = "$prefix$this"
```

```java
// Java에서 호출
String result = StringExtensionsKt.addPrefix("World", "Hello, ");
// Kotlin에서는: "World".addPrefix("Hello, ")
```

파일명을 바꾸고 싶으면 `@file:JvmName("StringUtils")`으로 지정합니다.

---

#### Q6. `when`에 `else` 브랜치가 항상 필요한가요?

**A.** `sealed class`나 `enum class`를 `when`의 인자로 쓰면 모든 경우가 처리되므로 `else`가 필요 없습니다. `else`를 생략하면 컴파일러가 누락된 케이스를 경고합니다.

```kotlin
sealed class Status { object Active : Status(); object Inactive : Status() }

// else 불필요 — 컴파일러가 완전성 검사
fun describe(s: Status) = when (s) {
    is Status.Active   -> "활성"
    is Status.Inactive -> "비활성"
}

// else 필요 — Int는 무한한 경우의 수
fun describe(n: Int) = when (n) {
    1 -> "하나"
    2 -> "둘"
    else -> "기타"
}
```

---

#### Q7. 코루틴에서 예외 처리는 어떻게 하나요?

**A.** `launch`와 `async`의 예외 처리 방식이 다릅니다.

```kotlin
// launch — CoroutineExceptionHandler 또는 try-catch
val handler = CoroutineExceptionHandler { _, exception ->
    println("오류 처리: ${exception.message}")
}

CoroutineScope(Dispatchers.IO + handler).launch {
    throw RuntimeException("launch 오류")
}

// async — await() 호출 시 예외 발생
val scope = CoroutineScope(Dispatchers.IO)
val deferred = scope.async {
    throw RuntimeException("async 오류")
}
try {
    deferred.await()  // 여기서 예외 발생
} catch (e: RuntimeException) {
    println("오류 처리: ${e.message}")
}
```

---

#### Q8. `object`와 `companion object`는 어떻게 다른가요?

**A.** `object`는 독립 싱글톤이고, `companion object`는 특정 클래스에 귀속된 싱글톤입니다.

```kotlin
// 독립 싱글톤
object AppLogger {
    fun log(msg: String) = println("[LOG] $msg")
}
AppLogger.log("시작")

// 클래스에 귀속된 싱글톤 — 클래스 이름으로 접근
class User private constructor(val name: String) {
    companion object {
        fun create(name: String) = User(name)
    }
}
val user = User.create("홍길동")
```

---

#### Q9. `==`와 `===`의 차이는 무엇인가요?

**A.** Kotlin에서 `==`는 `equals()` 메서드를 호출하는 **구조적 동등성** 비교입니다. `===`는 **참조 동일성** 비교입니다.

```kotlin
val a = "hello"
val b = "hello"
val c = a

println(a == b)    // true — 내용이 같음
println(a === b)   // true — JVM String 풀에서 같은 객체일 수 있음

data class Point(val x: Int, val y: Int)
val p1 = Point(1, 2)
val p2 = Point(1, 2)

println(p1 == p2)    // true — data class equals
println(p1 === p2)   // false — 다른 객체
```

---

#### Q10. `List`와 `MutableList`의 차이는 무엇인가요?

**A.** `List`는 read-only 뷰, `MutableList`는 수정 가능한 뷰입니다. `listOf()`가 반환하는 객체는 실제로 `ArrayList`일 수도 있지만, `List` 인터페이스를 통해서는 수정 메서드가 없습니다.

```kotlin
val readOnly: List<Int> = listOf(1, 2, 3)
// readOnly.add(4)  // 컴파일 오류

val mutable: MutableList<Int> = mutableListOf(1, 2, 3)
mutable.add(4)  // OK

// read-only → mutable 변환
val converted = readOnly.toMutableList()
converted.add(4)   // OK (새 MutableList 생성)
```

---

#### Q11. Kotlin의 `inline` 함수는 언제 사용해야 하나요?

**A.** 주로 **람다를 매개변수로 받는 고차 함수** 에 사용합니다. 람다는 호출 시 객체를 생성하는데, `inline`을 사용하면 호출 지점에 코드가 삽입되어 객체 생성 오버헤드가 없어집니다. 또한 `reified` 타입 매개변수를 사용할 수 있습니다.

```kotlin
// inline 없이 — 람다 객체 생성 발생
fun <T> measure(block: () -> T): T {
    val start = System.currentTimeMillis()
    val result = block()
    println("소요 시간: ${System.currentTimeMillis() - start}ms")
    return result
}

// inline — 호출 지점에 코드 삽입
inline fun <T> measureInline(block: () -> T): T {
    val start = System.currentTimeMillis()
    val result = block()
    println("소요 시간: ${System.currentTimeMillis() - start}ms")
    return result
}
```

---

#### Q12. Kotlin에서 싱글톤 패턴을 어떻게 구현하나요?

**A.** `object` 선언을 사용하면 됩니다. 스레드 안전하며 지연 초기화됩니다.

```kotlin
object DatabasePool {
    private val connections = mutableListOf<Connection>()

    fun getConnection(): Connection {
        return connections.firstOrNull() ?: createConnection()
    }

    private fun createConnection(): Connection {
        val conn = Connection()
        connections.add(conn)
        return conn
    }
}

// 사용
val conn = DatabasePool.getConnection()
```

---

#### Q13. `String?`을 `String`으로 변환하는 가장 좋은 방법은?

**A.** 상황에 따라 다릅니다.

```kotlin
val nullable: String? = getMaybeNull()

// 1. 기본값으로 대체 (가장 일반적)
val safe = nullable ?: ""
val withDefault = nullable ?: "알 수 없음"

// 2. null이면 함수 조기 종료
fun process(s: String?) {
    val value = s ?: return
    println(value)
}

// 3. null이면 예외 (필수값인 경우)
val required = requireNotNull(nullable) { "값은 필수입니다" }

// 4. null이면 다른 처리를 하고 싶을 때
nullable?.let { value ->
    println("값: $value")
} ?: println("값 없음")
```

---

#### Q14. Flow와 LiveData/RxJava의 차이는?

**A.** Kotlin Flow는 코루틴 기반의 비동기 스트림입니다.

| 항목 | Flow | LiveData | RxJava |
|------|------|----------|--------|
| 플랫폼 | 범용 Kotlin | Android 전용 | 범용 |
| 베이스 | 코루틴 | Android Lifecycle | 자체 스레드 모델 |
| 구독 취소 | Structured Concurrency | Lifecycle 자동 | Disposable 수동 |
| 학습 곡선 | 보통 | 낮음 | 높음 |
| 에러 처리 | try-catch, catch 연산자 | 직접 처리 | onError |

---

#### Q15. Kotlin Script(.kts)와 일반 .kt 파일의 차이는?

**A.** `.kts` 파일은 Kotlin 스크립트 파일로, `main` 함수 없이 최상위 코드를 직접 실행할 수 있습니다. Gradle 빌드 스크립트(`build.gradle.kts`)가 대표적인 예입니다.

```kotlin
// script.kts — main 없이 직접 실행
val message = "Hello, Script!"
println(message)

// 스크립트 실행
// kotlinc -script script.kts
```

```kotlin
// Main.kt — main 함수 필요
fun main() {
    val message = "Hello, Kotlin!"
    println(message)
}
```

---

#### 다음 단계

- [용어 사전](glossary/) - 핵심 용어 정의 확인
- [버전 비교](version-comparison/) - 버전별 변경점 파악
- [참고 자료](references/) - 추가 학습 자료
