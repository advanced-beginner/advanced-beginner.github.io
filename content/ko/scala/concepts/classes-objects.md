---
title: 클래스와 객체
weight: 4
---

Scala는 객체지향과 함수형 프로그래밍을 모두 지원합니다. 이 문서에서는 클래스, 객체, 트레이트 등 OOP 기능을 다룹니다.

## 클래스

### 기본 클래스 정의

```scala
// 기본 클래스
class Person(name: String, age: Int) {
  def greet(): String = s"안녕하세요, $name 입니다."
}

val person = new Person("김철수", 30)
println(person.greet())  // 안녕하세요, 김철수 입니다.
```

### 생성자 매개변수

생성자 매개변수에 `val` 또는 `var`를 붙이면 자동으로 필드가 됩니다.

```scala
// name은 외부에서 접근 불가, age는 val 필드
class Person(name: String, val age: Int)

val p = new Person("김철수", 30)
// println(p.name)  // 컴파일 에러
println(p.age)      // 30

// var로 선언하면 변경 가능
class MutablePerson(var name: String, var age: Int)

val mp = new MutablePerson("김철수", 30)
mp.age = 31
println(mp.age)  // 31
```

### 보조 생성자

```scala
class Person(val name: String, val age: Int) {
  // 보조 생성자
  def this(name: String) = this(name, 0)
  def this() = this("Unknown", 0)
}

val p1 = new Person("김철수", 30)
val p2 = new Person("김영희")      // age = 0
val p3 = new Person()              // name = "Unknown", age = 0
```

### 기본값 사용 (권장)

```scala
// 보조 생성자보다 기본값이 더 깔끔함
class Person(val name: String = "Unknown", val age: Int = 0)

val p1 = new Person("김철수", 30)
val p2 = new Person("김영희")
val p3 = new Person()
```

### Scala 3 문법

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
class Person(val name: String, val age: Int):
  def greet(): String = s"안녕하세요, $name 입니다."

  def isAdult: Boolean = age >= 18

  override def toString: String = s"Person($name, $age)"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
class Person(val name: String, val age: Int) {
  def greet(): String = s"안녕하세요, $name 입니다."

  def isAdult: Boolean = age >= 18

  override def toString: String = s"Person($name, $age)"
}
```
{{% /tab %}}
{{< /tabs >}}

## 객체 (Object)

`object`는 싱글톤 인스턴스를 정의합니다.

### 싱글톤 객체

```scala
object DatabaseConnection {
  private var connection: String = _

  def connect(url: String): Unit = {
    connection = url
    println(s"Connected to $url")
  }

  def getConnection: String = connection
}

// new 없이 직접 사용
DatabaseConnection.connect("jdbc:mysql://localhost/db")
println(DatabaseConnection.getConnection)
```

### 유틸리티 메서드

```scala
object MathUtils {
  def square(x: Int): Int = x * x
  def cube(x: Int): Int = x * x * x
  val PI: Double = 3.14159
}

println(MathUtils.square(5))  // 25
println(MathUtils.PI)         // 3.14159
```

## 컴패니언 객체

클래스와 같은 이름의 객체를 **컴패니언 객체**라고 합니다.

```scala
class Circle(val radius: Double) {
  import Circle._  // 컴패니언 객체의 멤버 import

  def area: Double = PI * radius * radius
  def circumference: Double = 2 * PI * radius
}

object Circle {
  val PI: Double = 3.14159

  // 팩토리 메서드
  def apply(radius: Double): Circle = new Circle(radius)

  def fromDiameter(diameter: Double): Circle = new Circle(diameter / 2)
}

// apply 덕분에 new 없이 생성 가능
val c1 = Circle(5)
val c2 = Circle.fromDiameter(10)

println(c1.area)  // 78.53975
```

### private 멤버 접근

컴패니언 객체와 클래스는 서로의 `private` 멤버에 접근할 수 있습니다.

```scala
class Person private (val name: String, val age: Int)

object Person {
  def create(name: String, age: Int): Option[Person] =
    if (age >= 0) Some(new Person(name, age))  // private 생성자 접근
    else None
}

val person = Person.create("김철수", 30)  // Some(Person)
val invalid = Person.create("오류", -5)   // None
```

## 트레이트 (Trait)

트레이트는 Java의 인터페이스와 유사하지만, 구현을 포함할 수 있습니다.

### 기본 트레이트

```scala
trait Greeter {
  def greet(name: String): String
}

class FormalGreeter extends Greeter {
  def greet(name: String): String = s"안녕하십니까, $name 님."
}

class CasualGreeter extends Greeter {
  def greet(name: String): String = s"안녕, $name!"
}
```

### 구현 포함 트레이트

```scala
trait Logger {
  def log(message: String): Unit = println(s"[LOG] $message")

  def info(message: String): Unit = log(s"[INFO] $message")
  def error(message: String): Unit = log(s"[ERROR] $message")
}

class MyService extends Logger {
  def doSomething(): Unit = {
    info("작업 시작")
    // 작업 수행
    info("작업 완료")
  }
}
```

### 다중 트레이트 (Mixin)

```scala
trait Swimmer {
  def swim(): String = "수영 중..."
}

trait Flyer {
  def fly(): String = "비행 중..."
}

// 다중 트레이트 믹스인
class Duck extends Swimmer with Flyer {
  def quack(): String = "꽥꽥!"
}

val duck = new Duck
println(duck.swim())   // 수영 중...
println(duck.fly())    // 비행 중...
println(duck.quack())  // 꽥꽥!
```

### 트레이트 스태킹

```scala
trait Base {
  def process(s: String): String = s
}

trait Uppercase extends Base {
  override def process(s: String): String = super.process(s.toUpperCase)
}

trait Trim extends Base {
  override def process(s: String): String = super.process(s.trim)
}

// 오른쪽에서 왼쪽으로 적용
class TextProcessor extends Base with Trim with Uppercase

val processor = new TextProcessor
println(processor.process("  hello world  "))  // HELLO WORLD
```

## 추상 클래스

```scala
abstract class Animal(val name: String) {
  // 추상 메서드
  def speak(): String

  // 구현된 메서드
  def describe(): String = s"$name 은(는) ${speak()} 소리를 냅니다."
}

class Dog(name: String) extends Animal(name) {
  def speak(): String = "멍멍"
}

class Cat(name: String) extends Animal(name) {
  def speak(): String = "야옹"
}

val dog = new Dog("바둑이")
println(dog.describe())  // 바둑이 은(는) 멍멍 소리를 냅니다.
```

### 추상 클래스 vs 트레이트

| 특성 | 추상 클래스 | 트레이트 |
|------|------------|---------|
| 생성자 매개변수 | 가능 | Scala 3에서만 가능 |
| 다중 상속 | 불가 | 가능 (믹스인) |
| Java 호환성 | 좋음 | 제한적 |

> **권장:** 특별한 이유가 없으면 트레이트를 사용하세요.

## 접근 제어자

```scala
class MyClass {
  private val privateField = 1      // 이 클래스에서만
  protected val protectedField = 2  // 이 클래스와 하위 클래스에서
  val publicField = 3               // 어디서나

  private[this] val strictPrivate = 4  // 이 인스턴스에서만
}

// 패키지 레벨 접근
class PackageAccess {
  private[mypackage] val packagePrivate = 5  // mypackage 내에서만
}
```

## Enum (Scala 3)

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// 단순 열거형
enum Color:
  case Red, Green, Blue

val color = Color.Red
println(color)  // Red

// 매개변수가 있는 열거형
enum Planet(val mass: Double, val radius: Double):
  case Mercury extends Planet(3.303e+23, 2.4397e6)
  case Venus   extends Planet(4.869e+24, 6.0518e6)
  case Earth   extends Planet(5.976e+24, 6.37814e6)

println(Planet.Earth.mass)  // 5.976E24

// ADT 스타일
enum Shape:
  case Circle(radius: Double)
  case Rectangle(width: Double, height: Double)
  case Triangle(base: Double, height: Double)

import Shape.*
val shapes = List(Circle(5), Rectangle(3, 4), Triangle(6, 4))
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// sealed trait + case object으로 열거형 구현
sealed trait Color
object Color {
  case object Red extends Color
  case object Green extends Color
  case object Blue extends Color
}

val color: Color = Color.Red

// ADT 스타일
sealed trait Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(width: Double, height: Double) extends Shape
case class Triangle(base: Double, height: Double) extends Shape

val shapes: List[Shape] = List(Circle(5), Rectangle(3, 4), Triangle(6, 4))
```
{{% /tab %}}
{{< /tabs >}}

## 연습 문제

### 1. 은행 계좌 클래스

잔액을 관리하는 `BankAccount` 클래스를 구현하세요.
- `deposit(amount)`: 입금
- `withdraw(amount)`: 출금 (잔액 부족 시 false 반환)
- `balance`: 현재 잔액

<details>
<summary>정답 보기</summary>

```scala
class BankAccount(initialBalance: Double) {
  private var _balance: Double = initialBalance

  def balance: Double = _balance

  def deposit(amount: Double): Unit =
    if (amount > 0) _balance += amount

  def withdraw(amount: Double): Boolean =
    if (amount > 0 && amount <= _balance) {
      _balance -= amount
      true
    } else false
}

val account = new BankAccount(1000)
account.deposit(500)
println(account.balance)       // 1500.0
println(account.withdraw(200)) // true
println(account.balance)       // 1300.0
println(account.withdraw(2000)) // false
```

</details>

### 2. 트레이트 믹스인

`Printable` 트레이트와 `Comparable` 트레이트를 정의하고, `Product` 클래스에 믹스인하세요.

<details>
<summary>정답 보기</summary>

```scala
trait Printable {
  def print(): String
}

trait Comparable[T] {
  def compare(other: T): Int
}

case class Product(name: String, price: Double)
    extends Printable with Comparable[Product] {

  def print(): String = s"상품: $name, 가격: $price"

  def compare(other: Product): Int = this.price.compare(other.price)
}

val p1 = Product("노트북", 1500000)
val p2 = Product("마우스", 50000)

println(p1.print())         // 상품: 노트북, 가격: 1500000.0
println(p1.compare(p2))     // 1 (p1이 더 비쌈)
```

</details>

## 다음 단계

- [케이스 클래스](../case-classes/) — 불변 데이터 모델링
- [패턴 매칭](../pattern-matching/) — match 표현식 심화
