---
lastmod: "2026-01-06"
title: 함수와 메서드
weight: 3
---

Scala에서 함수는 일급 시민(first-class citizen)입니다. 함수를 변수에 저장하고, 인자로 전달하고, 반환값으로 사용할 수 있습니다.

## 메서드 정의

### 기본 문법

`def` 키워드로 메서드를 정의합니다.

```scala
// 기본 형태
def add(a: Int, b: Int): Int = {
  a + b
}

// 한 줄이면 중괄호 생략 가능
def add(a: Int, b: Int): Int = a + b

// 반환 타입 추론 (권장하지 않음 - 명시적이 좋음)
def add(a: Int, b: Int) = a + b
```

### 매개변수 타입은 필수

Scala는 매개변수 타입을 추론하지 않습니다. 반드시 명시해야 합니다.

```scala
// 올바름
def greet(name: String): String = s"Hello, $name!"

// 컴파일 에러
// def greet(name) = s"Hello, $name!"
```

### Unit 반환 (부수 효과)

아무것도 반환하지 않는 메서드는 `Unit`을 반환합니다.

```scala
def printGreeting(name: String): Unit = {
  println(s"Hello, $name!")
}

// 축약형 (반환 타입 생략)
def printGreeting(name: String) = println(s"Hello, $name!")
```

### Scala 3 문법

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// 들여쓰기 기반
def greet(name: String): String =
  val greeting = s"Hello, $name!"
  greeting

// 여러 줄
def calculate(x: Int, y: Int): Int =
  val sum = x + y
  val product = x * y
  sum + product
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// 중괄호 필수
def greet(name: String): String = {
  val greeting = s"Hello, $name!"
  greeting
}

// 여러 줄
def calculate(x: Int, y: Int): Int = {
  val sum = x + y
  val product = x * y
  sum + product
}
```
{{% /tab %}}
{{< /tabs >}}

## 기본 매개변수 값

매개변수에 기본값을 지정할 수 있습니다.

```scala
def greet(name: String = "World", punctuation: String = "!"): String =
  s"Hello, $name$punctuation"

println(greet())                    // Hello, World!
println(greet("Scala"))             // Hello, Scala!
println(greet("Scala", "?"))        // Hello, Scala?
```

## 이름 있는 인자

인자를 이름으로 전달하면 순서를 바꿀 수 있습니다.

```scala
def createPerson(name: String, age: Int, city: String): String =
  s"$name, $age살, $city 거주"

// 순서 변경 가능
println(createPerson(age = 30, city = "서울", name = "김철수"))

// 일부만 이름 지정
println(createPerson("김영희", city = "부산", age = 25))
```

## 가변 인자 (Varargs)

`*`를 사용하여 가변 개수의 인자를 받을 수 있습니다.

```scala
def sum(numbers: Int*): Int = numbers.sum

println(sum(1, 2, 3))        // 6
println(sum(1, 2, 3, 4, 5))  // 15

// 시퀀스를 펼쳐서 전달
val nums = Seq(1, 2, 3, 4, 5)
println(sum(nums*))          // Scala 3
// println(sum(nums: _*))    // Scala 2
```

## 익명 함수 (람다)

함수를 이름 없이 정의할 수 있습니다.

### 기본 문법

```scala
// 전체 형태
val add: (Int, Int) => Int = (a: Int, b: Int) => a + b

// 타입 추론
val add = (a: Int, b: Int) => a + b

// 매개변수가 하나면 괄호 생략 가능
val double = (x: Int) => x * 2

// 사용
println(add(1, 2))    // 3
println(double(5))    // 10
```

### 축약 문법

```scala
val numbers = List(1, 2, 3, 4, 5)

// 전체 형태
numbers.map((x: Int) => x * 2)

// 타입 추론
numbers.map(x => x * 2)

// 플레이스홀더 문법 (각 _ 는 다른 인자)
numbers.map(_ * 2)

// 여러 인자
numbers.reduce((a, b) => a + b)
numbers.reduce(_ + _)
```

### 여러 줄 람다

```scala
val process = (x: Int) => {
  val doubled = x * 2
  val squared = doubled * doubled
  squared
}
```

## 고차 함수

함수를 인자로 받거나 반환하는 함수입니다.

### 함수를 인자로 받기

```scala
def applyTwice(f: Int => Int, x: Int): Int = f(f(x))

val double = (x: Int) => x * 2
println(applyTwice(double, 3))  // 12 (3 -> 6 -> 12)

// 람다 직접 전달
println(applyTwice(x => x + 10, 5))  // 25 (5 -> 15 -> 25)
```

### 함수를 반환하기

```scala
def multiplier(factor: Int): Int => Int = {
  (x: Int) => x * factor
}

val triple = multiplier(3)
println(triple(5))   // 15
println(triple(10))  // 30
```

## 커링 (Currying)

여러 매개변수 리스트로 함수를 정의할 수 있습니다.

```scala
// 커링된 함수
def add(a: Int)(b: Int): Int = a + b

println(add(1)(2))  // 3
```

### 부분 적용 (Partial Application)

커링된 함수의 일부 인자만 적용하여 새 함수를 만들 수 있습니다:

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
def add(a: Int)(b: Int): Int = a + b

// 첫 번째 매개변수만 적용
val add5 = add(5)   // Int => Int
println(add5(10))   // 15

// 타입 명시도 가능
val add10: Int => Int = add(10)
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
def add(a: Int)(b: Int): Int = a + b

// 언더스코어로 부분 적용 명시 필요
val add5 = add(5)_  // Int => Int
println(add5(10))   // 15

// 또는 타입 힌트 제공
val add10: Int => Int = add(10)
```
{{% /tab %}}
{{< /tabs >}}

> 💡 **차이점:** Scala 2에서는 `add(5)_`처럼 언더스코어가 필요했지만, Scala 3에서는 컴파일러가 문맥에서 추론합니다.

### 커링의 활용

```scala
// 타입 추론 개선
def transform[A, B](list: List[A])(f: A => B): List[B] =
  list.map(f)

// f의 타입이 첫 번째 매개변수에서 추론됨
transform(List(1, 2, 3))(x => x * 2)  // List(2, 4, 6)

// 커링을 사용한 DSL
def withResource[T](resource: => T)(cleanup: T => Unit)(action: T => Unit): Unit = {
  val r = resource
  try {
    action(r)
  } finally {
    cleanup(r)
  }
}
```

## 부분 적용 함수

일부 인자만 적용한 새 함수를 만들 수 있습니다.

```scala
def log(level: String, message: String): Unit =
  println(s"[$level] $message")

// 부분 적용
val info = log("INFO", _)
val error = log("ERROR", _)

info("시작합니다")   // [INFO] 시작합니다
error("오류 발생")   // [ERROR] 오류 발생
```

## 재귀 함수

재귀 함수는 반환 타입을 반드시 명시해야 합니다.

```scala
def factorial(n: Int): Int =
  if (n <= 1) 1
  else n * factorial(n - 1)

println(factorial(5))  // 120
```

### 꼬리 재귀 최적화

`@tailrec` 어노테이션으로 꼬리 재귀 최적화를 보장할 수 있습니다.

```scala
import scala.annotation.tailrec

def factorial(n: Int): Int = {
  @tailrec
  def loop(n: Int, acc: Int): Int =
    if (n <= 1) acc
    else loop(n - 1, n * acc)

  loop(n, 1)
}

println(factorial(5))  // 120
```

## 함수 타입

함수 타입은 `(인자타입들) => 반환타입` 형식입니다.

```scala
// 함수 타입 선언
val f1: Int => Int = x => x * 2
val f2: (Int, Int) => Int = (a, b) => a + b
val f3: () => Int = () => 42
val f4: Int => Int => Int = a => b => a + b  // 커링

// 고차 함수의 타입
def process(f: String => Int): Int = f("hello")
```

## @main 어노테이션 (Scala 3)

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// 간단한 진입점
@main def hello(): Unit =
  println("Hello, World!")

// 인자 받기
@main def greet(name: String, times: Int): Unit =
  for _ <- 1 to times do
    println(s"Hello, $name!")

// 실행: scala greet Scala 3
// 출력:
// Hello, Scala!
// Hello, Scala!
// Hello, Scala!
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// object의 main 메서드
object Hello {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
  }
}

// 또는 App 트레이트 확장
object Hello extends App {
  println("Hello, World!")
}
```
{{% /tab %}}
{{< /tabs >}}

## 연습 문제

### 1. 고차 함수 구현 ⭐⭐

`applyAll` 함수를 구현하세요. 값과 함수 리스트를 받아 모든 함수를 순차적으로 적용합니다.

```scala
def applyAll(x: Int, functions: List[Int => Int]): Int = ???

val fns = List(
  (x: Int) => x + 1,
  (x: Int) => x * 2,
  (x: Int) => x - 3
)
println(applyAll(5, fns))  // ((5 + 1) * 2) - 3 = 9
```

<details>
<summary>정답 보기</summary>

```scala
def applyAll(x: Int, functions: List[Int => Int]): Int =
  functions.foldLeft(x)((acc, f) => f(acc))
```

</details>

### 2. 커링 변환 ⭐

일반 함수를 커링된 함수로 변환하세요.

```scala
def add(a: Int, b: Int, c: Int): Int = a + b + c

// 변환 결과
val curriedAdd: Int => Int => Int => Int = ???
```

<details>
<summary>정답 보기</summary>

```scala
val curriedAdd: Int => Int => Int => Int =
  a => b => c => a + b + c

// 또는
val curriedAdd = (add _).curried
```

</details>

### 3. 꼬리 재귀 피보나치 ⭐⭐⭐

꼬리 재귀로 피보나치 수를 계산하는 함수를 작성하세요.

<details>
<summary>정답 보기</summary>

```scala
import scala.annotation.tailrec

def fibonacci(n: Int): Long = {
  @tailrec
  def loop(n: Int, prev: Long, curr: Long): Long =
    if (n <= 0) prev
    else loop(n - 1, curr, prev + curr)

  loop(n, 0, 1)
}

println(fibonacci(10))  // 55
println(fibonacci(50))  // 12586269025
```

</details>

## 다음 단계

- [클래스와 객체](../classes-objects/) — OOP 기초
- [고차 함수](../higher-order-functions/) — 함수형 프로그래밍 심화
