---
lastmod: "2026-01-15"
title: 고차 함수
weight: 8
---

## 전체 비유: 식품 가공 공장

고차 함수를 **식품 가공 공장의 생산 라인**에 비유하면 이해하기 쉽습니다:

| 공장 비유 | Scala 개념 | 역할 |
|----------|-----------|------|
| 재료 선별기 | `filter` | 조건에 맞는 요소만 통과 |
| 가공 기계 | `map` | 각 요소를 변환 |
| 포장 → 개봉 기계 | `flatMap` | 변환 후 평탄화 |
| 최종 계량기 | `fold`/`reduce` | 모든 요소를 하나로 축약 |
| 분류 컨베이어 | `partition`/`groupBy` | 조건에 따라 분류 |
| 기계 연결 | 함수 합성 (`andThen`) | 여러 변환을 연결 |
| 기계 설정 | 커링 | 일부 파라미터 미리 고정 |
| 기계 조작법 전달 | 함수를 인자로 | "어떻게" 처리할지 지정 |

공장에서 **기계(함수)를 조립하여 생산 라인(파이프라인)을 구성**하듯이, 고차 함수는 작은 함수들을 조합하여 복잡한 데이터 처리를 수행합니다. 핵심은 **"무엇을" 처리할지와 "어떻게" 처리할지를 분리**하는 것입니다.

{{< callout type="info" title="TL;DR" >}}
- **고차 함수**는 함수를 인자로 받거나 함수를 반환하는 함수입니다
- "무엇을" 할지와 "어떻게" 할지를 분리하여 코드 재사용성을 높입니다
- map, filter, fold 등으로 컬렉션을 선언적으로 처리합니다
- 커링, 클로저, 부분 함수로 고급 추상화를 구현합니다
{{< /callout >}}

**대상 독자:** 함수형 프로그래밍에 관심 있는 개발자
**선수 지식:** Scala 함수와 메서드, 컬렉션 기초

고차 함수(Higher-Order Function)는 함수를 인자로 받거나, 함수를 반환하는 함수입니다. 함수형 프로그래밍의 핵심 개념으로, 코드 재사용성을 높이고 추상화 수준을 끌어올립니다.

#### 왜 고차 함수가 강력한가?

고차 함수는 "무엇을" 할지와 "어떻게" 할지를 분리합니다. 반복되는 패턴을 함수로 추상화하고, 달라지는 부분만 인자로 전달합니다.

**문제: 반복되는 패턴**

Java 스타일의 명령형 코드에서는 필터링, 변환, 축약 같은 비슷한 패턴이 반복됩니다.

```java
// Java: 주문 목록에서 총액 계산
List<Order> orders = getOrders();
double total = 0;
for (Order order : orders) {
    if (order.getStatus().equals("COMPLETED")) {  // 필터링
        double price = order.getPrice() * 1.1;     // 변환 (세금 추가)
        total += price;                             // 축약
    }
}

// 같은 패턴이 다른 곳에서도 반복...
List<String> names = new ArrayList<>();
for (User user : users) {
    if (user.isActive()) {                        // 필터링
        names.add(user.getName().toUpperCase());  // 변환
    }
}
```

**문제점:**
- "무엇을 하는지"보다 "어떻게 하는지"에 집중
- 가변 상태(`total`, `names`)로 인한 버그 가능성
- 재사용이 어려움

**고차 함수의 해결책**

Scala에서는 "무엇을"만 선언합니다:

```scala
// Scala: 선언적 스타일
val total = orders
  .filter(_.status == "COMPLETED")  // 무엇을: 완료된 주문만
  .map(_.price * 1.1)               // 무엇을: 세금 추가
  .sum                              // 무엇을: 합계

val names = users
  .filter(_.isActive)               // 무엇을: 활성 사용자만
  .map(_.name.toUpperCase)          // 무엇을: 이름을 대문자로
```

**장점:**
- 의도가 명확하게 드러남
- 불변성 유지 (가변 상태 없음)
- 각 연산을 독립적으로 테스트 가능
- 체이닝으로 복잡한 변환도 읽기 쉬움

**Java Stream API와 비교**

Java 8+의 Stream API도 비슷한 기능을 제공하지만, Scala가 더 간결합니다. 아래 표는 두 접근법의 차이를 정리한 것입니다.

```java
// Java Stream
double total = orders.stream()
    .filter(o -> o.getStatus().equals("COMPLETED"))
    .mapToDouble(o -> o.getPrice() * 1.1)
    .sum();
```

```scala
// Scala
val total = orders
  .filter(_.status == "COMPLETED")
  .map(_.price * 1.1)
  .sum
```

| 비교 항목 | Java Stream | Scala Collection |
|----------|-------------|------------------|
| 기본 동작 | 지연 평가 | 즉시 평가 (view로 지연 가능) |
| 재사용 | 한 번만 사용 가능 | 무제한 재사용 |
| 문법 | `.stream()`, `.collect()` 필요 | 바로 사용 |
| 타입 힌트 | 자주 필요 | 대부분 추론 |
| 기본형 처리 | `mapToInt`, `mapToDouble` 필요 | 자동 변환 |

#### 실무 예제: 주문 처리 파이프라인

실제 비즈니스 로직에서 고차 함수를 어떻게 활용하는지 살펴봅니다.

**요구사항**

온라인 쇼핑몰에서 다음 처리가 필요합니다:
1. 유효한 주문만 필터링
2. 회원 등급에 따른 할인 적용
3. 상품별 총액 계산
4. 배송비 추가
5. 최종 결제 금액 산출

**도메인 모델**

먼저 주문 처리에 필요한 도메인 모델을 정의합니다.

```scala
case class Order(
  id: String,
  customerId: String,
  items: List[OrderItem],
  status: OrderStatus
)

case class OrderItem(
  productId: String,
  name: String,
  price: Double,
  quantity: Int
)

enum OrderStatus:
  case Pending, Confirmed, Shipped, Cancelled

case class Customer(
  id: String,
  name: String,
  tier: CustomerTier
)

enum CustomerTier:
  case Bronze, Silver, Gold, Platinum
```

**고차 함수로 파이프라인 구현**

고차 함수를 활용하여 각 단계를 독립적인 함수로 정의하고 조합합니다. 함수를 반환하는 applyDiscount가 핵심입니다.

```scala
object OrderProcessor:
  // 할인율 매핑
  val discountRates: Map[CustomerTier, Double] = Map(
    CustomerTier.Bronze   -> 0.0,
    CustomerTier.Silver   -> 0.05,
    CustomerTier.Gold     -> 0.10,
    CustomerTier.Platinum -> 0.15
  )

  // 주문 유효성 검사 - 고차 함수로 유연하게
  def isValidOrder(order: Order): Boolean =
    order.status != OrderStatus.Cancelled &&
    order.items.nonEmpty

  // 주문 아이템 총액 계산
  def calculateItemTotal(item: OrderItem): Double =
    item.price * item.quantity

  // 주문 총액 계산
  def calculateOrderTotal(order: Order): Double =
    order.items.map(calculateItemTotal).sum

  // 할인 적용 함수 생성 (함수를 반환하는 고차 함수)
  def applyDiscount(tier: CustomerTier): Double => Double = {
    val rate = discountRates.getOrElse(tier, 0.0)
    total => total * (1 - rate)
  }

  // 배송비 계산
  def calculateShipping(total: Double): Double =
    if (total >= 50000) 0
    else if (total >= 30000) 2500
    else 3500

  // 전체 파이프라인
  def processOrders(
    orders: List[Order],
    getCustomer: String => Option[Customer]
  ): List[(Order, Double)] = {
    orders
      .filter(isValidOrder)                          // 1. 유효한 주문만
      .flatMap { order =>                            // 2. 고객 정보 결합
        getCustomer(order.customerId).map(c => (order, c))
      }
      .map { case (order, customer) =>               // 3. 가격 계산
        val subtotal = calculateOrderTotal(order)
        val discounted = applyDiscount(customer.tier)(subtotal)
        val shipping = calculateShipping(discounted)
        val finalTotal = discounted + shipping
        (order, finalTotal)
      }
  }
```

**사용 예시**

```scala
// 테스트 데이터
val orders = List(
  Order("O001", "C001", List(
    OrderItem("P1", "노트북", 1200000, 1),
    OrderItem("P2", "마우스", 50000, 2)
  ), OrderStatus.Confirmed),
  Order("O002", "C002", List(
    OrderItem("P3", "키보드", 80000, 1)
  ), OrderStatus.Cancelled),  // 제외됨
  Order("O003", "C003", List(
    OrderItem("P4", "모니터", 350000, 2)
  ), OrderStatus.Confirmed)
)

val customers = Map(
  "C001" -> Customer("C001", "김철수", CustomerTier.Gold),
  "C003" -> Customer("C003", "이영희", CustomerTier.Silver)
)

val results = OrderProcessor.processOrders(
  orders,
  id => customers.get(id)
)

results.foreach { case (order, total) =>
  println(s"주문 ${order.id}: ${total}원")
}
// 주문 O001: 1170000.0원 (10% 할인 + 무료배송)
// 주문 O003: 665000.0원 (5% 할인 + 무료배송)
```

**확장: 비동기 처리**

같은 패턴을 Future와 조합하여 비동기 파이프라인으로 확장할 수 있습니다.

```scala
import scala.concurrent.{Future, ExecutionContext}

def processOrdersAsync(
  orders: List[Order],
  getCustomer: String => Future[Option[Customer]]
)(using ec: ExecutionContext): Future[List[(Order, Double)]] = {
  Future.sequence {
    orders
      .filter(isValidOrder)
      .map { order =>
        getCustomer(order.customerId).map { maybeCustomer =>
          maybeCustomer.map { customer =>
            val subtotal = calculateOrderTotal(order)
            val discounted = applyDiscount(customer.tier)(subtotal)
            val shipping = calculateShipping(discounted)
            (order, discounted + shipping)
          }
        }
      }
  }.map(_.flatten)
}
```

#### 고차 함수 선택 가이드

상황에 따라 적합한 고차 함수를 선택해야 합니다.

**언제 무엇을 사용할까?**

아래 표는 작업 유형에 따라 어떤 함수를 사용해야 하는지 정리한 것입니다.

| 작업 | 함수 | 예시 | 결과 |
|------|------|------|------|
| 1:1 변환 | `map` | 가격에 세금 추가 | `List[A]` → `List[B]` |
| 조건 필터링 | `filter` | 유효한 주문만 | `List[A]` → `List[A]` (더 적음) |
| 1:N 변환 + 평탄화 | `flatMap` | 주문 → 개별 상품들 | `List[A]` → `List[B]` (펼쳐짐) |
| 단일 값으로 축약 | `fold`/`reduce` | 총액 계산 | `List[A]` → `B` |
| 부수효과 실행 | `foreach` | 로깅, DB 저장 | `Unit` |
| 조건으로 분리 | `partition` | 성공/실패 분류 | `(List[A], List[A])` |
| 키로 그룹화 | `groupBy` | 카테고리별 상품 | `Map[K, List[A]]` |
| 패턴 매칭 변환 | `collect` | 특정 타입만 추출 | `List[B]` (매칭된 것만) |

**성능 고려사항**

큰 컬렉션을 처리할 때는 중간 컬렉션 생성을 피하기 위해 view나 iterator를 사용합니다.

```scala
// ❌ 큰 컬렉션에서 중간 컬렉션 생성
val result = (1 to 1000000)
  .map(_ * 2)      // 100만 개 리스트 생성
  .filter(_ > 100) // 또 다른 리스트 생성
  .take(10)        // 10개만 필요했는데...

// ✅ view로 지연 평가 - 필요한 만큼만 계산
val result = (1 to 1000000)
  .view
  .map(_ * 2)
  .filter(_ > 100)
  .take(10)
  .toList

// ✅ Iterator도 지연 평가
val result = (1 to 1000000)
  .iterator
  .map(_ * 2)
  .filter(_ > 100)
  .take(10)
  .toList
```

**주의사항: 과도한 체이닝**

너무 긴 체이닝은 디버깅이 어렵습니다. 의미 있는 단위로 분리하고 이름을 부여하세요.

```scala
// ❌ 너무 긴 체이닝은 디버깅이 어려움
val result = data
  .filter(_.isValid)
  .map(_.transform)
  .flatMap(_.split)
  .groupBy(_.category)
  .map { case (k, v) => k -> v.map(_.process) }
  .filter { case (_, v) => v.nonEmpty }
  .toMap

// ✅ 의미 있는 단위로 분리하고 이름 부여
val validData = data.filter(_.isValid)
val transformed = validData.map(_.transform).flatMap(_.split)
val grouped = transformed.groupBy(_.category)
val processed = grouped.map { case (k, v) =>
  k -> v.map(_.process)
}.filter { case (_, v) => v.nonEmpty }
```

#### 고차 함수란?

고차 함수의 기본 개념을 예제로 살펴봅니다.

```scala
// 함수를 인자로 받는 함수
def applyTwice(f: Int => Int, x: Int): Int = f(f(x))

val double = (x: Int) => x * 2
applyTwice(double, 3)  // 12 (3 -> 6 -> 12)

// 함수를 반환하는 함수
def multiplier(factor: Int): Int => Int = {
  (x: Int) => x * factor
}

val triple = multiplier(3)
triple(4)  // 12
```

#### 주요 고차 함수

Scala 컬렉션에서 가장 많이 사용되는 고차 함수들을 상세히 살펴봅니다.

**map**

map은 각 요소를 변환합니다. 원본 컬렉션과 같은 크기의 새 컬렉션을 반환합니다.

```scala
val numbers = List(1, 2, 3, 4, 5)

// 각 요소를 2배
numbers.map(x => x * 2)     // List(2, 4, 6, 8, 10)
numbers.map(_ * 2)          // 축약형

// 타입 변환
numbers.map(_.toString)     // List("1", "2", "3", "4", "5")

// 복잡한 변환
case class Person(name: String, age: Int)
val ages = List(25, 30, 35)
ages.map(age => Person(s"Person$age", age))
```

**filter**

filter는 조건에 맞는 요소만 선택합니다. 원본보다 작거나 같은 크기의 컬렉션을 반환합니다.

```scala
val numbers = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

numbers.filter(_ % 2 == 0)     // List(2, 4, 6, 8, 10)
numbers.filter(_ > 5)          // List(6, 7, 8, 9, 10)
numbers.filterNot(_ % 2 == 0)  // List(1, 3, 5, 7, 9)

// 체이닝
numbers
  .filter(_ % 2 == 0)
  .filter(_ > 4)
// List(6, 8, 10)
```

**flatMap**

flatMap은 각 요소를 컬렉션으로 변환한 후 하나로 평탄화합니다. 1:N 변환이나 Option을 다룰 때 필수적입니다.

```scala
val numbers = List(1, 2, 3)

// map + flatten
numbers.map(n => List(n, n * 10))
// List(List(1, 10), List(2, 20), List(3, 30))

numbers.flatMap(n => List(n, n * 10))
// List(1, 10, 2, 20, 3, 30)

// Option과 함께
def parse(s: String): Option[Int] = s.toIntOption

val strings = List("1", "two", "3")
strings.flatMap(parse)  // List(1, 3)
```

**fold / foldLeft / foldRight**

fold 계열 함수는 초기값과 이항 연산을 사용해 컬렉션을 단일 값으로 축소합니다. 합계, 곱, 문자열 연결 등 다양한 집계에 사용됩니다.

```scala
val numbers = List(1, 2, 3, 4, 5)

// foldLeft: 왼쪽부터 축소
numbers.foldLeft(0)(_ + _)    // 15
numbers.foldLeft(1)(_ * _)    // 120

// 과정 시각화: ((((0 + 1) + 2) + 3) + 4) + 5

// foldRight: 오른쪽부터 축소
numbers.foldRight(0)(_ + _)   // 15
// 과정: 1 + (2 + (3 + (4 + (5 + 0))))

// 문자열 연결
List("a", "b", "c").foldLeft("")(_ + _)  // "abc"

// 복잡한 축소
case class Stats(sum: Int, count: Int)
numbers.foldLeft(Stats(0, 0)) { (stats, n) =>
  Stats(stats.sum + n, stats.count + 1)
}
// Stats(15, 5)
```

**reduce**

reduce는 초기값 없이 축소합니다. 빈 컬렉션에서 에러가 발생하므로 주의해야 합니다.

```scala
val numbers = List(1, 2, 3, 4, 5)

numbers.reduce(_ + _)    // 15
numbers.reduce(_ * _)    // 120
numbers.reduce(_ max _)  // 5
numbers.reduce(_ min _)  // 1

// reduceOption: 빈 컬렉션에서 None 반환
List.empty[Int].reduceOption(_ + _)  // None
```

**collect**

collect는 패턴 매칭으로 필터링과 변환을 동시에 수행합니다. PartialFunction을 인자로 받습니다.

```scala
val mixed: List[Any] = List(1, "hello", 2, "world", 3)

// 정수만 추출하고 2배
mixed.collect {
  case i: Int => i * 2
}
// List(2, 4, 6)

// Option에서 값 추출
val maybes = List(Some(1), None, Some(3), None, Some(5))
maybes.collect {
  case Some(n) => n
}
// List(1, 3, 5)
```

**partition**

partition은 조건을 만족하는 요소와 그렇지 않은 요소를 두 컬렉션으로 분리합니다.

```scala
val numbers = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

val (evens, odds) = numbers.partition(_ % 2 == 0)
// evens = List(2, 4, 6, 8, 10)
// odds = List(1, 3, 5, 7, 9)
```

**groupBy**

groupBy는 키 함수의 결과에 따라 요소들을 그룹화하여 Map을 생성합니다.

```scala
val words = List("apple", "banana", "avocado", "cherry", "apricot")

val byFirstLetter = words.groupBy(_.head)
// Map(
//   'a' -> List("apple", "avocado", "apricot"),
//   'b' -> List("banana"),
//   'c' -> List("cherry")
// )

case class Person(name: String, city: String)
val people = List(
  Person("Alice", "서울"),
  Person("Bob", "부산"),
  Person("Carol", "서울")
)

val byCity = people.groupBy(_.city)
// Map("서울" -> List(Alice, Carol), "부산" -> List(Bob))
```

#### 함수 합성

작은 함수들을 조합하여 더 큰 함수를 만들 수 있습니다.

**andThen과 compose**

andThen은 왼쪽에서 오른쪽으로, compose는 오른쪽에서 왼쪽으로 함수를 합성합니다.

```scala
val addOne = (x: Int) => x + 1
val double = (x: Int) => x * 2

// andThen: 왼쪽 -> 오른쪽
val addThenDouble = addOne andThen double
addThenDouble(3)  // (3 + 1) * 2 = 8

// compose: 오른쪽 -> 왼쪽
val doubleThenAdd = addOne compose double
doubleThenAdd(3)  // (3 * 2) + 1 = 7
```

**체이닝**

컬렉션 메서드들을 연속으로 호출하여 데이터 파이프라인을 구성합니다.

```scala
val numbers = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

val result = numbers
  .filter(_ % 2 == 0)     // 짝수만
  .map(_ * 2)             // 2배
  .filter(_ > 10)         // 10 초과만
  .sum                    // 합계

// result = 12 + 16 + 20 = 48
```

#### 커링 (Currying)

커링은 여러 인자를 받는 함수를 단일 인자 함수의 체인으로 변환합니다. 부분 적용과 타입 추론에 유용합니다.

```scala
// 일반 함수
def add(a: Int, b: Int): Int = a + b
add(1, 2)  // 3

// 커링된 함수
def addCurried(a: Int)(b: Int): Int = a + b
addCurried(1)(2)  // 3

// 부분 적용
val add5 = addCurried(5)
add5(3)  // 8

// 기존 함수를 커링
val addCurried2 = (add _).curried
val add10 = addCurried2(10)
add10(5)  // 15
```

**커링의 활용**

커링은 타입 추론 개선과 DSL 구축에 특히 유용합니다.

```scala
// 타입 추론 개선
def transform[A, B](list: List[A])(f: A => B): List[B] =
  list.map(f)

// 첫 번째 인자에서 A 타입이 추론되어 f의 타입 명시 불필요
transform(List(1, 2, 3))(x => x * 2)

// DSL 스타일 (의사 코드)
// Database와 Connection은 가상의 타입입니다
trait Connection:
  def execute(sql: String): Unit
  def close(): Unit

trait Database:
  def connect(): Connection

def withTransaction[T](db: Database)(block: Connection => T): T =
  val conn = db.connect()
  try block(conn)
  finally conn.close()

// 사용 예시
// withTransaction(myDatabase) { conn =>
//   conn.execute("INSERT ...")
// }
```

#### 클로저 (Closure)

클로저는 함수가 정의된 환경의 변수를 캡처하는 것입니다. 함수가 자신이 정의된 스코프 밖에서 실행되어도 해당 변수에 접근할 수 있습니다.

```scala
def makeCounter(): () => Int = {
  var count = 0
  () => {
    count += 1
    count
  }
}

val counter = makeCounter()
counter()  // 1
counter()  // 2
counter()  // 3

val anotherCounter = makeCounter()
anotherCounter()  // 1 (독립적인 count)
```

#### 부분 함수 (Partial Function)

PartialFunction은 일부 입력에 대해서만 정의된 함수입니다. isDefinedAt으로 정의 여부를 확인할 수 있으며, collect와 함께 자주 사용됩니다.

```scala
val divide: PartialFunction[(Int, Int), Int] = {
  case (a, b) if b != 0 => a / b
}

divide.isDefinedAt((10, 2))   // true
divide.isDefinedAt((10, 0))   // false

divide((10, 2))  // 5
// divide((10, 0))  // MatchError

// collect와 함께
val pairs = List((10, 2), (20, 0), (30, 3))
pairs.collect(divide)  // List(5, 10)

// orElse로 결합
val safeDivide = divide orElse {
  case (a, 0) => 0
}
safeDivide((10, 0))  // 0
```

#### 흔한 실수와 Anti-patterns

고차 함수를 사용할 때 흔히 발생하는 실수와 올바른 해결 방법을 정리했습니다.

**❌ 피해야 할 것**

```scala
// 1. 불필요한 람다 래핑
list.map(x => f(x))  // 비효율적
list.map(x => x.toString)  // 비효율적

// 2. foldLeft 대신 var + foreach
var sum = 0
list.foreach(sum += _)  // 가변 상태!

// 3. map + flatten 대신 flatMap
list.map(f).flatten  // 중간 컬렉션 생성

// 4. 복잡한 플레이스홀더 남용
list.map(_ + _ * _)  // 읽기 어려움!

// 5. 부수 효과가 있는 map
list.map { x =>
  println(x)  // 부수 효과!
  x * 2
}
```

**✅ 올바른 방법**

```scala
// 1. 메서드 참조 사용 (eta expansion)
list.map(f)
list.map(_.toString)

// 2. foldLeft 사용
list.foldLeft(0)(_ + _)

// 3. flatMap 사용
list.flatMap(f)

// 4. 명시적 람다 사용
list.reduce((a, b) => a + b * c)

// 5. 변환과 부수 효과 분리
val doubled = list.map(_ * 2)
doubled.foreach(println)
// 또는 tap 사용 (Scala 2.13+)
list.map(_ * 2).tapEach(println)
```

**성능 팁**

큰 컬렉션 처리 시 view를 사용하면 지연 평가로 중간 컬렉션 생성을 피할 수 있습니다.

```scala
// 체이닝 vs View
// 각 연산마다 새 컬렉션 생성
list.map(_ * 2).filter(_ > 10).take(5)

// View로 지연 평가 (중간 컬렉션 없음)
list.view.map(_ * 2).filter(_ > 10).take(5).toList

// 큰 컬렉션에서 특히 효과적
(1 to 1000000)
  .view
  .map(_ * 2)
  .filter(_ % 3 == 0)
  .take(10)
  .toList
```

#### 연습 문제

다음 연습 문제들을 통해 고차 함수 개념을 복습해보세요.

**1. 나만의 map 구현 ⭐⭐**

`myMap` 함수를 foldRight로 구현하세요.

<details>
<summary>정답 보기</summary>

```scala
def myMap[A, B](list: List[A])(f: A => B): List[B] =
  list.foldRight(List.empty[B]) { (elem, acc) =>
    f(elem) :: acc
  }

myMap(List(1, 2, 3))(_ * 2)  // List(2, 4, 6)
```

</details>

**2. 파이프라인 함수 ⭐⭐**

여러 함수를 순차적으로 적용하는 `pipe` 함수를 구현하세요.

<details>
<summary>정답 보기</summary>

```scala
def pipe[A](value: A)(functions: (A => A)*): A =
  functions.foldLeft(value)((v, f) => f(v))

pipe(5)(
  _ + 1,   // 6
  _ * 2,   // 12
  _ - 3    // 9
)  // 9
```

</details>

**3. 메모이제이션 ⭐⭐⭐**

결과를 캐싱하는 고차 함수를 구현하세요.

<details>
<summary>정답 보기</summary>

```scala
def memoize[A, B](f: A => B): A => B = {
  val cache = scala.collection.mutable.Map.empty[A, B]
  (a: A) => cache.getOrElseUpdate(a, f(a))
}

def slowFib(n: Int): BigInt =
  if (n <= 1) n else slowFib(n - 1) + slowFib(n - 2)

lazy val fastFib: Int => BigInt = memoize { n =>
  if (n <= 1) n else fastFib(n - 1) + fastFib(n - 2)
}

fastFib(100)  // 빠르게 계산됨
```

</details>

#### 관련 개념

고차 함수는 다음 개념들과 밀접하게 연결됩니다:

| 관련 개념 | 연결 관계 |
|----------|----------|
| [함수와 메서드]({{< relref "/docs/scala/concepts/functions-methods" >}}) | 일급 함수와 함수 리터럴 기초 |
| [컬렉션]({{< relref "/docs/scala/concepts/collections" >}}) | `map`, `filter` 등 고차 함수의 주요 사용처 |
| [for 표현식]({{< relref "/docs/scala/concepts/for-comprehensions" >}}) | `flatMap` 체인의 문법적 설탕 |
| [함수형 패턴]({{< relref "/docs/scala/concepts/functional-patterns" >}}) | Functor(`map`), Monad(`flatMap`) 추상화 |
| [패턴 매칭]({{< relref "/docs/scala/concepts/pattern-matching" >}}) | `collect`에서 패턴 기반 변환 |
| [동시성]({{< relref "/docs/scala/concepts/concurrency" >}}) | Future의 `map`, `flatMap` 조합 |

#### 다음 단계

| 학습 경로 | 설명 |
|----------|------|
| [for 표현식]({{< relref "/docs/scala/concepts/for-comprehensions" >}}) | `flatMap` 체인을 읽기 쉽게 표현 |
| [함수형 패턴]({{< relref "/docs/scala/concepts/functional-patterns" >}}) | Functor, Monad 추상화 심화 |
| [Implicits]({{< relref "/docs/scala/concepts/implicits" >}}) | 문맥적 추상화와 타입 클래스 |
