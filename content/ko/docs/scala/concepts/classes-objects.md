---
lastmod: "2026-01-10"
title: 클래스와 객체
weight: 4
---

{{< callout type="info" title="TL;DR" >}}
- **클래스**: 생성자 매개변수를 선언부에 직접 작성하여 보일러플레이트를 줄입니다
- **object**: 싱글톤 인스턴스를 언어 차원에서 지원합니다
- **컴패니언 객체**: 클래스와 같은 이름으로 static 멤버를 대체합니다
- **트레이트**: 다중 상속의 장점을 누리면서 다이아몬드 문제를 피합니다
{{< /callout >}}

**대상 독자:** Java/OOP 경험이 있는 개발자
**선수 지식:** Scala 기본 문법, 함수와 메서드

Scala는 객체지향과 함수형 프로그래밍을 모두 지원합니다. 클래스, 객체, 트레이트 등 OOP 기능을 함수형 패러다임과 조화롭게 사용할 수 있습니다. 특히 Scala의 object는 싱글톤 패턴을 언어 차원에서 지원하고, trait는 다중 상속의 문제를 해결하면서 코드 재사용을 극대화합니다.

#### 클래스

클래스는 데이터와 동작을 캡슐화하는 기본 단위입니다. Scala의 클래스는 생성자 매개변수를 클래스 선언부에 직접 작성하여 보일러플레이트 코드를 줄입니다.

**기본 클래스 정의**

```scala
// 기본 클래스
class Person(name: String, age: Int) {
  def greet(): String = s"안녕하세요, $name 입니다."
}

val person = new Person("김철수", 30)
println(person.greet())  // 안녕하세요, 김철수 입니다.
```

**생성자 매개변수**

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

**보조 생성자**

보조 생성자는 `this` 키워드로 정의하며, 반드시 주 생성자나 다른 보조 생성자를 먼저 호출해야 합니다.

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

**기본값 사용 (권장)**

보조 생성자보다 기본 매개변수 값을 사용하는 것이 더 간결하고 유지보수하기 쉽습니다.

```scala
// 보조 생성자보다 기본값이 더 깔끔함
class Person(val name: String = "Unknown", val age: Int = 0)

val p1 = new Person("김철수", 30)
val p2 = new Person("김영희")
val p3 = new Person()
```

**Scala 3 문법**

Scala 3에서는 콜론과 들여쓰기로 클래스 본문을 정의할 수 있습니다. 중괄호 없이도 깔끔하게 작성할 수 있습니다.

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

{{< callout type="info" title="핵심 포인트" >}}
- 생성자 매개변수에 `val`/`var`를 붙이면 자동으로 필드가 됩니다
- 기본 매개변수 값을 사용하면 보조 생성자보다 간결합니다
- Scala 3에서는 콜론과 들여쓰기로 클래스 본문을 정의합니다
{{< /callout >}}

#### 객체 (Object)

`object`는 싱글톤 인스턴스를 정의합니다. Java에서 싱글톤 패턴을 구현하려면 private 생성자, static 필드, 동기화 등 복잡한 코드가 필요하지만, Scala에서는 `object` 키워드 하나로 해결됩니다. object는 처음 접근할 때 지연 초기화되며, 스레드 안전합니다.

**싱글톤 객체**

object는 프로그램 전체에서 하나의 인스턴스만 존재합니다. 전역 상태, 유틸리티 메서드, 팩토리 등에 적합합니다.

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

**유틸리티 메서드**

상태가 없는 순수 함수들의 모음으로 object를 사용하면 Java의 static 메서드와 유사하게 활용할 수 있습니다.

```scala
object MathUtils {
  def square(x: Int): Int = x * x
  def cube(x: Int): Int = x * x * x
  val PI: Double = 3.14159
}

println(MathUtils.square(5))  // 25
println(MathUtils.PI)         // 3.14159
```

{{< callout type="info" title="핵심 포인트" >}}
- `object`는 싱글톤 인스턴스를 정의합니다 (new 없이 사용)
- 지연 초기화되며 스레드 안전합니다
- 유틸리티 메서드, 전역 상태, 팩토리에 적합합니다
{{< /callout >}}

#### 컴패니언 객체

클래스와 같은 이름의 객체를 **컴패니언 객체**라고 합니다. 컴패니언 객체는 반드시 같은 파일에 정의해야 하며, 클래스와 서로의 private 멤버에 접근할 수 있습니다. Java의 static 멤버를 대체하며, 팩토리 메서드나 상수를 정의하는 데 주로 사용됩니다.

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

**private 멤버 접근**

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

{{< callout type="info" title="핵심 포인트" >}}
- 컴패니언 객체는 클래스와 같은 이름으로 같은 파일에 정의합니다
- 서로의 `private` 멤버에 접근할 수 있습니다
- `apply` 메서드로 new 없이 인스턴스를 생성할 수 있습니다
{{< /callout >}}

#### 트레이트 (Trait)

트레이트는 Java의 인터페이스와 유사하지만, 구현을 포함할 수 있습니다. 클래스는 여러 트레이트를 믹스인(mixin)할 수 있어 다중 상속의 장점을 누리면서도 다이아몬드 문제를 피할 수 있습니다. 트레이트는 코드 재사용, 관심사 분리, 모듈화에 핵심적인 역할을 합니다.

**기본 트레이트**

추상 메서드만 정의하면 Java 인터페이스처럼 사용할 수 있습니다.

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

**구현 포함 트레이트**

트레이트에 기본 구현을 제공하면 구현 클래스에서 재정의하지 않고 그대로 사용할 수 있습니다.

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

**다중 트레이트 (Mixin)**

`with` 키워드로 여러 트레이트를 조합할 수 있습니다. 첫 번째 상위 타입은 `extends`, 나머지는 `with`를 사용합니다.

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

**트레이트 스태킹**

여러 트레이트가 같은 메서드를 오버라이드하면 선형화(linearization) 순서에 따라 호출됩니다. `super.process`는 선형화 순서상 다음 트레이트의 메서드를 호출합니다. 이를 스택커블 수정(stackable modification) 패턴이라고 합니다.

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

{{< callout type="info" title="핵심 포인트" >}}
- 트레이트는 구현을 포함할 수 있는 인터페이스입니다
- `with` 키워드로 여러 트레이트를 믹스인할 수 있습니다
- 트레이트 스태킹으로 동작을 조합할 수 있습니다 (선형화 순서)
{{< /callout >}}

#### 추상 클래스

추상 클래스는 인스턴스화할 수 없으며 하위 클래스에서 구현해야 하는 추상 멤버를 정의할 수 있습니다. 트레이트와 달리 생성자 매개변수를 가질 수 있습니다.

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

**추상 클래스 vs 트레이트**

아래 표는 추상 클래스와 트레이트의 주요 차이점을 정리한 것입니다. 일반적으로 트레이트를 선호하지만, 생성자 매개변수가 필요하거나 Java 호환성이 중요한 경우 추상 클래스를 사용합니다.

| 특성 | 추상 클래스 | 트레이트 |
|------|------------|---------|
| 생성자 매개변수 | 가능 | Scala 3에서만 가능 |
| 다중 상속 | 불가 | 가능 (믹스인) |
| Java 호환성 | 좋음 | 제한적 |

> **권장:** 특별한 이유가 없으면 트레이트를 사용하세요.

{{< callout type="info" title="핵심 포인트" >}}
- 추상 클래스는 생성자 매개변수를 가질 수 있습니다
- 트레이트는 다중 상속이 가능하지만 추상 클래스는 단일 상속입니다
- 일반적으로 트레이트를 선호하고, 생성자가 필요하면 추상 클래스를 사용합니다
{{< /callout >}}

#### 접근 제어자

Scala의 접근 제어자는 Java보다 세밀합니다. 기본값은 public이며, private와 protected를 범위 지정자와 함께 사용하여 접근 범위를 정밀하게 제어할 수 있습니다.

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

{{< callout type="info" title="핵심 포인트" >}}
- 기본값은 public이며, `private`와 `protected`를 사용합니다
- `private[this]`는 같은 인스턴스에서만 접근 가능합니다
- `private[패키지명]`으로 패키지 레벨 접근을 제어합니다
{{< /callout >}}

#### Enum (Scala 3)

Scala 3에서는 `enum` 키워드로 열거형을 정의할 수 있습니다. 단순한 열거형부터 매개변수가 있는 열거형, ADT(Algebraic Data Type) 스타일까지 다양한 형태를 지원합니다. Scala 2에서는 sealed trait와 case object 조합으로 열거형을 구현했습니다.

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

{{< callout type="info" title="핵심 포인트" >}}
- Scala 3: `enum`으로 열거형과 ADT를 간결하게 정의합니다
- Scala 2: `sealed trait` + `case object/class` 조합을 사용합니다
- 매개변수가 있는 열거형과 ADT 스타일 모두 지원합니다
{{< /callout >}}

#### 연습 문제

다음 연습 문제들을 통해 클래스와 객체 개념을 복습해보세요.

**1. 은행 계좌 클래스**

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

**2. 트레이트 믹스인**

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

#### 다음 단계

- [케이스 클래스](case-classes/) — 불변 데이터 모델링
- [패턴 매칭](pattern-matching/) — match 표현식 심화
