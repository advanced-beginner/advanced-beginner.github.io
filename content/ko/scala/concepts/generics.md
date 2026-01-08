---
lastmod: "2026-01-06"
title: 제네릭
weight: 9
---

제네릭(Generics)을 사용하면 타입 안전하면서 재사용 가능한 코드를 작성할 수 있습니다.

## 타입 매개변수

### 클래스에서

```scala
// 단일 타입 매개변수
class Box[A](value: A) {
  def get: A = value
  def map[B](f: A => B): Box[B] = new Box(f(value))
}

val intBox = new Box(42)
val strBox = new Box("hello")

intBox.get        // 42
strBox.get        // "hello"
intBox.map(_ * 2) // Box(84)

// 여러 타입 매개변수
class Pair[A, B](val first: A, val second: B) {
  def swap: Pair[B, A] = new Pair(second, first)
}

val pair = new Pair(1, "one")
pair.first   // 1
pair.second  // "one"
pair.swap    // Pair("one", 1)
```

### 메서드에서

```scala
def identity[A](x: A): A = x

identity(42)      // 42
identity("hello") // "hello"

def swap[A, B](pair: (A, B)): (B, A) = (pair._2, pair._1)

swap((1, "one"))  // ("one", 1)
```

### 트레이트에서

```scala
trait Container[A] {
  def get: A
  def map[B](f: A => B): Container[B]
}

class Box[A](value: A) extends Container[A] {
  def get: A = value
  def map[B](f: A => B): Container[B] = new Box(f(value))
}
```

## 타입 경계 (Type Bounds)

### 타입 경계 시각화

```mermaid
graph TB
    subgraph "상한 경계 (Upper Bound)"
        direction TB
        Animal["Animal"]
        Dog["Dog"]
        Cat["Cat"]
        Dog -->|"<:"| Animal
        Cat -->|"<:"| Animal
        UB["A <: Animal<br/>A는 Animal의 하위 타입"]
    end

    subgraph "하한 경계 (Lower Bound)"
        direction TB
        Fruit["Fruit"]
        Apple["Apple"]
        RedApple["RedApple"]
        Apple -->|"<:"| Fruit
        RedApple -->|"<:"| Apple
        LB["B >: Apple<br/>B는 Apple의 상위 타입"]
    end
```

### 상한 경계 (Upper Bound)

`A <: B`는 A가 B의 하위 타입이어야 함을 의미합니다.

```scala
trait Animal {
  def name: String
}

class Dog(val name: String) extends Animal
class Cat(val name: String) extends Animal

// A는 Animal의 하위 타입이어야 함
def printNames[A <: Animal](animals: List[A]): Unit =
  animals.foreach(a => println(a.name))

printNames(List(Dog("바둑이"), Dog("멍멍이")))
// printNames(List("not an animal"))  // 컴파일 에러
```

### 하한 경계 (Lower Bound)

`A >: B`는 A가 B의 상위 타입이어야 함을 의미합니다.

```scala
class Fruit
class Apple extends Fruit
class RedApple extends Apple

// B는 Apple의 상위 타입이어야 함
def addFruit[B >: Apple](fruits: List[B], fruit: B): List[B] =
  fruit :: fruits

val fruits: List[Fruit] = List(new Apple)
addFruit(fruits, new Fruit)     // OK - Fruit >: Apple
addFruit(fruits, new Apple)     // OK - Apple >: Apple (같은 타입)
addFruit(fruits, new RedApple)  // OK - RedApple은 Apple의 서브타입이므로 Apple로 업캐스트됨
```

> 💡 **하한 경계의 핵심:** `B >: Apple`은 "B는 Apple이거나 Apple의 상위 타입"을 의미합니다. 서브타입(RedApple)도 Apple로 업캐스트되어 사용 가능합니다.

### 컨텍스트 경계 (Context Bound)

`A : Ordering`는 `Ordering[A]`의 암시적 인스턴스가 필요함을 의미합니다.

```scala
// 컨텍스트 경계
def max[A: Ordering](a: A, b: A): A = {
  val ord = implicitly[Ordering[A]]
  if (ord.gt(a, b)) a else b
}

max(1, 2)        // 2
max("a", "b")    // "b"

// 위와 동등한 표현
def max2[A](a: A, b: A)(implicit ord: Ordering[A]): A =
  if (ord.gt(a, b)) a else b
```

## 타입 추론

```scala
// 타입이 추론됨
val list = List(1, 2, 3)           // List[Int]
val map = Map("a" -> 1, "b" -> 2)  // Map[String, Int]

// 명시적 타입 필요한 경우
val empty = List.empty[Int]        // List[Int]
val none: Option[Int] = None       // Option[Int]
```

## 공통 제네릭 타입

### Option[A]

```scala
val some: Option[Int] = Some(42)
val none: Option[Int] = None

some.map(_ * 2)        // Some(84)
none.map(_ * 2)        // None
some.getOrElse(0)      // 42
none.getOrElse(0)      // 0
```

### Either[A, B]

```scala
val right: Either[String, Int] = Right(42)
val left: Either[String, Int] = Left("error")

right.map(_ * 2)       // Right(84)
left.map(_ * 2)        // Left("error")

// 패턴 매칭
right match {
  case Right(value) => s"값: $value"
  case Left(error)  => s"오류: $error"
}
```

### Try[A]

```scala
import scala.util.{Try, Success, Failure}

val success: Try[Int] = Try("42".toInt)
val failure: Try[Int] = Try("abc".toInt)

success.map(_ * 2)     // Success(84)
failure.map(_ * 2)     // Failure(NumberFormatException)

success.getOrElse(0)   // 42
failure.getOrElse(0)   // 0
```

## 제네릭 ADT

```scala
// 제네릭 결과 타입
sealed trait Result[+E, +A]
case class Success[A](value: A) extends Result[Nothing, A]
case class Error[E](error: E) extends Result[E, Nothing]

def divide(a: Int, b: Int): Result[String, Int] =
  if (b == 0) Error("0으로 나눌 수 없음")
  else Success(a / b)

divide(10, 2) match {
  case Success(v) => println(s"결과: $v")
  case Error(e)   => println(s"오류: $e")
}
```

## Java 제네릭과의 비교

| 특성 | Scala | Java |
|------|-------|------|
| 문법 | `[A]` | `<A>` |
| 상한 경계 | `A <: B` | `A extends B` |
| 하한 경계 | `A >: B` | `A super B` |
| 와일드카드 | `_` | `?` |
| 변성 | 선언 시점 | 사용 시점 |

```scala
// Scala
class Box[A](val value: A)
def process[A <: Comparable[A]](a: A): Unit = ???

// Java 등가
// class Box<A> { ... }
// void process<A extends Comparable<A>>(A a) { ... }
```

## 연습 문제

### 1. 제네릭 Stack 구현 ⭐⭐⭐

불변 Stack을 제네릭으로 구현하세요.

<details>
<summary>정답 보기</summary>

```scala
sealed trait Stack[+A] {
  def push[B >: A](elem: B): Stack[B]
  def pop: (A, Stack[A])
  def isEmpty: Boolean
}

case object EmptyStack extends Stack[Nothing] {
  def push[B](elem: B): Stack[B] = NonEmptyStack(elem, this)
  def pop: Nothing = throw new NoSuchElementException("Empty stack")
  def isEmpty: Boolean = true
}

case class NonEmptyStack[+A](top: A, rest: Stack[A]) extends Stack[A] {
  def push[B >: A](elem: B): Stack[B] = NonEmptyStack(elem, this)
  def pop: (A, Stack[A]) = (top, rest)
  def isEmpty: Boolean = false
}

val stack = EmptyStack.push(1).push(2).push(3)
val (top, rest) = stack.pop  // (3, Stack(2, 1))
```

</details>

### 2. 제네릭 find 함수 ⭐⭐

리스트에서 조건에 맞는 첫 번째 요소를 찾는 제네릭 함수를 구현하세요.

<details>
<summary>정답 보기</summary>

```scala
def find[A](list: List[A])(predicate: A => Boolean): Option[A] =
  list match {
    case Nil                        => None
    case head :: _ if predicate(head) => Some(head)
    case _ :: tail                  => find(tail)(predicate)
  }

find(List(1, 2, 3, 4, 5))(_ > 3)  // Some(4)
find(List("a", "bb", "ccc"))(_.length > 2)  // Some("ccc")
```

</details>

## 다음 단계

- [공변성/반공변성](../variance/) — 제네릭 타입의 변성
- [타입 클래스](../type-classes/) — Ad-hoc 다형성
