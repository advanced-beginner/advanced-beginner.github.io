---
lastmod: "2026-01-10"
title: 기본 예제
weight: 2
---

{{% notice style="primary" title="TL;DR" %}}
- **케이스 클래스**로 불변 데이터 모델링, 자동 생성되는 `equals`, `copy` 활용
- **Option**으로 null 안전성, **Either**로 상세한 에러 처리
- **패턴 매칭**으로 타입에 따른 분기 처리
- **컬렉션 API** (`map`, `filter`, `groupBy`)로 선언적 데이터 처리
- **타입 클래스**로 확장 가능한 기능 구현
{{% /notice %}}

**대상 독자**: Scala 환경 설정을 마친 개발자, 실제 코드로 Scala를 학습하려는 분

**선수 지식**:
- Scala 개발 환경 설정 완료 ([환경 설정](../setup/) 참조)
- 객체지향 프로그래밍 기본 개념
- 함수형 프로그래밍 기초 (권장)

---

Scala의 핵심 개념을 활용한 종합 예제입니다. 이 문서에서는 케이스 클래스, 패턴 매칭, 컬렉션 처리, Option, Either, 타입 클래스 등 실무에서 자주 사용하는 기능들을 예제 코드와 함께 설명합니다.

> 💻 **온라인 실행:** 아래 모든 예제는 [Scastie](https://scastie.scala-lang.org/)에서 복사하여 바로 실행할 수 있습니다.
> Scala 3를 선택하고 코드를 붙여넣으세요!

#### 예제 1: 데이터 모델링

케이스 클래스로 도메인 모델을 정의합니다. 케이스 클래스는 불변 데이터를 표현하는 데 적합하며, equals, hashCode, toString, copy 메서드가 자동으로 생성됩니다. 전자상거래 도메인에서 자주 볼 수 있는 Product, OrderLine, Order를 모델링해보겠습니다.

```scala
// 도메인 모델
case class Product(id: Int, name: String, price: Double)
case class OrderLine(product: Product, quantity: Int)
case class Order(id: Int, customer: String, lines: List[OrderLine])

// 팩토리 메서드
object Order:
  def create(id: Int, customer: String, lines: List[OrderLine]): Option[Order] =
    if lines.isEmpty then None
    else Some(Order(id, customer, lines))

// 사용
val laptop = Product(1, "노트북", 1500000)
val mouse = Product(2, "마우스", 50000)

val order = Order.create(
  1,
  "김철수",
  List(
    OrderLine(laptop, 1),
    OrderLine(mouse, 2)
  )
)
```

위 코드에서 Order.create 팩토리 메서드는 주문 라인이 비어있으면 None을 반환하고, 그렇지 않으면 Some(Order(...))를 반환합니다. 이렇게 하면 빈 주문이 생성되는 것을 컴파일 타임에 방지할 수 있습니다.

{{% notice style="tip" title="핵심 포인트" %}}
- **케이스 클래스**: 불변 데이터 표현에 적합, `equals`, `hashCode`, `toString`, `copy` 자동 생성
- **팩토리 메서드**: 객체 생성 시 유효성 검사를 강제하여 잘못된 상태 방지
- **Option 반환**: null 대신 Option으로 "값이 없을 수 있음"을 타입으로 표현
{{% /notice %}}

#### 예제 2: 주문 처리

패턴 매칭과 고차 함수로 비즈니스 로직을 구현합니다. Scala 3의 extension 키워드를 사용하면 기존 클래스에 새로운 메서드를 추가할 수 있습니다. 이를 통해 Order 클래스를 수정하지 않고도 새로운 기능을 추가합니다.

```scala
// 주문 확장 메서드
extension (order: Order)
  def totalPrice: Double =
    order.lines.map(line => line.product.price * line.quantity).sum

  def itemCount: Int =
    order.lines.map(_.quantity).sum

  def hasProduct(productId: Int): Boolean =
    order.lines.exists(_.product.id == productId)

  def applyDiscount(rate: Double): Order =
    order.copy(
      lines = order.lines.map { line =>
        line.copy(
          product = line.product.copy(
            price = line.product.price * (1 - rate)
          )
        )
      }
    )

// 사용
order.foreach { o =>
  println(s"총 금액: ${o.totalPrice}원")        // 1,600,000원
  println(s"상품 수: ${o.itemCount}개")         // 3개
  println(s"노트북 포함: ${o.hasProduct(1)}")   // true

  val discounted = o.applyDiscount(0.1)
  println(s"할인 후: ${discounted.totalPrice}원")  // 1,440,000원
}
```

totalPrice 메서드는 각 주문 라인의 가격과 수량을 곱한 값들의 합계를 계산합니다. applyDiscount 메서드는 케이스 클래스의 copy 메서드를 중첩 사용하여 불변성을 유지하면서 할인을 적용합니다.

{{% notice style="tip" title="핵심 포인트" %}}
- **extension**: 기존 클래스를 수정하지 않고 새 메서드 추가 (Scala 3)
- **copy**: 불변 객체의 일부 필드만 변경한 새 객체 생성
- **고차 함수**: `map`, `sum`, `exists` 등으로 컬렉션을 선언적으로 처리
{{% /notice %}}

#### 예제 3: 에러 처리

Either를 사용한 안전한 에러 처리입니다. Either는 성공(Right)과 실패(Left)를 표현하며, 실패 시 에러 정보를 담을 수 있어 Option보다 더 상세한 에러 처리가 가능합니다. 검증 로직에서 어떤 이유로 실패했는지 알려주는 데 유용합니다.

```scala
// 에러 타입
enum ValidationError:
  case EmptyName
  case InvalidPrice(price: Double)
  case InvalidQuantity(qty: Int)

import ValidationError.*

// 검증 함수
def validateProduct(name: String, price: Double): Either[ValidationError, Product] =
  if name.isEmpty then Left(EmptyName)
  else if price <= 0 then Left(InvalidPrice(price))
  else Right(Product(0, name, price))

def validateOrderLine(
  product: Product,
  quantity: Int
): Either[ValidationError, OrderLine] =
  if quantity <= 0 then Left(InvalidQuantity(quantity))
  else Right(OrderLine(product, quantity))

// 조합
def createOrderLine(
  name: String,
  price: Double,
  quantity: Int
): Either[ValidationError, OrderLine] =
  for
    product <- validateProduct(name, price)
    line <- validateOrderLine(product, quantity)
  yield line

// 사용
createOrderLine("노트북", 1500000, 1) match
  case Right(line) => println(s"주문 라인: $line")
  case Left(EmptyName) => println("상품명이 비어있습니다")
  case Left(InvalidPrice(p)) => println(s"잘못된 가격: $p")
  case Left(InvalidQuantity(q)) => println(s"잘못된 수량: $q")
```

for comprehension을 사용하면 Either 값들을 순차적으로 연결할 수 있습니다. 첫 번째 검증이 실패하면 즉시 Left를 반환하고, 모든 검증이 통과하면 최종 결과를 Right로 감싸서 반환합니다.

{{% notice style="tip" title="핵심 포인트" %}}
- **Either[L, R]**: `Left`는 실패, `Right`는 성공 (관례)
- **enum**: 에러 타입을 열거형으로 정의하여 타입 안전성 확보
- **for comprehension**: 여러 Either를 순차적으로 연결, 첫 실패에서 중단
- **패턴 매칭**: 모든 에러 케이스를 명시적으로 처리
{{% /notice %}}

#### 예제 4: 컬렉션 처리

함수형 스타일로 데이터를 처리합니다. Scala 컬렉션은 map, filter, groupBy, sortBy 등 풍부한 메서드를 제공합니다. 명령형 반복문 대신 이러한 선언적 메서드를 사용하면 코드가 간결하고 읽기 쉬워집니다.

```scala
// 샘플 데이터
val products = List(
  Product(1, "노트북", 1500000),
  Product(2, "마우스", 50000),
  Product(3, "키보드", 150000),
  Product(4, "모니터", 500000),
  Product(5, "스피커", 200000)
)

// 필터링
val expensive = products.filter(_.price >= 200000)
println(s"고가 상품: ${expensive.map(_.name)}")
// List(노트북, 모니터)

// 변환
val priceList = products.map(p => s"${p.name}: ${p.price}원")
println(priceList.mkString("\n"))

// 그룹화
val byPriceRange = products.groupBy { p =>
  if p.price < 100000 then "저가"
  else if p.price < 500000 then "중가"
  else "고가"
}
println(s"가격대별: $byPriceRange")

// 집계
val totalValue = products.map(_.price).sum
val avgPrice = products.map(_.price).sum / products.length
val maxPrice = products.maxBy(_.price)

println(s"총 가치: ${totalValue}원")
println(s"평균 가격: ${avgPrice}원")
println(s"최고가 상품: ${maxPrice.name}")

// 정렬
val sortedByPrice = products.sortBy(_.price)
val sortedByName = products.sortBy(_.name)
```

filter는 조건에 맞는 요소만 추출하고, map은 각 요소를 변환합니다. groupBy는 키 함수를 기준으로 요소들을 그룹화하여 Map을 반환합니다. sortBy는 주어진 함수의 결과를 기준으로 정렬합니다.

{{% notice style="tip" title="핵심 포인트" %}}
- **filter**: 조건에 맞는 요소만 추출
- **map**: 각 요소를 변환하여 새 컬렉션 생성
- **groupBy**: 키 함수 기준으로 그룹화 → `Map[K, List[V]]` 반환
- **sortBy**: 정렬 기준 지정, **maxBy/minBy**: 최대/최소 요소 추출
{{% /notice %}}

#### 예제 5: Option 활용

null 대신 Option을 사용합니다. Option은 값이 있을 수도 있고 없을 수도 있는 상황을 타입으로 표현합니다. NullPointerException을 컴파일 타임에 방지할 수 있어 더 안전한 코드를 작성할 수 있습니다.

```scala
// 저장소
object ProductRepository:
  private val products = Map(
    1 -> Product(1, "노트북", 1500000),
    2 -> Product(2, "마우스", 50000)
  )

  def findById(id: Int): Option[Product] = products.get(id)

  def findByName(name: String): Option[Product] =
    products.values.find(_.name.contains(name))

// 사용
ProductRepository.findById(1) match
  case Some(product) => println(s"찾음: $product")
  case None => println("상품 없음")

// 체이닝
val price = ProductRepository
  .findById(1)
  .map(_.price)
  .getOrElse(0.0)

// for comprehension
val orderTotal = for
  laptop <- ProductRepository.findById(1)
  mouse <- ProductRepository.findById(2)
yield laptop.price + mouse.price

println(s"주문 합계: ${orderTotal.getOrElse(0.0)}원")
```

Option에 map을 적용하면 값이 있는 경우에만 변환이 수행됩니다. for comprehension을 사용하면 여러 Option 값을 조합할 수 있으며, 중간에 None이 있으면 전체 결과가 None이 됩니다.

{{% notice style="tip" title="핵심 포인트" %}}
- **Option[T]**: `Some(값)` 또는 `None`으로 값의 존재 여부 표현
- **getOrElse**: None일 때 기본값 반환
- **map/flatMap**: 값이 있을 때만 변환 수행
- **for comprehension**: 여러 Option 조합, 하나라도 None이면 전체가 None
{{% /notice %}}

#### 예제 6: 타입 클래스

타입 클래스로 확장 가능한 기능을 구현합니다. 타입 클래스는 기존 타입을 수정하지 않고 새로운 기능을 추가하는 패턴입니다. JSON 인코더를 예로 들어 타입 클래스의 활용법을 보여줍니다.

```scala
// JSON 인코더 타입 클래스
trait JsonEncoder[A]:
  def encode(a: A): String

object JsonEncoder:
  given JsonEncoder[String] with
    def encode(s: String): String = s"\"$s\""

  given JsonEncoder[Int] with
    def encode(i: Int): String = i.toString

  given JsonEncoder[Double] with
    def encode(d: Double): String = d.toString

  given JsonEncoder[Product] with
    def encode(p: Product): String =
      s"""{"id":${p.id},"name":"${p.name}","price":${p.price}}"""

  given [A](using e: JsonEncoder[A]): JsonEncoder[List[A]] with
    def encode(list: List[A]): String =
      list.map(e.encode).mkString("[", ",", "]")

// 확장 메서드
extension [A](a: A)(using e: JsonEncoder[A])
  def toJson: String = e.encode(a)

// 사용
val laptop = Product(1, "노트북", 1500000)
println(laptop.toJson)
// {"id":1,"name":"노트북","price":1500000.0}

val products = List(
  Product(1, "노트북", 1500000),
  Product(2, "마우스", 50000)
)
println(products.toJson)
// [{"id":1,"name":"노트북","price":1500000.0},{"id":2,"name":"마우스","price":50000.0}]
```

given 키워드로 타입 클래스 인스턴스를 정의합니다. List에 대한 인코더는 요소 타입의 인코더가 있으면 자동으로 생성됩니다. extension을 통해 toJson 메서드를 모든 인코딩 가능한 타입에 추가합니다.

{{% notice style="tip" title="핵심 포인트" %}}
- **타입 클래스**: 기존 타입을 수정하지 않고 기능 추가하는 패턴
- **trait**: 타입 클래스 인터페이스 정의
- **given**: 타입 클래스 인스턴스 정의 (Scala 3)
- **extension + using**: 조건부 확장 메서드 (인코더가 있는 타입에만 `toJson` 추가)
{{% /notice %}}

#### 예제 프로젝트 실행

위 예제들을 로컬에서 실행하려면 예제 프로젝트 디렉토리에서 sbt run 명령을 실행합니다.

```bash
cd examples/scala/scala3-basics
sbt run
```

#### 예제 7: 실무 시나리오 - REST API 응답 처리

실제 API 응답을 처리하는 패턴입니다. HTTP 응답은 성공할 수도 있고 실패할 수도 있으므로, 이를 안전하게 처리하는 유틸리티를 만들어봅니다.

```scala
import scala.util.{Try, Success, Failure}

// API 응답 모델
case class ApiResponse[T](
  status: Int,
  data: Option[T],
  error: Option[String]
)

// 사용자 도메인
case class User(id: Long, name: String, email: String)

// API 클라이언트 시뮬레이션
object UserApiClient:
  def fetchUser(id: Long): ApiResponse[User] =
    if id > 0 then
      ApiResponse(200, Some(User(id, s"User$id", s"user$id@example.com")), None)
    else
      ApiResponse(404, None, Some("User not found"))

  def fetchUsers(ids: List[Long]): List[ApiResponse[User]] =
    ids.map(fetchUser)

// 응답 처리 유틸리티
object ApiResponseHandler:
  extension [T](response: ApiResponse[T])
    def toEither: Either[String, T] =
      response match
        case ApiResponse(status, Some(data), _) if status < 400 => Right(data)
        case ApiResponse(_, _, Some(error)) => Left(error)
        case _ => Left("Unknown error")

    def toOption: Option[T] = response.data.filter(_ => response.status < 400)

// 사용 예시
import ApiResponseHandler.*

val response = UserApiClient.fetchUser(1)
val userOrError = response.toEither

userOrError match
  case Right(user) => println(s"환영합니다, ${user.name}!")
  case Left(error) => println(s"오류: $error")

// 여러 사용자 처리
val userIds = List(1L, 2L, -1L, 3L)
val results = UserApiClient.fetchUsers(userIds)
  .map(_.toEither)
  .collect { case Right(user) => user }

println(s"성공적으로 조회된 사용자: ${results.length}명")
```

ApiResponse를 Either나 Option으로 변환하는 확장 메서드를 정의했습니다. collect 메서드는 부분 함수를 사용하여 Right인 경우만 추출합니다.

{{% notice style="tip" title="핵심 포인트" %}}
- **API 응답 래퍼**: 상태 코드, 데이터, 에러 메시지를 하나의 타입으로 표현
- **확장 메서드**: 응답을 `Either`나 `Option`으로 변환하여 일관된 에러 처리
- **collect**: 특정 패턴에 맞는 요소만 추출하여 새 컬렉션 생성
{{% /notice %}}

#### 예제 8: 실무 시나리오 - 설정 관리

환경별 설정을 타입 안전하게 관리하는 패턴입니다. enum으로 환경을 정의하고, 패턴 매칭을 통해 각 환경에 맞는 설정을 반환합니다.

```scala
// 설정 ADT
enum Environment:
  case Development, Staging, Production

case class DatabaseConfig(
  host: String,
  port: Int,
  database: String,
  maxConnections: Int
)

case class AppConfig(
  environment: Environment,
  database: DatabaseConfig,
  debug: Boolean
)

object AppConfig:
  import Environment.*

  def load(env: Environment): AppConfig = env match
    case Development =>
      AppConfig(
        environment = Development,
        database = DatabaseConfig("localhost", 5432, "dev_db", 5),
        debug = true
      )
    case Staging =>
      AppConfig(
        environment = Staging,
        database = DatabaseConfig("staging.db.internal", 5432, "staging_db", 20),
        debug = true
      )
    case Production =>
      AppConfig(
        environment = Production,
        database = DatabaseConfig("prod.db.internal", 5432, "prod_db", 100),
        debug = false
      )

  def fromString(envStr: String): Either[String, AppConfig] =
    envStr.toLowerCase match
      case "dev" | "development" => Right(load(Development))
      case "staging" => Right(load(Staging))
      case "prod" | "production" => Right(load(Production))
      case _ => Left(s"Unknown environment: $envStr")

// 사용 예시
val config = AppConfig.fromString("production")

config match
  case Right(cfg) =>
    println(s"환경: ${cfg.environment}")
    println(s"DB 호스트: ${cfg.database.host}")
    println(s"디버그 모드: ${cfg.debug}")
  case Left(error) =>
    println(s"설정 로드 실패: $error")
```

Environment enum으로 가능한 환경을 열거하고, 각 환경에 대해 적절한 설정을 패턴 매칭으로 반환합니다. fromString 메서드는 문자열 입력을 받아 Either로 결과를 반환하므로 잘못된 환경 문자열도 안전하게 처리됩니다.

{{% notice style="tip" title="핵심 포인트" %}}
- **enum**: 가능한 값들을 타입으로 열거하여 컴파일 타임 안전성 확보
- **패턴 매칭**: 각 환경에 맞는 설정을 명확하게 분기
- **Either 반환**: 잘못된 입력을 안전하게 처리, 에러 메시지 전달 가능
{{% /notice %}}

#### 연습 과제

다음 과제들을 직접 구현해보면서 Scala 실력을 향상시켜 보세요.

1. **재고 관리 추가** ⭐: `Product`에 `stock` 필드를 추가하고, 재고 확인 로직을 구현하세요. 재고가 부족하면 주문이 생성되지 않도록 합니다.

2. **주문 상태** ⭐⭐: `Order`에 상태(PENDING, CONFIRMED, SHIPPED)를 추가하세요. enum을 사용하여 정의하고, 상태 전이 로직도 구현해보세요.

3. **검색 기능** ⭐⭐: 가격 범위와 이름으로 상품을 검색하는 함수를 구현하세요. 여러 조건을 조합할 수 있는 검색 기능을 만들어봅니다.

> 💡 연습 과제 해답은 [Scastie](https://scastie.scala-lang.org/)에서 직접 구현하고 테스트해보세요!

#### 다음 단계

기본 예제를 학습했다면 Scala 2와 3의 문법 차이를 비교해보세요.

- [Scala 2 vs 3 비교](../scala2-vs-scala3/) — 버전별 코드 비교

