---
title: "버전 비교"
description: "Kotlin 1.x에서 2.x로의 주요 변경점, K2 컴파일러, value class, Context Receivers, Multiplatform 안정화 등을 정리합니다."
weight: 2
lastmod: "2026-05-13"
---

> **소요 시간**: 약 10분

Kotlin의 주요 버전별 변경점을 정리합니다. 마이그레이션 시 참고할 핵심 변경사항과 팁을 제공합니다.

{{< callout type="tip" title="핵심 요약" >}}
- **Kotlin 2.0**: K2 컴파일러 안정화 — 최대 2배 빠른 컴파일 속도
- **Kotlin 2.0**: `data object` 정식 지원
- **Kotlin 1.9**: `Enum.entries` 추가 (`.values()` 대체)
- **Kotlin 1.7**: 빌더 추론 개선, `by lazy` 스레드 모드 개선
- **Kotlin 1.5**: `value class` (인라인 클래스) 안정화, `sealed interface` 추가
{{< /callout >}}

---

#### Kotlin 버전 한눈에 보기

| 버전 | 주요 변경점 | JVM 최소 요구 |
|------|------------|--------------|
| 1.4.x | SAM 변환 개선, `fun interface` | JVM 8 |
| 1.5.x | `value class`, `sealed interface`, `Result` 개선 | JVM 8 |
| 1.6.x | `sealed class` when 완전성 경고, `Regex` 개선 | JVM 8 |
| 1.7.x | K1 컴파일러 개선, `@OptIn` 안정화 | JVM 8 |
| 1.8.x | `kotlin-reflect` 경량화, Java 18-19 지원 | JVM 8 |
| 1.9.x | `Enum.entries`, `data object`, 범위 함수 개선 | JVM 8 |
| 2.0.x | **K2 컴파일러 안정화**, 스마트 캐스트 개선 | JVM 8 |
| 2.1.x | 가드 조건(`when` guard), Non-local break/continue | JVM 8 |

---

#### Kotlin 2.0 — 핵심 변경점

**K2 컴파일러 안정화**

K2는 Kotlin 컴파일러를 완전히 새로 작성한 프론트엔드입니다. Kotlin 2.0에서 `Stable`로 승격되었습니다.

| 항목 | 변화 |
|------|------|
| 컴파일 속도 | 평균 최대 2배 향상 (프로젝트 규모에 따라 다름) |
| 타입 추론 | 더 정확한 추론, 일부 오류 수정 |
| IDE 분석 | IntelliJ 분석 엔진을 K2 기반으로 전환 |
| 플러그인 호환성 | 기존 컴파일러 플러그인은 K2 API로 업데이트 필요 |

```kotlin
// 2.0에서 스마트 캐스트 개선 예시
class Container(val value: Any?) {
    fun printLength() {
        val v = value
        if (v is String) {
            println(v.length)   // 이전: 일부 케이스에서 스마트 캐스트 실패
                                // 2.0: 올바르게 스마트 캐스트
        }
    }
}
```

**data object (2.0 정식)**

`object` 선언에 `data`를 붙이면 `toString()`이 클래스 이름을 반환합니다. `sealed` 계층에서 값이 없는 케이스에 유용합니다.

```kotlin
sealed class ApiResult {
    data class Success(val body: String) : ApiResult()
    data class Error(val code: Int, val message: String) : ApiResult()
    data object Loading : ApiResult()     // toString() = "Loading"
    data object Empty : ApiResult()       // toString() = "Empty"
}

println(ApiResult.Loading)   // Loading (object만 쓰면 패키지 경로 포함 문자열)
```

---

#### Kotlin 1.9 — 실용적 개선

**Enum.entries**

`.values()` 는 호출할 때마다 새 배열을 생성했습니다. `.entries`는 프로퍼티로 미리 생성된 불변 리스트를 반환합니다.

```kotlin
enum class Direction { NORTH, SOUTH, EAST, WEST }

// 기존 — 배열 복사 발생
val old = Direction.values().toList()

// 1.9+ — 미리 생성된 리스트
val new = Direction.entries
println(new)   // [NORTH, SOUTH, EAST, WEST]
```

**범위 함수 개선**

`..` 연산자와 `until` 함수에 `Float`, `Double` 범위가 추가되고, `rangeUntil` 연산자 (`..<`)가 도입되었습니다.

```kotlin
for (i in 0..<10) {     // 0 이상 10 미만 (1.9+)
    print("$i ")
}
// 기존: for (i in 0 until 10)
```

---

#### Kotlin 1.7 — 빌더와 안정화

**빌더 추론**

`buildList`, `buildMap`, `buildSet`이 `Stable`로 승격되었습니다.

```kotlin
val list = buildList {
    add("A")
    addAll(listOf("B", "C"))
    add(if (condition) "D" else "E")
}   // List<String> 반환
```

---

#### Kotlin 1.5 — value class와 sealed interface

**value class (인라인 클래스)**

단일 프로퍼티를 래핑하지만 런타임에 래퍼 객체를 생성하지 않는 클래스. 타입 안전성과 성능을 동시에 확보합니다.

```kotlin
// 런타임에는 Long처럼 동작하지만 타입 안전
@JvmInline
value class UserId(val id: Long)

@JvmInline
value class Email(val address: String) {
    init {
        require(address.contains("@")) { "올바른 이메일 형식이 아닙니다" }
    }
}

fun findUser(id: UserId): User = TODO("DB 조회 구현")

val id = UserId(42L)
// findUser(42L)  // 컴파일 오류 — 타입 안전
findUser(id)      // OK
```

**sealed interface**

`sealed` 수식자를 `interface`에도 적용할 수 있게 되었습니다. 클래스와 달리 여러 `sealed interface`를 구현할 수 있습니다.

```kotlin
sealed interface Drawable { fun draw() }
sealed interface Resizable { fun resize(factor: Double) }

// 두 sealed interface 동시 구현 가능
class Image(val path: String) : Drawable, Resizable {
    override fun draw() { println("이미지 그리기: $path") }
    override fun resize(factor: Double) { println("크기 조절: $factor") }
}
```

---

#### Kotlin 1.4 — fun interface (SAM 변환)

단일 추상 메서드(SAM)를 가진 인터페이스를 `fun` 키워드로 선언하면 람다로 구현할 수 있습니다.

```kotlin
fun interface Validator<T> {
    fun validate(value: T): Boolean
}

// 람다로 직접 전달
val emailValidator = Validator<String> { it.contains("@") }
println(emailValidator.validate("user@example.com"))   // true
```

---

#### Kotlin Multiplatform — 안정화 여정

| 버전 | 상태 |
|------|------|
| 1.4 이전 | 실험적 (Experimental) |
| 1.4 ~ 1.8 | 알파/베타 |
| 1.9.20 | **Kotlin Multiplatform Stable** |
| 2.0+ | Android/iOS/JVM/Web 타깃 안정 |

Kotlin 1.9.20부터 Kotlin Multiplatform(KMP)이 Stable 상태가 되었습니다. iOS, Android, JVM, JavaScript, WASM 등 다양한 플랫폼을 단일 Kotlin 코드베이스로 타깃할 수 있습니다.

---

#### Context Receivers (실험적)

Kotlin 1.6.20에 도입된 실험적 기능. 함수가 여러 수신 객체를 컨텍스트로 요구할 수 있습니다.

```kotlin
// 실험적 기능 — 활성화 필요: -Xcontext-receivers
context(Logger, TransactionScope)
fun processOrder(order: Order) {
    log("주문 처리 시작: ${order.id}")    // Logger의 log()
    beginTransaction()                    // TransactionScope의 beginTransaction()
    // ...
}
```

{{< callout type="warning" title="Context Receivers 사용 시 주의" >}}
Context Receivers는 아직 실험적 기능입니다. 프로덕션 코드에서는 안정화될 때까지 사용을 자제하거나, `@OptIn(ExperimentalContextReceivers::class)` 어노테이션을 명시하는 것이 좋습니다.
{{< /callout >}}

---

#### 마이그레이션 팁

**1.x → 2.0 마이그레이션**

| 체크 항목 | 조치 |
|----------|------|
| K2 컴파일러 활성화 | `build.gradle.kts`에서 `languageVersion = "2.0"` |
| 컴파일러 플러그인 호환성 확인 | 사용 중인 플러그인의 K2 지원 여부 확인 |
| `Enum.values()` → `.entries` | 성능 개선, 린터 경고 발생 |
| `data object` 활용 | `sealed` 계층의 값 없는 케이스에 적용 |

```kotlin
// build.gradle.kts — K2 컴파일러 활성화
kotlin {
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_0
    }
}
```

**기본 JVM 타깃 버전**

Kotlin 1.8부터 기본 JVM 타깃이 **JVM 8** 입니다. Spring Boot 3.x는 JVM 17을 요구하므로, Spring Boot 프로젝트에서는 명시적으로 지정합니다.

```kotlin
// build.gradle.kts
kotlin {
    jvmToolchain(17)   // Java 17 toolchain 사용
}
```

---

#### 다음 단계

- [용어 사전](glossary/) - Kotlin 핵심 용어 정의
- [FAQ](faq/) - 자주 묻는 질문과 답변
- [참고 자료](references/) - 공식 릴리즈 노트 링크
