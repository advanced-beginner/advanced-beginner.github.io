---
lastmod: "2026-01-06"
title: 제어 구조
weight: 2
---

Scala의 제어 구조는 **표현식(expression)**입니다. 즉, 모든 제어 구조가 값을 반환합니다.

## if 표현식

### 기본 사용법

Scala의 `if`는 문(statement)이 아니라 표현식(expression)입니다.

```scala
val x = 10

// if 표현식은 값을 반환
val result = if (x > 5) "크다" else "작거나 같다"
println(result)  // 크다

// 삼항 연산자가 필요 없음 (if 자체가 값을 반환)
val max = if (a > b) a else b
```

### Scala 3 문법

Scala 3에서는 `then` 키워드를 사용할 수 있습니다.

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
val x = 10

// then 키워드 사용 (권장)
val result = if x > 5 then "크다" else "작거나 같다"

// 여러 줄
val message =
  if x > 100 then
    "매우 크다"
  else if x > 50 then
    "크다"
  else
    "작다"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
val x = 10

// 괄호 필수
val result = if (x > 5) "크다" else "작거나 같다"

// 여러 줄
val message = {
  if (x > 100) {
    "매우 크다"
  } else if (x > 50) {
    "크다"
  } else {
    "작다"
  }
}
```
{{% /tab %}}
{{< /tabs >}}

### Unit 반환

`else`가 없으면 `Unit`을 반환할 수 있습니다.

```scala
val x = 10

// else가 없으면 타입이 Unit으로 추론될 수 있음
if (x > 5) println("크다")

// 명시적으로 Unit 타입
val result: Unit = if (x > 5) println("크다")
```

## for 표현식

Scala의 `for`는 매우 강력합니다. 단순 반복부터 컬렉션 변환까지 다양하게 사용됩니다.

### 기본 반복

```scala
// Range를 사용한 반복
for (i <- 1 to 5) {
  println(i)  // 1, 2, 3, 4, 5
}

// until: 끝 값 제외
for (i <- 1 until 5) {
  println(i)  // 1, 2, 3, 4
}

// 컬렉션 반복
val fruits = List("사과", "바나나", "체리")
for (fruit <- fruits) {
  println(fruit)
}
```

### 가드 (조건 필터)

```scala
// 조건이 true인 경우만 실행
for (i <- 1 to 10 if i % 2 == 0) {
  println(i)  // 2, 4, 6, 8, 10
}

// 여러 조건
for {
  i <- 1 to 100
  if i % 3 == 0
  if i % 5 == 0
} println(i)  // 15, 30, 45, 60, 75, 90
```

### 중첩 반복

```scala
// 구구단
for {
  i <- 2 to 9
  j <- 1 to 9
} {
  println(s"$i x $j = ${i * j}")
}

// 좌표 생성
for {
  x <- 0 until 3
  y <- 0 until 3
} println(s"($x, $y)")
```

### yield - 새 컬렉션 생성

`yield`를 사용하면 for 표현식이 새 컬렉션을 반환합니다.

```scala
// 각 요소를 변환하여 새 리스트 생성
val numbers = List(1, 2, 3, 4, 5)
val doubled = for (n <- numbers) yield n * 2
// List(2, 4, 6, 8, 10)

// 필터 + 변환
val evenSquares = for {
  n <- 1 to 10
  if n % 2 == 0
} yield n * n
// Vector(4, 16, 36, 64, 100)

// 중첩 + yield
val pairs = for {
  x <- 1 to 3
  y <- 1 to 3
} yield (x, y)
// Vector((1,1), (1,2), (1,3), (2,1), (2,2), (2,3), (3,1), (3,2), (3,3))
```

### 패턴 매칭과 함께

```scala
val pairs = List((1, "one"), (2, "two"), (3, "three"))

for ((num, str) <- pairs) {
  println(s"$num = $str")
}

// Option에서 값 추출
val maybeValues = List(Some(1), None, Some(3), None, Some(5))
for (Some(value) <- maybeValues) {
  println(value)  // 1, 3, 5 (None은 건너뜀)
}
```

### Scala 3 문법

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// do 키워드 (선택)
for i <- 1 to 5 do
  println(i)

// 들여쓰기 기반
for
  i <- 1 to 3
  j <- 1 to 3
do
  println(s"$i, $j")

// yield
val result = for
  i <- 1 to 5
  if i % 2 == 0
yield i * i
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// 중괄호 사용
for (i <- 1 to 5) {
  println(i)
}

// 여러 생성자
for {
  i <- 1 to 3
  j <- 1 to 3
} {
  println(s"$i, $j")
}

// yield
val result = for {
  i <- 1 to 5
  if i % 2 == 0
} yield i * i
```
{{% /tab %}}
{{< /tabs >}}

## while 루프

`while`은 표현식이 아닌 문(statement)입니다. 값을 반환하지 않으며 `Unit`을 반환합니다.

```scala
var i = 0
while (i < 5) {
  println(i)
  i += 1
}
```

### do-while (Scala 2 전용)

> ⚠️ **주의:** `do-while`은 **Scala 3에서 제거**되었습니다. Scala 3에서는 `while` 루프로 대체하세요.

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// do-while 대신 while 사용
var j = 0
while {
  println(j)
  j += 1
  j < 5  // 조건을 마지막에 평가
} do ()

// 또는 더 간단하게
var k = 0
while
  println(k)
  k += 1
  k < 5
do ()
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// do-while 사용 가능
var j = 0
do {
  println(j)
  j += 1
} while (j < 5)
```
{{% /tab %}}
{{< /tabs >}}

> **함수형 프로그래밍에서는 `while`보다 `for`나 재귀를 선호합니다.**
> `while`은 가변 상태(`var`)가 필요하기 때문입니다.

## match 표현식

Scala의 `match`는 Java의 `switch`보다 훨씬 강력합니다.

### 기본 매칭

```scala
val day = 3

val dayName = day match {
  case 1 => "월요일"
  case 2 => "화요일"
  case 3 => "수요일"
  case 4 => "목요일"
  case 5 => "금요일"
  case 6 => "토요일"
  case 7 => "일요일"
  case _ => "잘못된 값"  // 기본값 (와일드카드)
}
println(dayName)  // 수요일
```

### 타입 매칭

```scala
def describe(x: Any): String = x match {
  case i: Int    => s"정수: $i"
  case s: String => s"문자열: $s"
  case d: Double => s"실수: $d"
  case _         => "알 수 없는 타입"
}

println(describe(42))      // 정수: 42
println(describe("hello")) // 문자열: hello
println(describe(3.14))    // 실수: 3.14
```

### 가드 조건

```scala
val x = 15

val result = x match {
  case n if n < 0  => "음수"
  case n if n == 0 => "영"
  case n if n < 10 => "한 자리 양수"
  case n if n < 100 => "두 자리 양수"
  case _ => "세 자리 이상"
}
println(result)  // 두 자리 양수
```

### OR 패턴

```scala
val char = 'a'

val result = char match {
  case 'a' | 'e' | 'i' | 'o' | 'u' => "모음"
  case _ => "자음"
}
```

### Scala 3 문법

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
val day = 3

// 들여쓰기 기반
val dayName = day match
  case 1 => "월요일"
  case 2 => "화요일"
  case 3 => "수요일"
  case _ => "기타"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
val day = 3

// 중괄호 필수
val dayName = day match {
  case 1 => "월요일"
  case 2 => "화요일"
  case 3 => "수요일"
  case _ => "기타"
}
```
{{% /tab %}}
{{< /tabs >}}

## 표현식 vs 문

Scala에서 거의 모든 것은 표현식입니다.

```scala
// 블록도 표현식 - 마지막 값이 결과
val result = {
  val a = 1
  val b = 2
  a + b  // 블록의 결과값
}
println(result)  // 3

// try-catch도 표현식
val parsed: Int = try {
  "42".toInt
} catch {
  case _: NumberFormatException => 0
}

// throw도 표현식 (Nothing 타입)
def divide(a: Int, b: Int): Int =
  if (b == 0) throw new ArithmeticException("0으로 나눌 수 없음")
  else a / b
```

## 연습 문제

### 1. FizzBuzz

1부터 100까지 숫자에 대해:
- 3의 배수면 "Fizz"
- 5의 배수면 "Buzz"
- 3과 5의 배수면 "FizzBuzz"
- 그 외에는 숫자 출력

<details>
<summary>정답 보기</summary>

```scala
for (i <- 1 to 100) {
  val result = (i % 3, i % 5) match {
    case (0, 0) => "FizzBuzz"
    case (0, _) => "Fizz"
    case (_, 0) => "Buzz"
    case _      => i.toString
  }
  println(result)
}
```

</details>

### 2. 구구단 표

2단부터 9단까지 구구단을 `for` + `yield`로 생성하세요.

<details>
<summary>정답 보기</summary>

```scala
val gugudan = for {
  i <- 2 to 9
  j <- 1 to 9
} yield s"$i x $j = ${i * j}"

gugudan.foreach(println)
```

</details>

### 3. 학점 계산

점수(0~100)를 받아 학점을 반환하는 함수를 작성하세요.
- 90 이상: A
- 80 이상: B
- 70 이상: C
- 60 이상: D
- 60 미만: F

<details>
<summary>정답 보기</summary>

```scala
def grade(score: Int): String = score match {
  case s if s >= 90 => "A"
  case s if s >= 80 => "B"
  case s if s >= 70 => "C"
  case s if s >= 60 => "D"
  case _            => "F"
}

println(grade(95))  // A
println(grade(72))  // C
println(grade(55))  // F
```

</details>

## 다음 단계

- [함수와 메서드](../functions-methods/) — 함수 정의와 고급 기능
- [패턴 매칭](../pattern-matching/) — match 표현식 심화
