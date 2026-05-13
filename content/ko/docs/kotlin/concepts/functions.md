---
title: "함수"
description: "Kotlin의 fun 정의, 표현식 본문, 기본값/명명 인자, vararg, 람다와 고차 함수, 단일 표현식 함수를 설명합니다."
weight: 3
lastmod: "2026-05-13"
---

## 전체 비유: 레시피와 요리

Kotlin의 함수를 **레시피와 요리** 에 비유하면 이해하기 쉽습니다. 함수는 레시피처럼 재료(매개변수)를 받아 결과물(반환값)을 만들어 냅니다. 재료에 기본값을 정해두면 매번 모든 재료를 나열하지 않아도 됩니다. 레시피를 재료로 넘길 수도 있는데, 이것이 바로 람다와 고차 함수입니다.

| 비유 | Kotlin 개념 | 역할 |
|------|------------|------|
| 레시피 | 함수 (`fun`) | 입력을 받아 출력을 만드는 단위 |
| 재료 기본값 | 기본값 인자 | 인자를 생략하면 기본값 사용 |
| "설탕 먼저" 지시 | 명명 인자 | 인자 이름을 지정해 순서 변경 |
| "원하는 만큼" 재료 | vararg | 가변 개수의 인자 |
| 레시피를 재료로 | 람다/고차 함수 | 함수를 값처럼 전달 |

---

> **대상 독자**: [변수와 타입](../variables-types/)을 읽은 학습자
> **선수 지식**: Kotlin val/var, 기본 타입
> **소요 시간**: 약 30분
> **이 문서를 읽으면**: 다양한 형태의 함수를 선언하고, 람다를 만들어 고차 함수에 전달할 수 있습니다.

{{< callout type="tip" title="TL;DR" >}}
- **`fun` 키워드** 로 함수를 정의하고, 단일 표현식이면 `=`로 바로 씁니다
- **기본값 인자** 로 오버로딩 없이 다양한 호출 형태를 지원합니다
- **명명 인자** 로 매개변수 순서에 관계없이 명확하게 호출합니다
- **람다** 는 `{ 매개변수 -> 본문 }` 형식이며 고차 함수에 전달합니다
{{< /callout >}}

#### 왜 함수에 이렇게 다양한 형태가 있는가?

함수는 코드 재사용의 가장 기본 단위입니다. Kotlin은 **보일러플레이트를 줄이면서도 의도를 명확히 표현** 하기 위해 다양한 함수 문법을 제공합니다. 단순한 변환이라면 단일 표현식 함수로 한 줄에, 복잡한 로직이라면 블록 본문 함수로 명확하게 작성합니다.

---

#### fun 정의 기본

```kotlin
// 기본 형태
fun 함수명(매개변수명: 타입): 반환타입 {
    // 본문
    return 반환값
}

// 예시
fun add(a: Int, b: Int): Int {
    return a + b
}
```

**반환 타입 생략**

반환 타입이 `Unit`(반환값 없음)이면 생략할 수 있습니다.

```kotlin
fun printGreeting(name: String) {     // : Unit 생략
    println("안녕하세요, $name!")
}
```

---

#### 단일 표현식 함수 (표현식 본문)

함수 본문이 단 하나의 표현식이라면 `=`로 간결하게 작성할 수 있습니다. 반환 타입도 추론되므로 생략 가능합니다.

```kotlin
// 블록 본문 — 장황한 형태
fun double(n: Int): Int {
    return n * 2
}

// 단일 표현식 함수 — 간결한 형태
fun double(n: Int): Int = n * 2

// 반환 타입도 추론 — 더 간결
fun double(n: Int) = n * 2

// 복잡한 표현식도 가능
fun max(a: Int, b: Int) = if (a > b) a else b
fun grade(score: Int) = when {
    score >= 90 -> "A"
    score >= 80 -> "B"
    else        -> "C"
}
```

---

#### 기본값 인자

매개변수에 기본값을 설정하면 인자를 생략해서 호출할 수 있습니다. Java처럼 오버로딩 함수를 여러 개 만들 필요가 없습니다.

```kotlin
fun connect(
    host: String,
    port: Int = 5432,
    ssl: Boolean = false,
    timeout: Int = 3000
) {
    println("$host:$port (ssl=$ssl, timeout=${timeout}ms)")
}

// 다양한 호출 방식
connect("localhost")                         // 기본값 모두 적용
connect("db.example.com", 5433)              // port만 변경
connect("db.example.com", ssl = true)        // ssl만 변경
connect("db.example.com", 5433, true, 5000)  // 모두 지정
```

{{< callout type="info" title="기본값과 오버로딩" >}}
기본값 인자가 있는 Kotlin 함수를 Java에서 호출할 때는 항상 모든 인자를 지정해야 합니다. Java에서 오버로딩 형태로 호출하려면 함수에 `@JvmOverloads` 어노테이션을 추가합니다.

```kotlin
@JvmOverloads
fun connect(host: String, port: Int = 5432, ssl: Boolean = false) { ... }
```
{{< /callout >}}

---

#### 명명 인자

인자를 전달할 때 매개변수 이름을 명시하면 순서에 관계없이 호출할 수 있습니다.

```kotlin
fun createUser(
    name: String,
    age: Int,
    email: String,
    isAdmin: Boolean = false
) {
    println("사용자 생성: $name ($age세) - $email, 관리자: $isAdmin")
}

// 명명 인자 사용
createUser(
    name = "홍길동",
    email = "hong@example.com",  // 순서 변경 가능
    age = 30
)

// 일부는 위치, 일부는 이름 지정
createUser("홍길동", 30, email = "hong@example.com", isAdmin = true)
```

명명 인자를 사용하면 호출 코드의 **의도가 명확** 해집니다. `true`, `false` 같은 Boolean 인자를 쓸 때 특히 유용합니다.

---

#### vararg — 가변 인자

`vararg` 키워드로 개수가 정해지지 않은 인자를 받을 수 있습니다. 함수 내부에서는 배열처럼 다룹니다.

```kotlin
fun printAll(vararg messages: String) {
    for (msg in messages) {
        println(msg)
    }
}

printAll("첫 번째")
printAll("첫 번째", "두 번째", "세 번째")

// 배열을 vararg로 전달할 때는 스프레드 연산자(*) 사용
val lines = arrayOf("A", "B", "C")
printAll(*lines)
```

```kotlin
// vararg와 다른 매개변수 조합 시 vararg는 마지막에
fun log(level: String, vararg messages: String) {
    messages.forEach { println("[$level] $it") }
}

log("INFO", "서버 시작", "포트: 8080")
```

---

#### 람다와 고차 함수

**람다(Lambda)** 는 이름이 없는 함수입니다. 변수에 저장하거나 다른 함수의 인자로 전달할 수 있습니다.

**람다 문법**

```kotlin
// 기본 람다 형태: { 매개변수 -> 본문 }
val add: (Int, Int) -> Int = { a, b -> a + b }
val greet: (String) -> String = { name -> "안녕하세요, $name!" }
val printLine: () -> Unit = { println("===") }

// 호출
println(add(3, 5))       // 8
println(greet("홍길동"))  // 안녕하세요, 홍길동!
printLine()               // ===
```

**it 키워드 — 단일 매개변수 생략**

람다에 매개변수가 하나뿐이면 이름을 `it`으로 자동 사용할 수 있습니다.

```kotlin
val double: (Int) -> Int = { it * 2 }
val isEven: (Int) -> Boolean = { it % 2 == 0 }

println(double(5))    // 10
println(isEven(4))    // true
```

**고차 함수(Higher-Order Function)**

함수를 매개변수로 받거나 함수를 반환하는 함수입니다.

```kotlin
// 함수를 매개변수로 받는 고차 함수
fun transform(numbers: List<Int>, operation: (Int) -> Int): List<Int> {
    return numbers.map(operation)
}

val nums = listOf(1, 2, 3, 4, 5)
val doubled = transform(nums) { it * 2 }       // [2, 4, 6, 8, 10]
val squared = transform(nums) { it * it }       // [1, 4, 9, 16, 25]

// 함수를 반환하는 고차 함수
fun multiplier(factor: Int): (Int) -> Int {
    return { number -> number * factor }
}

val triple = multiplier(3)
println(triple(5))   // 15
```

---

#### 트레일링 람다

람다가 함수의 **마지막 인자** 라면 소괄호 밖으로 꺼낼 수 있습니다. Kotlin 표준 라이브러리가 이 패턴을 활용합니다.

```kotlin
// 일반 형태
val result = listOf(1, 2, 3).map({ it * 2 })

// 트레일링 람다 — 더 자연스러운 형태
val result = listOf(1, 2, 3).map { it * 2 }

// 람다만 인자라면 소괄호 생략
val evens = listOf(1, 2, 3, 4).filter { it % 2 == 0 }

// 복잡한 람다 — 트레일링 람다로 가독성 향상
val total = listOf(1, 2, 3, 4, 5).fold(0) { acc, n ->
    println("$acc + $n = ${acc + n}")
    acc + n
}
```

---

#### 함수 타입과 함수 참조

함수도 값처럼 참조할 수 있습니다. `::함수명`으로 함수 참조를 만듭니다.

```kotlin
fun isPositive(n: Int): Boolean = n > 0

val nums = listOf(-3, -1, 0, 2, 5)

// 람다로 전달
val positives1 = nums.filter { it > 0 }

// 함수 참조로 전달 — 더 간결
val positives2 = nums.filter(::isPositive)

// 멤버 함수 참조
val strings = listOf("hello", "WORLD", "Kotlin")
val uppercased = strings.map(String::uppercase)
// ["HELLO", "WORLD", "KOTLIN"]
```

---

#### 코드 예제: 전부 합쳐보기

```kotlin
package com.example.functions

// 단일 표현식 함수
fun square(n: Int) = n * n

// 기본값 인자
fun formatMessage(
    text: String,
    prefix: String = "[INFO]",
    suffix: String = ""
) = "$prefix $text$suffix"

// 고차 함수
fun applyTwice(value: Int, operation: (Int) -> Int): Int =
    operation(operation(value))

fun main() {
    // 단일 표현식 함수
    println(square(5))   // 25

    // 기본값 인자
    println(formatMessage("서버 시작"))                        // [INFO] 서버 시작
    println(formatMessage("오류 발생", prefix = "[ERROR]"))   // [ERROR] 오류 발생

    // 명명 인자
    println(formatMessage(
        text = "완료",
        prefix = "[SUCCESS]",
        suffix = " ✓"
    ))  // [SUCCESS] 완료 ✓

    // 고차 함수 + 람다
    println(applyTwice(3) { it * 2 })   // 12 (3 → 6 → 12)
    println(applyTwice(3, ::square))     // 81 (3 → 9 → 81)

    // vararg
    fun sum(vararg nums: Int) = nums.sum()
    println(sum(1, 2, 3, 4, 5))   // 15

    // 컬렉션 연산 — 람다와 고차 함수 활용
    val scores = listOf(85, 92, 67, 78, 95)
    val passing = scores.filter { it >= 70 }
    val grades = passing.map { score ->
        when {
            score >= 90 -> "A"
            score >= 80 -> "B"
            else        -> "C"
        }
    }
    println(grades)   // [B, A, B, A]
}
```

---

#### 핵심 포인트

{{< callout type="info" title="핵심 정리" >}}
- **단일 표현식 함수** `fun f(x: Int) = x * 2`로 간결하게 작성합니다
- **기본값 인자** 로 오버로딩 없이 다양한 호출을 지원합니다
- **명명 인자** 로 Boolean 등 의미가 불분명한 인자를 명확하게 합니다
- **람다** 는 `{ 매개변수 -> 본문 }`, 단일 매개변수는 `it`으로 생략합니다
- **트레일링 람다** — 마지막 인자가 람다면 소괄호 밖으로 꺼낼 수 있습니다
{{< /callout >}}

#### 다음 단계

- [Null Safety](../null-safety/) - nullable 타입과 안전 호출 연산자를 학습합니다
- [클래스와 객체](../classes-objects/) - 함수를 클래스 멤버로 활용하는 방법을 배웁니다
