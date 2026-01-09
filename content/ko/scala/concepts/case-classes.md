---
lastmod: "2026-01-09"
title: 케이스 클래스
weight: 5
---

케이스 클래스는 **불변 데이터 모델링**을 위한 특별한 클래스입니다. 보일러플레이트 코드 없이 데이터 클래스를 정의할 수 있습니다. 컴파일러가 equals, hashCode, toString, copy 등 유용한 메서드를 자동으로 생성하므로 데이터 중심의 클래스를 매우 간결하게 정의할 수 있습니다. 특히 패턴 매칭과 함께 사용하면 그 진가를 발휘합니다.

#### 기본 문법

케이스 클래스는 `case class` 키워드로 정의합니다. new 키워드 없이 인스턴스를 생성할 수 있으며, 이는 컴파일러가 자동으로 apply 팩토리 메서드를 생성하기 때문입니다.

```scala
case class Person(name: String, age: Int)

// new 키워드 없이 생성 가능
val alice = Person("Alice", 30)
val bob = Person("Bob", 25)
```

#### 자동 생성되는 기능

케이스 클래스를 선언하면 컴파일러가 여러 유용한 메서드를 자동으로 생성합니다. 이 덕분에 데이터 클래스를 정의할 때 보일러플레이트 코드를 작성할 필요가 없습니다.

**1. apply 메서드 (팩토리)**

apply 메서드가 자동 생성되어 new 키워드 없이 인스턴스를 생성할 수 있습니다.

```scala
// new 없이 생성 가능
val person = Person("Alice", 30)

// 실제로는 이렇게 동작
val person = Person.apply("Alice", 30)
```

**2. unapply 메서드 (추출자)**

unapply 메서드가 자동 생성되어 패턴 매칭에서 필드를 추출할 수 있습니다.

```scala
val Person(name, age) = Person("Alice", 30)
println(name)  // Alice
println(age)   // 30
```

**3. 필드 접근자**

모든 생성자 매개변수가 자동으로 `val`로 선언되어 외부에서 접근할 수 있습니다. 케이스 클래스는 기본적으로 불변입니다.

```scala
val person = Person("Alice", 30)
println(person.name)  // Alice
println(person.age)   // 30

// 불변이므로 수정 불가
// person.age = 31  // 컴파일 에러!
```

**4. copy 메서드**

불변 객체의 일부 필드만 변경한 새 인스턴스를 생성합니다. 원본 객체는 변하지 않습니다.

```scala
val alice = Person("Alice", 30)

// 나이만 변경한 새 인스턴스
val olderAlice = alice.copy(age = 31)
println(olderAlice)  // Person(Alice,31)

// 이름만 변경
val bob = alice.copy(name = "Bob")
println(bob)  // Person(Bob,30)

// 여러 필드 변경
val carol = alice.copy(name = "Carol", age = 25)
println(carol)  // Person(Carol,25)
```

**5. equals와 hashCode**

구조적 동등성(structural equality)을 제공합니다. 일반 클래스와 달리 참조가 아닌 필드 값으로 동등성을 비교합니다.

```scala
val person1 = Person("Alice", 30)
val person2 = Person("Alice", 30)
val person3 = Person("Bob", 25)

println(person1 == person2)  // true (내용이 같음)
println(person1 == person3)  // false

// HashSet/HashMap에서 올바르게 동작
val set = Set(person1, person2)
println(set.size)  // 1 (중복 제거됨)
```

**6. toString**

읽기 좋은 문자열 표현을 제공합니다. 디버깅과 로깅에 유용합니다.

```scala
val person = Person("Alice", 30)
println(person.toString)  // Person(Alice,30)
println(person)           // Person(Alice,30)
```

#### 중첩 케이스 클래스

케이스 클래스는 다른 케이스 클래스를 필드로 가질 수 있습니다. 중첩 copy를 사용하면 깊은 구조의 불변 객체도 쉽게 업데이트할 수 있습니다.

```scala
case class Address(city: String, zipCode: String)
case class Employee(name: String, address: Address)

val emp = Employee("김철수", Address("서울", "12345"))

// 중첩 copy
val empInBusan = emp.copy(address = emp.address.copy(city = "부산"))
println(empInBusan)  // Employee(김철수,Address(부산,12345))
```

#### 패턴 매칭과 함께 사용

케이스 클래스는 패턴 매칭과 함께 사용할 때 진가를 발휘합니다. 자동 생성되는 unapply 메서드 덕분에 필드를 쉽게 추출하고 가드 조건도 적용할 수 있습니다.

```scala
case class Order(id: Int, product: String, quantity: Int)

def processOrder(order: Order): String = order match {
  case Order(_, _, q) if q <= 0   => "잘못된 수량"
  case Order(_, _, q) if q > 100  => "대량 주문"
  case Order(id, product, q)      => s"주문 #$id: $product $q개"
}

println(processOrder(Order(1, "노트북", 5)))   // 주문 #1: 노트북 5개
println(processOrder(Order(2, "마우스", 150))) // 대량 주문
println(processOrder(Order(3, "키보드", -1)))  // 잘못된 수량
```

#### ADT (Algebraic Data Types)

케이스 클래스와 `sealed trait`를 조합하여 ADT(대수적 데이터 타입)를 정의할 수 있습니다. ADT는 함수형 프로그래밍에서 도메인 모델을 표현하는 강력한 방법입니다. sealed로 선언하면 모든 하위 타입이 같은 파일에 정의되어야 하므로 컴파일러가 패턴 매칭의 완전성을 검사할 수 있습니다.

**Scala 3**

Scala 3에서는 enum 키워드로 ADT를 더 간결하게 정의할 수 있습니다.

```scala
enum Shape:
  case Circle(radius: Double)
  case Rectangle(width: Double, height: Double)
  case Triangle(base: Double, height: Double)

import Shape.*

def area(shape: Shape): Double = shape match
  case Circle(r)         => math.Pi * r * r
  case Rectangle(w, h)   => w * h
  case Triangle(b, h)    => 0.5 * b * h

println(area(Circle(5)))         // 78.539...
println(area(Rectangle(3, 4)))   // 12.0
```

**Scala 2**

Scala 2에서는 sealed trait과 case class 조합으로 ADT를 구현합니다.

```scala
sealed trait Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(width: Double, height: Double) extends Shape
case class Triangle(base: Double, height: Double) extends Shape

def area(shape: Shape): Double = shape match {
  case Circle(r)         => math.Pi * r * r
  case Rectangle(w, h)   => w * h
  case Triangle(b, h)    => 0.5 * b * h
}
```

**sealed의 중요성**

`sealed`는 같은 파일에서만 상속 가능하게 제한합니다. 이로 인해 다음과 같은 이점이 있습니다.

1. **완전한 패턴 매칭**: 컴파일러가 모든 케이스를 알 수 있음
2. **경고 제공**: 누락된 케이스가 있으면 경고

```scala
// 케이스 누락 시 경고
def describe(shape: Shape): String = shape match {
  case Circle(r) => s"원, 반지름 $r"
  // Rectangle, Triangle 누락 - 컴파일 경고!
}
```

#### Option, Either, Try

Scala 표준 라이브러리의 대표적인 케이스 클래스 활용 예입니다. 이들은 모두 sealed trait와 케이스 클래스로 정의된 ADT이며, null이나 예외 대신 타입 안전한 방식으로 실패 가능한 연산을 표현합니다.

> 💡 아래는 **개념적인 구조**를 보여주는 예시입니다. 실제 표준 라이브러리 구현은 더 복잡하며 다양한 최적화가 적용되어 있습니다.

**Option**

Option은 값이 있거나 없을 수 있는 경우를 표현합니다. null 대신 Option을 사용하면 NullPointerException을 방지할 수 있습니다.

```scala
// 개념적 구조 (실제 구현과 다름)
sealed trait Option[+A]
case class Some[+A](value: A) extends Option[A]
case object None extends Option[Nothing]

// 사용
def divide(a: Int, b: Int): Option[Int] =
  if (b == 0) None else Some(a / b)

divide(10, 2) match {
  case Some(result) => println(s"결과: $result")
  case None         => println("0으로 나눌 수 없음")
}
```

**Either**

Either는 두 가지 가능한 타입 중 하나의 값을 가집니다. 관례상 Left는 실패(에러 메시지 등), Right는 성공 값을 담습니다.

```scala
sealed trait Either[+L, +R]
case class Left[+L](value: L) extends Either[L, Nothing]
case class Right[+R](value: R) extends Either[Nothing, R]

// 사용
def parseAge(input: String): Either[String, Int] =
  input.toIntOption match {
    case Some(age) if age >= 0 => Right(age)
    case Some(_)               => Left("나이는 음수일 수 없습니다")
    case None                  => Left("숫자가 아닙니다")
  }
```

#### 케이스 클래스 vs 일반 클래스

아래 표는 케이스 클래스와 일반 클래스의 주요 차이점을 정리한 것입니다. 케이스 클래스는 불변 데이터 모델링에 최적화되어 있고, 일반 클래스는 가변 상태나 복잡한 동작이 필요할 때 적합합니다.

| 특성 | 케이스 클래스 | 일반 클래스 |
|------|-------------|-----------|
| 불변성 | 기본 불변 (val) | 선택 가능 |
| equals/hashCode | 자동 구조적 비교 | 참조 비교 (기본) |
| copy 메서드 | 자동 생성 | 직접 구현 필요 |
| 패턴 매칭 | unapply 자동 | 직접 구현 필요 |
| new 키워드 | 불필요 | 필요 |

#### 모범 사례

케이스 클래스를 효과적으로 사용하기 위한 권장 사항들입니다.

**1. 불변 데이터에 사용**

케이스 클래스는 불변 데이터를 위해 설계되었습니다. var를 사용하면 equals/hashCode의 불변 가정이 깨질 수 있습니다.

```scala
// 좋음: 불변 데이터
case class Config(host: String, port: Int)

// 피하세요: 가변 상태가 필요한 경우
// case class Counter(var count: Int)  // 안티패턴
```

**2. 작은 도메인 모델에 적합**

케이스 클래스는 간결한 도메인 모델을 정의하는 데 이상적입니다.

```scala
case class Money(amount: BigDecimal, currency: String)
case class OrderLine(product: String, quantity: Int, unitPrice: Money)
case class Order(id: String, lines: List[OrderLine])
```

**3. DTO (Data Transfer Object)**

API 요청/응답 객체에도 케이스 클래스가 적합합니다. JSON 직렬화 라이브러리들도 케이스 클래스를 잘 지원합니다.

```scala
case class CreateUserRequest(name: String, email: String)
case class UserResponse(id: Long, name: String, email: String)
```

**4. 상속 대신 합성**

케이스 클래스 상속은 equals/hashCode 구현에 문제가 생길 수 있어 권장하지 않습니다. 대신 합성을 사용하세요.

```scala
// 피하세요: 케이스 클래스 상속
// case class SpecialPerson(name: String, age: Int, badge: String)
//     extends Person(name, age)  // 문제 발생 가능

// 좋음: 합성 사용
case class Person(name: String, age: Int)
case class Badge(id: String, level: String)
case class Employee(person: Person, badge: Badge)
```

#### 연습 문제

다음 연습 문제들을 통해 케이스 클래스와 ADT 개념을 복습해보세요.

**1. 결과 타입 구현**

`Result[T]` 타입을 구현하세요: `Success(value)` 또는 `Failure(message)`

<details>
<summary>정답 보기</summary>

```scala
sealed trait Result[+T]
case class Success[+T](value: T) extends Result[T]
case class Failure(message: String) extends Result[Nothing]

def divide(a: Int, b: Int): Result[Int] =
  if (b == 0) Failure("0으로 나눌 수 없음")
  else Success(a / b)

divide(10, 2) match {
  case Success(v) => println(s"결과: $v")
  case Failure(m) => println(s"오류: $m")
}
```

</details>

**2. 표현식 트리**

수학 표현식을 나타내는 ADT를 정의하고 계산 함수를 작성하세요.

<details>
<summary>정답 보기</summary>

```scala
sealed trait Expr
case class Num(value: Double) extends Expr
case class Add(left: Expr, right: Expr) extends Expr
case class Mul(left: Expr, right: Expr) extends Expr

def eval(expr: Expr): Double = expr match {
  case Num(v)      => v
  case Add(l, r)   => eval(l) + eval(r)
  case Mul(l, r)   => eval(l) * eval(r)
}

// (1 + 2) * 3
val expr = Mul(Add(Num(1), Num(2)), Num(3))
println(eval(expr))  // 9.0
```

</details>

#### 다음 단계

- [패턴 매칭](../pattern-matching/) — match 표현식 심화
- [컬렉션](../collections/) — Scala 컬렉션 라이브러리
