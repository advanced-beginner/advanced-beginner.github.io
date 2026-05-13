---
title: "확장 함수"
description: "기존 클래스를 수정하지 않고 새로운 메서드를 추가하는 Kotlin 확장 함수의 원리와 활용법을 설명합니다."
weight: 8
lastmod: "2026-05-13"
---

## 전체 비유: 기존 건물에 엘리베이터 추가하기

확장 함수를 **리모델링 공사** 에 비유하면 이해하기 쉽습니다. 기존 건물(클래스)의 구조를 바꾸지 않고도 외부에서 엘리베이터(새 기능)를 붙일 수 있습니다.

| 리모델링 비유 | Kotlin 개념 | 역할 |
|-------------|-----------|------|
| 기존 건물 | 수신 타입 (`String`, `List` 등) | 기능을 추가할 대상 |
| 엘리베이터 설계 | `fun Type.method()` 정의 | 확장 함수 선언 |
| 엘리베이터 내부 | `this` (수신 객체) | 기존 타입의 멤버에 접근 |
| 외부 시공사 | 확장 함수가 정의된 파일/모듈 | 소유권 없이 기능 추가 |
| 건물 설계도 | 원본 클래스 | 수정 불가, 열람만 가능 |

기존 건물 설계도를 바꾸지 않아도 외부에서 기능을 덧붙일 수 있듯이, 확장 함수는 **소스 코드 접근 권한 없이도 타입에 기능을 추가** 합니다.

---

> **대상 독자**: Kotlin 기초 문법을 익힌 개발자
> **선수 지식**: 함수 정의, 클래스 기초
> **소요 시간**: 약 25분
> **이 문서를 읽으면**: 확장 함수를 직접 정의하고, 표준 라이브러리의 확장 함수를 자신 있게 사용할 수 있습니다.

{{< callout type="tip" title="TL;DR" >}}
- `fun String.greet()` 형태로 기존 타입에 메서드를 추가합니다
- 함수 내부에서 `this`는 수신 객체(확장 대상)를 가리킵니다
- 확장 함수는 **정적 디스패치** — 런타임 타입이 아닌 선언 타입으로 결정됩니다
- 멤버 함수와 시그니처가 충돌하면 **멤버 함수가 이깁니다**
{{< /callout >}}

#### 왜 확장 함수가 필요한가?

라이브러리 클래스는 소스 코드를 직접 수정할 수 없습니다. 예를 들어 `String`에 이메일 검증 메서드를 추가하고 싶다면 기존에는 유틸리티 클래스를 만들어야 했습니다.

```kotlin
// 기존 방식: 유틸리티 클래스
object StringUtils {
    fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}

// 호출 — 가독성이 어색합니다
val valid = StringUtils.isValidEmail("user@example.com")
```

확장 함수를 사용하면 마치 `String`의 원래 메서드처럼 호출할 수 있습니다.

```kotlin
// 확장 함수
fun String.isValidEmail(): Boolean {
    return this.contains("@") && this.contains(".")
}

// 호출 — 자연스럽습니다
val valid = "user@example.com".isValidEmail()
```

#### 기본 문법

확장 함수의 선언 형태는 `fun 수신타입.함수이름(파라미터): 반환타입 { ... }` 입니다.

```kotlin
// 기본 형태
fun String.repeat(n: Int): String {
    return this.repeat(n)           // this = 수신 객체(String 인스턴스)
}

// this는 대부분 생략 가능합니다
fun String.wordCount(): Int {
    return trim().split("\\s+".toRegex()).size
}

// 제네릭 타입에도 적용 가능합니다
fun <T> List<T>.secondOrNull(): T? {
    return if (size >= 2) this[1] else null
}
```

#### 수신 객체(this)

확장 함수 내부에서 `this`는 확장 대상 인스턴스입니다. 명시적으로 쓸 수도 있고, 생략할 수도 있습니다.

```kotlin
fun String.isPalindrome(): Boolean {
    val cleaned = this.replace(" ", "").lowercase()  // this 명시
    return cleaned == cleaned.reversed()
}

fun String.shout(): String {
    return uppercase() + "!!!"                       // this 생략 (권장)
}

// 사용
println("racecar".isPalindrome())   // true
println("hello".shout())            // HELLO!!!
```

#### 확장 프로퍼티

함수뿐 아니라 프로퍼티도 확장할 수 있습니다. 단, 백킹 필드(backing field)는 가질 수 없으므로 `val`/`var` 모두 getter(와 setter)를 반드시 정의해야 합니다.

```kotlin
// 확장 프로퍼티 — getter 필수
val String.lastChar: Char
    get() = this[length - 1]

var StringBuilder.lastChar: Char
    get() = this[length - 1]
    set(value) {
        this.setCharAt(length - 1, value)
    }

// 사용
println("Kotlin".lastChar)          // n

val sb = StringBuilder("Kotlin")
sb.lastChar = '!'
println(sb)                         // Kotli!
```

{{< callout type="info" title="확장 프로퍼티의 제약" >}}
확장 프로퍼티는 상태를 저장할 수 없습니다. 초기화식(`val x: Int = 0`)은 허용되지 않으며, 반드시 getter/setter로 계산된 값을 반환해야 합니다.
{{< /callout >}}

#### 확장의 정적 디스패치

확장 함수는 **컴파일 타임에 수신 타입이 결정** 됩니다. 가상 디스패치(오버라이드)가 아니므로, 변수의 선언 타입에 따라 어느 확장 함수가 호출될지 결정됩니다.

```kotlin
open class Animal
class Dog : Animal()

fun Animal.sound() = "..."
fun Dog.sound() = "멍멍"

fun printSound(animal: Animal) {
    println(animal.sound())     // Animal.sound() 호출 — "..."
}

printSound(Dog())               // "..." — Dog이지만 파라미터 타입이 Animal
```

```mermaid
graph LR
    A["printSound(Dog())"] --> B["파라미터 타입: Animal"]
    B --> C["Animal.sound() 호출"]
    C --> D["결과: '...'"]
```

이 동작이 **예상과 다를 수 있다** 는 점을 반드시 기억하세요. 다형성이 필요하다면 일반 멤버 함수(오버라이드)를 사용해야 합니다.

#### 멤버 함수 vs 확장 함수 우선순위

**멤버 함수가 항상 확장 함수보다 우선합니다.** 같은 시그니처가 있다면 멤버 함수가 호출됩니다.

```kotlin
class Greeter {
    fun hello() = "멤버 hello"
}

fun Greeter.hello() = "확장 hello"

val g = Greeter()
println(g.hello())      // "멤버 hello" — 멤버 함수 우선
```

확장 함수는 멤버 함수를 **오버라이드할 수 없습니다**. 이 특성 덕분에 외부에서 클래스의 기존 동작을 몰래 바꾸는 일을 방지합니다.

#### Nullable 수신 타입

수신 타입에 `?`를 붙이면 `null`인 경우에도 확장 함수를 안전하게 호출할 수 있습니다.

```kotlin
fun String?.orEmpty(): String {
    return this ?: ""
}

fun Any?.isNull(): Boolean {
    return this == null
}

// 사용
val name: String? = null
println(name.orEmpty())    // "" — NPE 없음
println(name.isNull())     // true
```

표준 라이브러리의 `String?.isNullOrEmpty()`, `String?.isNullOrBlank()`도 이 방식으로 정의되어 있습니다.

#### 표준 라이브러리 활용 예

Kotlin 표준 라이브러리의 상당 부분이 확장 함수로 구현되어 있습니다.

```kotlin
// 컬렉션 확장 함수
val numbers = listOf(3, 1, 4, 1, 5, 9, 2, 6)

val result = numbers
    .filter { it > 3 }          // List<Int> 확장
    .sortedDescending()         // List<Int> 확장
    .take(3)                    // List<T> 확장
    .sumOf { it }               // Collection<Int> 확장

// 문자열 확장 함수
val csv = "홍길동,30,서울"
val parts = csv.split(",")             // String 확장
val name = parts.first()              // List<T> 확장
val trimmed = "  hello  ".trim()      // String 확장

// 파일 I/O 확장 함수
import java.io.File
val content = File("data.txt").readText()   // File 확장
```

#### 실무 패턴: 도메인 특화 확장

확장 함수는 특정 비즈니스 도메인에 맞는 가독성 좋은 코드를 작성할 때 특히 유용합니다.

```kotlin
// 금액 관련 확장
val Int.원: Long get() = this.toLong()
val Long.원: Long get() = this
val Int.만원: Long get() = this * 10_000L

// 사용
val price = 5.만원 + 3000.원   // 53,000

// 날짜 관련 확장
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

// 사용
val today = LocalDate.now()
if (today.isWeekend()) {
    println("오늘은 주말입니다. 다음 근무일: ${today.nextWorkday()}")
}
```

#### 어디에 확장 함수를 정의할까?

| 위치 | 적합한 경우 |
|------|-----------|
| 최상위 파일 | 프로젝트 전체에서 공통 사용 |
| 동반 객체(companion) | 해당 클래스와 연관된 팩토리 역할 |
| 클래스/함수 내부 | 특정 스코프에서만 사용하는 지역 확장 |

```kotlin
// 로컬 확장 함수 — 해당 함수 내에서만 유효
fun processData(input: String): String {
    fun String.clean() = trim().lowercase().replace(" ", "_")
    return input.clean()
}
```

{{< callout type="info" title="핵심 정리" >}}
- `fun 타입.함수명()` 형태로 기존 타입을 수정하지 않고 기능을 추가합니다
- 함수 내부에서 `this`는 수신 객체를 가리키며 생략할 수 있습니다
- 확장 함수는 **정적 디스패치** — 변수의 선언 타입에 따라 결정됩니다
- 멤버 함수와 충돌 시 **멤버 함수가 우선** 합니다
- `String?`처럼 nullable 수신 타입도 지원합니다
{{< /callout >}}

#### 다음 단계

- [스코프 함수](scope-functions/) — `let`, `run`, `apply` 등 확장 함수 기반의 표준 라이브러리 활용
- [인라인/Reified](inline-reified/) — 확장 함수를 인라인으로 최적화하는 방법
